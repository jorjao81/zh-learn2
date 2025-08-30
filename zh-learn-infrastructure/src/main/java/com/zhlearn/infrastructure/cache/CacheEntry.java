package com.zhlearn.infrastructure.cache;

import java.io.Serializable;
import java.time.Instant;

public class CacheEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String response;
    private final Instant timestamp;
    
    public CacheEntry(String response, Instant timestamp) {
        this.response = response;
        this.timestamp = timestamp;
    }
    
    public String getResponse() {
        return response;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public boolean isExpired(Instant now, long maxAgeSeconds) {
        return now.isAfter(timestamp.plusSeconds(maxAgeSeconds));
    }
}