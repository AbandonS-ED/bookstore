package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.service.CategoryService;
import com.example.bookstore.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;

    @Override
    public List<CategoryVO> getAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(cat -> {
            CategoryVO vo = convertToVO(cat);
            Long count = bookMapper.selectCount(
                new LambdaQueryWrapper<Book>().eq(Book::getCategoryId, cat.getId())
            );
            vo.setBookCount(count.intValue());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> findByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (parentId == null || parentId == 0) {
            wrapper.eq(Category::getParentId, 0);
        } else {
            wrapper.eq(Category::getParentId, parentId);
        }
        wrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void add(Category category) {
        categoryMapper.insert(category);
    }

    @Override
    public void update(Category category) {
        if (category.getId() == null) {
            throw new BusinessException(1, "分类ID不能为空");
        }
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        LambdaQueryWrapper<Category> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Category::getParentId, id);
        if (categoryMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException(1, "存在子分类，无法删除");
        }

        LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.eq(Book::getCategoryId, id);
        if (bookMapper.selectCount(bookWrapper) > 0) {
            throw new BusinessException(1, "该分类下存在书籍，无法删除");
        }

        categoryMapper.deleteById(id);
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());
        return vo;
    }
}