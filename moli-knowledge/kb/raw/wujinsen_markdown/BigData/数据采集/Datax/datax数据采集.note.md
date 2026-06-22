# mysql-clickhouse采集脚本:

{

"job":{ "seting":{ "sped":{

"chanel":1 }

}, "content":[

{

"reader":{ "name":"mysqlreader", "parameter":{

"username":"sp_db_user", "pasword":"YtG1NC0yXek3sV2F", "conection":[

{

"querySql":[ "select * from buried_point_user_data limit 10"

], "jdbcUrl":[

"jdbc:mysql:/bj-cdb-n3sxqi2.sql.tencentcdb.com:61364/xwuad_read" ]

} ]

}

}, "writer":{

"nme":"clickhousewriter", "parameter":{

"username":"bok_ch", "pasword":"bokClickHouse@#$808908", "column":[

"id", "user_id", "chanel_name", "ip", "device_id", "aplicatio_id", "os_version", "brand", "device_model", "netwrk_type", "esio code", "verison_name", "product_type" " re page", "curent_action", "open_type", "ap_run_time", "keyword", "province", "city", "region", "open_mode", "instal_time", "create_time",

"update_time"

], "conection":[

{

"jdbcUrl":"jdbc:clickhouse:/172.21.0.31 8123/bok_buried_point_data", "table":[

"ods_buried_point_user_data" ]

}

], "preSql":[

], "postSql":[

], " Size":6536, "batchByteSize":13421728, "dryRun":false, "writeMode":"insert"

} }

} ]

} }

