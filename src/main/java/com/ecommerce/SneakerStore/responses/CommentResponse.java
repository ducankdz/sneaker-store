package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse extends BaseResponse{
    private Long id;

    private User user;

    private ProductResponse product;

    private String content;

    private int star;

    private double starPercent;

    private String duration;

    private List<Reaction> reactions;

    private int likeCount;

    private int dislikeCount;

    private boolean userHasLiked;

    private boolean userHasDisliked;

    public static CommentResponse fromComment(Comment comment){
        LocalDateTime now = LocalDateTime.now();
        long diffInSeconds = Duration.between(comment.getCreatedAt(),now).toSeconds();

        String dur;

        if (diffInSeconds < 60) {
            dur = diffInSeconds + " giây trước";
        } else if (diffInSeconds < 3600) {
            long minutes = diffInSeconds / 60;
            dur = minutes + " phút trước";
        } else if (diffInSeconds < 86400) {
            long hours = diffInSeconds / 3600;
            dur = hours + " giờ trước";
        } else {
            long days = diffInSeconds / 86400;
            dur = days + " ngày trước";
        }

        double percent = (double) comment.getStar()/5*100;

        int likeCnt = 0;
        int dislikeCnt = 0;

        for(Reaction reaction : comment.getReactions()){
            if(reaction.getReactionType().equals(ReactionType.LIKE)){
                likeCnt ++;
            } else if (reaction.getReactionType().equals(ReactionType.DISLIKE)) {
                dislikeCnt ++;
            }
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .user(comment.getUser())
                .product(ProductResponse.fromProduct(comment.getProduct()))
                .content(comment.getContent())
                .star(comment.getStar())
                .duration(dur)
                .starPercent(percent)
                .reactions(comment.getReactions())
                .likeCount(likeCnt)
                .dislikeCount(dislikeCnt)
                .build();
    }
}
