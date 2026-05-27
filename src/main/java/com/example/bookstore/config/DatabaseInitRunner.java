package com.example.bookstore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DatabaseMetaData meta = jdbcTemplate.getDataSource().getConnection().getMetaData();
        String dbName = meta.getDatabaseProductName();

        // Check if community_post table exists
        boolean tableExists = false;
        ResultSet rs = meta.getTables(null, null, "community_post", null);
        if (rs.next()) {
            tableExists = true;
        }
        rs.close();

        if (!tableExists) {
            log.info("Creating community_post table...");
            jdbcTemplate.execute("""
                CREATE TABLE `community_post` (
                    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                    `user_id` BIGINT COMMENT '发布用户ID',
                    `username` VARCHAR(50) COMMENT '用户名',
                    `content` VARCHAR(500) COMMENT '帖子内容',
                    `image_url` VARCHAR(500) COMMENT '图片URL',
                    `likes` INT DEFAULT 0 COMMENT '点赞数',
                    `liked` TINYINT DEFAULT 0 COMMENT '是否已点赞（0否/1是）',
                    `book_id` BIGINT COMMENT '关联书籍ID',
                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书斋社区帖子表'
                """);

            log.info("Inserting seed data...");
            jdbcTemplate.execute("""
                INSERT INTO `community_post` (`user_id`, `username`, `content`, `image_url`, `likes`, `liked`, `book_id`) VALUES
                (2, '林小雅', '读完《瓦尔登湖》，决定开始极简生活', 'https://picsum.photos/seed/walden/400/500', 128, 0, 1),
                (2, '书虫阿东', '深夜推理时间，《白夜行》太绝了', 'https://picsum.photos/seed/whiteNight/400/300', 89, 0, 4),
                (2, '雨落江南', '诗集读完了，整个人都安静下来', 'https://picsum.photos/seed/poetry/400/600', 256, 0, 3),
                (2, '北漂青年', '打工人的日常，地铁上读完半本《活着》', 'https://picsum.photos/seed/toLive/400/350', 67, 0, 4),
                (2, '文艺大叔', '三体让我对宇宙充满敬畏', 'https://picsum.photos/seed/threeBody/400/450', 312, 0, 1),
                (2, '小红迷', '月下看《围城》，方渐鸿的无奈', 'https://picsum.photos/seed/fortress/400/400', 178, 0, 8),
                (2, '阅读者', '人类简史刷新了我的认知边界', 'https://picsum.photos/seed/sapiens/400/550', 234, 0, 5),
                (2, '书语者', '我与地坛的文字让我泪流满面', 'https://picsum.photos/seed/ditian/400/320', 445, 0, 9),
                (2, '墨染白', '边城的翠翠，那条溪水清澈如她的眼眸', 'https://picsum.photos/seed/biancheng/400/480', 189, 0, 10),
                (2, '星河书社', '认知觉醒，让我开始真正思考', 'https://picsum.photos/seed/awaken/400/380', 156, 0, 12),
                (2, '豆瓣用户', '百年孤独里的魔幻现实让人着迷', 'https://picsum.photos/seed/century/400/520', 278, 0, 2),
                (2, '读书笔记', '小王子的纯真，是我们丢失的美好', 'https://picsum.photos/seed/principle/400/280', 367, 0, 6)
                """);
            log.info("Community post table created successfully");
        } else {
            log.info("community_post table already exists, skipping init");
        }
    }
}