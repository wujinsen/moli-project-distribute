htps:/ w.cnblogs.com/pekle/p/1219029.html

- 1. 安装docker，百度解决；
- 2. 拉取postgreSQL的docker镜像⽂件：docker pull postgres:12.1
- 3. 创建 docker volume，名字为“dv_pgdata"（其实可以省略⼿动创建，直接跑下⼀步，docker也 会⾃动创建的）：docker volume create dv_pgdata
- 4. 启动容器，⽤-v来指定把postgres的数据⽬录映射到上⾯创建的dv_pgdata⾥⾯：docker run -name my_postgres -v dv_pgdata:/var/lib/postgresql/data -e POSTGRES_PASSWORD=xxxxxx -p 5432:5432 -d postgres:12.1
- 5. 这时候查看已存在的docker volume: docker volume ls
- 6. 查看volume信息：


[root@VM_0_6_centos _data]# docker inspect dv_pgdata [

{

"CreatedAt": "2020-01-14T08:40:03+08:00", "Driver": "local", "Labels": {}, "Mountpoint": "/var/lib/docker/volumes/dv_pgdata/_data", "Name": "dv_pgdata", "Options": {}, "Scope": "local"

} ]

<table>
  <tr>
    <th>![image 1](<用docker运行postgreSQL.note_images/imageFile1.png>)</th>
  </tr>
</table>


# 7. 在宿主机，也可以直接查看volume⾥的内容：

<table>
  <tr>
    <th>![image 2](<用docker运行postgreSQL.note_images/imageFile2.png>)</th>
  </tr>
</table>


[root@VM_0_6_centos _data]# cd /var/lib/docker/volumes/dv_pgdata/_data [root@VM_0_6_centos _data]# ll total 132 drwx------ 19 polkitd ssh_keys 4096 Jan 14 08:40 . drwxr-xr-x 3 root root 4096 Jan 14 08:33 .. drwx------ 5 polkitd ssh_keys 4096 Jan 14 08:38 base drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 global drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_commit_ts drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_dynshmem

- -rw------- 1 polkitd ssh_keys 4535 Jan 14 08:38 pg_hba.conf

- -rw------- 1 polkitd ssh_keys 1636 Jan 14 08:38 pg_ident.conf drwx------ 4 polkitd ssh_keys 4096 Jan 14 08:45 pg_logical drwx------ 4 polkitd ssh_keys 4096 Jan 14 08:38 pg_multixact drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:40 pg_notify drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_replslot drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_serial drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_snapshots drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:40 pg_stat drwx------ 2 polkitd ssh_keys 4096 Jan 14 16:44 pg_stat_tmp drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_subtrans drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_tblspc

- drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_twophase

-rw------- 1 polkitd ssh_keys 3 Jan 14 08:38 PG_VERSION

- drwx------ 3 polkitd ssh_keys 4096 Jan 14 08:38 pg_wal drwx------ 2 polkitd ssh_keys 4096 Jan 14 08:38 pg_xact


- -rw------- 1 polkitd ssh_keys 88 Jan 14 08:38 postgresql.auto.conf

- -rw------- 1 polkitd ssh_keys 26588 Jan 14 08:38 postgresql.conf

- -rw------- 1 polkitd ssh_keys 36 Jan 14 08:40 postmaster.opts

- -rw------- 1 polkitd ssh_keys 94 Jan 14 08:40 postmaster.pid


<table>
  <tr>
    <th>![image 3](<用docker运行postgreSQL.note_images/imageFile3.png>)</th>
  </tr>
</table>


# 8. 查看postgresql：

<table>
  <tr>
    <th>![image 4](<用docker运行postgreSQL.note_images/imageFile4.png>)</th>
  </tr>
</table>


[root@VM_0_6_centos ~]#docker exec -it 618 bash root@618f1a4128ee:/# psql -U postgres

psql (12.1 (Debian 12.1-1.pgdg100+1)) Type "help" for help.

postgres=#

<table>
  <tr>
    <th>![image 5](<用docker运行postgreSQL.note_images/imageFile5.png>)</th>
  </tr>
</table>


# 9. 更多的时候，我们希望能⽤图形界⾯来管理和操作数据库，可以部署pgadmin⼯具（例如下⾯）， 然后在浏览器中访问宿主机的5080端⼝，便能打开pgadmin。

<table>
  <tr>
    <th>![image 6](<用docker运行postgreSQL.note_images/imageFile6.png>)</th>
  </tr>
</table>


docker pull dpage/pgadmin4:4.17 docker run --name pgadmin -p 5080:80 \

- -e 'PGADMIN_DEFAULT_EMAIL=pekkle@abc.com' \

- -e 'PGADMIN_DEFAULT_PASSWORD=xxxxxx' \

- -e 'PGADMIN_CONFIG_ENHANCED_COOKIE_PROTECTION=True' \

- -e 'PGADMIN_CONFIG_LOGIN_BANNER="Authorised users only!"' \

- -e 'PGADMIN_CONFIG_CONSOLE_LOG_LEVEL=10' \

- -d dpage/pgadmin4:4.17


<table>
  <tr>
    <th>![image 7](<用docker运行postgreSQL.note_images/imageFile7.png>)</th>
  </tr>
</table>


