package com.ecommerce.SneakerStore.utils;


import com.ecommerce.SneakerStore.entities.Cart;
import com.ecommerce.SneakerStore.entities.Order;
import com.ecommerce.SneakerStore.entities.OrderDetail;

import java.text.DecimalFormat;

public class BuilderEmailContent {
    public static String buildOrderEmailContent(Order order) {
        StringBuilder content = new StringBuilder();

        content.append("<html><body>");
        content.append("<h1>Đặt hàng thành công từ Sneaker Store - Đơn hàng #" + order.getId() + "</h1>");
        content.append("<p>Chào " + order.getFullName() + ",</p>");
        content.append("<p>Chúng tôi xin gửi lời cảm ơn chân thành đến bạn vì đã đặt hàng tại Sneaker Store.</p>");
        content.append("<p>Đơn hàng của bạn đã được xác nhận và đang được chuẩn bị để giao đến bạn.</p>");
        content.append("<p><strong>Địa chỉ nhận hàng của bạn:</strong> " + order.getAddress() + "</p>");
        content.append("<p><strong>Số điện thoại liên hệ:</strong> " + order.getPhoneNumber() + "</p>");
        content.append("<h2>Thông tin đơn hàng:</h2>");

        // Thêm danh sách sản phẩm trong đơn hàng (nếu có)
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            content.append("<ul>");
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                content.append("<li>");
                content.append("<p><strong>Tên sản phẩm:</strong> " + orderDetail.getProduct().getName() + "</p>");
                content.append("<p><strong>Kích cỡ:</strong> " + orderDetail.getSize() + "</p>");
                content.append("<p><strong>Số lượng:</strong> " + orderDetail.getNumberOfProducts() + "</p>");
                // Sử dụng DecimalFormat để định dạng số có dấu phân cách ","
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                String formattedPrice = decimalFormat.format(orderDetail.getPrice());
                content.append("<p><strong>Đơn giá:</strong> " + formattedPrice + "đ" +"</p>");
                content.append("</li>");
            }
            content.append("</ul>");
        }
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        long subTotal = 0L;
        for(OrderDetail orderDetail : order.getOrderDetails()){
            subTotal += orderDetail.getPrice() * orderDetail.getNumberOfProducts();
        }

        content.append("<p><strong>Tổng phụ:</strong> " + decimalFormat.format(subTotal) + "đ" + "</p>");


        String shippingMethod = order.getShippingMethod();
        long shippingCost = switch (shippingMethod) {
            case "Tiêu Chuẩn" -> 30000;
            case "Nhanh" -> 40000;
            case "Hoả Tốc" -> 50000;
            default -> 0;
        };
        content.append("<p><strong>Phí vận chuyển:</strong> " + decimalFormat.format(shippingCost) + "đ" + "</p>");

        // Định dạng tổng tiền cần thanh toán cũng có dấu phân cách ","
        String formattedTotalMoney = decimalFormat.format(order.getTotalMoney());
        content.append("<p><strong>Tổng tiền cần thanh toán:</strong> " + formattedTotalMoney + "đ" + "</p>");

        content.append("<p>Nếu có bất kỳ thắc mắc hoặc vấn đề nào khác, đừng ngần ngại liên hệ với chúng tôi qua địa chỉ email này hoặc số điện thoại hỗ trợ của chúng tôi.</p>");
        content.append("<p>Chúng tôi rất mong bạn sẽ hài lòng với sản phẩm của mình. Cảm ơn bạn đã lựa chọn Sneaker Store!</p>");
        content.append("<p>Trân trọng,</p>");
        content.append("<p>Sneaker Store</p>");
        content.append("</body></html>");

        return content.toString();
    }
}