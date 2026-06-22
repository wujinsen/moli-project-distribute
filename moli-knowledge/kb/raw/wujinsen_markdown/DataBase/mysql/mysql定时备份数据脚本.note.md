htps:/zhuanlan.zhihu.com/p/2 90987

单库备份

#!/bin/bash time=` date +%Y%m%d%H%M%S ` pass=1111 db_name=my_db dir=/root/db-bak/${db_name} mkdir -p $dir mysqldump -uroot -p${pass} --databases ${db_name} | zip > ${dir}/db-${db_name}-${time}.sql.zip

完整备份

time=` date +%Y%m%d%H%M%S ` dir=/root/db-bak/all mkdir -p $dir

MYSQL_USER=root MYSQL_PASS=1111 MYSQL_CONN="-u${MYSQL_USER} -p${MYSQL_PASS}"

# # Collect all database names except for # mysql, information_schema, and performance_schema # SQL="SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT IN" # 需 要 排 除 的 数据 库 SQL="${SQL} ('mysql','information_schema','performance_schema')"

DBLISTFILE=/tmp/DatabasesToDump.txt mysql ${MYSQL_CONN} -ANe"${SQL}" > ${DBLISTFILE}

DBLIST="" for DB in `cat ${DBLISTFILE}` ; do DBLIST="${DBLIST} ${DB}" ; done

MYSQLDUMP_OPTIONS="--routines --triggers --single-transaction" mysqldump ${MYSQL_CONN} ${MYSQLDUMP_OPTIONS} --databases ${DBLIST} | zip > ${dir}/alldbs-${time}.sql.zip

定时执⾏

# 例 如 ： 每 ⽉ 1,11,21号 早 上 6点 执 ⾏ echo "0 6 1,11,21 * * root /root/db-bak/mydb.sh" >> /etc/crontab systemctl restart crond # 或 者 crontab -e # 输 ⼊ 以 下 内 容 保 存 退 出 即可 0 6 1,11,21 * * /root/db-bak/mydb.sh

参考：

htps:/ w.flynsarmy.com/2018/06/how-to-mysqldump-al-databases-excluding-information_sch ema/

# 发布于 2020-1-18

