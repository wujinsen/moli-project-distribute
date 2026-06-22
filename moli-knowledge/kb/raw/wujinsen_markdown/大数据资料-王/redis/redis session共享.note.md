多台tomcat服务的sesion共享 memcached与redis

由于tomcat的并发数瓶颈问题，可以说使⽤tomcat的web应⽤，⼏乎都存在sesion不同步问题。 借鉴⽹上的资料，我也找时间实验⼀把。 ⽂中涉及的软件下载和安装，⼀⼀略过，想必⼤家也没必要看。 注：本⽂不对memcached和redis做任何⼝⽔讨论，望各个⽹友⾃⾏问⾕歌和度娘。 (个⼈愚⻅，它们作为⼀个软件，能获得各⾃众多⽀持者，想必它们⾃然有各⾃的优点，重点还是从实 际需要出发，选择合适⾃⼰的东东。)

# ⼀、nginx+tomcat+memcached ( )

依赖包下载

- 1.memcached配置：（v1.4.13）

- 节点1（192.168.159.131 1 4）
- 节点2（192.168.159.131 1 3）


- 2.tomcat配置

- tomcat1（192.168.159.128 8081）
- tomcat2（192.168.159.128 8082）


- 3.nginx安装在192.168.159.131。 ⾸先，是配置tomcat，使其将sesion保存到memcached上。有两种⽅法： ⽅法⼀：在server.xml中配置。


找到host节点，加⼊ [html]

view plaincopyprint?

<table>
  <tr>
    <th>![image 1](<redis session共享.note_images/imageFile1.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 2](<redis session共享.note_images/imageFile2.png>)</th>
  </tr>
</table>


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


<Context docBase="/var/ w/html" path=">

<Manager clasName="de.javakafe.web.msm.MemcachedBackupSesionManager "

memcachedNodes="n1 192.168.159.131 1 4 n2 192.168.159.131 1 3" requestUriIgnorePatern=".*\.(png|gif|jpg|cs|js)$" sesionBackupAsync="false" sesionBackupTimeout="3 0" transcoderFactoryClas="de.javakafe.web.msm.serializer.javolution.Javolution

TranscoderFactory"

copyColectionsForSerialization="false" /> </Context>

⽅法⼆：在context.xml中配置。 找到Context节点，加⼊ [html]

view plaincopyprint?

<table>
  <tr>
    <th>![image 3](<redis session共享.note_images/imageFile3.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 4](<redis session共享.note_images/imageFile4.png>)</th>
  </tr>
</table>


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


<Manager clasName="de.javakafe.web.msm.MemcachedBackupSesionManager"

memcachedNodes="n1 192.168.159.131 1 4" requestUriIgnorePatern=".*\.(png|gif|jpg|cs|js)$" sesionBackupAsync="false" sesionBackupTimeout="3 0" transcoderFactoryClas="de.javakafe.web.msm.serializer.javolution.JavolutionTr

anscoderFactory" copyColectionsForSerialization="false" />

其次，配置nginx，⽤于测试sesion保持共享。 [html]

view plaincopyprint?

<table>
  <tr>
    <th>![image 5](<redis session共享.note_images/imageFile5.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 6](<redis session共享.note_images/imageFile6.png>)</th>
  </tr>
</table>


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


upstreamxy.com {

- server 192.168.159.128 8081 ;
- server 192.168.159.128 8082 ;


}

log_format w_xy_com '$remote_adr - $remote_user [$time_local] $request ' '"$status" $body_bytes_sent "$htp_referer"' '"$htp_user_agent" "$htp_x_forwarded_for"';

server {

listen 80; server_name xy.com;

location / { proxy_pas htp:/xy.com; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_adr; proxy_set_header X-Forwarded-For$proxy_ad_x_forwarded_for;

}

aces_log /data/base_files/logs/ w.xy.log w_xy_com; }

最后，将你的应⽤放到两个tomcat中，并依次启动memcached、tomcat、nginx。访问你的 nginx，可以发现两个tomcat中的sesion可以保持共享了。

# ⼆、nginx+tomcat+redis ( )

# 依赖包下载

- 1.redis配置（192.168.159.131 1630）（v2.8.3）
- 2.tomcat配置

- tomcat1（192.168.159.130 8081）
- tomcat2（192.168.159.130 8082）


- 3.nginx安装在192.168.159.131。 ⾸先，是配置tomcat，使其将sesion保存到redis上。有两种⽅法，也是在server.xml或


context.xml中配置，不同的是memcached只需要添加⼀个manager标签，⽽redis需要增加的内容 如下：（注意：valve标签⼀定要在manager前⾯。） [html]

view plaincopyprint?

<table>
  <tr>
    <th>![image 7](<redis session共享.note_images/imageFile7.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 8](<redis session共享.note_images/imageFile8.png>)</th>
  </tr>
</table>


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


<Valve clasName="com.radiadesign.catalina.sesion.RedisSesionHandlerValve" /> <Manager clasName="com.radiadesign.catalina.sesion.RedisSesionManager"

host="192.168.159.131" port="1630" database="0" maxInactiveInterval="60"/>

其次，配置nginx，⽤于测试sesion保持共享。 [html]

view plaincopyprint?

<table>
  <tr>
    <th>![image 9](<redis session共享.note_images/imageFile9.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 10](<redis session共享.note_images/imageFile10.png>)</th>
  </tr>
</table>


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


upstream redis.xy.com {

- server 192.168.159.130 8081;
- server 192.168.159.130 8082;


}

log_format w_xy_com '$remote_adr - $remote_user [$time_local] $request ' '"$status" $body_bytes_sent "$htp_referer"' '"$htp_user_agent" "$htp_x_forwarded_for"';

server {

listen 80; server_name redis.xy.com;

location / { proxy_pas htp:/redis.xy.com; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_adr; proxy_set_header X-Forwarded-For$proxy_ad_x_forwarded_for;

}

aces_log /data/base_files/logs/redis.xy.log w_xy_com; }

最后，将你的应⽤放到两个tomcat中，并依次启动redis、tomcat、nginx。访问你的nginx，可以 发现两个tomcat中的sesion可以保持共享了。

上⾯⽂章中，有⼀点需要说明的是： 如果tomcat配置中，将manager放在server.xml中，那么使⽤maven做热部署时，会发⽣失败。所以， 本⼈推荐放在context.xml中。

