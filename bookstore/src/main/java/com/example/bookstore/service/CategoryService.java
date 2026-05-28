package com.example.bookstore.service;

import com.example.bookstore.entity.Category;
import com.example.bookstore.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> getAll();

    List<CategoryVO> findByParentId(Long parentId);

    void add(Category category);

    void update(Category category);

    void delete(Long id);
}