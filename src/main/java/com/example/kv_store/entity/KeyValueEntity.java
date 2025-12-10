package com.example.kv_store.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "kv_store")
@Data
public class KeyValueEntity {

    @Id
    @Column(name = "k")
    private String key;

    @Lob
    @Column(name = "v", nullable = false)
    private byte[] value;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "partition_id", nullable = false)
    private int partitionId;

    public boolean isExpired() {
        return expireAt != null && expireAt.isBefore(LocalDateTime.now());
    }
}
