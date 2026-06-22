https://github.com/sripathikrishnan/redis-rdb-tools#generate-mem ory-report

帮你找了⼀个⼯具，经测试可⽤：

安装和使⽤⽅法⽂档上写了，如果安装完成找不到rdb命令的话，直接在安装⽬录下执⾏也可以：

rdbtools/cli/rdb.py -c memory /path/to/your/dump.rdb > result.csv

<table>
  <tr>
    <th>database</th>
    <th>type</th>
    <th>key</th>
    <th>size_in_bytes</th>
    <th>encoding</th>
    <th>num_element</th>
    <th>len_largest_el</th>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td>s</td>
    <td>ement</td>
  </tr>
  <tr>
    <td>0</td>
    <td>string</td>
    <td>" c"</td>
    <td>98</td>
    <td>string</td>
    <td>4</td>
    <td>4</td>
  </tr>
  <tr>
    <td>0</td>
    <td>string</td>
    <td>" b"</td>
    <td>96</td>
    <td>string</td>
    <td>3</td>
    <td>3</td>
  </tr>
  <tr>
    <td>0</td>
    <td>hash</td>
    <td>"user"</td>
    <td>102</td>
    <td>ziplist</td>
    <td>1</td>
    <td>6</td>
  </tr>
</table>


0 string "a" 94 string 2 2

结果列中的sizeinbytes就是你要的⼤⼩，导出后⾃⼰排下序就可以了。另外，注意修改你⾃⼰ dump.rdb的⽂件路径。对于数据量太⼤的情况我没测试过，分析估计会⽐较慢。

