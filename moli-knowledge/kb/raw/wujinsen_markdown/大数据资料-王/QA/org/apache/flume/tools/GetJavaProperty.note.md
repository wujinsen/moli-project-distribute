Exception in thread "main" java.lang.NoClasDefFoundEror:org/apache/flume/tols/GetJavaProperty Caused by:java.lang.ClasNotFoundException: org.apache.flume.tols.GetJavaProperty

at java.net.URLClasLoader$1.run(URLClasLoader.java:202) at java.security.AcesControler.doPrivileged(Native Method) at java.net.URLClasLoader.findClas(URLClasLoader.java:190) at java.lang.ClasLoader.loadClas(ClasLoader.java:306) at sun.misc.Launcher$ApClasLoader.loadClas(Launcher.java:301) at java.lang.ClasLoader.loadClas(ClasLoader.java:247)

Could

not find the main clas: org.apache.flume.tols.GetJavaProperty. Program wil exit.

错误: 找不到或⽆法加载主类 org.apache.flume.tols.GetJavaProperty原因：1、jdk冲突2、安装了hbase就会报着 个错解决：1、卸载openjdk2、安装jdk7.3、将hbase的hbase.env.sh的⼀⾏配置注释掉# Extra Java CLASPATH elements. Optional.#export HBASE_CLASPATH=/home/hadop/hbase/conf4、或者将HBASE_CLASPATH改 为JAVA_CLASPATH,配置如下# Extra Java CLASPATH elements. Optional.export JAVA_CLASPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tols.jar

