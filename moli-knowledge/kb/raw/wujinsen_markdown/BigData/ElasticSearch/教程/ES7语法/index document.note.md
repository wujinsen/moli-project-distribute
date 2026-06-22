- 1.创建index PUT / student_test
- 2.创建⽂档 PUT /<target>/_doc/<_id>

es7只有默认的type: _doc PUT student_test2/_doc/1 {

"user": "李四", "title": "⼯程师", "desc": "数据库管理"

}

- 3. 查询索引结构: GET student_test2 查询索引下的⽂档数据: GET student_test2/_search


