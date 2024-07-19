package com.ecommerce.SneakerStore.controllers;

import com.ecommerce.SneakerStore.dtos.CartItemDTO;
import com.ecommerce.SneakerStore.dtos.CommentDTO;
import com.ecommerce.SneakerStore.dtos.ProductDTO;
import com.ecommerce.SneakerStore.dtos.ReactionDTO;
import com.ecommerce.SneakerStore.entities.*;
import com.ecommerce.SneakerStore.responses.CategoryResponse;
import com.ecommerce.SneakerStore.responses.CommentResponse;
import com.ecommerce.SneakerStore.responses.ProductResponse;
import com.ecommerce.SneakerStore.services.CategoryService;
import com.ecommerce.SneakerStore.services.CommentService;
import com.ecommerce.SneakerStore.services.ProductService;
import com.ecommerce.SneakerStore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final UserService userService;


    @GetMapping("")
    public String getProducts(Model model,
                              @RequestParam(name = "page",defaultValue = "0") int page,
                              @RequestParam(name = "limit",defaultValue = "16") int limit){
        Pageable pageable = PageRequest.of(page,limit);
        Page<Product> productPage = productService.getProducts(pageable);
        model.addAttribute("products", productPage
                .getContent()
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList()));
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        model.addAttribute("categories",categories);
        model.addAttribute("limit",16);
        model.addAttribute("totalPages",productPage.getTotalPages());
        model.addAttribute("currentPage",page);
        model.addAttribute("title","Sản Phẩm");
        model.addAttribute("source", "products");
        model.addAttribute("cartItemDTO", new CartItemDTO());
        return "products";
    }
    @GetMapping("/{id}")
    public String getProductById(Model model,
                                 @PathVariable("id") Long id,
                                 @CookieValue(value = "authToken", required = false) String token) throws Exception {
        ProductResponse product = ProductResponse.fromProduct(productService.getProductById(id));
        List<ProductResponse> relatedProducts = productService.getRelatedProducts(id).stream().map(ProductResponse::fromProduct).collect(Collectors.toList());
        List<ProductResponse> newProducts = productService.newProducts().stream().map(ProductResponse::fromProduct).collect(Collectors.toList());
        List<CommentResponse> comments = commentService.getCommentsByProductId(id);

        String message = (String) model.asMap().get("message");

        long sum = 0;
        for (CommentResponse cr : comments) {
            sum += cr.getStar();
        }
        double avg = comments.size() > 0 ? (double) sum / comments.size() : 0;
        System.out.println("% avg = " + avg / 5 * 100);

        User currentUser;
        if (token != null) {
            currentUser = userService.getUserFromToken(token);
        } else {
            currentUser = null;
        }

        List<CommentResponse> updatedComments = comments.stream().map(comment -> {
            boolean userHasLiked = false;
            boolean userHasDisliked = false;
            if (currentUser != null) {
                for (Reaction reaction : comment.getReactions()) {
                    if (reaction.getUser().getId().equals(currentUser.getId())) {
                        if (reaction.getReactionType().equals(ReactionType.LIKE)) {
                            userHasLiked = true;
                        } else if (reaction.getReactionType().equals(ReactionType.DISLIKE)) {
                            userHasDisliked = true;
                        }
                    }
                }
            }
            return CommentResponse.builder()
                    .id(comment.getId())
                    .user(comment.getUser())
                    .product(comment.getProduct())
                    .content(comment.getContent())
                    .star(comment.getStar())
                    .starPercent(comment.getStarPercent())
                    .duration(comment.getDuration())
                    .reactions(comment.getReactions())
                    .likeCount(comment.getLikeCount())
                    .dislikeCount(comment.getDislikeCount())
                    .userHasLiked(userHasLiked)
                    .userHasDisliked(userHasDisliked)
                    .build();
        }).collect(Collectors.toList());

        model.addAttribute("avg", avg);
        model.addAttribute("message", message);
        model.addAttribute("product", product);
        model.addAttribute("comments", updatedComments);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("cartItemDTO", new CartItemDTO());
        model.addAttribute("commentDTO", new CommentDTO());
        model.addAttribute("reactionDTO", new ReactionDTO());
        System.out.println("MSG: " + message);
        return "product-detail";
    }

    @GetMapping("/category/{categoryId}")
    public String getProductByCategory(Model model,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "limit",defaultValue = "16") int limit,
                                       @PathVariable("categoryId") Long categoryId) throws Exception {
        Pageable pageable = PageRequest.of(page,limit);
        Page<Product> pageProduct = productService.getProductsByCategory(categoryId,pageable);
        model.addAttribute("products",pageProduct
                .getContent()
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList()));
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        model.addAttribute("categories",categories);
        model.addAttribute("categoryId",categoryId);
        model.addAttribute("totalPages",pageProduct.getTotalPages());
        model.addAttribute("currentPage",page);
        model.addAttribute("limit",16);
        model.addAttribute("title",categoryService.getCategoryById(categoryId).getName());
        model.addAttribute("source", "category");
        model.addAttribute("cartItemDTO", new CartItemDTO());

        return "products";
    }
    @GetMapping("/search")
    public String getProductsByKeyword(Model model,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "limit",defaultValue = "16") int limit,
                                       @RequestParam("keyword") String keyword){
        Pageable pageable = PageRequest.of(page,limit);
        Page<Product> pageProduct = productService.getProductsByKeyword(keyword, pageable);

        model.addAttribute("products",pageProduct
                .getContent()
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList()));
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        model.addAttribute("categories",categories);
        model.addAttribute("title","Kết quả tìm kiếm với từ khoá " + keyword);
        model.addAttribute("keyword",keyword);
        model.addAttribute("totalPages",pageProduct.getTotalPages());
        model.addAttribute("currentPage",page);
        model.addAttribute("limit",16);
        model.addAttribute("source","search");
        model.addAttribute("cartItemDTO", new CartItemDTO());

        return "products";
    }
    @GetMapping("sort/rating")
    public String getProductsByRating(Model model,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "limit",defaultValue = "16") int limit){
        Pageable pageable = PageRequest.of(page,limit);
        Page<Product> pageProduct = productService.getProductsByRating(pageable);
        model.addAttribute("products",pageProduct
                .getContent()
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList()));
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        model.addAttribute("categories",categories);
        model.addAttribute("totalPages",pageProduct.getTotalPages());
        model.addAttribute("currentPage",page);
        model.addAttribute("limit",16);
        model.addAttribute("source","rating");
        model.addAttribute("title","Sản Phẩm");
        model.addAttribute("cartItemDTO", new CartItemDTO());

        return "products";
    }
    @GetMapping("sort/popularity")
    public String getProductsByPopularity(Model model,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "limit",defaultValue = "16") int limit){
        Pageable pageable = PageRequest.of(page,limit);
        Page<Product> pageProduct = productService.getProductsByPopularity(pageable);
        model.addAttribute("products",pageProduct
                .getContent()
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList()));
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        model.addAttribute("categories",categories);
        model.addAttribute("totalPages",pageProduct.getTotalPages());
        model.addAttribute("currentPage",page);
        model.addAttribute("limit",16);
        model.addAttribute("source","popularity");
        model.addAttribute("title","Sản Phẩm");
        model.addAttribute("cartItemDTO", new CartItemDTO());

        return "products";
    }
    @GetMapping("/price")
    public String getProductsByPrice(Model model,
                                     @RequestParam(value = "minPrice", defaultValue = "0") Long minPrice,
                                     @RequestParam(value = "maxPrice", defaultValue = "5000000") Long maxPrice,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "limit",defaultValue = "16") int limit){
        Pageable pageable = PageRequest.of(page,limit);
        Page<Product> pageProduct = productService.getProductsByPrice(minPrice,maxPrice,pageable);
        model.addAttribute("products",pageProduct
                .getContent()
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList()));
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        model.addAttribute("categories",categories);
        model.addAttribute("totalPages",pageProduct.getTotalPages());
        model.addAttribute("currentPage",page);
        model.addAttribute("limit",16);
        model.addAttribute("source","price");
        model.addAttribute("title","Sản Phẩm");
        model.addAttribute("minPrice",minPrice);
        model.addAttribute("maxPrice",maxPrice);
        model.addAttribute("cartItemDTO", new CartItemDTO());

        return "products";
    }
}
