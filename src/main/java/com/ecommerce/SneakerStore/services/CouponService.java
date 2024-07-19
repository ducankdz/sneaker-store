package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.CouponConditionDTO;
import com.ecommerce.SneakerStore.dtos.CouponDTO;
import com.ecommerce.SneakerStore.entities.Coupon;
import com.ecommerce.SneakerStore.entities.CouponCondition;
import com.ecommerce.SneakerStore.entities.Order;
import com.ecommerce.SneakerStore.repositories.CouponConditionRepository;
import com.ecommerce.SneakerStore.repositories.CouponRepository;
import com.ecommerce.SneakerStore.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponConditionRepository couponConditionRepository;

    public Coupon addCoupon(CouponDTO couponDTO) throws Exception {
        if(couponRepository.existsByCode(couponDTO.getCode())){
            throw new Exception("Code existed");
        }
        Coupon coupon = Coupon.builder()
                .code(couponDTO.getCode())
                .active(true)
                .build();
        couponRepository.save(coupon);

        List<CouponCondition> conditions = new ArrayList<>();
        for(CouponConditionDTO conditon : couponDTO.getCouponConditions()){
            String attribute = conditon.getAttribute();
            String operator = conditon.getOperator();
            String value = conditon.getValue();

            // Bỏ qua điều kiện nếu value rỗng
            if (value.equals("") || value.trim().equals("")) {
                continue;
            }

            double discount = Double.parseDouble(conditon.getDiscount());
            
            CouponCondition couponCondition = CouponCondition
                    .builder()
                    .coupon(coupon)
                    .attribute(attribute)
                    .operator(operator)
                    .value(value)
                    .discount(discount)
                    .build();
            conditions.add(couponCondition);
        }
        coupon.setConditions(conditions);
        couponConditionRepository.saveAll(conditions);
        return coupon;
    }

    public Long calculateCouponValue(long quantity,Long total, String code) throws Exception {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new Exception(
                        "Cannot find coupon with code = " + code));
        if(!coupon.isActive()){
            throw new Exception("Coupon is not active");
        }
        Long discount = calculateDiscount(coupon,total,quantity);
        return total-discount;
    }

    private Long calculateDiscount(Coupon coupon,Long total,long quantity){
        List<CouponCondition> conditons = couponConditionRepository
                .findByCouponId(coupon.getId());
        double discount = 0;
        double newTotal = (double) total;
        for(CouponCondition condition : conditons){
            String attribute = condition.getAttribute();
            String operator = condition.getOperator();
            String value = condition.getValue();
            double discountPercent = condition.getDiscount();

            if(attribute.equals("total_money")){
                if(operator.equals(">") && newTotal>Double.parseDouble(value)){
                    discount += newTotal * discountPercent;
                }
                else if(operator.equals("<") && newTotal<Double.parseDouble(value)){
                    discount += newTotal * discountPercent;
                }
                else if(operator.equals("=") && newTotal==Double.parseDouble(value)){
                    discount += newTotal * discountPercent;
                }
            }
            else if(attribute.equals("applicable_date")){
                LocalDate applicableDate = LocalDate.parse(value);
                LocalDate currentDate = LocalDate.now();
                if(operator.equals("<") && currentDate.isBefore(applicableDate)){
                    discount += newTotal * discountPercent;
                }
                else if(operator.equals(">") && currentDate.isAfter(applicableDate)){
                    discount += newTotal * discountPercent;
                }
                else if(operator.equals("=") && currentDate.equals(applicableDate)){
                    discount += newTotal * discountPercent;
                }
            }
            else if (attribute.equals("quantity")){
                if(operator.equals(">") && quantity > Long.parseLong(value)){
                    discount += newTotal * discountPercent;
                }
                else if(operator.equals("<") && quantity < Long.parseLong(value)){
                    discount += newTotal * discountPercent;
                }
                else if(operator.equals("=") && quantity == Long.parseLong(value)){
                    discount += newTotal * discountPercent;
                }
            }
            newTotal -= discount;
        }
        return (long) discount;
    }
    public List<Coupon> getCoupons(){
        return couponRepository.findAll();
    }
    public Coupon updateCoupon(Long id, int active) throws Exception {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new Exception("Role does not exist"));
        coupon.setActive(active == 1);
        return couponRepository.save(coupon);
    }
    public void deleteCoupon(Long id){
        couponRepository.deleteById(id);
    }
    public long calculateDiscountedAmount(Order order, List<CouponCondition> conditions) {
        double totalMoney = order.getTotalMoney();
        double initialTotalMoney = totalMoney;

        for (CouponCondition condition : conditions) {
            initialTotalMoney /= (1 - condition.getDiscount());
        }

        return (long )(initialTotalMoney - totalMoney);
    }
}
