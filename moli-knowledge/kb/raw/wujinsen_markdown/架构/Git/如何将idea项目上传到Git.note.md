Git是⼀款免费的分布式版本控制⼯具。每个⼈的电脑都是⼀个完整的版本库，那么我们该如何将⼀个 java项⽬上传到Git呢？

⼀、准备⼯作

- 1、Git下载及安装
- 2、GitHub注册账号
- 3、idea上创建java项⽬


htps:/jingyan.baidu.com/article/7f76dafba84f04101e1d0b0.html

htps:/github.com/

⼆、配置idea项⽬

- 1、配置idea
- 2、idea上创建本地仓库
- 3、项⽬变红，然后Git–>Ad

ad后变绿

- 4、提交到本地


三、配置Git

- 1、⽣成密钥：

右键–>Git Bash Here：先输⼊ sh-keygen –t rsa –C “邮箱地址”,注意 sh-keygen之间是没有空格的,其 他的之间是有空格的，邮箱地址是咱们在注册GitHub的时候⽤的邮箱。

⽣成的密钥在这⾥：

- 2、将id_rsa.pub⽤记事本打开，复制⾥⾯全部的内容，放在GitHub的 SH Keys上：
- 3、 sh –T git@github.com 验证设置是否成功：
- 4、设置⽤户名，邮箱：

git config –global user.name “⽤户名” git config –global user.email “邮箱”

- 5、GitHub上创建⼀个仓库：
- 6、将本地git项⽬上传到github上事先新建好的repository中：


进⼊⼯程⽂件夹所在⽬录，右键Git Init Here，出现.git⽂件，是有关配置等功能的，不⽤管。然后到 git bash here，依次输⼊以下命令：

git remote ad origin git@github.com:{github⽤户名}/{repository名}.git

git pul git@github.com:{github⽤户名}/{repository名}.git

- 7、ad comit push：


ad： 输⼊命令：ad . ad后⾯加了⼀个点，是想要提交所有⽂件，如果想提交指定的⽂件，可以写⽂件名，执⾏完增加命令 后，要执⾏提交命令，如下：

comit： 输⼊命令：git comit –m “taotao_v1.0版本”

push： 输⼊命令：git push git@github.com:12wanghongwei/taotao.git

配置到此完成。

之后再修改了代码，直接在idea中comit and push。

作者：种下星星的⽇⼦ 来源：CSDN 原⽂：htps:/blog.csdn.net/hongwei1573262364/article/details/78549315 版权声明：本⽂为博主原创⽂章，转载请附上博⽂链接！

