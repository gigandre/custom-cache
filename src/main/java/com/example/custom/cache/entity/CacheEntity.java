package com.example.custom.cache.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntity {
  private String id;
  private String cacheValue;
  private String objType;
  private LocalDateTime evictTime;

}