package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community_like")
public class CommunityLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long postId;

    private LocalDateTime createTime;
}
