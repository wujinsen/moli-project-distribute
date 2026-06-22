⼀、定义MySQL Cluster的TCP/IP连接 TCP/IP是MySQL集群⽤于建⽴连接的默认传输协议，正常情况下不需要定义连接。可使⽤“[TCP DEFAULT]”或“[TCP]”进⾏定义。

- 1. SendBuferMemory TCP传输缓存。默认值为 256KB。
- 2. SendSignalId 通过⽹络传输消息ID。默认禁⽌该特性（取值: Y/N或1/0）。
- 3. Checksum 启⽤该参数将在所有消息置于发送缓冲之前，为所有参数计算校验和。默认禁⽌该特性（取值: Y/N或 1/0）。
- 4. ReceiveBuferMemory 指定从TCP/IP Socket接收数据时所使⽤的缓冲⼤⼩。⼏乎不需要更改该参数的默认值，默认值为 64KB。


⼆、定义数据结点默认⾏为 NoOfReplicas为必要参数，使⽤“[NDBD DEFAULT]”进⾏定义。

1. NoOfReplicas 定义集群中每个表保存的拷⻉数，另外还指定结点组的⼤⼩。结点组指保存相同信息的结点集合。通 常情况下不需要为该参数指定值。NoOfReplicas没有默认值，最⼤的可能值为 4。

三、定义管理服务器(MGM) ⽤于配置管理服务器的⾏为。下⾯的参数均可以被忽略，如果是这样，将使⽤其默认值。如果没有定 义ExecuteOnComputer或HostName，则会指定为localhost。可使⽤“[NDB_MGMD]”定义单个管理结 点的⾏为，也可使⽤“[NDB_MGMD DEFAULT]”定义多个管理结点的默认⾏为。

- 1. NodeId 集群中结点的唯⼀标识，取值 1~63。
- 2. HostName 指定结点主机名或IP。
- 3. ExecuteOnComputer


- 引⽤在“[COMPUTER]”部分中定义的计算机之⼀。
- 4. PortNumber 管理服务器监听端⼝（默认值: 202）。
- 5. LogDestination结点⽇志出处理⽅式，可取下述值：

- 5.1. CONSOLE 将⽇志输出到标准输出设备(stdout)。
- 5.2. SYSLOG:facility=syslog 将⽇志发送到syslog(系统⽇志)软设备，可能值： auth, authpriv, cron, daemon, ftp, kern, lpr, mail, news, syslog , user, ucp, local0, local12~7
- 5.3. FILE:filename=/var/log/mgmd.log,maxsize=1 0,maxfiles=6 讲⽇志输出到⽂件,可指定⼀下值： filename：⽇志⽂件名称。 maxsize：⽇志⽂件最⼤尺⼨,⼤于该尺⼨时⾃动创建新⽇志⽂件。 maxfiles：⽇志⽂件最⼤数量。


- 6. ArbitrationRank指定哪个结点扮演决策⻆⾊，只有MGM结点和SQL结点可以使⽤（默认值：1）。 通常情况下，应将值设为 1，并将所有SQL结点设为 0，以MGM服务器作为决策程序。可取下述值之 ⼀：

- 0：该结点永远不⽤作决策。
- 1：该结点具有⾼优先级。
- 2：该结点具有低有限级。


- 7. ArbitrationDelay 指定管理服务器对决策请求的延迟时间，毫秒为单位，默认为 0。通常情况下不需要改变它。
- 8. DataDir 保存管理服务器输出⽂件的位置，包括⽇志，进程输出⽂件，以及程序的pid⽂件。对于⽇志⽂件，可 通过设置LogDestination的FILE参数覆盖它。


四、定义数据结点（NDBD） ⽤于配置数据结点的⾏为。ExecuteOnComputer或HostName为必要参数。对于各种参数，可以使⽤ 后缀k、M或G指明单位。使⽤“[NDBD]”进⾏定义。

- 1. NodeId


- 启动结点时，可在命令⾏中分配ID（即数据结点ID），也能在配置⽂件中分配。
- 2. HostName 指定结点主机名或IP。
- 3. ExecuteOnComputer 引⽤在“[COMPUTER]”部分中定义的计算机之⼀。
- 4. DataDir 指定存放跟踪⽂件，⽇志⽂件，pid⽂件以及错误⽇志的⽬录。
- 5. BackupDataDir 指定存放备份的⽬录，默认为 {FileSystemPath}/BACKUP。
- 6. DataMemory 指定数据内存，默认值为 80MB，最⼩值 1MB，⽆⼤⼩限制。
- 7. IndexMemory 指定索引内存，默认值为 18MB，最⼩值 1MB，⽆⼤⼩限制。
- 8. MaxNoOfConcurentTransactions ⽤于设定结点内可能的并发事务数，默认值为 4096。对于所有结点，必须将参数设置为相同的值。
- 9. MaxNoOfConcurentOperations 设置能同时出现在更新阶段或同时锁定的记录数。默认值为 32768。
- 10. MaxNoOfLocalOperations 默认情况下，将按照1.1 * MaxNoOfConcurentOperations计算该参数，它适合于具有很多并发事务， 但不存在特⼤事务的系统。如果需要在某⼀时间处理特⼤事务，⽽且有很多结点，最好通过明确指定 该参数以覆盖默认值。


1. MaxNoOfConcurentIndexOperations 该参数的默认值为8192。只有在极其罕⻅的情况下，需要使⽤唯⼀性哈希索引执⾏极⾼的并⾏操作 时，才有必要增⼤该值。如果确信该集群不需要⾼的并⾏操作，可以使⽤较⼩的值并节省内存。

- 12. MaxNoOfFiredTri gers


- 默认值是4 0，它⾜以应付⼤多数情况。在某些情况下，如果认为在集群中对并⾏操作的要求并不⾼, 甚⾄还能降低它。
- 13. TransactionBuferMemory 该参数影响的内存⽤于跟踪更新索引表和读取唯⼀索引时执⾏的操作。该内存⽤于保存关于这类操作 的键和列信息。⼏乎不需要更改该参数的默认值。
- 14. MaxNoOfConcurentScans 该参数⽤于控制可在集群中执⾏的并⾏扫描的数量。默认值为256，最⼤值为50。
- 15. MaxNoOfLocalScans 如果很多扫描不是完全并⾏化的，指定本地扫描记录的数量。
- 16. BatchSizePerLocalScan 该参数⽤于计算锁定记录的数量。要想处理很多并发扫描操作，需要这类记录。默认值是64，该值与 SQL结点中定义的 ScanBatchSize 关系密切。
- 17. LongMesageBufer ⽤于在单个结点内和结点之间传递消息的内部缓冲。尽管⼏乎不需要改变它，但它仍是可配置的。默 认情况下，它被设置为1MB。
- 18. NoOfFragmentLogFiles 设置结点的REDO⽇志⽂件的⼤⼩，默认值为 8。
- 19. MaxNoOfSavedMesages 设置跟踪⽂件的最⼤数，默认值为 25。
- 20. MaxNoOfAtributes 设置可在集群中定义的属性数量，默认值为 1 0，最⼩值为 32。
- 21. MaxNoOfTables 设置集群中最⼤表对象数量。默认值为128，最⼩值为 8，最⼤值为 160。


2. MaxNoOfOrderedIndexes

对于集群中的每个有序索引，会分配⼀个对象，⽤于描述索引的内容，以及它的存储⽚段。在默认情 况下，每个如此定义的索引还定义了⼀个有序索引。每个唯⼀索引和主键都具有⼀个有序索引和⼀个 哈希索引。MaxNoOfOrderedIndexes设置有序索引的总数，这是系统任何时候能够使⽤的有序索引的 总数。这个参数的默认值是128。每个结点中的每个索引对象尺⼨⼤约为10KB。

- 23. MaxNoOfUniqueHashIndexes 对于每个不是主键的唯⼀索引，会分配⼀个表，⽤于将唯⼀键映射⾄索引表的主键。在默认情况下， 还会为每个唯⼀索引定义⼀个有序索引。想要避免这种情况，当定义唯⼀索引时，你还必须指定 “USING HASH”选项。默认值是64。每个结点中的每个索引的尺⼨⼤约为15KB。
- 24. MaxNoOfTri gers 该参数⽤于设置集群中触发器的最⼤数量。
- 25. LockPagesInMainMemory 对于很多操作系统，能够将进程锁定在内存中，以避免与磁盘的交换。使⽤它可以确保集群的实时特 性。默认情况下，该特性是被禁⽌的（取值：Y/N或1/0）。
- 26. StopOnEror 出现错误时，该参数指定NDBD进程是退出还是⾃动重启。默认情况下，该特性是启⽤的（取值：Y/N 或1/0）。
- 27. Diskles 指定集群为“⽆磁盘”，意味着不会为表在磁盘上设⽴检查点，也不会记录任何⽇志。默认情况下，该 特性是被禁⽌的（取值：Y/N或1/0）。
- 28. RestartOnErorInsert 仅当编译为调试版时才能访问该特性。默认情况下，该特性是被禁⽌的。
- 29. TimeBetwenWatchDogCheck 指定监控线程检查的间隔。该参数以毫秒为单位，默认值为 4 0 毫秒。
- 30. StartPartialTimeout 该参数指定了在调⽤集群初始化⼦程序之前，集群等待所有存储结点出现的时间。默认值为 3 0 毫 秒（0 表示⽆限超时）。
- 31. StartPartitionedTimeout


- 如果集群做好了启动准备，但仍可能处于隔离状态，集群将等待该超时时间结束。默认值为 6 0 毫 秒。
- 32. StartFailureTimeout 如果数据结点在该参数指定的时间内未完成其启动序列，结点启动将失败。如果将该参数设置为0，表 示不采⽤数据结点超时。默认值为 6 0 毫秒。


3. HeartbeatIntervalDbDb 每个数据结点发送⼼跳信号到SQL结点的间隔。默认值为 150 毫秒。

- 34. HeartbeatIntervalDbApi 每个数据结点都会向每个MySQL服务器（SQL结点）发送⼼跳信号，以确保它们依然保持接触。如果 ⼀个MySQL服务器没能成功地及时发送⼀个⼼跳，那么就会将其声明为“失效”，在这种情况下，所有 正在进⾏的事务都会结束，并且释放所有资源。SQL结点不能重连，直到由先前的MySQL实例所初始 化的所有活动都已经结束为⽌。默认的时间间隔是150毫秒（1.5秒）单个数据结点之间的这个时间间 隔可以是不同的，因为每个数据结点都会监视与其连接的MySQL服务器，与所有其他的数据结点⽆ 关。
- 35. TimeBetwenLocalCheckpoints 该参数默认值为20。
- 36. TimeBetwenGlobalCheckpoints 该参数定义了全局检查点操作之间的时间间隔。默认值为 2 0 毫秒。
- 37. TimeBetwenInactiveTransactionAbortCheck 该参数默认值为 1 0 毫秒。
- 38. TransactionInactiveTimeout 如果事务⽬前未执⾏任何查询，⽽是等待进⼀步的⽤户输⼊，该参数指明了放弃事务之前⽤户能够等 待的最⻓时间。默认值为 0。
- 39. TransactionDeadlockDetectionTimeout 该超时参数指明了放弃事务之前，事务协调器等候另⼀结点执⾏查询的时间。
- 40. NoOfDiskPagesToDiskAfterRestartTUP 该参数指定了执⾏本地检查点操作的速度，并能与NoOfFragmentLogFiles、DataMemory和 IndexMemory⼀起使⽤。默认值是 40（每秒3.2MB的数据⻚）。


- 41. NoOfDiskPagesToDiskAfterRestartAC 该参数使⽤的单位与NoOfDiskPagesToDiskAfterRestartTUP的相同。⼯作⽅式也类似，但限制的是从 索引内存进⾏的索引⻚写⼊速度。该参数的默认值为每秒20个索引内存⻚（1.6MB每秒）。
- 42. NoOfDiskPagesToDiskDuringRestartTUP 该参数涉及从数据内存写⼊的⻚。默认值是40（3.2MB每秒）。
- 43. NoOfDiskPagesToDiskDuringRestartAC 该参数默认值是20（1.6MB每秒）。


4. ArbitrationTimeout 指定数据结点等待决策程序对决策消息的回应的时间。默认值为 1 0 毫秒。

- 45. UndoIndexBufer 指定UNDO索引缓冲区⼤⼩。默认值为 2MB，最⼩值为1MB。
- 46. UndoDataBufer 指定UNDO数据缓冲区⼤⼩。默认值为 16MB，最⼩值为1MB。
- 47. RedoBufer 指定REDO数据缓冲区⼤⼩。默认值为 8MB，最⼩值为1MB。
- 48. LogLevelStartup ⽇志级别，⽤于进程启动过程中⽣成的事件。默认级别为 1。
- 49. LogLevelShutdown ⽇志级别，⽤于作为结点恰当关闭进程组成部分⽽⽣成的事件。默认级别为 0。
- 50. LogLevelStatistic ⽇志级别，⽤于统计事件，如主键法读取次数、更新数、插⼊数、与缓冲使⽤有关的信息等。默认级 别为 0。
- 51. LogLevelCheckpoint ⽇志级别，⽤于由本地和全局检查点操作⽣成的事件。默认级别为 0。
- 52. LogLevelNodeRestart


- ⽇志级别，⽤于在结点重启过程中⽣成的事件。默认级别为 0。
- 53. LogLevelConection ⽇志级别，⽤于由集群结点间的连接⽣成的事件。默认级别为 0。
- 54. LogLevelEror ⽇志级别，⽤于由在整个集群内的错误和警告⽣成的事件。这类错误不会导致任何结点失败，但仍值 得记录。默认级别为 0。


5. LogLevelInfo ⽇志级别，⽤于为集群的⼀般状态信息⽽⽣成的事件。默认级别为 0

- 56. BackupDataBuferSize 指定数据备份缓冲区⼤⼩。默认值为 2MB。
- 57. BackupLogBuferSize 指定⽇志备份缓冲区⼤⼩。默认值为 2MB。
- 58. BackupMemory 该参数是BackupDataBuferSize和BackupLogBuferSize之和。默认值是2MB + 2MB = 4MB。
- 59. BackupWriteSize 该参数指定了由备份⽇志缓冲和备份数据缓冲写⼊磁盘的消息⼤⼩。默认值为 32KB。
- 60. FileSystemPath 该参数指定了存放为元数据创建的所有⽂件、REDO⽇志、UNDO⽇志和数据⽂件⽬录。注意：在 ndbd进程启动前，该⽬录必须已存在。


五、定义MySQL服务器（SQL） 定义⽤于访问集群数据的MySQL服务器(SQL结点)的⾏为。可使⽤“[MYSQLD]”定义单个SQL结点的⾏ 为，也可使⽤“[MYSQLD DEFAULT]”定义多个SQL结点的默认⾏为。

- 1. NodeId 集群中结点的唯⼀标识。取值 1~63。
- 2. HostName 指定结点主机名或IP。


- 3. ExecuteOnComputer 引⽤在“[COMPUTER]”部分中定义的计算机之⼀。
- 4. ArbitrationRank 对于正常配置，使⽤管理服务器作为决策程序。将管理服务器的ArbitrationRank设置为 1（默认），并 将所有SQL结点的ArbitrationRank设置为 0。
- 5. ArbitrationDelay 指定管理服务器对决策请求的延迟时间，以毫秒为单位。默认为 0，通常情况下不需要改变它。
- 6. BatchByteSize 对于被转换为全表扫描或索引范围扫描的查询来说，以适当⼤⼩批量获取记录，可以获得最佳的性 能。这个合适的尺⼨既可以⽤记录数量（BatchSize）表示，也可以⽤字节数量（BatchByteSize）表 示。实际的批量尺⼨受这两个参数的限制。根据这个参数的设置⽅法，查询性能最多可以提⾼40%。 该参数以字节为单位，默认值是 32KB。
- 7. BatchSize 该参数以字节为单位，默认值是 64，最⼤值为 92。
- 8. MaxScanBatchSize 指定从各数据结点发送的每批数据的⼤⼩，默认值是 256KB，最⼤值为 16MB。


六、配置⽂件示例

<table>
  <tr>
    <th>[ndbd default] # 数据结点的默认配置<br><br>NoOfReplicas=2 # 数据在集群中具有两份拷⻉<br><br>DataMemory=80M # 数据内存⼤⼩为80 MB<br><br>IndexMemory=18M # 索引内存⼤⼩为18 MB<br><br>[ndb_mgmd] # 管理结点配置<br><br>NodeId=1<br><br>hostname=192.168.124.141 # 管理结点的IP地址 datadir=/var/lib/mysql-cluster # 保存管理结点的输出⽂件的位置<br><br>[ndbd] # 数据结点的配置 NodeId=2<br><br>hostname=192.168.124.142 # 数据结点的IP地址<br><br>datadir=/usr/local/mysql/data # 指定存放跟踪⽂件、⽇志⽂件、pid⽂件以及错误⽇志的⽬录<br><br>[ndbd] # 数据结点的配置 NodeId=3<br><br>hostname=192.168.124.143 # 数据结点的IP地址 datadir=/usr/local/mysql/data [mysqld] # SQL结点的配置<br><br><br>NodeId=4 hostname=192.168.124.14 # SQL结点的IP地址<br><br>[mysqld] # SQL结点的配置<br><br>NodeId=5 地址<br></th>
  </tr>
</table>


# hostname=192.168.124.145 # SQL结点的IP

