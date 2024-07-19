package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.CartItemDTO;
import com.ecommerce.SneakerStore.dtos.OrderDTO;
import com.ecommerce.SneakerStore.entities.*;
import com.ecommerce.SneakerStore.repositories.CouponRepository;
import com.ecommerce.SneakerStore.repositories.OrderDetailRepository;
import com.ecommerce.SneakerStore.repositories.OrderRepository;
import com.ecommerce.SneakerStore.responses.OrderResponse;
import com.ecommerce.SneakerStore.responses.ProductSaleResponse;
import com.ecommerce.SneakerStore.utils.BuilderEmailContent;
import com.ecommerce.SneakerStore.utils.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CouponRepository couponRepository;
    private final UserService userService;
    private final CartService cartService;
    private final CouponService couponService;
    public Order createOrder(OrderDTO orderDTO, String token) throws Exception {
        User user = userService.getUserFromToken(token);
        long finalTotal = cartService.convertStringToLong(orderDTO.getTotalMoney());
        Coupon coupon = null;

        if(!orderDTO.getCode().equals("")){
            finalTotal = couponService.calculateCouponValue(cartService.getCartByUser(token).size(),finalTotal,orderDTO.getCode());
            coupon = couponRepository.findByCode(orderDTO.getCode()).orElse(null);
        }
        Order order = Order.builder()
                .fullName(orderDTO.getFullName())
                .user(user)
                .orderDate(LocalDate.now())
                .status(OrderStatus.PENDING)
                .email(orderDTO.getEmail())
                .phoneNumber(orderDTO.getPhoneNumber())
                .address(orderDTO.getAddress())
                .paymentMethod(orderDTO.getPaymentMethod())
                .paymentStatus(orderDTO.getPaymentStatus())
                .active(true)
                .shippingDate(LocalDate.now().plusDays(3))
                .note(orderDTO.getNote())
                .shippingMethod(orderDTO.getShippingMethod())
                .totalMoney(finalTotal)
                .coupon(coupon)
                .build();
        orderRepository.save(order);
        List<OrderDetail> orderDetails = new ArrayList<>();
        List<Cart> carts = cartService.findCartByUserId(user.getId());
        for(Cart cart : carts){
            long sellPrice = (long) (cart.getProduct().getPrice() * (1- cart.getProduct().getDiscount()));

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(cart.getProduct());
            orderDetail.setPrice(sellPrice);
            orderDetail.setNumberOfProducts(cart.getQuantity());
            orderDetail.setTotalMoney(sellPrice * cart.getQuantity());
            orderDetail.setSize(cart.getSize());

            orderDetails.add(orderDetail);
        }
        order.setOrderDetails(orderDetails);
        orderDetailRepository.saveAll(orderDetails);

        Email email = new Email();

        String to = order.getEmail();
        String subject = "Đặt hàng thành công từ Sneaker Store - Đơn hàng #" + order.getId();
        String content = BuilderEmailContent.buildOrderEmailContent(order);

        boolean sendEmail = email.sendEmail(to,subject,content);

        if(!sendEmail){
            throw new Exception("Cannot send email");
        }

        return order;
    }
    public List<Order> getOrdersByUser(String token) throws Exception {
        User user = userService.getUserFromToken(token);
        return orderRepository.findByUser(user);
    }
    public Order findOrderById(Long id){
        return orderRepository.findById(id).orElse(null);
    }
    public OrderResponse getOrderById(String token, Long id) throws Exception {

        Order order = orderRepository.findById(id).orElseThrow(
                () -> new Exception("Cannot find order with id = " + id));

        User user = userService.getUserFromToken(token);
        if(!user.getId().equals(order.getUser().getId())){
            throw new Exception("Cannot see order of another user");
        }
        return OrderResponse.fromOrder(order);
    }
    public String getOrderSubTotal(Long id) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new Exception("Cannot find order with id = " + id));
        long subTotal = 0;
        for(OrderDetail orderDetail : order.getOrderDetails()){
            subTotal += orderDetail.getPrice() * orderDetail.getNumberOfProducts();
        }
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(subTotal) + 'đ';
    }

    public String getOrderShippingCost(Long id) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new Exception("Cannot find order with id = " + id));
        return switch (order.getShippingMethod()) {
            case "Tiêu Chuẩn" -> "30,000đ";
            case "Nhanh" -> "40,000đ";
            case "Hoả Tốc" -> "50,000đ";
            default -> "";
        };
    }
    public void rebuy(Long id, String token) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new Exception("Cannot find order with id = " + id));
        User user = userService.getUserFromToken(token);
        if(!user.getId().equals(order.getUser().getId())){
            throw new Exception("Cannot see order of another user");
        }
        List<Cart> carts = new ArrayList<>();
        for(OrderDetail orderDetail : order.getOrderDetails()){
            CartItemDTO cartItemDTO = CartItemDTO.builder()
                    .productId(orderDetail.getProduct().getId())
                    .quantity(orderDetail.getNumberOfProducts())
                    .size(orderDetail.getSize())
                    .build();
            Cart cart = cartService.addToCart(cartItemDTO,token);
        }
    }
    public Order updateOrder(Long id, String status, String paymentStatus) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow(() ->
                new Exception("Cannot find order with id =" + id));
        order.setStatus(status);
        order.setPaymentStatus(paymentStatus);
        return orderRepository.save(order);
    }
    public List<OrderResponse> getOrders(){
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    public String totalEarning(){
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(orderRepository.totalEarning()) + 'đ';
    }

    public Map<String, Object> getOrdersGrowthByMonth(){
        return Map.of(
                "months",List.of("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5",
                        "Tháng 6", "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"),
                "orderCounts",List.of(orderRepository.countOrdersByMonth(1),
                        orderRepository.countOrdersByMonth(2),
                        orderRepository.countOrdersByMonth(3),
                        orderRepository.countOrdersByMonth(4),
                        orderRepository.countOrdersByMonth(5),
                        orderRepository.countOrdersByMonth(6),
                        orderRepository.countOrdersByMonth(7),
                        orderRepository.countOrdersByMonth(8),
                        orderRepository.countOrdersByMonth(9),
                        orderRepository.countOrdersByMonth(10),
                        orderRepository.countOrdersByMonth(11),
                        orderRepository.countOrdersByMonth(12)));
    }
    public Map<String, Object> getEarningByMonth(){
        return Map.of(
                "months",List.of("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5",
                        "Tháng 6", "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"),
                "earnings",List.of(orderRepository.getEarningByMonth(1),
                        orderRepository.getEarningByMonth(2),
                        orderRepository.getEarningByMonth(3),
                        orderRepository.getEarningByMonth(4),
                        orderRepository.getEarningByMonth(5),
                        orderRepository.getEarningByMonth(6),
                        orderRepository.getEarningByMonth(7),
                        orderRepository.getEarningByMonth(8),
                        orderRepository.getEarningByMonth(9),
                        orderRepository.getEarningByMonth(10),
                        orderRepository.getEarningByMonth(11),
                        orderRepository.getEarningByMonth(12)));
    }
    public List<Order> getTopOrders(int limit) {
        Pageable pageable = PageRequest.of(0,limit);
        return orderRepository.getTopOrders(pageable);
    }
}
