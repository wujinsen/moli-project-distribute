常⽤的HTTP动词有下⾯五个（括号⾥是对应的SQL命令）。

# GET（SELECT）：从服务器取出资源（⼀项或多项）。

# POST（CREATE）：在服务器新建⼀个资源。

# PUT（UPDATE）：在服务器更新资源（客户端提供改变后的完整资源）。

# PATCH（UPDATE）：在服务器更新资源（客户端提供改变的属性）。

DELETE（DELETE）：从服务器删除资源。

GET htp:/localhost/SpringTest/students 查询所有学⽣信息 POST htp:/localhost/SpringTest/students 新建学⽣信息(ID⾃动⽣成) GEThtp:/localhost/SpringTest/ID 根据学⽣ID取得学⽣信息 PUT htp:/localhost/SpringTest/ID 使⽤指定ID插⼊学⽣信息 PATCH DELETE htp:/localhost/SpringTest/ID 根据学⽣ID删除学⽣

