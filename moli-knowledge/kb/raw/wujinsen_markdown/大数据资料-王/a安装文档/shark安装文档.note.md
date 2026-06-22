- 1.在任意安装了hadop、hive以及spark的环境上解压缩shark-0.8.0-bin-hadop1.tgz，本例在client 上操作
- 2.进⼊到shark/conf下，将⽂件sh ark-env.sh.template复制为shark-env.sh,并修改： export HADOP_HOME=/home/hadop/hadop export HIVE_HOME=/home/hadop/hive export MASTER=spark:/master:707 export SPARK_HOME=/home/hadop/spark export SPARK_MEM=256M #我的环境没这么⼤内存，按道理应该是16G以上才好 export SCALA_HOME=/home/hadop/scala
- 3.测试安装成功失败 shark CREATE TABLE src(key INT, value STRING); SELECT COUNT(1) FROM src; 也可以运⾏shark-withinfo使⽤详细的带⽇志的⼯具操作
- 4.启动shark服务shark-service sharkserver 9


