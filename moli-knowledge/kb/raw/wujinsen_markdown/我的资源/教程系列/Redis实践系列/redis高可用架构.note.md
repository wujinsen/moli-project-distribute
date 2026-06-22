# ⼀、背景

公司的业务在⼤量的使⽤redis，访问量⼤的业务我们有在使⽤codis集群，redis 3.0集群，说到redis 3.0集群，我们线上已经跑了半年多了，集群本身没有出现过任务问题，但是由于我们这个业务是海外 的，集群建在aws的ec2上，由于ec2的⽹络抖动或者ec2本身的原因，导致主从切换，⽬前aws的技术 正在跟进，这个集群⽬前的QPS 50w+，集群本身已经做到了⾼可⽤和横向扩展，但是，实际情况⼀些 ⼩的业务没必要上集群，单个实例就可以满⾜业务需求，那么我们就要想办法如何保证单个实例的⾼ 可⽤，最近也在看相关的⽂档，做⼀些测试，⼤家有在使⽤redis主从+lvs 漂VIP的⽅案，也有使⽤ redis主从+哨兵 漂VIP的⽅案，甚⾄有在代码逻辑做故障切换等等，各种各样的⽅案都有，下⾯我介绍 ⼀下redis主从+哨兵 漂VIP的⽅案，后⾯我们打算线上⼤规模的使⽤这个⽅案。

⼆、环境

![image 1](<redis高可用架构.note_images/imageFile1.png>)

三、部署

- 1、安装
- 2、撰写redis配置⽂件（10.10.32.54 和10.10.32.5）


![image 2](<redis高可用架构.note_images/imageFile2.png>)

![image 3](<redis高可用架构.note_images/imageFile3.png>)

![image 4](<redis高可用架构.note_images/imageFile4.png>)

## 3、撰写sentinel配置⽂件（10.10.32.54 、10.10.32.5 和10.10.32.57）

![image 5](<redis高可用架构.note_images/imageFile5.png>)

## PS：

关于sentinel 的⼀些⼯作原理和参数说明，请参阅：

htp:/redisdoc.com/topic/sentinel.html

- 4、撰写漂VIP的脚本（10.10.32.54 、10.10.32.5）

PS：

这⾥⼤概说⼀下这个脚本的⼯作原理，sentinel在做failover的 过程中会传出6个参数，分别是<mastername>、 <role>、 <state>、 <from-ip>、 <from-port>、 <to-ip> 、<to-port>，其中第6个参数 from-ip也就是新的master的ip，对应脚本中的MASTER_IP，下⾯的if判断⼤家应该都很了然了，如果 MASTER_IP=LOCAL_IP，那就绑定VIP，反之删除VIP。

- 5、启动redis服务（10.10.32.54、10.10.32.5）


![image 6](<redis高可用架构.note_images/imageFile6.png>)

![image 7](<redis高可用架构.note_images/imageFile7.png>)

![image 8](<redis高可用架构.note_images/imageFile8.png>)

- 6、初始化主从（10.10.32.5）
- 7、绑定VIP到主库（10.10.32.54）
- 8、启动sentinel服务（10.10.32.54、10.10.32.5、10.10.32.57）


![image 9](<redis高可用架构.note_images/imageFile9.png>)

![image 10](<redis高可用架构.note_images/imageFile10.png>)

![image 11](<redis高可用架构.note_images/imageFile11.png>)

⾄此，整个⾼可⽤⽅案已经搭建完成。

![image 12](<redis高可用架构.note_images/imageFile12.png>)

![image 13](<redis高可用架构.note_images/imageFile13.png>)

![image 14](<redis高可用架构.note_images/imageFile14.png>)

# 四、测试

- 1、把主库停掉


![image 15](<redis高可用架构.note_images/imageFile15.png>)

- 2、看从库是否提升为主库
- 3、看VIP是否漂移到10.10.32.5上
- 4、看Sentinel的监控状态


![image 16](<redis高可用架构.note_images/imageFile16.png>)

![image 17](<redis高可用架构.note_images/imageFile17.png>)

![image 18](<redis高可用架构.note_images/imageFile18.png>)

来源：

屌丝运维男 原⽂：htp:/navyaijm.blog.51cto.com/4647068/174569

如有侵权或不周之处，敬请劳烦联系若⻜（微信：132 13940）⻢上删除，谢谢！

微运维：vYunWei

![image 19](<redis高可用架构.note_images/imageFile19.png>)

服务器管理、监控、维护、优化 运维业务、运维规划、运维开发 欢迎有想法、乐于分享的运维⼈交流学习

现已开通多个微信群，有兴趣交流学习的同学 可加若⻜的微信：132 13940 进群

合作邮箱：admin@137x.com

