time=` date +%Y%m%d %H%M%S ` dir=/opt/sql/ MYSQL_USER=rot MYSQL_PAS="v6|8@I36G@"

MYSQL_CON="-u${MYSQL_USER} -p${MYSQL_PAS}" # # Colect al database names except for # mysql, information_schema, and performance_schema # SQL="SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT IN" # 需要排除的数据库 SQL="${SQL} ('mysql','information_schema','performance_schema')"

DBLISTFILE=/tmp/DatabasesToDump.txt echo mysql $MYSQL_CON mysql -u${MYSQL_USER} -p${MYSQL_PAS} -ANe"${SQL}" > ${DBLISTFILE}

DBLIST=" for DB in `cat ${DBLISTFILE}` ; do DBLIST="${DBLIST} ${DB}" ; done

MYSQLDUMP_OPTIONS="-routines-tri gers-single-transaction" mysqldump ${MYSQL_CON} ${MYSQLDUMP_OPTIONS}-databases ${DBLIST} | zip > ${dir}/aldbs-${time}.sql.zip

