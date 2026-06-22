htps:/segmentfault.com/a/19 017207205?utm_source=wekly&utm_medium=email&utm_ca mpaign=email_wekly

# ⼿把⼿教你使⽤ VuePress 搭建个⼈博客

有阅读障碍的同学，可以跳过第⼀⾄四节，下载我写好的 : git clone https://github.com/zhangyunchencc/vuepress-devkit.git 然后从第五节开始看。

⼯具包

## ⼀、为什么你需要⼀个博客？

优秀的程序员都在写博客，写博客有很多好处：

帮助⾃⼰梳理、总结、理解知识点（个⼈提升）

帮助别⼈理解知识点（好⼈⼀⽣平安）

简历更好看，更多⾯试机会（升职加薪）

⼆、什么是 VuePress，为什么要使⽤ VuePress ？

VuePress 是尤⾬溪（vue.js 框架作者）4⽉12⽇发布的⼀个全新的基于 vue 的静态⽹站⽣成器，实际 上就是⼀个 vue 的 spa 应⽤，内置 webpack，可以⽤来写⽂档。详⻅ 其实类似的建站⼯具有很多，⽐如 WordPress、Jekyll、Hexo 等，其中 WordPress 需要⾃⼰购买虚拟 主机，不考虑；Jekyll 是 Github-Page 默认⽀持的，听说操作⽐较复杂，没有⽤过不做过多评价了； Hexo 之前⼀直在⽤，但⼀直觉得主题不好看，⻛格不够简洁优雅。⾃从遇⻅ VuePress，嗯，就是它 了~ VuePress 有很多优点：

VuePress中⽂⽹

界⾯简洁优雅（个⼈感觉⽐ HEXO 好看）

容易上⼿（半⼩时能搭好整个项⽬）

更好的兼容、扩展 Markdown 语法

响应式布局，PC端、⼿机端

Google Analytics 集成

⽀持 PWA

## 三、开始搭建

创建项⽬⽂件夹

可以右键⼿动新建，也可以使⽤ mkdir 命令新建：

mkdir vuepresBlogDemo

全局安装 VuePress

npminstal -g vuepres

进⼊ vuepressBlogDemo ⽂件夹，初始化项⽬

使⽤ npm init 或 npm init -y（默认yes）

npm init -y

创建⽂件夹和⽂件

在 vuepressBlogDemo ⽂件夹中创建 docs ⽂件夹，在 docs 中创建 .vuepress ⽂件夹，在.vuepress中 创建 public ⽂件夹和 config.js ⽂件，最终项⽬结构如下所示：

vuepresBlogDemo ├─── docs │ ├── README.md │ └── .vuepres │ ├── public │ └── config.js └── package.json

在 config.js ⽂件中配置⽹站标题、描述、主题等信息

module.exports = { title:'Chen\'s blog', description:'我的个⼈⽹站', head: [ / 注⼊到当前⻚⾯的 HTML <head> 中的标签

['link', {rel:'icon',href:'/logo.jpg' }], / 增加⼀个⾃定义的 favicon(⽹⻚标签的图标) ], base:'/', / 这是部署到github相关的配置 markdown: {

lineNumbers:false/ 代码块显示⾏号 }, themeConfig: {

nav:[ / 导航栏配置 {text:'前端基础',link:'/acumulate/' }, {text:'算法题库',link:'/algorithm/'}, {text:'微博',link:'htps:/baidu.com'}

], sidebar:'auto', / 侧边栏配置 sidebarDepth:2, / 侧边栏显示2级

} };

在 package.json ⽂件⾥添加两个启动命令

"scripts": { "dev":"vuepres dev docs", "build":"vuepres build docs"

}

⼀切就绪 跑起来看看吧

npmrun dev

## 四、⼀些⼩亮点

完成了基础搭建后，就可以在docs⽬录下新建 .md ⽂件写⽂章了（.md 是 Markdown 语法⽂件，你需 要知道 Markdown 的⼀些基本写法，很简单，这⾥给⼤家⼀份 ） 下⾯给⼤家安利⼀些实⽤的⽅法。

Markdown 语法整理⼤集合

代码块⾼亮

在 .md ⽂件中书写代码时，可在 ``` 后增加 js、html、json等格式类型，代码块即可按照指定类型⾼亮

⾃定义容器

代码：

: tip 提示 this is a tip :

: warning 注意 this is a tip

:

: danger 警告 this is a tip

:

效果：

⽀持Emoji

代码：

:tada: :10: :bambo: :gift_heart: :fire:

效果： 这⾥有⼀份

Emoji ⼤全

⽀持 PWA

VuePress 默认⽀持 ，配置⽅法如下： config.js ⽂件中增加

PWA

head: [ / 注⼊到当前⻚⾯的 HTML <head> 中的标签 ['link', {rel:'manifest',href:'/photo.jpg' }], ['link', {rel:'aple-touch-icon',href:'/photo.jpg' }],

], serviceWorker:true/ 是否开启 PWA

public ⽂件夹下新建 manifest.json ⽂件，添加

{

"name":"张三", "short_name":"张三", "start_url":"index.html", "display":"standalone", "background_color":"#2196f3", "description":"张三的个⼈主⻚", "theme_color":"blue", "icons": [

{

"src":"./photo.jpg", "sizes":"14x14", "type":"image/png"

} ], "related_aplications": [

{

"platform":"web" }, {

"platform":"play", "url":"htps:/play.gogle.com/store/aps/details?id=cheaun.hackerweb"

} ]

}

最后在 iPhone 的 safrai 浏览器中打开本⽹站，点击 +添加到主屏幕 就能在桌⾯看到⼀个像原⽣ App ⼀ 样的图标（感觉⾃⼰写了⼀个 App 有⽊有 ）

## 五、部署上线

说了这么多都是在本地进⾏的，现在我们要把本地的内容推送到某个服务器上，这样只要有⽹络，就 可以随时随地看⾃⼰的⽹站了。 ⼀般来说，有两种⽅案可供选择：

- 1.
- 2.


⾃⼰买⼀个服务器，阿⾥云、腾讯云等，这种⽅式的好处是速度有保证、可以被搜索引擎收录， 坏处是要花钱啊 ⼟豪同学可以考虑。 使⽤ 。什么是 Github Pages 呢？简单说就是 Github 提供的、⽤于搭建个⼈⽹站的 静态站点托管服务。很多⼈⽤它搭建个⼈博客。这种⽅式的好处是免费、⽅便，坏处是速度可能 会有些慢、不能被国内的搜索引擎收录。

Github Pages

最终我选择了⽅案2，下⾯将给⼤家讲解如何使⽤ Github Pages 服务。

Github

登陆

打开 github ⽹站，登陆⾃⼰的 github 账号（没有账号的快去注册并⾯壁思过作为⼀个优秀的程序员为 啥连⼀个github账号都没有） 接着我们新建两个仓库，

新建仓库⼀： USERNAME.github.io （不⽤克隆到本地）

注意！

USERNAME 必须是你 Github 的账号名称，不是你的名字拼⾳，也不是你的⾮主流⽹名，不要瞎起，要保 证和Github账号名⼀模⼀样！ 例如我的 Github 账号名称是 zhangyunchencc 那么新建仓库，Repository name 就填写为：zhangyunchencc.github.io 这个仓库建好后，不⽤克隆到本地，内容更新修改都在下⾯的仓库中进⾏。

新建仓库⼆：随便起⼀个名字，⽐如：vuepressBlog （克隆到本地）

这个项⽬是⽤来开发博客的，以后只需要改这个项⽬就够了。

使⽤⼯具包的，将 vuepress-devkit 中的内容拷⻉到 vuepressBlog ⽂件夹中

⾃⼰从头搭建的，将 vuepressBlogDemo ⽂件夹的内容拷⻉到仓库⼆，并在根⽬录下创建 deploy.sh ⽂件，内容如下：

#!/usr/bin/env sh

# 确保脚本抛出遇到的错误 set -e

# ⽣成静态⽂件 npm run build

# 进⼊⽣成的⽂件夹 cd docs/.vuepres/dist

# 如果是发布到⾃定义域名 # echo ' w.yourwebsite.com' > CNAME

git init git ad -A git co mit -m'deploy'

# 如果你想要部署到 htps:/USERNAME.github.io git push -f git@github.com:USERNAME/USERNAME.github.io.git master

# 如果发布到 htps:/USERNAME.github.io/<REPO> REPO=github上的项⽬ # git push -f git@github.com:USERNAME/<REPO>.git master:gh-pages

cd -

修改仓库⼆中的 deploy.sh 发布脚本

把⽂件中的 USERNAME 改成 Github 账号名，例如我的账号名是 zhangyunchencc，那么就可以改 为：

# 如果你想要部署到 htps:/USERNAME.github.io git push -f git@github.com:zhangyunchenc/zhangyunchenc.github.io.git master

这样仓库⼆和仓库⼀就建⽴了关联。 简单说⼆者的关系是：仓库⼀负责显示⽹站内容，我们不需要改动它；⽇常开发和新增内容，都在仓 库⼆中，并通过 npm run deploy 命令，将代码发布到仓库⼀。

在 package.json ⽂件夹中添加发布命令（使⽤⼯具包的请忽略）

"scripts": {

"deploy":"bash deploy.sh" }

⼤功告成，运⾏发布命令

npmrun deploy

此时打开 Github Settings 中下⾯的链接: 即可看到⾃⼰的主⻚啦~

https://zhangyunchencc.github.io/

PC 端⻚⾯是这样的： ⼿机端⻚⾯是这样的：

## 六、发布到⾃⼰的个⼈域名

https://zhangyunchencc.github... h ttp://www.zhangyunchen.cc/ http://www.ruanyifeng.com/blog/

如果你不满⾜于 这样的域名，想要⼀个⾃⼰个⼈的专属域名，⽐如

，毕竟⼀些⼤⽜（阮⼀峰 ） 都是⾃⼰名 字的⽹址哦，很⽅便很酷呢 😎 下⾯跟着步骤⼀步步来就好啦~

购买域名

新⽹ 万⽹

推荐在 或 购买 我是在新⽹购买的，下⾯以新⽹为例，万⽹是类似的。 购买完成后进⼊管理后台，点击 ”解析“ 按钮，添加下⾯两条内容： 注意这⾥有坑！！！在 万⽹ 购买域名的同学请注意，第⼆条记录中的 请 ⽤ @ 代 替 ，万 ⽹ 不 ⽀ 持 记录值⾥的 IP 可以通过 ping ⾃⼰的域名得到：

ping w.username.github.io

添加 CNAME ⽂件

在仓库⼀ USERNAME.github.io 中找到 Settings > Custom domain 把 www.zhangyunchen.cc 添加进 去即可。

⼤功告成，打开 看⼀下吧~~~

https://www.zhangyunchen.cc

## 七、最后

你需要⼀些 Markdown 语法的基础知识；

你需要⼀个 Github 账号，并在⾥⾯创建两个 repo

Github 需要添加 ssh key，遇到问题可以百度解决；

个⼈博客不只可以⽤来写技术相关的内容，也可以有⾃⼰写的⽂章、随笔，甚⾄上传⼀些照⽚。

以上， Chen | 2018.10

