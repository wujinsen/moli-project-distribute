mysql5.6是⾮安装版的，⼿⼯配置my.ini⽂件后数据库可以使⽤，也设置了编码UTF8

<table>
  <tr>
    <th>[mysql] port=306 default-character-set=utf8<br><br>[mysqld]</th>
  </tr>
</table>


character-set-server=utf8

后来数据库插⼊内容报错 [Er] 136 - Incorect string value: '\xE6\x97\xB6\xE5\x8F\x91.' for column 'name' at row 1 数据库的编码不对 查看数据库编码: SHOW VARIABLES LIKE '%character%';

<table>
  <tr>
    <th>caracter_et_client utf8 caracter_et_conection utf8 caracter_set_database lati 1 caracter_set_filesystembinary caracter_set_results utf8 caracter_set_server latin1 caracter_set_systemut8</th>
  </tr>
</table>


character_sets_dir D:\Software\mysql5.6\share\charsets\

