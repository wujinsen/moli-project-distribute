打开finder->应⽤程序->idea.ap->右键->显示包内容->然后MaxOS->双击shel脚本（idea）

查看idea⽇志，此次遇到问题是Initial heap size set to a larger value than the maximum heap size 修改 /Users/jinsenwu/Library/Preferences/InteliJIdea2019.2/idea.vmoptions

- -Xms4096m
- -Xmx4096m 改成⼀致就⾏了，之前idea2018 xms可以设置⼤于xmx，idea2019.2改成⼀致就⾏


