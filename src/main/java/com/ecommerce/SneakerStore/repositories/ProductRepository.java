package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.responses.ProductSaleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query("SELECT p FROM Product p " +
            "WHERE (:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId)")
    Page<Product> getProductsByCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE (:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId)")
    List<Product> getAllProductsByCategory(
            @Param("categoryId") Long categoryId);
    @Query("SELECT p FROM Product p " +
            "ORDER BY p.discount DESC")
    List<Product> getProductsWithHighestDiscount();
    @Query("SELECT p FROM Product p " +
            "WHERE (:keyword IS NULL OR :keyword = '' " +
            "OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> getProductsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p \n" +
            "FROM Product  p \n" +
            "LEFT JOIN Comment c\n" +
            "ON p.id = c.product.id\n" +
            "GROUP BY p.id\n" +
            "ORDER BY AVG(c.star) DESC")
    Page<Product> getProductByRating(Pageable pageable);

    @Query("SELECT p \n" +
            "FROM Product  p \n" +
            "LEFT JOIN OrderDetail o\n" +
            "ON p.id = o.product.id\n" +
            "GROUP BY p.id\n" +
            "ORDER BY SUM(o.numberOfProducts) DESC")
    Page<Product> getProductByPopularity(Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> getProductsByPrice(
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            Pageable pageable);

    @Query("SELECT SUM(od.numberOfProducts) FROM OrderDetail od")
    long countProductsSold();

    @Query("SELECT IFNULL(SUM(od.numberOfProducts),0) " +
            "FROM Order o, OrderDetail od " +
            "WHERE o.id = od.order.id " +
            "AND MONTH(o.orderDate) = :month")
    long countProductsSoldByMonth(int month);

    @Query("SELECT new com.ecommerce.SneakerStore.responses.ProductSaleResponse(p, SUM(od.numberOfProducts)) " +
            "FROM OrderDetail od JOIN od.product p " +
            "GROUP BY p.id, p.name " +
            "ORDER BY SUM(od.numberOfProducts) DESC")
    List<ProductSaleResponse> findTopSellingProducts(Pageable pageable);
}
