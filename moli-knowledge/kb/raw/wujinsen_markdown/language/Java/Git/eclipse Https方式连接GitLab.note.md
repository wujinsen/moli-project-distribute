现在我们来把⼀个Eclipse⾥的项⽬放到GitLab⾥来管理` 让我们在eclipse⾥新建⼀个项⽬:

然后在GItlab上也新建⼀个项⽬.管理员⽤户在admin area⾥点new project`

输⼊项⽬名,选私有项⽬,创建

在这⾥就能看到新建项⽬的地址了,我们选HTP⽅式.记下这个地址,⼀会⼉eclipse连接要⽤.

现在还要给项⽬添加可⽤的⽤户,点项⽬的详情

新项⽬成员

选择要加⼊项⽬的⽤户

权限这⾥选的是developer，但是后⾯进⾏提交时，会报错误：

- 1 remote: GitLab: You are not allowed to access some of the refs!

- 2 ! [remote rejected] master -> master (pre-receive hook declined)

- 3


原因不明，所以这⾥最好选Master权限。到底啥原因还请明⽩⼈指点。 现在GitLab上的配置基本可⽤了。我们再来看Eclipse.在项⽬上右键，team->share project，选git

这⾥选中创建.git⽬录在项⽬的⽬录

点下⾯的创建.git⽬录

finish.现在项⽬已经加到本地的git管理了。看项⽬树已经有版本的标识了。

右键项⽬，team->comit，提交项⽬ 先选中所有⽂件，点Comit and Push，提交到本地并发布到服务器上，comit是只提交到本地。

在这⾥输⼊之前在GitLab上记下的项⽬HTP地址，下⾯填⽤户名密码。next

点source ref，选master,再点ad spec

可以不⽤选，选上它是强制提交force update，也就是说不管有没有冲突就覆盖前⾯的强制进⾏提交， 点finish

正常的话，项⽬就提交到本地并上传服务器了。

前⾯是提交本地及服务器，⾥⾯填的服务器信息只是对于这⼀次提交的， 每次提交不⽤都填信息，我们要给这个git项⽬添加⼀个固定的远程信息。 在Git Repositories窗⼝中，选Remotes,右键，create remotes

⽤默认的远程名

点change，这⾥和前⾯第⼀次comit and push⾥的配置⼀样，然后再点advanced，也与前⾯配置⼀ 致。

点save，就可以看到新建的远程地址。

之后再comit and push，就不⽤再填信息了。

这⾥就可以选择已配置的信息直接finish.

