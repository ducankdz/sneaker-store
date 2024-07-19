package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Order;
import com.ecommerce.SneakerStore.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUser(User user);

    @Query("SELECT SUM(o.totalMoney) FROM Order o")
    long totalEarning();

    @Query("SELECT COUNT(o) FROM Order o WHERE MONTH(o.orderDate) = :month")
    long countOrdersByMonth(int month);

    @Query("SELECT IFNULL(SUM(o.totalMoney),0) FROM Order o WHERE MONTH(o.orderDate) = :month")
    long getEarningByMonth(int month);

    @Query("SELECT o FROM Order o ORDER BY o.totalMoney DESC")
    List<Order> getTopOrders(Pageable pageable);
}
