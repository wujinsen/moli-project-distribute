# mysql数据 库 的 配 置 spring:

datasource: url: jdbc:mysql:/localhost: 306/moli?useSL=false&useUnicode=true&characterEncoding=utf-

8&autoReconect=true&serverTimezone=Asia/Shanghai driver-clas-name:com.mysql.cj.jdbc.Driver type:com.alibaba.druid.pol.DruidDataSource username: rot pasword: mWDQ34IPDq # druid 配 置 信 息 druid:

min-idle:10 max-active:20 initial-size:5 max-wait:6 0 min-evictable-idle-time-milis:6 0 max-evictable-idle-time-milis:9 0 validation-query: SELECT 1

servlet:

multipart: max-file-size: 10MB max-request-size: 10MB

# 彩 ⾊ 控 制 台 ⽇ 志 级 别 output: ansi:

enabled:always

jackson: date-format: y- M-d H: m:s time-zone: GMT+8

# Redis数据 源 redis:

host: localhost port:6379 pasword: p%6enCsUY8 timeout: 864 0 jedis:

pol: max-active:1 0# 连 接 池 最 ⼤ 连 接数 （ 使 ⽤ 负 值 表 示 没 有 限 制 ） max-wait: -1 # 连 接 池 最 ⼤ 阻 塞 等 待 时 间 （ 使 ⽤ 负 值 表 示 没 有 限 制 ）

max-idle:10 # 连 接 池 中 的 最 ⼤ 空 闲 连 接 min-idle:5 # 连 接 池 中 的 最 ⼩ 空 闲 连 接

database:1

mybatis-plus: # 配 置 映 射 ⽂ 件位 置 ， claspath指 resources maper-locations: claspath*:maper/*.xml# maper.xml⽂ 件 所 在 位 置 # 打 印 sql到 控 制 台 configuration:

log-impl:org.apache.ibatis.loging.stdout.StdOutImpl

# ⽇ 志 相 关 配 置 loging:

# 指 定 ⾃ 定 义 命名 的 配 置 ⽂ 件 config: claspath:logback-spring.xml

# pageHelper配 置 （ 官 ⽹ 推 荐 配 置 ） pagehelper:

# 分 ⻚ 插 件会 ⾃ 动 检 测 当 前 的 数据 库 链 接 ， ⾃ 动 选 择 合 适 的 分 ⻚ ⽅ 式 helperDialect: mysql # 分 ⻚ 合 理 化 参 数 ， 默 认 值 为 false。当 该 参 数 设 置 为 true 时 ， pageNum<=0 时 会 查 询 第 ⼀ ⻚ ， pageNum>pages（ 超 过 总

数 时 ）， 会 查 询 最 后 ⼀ ⻚ 。默 认 false 时 ， 直 接 根 据 参 数 进 ⾏ 查 询 。 reasonable: true # ⽀ 持 通过 Maper 接 ⼝ 参 数 来 传 递 分 ⻚ 参 数 ， 默 认 值 false， 分 ⻚ 插 件会从 查 询 ⽅ 法 的 参 数 值 中 ， ⾃ 动 根 据 上 ⾯ params 配

置 的 字 段 中 取 值 ， 查 找 到 合 适 的 值 时 就 会 ⾃ 动分 ⻚ 。 suportMethodsArguments: true # 为了 ⽀ 持 startPage(Object params)⽅ 法 ， 增 加 了 该 参 数 来 配 置 参 数 映 射 ， ⽤ 于从 对 象 中 根 据 属 性 名取 值 params: count=countSql

minio: url: htp:/localhost:9 0 acesKey: minioadmin secretKey: minioadmsin

swager: show: true

