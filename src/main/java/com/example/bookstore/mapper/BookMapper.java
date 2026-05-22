package com.example.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookstore.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Update("UPDATE book SET stock = stock - #{quantity} WHERE id = #{bookId} AND stock >= #{quantity}")
    int decreaseStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    @Update("UPDATE book SET sales = sales + #{quantity} WHERE id = #{bookId}")
    int increaseSales(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    @Update("UPDATE book SET favorited_count = favorited_count + 1 WHERE id = #{bookId}")
    int increaseFavoritedCount(@Param("bookId") Long bookId);

    @Update("UPDATE book SET favorited_count = favorited_count - 1 WHERE id = #{bookId}")
    int decreaseFavoritedCount(@Param("bookId") Long bookId);
}