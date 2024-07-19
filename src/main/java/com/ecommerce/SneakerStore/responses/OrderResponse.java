package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.Coupon;
import com.ecommerce.SneakerStore.entities.Order;
import com.ecommerce.SneakerStore.entities.OrderDetail;
import com.ecommerce.SneakerStore.entities.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse{
    private Long id;

    private User user;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String address;

    private String note;

    private LocalDate orderDate;

    private String status;

    private String totalMoney;

    private String shippingMethod;

    private LocalDate shippingDate;

    private String paymentMethod;

    private String paymentStatus;

    private Boolean active;

    private List<OrderDetailResponse> orderDetails;

    private Coupon coupon;

    public static OrderResponse fromOrder(Order order){
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return OrderResponse.builder()
                .id(order.getId())
                .active(order.getActive())
                .orderDate(order.getOrderDate())
                .email(order.getEmail())
                .address(order.getAddress())
                .fullName(order.getFullName())
                .phoneNumber(order.getPhoneNumber())
                .paymentMethod(order.getPaymentMethod())
                .note(order.getNote())
                .shippingDate(order.getShippingDate())
                .shippingMethod(order.getShippingMethod())
                .totalMoney(decimalFormat.format(order.getTotalMoney()) + "đ")
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .user(order.getUser())
                .coupon(order.getCoupon())
                .orderDetails(order.getOrderDetails().stream().map(OrderDetailResponse::fromOrderDetail).collect(Collectors.toList()))
                .build();
    }
}
