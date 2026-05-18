package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.service.CategoryService;
import com.example.bookstore.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        List<CategoryVO> categories = categoryService.getAll();
        return Result.success(categories);
    }

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree(@RequestParam(required = false) Long parentId) {
        List<CategoryVO> categories = categoryService.findByParentId(parentId);
        return Result.success(categories);
    }
}