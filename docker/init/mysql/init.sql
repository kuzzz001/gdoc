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

-- ============ v1.3 新增 ============

CREATE TABLE IF NOT EXISTS `gdoc_folder` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`         VARCHAR(128) NOT NULL,
    `parent_id`    BIGINT       DEFAULT 0 COMMENT '父文件夹ID，0表示根目录',
    `owner_id`     BIGINT       NOT NULL,
    `sort_order`   INT          NOT NULL DEFAULT 0,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_owner` (`owner_id`),
    INDEX `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_comment` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `user_id`      BIGINT       NOT NULL,
    `content`      TEXT         NOT NULL,
    `range_start`  INT          COMMENT '选区起始位置',
    `range_end`    INT          COMMENT '选区结束位置',
    `resolved`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0-未解决 1-已解决',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_doc_id` (`doc_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_comment_reply` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `comment_id`   BIGINT       NOT NULL,
    `user_id`      BIGINT       NOT NULL,
    `content`      TEXT         NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_notification` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL,
    `type`         VARCHAR(32)  NOT NULL COMMENT 'collab_invite/comment/doc_shared/mention',
    `content`      VARCHAR(512) NOT NULL,
    `related_id`   BIGINT       COMMENT '关联ID（文档/评论等）',
    `is_read`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0-未读 1-已读',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_read` (`user_id`, `is_read`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============ v1.4 新增 ============

CREATE TABLE IF NOT EXISTS `gdoc_tag` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`         VARCHAR(64)  NOT NULL,
    `owner_id`     BIGINT       NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_owner_name` (`owner_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_document_tag` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `tag_id`       BIGINT       NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_doc_tag` (`doc_id`, `tag_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add FULLTEXT index for search
ALTER TABLE `gdoc_document` ADD FULLTEXT INDEX `ft_title_content` (`title`, `content`) WITH PARSER ngram;

-- ============ v1.5 新增 ============

CREATE TABLE IF NOT EXISTS `gdoc_template` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`         VARCHAR(128) NOT NULL,
    `description`  VARCHAR(512),
    `content`      LONGTEXT     COMMENT '模板内容HTML',
    `category`     VARCHAR(64)  COMMENT '模板分类',
    `owner_id`     BIGINT       NOT NULL,
    `is_public`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0-私有 1-公开',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_owner` (`owner_id`),
    INDEX `idx_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_team` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`         VARCHAR(128) NOT NULL,
    `description`  VARCHAR(512),
    `avatar_url`   VARCHAR(512),
    `owner_id`     BIGINT       NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_team_member` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `team_id`      BIGINT       NOT NULL,
    `user_id`      BIGINT       NOT NULL,
    `role`         VARCHAR(16)  NOT NULL DEFAULT 'member' COMMENT 'admin/member',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_team_user` (`team_id`, `user_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_group_chat` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `name`         VARCHAR(128) NOT NULL,
    `avatar_url`   VARCHAR(512),
    `owner_id`     BIGINT       NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_group_member` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `group_id`     BIGINT       NOT NULL,
    `user_id`      BIGINT       NOT NULL,
    `role`         VARCHAR(16)  NOT NULL DEFAULT 'member' COMMENT 'admin/member',
    `joined_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_group_message` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `group_id`     BIGINT       NOT NULL,
    `sender_id`    BIGINT       NOT NULL,
    `content`      TEXT         NOT NULL,
    `msg_type`     VARCHAR(16)  NOT NULL DEFAULT 'text',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_group` (`group_id`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gdoc_document_version` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `doc_id`          BIGINT       NOT NULL,
    `content`         LONGTEXT,
    `version_number`  INT          NOT NULL,
    `version_name`    VARCHAR(128),
    `created_by`      BIGINT,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_doc` (`doc_id`),
    UNIQUE KEY `uk_doc_version` (`doc_id`, `version_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
