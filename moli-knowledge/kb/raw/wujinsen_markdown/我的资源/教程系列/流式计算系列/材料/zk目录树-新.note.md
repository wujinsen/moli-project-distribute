- 1 /-{storm-zk-root} -- storm在zookeeper上的根⽬录(默认为/storm)

- 2 |

- 3 |-/assignments -- topology的任务分配信息

- 4 | |

| |-/{topology-id} -- 这个⽬录保存的是每个topology的assignments信息包括：对 应的nimbus上

- 5

| -- 的代码⽬录,所有task的启动时间,每个task与机器、端⼝的映 射。操作为

- 6

| -- (assignments)来获取所有assignments的值；以及 (assignment-info storm-id)

- 7

- 8 | -- 来得到给定的storm-id对应的AssignmentInfo信息

- 9 | -- 在AssignmentInfo中存储的内容有:

| -- :executor->node+port :executor->start-time-secs :node->host

- 10

- 11 | -- 具体定义在common.clj中的

| -- (defrecord Assignment[master-code-dir node>host executor->node+port executor->starttime-secs])

- 12

- 13 |

- 14 |-/storms -- 这个⽬录保存所有正在运⾏的topology的id

- 15 | |

- 16 | |

| |-/{topology-id} -- 这个⽂件保存这个topology的⼀些信息，包括topology的名 字，topology开始运⾏

- 17

| -- 的时间以及这个topology的状态。操作(active-storms),获得 当前路径活跃的下

- 18

| -- topology数据。保存的内容参考类StormBase；(storm-base storm-id)得到给定的

- 19

- 20 | -- storm-id下的StormBase数据,具体定义在common.clj中的

| -- (defrecord StormBase [storm-name launch-time-secs status numworkers component->executors])

- 21

- 22 |

- 23 |-/supervisors -- 这个⽬录保存所有的supervisor的⼼跳信息

- 24 | |

- 25 | |

| |-/{supervisor-id} -- 这个⽂件保存supervisor的⼼跳信息包括:⼼跳时间，主机名， 这个supervisor上

- 26

| -- worker的端⼝号，运⾏时间(具体看SupervisorInfo类)。操作 (supervisors)得到

- 27

| -- 所有的supervisors节点；(supervisor-info supervisorid)得到给定的

- 28

| -- supervisor-id对应的SupervisorInfo信息；具体定义在 common.clj中的

- 29


- 30 |

| -- (defrecord SupervisorInfo [time-secs hostname assignment-id usedports meta scheduler-meta uptime-secs])

- 31

- 32 |

- 33 |-/workerbeats -- 所有worker的⼼跳

- 34 | |

| |-/{topology-id} -- 这个⽬录保存这个topology的所有的worker的⼼跳信 息

- 35

- 36 | |

| |-/{supervisorId-port} -- worker的⼼跳信息，包括⼼跳的时间，worker运⾏时 间以及⼀些统计信息

- 37

- 38 |

| -- 操作(heartbeat-storms)得到所有有⼼跳数据的 topology，

- 39

| -- (get-worker-heartbeat storm-id node port) 得到具体⼀个topology下

- 40

- 41 | -- 的某个worker(node:port)的⼼跳状况，

| -- (executor-beats storm-id executor->node+port)得 到⼀个executor的⼼跳状况

- 42

- 43 |

- 44 |-/errors -- 所有产⽣的error信息

- 45 |

|-/{topology-id} -- 这个⽬录保存这个topology下⾯的错误信息。操作(errortopologies)得到出错

- 46

- 47 | -- 的topology；(errors storm-id component-id)得到

- 48 | -- 给定的storm-id component-id下的出错信息

- 49 |-/{component-id}


