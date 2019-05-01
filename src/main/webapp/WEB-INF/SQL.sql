create table electric_room
(
  area_id             int         not null comment '校区id',
  build_id            int         not null comment '楼栋id',
  floor_id            int         not null comment '楼层id',
  room_id             int         not null comment '房间id',
  area_name           char(10)    not null comment '校区名',
  build_name          char(30)    not null comment '楼栋名',
  floor_name          char(30)    not null comment '楼层名',
  room_name           char(30)    not null comment '房间号',
  balance             double(7,2) null comment '电费余额',
  threshold           int         null comment '触发通知的电费值',
  expire_time         date        null comment '通知过期时间',
  last_notify_time    date        null comment '最近一次通知的时间',
  last_notify_balance double(7,2) null comment '最近一次通知时的电费余额',
  phone               char(13)    null comment '手机号码',
  email               char(30)    null comment '联系邮箱',
  primary key (area_id, build_id, room_id)
);