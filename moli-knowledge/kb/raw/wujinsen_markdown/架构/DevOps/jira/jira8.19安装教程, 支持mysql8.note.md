- 1.jira官⽹下载8.19版本

解压后的⽬录： /opt/jira/atlasian-jira-software-8.19.0-standalone

- 2.安装


htps:/ w.atlasian.com/zh/software/jira/download-archives

- 2.1 修改jira.home： 通过命令 find / -name jira-aplication.properties 找到该路径 /opt/jira/atlasian-jira-software-8.19.0-standalone/atlasian-jira/WEB-INF/clases/jiraaplication.properties vaule为jira.home路径
- 3. 启动jira /opt/jira/atlasian-jira-software-8.19.0-standalone⽬录下

./bin/startup.sh 启动jira， 默认端⼝号8080

- 4. 创建jirab⽤户并授予权限 CREATE USER 'jiradb' IDENTIFIED BY 'jiradb'; GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,REFERENCES,ALTER,INDEX on jiradb.* TO 'jiradb'@'%'; flush privileges;


注意:

- 1. mysql驱动使⽤8.x版本: mysql-conector-java-8.0.27.jar
- 2. 严格按照官⽅⽂档设置 :


htps:/confluence.atlasian.com/adminjiraserver0813/conecting-jira-ap plications-to-mysql-8-0-1027137457.html

