⼀.Colections API 参考： 因为API⽐较多，我就不⼀⼀列举，只列出⽐较重要的⼏个

htps:/cwiki.apache.org/confluence/display/solr/Colections+API

- 1.创建colection 官⽅示例：/admin/colections? action=CREATE&name=name&numShards=number&replicationFactor=number&maxShardsPerNod e=number&createNodeSet=nodelist&colection.configName=configname


- （1） 我的示例：

name指明colection名称 numShards指明分⽚数 replicationFactor指明副本数 maxShardsPerNode 每个节点最⼤分⽚数（默认为1）

- （2）当我们想指定配置⽂件，索引⽬录时，可以加⼊如下参数


htp:/192.168. 6. 9 8080/solr/admin/colections?action=CREATE&name=test&numShard s=2&replicationFactor=2&maxShardsPerNode=3

<table>
  <tr>
    <th>property.name=value</th>
    <th>string</th>
    <th>N o</th>
    <th> </th>
    <th>Set core property name to value. Se coreproperties file</th>
  </tr>
</table>


contents.

可选参数如下：

key Description

<table>
  <tr>
    <th>name</th>
    <th>The name of the SolrCore. You'l use this na e to reference the SolrCore when runing</th>
  </tr>
  <tr>
    <td>config</td>
    <td>comands with the CoreAdminHandler. e configuration file name for a given core.</td>
  </tr>
  <tr>
    <td>schema</td>
    <td>The default is solrconfig.xml. The schema file name for a given core. The</td>
  </tr>
  <tr>
    <td>dataDir</td>
    <td>default is schema.xml Core's data directory as a path relative to the</td>
  </tr>
  <tr>
    <td>configSet</td>
    <td>instanceDir, data by default. If set, the name of the configset to use to</td>
  </tr>
  <tr>
    <td>properties</td>
    <td>configure the core (se Config Sets).<br><br>he nme of the properties file for this core. The value can be an absolute pathname or a</td>
  </tr>
  <tr>
    <td>transient</td>
    <td>path relative to the value of instanceDir. If true, the core can be unloaded if Solr reaches the transientCacheSize. The default if not specified is false. Cores are unloaded in</td>
  </tr>
  <tr>
    <td>loadOnStartup</td>
    <td>order of least recently used first. If true, the default if it is not specified, the</td>
  </tr>
  <tr>
    <td>coreNodeName</td>
    <td>core wil loaded when Solr starts. Aded in Solr 4.2, this atributes alows naming a core. The name can then be used later if you ned to replace a machine with a new one. By asigning the new machine the same coreNodeName as the old core, it wil</td>
  </tr>
  <tr>
    <td>ulogDir</td>
    <td>take over for the old SolrCore. The absolute or relative directory for the</td>
  </tr>
  <tr>
    <td>shard</td>
    <td>update log for this core (SolrCloud) The shard to asign this core to (SolrCloud)</td>
  </tr>
  <tr>
    <td>colection</td>
    <td>The name of the colection this core is part of</td>
  </tr>
  <tr>
    <td>roles</td>
    <td>(SolrCloud) Future param for SolrCloud or a way for users</td>
  </tr>
</table>


to mark nodes for their own use.

htp:/192.168. 6. 9 8080/solr/admin/colections?action=CREATE&name=test&num Shards=2&replicationFactor=2&maxShardsPerNode=3&property.schema=schema2.xml&prop erty.dataDir=/usr/local/data/solr

- （3）运⾏


以上命令将会创建colection test，指定schema2.xml作为其schema配置⽂件，并指 定/usr/local/data/solr为其数据存放⽬录 （注意如果指定相关配置⽂件，⾸先要向zokeper中上传相关的配置，运⾏⼀下命令将 schema2.xml上传到zokeper

java -claspath .:/usr/local/solr/solrhome-1/lib/* org.apache.solr.cloud.ZkCLI -cmd upconfig zkhost 127.0.0.1 181,127.0.0.1 2181,127.0.0.1 3181 -confdir /usr/local/solr/solrhome-1/update/ confname solr-conf ） 在我本机运⾏时出现错： org.apache.solr.client.solrj.impl.HtpSolrServer$RemoteSolrException:Eror CREATEing SolrCore 'test_shard1_replica1': Unable to create core: test_shard1_replica1 Caused by: Lock obtain timed out: NativeFSLock@/usr/local/data/solr/index/write.lock

这是因为3个节点都在我本机，我们将索引⽬录指定为同⼀个，这种创建⽅式默认的数据⽂件 夹会重复，我们可以分别指定分⽚⽂件夹

- 2.删除colection 官⽅示例：/admin/colections?action=DELETE&name=colection 我的示例：
- 3.创建分⽚ 官⽅示例：/admin/colections?action=CREATESHARD&shard=shardName&colection=name /admin/colections?action=SPLITSHARD: split a shard into two new shards 我的示例：


htp:/192.168. 6. 9 8080/solr/admin/colections?action=DELETE&name=test

htp:/192.168. 6. 9 8080/solr/admin/colections?action=CREATESHARD&colection=te st&shard=shard1&name=test_shard1_replica1&property.schema=schema2.xml&property.dataDir=/ usr/local/data/solr/test_shard1_replica1

本⼈测试，如果colection是使⽤第1节⽅式创建的，使⽤这种⽅式进⾏创建分⽚时，⽆法正确执⾏，原 因待研究4.其他

/admin/colections?action=RELOAD: reload a colection /admin/colections?action=SPLITSHARD: split a shard into two new shards /admin/colections?action=CREATESHARD: create a new shard /admin/colections?action=DELETESHARD: delete an inactive shard /admin/colections?action=CREATEALIAS: create or modify an alias for a colection /admin/colections?action=DELETEALIAS: delete an alias for a colection

/admin/colections?action=DELETEREPLICA: delete a replica of a shard /admin/colections?action=ADREPLICA: ad a replica of a shard /admin/colections?action=CLUSTERPROP: Ad/edit/delete a cluster-wide property /admin/colections?action=MIGRATE: Migrate documents to another colection /admin/colections?action=ADROLE: Ad a specific role to a node in the cluster /admin/colections?action=REMOVEROLE: Remove an asigned role /admin/colections?action=OVERSERSTATUS: Get status and statistics of the overser /admin/colections?action=CLUSTERSTATUS: Get cluster status /admin/colections?action=REQUESTSTATUS: Get the status of a previous asynchronous request /admin/colections?action=LIST: List al colections

⼆.Cores API solr的core在我看来是对shard进⾏各种操作的，⼀个core可视为⼀个shard或者其replica的管理，但是 也可以创建colection， 参考：

htps:/cwiki.apache.org/confluence/display/solr/CoreAdminHandler+Parameters+and+Usag e

访问⽅式： ，操作有以下⼏种

htp:/localhost:8983/solr/admin/cores?action=action

STATUS CREATE RELOAD RENAME SWAP

UNLOAD

MERGEINDEXES

SPLIT

REQUESTSTATUS

- 1.查看状态 官⽅示例：
- 2.创建core官⽅示例：


htp:/localhost:8983/solr/admin/cores?action=STATUS&core=core0

htp:/localhost:8983/solr/admin/cores?action=CREATE&name=coreX&instan ceDir=path/to/dir&config=config_file_name.xml&schema=schem_file_name.xml&dataDir=data

可选参数基本与创建colection相同 Parameter Description

<table>
  <tr>
    <th>name</th>
    <th>The n<core>ame of the new core. Same as "name" on<br><br></th>
  </tr>
  <tr>
    <td>instanceDir</td>
    <td>the element. The directory where files for this SolrCore should be stored. Same as instanceDir on<br><br><core></td>
  </tr>
  <tr>
    <td>config</td>
    <td>the element.<br><br>Optional) Name of the confiinstanceDirg file<br><br></td>
  </tr>
  <tr>
    <td>schema</td>
    <td>(solrconfig.xml) relative to .<br><br>Optional) Name of the schinstanceDirema file<br><br></td>
  </tr>
  <tr>
    <td>datadir</td>
    <td>(schema.xml) relative to .<br><br>(OptinstanceDirional) Name of the data directory relative<br><br></td>
  </tr>
  <tr>
    <td>configSet</td>
    <td>to . (Optional) Name of the configset to use for this core (se Config Sets</td>
  </tr>
  <tr>
    <td>colection</td>
    <td>) (Optional) The name of the colection to which this core belongs. The default is the name of the core. collection.<param>=<value> causes a property of <param>=<value> to be set if a new colection is being created. Use collection.configName=<configname> to<br><br></td>
  </tr>
  <tr>
    <td>shard</td>
    <td>point to the configuration for a new colection. (Optional) The shard id this core represents. Normaly you want to be auto-asigned a shard</td>
  </tr>
  <tr>
    <td>property.name=value</td>
    <td>id. (Optional) Sets the core property name to value. Se core.properties file contents</td>
  </tr>
  <tr>
    <td>async</td>
    <td>. (Optional) Request ID to track this action which</td>
  </tr>
</table>


wil be procesed asynchronously

我的示例：

htp:/192.168. 6. 9 8080/solr/admin/cores?action=CREATE&name=test&colection=test&shard=s hard1&instanceDir=/usr/local/data/solr/solr-1/test/&schema=schema2.xml

name指明core名称 该名称为solrhome下的⽂件夹名称，该⽂件夹下存放该分⽚的数据⽂件 colection指明colection名称 若colection 不存在则创建 若存在则判断shard shard指明分⽚名称 若shard不存在，则创建 若存在则创建⼀个该分⽚的副本 该命令会在 上创建⼀个名为test的colection，并且创建⼀个名为shard1的 分⽚，并且该机器为这个分⽚的leader

htp:/192.168. 6. 9 8080

htp:/192.168. 6. 9 8080/solr/admin/cores?action=CREATE&name=test_shard1_replica_2&colect ion=test&shard=shard1

该命令会在 上为test创建shard1的副本

htp:/192.168. 6. 9 8080

- 3.刷新core 官⽅示例：
- 4.重命名core 官⽅示例：
- 5.交换core 官⽅示例：
- 6.下线core 官⽅示例： 可选参数：
- 7.合并索引

官⽅示例：

- ⽅式1：
- ⽅式2：


- 8.切分 官⽅示例：


htp:/localhost:8983/solr/admin/cores?action=RELOAD&core=core0

htp:/localhost:8983/solr/admin/cores?action=RENAME&core=core0&other=core5

htp:/localhost:8983/solr/admin/cores?action=SWAP&core=core1&other=core0

htp:/localhost:8983/solr/admin/cores?action=UNLOAD&core=core0

1.

deleteIndex : if true, will remove the index when unloading the core. deleteDataDir : if true, removes the data directory and al sub-directories. deleteInstanceDir : if true, removes everything related to the core, including the index

directory, configuration files, and other related files.

async : if set to a value, makes the cal asynchronous. This cal can then be tracked using the REQUESTSTATUS API.

htp:/localhost:8983/solr/admin/cores?action=MERGEINDEXES&core=core0&indexDir=/op t/solr/core1/data/index&indexDir=/opt/solr/core2/data/index

htp:/localhost:8983/solr/admin/cores?action=mergeindexes&core=core0&srcCore=core1 &srcCore=core2

htp:/localhost:8983/solr/admin/cores?action=SPLIT&core=core0&targetCore=core1&t argetCore=core2

可选参数： Parameter Description Multi-valued

<table>
  <tr>
    <th>core</th>
    <th>The name of the core to be</th>
    <th>false</th>
  </tr>
  <tr>
    <td>path</td>
    <td>split. The directory path in which a piece of the index wil be</td>
    <td>true</td>
  </tr>
  <tr>
    <td>targetCore</td>
    <td>writen. The target Solr core to which a piece of the index wil be</td>
    <td>true</td>
  </tr>
  <tr>
    <td>ranges</td>
    <td>merged A coma-separated list of hash ranges in hexadecimal</td>
    <td>false</td>
  </tr>
  <tr>
    <td>split.key</td>
    <td>format The key to be used for spliting</td>
    <td>false</td>
  </tr>
  <tr>
    <td>async</td>
    <td>the index (Optional) Request ID to track this action which wil</td>
    <td>false</td>
  </tr>
</table>


be procesed asynchronously

- 9.查看请求状态 官⽅示例：


htp:/localhost:8983/solr/admin/cores?action=REQUESTSTATUS&requestid=1

三.colection实践拓展 上述API提供给了我们⼀组操作colection和core的⽅法，现在来想⼀想实际场景中可能遇到的问题

- 1.场景1新增colection 搭建完solrcloud后我们⾸先要考虑的就是建⽴colection，并对其进⾏分⽚，我们有两种⽅式来做这件 事

- （1）让solrcloud⾃动帮我们分⽚，指定分⽚名称等，即运⾏命令：
- （2）⾃⼰指定每个分⽚的机器，即分别运⾏命令：


. 这两种⽅式均可以指定配置⽂件，及存储路径

- 2.场景2-扩容 随着数据量和访问量的增⼤，我们需要对solrcloud进⾏扩容，以维持其运⾏，这⼜可能包含两种场景


htp:/192.168. 6. 9 8080/solr/admin/colections?action=CREATE&name=test&numShards=2&r eplicationFactor=2&maxShardsPerNode=3

htp:/192.168. 6. 9 7080/solr/admin/cores?action=CREATE&name=test_shard1_replica_1&cole ction=test&shard=shard1

- （1）增加⼀个colection shard ⽅式⼀：使⽤action=SPLITSHARD将⼀个分⽚切分成两块，然后再进⾏重命名等其他操作 ⽅式⼆：使⽤cores?action=CREATE&name=test&colection=test&shard=shard1直接创建
- （2）增加⼀个shard的副本 同样使⽤cores?action=CREATE&name=test&colection=test&shard=shard1直接创建


- 3.场景3-更换服务器 个⼈建议如下，先将新服务器加⼊solrcloud，同步索引⽂件，然后再下线⽼服务器，安全快捷直接通 过管理界⾯即可实现 通过以上场景可以发现，使⽤core api在实际情况下可能更加快捷，因此可以重点学习
- 4.另外，有时我们在配置solrcloud过程中可能会出现各种配置错误，这种错误会在solrcloud的管理界 ⾯进⾏提示，⽐如配置colection时指定schema.xml⽽在zokeper中并不存在指定的⽂件 这时solrcloud就会提示： test3_shard2_replica1: org.apache.solr.comon.SolrException:org.apache.solr.comon.SolrExcept ion: Could not load core configuration for core test3_shard2_replica1 如何处理这种错误呢：


- （1）删除solrhome下的相关⽂件夹
- （2）挨个重启solrcloud节点


