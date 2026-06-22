# Error attempting to get column ‘time‘ from result set java.sql.SQLException: ⽆效的列类型 解决⽅法

Eror atempting to get column 'time' from result setjava.sql.SQLException: ⽆效的列类型

的原因中，我个⼈是使⽤了mybatis-plus+oracle，从旧项⽬迁移到新项⽬时⽽导致的。

主要是mybaitisplus，mybatis，druid和ojdbc的版本联合问题导致的

解决⽅法介绍

- 1、使⽤旧的mybatis-plus版本3.1.0。druid版本在1.1.21以下。jdbc版本在4.2以下；
- 2、更新最新的版本mybatis-plus3.1.15，mybatis在3.5.1以上，druid版本1.1.2，mysql-conectorjava:8.0.26


