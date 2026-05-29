package com.example.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookstore.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT COUNT(*) FROM order_item oi INNER JOIN `order` o ON oi.order_id = o.id " +
            "WHERE o.user_id = #{userId} AND oi.book_id = #{bookId} " +
            "AND o.status IN ('delivered', 'completed')")
    int countPurchased(@Param("userId") Long userId, @Param("bookId") Long bookId);
}