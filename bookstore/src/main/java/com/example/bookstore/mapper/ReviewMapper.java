package com.example.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookstore.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}