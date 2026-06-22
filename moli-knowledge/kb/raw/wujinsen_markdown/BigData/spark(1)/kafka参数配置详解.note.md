# System # #唯 ⼀ 标 识 在 集 群 中 的 ID， 要 求 是 正 数 。 broker.id=0 #服 务 端 ⼝ ， 默 认 9092 port=9092 #监 听 地址 ，不 设 为 所 有 地址 host.name=debugo01 # 处 理 ⽹ 络 请 求 的 最 ⼤ 线 程 数 num.network.threads=2 # 处 理 磁 盘 I/O的 线 程 数 num.io.threads=8 # ⼀ 些 后台 线 程 数 background.threads

=

- 4 # 等 待 IO线 程 处 理 的 请 求 队 列 最 ⼤ 数 queued.max.requests


= 50 # socket的 发 送 缓 冲 区 （ SO_SNDBUF） socket.send.bufer.bytes=1048576 # socket的 接收 缓 冲 区 (SO_RCVBUF) socket.receive.bufer.bytes=1048576 # socket请 求 的 最 ⼤ 字 节 数 。为了 防 ⽌ 内 存 溢 出 ， mesage.max.bytes必 然 要 ⼩ 于 socket.request.max.bytes

= 10485760

# Topic # # 每 个 topic的 分 区 个 数 ， 更 多 的 partition会产 ⽣ 更 多 的 segment file num.partitions=2 # 是 否 允 许 ⾃ 动创 建 topic ， 若 是 false， 就 需 要 通过 命 令 创 建 topic auto.create.topics.enable

=true # ⼀ 个 topic ， 默 认 分 区 的 replication个 数 ，不 能 ⼤ 于 集 群 中 broker的 个 数 。 default.replication.factor

# =1 # 消 息 体 的 最 ⼤ ⼤ ⼩ ， 单 位 是 字 节

mesage.max.bytes

=

- 1 0 # ZoKeper #

# Zokeper quorum设 置 。如 果有 多 个 使 ⽤ 逗 号 分割

- zokeper.conect=debugo01:2181,debugo02,debugo03 # 连 接 zk的 超 时时 间 zokeper.conection.timeout.ms=1 0 # ZoKeper集 群 中 leader和 folower之 间 的 同 步 实 际 zokeper.sync.time.ms


=

- 2 0 # Log #

#⽇ 志 存 放 ⽬ 录 ， 多 个 ⽬ 录 使 ⽤ 逗 号 分割 log.dirs=/var/log/kafka # 当 达 到 下 ⾯ 的 消 息 数 量 时 ， 会 将 数据 flush到 ⽇ 志 ⽂ 件中 。默 认 1 0 #log.flush.interval.mesages=1 0 # 当 达 到 下 ⾯ 的 时 间 (ms)时 ， 执 ⾏ ⼀ 次 强 制 的 flush操 作 。interval.ms和 interval.mesages⽆ 论 哪 个 达 到 ， 都 会 flush。默 认 3 0ms #log.flush.interval.ms=1 0 # 检 查是 否 需 要 将 ⽇ 志 flush的 时 间间隔 log.flush.scheduler.interval.ms

=

- 3 0 # ⽇ 志 清 理 策 略 （ delete|compact） log.cleanup.policy


= delete # ⽇ 志 保 存 时 间 (hours|minutes)， 默 认 为 7天 （ 168⼩ 时 ） 。超 过这 个 时 间 会 根 据 policy处 理 数据 。 bytes和 minutes⽆ 论 哪 个 先 达 到 都 会 触 发 。 log.retention.hours=168 # ⽇ 志 数据 存 储 的 最 ⼤ 字 节 数 。超 过这 个 时 间 会 根 据 policy处 理 数据 。 #log.retention.bytes=1073741824 # 控 制 ⽇ 志 segment⽂ 件 的 ⼤ ⼩ ， 超 出 该 ⼤ ⼩ 则 追 加到 ⼀ 个 新 的 ⽇ 志 segment⽂ 件中 （ -1表 示 没 有 限 制 ） log.segment.bytes=536870912 # 当 达 到 下 ⾯ 时 间 ， 会 强 制 新 建 ⼀ 个 segment

log.rol.hours

= 24*7 # ⽇ 志 ⽚ 段 ⽂ 件 的 检 查 周 期 ， 查 看 它 们 是 否 达 到 了 删 除 策 略 的 设 置 （ log.retention.hours或 log.retention.bytes） log.retention.check.interval.ms=6 0 # 是 否 开 启压 缩 log.cleaner.enable=false

# 对 于 压 缩 的 ⽇ 志 保 留 的 最 ⻓ 时 间 log.cleaner.delete.retention.ms =

1 day # 对 于 segment⽇ 志 的 索 引 ⽂ 件 ⼤ ⼩ 限 制 log.index.size.max.bytes

= 10

- * 1024
- * 1024 #y索 引 计 算 的 ⼀ 个 缓 冲 区 ， ⼀ 般 不 需 要设 置 。 log.index.interval.bytes


= 4096

# replica # # partition management controler 与 replicas之 间 通 讯 的 超 时时 间 controler.socket.timeout.ms

=

- 3 0 # controler-to-broker-chanels消 息 队 列 的 尺 ⼨ ⼤ ⼩ controler.mesage.queue.size=10 # replicas响 应 leader的 最 ⻓ 等 待 时 间 ， 若 是 超 过这 个 时 间 ， 就将 replicas排 除 在 管 理 之 外 replica.lag.time.max.ms


= 1 0

# 是 否 允 许 控 制 器 关 闭 broker ,若 是 设 置 为 true,会 关 闭 所 有 在 这 个 broker上 的 leader， 并 转 移 到其 他 broker controled.shutdown.enable

= false # 控 制 器 关 闭 的 尝 试 次 数 controled.shutdown.max.retries

=

- 3 # 每次 关 闭 尝 试 的 时 间间隔 controled.shutdown.retry.backof.ms

= 5 0

# 如 果 relicas落 后 太多 ,将 会 认 为 此 partition relicas已 经 失 效 。⽽ ⼀ 般 情 况 下 ,因 为 ⽹ 络 延 迟 等 原 因 ,总 会 导 致 replicas中 消 息 同 步 滞 后 。如 果 消 息 严 重 滞 后 ,leader将 认 为 此 relicas⽹ 络 延 迟较 ⼤ 或 者 消 息 吞吐 能 ⼒ 有 限 。在 broker数 量 较 少 ,或 者 ⽹ 络 不 ⾜ 的 环 境 中 ,建 议 提 ⾼ 此 值 .

replica.lag.max.mesages

=

- 4 0 #leader与 relicas的 socket超 时时 间 replica.socket.timeout.ms= 30


- * 1 0 # leader复 制 的 socket缓 存 ⼤ ⼩ replica.socket.receive.bufer.bytes=64
- * 1024 # replicas每次 获 取 数据 的 最 ⼤ 字 节 数 replica.fetch.max.bytes


= 1024

* 1024 # replicas同 leader之 间 通 信 的 最 ⼤ 等 待 时 间 ， 失 败 了会 重 试 replica.fetch.wait.max.ms

=

50 # 每 ⼀ 个 fetch操 作 的 最 ⼩ 数据 尺 ⼨ ,如 果 leader中 尚 未 同 步 的 数据 不 ⾜ 此 值 ,将 会 等 待 直 到 数据 达 到 这 个 ⼤ ⼩ replica.fetch.min.bytes

=1 # leader中 进 ⾏ 复 制 的 线 程 数 ， 增 ⼤ 这 个 数 值 会 增 加 relipca的 IO num.replica.fetchers

=

- 1 # 每 个 replica将 最 ⾼ ⽔ 位 进 ⾏ flush的 时 间间隔 replica.high.watermark.checkpoint.interval.ms


=

- 5 0


# 是 否 ⾃ 动 平 衡 broker之 间 的 分 配 策 略 auto.leader.rebalance.enable

= false # leader的 不 平 衡 ⽐ 例 ， 若 是 超 过这 个 数 值 ， 会 对 分 区 进 ⾏ 重 新 的 平 衡 leader.imbalance.per.broker.percentage

= 10 # 检 查 leader是 否 不 平 衡 的 时 间间隔 leader.imbalance.check.interval.seconds

= 30 # 客 户 端 保 留 ofset信 息 的 最 ⼤ 空 间 ⼤ ⼩ ofset.metadata.max.bytes

= 1024

#Consumer # # Consumer端 核 ⼼ 的 配 置 是 group.id、zokeper.conect # 决 定 该 Consumer归 属 的 唯 ⼀ 组 ID， By seting the same group id multiple proceses indicate that they are al part of the same consumer group. group.id # 消 费 者 的 ID， 若 是 没 有 设 置 的 话 ， 会 ⾃ 增 consumer.id

# ⼀ 个 ⽤ 于 跟踪 调 查 的 ID ， 最 好 同 group.id相 同 client.id

= <group_id>

# 对 于 zokeper集 群 的 指 定 ， 必 须 和 broker使 ⽤ 同 样 的 zk配 置

- zokeper.conect=debugo01:2182,debugo02:2182,debugo03:2182 # zokeper的 ⼼ 跳超 时时 间 ， 查 过这 个 时 间 就 认 为 是 ⽆ 效 的 消 费 者 zokeper.sesion.timeout.ms


=

- 6 0 # zokeper的 等 待 连 接 时 间 zokeper.conection.timeout.ms


=

- 6 0 # zokeper的 folower同 leader的 同 步 时 间 zokeper.sync.time.ms


=

- 2 0 # 当 zokeper中 没 有 初 始 的 ofset时 ， 或 者 超 出 ofset上 限 时 的 处 理 ⽅ 式 。 # smalest ： 重 置 为 最 ⼩ 值 # largest:重 置 为 最 ⼤ 值 # anything else： 抛 出 异常 给 consumer auto.ofset.reset


= largest # socket的 超 时时 间 ， 实 际 的 超 时时 间 为 max.fetch.wait + socket.timeout.ms. socket.timeout.ms= 30

- * 1 0 # socket的 接收 缓 存 空 间 ⼤ ⼩ socket.receive.bufer.bytes=64
- * 1024 #从 每 个 分 区 fetch的 消 息 ⼤ ⼩ 限 制 fetch.mesage.max.bytes


= 1024

* 1024

# true时 ， Consumer会 在 消 费 消 息 后 将 ofset同 步 到 zokeper， 这 样 当 Consumer失 败 后 ， 新 的 consumer就 能 从 zokeper获 取 最 新 的 ofset auto.comit.enable

= true # ⾃ 动 提 交 的 时 间间隔 auto.comit.interval.ms

= 60

*

- 1 0

# ⽤ 于 消 费 的 最 ⼤ 数 量 的 消 息 块 缓 冲 ⼤ ⼩ ， 每 个 块 可 以 等 同 于 fetch.mesage.max.bytes中 数 值 queued.max.mesage.chunks

= 10 # 当 有 新 的 consumer加 ⼊ 到 group时 ,将尝 试 reblance,将 partitions的 消 费 端 迁 移 到 新 的 consumer中 , 该 设 置 是 尝 试 的 次 数 rebalance.max.retries

= 4 # 每次 reblance的 时 间间隔

rebalance.backof.ms

=

- 2 0 # 每次 重 新 选 举 leader的 时 间 refresh.leader.backof.ms


# # server发 送 到 消 费 端 的 最 ⼩ 数据 ， 若 是 不 满 ⾜ 这 个 数 值 则 会 等 待 直 到 满 ⾜ 指 定 ⼤ ⼩ 。默 认 为 1表 示 ⽴ 即 接收 。 fetch.min.bytes

1 # 若 是 不 满 ⾜ fetch.min.bytes时 ， 等 待 消 费 端 请 求 的 最 ⻓ 等 待 时 间 fetch.wait.max.ms

= 10 # 如 果 指 定 时 间 内 没 有 新 消 息 可 ⽤ 于 消 费 ， 就 抛 出 异常 ， 默 认 -1表 示 不 受 限 consumer.timeout.ms

= -1

#Producer #

# 核 ⼼ 的 配 置 包 括 ： # metadata.broker.list # request.required.acks # producer.type # serializer.clas # 消 费 者 获 取 消 息 元 信 息 (topics, partitions and replicas)的 地址 ,配 置 格 式 是 ： host1:port1,host2:port2， 也 可 以 在 外 ⾯ 设 置 ⼀ 个 vip metadata.broker.list

#消 息 的确 认 模 式

- # 0：不 保 证 消 息 的 到 达 确 认 ， 只 管 发 送 ， 低 延 迟 但 是 会 出 现 消 息 的 丢 失 ， 在 某 个 server失 败 的 情 况 下， 有 点 像 TCP
- # 1： 发 送 消 息 ， 并 会 等 待 leader 收 到 确 认 后 ， ⼀ 定 的 可 靠 性 # -1： 发 送 消 息 ， 等 待 leader收 到 确 认 ， 并 进 ⾏ 复 制 操 作 后 ， 才 返 回 ， 最 ⾼ 的 可 靠 性 request.required.acks


=

- 0

# 消 息 发 送 的 最 ⻓ 等 待 时 间 request.timeout.ms

=

- 1 0 # socket的 缓 存 ⼤ ⼩ send.bufer.bytes=10*1024 # key的 序 列化 ⽅ 式 ， 若 是 没 有 设 置 ， 同 serializer.clas key.serializer.clas # 分 区 的 策 略 ， 默 认 是 取 模


partitioner.clas=kafka.producer.DefaultPartitioner # 消 息 的 压 缩 模 式 ， 默 认 是 none， 可 以 有 gzip和 snapy compresion.codec

= none # 可 以 针 对 默 写 特 定 的 topic进 ⾏ 压 缩 compresed.topics=nul # 消 息 发 送 失 败 后 的 重 试 次 数 mesage.send.max.retries

=

- 3 # 每次 失 败 后 的 间隔 时 间 retry.backof.ms


= 10 # ⽣ 产 者 定 时更 新 topic元 信 息 的 时 间间隔 ， 若 是 设 置 为 0， 那 么会 在 每 个 消 息 发 送 后 都 去 更 新数据 topic.metadata.refresh.interval.ms

= 60

* 1 0 # ⽤ 户 随 意指 定 ， 但 是 不 能 重 复 ， 主 要 ⽤ 于 跟踪 记 录 消 息 client.id="

# 异 步模 式 下 缓 冲 数据 的 最 ⼤ 时 间 。例 如 设 置 为 10则 会 集 合 10ms内 的 消 息 后发 送 ， 这 样 会 提 ⾼ 吞吐 量 ， 但 是 会 增 加 消 息 发 送 的 延 时 queue.bufering.max.ms

=

- 5 0 # 异 步模 式 下 缓 冲 的 最 ⼤ 消 息 数 ， 同 上 queue.bufering.max.mesages


# = 1 0 # 异 步模 式 下， 消 息 进 ⼊ 队 列 的 等 待 时 间 。若 是 设 置 为 0， 则 消 息 不 等 待 ， 如 果 进 ⼊ 不 了 队 列 ， 则 直 接 被 抛 弃 queue.enqueue.timeout.ms

# -1 # 异 步模 式 下， 每次 发 送 的 消 息 数 ， 当 queue.bufering.max.mesages或 queue.bufering.max.ms满 ⾜ 条 件之 ⼀ 时 producer会 触 发发 送 。 batch.num.mesages=20

