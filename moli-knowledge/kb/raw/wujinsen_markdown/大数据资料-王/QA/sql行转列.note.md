姓名

课程 分数

张三 语⽂ 74 张三 数学 83 张三 物理 93 李四 语⽂ 74 李四 数学 84 李四 物理 94

想变成(得到如下结果)：

姓名 语⽂ 数学 物理

- - - -

李四 74 84 94 张三 74 83 93

-

创建表： create table stu_score( name varchar(20), cource varchar(20), score int ); INSERT INTO stu_score VALUES('zhangsan', '数学', 76); INSERT INTO stu_score VALUES('zhangsan', '语⽂', 7); INSERT INTO stu_score VALUES('zhangsan', '英语', 6); INSERT INTO stu_score VALUES('lisi', '数学', 45); INSERT INTO stu_score VALUES('lisi', '语⽂', 67); INSERT INTO stu_score VALUES('lisi', '英语', 9);

步骤：

- 1.
- 2.


按姓名分组，分组查询字段为姓名和最⼤值 在最⼤值中设置：

- a.
- b.


如果你想查询语⽂成绩，那么就通过case或者if设置为正常值，将其与的分数设置为0 对这个值取sum或者max

- 3、IF(id='某值',value1,value2)


- value1：true时取值
- value2：false时取值


- 4、如果在group by后加上WITH ROLUP将会统计出总值。 写法： SELECT s.name,SUM(IF(s.courcr='数学',score,0) AS 数学,SUM(IF(s.courcr='语⽂',score,0) AS 语 ⽂ ,SUM(IF(s.courcr='英语',score,0) AS 英语 FROM stu_score s GROUP BY s.name;


sql: SELECT st.true_name,aq.id ,aua.answer_score FROM ases a JOIN ases_paper

- ap ON a.paper_id=ap.id JOIN ases_question
- aq ON aq.paper_id=ap.id JOIN ases_rule_paper arp ON arp.ases_id=a.id JOIN ases_rule ar ON arp.rule_id=ar.id JOIN ases_user_answer aua ON aq.id=aua.question_id JOIN student st ON st.user_id=aua.user_id JOIN clasrom cr ON st.clas_id=cr.clas_id JOIN center c ON cr.center_id=c.center_id JOIN series_clas sc ON sc.series_clas_name=cr.series_name


结果： true_name id answer_score

冯慧

- 1 5 冯慧
- 2

- 4 冯慧


- 3


- 3 冯慧
- 4

- 2 冯慧

5 1 张瑞雪 1

- 1 张瑞雪
- 2 1 张瑞雪
- 3 1 张瑞雪
- 4 1 张瑞雪
- 5 1 杨⽟茹


- 1

3 杨⽟茹

- 2
- 3 杨⽟茹






# 3

- 3 杨⽟茹
- 4

- 3 杨⽟茹

5

- 3 杨⽉圆

- 1

4 杨⽉圆

- 2

4 杨⽉圆

- 3
- 4 杨⽉圆

4 4 杨⽉圆 5 4 罗妃

- 1

5 罗妃

- 2

5 罗妃

- 3

5 罗妃

- 4
- 5 罗妃






- 5




- 5 唐志琦


- 1

5 唐志琦

- 2 5 唐志琦
- 3 5 唐志琦
- 4
- 5 唐志琦 5 5 刘江波


- 1 5 刘江波
- 2 5 刘江波
- 3 5 刘江波
- 4
- 5 刘江波 5 5


⾏转列的sql： SELECT true_name AS 姓名,

- SUM(IF(id='1',answer_score,0) AS 问题1,
- SUM(IF(id='2',answer_score,0) AS 问题2,
- SUM(IF(id='3',answer_score,0) AS 问题3,


- SUM(IF(id='4',answer_score,0) AS 问题4,
- SUM(IF(id='5',answer_score,0) AS 问题5


FROM ( SELECT st.true_name,aq.id ,aua.answer_score FROM ases a JOIN ases_paper

- ap ON a.paper_id=ap.id JOIN ases_question
- aq ON aq.paper_id=ap.id JOIN ases_rule_paper arp ON arp.ases_id=a.id JOIN ases_rule ar ON arp.rule_id=ar.id JOIN ases_user_answer aua ON aq.id=aua.question_id JOIN student st ON st.user_id=aua.user_id JOIN clasrom cr ON st.clas_id=cr.clas_id JOIN center c ON cr.center_id=c.center_id JOIN series_clas sc ON sc.series_clas_name=cr.series_name WHERE ar.rule_name='职业发展顾问的满意度调查php1301' AND c.center_name='北京亚运村中⼼' ) wsf GROUP BY true_name WITH ROLUP ;


结果：

![image 1](<sql行转列.note_images/imageFile1.png>)

