配置好Hive之后，使⽤hive命令启动hive框架。hive启动属于懒加载模式，会⽐较慢

hive;

使⽤show databases命令查看当前数据库信息

hive> show databases; OK default hive Time taken: 3.389 seconds

使⽤ use hive命令，使⽤指定的数据库 hive数据库是我之前创建的

use hive；

创建表，这⾥是创建内表。

内表加载hdfs上的数据，会将被加载⽂件中的内容剪切⾛。 外表没有这个问题，所以在实际的⽣产环境中，建议使⽤外表。 create tablel(reportTime string,msisdn string,apmac string,acmac string,host string,siteType s tring,upPackNum bigint,downPackNum bigint,upPayLoad bigint,downPayLoad bigint,htpStatu s string)row format delimited fields terminated by '\t';

加载数据

这⾥是从hdfs加载数据，也可⽤linux下加载数据 需要local关键字 load data inpath'/HTP_20130313143750.dat' into tablel; 数据加载完毕之后，hdfs的上的数据就被剪切⾛了

请求时间戳 响应时间戳 终端IMEI码 终端IP地址 请求域名 ⽹站类别标签 上⾏数据包 下⾏数据 包 上⾏流量 下⾏流量 请求响应码

执⾏hive 的hsql语句,对数据进⾏统计

select msisdn,sum(upacknum),sum(downpacknum),sum(upayload),sum(downpayload) from l l group by msisdn;

select msisdn as mobile,sum(upacknum) as totalUpPackNum,sum(downpacknum) as totalDow nPackNum,sum(upayload) as totalUpayload,sum(downpayload) as totalDownload from wareh ouse.mobile_log group by msisdn;

