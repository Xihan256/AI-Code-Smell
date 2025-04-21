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
    order_id           CHAR(64)  NOT NULL PRIMARY KEY,
    user_id            INT       NOT NULL,
    create_time        TIMESTAMP NOT NULL                               DEFAULT CURRENT_TIMESTAMP,
    order_status       ENUM ('new', 'processing', 'failed', 'finished') DEFAULT 'new' COMMENT 'new=新建, processing=分析中, failed=分析失败, finished=分析成功',
    doc_url            VARCHAR(128),
    code_url           VARCHAR(128),
    result_url         VARCHAR(128),
    deleted            TINYINT   NOT NULL                               DEFAULT 0,
    document_component JSON      NULL COMMENT '文档里找到的组件的列表',
    time_cost          BIGINT    NULL COMMENT '耗时',
    index idx_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS order_detail
(
    detail_id      BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    order_id       CHAR(64)     NOT NULL,
    code_component VARCHAR(256) NULL COMMENT '匹配上的组件对应的代码组件',
    component_name VARCHAR(128) NULL COMMENT '匹配上的组件名称',
    probability    DOUBLE       NULL COMMENT '匹配率',
    deleted        TINYINT      NOT NULL DEFAULT 0,
    index idx_order_id (order_id)
);

CREATE TABLE IF NOT EXISTS component_doc_phrases
(
    phrase_id      BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    order_id       CHAR(64)     NOT NULL,
    component_name VARCHAR(128) NULL COMMENT '匹配上的组件名称',
    phrase         TEXT         NOT NULL,
    # 联合索引
    index idx_order_name (order_id, component_name)
);