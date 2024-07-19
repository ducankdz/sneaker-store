package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.ProductDTO;
import com.ecommerce.SneakerStore.dtos.ProductImageDTO;
import com.ecommerce.SneakerStore.entities.Category;
import com.ecommerce.SneakerStore.entities.Product;
import com.ecommerce.SneakerStore.entities.ProductImage;
import com.ecommerce.SneakerStore.repositories.CategoryRepository;
import com.ecommerce.SneakerStore.repositories.ProductImageRepository;
import com.ecommerce.SneakerStore.repositories.ProductRepository;
import com.ecommerce.SneakerStore.responses.ProductResponse;
import com.ecommerce.SneakerStore.responses.ProductSaleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    public Product addProduct(ProductDTO productDTO) throws Exception {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new Exception(
                        "Cannot find category with id =" + productDTO.getCategoryId()));
        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .category(category)
                .discount(productDTO.getDiscount())
                .build();
        return productRepository.save(product);
    }

    public void uploadImage(Long productId, List<MultipartFile> files) throws Exception {
        Product existingProduct = getProductById(productId);
        files = files == null ? new ArrayList<>() : files;
        if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new Exception("You can only upload maximum 5 images");
        }
        List<ProductImage> images = new ArrayList<>();
        for(MultipartFile file : files){
            if(file.getSize() == 0){
                continue;
            }
            if(file.getSize() > 10*1024*1024){
                throw new Exception("Size of image is too large");
            }
            String fileName = storeFile(file);
            ProductImage productImage = createProductImage(
                    existingProduct.getId(),
                    ProductImageDTO.builder()
                            .imageUrl(fileName)
                            .build()
            );
            images.add(productImage);
        }
        List<ProductImage> productImages = productImageRepository.findByProductId(productId);
        if(productImages.isEmpty()){
            setThumbnail(productId,"notfound.jpg");
        }
        else {
            setThumbnail(productId, productImages.get(0).getImageUrl());
        }
    }
    public Product setThumbnail(Long id, String thumbnail) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Cannot find product with id = " + id));
        product.setThumbnail(thumbnail);
        return productRepository.save(product);
    }
    public ProductImage createProductImage(Long productId,
                                           ProductImageDTO productImageDTO) throws Exception {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new Exception(
                        "Cannot find category with id = "+productImageDTO.getProductId()));
        ProductImage newProductImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        int size = productImageRepository.findByProductId(productId).size();
        if(size>=ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new Exception(
                    "Number of images must be <= " + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        return productImageRepository.save(newProductImage);
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null){
            throw new IOException("Invalid image format");
        }
        //Lấy tên file
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        //Thêm UUID
        String uniqueFileName = UUID.randomUUID().toString() +  "_" + fileName;

        //Đường dẫn đến thư mục lưu file
        Path uploadDir = Paths.get("uploads");

        //Kiểm tra và tạo thư mục nếu nó không tồn tại
        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }

        //Đường dẫn đầy đủ đến file
        Path destination = Paths.get(uploadDir.toString(),uniqueFileName);

        //Sao chéo file vào thư mục đích
        Files.copy(file.getInputStream(),destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public boolean isImageFile(MultipartFile file){
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    public Page<Product> getProducts(Pageable pageable){
        return productRepository.findAll(pageable);
    }
    public List<Product> newProducts(){
        return productRepository.findAll()
                .stream().limit(8).collect(Collectors.toList());
    }
    public Product getProductById(Long id) throws Exception {
        Product product = productRepository.findById(id).
                orElseThrow(() -> new Exception("Cannot find product with id = " + id));
        return product;
    }
    public List<Product> getRelatedProducts(Long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new Exception("Cannot find products with id = " + id));
        List<Product> products = productRepository
                .getAllProductsByCategory(product.getCategory().getId());
        return products.stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .collect(Collectors.toList());
    }
    public List<Product> get8ProductsByCategory(Long categoryId){
        return productRepository.getAllProductsByCategory(categoryId)
                .stream().limit(8).collect(Collectors.toList());
    }
    public Page<Product> getProductsByCategory(Long categoryId,Pageable pageable){
        return productRepository.getProductsByCategory(categoryId,pageable);
    }
    public List<Product> getProductsWithHighestDiscount(){
        return productRepository.getProductsWithHighestDiscount()
                .stream().limit(2).collect(Collectors.toList());
    }
    public Page<Product> getProductsByKeyword(String keyword, Pageable pageable){
        return productRepository.getProductsByKeyword(keyword, pageable);
    }
    public Page<Product> getProductsByRating(Pageable pageable){
        return productRepository.getProductByRating(pageable);
    }
    public Page<Product> getProductsByPopularity(Pageable pageable){
        return productRepository.getProductByPopularity(pageable);
    }
    public Page<Product> getProductsByPrice(Long minPrice, Long maxPrice, Pageable pageable){
        return productRepository.getProductsByPrice(minPrice,maxPrice,pageable);
    }
    public List<ProductResponse> allProducts(){
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }
    public void deleteProductById(Long id){
        productRepository.deleteById(id);
    }
    public Product updateProduct(Long productId, ProductDTO productDTO) throws Exception {
        Product existingProduct = getProductById(productId);
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new Exception(
                        "Cannot find category with id = " + productDTO.getCategoryId()));
        existingProduct.setName(productDTO.getName());
        existingProduct.setCategory(category);
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setDiscount(productDTO.getDiscount());
        return productRepository.save(existingProduct);
    }
    public long countProductsSold(){
        return productRepository.countProductsSold();
    }
    public Map<String, Object> getProductSoldGrowthStatistics(){
        return Map.of(
                "months",List.of("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5",
                        "Tháng 6", "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"),
                "productCounts",List.of(productRepository.countProductsSoldByMonth(1),
                        productRepository.countProductsSoldByMonth(2),
                        productRepository.countProductsSoldByMonth(3),
                        productRepository.countProductsSoldByMonth(4),
                        productRepository.countProductsSoldByMonth(5),
                        productRepository.countProductsSoldByMonth(6),
                        productRepository.countProductsSoldByMonth(7),
                        productRepository.countProductsSoldByMonth(8),
                        productRepository.countProductsSoldByMonth(9),
                        productRepository.countProductsSoldByMonth(10),
                        productRepository.countProductsSoldByMonth(11),
                        productRepository.countProductsSoldByMonth(12)));
    }
    public List<ProductSaleResponse> getTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(PageRequest.of(0, limit));
    }
}
