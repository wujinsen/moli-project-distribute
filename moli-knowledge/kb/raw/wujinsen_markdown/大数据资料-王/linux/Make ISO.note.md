<table>
  <tr>
    <th>⼀ 、制作iso：<br><br>1 从perforce下载代码,给当前版本做标记（lable），并且改version.txt⽂件中的版本号。<br>2 给整个project做个lable（右键project），相当于打个标记。<br>3 cd 到CDBuildscript⽬录执⾏makebuild.sh（. /makebuild.sh）. 4、修改admin/。。。/suport.jsp中的9.2.0.0.27 为现有的版本。<br></th>
  </tr>
  <tr>
    <td>⼆ 、FM 的安装和部署<br><br>1 挂载iso CD 如：mount /dev/cdrom /media/ -t iso960<br>2 执⾏instal.sh，需要配置FM的登录名和密码 Mysql的密码 ⽤户下进⾏<br></td>
  </tr>
  <tr>
    <td>注意：需要在rot 三 、制作iso<br><br>1 下载代码，修改version.xml记录当前的版本<br>2 添加Label（版本控制的游标）<br>3 cd到CDBuildscript⽬录下⾯执⾏./makebuild.sh<br>4 ⼿动修改suport.jsp中的版本信息，改为现在的版本 <span id=ʼFlexMasterVersionʼ>9.7.0.0.1</span><br></td>
  </tr>
  <tr>
    <td>四 、制作patch<br><br>1 先到⾼版本的⽬录下执⾏creatpatch.sh<br>2 输⼊source 版本的⽬录<br>3 输⼊target版本的⽬录<br>4 执⾏makepatch.sh<br>5 ⽤upgrade.sh升级脚本 注意：⼀般从低版本到⾼版本0，且版本只差⼀个为优<br><br><br>⽂件</td>
  </tr>
</table>


# patch的⼊⼝为patch.xml

