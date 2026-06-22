NoSQL现在风⽣⽔起，hbase的使⽤也越来越⼴，但⽬前⼏乎所有的NoSQL产品在运维上都没法和DB 相提并论，在这篇blog中来总结下我们在运维hbase时的⼀些问题以及解决的⽅法，也希望得到更多 hbase同⾏们的建议，:) 在运维hbase时，⽬前我们最为关注的主要是三⼤⽅⾯的状况：

- 1. Cluster load；
- 2. 读写；
- 3. 磁盘空间。


- 1. Cluster load 集群的load状况直接反映了集群的健康程度，load状况的获取⾮常容易，直接部署ganglia即可得到， 由于hbase以优秀的可伸缩性著称，因此多数情况下load超出接受范围时加机器是⼀个不错的解决⽅ 法，当然，这还和系统的设计和使⽤hbase的⽅式有关。 如有出现个别机器load⽐较⾼的现象，通常是由于集群使⽤的不均衡造成，需要进⾏⼀定的处理，这 个放到读写部分再说吧。
- 2. 读写 读写⽅⾯的信息主要包括了读写的次数以及速度，这个通过ganglia看其实不怎么⽅便，最好还是⾃⼰ 改造下实现，读写次数反映了集群的使⽤程度，⼀般来说需要根据压⼒测试中形成的报告来设置⼀个 读写次数的阈值，以保护系统的稳定。


读写速度⽅⾯主要是显示当前的读写速度状况（当然，也需要有历史的报表），如读写速度是在可接 受范围，其实就可以不⽤过多的关⼼了，如读写速度不OK的话，则需要进⾏⼀定的处理。

如读速度不OK，则需要关注以下⼏点：

- * 集群均衡吗？ 集群是否均衡主要需要通过三个⽅⾯来判断：各region server的region数是否均衡、table的region是 否均衡分布还有就是读请求是否均衡分布。 通常各region server的region数是均衡的，这个是⽬前hbase master的balance策略来保证的，顶多就 是有短暂时间的不均衡现象。 table 的region分布则不⼀定是均衡的，对于有多个table的情况，完全有可能出现某张表的⼀堆的 region在同⼀台上的现象，对于这种情况，⼀种⽅法 是修改master的balance策略，让其在balance时 考虑table的region的balance，我们⽬前采⽤的是另外⼀种⽅法，提供了 ⼀个界⾯来⼿⼯balance table的region，如果是由于table的region不均衡导致了读速度的不OK，可以采⽤这种办法解决下。 读 请求是否均衡分布⼀⽅⾯取决于table的region的分布状况，另⼀⽅⾯则取决于应⽤的使⽤⽅法，如 table的region分布均衡，读请求仍然不 均衡分布的话，说明应⽤的请求有热点的状况，如这种状况造 成了读速度的不OK，可以⼿⼯将region进⾏拆分，并分配到不同的region server上，这是hbase很简 单的⼀种应对热点的解决⽅法。


- * cache的命中率如何？ cache的命中率⽬前通过ganglia以及region server的log都不是很好看，因此我们也进⾏了改造，更直 ⽩的显示cache的命中率的变化状况。 如 cache的命中率不⾼，⾸先需要看下⽬前系统⽤于做LRUBlockcache的⼤⼩是不是⽐较⼩，这主要 靠调整region server启动的-Xmx以及hfile.block.cache.size参数来控制，可通过修改hbase-env.sh， 增加export HBASE_REGIONSERVER_OPTS=”-Xmx16g”来单独的调整region server的heap size，如 LRUBlockCache已⾜够⼤，cache命中率仍然不⾼的话，则多数情况是由于随机读⽆热点造成的，这 种情况如果要提升cache命 中率的话，就只能靠加机器了。 在cache的使⽤率上要关注应⽤对数据的读取⽅式，经常整⾏数据读取的适合设计在同⼀cf⾥，不经常 整⾏读取的适合设计在多cf⾥。
- * bl omfilter打开了吗？ 默 认情况下创建的table是不打开bl omfilter的（可通过describe table来确认，如看到BLOMFILTER

=> ‘NONEʼ则表示未打开），对于随机读⽽⾔这个影响还是⽐较明显的，由于bl omfilter⽆法在之后动 态打开，因此创建表时最好就处理好，⽅法类 似如此：create ‘t1′, { NAME => ‘f1′, BLOMFILTER => ‘ROWCOLʼ }。

- * Compact 在某些特殊的应⽤场景下，为了保证写速度的平稳，可能会考虑不进⾏Compact，不进⾏compact会 导致读取数据时需要扫描⼤量的⽂件，因此compact还是有必要做的。
- * 应⽤的使⽤⽅式 应⽤在读取数据时是随机读、有热点的随机读还是连续读，这个对读速度也有很⼤的影响，这个需要 结合业务进⾏分析，总的来说，hbase在随机读上效率还是很⼀般的，这和它的存储结构有⼀定关系。 另外，应⽤在读取时如每次都是读取⼀⾏的所有数据的话，schema设计的时候在同⼀个cf下就⽐较合 适。

如写速度不OK，则需要关注以下⼏点：

- * 集群均衡吗？ 除 了和读⼀样的集群均衡性问题外，还有⼀个问题是rowKey的设计问题，因为hbase是按rowKey连续 存储的，因此如应⽤写⼊数据时rowKey是 连续的，那么就会造成写的压⼒会集中在单台region server 上，因此应⽤在设计rowKey时，要尽可能的保证写⼊是分散的，当然，这可能会对有连续读需求的场 景产⽣⼀定的冲击。
- * ⽇志中是否出现过以下信息？
- * Flush thread woke up with memory above low water.


⽇ 志中出现这个信息说明有部分写出现过阻塞等待的现象，造成这个现象的原因是各个region的 memstore使⽤的⼤⼩加起来超过了总的阈值，于是阻塞 并开始找⼀个region进⾏flush，这个过程会 需要消耗掉⼀些时间，通常来说造成这个的原因是单台region server上region数太多了，因此其实单台 region server上最好不要放置过多的region，⼀种调节⽅法是调⼤split的fileSize，这样可以适当的减 少region数，但需要关注调整后 读性能的变化。

* delaying flush up to 当⽇志中出现这个信息时，可能会造成出现下⾯的现象，从⽽产⽣影响，这个通常是store file太多造 成的，通常可以调⼤点store file个数的阈值。

- * Blocking updates for

当 ⽇志中出现这个信息时，表示写动作已被阻塞，造成这个现象的原因是memstore中使⽤的⼤⼩已超 过其阈值的2倍，通常是由于上⾯的delaying flush up to造成的，或者是region数太多造成的，或者是 太多hlog造成的，这种情况下会造成很⼤的影响，如内存够⽤的话，可以调⼤阈值，如其他原因则需 要 对症下药。

- * split造成的？ split会造成读写短暂的失败，如写的数据⽐较⼤的话，可能会有频繁的split出现，对于这种情况主要需 要依靠调⼤split的filesize（hbase.hregion.max.filesize）来控制。


- 3. 磁盘空间 磁 盘空间可直接通过hdfs的管理界⾯查看，磁盘空间如占⽤⽐较多的话，可以关注下表的压缩是否开 启（describe表后，COMPRESION => ‘NONEʼ表示未开启），默认是不开启的，在创建表时可 create ‘t1′,{NAME => “cf1″,COMPRESION => “LZO”}，如为已经创建的表，则需要先disable、alter、 enable后再执⾏下major_compact，在我们的⼏个案例中⼤概能压 缩到原⼤⼩的20%–30%，还是很 可观的。 如压缩已开启，占⽤仍然⽐较多的话，基本就只能增加机器或升级硬盘了，由于hbase需要对每列重复 存储rowkey，因此会有⼀定的空间浪费。


