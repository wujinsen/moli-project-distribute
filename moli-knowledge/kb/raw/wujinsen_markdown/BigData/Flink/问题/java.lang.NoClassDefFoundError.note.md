Eror: A JNI eror has ocured, please check your instalation and try again Exception in thread "main" java.lang.NoClasDefFoundEror: org/apache/flink/api/comon/functions/Function

at java.lang.ClasLoader.defineClas1(Native Method)

解决⽅式: idea运⾏项⽬处: edit configurations勾选 include depencies with "provided" scope 添加provided scope依赖jar包

或者modules处修改对应jar包scope: provided->compile

