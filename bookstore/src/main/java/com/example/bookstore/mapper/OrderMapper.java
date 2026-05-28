package com.example.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookstore.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM `order` " +
            "WHERE status IN ('paid', 'shipped', 'delivered', 'completed', 'refunding', 'after_sale') " +
            "AND pay_time >= #{start} AND pay_time < #{end}")
    BigDecimal sumRevenueBetween(@Param("start") String start, @Param("end") String end);

    @Select("SELECT DATE(pay_time) day, COALESCE(SUM(total_amount), 0) revenue " +
            "FROM `order` " +
            "WHERE status IN ('paid', 'shipped', 'delivered', 'completed', 'refunding', 'after_sale') " +
            "AND pay_time >= #{start} AND pay_time < #{end} " +
            "GROUP BY DATE(pay_time) ORDER BY day")
    List<Map<String, Object>> dailyRevenueBetween(@Param("start") String start, @Param("end") String end);

    @Select("SELECT DATE_FORMAT(pay_time, '%Y-%m') month, COALESCE(SUM(total_amount), 0) revenue " +
            "FROM `order` " +
            "WHERE status IN ('paid', 'shipped', 'delivered', 'completed', 'refunding', 'after_sale') " +
            "AND pay_time >= #{start} AND pay_time < #{end} " +
            "GROUP BY DATE_FORMAT(pay_time, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> monthlyRevenueBetween(@Param("start") String start, @Param("end") String end);
}