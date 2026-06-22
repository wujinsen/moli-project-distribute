- 1.
- 2.
- 3.
- 4.

- a.
- b.
- c.
- d.


- 5.

- a.
- b.


- 6.
- 7.
- 8.


在worker启动时，通过脚本start-slave.sh脚本中调⽤main（） 在main（）中封装参数，调⽤startRpcEnvAndEndpoint（）创建RpcEnv 在startRpcEnvAndEndpoint（）中创建RpcEnv和endpiont，并实例化Worker，执⾏ Worker的onStart（）⽅法 在onStart（）⽅法中主要流程：

创建⼯作⽬录 启动shufleservice 创建worker webui 调⽤registerWithMaster（）向master注册worker

在registerWithMaster（）⽅法中"

⾸先worker回向master注册⾃⼰

其次会启动定时任务，不断的向⾃⼰发送caseclas，调⽤reregisterWithMaster（） 在reregisterWithMaster（）⽅法中，如果之前的注册失败，会重复（15、16次）注册⾃⼰ 如果在registerWithMaster（）中注册成功，master会向worker发送case object RegisteredWorker，worker接收到消息后，会定时向master发送⼼跳 master接收到⼼跳信息后，会修改worker的上次⼼跳时间

![image 1](<worker启动流程.note_images/imageFile1.png>)

