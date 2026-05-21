-- 网上书店系统 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `bookstore` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `bookstore`;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(64) NOT NULL COMMENT '密码（加密）',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色（user/admin）',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1正常/0禁用）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID（0为顶级）',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 书籍表
CREATE TABLE `book` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `isbn` VARCHAR(20) UNIQUE COMMENT 'ISBN号',
    `title` VARCHAR(200) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) NOT NULL COMMENT '作者',
    `publisher` VARCHAR(100) COMMENT '出版社',
    `publish_date` DATE COMMENT '出版日期',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `category_id` BIGINT COMMENT '分类ID',
    `description` TEXT COMMENT '简介',
    `cover_url` VARCHAR(500) COMMENT '封面图',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1上架/0下架）',
    `quote` VARCHAR(500) COMMENT '书中摘抄名言',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍表';

-- 收货地址表
CREATE TABLE `address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) COMMENT '区县',
    `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 购物车表
CREATE TABLE `cart` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '书籍ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`),
    UNIQUE KEY `uk_user_book` (`user_id`, `book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
    `status` VARCHAR(20) NOT NULL COMMENT '订单状态',
    `pay_status` VARCHAR(20) DEFAULT 'unpaid' COMMENT '支付状态',
    `address_id` BIGINT COMMENT '收货地址ID',
    `receiver_name` VARCHAR(50) COMMENT '收货人',
    `receiver_phone` VARCHAR(20) COMMENT '联系电话',
    `receiver_address` VARCHAR(300) COMMENT '收货地址',
    `remark` VARCHAR(500) COMMENT '备注',
    `payment_id` BIGINT COMMENT '支付记录ID',
    `pay_time` DATETIME COMMENT '支付时间',
    `express_no` VARCHAR(50) COMMENT '物流单号',
    `expire_time` DATETIME COMMENT '订单过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 支付记录表
CREATE TABLE `payment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `payment_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '支付流水号',
    `payment_method` VARCHAR(20) COMMENT '支付方式(alipay/wechat/card)',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending/success/failed/refunded)',
    `paid_time` DATETIME COMMENT '支付时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 订单明细表
CREATE TABLE `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `book_id` BIGINT NOT NULL COMMENT '书籍ID',
    `book_title` VARCHAR(200) NOT NULL COMMENT '书名（冗余）',
    `book_author` VARCHAR(100) COMMENT '作者（冗余）',
    `cover_url` VARCHAR(500) COMMENT '封面（冗余）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `quantity` INT NOT NULL COMMENT '数量',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
    FOREIGN KEY (`order_id`) REFERENCES `order`(`id`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 评论表
CREATE TABLE `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '书籍ID',
    `rating` TINYINT NOT NULL COMMENT '评分（1-5）',
    `content` VARCHAR(500) COMMENT '评论内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1显示/0隐藏）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 初始化测试数据
-- 插入分类
INSERT INTO `category` (`name`, `parent_id`, `sort`) VALUES
('文学', 0, 1),
('科幻', 0, 2),
('技术', 0, 3),
('儿童', 0, 4),
('历史', 0, 5);

-- 插入管理员账号 (密码: 123456, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `email`, `role`) VALUES
('admin', '$2a$10$2AXXtLtX/U.0S9fhNXZW3OM7l3VyAMNulKCbuD86ZWQdhdOYJI89C', 'admin@bookstore.com', 'admin');

-- 插入测试书籍
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `price`, `stock`, `sales`, `category_id`, `description`, `cover_url`, `quote`) VALUES
('978-7-111-54742-2', 'Java核心技术卷I', '凯·S·霍斯特曼', '机械工业出版社', 119.00, 100, 3200, 3, 'Java技术经典参考书', '/pictures/Java核心技术.jpg', '历史在重演，科学的发展也不过是重演前代的思想。'),
('978-7-115-42835-7', 'Python编程：从入门到实践', '埃里克·马瑟斯', '人民邮电出版社', 79.00, 80, 5600, 3, 'Python入门经典', '/pictures/python编程.jpg', '代码是写给人读的，顺便让机器执行。'),
('978-7-5322-5000-2', '三体', '刘慈欣', '重庆出版社', 68.00, 50, 12800, 2, '科幻巨著，中国科幻文学里程碑', '/pictures/三体.jpg', '给岁月以文明，而不是给文明以岁月。'),
('978-7-5443-7010-3', '活着', '余华', '作家出版社', 35.00, 60, 9800, 1, '当代经典小说，讲述生命的韧性', '/pictures/活着.jpg', '人是为活着本身而活着，而不是为了活着之外的任何事物所活着。'),
('978-7-5443-5012-9', '百年孤独', '加西亚·马尔克斯', '南海出版公司', 55.00, 120, 22000, 1, '魔幻现实主义经典之作', '/pictures/百年孤独.jpg', '过去都是假的，回忆是一条没有归途的路。'),
('978-7-5402-3412-8', '小王子', '安托万·德·圣-埃克苏佩里', '人民文学出版社', 28.00, 200, 25000, 1, '写给成年人的童话', '/pictures/小王子.jpg', '重要的东西用眼睛是看不见的，要用心去感受。'),
('978-7-101-04013-5', '红楼梦', '曹雪芹', '中华书局', 98.00, 80, 18500, 1, '中国古典四大名著之首', '/pictures/红楼梦.jpg', '满纸荒唐言，一把辛酸泪。'),
('978-7-5442-4531-9', '挪威的森林', '村上春树', '上海译文出版社', 38.00, 90, 16000, 1, '村上春树代表作，青春爱情小说', '/pictures/挪威的森林.jpg', '每个人都有属于自己的一片森林，也许我们从来不曾去过。'),
('978-7-80173-502-3', '围城', '钱钟书', '人民文学出版社', 39.00, 70, 14000, 1, '中国现代文学经典', '/pictures/围城.jpg', '婚姻是一座围城，城外的人想进去，城里的人想出来。'),
('978-7-5302-1753-5', '平凡的世界', '路遥', '北京十月文艺出版社', 79.00, 150, 35000, 1, '茅盾文学奖获奖作品', '/pictures/平凡的世界.jpg', '生活不能等待别人来安排，要自己去争取和奋斗。'),
('978-7-5354-5422-5', '白夜行', '东野圭吾', '南海出版公司', 45.00, 85, 19000, 1, '东野圭吾悬疑推理巅峰之作', '/pictures/白夜行.jpg', '世上有两样东西不可直视，一是太阳，二是人心。'),
('978-7-5402-3612-2', '月亮与六便士', '毛姆', '人民文学出版社', 42.00, 75, 13000, 1, '关于理想与现实的经典之作', '/pictures/月亮与六便士.jpg', '满地都是六便士，他却抬头看见了月亮。'),
('978-7-5402-3411-1', '人间失格', '太宰治', '人民文学出版社', 32.00, 65, 11000, 1, '太宰治半自传体小说', '/pictures/人间失格.jpg', '生而为人，我很抱歉。'),
('978-7-5354-5423-2', '解忧杂货店', '东野圭吾', '南海出版公司', 42.00, 110, 28000, 1, '温暖人心的奇幻治愈小说', '/pictures/解忧杂货店.jpg', '人的心声是绝对不能无视的。'),
('978-7-5442-4532-6', '追风筝的人', '卡勒德·胡赛尼', '上海译文出版社', 36.00, 95, 24000, 1, '关于赎罪的感人故事', '/pictures/追风筝的人.jpg', '为你，千千万万遍。'),
('978-7-5302-1754-2', '边城', '沈从文', '北京十月文艺出版社', 29.00, 55, 6000, 1, '中国乡土文学经典', '/pictures/边城.jpg', '凡事都有偶然的凑巧，结果却又如宿命的必然。'),
('978-7-5402-3613-9', '骆驼祥子', '老舍', '人民文学出版社', 30.00, 60, 7500, 1, '老舍代表作，底层人民的生活写照', '/pictures/骆驼祥子.jpg', '钱会把人引进恶劣的社会中去，把高尚的理想撇开。'),
('978-7-101-04014-2', '呐喊', '鲁迅', '中华书局', 32.00, 70, 9000, 1, '中国现代文学奠基之作', '/pictures/呐喊.jpg', '希望是本无所谓有，无所谓无的。'),
('978-7-5302-1755-9', '家', '巴金', '北京十月文艺出版社', 35.00, 50, 5500, 1, '巴金代表作，激流三部曲之一', '/pictures/家.jpg', '青春是美丽的，但一个人的青春可以平庸无奇。'),
('978-7-5402-3614-6', '茶馆', '老舍', '人民文学出版社', 28.00, 45, 4800, 1, '中国话剧经典', '/pictures/茶馆.jpg', '改良改良，越改越凉。'),
('978-7-5402-3615-3', '雷雨', '曹禺', '人民文学出版社', 32.00, 55, 6500, 1, '中国话剧史上的里程碑', '/pictures/雷雨.jpg', '一切都是命中注定，谁也逃不掉。'),
('978-7-5302-1756-6', '子夜', '茅盾', '北京十月文艺出版社', 38.00, 40, 3500, 1, '茅盾代表作，社会剖析小说', '/pictures/子夜.jpg', '天亮之前有一段时间是非常暗的。'),
('978-7-5442-4533-3', '黄金时代', '王小波', '上海译文出版社', 35.00, 60, 8500, 1, '王小波代表作，自由与理性的呐喊', '/pictures/黄金时代.jpg', '一个人只拥有此生此世是不够的，他还应该拥有诗意的世界。'),
('978-7-5442-4534-0', '沉默的大多数', '王小波', '上海译文出版社', 32.00, 55, 3000, 1, '王小波杂文集，独立思考的启蒙', '/pictures/沉默的大多数.jpg', '从话语中，你很少能学到人性，从沉默中却能。'),
('978-7-5402-3616-0', '杀死一只知更鸟', '哈珀·李', '人民文学出版社', 39.00, 80, 17000, 1, '关于正义与勇气的成长小说', '/pictures/杀死一只知更鸟.jpg', '你永远不可能真正了解一个人，除非你从他的角度去看问题。'),
('978-7-5354-5424-9', '1984', '乔治·奥威尔', '南海出版公司', 42.00, 70, 15000, 2, '反乌托邦经典，对极权主义的深刻洞察', '/pictures/1984.jpg', '战争即和平，自由即奴役，无知即力量。'),
('978-7-5442-4535-7', '霍乱时期的爱情', '加西亚·马尔克斯', '上海译文出版社', 49.00, 65, 12000, 1, '马尔克斯爱情巨著', '/pictures/霍乱时期的爱情.jpg', '爱情首先是一种本能，要么生下来就会，要么永远都不会。');