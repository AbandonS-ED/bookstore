package com.example.bookstore.service;

import com.example.bookstore.common.PageResult;
import com.example.bookstore.entity.CommunityPost;

import java.util.List;

public interface CommunityPostService {

    List<CommunityPost> listPosts(String keyword);

    PageResult<CommunityPost> listPosts(String keyword, Integer pageNum, Integer pageSize);

    CommunityPost getById(Long id);

    void add(CommunityPost post);

    void update(CommunityPost post);

    void delete(Long id);

    void toggleLike(Long id, Long userId);
}
