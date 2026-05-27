CREATE TABLE IF NOT EXISTS gdoc_user (
    id           BIGSERIAL    PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL UNIQUE,
    password     VARCHAR(256) NOT NULL,
    nickname     VARCHAR(64),
    email        VARCHAR(128),
    avatar_url   VARCHAR(512),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gdoc_document (
    id           BIGSERIAL    PRIMARY KEY,
    title        VARCHAR(256) NOT NULL DEFAULT '无标题文档',
    content      TEXT,
    owner_id     BIGINT       NOT NULL,
    version      INT          NOT NULL DEFAULT 1,
    deleted      SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_owner ON gdoc_document(owner_id);

CREATE TABLE IF NOT EXISTS gdoc_share (
    id           BIGSERIAL    PRIMARY KEY,
    doc_id       BIGINT       NOT NULL,
    token        VARCHAR(64)  NOT NULL UNIQUE,
    permission   VARCHAR(16)  NOT NULL DEFAULT 'view',
    expire_at    TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_token ON gdoc_share(token);
CREATE INDEX IF NOT EXISTS idx_doc_id ON gdoc_share(doc_id);

CREATE TABLE IF NOT EXISTS gdoc_collaborator (
    id           BIGSERIAL    PRIMARY KEY,
    doc_id       BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    role         VARCHAR(16)  NOT NULL DEFAULT 'editor',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (doc_id, user_id)
);

CREATE TABLE IF NOT EXISTS gdoc_snapshot (
    id            BIGSERIAL    PRIMARY KEY,
    doc_id        BIGINT       NOT NULL,
    version       INT          NOT NULL,
    content       TEXT         NOT NULL,
    operation_seq BIGINT,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_doc_version ON gdoc_snapshot(doc_id, version);

CREATE TABLE IF NOT EXISTS gdoc_operation_log (
    id           BIGSERIAL    PRIMARY KEY,
    doc_id       BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    revision     INT          NOT NULL,
    operation    TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_doc_revision ON gdoc_operation_log(doc_id, revision);

CREATE TABLE IF NOT EXISTS gdoc_friendship (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    friend_id    BIGINT       NOT NULL,
    status       SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, friend_id)
);
CREATE INDEX IF NOT EXISTS idx_friend ON gdoc_friendship(friend_id);
CREATE INDEX IF NOT EXISTS idx_status ON gdoc_friendship(status);

CREATE TABLE IF NOT EXISTS gdoc_message (
    id           BIGSERIAL    PRIMARY KEY,
    sender_id    BIGINT       NOT NULL,
    receiver_id  BIGINT       NOT NULL,
    content      TEXT         NOT NULL,
    msg_type     VARCHAR(16)  NOT NULL DEFAULT 'text',
    file_url     VARCHAR(512),
    file_name    VARCHAR(256),
    file_size    BIGINT,
    status       SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sender ON gdoc_message(sender_id);
CREATE INDEX IF NOT EXISTS idx_receiver ON gdoc_message(receiver_id);
CREATE INDEX IF NOT EXISTS idx_created ON gdoc_message(created_at);

CREATE TABLE IF NOT EXISTS gdoc_collab_invitation (
    id           BIGSERIAL    PRIMARY KEY,
    doc_id       BIGINT       NOT NULL,
    inviter_id   BIGINT       NOT NULL,
    invitee_id   BIGINT       NOT NULL,
    role         VARCHAR(16)  NOT NULL DEFAULT 'editor',
    status       SMALLINT     NOT NULL DEFAULT 0,
    message      VARCHAR(512),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_inviter ON gdoc_collab_invitation(inviter_id);
CREATE INDEX IF NOT EXISTS idx_invitee ON gdoc_collab_invitation(invitee_id);
CREATE INDEX IF NOT EXISTS idx_doc ON gdoc_collab_invitation(doc_id);
CREATE INDEX IF NOT EXISTS idx_status ON gdoc_collab_invitation(status);

-- ============ v1.3 新增 ============

CREATE TABLE IF NOT EXISTS gdoc_folder (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(128) NOT NULL,
    parent_id    BIGINT       DEFAULT 0,
    owner_id     BIGINT       NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_folder_owner ON gdoc_folder(owner_id);
CREATE INDEX IF NOT EXISTS idx_folder_parent ON gdoc_folder(parent_id);

CREATE TABLE IF NOT EXISTS gdoc_comment (
    id           BIGSERIAL    PRIMARY KEY,
    doc_id       BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    content      TEXT         NOT NULL,
    range_start  INT,
    range_end    INT,
    resolved     SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_comment_doc ON gdoc_comment(doc_id);
CREATE INDEX IF NOT EXISTS idx_comment_user ON gdoc_comment(user_id);

CREATE TABLE IF NOT EXISTS gdoc_comment_reply (
    id           BIGSERIAL    PRIMARY KEY,
    comment_id   BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    content      TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_reply_comment ON gdoc_comment_reply(comment_id);

CREATE TABLE IF NOT EXISTS gdoc_notification (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    type         VARCHAR(32)  NOT NULL,
    content      VARCHAR(512) NOT NULL,
    related_id   BIGINT,
    is_read      SMALLINT     NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notif_user_read ON gdoc_notification(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notif_created ON gdoc_notification(created_at);
