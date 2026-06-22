hive.metastore.warehouse.dir：指定数据⽬录，默认值是/user/hive/warehouse；

hive.exec.scratchdir：指定临时⽂件⽬录，默认值是/tmp/hive-${user.name}；

hive.metastore.local：指定是否使⽤本地元数据，此处改为false，使⽤远端的MySQL数据库存储 元数据；

javax.jdo.option.ConectionURL：指定数据库的连接串，此处修改为： jdbc:mysql:/192.168.10.203  306/hivedb?characterEncoding=utf8；

javax.jdo.option.ConectionDriverName：指定数据库连接驱动，此处修改为 com.mysql.jdbc.Driver；

javax.jdo.option.ConectionUserName：指定连接MySQL的⽤户名，根据实际情况设定；

javax.jdo.option.ConectionPasword：指定连接MySQL的密码，根据实际情况设定；

hive.stats.dbclas：指定数据库类型，此处修改为jdbc:mysql；

hive.stats.jdbcdriver：指定数据库连接驱动，此处指定为com.mysql.jdbc.Driver；

hive.stats.dbconectionstring：指定hive临时统计信息的数据库连接⽅式，此处指定为 jdbc:mysql:/192.168.10.203  306/hivestat? useUnicode=true&characterEncoding=utf8$amp;user=hive&pasword=hive$amp;createDataba seIfNotExist=true；

hive.metastore.uris：指定hive元数据访问路径，此处指定为thrift:/127.0.0.1 9083；

