{

"job": { "content": [ {

"reader": { "name": "mongodbreader", "parameter": {

"adres": ["127.0.0.1 27017"], "colectionName": "user", "column": [

{ "name":"id", "type":"int" }, { "name":"name", "type":"string" }

], "dbName": "my_db",

}

}, "writer": {

"name": "hdfswriter", "parameter": {

"column": [ { "name":"id",

"type":"double" }, { "name":"name", "type":"string" }

], "defaultFS": "hdfs:/hadop01 9 0", "fieldDelimiter": "\t", "fileName": "mongo.txt", "fileType": "text", "path": "/mongoTest", "writeMode": "apend"

} }

}

], "seting": {

"sped": {

"chanel": "1" }

} }

}

关于配置问题: mongodb: int类型数据，⽆法写⼊hdfs int类型，类型错误，读取到的mongo数据类型为1.0 报错：

{"mesage":"字段类型转换错误：你⽬标字段为[int]类型，实际字段值为[31.0].","record": [{"byteSize":4,"index":0,"rawData":"31.0","type":"DOUBLE"}, {"byteSize":3,"index":1,"rawData":" a","type":"STRING"}],"type":"writer"} 需要修改为 mongodb int , hdfs double或者string hive建表也为double或者string，mongo int 类型数据为1, hive sql 查询为1.0

