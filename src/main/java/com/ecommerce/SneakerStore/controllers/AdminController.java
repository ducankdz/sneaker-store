package com.ecommerce.SneakerStore.controllers;

import com.ecommerce.SneakerStore.components.SessionListener;
import com.ecommerce.SneakerStore.dtos.CategoryDTO;
import com.ecommerce.SneakerStore.dtos.CouponDTO;
import com.ecommerce.SneakerStore.dtos.ProductDTO;
import com.ecommerce.SneakerStore.entities.*;
import com.ecommerce.SneakerStore.responses.CategoryResponse;
import com.ecommerce.SneakerStore.responses.OrderResponse;
import com.ecommerce.SneakerStore.responses.ProductResponse;
import com.ecommerce.SneakerStore.responses.ProductSaleResponse;
import com.ecommerce.SneakerStore.services.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final CategoryService categoryService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final CommentService commentService;

    @GetMapping("")
    public String displayAdminHome(Model model){
        String message = (String) model.asMap().get("message");
        model.addAttribute("message",message);

        int countUsers = userService.getUsers().size();
        long productsSold = productService.countProductsSold();
        long ordersCount = orderService.getOrders().size();
        String totalEarning = orderService.totalEarning();
        Map<String, Object> userGrowthStatistics = userService.getUserGrowthStatistics();
        Map<String, Object> productCountsGrowthStatistics = productService.getProductSoldGrowthStatistics();
        Map<String, Object> ordersGrowthStatistics = orderService.getOrdersGrowthByMonth();
        Map<String, Object> earningGrowthStatistics = orderService.getEarningByMonth();
        Map<String, Object> positiveCommentStatistics = commentService.getPositiveCommentsByMonth();
        Map<String, Object> negativeCommentStatistics = commentService.getNegativeCommentsByMonth();
        Map<String, Object> categoryStatistics = categoryService.categoryStatistics();
        List<ProductSaleResponse> topSellingProducts = productService.getTopSellingProducts(8);
        List<OrderResponse> topOrders = orderService.getTopOrders(8)
                .stream()
                .map(OrderResponse :: fromOrder)
                .toList();



        model.addAttribute("countUsers",countUsers);
        model.addAttribute("topOrders",topOrders);
        model.addAttribute("topSellingProducts",topSellingProducts);
        model.addAttribute("categories",categoryStatistics.get("categories"));
        model.addAttribute("productSoldCounts",categoryStatistics.get("productSoldCounts"));
        model.addAttribute("positiveComments",positiveCommentStatistics.get("positiveComments"));
        model.addAttribute("negativeComments",negativeCommentStatistics.get("negativeComments"));
        model.addAttribute("userCounts",userGrowthStatistics.get("userCounts"));
        model.addAttribute("orderCounts",ordersGrowthStatistics.get("orderCounts"));
        model.addAttribute("productCounts",productCountsGrowthStatistics.get("productCounts"));
        model.addAttribute("earnings",earningGrowthStatistics.get("earnings"));
        model.addAttribute("months",userGrowthStatistics.get("months"));
        model.addAttribute("productsSold",productsSold);
        model.addAttribute("countOrders",ordersCount);
        model.addAttribute("totalEarning",totalEarning);

        return "admin_index";
    }
    @GetMapping("/category")
    public String displayAdminCategory(Model model){
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        String message = (String) model.asMap().get("message");
        System.out.println("Thong bao: " + message);
        model.addAttribute("message",message);
        model.addAttribute("categories",categories);
        model.addAttribute("categoryDTO",new CategoryDTO());
        return "admin_category";
    }
    @PostMapping("/category/add")
    public String addCategory(RedirectAttributes redirectAttributes,
                              @ModelAttribute("categoryDTO") CategoryDTO categoryDTO){
        try {
            Category category = categoryService.addCategory(categoryDTO);
            redirectAttributes.addFlashAttribute("message","Thêm danh mục thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message","Thêm danh mục thất bại");
        }
        return "redirect:/admin/category";
    }
    @GetMapping("/category/delete/{id}")
    public String deleteCategory(RedirectAttributes redirectAttributes,
                                 @PathVariable("id") Long id){
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("message","Xoá danh mục thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message","Xoá danh mục thất bại");
        }
        return "redirect:/admin/category";
    }
    @PostMapping("/category/update/{id}")
    public String updateCategory(RedirectAttributes redirectAttributes,
                                 @PathVariable("id") Long id,
                                 @ModelAttribute("categoryDTO") CategoryDTO categoryDTO){
        try {
            Category category = categoryService.updateCategory(categoryDTO,id);
            redirectAttributes.addFlashAttribute("message","Cập nhật danh mục thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message","Cập nhật danh mục thất bại");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/product")
    public String displayAdminProduct(Model model){
        String message = (String) model.asMap().get("message");
        model.addAttribute("message",message);

        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories",categoryService.getCategories());

        List<ProductResponse> products = productService.allProducts();
        model.addAttribute("products",products);
        return "admin_product";
    }

    @PostMapping("/product/add")
    public String addProducts(RedirectAttributes redirectAttributes,
                              @ModelAttribute("productDTO") ProductDTO productDTO,
                              @RequestParam("images") List<MultipartFile> images){
        try {
            Product product = productService.addProduct(productDTO);
            productService.uploadImage(product.getId(),images);
            redirectAttributes.addFlashAttribute("message",
                    "Thêm sản phẩm mới thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Thêm sản phẩm mới thất bại");
        }
        return "redirect:/admin/product";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(RedirectAttributes redirectAttributes,
                                @PathVariable("id") Long id){
        try {
            productService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("message",
                    "Xoá sản phẩm thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Xoá sản phẩm thất bại");
        }
        return "redirect:/admin/product";
    }
    @PostMapping("/product/update/{id}")
    public String updateProduct(RedirectAttributes redirectAttributes,
                              @PathVariable("id") Long id,
                              @ModelAttribute("productDTO") ProductDTO productDTO,
                              @RequestParam("images") List<MultipartFile> images){
        try {
            Product product = productService.updateProduct(id,productDTO);
            productService.uploadImage(id,images);
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật sản phẩm thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật sản phẩm thất bại");
        }
        return "redirect:/admin/product";
    }
    @GetMapping("/user")
    public String displayAdminUser(Model model){
        String message = (String) model.asMap().get("message");
        model.addAttribute("message",message);

        List<User> users = userService.getUsers();
        model.addAttribute("users",users);
        return "admin_user";
    }

    @GetMapping("/user/role/update/{id}")
    public String updateRole(RedirectAttributes redirectAttributes,
                             @PathVariable("id") Long id,
                             @RequestParam("roleId") Long roleId){
        try {
            User user = userService.updateRole(id,roleId);
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật vai trò người dùng thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật vai trò người dùng thất bại");
        }
        return "redirect:/admin/user";
    }
    @GetMapping("/user/lock/{id}")
    public String lockUser(RedirectAttributes redirectAttributes,
                           @PathVariable("id") Long id){
        try {
            User user = userService.lockUser(id);
            redirectAttributes.addFlashAttribute("message",
                    "Mở khoá tài khoản người dùng thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Mở khoá tài khoản người dùng thất bại");
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/user/unlock/{id}")
    public String unLockUser(RedirectAttributes redirectAttributes,
                           @PathVariable("id") Long id){
        try {
            User user = userService.unLockUser(id);
            redirectAttributes.addFlashAttribute("message",
                    "Khoá tài khoản người dùng thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Khoá tài khoản người dùng thất bại");
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/order")
    public String displayAdminOrder(Model model){
        String message = (String) model.asMap().get("message");
        model.addAttribute("message",message);


        List<OrderResponse> orders = orderService.getOrders();
        Collections.reverse(orders);
        model.addAttribute("orders",orders);
        return "admin_order";
    }
    @GetMapping("/order/update/{id}")
    public String updateOrder(RedirectAttributes redirectAttributes,
                              @PathVariable("id") Long id,
                              @RequestParam("status") String status,
                              @RequestParam("paymentStatus") String paymentStatus){
        try {
            Order order = orderService.updateOrder(id,status,paymentStatus);
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật đơn hàng thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật đơn hàng thất bại");
        }
        return "redirect:/admin/order";
    }

    @GetMapping("/coupon")
    public String displayAdminCoupon(Model model){
        String message = (String) model.asMap().get("message");
        model.addAttribute("message",message);
        model.addAttribute("couponDTO",new CouponDTO());

        List<Coupon> coupons = couponService.getCoupons();
        model.addAttribute("coupons",coupons);
        return "admin_coupon";
    }
    @PostMapping("/coupon/add")
    public String addCoupon(RedirectAttributes redirectAttributes,
                            @ModelAttribute("couponDTO")CouponDTO couponDTO){
        try {
            Coupon coupon = couponService.addCoupon(couponDTO);
            redirectAttributes.addFlashAttribute("message",
                    "Thêm Coupon thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Thêm Coupon thất bại");
        }
        return "redirect:/admin/coupon";
    }
    @GetMapping("/coupon/update/{id}")
    public String updateCoupon(RedirectAttributes redirectAttributes,
                               @PathVariable("id") Long id,
                               @RequestParam("active") int active){
        try {
            Coupon coupon = couponService.updateCoupon(id,active);
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật Coupon thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Cập nhật Coupon thất bại");
        }
        return "redirect:/admin/coupon";
    }
    @GetMapping("/coupon/delete/{id}")
    public String deleteCoupon(RedirectAttributes redirectAttributes,
                               @PathVariable("id") Long id){
        try {
            couponService.deleteCoupon(id);
            redirectAttributes.addFlashAttribute("message",
                    "Xoá Coupon thành công");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("message",
                    "Xoá Coupon thất bại");
        }
        return "redirect:/admin/coupon";
    }
}
