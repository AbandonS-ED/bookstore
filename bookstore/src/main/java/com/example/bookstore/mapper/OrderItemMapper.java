package com.example.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookstore.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}