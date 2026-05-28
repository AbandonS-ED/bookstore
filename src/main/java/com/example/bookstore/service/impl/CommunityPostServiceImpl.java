package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.CommunityPost;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CommunityPostMapper;
import com.example.bookstore.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost>
    implements CommunityPostService {

    private final BookMapper bookMapper;

    @Override
    public List<CommunityPost> listPosts(String keyword) {
        LambdaQueryWrapper<CommunityPost> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like(CommunityPost::getContent, keyword)
              .or().like(CommunityPost::getUsername, keyword);
        }
        qw.orderByDesc(CommunityPost::getCreateTime);
        List<CommunityPost> posts = baseMapper.selectList(qw);

        for (CommunityPost post : posts) {
            if (post.getBookId() != null) {
                Book book = bookMapper.selectById(post.getBookId());
                if (book != null) {
                    post.setBookTitle(book.getTitle());
                }
            }
        }

        return posts;
    }

    @Override
    public CommunityPost getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void toggleLike(Long id, Long userId) {
        CommunityPost post = baseMapper.selectById(id);
        if (post == null) return;
        int delta = (post.getLiked() != null && post.getLiked() == 1) ? -1 : 1;
        post.setLikes(post.getLikes() == null ? delta : post.getLikes() + delta);
        post.setLiked(delta > 0 ? 1 : 0);
        baseMapper.updateById(post);
    }

    @Override
    public void add(CommunityPost post) {
        baseMapper.insert(post);
    }

    @Override
    public void update(CommunityPost post) {
        baseMapper.updateById(post);
    }

    @Override
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
}
