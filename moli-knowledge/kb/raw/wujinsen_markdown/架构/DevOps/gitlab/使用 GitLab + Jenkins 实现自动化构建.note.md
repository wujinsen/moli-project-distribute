htps:/ w.jianshu.com/p/7e8037c63d63

在测试环境部署这块, 经历过以下⼏个阶段:

阶段⼀

有⼀台测试服务器把项⽬放上⾯测试, 当初也没有什么相关的经验, 每次改完代码本地打包上传到服务 器上, 然后⼀顿命令启动项⽬, 完成了最原始的部署。

这种⽅式构建和部署全靠⼈⾁, 项⽬简单的时候还好说, 项⽬⼀多配置⼀多 ( ⽐如微服务 ) 中间哪个环节 粗⼼出点错那简直就是灾难。

同时还要专⻔有⼈对运维相关的技术⽐较了解, 不然哪天我不在测试⼯作就完全停滞了。

阶段⼆

既然都是重复⼯作, 那就整理下步骤写个脚本

从 SVN 拉代码

Maven 构建打包

重启 Tomcat

每次执⾏下脚本就搞定了。看着挺不错的, 不过实际执⾏时的情况总会复杂许多 ( 服务器帐号权限、测 试⼈员对 Linux 的熟悉程度、项⽬启动依赖复杂等等问题 ) 。

阶段三

了解到 Jenkins 是个不错的⼯具, 那就把脚本的内容迁移到 Jenkins 上, 不管是开发还是测试只要在 web 界⾯上点击⼀下按钮即可完成构建部署, 很 easy。

阶段四

容器化: 使⽤ Docker 来部署项⽬, 这样就可以⼲掉原来服务器上散落各地参差不⻬的 Tomcat ( 不同项 ⽬依赖不同 ) , 利⽤ Docker Compose 对项⽬进⾏编排, 提供⼀种规范的构建配置 ( 同时也是⼀份⽂档 ) , ⼤⼤减⼩了后期维护和交接的成本。

阶段五

上⾯的阶段已经能解决⽇常需求了, 但是还有⼀点问题就是每次提交完代码还要⼿动去 Jenkins 上发布, 能更⾃动点就更好了 ( 嗯, 就是懒 ) 。

于是就引出了本⽂的⽬标 ⸺ ⾃动持续构建, 不需要⼈⼯操作 ( 留⼈⼯操作⽤于处理特殊情况 )

⽅案流程

流程图 开发提交代码

开发对需要发布的版本打上 Tag

触发 GitLab 的 tag push 事件, 调⽤ Webhok

Webhok 触发 Jenkins 的构建任务

Jenkins 构建完项⽬可以按版本号上传到仓库、部署、通知相关⼈员等等

安装 GitLab

GitLab 官⽅⽂档 已经介绍的⽐较详细了, 这⾥不再赘述, 下⾯给出最终调整过的 Docker Compose 配 置:

gitlab: image: "twang218/gitlab-ce-zh:1.0.2" restart: always hostname: 'gitlab' ports:

- - "102  2"
- - "1080 1080"


- # postgresql 端⼝
- - "5432 5432"


volumes:

- - ./gitlab/data:/var/opt/gitlab
- - ./gitlab/log:/var/log/gitlab
- - ./gitlab/config:/etc/gitlab


environment:

GITLAB_OMNIBUS_CONFIG: | # 仓库路径, 填写宿主机的域名或 IP external_url 'htp:/192.168. x. x:1080' # 调整⼯作进程数减⼩内存占⽤，最⼩为 2 unicorn['worker_proceses'] = 2 gitlab_rails['time_zone'] = 'Asia/Shanghai'

# 邮箱配置 gitlab_rails['gitlab_email_from'] = '<your_email>' gitlab_rails['gitlab_email_display_name'] = '<your_email_name>' gitlab_rails['smtp_enable'] = true gitlab_rails['smtp_adres'] = 'smtp.163.com' gitlab_rails['smtp_port'] = 25 gitlab_rails['smtp_user_name'] = "<your_email_acount>" gitlab_rails['smtp_pasword'] = "<your_email_pasword>" gitlab_rails['smtp_domain'] = 'smtp.163.com' gitlab_rails['smtp_tls'] = false gitlab_rails['smtp_opensl_verify_mode'] = 'none' gitlab_rails['smtp_enable_startls_auto'] = false gitlab_rails['smtp_sl'] = false gitlab_rails['smtp_force_sl'] = false

# 数据库配置 gitlab_rails['db_host'] = '127.0.0.1' gitlab_rails['db_port'] = 5432 gitlab_rails['db_username'] = "gitlab" gitlab_rails['db_pasword'] = "gitlab"

postgresql['listen_adres'] = '0.0.0.0' postgresql['port'] = 5432

postgresql['md5_auth_cidr_adreses'] = %w() postgresql['trust_auth_cidr_adreses'] = %w(0.0.0.0/0) postgresql['sql_user'] = "gitlab" postgresql['sql_user_pasword'] = Digest:MD5.hexdigest "gitlab" < postgresql['sql_user']

# 备份设置-保留7天 gitlab_rails['backup_kep_time'] = 60480

GITLAB_BACKUPS: "daily" GITLAB_SIGNUP: "true" GITLAB_ROT_PASWORD: "lb80h&85" GITLAB_GRAVATAR_ENABLED: "true"

说明：

这⾥使⽤ 汉化版 镜像, 如果不适应可以换回 官⽅原版 镜像 gitlab/gitlab-ce:1.0.2-ce.0

项⽬初始配置 + 启动很慢, 需要⼀段时间, ⽇志中出现 Reconfigured 时表示启动成功

192.168. x. x 替换为宿主机的 IP 地址

初始管理员帐号密码: rot / lb80h&85 ( ⾃⾏修改配置⽂件中的密码 )

该配置为 乞丐版 , 内存占⽤ 2G+ ( worker_proceses 越多内存占⽤越⼤, 默认为 8G )

postgresql 为容器中内置的数据库 ( 帐号: gitlab / gitlab ) , 没必要就不⽤暴露端⼝了

邮箱填写⽤于发送找回密码和通知的发件⼈帐号 ( 收不到邮件? ) , 不想配置就删掉相关配置好了, 不影 响正常使⽤

安装 Jenkins

为了测试⽅便, 使⽤ Docker 化的 Jenkins , 如果需要调⽤⼀些特殊的命令或脚本就不是很⽅便, 实际使 ⽤过程中可以换成普通版的。

Docker Compose 配置如下:

version: '3' services:

jenkins: image: jenkins/jenkins:2.151 container_name: jenkins networks:

- - net user: "rot" restart: always ports:
- - 9 0 8080 environment:
- - JAVA_OPTS="-Duser.timezone=Asia/Shanghai" volumes:
- - /etc/localtime:/etc/localtime:ro
- - ./data:/var/jenkins_home:rw
- - ./backup:/var/jenkins_backup:rw


# ⽹络配置 networks:

net: driver: bridge ipam:

driver: default config:

- subnet: 172.23.0.0/16 说明：

初次启动请打印⽇志, ⽇志中有管理员帐号的初始密码, 第⼀次登陆的时候需要⽤到

为了⽅便, 使⽤ rot 帐号启动容器, 如果使⽤默认帐号启动需要修改本地⽬录 ( data、backup ) 的权限

配置 GitLab

注册账号什么的就不赘述了, 建⼀个测试项⽬ test , 随便 comit ⼏条内容

新建测试项⽬ 按下图步骤创建账号的 aces token , ⽤于 Jenkins 调⽤ GitLab 的 API

创建 aces token

获取 aces token 记下这⾥⽣成的 aces token ( gRCtwVWU8cxwHdxVZJD ) , 后⾯要⽤到。

配置 Jenkins

安装插件 ( 安装过程可能会失败,多试⼏次就好了 )

Git Parameter ( ⽤于参数化构建中动态获取项⽬分⽀ )

Generic Webhok Tri ger ( ⽤于解析 Webhok 传过来的参数 )

GitLab ( ⽤于推送构建结果给 GitLab )

添加 GitLab 凭据

⾸⻚ -> 凭据 -> 系统 -> 全局凭据 -> 添加凭据, 把上⾯ GitLab 中⽣成的 aces token 填进去 配置 GitLab 连接

⾸⻚ -> 系统管理 -> 系统设置 -> Gitlab 配置项, 填⼊ GitLab 相关的配置, 后⾯配置项⽬时⽤到 新建项⽬ test

Jenkins项⽬完整配置 勾选 参数化构建过程, 添加 Git Parameter 类型的参数 ref , 这样构建的时候就可以指定分⽀进⾏构建。

Source Code Management 选择 Git , 添加项⽬地址和授权⽅式 ( 帐号密码 或者 sh key ) , 分⽀填写 构建参数 $ref。

Build Tri gers 选择 Generic Webhok Tri ger ⽅式⽤于解析 GitLab 推过来的详细参数 ( jsonpath 在 线测试 ) 。其他触发⽅式中: Tri ger builds remotely 是 Jenkins ⾃带的, Build when a change is pushed to GitLab 是 GitLab 插件 提供的, 都属于简单的触发构建, ⽆法做复杂的处理。

虽然 Generic Webhok Tri ger 提供了 Token 参数进⾏鉴权, 但为了避免不同项⽬进⾏混调 ( ⽐如 A 项⽬提交代码却触发了 B 项⽬的构建) , 还要对请求做下过滤。Optional filter 中 Text 填写需要校验的 内容 ( 可使⽤变量 ) , Expresion 使⽤正则表达式对 Text 进⾏匹配, 匹配成功才允许触发构建。

Build 内容按⾃⼰实际的项⽬类型进⾏调整, 使⽤ Maven 插件 或 脚本 等等。

GitLab Conection 选择上⾯添加的 GitLab 连接 ( Jenkins ) , Post-build Actions 添加 Publish build status to GitLab 动作, 实现构建结束后通知构建结果给 GitLab。

回到 GitLab 的项⽬⻚⾯中, 添加⼀个 Webhok ( htp:/JENKINS_URL/generic-webhoktri ger/invoke?token=<上⾯ Jenkins 项⽬配置中的 token> ) , 触发器选择 标签推送事件。因为⽇常开 发中 push 操作⽐较频繁⽽且不是每个版本都需要构建, 所以只针对需要构建的版本打上 Tag 就好了。

gitlab添加 Webhok 创建完使⽤ test 按钮 先测试下, 可能会出现下⾯的错误

Hok execution failed: URL 'htp:/192.168. x. x:9 0/generic-webhok-tri ger/invoke? token=d63ad84eb18cb04d459ec347a196dce' is blocked: Requests to the local network are not alowed 解决办法: 允许 GitLab 本地⽹络发送 Webhok 请求

测试效果

可以在 GitLab 直接添加 Tag , 不过我觉得⽤ IDEA 上操作更⽅便点, 就把代码拉下来在本地操作

针对每个comit添加tag 然后使⽤快捷键 Ctrl + Shift + K 调出 Push 窗⼝ , 把 Tag 推送到 GitLab 中

push tag 回到 GitLab ⻚⾯可以看到触发了 Webhok , View details 查看请求详情, Response body 中 tri gered 字段值为 true 则表示成功触发了 Jenkins 进⾏构建

Webhok 触发历史

再看下构建结果

gitlab 查看构建结果 — 流⽔线

gitlab 中查看构建结果 — comits 注意: 每添加⼀个 Tag 就会触发⼀次事件, 不管是不是⼀起 push 的。所以⼀次 push 多个 Tag 会触发 Jenkins 进⾏多次构建。不过 Jenkins 已经做了处理, 默认串⾏执⾏任务 ( ⼀个任务结束再执⾏下⼀个 ) , ⽽且在构建前有⼀个 pending 状态, 此时被多次触发会进⾏合并, 并取⾸次触发的参数, 如下图所示:

同时触发多次事件 关于 Tag 的⼏点说明

推送 Tag 到远端的时候, 远端已存在 ( 同名 ) 的 Tag 不会被添加到远端

拉取远端的 Tag 时, 本地已存在 ( 同名 ) 的 Tag 不会添加到本地

拉取远端的 Tag 时, 本地不会删除远端已删除的 Tag , 需要同步远端的 Tag 可以先删除本地所有 Tag 再 pul

删除 Tag 也会推送事件, 要做好过滤 ( 上⾯配置中已使⽤ comitsId 字段进⾏过滤 )

未完待续

通过上⾯的步骤已经初步实现了想要的效果, 还有⼏个点后续可以再考虑下：

上⽂只包含⾃动构建的内容, 对于项⽬的部署可以考虑⼏种⽅式: ⼿动选择指定的版本进⾏发布、构建 任务结束后直接触发部署任务、定时部署最新版本 ( 根据实际需求调整 )。

测试发版的频率会⽐较⾼, 会⽣成⼤量的 Tag , 可以约定 Tag 的格式, ⽐如 test 0.0.1 表示触发测试环境 的项⽬构建, online 1.0.0 表示触发正式版本构建, 隔离之后可以⽅便后续的维护和清理。

构建部分可以整合 Docker , 把构建结果打包到 Docker 镜像中 ( 代码版本库的 Tag 正好可以作为镜像 的 Tag ) , 再上传到 Docker 镜像仓库 ( 私服 或者第三⽅仓库 ) 中, 后续部署就可以直接从镜像仓库拉取 镜像直接运⾏了。

集成⾃动化测试 , ⽐如 这个

尝试配置 GitLab ⾃带的 CI / CD

总结

以上就是对曾经踩过的⼀些坑进⾏的整合, 也没什么好总结的。总之, 合理地利⽤现有⼯具来解放双⼿, 就能有更多时间做其他想做的事!

时间有限⼀些基础的步骤就不细讲直接⼀笔带过了, ⽅案上可能有些细节⽅⾯也没考虑全, 欢迎评论留 ⾔。

转载请注明出处：htps:/ w.jianshu.com/p/7e8037c63d63

作者：anyesu 链接：htps:/ w.jianshu.com/p/7e8037c63d63 来源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

