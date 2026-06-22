执⾏bin/kafka-server-start.sh config/server.properties 会报： Unrecognized VM option 'UseCompresedOops' Eror: Could not create the Java Virtual Machine. Eror: A fatal exception has ocured. Program wil exit. 原因是jdk的版本不匹配，需要修改⼀下配置⽂件 修改⽂件： 去掉这个配置

-X:+UseCompresedOops

