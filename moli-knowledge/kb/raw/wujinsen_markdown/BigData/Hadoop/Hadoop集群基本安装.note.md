3台机器 192.168.27.1 hadop01

- 192.168.27.12 hadop02
- 192.168.27.13 hadop03 说明: hadop01是master, hadop02,hadop03是slave


vi /etc/sysconfig/network HOSTNAME=hadoop01 --第⼀台机器，每台都修改对应的 vi /etc/hosts

<table>
  <tr>
    <th>12.1 .2.3 o ee 1<br>12.1 .2.4 o ee 2<br>12.1 .2.5 zokeper03 12.1 .2.1 hadop01 storm01<br><br><br>12.1 .2.12 o02 o 02<br>12.1 .2.13 hadop03 storm03<br>12.1 .2.14 1<br>12.1 .2.15 2<br></th>
  </tr>
</table>


192.168.27.16 spark03

修改ip vi /etc/sysconfig/network-scripts/ifcfg-eth0

<table>
  <tr>
    <th>DEVICE=eth0 TYPE=Ethernet ONBOT=yes BOTPROTO=static IPADR=192.168.27.1 NETMASK=25.25.25.0<br><br></th>
  </tr>
</table>


配置 sh免密登陆 sh-keygen –t rsa 配置防⽕墙service iptables status chkconfig iptables of 开机不启动防⽕墙

ssh⽆密码登录 ssh-keygen ssh-copy-id hadoop02

JDK环境安装 jdk-7u45-linux-x64.tar.gz make dir /usr/local/java 创建⽬录 解压: tar -zxvf jdk-7u45-linux-x64.tar.gz -C /usr/local/java/ 把hadop01下的jdk⽂件夹发送到hadop02,hadop03 scp /usr/local/jdk1.7.0_45 hadop02:$PWD

配置环境变量 vi /etc/profile

<table>
  <tr>
    <th>JAVA_HOME=/usr/local/jdk1.7.0_45 PATH=$JAVA_HOME/bin:$PATH</th>
  </tr>
</table>


export CLASPATH=.:$JAVA_HOME/jre/lib

source /etc/profile 刷新 javac 查看

