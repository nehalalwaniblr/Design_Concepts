package com.example.kv_store.controller;

import com.example.kv_store.service.DistributedKeyValueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/internal/kv")
public class InternalKVController {

    private final DistributedKeyValueService service;

    public InternalKVController(DistributedKeyValueService service) {
        this.service = service;
    }

    @GetMapping("/{k}")
    public ResponseEntity<byte[]> getLocal(@PathVariable("k") String key) {
        return service.internalGetLocalOnly(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{k}")
    public ResponseEntity<Void> putLocal(@PathVariable("k") String key,
                                         @RequestParam(required = false) Long ttlSec,
                                         @RequestBody byte[] value) {
        Duration ttl = ttlSec == null ? null : Duration.ofSeconds(ttlSec);
        service.internalPutLocalOnly(key, value, ttl);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{k}")
    public ResponseEntity<Void> deleteLocal(@PathVariable("k") String key) {
        service.internalDeleteLocalOnly(key);
        return ResponseEntity.noContent().build();
    }
}

