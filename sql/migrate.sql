ALTER TABLE `order` ADD COLUMN `payment_id` BIGINT COMMENT '支付记录ID' AFTER `remark`;
ALTER TABLE `order` ADD COLUMN `express_no` VARCHAR(50) COMMENT '物流单号' AFTER `pay_time`;
ALTER TABLE `order` ADD COLUMN `expire_time` DATETIME COMMENT '订单过期时间' AFTER `express_no`;

CREATE TABLE IF NOT EXISTS `payment` (
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

CREATE TABLE IF NOT EXISTS `order_item` (
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

CREATE TABLE IF NOT EXISTS `review` (
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
