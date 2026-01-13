CREATE TABLE user
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    last_login_time DATETIME(6) NULL,
    created_at      DATETIME(6) NOT NULL,
    created_by      BIGINT       NOT NULL,
    updated_at      DATETIME(6) NULL,
    updated_by      BIGINT NULL,
    version         INT          NOT NULL,
    deleted         TINYINT(1) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE todo_list
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    inbox      TINYINT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by BIGINT       NOT NULL,
    updated_at DATETIME(6) NULL,
    updated_by BIGINT NULL,
    version    INT          NOT NULL,
    deleted    TINYINT(1) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE todo
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    content          VARCHAR(255) NOT NULL,
    due_date         DATETIME(6) NULL,
    done             TINYINT(1) NOT NULL,
    description      TEXT NULL,
    belonged_list_id BIGINT       NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    created_by       BIGINT       NOT NULL,
    updated_at       DATETIME(6) NULL,
    updated_by       BIGINT NULL,
    version          INT          NOT NULL,
    deleted          TINYINT(1) NOT NULL,
    PRIMARY KEY (id),
    INDEX            idx_todo_belonged_list_id (belonged_list_id),
    CONSTRAINT fk_todo_belonged_list_id
        FOREIGN KEY (belonged_list_id)
            REFERENCES todo_list (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
