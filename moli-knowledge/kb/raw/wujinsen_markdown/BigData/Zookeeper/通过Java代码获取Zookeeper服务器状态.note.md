有的时候，我们想知道Zookeeper集群中哪些服务器属于Leader，哪些属于Follower，哪些属于Observer。 通过命令，可以很容易的获取到Zookeeper服务器的状态：

[hadoop@hadoopcluster84 bin]$ ./zkServer.sh status JMX enabled by default Using config: /home/hadoop/apache/zookeeper-3.4.5/bin/../conf/zoo.cfg Mode: follower

# 下⾯我们⽤Java代码来获取Zookeeper服务器的状态：

public class ServerStatus { @Test public void serverStatus() throws IOException {

String host = "10.0.1.84"; int port = 2181; String cmd = "stat";

Socket sock = new Socket(host, port); BufferedReader reader = null; try {

OutputStream outstream = sock.getOutputStream(); // 通过Zookeeper的四字命令获取服务器的状态 outstream.write(cmd.getBytes()); outstream.flush(); sock.shutdownOutput();

reader = new BufferedReader(new InputStreamReader(sock.getInputStream())); String line; while ((line = reader.readLine()) != null) {

if (line.indexOf("Mode: ") != -1) {

System.out.println(line.replaceAll("Mode: ", "").trim()); }

}

} finally { sock.close(); if (reader != null) {

reader.close(); }

} }

}

# 输出结果：

follower

# 上⾯的代码可以改改，通过其他四字命令获取zookeeper集群的状 态。

