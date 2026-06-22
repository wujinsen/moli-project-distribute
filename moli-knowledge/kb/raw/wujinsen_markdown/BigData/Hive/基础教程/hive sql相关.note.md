create external table dws_pay_user_detail( `user_id` string comment '付费⽤户id', `name` string comment '付费⽤户姓名', `birthday` string COMMENT '', `gender` string COMMENT '', `email` string COMMENT '', `user_level` string COMMENT ''

) COMMENT '付费⽤户表' PARTITIONED BY (`dt` string) stored as parquet location '/warehouse/gmall/dws/dws_pay_user_detail/';

select * from ods_order_info where dt='2019-02-10' limit 1;

