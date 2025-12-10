package com.example.kv_store.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AffinityService {

    private final String localNodeId;
    private final List<String> allNodes;  // ordered list
    private final int partitions;
    private final int backups;
    private final Map<String, String> nodeUrls;

    public AffinityService(
            @Value("${cluster.nodeId}") String localNodeId,
            @Value("${cluster.partitions}") int partitions,
            @Value("${cluster.nodes}") String nodesCsv,
            @Value("${cluster.backups:1}") int backups,
            Environment env
    ) {
        this.localNodeId = localNodeId;
        this.partitions = partitions;
        this.backups = backups;
        this.allNodes = Arrays.stream(nodesCsv.split(","))
                .map(String::trim)
                .toList();

        this.nodeUrls = new HashMap<>();
        for (String n : allNodes) {
            String url = env.getProperty("cluster.nodeUrls." + n);
            this.nodeUrls.put(n, url);
        }
    }

    public int partition(String key) {
        return Math.floorMod(key.hashCode(), partitions);
    }

    /** owners[0] = primary, others = backups */
    public List<String> owners(int partitionId) {
        // simple ring: rotate over allNodes
        int start = partitionId % allNodes.size();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < backups + 1; i++) {
            result.add(allNodes.get((start + i) % allNodes.size()));
        }
        return result;
    }

    public boolean isPrimaryOwner(String key) {
        int p = partition(key);
        List<String> owners = owners(p);
        return owners.get(0).equals(localNodeId);
    }

    public String primaryOwner(String key) {
        return owners(partition(key)).get(0);
    }

    public String urlOf(String nodeId) {
        return nodeUrls.get(nodeId);
    }

    public String getLocalNodeId() {
        return localNodeId;
    }
}
