package com.ecommerce.SneakerStore.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDTO {
    @Min(value = 1,message = "Product's id must be > 0")
    private Long productId;

    @Size(min = 5,max = 200,message = "Image's name must be between 3 and 200 characters")
    private String imageUrl;
}