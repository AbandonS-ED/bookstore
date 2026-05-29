package com.example.bookstore.controller.admin;

import com.example.bookstore.common.Result;
import com.example.bookstore.entity.CommunityPost;
import com.example.bookstore.mapper.CommunityPostMapper;
import com.example.bookstore.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/community")
@RequiredArgsConstructor
public class CommunityManageController {

    private final CommunityPostService communityPostService;
    private final CommunityPostMapper communityPostMapper;

    @GetMapping("/list")
    public Result<List<CommunityPost>> list(@RequestParam(required = false) String keyword) {
        return Result.success(communityPostService.listPosts(keyword));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        communityPostMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody CommunityPost post) {
        CommunityPost existing = communityPostService.getById(post.getId());
        if (existing == null) {
            return Result.error("帖子不存在");
        }
        existing.setContent(post.getContent());
        existing.setImageUrl(post.getImageUrl());
        existing.setBookId(post.getBookId());
        communityPostService.update(existing);
        return Result.success();
    }
}
