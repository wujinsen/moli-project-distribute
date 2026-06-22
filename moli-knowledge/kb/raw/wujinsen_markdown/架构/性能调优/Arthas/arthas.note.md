# 1.启动 arthas

curl -O htps:/arthas.aliyun.com/arthas-bot.jar java -jar arthas-bot.jar

# 2. 查看命令: dashboard

使⽤arthas⽣成内存分区的⽕焰图: profiler start -event aloc

获取已采集的sample的数量: profiler getSamples

停⽌采集，并⽣成html⽂件，后⾯时⽂件存放的地址: profiler stop-format html -file /opt/output3.html

⽣成hprof快照: heapdump /opt/heapdump.hprof

# 3. mat下载 htps:/ w.eclipse.org/mat/downloads.php

