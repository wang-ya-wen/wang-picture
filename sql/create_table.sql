# --创建数据库
create database if not exists wang_picture;

# 切换库
use wang_picture;

# 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount   varchar(256)        not null comment '账号',
    userPassword  varchar(512)        not null comment '密码',
    userName      varchar(256)        null     comment '用户昵称',
    userAvatar    varchar(1024)       null     comment '用户头像',
    userProfile   varchar(512)        null     comment '用户简介',
    userRole      varchar(256)  default 'user' not null comment '用户角色',
    editTime      datetime      default  CURRENT_TIMESTAMP  not null comment '编辑时间',
    createTime    datetime      default  CURRENT_TIMESTAMP  not null comment '创建时间',
    updateTime    datetime      default  CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint       default 0  not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate =utf8mb4_unicode_ci;

#图片表
-- auto-generated definition
create table if not exists picture
(
    id           bigint auto_increment comment 'id'
        primary key,
    url          varchar(512)                       not null comment '图片url
',
    name         varchar(128)                       not null comment '图片名称
',
    introduction varchar(512)                       not null comment '简介',
    category     varchar(64)                        not null comment '分类',
    tags         varchar(512)                       not null comment '标签(JSON数组)',
    picSize      bigint                             not null comment '图片体积',
    picWidth     int                                not null comment '图片宽度',
    picHeight    int                                not null comment '图片高度',
    picScale     double                             not null comment '图片宽高比例',
    picFormat    varchar(32)                        not null comment '图片格式',
    userId       bigint                             not null comment '创建用户id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除
',
#     --提升基于图片名称的查询性能
    INDEX idx_name (name),
#用于模糊搜索图片简介
    INDEX idx_introduction (introduction),
#提升基于分类的查询性能
    INDEX idx_category (category),
#提升基于标签的查询性能
    INDEX idx_tags (tags),
#提升基于用户ID的查询性能
    INDEX idx_userId (userId)

) comment '图片' collate =utf8mb4_unicode_ci;

ALTER TABLE picture
       ADD COLUMN spaceId bigint null comment '空间id(为空表示公共空间';
CREATE INDEX idx_spaceId ON picture(spaceId);

create table space
(
    id         bigint auto_increment comment 'id'
        primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别:0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    userId     bigint                             not null comment '创建用户的id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIME comment '更新时间时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    spaceId    mediumtext                         null comment '空间的id'
);

create index idx_spaceLevel
    on space (spaceLevel);

create index idx_spaceName
    on space (spaceName);

create index idx_userId
    on space (userId);

ALTER TABLE picture
     ADD COLUMN picColor varchar(16) null comment '图片主色调';

ALTER TABLE picture
    ADD COLUMN thumbnailUrl varchar(512) null comment '图片缩略图url';

ALTER TABLE space
    ADD COLUMN spaceType int default 0 not null comment '空间类型:0-个人空间 1-团队空间';
CREATE INDEX idx_spaceType ON space(spaceType);

# 空间成员表
create table if not exists space_user
(
    id          bigint auto_increment comment 'id' primary key ,
    spaceId     bigint                             not null comment '空间id',
    userId      bigint                           not null comment '用户id',
    spaceRole   varchar(128) default 'viewer'     null comment '空间角色:viewer/editor/admin',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
#     索引设计
    UNIQUE KEY uk_spaceId_userId (spaceId, userId), #唯一索引，用户在一个空间中只能有一个角色
    INDEX idx_spaceId (spaceId),
    INDEX idx_userId (userId)
) comment '空间用户关联' collate =utf8mb4_unicode_ci;