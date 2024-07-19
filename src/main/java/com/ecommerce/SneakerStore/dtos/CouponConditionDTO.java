package com.ecommerce.SneakerStore.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CouponConditionDTO {
    private String attribute;
    private String operator;
    private String value;
    private String discount;
}
