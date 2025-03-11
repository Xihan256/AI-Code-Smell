CREATE TABLE IF NOT EXISTS user
(
    user_id    INT         NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_name  VARCHAR(32) NOT NULL UNIQUE COMMENT 'md5',
    `password` VARCHAR(32) NOT NULL COMMENT 'md5',
    deleted    TINYINT     NOT NULL DEFAULT 0,
    index idx_name (user_name)
);

CREATE TABLE IF NOT EXISTS process_order
(
    order_id     CHAR(64)  NOT NULL PRIMARY KEY,
    user_id      INT       NOT NULL,
    create_time  TIMESTAMP NOT NULL                               DEFAULT CURRENT_TIMESTAMP,
    order_status ENUM ('new', 'processing', 'failed', 'finished') DEFAULT 'new' COMMENT 'new=新建, processing=分析中, failed=分析失败, finished=分析成功',
    doc_url      VARCHAR(128),
    code_url     VARCHAR(128),
    result_url   VARCHAR(128),
    deleted      TINYINT   NOT NULL                               DEFAULT 0,
    index idx_user_id (user_id)
);