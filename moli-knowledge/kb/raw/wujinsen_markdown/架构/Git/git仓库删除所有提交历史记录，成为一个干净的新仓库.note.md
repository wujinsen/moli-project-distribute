htps:/blog.csdn.net/yc102/article/details/56487680

把旧项⽬提交到git上，但是会有⼀些历史记录，这些历史记录中可能会有项⽬密码等敏感信息。如何 删除这些历史记录，形成⼀个全新的仓库，并且保持代码不变呢？ 1.切换到新的分⽀

git checkout --orphan latest_branch

1.

缓存所有⽂件（除了.gitignore中声名排除的）

git add -A

1.

提交跟踪过的⽂件（Comit the changes）

git commit -am "commit message"

1.

删除master分⽀（Delete the branch）

git branch -D master

- 5.重命名当前分⽀为master（Rename the curent branch to master） git branch -m master
- 6.提交到远程master分⽀ （Finaly, force update your repository） git push -f origin master


htps:/stackoverflow.com/questions/1371658/how-to-delete-al-comit-history-in-github

