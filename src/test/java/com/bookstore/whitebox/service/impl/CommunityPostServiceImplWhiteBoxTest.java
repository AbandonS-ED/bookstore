package com.bookstore.whitebox.service.impl;

import com.example.bookstore.entity.CommunityPost;
import com.example.bookstore.mapper.CommunityPostMapper;
import com.example.bookstore.service.CommunityPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-017 白盒测试：社区帖子点赞 toggle 逻辑
 */
@SpringBootTest
@Transactional
class CommunityPostServiceImplWhiteBoxTest {

    @Autowired
    private CommunityPostService communityPostService;

    @Autowired
    private CommunityPostMapper communityPostMapper;

    @Test
    void toggleLike_fromUnliked_shouldIncreaseLikeAndSetLiked() {
        CommunityPost post = new CommunityPost();
        post.setUserId(1L);
        post.setUsername("testuser");
        post.setContent("T017测试帖子");
        post.setLikes(0);
        post.setLiked(0);
        post.setCreateTime(LocalDateTime.now());
        communityPostMapper.insert(post);

        communityPostService.toggleLike(post.getId(), 2L);

        CommunityPost updated = communityPostMapper.selectById(post.getId());
        assertEquals(1, updated.getLikes());
        assertEquals(1, updated.getLiked());
    }

    @Test
    void toggleLike_fromLiked_shouldDecreaseLikeAndClearLiked() {
        CommunityPost post = new CommunityPost();
        post.setUserId(1L);
        post.setUsername("testuser");
        post.setContent("T017测试帖子_已点赞");
        post.setLikes(5);
        post.setLiked(1);
        post.setCreateTime(LocalDateTime.now());
        communityPostMapper.insert(post);

        communityPostService.toggleLike(post.getId(), 2L);

        CommunityPost updated = communityPostMapper.selectById(post.getId());
        assertEquals(4, updated.getLikes());
        assertEquals(0, updated.getLiked());
    }

    @Test
    void toggleLike_nullLiked_shouldTreatAsUnliked() {
        CommunityPost post = new CommunityPost();
        post.setUserId(1L);
        post.setUsername("testuser");
        post.setContent("T017测试帖子_null状态");
        post.setLikes(3);
        post.setLiked(null);
        post.setCreateTime(LocalDateTime.now());
        communityPostMapper.insert(post);

        communityPostService.toggleLike(post.getId(), 2L);

        CommunityPost updated = communityPostMapper.selectById(post.getId());
        assertEquals(4, updated.getLikes());
        assertEquals(1, updated.getLiked());
    }

    @Test
    void toggleLike_nullLikes_shouldInitializeToDelta() {
        CommunityPost post = new CommunityPost();
        post.setUserId(1L);
        post.setUsername("testuser");
        post.setContent("T017测试帖子_null计数");
        post.setLikes(null);
        post.setLiked(0);
        post.setCreateTime(LocalDateTime.now());
        communityPostMapper.insert(post);

        communityPostService.toggleLike(post.getId(), 2L);

        CommunityPost updated = communityPostMapper.selectById(post.getId());
        assertEquals(1, updated.getLikes());
        assertEquals(1, updated.getLiked());
    }

    @Test
    void toggleLike_postNotExist_shouldNotThrow() {
        assertDoesNotThrow(() -> communityPostService.toggleLike(999999L, 1L));
    }
}
