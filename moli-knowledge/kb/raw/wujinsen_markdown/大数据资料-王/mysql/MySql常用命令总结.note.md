1:使⽤SHOW语句找出在服务器上当前存在什么数据库： mysql> SHOW DATABASES; 2 2、创建⼀个数据库MYSQLDATA mysql> CREATE DATABASE MYSQLDATA;

- 3:选择你所创建的数据库 mysql> USE MYSQLDATA; (按回⻋键出现Database changed 时说明操作成功！)
- 4:查看现在的数据库中存在什么表 mysql> SHOW TABLES;
- 5:创建⼀个数据库表 mysql> CREATE TABLE MYTABLE (name VARCHAR(20), sex CHAR(1);
- 6:显示表的结构： mysql> DESCRIBE MYTABLE;
- 7:往表中加⼊记录 mysql> insert into MYTABLE values (”hyq,”M”);
- 8:⽤⽂本⽅式将数据装⼊数据库表中（例如D:/mysql.txt） mysql> LOAD DATA LOCAL INFILE “D:/mysql.txt” INTO TABLE MYTABLE;
- 9:导⼊.sql⽂件命令（例如D:/mysql.sql） mysql>use database; mysql>source d:/mysql.sql;
- 10:删除表 mysql>drop TABLE MYTABLE;


1:清空表 mysql>delete from MYTABLE; 12:更新表中数据 mysql>update MYTABLE set sex=”f” where name=ʼhyqʼ;

