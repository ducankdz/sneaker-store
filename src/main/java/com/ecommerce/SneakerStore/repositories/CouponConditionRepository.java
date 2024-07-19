package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.CouponCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponConditionRepository extends JpaRepository<CouponCondition,Long> {
    List<CouponCondition> findByCouponId(Long couponId);
}
