# l列出全部表

>list 清空表数据：truncate ‘tableʼ

<table>
  <tr>
    <th>名称</th>
    <th>命令表达式</th>
  </tr>
  <tr>
    <td>创建表</td>
    <td> </td>
  </tr>
  <tr>
    <td>描述表</td>
    <td>create '表名','列族名1','列族名2','列族名N'</td>
  </tr>
  <tr>
    <td>添加记录</td>
    <td>describe ‘表名ʼ</td>
  </tr>
  <tr>
    <td>查看记录</td>
    <td>put ‘表名ʼ,‘rowKeyʼ,‘列族 : 列‘ , '值'</td>
  </tr>
  <tr>
    <td>查看表中的记录总数</td>
    <td>get '表名' , 'rowKey'</td>
  </tr>
  <tr>
    <td>删除记录</td>
    <td>count '表名'</td>
  </tr>
  <tr>
    <td>删除⼀张表</td>
    <td>delete ‘表名ʼ,‘⾏名ʼ ,‘列族：列'<br><br>先要屏蔽该表，才能对该表进⾏删除 第⼀步</td>
  </tr>
  <tr>
    <td>查看所有记录</td>
    <td>disable ‘表名ʼ ，第⼆步 drop '表名'</td>
  </tr>
  <tr>
    <td>查看某个表某个列中 所有数据</td>
    <td>scan "表名"<br><br>scan "表名" , {COLUMNS=>'列族名:列名'}</td>
  </tr>
  <tr>
    <td>更新记录</td>
    <td>就是重写⼀遍，进⾏覆盖，hbase没有修改，都是追加<br><br></td>
  </tr>
</table>


