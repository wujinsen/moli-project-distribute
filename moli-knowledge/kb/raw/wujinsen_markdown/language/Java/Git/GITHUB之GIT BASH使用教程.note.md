htp:/blog.lmlphp.com/archives/7/The_use_tutorial_of_git_bash_and_how_to_start_ with_github LMLPHP后院

原⽂链接 :

来⾃ :

写在前⾯

这篇⽂章写完后，感觉不是很满意，漏掉了⼀些常⽤的命令忘记写，如“git tag”，“git dif”，“git show”，“git log”，“git remote”等。但是作为教程，应该是越简单越好，⽂章太⻓，反⽽惹⼈讨 厌，这样⼀想，也就没有继续补充了。

GITHUB 是全球最出名的基于 GIT 的代码托管平台之⼀，可以免费的托管开源代码。作为⼀名软 件⼯程师，对代码的管理养成⼀个良好的习惯是⾮常重要的。本⼈将讲解如何使⽤ GIT BASH 管 理 GITHUB 中的代码库。

Git 是基于 Linux 内核开发的版本控制⼯具。与常⽤的版本控制⼯具 CVS,Subversion 等不同，它 采⽤了分布式版本库的⽅式，不必服务器端软件⽀持，使源代码的发布和交流极其⽅便。 Git 的速 度很快，这对于诸如 Linux kernel 这样的⼤项⽬来说⾃然很重要。 Git 最为出⾊的是它的分⽀、合 并、跟踪的能⼒。

申请 GITHUB 账号

关于 GITHUB账号如何申请，本⽂将不再描述。但是需要注意的⼀点是，国外的⽹站对密码的要 求都⾮常的⾼，密码设置的不够复杂可能导致⽆法注册提交。

安装 GIT 客户端

GITHUB 官⽹有上提供 GITHUB for Windows 应⽤程序，关于 GITHUB for Windows 的使⽤⾮常 的⽅便，图形界⾯同时也提供了 GIT Bash。要求必须是 Windows7 及以上的操作系统，如果您使 ⽤的 Windows XP 的系统，就只能安装其他 GIT 客户端了。

配置 SH KEYS

要使⽤ SH协议连接 GITHUB，⾸先需要⽣成 SH KEYS。⽣成的密钥是两个⽂件，⼀个公钥⼀个 私钥。公钥需要提交给GITHUB 官⽹您的账号中。关于如何⽣成 SH Keys，请看如下步骤示例：

检查 SSH keys

$ ls -al ~/.ssh

# Lists the files in your .ssh directory, if they exist

# ⽣成 keys

$ ssh-keygen -t rsa -C "your_email@example.com"

# Creates a new ssh key, using the provided email as a label

# Generating public/private rsa key pair.

# Enter file in which to save the key (/c/Users/you/.ssh/id_rsa): [Press enter]

然后按照提示输⼊密码，最后出现如下提示则说明⽣成成功。

Your identification has been saved in /c/Users/you/.ssh/id_rsa.

# Your public key has been saved in /c/Users/you/.ssh/id_rsa.pub.

# The key fingerprint is:

# 01:0f:f4:3b:ca:85:d6:17:a1:7d:f0:68:9d:f0:a2:db your_email@example.com

将⽣成的新秘钥加⼊ SSH 客户端

# start the ssh-agent in the background

$ ssh-agent -s

# Agent pid 59566

$ ssh-add ~/.ssh/id_rsa

复制公钥

复制公钥可以打开⽂件复制，也可以使⽤命令，建议使⽤命令复制，否则可能出现⽆法授权的问 题。如下命令：

$ clip < ~/.ssh/id_rsa.pub

# Copies the contents of the id_rsa.pub file to your clipboard

将公钥加⼊到 GITHUB 账号

登录 GITHUB 在 Setings->SH keys 菜单下添加，将剪切板的内容粘贴到 Key ⽂本框中，名称 可以随意填写。到现在为⽌，配置⼯作已经完成。

GIT BASH 命令详解 git init

初始化 GIT，只有初始化了以后才可以使⽤ GIT 相关命令。在初始化之前，可以先创建⼀个⽂件 夹。如下示例

$ mkdir lmlphp

$ cd lmlphp

$ git init

# git clone

获取远程项⽬，并下载到本地。远程库的地址在 GITHUB 项⽬中会有提供。下⾯是我测试时显示 的内容，若执⾏成功，则将显示同下⾯类似的内容。

C:\Users\May\Documents\GitHub\test> git clone git@github.com:leiminglin/LMLPHP.g

it

Cloning into 'LMLPHP'...

Warning: Permanently added 'github.com,192.30.252.128' (RSA) to the list of know

n hosts.

remote: Counting objects: 210, done.

remote: Total 210 (delta 0), reused 0 (delta 0)

Receiving objects: 100% (210/210), 66.48 KiB | 15.00 KiB/s, done.

Resolving deltas: 100% (102/102), done.

Checking connectivity... done.

C:\Users\May\Documents\GitHub\test>

# git branch

git branch 命令⽤于创建分⽀，查看分⽀。查看分⽀可以使⽤参数-a,-v,-r等，a代表所有，v代表 版本信息，r 代表显示远程分⽀。下⾯的例⼦使⽤“git branch develop”创建了⼀个新的分⽀。

C:\Users\May\Documents\GitHub\test> cd .\LMLPHP

C:\Users\May\Documents\GitHub\test\LMLPHP [master]> git branch -av

* master 405960a session_write_close() when fatal error occured

remotes/origin/HEAD -> origin/master

remotes/origin/develop 405960a session_write_close() when fatal error occured

remotes/origin/master 405960a session_write_close() when fatal error occured

C:\Users\May\Documents\GitHub\test\LMLPHP [master]> git branch develop

C:\Users\May\Documents\GitHub\test\LMLPHP [master]> git branch -av

develop 405960a session_write_close() when fatal error occured

* master 405960a session_write_close() when fatal error occured

remotes/origin/HEAD -> origin/master

remotes/origin/develop 405960a session_write_close() when fatal error occured

remotes/origin/master 405960a session_write_close() when fatal error occured

C:\Users\May\Documents\GitHub\test\LMLPHP [master]>

# git checkout

git checkout 命令⽤于创建分⽀和切换分⽀。*号代表当前分⽀，下⾯通过 checkout 命令切换到 develop 分⽀。"checkout"在英⽂中的意思是检出，但是也不难理解，GIT 中分⽀其实就是⼀个指 向，速度很快；现在我的本地有两个分⽀，但是只有⼀份代码，当使⽤ checkout 命令切换分⽀并 且两个分⽀的内容不同时，你会发现磁盘上的⽂件内容即刻发⽣了变化。checkout 命令还可以⽤ 来创建分⽀并切换到这个分⽀，使⽤ checkout -b 参数即可，下⾯的例⼦使⽤此命令创建了 newFeature 分⽀并切换到了这个分⽀。

C:\Users\May\Documents\GitHub\test\LMLPHP [master]> git checkout develop

Switched to branch 'develop'

C:\Users\May\Documents\GitHub\test\LMLPHP [develop]> git branch -av

* develop 405960a session_write_close() when fatal error occured

master 405960a session_write_close() when fatal error occured

remotes/origin/HEAD -> origin/master

remotes/origin/develop 405960a session_write_close() when fatal error occured

remotes/origin/master 405960a session_write_close() when fatal error occured

C:\Users\May\Documents\GitHub\test\LMLPHP [develop]> git checkout -b newFeature

Switched to a new branch 'newFeature'

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]> git branch -av

develop 405960a session_write_close() when fatal error occured

master 405960a session_write_close() when fatal error occured

* newFeature 405960a session_write_close() when fatal error occured

remotes/origin/HEAD -> origin/master

remotes/origin/develop 405960a session_write_close() when fatal error occured

remotes/origin/master 405960a session_write_close() when fatal error occured

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]>

# git status

git status 命令⽤来查看当前分⽀状态。如下示例：

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]> git status

On branch newFeature

nothing to commit, working directory clean

# git pull

git pul 命令⽤来更新代码，该命令相当于 git fetch 和 git merge 的组合。需要注意的是，如果来 源是远程分⽀ develop，则必须这样写“origin develop”，origin 后⾯有个空格。如果远程分⽀存在 有和当前分⽀⼀样的名字，则可以不指定分⽀。如下示例：

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]> git pull

Warning: Permanently added 'github.com,192.30.252.130' (RSA) to the list of know

n hosts.

There is no tracking information for the current branch.

Please specify which branch you want to merge with.

See git-pull(1) for details

git pull

If you wish to set tracking information for this branch you can do so with:

git branch --set-upstream-to=origin/ newFeature

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]> git pull origin develop

Warning: Permanently added 'github.com,192.30.252.130' (RSA) to the list of know

n hosts.

From github.com:leiminglin/LMLPHP

* branch develop -> FETCH_HEAD

Already up-to-date.

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]>

# git add

git ad 命令⽤来增加更新的内容，后⾯的参数为⽬录名或者⽂件名，⼀般在 git comit 命令之前 使⽤。通过 git dif 可以查看有哪些不同之处，只有被增加的更新才会被提交到版本库。

# git commit

git comit 命令⽤来提交更新。更新时需要提交注释，使⽤ -m 参数。GITHUB 提供的 GIT 客户 端做的⾮常好，如果您忘记添加注释，它会弹出⽂本框让你填写。如下示例：

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]> git commit -m "test"

On branch newFeature

nothing to commit, working directory clean

# git push

git push 命令⽤来推送更新到远程库，此命令⼀般在 comit 命令之后使⽤。如果远程没有对应的 分⽀名，则需要通过设置参数“-set-upstream”指定提交到哪个分⽀。如下示例：

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]> git push

fatal: The current branch newFeature has no upstream branch.

To push the current branch and set the remote as upstream, use

git push --set-upstream origin newFeature

C:\Users\May\Documents\GitHub\test\LMLPHP [newFeature]> git push --set-upstream

origin develop

Warning: Permanently added 'github.com,192.30.252.130' (RSA) to the list of know

n hosts.

Branch develop set up to track remote branch develop from origin.

Everything up-to-date

GIT BASH 的功能⾮常强⼤，使⽤也⾮常⽅便，特别适合⼤型项⽬多⼈同时开发。图形界⾯⼯具虽 然⽅便简单，但是效率远远不能跟命令⾏相⽐。本⽂简单的介绍了 GIT BASH 的使⽤，更多的功 能请使⽤ -help 命令查看，系统会使⽤浏览器打开 HTML 版本⽂档，描述的⾮常详细。

如何使⽤ GIT BASH 同时管理多个远程库和推送更新到多个远程 GIT 服务端，请看我的另⼀篇⽂ 章《使⽤GIT BASH管理多个远程代码库》。

