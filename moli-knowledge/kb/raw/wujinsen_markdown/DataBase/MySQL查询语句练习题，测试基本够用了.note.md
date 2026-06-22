Sutdent表的定义

<table>
  <tr>
    <th>字段名</th>
    <th>字段描述</th>
    <th>数据类型</th>
    <th>主键</th>
    <th>外键</th>
    <th>⾮空</th>
    <th>唯⼀</th>
    <th>⾃增</th>
  </tr>
  <tr>
    <td>Id</td>
    <td>学号</td>
    <td>INT(10)</td>
    <td>是</td>
    <td>否</td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
  </tr>
  <tr>
    <td>Name</td>
    <td>姓名</td>
    <td>VARCHAR(20)</td>
    <td>否</td>
    <td>否</td>
    <td>是</td>
    <td>否</td>
    <td>否</td>
  </tr>
  <tr>
    <td>Sex</td>
    <td>性别</td>
    <td>VARCHAR(4)</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
  </tr>
  <tr>
    <td>Birth</td>
    <td>出⽣年份</td>
    <td>YEAR</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
  </tr>
  <tr>
    <td>Department</td>
    <td>院系</td>
    <td>VARCHAR(20)</td>
    <td>否</td>
    <td>否</td>
    <td>是</td>
    <td>否</td>
    <td>否</td>
  </tr>
  <tr>
    <td>Address</td>
    <td>家庭住址</td>
    <td>VARCHAR(50)</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
  </tr>
</table>


Score表的定义

<table>
  <tr>
    <th>字段名</th>
    <th>字段描述</th>
    <th>数据类型</th>
    <th>主键</th>
    <th>外键</th>
    <th>⾮空</th>
    <th>唯⼀</th>
    <th>⾃增</th>
  </tr>
  <tr>
    <td>Id</td>
    <td>编号</td>
    <td>INT(10)</td>
    <td>是</td>
    <td>否</td>
    <td>是</td>
    <td>是</td>
    <td>是</td>
  </tr>
  <tr>
    <td>Stu_id</td>
    <td>学号</td>
    <td>INT(10)</td>
    <td>否</td>
    <td>否</td>
    <td>是</td>
    <td>否</td>
    <td>否</td>
  </tr>
  <tr>
    <td>C_name</td>
    <td>课程名</td>
    <td>VARCHAR(20)</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
  </tr>
  <tr>
    <td>Grade</td>
    <td>分数</td>
    <td>INT(10)</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
    <td>否</td>
  </tr>
</table>


- 1.创建student和score表 CREATE TABLE student ( id INT(10) NOT NULL UNIQUE PRIMARY KEY , name VARCHAR(20) NOT NULL , sex VARCHAR(4) , birth YEAR, department VARCHAR(20) , address VARCHAR(5 0) ); 创建score表。SQL代码如下： CREATE TABLE score ( id INT(10) NOT NULL UNIQUE PRIMARY KEY AUTO_INCREMENT , stu_id INT(10) NOT NULL , c_name VARCHAR(20) , grade INT(10) );


- 2.为student表和score表增加记录 向student表插⼊记录的INSERT语句如下：

- INSERT INTO student VALUES( 901,'张⽼⼤', '男',1985,'计算机系', '北京市海淀区');
- INSERT INTO student VALUES( 902,'张⽼⼆', '男',1986,'中⽂系', '北京市昌平区');
- INSERT INTO student VALUES( 903,'张三', '⼥',1990,'中⽂系', '湖南省永州市');
- INSERT INTO student VALUES( 904,'李四', '男',1990,'英语系', '辽宁省⾩新市');
- INSERT INTO student VALUES( 905,'王五', '⼥',1991,'英语系', '福建省厦门市');
- INSERT INTO student VALUES( 906,'王六', '男',1988,'计算机系', '湖南省衡阳市'); 向score表插⼊记录的INSERT语句如下：


- INSERT INTO score VALUES(NULL,901, '计算机',98);

- INSERT INTO score VALUES(NULL,901, '英语', 80);
- INSERT INTO score VALUES(NULL,902, '计算机',65);


- INSERT INTO score VALUES(NULL,902, '中⽂',88);
- INSERT INTO score VALUES(NULL,903, '中⽂',95);
- INSERT INTO score VALUES(NULL,904, '计算机',70);


- INSERT INTO score VALUES(NULL,904, '英语',92);
- INSERT INTO score VALUES(NULL,905, '英语',94);
- INSERT INTO score VALUES(NULL,906, '计算机',90); INSERT INTO score VALUES(NULL,906, '英语',85);


- 3.查询student表的所有记录 mysql> SELECT * FROM student;

+-----+--------+------+-------+------------+--------------+ | id | name | sex | birth | department | address |

+-----+--------+------+-------+------------+--------------+

- | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 |
- | 902 | 张⽼⼆ | 男 | 1986 | 中⽂系 | 北京市昌平区 |
- | 903 | 张三 | ⼥ | 1990 | 中⽂系 | 湖南省永州市 |
- | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 |
- | 905 | 王五 | ⼥ | 1991 | 英语系 | 福建省厦门市 |
- | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 |


+-----+--------+------+-------+------------+--------------+

- 4.查询student表的第2条到4条记录 mysql> SELECT * FROM student LIMIT 1,3;


+-----+--------+------+-------+------------+--------------+ | id | name | sex | birth | department | address |

+-----+--------+------+-------+------------+--------------+

- | 902 | 张⽼⼆ | 男 | 1986 | 中⽂系 | 北京市昌平区 |
- | 903 | 张三 | ⼥ | 1990 | 中⽂系 | 湖南省永州市 |
- | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 |


+-----+--------+------+-------+------------+--------------+

- 5.从student表查询所有学⽣的学号（id）、姓名（name）和院系（department）的信息 mysql> SELECT id,name,department FROM student;

+-----+--------+------------+ | id | name | department |

+-----+--------+------------+

- | 901 | 张⽼⼤ | 计算机系 |
- | 902 | 张⽼⼆ | 中⽂系 |
- | 903 | 张三 | 中⽂系 |
- | 904 | 李四 | 英语系 |
- | 905 | 王五 | 英语系 |
- | 906 | 王六 | 计算机系 |


+-----+--------+------------+

- 6.从student表中查询计算机系和英语系的学⽣的信息 mysql> SELECT * FROM student WHERE department IN ('计算机系','英语系');

+-----+--------+------+-------+------------+--------------+ | id | name | sex | birth | department | address |

+-----+--------+------+-------+------------+--------------+

- | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 |


- | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 |
- | 905 | 王五 | ⼥ | 1991 | 英语系 | 福建省厦门市 |
- | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 |


+-----+--------+------+-------+------------+--------------+

- 7.从student表中查询年龄18~22岁的学⽣信息 mysql> SELECT id,name,sex,2013-birth AS age,department,address


- -> FROM student
- -> WHERE 2013-birth BETWEEN 18 AND 22;


+-----+------+------+------+------------+--------------+ | id | name | sex | age | department | address |

+-----+------+------+------+------------+--------------+

- | 905 | 王五 | ⼥ | 22 | 英语系 | 福建省厦门市 |


+-----+------+------+------+------------+--------------+ mysql> SELECT id,name,sex,2013-birth AS age,department,address

- -> FROM student
- -> WHERE 2013-birth>=18 AND 2013-birth<=22;


+-----+------+------+------+------------+--------------+ | id | name | sex | age | department | address |

+-----+------+------+------+------------+--------------+

- | 905 | 王五 | ⼥ | 22 | 英语系 | 福建省厦门市 |


+-----+------+------+------+------------+--------------+

- 8.从student表中查询每个院系有多少⼈ mysql> SELECT department, COUNT(id) FROM student GROUP BY department;

+------------+-----------+ | department | COUNT(id) |

+------------+-----------+ | 计算机系 | 2 | | 英语系 | 2 | | 中⽂系 | 2 | +------------+-----------+

- 9.从score表中查询每个科⽬的最⾼分 mysql> SELECT c_name,MAX(grade) FROM score GROUP BY c_name;

+--------+------------+ | c_name | MAX(grade) |

+--------+------------+ | 计算机 | 98 | | 英语 | 94 | | 中⽂ | 95 | +--------+------------+

- 10.查询李四的考试科⽬（c_name）和考试成绩（grade） mysql> SELECT c_name, grade

- -> FROM score WHERE stu_id=
- -> (SELECT id FROM student
- -> WHERE name= '李四' );


+--------+-------+ | c_name | grade |

+--------+-------+ | 计算机 | 70 | | 英语 | 92 | +--------+-------+

- 11.⽤连接的⽅式查询所有学⽣的信息和考试信息 mysql> SELECT student.id,name,sex,birth,department,address,c_name,grade


- -> FROM student,score
- -> WHERE student.id=score.stu_id;


+-----+--------+------+-------+------------+--------------+--------+-------+ | id | name | sex | birth | department | address | c_name | grade |

+-----+--------+------+-------+------------+--------------+--------+-------+

- | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 | 计算机 | 98 |


- | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 | 英语 | 80 |
- | 902 | 张⽼⼆ | 男 | 1986 | 中⽂系 | 北京市昌平区 | 计算机 | 65 |


- | 902 | 张⽼⼆ | 男 | 1986 | 中⽂系 | 北京市昌平区 | 中⽂ | 88 |
- | 903 | 张三 | ⼥ | 1990 | 中⽂系 | 湖南省永州市 | 中⽂ | 95 |
- | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 | 计算机 | 70 |


- | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 | 英语 | 92 |
- | 905 | 王五 | ⼥ | 1991 | 英语系 | 福建省厦门市 | 英语 | 94 |
- | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 | 计算机 | 90 |


- | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 | 英语 | 85 |


+-----+--------+------+-------+------------+--------------+--------+-------+

- 12.计算每个学⽣的总成绩 mysql> SELECT student.id,name,SUM(grade) FROM student,score

- -> WHERE student.id=score.stu_id
- -> GROUP BY id;


+-----+--------+------------+ | id | name | SUM(grade) |

+-----+--------+------------+

- | 901 | 张⽼⼤ | 178 |
- | 902 | 张⽼⼆ | 153 |
- | 903 | 张三 | 95 |
- | 904 | 李四 | 162 |
- | 905 | 王五 | 94 |
- | 906 | 王六 | 175 |


+-----+--------+------------+

- 13.计算每个考试科⽬的平均成绩 mysql> SELECT c_name,AVG(grade) FROM score GROUP BY c_name;

+--------+------------+ | c_name | AVG(grade) |

+--------+------------+ | 计算机 | 80.7500 | | 英语 | 87.7500 | | 中⽂ | 91.5000 | +--------+------------+

- 14.查询计算机成绩低于95的学⽣信息 mysql> SELECT * FROM student


- -> WHERE id IN
- -> (SELECT stu_id FROM score
- -> WHERE c_name="计算机" and grade<95);


+-----+--------+------+-------+------------+--------------+ | id | name | sex | birth | department | address |

+-----+--------+------+-------+------------+--------------+

- | 902 | 张⽼⼆ | 男 | 1986 | 中⽂系 | 北京市昌平区 | | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 |


- | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 |


+-----+--------+------+-------+------------+--------------+

- 15.查询同时参加计算机和英语考试的学⽣的信息 mysql> SELECT * FROM student

- -> WHERE id =ANY
- -> ( SELECT stu_id FROM score
- -> WHERE stu_id IN (
- -> SELECT stu_id FROM
- -> score WHERE c_name= '计算机')
- -> AND c_name= '英语' );


+-----+--------+------+-------+------------+--------------+ | id | name | sex | birth | department | address |

+-----+--------+------+-------+------------+--------------+ | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 | | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 | | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 |

+-----+--------+------+-------+------------+--------------+ mysql> SELECT a.* FROM student a ,score b ,score c

- -> WHERE a.id=b.stu_id
- -> AND b.c_name='计算机'
- -> AND a.id=c.stu_id
- -> AND c.c_name='英语';


+-----+--------+------+-------+------------+--------------+ | id | name | sex | birth | department | address |

+-----+--------+------+-------+------------+--------------+ | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 | | 904 | 李四 | 男 | 1990 | 英语系 | 辽宁省⾩新市 | | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 |

+-----+--------+------+-------+------------+--------------+

- 16.将计算机考试成绩按从⾼到低进⾏排序 mysql> SELECT stu_id, grade


- -> FROM score WHERE c_name= '计算机'
- -> ORDER BY grade DESC;


+--------+-------+ | stu_id | grade |

+--------+-------+

- | 901 | 98 | | 906 | 90 |


- | 904 | 70 |

| 902 | 65 |

+--------+-------+

17.从student表和score表中查询出学⽣的学号，然后合并查询结果 mysql> SELECT id FROM student

- -> UNION
- -> SELECT stu_id FROM score;


+-----+ | id |

+-----+

- | 901 |
- | 902 |
- | 903 |
- | 904 |
- | 905 |
- | 906 |


+-----+

18.查询姓张或者姓王的同学的姓名、院系和考试科⽬及成绩 mysql> SELECT student.id, name,sex,birth,department, address, c_name,grade

- -> FROM student, score
- -> WHERE
- -> (name LIKE '张%' OR name LIKE '王%')
- -> AND
- -> student.id=score.stu_id ;


+-----+--------+------+-------+------------+--------------+--------+-------+ | id | name | sex | birth | department | address | c_name | grade |

+-----+--------+------+-------+------------+--------------+--------+-------+

- | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 | 计算机 | 98 |

- | 901 | 张⽼⼤ | 男 | 1985 | 计算机系 | 北京市海淀区 | 英语 | 80 |
- | 902 | 张⽼⼆ | 男 | 1986 | 中⽂系 | 北京市昌平区 | 计算机 | 65 |


- | 902 | 张⽼⼆ | 男 | 1986 | 中⽂系 | 北京市昌平区 | 中⽂ | 88 |
- | 903 | 张三 | ⼥ | 1990 | 中⽂系 | 湖南省永州市 | 中⽂ | 95 |


- | 905 | 王五 | ⼥ | 1991 | 英语系 | 福建省厦门市 | 英语 | 94 |
- | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 | 计算机 | 90 | | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 | 英语 | 85 |


+-----+--------+------+-------+------------+--------------+--------+-------+

- 19.查询都是湖南的学⽣的姓名、年龄、院系和考试科⽬及成绩 mysql> SELECT student.id, name,sex,birth,department, address, c_name,grade


- -> FROM student, score


- -> WHERE address LIKE '湖南%' AND
- -> student.id=score.stu_id;


+-----+------+------+-------+------------+--------------+--------+-------+ | id | name | sex | birth | department | address | c_name | grade |

+-----+------+------+-------+------------+--------------+--------+-------+

- | 903 | 张三 | ⼥ | 1990 | 中⽂系 | 湖南省永州市 | 中⽂ | 95 | | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 | 计算机 | 90 | | 906 | 王六 | 男 | 1988 | 计算机系 | 湖南省衡阳市 | 英语 | 85 |


+-----+------+------+-------+------------+--------------+--------+-------+

