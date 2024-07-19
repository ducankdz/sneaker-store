package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByProductId(Long productId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.star >= 3 AND MONTH(c.createdAt) = :month")
    long countPositiveCommentsByMonth(int month);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.star < 3 AND MONTH(c.createdAt) = :month")
    long countNegativeCommentsByMonth(int month);
}
