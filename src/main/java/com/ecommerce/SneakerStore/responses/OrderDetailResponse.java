package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.Order;
import com.ecommerce.SneakerStore.entities.OrderDetail;
import com.ecommerce.SneakerStore.entities.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.text.DecimalFormat;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private Long id;

    private Order order;

    private ProductResponse product;

    private String price;

    private Long numberOfProducts;

    private String totalMoney;

    private Long size;

    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail){
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return OrderDetailResponse.builder()
                .id(orderDetail.getId())
                .product(ProductResponse.fromProduct(orderDetail.getProduct()))
                .order(orderDetail.getOrder())
                .price(decimalFormat.format(orderDetail.getPrice())+'đ')
                .numberOfProducts(orderDetail.getNumberOfProducts())
                .totalMoney(decimalFormat.format(orderDetail.getTotalMoney())+'đ')
                .size(orderDetail.getSize())
                .build();
    }
}
