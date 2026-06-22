redis-cluster也就是redis的3.0版本，该版本集成redis集群功能。⾸先到该地址下载⼀个压缩包，百度 云盘：

htp:/pan.baidu.com/s/1ntr6e4T

- 1、 将压缩包放置到Linux机器的/opt⽬录下
- 2、 使⽤cd命令进⼊到/opt/redis/ned/⽬录，安装⽬录⾥的软件。 ruby-2.1.3步骤如下：

- 1）、cd ruby-2.1.3
- 2）、chmod 75 ./configure
- 3）、./configure
- 4）、chmod 75 *
- 5）、make （此步骤报错的话重新执⾏上述步骤）
- 6）、make instal Zlib步骤同上： Tcl步骤：


- 1）、cd tcl8.6.1/unix
- 2）-6)、同上 Rubygems 步骤如下：


- 1）、cd rubygems
- 2）、ruby setup.rb Redis-3.0.0.gem步骤如下： 1）、gem instal –l redis-3.0.0.gem


- 3、 安装redis3.0

- 1）、使⽤cd到/opt/redis/redis-3.0.0-rc1
- 2）、make MALOC=libc
- 3）、make instal
- 4）、cp src/redis-trib.rb /usr/local/bin/


- 4、修改配置⽂件 进⼊/opt/redis/conf,修改配置⽂件最⼤可⽤内存⼀项，根据机器配置适量修改
- 5、 启动所有的redis节点

- 1）、Cd /opt/redis
- 2）、chmod 75 redis-start
- 3）、./redis-start


- 6、创建redis集群

- 1）、cd /opt/redis
- 2）、./redis-start
- 3）、./cluster-start


- 7、测试效果


- 1）、redis-cli -c -p 6379


- 2）、set fo bar
- 3）、get fo


- 8、查看和杀死进程 查看：ps -ef | grep redis 杀死：ps -ef|grep redis|egrep -v grep|awk -F ' ' '{print $2}'|xargs kill -9


近期将写关于java连接redis-cluster的教程。

分享到：

2 分钟前

浏览 14

评论(0)

分类:企业架构

相关推荐

