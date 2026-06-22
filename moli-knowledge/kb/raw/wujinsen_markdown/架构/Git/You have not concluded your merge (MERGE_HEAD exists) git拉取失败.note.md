错误可能是因为在你以前pul下来的代码没有⾃动合并导致的. 有2个解决办法:

- 1.保留你本地的修改 git merge --abort git reset --merge 合并后记得⼀定要提交这个本地的合并 然后在获取线上仓库 git pull

- 2.down下线上代码版本,抛弃本地的修改 不建议这样做,但是如果你本地修改不⼤,或者⾃⼰有⼀份备份留存,可以直接⽤线上最新版本覆盖到本 地 git fetch --all git reset --hard origin/master git fetch


