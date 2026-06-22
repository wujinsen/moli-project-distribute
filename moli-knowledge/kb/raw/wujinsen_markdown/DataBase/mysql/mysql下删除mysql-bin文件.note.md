# mysql下删除mysql-bin⽂件

2) 删除mysql-bin.000010 之前的⽇志 mysql> purge binary logs to 'mysql-bin.000010'; Query OK, 0 rows affected (0.35 sec) mysql> show binary logs;

