package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.service.FavoriteService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, String> body) {
        Long userId = AuthContext.getCurrentUserId();
        Long bookId = Long.parseLong(body.get("bookId"));
        favoriteService.add(userId, bookId);
        return Result.success();
    }

    @DeleteMapping("/{bookId}")
    public Result<Void> remove(@PathVariable Long bookId) {
        Long userId = AuthContext.getCurrentUserId();
        favoriteService.remove(userId, bookId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<FavoriteVO>> list() {
        Long userId = AuthContext.getCurrentUserId();
        List<FavoriteVO> list = favoriteService.list(userId);
        return Result.success(list);
    }

    @GetMapping("/check/{bookId}")
    public Result<Boolean> check(@PathVariable Long bookId) {
        Long userId = AuthContext.getCurrentUserId();
        boolean isFav = favoriteService.isFavorited(userId, bookId);
        return Result.success(isFav);
    }

    @GetMapping("/ids")
    public Result<Set<String>> ids() {
        Long userId = AuthContext.getCurrentUserId();
        Set<String> ids = favoriteService.getFavoritedBookIds(userId);
        return Result.success(ids);
    }
}
