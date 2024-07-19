package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Comment;
import com.ecommerce.SneakerStore.entities.Reaction;
import com.ecommerce.SneakerStore.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction,Long> {
    Reaction findByCommentAndUser(Comment comment, User user);
    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.id = :id")
    void deleteSingleReaction(Long id);
}
