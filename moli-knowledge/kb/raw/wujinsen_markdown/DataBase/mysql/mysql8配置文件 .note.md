[mysqld] bind-adres=0.0.0.0 port=306 user=mysql basedir=/usr/local/mysql datadir=/data/mysql socket=/tmp/mysql.sock log-eror=/data/mysql/mysql.er pid-file=/data/mysql/mysql.pid #character config character_set_server=utf8mb4 symbolic-links=0 explicit_defaults_for_timestamp=true

explicit_defaults_for_timestamp=1 lower_case_table_names=1 i nodb_default_row_format=DYNAMIC

table_open_cache = 8 0

# # time out

#

conect_timeout = 20 wait_timeout = 60

# # conection

#

max_conections = 2 0 max_user_conections = 190 max_conect_erors = 1 0 max_alowed_packet = 1G

#

# character set

#

character-set-server = utf8mb4 colation-server = utf8mb4_bin

# # log bin

#

server-id = 1 log_bin = mysql-bin # ROW、STATEMENT、MIXED binlog_format = row sync_binlog = 1 expire_logs_days = 7 binlog_cache_size = 128m max_binlog_cache_size = 512m max_binlog_size = 256M master_info_repository=TABLE relay_log_info_repository=TABLE log_slave_updates=ON binlog_checksum=none

default-storage-engine=I NODB # i nodb

#

i nodb_file_per_table=1 i nodb_log_file_size=2G i nodb_log_bufer_size=64M sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,EROR_FOR_DIVISION_ BY_ZERO,NO_ENGINE_SUBSTITUTION

