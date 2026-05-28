package com.example.bookstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookstore.entity.Author;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthorMapper extends BaseMapper<Author> {
}
