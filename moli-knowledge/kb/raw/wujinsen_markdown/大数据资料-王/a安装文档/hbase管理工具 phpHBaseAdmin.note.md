phphbaseadmin-master.zip 2.4MB

phphbaseadmin是使⽤php、python开发的通过hbase thrift接⼝对hbase表创建、查看、删除记录、 监控等的web ui⼯具 安装⽅法：

- (1) 使⽤根⽬录中的 setup_centos5.sh 或者setup_centos6.sh 脚本安装所需环境
- (2) 启动hbase thrift server
- (3) 修改根⽬录中的配置⽂件 config.inc.php,修改$configure['hbase_host']=你的thrift server服务器 地址
- (4) 在mysql server中创建数据库phphbaseadmin ,导⼊database/phphbaseadmin.sql⽂件，修改 aplication/config /database.php,$db['default']['hostname']、 $db['default']['username'] 、 $db['default']['pasword'] =';
- (5) 打开浏览器访问 htp:/serverip/phphbaseadmin，缺省⽤户名admin 密码admin 8登录
- (6) 登录后选择 system->user manager 菜单设置⽤户所属hbase table表的所属权限
- (7) 选择 Tables->view 菜单即可查看hbase table 记录。


![image 1](<hbase管理工具 phpHBaseAdmin.note_images/imageFile1.png>)

![image 2](<hbase管理工具 phpHBaseAdmin.note_images/imageFile2.png>)

![image 3](<hbase管理工具 phpHBaseAdmin.note_images/imageFile3.png>)

![image 4](<hbase管理工具 phpHBaseAdmin.note_images/imageFile4.png>)

![image 5](<hbase管理工具 phpHBaseAdmin.note_images/imageFile5.png>)

![image 6](<hbase管理工具 phpHBaseAdmin.note_images/imageFile6.png>)

![image 7](<hbase管理工具 phpHBaseAdmin.note_images/imageFile7.png>)

![image 8](<hbase管理工具 phpHBaseAdmin.note_images/imageFile8.png>)

![image 9](<hbase管理工具 phpHBaseAdmin.note_images/imageFile9.png>)

