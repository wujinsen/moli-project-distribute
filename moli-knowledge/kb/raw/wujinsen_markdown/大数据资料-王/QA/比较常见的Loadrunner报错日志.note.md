- 1. Loadruner报错⽇志： Action.c(13):错误-2727: Step download timeout (120 seconds) has expired when downloading resource(s). Set the "Step Timeout caused by resources is a warning" Run-Time Seting to Yes/No to have this mesage as a warning/eror, respectively

解决⽅案： 修改“运⾏时设置-HTP请求连接超时、HTP请求接收超时”的值为60s或者更长时间

- 2. Loadruner报错⽇志： Action.c(39):错误-2796:连接服务器“test0105.s1.diy.com:80”失败: [1061] Conection refused 有可能是服务器有太多的数据库连接，提示连接被拒绝 解决⽅案： 可以让开发尝试调整：


- 1）.数据库最⼤连接数；
- 2）. tomcat的最⼤并发数限制
- 3. Loadruner报错⽇志： Action.c(9):错误-2791:服务器“test0105*.s1.diy.com”已过早关闭连接 访问时已经下载不到资源了，有可能是已经达到服务器资源的瓶颈了，可以查看服务器资源如CPU、 负载等
- 4.Loadruner报错⽇志：Action.c(7): Eror -2791:Server"10.10.0. 8" has shut down the


conection prematurely借鉴51Testing⽹友提供的解决⽅案：1)、应⽤服务器死掉。⼩⽤户时程序上的问题，程序上处理数据库 的问题2)、应⽤服务没有死。应⽤服务参数设置问题。例如：在许多客户端weblogic应⽤服务器被拒绝，⽽在服务器端没有错误显⽰，则 有可能是weblogic中的server元素的acceptbacklog属性值设得过低。如果连接时收到connection refused消息，说明应提⾼该值，每次增 加25%。3)、数据库的连接在应⽤服务的性能参数可能太⼩了数据库启动的最⼤连接数（跟硬件的内存有关）4)、有时关闭防⽕墙如卡巴 斯基也会解决如上问题Continue~~~~

