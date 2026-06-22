- 1、在start-master.sh脚本中调⽤Mater.scala中的main⽅法
- 2、在main⽅法中封装spark参数，并调⽤startRpcEnvAndEndpoint（）创建RpcEnv
- 3、在startRpcEnvAndEndpoint中创建RpcEnv（AkaSystem），创建masterEndPoint（actor），new Master （），实例化Master

- 4、实例化Master后会调⽤Master的Onstart（）⽅法

- 5、在onStart（）中启动webinfo，然后通过定时器循环发送消息给⾃⼰ self.send(CheckForWorkerTimeOut)，通过case object执⾏

- 6、在case Object中调⽤timeOutDeadWorker（），检查超时的work
- 7、在timeOutDeadWorker（）中检查超时的worker，调⽤removeWorker(worker)删除节点，原理就 是修改3个集合，其中，会对每个节点尝试15次检查。

- 8、master启动完成后，在recive（）⽅法中定义⼤量的case object，等待接受其他actor的请求


![image 1](<master启动流程.note_images/imageFile1.png>)

