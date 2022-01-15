CREATE
DATABASE `password_vault` IF NOT EXISTS;

USE
`password_vault`;

CREATE TABLE `user`
(
    `username` VARCHAR(255) NOT NULL PRIMARY KEY,
    `email`    VARCHAR(255) NOT NULL UNIQUES,
    `password` VARBINARY    NOT NULL,
    `salt`     VARBINARY    NOT NULL
);


CREATE TABLE `credential`
(
    `username`      VARCHAR(255) NOT NULL FOREIGN KEY REFERENCES `user`(`username`),
    `website`       VARCHAR(255) NOT NULL,
    `site_username` VARCHAR(255) NOT NULL,

    `password`      VARBINARY    NOT NULL,
    `salt`          VARBINARY    NOT NULL,
    `iv`            VARBINARY    NOT NULL,

    PRIMARY KEY (`website`, `site_username`, `username`)
);
