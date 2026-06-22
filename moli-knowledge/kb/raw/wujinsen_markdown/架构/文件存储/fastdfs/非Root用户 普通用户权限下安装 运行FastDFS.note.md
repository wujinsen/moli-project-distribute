htps:/blog.csdn.net/ q_42924 6/article/details/108146721

⾮Rot⽤户/普通⽤户权限下安装/运⾏FastDFS

- 1.准备安装包 libfastcomon-1.0.39.tar.gz fastdfs-5.1.tar.gz
- 2.安装相关依赖

yum instal gc gc-c+ make automake autoconf libtol pcre* zlib opensl opensl-devel 1

- 3.新建普通⽤户

userad fastdfs su - fastdfs

- 1
- 2


- 4.新建安装⽬录

mkdir -pv /home/opt/fastdfs 1 ⽬录层级示意

/home/fastdfs/ 1 ├── opt │ └── fastdfs ├── data └── soft ├── fastdfs-5.1.tar.gz ├── libfastcomon-1.0.39.tar.gz

- 5.编译安装fastdfs


export DESTDIR=/home/fastdfs/opt/fastdfs 1 设置⼀个环境变量，软件的安装路径，因为作者⾃⼰的make.sh⽂件中有这个变量，但为空所以默认 rot⽤户编译的安装的时候就在系统根⽬录下，但普通⽤户是没有根⽬录的写⼊权限的。

- 5.1安装公共lib


- 5.1.1解压安装⽂件


cd /home/fastdfs/soft tar -zxvf libfastcomon-1.0.39.tar.gz

- 1
- 2


- 5.1.2编译安装


cd libfastcomon-1.0.39/

./make.sh

./make.sh instal

- 1
- 2
- 3 安装路径及⽂件示意 /home/fastdfs/opt/ └── fastdfs └── usr ├── include │ └── fastcomon │ ├── avl_tre.h │ ├── base64.h │ ├── chain.h │ ├── char_converter.h │ ├── char_convert_loader.h │ ├── comon_blocked_queue.h │ ├── comon_define.h │ ├── conection_pol.h │ ├── fast_alocator.h


│ ├── fast_blocked_queue.h │ ├── fast_bufer.h │ ├── fast_mblock.h │ ├── fast_mpol.h │ ├── fast_task_queue.h │ ├── fast_timer.h │ ├── fc_list.h │ ├── flat_skiplist.h │ ├── hash.h │ ├── htp_func.h │ ├── id_generator.h │ ├── ini_file_reader.h │ ├── ioevent.h │ ├── ioevent_l op.h │ ├── local_ip_func.h │ ├── loger.h │ ├── md5.h │ ├── multi_skiplist.h │ ├── multi_socket_client.h │ ├── _os_define.h │ ├── php7_ext_wraper.h │ ├── proces_ctrl.h │ ├── pthread_func.h │ ├── sched_thread.h │ ├── shared_func.h │ ├── skiplist_comon.h │ ├── skiplist.h │ ├── skiplist_set.h │ ├── sockopt.h │ └── system_info.h ├── lib │ └── libfastcomon.so -> /home/fastdfs/opt/fastdfs/usr/lib64/libfastcomon.so └── lib64 └── libfastcomon.so

- 5.2安装fastdfs


cd /home/fastdfs/soft tar -zxvf fastdfs-5.1.tar.gz cd /home/fastdfs/soft/fastdfs-5.1

- 1
- 2
- 3


- 5.2.1修改make.sh 说明：编译完的程序运⾏时读取额外lib⽂件的路径。 修改前：


LIBS=' 1 修改后：

LIBS="-Wl,-rpath=/home/fastdfs/opt/fastdfs/usr/lib64" 1 说明：编译时⽣成默认配置⽂件的路径 修改前：

if [ ! -d /etc/fdfs ]; then mkdir -p /etc/fdfs

- 1
- 2 修改后：


if [ ! -d $TARGET_CONF_PATH ]; then mkdir -p $TARGET_CONF_PATH

- 1
- 2


- 5.2.2修改Makefile.in⽂件


vi /home/fastdfs/soft/fastdfs-5.1/tracker/Makefile.in

- 1 修改前：


INC_PATH = -I./comon -I/usr/include/fastcomon LIB_PATH = $(LIBS) -lfastcomon

- 2 修改后：


INC_PATH = -I./comon -I${DESTDIR}/usr/include/fastcomon LIB_PATH = $(LIBS) -L${DESTDIR}/usr/lib64 -lfastcomon

- 1
- 2 vi /home/fastdfs/soft/fastdfs-5.1/storage/Makefile.in 1 修改前：


INC_PATH = -I. -Itrunk_mgr -I./comon -I./tracker -I./client -Ifdht_client I/usr/include/fastcomon LIB_PATH = $(LIBS) -lfastcomon

- 1
- 2 修改后：


INC_PATH = -I. -Itrunk_mgr -I./comon -I./tracker -I./client -Ifdht_client I${DESTDIR}/usr/include/fastcomon LIB_PATH = $(LIBS) -L${DESTDIR}/usr/lib64 -lfastcomon

- 1
- 2 vi /home/fastdfs/soft/fastdfs-5.1/client/Makefile.in 1 修改前：


INC_PATH = -I./comon -I./tracker -I/usr/include/fastcomon LIB_PATH = $(LIBS) -lfastcomon

- 1
- 2 修改后：


INC_PATH = -I./comon -I./tracker -I${DESTDIR}/usr/include/fastcomon LIB_PATH = $(LIBS) -L${DESTDIR}/usr/lib64 -lfastcomon

5.2.3编译安装

cd /home/fastdfs/soft/fastdfs-5.1/

./make.sh

./make.sh instal

- 1
- 2
- 3 安装⽬录示意 /home/fastdfs/opt/fastdfs/ /home/fastdfs/opt/fastdfs/ ├── etc │ ├── fdfs（sample配置⽂件⽬录） │ └── init.d（启停脚本⽬录） └── usr ├── bin（可执⾏⼆进制程序⽬录） ├── include（头⽂件） ├── lib（库⽂件） └── lib64（库⽂件）


- 5.2.4检查是否安装程序正常加载所有依赖

l d /home/fastdfs/opt/fastdfs/usr/bin/fdfs_trackerd l d /home/fastdfs/opt/fastdfs/usr/bin/fdfs_storaged l d /home/fastdfs/opt/fastdfs/usr/bin/fdfs_monitor

- 1
- 2
- 3


- 6.复制fastdfs源码中给的参考配置⽂件到fastdfs安装⽬录


cd /home/fastdfs/soft/fastdfs-5.1/conf cp ./*.conf /home/fastdfs/opt/fastdfs/etc/fdfs/

- 1
- 2 /home/fastdfs/opt/fastdfs/etc/fdfs/ ├── client.conf


├── client.conf.sample ├── htp.conf ├── mod_fastdfs.conf ├── storage.conf ├── storage.conf.sample ├── storage_ids.conf ├── storage_ids.conf.sample ├── tracker.conf └── tracker.conf.sample

安装完毕后，本次实验配置⽂件⽬录/home/fastdfs/opt/fastdfs/etc/fdfs/中。 tracker.conf #tracker服务依赖的配置 storage_ids.conf #tracker服务依赖的配置 storage.conf #storage服务依赖的配置 htp.conf #nginx模块依赖的配置 mime.types #nginx模块依赖的配置 mod_fastdfs.conf #nginx模块依赖的配置 client.conf #测试客户依赖的配置

- 7.准备启动脚本 你可以去/home/fastdfs/opt/fastdfs/etc/init.d/⽬录下启动程序 当然也可以把两个启动程序复制出来


7.1创建启动脚本⽬录

mkdir /home/fastdfs/bin cp /home/fastdfs/opt/fastdfs/etc/init.d/* /home/fastdfs/bin 1 2 /home/fastdfs/bin/ ├── fdfs_storaged └── fdfs_trackerd

- 7.2修改两个启动脚本中程序⽬录和配置⽂件⽬录


vi /home/fastdfs/bin/fdfs_trackerd

修改前：

PRG=/usr/bin/fdfs_trackerd CONF=/etc/fdfs/tracker.conf

- 1
- 2 修改后：


PRG=/home/fastdfs/opt/fastdfs/usr/bin/fdfs_trackerd CONF=/home/fastdfs/opt/fastdfs/etc/fdfs/tracker.conf 1 2 注：注意修改所有的/usr/local/bin路径为/home/fastdfs/opt/fastdfs/usr/bin

vi /home/fastdfs/bin/fdfs_storaged 1 修改前：

PRG=/usr/bin/fdfs_storaged CONF=/etc/fdfs/storage.conf

- 1
- 2 修改后：


PRG=/home/fastdfs/opt/fastdfs/usr/bin/fdfs_storaged CONF=/home/fastdfs/opt/fastdfs/etc/fdfs/storage.conf

- 1
- 2 注：注意修改所有的/usr/local/bin路径为/home/fastdfs/opt/fastdfs/usr/bin


- 7.3修改参数 修改/home/fastdfs/opt/fastdfs/etc/fdfs/下的tracker.conf


#修改第 2⾏，base_path=/home/fastdfs/data/tracker #修改第250⾏，htp.server_port=80

1

修改/home/fastdfs/opt/fastdfs/etc/fdfs/下的client.conf

#修改第10⾏，base_path=/home/fastdfs/data/tracker #修改第14⾏，tracker_server=10.1. X.X  212

1 2 修改/home/fastdfs/opt/fastdfs/etc/fdfs/下的storage.conf

#修改第 1⾏为 "group_name=g1" #修改第41⾏为 "base_path=/home/fastdfs/data/storage" #修改第109⾏为 "store_path0=/home/fastdfs/data/storage" #修改第 18⾏为 "tracker_server=10.1. X.X  212" #修改第13⾏为 "run_by_group=fastdfs" #修改第137⾏为 "run_by_user=fastdfs" #修改第149⾏为 "file_distribute_path_mode=1" #修改第235⾏为 "rotate_eror_log=true"

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


- 7.4启停命令

./fdfs_trackerd start|stop|restart

./fdfs_storaged start|stop|restart

- 1
- 2


- 8.⼩结


- 1、定义DESTDIR变量的⽅式改了程序编译安装的默认根路径；
- 2、开放端⼝ 212、23 0


参考：htps:/blog.51cto.com/ 18518/2393036 ⸻—

# 版权声明：本⽂为CSDN博主「⾄今没搞明⽩」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附 上原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/ q_42924 6/article/details/108146721

