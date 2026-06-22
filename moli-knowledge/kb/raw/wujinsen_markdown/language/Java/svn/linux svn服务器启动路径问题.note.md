版本库创建路径 svnadmin create /opt/svn/svnrepos svn启动路径 svnserve -d -r /opt/svn/svnrepos 此时直接访问 svn:/192.168.49.17 可以访问 svn:/192.168.49.17/svnrepos 不可以访问 svn启动路径 svnserve -d -r /opt/svn/ 此时直接访问 svn:/192.168.49.17/svnrepos 可以访问 svn:/192.168.49.17 不可以访问

启动另⼀个svn，端⼝号不重复 svnserve -d-listen-port 3691 -r /opt/svn/

