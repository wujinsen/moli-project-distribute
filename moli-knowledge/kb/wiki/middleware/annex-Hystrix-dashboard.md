---
title: Hystrix dashboard.note（原文插图 annex）
slug: annex-Hystrix-dashboard
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/Hystrix dashboard.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

# ⼀、hystrixdashboard 作⽤：

监控各个hystrixcommand的各种值。 通过dashboards的实时监控来动态修改配置，直到满意为⽌

仪表盘：

![image 1](assets/imageFile1.png)

⼆、启动hystrix

standalone-hystrix-dashboard is availabl at Maven Central

- 1、下载standalone-hystrix-dashboard-1.5.3-all.jar

- 2、启动hystrix-dashboard

- 3、测试


https://github.com/kennedyoliveira/standalone-hystrix-dashboard:该⻚⾯提供了⼀个很好的视频教学。

java -jar -DserverPort=7979 -DbindAddress=localhost standalone-hystrix-dashboard-1.5.3-all.jar 注意：其中的serverPort、bindAddress是可选参数，若不添加，默认是7979和localhost

浏览器输⼊http://localhost:7979/hystrix-dashboard/，出现⼩熊⻚⾯就是正确了。

三、代码

# 1、pom.xml

![image 2](assets/imageFile2.png)

复制代码

- 1 <dependency>

- 2 <groupId>com.netflix.hystrix</groupId>

- 3 <artifactId>hystrix-core</artifactId>

- 4 <version>1.4.10</version>

- 5 </dependency>

- 6 <!-- http://mvnrepository.com/artifact/com.netflix.hystrix/hystrix-metrics-event-stream

-->

- 7 <dependency>

- 8 <groupId>com.netflix.hystrix</groupId>

- 9 <artifactId>hystrix-metrics-event-stream</artifactId>

- 10 <version>1.4.10</version>

- 11 </dependency>


![image 3](assets/imageFile3.png)

复制代码

说明：

hystrix-core：hystrix核⼼接⼝包 hystrix-metrics-event-stream：只要客户端连接还连着，hystrix-metrics-event-stream就会不断的向客户端以 text/event-stream的形式推送计数结果（metrics）

# 2、配置HystrixMetricsStreamServlet

![image 4](assets/imageFile4.png)

复制代码

- 1 package com.xxx.firstboot.hystrix.dashboard;

- 2

- 3 import org.springframework.boot.context.embedded.ServletRegistrationBean;

- 4 import org.springframework.context.annotation.Bean;

- 5 import org.springframework.context.annotation.Configuration;

- 6

- 7 import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

- 8

- 9 @Configuration

- 10 public class HystrixConfig {

- 11

- 12 @Bean

- 13 public HystrixMetricsStreamServlet hystrixMetricsStreamServlet(){

- 14 return new HystrixMetricsStreamServlet();

- 15 }

- 16

- 17 @Bean

- 18 public ServletRegistrationBean registration(HystrixMetricsStreamServlet servlet){

- 19 ServletRegistrationBean registrationBean = new ServletRegistrationBean();

- 20 registrationBean.setServlet(servlet);

- 21 registrationBean.setEnabled(true);//是否启⽤该registrationBean

- 22 registrationBean.addUrlMappings("/hystrix.stream");

- 23 return registrationBean;

- 24 }

- 25 }


![image 5](assets/imageFile5.png)

复制代码

说明：以上⽅式是springboot注⼊servlet并进⾏配置的⽅式。
