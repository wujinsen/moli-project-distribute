tar -zxvf zokeper-3.4.5.tar.gz（解压）

- 1.1.1.重命名 mv zokeper-3.4.5 zokeper（重命名⽂件夹zokeper-3.4.5为zokeper）

- 1.1.2.修改环境变量

- 1、su – rot(切换⽤户到rot)
- 2、vi /etc/profile(修改⽂件)
- 3、添加内容：
- 4、重新编译⽂件： source /etc/profile
- 5、注意：3台zokeper都需要修改
- 6、修改完成后切换回hadop⽤户： su - hadop


- 1.1.3.修改配置⽂件


<table>
  <tr>
    <th>eporZOKEPER_HOME=/home/hadop/zokeper</th>
  </tr>
</table>


export PATH=$PATH:$ZOKEPER_HOME/bin

- 1、⽤hadop⽤户操作 cd zokeper/conf cp zo_sample.cfg zo.cfg
- 2、vi zo.cfg
- 3、添加内容：
- 4、创建⽂件夹： cd /home/hadop/zokeper/ mkdir -m 75 data 说明： -m 创建⽬录同时设定权限 mkdir -m 75 log
- 5、在data⽂件夹下新建myid⽂件，myid的⽂件内容为： cd data vi myid 添加内容：


<table>
  <tr>
    <th>dataDir=/home/hadop/zokeper/data dataLogDir=/home/hadop/zokeper/log server.1=slave1 2 8 3 8 (⼼跳端⼝、数据端⼝)<br><br>server2sve2  8 3 8 server.3=slave3 2 8 3 8</th>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
  </tr>
</table>


1

- 1.1.4.将集群下发到其他机器上

- scp -r /home/hadop/zokeper hadop@slave2:/home/hadop/
- scp -r /home/hadop/zokeper hadop@slave3:/home/hadop/


- 1.1.5.修改其他机器的配置⽂件

- 到slave2上：修改myid为：2
- 到slave3上：修改myid为：3


- 1.1.6.启动（每台机器）

- 1.1.7.查看集群状态


bin/zkServer.sh start

- 1.
- 2.


jps（查看进程） zkServer.sh status（查看集群状态，主从信息）

