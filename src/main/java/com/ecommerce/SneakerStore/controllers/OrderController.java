package com.ecommerce.SneakerStore.controllers;

import com.ecommerce.SneakerStore.dtos.OrderDTO;
import com.ecommerce.SneakerStore.entities.Order;
import com.ecommerce.SneakerStore.entities.PaymentStatus;
import com.ecommerce.SneakerStore.responses.OrderDetailResponse;
import com.ecommerce.SneakerStore.responses.OrderResponse;
import com.ecommerce.SneakerStore.services.*;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final VNPayService vnPayService;
    private final PaypalService paypalService;
    private final CouponService couponService;

    @PostMapping("/create")
    public String createOrder(HttpServletRequest request,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              @Valid @ModelAttribute OrderDTO orderDTO,
                              @CookieValue("authToken") String token){
        try {
            long finalTotal = cartService.convertStringToLong(orderDTO.getTotalMoney());

            if(!orderDTO.getCode().equals("")){
                finalTotal = couponService.calculateCouponValue(cartService.getCartByUser(token).size(),finalTotal,orderDTO.getCode());
            }
            switch (orderDTO.getPaymentMethod()) {
                case "COD" -> {
                    orderDTO.setPaymentStatus(PaymentStatus.NO);
                    Order order = orderService.createOrder(orderDTO, token);
                    cartService.deleteCartByUser(token);
                    redirectAttributes.addFlashAttribute("message", "Đặt hàng thành công");
                    return "redirect:/";
                }
                case "VNPAY" -> {
                    String vnPayUrl = vnPayService.createPayment(request,finalTotal);
                    session.setAttribute("orderDTO", orderDTO);
                    return "redirect:" + vnPayUrl;
                }
                case "PAYPAL" -> {
                    Payment payment = paypalService.createPayment(
                            paypalService.convertVNDtoUSD(finalTotal),
                            "USD",
                            "paypal",
                            "sale",
                            orderDTO.getNote(),
                            "http://localhost:8087/products",
                            "http://localhost:8087/payment/paypal"
                    );
                    session.setAttribute("orderDTO", orderDTO);
                    for (Links link : payment.getLinks()) {
                        if (link.getRel().equals("approval_url")) {
                            return "redirect:" + link.getHref();
                        }
                    }
                }
            }
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message", "Đặt hàng không thành công");
            return "redirect:/404";
        }
        return "redirect:/404";
    }
    @GetMapping("/user/{userId}")
    public String getOrderByUser(Model model,
                                 @PathVariable("userId") Long userId,
                                 @CookieValue("authToken") String token) throws Exception {

        List<OrderResponse> orders = orderService
                .getOrdersByUser(token)
                .stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
        Collections.reverse(orders);
        model.addAttribute("orders",orders);
        return "order-history";
    }
    @GetMapping("/{id}")
    public String getOrderById(Model model,
                               @CookieValue("authToken") String token,
                               @PathVariable("id") Long id) throws Exception {
        OrderResponse orderResponse = orderService.getOrderById(token,id);
//        List<OrderDetailResponse> orderDetailResponse = orderDetailService.getOrderDetailsByOrderId(id);
//        orderResponse.setOrderDetails(orderDetailResponse);

        model.addAttribute("order",orderResponse);
        model.addAttribute("subTotal",orderService
                .getOrderSubTotal(orderResponse.getId()));
        model.addAttribute("shippingCost",orderService
                .getOrderShippingCost(orderResponse.getId()));
        Order order = orderService.findOrderById(id);
        if(order.getCoupon() != null){
            long discountAmount = couponService.calculateDiscountedAmount(order,order.getCoupon().getConditions());
            DecimalFormat df = new DecimalFormat("#,###");
            model.addAttribute("discountAmount",df.format(discountAmount)+'đ');
        }
        return "order-detail";
    }

    @GetMapping("/rebuy/{id}")
    public String rebuy(RedirectAttributes redirectAttributes,
                        @CookieValue("authToken") String token,
                        @PathVariable("id") Long id) throws Exception {
        orderService.rebuy(id,token);
        redirectAttributes.addFlashAttribute("message",
                "Đã thêm các sản phẩm trong đơn hàng vào giỏ hàng");
        return "redirect:/cart";
    }
}
