Elasticsearch是⼀个实时的分布式搜索和分析引擎。它可以帮助你⽤前所未有的速度去处理⼤规模数 据。ElasticSearch是⼀个基于Lucene的搜索服务器。它提供了⼀个分布式多⽤户能⼒的全⽂搜索引 擎，基于RESTful web接⼝。Elasticsearch是⽤Java开发的，并作为Apache许可条款下的开放源码发 布，是当前流⾏的企业级搜索引擎。设计⽤于云计算中，能够达到实时搜索，稳定，可靠，快速，安 装使⽤⽅便。 安装es以及head插件，略。 为了使海量数据能够提供实时快速的查询，mysql很显然⼒不从⼼，于是我们需要利⽤es提供⼤数据搜 索服务，典型的场景就是：产品或者商品搜索。 ⾸先是数据同步，将mysql数据同步到es的⽅式很多，经过测试，稳定且易⽤的是 logstash-inputjdbc 如何安装logstash-input-jdbc插件？ 直接folow： 全量同步与增量同步 全量同步是指全部将数据同步到es，通常是刚建⽴es，第⼀次同步时使⽤。增量同步是指将后续的更 新、插⼊记录同步到es。（删除记录没有办法同步，只能两边执⾏⾃⼰的删除命令） 根据公司内部实践，logstash-input-jdbc增量同步的原理很简单。我们做增量同步是需要知道插⼊和 更新记录的，因此，进⼊ES提供搜索服务的表（要同步的标），都要加上update_time,每次插⼊和更 新的时候更新这个字段，让logstash-input-jdbc知道即可。 详⻅：

htp:/blog.csdn.net/yeyuma/article/details/50240595#quote

htps:/ w.elastic.co/guide/en/logstash/curent/plugins-inputs-jdbc.html#_predefined_par ameters

关键点： where t.update_time > :sql_last_value 测试结果： 先更新⼀条数据看看

![image 1](<同步mysql数据到ElasticSearch的最佳实践.note_images/imageFile1.png>)

然后在es中查询看看有没有更新到

![image 2](<同步mysql数据到ElasticSearch的最佳实践.note_images/imageFile2.png>)

成功，⾃动同步了！

