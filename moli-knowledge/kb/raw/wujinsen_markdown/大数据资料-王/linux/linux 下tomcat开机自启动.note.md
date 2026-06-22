- 1、把下⾯的代码保存为tomcat⽂件


vi /etc/init.d/tomcat

<table>
  <tr>
    <th>#!/bin/bash # /etc/rc.d/init.d/tomcat # init script for tomcat precesses # # processname: tomcat<br><br>## descriptichkconfigo:n:2345tomcat86 16is a j2se server # description: Start up the Tomcat servlet engine. if [ -f /etc/init.d/functions ]; then . /etc/init.d/functions<br><br>elif [ -f /etc/rc.d/init.d/functions ]; then<br><br>. /etc/rc.d/init.d/functions else<br><br>echo-e "/atomcat: unable to locate functions lib. Cannot continue." exit-1<br><br>fi RETVAL=$? CATALINA_HOME="/usr/local/tomcat"<br><br>casestart)"$1" in if [-f $CATALINA_HOME/bin/startup.sh ];<br><br>then echo $"Starting Tomcat" $CATALINA_HOME/bin/startup.sh<br><br>fi ;;<br><br>stop)<br><br>if [-f $CATALINA_HOME/bin/shutdown.sh ];<br><br>then echo $"Stopping Tomcat" $CATALINA_HOME/bin/shutdown.sh<br><br>fi ;;<br><br>*)<br><br>echexito1$"Usage: $0 {start|stop}" ;;<br><br>esac exit $RETVA<br><br></th>
  </tr>
</table>


L

- 2、并让它成为可执⾏⽂件 chmod 755 tomcat.

chmod 755 /etc/init.d/tomcat

- 3、加⼊开机启动

chkconfig --add tomcat

- 4、在tomcat/bin/catalina.sh⽂件中加⼊以下语句：

export JAVA_HOME=/usr/jdk

export CATALINA_HOME=/usr/local/tomcat

export CATALINA_BASE=/usr/local/tomcat

export CATALINA_TMPDIR=/usr/local/tomcat/temp

- 5、启停 启动tomcat： service tomcat start


停⽌tomcat: service tomcat stop

