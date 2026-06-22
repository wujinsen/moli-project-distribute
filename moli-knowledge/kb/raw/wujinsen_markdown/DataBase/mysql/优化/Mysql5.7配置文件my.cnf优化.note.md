htps:/blog.csdn.net/wayne_primes/article/details/107972504

[mysql]

port = 306 default-character-set=utf8mb4 socket=/data/mysql/mysql.sock

[mysqld]

# # sumary

#

user = mysql bind-adres = 0.0.0.0 port = 306 basedir=/data/mysql datadir=/data/mysql/data socket=/data/mysql/mysql.sock tmpdir = /tmp pid-file=/tmp/mysqld.pid #skip-grant-tables #skip-networking # 默认安装后eror_log,slow_log ⽇志时间戳默认为UTC log_timestamps=SYSTEM

explicit_defaults_for_timestamp=1 lower_case_table_names=1

table_open_cache = 8 0

# # time out

conect_timeout = 20 wait_timeout = 60

# # conection

#

max_conections = 2 0 max_user_conections = 190 max_conect_erors = 1 0 max_alowed_packet = 1G

# # character set

#

character-set-server = utf8mb4 colation-server = utf8mb4_bin

# # log bin

#

server-id = 1 log_bin = mysql-bin # ROW、STATEMENT、MIXED binlog_format = row sync_binlog = 1 expire_logs_days = 7 binlog_cache_size = 128m max_binlog_cache_size = 512m max_binlog_size = 256M master_info_repository=TABLE relay_log_info_repository=TABLE log_slave_updates=ON binlog_checksum=none

#binlog_ignore_db=information_schema #binlog_ignore_db=mysql #binlog_ignore_db=performation_schema

#binlog_ignore_db=sys #binlog_do_do= replicate_ignore_db=information_schema replicate_ignore_db=mysql replicate_ignore_db=performance_schema replicate_ignore_db=sys #replicate_do_db=

# # gtid

#

gtid_mode = on enforce_gtid_consistency = on

# # slave paralel

#

slave_net_timeout=60 slave_paralel_type=LOGICAL_CLOCK slave_paralel_workers=4

# # log relay

#

relay_log = mysql-relay-bin relay_log_purge = on relay_log_recovery = on max_relay_log_size = 1G

# # log eror

# log_eror=/data/mysql/mysqld_eror.log

# # log slow

slow_query_log = on slow_query_log_file = /data/mysql/mysqld_slow.log long_query_time = 2 log_queries_not_using_indexes = on

# # log general

#

general_log = on general_log_file = /data/mysql/mysqld_gener.log

# # thread pol

# #thread_handling=pol-of-threads #thread_handling=one-thread-per-conection #thread_pol_oversubscribe=8

# # i nodb

#

i nodb_file_per_table=1 i nodb_log_file_size=1024M i nodb_log_bufer_size=64M

#

# pasword policy # after init

#

#validate_pasword_policy=0 #validate_pasword_length=4 #validate_pasword_mixed_case_count=0 #validate_pasword_number_count=0 #validate_pasword_special_char_count=0

⸻版权声明：本⽂为CSDN博主「⼤锅霍⽪久」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上 原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/wayne_primes/article/details/107972504

