package com.example.custom.cache.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.custom.cache.entity.CacheEntity;

@Repository
public class MyCacheRepository {
    private static Map<String, CacheEntity> map = new HashMap<>();

    public Optional<CacheEntity> findById(String cacheKey){
        return Optional.ofNullable(map.get(cacheKey));
    }

    public void save(CacheEntity cacheEntity){
        map.put(cacheEntity.getId(), cacheEntity);
    }
    
}
