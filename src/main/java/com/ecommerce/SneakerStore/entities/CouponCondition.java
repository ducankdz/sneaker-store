package com.ecommerce.SneakerStore.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coupon_conditions")
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    @JsonBackReference
    private Coupon coupon;

    @Column(name = "attribute")
    private String attribute;

    @Column(name = "operator")
    private String operator;

    @Column(name = "value")
    private String value;

    @Column(name = "discount")
    private double discount;
}
