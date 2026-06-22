<table>
  <tr>
    <th>选项名称</th>
    <th>使⽤格式</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>-ls</td>
    <td>-ls <路径></td>
    <td>查看指定路径的当前⽬录结构</td>
  </tr>
  <tr>
    <td>-lsr</td>
    <td>-lsr <路径></td>
    <td>递归查看指定路径的⽬录结构</td>
  </tr>
  <tr>
    <td>-du</td>
    <td>-du <路径></td>
    <td>统计⽬录下个⽂件⼤⼩</td>
  </tr>
  <tr>
    <td>-dus</td>
    <td>-dus <路径></td>
    <td>汇总统计⽬录下⽂件(夹)⼤⼩</td>
  </tr>
  <tr>
    <td>-count</td>
    <td>-count [-q] <路径></td>
    <td>统计⽂件(夹)数量</td>
  </tr>
  <tr>
    <td>-mv</td>
    <td>-mv <源路径> <⽬的路径></td>
    <td>移动</td>
  </tr>
  <tr>
    <td>-cp</td>
    <td>-cp <源路径> <⽬的路径></td>
    <td>复制</td>
  </tr>
  <tr>
    <td>-rm</td>
    <td>-rm [-skipTrash] <路径></td>
    <td>删除⽂件/空⽩⽂件夹</td>
  </tr>
  <tr>
    <td>-rmr</td>
    <td>-rmr [-skipTrash] <路径></td>
    <td>递归删除</td>
  </tr>
  <tr>
    <td>-put</td>
    <td>-put <多个linux上的⽂件></td>
    <td>上传⽂件</td>
  </tr>
  <tr>
    <td>-copyFromLocal</td>
    <td><hdfs路径><br><br>-copyFromLocal <多个linux上</td>
    <td>从本地复制</td>
  </tr>
  <tr>
    <td>-moveFromLocal</td>
    <td>的⽂件> <hdfs路径><br><br>-moveFromLocal <多个linux上</td>
    <td>从本地移动</td>
  </tr>
  <tr>
    <td>-getmerge</td>
    <td>的⽂件> <hdfs路径><br><br>-getmerge <源路径> <linux路</td>
    <td>合并到本地</td>
  </tr>
  <tr>
    <td>-cat</td>
    <td>径><br><br>-cat <hdfs路径></td>
    <td>查看⽂件内容</td>
  </tr>
  <tr>
    <td>-text</td>
    <td>-text <hdfs路径></td>
    <td>查看⽂件内容</td>
  </tr>
  <tr>
    <td>-copyToLocal</td>
    <td>-copyToLocal [-ignoreCrc] [crc] [hdfs源路径] [linux⽬的路</td>
    <td>从本地复制</td>
  </tr>
  <tr>
    <td>-moveToLocal</td>
    <td>径]<br><br>-moveToLocal [-crc] <hdfs源路</td>
    <td>从本地移动</td>
  </tr>
  <tr>
    <td>-mkdir</td>
    <td>径> <linux⽬的路径><br><br>-mkdir <hdfs路径></td>
    <td>创建空⽩⽂件夹</td>
  </tr>
  <tr>
    <td>-setrep</td>
    <td>-setrep [-R] [-w] <副本数> <路</td>
    <td>修改副本数量</td>
  </tr>
  <tr>
    <td>-touchz</td>
    <td>径><br><br>-touchz <⽂件路径></td>
    <td>创建空⽩⽂件</td>
  </tr>
  <tr>
    <td>-stat</td>
    <td>-stat [format] <路径></td>
    <td>显示⽂件统计信息</td>
  </tr>
</table>


<table>
  <tr>
    <th>-tail</th>
    <th>-tail [-f] <⽂件></th>
    <th>查看⽂件尾部信息</th>
  </tr>
  <tr>
    <td>-chmod</td>
    <td>-chmod [-R] <权限模式> [路径]</td>
    <td>修改权限</td>
  </tr>
  <tr>
    <td>-chown</td>
    <td>-chown [-R] [属主][:[属组 ] 路 径</td>
    <td>修改属主</td>
  </tr>
  <tr>
    <td>-chgrp</td>
    <td>-chgrp [-R] 属组名称 路径</td>
    <td>修改属组</td>
  </tr>
  <tr>
    <td>-help</td>
    <td>-help [命令选项]</td>
    <td>帮助</td>
  </tr>
</table>


