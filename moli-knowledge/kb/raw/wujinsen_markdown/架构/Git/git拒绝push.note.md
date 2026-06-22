error: failed to push some refs to 'git@192.168.1.X:/var/git.server/.../web' 这是由于git默认拒绝了push操作，需要进⾏设置，修改.git/config添加如下代码：

[receive] denyCurrentBranch = ignore

在初始化远程仓库时最好使⽤ git --bare init ⽽不要使⽤：git init

如果使⽤了git init初始化，则远程仓库的⽬录下，也包含work tree，当本地仓库向远程仓库push时, 如果远程仓库正在 push的分⽀上（如果当时不在push的分⽀，就没有问题）, 那么push后的结果不会反应在work tree上, 也即在远程仓库的 ⽬录下对应的⽂件还是之前的内容，必须得使⽤git reset --hard才能看到push后的内容.

