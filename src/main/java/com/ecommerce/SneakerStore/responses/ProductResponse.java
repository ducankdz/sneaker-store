package com.ecommerce.SneakerStore.responses;

import com.ecommerce.SneakerStore.entities.Category;
import com.ecommerce.SneakerStore.entities.Comment;
import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.ProductImage;
import lombok.*;

import java.text.DecimalFormat;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse extends BaseResponse{
    private Long id;

    private String name;

    private String price;

    private String thumbnail;

    private String description;

    private Category category;

    private double discount;

    private double avg;

    private String sellPrice;

    private List<ProductImage> productImages;

    private List<Comment> comments;

    public static ProductResponse fromProduct(Product product){
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        long sellPrice = (long) (product.getPrice() * (1- product.getDiscount()));
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .category(product.getCategory())
                .discount(product.getDiscount())
                .productImages(product.getProductImages())
                .price(decimalFormat.format(product.getPrice()) + "đ")
                .comments(product.getComments())
                .sellPrice(decimalFormat.format(sellPrice) + 'đ')
                .build();
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());

        // Tính trung bình số sao từ danh sách comment
        if (product.getComments() != null && !product.getComments().isEmpty()) {
            double totalStars = 0;
            for (Comment comment : product.getComments()) {
                totalStars += comment.getStar();
            }
            double avgStars = totalStars / product.getComments().size();
            double roundedAvg = Double.parseDouble(String.format("%.1f", avgStars));
            productResponse.setAvg(roundedAvg);
        } else {
            productResponse.setAvg(0); // Nếu không có comment, avg được set là 0
        }
        return productResponse;
    }
}
