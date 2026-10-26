package com.aranthangimart.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByPhoneOrderByCreatedAtDesc(String phone);
    Optional<OrderEntity> findByIdAndPhone(Long id, String phone);
}
