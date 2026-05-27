package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.entity.CommunityPost;
import com.example.bookstore.service.CommunityPostService;
import com.example.bookstore.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @GetMapping("/list")
    public Result<List<CommunityPost>> list(@RequestParam(required = false) String keyword) {
        return Result.success(communityPostService.listPosts(keyword));
    }

    @GetMapping("/{id}")
    public Result<CommunityPost> detail(@PathVariable Long id) {
        return Result.success(communityPostService.getById(id));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        CommunityPost post = new CommunityPost();
        post.setUserId(AuthContext.getCurrentUserId());
        post.setUsername((String) body.getOrDefault("username", "匿名用户"));
        post.setContent((String) body.get("content"));
        post.setImageUrl((String) body.get("imageUrl"));
        post.setLikes(0);
        post.setLiked(0);
        Object bookId = body.get("bookId");
        if (bookId != null) {
            post.setBookId(Long.parseLong(bookId.toString()));
        }
        communityPostService.add(post);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody CommunityPost post) {
        communityPostService.update(post);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        communityPostService.delete(id);
        return Result.success();
    }

    @PostMapping("/like/{id}")
    public Result<Void> toggleLike(@PathVariable Long id) {
        communityPostService.toggleLike(id, AuthContext.getCurrentUserId());
        return Result.success();
    }
}