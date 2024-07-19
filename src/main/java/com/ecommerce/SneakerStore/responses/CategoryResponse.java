package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.Category;
import com.ecommerce.SneakerStore.services.CategoryService;
import lombok.*;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private int productCount;
    public static CategoryResponse fromCategory(Category category){
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
