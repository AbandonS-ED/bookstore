package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("book_chapter")
public class BookChapter {

    private Long id;

    private Long bookId;

    private Integer chapterNum;

    private String title;

    private Integer page;
}
