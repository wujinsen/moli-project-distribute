# Install Ambari 2.2.2 from Public Repositories

## Step1: Download the Ambari repository on the Ambari Server host

<table>
  <tr>
    <th>For Redhat/CentOS/Oracle: cd /etc/yum.repos.d/ wget <ambari-repo-url><br><br>For SUSE: cd /etc/zypp/repos.d wget <ambari-repo-url><br><br>For Ubuntu/Debian: cd /etc/apt/sources.list.d wget <ambari-repo-url></th>
  </tr>
</table>


Choose an appropriate <ambari-repo-url> based on the platform used:

<table>
  <tr>
    <th>(Redhat / CentOS / Oracle) 6</th>
    <th>htp:/public-repo-1.hortonworks.com/ambari/ce</th>
  </tr>
  <tr>
    <td>(Redhat / CentOS / Oracle) 7</td>
    <td>ntos6/2.x/updates/2.2.2.0/ambari.repo htp:/public-repo-1.hortonworks.com/ambari/c</td>
  </tr>
  <tr>
    <td>SUSE1</td>
    <td>entos7/2.x/updates/2.2.2.0/ambari.repo htp:/public-repo-1.hortonworks.com/ambari/su</td>
  </tr>
  <tr>
    <td>Ubuntu 12</td>
    <td>se1/2.x/updates/2.2.2.0/ambari.repo htp:/public-repo-1.hortonworks.com/ambari/u</td>
  </tr>
  <tr>
    <td>Ubuntu 14</td>
    <td>buntu12/2.x/updates/2.2.2.0/ambari.list htp:/public-repo-1.hortonworks.com/ambari/u</td>
  </tr>
  <tr>
    <td>Debian 7</td>
    <td>buntu14/2.x/updates/2.2.2.0/ambari.list htp:/public-repo-1.hortonworks.com/ambari/d</td>
  </tr>
</table>


ebian7/2.x/updates/2.2.2.0/ambari.list

- Step 2: Install, Setup, and Start Ambari Server Install Ambari Server from the public Ambari repository:


<table>
  <tr>
    <th>For Redhat/CentOS/Oracle:<br><br>yum install ambari-server For SUSE:<br><br>zypper install ambari-server For Ubuntu/Debian:<br><br>apt-key adv --recv-keys --keyserver keyserver.ubuntu.com<br><br>B9733A7A07513CAD apt-get update apt-get install ambari- erver</th>
  </tr>
</table>


s

Run the setup command to configure your Ambari Server, Database, JDK, LDAP, and other options:

<table>
  <tr>
    <th>ambari-server setup</th>
  </tr>
</table>


Start Ambari Server:

<table>
  <tr>
    <th>ambari-server start</th>
  </tr>
</table>


- Step 3: Deploy Cluster using Ambari Web UI Open up a web browser and go to http://<ambari-server-host>:8080. Log in with username admin and password admin and follow on-screen instructions.


