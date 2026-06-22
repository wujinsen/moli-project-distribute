- 1 docker简介 Docker 提供了⼀个可以运⾏你的应⽤程序的封套(envelope)，或者说容器。它原本是 dotCloud 启动的⼀个业余项⽬，并在

前些时候开源了。它吸引了⼤量的关注和讨论，导致 dotCloud 把它重命名到 Docker Inc。它最初是⽤ Go 语⾔编写的，它就 相当于是加在 LXC（LinuX Containers，linux 容器）上的管道，允许开发者在更⾼层次的概念上⼯作。

Docker 扩展了 Linux 容器（Linux Containers），或着说 LXC，通过⼀个⾼层次的 API 为进程单独提供了⼀个轻量级的虚 拟环境。Docker 利⽤了 LXC， cgroups 和 Linux ⾃⼰的内核。和传统的虚拟机不同的是，⼀个 Docker 容器并不包含⼀个单 独的操作系统，⽽是基于已有的基础设施中操作系统提供的功能来运⾏的。 Docker类似虚拟机的概念，但是与虚拟化技术的不同点在于下⾯⼏点：

- 1.虚拟化技术依赖物理CPU和内存，是硬件级别的；⽽docker构建在操作系统上，利⽤操作系统的containerization技术，所

以docker甚⾄可以在虚拟机上运⾏。

- 2.虚拟化系统⼀般都是指操作系统镜像，⽐较复杂，称为“系统”；⽽docker开源⽽且轻量，称为“容器”，单个容器适合部署

少量应⽤，⽐如部署⼀个redis、⼀个memcached。

- 3.传统的虚拟化技术使⽤快照来保存状态；⽽docker在保存状态上不仅更为轻便和低成本，⽽且引⼊了类似源代码管理机

制，将容器的快照历史版本⼀⼀记录，切换成本很低。

- 4.传统的虚拟化技术在构建系统的时候较为复杂，需要⼤量的⼈⼒；⽽docker可以通过Dockfile来构建整个容器，重启和构

建速度很快。更重要的是Dockfile可以⼿动编写，这样应⽤程序开发⼈员可以通过发布Dockfile来指导系统环境和依赖，这样对 于持续交付⼗分有利。

- 5.Dockerfile可以基于已经构建好的容器镜像，创建新容器。Dockerfile可以通过社区分享和下载，有利于该技术的推⼴。 Docker 会像⼀个可移植的容器引擎那样⼯作。它把应⽤程序及所有程序的依赖环境打包到⼀个虚拟容器中，这个虚拟容器


可以运⾏在任何⼀种 Linux 服务器上。这⼤⼤地提⾼了程序运⾏的灵活性和可移植性，⽆论需不需要许可、是在公共云还是私 密云、是不是裸机环境等等。

Docker也是⼀个云计算平台，它利⽤Linux的LXC、AUFU、Go语⾔、cgroup实现了资源的独⽴，可以很轻松的实现⽂件、 资源、⽹络等隔离，其最终的⽬标是实现类似PaaS平台的应⽤隔离。

Docker 由下⾯这些组成：

- 1. Docker 服务器守护程序（server daemon），⽤于管理所有的容器。
- 2. Docker 命令⾏客户端，⽤于控制服务器守护程序。
- 3. Docker 镜像：查找和浏览 docker 容器镜像。


- 2 docker特性 ⽂件系统隔离：每个进程容器运⾏在完全独⽴的根⽂件系统⾥。 资源隔离：可以使⽤cgroup为每个进程容器分配不同的系统资源，例如CPU和内存。 ⽹络隔离：每个进程容器运⾏在⾃⼰的⽹络命名空间⾥，拥有⾃⼰的虚拟接⼝和IP地址。 写时复制：采⽤写时复制⽅式创建根⽂件系统，这让部署变得极其快捷，并且节省内存和硬盘空

间。

⽇志记录：Docker将会收集和记录每个进程容器的标准流（stdout/stder/stdin），⽤于实时检索或 批量检索。

变更管理：容器⽂件系统的变更可以提交到新的映像中，并可重复使⽤以创建更多的容器。⽆需使 ⽤模板或⼿动配置。

交互式Shel：Docker可以分配⼀个虚拟终端并关联到任何容器的标准输⼊上，例如运⾏⼀个⼀次性 交互shel。

- 3 两个基础概念images与container Container和Image 在Docker的世界⾥，Image是指⼀个只读的层（Layer），这⾥的层是AUFS⾥的


概念，最直观的⽅式就是看⼀下docker官⽅给出的图：

![image 1](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile1.png>)

Docker使⽤了⼀种叫AUFS的⽂件系统，这种⽂件系统可以让你⼀层⼀层地叠加修改你的⽂件，最底 下的⽂件系统是只读的，如果需要修改⽂件，AUFS会增加⼀个可写的层（Layer），这样有很多好 处，例如不同的Container可以共享底层的只读⽂件系统（同⼀个Kernel），使得你可以跑N多个 Container⽽不⾄于你的硬盘被挤爆了！这个只读的层就是Image！⽽如你所看到的，⼀个可写的层就 是Container。

那Image和Container的区别是什么？很简单，他们的区别仅仅是⼀个是只读的层，⼀个是可写的 层，你可以使⽤docker comit 命令，将你的Container变成⼀个Image，也就是提交你所运⾏的 Container的修改内容，变成⼀个新的只读的Image，这⾮常类似于git comit命令。

- 4 docker安装与启动


安装docker

[root@localhost /]# yum -y install docker-io

更改配置⽂件

[root@localhost /]# vi /etc/sysconfig/docker

other-args列更改为：other_args="--exec-driver=lxc --selinux-enabled"

启动docker服务

[root@localhost /]# service docker start Starting cgconfig service: [ OK ] Starting docker: [ OK ]

将docker加⼊开机启动 [root@localhost /]# chkconfig docker on

基本信息查看 docker version：查看docker的版本号，包括客户端、服务端、依赖的Go等

[root@localhost /]# docker version Client version: 1.0.0 Client API version: 1.12 Go version (client): go1.2.2 Git commit (client): 63fe64c/1.0.0 Server version: 1.0.0 Server API version: 1.12 Go version (server): go1.2.2 Git commit (server): 63fe64c/1.0.0

docker info ：查看系统(docker)层⾯信息，包括管理的images, containers数等 [root@localhost /]# docker info Containers: 16 Images: 40 Storage Driver: devicemapper

Pool Name: docker-253:0-1183580-pool Data file: /var/lib/docker/devicemapper/devicemapper/data Metadata file: /var/lib/docker/devicemapper/devicemapper/metadata Data Space Used: 2180.4 Mb Data Space Total: 102400.0 Mb Metadata Space Used: 3.4 Mb Metadata Space Total: 2048.0 Mb

Execution Driver: lxc-0.9.0 Kernel Version: 2.6.32-431.el6.x86_64

- 5 镜像的获取与容器的使⽤ 镜像可以看作是包含有某些软件的容器系统，⽐如ubuntu就是⼀个官⽅的基础镜像，很多镜像都是


基于这个镜像“衍⽣”，该镜像包含基本的ubuntu系统。再⽐如，hipache是⼀个官⽅的镜像容器，运⾏ 后可以⽀持htp和websocket的代理服务，⽽这个镜像本身⼜基于ubuntu。

搜索镜像 docker search <image>：在docker index中搜索image

[root@localhost /]# docker search ubuntu12.10 NAME DESCRIPTION STARS OFFICIAL AUTOMATED mirolin/ubuntu12.10 0 marcgibbons/ubuntu12.10 0

mirolin/ubuntu12.10_redis 0 chug/ubuntu12.10x32 Ubuntu Quantal Quetzal 12.10 32bit base i... 0 chug/ubuntu12.10x64 Ubuntu Quantal Quetzal 12.10 64bit base i... 0

下载镜像 docker pul <image> ：从docker registry server 中下拉image

[root@localhost /]# docker pull chug/ubuntu12.10x64

查看镜像 docker images： 列出images docker images -a ：列出所有的images（包含历史） docker images-tre ：显示镜像的所有层(layer) docker rmi <image ID>： 删除⼀个或多个image

[root@localhost /]# docker images REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE chug/ubuntu12.10x64 latest 0b96c14dafcd 4 months ago 270.3 MB [root@localhost /]# docker images -a REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE chug/ubuntu12.10x64 latest 0b96c14dafcd 4 months ago 270.3 MB <none> <none> 31edfed3bb88 4 months ago 175.8 MB [root@localhost /]# docker images --tree Warning: '--tree' is deprecated, it will be removed soon. See usage. └─31edfed3bb88 Virtual Size: 175.8 MB

└─0b96c14dafcd Virtual Size: 270.3 MB Tags: chug/ubuntu12.10x64:latest [root@localhost /]# docker rmi <image ID> ....

使⽤镜像创建容器

[root@localhost /]# docker run chug/ubuntu12.10x64 /bin/echo hello world hello world

交互式运⾏ [root@localhost /]# docker run -i -t chug/ubuntu12.10x64 /bin/bash root@2161509ff65e:/#

查看容器 docker ps ：列出当前所有正在运⾏的container docker ps -l ：列出最近⼀次启动的container

docker ps -a ：列出所有的container（包含历史，即运⾏过的container） docker ps -q ：列出最近⼀次运⾏的container ID

[root@localhost /]# docker ps CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAME S ccf3de663dc9 chug/ubuntu12.10x64:latest /bin/bash 22 hours ago Up 22 hours sharp_hyp atia [root@localhost /]# docker ps -l CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NA MES f145f184647b chug/ubuntu12.10x64:latest /bin/bash 6 seconds ago Exited (0) 3 seconds ago c ompassionate_galileo [root@localhost /]# docker ps -a CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS N AMES f145f184647b chug/ubuntu12.10x64:latest /bin/bash 30 seconds ago Exited (0) 26 seconds ago

compassionate_galileo f4624b42fe7e chug/ubuntu12.10x64:latest /bin/bash 2 minutes ago Exited (0) 2 minutes ago sharp_wilson ccf3de663dc9 chug/ubuntu12.10x64:latest /bin/bash 22 hours ago Up 22 hours shar p_hypatia 9cbaa79b9703 chug/ubuntu12.10x64:latest /bin/bash 22 hours ago Exited (127) 36 minutes ago

berserk_mcclintock 2161509ff65e chug/ubuntu12.10x64:latest /bin/bash 22 hours ago Exited (0) 22 hours ago b ackstabbing_mclean [root@localhost /]# docker ps -q ccf3de663dc9

再次启动容器 docker start/stop/restart <container> ：开启/停⽌/重启container docker start [container_id] ：再次运⾏某个container （包括历史container） docker atach [container_id] ：连接⼀个正在运⾏的container实例（即实例必须为start状态，可以

多个窗⼝同时atach ⼀个container实例） docker start -i <container> ：启动⼀个container并进⼊交互模式（相当于先start，在atach） docker run -i -t <image> /bin/bash ：使⽤image创建container并进⼊交互模式, login shel

是/bin/bash docker run -i -t -p <host_port:contain_port> ：映射 HOST 端⼝到容器，⽅便外部访问容器内服

务，host_port 可以省略，省略表示把 container_port 映射到⼀个动态端⼝。 注：使⽤start是启动已经创建过得container，使⽤run则通过image开启⼀个新的container。 删除容器 docker rm <container.> ：删除⼀个或多个container

docker rm `docker ps -a -q` ：删除所有的container docker ps -a -q | xargs docker rm ：同上, 删除所有的container

- 6 持久化容器与镜像 6.1 通过容器⽣成新的镜像 运⾏中的镜像称为容器。你可以修改容器（⽐如删除⼀个⽂件），但这些修改不会影响到镜像。不


过，你使⽤docker comit <container-id> <image-name>命令可以把⼀个正在运⾏的容器变成⼀个 新的镜像。

docker comit <container> [repo:tag] 将⼀个container固化为⼀个新的image，后⾯的repo:tag可 选。

[root@localhost /]# docker images REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE chug/ubuntu12.10x64 latest 0b96c14dafcd 4 months ago 270.3 MB [root@localhost /]# docker commit d0fd23b8d3ac chug/ubuntu12.10x64_2 daa11948e23d970c18ad89c9e5d8972157fb6f0733f4742db04219b9bb6d063b [root@localhost /]# docker images REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE chug/ubuntu12.10x64_2 latest daa11948e23d 6 seconds ago 270.3 MB chug/ubuntu12.10x64 latest 0b96c14dafcd 4 months ago 270.3 MB

- 6.2 持久化容器 export命令⽤于持久化容器 docker export <CONTAINER ID> > /tmp/export.tar
- 6.3 持久化镜像 Save命令⽤于持久化镜像 docker save 镜像ID > /tmp/save.tar
- 6.4 导⼊持久化container 删除container 2161509f65e


![image 2](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile2.png>)

![image 3](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile3.png>)

![image 4](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile4.png>)

导⼊export.tar⽂件

[root@localhost /]# cat /tmp/export.tar | docker import - export:latest af19a55ff0745fb0a68655392d6d7653c29460d22d916814208bbb9626183aaa [root@localhost /]# docker images REPOSITORY TAG IMAGE ID CREATED VIRTUAL SIZE export latest af19a55ff074 34 seconds ago 270.3 MB chug/ubuntu12.10x64_2 latest daa11948e23d 20 minutes ago 270.3 MB chug/ubuntu12.10x64 latest 0b96c14dafcd 4 months ago 270.3 MB

6.5 导⼊持久化image 删除image da1948e23d

![image 5](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile5.png>)

导⼊save.tar⽂件

[root@localhost /]# docker load < /tmp/save.tar

![image 6](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile6.png>)

对image打tag [root@localhost /]# docker tag daa11948e23d load:tag

![image 7](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile7.png>)

6.6 export-import与save-load的区别 导出后再导⼊(export-import)的镜像会丢失所有的历史，⽽保存后再加载（save-load）的镜像没有

丢失历史和层(layer)。这意味着使⽤导出后再导⼊的⽅式，你将⽆法回滚到之前的层(layer)，同时，使 ⽤保存后再加载的⽅式持久化整个镜像，就可以做到层回滚。（可以执⾏docker tag <LAYER ID> <IMAGE NAME>来回滚之前的层）。

![image 8](<CentOS系统下docker的安装配置及使用详解.note_images/imageFile8.png>)

6.7 ⼀些其它命令 docker logs $CONTAINER_ID #查看docker实例运⾏⽇志，确保正常运⾏ docker inspect $CONTAINER_ID #docker inspect <image|container> 查看image或container的底

层信息 docker build <path> 寻找path路径下名为的Dockerfile的配置⽂件，使⽤此配置⽣成新的image docker build -t repo[:tag] 同上，可以指定repo和可选的tag docker build - < <dockerfile> 使⽤指定的dockerfile配置⽂件，docker以stdin⽅式获取内容，使⽤

此配置⽣成新的image

docker port <container> <container port> 查看本地哪个端⼝映射到container的指定端⼝，其实⽤ docker ps 也可以看到

- 7 ⼀些使⽤技巧 7.1 docker⽂件存放⽬录


Docker实际上把所有东⻄都放到/var/lib/docker路径下了。

[root@localhost docker]# ls -F containers/ devicemapper/ execdriver/ graph/ init/ linkgraph.db repositories-devicemapper volumes/

containers⽬录当然就是存放容器（container）了，graph⽬录存放镜像，⽂件层（file system layer）存放在 graph/imageid/layer路径下，这样我们就可以看看⽂件层⾥到底有哪些东⻄，利⽤这种层级结构可以清楚的看到⽂件层是如何 ⼀层⼀层叠加起来的。

7.2 查看rot密码

docker容器启动时的rot⽤户的密码是随机分配的。所以，通过这种⽅式就可以得到容器的rot⽤户 的密码了。

docker logs 5817938c3f6e 2>&1 | grep 'User: ' | tail -n1

# * 转载请注明原⽂地址：htp:/ w.server10.com/docker/20141/ 105.html

