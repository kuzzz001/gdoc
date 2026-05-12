CREATE TABLE IF NOT EXISTS `gdoc_user` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`     VARCHAR(64)  NOT NULL UNIQUE,
    `password`     VARCHAR(256) NOT NULL COMMENT 'BCrypt加密',
    `nickname`     VARCHAR(64),
    `email`        VARCHAR(128),
    `avatar_url`   VARCHAR(512),
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_document` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `title`        VARCHAR(256) NOT NULL DEFAULT '无标题文档',
    `content`      LONGTEXT     COMMENT '文档正文JSON（最新版本）',
    `owner_id`     BIGINT       NOT NULL,
    `version`      INT          NOT NULL DEFAULT 1 COMMENT '当前版本号',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0-正常 1-已删除',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_share` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `token`        VARCHAR(64)  NOT NULL UNIQUE,
    `permission`   VARCHAR(16)  NOT NULL DEFAULT 'view' COMMENT 'view / editor',
    `expire_at`    DATETIME,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_token` (`token`),
    INDEX `idx_doc_id` (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_collaborator` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `user_id`      BIGINT       NOT NULL,
    `role`         VARCHAR(16)  NOT NULL DEFAULT 'editor' COMMENT 'editor / viewer',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_doc_user` (`doc_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_snapshot` (
    `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`        BIGINT       NOT NULL,
    `version`       INT          NOT NULL,
    `content`       LONGTEXT     NOT NULL COMMENT '该版本完整文档JSON',
    `operation_seq` BIGINT       COMMENT '操作序列号',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_doc_version` (`doc_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_operation_log` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `user_id`      BIGINT       NOT NULL,
    `revision`     INT          NOT NULL,
    `operation`    TEXT         NOT NULL COMMENT '操作JSON',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_doc_revision` (`doc_id`, `revision`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_friendship` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL,
    `friend_id`    BIGINT       NOT NULL,
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0-待确认 1-已接受 2-已拒绝 3-已拉黑',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    INDEX `idx_friend` (`friend_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_message` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `sender_id`    BIGINT       NOT NULL,
    `receiver_id`  BIGINT       NOT NULL,
    `content`      TEXT         NOT NULL COMMENT '消息内容',
    `msg_type`     VARCHAR(16)  NOT NULL DEFAULT 'text' COMMENT 'text/image/file/system',
    `file_url`     VARCHAR(512) COMMENT '文件/图片URL',
    `file_name`    VARCHAR(256) COMMENT '文件名',
    `file_size`    BIGINT       COMMENT '文件大小(字节)',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0-已发送 1-已送达 2-已读',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_receiver` (`receiver_id`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_collab_invitation` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `inviter_id`   BIGINT       NOT NULL,
    `invitee_id`   BIGINT       NOT NULL,
    `role`         VARCHAR(16)  NOT NULL DEFAULT 'editor' COMMENT 'editor/viewer',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0-待确认 1-已接受 2-已拒绝 3-已取消',
    `message`      VARCHAR(512) COMMENT '邀请附言',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_inviter` (`inviter_id`),
    INDEX `idx_invitee` (`invitee_id`),
    INDEX `idx_doc` (`doc_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
