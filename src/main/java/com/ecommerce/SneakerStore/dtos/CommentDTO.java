package com.ecommerce.SneakerStore.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommentDTO {
    private Long productId;
    private String content;
    private int star;
}
