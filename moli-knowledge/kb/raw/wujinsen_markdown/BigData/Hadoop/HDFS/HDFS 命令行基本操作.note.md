htps:/ w.cnblogs.com/java-h/p/ 1917.html

1.hdfs命令⾏

- （1）查看帮助 hdfs dfs -help
- （2）查看当前⽬录信息 hdfs dfs -ls /
- （3）上传⽂件 hdfs dfs -put /本地路径 /hdfs路径
- （4）剪切⽂件 hdfs dfs -moveFromLocal a.txt /a.txt
- （5）下载⽂件到本地 hdfs dfs -get /hdfs路径 /本地路径
- （6）合并下载 hdfs dfs -getmerge /hdfs路径⽂件夹 /合并后的⽂件
- （7）创建⽂件夹 hdfs dfs -mkdir /helo
- （8）创建多级⽂件夹 hdfs dfs -mkdir -p /helo/world
- （9）移动hdfs⽂件 hdfs dfs -mv /hdfs路径 /hdfs路径
- （10）复制hdfs⽂件 hdfs dfs -cp /hdfs路径 /hdfs路径


（ 1）删除hdfs⽂件 hdfs dfs -rm /a.txt

- （12）删除hdfs⽂件夹 hdfs dfs -rm -r /helo
- （13）查看hdfs中的⽂件 hdfs dfs -cat /⽂件 hdfs dfs -tail -f /⽂件
- （14）查看⽂件夹中有多少个⽂件 hdfs dfs -count /⽂件夹
- （15）查看hdfs的总空间 hdfs dfs -df / hdfs dfs -df -h /
- （16）修改副本数 hdfs dfs -setrep 1 /a.txtZ-blog： w.361wx.com


