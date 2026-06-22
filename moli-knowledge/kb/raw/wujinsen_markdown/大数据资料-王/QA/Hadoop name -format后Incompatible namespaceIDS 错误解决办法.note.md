Hadop集群在namenode格式化（bin/hadop namenode -format）后重启集群会出现如下

Incompatible namespaceIDS in. :namenode namespaceID =. ,datanode namespaceID=. 错误，

原因是格式化namenode后会重新创建⼀个新的namespaceID,以⾄于和datanode上原有的不⼀ 致。

解决⽅法： 1、删除datanode dfs.data.dir⽬录（默认为tmp/dfs/data）下的数据⽂件， 2、hadop namenode -format

3、修改/home/hadop/data/curent/VERSION ⽂件，把namespaceID修成与 namenode上相同即可（log错误⾥会有提示）

另外的解决⽅案

- 1、查看集群的所有的namespaceid namenode：${hadop}/namenode/curent/VERSION datanode：${hadop}/data/curent/VERSION

- 2、找出和集群namespaceid不⼀样的，改成⼀样


