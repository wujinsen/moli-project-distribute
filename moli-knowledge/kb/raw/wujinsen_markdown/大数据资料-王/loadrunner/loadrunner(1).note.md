LoadRuner，是⼀种预测系统⾏为和 的负载测试⼯具。通过以模拟上千万⽤户实施并发负载及实 时 监测的⽅式来确认和查找问题，LoadRuner能够对整个企业架构进⾏测试。通过使⽤ LoadRuner，企业能最⼤限度地缩短测试时间，优化 和加速应⽤系统的发布周期。 LoadRuner 是⼀种适⽤于各种体系架构的⾃动负载测试⼯具，它能预测系统⾏为并评估系统 。

性能 性能

性能

性能

# ⽬录

1摘要介绍 2对象 3主要功能

- ▪ ▪ ▪ ▪ ▪ 4性能测试
- ▪ ▪ ▪ 5组件 6实例应⽤
- ▪ ▪ ▪ ▪ 7最新版本 8背景 9参数对照
- ▪ ▪ ▪


虚拟⽤户 真实负载 定位性能 分析结果 重复测试

LoadRuner的虚拟⽤户 使⽤Vugen创建虚拟⽤户执⾏脚本 使⽤控制器来调度虚拟⽤户

随机函数 举例 分析占⽤率 版本要求

LR函数： 脚本 WEB函数列表：

# 1摘要介绍编辑

概要介绍

LoadRuner 是⼀种预测系统⾏为和性能的⼯业标准级负载测试⼯具。通过以模拟上千万⽤户实施并发 负载及实时性能监测的⽅式来确认和查找问题，LoadRuner 能够对整个企业架构进⾏测试。通过使⽤ LoadRuner ， 企业能最⼤限度地缩短测试时间， 优化性能和加速应⽤系统的发布周期。企业的⽹络 应⽤环境都必须⽀持⼤量⽤户，⽹络体系架构中含各类应⽤环境且由不同供应商提供软件和硬件产 品。难以预知的⽤户负载和愈来愈复杂的应⽤环境使公司时时担⼼会发⽣⽤户响应速度过慢， 系统崩 溃等问题。这些都不可避免地导致公司收益的损失。Mercury Interactive 的 LoadRuner 能让企业保 护⾃⼰的收⼊来源， ⽆需购置额外硬件⽽最⼤限度地利⽤现有的IT 资源， 并确保终端⽤户在应⽤系统 的各个环节中对其测试应⽤的质量， 可靠性和可扩展性都有良好的评价。LoadRuner 是⼀种适⽤于 各种体系架构的⾃动负载测试⼯具， 它能预测系统⾏为并优化系统性能。LoadRuner 的测试对象是 整个企业的系统， 它通过模拟实际⽤户的操作⾏为和实⾏实时性能监测， 来帮助您更快的查找和发现 问题。此外，LoadRuner 能⽀持⼴范的协议和技术， 为您的特殊环境提供特殊的解决⽅案。

# 2对象编辑

LoadRuner的测试对象是整个企业的系统，它通过模拟实际⽤户的操作⾏为和实⾏实时 监测，来 帮助您更快地查找和发现问题。此外，LoadRuner能⽀持⼴泛的协议和技术，为您的特殊环境提供特 殊的解决⽅案。 [1]

性能

# 3主要功能编辑

## 虚拟⽤户使⽤LoadRuner的Virtual User Generator，您能很简便地创⽴起系统 负载。该引擎能

LoadRuner性能虚拟⽤户模拟测试够⽣成虚拟⽤户，以虚拟⽤户的⽅式模拟真实⽤户的业务操作⾏ 为。它先记录下业务流程（如下订单或机票预定），然后将其转化为 。利⽤虚拟⽤户，您可 以在Windows ，UNⅨ 或Linux 机器上同时产⽣成千上万个⽤户访问。所以LoadRuner能极⼤的减少

测试脚本

所需的硬件和⼈⼒资源。 ⽤Virtual User Generator 建⽴测试 后，您可以对其进⾏参数化操作，这⼀操作能让您利⽤⼏套不 同的实际发⽣数据来测试您的 ，从⽽反映出本系统的负载能⼒。以⼀个订单输⼊过程为例， 参数化操作可将记录中的固定数据，如订单号和客户名称，由可变值来代替。在这些变量内随意输⼊ 可能的订单号和客户名，来匹配多个实际⽤户的操作⾏为。

负载测试

脚本 应⽤程序

## 真实负载

Virtual users 建⽴起后，您需要设定您的负载⽅案，业务流程组合和虚拟 。⽤LoadRuner的 Controler，您能很快组织起多⽤户的测试⽅案。Controler 的Rendezvous 功能提供⼀个互动的环 境，在其中您既能建⽴起持续且循环的负载，⼜能管理和驱动 ⽅案。

⽤户数量

负载测试

⽽且，您可以利⽤它的⽇程计划服务来定义⽤户在什么时候访问系统以产⽣负载。这样，您就能将

测 试过程

⾃动化。同样您还可以⽤Controler 来限定您的负载⽅案，在这个⽅案中所有的⽤户同时执⾏⼀ 个动作 -如登陆到⼀个库存 -来模拟峰值负载的情况。另外，您还能监测系统架构中各个 组件的 - 包括 ， ， 等 -来帮助客户决定系统的配置。

应⽤程序 性能 服务器 数据库 ⽹络设备

## 定位性能

LoadRuner内含集成的实时监测器，在负载测试过程的任何时候，您都可以观察到应⽤系统的运⾏ 。这些 监测器为您实时显示交易性能数据（如响应时间）和其它 包括aplication server,web server，⽹路设备和 等的实时性能。这样，您就可以在 中从客户和 的双⽅⾯评估这些 的运⾏ ，从⽽更快地发现问题。 利⽤LoadRuner的ContentCheck TM ，您可以判断负载下的 功能正常与否。ContentCheck 在Virtual users 运⾏时，检测 的 数据包内容，从中确定是否有错误内容传送出去。它的 实时 帮助您从终端⽤户⻆度观察程序 状况。

性 能 性能 系统组件

数据库 测试过程 服务器 系统组件 性能

应⽤程序 应⽤程序 ⽹络

浏览器 性能

## 分析结果

⼀旦测试完毕后，LoadRuner收集汇总所有的测试数据，并提供⾼级的分析和报告⼯具，以便迅速查 找到 问题并追溯原由。使⽤LoadRuner的Web 交易细节监测器，您可以了解到将所有的图象、框 架和⽂本下载到每⼀⽹⻚上所需的时间。例如，这个交易细节分析机制能够分析是否因为⼀个⼤尺⼨ 的图形⽂件或是第三⽅的数据组件造成应⽤系统运⾏速度减慢。另外，Web 交易细节监测器分解⽤于

性能

、 和 上端到端的反应时间，便于确认问题，定位查找真正出错的组件。例如，您可 以将 进⾏分解，以判断 解析时间，连接 或 认证所花费的时间。通过使⽤ LoadRuner的分析⼯具，您能很快地查找到出错的位置和原因并作出相应的调整。

客户端 ⽹络 服务器 ⽹络延时 DNS 服务器 SL

## 重复测试

是⼀个重复过程。每次处理完⼀个出错情况，您都需要对您的 在相同的⽅案下，再 进⾏⼀次 。以此检验您所做的修正是否改善了运⾏ 。 LoadRuner完全⽀持EJB 的 。这些基于Java 的组件运⾏在 上，提供⼴泛的应⽤服 务。通过测试这些组件，您可以在 的早期就确认并解决可能产⽣的问题。 利⽤LoadRuner,您可以很⽅便地了解系统的 。它的Controler 允许您重复执⾏与出错修改前相同 的测试⽅案。它的基于HTML 的报告为您提供⼀个⽐较 结果所需的基准，以此衡量在⼀段时间 内，有多⼤程度的改进并确保应⽤成功。由于这些报告是基于HTML 的⽂本，您可以将其公布于您公 司的内部⽹上，便于随时查阅。 接下来的⽂章编者就将辑录⼀篇⽹上的使⽤LoadRuner&reg;来测试BEA中间件产品⽂章来与⼤家分享 如何使⽤LoadRuner进⾏实际的 。

负载测试 应⽤程序 负载测试 性能 负载测试 应⽤服务器 应⽤程序开发 性能

性能

性能测试

# 4性能测试编辑

## LoadRuner的虚拟⽤户

LoadRuner使⽤虚拟⽤户（Virtual users）来模拟实际⽤户对业务系统施加压⼒。虚拟⽤户在⼀个中 央控制器（controler station）的监视下⼯作。 在做⼀个测试⽅案时，要做的第⼀件事就是创建虚拟⽤户执⾏ 。LoadRuner提供了Virtual User Generator来录制或编辑虚拟⽤户脚本。

脚本

## 使⽤Vugen创建虚拟⽤户执⾏脚本

- A．从 中选择运⾏Virtual User Generator：
- B．创建⼀个单协议 ，选择协议类型为"Tuxedo 7"
- C．在弹出的窗⼝中输⼊Tuxedo客户机程序的可执⾏⽂件名（SimpAp.exe），并选择"Record into Action"为Action。 点击"OK"开始录制 ，这时Vugen就会启动Simpap.exe，如下图所示，输⼊WSNADR，输⼊字 符串（Tuxedo is powerful！）之后，点击TOUPER，TUXEDO 完成请求后把输出字符串 （TUXEDO IS POWERFUL！）写到"Output string"中，点击停⽌录制按钮。
- D．编辑Vuser 。在C中做的所有操作都被录了下来，记录到⼀个 中，其内容如下，把它 存为simpap。


菜单

脚本

脚本

服务器

脚本 脚本⽂件

内容如下： #include "lrt.h" #include "replay.vdf" Actions() { lrt_tuxputenv("WSNADR=/172.2.32.25 710"); lr_think_time⑶； tpresult_int = lrt_tpinitialize(LRT_END_OF_PARMS); lrt_abort_on_eror();

脚本

- data_0 = lrt_tpaloc("STRING",",1）； lrt_strcpy(data_0,sbuf_1）；
- data_1 = lrt_tpaloc("STRING",",1）； tpresult_int = lrt_tpcal("TOUPER",data_0,0,&data_1,&olen,0); lrt_abort_on_eror(); lrt_tpfre(data_0); lrt_tpfre(data_1）； lrt_tpterm(); return 0; } 代码中加粗的函数是LoadRuner对TUXEDO函数的⼆次包装。


- E．点击⼯具栏中的"执⾏"按钮来执⾏我们刚才录制的 ，确保执⾏⽆误。


脚本

## 使⽤控制器来调度虚拟⽤户

- A．从菜单中选择运⾏Controler：
- B．创建⼀个新的Scenario，选择刚才录制的 （simpap）： 点击"OK"，弹出Scenario调度界⾯。在"Quantity"中输⼊10，表示使⽤10个虚拟⽤户。（虚拟⽤户 与购买的LICENSE有关联）
- C．点击"Edit Schedule"来编辑压⼒调度。
- D．选择"Runtime setings"来作运⾏时设置。 在Pacing的设置中，"Number of Iterations"⽤于设置Vusers的Actions被执⾏的次数；"Start new iteration"⽤于设置调度器在什么时机迭代执⾏Vusers的Actions。 "Think Time"⽤于设置Vusers的反应和思考时间，以尽量做到和正常⼈⼀样来施压。"Ignore think time"表示忽略思考时间，这是理想状态，⼀般不使⽤。"As recorded"表示按照录制时的实际操作时 间。"Multiply recorded think time by"表示Vusers的思考时间是实际录制时间的若⼲倍。 在"Miscelaneous"中设置⼀些杂项，如使⽤进程还是使⽤ 等。对于TUXEDO，好象只能选进程模 式。
- E．选择"Start scenario"来开始本次压⼒测试调度。 执⾏结果分析如下：施压时间为5分41秒，Vusers数量为10，⼀共完成的Actions交易数量为5625 笔，平均响应时间为5.561秒，TPS为17.8。 [1]


脚本

线程

# 5组件编辑

- 1.VuGen Load Generator（虚拟⽤户⽣成器）⽤于捕获最终⽤户业务流程和创建⾃动性能 （也称为虚拟⽤户脚本）。
- 2.Controler （控制器）⽤于组织、驱动、管理和监控 。
- 3.Analysis （分析器）有助于您查看、分析和⽐较 结果。


测试脚 本

负载测试 性能

# 6实例应⽤编辑

## 随机函数

在 ⼯具中如何巧⽤LoadRuner的 LoadRuner有⾃带的随机函数，如果巧妙的加以采⽤，能解决⼀些看似很困难的实际问题。 ⼀个项⽬的 。与 直连，根据外部传⼊的SQL ID和SQL参数，从指定数据库中读取SQL 模版，拼装成真实的SQL语句、执⾏，并将得到的结果放⼊缓存中。⽬的是减少 的压⼒。 该系统将⽀撑⼤量的SQL操作， ⾃然成为备受关注的焦点之⼀。

软件测试 随机函数

性能测试 数据库

数据库 性能

由于它跟SQL语句相关，在真实环境下，同⼀时间可能执⾏着不同类型的SQL，即便是同⼀类型，其参 数也各式各样。那么，怎样才能模拟出最符合实际情况的 场景呢？ ⾸先设计场景，即，在LoadRuner中按照⽐例随机取到某⼀类型的SQL，再随机传⼊参数给它，让最 终的每条SQL都是随机⽣成，各不相同。 从场景中，可以看到，此处涉及双重随机。只采⽤loadruner的参数设置是⽆法实现的。此时需要想办 法先按设定好的⽐例随机取到SQL，然后在每条SQL上随机取 中的参数。 于是想到了 的随机函数。先实现随机取SQL ID，之后再在特定的SQL中随机取 中 的参数。 LoadRuner中， 是rand()，它⽤来产⽣0到rand_max之间的随机整数。函数原型是 int rand (void); 然⽽调⽤rand之前，必须给随机数产⽣⼀个 。这个种⼦由srand()函数产⽣。其原型是 int srand (sedTime);

性能测试

参数列表 loadruner 参数列表

随机函数

随机种⼦

## 举例

采⽤上述两个函数，就能实现第⼀重随机了。具体 代码如下：

脚本 脚本 性能测试

通过上⾯的 ，实现了 设计的场景。调试通过后，放⼊Controler中执⾏。实际执⾏过 程中，Vuser将会按⽐例随机取到不同类型的SQL，并随机取到SQL中的参数，执⾏特定的SQL语句。 注：sqlid_name是SQL ID名称；random_para是通过file⽅式实现的随机参数；tn是web_url函数的

快 照

名称。 巧⽤LoadRuner的随机函数，能解决不少实际问题。[2]

## 分析占⽤率

LoadRuner分析⻚⾯1. 平均 响应时间 Average Transation Response Time 优秀：<2s 良好：2-5s 及格：6-10s 不及格：>10s

事务

- 2. 每秒点击率 Hits per Second 当增⼤系统的压⼒（或增加 ）时，吞吐率和TPS的变化 呈⼤体⼀致，则系统基本稳定。 若压⼒增⼤时，吞吐率的曲线增加到⼀定程度后出现变化缓慢，甚⾄平坦，很可能是 出现 瓶 颈，同理若点击率/TPS曲线出现变化缓慢或者平坦，很可能是服务器响应时间增加，观察服务器资源 使⽤情况，确定是否是 问题。
- 3. Time to Last Byte
- 4. 每秒系统处理事务数 Transaction per second


并发⽤户数 曲线

⽹络 带宽

服务器 请求响应时间

- 5.
- 6. CPU利⽤率 Procesor / %Procesor Time 好：70% 坏：85% 很差：90%+
- 7. 操作消耗的CPU时间 Procesor / %User Time 如果该值较⼤，可以考虑是否能通过友好算法等⽅法降低这个值。如果该

是 ， Procesor\%User Time 值⼤的原因很可能是数据库的排序或是函数操作消耗 了过多的CPU时间，此时可以考虑对 进⾏优化。

- 8. CPU平均利⽤率 Procesor /%Privileged Time 如果该参数值和"Physical Disk"参数值⼀直很⾼，表明I/O有问题。可考 虑更换更快的硬盘系统
- 9. 处理队列中的 Procesor / Procesor Queue Length 如果该值保持不变（>=2）个并且%Procesor Time 超过 90%，那么可能存在处理器瓶颈。如果发现超过2，⽽处理器的利⽤率却⼀直很低，那么或许更应该去 解决处理器阻塞问题，这⾥处理器⼀般不是瓶颈。
- 10. ⽂件系统缓存 Memory / Cache Bytes 50%的可⽤


吞吐量 Throughout

数据库

服 务器 数据库服务器

数据库系统 核⼼态

线程数

物理内存

1. 剩余的可⽤内存 Memory / Avaiable Mbytes ⾄少要有10% 的 值

物理内存

- 12. 每秒下载⻚数 Memory / pages/sec 好：⽆⻚交换 坏：CPU每秒10个⻚交换 很差：更多的⻚交换
- 13. ⻚⾯读取操作速率 Memory / page read/sec 如果⻚⾯读取操作速率很低，同时 % Disk Time 和 Avg.Disk Queue Length 的值很⾼，则可能有磁盘瓶径。但是，如果队列⻓度增加的同时⻚⾯读取速率并未降低，则

。

- 14. 利⽤率 Physical Disk / %Disk Time 好：<30% 坏：<40% 很差：<50%+
- 15. 物理磁盘平均磁盘I/O队列⻓度 Physical Disk / Avg.Disk Queue Length 该值应不超过磁盘数的1.5~2 倍。要提⾼ ，可增加磁盘
- 16.


内存不 ⾜

物理磁盘

性能 ⽹络吞吐量

Network Interface / Bytes Total/sec 判断 连接速度是否是瓶颈，可以⽤该计数器的值和当前⽹络 的带宽，结果应该⼩于50%

⽹络

- 17. 数据⾼速缓存区命中率 命中率应⼤于0.90最好
- 18. 共享区库缓存区命中率 命中率应⼤于0. 9
- 19. 监控 SGA 中字典 的命中率 命中率应⼤于0.85
- 20. 检测回滚段的争⽤ ⼩于1%
- 21. 监控 SGA 中重做⽇志缓存区的命中率 应该⼩于1%2. 监控内存和硬盘的排序⽐率 最好使它⼩于 10% [3]安装


缓冲区

## 版本要求

LoadRuner 分为Windows 版本和Unix 版本。如果所有 基于Windows平台，那么只要安装 Windows 版本即可。 LoadRuner的Unix版本仅提供Load Generator组件的安装（即LoadRuner中的负载⽣成器）。也就 是说，这个负载⽣成器可以在Unix环境下安装和运⾏，并提供给Controler进⾏远程管理。但是， 的录制和场景的设计必须在Windows平台完成。 系统要求 运⾏LoadRuner，内存最好在128M 以上，LoadRuner7.8 的最低要求。内存最好在512M 以上，安 装LoadRuner 的磁盘空间⾄少剩余50M。 最好为Windows 2 0。

测试环境

脚本

操作系统

- 7最新版本编辑
- 8背景编辑


⽬前（2012年7⽉）可⽤的最新版本为：HP LoadRuner1.50

Mercury HP

（美科利）已于206年被 （惠普）收购。

# 9参数对照编辑

LR函数：

lr_start_transaction 为 标记 的开始 lr_end_transaction 为性能分析标记事务的结束 lr_rendezvous 在 Vuser 中设置集合点 lr_think_time 暂停 Vuser 中命令之间的执⾏ lr_end_sub_transaction 标记⼦事务的结束以便进⾏性能分析 lr_end_transaction 标记 LoadRuner 事务的结束 Lr_end_transaction("trans1",Lr_auto);

性能分析 事务

脚本 脚本

lr_end_transaction_instance 标记事务实例的结束以便进⾏性能分析 lr_fail_trans_with_eror 将打开事务的状态设置为 LR_FAIL 并发送错误消息 lr_get_trans_instance_duration 获取 实例的持续时间（由它的句柄指定） lr_get_trans_instance_wasted_time 获取事务实例浪费的时间（由它的句柄指定） lr_get_transaction_duration 获取 的持续时间（按事务的名称） lr_get_transaction_think_time 获取 的思考时间（按事务的名称） lr_get_transaction_wasted_time 获取 浪费的时间（按事务的名称） lr_resume_transaction 继续收集事务数据以便进⾏性能分析 lr_resume_transaction_instance 继续收集事务实例数据以便进⾏性能分析 lr_set_transaction_instance_status 设置事务实例的状态 lr_set_transaction_status 设置打开事务的状态 lr_set_transaction_status_by_name 设置事务的状态 lr_start_sub_transaction 标记⼦事务的开始 lr_start_transaction 标记事务的开始 Lr_start_transaction("trans1"); lr_start_transaction_instance 启动嵌套事务（由它的⽗事务的句柄指定） lr_stop_transaction 停⽌事务数据的收集 lr_stop_transaction_instance 停⽌事务（由它的句柄指定）数据的收集 lr_wasted_time 消除所有打开事务浪费的时间 lr_get_atrib_double 检索 命令⾏中使⽤的 double 类型变量 lr_get_atrib_long 检索 命令⾏中使⽤的 long 类型变量 lr_get_atrib_string 检索 命令⾏中使⽤的字符串 lr_user_data_point 记录⽤户定义的数据示例

事务

事务 事务 事务

脚本 脚本

脚本

## 脚本

将有关 Vuser 的信息返回给 Vuser 脚本 lr_get_host_name 返回执⾏ Vuser 的主机名 lr_get_master_host_name 返回运⾏ LoadRuner Controler 的计算机名 lr_eval_string ⽤参数的当前值替换参数 lr_save_string 将以 NUL 结尾的字符串保存到参数中 lr_save_var 将变⻓字符串保存到参数中 lr_save_datetime 将当前⽇期和时间保存到参数中 lr _advance_param 前进到下⼀个可⽤参数 lr _decrypt 解密已编码的字符串 lr_eval_string_ext 检索指向包含参数数据的 的指针 lr_eval_string_ext_fre 释放由 lr_eval_string_ext 分配的指针

lr_whoami 脚本

脚本

缓冲区

lr_save_searched_string 在 中搜索字符串实例，并相对于该字符串实例，将该缓冲区的⼀部分 保存到参数中 lr_debug_mesage 将调试信息发送到输出窗⼝ lr_eror_mesage 将错误消息发送到输出窗⼝ lr_get_debug_mesage 检索当前消息类 lr_log_mesage 将消息发送到⽇志⽂件 lr_output_mesage 将消息发送到输出窗⼝ lr_set_debug_mesage 设置调试消息类 lr_vuser_status_mesage ⽣成带格式的输出，并将其写到 ControlerVuser 状态区域 lr_mesage 将消息发送到 Vuser ⽇志和输出窗⼝ lr_load_dl 加载外部 DL lr_pek_events 指明可以暂停 Vuser 执⾏的位置 lr_think_time 暂停 的执⾏，以模拟思考时间（实际⽤户在操作之间暂停以进⾏思考的时间） lr_continue_on_eror 指定处理错误的⽅法 lr_continue_on_eror (0）；lr_continue_on_eror ⑴； lr_rendezvous 在 Vuser 脚本中设置集合点 TE_wait_cursor 等待光标出现在终端窗⼝的指定位置 TE_wait_silent 等待 在指定秒数内处于静默状态 TE_wait_sync 等待系统从 X-SYSTEM 或输⼊禁⽌模式返回 TE_wait_text 等待字符串出现在指定位置 TE_wait_sync_transaction 记录系统在最近的 X SYSTEM 模式下保持的时间

缓冲区

脚本 脚本

客户端应⽤程序

## WEB函数列表：

web_custom_request 允许您使⽤ HTP ⽀持的任何⽅法来创建⾃定义 HTP 请求 web_image 在定义的图像上模拟⿏标单击 web_link 在定义的⽂本链接上模拟⿏标单击 web_submit_data 执⾏“⽆条件”或“⽆上下⽂”的 web_submit_form 模拟表单的提交 web_url 加载由“URL”属性指定的 URL web_set_certificate 使 Vuser 使⽤在 Internet Explorer 注册表中列出的特定证书 web_set_certificate_ex 指定证书和密钥⽂件的位置和格式信息 web_set_user 指定 Web 的登录字符串和密码，⽤于 Web 服务器上已验证⽤户身份的区域 web_cache_cleanup 清除缓存模拟程序的内容 web_find 在 HTML ⻚内搜索指定的⽂本字符串 web_global_verification 在所有后⾯的 HTP 请求中搜索⽂本字符串 web_image_check 验证指定的图像是否存在于 HTML⻚内 web_reg_find 在后⾯的 HTP 请求中注册对 HTML源或原始 中⽂本字符串的搜索

表单

服务器

缓冲区

web_disable_kep_alive 禁⽤ Kep-Alive HTP 连接 web_enable_kep_alive 启⽤ Kep-Alive HTP 连接 web_set_conections_limit 设置 Vuser 在运⾏ 时可以同时打开连接的最⼤数⽬ web_concurent_end 标记并发组的结束 web_concurent_start 标记并发组的开始 web_ad_cokie 添加新的 Cokie 或修改现有的 Cokie web_cleanup_cokies 删除当前由 Vuser 存储的所有 Cokie web_remove_cokie 删除指定的 Cokie web_create_html_param 将 HTML ⻚上的动态信息保存到参数中。（LR 6.5 及更低版本） web_create_html_param_ex 基于包含在 HTML ⻚内的动态信息创建参数（使⽤嵌⼊边界）（LR 6.5 及更低版本）。 web_reg_save_param 基于包含在 HTML ⻚内的动态信息创建参数（不使⽤嵌⼊边界） web_set_max_html_param_len 设置已检索的动态 HTML 信息的最⼤⻓度 web_ad_filter 设置在下载时包括或排除 URL 的条件 web_ad_auto_filter 设置在下载时包括或排除 URL 的条件 web_remove_auto_filter 禁⽤对下载内容的筛选 web_ad_auto_header 向所有后⾯的 HTP 请求中添加⾃定义标头 web_ad_header 向下⼀个 HTP 请求中添加⾃定义标头 web_cleanup_auto_headers 停⽌向后⾯的 HTP 请求中添加⾃定义标头 web_remove_auto_header 停⽌向后⾯的 HTP 请求中添加特定的标头 web_revert_auto_header 停⽌向后⾯的 HTP 请求中添加特定的标头，但是⽣成隐性标头 web_save_header 将请求和响应标头保存到变量中 web_set_proxy 指定将所有后⾯的 HTP 请求定向到指定的代理 web_set_proxy_bypas 指定 Vuser 直接访问（即不通过指定的代理 访问）的服务器列表 web_set_proxy_bypas_local 指定 Vuser 对于本地 (Intranet) 地址是否应该避开代理 web_set_secure_proxy 指定将所有后⾯的 HTP 请求定向到 web_set_max_retries 设置操作步骤的最⼤重试次数 web_set_timeout 指定 Vuser 等待执⾏指定任务的最⻓时间 web_convert_param 将 HTML 参数转换成 URL 或纯⽂本 web_get_int_property 返回有关上⼀个 HTP 请求的特定信息 web_report_data_point 指定数据点并将其添加到测试结果中 web_set_option 在⾮ HTML 资源的编码、重定向和下载区域中设置 Web 选项 web_set_sockets_option 设置套接字的选项 参考资料 1． LoadRuner介绍与应⽤实例 ．2． 在软件测试⼯具中如何巧⽤LoadRuner的随机函数 ． 3． 细说LoadRuner参数化 ．

脚本

服务器 服务器

服务器 服务器

