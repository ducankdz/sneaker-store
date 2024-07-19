package com.ecommerce.SneakerStore.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReactionDTO {
    private Long commentId;
    private String reactionType;
}
