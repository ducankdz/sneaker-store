package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.Product;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSaleResponse {
    private Product product;
    private long quantity;
}
