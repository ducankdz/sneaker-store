package com.ecommerce.SneakerStore.dtos;

import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String totalMoney;
    private String status;
    private String shippingMethod;
    private String note;
    private String paymentMethod;
    private String paymentStatus;
    private String code;
}
