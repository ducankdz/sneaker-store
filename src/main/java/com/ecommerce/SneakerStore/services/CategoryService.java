package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.dtos.CategoryDTO;
import com.ecommerce.SneakerStore.entities.Category;
import com.ecommerce.SneakerStore.repositories.CategoryRepository;
import com.ecommerce.SneakerStore.responses.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category addCategory(CategoryDTO categoryDTO) throws Exception {
        if(categoryRepository.existsByName(categoryDTO.getName())){
            throw new Exception("Category name existed");
        }
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .build();
        return categoryRepository.save(category);
    }
    public Category getCategoryById(Long id) throws Exception {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Cannot find category with id = " + id));
    }

    public Category updateCategory(CategoryDTO categoryDTO, Long id) throws Exception {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Cannot find category with id = " + id));
        String name = categoryDTO.getName();
        if(categoryRepository.existsByNameOfOtherCategories(name,id)){
            throw new Exception("Category name existed");
        }
        existingCategory.setName(name);
        return categoryRepository.save(existingCategory);
    }

    public List<CategoryResponse> getCategories(){
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }
    public int countProductByCategoryId(Long categoryId) {
        return categoryRepository.countProductsByCategoryId(categoryId);
    }
    public void deleteById(Long id){
        categoryRepository.deleteById(id);
    }

    public Map<String, Object> categoryStatistics() {

        List<String> categoryList = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            categoryList.add(category.getName());
        }

        List<Long> productCounts = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            productCounts.add(categoryRepository.countProductsSoldByCategoryId(category.getId()));
        }

        return Map.of(
                "categories", categoryList,
                "productSoldCounts", productCounts);
    }
}
