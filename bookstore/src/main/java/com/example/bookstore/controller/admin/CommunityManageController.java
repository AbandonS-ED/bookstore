package com.example.bookstore.controller.admin;

import com.example.bookstore.common.PageResult;
import com.example.bookstore.common.Result;
import com.example.bookstore.entity.CommunityPost;
import com.example.bookstore.mapper.CommunityPostMapper;
import com.example.bookstore.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/community")
@RequiredArgsConstructor
public class CommunityManageController {

    private final CommunityPostService communityPostService;
    private final CommunityPostMapper communityPostMapper;

    @GetMapping("/list")
    public Result<PageResult<CommunityPost>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(communityPostService.listPosts(keyword, pageNum, pageSize));
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody CommunityPost post) {
        communityPostMapper.updateById(post);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        communityPostMapper.deleteById(id);
        return Result.success();
    }
}
