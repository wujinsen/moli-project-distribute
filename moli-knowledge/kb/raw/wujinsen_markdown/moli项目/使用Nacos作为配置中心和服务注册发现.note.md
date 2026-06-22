配置中⼼:

- 1.引⼊相关依赖：
- 2.resoureces⽬录下新建botstrap.properties:
- 3. 相关配置: 数据库配置:datasource.properties redis配置： redis.properties mybaits配置: mybatis.properties


<table>
  <tr>
    <th><dependency> <groupId>com.alibaba.cloud</groupId> <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId><br><br></th>
  </tr>
</table>


</dependency>

<table>
  <tr>
    <th>erver.port=25 sprin.aplication.name=user-center-server</th>
  </tr>
</table>


spring.cloud.nacos.config.server-adr=127.0.0.1  848

<table>
  <tr>
    <th>spring.coud.nacos.config.et-config .data-id=datasource.properties<br><br>spring.cloud.nacos.config.ext-config[0].group=moli-user-center spring.cod.nacos.config.et-config .data-id=redis.properties<br>spring.cloud.nacos.config.ext-config[1].group=moli-user-center spring.cod.nacos.config.et-config .data-id=mybatis.properties<br></th>
  </tr>
</table>


spring.cloud.nacos.config.ext-config[2].group=moli-user-center

3.启动nacos服务和本地服务，对应的配置写⼊nacos即可

服务注册发现:

- 1.引⼊相关依赖：


<table>
  <tr>
    <th><dependency> <groupId>com.alibaba.cloud</groupId> <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId> <exclusions><br><br><exclusion> <groupId>com.alibaba.nacos</groupId> <artifactId>nacos-client</artifactId><br><br></exclusion> </exclusions /dependency><br><br><dependency> <groupId>com.alibaba.nacos</groupId> <artifactId>nacos-client</artifactId><br><br></th>
  </tr>
</table>


</dependency>

- 2.resoureces⽬录下新建botstrap.properties:
- 3.启动nacos服务和本地服务即可


<table>
  <tr>
    <th>erver.port=25 sprin.aplication.name=user-center-server</th>
  </tr>
</table>


spring.cloud.nacos.discovery.server-adr=127.0.0.1  848

