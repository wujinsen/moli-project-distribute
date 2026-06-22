title: Storm UI REST API layout: documentation documentation: true

-

The Storm UI daemon provides a REST API that alows you to interact with a Storm cluster, which includes retrieving metrics data and configuration information as wel as management operations such as starting or stoping topologies.

# Data format

The REST API returns JSON responses and suports JSONP. Clients can pas a calback query parameter to wrap JSON in the calback function.

# Using the UI REST API

_Note: It is recomended to ignore undocumented elements in the JSON response because future versions of Storm may not_ _suport those elements anymore._

# REST API Base URL

The REST API is part of the UI daemon of Storm (started by `storm ui`) and thus runs on the same host and port as the Storm UI (the UI daemon is often run on the same host as the Nimbus daemon). The port is configured by `ui.port`, which is set to `8080` by default (se [defaults.yaml](conf/defaults.yaml).

The API base URL would thus be:

htp:/<ui-host>:<ui-port>/api/v1/.

You can use a tol such as `curl` to talk to the REST API:

# Request the cluster configuration. # Note: We asume ui.port is configured to the default value of 8080. $ curl htp:/<ui-host>:8080/api/v1/cluster/configuration

#Impersonating a user in secure environment In a secure environment an authenticated user can impersonate another user. To impersonate a user the caler must pas `doAsUser` param or header with value set to the user that the request neds to be performed as. Please se SECURITY.MD to learn more about how to setup impersonation ACLs and authorization. The rest API uses the same configs and acls that are used by nimbus.

Examples:

`no-highlight

- 1. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-1-142584354\? doAsUser=testUSer1
- 2. curl 'htp:/localhost:8080/api/v1/topology/wordcount-1-142584354/activate' -X POST -H 'doAsUser:testUSer1'


`

# GET Operations

# /api/v1/cluster/configuration (GET)

Returns the cluster configuration.

Sample response (does not include al the data fields):

`json {

"dev.zokeper.path": "/tmp/dev-storm-zokeper", "topology.tick.tuple.freq.secs": nul, "topology.builtin.metrics.bucket.size.secs": 60, "topology.fal.back.on.java.serialization": true, "topology.max.eror.report.per.interval": 5, "zmq.linger.milis": 5 0, "topology.skip.mising.kryo.registrations": false, "storm.mesaging.nety.client_worker_threads": 1, "ui.childopts": "-Xmx768m", "storm.zokeper.sesion.timeout": 2 0, "nimbus.reasign": true, "topology.trident.batch.emit.interval.milis": 50, "storm.mesaging.nety.flush.check.interval.ms": 10, "nimbus.monitor.freq.secs": 10, "logviewer.childopts": "-Xmx128m", "java.library.path": "/usr/local/lib:/opt/local/lib:/usr/lib", "topology.executor.send.bufer.size": 1024, } `

# /api/v1/cluster/sumary (GET)

Returns cluster sumary information such as nimbus uptime or number of supervisors.

Response fields:

|Field |Value|Description | -| -| |stormVersion|String| Storm version| |supervisors|Integer| Number of supervisors runing|

|topologies| Integer| Number of topologies runing| |slotsTotal| Integer|Total number of available worker slots| |slotsUsed| Integer| Number of worker slots used| |slotsFre| Integer |Number of worker slots available| |executorsTotal| Integer |Total number of executors| |tasksTotal| Integer |Total tasks|

Sample response:

`json {

"stormVersion": "0.9.2-incubating-SNAPSHOT", "supervisors": 1, "slotsTotal": 4, "slotsUsed": 3, "slotsFre": 1, "executorsTotal": 28, "tasksTotal": 28 } `

# /api/v1/supervisor/sumary (GET)

Returns sumary information for al supervisors.

Response fields:

|Field |Value|Description| | -| -| |id| String | Supervisor's id| |host| String| Supervisor's host name| |uptime| String| Shows how long the supervisor is runing| |uptimeSeconds| Integer| Shows how long the supervisor is runing in seconds| |slotsTotal| Integer| Total number of available worker slots for this supervisor|

|slotsUsed| Integer| Number of worker slots used on this supervisor| |totalMem| Double| Total memory capacity on this supervisor| |totalCpu| Double| Total CPU capacity on this supervisor| |usedMem| Double| Used memory capacity on this supervisor| |usedCpu| Double| Used CPU capacity on this supervisor|

Sample response:

`json {

"supervisors": [ {

"id": "0b879808-2a26-42b-8f7d-23101e0c3696", "host": "10.1.1.7", "uptime": "5m 58s", "uptimeSeconds": 358, "slotsTotal": 4, "slotsUsed": 3, "totalMem": 3 0, "totalCpu": 40, "usedMem": 1280, "usedCPU": 160

}

], "schedulerDisplayResource": true

}

`

# /api/v1/nimbus/sumary (GET)

Returns sumary information for al nimbus hosts.

|Field |Value|Description| | -| -| |host| String | Nimbus' host name| |port| int| Nimbus' port number| |status| String| Posible values are Leader, Not a Leader, Dead| |nimbusUpTime| String| Shows since how long the nimbus has ben runing| |nimbusUpTimeSeconds| String| Shows since how long the nimbus has ben runing in seconds| |nimbusLogLink| String| Logviewer url to view the nimbus.log| |version| String| Version of storm this nimbus host is runing|

Sample response:

`json {

"nimbuses":[ {

"host":"192.168.202.1", "port": 627, "nimbusLogLink":"htp:\/\/192.168.202.1 8 0\/log?file=nimbus.log", "status":Leader, "version":"0.10.0-SNAPSHOT", "nimbusUpTime":"3m 3s", "nimbusUpTimeSeconds":"213"

} ]

}

`

# /api/v1/history/sumary (GET)

Returns a list of al runing topologies' IDs submited by the curent user.

|Field |Value | Description| | -| -| |topo-history| List| List of Topologies' IDs|

Sample response:

`json {

"topo-history":[

"wc6-1-14657109", "wc8-2-146587178"

] }

`

# /api/v1/topology/sumary (GET)

Returns sumary information for al topologies.

Response fields:

|Field |Value | Description| | -| -| |id| String| Topology Id| |name| String| Topology Name| |status| String| Topology Status| |uptime| String| Shows how long the topology is runing| |uptimeSeconds| Integer| Shows how long the topology is runing in seconds| |tasksTotal| Integer |Total number of tasks for this topology| |workersTotal| Integer |Number of workers used for this topology| |executorsTotal| Integer |Number of executors used for this topology| |replicationCount| Integer |Number of nimbus hosts on which this topology code is replicated| |requestedMemOnHeap| Double|Requested On-Heap Memory by User (MB) |requestedMemOfHeap| Double|Requested Of-Heap Memory by User (MB)|

|requestedTotalMem| Double|Requested Total Memory by User (MB)| |requestedCpu| Double|Requested CPU by User (%)| |asignedMemOnHeap| Double|Asigned On-Heap Memory by Scheduler (MB)| |asignedMemOfHeap| Double|Asigned Of-Heap Memory by Scheduler (MB)| |asignedTotalMem| Double|Asigned Total Memory by Scheduler (MB)| |asignedCpu| Double|Asigned CPU by Scheduler (%)|

Sample response:

`json {

"topologies": [ {

"id": "WordCount3-1-1402960825", "name": "WordCount3", "status": "ACTIVE", "uptime": "6m 5s", "uptimeSeconds": 365, "tasksTotal": 28, "workersTotal": 3, "executorsTotal": 28, "replicationCount": 1, "requestedMemOnHeap": 640, "requestedMemOfHeap": 128, "requestedTotalMem": 768, "requestedCpu": 80, "asignedMemOnHeap": 640, "asignedMemOfHeap": 128, "asignedTotalMem": 768, "asignedCpu": 80

}

] "schedulerDisplayResource": true

}

`

# /api/v1/topology-workers/:id (GET)

Returns the worker' information (host and port) for a topology.

Response fields:

|Field |Value | Description| | -| -| |hostPortList| List| Workers' information for a topology| |name| Integer| Logviewer Port|

Sample response:

`json {

"hostPortList":[ {

"host":"192.168.202.2",

- "port":6701

}, {

"host":"192.168.202.2",

- "port":6702


}, {

"host":"192.168.202.3", "port":670

} ],

"logviewerPort":8 0 }

`

# /api/v1/topology/:id (GET)

Returns topology information and statistics. Substitute id with topology id.

Request parameters:

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |window |String. Default value :al-time| Window duration for metrics in seconds| |sys |String. Values 1 or 0. Default value 0| Controls including sys stats part of the response|

Response fields:

|Field |Value |Description| | -| -| |id| String| Topology Id| |name| String |Topology Name| |uptime| String |How long the topology has ben runing| |uptimeSeconds| Integer |How long the topology has ben runing in seconds| |status| String |Curent status of the topology, e.g. "ACTIVE"| |tasksTotal| Integer |Total number of tasks for this topology| |workersTotal| Integer |Number of workers used for this topology| |executorsTotal| Integer |Number of executors used for this topology| |msgTimeout| Integer | Number of seconds a tuple has before the spout considers it failed | |windowHint| String | window param value in "h ms" format. Default value is "Al Time"| |schedulerDisplayResource| Bolean | Whether to display scheduler resource information| |topologyStats| Aray | Aray of al the topology related stats per time window| |topologyStats.windowPrety| String |Duration pased in H  M  S format| |topologyStats.window| String |User requested time window for metrics| |topologyStats.emited| Long |Number of mesages emited in given window| |topologyStats.trasfered| Long |Number mesages transfered in given window|

|topologyStats.completeLatency| String (double value returned in String format) |Total latency for procesing the mesage| |topologyStats.acked| Long |Number of mesages acked in given window| |topologyStats.failed| Long |Number of mesages failed in given window| |spouts| Aray | Aray of al the spout components in the topology| |spouts.spoutId| String |Spout id| |spouts.executors| Integer |Number of executors for the spout| |spouts.emited| Long |Number of mesages emited in given window | |spouts.completeLatency| String (double value returned in String format) |Total latency for procesing the mesage| |spouts.transfered| Long |Total number of mesages transfered in given window| |spouts.tasks| Integer |Total number of tasks for the spout| |spouts.lastEror| String |Shows the last eror hapened in a spout| |spouts.erorLapsedSecs| Integer | Number of seconds elapsed since that last eror hapened in a spout| |spouts.erorWorkerLogLink| String | Link to the worker log that reported the exception | |spouts.acked| Long |Number of mesages acked| |spouts.failed| Long |Number of mesages failed| |bolts| Aray | Aray of bolt components in the topology| |bolts.boltId| String |Bolt id| |bolts.capacity| String (double value returned in String format) |This value indicates number of mesages executed * average execute latency / time window| |bolts.procesLatency| String (double value returned in String format)|Average time of the bolt to ack a mesage after it was received| |bolts.executeLatency| String (double value returned in String format) |Average time to run the execute method of the bolt| |bolts.executors| Integer |Number of executor tasks in the bolt component| |bolts.tasks| Integer |Number of instances of bolt| |bolts.acked| Long |Number of tuples acked by the bolt| |bolts.failed| Long |Number of tuples failed by the bolt| |bolts.lastEror| String |Shows the last eror ocured in the bolt| |bolts.erorLapsedSecs| Integer |Number of seconds elapsed since that last eror hapened in a bolt| |bolts.erorWorkerLogLink| String | Link to the worker log that reported the exception |

|bolts.emited| Long |Number of tuples emited| |replicationCount| Integer |Number of nimbus hosts on which this topology code is replicated|

Examples:

`no-highlight

- 1. htp:/ui-daemon-host-name:8080/api/v1/topology/WordCount3-1-1402960825
- 2. htp:/ui-daemon-host-name:8080/api/v1/topology/WordCount3-1-1402960825?sys=1
- 3. htp:/ui-daemon-host-name:8080/api/v1/topology/WordCount3-1-1402960825?window=60 `


Sample response:

`json {

"name": "WordCount3", "id": "WordCount3-1-1402960825", "workersTotal": 3, "window": "60", "status": "ACTIVE", "tasksTotal": 28, "executorsTotal": 28, "uptime": "29m 19s", "uptimeSeconds": 1759, "msgTimeout": 30, "windowHint": "10m 0s", "schedulerDisplayResource": true, "topologyStats": [

{

"windowPrety": "10m 0s", "window": "60", "emited": 397960, "transfered": 21380, "completeLatency": "0. 0",

"acked": 213460, "failed": 0

}, {

"windowPrety": "3h 0m 0s", "window": "1080", "emited": 190260, "transfered": 638260, "completeLatency": "0. 0", "acked": 638280, "failed": 0

}, {

"windowPrety": "1d 0h 0m 0s", "window": "8640", "emited": 190260, "transfered": 638260, "completeLatency": "0. 0", "acked": 638280, "failed": 0

}, {

"windowPrety": "Al time", "window": ":al-time", "emited": 190260, "transfered": 638260, "completeLatency": "0. 0", "acked": 638280, "failed": 0

}

], "spouts": [

{

"executors": 5,

"emited": 2 80, "completeLatency": "0. 0", "transfered": 2 80, "acked": 0, "spoutId": "spout", "tasks": 5, "lastEror": ", "erorLapsedSecs": nul, "failed": 0

} ],

"bolts": [ {

"executors": 12, "emited": 184580, "transfered": 0, "acked": 184640, "executeLatency": "0.048", "tasks": 12, "executed": 184620, "procesLatency": "0.043", "boltId": "count", "lastEror": ", "erorLapsedSecs": nul, "capacity": "0.03", "failed": 0

}, {

"executors": 8, "emited": 18450, "transfered": 18450, "acked": 2820, "executeLatency": "0.024", "tasks": 8,

"executed": 28780, "procesLatency": "2.12", "boltId": "split", "lastEror": ", "erorLapsedSecs": nul, "capacity": "0. 0", "failed": 0

}

], "configuration": {

"storm.id": "WordCount3-1-1402960825", "dev.zokeper.path": "/tmp/dev-storm-zokeper", "topology.tick.tuple.freq.secs": nul, "topology.builtin.metrics.bucket.size.secs": 60, "topology.fal.back.on.java.serialization": true, "topology.max.eror.report.per.interval": 5, "zmq.linger.milis": 5 0, "topology.skip.mising.kryo.registrations": false, "storm.mesaging.nety.client_worker_threads": 1, "ui.childopts": "-Xmx768m", "storm.zokeper.sesion.timeout": 2 0, "nimbus.reasign": true, "topology.trident.batch.emit.interval.milis": 50, "storm.mesaging.nety.flush.check.interval.ms": 10, "nimbus.monitor.freq.secs": 10, "logviewer.childopts": "-Xmx128m", "java.library.path": "/usr/local/lib:/opt/local/lib:/usr/lib", "topology.executor.send.bufer.size": 1024, "storm.local.dir": "storm-local", "storm.mesaging.nety.bufer_size": 524280, "supervisor.worker.start.timeout.secs": 120, "topology.enable.mesage.timeouts": true, "nimbus.cleanup.inbox.freq.secs": 60, "nimbus.inbox.jar.expiration.secs": 360,

"drpc.worker.threads": 64, "topology.worker.shared.thread.pol.size": 4, "nimbus.seds": [

"hw10843.local"

], "storm.mesaging.nety.min_wait_ms": 10, "storm.zokeper.port": 2181, "transactional.zokeper.port": nul, "topology.executor.receive.bufer.size": 1024, "transactional.zokeper.servers": nul, "storm.zokeper.rot": "/storm", "storm.zokeper.retry.intervalceiling.milis": 3 0, "supervisor.enable": true, "storm.mesaging.nety.server_worker_threads": 1

}, "replicationCount": 1

}

`

# /api/v1/topology/:id/component/:component (GET)

Returns detailed metrics and executor information

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |component |String (required)| Component Id | |window |String. Default value :al-time| window duration for metrics in seconds| |sys |String. Values 1 or 0. Default value 0| controls including sys stats part of the response|

Response fields:

|Field |Value |Description|

| -| -| |id| String | Component id| |name | String | Topology name| |componentType | String | component type: SPOUT or BOLT| |windowHint| String | window param value in "h ms" format. Default value is "Al Time"| |executors| Integer |Number of executor tasks in the component| |componentErors| Aray of Erors | List of component erors| |componentErors.erorTime| Long | Timestamp when the exception ocured (Prior to 0.1.0, this field was named 'time'.)| |componentErors.erorHost| String | host name for the eror| |componentErors.erorPort| String | port for the eror| |componentErors.eror| String |Shows the eror hapened in a component| |componentErors.erorLapsedSecs| Integer | Number of seconds elapsed since the eror hapened in a component | |componentErors.erorWorkerLogLink| String | Link to the worker log that reported the exception | |topologyId| String | Topology id| |tasks| Integer |Number of instances of component| |window |String. Default value "Al Time" | window duration for metrics in seconds| |spoutSumary or boltStats| Aray |Aray of component stats. *Please note this element tag can be spoutSumary or boltStats depending on the componentType*| |spoutSumary.windowPrety| String |Duration pased in H  M  S format| |spoutSumary.window| String | window duration for metrics in seconds| |spoutSumary.emited| Long |Number of mesages emited in given window | |spoutSumary.completeLatency| String (double value returned in String format) |Total latency for procesing the mesage| |spoutSumary.transfered| Long |Total number of mesages transfered in given window| |spoutSumary.acked| Long |Number of mesages acked| |spoutSumary.failed| Long |Number of mesages failed| |boltStats.windowPrety| String |Duration pased in H  M  S format| |boltStats.window| String | window duration for metrics in seconds| |boltStats.transfered| Long |Total number of mesages transfered in given window| |boltStats.procesLatency| String (double value returned in String format)|Average time of the bolt to ack a mesage after it was received| |boltStats.acked| Long |Number of mesages acked|

|boltStats.failed| Long |Number of mesages failed| |profilingAndDebugingCapable| Bolean |true if there is suport for Profiling and Debuging Actions| |profileActionEnabled| Bolean |true if worker profiling (Java Flight Recorder) is enabled| |profilerActive| Aray |Aray of curently active Profiler Actions|

Examples:

`no-highlight

- 1. htp:/ui-daemon-host-name:8080/api/v1/topology/WordCount3-11402960825/component/spout
- 2. htp:/ui-daemon-host-name:8080/api/v1/topology/WordCount3-11402960825/component/spout?sys=1
- 3. htp:/ui-daemon-host-name:8080/api/v1/topology/WordCount3-11402960825/component/spout?window=60


`

Sample response:

`json {

"name": "WordCount3", "id": "spout", "componentType": "spout", "windowHint": "10m 0s", "executors": 5, "componentErors":[{"erorTime": 140606074 0,

"erorHost": "10.1.1.70", "erorPort": 6701, "erorWorkerLogLink": "htp:/10.1.1.7 8 0/log?file=worker-6701.log", "erorLapsedSecs": 16,

"eror": "java.lang.RuntimeException: java.lang.StringIndexOutOfBoundsException: Some Eror\n\tat org.apache.storm.utils.DisruptorQueue.consumeBatchToCursor(DisruptorQueue.java:128)\n\tat org.apache.storm.utils.DisruptorQueue.consumeBatchWhenAvailable(DisruptorQueue.java: 9)\n\ta t org.apache.storm.disruptor$consume_batch_when_available.invoke(disruptor.clj:80)\n\tat backtype.more."

}], "topologyId": "WordCount3-1-1402960825", "tasks": 5, "window": "60", "profilerActive": [

{

"host": "10.1.1.70", "port": "6701", "dumplink":"htp:\/\/10.1.1.70 8 0\/dumps\/ex-1-145271803\/10.1.1.70%3A6701", "timestamp":"576328"

}

], "profilingAndDebugingCapable": true, "profileActionEnabled": true, "spoutSumary": [

{

"windowPrety": "10m 0s", "window": "60", "emited": 2850, "transfered": 28460, "completeLatency": "0. 0", "acked": 0, "failed": 0

}, {

"windowPrety": "3h 0m 0s", "window": "1080", "emited": 127640,

"transfered": 12740, "completeLatency": "0. 0", "acked": 0, "failed": 0

}, {

"windowPrety": "1d 0h 0m 0s", "window": "8640", "emited": 127640, "transfered": 12740, "completeLatency": "0. 0", "acked": 0, "failed": 0

}, {

"windowPrety": "Al time", "window": ":al-time", "emited": 127640, "transfered": 12740, "completeLatency": "0. 0", "acked": 0, "failed": 0

}

], "outputStats": [

{

"stream": "_metrics", "emited": 40, "transfered": 0, "completeLatency": "0", "acked": 0, "failed": 0

}, {

"stream": "default", "emited": 28460, "transfered": 28460, "completeLatency": "0", "acked": 0, "failed": 0

}

], "executorStats": [

{

"workerLogLink": "htp:/10.1.1.7 8 0/log?file=worker-6701.log", "emited": 5720, "port": 6701, "completeLatency": "0. 0", "transfered": 5720, "host": "10.1.1.7", "acked": 0, "uptime": "43m 4s", "uptimeSeconds": 2584, "id": "[24-24]", "failed": 0

}, {

"workerLogLink": "htp:/10.1.1.7 8 0/log?file=worker-6703.log", "emited": 570, "port": 6703, "completeLatency": "0. 0", "transfered": 570, "host": "10.1.1.7", "acked": 0, "uptime": "42m 57s", "uptimeSeconds": 257, "id": "[25-25]", "failed": 0

}, {

"workerLogLink": "htp:/10.1.1.7 8 0/log?file=worker-6702.log", "emited": 570, "port": 6702, "completeLatency": "0. 0", "transfered": 5680, "host": "10.1.1.7", "acked": 0, "uptime": "42m 57s", "uptimeSeconds": 257, "id": "[26-26]", "failed": 0

}, {

"workerLogLink": "htp:/10.1.1.7 8 0/log?file=worker-6701.log", "emited": 570, "port": 6701, "completeLatency": "0. 0", "transfered": 5680, "host": "10.1.1.7", "acked": 0, "uptime": "43m 4s", "uptimeSeconds": 2584, "id": "[27-27]", "failed": 0

}, {

"workerLogLink": "htp:/10.1.1.7 8 0/log?file=worker-6703.log", "emited": 5680, "port": 6703, "completeLatency": "0. 0", "transfered": 5680, "host": "10.1.1.7",

"acked": 0, "uptime": "42m 57s", "uptimeSeconds": 257, "id": "[28-28]", "failed": 0

} ]

}

`

# Profiling and Debuging GET Operations

# /api/v1/topology/:id/profiling/start/:host-port/:timeout (GET)

Request to start profiler on worker with timeout. Returns status and link to profiler artifacts for worker.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |host-port |String (required)| Worker Id | |timeout |String (required)| Time out for profiler to stop in minutes |

Response fields:

|Field |Value |Description| | - | - | -| |id| String | Worker id| |status | String | Response Status | |timeout | String | Requested timeout |dumplink | String | Link to logviewer URL for worker profiler documents.|

Examples:

`no-highlight

- 1. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/start/10.1.1.7 6701/10
- 2. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/start/10.1.1.7 6701/5
- 3. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/start/10.1.1.7 6701/20


`

Sample response:

`json {

"status": "ok", "id": "10.1.1.7 6701", "timeout": "10", "dumplink": "htp:\/\/10.1.1.7 8 0\/dumps\/wordcount-1-14614150\/10.1.1.7%3A6701"

}

`

# /api/v1/topology/:id/profiling/dumprofile/:host-port (GET)

Request to dump profiler recording on worker. Returns status and worker id for the request.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |host-port |String (required)| Worker Id |

Response fields:

|Field |Value |Description| | - | - | -| |id| String | Worker id|

|status | String | Response Status |

Examples:

`no-highlight

1. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/dumprofile/10.1.1.7 6701

`

Sample response:

`json {

"status": "ok", "id": "10.1.1.7 6701",

}

`

# /api/v1/topology/:id/profiling/stop/:host-port (GET)

Request to stop profiler on worker. Returns status and worker id for the request.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |host-port |String (required)| Worker Id |

Response fields:

|Field |Value |Description| | - | - | -| |id| String | Worker id| |status | String | Response Status |

Examples:

`no-highlight

1. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/stop/10.1.1.7 6701

`

Sample response:

`json {

"status": "ok", "id": "10.1.1.7 6701",

}

`

# /api/v1/topology/:id/profiling/dumpjstack/:host-port (GET)

Request to dump jstack on worker. Returns status and worker id for the request.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |host-port |String (required)| Worker Id |

Response fields:

|Field |Value |Description| | - | - | -| |id| String | Worker id| |status | String | Response Status |

Examples:

`no-highlight

1. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/dumpjstack/10.1.1.7 6701

`

Sample response:

`json {

"status": "ok", "id": "10.1.1.7 6701",

}

`

# /api/v1/topology/:id/profiling/dumpheap/:host-port (GET)

Request to dump heap (jmap) on worker. Returns status and worker id for the request.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |host-port |String (required)| Worker Id |

Response fields:

|Field |Value |Description| | - | - | -| |id| String | Worker id| |status | String | Response Status |

Examples:

`no-highlight

1. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/dumpheap/10.1.1.7 6701

`

Sample response:

`json {

"status": "ok", "id": "10.1.1.7 6701",

}

`

# /api/v1/topology/:id/profiling/restartworker/:host-port (GET)

Request to request the worker. Returns status and worker id for the request.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |host-port |String (required)| Worker Id |

Response fields:

|Field |Value |Description| | - | - | -| |id| String | Worker id| |status | String | Response Status |

Examples:

`no-highlight

1. htp:/ui-daemon-host-name:8080/api/v1/topology/wordcount-114614150/profiling/restartworker/10.1.1.7 6701

`

Sample response:

`json {

"status": "ok", "id": "10.1.1.7 6701",

}

`

# POST Operations

# /api/v1/topology/:id/activate (POST)

Activates a topology.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id |

Sample Response:

`json {"topologyOperation":"activate","topologyId":"wordcount-1-142030865","status":"suces"} `

# /api/v1/topology/:id/deactivate (POST)

Deactivates a topology.

|Parameter |Value|Description | | -| -| -|

|id |String (required)| Topology Id |

Sample Response:

`json {"topologyOperation":"deactivate","topologyId":"wordcount-1-142030865","status":"suces"} `

# /api/v1/topology/:id/rebalance/:wait-time (POST)

Rebalances a topology.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |wait-time |String (required)| Wait time before rebalance hapens | |rebalanceOptions| Json (optional) | topology rebalance options |

Sample rebalanceOptions json:

`json {"rebalanceOptions" : {"numWorkers" : 2, "executors" : {"spout" :4, "count" : 10}, "calback" : "fo"} `

Examples:

`no-highlight curl -i -b ~/cokiejar.txt -c ~/cokiejar.txt -X POST

- -H "Content-Type: aplication/json"
- -d '{"rebalanceOptions": {"numWorkers": 2, "executors": { "spout" : "5", "split": 7, "count": 5}, "calback":"fo"}'


htp:/localhost:8080/api/v1/topology/wordcount-1-142030865/rebalance/0 `

Sample Response:

`json {"topologyOperation":"rebalance","topologyId":"wordcount-1-142030865","status":"suces"} `

# /api/v1/topology/:id/kil/:wait-time (POST)

Kils a topology.

|Parameter |Value|Description | | -| -| -| |id |String (required)| Topology Id | |wait-time |String (required)| Wait time before rebalance hapens |

Caution: Smal wait times (0-5 seconds) may increase the probability of tri gering the bug reported in [STORM-12](htps:/isues.apache.org/jira/browse/STORM-12), which may result in broker Super visor daemons.

Sample Response:

`json {"topologyOperation":"kil","topologyId":"wordcount-1-142030865","status":"suces"} `

# API erors

The API returns 50 HTP status codes in case of any erors.

Sample response:

`json {

"eror": "Internal Server Eror", "erorMesage": "java.lang.NulPointerException\n\tat clojure.core$name.invoke(core.clj:1505)\n\t

at org.apache.storm.ui.core$component_page.invoke(core.clj:752)\n\tat org.apache.storm.ui.core $fn_76.invoke(core.clj:782)\n\tat compojure.core$make_route$fn_575.invoke(core.clj:93)\n\t at compojure.core$if_route$fn_5743.invoke(core.clj:39)\n\tat compojure.core$if_method$fn_57 36.invoke(core.clj:24)\n\tat compojure.core$routing$fn_5761.invoke(core.clj:106)\n\tat clojure.cor e$some.invoke(core.clj:243)\n\tat compojure.core$routing.doInvoke(core.clj:106)\n\tat clojure.lan g.RestFn.aplyTo(RestFn.java:139)\n\tat clojure.core$aply.invoke(core.clj:619)\n\tat compojure.cor e$routes$fn_5765.invoke(core.clj: 1)\n\tat ring.mi dleware.reload$wrap_reload$fn_680.invok e(reload.clj:14)\n\tat org.apache.storm.ui.core$catch_erors$fn_780.invoke(core.clj:836)\n\tat ri ng.mi dleware.keyword_params$wrap_keyword_params$fn_6319.invoke(keyword_params.clj:2 7)\n\tat ring.mi dleware.nested_params$wrap_nested_params$fn_6358.invoke(nested_params.c lj:65)\n\tat ring.mi dleware.params$wrap_params$fn_6291.invoke(params.clj: 5)\n\tat ring.mi dl eware.multipart_params$wrap_multipart_params$fn_6386.invoke(multipart_params.clj:103)\n\tat ring.mi dleware.flash$wrap_flash$fn_675.invoke(flash.clj:14)\n\tat ring.mi dleware.sesion$wra p_sesion$fn_ 64.invoke(sesion.clj:43)\n\tat ring.mi dleware.cokies$wrap_cokies$fn_659 5.invoke(cokies.clj:160)\n\tat ring.adapter.jety$proxy_handler$fn_612.invoke(jety.clj:16)\n\tat ri ng.adapter.jety.proxy$org.mortbay.jety.handler.AbstractHandler$0.handle(Unknown Source)\n\tat org.mortbay.jety.handler.HandlerWraper.handle(HandlerWraper.java:152)\n\tat org.mortbay.jet y.Server.handle(Server.java:326)\n\tat org.mortbay.jety.HtpConection.handleRequest(HtpCone ction.java:542)\n\tat org.mortbay.jety.HtpConection$RequestHandler.headerComplete(HtpCon nection.java:928)\n\tat org.mortbay.jety.HtpParser.parseNext(HtpParser.java:549)\n\tat org.mort bay.jety.HtpParser.parseAvailable(HtpParser.java:212)\n\tat org.mortbay.jety.HtpConection.han dle(HtpConection.java:404)\n\tat org.mortbay.jety.bio.SocketConector$Conection.run(Socket Conector.java: 28)\n\tat org.mortbay.thread.QueuedThreadPol$PolThread.run(QueuedThreadP

ol.java:582)\n" }

`

