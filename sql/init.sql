-- 网上书店系统 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `bookstore` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;




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
    `status` INT DEFAULT 1 COMMENT '状态（1启用 0禁用）',
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
    `orig_price` DECIMAL(10,2) NULL COMMENT '原价（划线价）',
    `discount_price` DECIMAL(10,2) NULL COMMENT '折扣价',
    `discount_end_time` DATETIME NULL COMMENT '折扣截止时间',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `favorited_count` INT DEFAULT 0 COMMENT '收藏数',
    `category_id` BIGINT COMMENT '分类ID',
    `description` TEXT COMMENT '简介',
    `cover_url` VARCHAR(500) COMMENT '封面图',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1上架/0下架/2预售/3即将上架）',
    `expected_shelf_date` DATE NULL COMMENT '预计上架日期',
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

-- 收藏表
CREATE TABLE `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '书籍ID',
    `favorited_price` DECIMAL(10,2) NULL COMMENT '收藏时价格',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`),
    UNIQUE KEY `uk_user_book` (`user_id`, `book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 评论表
CREATE TABLE `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '书籍ID',
    `order_item_id` BIGINT COMMENT '订单项ID（从订单评价时关联）',
    `rating` TINYINT NOT NULL COMMENT '评分（1-5）',
    `content` VARCHAR(500) COMMENT '评论内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1显示/0隐藏）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`),
    UNIQUE KEY `uk_order_item` (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 作者表
CREATE TABLE `author` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '作者姓名',
    `avatar_url` VARCHAR(500) COMMENT '头像',
    `bio` TEXT COMMENT '简介',
    `country` VARCHAR(50) COMMENT '国籍',
    `birth_year` VARCHAR(20) COMMENT '生卒年',
    `awards` VARCHAR(500) COMMENT '奖项（逗号分隔）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作者表';

-- 目录表
CREATE TABLE `book_chapter` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `book_id` BIGINT NOT NULL COMMENT '书籍ID',
    `chapter_num` INT NOT NULL COMMENT '章节号',
    `title` VARCHAR(200) NOT NULL COMMENT '章节标题',
    `page` INT COMMENT '起始页码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目录表';

-- book 表新增 author_id 外键
ALTER TABLE `book` ADD COLUMN `author_id` BIGINT NULL COMMENT '作者ID' AFTER `author`;
ALTER TABLE `book` ADD FOREIGN KEY (`author_id`) REFERENCES `author`(`id`);

-- 初始化测试数据
-- 插入作者
INSERT INTO `author` (`name`, `bio`, `country`, `birth_year`, `awards`) VALUES
('凯·S·霍斯特曼', '凯·S·霍斯特曼（Cay S. Horstmann）是圣何塞州立大学计算机科学教授，Java技术领域的权威作家，拥有超过30年的教学与写作经验。', '美国', '1959—', 'Java Champion,ACM Distinguished Educator'),
('埃里克·马瑟斯', '埃里克·马瑟斯（Eric Matthes）是阿拉斯加的一位高中科学和数学教师，专注于Python编程教育，其编写的《Python编程：从入门到实践》是全球最畅销的Python入门书籍之一。', '美国', '1972—', NULL),
('刘慈欣', '刘慈欣，中国科幻小说作家，中国作家协会会员。他的作品将宏大的科学想象与深刻的人文关怀融为一体，代表作《三体》三部曲被认为是中国科幻文学的里程碑之作。', '中国', '1963—', '雨果奖,轨迹奖,银河奖,星云奖提名'),
('余华', '余华，中国当代作家，中国作家协会副主席。他的作品以冷静克制的笔触描绘普通人在极端环境下的生存状态，被翻译成多种语言在世界各地出版。', '中国', '1960—', '意大利格林扎纳·卡佛文学奖,法国文学与艺术骑士勋章,中国作家出版集团奖'),
('加西亚·马尔克斯', '加西亚·马尔克斯（Gabriel García Márquez）是哥伦比亚作家、记者和社会活动家，拉丁美洲魔幻现实主义文学的代表人物，1982年诺贝尔文学奖得主。', '哥伦比亚', '1927—2014', '诺贝尔文学奖,委内瑞拉罗慕洛·加列戈斯奖,意大利基安恰诺奖,美国纽斯塔特国际文学奖'),
('安托万·德·圣-埃克苏佩里', '安托万·德·圣-埃克苏佩里（Antoine de Saint-Exupéry）是法国作家、飞行员，其代表作《小王子》是全球最畅销的书籍之一，被翻译成300多种语言。', '法国', '1900—1944', '法国四个最高荣誉勋章'),
('曹雪芹', '曹雪芹，名霑，字梦阮，号雪芹，清代著名文学家。他以十年时间创作了《红楼梦》，成为中国古典文学的巅峰之作。', '中国', '约1715—约1763', NULL),
('村上春树', '村上春树，日本当代作家，其作品风格独特，融合了西方流行文化与日本传统美学，深受全球读者喜爱。', '日本', '1949—', '世界奇幻文学奖,耶路撒冷文学奖,卡夫卡奖,加泰罗尼亚国际奖'),
('钱钟书', '钱钟书，中国现代著名学者、作家，学贯中西，曾任中国社会科学院副院长。其代表作《围城》是中国现代文学的经典之作。', '中国', '1910—1998', NULL),
('路遥', '路遥，原名王卫国，中国当代作家，曾任陕西省作家协会副主席。其代表作《平凡的世界》获得第三届茅盾文学奖。', '中国', '1949—1992', '茅盾文学奖'),
('东野圭吾', '东野圭吾，日本当代推理小说作家，日本推理作家协会理事。他的作品情节精巧、推理严密，多次获得日本各大文学奖项。', '日本', '1958—', '江户川乱步奖,直木奖,本格推理小说大奖,日本推理作家协会奖'),
('毛姆', '威廉·萨默塞特·毛姆（W. Somerset Maugham），英国小说家、剧作家，以深刻的人性洞察和优雅的文笔著称，是20世纪最受欢迎的英语作家之一。', '英国', '1874—1965', '英国皇家文学学会会员,嘉德勋章'),
('太宰治', '太宰治，本名津岛修治，日本小说家，无赖派文学代表作家。其作品充满自我解嘲与忧郁气质，深刻影响了日本战后文学。', '日本', '1909—1948', NULL),
('卡勒德·胡赛尼', '卡勒德·胡赛尼（Khaled Hosseini），阿富汗裔美国作家、医生。其作品以阿富汗为背景，讲述了战争背景下的人性故事，感动了全球亿万读者。', '阿富汗/美国', '1965—', '联合国人道主义奖,古斯塔夫·海涅曼和平奖'),
('沈从文', '沈从文，原名沈岳焕，中国现代著名作家、文物研究家。其作品以湘西风土人情为背景，文字优美隽永，代表作《边城》是中国乡土文学经典。', '中国', '1902—1988', NULL),
('老舍', '老舍，原名舒庆春，字舍予，中国现代小说家、剧作家，人民艺术家。作品语言幽默生动，善于描绘北京市民生活。', '中国', '1899—1966', '人民艺术家'),
('鲁迅', '鲁迅，原名周树人，中国现代文学奠基人、思想家、革命家。其作品对中国社会和文化进行了深刻批判，影响了中国几代人的思想。', '中国', '1881—1936', NULL),
('巴金', '巴金，原名李尧棠，中国现代文学大师，曾任中国作家协会主席。其"激流三部曲"《家》《春》《秋》是中国现代文学的经典。', '中国', '1904—2005', '意大利但丁国际奖,法国荣誉军团勋章,斯大林国际和平奖'),
('曹禺', '曹禺，原名万家宝，中国现代话剧大师，中央戏剧学院副院长。其代表作《雷雨》是中国话剧史上的里程碑之作。', '中国', '1910—1996', NULL),
('茅盾', '茅盾，原名沈德鸿，字雁冰，中国现代文学巨匠，曾任文化部部长、中国作家协会主席。其代表作《子夜》是中国社会剖析小说的典范。', '中国', '1896—1981', NULL),
('王小波', '王小波，中国当代学者、作家。他的作品以独特的幽默风格和深刻的思想性著称，对中国知识分子的精神世界进行了深入探索。', '中国', '1952—1997', NULL),
('哈珀·李', '哈珀·李（Harper Lee），美国作家。其唯一长篇小说《杀死一只知更鸟》获得普利策小说奖，成为美国文学经典。', '美国', '1926—2016', '普利策小说奖,总统自由勋章'),
('乔治·奥威尔', '乔治·奥威尔（George Orwell），英国作家、记者和社会评论家。其代表作《1984》和《动物农场》是对极权主义的深刻剖析，影响深远。', '英国', '1903—1950', '英国文学杰出贡献奖,雨果奖荣誉奖');

-- 插入分类
INSERT INTO `category` (`name`, `parent_id`, `sort`) VALUES
('文学', 0, 1),
('科幻', 0, 2),
('技术', 0, 3),
('儿童', 0, 4),
('历史', 0, 5);

-- 插入管理员账号 (密码: 123456, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `email`, `role`) VALUES
('admin', '$2a$10$2AXXtLtX/U.0S9fhNXZW3OM7l3VyAMNulKCbuD86ZWQdhdOYJI89C', 'admin@bookstore.com', 'admin'),
('testuser', '$2a$10$2AXXtLtX/U.0S9fhNXZW3OM7l3VyAMNulKCbuD86ZWQdhdOYJI89C', 'test@bookstore.com', 'user');

-- 插入测试书籍
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `price`, `orig_price`, `stock`, `sales`, `favorited_count`, `category_id`, `description`, `cover_url`, `quote`) VALUES
('978-7-111-54742-2', 'Java核心技术卷I', '凯·S·霍斯特曼', '机械工业出版社', 119.00, 149.00, 100, 0, 0, 3, 'Java技术经典参考书', '/pictures/Java核心技术.jpg', '历史在重演，科学的发展也不过是重演前代的思想。'),
('978-7-115-42835-7', 'Python编程：从入门到实践', '埃里克·马瑟斯', '人民邮电出版社', 79.00, 99.00, 80, 0, 0, 3, 'Python入门经典', '/pictures/python编程.jpg', '代码是写给人读的，顺便让机器执行。'),
('978-7-5322-5000-2', '三体', '刘慈欣', '重庆出版社', 68.00, 88.00, 50, 0, 0, 2, '科幻巨著，中国科幻文学里程碑', '/pictures/三体.jpg', '给岁月以文明，而不是给文明以岁月。'),
('978-7-5443-7010-3', '活着', '余华', '作家出版社', 35.00, 45.00, 60, 0, 0, 1, '当代经典小说，讲述生命的韧性', '/pictures/活着.jpg', '人是为活着本身而活着，而不是为了活着之外的任何事物所活着。'),
('978-7-5443-5012-9', '百年孤独', '加西亚·马尔克斯', '南海出版公司', 55.00, 69.00, 120, 0, 0, 1, '魔幻现实主义经典之作', '/pictures/百年孤独.jpg', '过去都是假的，回忆是一条没有归途的路。'),
('978-7-5402-3412-8', '小王子', '安托万·德·圣-埃克苏佩里', '人民文学出版社', 28.00, NULL, 200, 0, 0, 1, '写给成年人的童话', '/pictures/小王子.jpg', '重要的东西用眼睛是看不见的，要用心去感受。'),
('978-7-101-04013-5', '红楼梦', '曹雪芹', '中华书局', 98.00, 128.00, 80, 0, 0, 1, '中国古典四大名著之首', '/pictures/红楼梦.jpg', '满纸荒唐言，一把辛酸泪。'),
('978-7-5442-4531-9', '挪威的森林', '村上春树', '上海译文出版社', 38.00, NULL, 90, 0, 0, 1, '村上春树代表作，青春爱情小说', '/pictures/挪威的森林.jpg', '每个人都有属于自己的一片森林，也许我们从来不曾去过。'),
('978-7-80173-502-3', '围城', '钱钟书', '人民文学出版社', 39.00, 49.00, 70, 0, 0, 1, '中国现代文学经典', '/pictures/围城.jpg', '婚姻是一座围城，城外的人想进去，城里的人想出来。'),
('978-7-5302-1753-5', '平凡的世界', '路遥', '北京十月文艺出版社', 79.00, 99.00, 150, 0, 0, 1, '茅盾文学奖获奖作品', '/pictures/平凡的世界.jpg', '生活不能等待别人来安排，要自己去争取和奋斗。'),
('978-7-5354-5422-5', '白夜行', '东野圭吾', '南海出版公司', 45.00, 58.00, 85, 0, 0, 1, '东野圭吾悬疑推理巅峰之作', '/pictures/白夜行.jpg', '世上有两样东西不可直视，一是太阳，二是人心。'),
('978-7-5402-3612-2', '月亮与六便士', '毛姆', '人民文学出版社', 42.00, 55.00, 75, 0, 0, 1, '关于理想与现实的经典之作', '/pictures/月亮与六便士.jpg', '满地都是六便士，他却抬头看见了月亮。'),
('978-7-5402-3411-1', '人间失格', '太宰治', '人民文学出版社', 32.00, NULL, 65, 0, 0, 1, '太宰治半自传体小说', '/pictures/人间失格.jpg', '生而为人，我很抱歉。'),
('978-7-5354-5423-2', '解忧杂货店', '东野圭吾', '南海出版公司', 42.00, 52.00, 110, 0, 0, 1, '温暖人心的奇幻治愈小说', '/pictures/解忧杂货店.jpg', '人的心声是绝对不能无视的。'),
('978-7-5442-4532-6', '追风筝的人', '卡勒德·胡赛尼', '上海译文出版社', 36.00, NULL, 95, 0, 0, 1, '关于赎罪的感人故事', '/pictures/追风筝的人.jpg', '为你，千千万万遍。'),
('978-7-5302-1754-2', '边城', '沈从文', '北京十月文艺出版社', 29.00, 38.00, 55, 0, 0, 1, '中国乡土文学经典', '/pictures/边城.jpg', '凡事都有偶然的凑巧，结果却又如宿命的必然。'),
('978-7-5402-3613-9', '骆驼祥子', '老舍', '人民文学出版社', 30.00, NULL, 60, 0, 0, 1, '老舍代表作，底层人民的生活写照', '/pictures/骆驼祥子.jpg', '钱会把人引进恶劣的社会中去，把高尚的理想撇开。'),
('978-7-101-04014-2', '呐喊', '鲁迅', '中华书局', 32.00, 42.00, 70, 0, 0, 1, '中国现代文学奠基之作', '/pictures/呐喊.jpg', '希望是本无所谓有，无所谓无的。'),
('978-7-5302-1755-9', '家', '巴金', '北京十月文艺出版社', 35.00, NULL, 50, 0, 0, 1, '巴金代表作，激流三部曲之一', '/pictures/家.jpg', '青春是美丽的，但一个人的青春可以平庸无奇。'),
('978-7-5402-3614-6', '茶馆', '老舍', '人民文学出版社', 28.00, NULL, 45, 0, 0, 1, '中国话剧经典', '/pictures/茶馆.jpg', '改良改良，越改越凉。'),
('978-7-5402-3615-3', '雷雨', '曹禺', '人民文学出版社', 32.00, 42.00, 55, 0, 0, 1, '中国话剧史上的里程碑', '/pictures/雷雨.jpg', '一切都是命中注定，谁也逃不掉。'),
('978-7-5302-1756-6', '子夜', '茅盾', '北京十月文艺出版社', 38.00, NULL, 40, 0, 0, 1, '茅盾代表作，社会剖析小说', '/pictures/子夜.jpg', '天亮之前有一段时间是非常暗的。'),
('978-7-5442-4533-3', '黄金时代', '王小波', '上海译文出版社', 35.00, 45.00, 60, 0, 0, 1, '王小波代表作，自由与理性的呐喊', '/pictures/黄金时代.jpg', '一个人只拥有此生此世是不够的，他还应该拥有诗意的世界。'),
('978-7-5442-4534-0', '沉默的大多数', '王小波', '上海译文出版社', 32.00, 42.00, 55, 0, 0, 1, '王小波杂文集，独立思考的启蒙', '/pictures/沉默的大多数.jpg', '从话语中，你很少能学到人性，从沉默中却能。'),
('978-7-5402-3616-0', '杀死一只知更鸟', '哈珀·李', '人民文学出版社', 39.00, 49.00, 80, 0, 0, 1, '关于正义与勇气的成长小说', '/pictures/杀死一只知更鸟.jpg', '你永远不可能真正了解一个人，除非你从他的角度去看问题。'),
('978-7-5354-5424-9', '1984', '乔治·奥威尔', '南海出版公司', 42.00, 55.00, 70, 0, 0, 2, '反乌托邦经典，对极权主义的深刻洞察', '/pictures/1984.jpg', '战争即和平，自由即奴役，无知即力量。'),
('978-7-5442-4535-7', '霍乱时期的爱情', '加西亚·马尔克斯', '上海译文出版社', 49.00, 65.00, 65, 0, 0, 1, '马尔克斯爱情巨著', '/pictures/霍乱时期的爱情.jpg', '爱情首先是一种本能，要么生下来就会，要么永远都不会。'),
-- 即将上架书籍
('978-7-5086-5325-0', '未来简史', '尤瓦尔·赫拉利', '中信出版社', 68.00, 88.00, 0, 0, 0, 2, '从人类过去到未来，赫拉利为我们描绘了一幅令人震撼的未来图景。当人工智能和生物技术重塑世界，人类将何去何从？一本让你重新思考人类命运的杰作。', NULL, '21世纪，人类社会面临前所未有的变革。'),
('978-7-5443-4432-6', '长安的荔枝', '马伯庸', '南海出版公司', 42.00, 56.00, 0, 0, 0, 1, '一个古代社畜的极限生存挑战。天宝十四年，长安小吏李善德接到一个不可能完成的任务：在贵妃诞日前从岭南运来新鲜荔枝。荔枝"一日色变，两日香变，三日味变"，而岭南距长安五千余里。马伯庸以历史考据与文学想象，写尽小人物在大时代下的挣扎与智慧。', NULL, '就算失败，我也想知道，自己倒在距离终点多远的地方。'),
('978-7-5086-4432-5', '规模', '杰弗里·韦斯特', '中信出版社', 75.00, 98.00, 0, 0, 0, 2, '复杂世界的简单法则。从城市到公司，从生物到社会，规模法则揭示了隐藏在复杂系统背后的统一规律。圣塔菲研究所前所长杰弗里·韦斯特以其跨学科视野，为我们呈现了一个由数学和物理学统治的奇妙世界。', NULL, '规模法则告诉我们，世界远比我们想象的更有秩序。'),
('978-7-0200-4378-3', '额尔古纳河右岸（精装典藏版）', '迟子建', '人民文学出版社', 48.00, 65.00, 0, 0, 0, 1, '茅盾文学奖获奖作品，一部关于鄂温克民族的史诗。在中俄边界的额尔古纳河右岸，原始部落的最后一个女酋长以自述的方式，讲述了一个民族近百年的沧桑与变迁。迟子建以诗意的笔触，书写了人与自然的和谐、生与死的轮回，以及现代文明对古老传统的冲击。', NULL, '我是雨和雪的老熟人了，我有九十岁了。雨雪看老了我，我也把它们给看老了。');

-- 扩充书籍简介
UPDATE `book` SET `description` = 'Java领域的经典权威著作，全面覆盖Java SE的核心技术与高级特性。本书由全球知名的Java技术专家凯·S·霍斯特曼精心编写，以清晰易懂的方式讲解了Java编程语言的基础知识、面向对象编程、继承、接口、异常处理、泛型、集合框架等核心内容。\n\n无论你是Java初学者还是有经验的开发者，本书都能帮助你深入理解Java语言的精髓。书中包含大量实用的代码示例和最佳实践，可以直接应用到实际开发工作中。\n\n★ 全球畅销超过百万册\n★ Java技术圣经级著作\n★ 覆盖Java 17最新特性' WHERE `isbn` = '978-7-111-54742-2';

UPDATE `book` SET `description` = '零基础学习Python编程的最佳入门书籍。本书从编程基础概念讲起，逐步引导读者掌握变量、列表、if语句、字典、函数、类等核心知识，并通过两个完整的项目实战——外星人入侵游戏和数据可视化——帮助读者巩固所学内容。\n\n作者埃里克·马瑟斯以其丰富的教学经验，将复杂的编程概念拆解为易于理解的小步骤，让读者在轻松愉快的氛围中掌握Python编程技能。每个章节都配有精心设计的练习，帮助读者在实践中学习和提高。\n\n★ 全球销量超过300万册\n★ 美国亚马逊编程入门书No.1\n★ 适合完全没有编程经验的读者' WHERE `isbn` = '978-7-115-42835-7';

UPDATE `book` SET `description` = '刘慈欣创作的科幻巨著，中国科幻文学的里程碑之作。故事从文化大革命时期开始，天体物理学家叶文洁在秘密基地向外太空发送信号，意外引来了三体文明的入侵威胁。人类面临着前所未有的生存危机。\n\n作品以宏大的宇宙视角和深邃的人文关怀，探讨了文明与文明之间的残酷法则。三体文明所处的混乱世界、黑暗森林法则的揭示、以及宇宙社会学的基本公理，都令人震撼不已。小说将科学想象与哲学思辨完美结合，开创了中国科幻文学的新纪元。\n\n★ 雨果奖最佳长篇小说\n★ 全球销量超过2900万册\n★ 被翻译成20多种语言\n★ 奥巴马、扎克伯格等名人推荐的科幻神作' WHERE `isbn` = '978-7-5322-5000-2';

UPDATE `book` SET `description` = '余华的代表作，讲述了大时代背景下徐福贵一家的命运起伏。福贵本是地主家的少爷，因赌博败尽家产，从此开启了一段艰难而坚韧的人生旅程。在接连不断的苦难中，他先后失去了亲人，最终只剩下一头老牛相伴。\n\n这部小说以冷静克制的笔触，展现了中国普通人在历史洪流中的生存状态。福贵的故事既是个人的悲剧，也是一个时代的缩影。余华用朴素的语言写出了生命最本质的力量——无论生活多么艰难，人总要活着。\n\n★ 意大利格林扎纳·卡佛文学奖获奖作品\n★ 入选香港《亚洲周刊》20世纪中文小说100强\n★ 张艺谋改编同名电影获多项国际大奖' WHERE `isbn` = '978-7-5443-7010-3';

UPDATE `book` SET `description` = '加西亚·马尔克斯的传世杰作，魔幻现实主义文学的巅峰之作。小说讲述了布恩迪亚家族七代人的传奇故事，以及加勒比海沿岸小镇马孔多的百年兴衰，深刻反映了拉丁美洲一个世纪以来风云变幻的历史。\n\n作品巧妙糅合了神话传说、民间故事、宗教典故等神秘因素，将现实与幻想融为一体，展现出一个瑰丽奇崛的想象世界。马尔克斯以其天马行空的想象力和精湛的叙事艺术，创造了一个既荒诞又真实、既魔幻又动人的文学宇宙。\n\n★ 诺贝尔文学奖获奖作品\n★ 全球销量超过5000万册\n★ 被译成46种语言\n★ 豆瓣评分9.4，豆瓣Top250第3位' WHERE `isbn` = '978-7-5443-5012-9';

UPDATE `book` SET `description` = '一部写给成年人的童话，全球最畅销的书籍之一。小王子住在一颗小行星上，有一天他决定拜访邻近的星球。在他的旅途中，他遇到了各种各样的成年人——国王、商人、点灯人、地理学家……每个人都在自己的世界里忙碌着，却忘记了生活的本质。\n\n最终小王子来到地球，在沙漠中遇到了飞行员。通过与小王子天真而深刻的对话，飞行员重新理解了爱、友谊和责任的意义。圣-埃克苏佩里用诗意而忧伤的文字，提醒我们最重要的东西用眼睛是看不见的，只有用心才能感受。\n\n★ 全球销量超过2亿册\n★ 被翻译成300多种语言\n★ 法国文学经典，入选多国教科书' WHERE `isbn` = '978-7-5402-3412-8';

UPDATE `book` SET `description` = '中国古典文学的巅峰之作，曹雪芹以毕生心血创作的不朽巨著。小说以贾宝玉、林黛玉、薛宝钗的爱情悲剧为主线，全面展现了贾、史、王、薛四大家族的兴衰沉浮，深刻揭示了中国封建社会由盛转衰的必然命运。\n\n作品塑造了众多性格鲜明的人物形象，从贾宝玉的叛逆、林黛玉的敏感、薛宝钗的圆融，到王熙凤的精明狠辣，每一个角色都栩栩如生。曹雪芹以细腻的笔触描绘了中国传统社会的方方面面，包括诗词曲赋、建筑园林、饮食服饰、礼仪制度等，堪称中国传统文化的百科全书。\n\n★ 中国古典四大名著之首\n★ 被译成30多种语言\n★ 毛泽东评价"中国只有一部《红楼梦》"\n★ 红学研究已成为一门独立的学科' WHERE `isbn` = '978-7-101-04013-5';

UPDATE `book` SET `description` = '村上春树的半自传体青春爱情小说，也是他最广为人知的代表作之一。故事以主人公渡边的视角，回忆了他在大学时代与直子和绿子两位女孩之间的情感纠葛。直子娴静而忧郁，始终无法走出恋人自杀的阴影；绿子活泼而率真，用自己的方式温暖着渡边。\n\n小说以细腻的笔触描绘了年轻人在爱情、死亡与成长之间的彷徨与探索。村上春树以其独特的文学风格，将青春的感伤与生命的哲思完美融合，创造出一个既现实又超脱的文学世界。\n\n★ 全球销量超过1500万册\n★ 村上春树最具代表性的作品之一\n★ 改编电影由松山研一、菊地凛子主演' WHERE `isbn` = '978-7-5442-4531-9';

UPDATE `book` SET `description` = '钱钟书唯一的长篇小说，中国现代文学史上最经典的作品之一。小说以留学归来的方鸿渐为主人公，描绘了他在抗战时期的一段人生旅程——从回国的船上到上海的社交圈，从爱情追逐到婚姻围城，从学校任教到报社工作。\n\n钱钟书以其犀利的幽默和深邃的洞察力，刻画了一群知识分子的众生相。小说中妙语连珠的讽刺和精辟的人生哲理，令人拍案叫绝。"婚姻是一座围城，城外的人想进去，城里的人想出来"这句经典之语，更是道尽了人间百态。\n\n★ 入选《亚洲周刊》20世纪中文小说100强\n★ 被翻译成多种语言在世界各地出版\n★ 被誉为"新《儒林外史》"' WHERE `isbn` = '978-7-80173-502-3';

UPDATE `book` SET `description` = '路遥呕心沥血创作的百万字长篇小说，以中国70年代中期到80年代中期十年间的社会生活为背景，通过孙少安和孙少平两兄弟的奋斗历程，展现了普通人在大时代历史进程中所走过的艰难曲折的道路。\n\n孙少平从一名贫困的农村青年，通过自己的努力和坚持，最终成为一名煤矿工人；孙少安则扎根农村，通过创办砖厂带领乡亲们脱贫致富。两兄弟的故事既是个人的奋斗史，也是那个时代的缩影。路遥以真挚的情感和朴实的语言，写出了中国普通劳动者的尊严与梦想。\n\n★ 茅盾文学奖获奖作品\n★ 中央人民广播电台连播，听众超过3亿\n★ 影响几代中国人的文学经典' WHERE `isbn` = '978-7-5302-1753-5';

UPDATE `book` SET `description` = '东野圭吾悬疑推理小说的巅峰之作。故事围绕两起看似无关的案件展开：当铺老板被杀案和一起女尸案，但随着时间的推移，这些案件背后的真相逐渐浮出水面，牵出一段跨越十九年的黑暗秘密。\n\n雪穗和亮司，两个在黑暗中相互取暖的灵魂，为了生存不惜犯下累累罪行。他们的关系既令人恐惧又令人心碎。东野圭吾以其精巧的情节设计和深刻的人性洞察，构建了一个令人不寒而栗却又无法释怀的故事世界。\n\n★ 东野圭吾最具代表性的作品\n★ 日本推理小说史上的里程碑\n★ 多次改编为电视剧、电影和舞台剧\n★ 豆瓣评分9.2，推理小说必读之作' WHERE `isbn` = '978-7-5354-5422-5';

UPDATE `book` SET `description` = '毛姆最负盛名的代表作之一，以法国后印象派画家高更的生平为原型创作的小说。主人公斯特里克兰德原本是一名普通的伦敦证券经纪人，却在中年时突然抛妻弃子，离家出走，开始追求绘画的梦想。\n\n他在巴黎穷困潦倒，后又远赴南太平洋的塔希提岛，在原始自然中找到了艺术的灵感。毛姆以冷静而犀利的笔触，探讨了理想与现实、艺术与生活之间的永恒矛盾。"满地都是六便士，他却抬头看见了月亮"——这句话道出了全书的核心主题。\n\n★ 20世纪最受欢迎的英语小说之一\n★ 多次改编为电影和电视剧\n★ 以高更为原型，探讨艺术家的灵魂' WHERE `isbn` = '978-7-5402-3612-2';

UPDATE `book` SET `description` = '太宰治的半自传体小说，日本无赖派文学的代表作。全书由序曲、后记以及主角大庭叶藏的三篇手记构成，讲述了叶藏从少年到中年逐渐走向自我毁灭的人生历程。他恐惧与人相处，不断用滑稽的表演来伪装自己，在酒精和药物中寻求解脱。\n\n太宰治以坦诚到近乎残忍的笔触，剖析了自己内心深处的软弱与挣扎。"生而为人，我很抱歉"——这句话既是叶藏的内心独白，也是太宰治对自身命运的预言。这部作品是太宰治留给世界最后的告白，写完后不久他便投水自尽。\n\n★ 日本文学史上销量最高的作品之一\n★ 太宰治的遗作，也是其代表作\n★ 被多次改编为电影、动画和漫画' WHERE `isbn` = '978-7-5402-3411-1';

UPDATE `book` SET `description` = '东野圭吾最温暖人心的作品，一部关于爱与救赎的奇幻治愈小说。三个小偷在逃亡途中躲进了一家废弃的杂货店，意外发现这里的信件穿越时空——只要写下烦恼投进卷帘门，第二天就会收到回信。\n\n通过一封封求助信件，三个小偷逐渐发现了这家杂货店的秘密，并在帮助他人的过程中找到了自己人生的答案。东野圭吾用精巧的叙事结构，将看似独立的故事编织成一个温暖动人的整体，让人相信善良和爱永远不会消失。\n\n★ 日本图书销售榜年度冠军\n★ 改编电影由山田凉介、西田敏行主演\n★ "东野圭吾最不一样的作品"——读者评价' WHERE `isbn` = '978-7-5354-5423-2';

UPDATE `book` SET `description` = '卡勒德·胡赛尼的处女作，一个关于友谊、背叛与赎罪的动人故事。阿富汗富家少爷阿米尔与仆人哈桑情同手足，然而在一场风筝比赛后，哈桑遭遇了不幸，而阿米尔因懦弱选择了沉默和逃避。\n\n因为无法面对自己的背叛，阿米尔设计赶走了哈桑。多年后，为了赎罪，他冒着生命危险重返被塔利班占领的喀布尔，踏上了"再次成为好人的路"。胡赛尼以细腻的笔触描绘了阿富汗的社会变迁和人性深处的挣扎，读来令人心碎又感动。\n\n★ 全球销量超过4000万册\n★ 美国图书销售榜冠军\n★ 改编电影获金球奖提名\n★ "为你，千千万万遍"感动全球读者' WHERE `isbn` = '978-7-5442-4532-6';

UPDATE `book` SET `description` = '沈从文最负盛名的代表作，中国乡土文学的经典之作。小说以20世纪30年代湘西边城茶峒为背景，讲述了船夫的孙女翠翠与两兄弟天保、傩送之间纯洁而忧伤的爱情故事。\n\n沈从文以优美如诗的文字，描绘了湘西独特的自然风光和淳朴的民风民俗。小说中的山水、人情、风俗都充满了诗情画意，令人心驰神往。然而故事的结局又是那样的含蓄而忧伤，正如沈从文所说："凡事都有偶然的凑巧，结果却又如宿命的必然。"\n\n★ 中国乡土文学巅峰之作\n★ 入选20世纪中文小说100强\n★ 被翻译成多种文字出版' WHERE `isbn` = '978-7-5302-1754-2';

UPDATE `book` SET `description` = '老舍最著名的代表作之一，以北平一个人力车夫祥子的命运为主线，展现了旧中国底层人民的悲惨生活。祥子年轻时身强力壮，充满了对生活的希望，他省吃俭用买了自己的洋车，却在乱世中被抢走。\n\n祥子一次又一次地挣扎、奋斗，却一次又一次地被现实击垮。最终，这个曾经淳朴善良的车夫在黑暗社会的压迫下彻底堕落，变成了一具行尸走肉。老舍以悲悯的笔触写出了底层人民在旧社会中的绝望与无奈。\n\n★ 老舍最经典的作品之一\n★ 中国现代文学必读经典\n★ 被改编为电影、话剧等多种艺术形式' WHERE `isbn` = '978-7-5402-3613-9';

UPDATE `book` SET `description` = '鲁迅的第一部短篇小说集，中国现代文学的奠基之作。收录了《狂人日记》《孔乙己》《药》《故乡》《阿Q正传》等14篇经典短篇小说。这些作品深刻揭露了封建礼教的吃人本质，批判了国民的劣根性。\n\n鲁迅以其犀利的笔锋和独特的叙事技巧，塑造了阿Q、孔乙己、祥林嫂等一系列不朽的文学形象。一百年来，这些作品对唤醒国民意识、推动社会进步产生了不可估量的影响。"希望是本无所谓有，无所谓无的"——鲁迅在黑暗中点燃的火炬，至今仍在照亮前行的路。\n\n★ 中国现代文学开山之作\n★ 入选中学语文教材篇目最多的作品集\n★ 被翻译成50多种语言' WHERE `isbn` = '978-7-101-04014-2';

UPDATE `book` SET `description` = '巴金的代表作"激流三部曲"的第一部，中国现代文学的经典之作。小说以20世纪20年代的四川成都为背景，描写了封建大家庭高家的没落和分化，以及年轻一代在时代浪潮中的觉醒与反抗。\n\n觉慧是全书的核心人物，他深受新思想的影响，勇敢地反抗封建礼教的束缚，追求自由和真理。巴金以其激情澎湃的笔触，描绘了新旧交替时期中国社会的剧烈变革，以及年轻人在时代洪流中的挣扎与成长。\n\n★ 巴金最具代表性的作品\n★ 激励了几代中国青年的进步之作\n★ 多次改编为电影、电视剧和话剧' WHERE `isbn` = '978-7-5302-1755-9';

UPDATE `book` SET `description` = '老舍最经典的话剧作品，中国话剧史上的不朽丰碑。剧本以北京一家名为"裕泰"的大茶馆为舞台，通过三个时代（清末、民初、抗战后）的横截面，展现了中国社会半个世纪的变迁。\n\n老舍以精炼的笔触，塑造了王利发、常四爷、秦仲义等一个个鲜活的人物形象。茶馆中各色人等的对话与命运，折射出时代的动荡与社会的黑暗。"改良改良，越改越凉"——这句台词道尽了旧中国改良主义的无奈与悲哀。\n\n★ 中国话剧史上最经典的作品之一\n★ 北京人艺的保留剧目，演出超过700场\n★ 被誉为"东方舞台上的奇迹"' WHERE `isbn` = '978-7-5402-3614-6';

UPDATE `book` SET `description` = '曹禺的代表作，中国话剧史上的里程碑之作。故事发生在1925年的一个夏日，通过周鲁两家两代人之间错综复杂的感情纠葛，揭示了封建资产阶级家庭的罪恶与崩溃。\n\n剧本塑造了周朴园、繁漪、周萍、四凤等一系列性格鲜明的人物形象。曹禺以精湛的戏剧技巧，将戏剧冲突层层推进，最终在雷雨之夜将所有矛盾推向高潮。这是一部关于命运、爱情与复仇的永恒悲剧。\n\n★ 中国话剧史上最伟大的作品之一\n★ 中学语文教材必读经典\n★ 被改编为电影、歌剧、芭蕾舞等多种形式' WHERE `isbn` = '978-7-5402-3615-3';

UPDATE `book` SET `description` = '茅盾的代表作，中国社会剖析小说的典范之作。小说以1930年的上海为背景，围绕民族工业资本家吴荪甫与买办金融资本家赵伯韬之间的斗争，全景式地展现了中国民族工业在帝国主义和买办资本双重压迫下的艰难处境。\n\n作品塑造了吴荪甫这一中国文学史上最具代表性的民族资本家形象，他在困境中奋力挣扎、不甘失败的精神，令人既敬佩又惋惜。茅盾以宏大的叙事格局和细腻的笔触，创作了一部中国现代文学的经典之作。\n\n★ 茅盾文学奖命名来源\n★ 中国现代文学史上的重要里程碑\n★ 被翻译成多种外文出版' WHERE `isbn` = '978-7-5302-1756-6';

UPDATE `book` SET `description` = '王小波的代表作，"时代三部曲"之一，也是他流传最广的作品。小说以文革时期为背景，讲述了知青王二与女医生陈清扬之间的爱情故事。在那个压抑荒诞的年代，他们的爱情既是肉体的解放，也是精神的抗争。\n\n王小波以独特的黑色幽默和犀利的思想锋芒，将个人的自由与理性的呐喊融入故事之中。他的文字既荒诞又深刻，既幽默又悲凉。正如他自己所说："一个人只拥有此生此世是不够的，他还应该拥有诗意的世界。"\n\n★ 王小波最经典的作品\n★ 影响了一代中国年轻人的思想启蒙之作\n★ 被誉为"中国自由主义文学的经典"' WHERE `isbn` = '978-7-5442-4533-3';

UPDATE `book` SET `description` = '王小波的杂文集，收录了他最具代表性的杂文作品。在这些文章中，王小波以他独特的幽默和犀利的思辨，对中国社会、文化、历史以及知识分子的命运进行了深刻的思考。\n\n他强调独立思考的重要性，反对盲从和愚昧。在《沉默的大多数》中，他提出"从话语中，你很少能学到人性，从沉默中却能"这一振聋发聩的观点。王小波的文字是中国知识分子的良心之声，至今仍然具有强烈的现实意义。\n\n★ 王小波最具思想性的作品\n★ 中国自由主义思想的启蒙读物\n★ 值得每一个中国人认真阅读的思考之书' WHERE `isbn` = '978-7-5442-4534-0';

UPDATE `book` SET `description` = '哈珀·李的传世之作，美国文学史上最受喜爱的小说之一。故事发生在美国大萧条时期的一个南方小镇，律师阿蒂克斯·芬奇为一名被指控强奸白人女性的黑人男子辩护。\n\n小说通过阿蒂克斯年幼女儿斯库特的视角，讲述了这段关于正义、偏见与勇气的故事。阿蒂克斯·芬奇以自己的行动教会了孩子们什么是真正的勇敢——"明知会输，但还是要坚持去做，并且无论如何都要坚持到底。"\n\n★ 普利策小说奖获奖作品\n★ 全球销量超过4000万册\n★ 美国图书馆借阅率最高的书籍之一\n★ 改编电影获奥斯卡三项大奖' WHERE `isbn` = '978-7-5402-3616-0';

UPDATE `book` SET `description` = '乔治·奥威尔的经典反乌托邦小说，20世纪最具影响力的文学巨著之一。故事设定在1984年的伦敦，整个世界被三个极权主义国家瓜分，大洋国由"老大哥"统治，到处都有电幕监控，思想警察无处不在。\n\n主人公温斯顿在真理部工作，负责改写历史记录。他对这个压抑的社会逐渐产生怀疑和反抗，但最终被思想警察抓获，在101号房间经历了最恐怖的洗脑。奥威尔以惊人的预见力，刻画了一个极权主义社会的可怕图景。\n\n★ 世界反乌托邦文学三大经典之一\n★ 被翻译成65种语言\n★ "老大哥""双重思想""新话"等词汇融入日常语言\n★ 入选全球读者最喜爱的100本书' WHERE `isbn` = '978-7-5354-5424-9';

UPDATE `book` SET `description` = '加西亚·马尔克斯继《百年孤独》之后的又一爱情巨著。小说讲述了弗洛伦蒂诺·阿里萨和费尔明娜·达萨之间持续了半个多世纪的爱情故事。年轻时他们曾热恋，但因现实原因分离，各自经历了不同的人生。\n\n五十一年九个月零四天——这是阿里萨等待达萨的时间。在这漫长的岁月里，他经历了无数段感情，但心中始终只有达萨一人。直到他们都已白发苍苍，终于有机会重新走到一起。马尔克斯以其深邃的洞察力和优美的文笔，探讨了爱情的本质——它是本能，是执念，更是选择。\n\n★ 马尔克斯用尽一生书写的爱情百科全书\n★ 被《纽约时报》评为"人类最伟大的爱情小说"\n★ 改编电影由哈维尔·巴登主演' WHERE `isbn` = '978-7-5442-4535-7';

-- 设置上架时间（按 ISBN 顺序从近到远分布）
UPDATE `book` SET `publish_date` = '2026-05-21' WHERE `isbn` = '978-7-111-54742-2';
UPDATE `book` SET `publish_date` = '2026-05-21' WHERE `isbn` = '978-7-115-42835-7';
UPDATE `book` SET `publish_date` = '2026-05-21' WHERE `isbn` = '978-7-5322-5000-2';
UPDATE `book` SET `publish_date` = '2026-05-21' WHERE `isbn` = '978-7-5443-7010-3';
UPDATE `book` SET `publish_date` = '2026-05-21' WHERE `isbn` = '978-7-5443-5012-9';
UPDATE `book` SET `publish_date` = '2026-05-20' WHERE `isbn` = '978-7-5402-3412-8';
UPDATE `book` SET `publish_date` = '2026-05-20' WHERE `isbn` = '978-7-101-04013-5';
UPDATE `book` SET `publish_date` = '2026-05-20' WHERE `isbn` = '978-7-5442-4531-9';
UPDATE `book` SET `publish_date` = '2026-05-20' WHERE `isbn` = '978-7-80173-502-3';
UPDATE `book` SET `publish_date` = '2026-05-19' WHERE `isbn` = '978-7-5302-1753-5';
UPDATE `book` SET `publish_date` = '2026-05-19' WHERE `isbn` = '978-7-5354-5422-5';
UPDATE `book` SET `publish_date` = '2026-05-19' WHERE `isbn` = '978-7-5402-3612-2';
UPDATE `book` SET `publish_date` = '2026-05-19' WHERE `isbn` = '978-7-5402-3411-1';
UPDATE `book` SET `publish_date` = '2026-05-19' WHERE `isbn` = '978-7-5354-5423-2';
UPDATE `book` SET `publish_date` = '2026-05-19' WHERE `isbn` = '978-7-5442-4532-6';
UPDATE `book` SET `publish_date` = '2026-05-18' WHERE `isbn` = '978-7-5302-1754-2';
UPDATE `book` SET `publish_date` = '2026-05-18' WHERE `isbn` = '978-7-5402-3613-9';
UPDATE `book` SET `publish_date` = '2026-05-18' WHERE `isbn` = '978-7-101-04014-2';
UPDATE `book` SET `publish_date` = '2026-05-18' WHERE `isbn` = '978-7-5302-1755-9';
UPDATE `book` SET `publish_date` = '2026-05-18' WHERE `isbn` = '978-7-5402-3614-6';
UPDATE `book` SET `publish_date` = '2026-05-18' WHERE `isbn` = '978-7-5402-3615-3';
UPDATE `book` SET `publish_date` = '2026-05-15' WHERE `isbn` = '978-7-5302-1756-6';
UPDATE `book` SET `publish_date` = '2026-05-15' WHERE `isbn` = '978-7-5442-4533-3';
UPDATE `book` SET `publish_date` = '2026-05-14' WHERE `isbn` = '978-7-5442-4534-0';
UPDATE `book` SET `publish_date` = '2026-05-12' WHERE `isbn` = '978-7-5402-3616-0';
UPDATE `book` SET `publish_date` = '2026-04-28' WHERE `isbn` = '978-7-5354-5424-9';
UPDATE `book` SET `publish_date` = '2026-04-15' WHERE `isbn` = '978-7-5442-4535-7';

-- 设置部分书籍为预售状态
UPDATE `book` SET `status` = 2 WHERE `isbn` IN ('978-7-5302-1756-6', '978-7-5442-4534-0', '978-7-5402-3616-0', '978-7-5354-5424-9');

-- 设置限时折扣（折扣价、截止时间）
UPDATE `book` SET `discount_price` = 63.20, `discount_end_time` = '2026-12-31 23:59:59' WHERE `isbn` = '978-7-115-42835-7';
UPDATE `book` SET `discount_price` = 28.00, `discount_end_time` = '2026-12-31 23:59:59' WHERE `isbn` = '978-7-5443-7010-3';
UPDATE `book` SET `discount_price` = 22.00, `discount_end_time` = '2026-12-31 23:59:59' WHERE `isbn` = '978-7-5402-3412-8';
UPDATE `book` SET `discount_price` = 30.40, `discount_end_time` = '2026-12-31 23:59:59' WHERE `isbn` = '978-7-5442-4531-9';
UPDATE `book` SET `discount_price` = 35.00, `discount_end_time` = '2026-12-31 23:59:59' WHERE `isbn` = '978-7-0200-4378-3';

-- 设置即将上架书籍状态和日期
UPDATE `book` SET `status` = 3, `expected_shelf_date` = '2026-05-24' WHERE `isbn` = '978-7-5086-5325-0';
UPDATE `book` SET `status` = 3, `expected_shelf_date` = '2026-05-26' WHERE `isbn` = '978-7-5443-4432-6';
UPDATE `book` SET `status` = 3, `expected_shelf_date` = '2026-05-29' WHERE `isbn` = '978-7-5086-4432-5';
UPDATE `book` SET `status` = 3, `expected_shelf_date` = '2026-06-02' WHERE `isbn` = '978-7-0200-4378-3';

-- 更新 book 表关联 author_id
UPDATE `book` SET `author_id` = (SELECT `id` FROM `author` WHERE `author`.`name` = `book`.`author`);

-- 插入目录数据（百年孤独, id=5）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(5, 1, '第一章', 1),
(5, 2, '第二章', 28),
(5, 3, '第三章', 56),
(5, 4, '第四章', 84),
(5, 5, '第五章', 118),
(5, 6, '第六章', 160),
(5, 7, '第七章', 210),
(5, 8, '第八章', 286),
(5, 9, '第九章', 340);

-- 插入目录数据（三体, id=3）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(3, 1, '第一章 科学边界', 1),
(3, 2, '第二章 三体问题', 45),
(3, 3, '第三章 红岸基地', 98),
(3, 4, '第四章 三体文明', 156),
(3, 5, '第五章 面壁计划', 210),
(3, 6, '第六章 黑暗森林', 278),
(3, 7, '第七章 死神永生', 356);

-- 插入目录数据（活着, id=4）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(4, 1, '第一章', 1),
(4, 2, '第二章', 35),
(4, 3, '第三章', 72),
(4, 4, '第四章', 110),
(4, 5, '第五章', 145);

-- 插入目录数据（红楼梦, id=7）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(7, 1, '第一回 甄士隐梦幻识通灵 贾雨村风尘怀闺秀', 1),
(7, 2, '第二回 贾夫人仙逝扬州城 冷子兴演说荣国府', 18),
(7, 3, '第三回 托内兄如海荐西宾 接外孙贾母惜孤女', 32),
(7, 4, '第四回 薄命女偏逢薄命郎 葫芦僧乱判葫芦案', 48),
(7, 5, '第五回 贾宝玉神游太虚境 警幻仙曲演红楼梦', 60),
(7, 6, '第六回 贾宝玉初试云雨情 刘姥姥一进荣国府', 78),
(7, 7, '第七回 送宫花贾琏戏熙凤 宴宁府宝玉会秦钟', 92),
(7, 8, '第八回 贾宝玉奇缘识金锁 薛宝钗巧合认通灵', 108);

-- 插入目录数据（平凡的世界, id=10）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(10, 1, '第一部 第一章', 1),
(10, 2, '第一部 第二章', 28),
(10, 3, '第一部 第三章', 56),
(10, 4, '第一部 第四章', 84),
(10, 5, '第一部 第五章', 112),
(10, 6, '第二部 第一章', 156),
(10, 7, '第二部 第二章', 184),
(10, 8, '第二部 第三章', 210);

-- 插入目录数据（解忧杂货店, id=14）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(14, 1, '第一章 回答在牛奶箱里', 1),
(14, 2, '第二章 深夜的口琴声', 45),
(14, 3, '第三章 在CIVIC车上等到天亮', 92),
(14, 4, '第四章 听着披头士默祷', 140),
(14, 5, '第五章 来自天上的祈祷', 188);

-- 插入目录数据（围城, id=9）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(9, 1, '第一章', 1),
(9, 2, '第二章', 38),
(9, 3, '第三章', 72),
(9, 4, '第四章', 112),
(9, 5, '第五章', 156),
(9, 6, '第六章', 196),
(9, 7, '第七章', 236),
(9, 8, '第八章', 274),
(9, 9, '第九章', 306);

-- 插入目录数据（挪威的森林, id=8）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(8, 1, '第一章', 1),
(8, 2, '第二章', 32),
(8, 3, '第三章', 68),
(8, 4, '第四章', 110),
(8, 5, '第五章', 148),
(8, 6, '第六章', 190),
(8, 7, '第七章', 230),
(8, 8, '第八章', 268),
(8, 9, '第九章', 300),
(8, 10, '第十章', 340),
(8, 11, '第十一章', 375);

-- 插入目录数据（白夜行, id=11）
INSERT INTO `book_chapter` (`book_id`, `chapter_num`, `title`, `page`) VALUES
(11, 1, '第一章', 1),
(11, 2, '第二章', 48),
(11, 3, '第三章', 96),
(11, 4, '第四章', 142),
(11, 5, '第五章', 188),
(11, 6, '第六章', 236),
(11, 7, '第七章', 280),
(11, 8, '第八章', 324),
(11, 9, '第九章', 366),
(11, 10, '第十章', 408),
(11, 11, '第十一章', 446),
(11, 12, '第十二章', 488),
(11, 13, '第十三章', 536);

-- 已有数据库迁移语句
-- ALTER TABLE `book` ADD COLUMN `orig_price` DECIMAL(10,2) NULL COMMENT '原价（划线价）' AFTER `price`;
-- ALTER TABLE `favorite` ADD COLUMN `favorited_price` DECIMAL(10,2) NULL COMMENT '收藏时价格' AFTER `book_id`;
-- ALTER TABLE `book` ADD COLUMN `expected_shelf_date` DATE NULL COMMENT '预计上架日期' AFTER `status`;
-- ALTER TABLE `book` ADD COLUMN `favorited_count` INT DEFAULT 0 COMMENT '收藏数' AFTER `sales`;

-- 补充已存在的收藏记录计数
UPDATE `book` b SET b.`favorited_count` = (SELECT COUNT(*) FROM `favorite` f WHERE f.`book_id` = b.`id`);

-- 插入测试评论（用户 testuser, id=2）
INSERT INTO `review` (`user_id`, `book_id`, `rating`, `content`, `status`) VALUES
(2, 3, 5, '刘慈欣的想象力令人叹为观止，黑暗森林法则让人不寒而栗。强烈推荐！', 1),
(2, 3, 4, '格局宏大，但人物塑造稍显单薄。瑕不掩瑜的国产科幻巅峰。', 1),
(2, 4, 5, '每次读都有不同的感受。活着的意义就是活着本身，余华写得太深刻了。', 1),
(2, 4, 4, '看哭了三次。在那个年代，活着本身就是一种勇气。', 1),
(2, 5, 5, '马尔克斯的魔幻现实主义杰作，家族七代的轮回，读完后久久不能平静。', 1),
(2, 6, 4, '小时候读是童话，长大后读是哲理。重要的东西用眼睛是看不见的。', 1),
(2, 10, 5, '平凡的世界里不平凡的人生。孙少平的身影让我看到了无数奋斗者的影子。', 1),
(2, 14, 4, '东野圭吾难得的温情之作，几个故事环环相扣，读完心里暖暖的。', 1),
(2, 15, 5, '"为你，千千万万遍"——这句话让我泪崩。关于赎罪与救赎的最好的故事。', 1),
(2, 1, 4, 'Java入门经典，讲解透彻，示例丰富。就是太厚了，适合当工具书。', 1),
(2, 2, 5, 'Python入门最好的书之一，从基础到项目循序渐进，很适合零基础读者。', 1),
(2, 11, 5, '东野圭吾的巅峰之作，雪穗和亮司的共生关系写得太绝了，结局震撼。', 1),
-- 补充更多评分数据
(2, 7, 5, '红楼一梦，千古绝唱。每次重读都有新的感悟，曹公的文笔真是登峰造极。', 1),
(2, 7, 4, '人物众多但个个鲜活，唯一缺点是后四十回的续写稍显逊色。', 1),
(2, 8, 4, '村上的文字总有一种淡淡的忧伤，挪威的森林是青春最好的注解。', 1),
(2, 9, 5, '钱钟书的幽默和犀利在这部小说中发挥到了极致，每次读都拍案叫绝。', 1),
(2, 12, 4, '毛姆用冷静的笔触写了一个疯狂的故事，理想主义者的悲歌。', 1),
(2, 13, 3, '太宰治的绝望太过浓烈，读完后心情沉重，但确实是一部震撼人心的作品。', 1),
(2, 16, 4, '沈从文的湘西世界美得像一幅水墨画，翠翠的爱情让人唏嘘。', 1),
(2, 17, 3, '祥子的悲剧是时代的悲剧，老舍先生写得真实而残酷。', 1),
(2, 18, 5, '鲁迅的文字一百年后读来仍然锋利，阿Q的形象永不过时。', 1),
(2, 19, 4, '巴金的文字充满了激情和力量，觉慧的觉醒令人振奋。', 1),
(2, 20, 5, '老舍的幽默中带着心酸，茶馆里的小人物折射出大时代的变迁。', 1),
(2, 21, 4, '曹禺的戏剧张力太强了，雷雨之夜的高潮戏让人喘不过气来。', 1),
(2, 22, 3, '茅盾的社会剖析小说格局很大，但对普通人来说略显艰深。', 1),
(2, 23, 5, '王小波是中国少有的有趣灵魂，黄金时代的自由精神令人向往。', 1),
(2, 24, 4, '沉默的大多数值得每个人阅读，独立思考是王小波留给我们最宝贵的遗产。', 1),
(2, 25, 5, '阿蒂克斯·芬奇是我心中最完美的父亲形象，这本书教会了我什么是勇气。', 1),
(2, 26, 5, '奥威尔的预言在今天看来依然振聋发聩，反乌托邦经典的巅峰之作。', 1),
(2, 27, 4, '马尔克斯的爱情百科全书，五十年的等待让人动容。但相比百年孤独稍逊一筹。', 1);
-- 书斋社区帖子表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书斋社区帖子表';

-- 初始社区帖子数据
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
(2, '读书笔记', '小王子的纯真，是我们丢失的美好', 'https://picsum.photos/seed/principle/400/280', 367, 0, 6);
