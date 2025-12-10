package com.example.kv_store.service;

import com.example.kv_store.entity.KeyValueEntity;
import com.example.kv_store.repository.KeyValueRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DistributedKeyValueService {

    private final KeyValueRepository repo;
    private final AffinityService affinity;
    private final WebClient webClient; // reactive HTTP client for forwarding

    public DistributedKeyValueService(KeyValueRepository repo,
                                      AffinityService affinity,
                                      WebClient.Builder webClientBuilder) {
        this.repo = repo;
        this.affinity = affinity;
        this.webClient = webClientBuilder.build();
    }

    // ---------- Public API (called by controller) ----------

    public Optional<byte[]> get(String key) {
        if (!affinity.isPrimaryOwner(key)) {
            return forwardGet(key);
        }
        return localGet(key);
    }

    public void put(String key, byte[] value, Duration ttl) {
        if (!affinity.isPrimaryOwner(key)) {
            forwardPut(key, value, ttl);
            return;
        }
        localPutAndReplicate(key, value, ttl);
    }

    public void delete(String key) {
        if (!affinity.isPrimaryOwner(key)) {
            forwardDelete(key);
            return;
        }
        localDeleteAndReplicate(key);
    }

    // ---------- Local operations ----------

    private Optional<byte[]> localGet(String key) {
        return repo.findValid(key)
                .filter(e -> !e.isExpired())
                .map(KeyValueEntity::getValue);
    }

    @Transactional
    protected void localPutAndReplicate(String key, byte[] value, Duration ttl) {
        int partitionId = affinity.partition(key);
        KeyValueEntity entity = new KeyValueEntity();
        entity.setKey(key);
        entity.setValue(value);
        entity.setPartitionId(partitionId);
        if (ttl != null) {
            entity.setExpireAt(LocalDateTime.now().plus(ttl));
        }
        repo.save(entity);

        // replicate to backups (best-effort)
        replicateToBackups("PUT", key, value, ttl);
    }

    @Transactional
    protected void localDeleteAndReplicate(String key) {
        repo.deleteById(key);
        replicateToBackups("DELETE", key, null, null);
    }

    // ---------- Replication to backups ----------

    private void replicateToBackups(String op, String key, byte[] value, Duration ttl) {
        List<String> owners = affinity.owners(affinity.partition(key));
        if (owners.size() <= 1) return; // no backups

        owners.stream()
                .skip(1) // skip primary
                .forEach(nodeId -> {
                    String baseUrl = affinity.urlOf(nodeId);
                    if (baseUrl == null) return;

                    if (op.equals("PUT")) {
                        webClient.post()
                                .uri(baseUrl + "/internal/kv/" + key + buildTtlQuery(ttl))
                                .bodyValue(value)
                                .retrieve()
                                .toBodilessEntity()
                                .subscribe(); // fire-and-forget
                    } else if (op.equals("DELETE")) {
                        webClient.delete()
                                .uri(baseUrl + "/internal/kv/" + key)
                                .retrieve()
                                .toBodilessEntity()
                                .subscribe();
                    }
                });
    }

    private String buildTtlQuery(Duration ttl) {
        if (ttl == null) return "";
        return "?ttlSec=" + ttl.toSeconds();
    }

    // ---------- Forwarding to primary ----------

    private Optional<byte[]> forwardGet(String key) {
        String nodeId = affinity.primaryOwner(key);
        String baseUrl = affinity.urlOf(nodeId);
        if (baseUrl == null) return Optional.empty();

        try {
            byte[] body = webClient.get()
                    .uri(baseUrl + "/internal/kv/" + key)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();  // can be made async later
            return Optional.ofNullable(body);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void forwardPut(String key, byte[] value, Duration ttl) {
        String nodeId = affinity.primaryOwner(key);
        String baseUrl = affinity.urlOf(nodeId);
        if (baseUrl == null) {
            throw new IllegalStateException("No URL for node " + nodeId);
        }

        webClient.post()
                .uri(baseUrl + "/internal/kv/" + key + buildTtlQuery(ttl))
                .bodyValue(value)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void forwardDelete(String key) {
        String nodeId = affinity.primaryOwner(key);
        String baseUrl = affinity.urlOf(nodeId);
        if (baseUrl == null) return;

        webClient.delete()
                .uri(baseUrl + "/internal/kv/" + key)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    // ---------- Internal APIs used across nodes ----------

    // These can be called by a controller mapped to /internal/kv
    public Optional<byte[]> internalGetLocalOnly(String key) {
        return localGet(key);
    }

    public void internalPutLocalOnly(String key, byte[] value, Duration ttl) {
        int partitionId = affinity.partition(key);
        KeyValueEntity entity = new KeyValueEntity();
        entity.setKey(key);
        entity.setValue(value);
        entity.setPartitionId(partitionId);
        if (ttl != null) entity.setExpireAt(LocalDateTime.now().plus(ttl));
        repo.save(entity);
    }

    public void internalDeleteLocalOnly(String key) {
        repo.deleteById(key);
    }
}


