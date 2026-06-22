![image 1](<Hadoop应用开发技术详解》迷你书_images/imageFile1.png>)

大数据技术丛书

# Hadoop应用开发技术详解

刘 刚 著

![image 2](<Hadoop应用开发技术详解》迷你书_images/imageFile2.png>)

![image 3](<Hadoop应用开发技术详解》迷你书_images/imageFile3.png>)

图书在版编目（CIP）数据

Hadoop 应用开发技术详解 / 刘刚著．— 北京：机械工业出版社，2014.1 （大数据技术丛书）

ISBN 978-7-111-45244-7

I. H… II. 刘… III. 数据处理软件－程序设计 IV. TP274

中国版本图书馆 CIP 数据核字（2013）第 309446 号

版权所有·侵权必究 封底无防伪标均为盗版 本书法律顾问 北京市展达律师事务所

本书由资深 Hadoop 技术专家撰写，系统、全面、深入地讲解了 Hadoop 开发者需要掌握的技术 和知识，包括 HDFS 的原理和应用、Hadoop 文件 I/O 的原理和应用、MapReduce 的原理和高级应用、 MapReduce 的编程方法和技巧，以及 Hive、HBase 和 Mahout 等技术和工具的使用。并且提供大量基于实 际生产环境的案例，实战性非常强。

![image 4](<Hadoop应用开发技术详解》迷你书_images/imageFile4.png>)

全书一共 12 章。第 1 ～ 2 章详细地介绍了 Hadoop 的生态系统、关键技术以及安装和配置；第 3 章 是 MapReduce 的使用入门，让读者了解整个开发过程；第 4 ～ 5 章详细讲解了分布式文件系统 HDFS 和 Hadoop 的文件 I/O；第 6 章分析了 MapReduce 的工作原理；第 7 章讲解了如何利用 Eclipse 来编译 Hadoop 的源代码，以及如何对 Hadoop 应用进行测试和调试；第 8 ～ 9 章细致地讲解了 MapReduce 的开发方法和 高级应用；第 10 ～ 12 章系统地讲解了 Hive、HBase 和 Mahout。

机械工业出版社（北京市西城区百万庄大街 22 号 邮政编码 100037） 责任编辑：白 宇

印刷 2014 年 1 月第 1 版第 1 次印刷 186mm×240 mm·2625 印张 标准书号：ISBN 978-7-111-45244-7 定 价：79.00 元

凡购本书，如有缺页、倒页、脱页，由本社发行部调换 客服热线：（010）88378991 88361066 投稿热线：（010）88379604 购书热线：（010）68326294 88379649 68995259 读者信箱：hzjsj@hzbook.com

## 前 言

### 为什么要写这本书

三年前接触 Hadoop 的时候，Hadoop 在国内还没有普及，学习的资料也是少之又少， 只能看英文文档一步一步操作，出现错误只能自己解决或者查看英文资料，有时候一个问 题好几天才解决，学习起来非常的麻烦。

古时候，人们用牛来拉重物，当一头牛拉不动一根圆木的时候，他们不曾想过培育更 强壮的牛。同样，我们也不需要尝试更大的计算机，而是应该开发更多的计算系统。简单 说，数据量越来越大，我们的大型机负担不了，因此集群就产生了，而 Hadoop 就是为分布 式计算和分布式存储而诞生的。最近两年，随着互联网的快速发展，公司的日志量成数量 级增长，从而导致使用 Hadoop 的公司也越来越多，Hadoop 人才也就越来越供不应求，尤 其是有丰富工作经验的人才。

![image 5](<Hadoop应用开发技术详解》迷你书_images/imageFile5.png>)

在开源的 Hadoop 社区里，很多人问我怎么学习 Hadoop，学习 Hadoop 需要什么样的 基础，如何才能成为一个 Hadoop 的技术大牛。为了帮助更多的人，我决定写本书，将我对 Hadoop 的理解和实战经验与大家分享。希望帮助读者在学习 Hadoop 的道路上少走弯路， 避免一些不必要的时间付出。

### 读者对象

本书适合以下读者阅读。

❑ 大数据爱好者

❑ Hadoop 入门者

❑ 海量数据处理的从业人员

❑ 有 Java 基础，想学习 Hadoop 的开发者

❑ Hive、HBase 和 Mahout 的爱好者

❑ 具有 1 ～ 3 年使用 Hadoop 经验，想进一步提高能力的使用者

❑ 开设相关课程的大专院校

### 如何阅读本书

本书共 12 章。

###### IV

- 第 1 章是 Hadoop 概述，互联网时代什么最重要——数据！大数据时代什么工具最流

行——Hadoop ！本章主要从大体上介绍 Hadoop，让读者对 Hadoop 有个大体的印象。

- 第 2 章主要是对 Hadoop 安装进行一步一步地详解，包括 Hadoop 的单机模式、伪分布

式模式以及分布式模式，让读者了解 Hadoop 的安装过程。

- 第 3 章是 MapReduce 快速入门，通过一个实例介绍 MapReduce 开发的整个过程。包括 环境的准备、Mapper 和 Reducer 代码的编写、打包、部署和运行的一整套流程。这套流程 对以后 MapReduce 的开发奠定了基础。此外，介绍了 MapReduce 开发遇到的常见错误，以 及解决方法。
- 第 4 章本章详细讲解了 Hadoop 分布式文件系统（HDFS），让读者对 HDFS 有一个全 面的了解。HDFS 是 Hadoop 里面的一个核心组件，任何基于 Hadoop 应用的工具都要用到 HDFS，学习本章对 Hadoop 系统的维护有很大的帮助。
- 第 5 章本章主要讲 Hadoop I/O 底层的知识，让读者了解 Hadoop 底层的一些原理，比 如数据在 Hadoop 上是怎么传输的。学完这章对 Hadoop 的输入和输出、序列化和反序列 化、Writable 类型等有进一步的理解。
- 第 6 章 主 要 介 绍 MapReduce 的 工 作 原 理， 讲 解 了 JobTracker、TaskTracker、 任 务 的调度、任务工作的原理以及 MapReduce 的 Shuffle 和 Sort 机制等，此外还介绍第二代 MapReduce（YARN）。通过本章内容的学习，将会对日后 MapReduce 编程开发奠定基础。
- 第 7 章 主 要 介 绍 三 部 分 内 容，Eclipse 的 MapReduce 插 件 安 装、 利 用 插 件 调 试 MapReduce 代码，以及 MapReduce 单元测试。目的是提高读者编写 MapReduce 的调试、 测试的效率。选择和使用一个好的工具是项目成功的一半。
- 第 8 章 详 细 讲 解 了 MapReduce 编 程 开 发 用 到 的 知 识 点， 如 Combiner、Partitioner、 DistributedCache 等。全面介绍了 Mapper 和 Reducer 两个抽象类，最后介绍了如何使用 Hadoop Streaming 接口开发 MapReduce 程序。
- 第 9 章介绍了 MapReduce 的一些高级应用、算法等知识点，算是对第 8 章学的知识进 行应用，本章内容是 MapReduce 开发和 Hadoop 系统调优常用的知识点，希望读者能够掌 握，为 MapReduce 的算法开发和调优打下基础。
- 第 10 章对 Hive 的知识点和使用做了介绍。Hive 是基于 Hadoop 的一个数据仓库，支持 类似 SQL 语句来替代 MapReduce，这使不会写代码的数据分析师也能利用 Hadoop 平台对 海量数据进行分析，如 DBA。Hive 工具现在已经非常成熟，使用的人也越来越多。
- 第 11 章介绍了 HBase 的安装和 Shell 操作、MapReduce 操作 HBase 等。HBase 是一个 基于列存储的 NoSQL 数据库，不同于一般的 NoSQL 数据库，HBase 是一个适合于非结构 化数据存储的数据库。
- 第 12 章介绍了 Mahout 算法框架的安装和使用。本章介绍的常用知识是入门的最佳实 战。Mahout 是非常优秀的基于 MapReduce 的算法框架。Mahout 工具在商品推荐、电影推荐、 广告推荐等领域应用非常广泛。


![image 6](<Hadoop应用开发技术详解》迷你书_images/imageFile6.png>)

附录 A 为 Hive 内置操作符与函数。附录 B 为 HBase 默认配置解释。附录 C 为 Hadoop

###### V

三个配置文件的参数含义说明。

其中第 8 章和第 9 章侧重于 Java 开发 MapReduce 的用户，如果你是一名有经验的 Hadoop 用户，并且想学习 MapReduce 开发，可以直接阅读这两章的内容。但是如果你是 一名初学者，请一定从第 1 章的基础理论知识开始学习。

### 勘误和支持

除封面署名外 , 参加本书编写工作的还有 ：向磊、艾男、曾泽、胡畔、郭洁、童小军、 王虎、李晋博、林斌、闫越等。由于作者的水平有限，编写时间仓促，书中难免会出现一 些错误或者不准确的地方，恳请读者批评指正。书中的全部源文件可以从华章网站 下载， 我也会定期将相应的功能更新及时更正出来。可以将书中的错误和遇到的任何问题，发邮 件到 jaylg2010@gmail.com。如果你有更多的宝贵意见，也欢迎发送邮件，期待能够得到你 们的真挚反馈。

![image 7](<Hadoop应用开发技术详解》迷你书_images/imageFile7.png>)

![image 8](<Hadoop应用开发技术详解》迷你书_images/imageFile8.png>)

### 致谢

首先要感谢 Doug Cutting，他开创了一款影响整个互联网领域的开源平台——Hadoop。 感谢机械工业出版社华章公司的杨福川老师，在这一年多的时间始终支持我的写作，

你的鼓励和帮助引导我顺利完成全部书稿。感谢编辑白宇对稿件的认真修改，你提出的一 些写书注意事项，让我受益匪浅。

感谢胡畔老师对我的支持和鼓励。 最后感谢我的爸爸、妈妈，感谢你们将我培养成人，并时时刻刻为我灌输着信心和

力量！ 谨以此书献给我最亲爱的家人，以及众多热爱 Hadoop 的朋友们！

一 参见华章网站 www.hzbook.com。——编辑注

## 目 录

前 言

- 3.1.1 使用 Eclipse 创建一个 Java 工程 / 30
- 3.1.2 导入 Hadoop 的 JAR 文件 / 31


- 第 1 章 Hadoop 概述 / 1

- 1.1 Hadoop 起源 / 1

- 1.1.1 Google 与 Hadoop 模块 / 1
- 1.1.2 为什么会有 Hadoop / 1
- 1.1.3 Hadoop 版本介绍 / 2


- 1.2 Hadoop 生态系统 / 3
- 1.3 Hadoop 常用项目介绍 / 4
- 1.4 Hadoop 在国内的应用 / 6
- 1.5 本章小结 / 7


- 第 2 章 Hadoop 安装 / 8

- 2.1 Hadoop 环境安装配置 / 8

- 2.1.1 安装 VMware / 8
- 2.1.2 安装 Ubuntu / 8
- 2.1.3 安装 VMware Tools / 15
- 2.1.4 安装 JDK / 15


- 2.2 Hadoop 安装模式 / 16

- 2.2.1 单机安装 / 17
- 2.2.2 伪分布式安装 / 18
- 2.2.3 分布式安装 / 20


- 2.3 如何使用 Hadoop / 27

- 2.3.1 Hadoop 的启动与停止 / 27
- 2.3.2 Hadoop 配置文件 / 28


- 2.4 本章小结 / 28


- 第 3 章 MapReduce 快速入门 / 30


- 3.2 MapReduce 代码的实现 / 32

- 3.2.1 编写 WordMapper 类 / 32
- 3.2.2 编写 WordReducer 类 / 33
- 3.2.3 编写 WordMain 驱动类 / 34


- 3.3 打包、部署和运行 / 35

- 3.3.1 打包成 JAR 文件 / 35
- 3.3.2 部署和运行 / 36
- 3.3.3 测试结果 / 38


- 3.4 本章小结 / 39


![image 9](<Hadoop应用开发技术详解》迷你书_images/imageFile9.png>)

第 4 章 Hadoop 分布式文件系统 详解 / 40

- 4.1 认识 HDFS / 40

- 4.1.1 HDFS 的特点 / 40
- 4.1.2 Hadoop 文件系统的接口 / 45
- 4.1.3 HDFS 的 Web 服务 / 46


- 4.2 HDFS 架构 / 46


- 4.2.1 机架 / 47
- 4.2.2 数据块 / 47
- 4.2.3 元数据节点 / 48
- 4.2.4 数据节点 / 50
- 4.2.5 辅助元数据节点 / 50
- 4.2.6 名字空间 / 52
- 4.2.7 数据复制 / 53
- 4.2.8 块备份原理 / 53
- 4.2.9 机架感知 / 54


- 3.1 WordCount 实例准备开发环境 / 30


###### VII

- 4.3 Hadoop 的 RPC 机制 / 55


- 4.3.1 RPC 的实现流程 / 56
- 4.3.2 RPC 的实体模型 / 56
- 4.3.3 文件的读取 / 57
- 4.3.4 文件的写入 / 58
- 4.3.5 文件的一致模型 / 59


- 4.8.5 删除节点 / 91

- 4.9 HDFS 权限管理 / 92

4.9.1 用户身份 / 92

- 4.9.2 权限管理的原理 / 93
- 4.9.3 设置权限的 Shell 命令 / 93
- 4.9.4 超级用户 / 93
- 4.9.5 HDFS 权限配置参数 / 94


- 4.10 本章小结 / 94




- 4.4 HDFS 的 HA 机制 / 59

- 4.4.1 HA 集群 / 59
- 4.4.2 HA 架构 / 60
- 4.4.3 为什么会有 HA 机制 / 61


- 4.5 HDFS 的 Federation 机制 / 62

- 4.5.1 单个 NameNode 的 HDFS 架构的局限性 / 62
- 4.5.2 为什么引入 Federation 机制 / 63
- 4.5.3 Federation 架构 / 64
- 4.5.4 多个名字空间的管理问题 / 65


- 4.6 Hadoop 文件系统的访问 / 66

- 4.6.1 安全模式 / 66
- 4.6.2 HDFS 的 Shell 访问 / 67
- 4.6.3 HDFS 处理文件的命令 / 67


- 4.7 Java API 接口 / 72

- 4.7.1 Hadoop URL 读取数据 / 73
- 4.7.2 FileSystem 类 / 73
- 4.7.3 FileStatus 类 / 75
- 4.7.4 FSDataInputStream 类 / 77
- 4.7.5 FSDataOutputStream 类 / 81
- 4.7.6 列出 HDFS 下所有的文件 / 83
- 4.7.7 文件的匹配 / 84
- 4.7.8 PathFilter 对象 / 84


- 4.8 维护 HDFS / 86


##### 第 5 章 Hadoop 文件 I/O 详解 / 95

- 5.1 Hadoop 文件的数据结构 / 95

- 5.1.1 SequenceFile 存储 / 95
- 5.1.2 MapFile 存储 / 99
- 5.1.3 SequenceFile 转换为 MapFile / 101


- 5.2 HDFS 数据完整性 / 103

- 5.2.1 校验和 / 103
- 5.2.2 数据块检测程序 / 104


- 5.3 文件序列化 / 106

- 5.3.1 进程间通信对序列化的 要求 / 106
- 5.3.2 Hadoop 文件的序列化 / 107
- 5.3.3 Writable 接口 / 107
- 5.3.4 WritableComparable 接口 / 108
- 5.3.5 自定义 Writable 接口 / 109
- 5.3.6 序列化框架 / 113
- 5.3.7 数据序列化系统 Avro / 114


- 5.4 Hadoop 的 Writable 类型 / 115

- 5.4.1 Writable 类的层次结构 / 115
- 5.4.2 Text 类型 / 116
- 5.4.3 NullWritable 类型 / 117
- 5.4.4 ObjectWritable 类型 / 117
- 5.4.5 GenericWritable 类型 / 117


- 5.5 文件压缩 / 117 5.5.1 Hadoop 支持的压缩格式 / 118


![image 10](<Hadoop应用开发技术详解》迷你书_images/imageFile10.png>)

- 4.8.1 追加数据 / 86
- 4.8.2 并行复制 / 88
- 4.8.3 升级与回滚 / 88
- 4.8.4 添加节点 / 90


###### VIII

- 6.5 Shuffle 阶段和 Sort 阶段 / 139

- 6.5.1 Map 端的 Shuffle / 140
- 6.5.2 Reduce 端的 Shuffle / 142
- 6.5.3 Shuffle 过程参数调优 / 143


- 6.6 任务的执行 / 144

- 6.6.1 推测执行 / 144
- 6.6.2 任务 JVM 重用 / 145
- 6.6.3 跳过坏的记录 / 145
- 6.6.4 任务执行的环境 / 146


- 6.7 作业调度器 / 146

- 6.7.1 先进先出调度器 / 146
- 6.7.2 容量调度器 / 146
- 6.7.3 公平调度器 / 149


- 6.8 自定义 Hadoop 调度器 / 153

- 6.8.1 Hadoop 调度器框架 / 153
- 6.8.2 编写 Hadoop 调度器 / 155


- 6.9 YARN 介绍 / 157

- 6.9.1 异步编程模型 / 157
- 6.9.2 YARN 支持的计算框架 / 158
- 6.9.3 YARN 架构 / 158
- 6.9.4 YARN 工作流程 / 159


- 6.10 本章小结 / 160


- 5.5.2 Hadoop 中的编码器和 解码器 / 118
- 5.5.3 本地库 / 121
- 5.5.4 可分割压缩 LZO / 122
- 5.5.5 压缩文件性能比较 / 122
- 5.5.6 Snappy 压缩 / 124
- 5.5.7 gzip、LZO 和 Snappy 比较 / 124


- 5.6 本章小结 / 125

第 6 章 MapReduce 工作原理 / 126

- 6.1 MapReduce 的函数式编程概念 / 126


- 6.1.1 列表处理 / 126
- 6.1.2 Mapping 数据列表 / 127
- 6.1.3 Reducing 数据列表 / 127
- 6.1.4 Mapper 和 Reducer 如何 工作 / 128
- 6.1.5 应用实例：词频统计 / 129


![image 11](<Hadoop应用开发技术详解》迷你书_images/imageFile11.png>)

- 6.2 MapReduce 框架结构 / 129

- 6.2.1 MapReduce 模型 / 130
- 6.2.2 MapReduce 框架组成 / 130


- 6.3 MapReduce 运行原理 / 132

- 6.3.1 作业的提交 / 132
- 6.3.2 作业初始化 / 134
- 6.3.3 任务的分配 / 136
- 6.3.4 任务的执行 / 136
- 6.3.5 进度和状态的更新 / 136
- 6.3.6 MapReduce 的进度组成 / 137
- 6.3.7 任务完成 / 137


- 6.4 MapReduce 容错 / 137


##### 第 7 章 Eclipse 插件的应用 / 161

- 7.1 编译 Hadoop 源码 / 161

- 7.1.1 下载 Hadoop 源码 / 161
- 7.1.2 准备编译环境 / 161
- 7.1.3 编译 common 组件 / 162


- 7.2 Eclipse 安装 MapReduce 插件 / 166

- 7.2.1 查找 MapReduce 插件 / 166
- 7.2.2 新建一个 Hadoop location / 167
- 7.2.3 Hadoop 插件操作 HDFS / 168
- 7.2.4 运行 MapReduce 的 驱动类 / 170


- 7.3 MapReduce 的 Debug 调试 / 171


- 6.4.1 任务失败 / 138
- 6.4.2 TaskTracker 失败 / 138
- 6.4.3 JobTracker 失败 / 138
- 6.4.4 子任务失败 / 138
- 6.4.5 任务失败反复次数的处理 方法 / 139


###### IX

- 7.3.1 进入 Debug 运行模式 / 171
- 7.3.2 Debug 调试具体操作 / 172


8.4.2 Map 作业输出结果的 压缩 / 212

- 8.5 MapReduce 优化 / 212

- 8.5.1 Combiner 类 / 212
- 8.5.2 Partitioner 类 / 213
- 8.5.3 分布式缓存 / 217


- 8.6 辅助类 / 218

- 8.6.1 读取 Hadoop 配置文件 / 218
- 8.6.2 设置 Hadoop 的配置 文件属性 / 219
- 8.6.3 GenericOptionsParser 选项 / 220


- 8.7 Streaming 接口 / 221

- 8.7.1 Streaming 工作原理 / 221
- 8.7.2 Streaming 编程接口参数 / 221
- 8.7.3 作业配置属性 / 222
- 8.7.4 应用实例：抓取网页的 标题 / 223


- 8.8 本章小结 / 225


- 7.4 单元测试框架 MRUnit / 174

- 7.4.1 认识 MRUnit 框架 / 174
- 7.4.2 准备测试案例 / 174
- 7.4.3 Mapper 单元测试 / 176
- 7.4.4 Reducer 单元测试 / 177
- 7.4.5 MapReduce 单元测试 / 178


- 7.5 本章小结 / 179


第 8 章 MapReduce 编程开发 / 180

- 8.1 WordCount 案例分析 / 180

- 8.1.1 MapReduce 工作流程 / 180
- 8.1.2 WordCount 的 Map 过程 / 181
- 8.1.3 WordCount 的 Reduce 过程 / 182
- 8.1.4 每个过程产生的结果 / 182
- 8.1.5 Mapper 抽象类 / 184
- 8.1.6 Reducer 抽象类 / 186
- 8.1.7 MapReduce 驱动 / 188
- 8.1.8 MapReduce 最小驱动 / 189


- 8.2 输入格式 / 193

- 8.2.1 InputFormat 接口 / 193
- 8.2.2 InputSplit 类 / 195
- 8.2.3 RecordReader 类 / 197
- 8.2.4 应用实例：随机生成 100 个 小数并求最大值 / 198


- 8.3 输出格式 / 205

- 8.3.1 OutputFormat 接口 / 205
- 8.3.2 RecordWriter 类 / 206
- 8.3.3 应用实例：把首字母相同的 单词放到一个文件里 / 206


- 8.4 压缩格式 / 211 8.4.1 如何在 MapReduce 中使用


![image 12](<Hadoop应用开发技术详解》迷你书_images/imageFile12.png>)

第 9 章 MapReduce 高级应用 / 226

- 9.1 计数器 / 226

- 9.1.1 默认计数器 / 226
- 9.1.2 自定义计数器 / 229
- 9.1.3 获取计数器 / 231


- 9.2 MapReduce 二次排序 / 232 9.2.1 二次排序原理 / 232 9.2.2 二次排序的算法流程 / 233 9.2.3 代码实现 / 235
- 9.3 MapReduce 中的 Join 算法 / 240

- 9.3.1 Reduce 端 Join / 240
- 9.3.2 Map 端 Join / 242
- 9.3.3 半连接 Semi Join / 244


- 9.4 MapReduce 从 MySQL 读写 数据 / 244


压缩 / 211

9.4.1 读数据 / 245

###### X

- 9.4.2 写数据 / 248

- 9.5 Hadoop 系统调优 / 248

9.5.1 小文件优化 / 249 9.5.2 Map 和 Reduce 个数设置 / 249

- 9.6 本章小结 / 250




10.6.3 外部 JOIN / 295 10.6.4 Map 端 JOIN / 296 10.6.5 JOIN 中处理 NULL 值的

语义区别 / 296

- 10.7 Hive 优化策略 / 297 10.7.1 列裁剪 / 297 10.7.2 Map Join 操作 / 297 10.7.3 Group By 操作 / 298 10.7.4 合并小文件 / 298
- 10.8 Hive 内置操作符与函数 / 298 10.8.1 字符串函数 / 299 10.8.2 集合统计函数 / 299 10.8.3 复合类型操作 / 301
- 10.9 Hive 用户自定义函数接口 / 302 10.9.1 用户自定义函数 UDF / 302 10.9.2 用户自定义聚合函数

UDAF / 304

- 10.10 Hive 的权限控制 / 306

- 10.10.1 角色的创建和删除 / 307
- 10.10.2 角色的授权和撤销 / 307
- 10.10.3 超级管理员权限 / 309


- 10.11 应用实例：使用 JDBC 开发 Hive 程序 / 311

- 10.11.1 准备测试数据 / 311
- 10.11.2 代码实现 / 311


- 10.12 本章小结 / 313


第 10 章 数据仓库工具 Hive / 251

- 10.1 认识 Hive / 251

- 10.1.1 Hive 工作原理 / 251
- 10.1.2 Hive 数据类型 / 252
- 10.1.3 Hive 的特点 / 253
- 10.1.4 Hive 下载与安装 / 255


- 10.2 Hive 架构 / 256 10.2.1 Hive 用户接口 / 257 10.2.2 Hive 元数据库 / 259 10.2.3 Hive 的数据存储 / 262 10.2.4 Hive 解释器 / 263
- 10.3 Hive 文件格式 / 264

- 10.3.1 TextFile 格式 / 265
- 10.3.2 SequenceFile 格式 / 265
- 10.3.3 RCFile 文件格式 / 265
- 10.3.4 自定义文件格式 / 269


- 10.4 Hive 操作 / 270

- 10.4.1 表操作 / 270
- 10.4.2 视图操作 / 278
- 10.4.3 索引操作 / 280
- 10.4.4 分区操作 / 283
- 10.4.5 桶操作 / 289


- 10.5 Hive 复合类型 / 290

- 10.5.1 Struct 类型 / 291
- 10.5.2 Array 类型 / 292
- 10.5.3 Map 类型 / 293


- 10.6 Hive 的 JOIN 详解 / 294


![image 13](<Hadoop应用开发技术详解》迷你书_images/imageFile13.png>)

第 11 章 开源数据库 HBase / 314

- 11.1 认识 HBase / 314 11.1.1 HBase 的特点 / 314 11.1.2 HBase 访问接口 / 314 11.1.3 HBase 存储结构 / 315 11.1.4 HBase 存储格式 / 317
- 11.2 HBase 设计 / 319 11.2.1 逻辑视图 / 320


- 10.6.1 JOIN 操作语法 / 294
- 10.6.2 JOIN 原理 / 294


###### XI

第 12 章 Mahout 算法 / 354

- 11.2.2 框架结构及流程 / 321
- 11.2.3 Table 和 Region 的关系 / 323
- 11.2.4 -ROOT- 表和 .META. 表 / 323


- 12.1 Mahout 的使用 / 354

- 12.1.1 安装 Mahout / 354
- 12.1.2 运行一个 Mahout 案例 / 354


- 12.2 Mahout 数据表示 / 356

- 12.2.1 偏好 Perference 类 / 356
- 12.2.2 数据模型 DataModel 类 / 357
- 12.2.3 Mahout 链接 MySQL 数据库 / 358


- 12.3 认识 Taste 框架 / 360
- 12.4 Mahout 推荐器 / 361 12.4.1 基于用户的推荐器 / 361 12.4.2 基于项目的推荐器 / 362 12.4.3 Slope One 推荐策略 / 363
- 12.5 推荐系统 / 365

- 12.5.1 个性化推荐 / 365
- 12.5.2 商品推荐系统案例 / 366


- 12.6 本章小结 / 370


- 11.3 关键算法和流程 / 324

- 11.3.1 Region 定位 / 324
- 11.3.2 读写过程 / 325
- 11.3.3 Region 分配 / 327
- 11.3.4 Region Server 上线和 下线 / 327
- 11.3.5 Master 上线和下线 / 327


- 11.4 HBase 安装 / 328

- 11.4.1 HBase 单机安装 / 328
- 11.4.2 HBase 分布式安装 / 330


- 11.5 HBase 的 Shell 操作 / 334

- 11.5.1 一般操作 / 334
- 11.5.2 DDL 操作 / 335
- 11.5.3 DML 操作 / 337
- 11.5.4 HBase Shell 脚本 / 339


- 11.6 HBase 客户端 / 340

- 11.6.1 Java API 交互 / 340
- 11.6.2 MapReduce 操作 HBase / 344
- 11.6.3 向 HBase 中写入数据 / 348
- 11.6.4 读取 HBase 中的数据 / 350
- 11.6.5 Avro、REST 和 Thrift 接口 / 352


- 11.7 本章小结 / 353


![image 14](<Hadoop应用开发技术详解》迷你书_images/imageFile14.png>)

- 附录 A Hive 内置操作符与函数 / 371
- 附录 B HBase 默认配置解释 / 392
- 附录 C Hadoop 三个配置文件的参数 含义说明 / 398


## 第1章 Hadoop 概述

Hadoop 是一个开发和运行处理大规模数据的软件平台，是 Apache 的一个用 Java 语言 实现开源软件框架，实现在大量计算机组成的集群中对海量数据进行分布式计算。

### 1.1 Hadoop 起源

Hadoop 框架中最核心设计就是：HDFS 和 MapReduce。HDFS 提供了海量数据的存储， MapReduce 提供了对数据的计算。

![image 15](<Hadoop应用开发技术详解》迷你书_images/imageFile15.png>)

#### 1.1.1 Google 与 Hadoop 模块

Google 的数据中心使用廉价的 Linux PC 机组成集群，在上面运行各种应用。即使是分 布式开发的新手也可以迅速使用 Google 的基础设施。Hadoop 核心组件与 Google 对应的组 件对应关系如表 1-1 所示。

表 1-1 Google 与 Hadoop 对应模块

<table>
  <tr>
    <th>Google</th>
    <th>功能描述</th>
    <th>对应 Hadoop 模块</th>
  </tr>
  <tr>
    <td>GFS</td>
    <td>分布式文件系统（Google File System），隐藏下层负载均衡、冗余复制等细 节，对上层程序提供一个统一的文件系统 API 接口。Google 根据自己的需求对 它进行了特别优化，包括超大文件的访问、读操作比例远超过写操作、PC 机 极易发生故障造成节点失效等。GFS 把文件分成 64MB 的块，分布在集群的机 器上，使用 Linux 的文件系统存放，同时每块文件至少有 3 份以上的冗余。中 心是一个 Master 节点，根据文件索引找寻文件块</td>
    <td>HDFS</td>
  </tr>
  <tr>
    <td>MapReduce</td>
    <td>Google 发现大多数分布式运算可以抽象为 MapReduce 操作。Map 是把输入 Input 分解成中间的 Key-Value 对，Reduce 把 Key-Value 合成最终输出 Output。 这两个函数由程序员提供给系统，下层设施把 Map 和 Reduce 操作分布在集群 上运行，并把结果存储在 GFS 上</td>
    <td>MapReduce</td>
  </tr>
  <tr>
    <td>BigTable</td>
    <td>一个大型的分布式数据库，这个数据库不是关系式的数据库。像它的名字一 样，就是一个巨大的表格，用来存储结构化的数据</td>
    <td>HBase</td>
  </tr>
</table>


#### 1.1.2 为什么会有 Hadoop

随着互联网快速的发展，产生的日志数量级的增加，大量的日志给公司带来了很大的

挑战。如日志存储问题、海量日志分析的效率问题、成本问题等。 下面我们来分析个问题。 一般网站把用户的访问行为记录以 Apache 日志的形式记录下来，这些日志中包含了用

户访问网站的所有信息，下面列举关键的信息，关键字段如下。

客服端 IP 用户标示 访问时间 访问 URL 关联的 URL 状态 流量 代理 client_ip user_ip access_time url refrence status page_size agent

因为需要统一对数据进行离线计算，所以常常把它们全部移到同一个地方，如 Oracle 数据库等，每天产生的日志量大概计算一下，如下所示。

❑ 网站请求数：1000 万条 / 天

❑ 每天日志大小：450 字节 / 行 × 1000 万条 = 4.2 GB

❑ 日志存储周期：2 年 一天产生 4.5 GB 日志，2 年需要 4.2 GB × 2 × 365 = 3.0 TB。 怎么来解决 3.0 TB 的数据备份和容错的问题？解决方案如下

- 1）为了方便系统命令查看日志，不压缩，总共需要 3.0 TB 空间，刚好有一些 2U 的服

务器，每台 1 TB 的磁盘空间。

- 2）为了避免系统盘坏掉影响服务器使用，对系统盘做 Raid1。
- 3）为了避免其他存放数据的盘坏掉导致数据无法恢复，对剩下的盘做 Raid5。 所有的数据都汇聚到这几台 LogBackUp 服务器上了。有了 LogBackUp 服务器，离线


![image 16](<Hadoop应用开发技术详解》迷你书_images/imageFile16.png>)

统计就可以全部在这些服务器上进行了。在这套架构上，用 wc、grep、sort、uniq、awk、 sed 等系统命令，完成了很多的统计需求，比如统计访问频率较高的 client_ip，某个新上线 的页面的 reference 主要是哪些网站。

当公司业务迅猛发展，网站流量爆发增长，日志量也就成指数级增长，日志对于一个 互联网公司来说是非常重要的，互联网公司可以通过分析日志来获取用户的行为，如推荐 系统、淘宝的用户买卖行为分析等。假如现在的日志量计算如下所示。

❑ 日志总行数：10 亿 / 天

❑ 每天日志大小：450 字节 / 行 × 10 亿 = 420 GB

❑ 日志种类：5 种 1 天产生 420 GB 日志，2 年需要 420 GB × 2 × 365 = 300 TB， 这么大的数据量怎么来

存储和分析？

#### 1.1.3 Hadoop 版本介绍

最新发布的 Apache Hadoop 版本如图 1-1 所示。一些 Hadoop 入门者会问：这个版本的 功能有哪些？基于哪个版本？后续的版本是什么？要解释这一点，我们应该从 Apache 项目 发布的一些基本知识开始分析。一般来说，Apache 项目的新功能在主干（trunk）代码上开 发。有时候，很大的特性也会有自己的开发分支（branch），它们期望后续会并入 trunk。新 功能通常是在 trunk 发布之前就有的，一般质量或稳定性没有太大保证。候选的分支会定期 从主干分支上分离出来发布。一旦一个候选分支发布，它通常停止获得新的功能。如果有 bug 修复，经过投票后，会针对这个特定的分支再发布一个新版本。社区的任何成员可以创 建一个版本分支，并可随意命名。

1.2 Hadoop生态系统 3

Trunk development (source of new features)

0.23

0.23.1 0.22

0.21

0.20.1 0.20.2

“0.20.append”

“0.20.security”

0.20.205

0.20.204 1.0

0.20.203

2009 2010 2011 2012

图 1-1 Hadoop 版本图

注意 目前使用比较多的版本是 v-0.20.2、v-1.0.3 或 v-1.0.4。Hadoop 2.0 版本还处于测试 阶段。目前 Hadoop 只能在 Linux 环境下运行。JDK 版本要在 1.5 以上。

![image 17](<Hadoop应用开发技术详解》迷你书_images/imageFile17.png>)

### 1.2 Hadoop 生态系统

当下 Hadoop 已经成长为一个庞大的体系，只要和海量数据相关的领域，都有 Hadoop 的身影。图 1-2 是一个 Hadoop 生态系统的图谱，详细列举了在 Hadoop 这个生态系统中出 现的各种数据工具。

<table>
  <tr>
    <th>![image 18](<Hadoop应用开发技术详解》迷你书_images/imageFile18.png>)</th>
  </tr>
</table>


图 1-2 Hadoop 生态系统

这一切，都起源自 Web 数据爆炸时代的来临。Hadoop 生态系统的功能以及对应的开源 工具说明如下。

- 1）海量数据怎么存，当然是用分布式文件系统 —— HDFS。
- 2）数据怎么用呢，分析、处理 MapReduce 框架，让你通过编写代码来实现对大数据


的分析工作。

- 3）非结构化数据（日志）收集处理 —— Fuse、WebDAV、Chukwa、Flume 和 Scribe。
- 4）数据导入 HDFS 中，RDBSM 也可以加入 HDFS 的狂欢了 —— HIHO、Sqoop。
- 5）MapReduce 太麻烦，用熟悉的方式操作 Hadoop 里的数据 —— Pig、Hive、Jaql。
- 6）让你的数据可见 —— Drilldown、Intellicus。
- 7）用高级语言管理你的任务流 —— Oozie、Cascading。
- 8）Hadoop 自己的监控管理工具 —— Hue、Karmasphere、Eclipse Plugin、Cacti、Ganglia。
- 9）数据序列化处理与任务调度 —— Avro、ZooKeeper。
- 10）更多构建在 Hadoop 上层的服务 —— Mahout、Elastic Map Reduce。
- 11）OLTP 存储系统 —— HBase。
- 12）基于 Hadoop 的实时分析—— Impala。


### 1.3 Hadoop 常用项目介绍

![image 19](<Hadoop应用开发技术详解》迷你书_images/imageFile19.png>)

随着 Hadoop 的使用越来越多，基于 Hadoop 开发的工具也越来越多，下面介绍常用的 一些工具。

1．Hive

Hive 是 Facebook 捐献给 Apache 的一个项目，Hive 是基于 Hadoop 的一个数据仓库工 具，可以将结构化的数据文件映射为一张数据库表，并提供完整的 SQL 查询功能，可以将 SQL 语句转换为 MapReduce 任务进行运行。 其优点是学习成本低，可以通过类 SQL 语句 快速实现简单的 MapReduce 统计，不必开发专门的 MapReduce 应用，十分适合数据仓库的 统计分析。

Hive 是建立在 Hadoop 上的数据仓库基础构架。它提供了一系列的工具，可以用来 进行数据提取转化加载（Extract Transform and Load ，ETL），这是一种可以存储、查询和 分析存储在 Hadoop 中的大规模数据的机制。Hive 定义了简单的类 SQL 查询语言，称为 HQL，它允许熟悉 SQL 的用户查询数据。同时，允许熟悉 MapReduce 的开发者开发自定 义的 Mapper 和 Reducer，以处理内建的 Mapper 和 Reducer 无法完成的复杂的分析工作。

2．Pig Pig 是 Yahoo 捐献给 Apache 的一个项目，目前还在 Apache 孵化器（incubator）阶段，

目前版本是 v0.5.0。

Pig 是一个基于 Hadoop 的大规模数据分析平台，它提供的 SQL-like 语言叫 Pig Latin， 该语言的编译器会把类 SQL 的数据分析请求转换为一系列经过优化处理的 MapReduce 运 算。Pig 为复杂的海量数据并行计算提供了一个简易的操作和编程接口。

3．Mahout Mahout 是 ASF（Apache Software Foundation）旗下的一个开源项目，提供一些可扩展

的机器学习领域经典算法的实现，旨在帮助开发人员更加方便快捷地创建智能应用程序。 Apache Mahout 项目已经发展第四个年头，目前已经有了三个公共发行版本。Mahout 包含

1.3 Hadoop常用项目介绍 5

许多实现，包括集群、分类、推荐过滤、频繁子项挖掘。此外，通过使用 Apache Hadoop 库，Mahout 可以有效地扩展到云中。

Mahout 算法库提供了以下一些功能。

❑ 支持 MapReduce 的集群实现包括 K-means、模糊 K-means、Canopy、Dirichlet 和 Mean-Shift；

❑ Distributed Naive Bayes 和 Complementary Naive Bayes 分类实现；

❑ 针对进化编程的分布式适用性功能；

❑ Matrix 和矢量库等。 4．Flume Flume 是 Cloudera 提供的一个高可用的、高可靠的、分布式的海量日志采集、聚合和

传输的系统。Flume 支持在日志系统中定制各类数据发送方，用于收集数据；同时，Flume 提供对数据进行简单处理，并写到各种数据接受方（可定制）的能力。

![image 20](<Hadoop应用开发技术详解》迷你书_images/imageFile20.png>)

Flume 提供了从 console（控制台）、RPC（Thrift-RPC）、text（文件）、tail（UNIX tail）、 syslog（syslog 日志系统，支持 TCP 和 UDP 等两种模式）、exec（命令执行）等数据源上收 集数据的能力。

Flume 采用多 Master 的方式。为了保证配置数据的一致性，Flume 引入 ZooKeeper，

用于保存配置数据。ZooKeeper 本身可保证配置数据的一致性和高可用，另外，在配置数据 发生变化时，ZooKeeper 可以通知 Flume Master 节点。Flume Master 间使用 gossip 协议同 步数据。

Apache 孵化的 Flume 又称 FlumeNG。 5．Sqoop Sqoop 是一个用来将 Hadoop 和关系型数据库中的数据相互转移的工具，可以将一个关

系型数据库（如 MySQL、Oracle、Postgres 等）中的数据导入 Hadoop 的 HDFS 中，也可以 将 HDFS 的数据导入关系型数据库中。

6．Oozie Oozie 是一种 Java Web 应用程序，它运行在 Java Servlet 容器（即 Tomcat）中，并使用

数据库来存储以下内容： ❑ 工作流定义；

❑ 当前运行的工作流实例，包括实例的状态和变量。 Oozie 工作流是放置在控制依赖 DAG（Direct Acyclic Graph，有向无环图）中的一组动

作（如 Hadoop 的 Map/Reduce 作业、Pig 作业等），其中指定了动作执行的顺序。我们会使 用 hPDL（一种 XML 流程定义语言）来描述这个图。

hPDL 是一种很简洁的语言，只会使用少数流程控制和动作节点。控制节点会定义执行 的流程，并包含工作流的起点和终点（start、end 和 fail 节点）以及控制工作流执行路径的 机制（decision、fork 和 join 节点）。动作节点是一些机制，通过它们工作流会触发执行计算 或者处理任务。Oozie 为以下类型的动作提供支持：Hadoop MapReduce、Hadoop 文件系统、

Pig、Java 和 Oozie 的子工作流（SSH 动作已经从 Oozie Schema 0.2 之后的版本中移除了）。

所有由动作节点触发的计算和处理任务都不在 Oozie 之中——它们是由 Hadoop 的 Map/Reduce 框架执行的。这种方法让 Oozie 可以支持现存的 Hadoop 用于负载平衡、灾难 恢复的机制。这些任务主要是异步执行的（只有文件系统动作例外，它是同步处理的）。这 意味着对于大多数工作流动作触发的计算或处理任务的类型来说，在工作流操作转换到工 作流的下一个节点之前都需要等待，直到计算或处理任务结束了之后才能够继续。Oozie 可 以通过两种不同的方式来检测计算或处理任务是否完成，也就是回调和轮询。当 Oozie 启 动了计算或处理任务的时候，它会为任务提供唯一的回调 URL，然后任务会在完成的时候 发送通知给特定的 URL。在任务无法触发回调 URL 的情况下（可能是因为任何原因，比方 说网络闪断），或者当任务的类型无法在完成时触发回调 URL 的时候，Oozie 有一种机制， 可以对计算或处理任务进行轮询，从而保证能够完成任务。

Oozie 工作流可以参数化（在工作流定义中使用像 ${inputDir} 之类的变量）。在提交工 作流操作的时候，我们必须提供参数值。如果经过合适地参数化（比方说，使用不同的输出 目录），那么多个同样的工作流操作可以并发。

![image 21](<Hadoop应用开发技术详解》迷你书_images/imageFile21.png>)

一些工作流是根据需要触发的，但是大多数情况下，我们有必要基于一定的时间段和

（或）数据可用性和（或）外部事件来运行它们。Oozie 协调系统（Coordinator System）让用 户可以基于这些参数来定义工作流执行计划。Oozie 协调程序让我们可以以谓词的方式对工 作流执行触发器进行建模，那可以指向数据、事件和（或）外部事件。工作流作业会在谓词 得到满足的时候启动。

有时我们还需要连接定时运行但时间间隔不同的工作流操作。多个随后运行的工作流 的输出会成为下一个工作流的输入。把这些工作流连接在一起，会让系统把它作为数据应 用的管道来引用。Oozie 协调程序支持创建这样的数据应用管道。

7．ZooKeeper ZooKeeper 是 Hadoop 的正式子项目，它是一个针对大型分布式系统的可靠协调系统。

提供的功能包括：配置维护、名字服务、分布式同步、组服务等。ZooKeeper 的目标就是封 装好复杂易出错的关键服务，将简单易用的接口和性能高效、功能稳定的系统提供给用户。

8．Impala Impala 采 用 与 Hive 相 同 的 元 数 据、SQL 语 法、ODBC 驱 动 程 序 和 用 户 接 口（Hue

Beeswax），这样在使用 CDH 产品时，批处理和实时查询的平台是统一的。目前支持的文 件格式是文本文件和 Sequence Files（可以压缩为 Snappy、GZIP 和 BZIP，前者性能最好）。 其他格式如 Avro、RCFile、LZO 文本和 Doug Cutting 的 Trevni 将在正式版中支持，官方测 试速度是 Hive 的 3 ～ 90 倍。

### 1.4 Hadoop 在国内的应用

图 1-3 由 Hadoop 技术论坛提供，数据的绝对值参考意义不大，主要是看各城市间的相

1.5 本章小结 7

对数据。北京、深圳和杭州位列前三甲，分析主要原因是：北京有淘宝和百度，深圳有腾 讯，杭州有网易等。互联网公司是 Hadoop 在国内的主要使用力量。淘宝是在国内最先使用 Hadoop 的公司之一，而百度赞助了 HyperTable 的开发，另外北京研究 Hadoop 的高校多，

所以北京是 Hadoop 方面研究和应用需求最高的城市。

![image 22](<Hadoop应用开发技术详解》迷你书_images/imageFile22.png>)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


Beijing Shenzhen Hangzhou Shanghai Guangzhou Nanjing Chendu Changsha Shenyang Wuhan

656 332 241 144 144

30.51% 15.44% 11.21%

6.70% 6.70%

16.51%

6.70%

11.21%

6.70 3.12% 2.98% 2.74% 2.23% 1.86%

67 64 59 48 40

![image 23](<Hadoop应用开发技术详解》迷你书_images/imageFile23.png>)

15.44%

30.51%

图 1-3 Hadoop 使用统计图

目前，Hadoop 在国内深得互联网企业的青睐，很多互联网公司都在使用 Hadoop 来实 现公司的核心业务，如搜索、推荐等。

❑ 奇虎 360 ：Hadoop 存储软件管家中的软件，使用 CDN 技术将用户请求引到最近的 Hadoop 集群并进行下载。

❑ 京东、百度：存储、分析日志、数据挖掘和机器学习（主要是推荐系统）。

❑ 广告类公司：存储日志，通过协调过滤算法为客户推荐广告。

❑ 华为：云计算平台。

❑ 学校：学生上网与社会行为分析。

❑ 淘宝、阿里巴巴：国内使用 Hadoop 最深入的公司，整个淘宝和阿里巴巴都是数据 驱动的。

### 1.5 本章小结

互联网时代什么最重要——数据！大数据时代什么工具最流行——Hadoop ！本章主 要从大体上介绍 Hadoop，让读者对 Hadoop 有个大体的印象。接下来我们开始深入介绍 Hadoop 的相关知识，以及 Hadoop 的安装、使用和开发。

## 第2章 Hadoop 安装

Hadoop 目前只能在 Linux 环境下运行，所以我们要准备 VMware 8.0.2 和 Ubuntu 10.04 还有 JDK 1.6，这些直接去官网下载就可以了。本书中，Hadoop 分布式安装使用了虚拟机 的形式，采用 VMware 和 Ubuntu 虚拟出三台设备，然后在这三台上面搭建 Hadoop 集群。

### 2.1 Hadoop 环境安装配置

本书内容基于 Hadoop-1.0.3 版本来介绍，首先，从 Apache 官网（http://hadoop.apache. org/releases.html#Ne）下载 Hadoop-1.0.3 版本。

![image 24](<Hadoop应用开发技术详解》迷你书_images/imageFile24.png>)

#### 2.1.1 安装 VMware

从官网下载 VMware-workstation-full-8.0.2-591240.exe 文件，一直点击“下一步”就可 以安装，非常简单。安装成功以后如图 2-1 所示。

![image 25](<Hadoop应用开发技术详解》迷你书_images/imageFile25.png>)

图 2-1 VMware 运行图

#### 2.1.2 安装 Ubuntu Ubuntu（友帮拓）是一个以桌面应用为主的 Linux 操作系统，其名称来自非洲的

“ubuntu”一词，意思是“人性”、“我的存在是因为大家的存在”，类似“仁爱”思想。 1．获取 Ubuntu 发行版本

Ubuntu 基于 Debian 发行版和 GNOME 桌面环境，与 Debian 的不同在于它每 6 个月会 发布一个新版本。Ubuntu 的目标在于为一般用户提供一个最新的、同时又相当稳定的、主 要由自由软件构建而成的操作系统。Ubuntu 具有庞大的社区力量，用户可以方便地从社区 获得帮助。2013 年 1 月 3 日，Ubuntu 正式发布面向智能手机的移动操作系统。

本书中为什么使用 Ubuntu 系统来安装 Hadoop 集群？原因在于 Ubuntu 系统的完全免 费，而且使用非常多。安装 Ubuntu 系统的步骤如下。

- 1）从镜像站点上下载 ISO 的镜像文件。

❑ 中文官网：http://www.ubuntu.com.cn

❑ 英文官网：http://www.ubuntu.com

- 2）有时我们在网上下载的压缩包会有损坏，可以使用 md5 来验证下载的 ISO 镜像文


![image 26](<Hadoop应用开发技术详解》迷你书_images/imageFile26.png>)

件的完整性，使用如下代码。

#md5sum ubuntu-10.04-desktop-i386.iso

2．创建虚拟机 启动 VMware 以后，会进入如图 2-2 所示的对话框，用户可打开一个已有的虚拟机或

创建一个新的虚拟机等操作。

<table>
  <tr>
    <th>![image 27](<Hadoop应用开发技术详解》迷你书_images/imageFile27.png>)</th>
  </tr>
</table>


图 2-2 VMware 创建虚拟机对话框

双击 Create a New Virtual Machine 以后，会看到图 2-3 所示的对话框。 还有一种方式来创建虚拟机，步骤是 VMware 的工具栏 File New → Virtual Machine，

选中以后会直接出现图 2-3 的对话框。Machine Wizard 有两种安装模式。

❑ Typical 安装：默认的安装，如果对 VMware 不熟悉的同学可以使用这种安装模式， 简单而且不容易出错。

❑ Custom 安装：用户自动安装，这种安装可以根据自己的需求来定制化，但要求比较高。

选择默认的 Typical 安装后单击 Next 按钮，将进入加载 ISO 文件对话框，如图 2-4 所示。

<table>
  <tr>
    <th>![image 28](<Hadoop应用开发技术详解》迷你书_images/imageFile28.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 29](<Hadoop应用开发技术详解》迷你书_images/imageFile29.png>)</th>
  </tr>
</table>


![image 30](<Hadoop应用开发技术详解》迷你书_images/imageFile30.png>)

图 2-3 虚拟机安装方式 图 2-4 加载 ISO 文件对话框

在加载 ISO 对话框里有三种来源。

❑ Installer disk：这需要有光盘才能安装，选择光盘的位置即可。

❑ Installer disc image file(iso)：这就是 ISO 镜像文件的安装，找到 ISO 文件存放的位置。

❑ I will install the operating system later ：选中这个以后就代表先创建一个空的磁盘虚 拟机，等启动虚拟的时候再安装 Linux 系统。

选择“ I will install the operating system later”后单击 Next 按钮，进入图 2-5 所示的 Linux 系统选择对话框。选择“Linux”和“Ubuntu”后点击 Nent 按钮，如图 2-6 所示。

<table>
  <tr>
    <th>![image 31](<Hadoop应用开发技术详解》迷你书_images/imageFile31.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 32](<Hadoop应用开发技术详解》迷你书_images/imageFile32.png>)</th>
  </tr>
</table>


图 2-5 Linux 版本选择 图 2-6 Ubuntu 名称设置

由 于 是 在 VMware 里 面 安 装 的 Linux 系 统， 所 以 Guest operating system 应 该 选 择 Linux，Version 表示要安装哪个 Linux 版本，有 Redhat、Centos、Ubuntu 等，选择 Ubuntu。

选择完成后单击 Next 按钮，如图 2-6 所示，进入虚拟机名称设置对话框。Ubuntu 名称 设置对话框有两个属性。

❑ Virtual machine name：创建虚拟机的名称，这里设置为 Ubuntu 1。

❑ Location ：选择虚拟机安装的路径。Hadoop 文件夹所在的磁盘空间一定要够用，大 概需要 20 GB。

设置好以后选择 Next 按钮，进入虚拟机的磁盘设置对话框，如图 2-7 所示。虚拟机磁 盘设置有三个选项。

❑ Maximum disk size ：最大磁盘空间大小，可以根据实际的磁盘空间来设置，一般设 置为 20 GB。

❑ Store virtual disk as a single file：设置虚拟机的磁盘只有一个文件，相当于 Windows 系统只有 C 盘。

![image 33](<Hadoop应用开发技术详解》迷你书_images/imageFile33.png>)

❑ Split virtual disk in to multiple file：设置虚拟机的磁盘有多个文件，相当于 Windows 系统有 C、D、E 盘等。 设置完以后单击 Next 按钮，将进入如图 2-8 所示的确认对话框。

<table>
  <tr>
    <th>![image 34](<Hadoop应用开发技术详解》迷你书_images/imageFile34.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 35](<Hadoop应用开发技术详解》迷你书_images/imageFile35.png>)</th>
  </tr>
</table>


图 2-7 虚拟机磁盘设置 图 2-8 虚拟机设置确认

这个对话框显示了我们设置虚拟机的一些信息，如虚拟机名称、安装路径、虚拟机版 本、磁盘空间等信息，如果不对还可以选择 Back 重新设置。

确认以后，选择单击 Finish 按钮，到此我们创建一个名为 Ubuntu_1 的虚拟机就成功

了，这相当于一个没有操作系统的机器。 3．安装 Ubuntu 操作系统 接下来启动刚才创建的 Ubuntu_1 虚拟机，如图 2-9 所示，安装 Ubuntu 操作系统。

<table>
  <tr>
    <th>![image 36](<Hadoop应用开发技术详解》迷你书_images/imageFile36.png>)</th>
  </tr>
</table>


![image 37](<Hadoop应用开发技术详解》迷你书_images/imageFile37.png>)

图 2-9 启动虚拟机 Ubuntu_1

启动虚拟机之前，我们可以做一些设置。

❑ Memory：设置虚拟机的内存大小。

❑ Processor：设置虚拟机的处理器个数。

❑ Hard Disk：磁盘大小。

❑ Network Adapt：设置虚拟机的网络连接方式。 双击 CD/DVD(IDE)，将进入加载 ISO 镜像文件对话框，如图 2-10 所示。 加载 ISO 文件对话框有两个属性设置。

- 1）Device status 属性。

❑ Connected ：每次启动虚拟机都加载 ISO 文件。

❑ Connect at power on：只加载一次。

- 2）Connection 属性。


<table>
  <tr>
    <th>![image 38](<Hadoop应用开发技术详解》迷你书_images/imageFile38.png>)</th>
  </tr>
</table>


❑ Use physical drive：使用物理的光盘；

❑ Use ISO image file ：使用 ISO 文件安 装，选择 ISO 文件所在的路径。

选择单击 OK 按钮，回到虚拟机启动 的界面，如图 2-11 所示。

单击 Power On this Virtual machine 进 入安装操作系统界面，如图 2-12 所示。大 概 1 分钟，会进入 Ubuntu 操作系统语言配 置对话框。

图 2-10 加载 ISO 镜像文件

<table>
  <tr>
    <th>![image 39](<Hadoop应用开发技术详解》迷你书_images/imageFile39.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 40](<Hadoop应用开发技术详解》迷你书_images/imageFile40.png>)</th>
  </tr>
</table>


图 2-11 虚拟机启动界面 图 2-12 操作系统的安装

如图 2-13 所示，选择 English，当然也有中文的，建议使用英文，因为这样对学习有帮 助，这个大家可以根据自己的喜好来选择。

![image 41](<Hadoop应用开发技术详解》迷你书_images/imageFile41.png>)

选择好以后，双击 Install Ubuntu 10.04 LTS ，进入时区设置对话框，如图 2-14 所示。

<table>
  <tr>
    <th>![image 42](<Hadoop应用开发技术详解》迷你书_images/imageFile42.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 43](<Hadoop应用开发技术详解》迷你书_images/imageFile43.png>)</th>
  </tr>
</table>


图 2-13 Ubuntu 系统语言设置 图 2-14 Ubuntu 时区设置

时区设置有两个属性。

❑ Region：地理区域，这里选择“Asia”。

❑ Time Zone：时区，这里选择“China Shanghai”。 选择好以后，单击 Forward 按钮，进入输入法设置对话框，如图 2-15 所示。 如果选择安装的操作系统为英文环境，在这里我们就选择 USA，美国的输入法，没有

中文输入法；如果选择的是中文操作系统，这里就有中文的输入法可以选择了。

选择好以后单击 Forward 按钮，进入磁盘设置对话框，如图 2-16 所示。这里用默认设 置就可以了，单击 Forward 按钮，进入 Ubuntu 系统用户设置对话框。

如图 2-17 所示，创建一个登录 Ubuntu 系统的用户。注意，这里不是 root 用户，因 为 root 用户默认没有密码。用户名、密码和机器名设置完成以后单击 Forward 按钮，进入

Ubuntu 系统信息设置确认对话框，如图 2-18 所示。

<table>
  <tr>
    <th>![image 44](<Hadoop应用开发技术详解》迷你书_images/imageFile44.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 45](<Hadoop应用开发技术详解》迷你书_images/imageFile45.png>)</th>
  </tr>
</table>


图 2-15 Ubuntu 输入法设置 图 2-16 磁盘设置对话框

![image 46](<Hadoop应用开发技术详解》迷你书_images/imageFile46.png>)

<table>
  <tr>
    <th>![image 47](<Hadoop应用开发技术详解》迷你书_images/imageFile47.png>)</th>
  </tr>
</table>


![image 48](<Hadoop应用开发技术详解》迷你书_images/imageFile48.png>)

图 2-17 用户设置对话框 图 2-18 Ubuntu 系统信息确认

确认没有错误以后，单击 Install 按钮，进入 Ubuntu 系统安装对话框如图 2-19 所示。 这步根据机器的配置不同需要的时间也不一样，快的大概需要 15 分钟。安装完成以后就会 进入 Ubuntu 系统的登录对话框，如图 2-20 所示。

<table>
  <tr>
    <th>![image 49](<Hadoop应用开发技术详解》迷你书_images/imageFile49.png>)</th>
  </tr>
</table>


![image 50](<Hadoop应用开发技术详解》迷你书_images/imageFile50.png>)

图 2-19 Ubuntu 系统安装界面 图 2-20 Ubuntu 系统登录

这里登录用户就是前面设置的用户名，输入刚才设置的密码就可以登录了。到这里 Ubuntu 系统的安装就成功了。按照同样的方式再创建两个虚拟机，分别为 Ubuntu_2 和 Ubuntu_3，安装的方法和 Ubuntu_1 一样。

#### 2.1.3 安装 VMware Tools

VMware Tools 是 VMware 虚拟机中自带的一种增强工具，相当于 VirtualBox 中的增强 功能（Sun VirtualBox Guest Additions），是 VMware 提供的增强虚拟显卡和硬盘性能，以及 同步虚拟机与主机时钟的驱动程序。

只有在 VMware 虚拟机中安装了 VMware Tools，才能实现主机与虚拟机之间的文件 共享，同时支持自由拖曳的功能，鼠标也可以在虚拟机与主机之前自由移动（不再需要按 Ctrl + Alt），且虚拟机屏幕也可以实现全屏化。

VMware Tools 的安装步骤如下。

![image 51](<Hadoop应用开发技术详解》迷你书_images/imageFile51.png>)

- 1）启动并进入 Linux 系统。
- 2）选择虚拟机“ vm”菜单中的“虚拟机 / 安装 VMware-Tools(install VMware-tolls)”，

此时就会有把 VMware-tools 文件映像到 CD-ROM 中。

- 3）把“VMwareTools-6.0.2-59824.tar.gz”文件复制到自己需要的位置。
- 4）解压，先要进入“VMwareTools-6.0.2-59824.tar.gz”存放目录，执行以下命令。 # tar -zxvf VmwareTools-6.0.2-59824.tar.gz
- 5）进入 /home/tsm/Tools/vmware-tools-distrib 目录。 cd /home/tsm/Tools/vmware-tools-distrib
- 6）输入“ ./vmware-install.pl”进行安装，在安装过程中根据提示进行选择，其实一直


按回车键即可完成安装。

#### 2.1.4 安装 JDK

JDK（Java Development Kit）是 Sun Microsystems 针对 Java 开发的产品。自从 Java 推 出以来，JDK 已经成为使用最广泛的 Java SDK。JDK 是整个 Java 的核心，包括 Java 运行 环境、Java 工具和 Java 基础类库，掌握 JDK 是学好 Java 的第一步。而专门运行在 x86 平 台的 JRocket 在服务器端运行效率比 Sun JDK 好很多。从 Sun JDK 5.0 开始，提供了泛型等 非常实用的功能，其版本也不断更新，运行效率得到了非常大的提高。

JDK 只要是 1.5 版本以上就可以，本书使用的是 JDK 1.6。安装过程如下。

- 1）下载 JDK 1.6，这里存放的目录是 /opt/tools/jdk-6u25-linux-i586.bin。
- 2）进入 /opt/tools 目录，给文件赋值权限，命令如下。


cd /opt/tools chmod u+x jdk-6u25-linux-i586.bin

- 3）执行下面的命令，执行完命令后，会在 /opt/tools 目录下生成 jdk1.6.0_25 文件夹。

./jdk-6u25-linux-i586.bin

- 4）配置 JDK 的环境命令如下。

sudo gedit /etc/proﬁ le export JAVA_HOME=/opt/tools/jdk1.6.0_25 export JRE_HOME=$JAVA_HOME/jre export CLASSPATH=$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH

- 5）再执行下面命令使配置文件生效。 source /etc/proﬁ le 或者以下命令：

. /etc/proﬁ le

- 6）使用以下命令查看 JDK 安装是否成功。 jayliu@Ubuntu1:~$ javac 如果显示信息如图 2-21 所示，恭喜你，JDK 安装成功了。


![image 52](<Hadoop应用开发技术详解》迷你书_images/imageFile52.png>)

![image 53](<Hadoop应用开发技术详解》迷你书_images/imageFile53.png>)

图 2-21 使用 javac 命令查看结果

### 2.2 Hadoop 安装模式

本节会用到刚才创建的 Ubuntu_1、Ubuntu_2、Ubuntu_3 这三台虚拟机。从 Apache 官 网下载 Hadoop-1.0.3 版本。

Hadoop 的安装模式分为三种，分别是：单机模式、伪分布模式及全分布模式。默认安 装是单机模式，下面分别演示三种模式的安装方法。准备就绪，下面就开始 Hadoop 的安装 旅途吧！

#### 2.2.1 单机安装

单 机 模 式 所 需 的 系 统 资 源 是 最 少 的， 这 种 安 装 模 式 下，Hadoop 的 core-site.xml、 mapred-site.xml、hdsf-site.xml 配置文件均为空。因此 Hadoop 保守地采取这种模式为默认 安装模式。

当配置文件为空时，Hadoop 完全运行在本地，不与其他节点交互，也不使用 Hadoop 文件系统，不加载任何守护进程，该模式主要用于开发调试 MapReduce 应用程序的逻辑， 不与任何守护进程交互进而避免复杂性。安装过程如下。

- 1）解压 Hadoop 的 TAR 文件。 tar -zxvf hadoop-1.0.3.tar.gz
- 2）配置 Hadoop 的环境变量。

sudo vi /etc/proﬁ le export HADOOP_HOME=/opt/hadoop-1.0.3 export PATH=$HADOOP_HOME/bin:$PATH

- 3）进入解压的文件夹下的 bin 目录执行 start-all.sh 命令。 jayliu@Ubuntu1:/opt/hadoop-1.0.3/bin$ ./start-all.sh
- 4）使用 jps 命令查看进程是否启动成功，如图 2-22 所示。

![image 54](<Hadoop应用开发技术详解》迷你书_images/imageFile54.png>)

- 图 2-22 使用 jps 命令查看

因为是单机模式，NameNode 和 JobTracker 等都没有启动，怎么知道安装是否成功呢？ 5）查看 HDFS 系统，如图 2-23 所示。

![image 55](<Hadoop应用开发技术详解》迷你书_images/imageFile55.png>)

- 图 2-23 HDFS 单机版目录结构




![image 56](<Hadoop应用开发技术详解》迷你书_images/imageFile56.png>)

大家发现什么？怎么像 Linux 的文件系统目录呢？没错，显示的就是 Linux 文件系统 目录。如果出现图 2-23 所示的结果，说明你的 Hadoop 单机版安装成功了！

到目前为止我们没有对 Hadoop 的配置文件做任何修改，全是默认配置，即配置文件全 是空的，如下所示。

<?xml version="1.0"?> <?xml-stylesheet type="text/xsl" href="conﬁ guration.xsl"?>

<!-- Put site-speciﬁ c property overrides in this ﬁ le. --> <conﬁ guration>

</conﬁ guration>

#### 2.2.2 伪分布式安装

伪分布模式即“单节点集群”模式，所有的守护进程都运行在同一台机器上。这种模 式下增加了代码调试功能，可以查看内存、HDFS 文件系统的输入 / 输出，以及与其他守护 进程交互。core-site.xml、mapred-site.xml、hdsf-site.xml 配置文件如下。

- 1）修改地址解析文件 /etc/hosts，加入以下代码。 192.168.195.140 Ubuntu1
- 2）修改 hadoop 目录下 conf 下的 core-sitexml 文件，如下所示。


![image 57](<Hadoop应用开发技术详解》迷你书_images/imageFile57.png>)

<?xml version="1.0"?> <?xml-stylesheet type="text/xsl" href="conﬁ guration.xsl"?>

<!-- Put site-speciﬁ c property overrides in this ﬁ le. -->

<conﬁ guration> <property>

<name>hadoop.tmp.dir</name> <value>/hadoop</value> <description>A base for other temporary directories.</description>

</property>

<property>

<!-- 用于 dfs 命令模块中指定默认的文件系统协议 --> <name>fs.default.name</name> <value>hdfs:// Ubuntu1:9000</value> <description>The name of the default ﬁ le system. A URI whose scheme and authority determine the FileSystem implementation. The uri's scheme determines the conﬁ g property (fs.SCHEME.impl) naming

the FileSystem implementation class. The uri's authority is used to

determine the host, port, etc. for a ﬁ lesystem.</description> </property> <property>

<name>dfs.name.dir</name> <value>/hadoop/name</value>

<description>Determines where on the local ﬁ lesystem the DFS name node should store the name table. If this is a comma-delimited list of directories then the name table is replicated in all of the directories, for redundancy. </description>

</property>

</conﬁ guration>

3）修改 hadoop 目录下的 conf/ hdfs-site.xml 文件，如下所示。

<?xml version="1.0"?> <?xml-stylesheet type="text/xsl" href="conﬁ guration.xsl"?>

<!-- Put site-speciﬁ c property overrides in this ﬁ le. --> <conﬁ guration> <property>

<name>dfs.data.dir</name> <value>/hadoop/data</value>

<description>Determines where on the local ﬁ lesystem an DFS data node should store its blocks. If this is a comma-delimited list of directories, then data will be stored in all named directories, typically on different devices. Directories that do not exist are ignored.</description>

</property> <property>

<name>dfs.replication</name> // 默认 Block 副本数

![image 58](<Hadoop应用开发技术详解》迷你书_images/imageFile58.png>)

<value>1</value>

<description>Default block replication. The actual number of replications can be specified when the file is created. The default is used if replication is not speciﬁ ed in create time.</description>

</property> </conﬁ guration>

4）修改 hadoop 目录下的 conf/ mapred-site.xml 文件，如下所示。

<?xml version="1.0"?> <?xml-stylesheet type="text/xsl" href="conﬁ guration.xsl"?>

<!-- Put site-speciﬁ c property overrides in this ﬁ le. --> <conﬁ guration> <property> <name>mapred.job.tracker</name>

<value> Ubuntu1:9001</value> <description>The host and port that the MapReduce job tracker runs at. If

"local", then jobs are run in-process as a single map and reduce task.</description> </property> </conﬁ guration>

在 core-site.xml 和 mapred-site.xml 文件中分别指定 NameNode 和 JobTracker 的主机名 和端口号，而在 hdfs-site.xml 中指定 HDFS 的默认副本数，由于伪分布式运行在单机上， 因此只有一个副本。同时在 masters 和 slavers 文件中指定 SecondaryNameNode 和从节点的 主机名为 Ubuntu1，即在文件中写入“Ubuntu1”。

配置好以后，启动步骤如下。

- 1）第一次启动 Hadoop 要格式化，使用如下命令格式化 NameNode。 hadoop namenode –format
- 2）执行 start-all.sh 命令启动 Hadoop 集群。
- 3）使用 jps 命令查看结果，如图 2-24 所示。


![image 59](<Hadoop应用开发技术详解》迷你书_images/imageFile59.png>)

- 图 2-24 使用 jps 查看结果
- 4）使用 hadoop fs–ls/ 命令查看 HDFS 系统，如图 2-25 所示。Hadoop 的伪分布式安装


成功。

![image 60](<Hadoop应用开发技术详解》迷你书_images/imageFile60.png>)

图 2-25 HDFS 伪分布式目录结构

以上操作需要注意两点。

❑ 启动 Hadoop 之前必须执行 hadoop namenode –format 操作，否则报错。

![image 61](<Hadoop应用开发技术详解》迷你书_images/imageFile61.png>)

❑ 设置的 hadoop.tmp.dir 路径必须存在，不存在会报错。 展

扩 展 阅 读

Linux 系统命令拓展

- 1）查看 IP 地址。 jayliu@Ubuntu1:/opt$ ifconﬁ g eth1 Link encap:Ethernet HWaddr 00:0c:29:d7:dc:52

inet addr:192.168.195.140 Bcast:192.168.195.255 Mask:255.255.255.0 inet6 addr: fe80::20c:29ff:fed7:dc52/64 Scope:Link UP BROADCAST RUNNING MULTICAST MTU:1500 Metric:1 RX packets:1207000 errors:0 dropped:0 overruns:0 frame:0 TX packets:2216586 errors:0 dropped:0 overruns:0 carrier:0 collisions:0 txqueuelen:1000 RX bytes:88270718 (88.2 MB) TX bytes:3325334709 (3.3 GB) Interrupt:19 Base address:0x2024

- 2）查看主机名。 jayliu@Ubuntu1:/opt$ hostname Ubuntu1


#### 2.2.3 分布式安装

我们在 Ubuntu1 虚拟机上安装了 Hadoop 的一个节点，按照上一节的步骤把 Hadoop 分

别安装在 Ubuntu2 和 Ubuntu3 上。安装成功以后再接着学习看下面的内容。 1．集群的构建和拓扑图 使用三台虚拟机来搭建 Hadoop 分布式环境，三台虚拟机的拓扑图如图 2-26 所示。

Ubuntul 192.168.195.140

Ubuntu2 192.168.195.141

Ubuntu3 192.168.195.142

![image 62](<Hadoop应用开发技术详解》迷你书_images/imageFile62.png>)

![image 63](<Hadoop应用开发技术详解》迷你书_images/imageFile63.png>)

![image 64](<Hadoop应用开发技术详解》迷你书_images/imageFile64.png>)

![image 65](<Hadoop应用开发技术详解》迷你书_images/imageFile65.png>)

![image 66](<Hadoop应用开发技术详解》迷你书_images/imageFile66.png>)

![image 67](<Hadoop应用开发技术详解》迷你书_images/imageFile67.png>)

![image 68](<Hadoop应用开发技术详解》迷你书_images/imageFile68.png>)

![image 69](<Hadoop应用开发技术详解》迷你书_images/imageFile69.png>)

![image 70](<Hadoop应用开发技术详解》迷你书_images/imageFile70.png>)

![image 71](<Hadoop应用开发技术详解》迷你书_images/imageFile71.png>)

![image 72](<Hadoop应用开发技术详解》迷你书_images/imageFile72.png>)

![image 73](<Hadoop应用开发技术详解》迷你书_images/imageFile73.png>)

![image 74](<Hadoop应用开发技术详解》迷你书_images/imageFile74.png>)

![image 75](<Hadoop应用开发技术详解》迷你书_images/imageFile75.png>)

![image 76](<Hadoop应用开发技术详解》迷你书_images/imageFile76.png>)

![image 77](<Hadoop应用开发技术详解》迷你书_images/imageFile77.png>)

![image 78](<Hadoop应用开发技术详解》迷你书_images/imageFile78.png>)

![image 79](<Hadoop应用开发技术详解》迷你书_images/imageFile79.png>)

![image 80](<Hadoop应用开发技术详解》迷你书_images/imageFile80.png>)

![image 81](<Hadoop应用开发技术详解》迷你书_images/imageFile81.png>)

![image 82](<Hadoop应用开发技术详解》迷你书_images/imageFile82.png>)

![image 83](<Hadoop应用开发技术详解》迷你书_images/imageFile83.png>)

![image 84](<Hadoop应用开发技术详解》迷你书_images/imageFile84.png>)

![image 85](<Hadoop应用开发技术详解》迷你书_images/imageFile85.png>)

![image 86](<Hadoop应用开发技术详解》迷你书_images/imageFile86.png>)

![image 87](<Hadoop应用开发技术详解》迷你书_images/imageFile87.png>)

![image 88](<Hadoop应用开发技术详解》迷你书_images/imageFile88.png>)

![image 89](<Hadoop应用开发技术详解》迷你书_images/imageFile89.png>)

![image 90](<Hadoop应用开发技术详解》迷你书_images/imageFile90.png>)

![image 91](<Hadoop应用开发技术详解》迷你书_images/imageFile91.png>)

![image 92](<Hadoop应用开发技术详解》迷你书_images/imageFile92.png>)

![image 93](<Hadoop应用开发技术详解》迷你书_images/imageFile93.png>)

![image 94](<Hadoop应用开发技术详解》迷你书_images/imageFile94.png>)

![image 95](<Hadoop应用开发技术详解》迷你书_images/imageFile95.png>)

Windows eclipse 3.3.2 192.168.1.5

![image 96](<Hadoop应用开发技术详解》迷你书_images/imageFile96.png>)

![image 97](<Hadoop应用开发技术详解》迷你书_images/imageFile97.png>)

![image 98](<Hadoop应用开发技术详解》迷你书_images/imageFile98.png>)

![image 99](<Hadoop应用开发技术详解》迷你书_images/imageFile99.png>)

![image 100](<Hadoop应用开发技术详解》迷你书_images/imageFile100.png>)

![image 101](<Hadoop应用开发技术详解》迷你书_images/imageFile101.png>)

![image 102](<Hadoop应用开发技术详解》迷你书_images/imageFile102.png>)

![image 103](<Hadoop应用开发技术详解》迷你书_images/imageFile103.png>)

![image 104](<Hadoop应用开发技术详解》迷你书_images/imageFile104.png>)

![image 105](<Hadoop应用开发技术详解》迷你书_images/imageFile105.png>)

![image 106](<Hadoop应用开发技术详解》迷你书_images/imageFile106.png>)

![image 107](<Hadoop应用开发技术详解》迷你书_images/imageFile107.png>)

![image 108](<Hadoop应用开发技术详解》迷你书_images/imageFile108.png>)

![image 109](<Hadoop应用开发技术详解》迷你书_images/imageFile109.png>)

![image 110](<Hadoop应用开发技术详解》迷你书_images/imageFile110.png>)

![image 111](<Hadoop应用开发技术详解》迷你书_images/imageFile111.png>)

图 2-26 Hadoop 分布式拓扑图

Hadoop 集群中每个节点的角色如表 2-1 所示。

表 2-1 Hadoop 集群节点角色

<table>
  <tr>
    <th>主机名</th>
    <th>Hadoop 角色</th>
    <th>IP</th>
    <th>Hadoop jps 命令结果</th>
    <th>Hadoop 用户</th>
    <th>Hadoop 安装目录</th>
  </tr>
  <tr>
    <td>Ubuntu1</td>
    <td>master slaves</td>
    <td>![image 112](<Hadoop应用开发技术详解》迷你书_images/imageFile112.png>)<br><br>192.168.195.140</td>
    <td>NameNode DataNode JobTracker TaskTracker SecondaryNameNode</td>
    <td rowspan="3">创建相同的用户组名：<br><br>jayliu 安 装 hadoop-1.0.3 时 使 用 jayliu 用 户， 并 且 jayliu 的文件夹归属 也是“jayliu：jayliu”</td>
    <td rowspan="3">/opt/hadoop</td>
  </tr>
  <tr>
    <td>Ubuntu2</td>
    <td>slaves</td>
    <td>192.168.195.141</td>
    <td>DataNode TaskTracker</td>
  </tr>
  <tr>
    <td>Ubuntu3</td>
    <td>slaves</td>
    <td>192.168.195.142</td>
    <td>DataNode TaskTracker</td>
  </tr>
  <tr>
    <td>Windows</td>
    <td>开发测试环境</td>
    <td colspan="4">安装 JDK 与 Eclipse 本身不需要安装 Hadoop，但是需要 Hadoop 安装包下面的 JAR 包</td>
  </tr>
</table>


注意 Ubuntu1 既是 NameNode 又是 DataNode，同时也是 JobTracker。 2．SSH 配置无密码验证配置

- 1）登录 master 节点（即 Ubuntu1 节点）上面，切换到 jayliu 用户，在 jayliu 的 home

目录下面创建“.ssh”目录。

$ cd $ mkdir .ssh

- 2）在 Master 节点（即主机 Ubuntu1）上生成密钥对。 $ ssh-keygen –t rsa
- 3）然后一直按 Enter 键，按默认的选项生成密钥对保存在 .ssh/id_rsa.pub 文件中。把


生成的 id_rsa.pub 复制一份，命名为 authorized_keys，然后分别复制到 Ubuntu2 和 Ubuntu3 两个节点上。执行如下命令。

$ cd .ssh

$ cp id_rsa.pub authorized_keys

- $ scp authorized_keys Ubuntu2:/home/jayliu/.ssh
- $ scp authorized_keys Ubuntu3:/home/jayliu/.ssh 4）从 Ubuntu1 向 Ubuntu2 和 Ubuntu3 发起 SSH 连接，第一次登录时需要输入密码，


以后就不需要了。

- $ ssh Ubuntu2
- $ ssh Ubuntu3


我们只需要配置从 master 向 slaves 发起 SSH 连接，不需要密码就可以，但这样只能在 master（即在主机 Ubuntu1）启动或关闭 Hadoop 服务。

注意 SSH 安装过程中，很多刚入门的学者会遇到麻烦，虽然按照老师的方法一步一步做 下去，但是最后还是连不上。注意：SSH 的安装对“.ssh”文件夹和里面文件的权限是非常 严格的，大了或小了都连不上。

![image 113](<Hadoop应用开发技术详解》迷你书_images/imageFile113.png>)

- 1） “.ssh”是一个隐藏的文件夹，用“ll –a”命令可以看到隐藏的文件夹，“.ssh”文件 夹权限必须是 700。
- 2）“.ssh”里面的文件权限最好是 600。


3．修改 hosts 文件 需要把 Ubuntu1、Ubuntu2、Ubuntu3 的主机名和 IP 添加到三台虚拟机的 hosts 文件里面，

Ubuntu1 的 hosts 文件信息如图 2-27 所示。

![image 114](<Hadoop应用开发技术详解》迷你书_images/imageFile114.png>)

图 2-27 Ubuntu1 的 hosts 文件配置

接下来把 Ubuntu1 的 hosts 文件里的配置分别复制到 Ubuntu2 和 Ubuntu3 上。 4．Hadoop 配置文件修改 这 里 只 需 要 修 改 conf 下 面 的 master 和 slaves 文 件 就 可 以，Ubuntu1 下 的 master 和

slaves 配置信息如图 2-28 和图 2-29 所示。

![image 115](<Hadoop应用开发技术详解》迷你书_images/imageFile115.png>)

![image 116](<Hadoop应用开发技术详解》迷你书_images/imageFile116.png>)

图 2-28 master 配置信息 图 2-29 slaves 配置信息

Ubuntu2 和 Ubuntu3 下的 master 和 slaves 文件按照 Ubuntu1 的配置就可以了！ Hadoop

的分布式安装就完成了。 5．Hadoop 启动和验证 使用 jayliu 用户启动。首先说明，Hadoop 命令和参数都是大小写敏感的，该用大写时

用大写，用小写时用小写，否则会执行错误。在 master 节点上进行如下操作。 1）格式化分布式文件系统。

$ hadoop namenode -format

2）在 master 上启动 Hadoop 守护进行。

$ start-all.sh

启动日志如图 2-30 所示。

![image 117](<Hadoop应用开发技术详解》迷你书_images/imageFile117.png>)

![image 118](<Hadoop应用开发技术详解》迷你书_images/imageFile118.png>)

- 图 2-30 Hadoop 启动日志

停止 Hadoop 守护进程命令如下。

$ stop-all.sh

- 在 Ubuntu1 上查看运行的进程结果如图 2-31 所示。

![image 119](<Hadoop应用开发技术详解》迷你书_images/imageFile119.png>)

图 2-31 Ubuntu1 进程

- 在 Ubuntu2 上查看运行的进程结果如图 2-32 所示。
- 在 Ubuntu3 上查看运行的进程结果如图 2-33 所示。




![image 120](<Hadoop应用开发技术详解》迷你书_images/imageFile120.png>)

![image 121](<Hadoop应用开发技术详解》迷你书_images/imageFile121.png>)

图 2-32 Ubuntu2 进程 图 2-33 Ubuntu3 进程

6．Hadoop WebUI 访问 访问 http://192.168.195.140:50070 如图 2-34 所示，可以查看 Hadoop 集群的节点数、

NameNode 及整个分布式文件系统的状态等。

<table>
  <tr>
    <th>![image 122](<Hadoop应用开发技术详解》迷你书_images/imageFile122.png>)</th>
  </tr>
</table>


![image 123](<Hadoop应用开发技术详解》迷你书_images/imageFile123.png>)

图 2-34 NameNode WebUI 访问

NameNode 的 WebUI 属性解释如下。 ❑ Started：Hadoop 系统启动的时间。 ❑ Version：Hadoop 的版本号。

❑ Compiled：Hadoop 源码编译的时间。

❑ Upgrades ：是否有升级进程没有结束。在 Hadoop 系统升级确保成功以后需要执行 Upgrades 命令，否则下次没法升级。

❑ Configured Capacity：HDFS 的容量大小。

❑ DFS Used：HDFS 使用的空间。

❑ Non DFS Used：HDFS 预留的空间。

❑ DFS Remaining：HDFS 系统剩余空间。

❑ DFS Used%：HDFS 空间使用的百分比。

❑ DFS Remaining%：HDFS 空间剩余的百分比。

❑ Live Nodes：Hadoop 集群活着的节点数。

❑ Dead Nodes：Hadoop 集群宕机的节点数。 访问 http:// 192.168.195.140:50030 如图 2-35 所示，可以查看 JobTracker 的运行状态，

如 Job 运行的进度、Map 个数、Reduce 个数等。 JobTracker WebUI 的属性解释如下。

❑ Maps：Job 运行使用的 Map 个数。

❑ Reduces：Job 运行使用的 Reduce 个数。

❑ Map Task Capacity：Hadoop 集群中 Map 的总个数。

❑ Reduce Task Capacity：Hadoop 集群中 Reduce 的总个数。

❑ Queue Name：队列的名称，默认为 default。

<table>
  <tr>
    <th>![image 124](<Hadoop应用开发技术详解》迷你书_images/imageFile124.png>)</th>
  </tr>
</table>


图 2-35 JobTracker WebUI 访问

7．Hadoop 集群测试 我们来运行 hadoop-example.jar 里面自带的 WordCount 程序，作用是统计单词的个数。

![image 125](<Hadoop应用开发技术详解》迷你书_images/imageFile125.png>)

- 1）在 Ubuntu1 的 Hadoop 的 home 目录下创建一个 test.txt 文件，内容如下。

Hello world Hello world Hello world Hello world

- 2）在 HDFS 系统里创建一个 input 文件夹，使用命令如下。 $ hadoop fs –mkdir /user/hadoop/input
- 3）把创建好的 test.txt 文件上传到 HDFS 系统的 input 文件夹下，命令如下。 $ hadoop fs –put /opt/hadoop-0.20.2/test.txt /user/hadoop/input/
- 4）查看文件是否上传成功，结果如图 2-36 所示。

![image 126](<Hadoop应用开发技术详解》迷你书_images/imageFile126.png>)

图 2-36 上传文件查询

- 5）运行 hadoop-1.0.3-examples.jar 下的单词统计案例，执行命令如下。


$ cd /opt/hadoop-1.0.3 $ hadoop jar hadoop-examples-1.0.3.jar wordcount /user/hadoop/input/test.txt

/user/hadoop/output

- 13/04/20 00:47:07 INFO input.FileInputFormat: Total input paths to process : 1


- 13/04/20 00:47:07 INFO util.NativeCodeLoader: Loaded the native-hadoop library


- 13/04/20 00:47:07 WARN snappy.LoadSnappy: Snappy native library not loaded
- 13/04/20 00:47:08 INFO mapred.JobClient: Running job: job_201304200039_0001
- 13/04/20 00:47:09 INFO mapred.JobClient: map 0% reduce 0%


- 13/04/20 00:47:45 INFO mapred.JobClient: map 100% reduce 0%
- 13/04/20 00:48:08 INFO mapred.JobClient: map 100% reduce 100%


- 13/04/20 00:48:13 INFO mapred.JobClient: Job complete: job_201304200039_0001


- 13/04/20 00:48:13 INFO mapred.JobClient: Counters: 29 13/04/20 00:48:13 INFO mapred.JobClient: Job Counters 13/04/20 00:48:13 INFO mapred.JobClient: Launched reduce tasks=1 13/04/20 00:48:13 INFO mapred.JobClient: SLOTS_MILLIS_MAPS=28822 13/04/20 00:48:13 INFO mapred.JobClient: Total time spent by all reduces


waiting after reserving slots (ms)=0 13/04/20 00:48:13 INFO mapred.JobClient: Total time spent by all maps

waiting after reserving slots (ms)=0 13/04/20 00:48:13 INFO mapred.JobClient: Launched map tasks=1 13/04/20 00:48:13 INFO mapred.JobClient: Data-local map tasks=1 13/04/20 00:48:13 INFO mapred.JobClient: SLOTS_MILLIS_REDUCES=18236 13/04/20 00:48:13 INFO mapred.JobClient: File Output Format Counters 13/04/20 00:48:13 INFO mapred.JobClient: Bytes Written=16 13/04/20 00:48:13 INFO mapred.JobClient: FileSystemCounters 13/04/20 00:48:13 INFO mapred.JobClient: FILE_BYTES_READ=30 13/04/20 00:48:13 INFO mapred.JobClient: HDFS_BYTES_READ=159 13/04/20 00:48:13 INFO mapred.JobClient: FILE_BYTES_WRITTEN=43053 13/04/20 00:48:13 INFO mapred.JobClient: HDFS_BYTES_WRITTEN=16 13/04/20 00:48:13 INFO mapred.JobClient: File Input Format Counters 13/04/20 00:48:13 INFO mapred.JobClient: Bytes Read=48 13/04/20 00:48:13 INFO mapred.JobClient: Map-Reduce Framework 13/04/20 00:48:13 INFO mapred.JobClient: Map output materialized bytes=30 13/04/20 00:48:13 INFO mapred.JobClient: Map input records=4 13/04/20 00:48:13 INFO mapred.JobClient: Reduce shufﬂ e bytes=30 13/04/20 00:48:13 INFO mapred.JobClient: Spilled Records=4 13/04/20 00:48:13 INFO mapred.JobClient: Map output bytes=80 13/04/20 00:48:13 INFO mapred.JobClient: CPU time spent (ms)=2870 13/04/20 00:48:13 INFO mapred.JobClient: Total committed heap usage

![image 127](<Hadoop应用开发技术详解》迷你书_images/imageFile127.png>)

(bytes)=210698240 13/04/20 00:48:13 INFO mapred.JobClient: Combine input records=8 13/04/20 00:48:13 INFO mapred.JobClient: SPLIT_RAW_BYTES=111 13/04/20 00:48:13 INFO mapred.JobClient: Reduce input records=2 13/04/20 00:48:13 INFO mapred.JobClient: Reduce input groups=2 13/04/20 00:48:13 INFO mapred.JobClient: Combine output records=2 13/04/20 00:48:13 INFO mapred.JobClient: Physical memory (bytes)

snapshot= 180101120 13/04/20 00:48:13 INFO mapred.JobClient: Reduce output records=2 13/04/20 00:48:13 INFO mapred.JobClient: Virtual memory (bytes)

snapshot= 749068288 13/04/20 00:48:13 INFO mapred.JobClient: Map output records=8

6）查看运行结果如图 3-37 所示。

![image 128](<Hadoop应用开发技术详解》迷你书_images/imageFile128.png>)

图 2-37 WordCount 结果

OK ！到这里 Hadoop 三个节点的集群就安装结束并且测试成功了。

2.3 如何使用Hadoop 27

### 2.3 如何使用 Hadoop

启动之前，要格式化 NameNode，先进入 /opt/hadoop 目录（如果在 /etc/profile 文件中 配置了 Hadoop 的环境变量，就不用进入 $HADOOP_HOME/bin 了），执行下面的命令。

[jayliu@Ubuntu1]$bin/hadoop namenode -format

不出意外，应该会提示格式化成功。如果不成功，就去 hadoop/logs/ 目录下查看日志文件。

#### 2.3.1 Hadoop 的启动与停止

下面就正式启动 Hadoop 啦，bin/ 目录下有很多启动脚本，可以根据自己的需要来启动 Hadoop 的守护进程。启动和停止的脚本和说明如表 2-2 所示。

表 2-2 Hadoop 的启动和停止说明

<table>
  <tr>
    <th>启动脚本</th>
    <th>脚本说明</th>
  </tr>
  <tr>
    <td>start-all.sh</td>
    <td>![image 129](<Hadoop应用开发技术详解》迷你书_images/imageFile129.png>)<br><br>启 动 所 有 的 Hadoop 守 护 进 程。 包 括 NameNode、Secondary NameNode、DataNode、JobTracker、TaskTrack</td>
  </tr>
  <tr>
    <td>stop-all.sh</td>
    <td>停 止 所 有 的 Hadoop 守 护 进 程。 包 括 NameNode、Secondary NameNode、DataNode、JobTracker、TaskTrack</td>
  </tr>
  <tr>
    <td>start-dfs.sh</td>
    <td>启动 Hadoop HDFS 守护进程 NameNode、SecondaryNameNode 和 DataNode</td>
  </tr>
  <tr>
    <td>stop-dfs.sh</td>
    <td>停止 Hadoop HDFS 守护进程 NameNode、SecondaryNameNode 和 DataNode</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh start namenode</td>
    <td>单独启动 NameNode 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh stop namenode</td>
    <td>单独停止 NameNode 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh start datanode</td>
    <td>单独启动 DataNode 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh stop datanode</td>
    <td>单独停止 DataNode 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh start secondarynamenode</td>
    <td>单独启动 SecondaryNameNode 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh stop secondarynamenode</td>
    <td>单独停止 SecondaryNameNode 守护进程</td>
  </tr>
  <tr>
    <td>start-mapred.sh</td>
    <td>启动 Hadoop MapReduce 守护进程 JobTracker 和 TaskTracker</td>
  </tr>
  <tr>
    <td>stop-mapred.sh</td>
    <td>停止 Hadoop MapReduce 守护进程 JobTracker 和 TaskTracker</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh start jobtracker</td>
    <td>单独启动 JobTracker 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh stop jobtracker</td>
    <td>单独停止 JobTracker 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh start tasktracker</td>
    <td>单独启动 TaskTracker 守护进程</td>
  </tr>
  <tr>
    <td>hadoop-daemons.sh stop tasktracker</td>
    <td>单独启动 TaskTracker 守护进程</td>
  </tr>
</table>


如果 Hadoop 集群是第一次启动，可以用 start-all.sh。比较常用的启动方式是一个一个 守护进程来启动，启动的步骤如下。

- 1．启动 Hadoop 的 HDFS 模块里的守护进程 HDFS 里面的守护进程启动也有顺序，即：


- 1）启动 NameNode 守护进程；


- 2）启动 DataNode 守护进程；
- 3）启动 SecondaryNameNode 守护进程。


- 2．启动 MapReduce 模块里面的守护进程 MapReduce 的守护进程启动也是有顺序的，即：


- 1）启动 JobTracker 守护进程；
- 2）启动 TaskTracker 守护进程。 关闭的步骤正好相反，在这里就不描述了，读者可以自己试一下。


注意 正常情况下，我们是不使用 start-all.sh 和 stop-all.sh 来启动和停止 Hadoop 集群的。 这样出错了不好找原因。建议读者一个一个守护进程来启动，哪个启动失败就去看相应的 log 日志，这样就缩小了找错的范围。

- 2.3.2 Hadoop 配置文件 Hadoop 安装的配置文件有很多，重要的几个文件如表 2-3 所示。


表 2-3 Hadoop 配置文件

<table>
  <tr>
    <th>文件名称</th>
    <th>格 式</th>
    <th>功能描述</th>
  </tr>
  <tr>
    <td>hadoop-env.sh</td>
    <td>Bash 脚本</td>
    <td>记录脚本要用的环境变量，以运行 Hadoop</td>
  </tr>
  <tr>
    <td>core-site.xml</td>
    <td>![image 130](<Hadoop应用开发技术详解》迷你书_images/imageFile130.png>)<br><br>Hadoop 配置 XML</td>
    <td>Hadoop 核心的配置文件，例如 MapReduce、HDFS 和 常用的 I/O 设置等</td>
  </tr>
  <tr>
    <td>ddfs-site.xml</td>
    <td>Hadoop 配置 XML</td>
    <td>Hadoop 守护进程的配置项，包括 NameNode、 Secondary NameNode 和 DataNode 等</td>
  </tr>
  <tr>
    <td>mapred-site.xml</td>
    <td>Hadoop 配置 XML</td>
    <td>MapReduce 守护进程的配置项，包含 JobTracker 和 Task Tracker</td>
  </tr>
  <tr>
    <td>masters</td>
    <td>纯文本</td>
    <td>运行 SecondaryNameNode 的机器列表（每行一个）</td>
  </tr>
  <tr>
    <td>slaves</td>
    <td>纯文本</td>
    <td>运行 DataNode 和 TaskTracker 的机器列表（每行一个）</td>
  </tr>
  <tr>
    <td>hadoop-metrics.properties</td>
    <td>Java 属性</td>
    <td>控制 metrics 在 Hadoop 上如何发布，一般对 Hadoop 监控会用到这个配置件</td>
  </tr>
  <tr>
    <td>log4j.properties</td>
    <td>Java 属性</td>
    <td>系统的日志文件、NameNode 审计日志、TaskTracker 子进程任务的日志属性</td>
  </tr>
</table>


以上文件全部放在 Hadoop 分发包的 conf 目录下面，这些配置文件也可以重新放在文 件系统的其他地方（Hadoop 安装的外面，以便于升级），但是守护进程启动需要用 -config 选项来指定。

### 2.4 本章小结

本章主要介绍 Hadoop 的安装，包括 Hadoop 单机模式、伪分布式模式以及分布式模 式，让读者了解 Hadoop 的安装过程。

2.4 本章小结 29

Master 和 Slave 上的几个 conf 配置文件不需要全部同步，如果确定都是通过 Master 启 动和关闭，Slave 机器上的配置不需要去维护。但如果希望在任意一台机器都可以启动和关 闭 Hadoop，那么就需要全部保持一致了。

Master 和 Slave 机器上的 hosts 文件中必须把集群中的机器都配置上去。这方面吃过不 少苦头，原来以为如果配成 IP 就不需要去配置 hosts，结果发现在执行 Reduce 的时候总是 卡，在复制的时候就无法继续下去，不断重试。另外，如果集群中有两台机器的机器名重 复，也会出现问题。

![image 131](<Hadoop应用开发技术详解》迷你书_images/imageFile131.png>)

## 第3章 MapReduce 快速入门

为了让大家快速地认识 Hadoop 的 MapReduce，本章给出一个实际的例子。Hello World 是学一门技术或语言最简单的案例，也是最直观的案例，在这里笔者觉得 Hello World 太简 单了，本章用一个比较有难度，并且能体现 MapReduce 特性的案例 WordCount，即统计单 词出现的次数，使大家快速入门。

### 3.1 WordCount 实例准备开发环境

![image 132](<Hadoop应用开发技术详解》迷你书_images/imageFile132.png>)

现在有两个文件 file1.txt 和 file2.txt。 文件 file1.txt 的内容为：

Hello, i love coding are you ok? Hello, i love hadoop are you ok?

文件 file2.txt 的内容为：

Hello i love coding are you ok ? Hello i love hadoop are you ok ?

现在就要实现在 file1.txt 和 file2.txt 两个文件中，按照空格做分隔符，来统计每个单词 出现的次数。

虽然 Hadoop 支持很多种语言开发 MapReduce 程序，但是 Java 语言支持得比较完美， 提供了原生态的 Java API 接口。C++、Python、Shell 等语言 Hadoop 也是支持的，但是要 用到 Hadoop 封装好的一个 Streaming 接口。下面使用 Eclipse 工具开发 MapReduce 程序。

#### 3.1.1 使用 Eclipse 创建一个 Java 工程

Eclipse 的安装很简单，从官网下载一个 Eclipse 版本（这里使用 Eclipse JEE 版本），下 载 Eclipse 以后，直接解压到某个文件夹下就可以了。必须要安装 JDK 1.6 以上的版本。打 开 Eclipse IDE 工具，鼠标单击 Eclipse 工具栏左上角文件（File）→新建（New）→ Java 项 目（Java Project）出现创建工程对话框。Eclipse 创建工程对话框设置属性如下。

- 1）Project name：创建工程的名字。
- 2）Use default location：默认是选中的，存放路径可以随便改，但是建议不要改。因为

它默认存在 Eclipse 的工作路径下面。

- 3）JRE 包括以下两个选项。


3.1 WordCount 实例准备开发环境 31

❑ Use an execution environment JRE： 选择 JRE 的路径。

❑ Use default JRE ： 表示使用 Eclipse 工 具自带的 JRE，如果没有安装 JDK， 可以使用自带的 JRE，如果自己安装 了 JDK，最好使用自己安装的 JDK。

<table>
  <tr>
    <th>![image 133](<Hadoop应用开发技术详解》迷你书_images/imageFile133.png>)</th>
  </tr>
</table>


- 4）Project layout ： 这 里 选 择“ Create


separate folder for sources and class files”，会 在工程下面自动创建一个存放源代码的 src 文 件夹。

填 好 以 后 单 击 Finish 按 钮，WordCount 工程就创建好了，如图 3-1 所示。

![image 134](<Hadoop应用开发技术详解》迷你书_images/imageFile134.png>)

#### 3.1.2 导入 Hadoop 的 JAR 文件

导入开发 MapReduce 程序所需要的 Hadoop 依赖的 JAR 文件，如图 3-2 所示，右击项目名 称，新建一个 Folder 文件夹，文件夹的名称为 lib，把 Hadoop 的核心包和 lib 下面依赖包全部 复制到新建的 lib 目录下面。

图 3-1 Eclipse 创建工程对话框

<table>
  <tr>
    <th>![image 135](<Hadoop应用开发技术详解》迷你书_images/imageFile135.png>)</th>
  </tr>
</table>


图 3-2 导入 Hadoop 依赖的 JAR 文件

全选 lib 文件夹下的 JAR 文件，鼠标右键单击，选择 Build Path → Add to Build Path， 如图 3-3 所示。

OK，到这里一个 Hadoop 工程就准备好了。接下来就可以在这个工程下面开发 MapReduce 程序了。

<table>
  <tr>
    <th>![image 136](<Hadoop应用开发技术详解》迷你书_images/imageFile136.png>)</th>
  </tr>
</table>


![image 137](<Hadoop应用开发技术详解》迷你书_images/imageFile137.png>)

图 3-3 将 Hadoop 的 JAR 文件导入 ClassPath 下面

### 3.2 MapReduce 代码的实现

使 用 Java 语 言 编 写 MapReduce 非 常 方 便， 因 为 Hadoop 的 API 提 供 了 Mapper 和 Reducer 抽象类，对于开发人员来说，只要继承这两个抽象类，然后实现抽象类里面的方法 就可以了。

#### 3.2.1 编写 WordMapper 类

在工程下创建一个 WordMapper 类，该类要继承 Mapper< Object, Text, Text, IntWritable> 抽象类，并且实现如下方法。

public void map(Object key, Text value, Context context ) throws IOException, InterruptedException

这个方法是 Mapper 抽象类的核心方法，它有三个参数。

❑ Object key：每行文件的偏移量。

❑ Text value：每行文件的内容。

❑ Context context：Map 端的上下文，与 OutputCollector 和 Reporter 的功能类似。 注意 OutputCollector 和 Reporter 是 Hadoop-0.19 以前版本里面的 API，在 Hadoop-0.20.2 以后就换成 Context，Context 的功能包含了 OutputCollector 和 Reporter 的功能，此外还添 加了一些别的功能。

编写 WordMapper 类如代码清单 3-1 所示。

3.2 MapReduce代码的实现 33

代码清单 3-1 代码文件 wordcount\WordMapper.java

package wordcount; import java.io.IOException; import java.util.StringTokenizer; import org.apache.hadoop.io.IntWritable; import org.apache.hadoop.io.Text; import org.apache.hadoop.mapreduce.Mapper; public class WordMapper extends Mapper<Object, Text, Text, IntWritable>{

private ﬁ nal static IntWritable one = new IntWritable(1); private Text word = new Text(); public void map(Object key, Text value, Context context )

throws IOException, InterruptedException { StringTokenizer itr = new StringTokenizer(value.toString()); while (itr.hasMoreTokens()) {

word.set(itr.nextToken()); context.write(word, one);

![image 138](<Hadoop应用开发技术详解》迷你书_images/imageFile138.png>)

} }

}

实现 Mapper 抽象类里的 map 方法，map 方法主要就是把字符串解析成 Key-Value 的形 式（例如 Key=hello，Value=1），发给 Reduce 端来统计。

#### 3.2.2 编写 WordReducer 类

在工程下创建一个 WordReducer 类，该类要继承 Reducer<Text, IntWritable, Text, IntWritable> 抽象类，并且实现如下方法。

public void reduce (Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException

这个方法是 Reducer 抽象类的核心方法，它有三个参数。

❑ Text key：Map 端输出的 Key 值。

❑ Iterable<IntWritable> values：Map 端输出的 Value 集合（相同 Key 的集合）。

❑ Context context：Reduce 端的上下文，与 OutputCollector 和 Reporter 的功能类似。 编写 WordReducer 类如代码清单 3-2 所示。

代码清单 3-2 代码文件 wordcount\WordReducer.java

package wordcount; import java.io.IOException; import org.apache.hadoop.io.IntWritable; import org.apache.hadoop.io.Text; import org.apache.hadoop.mapreduce.Reducer; public class WordReducer extends Reducer<Text,IntWritable,Text,IntWritable> {

private IntWritable result = new IntWritable(); public void reduce(Text key, Iterable<IntWritable> values, Context context )

throws IOException, InterruptedException { int sum = 0;

for (IntWritable val : values) { sum += val.get();

} result.set(sum); context.write(key, result); }

}

reduce 方法的主要功能就是获取 map 方法的 Key-Value 结果，相同的 Key 发送到同一 个 reduce 里处理，然后迭代 Key，把 Value 相加，结果写到 HDFS 系统里。

#### 3.2.3 编写 WordMain 驱动类

前面我们实现了 Mapper 和 Reducer 的抽象类，接下来实现一个 MapReduce 的驱动类， 驱动类主要用来启动一个 MapReduce 作业。

![image 139](<Hadoop应用开发技术详解》迷你书_images/imageFile139.png>)

编写 WordMain 驱动类如代码清单 3-3 所示。

代码清单 3-3 代码文件 wordcount\WordMain.java

package wordcount;

import org.apache.hadoop.conf.Conﬁ guration; import org.apache.hadoop.fs.Path; import org.apache.hadoop.io.IntWritable; import org.apache.hadoop.io.Text; import org.apache.hadoop.mapreduce.Job; import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; import org.apache.hadoop.util.GenericOptionsParser;

public class WordMain {

public static void main(String[] args) throws Exception { Conﬁ guration conf = new Conﬁ guration(); String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs(); /**

- * 这里必须有输入 / 输出
- */


if (otherArgs.length != 2) { System.err.println("Usage: wordcount <in> <out>"); System.exit(2);

} Job job = new Job(conf, "word count"); job.setJarByClass(WordMain.class); // 主类 job.setMapperClass(WordMapper.class); //Mapper job.setCombinerClass(WordReducer.class); // 作业合成类 job.setReducerClass(WordReducer.class); //Reducer job.setOutputKeyClass(Text.class); // 设置作业输出数据的关键类 job.setOutputValueClass(IntWritable.class); // 设置作业输出值类 FileInputFormat.addInputPath(job, new Path(otherArgs[0])); // 文件输入 FileOutputFormat.setOutputPath(job, new Path(otherArgs[1])); // 文件输出

System.exit(job.waitForCompletion(true) ? 0 : 1); // 等待完成退出 }

}

WordMain 驱动类解释如下。

❑ Configuration 类： 读 取 Hadoop 的 配 置 文 件， 如 site-core.xml、mapred-site.xml、 hdfs-site.xml 等。也可以使用 set 方法进行重新设置，如 conf.set("fs.default.name", "hdfs://xxxxx:9000")。注意，set 方法设置的值会替代配置文件里面配置的值。

❑ Job 类： 表示一个 MapReduce 任务。Job 的构造方法有两个参数，第一个参数为 Configuration，第二个参数为 Job 的名称（等同于任务的名称）。 至此，WordCount 就开发完成了，接下来就是把 WordCount 放在 Hadoop 上运行。

### 3.3 打包、部署和运行

![image 140](<Hadoop应用开发技术详解》迷你书_images/imageFile140.png>)

Hadoop 的部署环境其实指 Linux 的命令行环境，因为现在执行计算的服务器大多是 Linux 文本界面。在集群系统中有一台 Master，这台机器就是我们的部署环境，而且只需在 这台机器上部署即可，Hadoop 会自己把任务传送给各个 Slave。

部署是什么意思呢 ? 其实从代码编写完到任务，可以按照我们的想法在服务器上运行 的这段时间都叫部署。我们在部署环境下运行项目，一般的做法是：在开发环境下（比如 Eclipse 的图形开发界面），将项目编译好，保存成 JAR 文件，然后传送到 Master 上运行。

注意 传送之前的操作都是 Eclipse 上的操作，对于 Eclipse 开发人员来说，其实是很简单 的。所以“部署环境下的运行”只要把编译好的 JAR 文件传送到 Master 并让它运行即可 ( 在 Slave 上也可以运行 )。

一般情况下，默认已经在 Master 上获得 JAR 文件，并放置在 Master 的 $HADOOP_ HOME 下面。

#### 3.3.1 打包成 JAR 文件

把 3.2 节编写的 MapReduce 工程打包成 JAR 文件，然后发送到 Hadoop 的 Master 节点

上运行。如图 3-4 所示，右击 wordcount → Export → JAR fil。 选择 JAR file 以后，单击 Next 按钮，进入 JAR 文件过滤对话框，如图 3-5 所示。 这里只选择 src 文件夹就可以了，因为 lib 下面的 JAR 是 Hadoop 自带的，不需要把 它添加到 JAR 文件里面，还要注意的就是，不要把 classpath 和 project 文件添加到 JAR 文件中。在“ Select the export destination”下面的 JAR file 选项中，选择 JAR 文件存放的 位置和文件名。

<table>
  <tr>
    <th>![image 141](<Hadoop应用开发技术详解》迷你书_images/imageFile141.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 142](<Hadoop应用开发技术详解》迷你书_images/imageFile142.png>)</th>
  </tr>
</table>


图 3-4 将 WordCount 工程保存成 JAR 文件 图 3-5 JAR 文件过滤

- 3.3.2 部署和运行 部署和运行的步骤如下所示。


![image 143](<Hadoop应用开发技术详解》迷你书_images/imageFile143.png>)

- 1）把 wordcount.jar 文件发送到 Hadoop 集群的 Master 节点的 $HADOOP_HOME 下面。
- 2）在 Master 节点的 /opt/ 下面创建两个文件 file1.txt 和 file2.txt，文件中是我们要统计

单词个数的内容。

- 3）把 file1.txt 和 file2.txt 传到 HDFS 系统下面，命令如下所示。 jayliu@Ubuntu1:/opt/hadoop-1.0.3$ hadoop fs -put /opt/ﬁ le* /user/hadoop/input/
- 4）查看文件是否上传成功。


jayliu@Ubuntu1:/opt/hadoop-1.0.3$ hadoop fs -ls /user/hadoop/input/ Warning: $HADOOP_HOME is deprecated. Found 2 items

-rw-r--r-- 1 jayliu supergroup 66 2013-04-20 18:29

- /user/hadoop/input/ﬁ le1.txt

-rw-r--r-- 1 jayliu supergroup 66 2013-04-20 18:29

- /user/hadoop/input/ﬁ le2.txt 5）运行 WordCount 程序。


jayliu@Ubuntu1:/opt/hadoop-1.0.3$ hadoop jar wordcount.jar wordcount.WordMain /user/hadoop/input/ﬁ le* /user/hadoop/output1 Warning: $HADOOP_HOME is deprecated.

13/04/20 18:35:56 INFO input.FileInputFormat: Total input paths to process : 2

- 13/04/20 18:35:56 INFO util.NativeCodeLoader: Loaded the native-hadoop library

- 13/04/20 18:35:56 WARN snappy.LoadSnappy: Snappy native library not loaded
- 13/04/20 18:36:00 INFO mapred.JobClient: Running job: job_201304200039_0002


- 13/04/20 18:36:01 INFO mapred.JobClient: map 0% reduce 0%
- 13/04/20 18:37:56 INFO mapred.JobClient: map 50% reduce 0%
- 13/04/20 18:38:04 INFO mapred.JobClient: map 100% reduce 0% 13/04/20 18:38:26 INFO mapred.JobClient: map 100% reduce 100%


13/04/20 18:38:34 INFO mapred.JobClient: Job complete: job_201304200039_0002 13/04/20 18:38:35 INFO mapred.JobClient: Counters: 29 13/04/20 18:38:35 INFO mapred.JobClient: Job Counters 13/04/20 18:38:35 INFO mapred.JobClient: Launched reduce tasks=1 13/04/20 18:38:35 INFO mapred.JobClient: SLOTS_MILLIS_MAPS=214131 13/04/20 18:38:35 INFO mapred.JobClient: otal time spent by all reduces waiting

after reserving slots (ms)=0 13/04/20 18:38:35 INFO mapred.JobClient: otal time spent by all maps waiting

after reserving slots (ms)=0 13/04/20 18:38:35 INFO mapred.JobClient: Launched map tasks=2 13/04/20 18:38:35 INFO mapred.JobClient: Data-local map tasks=2 13/04/20 18:38:35 INFO mapred.JobClient: SLOTS_MILLIS_REDUCES=19234 13/04/20 18:38:35 INFO mapred.JobClient: File Output Format Counters 13/04/20 18:38:35 INFO mapred.JobClient: Bytes Written=73 13/04/20 18:38:35 INFO mapred.JobClient: FileSystemCounters 13/04/20 18:38:35 INFO mapred.JobClient: FILE_BYTES_READ=188 13/04/20 18:38:35 INFO mapred.JobClient: HDFS_BYTES_READ=356 13/04/20 18:38:35 INFO mapred.JobClient: FILE_BYTES_WRITTEN=64611 13/04/20 18:38:35 INFO mapred.JobClient: HDFS_BYTES_WRITTEN=73 13/04/20 18:38:35 INFO mapred.JobClient: File Input Format Counters 13/04/20 18:38:35 INFO mapred.JobClient: Bytes Read=132 13/04/20 18:38:35 INFO mapred.JobClient: Map-Reduce Framework 13/04/20 18:38:35 INFO mapred.JobClient: Map output materialized bytes=194 13/04/20 18:38:35 INFO mapred.JobClient: Map input records=8 13/04/20 18:38:35 INFO mapred.JobClient: Reduce shufﬂ e bytes=94 13/04/20 18:38:35 INFO mapred.JobClient: Spilled Records=34 13/04/20 18:38:35 INFO mapred.JobClient: Map output bytes=252 13/04/20 18:38:35 INFO mapred.JobClient: CPU time spent (ms)=5540 13/04/20 18:38:35 INFO mapred.JobClient: Total committed heap usage (bytes)= 413466624 13/04/20 18:38:35 INFO mapred.JobClient: Combine input records=30 13/04/20 18:38:35 INFO mapred.JobClient: SPLIT_RAW_BYTES=224 13/04/20 18:38:35 INFO mapred.JobClient: Reduce input records=17 13/04/20 18:38:35 INFO mapred.JobClient: Reduce input groups=11 13/04/20 18:38:35 INFO mapred.JobClient: Combine output records=17 13/04/20 18:38:35 INFO mapred.JobClient: Physical memory (bytes) snapshot=231944192 13/04/20 18:38:35 INFO mapred.JobClient: Reduce output records=11 13/04/20 18:38:35 INFO mapred.JobClient: Virtual memory (bytes) snapshot=1119518720 13/04/20 18:38:35 INFO mapred.JobClient: Map output records=30

![image 144](<Hadoop应用开发技术详解》迷你书_images/imageFile144.png>)

如果看到上面的结果，说明运行成功！ 注意，运行的 JAR 文件需要放在 $HADOOP_HOME 下面，否则会报文件打开错误，

错误提示如下所示。

Caused by: java.util.zip.ZipException: error in opening zip ﬁ le at java.util.zip.ZipFile.open(Native Method) at java.util.zip.ZipFile.<init>(ZipFile.java:127) at java.util.jar.JarFile.<init>(JarFile.java:135) at java.util.jar.JarFile.<init>(JarFile.java:72) at org.apache.hadoop.util.RunJar.main(RunJar.java:88)

结果输出的目录必须是不存在，否则会报文件已存在错误，错误提示如下所示。

13/04/20 18:39:54 INFO mapred.JobClient: Cleaning up the staging area hdfs:// Ubuntu1:9000/hadoop/mapred/staging/jayliu/.staging/job_201304200039_0003

13/04/20 18:39:54 ERROR security.UserGroupInformation: PriviledgedActionException as:jayliu cause:org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory /user/hadoop/output1 already exists

Exception in thread "main" org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory /user/hadoop/output1 already exists

at org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.checkOutputSpecs

(FileOutputFormat.java:137) at org.apache.hadoop.mapred.JobClient$2.run(JobClient.java:887) at org.apache.hadoop.mapred.JobClient$2.run(JobClient.java:850) at java.security.AccessController.doPrivileged(Native Method) at javax.security.auth.Subject.doAs(Subject.java:396) at org.apache.hadoop.security.UserGroupInformation.doAs

(UserGroupInformation.java:1121)

#### 3.3.3 测试结果

![image 145](<Hadoop应用开发技术详解》迷你书_images/imageFile145.png>)

接下来就是查看运行的结果，看看是否和我们预测的一样，预测的结果如下。

? 2 Hello 2 Hello, 2 are 4 coding 2 hadoop 2 i 4 love 4 ok 2 ok? 2 you 4

查看生成的结果文件，如图 3-6 所示。

![image 146](<Hadoop应用开发技术详解》迷你书_images/imageFile146.png>)

图 3-6 查看结果文件目录

结果文件一般由三部分组成。

❑ _SUCCESS 文件：表示 MapReduce 运行成功。

❑ _logs 文件夹：存放运行 MapReduce 的日志。

❑ part-r-00000 文件：存放结果，也是默认生成的结果文件。 使用“ hadoop fs –text/user/hadoop/output1/part-r-00000”命令查看 MapReduce 生成的

结果文件，如图 3-7 所示。

3.4 本章小结 39

![image 147](<Hadoop应用开发技术详解》迷你书_images/imageFile147.png>)

图 3-7 查看结果文件

到这里，整个 MapReduce 的快速入门就结束了。本章使用一个完整的案例，从开发到 部署再到查看结果，让大家对 MapReduce 的基本使用有所了解。

### 3.4 本章小结

![image 148](<Hadoop应用开发技术详解》迷你书_images/imageFile148.png>)

本章主要是通过一个实例来介绍 MapReduce 开发的整个过程。整个过程包括环境的准 备、Mapper 和 Reducer 代码的编写、打包、部署和运行的一整套流程。学习这套流程对以 后 MapReduce 的开发奠定了基础。此外，介绍了 MapReduce 开发遇到的常见错误，在这里 希望读者按照本章的步骤一步一步地往下做。争取使程序能跑起来，对以后章节的上机操 作打下基础。

