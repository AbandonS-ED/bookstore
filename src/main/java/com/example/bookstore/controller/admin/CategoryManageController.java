package com.example.bookstore.controller.admin;

import com.example.bookstore.common.Result;
import com.example.bookstore.entity.Category;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryManageController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Category category) {
        category.setStatus(1);
        categoryService.add(category);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryMapper.updateById(category);
        return Result.success();
    }
}