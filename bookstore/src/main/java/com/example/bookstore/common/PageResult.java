package com.example.bookstore.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {
    private Long total;
    private List<T> records;

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}