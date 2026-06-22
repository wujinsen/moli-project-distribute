- 1.查看⽇志常⽤命令 tail:

-n 是显示⾏号；相当于nl命令；例⼦如下： tail -10f test.log 实时监控10⾏⽇志 tail -n 10 test.log 查询⽇志尾部最后10⾏的⽇志; tail -n +10 test.log 查询10⾏之后的所有⽇志;

head:

跟tail是相反的，tail是看后多少⾏⽇志；例⼦如下： head -n 10 test.log 查询⽇志⽂件中的头10⾏⽇志; head -n -10 test.log 查询⽇志⽂件除了最后10⾏的其他所有⽇志;

cat： tac是倒序查看，是cat单词反写；例⼦如下： cat -n test.log |grep "debug" 查询关键字的⽇志

- 2. 应⽤场景⼀：按⾏号查看 -过滤出关键字附近的⽇志

- 1）cat -n test.log |grep "debug" 得到关键⽇志的⾏号
- 2）cat -n test.log |tail -n +92|head -n 20 选择关键字所在的中间⼀⾏. 然后查看这个关键字前10⾏和后10⾏的⽇志: tail -n +92表示查询92⾏之后的⽇志 head -n 20 则表示在前⾯的查询结果⾥再查前20条记录


- 3. 应⽤场景⼆：根据⽇期查询⽇志 sed -n '/2014-12-17 16 17 20/,/2014-12-17 16 17 36/p' test.log 特别说明:上⾯的两个⽇期必须是⽇志中打印出来的⽇志,否则⽆效；

先 grep '2014-12-17 16 17 20' test.log 来确定⽇志中是否有该 时间点

- 4.应⽤场景三：⽇志内容特别多，打印在屏幕上不⽅便查看


- (1)使⽤more和les命令, 如： cat -n test.log |grep "debug" |more 这样就分⻚打印了,通过点击空格键翻⻚
- (2)使⽤ > x.txt 将其保存到⽂件中,到时可以拉下这个⽂件分析 如：cat -n test.log |grep "debug" >debug.txt


cat catalina.out | grep 我是超⼈ | tail -f -n 50

