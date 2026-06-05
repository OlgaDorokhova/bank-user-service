package com.bank.repository;

import com.bank.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

    Optional<PhoneData> findByPhone(String phone);

    boolean existsByPhone(String phone);

    void deleteByUserIdAndPhone(Long userId, String phone);
}
