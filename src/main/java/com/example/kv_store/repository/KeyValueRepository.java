package com.example.kv_store.repository;

import com.example.kv_store.entity.KeyValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeyValueRepository extends JpaRepository<KeyValueEntity, String> {

    @Query("SELECT k FROM KeyValueEntity k WHERE k.key = :key AND (k.expireAt IS NULL OR k.expireAt > CURRENT_TIMESTAMP)")
    Optional<KeyValueEntity> findValid(@Param("key") String key);

//    @Modifying
//    @Query("DELETE FROM KeyValueEntity e WHERE e.expireAt IS NOT NULL AND e.expireAt <= CURRENT_TIMESTAMP")
//    int deleteExpired();
}
