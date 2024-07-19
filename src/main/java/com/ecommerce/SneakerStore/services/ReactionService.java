package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.ReactionDTO;
import com.ecommerce.SneakerStore.entities.*;
import com.ecommerce.SneakerStore.repositories.CommentRepository;
import com.ecommerce.SneakerStore.repositories.ProductRepository;
import com.ecommerce.SneakerStore.repositories.ReactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;

    @Transactional
    public void addReaction(ReactionDTO reactionDTO,String token) throws Exception {
        User user = userService.getUserFromToken(token);

        Comment comment = commentRepository.findById(reactionDTO.getCommentId())
                .orElseThrow(() -> new Exception(
                        "Cannot find comment with id = " + reactionDTO.getCommentId()));

        String reactionType = reactionDTO.getReactionType().equals("Like")
                ? ReactionType.LIKE
                : ReactionType.DISLIKE;

        Reaction existingReaction = reactionRepository.findByCommentAndUser(comment,user);

        if (existingReaction != null) {
            if (existingReaction.getReactionType().equals(reactionType)) {
                deleteById(existingReaction.getId());
            } else {
                existingReaction.setReactionType(reactionType);
                reactionRepository.save(existingReaction);
            }
        }
        else{
            Reaction reaction = Reaction
                    .builder()
                    .comment(comment)
                    .user(user)
                    .reactionType(reactionType)
                    .build();

            reactionRepository.save(reaction);
        }
    }

    public void deleteById(Long id){
        if(reactionRepository.existsById(id)){
            reactionRepository.deleteSingleReaction(id);
        }
    }
}
