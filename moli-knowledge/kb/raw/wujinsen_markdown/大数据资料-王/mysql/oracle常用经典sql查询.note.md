oracle 常⽤经典 SQL 查询 常⽤ SQL 查询：

- 1 、查看表空间的名称及⼤⼩

- select t.tablespace_name,round(sum(bytes/(1024*1024),0) ts_size from dba_tablespaces t, dba_data_files d where t.tablespace_name = d.tablespace_name group by t.tablespace_name;


- 2 、查看表空间物理⽂件的名称及⼤⼩ select tablespace_name, file_id, file_name, round(bytes/(1024*1024),0) total_space from dba_data_files order by tablespace_name;
- 3 、查看回滚段名称及⼤⼩ select segment_name, tablespace_name,r.status, (init ial_extent/1024)InitialExtent,(next_extent/1024) NextExtent, max_extents, v.curext CurExtent From dba_rolback_segs r, v$rolstat v Where r.segment_id = v.usn(+) order by segment_name;
- 4 、查看控制⽂件 select name from v$controlfile;
- 5 、查看⽇志⽂件 select member from v$logf ile;
- 6 、查看表空间的使⽤情况 select sum(bytes)/(1024*1024) asfre_space,tablespace_name from dba_fre_space group by tablespace_name; SELECT A.TABLESPACE_NAME,A.BYTESTOTAL,B.BYTES USED, C.BYTES FRE, (B.BYTES*10)/A.BYTES "%USED",(C.BYTES*10)/A.BYTES "% FRE" FROM SYS.SM$TS_AVAIL A,SYS.SM$TS_USEDB,SYS.SM$TS_FRE C WHERE A.TABLESPACE_NAME=B.TABLESPACE_NAMEAND A.TABLESPACE_NAME=C.TABLESPACE_NAME;
- 7 、查看数据库库对象 select owner, object_type, status, count(*)count# from al_objects group by owner, object_type, status;
- 8 、查看数据库的版本 Select version FROMProduct_component_version


Where SUBSTR(PRODUCT,1,6)='Oracle';9 、查看数据库的创建⽇期和归档⽅式 Select Created, Log_Mode, Log_Mode FromV$Database; 10 、捕捉运⾏很久的 SQL column username format a12 column opname format a16 column progres format a8 select username,sid,opname, round(sofar*10 / totalwork,0)| '%' asprogres, time_remaining,sql_text from v$sesion_longops , v$sql where time_remaining <> 0 and sql_adres = adres and sql_hash_value = hash_value /

- 1 。查看数据表的参数信息


SELECT partition_name, high_value,high_value_length, tablespace_name, pct_fre, pct_used, ini_trans, max_trans,init ial_extent, next_extent, min_extent, max_extent,pct_increase, FRELISTS, frelist_groups, LOGING, BUFER_POL,num_rows, blocks, empty_blocks, avg_space, chain_cnt,avg_row_len, sample_size, last_analyzed FROM dba_tab_partit ions

-WHERE table_name = :tname AND table_owner= :towner ORDER BY partition_position

- 12. 查看还没提交的事务 select * from v$locked_object; select * from v$transaction;
- 13 。查找 object 为哪些进程所⽤ select p.spid, s.sid, s.serial# serial_num, s.username user_name, a.type object_type, s.osuser os_user_name, a.owner, a.object object_name,


decode(sign(48 - comand), 1, to_char(comand), 'Action Code #' |to_char(comand) ) action, p.program oracle_proces, s.terminal terminal, s.program program, s.status sesion_status from v$sesion s, v$aces a, v$proces p where s.padr = p.adr and s.type = 'USER' and a.sid = s.sid and a.object='SUBSCRIBER_ATR' order by s.username, s.osuser14 。回滚段查看 select rownum,sys.dba_rolback_segs.segment_name Name, v$rolstat.extents Extents, v$rolstat.rsize Size_in_Bytes,v$rolstat.xacts XActs, v$rolstat.gets Gets, v$rolstat.waitsWaits, v$rolstat.writes Writes, sys.dba_rolback_segs.status status fromv$rolstat, sys.dba_rolback_segs, v$rolname where v$rolname.name(+) =sys.dba_rolback_segs.segment_name and v$rolstat.usn (+) = v$rolname.usn orderby rownum

- 15 。耗资源的进程（ top sesion ） select s.schemaname schema_name,decode(sign(48 - comand), 1, to_char(comand), 'Action Code #' |to_char(comand) ) action, status sesion_status, s.osuser os_user_name,s.sid, p.spid , s.serial# serial_num, nvl(s.username, '[Oracle proces]')user_name, s.terminal terminal, s.program program, st.value criteria_valuefrom v$sestat st, v$sesion s , v$proces p where st.sid = s.sid and st.statistic# =to_number('38') and ('AL' = 'AL' or s.status = 'AL') and p.adr = s.padrorder by st.value desc, p.spid asc, s.username asc, s.osuser asc
- 16 。查看锁（ lock ）情况 select /*+ RULE */ ls.osuser os_user_name,ls.username user_name, decode(ls.type, 'RW', 'Row wait enqueuelock', 'TM', 'DML enqueue lock', 'TX', 'Transaction enqueue lock', 'UL', 'Usersuplied lock') lock_type, o.object_name object, decode(ls.lmode, 1,nul, 2, 'Row Share', 3, 'Row Exclusive', 4, 'Share', 5, 'Share RowExclusive', 6, 'Exclusive', nul) lock_mode, o.owner, ls.sid, ls.serial#serial_num, ls.id1, ls.id2 from sys.dba_objects o, ( select s.osuser,s.username, l.type,


- l.lmode, s.sid, s.serial#, l.id1, l.id2from v$sesion s, v$lock l where s.sid = l.sid ) ls whereo.object_id = ls.id1 and o.owner <> 'SYS' order by o.owner,o.object_name
- 17 。查看等待（ wait ）情况 SELECT v$waitstat.clas, v$waitstat.countcount, SUM(v$systat.value) sum_value FROM v$waitstat, v$systat WHEREv$systat.name IN ('db block gets', 'consistent gets') group byv$waitstat.clas, v$waitstat.count
- 18 。查看 sga 情况 SELECT NAME, BYTES FROM SYS.V_$SGASTATORDER BY NAME ASC
- 19 。查看 catched object SELECT owner, name, db_link, namespace, type, sharable_mem, loads, executions, locks, pins, kept FROM v$db_object_cache
- 20 。查看 V$SQLAREA SELECT SQL_TEXT, SHARABLE_MEM,PERSISTENT_MEM, RUNTIME_MEM, SORTS, VERSION_COUNT, LOADED_VERSIONS,OPEN_VERSIONS, USERS_OPENING, EXECUTIONS, USERS_EXECUTING, LOADS, FIRST_LOAD_TIME,INVALIDATIONS, PARSE_CALS, DISK_READS, BUFER_GETS, ROWS_PROCESED FROM V$SQLAREA
- 21 。查看 object 分类数量 select decode (o.type#,1,'INDEX' ,2,'TABLE' , 3 , 'CLUSTER' , 4, 'VIEW' , 5 ,'SYNONYM' , 6 , 'SEQUENCE' , 'OTHER') object_type , count(*) quantity from sys.obj$ o where o.type# > 1 group bydecode (o.type#,1,'INDEX' , 2,'TABLE' , 3 , 'CLUSTER' , 4, 'VIEW' , 5 , 'SYNONYM' , 6, 'SEQUENCE' , 'OTHER' ) union select 'COLUMN' , count(*) from sys.col$ unionselect 'DB LINK' , count(*) from


2 。按⽤户查看 object 种类

- select u.name schema, sum(decode(o.type#,1, 1, NUL) indexes, sum(decode(o.type#, 2, 1, NUL) tables,sum(decode(o.type#, 3, 1, NUL) clusters, sum(decode(o.type#, 4, 1, NUL)views, sum(decode(o.type#, 5, 1, NUL) synonyms, sum(decode(o.type#, 6, 1,NUL) sequences, sum(decode(o.type#, 1, NUL, 2, NUL, 3,NUL, 4, NUL, 5, NUL, 6, NUL, 1) others from sys.obj$ o, sys.user$ u whereo.type# >= 1 and u.user# =


- o.owner# and u.name <> 'PUBLIC' groupby u.name order by sys.link$ union select 'CONSTRAINT' ,count(*) from sys.con$


- 23 。有关 conection 的相关信息


- 1 ）查看有哪些⽤户连接

- select s.osuser os_user_name,decode(sign(48 - comand), 1, to_char(comand), 'Action Code #' | to_char(comand) )action, p.program oracle_proces, status sesion_status, s.terminal terminal,s.program program, s.username user_name,s.fixed_table_sequence activity_meter, ' query,


- 0 memory, 0 max_memory, 0 cpu_usage, s.sid,s.serial# serial_num from v$sesion s, v$proces p wheres.padr=p.adr and s.type = 'USER'


order by s.username, s.osuser

- 2 ）根据 v.sid 查看对应连接的资源占⽤等情况 select n.name,

v.value, n.clas, n.statistic# from v$statname n, v$sestat v where v.sid = 71 and v.statistic# = n.statistic# order by n.clas, n.statistic#

- 3 ）根据 sid 查看对应连接正在运⾏的sql select /*+ PUSH_SUBQ */ comand_type, sql_text, sharable_mem, persistent_mem, runtime_mem, sorts, version_count, loaded_versions, open_versions, users_opening, executions, users_executing, loads, first_load_time, invalidat ions, parse_cals,


disk_reads, bufer_gets, rows_procesed, sysdate start_time, sysdate finish_t ime,'>' | adre sql_adres, 'N' status from v$sqlarea where adres = (select sql_adres fromv$sesion where sid = 71)

- 24 ．查询表空间使⽤情况 select a.tablespace_name " 表空间名称 ", 10-round(nvl(b.bytes_fre,0)/a.bytes_aloc)*10,2)" 占⽤率 (%)", round(a.bytes_aloc/1024/1024,2) " 容量 (M)", round(nvl(b.bytes_fre,0)/1024/1024,2)" 空闲 (M)", round(a.bytes_aloc-nvl(b.bytes_fre,0)/1024/1024,2)" 使⽤ (M)", Largest " 最⼤扩展段 (M)", to_char(sysdate,' y- m-d h24:mi:s')" 采样时间 " from (select f.tablespace_name, sum(f.bytes) bytes_aloc, sum(decode(f.autoextensible,'YES',f.maxbytes,'NO',f.bytes)maxbytes from dba_data_files f

- group by tablespace_name) a, (select f.tablespace_name, sum(f.bytes) bytes_fre from dba_fre_space f
- group by tablespace_name) b, (select round(max(f.length)*16/1024,2)Largest, ts.name tablespace_name from sys.fet$f, sys.file$ tf,sys.ts$ ts where ts.ts#=f.ts# andf.file#=tf.relfile# and ts.ts#=tf.ts# group by ts.name, tf.blocks) c where a.tablespace_name = b.tablespace_nameand a.tablespace_name = c.tablespace_name


- 25. 查询表空间的碎⽚程度 selectablespace_name,count(tablespace_name) from dba_fre_space group bytablespace_name having count(tablespace_name)>10; alter tablespace name coalesce; alter table name dealocate unused; create or replace view ts_blocks_v as


- select tablespace_name,block_id,bytes,blocks,'frespace' segment_name from dba_fre_space union al selectablespace_name,block_id,bytes,blocks,segment_name from dba_extents; select * from ts_blocks_v; selectablespace_name,sum(bytes),max(bytes),count(block_id) from dba_fre_space group by tablespace_name;
- 26 。查询有哪些数据库实例在运⾏ select inst_name from v$active_instances;


=

# 创建数据库 -l ok $ORACLE_HOME/rdbms/admin/buildal.sql # create database db01 maxlogf iles 10 maxdatafiles 1024maxinstances 2 logf ile

- GROUP 1('/u01/oradata/db01/log_01_db01.rdo') SIZE 15M,
- GROUP 2('/u01/oradata/db01/log_02_db01.rdo') SIZE 15M,
- GROUP 3('/u01/oradata/db01/log_03_db01.rdo') SIZE 15M, datafile'u01/oradata/db01/system_01_db01.dbf') SIZE 10M, undo tablespace UNDO datafile'/u01/oradata/db01/undo_01_db01.dbf' SIZE 40M default temporary tablespace TEMP tempfile'/u01/oradata/db01/temp_01_db01.dbf' SIZE 20M extent management local uniform size 128k character set AL32UTE8 national character set AL16UTF16 set time_zone='America/New_York';


# 数据字典 #

set wrap of select * from v$dba_users; grant select on table_name to user/rule; select * from user_tables; select * from al_tables; select * from dba_tables; revoke dba from user_name; shutdown i mediate startup nomount select * from v$instance;

select * from v$sga; select * from v$tablespace; alter sesion set nls_language=amer ican; alter database mount; select * from v$database; alter database open; desc dictionary select * from dict; desc v$fixed_table; select * from v$fixed_table;setoracle_sid=foxcon select * from dba_objects; set serveroutput on execute dbms_output.put_line('sfasd');

# 控制⽂件 # select * from v$database; select * from v$tablespace; select * from v$logf ile; select * from v$log; select * from v$backup; /* 备份⽤户表空间 */ alter tablespace users begin backup; select * from v$archived_log; select * from v$controlfile; alter system setcontrol_files='$ORACLE_HOME/oradata/u01/ctrl01.ctl', '$ORACLE_HOME/oradata/u01/ctrl02.ctl'scope=spfile; cp $ORACLE_HOME/oradata/u01/ctrl01.ctl$ORACLE_HOME/oradata/u01/ctrl02.ctl startup pfile='./initSID.ora' select * from v$parameter where name like'control%' ; show parameter control; select * from v$controlfile_record_section; select * from v$tempfile; /* 备份控制⽂件 */ alter database backup controlfile to'./filepath/control.bak'; /* 备份控制⽂件，并将⼆进制控制⽂件变为了 asc 的⽂本⽂件 */ alter database backup controlfile to trace;

# redo log # archive log list;

alter system archive log start;- 启动⾃动存档 alter system switch logf ile;- 强⾏进⾏⼀次⽇志 switch alter system checkpoint;- 强制进⾏⼀次 checkpointalter tablspaceusers begin backup; alter tablespace ofline; /*checkpoint 同步频率参数 FAST_START_MTR_TARGET, 同步频率越⾼，系统恢复所需时 间越短 */ show parameter fast; show parameter log_checkpoint; /* 加⼊⼀个⽇志组 */ alter database ad logf ile group 3('/$ORACLE_HOME/oracle/ora_log_f ile6.rdo' size 10M); /* 加⼊⽇志组的⼀个成员 */ alter database ad logf ile member'/$ORACLE_HOME/oracle/ora_log_f ile6.rdo' to group 3; /* 删除⽇志组 : 当前⽇志组不能删；活动的⽇志组不能删；⾮归档的⽇志组不能删 */ alter database drop logf ile group 3; /* 删除⽇志组中的某个成员，但每个组的最后⼀个成员不能被删除 */ alter databse drop logf ile member'$ORACLE_HOME/oracle/ora_log_f ile6.rdo'; /* 清除在线⽇志 */ alter database clear logf ile'$ORACLE_HOME/oracle/ora_log_f ile6.rdo'; alter database clear logf ile group 3; /* 清除⾮归档⽇志 */ alter database clear unarchived logf ilegroup 3; /* 重命名⽇志⽂件 */ alter database rename file'$ORACLE_HOME/oracle/ora_log_f ile6.rdo' to '$ORACLE_HOME/oracle/ora_log_f ile6a.rdo'; show parameter db_create; alter system setdb_create_online_log_dest_1='path_name'; select * from v$log; select * from v$logf ile; /* 数据库归档模式到⾮归档模式的互换 , 要启动到 mount 状态下才能改变 ;startup mount; 然后 再打开数据库 .*/ alter database noarchivelog/archivelog; achive log start; - 启动⾃动归档 alter system archive al; －－⼿⼯归档所有⽇志⽂件 select * from v$archived_log; show parameter log_archive;

# 分析⽇志⽂件 logmnr #1) 在 init.ora 中 set utl_file_dir 参数

- 2) 重新启动 oracle


- 3) create ⽬录⽂件 desc dbms_logmnr_d; dbms_logmnr_d.build;
- 4) 加⼊⽇志⽂件 ad/remove log file dhms_logmnr.ad_logfile dbms_logmnr.removefile
- 5) start logmnr dbms_logmnr.start_logmnr
- 6) 分析出来的内容查询 v$logmnr_content -sqlredo/sqlundo 实践： desc dbms_logmnr_d; /* 对数据表做⼀些操作，为恢复操作做准备 */ update 表 set qty=10 where stor_id=6380; delete 表 where stor_id=706; / */ utl_file_dir 的路径 executedbms_logmnr_d.build('foxdict.ora','$ORACLE_HOME/oracle/admin/fox/cdump'); execute dbms_logmnr.ad_logfile('$ORACLE_HOME/oracle/ora_log_file6.log',dbms_logmnr.newfile); execute dbms_logmnr.start_logmnr(dictfilename=>'$ORACLE_HOME/oracle/admin/fox/cdump/foxdict.o ra');


# tablespace # select * form v$tablespace; select * from v$datafile; /* 表空间和数据⽂件的对应关系 */

- select t1.name,t2.name from v$tablespacet1,v$datafile t2 where t1.ts#=t2.ts#; alter tablespace users ad datafile 'path'size 10M; select * from dba_rolback_segs; /* 限制⽤户在某表空间的使⽤限额 */ alter user user_name quota 10m on tablespace_name; create tablespace x [datafile'path_name/datafile_name'] [size x] [extent management local/dictionary] [default storage( x)]; exmple: create tablespace userdata datafile'$ORACLE_HOME/oradata/userdata01.dbf' size 10M AUTOEXTEND ON NEXT 5M MAXSIZE 20M; create tablespace userdata datafile'$ORACLE_HOME/oradata/userdata01.dbf' size 10Mextent management dictionary default storage(initial 10k next 10k pctincrease 10) ofline;


/*9i 以后， oracle 建议使⽤ local 管理，⽽不使⽤ dictionary 管理，因为 local 采⽤ bitmap 管理表空 间 ，不会产⽣系统表空间的⾃愿争⽤ ;*/ create tablespace userdata datafile'$ORACLE_HOME/oradata/userdata01.dbf' size 10Mextent management local uniform size 1m; create tablespace userdata datafile '$ORACLE_HOME/oradata/userdata01.dbf'size 10M extent management local autoalocate; /* 在创建表空间时，设置表空间内的段空间管理模式，这⾥⽤的是⾃动管理 */ create tablespace userdata datafile'$ORACLE_HOME/oradata/userdata01.dbf' size 10Mextent management local uniform size 1m segment space management auto; alter tablespace userdata mininum extent10; alter tablespace userdata defaultstorage(init ial 1m next 1m pctincrease 20); /*undo tablespace( 不能被⽤在字典管理模下 ) */ create undo tablespace undo1 datafile'$ORACLE_HOME/oradata/undo101.dbf' size 40Mextent management local; show parameter undo; /*temporary tablespace*/ create temporary tablespace userdatatempfile '$ORACLE_HOME/oradata/undo101.dbf' size 10m extent management local; /* 设置数据库缺省的临时表空间 */ alter database default temporary tablespacetablespace_name; /* 系统 / 临时 / 在线的 undo 表空间不能被 ofline*/ alter tablespace tablespace_nameofline/online; alter tablespace tablespace_name read only; /* 重命名⽤户表空间 */ alter tablespace tablespace_name renamedatafile '$ORACLE_HOME/oradata/undo101.dbf' to '$ORACLE_HOME/oradata/undo102.dbf'; /* 重命名系统表空间 , 但在重命名前必须将数据库shutdown, 并重启到 mount 状态 */ alter database rename file'$ORACLE_HOME/oradata/system01.dbf' to '$ORACLE_HOME/oradata/system02.dbf'; drop tablespace userdata including contentsand datafiles; -drop tablespce /*resize tablespace,autoextend datafilespace*/ alter database datafile'$ORACLE_HOME/oradata/undo102.dbf' autoextend on next 10m maxsize 50M; /*resize datafile*/ alter database datafile'$ORACLE_HOME/oradata/undo102.dbf' resize 50m; /* 给表空间扩展空间 */ alter tablespace userdata ad datafile'$ORACLE_HOME/oradata/undo102.dbf' size 10m;

/* 将表空间设置成 OMF 状态 */ alter system setdb_create_file_dest='$ORACLE_HOME/oradata'; create tablespace userdata; -use OMFstatus to create tablespace;drop tablespace userdata; user OMF status to droptablespace; select * fromdba_tablespace/v$tablespace/dba_data_f iles; /* 将表的某分区移动到另⼀个表空间 */ alter table table_name move partitionpartition_name tablespace tablespace_name;

# ORACLE storage structure and relationships # /* ⼿⼯分配表空间段的分区 (extend) ⼤⼩ */ alter table kong.test12 alocatextent(size 1mdatafile '$ORACLE_HOME/oradata/undo102.dbf'); alter table kong.test12 dealocate unused; - 释放表中没有⽤到的分区 show parameter db; alter system set db_8k_cache_size=10m; - 配置 8k 块的内存空间块参数 select * fromdba_extents/dba_segments/data_tablespace; select * fromdba_fre_space/dba_data_file/data_tablespace; /* 数据对象所占⽤的字节数 */ select sum(bytes) from dba_extents whereonwer='kong' and segment_name ='table_name';

# UNDO Data # show parameter undo; alter tablespace users ofline normal; alter tablespace users ofline i mediate; recover datafile'$ORACLE_HOME/oradata/undo102.dbf'; alter tablespace users online ; select * from dba_rolback_segs; alter system set undo_tablespace=undotbs1; /* 忽略回滚段的错误提⽰ */ alter system set undo_supres_erors=true; /* 在⾃动管理模式下 , 不会真正建⽴ rbs1; 在⼿⼯管理模式则可以建⽴ , 且是私有回滚段 */ create rolback segment rbs1 tablespaceundotbs; desc dbms_flashback; /* 在提交了修改的数据后 ,9i 提供了旧数据的回闪操作 , 将修改前的数据只读给⽤户看 , 但这 部 分数据不会又恢复在表中 , ⽽是旧数据的⼀个映射 */ execute dbms_flashback.enable_at_time('26-JAN-04 12 17  0 pm'); execute dbms_flashback.disable; /* 回滚段的统计信息 */select end_time,begin_time,undoblks from v$undostat; /*undo 表空间的⼤⼩计算公式 : UndoSpace=[UR * (UPS * DBS)] + (DBS * 24) UR :UNDO_RETENTION 保留的时间 ( 秒 )

UPS : 每秒的回滚数据块 DBS: 系统 EXTENT 和 FILE SIZE( 也就是 db_block_size)*/ select * fromdba_rolback_segs/v$rolname/v$rolstat/v$undostat/v$sesion/v$transaction; show parameter transactions; show parameter rolback; /* 在⼿⼯管理模式下 , 建⽴公共的回滚段 */ create public rolback segment prbs1tablespace undotbs; alter rolback segment rbs1 online; - 在⼿⼯管理模式 /* 在⼿ ⼯ 管 理 模 式 中 ,initSID.ora 中 指 定 undo_management=manual 、 rolback_segment=('rbs1','rbs2',.) 、 transactions=10 、transactions_per_rolback_segment=10 然后 shutdowni mediate ,startup pfile=.\ ?.ora */

# Managing Tables # /*char type maxlen=2 0;varchar2 typemaxlen=4 0 bytes rowid 是 18 位的 64 进制字符串 (10 个 bytes 80 bits) rowid 组成 : object#( 对象号 )-32bits,6 位 rfile#( 相对⽂件号 )-10bits,3 位 block#( 块号 )-2bits,6 位 row#( ⾏号 )-16bits,3 位 64 进制 : A-Z,a-z,0-9,/,+ 共 64 个符号 dbms_rowid 包中的函数可以提供对 rowid 的解释 */ selectrowid,dbms_rowid.rowid_block_number(rowid),dbms_rowid.rowid_row_number(rowid) from table_name; create table test2 ( id int, lname varchar2(20) not nul, fname varchar2(20) constraint ck_1check(fname like 'k%'), empdate date default sysdate) ) tablespace tablespace_name; create global temporary table test2 oncomit delete/preserve rows as select * from kong.authors; create table user.table(.) tablespacetablespace_name storage(.) pctfre10 pctused 40; alter table user.tablename pctfre 20pctused 50 storage(.); -changing table storage /* ⼿⼯分配分区 , 分配的数据⽂件必须是表所在表空间内的数据⽂件 */ alter table user.table_name alocatextent(size 50k datafile '.');/* 释放表中没有⽤到的空间 */ alter table table_name dealocate unused; alter table table_name dealocate unusedkep 8k;

/* 将⾮分区表的表空间搬到新的表空间 , 在移动表空间后，原表中的索引对象将会不可⽤， 必 须重建 */ alter table user.table_name move tablespacenew_tablespace_name; create index index_name onuser.table_name(column_name) tablespace users; alter index index_name rebuild; drop table table_name [CASCADECONSTRAINTS]; alter table user.table_name drop columncol_name [CASCADE CONSTRAINTS CHECKPOINT

- 1 0]; -drop column /* 给表中不⽤的列做标记 */ alter table user.table_name set unusedcolumn coments CASCADE CONSTRAINTS; /*drop 表中不⽤的做了标记列 */ alter table user.table_name drop unusedcolumns checkpoint 1 0; /* 当在 drop col 是出现异常，使⽤ CONTINUE ，防⽌重删前⾯的 column*/ ALTER TABLE USER.TABLE_NAME DROP COLUMNSCONTINUE CHECKPOINT 1 0; select * from dba_tables/dba_objects;


# managing indexes # /*create index*/ example: /* 创建⼀般索引 */ create index index_name ontable_name(column_name) tablespace tablespace_name; /* 创建位图索引 */ create bitmap index index_name ontable_name(column_name1,column_name2) tablespace tablespace_name; /* 索引中不能⽤ pctused*/ create [bitmap] index index_name ontable_name(column_name) tablespace tablespace_name pctfre 20 storage(inital 10k next 10k) ; /* ⼤数据量的索引最好不要做⽇志 */ create [bitmap] index index_nametable_name(column_name1,column_name2) tablespace_name pctfre 20 storage(inital 10k next 10k)nologing; /* 创建反转索引 */ create index index_name ontable_name(column_name) reverse; /* 创建函数索引 */ create index index_name ontable_name(function_name(column_name) tablespace tablespace_name; /* 建表时创建约束条件 */ create table user.table_name(column_namenumber(7) constraint constraint_name primary key deferable using index storage(init ial10k next 10k) tablespace tablespace_name,column_name2

varchar2(25) constraint constraint_name notnul,column_name3 number(7) tablespace tablespace_name;/* 给创建 bitmap index 分配的内存空间参数，以加速建索引 */ show parameter create_bit; /* 改变索引的存储参数 */ alter index index_name pctfre 30storage(init ial 20k next 20k); /* 给索引⼿⼯分配⼀个分区 */ alter index index_name alocate extent(size 20k datafile '$ORACLE/oradata/.'); /* 释放索引中没⽤的空间 */ alter index index_name dealocate unused; /* 索引重建 */ alter index index_name rebuild tablespacetablespace_name; /* 普通索引和反转索引的互换 */ alter index index_name rebuild tablespacetablespace_name reverse; /* 重建索引时，不锁表 */ alter index index_name rebuild online; /* 给索引整理碎⽚ */ alter index index_name COALESCE; /* 分析索引 , 事实上是更新统计的过程 */ analyze index index_name validatestructure; desc index_state; drop index index_name; alter index index_name monitoringusage; - 监视索引是否被⽤到 alter index index_name nomonitoringusage; - 取消监视 /* 有关索引信息的视图 */ select * from dba_indexes/dba_ind_columns/dbs_ind_expresions/v$object_usage;

# 数据完整性的管理 (Maintaining data integr ity) # alter table table_name drop constraintconstraint_name; -drop 约束 alter table table_name ad constraintconstraint_name primary key(column_name1,column_name2); - 创建主键 alter table table_name ad constraintconstraint_name unique(column_name1,column_name2); 创建唯⼀约束 /* 创建外键约束 */ alter table table_name ad constraintconstraint_name foreign key(column_name1) references table_name(column_name1); /* 不效验⽼数据，只约束新的数据 [enable/disable ：约束 / 不约束新数据;novalidate/validate: 不 对 / 对⽼数据进⾏验证 ]*/

alter table table_name ad constraintconstraint_name check(column_name like 'B%')enable/disable novalidate/validate; /* 修改约束条件，延时验证， comit 时验证 */ alter table table_name modify constraintconstraint_name initialy defered; /* 修改约束条件，⽴即验证 */ alter table table_name modify constraintconstraint_name initialy i mediate; alter sesion setconstraints=defered/i mediate; /*drop ⼀个有外键的主键表 , 带 cascadeconstraints 参数级联删除 */ drop table table_name cascade constraints; /* 当 truncate 外键表时，先将外键设为⽆效，再 truncate;*/ truncate table table_name; /* 设约束条件⽆效 */ alter table table_name disable constraintconstraint_name; alter table table_name enable novalidateconstraint constraint_name; /* 将⽆效约束的数据⾏放⼊ exception 的表中，此表记录了违反数据约束的⾏的⾏号；在此 之前，要先建exceptions 表 */ alter table table_name ad constraintconstraint_name check(column_name >15) enable validate exceptions into exceptions; /* 运⾏创建 exceptions 表的脚本 */ start $ORACLE_HOME/rdbms/admin/ut lexcpt.sql; /* 获取约束条件信息的表或视图 */ select * fromuser_constraints/dba_constraints/dba_cons_columns;

# managing paswordsecurity and resources # alter user user_name acountunlock/open; - 锁定 / 打开⽤户 ; alter user user_name pasword expire; - 设定⼜令到期 /* 建⽴⼜令配置⽂件 ,failed_login_atempts ⼜令输多少次后锁， pasword_lock_times 指多少 天 后⼜令被⾃动解锁 */ create profile profile_name limitfailed_login_atempts 3 pasword_lock_times 1/140; /* 创建⼜令配置⽂件 */ create profile profile_name limitfailed_login_atempts 3 pasword_lock_time unlimited pasword_life_time 30 pasword_reuse_time30 pasword_verify_function verify_function pasword_grace_time 5; /* 建⽴资源配置⽂件 */ create profile prfile_name limitsesion_per_user 2 cpu_per_sesion 1 0 idle_t ime 60 conect_time 480; alter user user_name profile profile_name; /* 设置⼜令解锁时间 */

alter profile profile_name limitpasword_lock_time 1/24; /*pasword_life_time 指⼜令⽂件多少时间到期， pasword_grace_time 指在第⼀次成功登录 后到⼜ 令到期有多少天时间可改变⼜令 */ alter profile profile_name limitpasword_lift_time 2 pasword_grace_time 3; /*pasword_reuse_time 指⼜令在多少天内可被重⽤ ,pasword_reuse_max ⼜令可被重⽤的最 ⼤ 次数 */ alter profile profile_name limitpasword_reuse_time 10[pasword_reuse_max 3]; alter user user_name ident if ied byinput_pasword; - 修改⽤户⼜令 drop profile profile_name; /* 建⽴了 profile 后，且指定给某个⽤户，则必须⽤ CASCADE 才能删除 */ drop profile profile_name CASCADE; alter system set resource_limit=true; - 启⽤⾃愿限制 , 缺省是 false /* 配置资源参数 */ alter profile profile_name limitcpu_per_sesion 1 0 conect_time 60 idle_t ime 5; /* 资源参数 (sesion 级 ) cpu_per_sesion 每个 sesion 占⽤ cpu 的时间 单位 1/10 秒 sesions_per_user 允许每个⽤户的并⾏ sesion 数 conect_time 允许连接的时间单位分钟 idle_t ime 连接被空闲多少时间后，被⾃动断开单位分钟 logical_reads_per_sesion 读块数 private_sga ⽤户能够在 SGA 中使⽤的私有的空间数 单位 bytes (cal 级 ) cpu_per_cal 每次 (1/10 秒 ) 调⽤ cpu 的时间 logical_reads_per_cal 每次调⽤能够读的块数 */ alter profile profile_name limitcpu_per_cal 1 0 logical_reads_per_cal 10; desc dbms_resouce_manager; - 资源管理器包 /* 获取资源信息的表或视图 */ select * from dba_users/dba_profiles;

# Managing users # show parameter os; create user testuser1 ident ified bykxf_01; grant conect,createtable to testuser1; alter user testuser1 quota 10m on tablespace_name; /* 创建⽤户 */ create user user_name ident ified bypasword default tablespace tablespace_name temporary tablespace tablespace_name quota 15m on tablespace_name pasword expire;

/* 数据库级设定缺省临时表空间 */ alter database default temporary tablespacetablespace_name;/* 制定数据库级的缺省表空间 */ alter database default tablespacetablespace_name; /* 创建 os 级审核的⽤户，需知道os_authent_prefix ，表⽰ oracle 和 os ⼜令对应的前缀 ,'OPS$' 为此参数的值，此值可以任意设置 */ create user user_name ident ified byexternaly default OPS$tablespace_name tablespace_name temporary tablespace tablespace_name quota 15m on tablespace_name pasword expire; /* 修改⽤户使⽤表空间的限额 , 回滚表空间和临时表空间不允许授予限额 */ alter user user_name quota 5m on tablespace_name; /* 删除⽤户或删除级联⽤户 ( ⽤户对象下有对象的要⽤CASCADE ，将其下⼀些对象⼀起删 除 )*/ drop user user_name [CASCADE]; /* 每个⽤户在哪些表空间下有些什么限额 */ desc dba_ts_quotas;select * fromdba_ts_quotas where username='.'; /* 改变⽤户的缺省表空间 */ alter user user_name default tablespacetablespace_name;

# Managing Privileges # grant create table,create sesion touser_name; grant create any table to user_name; revokecreate any table from user_name; /* 授予权限语法 ,public 标识所有⽤户 ,with admin option 允许能将权限授予第三者的权限 */ grant system_privs,[ .] to[user/role/public],[ .] [with admin option]; select * from v$pwfile_users; /* 当 O7_dictionary_acesiblity 参数为 True 时，标识 selectany table 时，包括系统表也能 select , 否则，不包含系统表 ; 缺省为 false*/ show parameter O7; /* 由于 O7_dictionary_acesiblity 为静态参数，不能动态改变，故加 scope=spfile, 下次启动 时 才⽣效 */ alter system setO7_dictionary_acesiblity=true scope=spfile; /* 授予对象中的某些字段的权限，如 select 某表中的某些字段的权限 */ grant [object_privs(column, .)],[.] onobject_name to user/role/public,. with grant option; /*oracle 不允许授予 select 某列的权限 , 但可以授 insert,update 某列的权限 */ grant insert(column_name1,column_name2,.)on table_name to user_name with grant option; select * from dba_sys_privs/sesion_privs/dba_tab_privs/user_tab_privs/dba_col_privs/user_col_privs; /*db/os/none 审计被记录在数据库 / 操作系统 / 不审计 缺省是 none*/ show parameter audit_trail; /* 启动对表的 select 动作 */

audit select on user.table_name bysesion;/*by sesion 在每个 sesion 中发出 comand 只记录⼀ 次， by aces 则每个 comand 都记录 */ audit [create table][select/update/inserton object by sesion/aces][whenever sucesful/not sucesful]; desc dbms_fga; - 进⼀步设计，则可使⽤ dbms_fgs 包 /* 取消审计 */ noaudit select on user.table_name; /* 查被审计信息 */ select * from al_def_audit_opts/dba_stmt_audit_opts/dba_priv_audit_opts/dba_obj_audit_opts; /* 获取审计记录 */ select * from dba_audit_trail/dba_audit_exists/dba_audit_object/dba_audit_sesion/dba_audit_statement;

# Managing Role # create role role_name; grant select ontable_name to role_name; grant role_name to user_name; set role role_name; create role role_name; create role role_name ident if ied bypasword; create role role_name ident if iedexternaly; set role role_name ; - 激活 role set role role_name ident if ied bypasword; alter role role_name not ident if ied; alter role role_name ident ified bypasword; alter role role_name ident ifiedexternaly; grant priv_name to role_name [WITH ADMINOPTION]; grant update(column_name1,col_name2,.) ontable_name to role_name; grant role_name1 to role_name2; /* 建⽴ default role, ⽤户登录时，缺省激活 default role*/ alter user user_name default rolerole_name1,role_name2,.; alter user user_name default role al; alter user user_name default role alexcept role_name1,.; alter user user_name default role none; set role role1 [ident ified bypasword],role2, .; set role al; set role except role1,role2,.; set role none; revoke role_name from user_name;

revoke role_name from public; drop role role_name; select * from dba_roles/dba_role_privs/role_role_privs/dba_sys_privs/role_sys_privs/role_tab_pr ivs/sesion_rol es;

# Basic SQL SELECT #select col_name as col_alias from

table_name ; select col_name from table_name where col1like '_o%'; -'_' 匹配单个字符 /* 使⽤字符函数 ( 右边截取 , 字段中包含某个字符 , 左边填充某字符到固定位数 , 右边填充某字 符到固定位数 )*/ selectsubstr(col1,-3,5),instr(col2,'g'),LPAD(col3,10,'$'),RPAD(col4,10,'%') fromtable_name; /* 使⽤数字函数 ( 往右 / 左⼏位四舍五⼊ , 取整 , 取余 )*/ select round(col1,-2),trunc(col2),mod(col3)from table_name ; /* 使⽤⽇期函数 ( 计算两个⽇期间相差⼏个星期, 两个⽇期间相隔⼏个⽉ , 在某个⽉份上加⼏ 个⽉ , 某个⽇期的下⼀个⽇期 , 某⽇期所在⽉的最后的⽇期 ,对某个⽇期的⽉分四舍五⼊，对某个⽇期的⽉份进⾏取整 )*/ select (sysdate-col1)/7 wek,months_betwen(sysdate,col1),ad_months(col1,2),next_day(sysdate,'FRIDAY'),last_day(s ysdate), round(sysdate,'MONTH'),trunc(sysdate,'MONTH')from table_name; /* 使⽤ NUL 函数 ( 当 expr1 为空取 expr2/ 当 expr1 为空取 expr2, 否则取 expr3/ 当 expr1=expr 2 返回空 )*/ selectnvl(expr1,expr2),nvl2(expr1,expr2,expr3),nulif(expr1,expr2) from table_name; select column1,column2,column3, casecolumn2 when '50' then column2*1.1 when '30' then column2*2.1 when '10' then column3/20 else column3 end as t from table_name ; - 使⽤ case 函数 select table1.col1,table2.col2 from table1 [CROS JOIN table2] | - 笛卡⼉连接 [NATURAL JOIN table2] | - ⽤两个表中的同名列连接 [JOIN table2 USING (column_name)] | - ⽤两个表中的同名列中的某⼀列或⼏列连接 [JOIN table2 ON (table1.col1=table2.col2)] | [LEFT|RIGHT|FUL OUTER JOIN table2 - 相当于 (+)=,=(+) 连接 , 全外连接 ON (table1.col1=table2.col2)]; -SQL1 9 中的 JOIN 语法 ;

example: select col1,col2 from table1 t1

- join table2 t2

- on t1.col1=t2.col2 and t1.col3=t2.col1

join table3 t3

- on t2.col1=t3.col3; select * from table_name where col1 <any (select col2 from table_name2 where continue group by col3); select * from table_name where col1 <al (select col2 from table_name2 where continue group by col3); insert into (select col1,col2,col3 formtable_name where col1> 50 with check option) values (value1,value2,value3);MERGE INTOtable_name table1 USING table_name2 table2 ON (table1.col1=table2.col2) WHEN MATCHED THEN UPDATE SET table1.col1=table2.col2, table1.col2=table2.col3,




. WHEN NOT MATCHED THEN INSERTVALUES(table2.col1,table2.col2,table2.col3,.); - 合并语句

# CREATE/ALTER TABLE # alter table table_name drop columncolumn_name ; -drop column alter table table_name set unused(col1,col2,.); - 设置列⽆效，这个⽐较快。 alter table table_name drop unusedcolumns; - 删除被设为⽆效的列 rename table_name1 to table_name2; - 重命名表 coment on table table_name is 'comentmesage'; - 给表放⼊注释信息 create table table_name (col1 int not nul,col2 varchar2(20),col3varchar2(20), constraint uk_test2_1 unique(col2,col3); - 定义表中的约束条件 alter table table_name ad constraintpk_test2 primary key(col1,col2,.); - 创建主键 /* 建⽴外键 */ create table table_name (rid int,namevarchar2(20),constraint fk_test3 foreign key(rid) references other_table_name(id); alter table table_name ad constraintck_test3 check(name like 'K%'); alter table table_name drop constraintconstraint_name; alter table table_name drop primary keycascade; - 级联删除主键

alter table table_name disable/enableconstraint constraint_name; - 使约束暂时⽆效 /* 删除列，并级联删除此列下的约束条件 */ alter table table_name drop columncolumn_name cascade constraint; select * from user_constraints/user_cons_columns; -约束条件相关视图

# Create Views # CREATE [OR REPLACE] [FORCE|NOFORCE] VIEWview_name [(alias[,alias].)] AS subquery [WITH CHECK OPTION [CONSTRAINTconstraint_name] [WITH READ ONLY [CONSTRAINT constraint_name]; - 创建视图的语法 example: Create or replace view testview aselect col1,col2,col3 from table_name; - 创建视图 /* 使⽤别名 */Create or replace view testview as select col1,sum(col2)col2_alias from table_name; /* 创建复杂视图 */ Create view view_name (alias1,alias2,alias3,alias4)as select

- d.col1,min(e.col1),max(e.col1),avg(e.col1)from table_name1 e,table_name2 d where
- e.col2=d.col2 group by d.col1; /* 当⽤ update 修改数据时，必须满⾜视图的 col1>10 的条件，不满⾜则不能被改变 .*/ Create or replace view view_name as select* from table_name where col1>10 with check option; /* 改变视图的值 . 对于简单视图可以⽤update 语法修改表数据，但复杂视图则不⼀定能改。 如使⽤了函数， groupby ,distinct 等的列 */ update view_name set col1=value1; /*TOP-N 分析 */ select [column_list],rownum from (select[column_list] from table_name order by Top-N_column) where rownum<=N; /* 找出某列三条最⼤值的记录 */ example: select rownum as rank ,col1 ,col2from (select col1 ,col2 from table_name order by col2 desc) where rownum<=3;


# Other database Object # CREATE SEQUENCE sequence_name [INCREMENT BYn] [START WITH n] [{MAXVALUE n | NOMAXVALUE}] [{MINVALUE n | NOMINVALUE}] [{CYCEL | NOCYCLE}] [{CACHE n | NOCACHE}]; - 创建 SEQUENCE example: CREATE SEQUENCE sequence_name INCREMENT BY10 START WITH 120 MAXVALUE 9

NOCACHE NOCYCLE; select * from user_sequences ; - 当前⽤户下记录 sequence 的视图 selectsequence_name.nextval,sequence_name.curval from dual; -sequence 的引⽤ alter sequence sequence_name INCREMENT BY20 MAXVALUE 9 NOCACHE NOCYCLE; - 修改 sequence, 不能改变起始序号 drop sequence sequence_name; - 删除 sequence CREATE [PUBLIC] SYNONYM synonym_name FORobject; - 创建同义词 DROP [PUBLIC] SYNONYM synonym_name; - 删除同义词 CREATE PUBLIC DATABASE LINK link_nameUSEING OBJECT; - 创建 DBLINK select * from object_name@link_name; - 访问远程数据库中的对象/*union 操作，它将两个集合的 交集部分压缩，并对数据排序 */ select col1,col2,col3 from table1_nameunion select col1,col2,col3 from table2_name; /*union al 操作，两个集合的交集部分不压缩，且不对数据排序 */ select col1,col2,col3 from table1_nameunion al select col1,col2,col3 from table2_name; /*intersect 操作，求两个集合的交集 , 它将对重复数据进⾏压缩，且排序 */ select col1,col2,col3 from table1_nameintersect select col1,col2,col3 from table2_name; /*minus 操作，集合减 , 它将压缩两个集合减后的重复记录 , 且对数据排序 */ select col1,col2,col3 from table1_nameminus select col1,col2,col3 from table2_name; /*EXTRACT 抽取时间函数 . 此例是抽取当前⽇期中的年*/ select EXTRACT(YEAR FROM SYSDATE) fromdual; /*EXTRACT 抽取时间函数 . 此例是抽取当前⽇期中的⽉*/ select EXTRACT(MONTH FROM SYSDATE) fromdual;

# 增强的 group by ⼦句 # select [column,] group_function(column). from table [WHERE condit ion] [GROUP BY [ROLUP] group_by_expresion] [HAVING having_expresion]; [ORDER BY column]; -ROLUP 操作字，对 group by ⼦句的各字段从右到左进⾏再聚 合 example: /* 其结果看起来象对 col1 做⼩计 */ select col1,col2,sum(col3) from table groupby rolup(col1,col2); /* 复合 rolup 表达式 */ select col1,col2,sum(col3) from table groupby rolup(col1,col2);

select [column,] group_function(column). from table [WHERE condit ion] [GROUP BY [CUBE] group_by_expresion] [HAVING having_expresion]; [ORDER BY column]; -CUBE 操作字，除完成 ROLUP 的功能外，再对 ROLUP 后的 结果集从右到左再聚合 example: /* 其结果看起来象对 col1 做⼩计后，再对 col2 做⼩计，最后算总计 */ select col1,col2,sum(col3) from table groupby cube(col1,col2); /* 复合 rolup 表达式 */ select col1,col2,sum(col3) from table groupby cube(col1,col2); /* 混合 rolup,cube 表达式 */ select col1,col2,col3,sum(col4) from tablegroup by col1,rolup(col2),cube(col3); /*GROUPING(expr) 函数，查看 select 语句种以何字段聚合，其取值为 0 或 1*/ select [column,]group_function(column).,GROUPING(expr) from table [WHERE condit ion] [GROUP BY [ROLUP] group_by_expresion] [HAVING having_expresion]; [ORDER BY column];example: selectcol1,col2,sum(col3),grouping(col1),grouping(col2) from table group bycube(col1,col2); /*grouping sets 操作，对 group by 结果集先对 col1 求和，再对 col2 求和，最后将其结果集 并 在⼀起 */ select col1,col2,sum(col3) from table groupby grouping sets(col1),(col2);

