执⾏时需要传⼊的参数说明

Usage: spark-submit [options] <app jar | python file> [app options]

<table>
  <tr>
    <th>参数名称</th>
    <th>含义</th>
  </tr>
  <tr>
    <td>-master MASTER_URL</td>
    <td>可以是spark:/host:port, mesos:/host:port, yarn, yarn-</td>
  </tr>
  <tr>
    <td>-deploy-mode DEPLOY_MODE</td>
    <td>cluster,yarn-client, local<br><br>Driver程序运⾏的地⽅，client或者cluster</td>
  </tr>
  <tr>
    <td>-clas CLAS_NAME</td>
    <td>主类名称，含包名</td>
  </tr>
  <tr>
    <td>-name NAME</td>
    <td>Aplication名称</td>
  </tr>
  <tr>
    <td>-jars JARS</td>
    <td>Driver依赖的第三⽅jar包</td>
  </tr>
  <tr>
    <td>-py-files PY_FILES</td>
    <td>⽤逗号隔开的放置在Python应⽤程序PYTHONPATH上 的.zip, .eg, .py⽂件列表</td>
  </tr>
  <tr>
    <td>-files FILES</td>
    <td>⽤逗号隔开的要放置在每个executor⼯作⽬录的⽂件列表</td>
  </tr>
  <tr>
    <td>-properties-file FILE</td>
    <td>设置应⽤程序属性的⽂件路径，默认是conf/spark-defaults.conf</td>
  </tr>
  <tr>
    <td>-driver-memory MEM</td>
    <td>Driver程序使⽤内存⼤⼩</td>
  </tr>
  <tr>
    <td>-driver-java-options</td>
    <td> </td>
  </tr>
  <tr>
    <td>-driver-library-path</td>
    <td>Driver程序的库路径</td>
  </tr>
  <tr>
    <td>-driver-clas-path</td>
    <td>Driver程序的类路径</td>
  </tr>
  <tr>
    <td>-executor-memory MEM</td>
    <td>executor内存⼤⼩，默认1G</td>
  </tr>
  <tr>
    <td>-driver-cores NUM</td>
    <td>Driver程序的使⽤CPU个数，仅限于Spark Alone模式</td>
  </tr>
  <tr>
    <td>-supervise</td>
    <td>失败后是否重启Driver，仅限于Spark Alone模式</td>
  </tr>
  <tr>
    <td>-total-executor-cores NUM</td>
    <td>executor使⽤的总核数，仅限于Spark Alone、Spark on Mesos 模式</td>
  </tr>
  <tr>
    <td>-executor-cores NUM</td>
    <td>每个executor使⽤的内核数，默认为1，仅限于Spark on Yarn模 式</td>
  </tr>
  <tr>
    <td>-queue QUEUE_NAME</td>
    <td>提交应⽤程序给哪个YARN的队列，默认是default队列，仅限于 Spark on Yarn模式</td>
  </tr>
  <tr>
    <td>-num-executors NUM</td>
    <td>启动的executor数量，默认是2个，仅限于Spark on Yarn模式</td>
  </tr>
  <tr>
    <td>-archives ARCHIVES</td>
    <td>仅限于Spark on Yarn模式</td>
  </tr>
</table>


