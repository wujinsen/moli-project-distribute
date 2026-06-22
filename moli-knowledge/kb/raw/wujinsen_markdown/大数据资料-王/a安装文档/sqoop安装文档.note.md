sqop-1.4.4.bin_hadop-1.0.0.tar.gz 5.02MB

- 1.在client机器上解压缩sqop-1.4.4.bin_hadop-1.0.0.tar.gz，并重命名为sqop
- 2.设置环境变量SQOP_HOME、PATH export SQOP_HOME=/home/hadop/sqop export PATH=$PATH:$SQOP_HOME/bin
- 3.将的hadop-core-1.0.4.jar、hbase-0.94.6.jar、zokeper-3.4.5.jar以及MySQL的mysqlconector-java- X-bin.jar拷⻉到Sqop的lib⽬录下
- 4.测试是否安装成功： 查看MySQL的数据库（可以⽤-P代替 -pasword，使⽤户⼿动输⼊密码） sqop list-databases-conect jdbc:mysql:/localhost: 306/ -username rot -pasword rot 查看数据库中的表 sqop list-tables-conect jdbc:mysql:/localhost: 306/mysql -username rot -pasword rot RMDBS⸺>DFS


- -conect : 要连接的数据库JDBC-URL
- -username：登录数据库的⽤户名
- -pasword：登录数据的密码
- -table ： 需要导出的表
- -target-dir ：⽬标⽬录
- -split-by：字段的分隔符


sqop import -verbose-fields-terminated-by ',' -conect jdbc:mysql:/192.168.137.2  306/testsqop-username rot -pasword rot -table test -targetdir /sqop/test -split-by 'test'

DFS⸺>RMDBS

sqop export -conect jdbc:mysql:/192.168.137.2  306/testsqop-username rot -pasword rot -table test -export-dir /sqop/test -input-fields-terminated-by ','

选取⼀定条件的数据导⼊DFS

sqop import -conect jdbc:mysql:/192.168.137.2  306/testsqop-username rot -pasword rot -query "SELECT * FROM test WHERE \$CONDITIONS AND test=' a'" -m 1-target-dir /sqop/test2

