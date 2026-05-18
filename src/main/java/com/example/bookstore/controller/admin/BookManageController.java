package com.example.bookstore.controller.admin;

import com.example.bookstore.common.Result;
import com.example.bookstore.entity.Book;
import com.example.bookstore.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/book")
@RequiredArgsConstructor
public class BookManageController {

    private final BookMapper bookMapper;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Book book) {
        book.setStatus(1);
        bookMapper.insert(book);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Book book) {
        bookMapper.updateById(book);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Book book = new Book();
        book.setId(id);
        book.setStatus(status);
        bookMapper.updateById(book);
        return Result.success();
    }
}