package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    int countProductsByCategoryId(@Param("categoryId") Long categoryId);

    boolean existsByName(String name);

    @Query("SELECT COUNT(c)>0 FROM Category c WHERE c.name = :name AND c.id <> :id")
    boolean existsByNameOfOtherCategories(String name, Long id);

    @Query("SELECT IFNULL(SUM(od.numberOfProducts),0) FROM OrderDetail od, Product p WHERE od.product.id = p.id " +
            "AND p.category.id = :categoryId")
    long countProductsSoldByCategoryId(Long categoryId);
}
