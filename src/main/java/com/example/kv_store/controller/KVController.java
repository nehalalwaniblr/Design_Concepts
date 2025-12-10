package com.example.kv_store.controller;

import com.example.kv_store.service.DistributedKeyValueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/kv")
public class KVController {

    private final DistributedKeyValueService service;

    public KVController(DistributedKeyValueService service) {
        this.service = service;
    }

    @GetMapping("/{k}")
    public ResponseEntity<byte[]> get(@PathVariable("k") String key) {
        return service.get(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{k}")
    public ResponseEntity<Void> put(@PathVariable("k") String key,
                                    @RequestParam(required = false) Long ttlSec,
                                    @RequestBody byte[] value) {
        Duration ttl = ttlSec == null ? null : Duration.ofSeconds(ttlSec);
        service.put(key, value, ttl);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{k}")
    public ResponseEntity<Void> delete(@PathVariable("k") String key) {
        service.delete(key);
        return ResponseEntity.noContent().build();
    }
}

