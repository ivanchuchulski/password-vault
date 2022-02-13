-- password vault mariadb config
CREATE
    DATABASE IF NOT EXISTS `password_vault`;

USE
    `password_vault`;

GRANT ALL
    ON password_vault.*
    TO jnp_user@localhost
        IDENTIFIED BY "jnp1234";


CREATE TABLE `user`
(
    `username`             VARCHAR(255)   NOT NULL,
    `email`                VARCHAR(255)   NOT NULL,
    `password`             VARBINARY(255) NOT NULL,
    `salt`                 VARBINARY(255) NOT NULL,
    `master_password`      VARBINARY(255) NOT NULL,
    `master_password_salt` VARBINARY(255) NOT NULL,
    CONSTRAINT PRIMARY KEY (`username`)
);


CREATE TABLE `credential`
(
    `username`      VARCHAR(255)   NOT NULL,
    `website`       VARCHAR(255)   NOT NULL,
    `site_username` VARCHAR(255)   NOT NULL,

    `password`      VARBINARY(255) NOT NULL,
    `salt`          VARBINARY(255) NOT NULL,
    `iv`            VARBINARY(255) NOT NULL,

    CONSTRAINT PRIMARY KEY (`website`, `site_username`, `username`),
    CONSTRAINT FOREIGN KEY (`username`) REFERENCES `user` (`username`)
);

CREATE TABLE `session`
(
    `username` VARCHAR(255) NOT NULL,
    CONSTRAINT FOREIGN KEY (`username`) REFERENCES `user` (`username`)
);
