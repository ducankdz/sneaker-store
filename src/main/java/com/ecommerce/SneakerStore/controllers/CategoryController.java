package com.ecommerce.SneakerStore.controllers;

import com.ecommerce.SneakerStore.responses.CategoryResponse;
import com.ecommerce.SneakerStore.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    @GetMapping("")
    public String getCategories(Model model){
        List<CategoryResponse> categories = categoryService.getCategories();
        for(CategoryResponse category : categories){
            category.setProductCount(categoryService.countProductByCategoryId(category.getId()));
        }
        model.addAttribute("categories",categories);
        return "categories";
    }
}
