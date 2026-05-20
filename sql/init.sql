-- 网上书店系统 数据库初始化脚本
-- 创建时间：2026/5/18

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
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
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
    `category_id` BIGINT COMMENT '分类ID',
    `description` TEXT COMMENT '简介',
    `cover_url` VARCHAR(500) COMMENT '封面图',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1上架/0下架）',
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
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
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
INSERT INTO `book` (`isbn`, `title`, `author`, `publisher`, `price`, `stock`, `category_id`, `description`, `cover_url`) VALUES
('978-7-111-54742-2', 'Java核心技术卷I', '凯·S·霍斯特曼', '机械工业出版社', 119.00, 100, 3, 'Java技术经典参考书', '/pictures/Java核心技术.jpg'),
('978-7-115-42835-7', 'Python编程：从入门到实践', '埃里克·马瑟斯', '人民邮电出版社', 79.00, 80, 3, 'Python入门经典', '/pictures/python编程.jpg'),
('978-7-5322-5000-2', '三体', '刘慈欣', '重庆出版社', 68.00, 50, 2, '科幻巨著', '/pictures/三体.jpg'),
('978-7-5443-7010-3', '活着', '余华', '作家出版社', 35.00, 60, 1, '当代经典小说', '/pictures/活着.jpg');