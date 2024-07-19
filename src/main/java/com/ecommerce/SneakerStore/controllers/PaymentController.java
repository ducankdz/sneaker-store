package com.ecommerce.SneakerStore.controllers;

import com.ecommerce.SneakerStore.configurations.VNPayConfig;
import com.ecommerce.SneakerStore.dtos.OrderDTO;
import com.ecommerce.SneakerStore.entities.Order;
import com.ecommerce.SneakerStore.entities.PaymentStatus;
import com.ecommerce.SneakerStore.services.CartService;
import com.ecommerce.SneakerStore.services.OrderService;
import com.ecommerce.SneakerStore.services.PaypalService;
import com.ecommerce.SneakerStore.services.VNPayService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaypalService paypalService;

    @GetMapping("/vnpay")
    public String vnPayCompleted(HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   @RequestParam("vnp_ResponseCode") String vnp_ResponseCode,
                                   @CookieValue("authToken") String token) throws Exception {
        OrderDTO orderDTO = (OrderDTO) session.getAttribute("orderDTO");

        if(vnp_ResponseCode.equals("00")){
            orderDTO.setPaymentStatus(PaymentStatus.YES);
            Order order = orderService.createOrder(orderDTO,token);
            cartService.deleteCartByUser(token);
            redirectAttributes.addFlashAttribute("message",
                    "Đặt hàng thành công");

            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("message",
                "Đặt hàng không thành công");
        return "redirect:/404";
    }

    @GetMapping("/paypal")
    public String paypalCompleted(@RequestParam("paymentId") String paymentId,
                                  @RequestParam("PayerID") String payerId,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session,
                                  @CookieValue("authToken") String token
                                  ) throws Exception {
        Payment payment = paypalService.executePayment(paymentId, payerId);
        OrderDTO orderDTO = (OrderDTO) session.getAttribute("orderDTO");

        if(payment.getState().equals("approved")){
            orderDTO.setPaymentStatus(PaymentStatus.YES);
            Order order = orderService.createOrder(orderDTO,token);
            cartService.deleteCartByUser(token);
            redirectAttributes.addFlashAttribute("message",
                    "Đặt hàng thành công");

            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("message",
                "Đặt hàng không thành công");
        return "redirect:/404";
    }
}
