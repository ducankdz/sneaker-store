package com.ecommerce.SneakerStore.dtos;

import com.ecommerce.SneakerStore.entities.CouponCondition;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
public class CouponDTO {
    private String code;
    private List<CouponConditionDTO> couponConditions = new ArrayList<>(3);
    public CouponDTO() {
        // Add default CouponConditionDTO objects
        couponConditions.add(new CouponConditionDTO());
        couponConditions.add(new CouponConditionDTO());
        couponConditions.add(new CouponConditionDTO());

        couponConditions.get(0).setAttribute("total_money");
        couponConditions.get(1).setAttribute("applicable_date");
        couponConditions.get(2).setAttribute("quantity");
    }
}
