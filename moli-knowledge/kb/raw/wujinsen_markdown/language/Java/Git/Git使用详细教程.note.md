⼀：Git是什么？ Git是⽬前世界上最先进的分布式版本控制系统。 ⼆：SVN与Git的最主要的区别？ SVN是集中式版本控制系统，版本库是集中放在中央服务器的，⽽⼲活的时候，⽤的都是⾃⼰的 电脑，所以⾸先要从中央服务器哪⾥得到最新的版本，然后⼲活，⼲完后，需要把⾃⼰做完的活推送 到中央服务器。集中式版本控制系统是必须联⽹才能⼯作，如果在局域⽹还可以，带宽够⼤，速度够 快，如果在互联⽹下，如果⽹速慢的话，就纳闷了。

Git是分布式版本控制系统，那么它就没有中央服务器的，每个⼈的电脑就是⼀个完整的版本库， 这样，⼯作的时候就不需要联⽹了，因为版本都是在⾃⼰的电脑上。既然每个⼈的电脑都有⼀个完整 的版本库，那多个⼈如何协作呢？⽐如说⾃⼰在电脑上改了⽂件A，其他⼈也在电脑上改了⽂件A，这 时，你们两之间只需把各⾃的修改推送给对⽅，就可以互相看到对⽅的修改了。

三：在windows上如何安装Git？ msysgit是 windows版的Git,如下：

![image 1](<Git使用详细教程.note_images/imageFile1.png>)

需要从⽹上下载⼀个，然后进⾏默认安装即可。安装完成后，在开始菜单⾥⾯找到 "Git -> Git Bash",如下：

![image 2](<Git使用详细教程.note_images/imageFile2.png>)

会弹出⼀个类似的命令窗⼝的东⻄，就说明Git安装成功。如下：

![image 3](<Git使用详细教程.note_images/imageFile3.png>)

安装完成后，还需要最后⼀步设置，在命令⾏输⼊如下：

![image 4](<Git使用详细教程.note_images/imageFile4.png>)

因为Git是分布式版本控制系统，所以需要填写⽤户名和邮箱作为⼀个标识。 注意：git config -global 参数，有了这个参数，表示你这台机器上所有的Git仓库都会使⽤这个

配置，当然你也可以对某个仓库指定的不同的⽤户名和邮箱。 四：如何操作？ ⼀：创建版本库。 什么是版本库？版本库⼜名仓库，英⽂名repository,你可以简单的理解⼀个⽬录，这个⽬录⾥⾯的

所有⽂件都可以被Git管理起来，每个⽂件的修改，删除，Git都能跟踪，以便任何时刻都可以追踪历 史，或者在将来某个时刻还可以将⽂件”还原”。

所以创建⼀个版本库也⾮常简单，如下我是D盘 –> w下 ⽬录下新建⼀个testgit版本库。

![image 5](<Git使用详细教程.note_images/imageFile5.png>)

pwd 命令是⽤于显示当前的⽬录。

1. 通过命令 git init 把这个⽬录变成git可以管理的仓库，如下：

![image 6](<Git使用详细教程.note_images/imageFile6.png>)

这时候你当前testgit⽬录下会多了⼀个.git的⽬录，这个⽬录是Git来跟踪管理版本的，没事千万不 要⼿动乱改这个⽬录⾥⾯的⽂件，否则，会把git仓库给破坏了。如下：

![image 7](<Git使用详细教程.note_images/imageFile7.png>)

2. 把⽂件添加到版本库中。 ⾸先要明确下，所有的版本控制系统，只能跟踪⽂本⽂件的改动，⽐如txt⽂件，⽹⻚，所有程序

的代码等，Git也不列外，版本控制系统可以告诉你每次的改动，但是图⽚，视频这些⼆进制⽂件，虽 能也能由版本控制系统管理，但没法跟踪⽂件的变化，只能把⼆进制⽂件每次改动串起来，也就是知 道图⽚从1kb变成2kb，但是到底改了啥，版本控制也不知道。

下⾯先看下demo如下演示： 我在版本库testgit⽬录下新建⼀个记事本⽂件 readme.txt 内容如下： 1 第⼀步：使⽤命令 git ad readme.txt添加到暂存区⾥⾯去。如下：

![image 8](<Git使用详细教程.note_images/imageFile8.png>)

如果和上⾯⼀样，没有任何提示，说明已经添加成功了。 第⼆步：⽤命令 git comit告诉Git，把⽂件提交到仓库。

![image 9](<Git使用详细教程.note_images/imageFile9.png>)

现在我们已经提交了⼀个readme.txt⽂件了，我们下⾯可以通过命令git status来查看是否还有⽂ 件未提交，如下：

![image 10](<Git使用详细教程.note_images/imageFile10.png>)

说明没有任何⽂件未提交，但是我现在继续来改下readme.txt内容，⽐如我在下⾯添加⼀⾏ 2内容，继续使⽤git status来查看下结果，如下：

![image 11](<Git使用详细教程.note_images/imageFile11.png>)

上⾯的命令告诉我们 readme.txt⽂件已被修改，但是未被提交的修改。 接下来我想看下readme.txt⽂件到底改了什么内容，如何查看呢？可以使⽤如下命令： git dif readme.txt 如下：

![image 12](<Git使用详细教程.note_images/imageFile12.png>)

如上可以看到，readme.txt⽂件内容从⼀⾏ 1改成 ⼆⾏ 添加了⼀⾏ 2内容。 知道了对readme.txt⽂件做了什么修改后，我们可以放⼼的提交到仓库了，提交修改和提交⽂件

是⼀样的2步(第⼀步是git ad 第⼆步是：git comit)。 如下：

![image 13](<Git使用详细教程.note_images/imageFile13.png>)

⼆：版本回退： 如上，我们已经学会了修改⽂件，现在我继续对readme.txt⽂件进⾏修改，再增加⼀⾏ 内容为 3.继续执⾏命令如下：

![image 14](<Git使用详细教程.note_images/imageFile14.png>)

现在我已经对readme.txt⽂件做了三次修改了，那么我现在想查看下历史记录，如何查呢？我们 现在可以使⽤命令 git log 演示如下所示：

![image 15](<Git使用详细教程.note_images/imageFile15.png>)

git log命令显示从最近到最远的显示⽇志，我们可以看到最近三次提交，最近的⼀次是,增加内容 为 3.上⼀次是添加内容 2，第⼀次默认是 1.如果嫌上⾯显示的信息太多的话，我们可 以使⽤命令 git log –prety=oneline 演示如下：

![image 16](<Git使用详细教程.note_images/imageFile16.png>)

现在我想使⽤版本回退操作，我想把当前的版本回退到上⼀个版本，要使⽤什么命令呢？可以使 ⽤如下2种命令，第⼀种是：git reset -hard HEAD^ 那么如果要回退到上上个版本只需把HEAD^ 改 成 HEAD^ 以此类推。那如果要回退到前10个版本的话，使⽤上⾯的⽅法肯定不⽅便，我们可以使 ⽤下⾯的简便命令操作：git reset -hard HEAD~10 即可。未回退之前的readme.txt内容如下：

![image 17](<Git使用详细教程.note_images/imageFile17.png>)

如果想回退到上⼀个版本的命令如下操作：

![image 18](<Git使用详细教程.note_images/imageFile18.png>)

再来查看下 readme.txt内容如下：通过命令cat readme.txt查看

![image 19](<Git使用详细教程.note_images/imageFile19.png>)

可以看到，内容已经回退到上⼀个版本了。我们可以继续使⽤git log 来查看下历史记录信息，如 下：

![image 20](<Git使用详细教程.note_images/imageFile20.png>)

我们看到 增加 3 内容我们没有看到了，但是现在我想回退到最新的版本，如：有 3的 内容要如何恢复呢？我们可以通过版本号回退，使⽤命令⽅法如下：

git reset -hard 版本号 ，但是现在的问题假如我已经关掉过⼀次命令⾏或者 3内容的版本号我 并不知道呢？要如何知道增加 3内容的版本号呢？可以通过如下命令即可获取到版本号：git reflog 演示如下：

![image 21](<Git使用详细教程.note_images/imageFile21.png>)

通过上⾯的显示我们可以知道，增加内容 3的版本号是 6fcfc89.我们现在可以命令 git reset -hard 6fcfc89来恢复了。演示如下：

![image 22](<Git使用详细教程.note_images/imageFile22.png>)

可以看到 ⽬前已经是最新的版本了。 三：理解⼯作区与暂存区的区别？ ⼯作区：就是你在电脑上看到的⽬录，⽐如⽬录下testgit⾥的⽂件(.git隐藏⽬录版本库除外)。或

者以后需要再新建的⽬录⽂件等等都属于⼯作区范畴。

版本库(Repository)：⼯作区有⼀个隐藏⽬录.git,这个不属于⼯作区，这是版本库。其中版本库⾥ ⾯存了很多东⻄，其中最重要的就是stage(暂存区)，还有Git为我们⾃动创建了第⼀个分⽀master,以 及指向master的⼀个指针HEAD。

我们前⾯说过使⽤Git提交⽂件到版本库有两步： 第⼀步：是使⽤ git ad 把⽂件添加进去，实际上就是把⽂件添加到暂存区。 第⼆步：使⽤git comit提交更改，实际上就是把暂存区的所有内容提交到当前分⽀上。 我们继续使⽤demo来演示下：

我们在readme.txt再添加⼀⾏内容为 4，接着在⽬录下新建⼀个⽂件为test.txt 内容为 test，我们先⽤命令 git status来查看下状态，如下：

![image 23](<Git使用详细教程.note_images/imageFile23.png>)

现在我们先使⽤git ad 命令把2个⽂件都添加到暂存区中，再使⽤git status来查看下状态，如 下：

![image 24](<Git使用详细教程.note_images/imageFile24.png>)

接着我们可以使⽤git comit⼀次性提交到分⽀上，如下：

![image 25](<Git使用详细教程.note_images/imageFile25.png>)

四：Git撤销修改和删除⽂件操作。 ⼀：撤销修改： ⽐如我现在在readme.txt⽂件⾥⾯增加⼀⾏ 内容为 5，我们先通过命令查看如下：

![image 26](<Git使用详细教程.note_images/imageFile26.png>)

在我未提交之前，我发现添加 5内容有误，所以我得⻢上恢复以前的版本，现在我 可以有如下⼏种⽅法可以做修改：

第⼀：如果我知道要删掉那些内容的话，直接⼿动更改去掉那些需要的⽂件，然后ad添加到暂存

区，最后comit掉。 第⼆：我可以按以前的⽅法直接恢复到上⼀个版本。使⽤ git reset -hard HEAD^ 但是现在我不想使⽤上⾯的2种⽅法，我想直接想使⽤撤销命令该如何操作呢？⾸先在做撤销之

前，我们可以先⽤ git status 查看下当前的状态。如下所示：

![image 27](<Git使用详细教程.note_images/imageFile27.png>)

可以发现，Git会告诉你，git checkout - file 可以丢弃⼯作区的修改，如下命令： git checkout - readme.txt,如下所示：

![image 28](<Git使用详细教程.note_images/imageFile28.png>)

命令 git checkout -readme.txt 意思就是，把readme.txt⽂件在⼯作区做的修改全部撤销，这⾥ 有2种情况，如下：

- 1.
- 2.


readme.txt⾃动修改后，还没有放到暂存区，使⽤ 撤销修改就回到和版本库⼀模⼀样的状态。 另外⼀种是readme.txt已经放⼊暂存区了，接着⼜作了修改，撤销修改就回到添加暂存区后的状 态。

对于第⼆种情况，我想我们继续做demo来看下，假如现在我对readme.txt添加⼀⾏ 内容为

6，我git ad 增加到暂存区后，接着添加内容 7，我想通过撤销命令让其回到 暂存区后的状态。如下所示：

![image 29](<Git使用详细教程.note_images/imageFile29.png>)

注意：命令git checkout - readme.txt 中的 - 很重要，如果没有 - 的话，那么命令变成创建分

⽀了。 ⼆：删除⽂件。 假如我现在版本库testgit⽬录添加⼀个⽂件b.txt,然后提交。如下：

![image 30](<Git使用详细教程.note_images/imageFile30.png>)

如上：⼀般情况下，可以直接在⽂件⽬录中把⽂件删了，或者使⽤如上rm命令：rm b.txt ，如果 我想彻底从版本库中删掉了此⽂件的话，可以再执⾏comit命令 提交掉，现在⽬录是这样的，

![image 31](<Git使用详细教程.note_images/imageFile31.png>)

只要没有comit之前，如果我想在版本库中恢复此⽂件如何操作呢？ 可以使⽤如下命令 git checkout - b.txt，如下所示：

![image 32](<Git使用详细教程.note_images/imageFile32.png>)

再来看看我们testgit⽬录，添加了3个⽂件了。如下所示：

![image 33](<Git使用详细教程.note_images/imageFile33.png>)

五：远程仓库。 在了解之前，先注册github账号，由于你的本地Git仓库和github仓库之间的传输是通过 SH加密

的，所以需要⼀点设置：

第⼀步：创建 SH Key。在⽤户主⽬录下，看看有没有.sh⽬录，如果有，再看看这个⽬录下有没 有id_rsa和id_rsa.pub这两个⽂件，如果有的话，直接跳过此如下命令，如果没有的话，打开命令⾏， 输⼊如下命令：

sh-keygen -t rsa –C “youremail@example.com”, 由于我本地此前运⾏过⼀次，所以本地有，如 下所示：

![image 34](<Git使用详细教程.note_images/imageFile34.png>)

id_rsa是私钥，不能泄露出去，id_rsa.pub是公钥，可以放⼼地告诉任何⼈。 第⼆步：登录github,打开” setings”中的 SH Keys⻚⾯，然后点击“Ad SH Key”,填上任意title，

在Key⽂本框⾥黏贴id_rsa.pub⽂件的内容。

![image 35](<Git使用详细教程.note_images/imageFile35.png>)

点击 Ad Key，你就应该可以看到已经添加的key。

![image 36](<Git使用详细教程.note_images/imageFile36.png>)

1. 如何添加远程库？

现在的情景是：我们已经在本地创建了⼀个Git仓库后，⼜想在github创建⼀个Git仓库，并且希望 这两个仓库进⾏远程同步，这样github的仓库可以作为备份，⼜可以其他⼈通过该仓库来协作。

⾸先，登录github上，然后在右上⻆找到“create a new repo”创建⼀个新的仓库。如下：

![image 37](<Git使用详细教程.note_images/imageFile37.png>)

在Repository name填⼊testgit，其他保持默认设置，点击“Create repository”按钮，就成功地创 建了⼀个新的Git仓库：

![image 38](<Git使用详细教程.note_images/imageFile38.png>)

⽬前，在GitHub上的这个testgit仓库还是空的，GitHub告诉我们，可以从这个仓库克隆出新的仓

库，也可以把⼀个已有的本地仓库与之关联，然后，把本地仓库的内容推送到GitHub仓库。 现在，我们根据GitHub的提示，在本地的testgit仓库下运⾏命令： git remote ad origin 所有的如下：

htps:/github.com/tugenhua0707/testgit.git

![image 39](<Git使用详细教程.note_images/imageFile39.png>)

把本地库的内容推送到远程，使⽤ git push命令，实际上是把当前分⽀master推送到远程。 由于远程库是空的，我们第⼀次推送master分⽀时，加上了 –u参数，Git不但会把本地的master

分⽀内容推送的远程新的master分⽀，还会把本地的master分⽀和远程的master分⽀关联起来，在以 后的推送或者拉取时就可以简化命令。推送成功后，可以⽴刻在github⻚⾯中看到远程库的内容已经 和本地⼀模⼀样了，上⾯的要输⼊github的⽤户名和密码如下所示：

![image 40](<Git使用详细教程.note_images/imageFile40.png>)

从现在起，只要本地作了提交，就可以通过如下命令： git push origin master 把本地master分⽀的最新修改推送到github上了，现在你就拥有了真正的分布式版本库了。

2. 如何从远程库克隆？ 上⾯我们了解了先有本地库，后有远程库时候，如何关联远程库。 现在我们想，假如远程库有新的内容了，我想克隆到本地来 如何克隆呢？ ⾸先，登录github，创建⼀个新的仓库，名字叫testgit2.如下：

![image 41](<Git使用详细教程.note_images/imageFile41.png>)

如下，我们看到：

![image 42](<Git使用详细教程.note_images/imageFile42.png>)

现在，远程库已经准备好了，下⼀步是使⽤命令git clone克隆⼀个本地库了。如下所示：

![image 43](<Git使用详细教程.note_images/imageFile43.png>)

接着在我本地⽬录下 ⽣成testgit2⽬录了，如下所示：

![image 44](<Git使用详细教程.note_images/imageFile44.png>)

六：创建与合并分⽀。 在版本回填退⾥，你已经知道，每次提交，Git都把它们串成⼀条时间线，这条时间线就是⼀个分

⽀。截⽌到⽬前，只有⼀条时间线，在Git⾥，这个分⽀叫主分⽀，即master分⽀。HEAD严格来说不 是指向提交，⽽是指向master，master才是指向提交的，所以，HEAD指向的就是当前分⽀。

⾸先，我们来创建dev分⽀，然后切换到dev分⽀上。如下操作：

![image 45](<Git使用详细教程.note_images/imageFile45.png>)

git checkout 命令加上 –b参数表示创建并切换，相当于如下2条命令 git branch dev git checkout dev git branch查看分⽀，会列出所有的分⽀，当前分⽀前⾯会添加⼀个星号。然后我们在dev分⽀上

继续做demo，⽐如我们现在在readme.txt再增加⼀⾏ 7 ⾸先我们先来查看下readme.txt内容，接着添加内容 7，如下：

![image 46](<Git使用详细教程.note_images/imageFile46.png>)

现在dev分⽀⼯作已完成，现在我们切换到主分⽀master上，继续查看readme.txt内容如下：

![image 47](<Git使用详细教程.note_images/imageFile47.png>)

现在我们可以把dev分⽀上的内容合并到分⽀master上了，可以在master分⽀上，使⽤如下命令 git merge dev 如下所示：

![image 48](<Git使用详细教程.note_images/imageFile48.png>)

git merge命令⽤于合并指定分⽀到当前分⽀上，合并后，再查看readme.txt内容，可以看到，和 dev分⽀最新提交的是完全⼀样的。

注意到上⾯的Fast-forward信息，Git告诉我们，这次合并是“快进模式”，也就是直接把master指 向dev的当前提交，所以合并速度⾮常快。

合并完成后，我们可以接着删除dev分⽀了，操作如下：

![image 49](<Git使用详细教程.note_images/imageFile49.png>)

总结创建与合并分⽀命令如下： 查看分⽀：git branch 创建分⽀：git branch name 切换分⽀：git checkout name 创建+切换分⽀：git checkout –b name 合并某分⽀到当前分⽀：git merge name 删除分⽀：git branch –d name

1. 如何解决冲突？

下⾯我们还是⼀步⼀步来，先新建⼀个新分⽀，⽐如名字叫fenzhi1，在readme.txt添加⼀⾏内容 8，然后提交，如下所示：

![image 50](<Git使用详细教程.note_images/imageFile50.png>)

同样，我们现在切换到master分⽀上来，也在最后⼀⾏添加内容，内容为 9，如下所 示：

![image 51](<Git使用详细教程.note_images/imageFile51.png>)

# 现在我们需要在master分⽀上来合并fenzhi1，如下操作：

![image 52](<Git使用详细教程.note_images/imageFile52.png>)

Git⽤ <， =， >标记出不同分⽀的内容，其中 <HEAD是指主分⽀修改的 内容， >fenzhi1 是指fenzhi1上修改的内容，我们可以修改下如下后保存：

![image 53](<Git使用详细教程.note_images/imageFile53.png>)

如果我想查看分⽀合并的情况的话，需要使⽤命令 git log.命令⾏演示如下：

![image 54](<Git使用详细教程.note_images/imageFile54.png>)

3.分⽀管理策略。 通常合并分⽀时，git⼀般使⽤”Fast forward”模式，在这种模式下，删除分⽀后，会丢掉分⽀信

息，现在我们来使⽤带参数 –no-f来禁⽤”Fast forward”模式。⾸先我们来做demo演示下： 创建⼀个dev分⽀。 修改readme.txt内容。 添加到暂存区。

- 1.
- 2.
- 3.


- 4.
- 5.
- 6.


切换回主分⽀(master)。 合并dev分⽀，使⽤命令 git merge –no-f -m “注释” dev 查看历史记录

截图如下：

![image 55](<Git使用详细教程.note_images/imageFile55.png>)

分⽀策略：⾸先master主分⽀应该是⾮常稳定的，也就是⽤来发布新版本，⼀般情况下不允许在 上⾯⼲活，⼲活⼀般情况下在新建的dev分⽀上⼲活，⼲完后，⽐如上要发布，或者说dev分⽀代码稳 定后可以合并到主分⽀master上来。

七：bug分⽀：

在开发中，会经常碰到bug问题，那么有了bug就需要修复，在Git中，分⽀是很强⼤的，每个bug 都可以通过⼀个临时分⽀来修复，修复完成后，合并分⽀，然后将临时的分⽀删除掉。

⽐如我在开发中接到⼀个404 bug时候，我们可以创建⼀个404分⽀来修复它，但是，当前的dev 分⽀上的⼯作还没有提交。⽐如如下：

![image 56](<Git使用详细教程.note_images/imageFile56.png>)

并不是我不想提交，⽽是⼯作进⾏到⼀半时候，我们还⽆法提交，⽐如我这个分⽀bug要2天完 成，但是我isue-404 bug需要5个⼩时内完成。怎么办呢？还好，Git还提供了⼀个stash功能，可以 把当前⼯作现场 ”隐藏起来”，等以后恢复现场后继续⼯作。如下：

![image 57](<Git使用详细教程.note_images/imageFile57.png>)

所以现在我可以通过创建isue-404分⽀来修复bug了。 ⾸先我们要确定在那个分⽀上修复bug，⽐如我现在是在主分⽀master上来修复的，现在我要在

master分⽀上创建⼀个临时分⽀，演示如下：

![image 58](<Git使用详细教程.note_images/imageFile58.png>)

# 修复完成后，切换到master分⽀上，并完成合并，最后删除isue-404分⽀。演示如下：

![image 59](<Git使用详细教程.note_images/imageFile59.png>)

现在，我们回到dev分⽀上⼲活了。

![image 60](<Git使用详细教程.note_images/imageFile60.png>)

⼯作区是⼲净的，那么我们⼯作现场去哪⾥呢？我们可以使⽤命令 git stash list来查看下。如下：

![image 61](<Git使用详细教程.note_images/imageFile61.png>)

⼯作现场还在，Git把stash内容存在某个地⽅了，但是需要恢复⼀下，可以使⽤如下2个⽅法：

- 1.
- 2.


git stash aply恢复，恢复后，stash内容并不删除，你需要使⽤命令git stash drop来删除。 另⼀种⽅式是使⽤git stash pop,恢复的同时把stash内容也删除了。

演示如下

![image 62](<Git使用详细教程.note_images/imageFile62.png>)

⼋：多⼈协作。 当你从远程库克隆时候，实际上Git⾃动把本地的master分⽀和远程的master分⽀对应起来了，并

且远程库的默认名称是origin。

- 1.
- 2.


要查看远程库的信息 使⽤ git remote 要查看远程库的详细信息 使⽤ git remote –v

如下演示：

![image 63](<Git使用详细教程.note_images/imageFile63.png>)

⼀：推送分⽀：

推送分⽀就是把该分⽀上所有本地提交到远程库中，推送时，要指定本地分⽀，这样，Git就会把该 分⽀推送到远程库对应的远程分⽀上：

使⽤命令 git push origin master ⽐如我现在的github上的readme.txt代码如下：

![image 64](<Git使用详细教程.note_images/imageFile64.png>)

本地的readme.txt代码如下：

![image 65](<Git使用详细教程.note_images/imageFile65.png>)

现在我想把本地更新的readme.txt代码推送到远程库中，使⽤命令如下：

![image 66](<Git使用详细教程.note_images/imageFile66.png>)

我们可以看到如上，推送成功，我们可以继续来截图github上的readme.txt内容 如下：

![image 67](<Git使用详细教程.note_images/imageFile67.png>)

可以看到 推送成功了，如果我们现在要推送到其他分⽀，⽐如dev分⽀上，我们还是那个命令 git push origin dev

那么⼀般情况下，那些分⽀要推送呢？

- 1.
- 2.


master分⽀是主分⽀，因此要时刻与远程同步。 ⼀些修复bug分⽀不需要推送到远程去，可以先合并到主分⽀上，然后把主分⽀master推送到远 程去。

⼆：抓取分⽀： 多⼈协作时，⼤家都会往master分⽀上推送各⾃的修改。现在我们可以模拟另外⼀个同事，可以

在另⼀台电脑上（注意要把 SH key添加到github上）或者同⼀台电脑上另外⼀个⽬录克隆，新建⼀个 ⽬录名字叫testgit2

但是我⾸先要把dev分⽀也要推送到远程去，如下

![image 68](<Git使用详细教程.note_images/imageFile68.png>)

接着进⼊testgit2⽬录，进⾏克隆远程的库到本地来，如下：

![image 69](<Git使用详细教程.note_images/imageFile69.png>)

现在⽬录下⽣成有如下所示：

![image 70](<Git使用详细教程.note_images/imageFile70.png>)

现在我们的⼩伙伴要在dev分⽀上做开发，就必须把远程的origin的dev分⽀到本地来，于是可以使

⽤命令创建本地dev分⽀：git checkout –b dev origin/dev 现在⼩伙伴们就可以在dev分⽀上做开发了，开发完成后把dev分⽀推送到远程库时。 如下：

![image 71](<Git使用详细教程.note_images/imageFile71.png>)

# ⼩伙伴们已经向origin/dev分⽀上推送了提交，⽽我在我的⽬录⽂件下也对同样的⽂件同个地⽅作 了修改，也试图推送到远程库时，如下：

![image 72](<Git使用详细教程.note_images/imageFile72.png>)

# 由上⾯可知：推送失败，因为我的⼩伙伴最新提交的和我试图推送的有冲突，解决的办法也很简 单，上⾯已经提示我们，先⽤git pul把最新的提交从origin/dev抓下来，然后在本地合并，解决冲突， 再推送。

![image 73](<Git使用详细教程.note_images/imageFile73.png>)

git pul也失败了，原因是没有指定本地dev分⽀与远程origin/dev分⽀的链接，根据提示，设置dev 和origin/dev的链接：如下：

![image 74](<Git使用详细教程.note_images/imageFile74.png>)

这回git pul成功，但是合并有冲突，需要⼿动解决，解决的⽅法和分⽀管理中的 解决冲突完全⼀ 样。解决后，提交，再push：

我们可以先来看看readme.txt内容了。

![image 75](<Git使用详细教程.note_images/imageFile75.png>)

现在⼿动已经解决完了，我接在需要再提交，再push到远程库⾥⾯去。如下所示：

![image 76](<Git使用详细教程.note_images/imageFile76.png>)

因此：多⼈协作⼯作模式⼀般是这样的：

- 1.
- 2.
- 3.


⾸先，可以试图⽤git push origin branch-name推送⾃⼰的修改. 如果推送失败，则因为远程分⽀⽐你的本地更新早，需要先⽤git pul试图合并。 如果合并有冲突，则需要解决冲突，并在本地提交。再⽤git push origin branch-name推送。

Git基本常⽤命令如下： mkdir： X (创建⼀个空⽬录 X指⽬录名) pwd： 显示当前⽬录的路径。 git init 把当前的⽬录变成可以管理的git仓库，⽣成隐藏.git⽂件。 git ad X 把 x⽂件添加到暂存区去。 git comit –m “X” 提交⽂件 –m 后⾯的是注释。 git status 查看仓库状态 git dif X 查看 X⽂件修改了那些内容 git log 查看历史记录 git reset -hard HEAD^ 或者 git reset -hard HEAD~ 回退到上⼀个版本 (如果想回退到10个版本，使⽤git reset –hard HEAD~10 ) cat X 查看 X⽂件内容 git reflog 查看历史记录的版本号id git checkout - X 把 X⽂件在⼯作区的修改全部撤销。 git rm X 删除 X⽂件 git remote ad origin 关联⼀个远程库

htps:/github.com/tugenhua0707/testgit

git push –u(第⼀次要⽤-u 以后不需要) origin master 把当前master分⽀推送到远程库 git clone 从远程库中克隆 git checkout –b dev 创建dev分⽀ 并切换到dev分⽀上 git branch 查看当前所有的分⽀ git checkout master 切换回master分⽀ git merge dev 在当前的分⽀上合并dev分⽀ git branch –d dev 删除dev分⽀ git branch name 创建分⽀ git stash 把当前的⼯作隐藏起来 等以后恢复现场后继续⼯作 git stash list 查看所有被隐藏的⽂件列表 git stash aply 恢复被隐藏的⽂件，但是内容不删除 git stash drop 删除⽂件 git stash pop 恢复⽂件的同时 也删除⽂件 git remote 查看远程库的信息 git remote –v 查看远程库的详细信息 git push origin master Git会把master分⽀推送到远程库对应的远程分⽀上

htps:/github.com/tugenhua0707/testgit

Git批量ad git ad .

