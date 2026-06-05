package com.bank.service;

import com.bank.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    @Cacheable(value = "userSearch", key = "#name + '_' + #email + '_' + #phone + '_' + #dateOfBirth + '_' + #page + '_' + #size", unless = "#result == null")
    public List<UserResponse> cacheUserSearch(String name, String email, String phone, String dateOfBirth, int page, int size) {
        log.debug("Cache MISS for user search");
        return null;
    }

    @CacheEvict(value = "userSearch", allEntries = true)
    public void evictUserSearchCache() {
        log.info("User search cache evicted");
    }

    @Cacheable(value = "userById", key = "#userId", unless = "#result == null")
    public UserResponse cacheUserById(Long userId) {
        log.debug("Cache MISS for user ID: {}", userId);
        return null;
    }

    @CacheEvict(value = "userById", key = "#userId")
    public void evictUserByIdCache(Long userId) {
        log.info("User cache evicted for ID: {}", userId);
    }
}
