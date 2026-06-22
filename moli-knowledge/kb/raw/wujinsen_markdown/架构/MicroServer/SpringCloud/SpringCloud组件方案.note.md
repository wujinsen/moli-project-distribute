表 1-1 传统 Spring Cloud 与 Kubernetes 提供的解决⽅案对⽐

<table>
  <tr>
    <th> </th>
    <th>Kubernetes</th>
    <th>Spring Cloud</th>
  </tr>
  <tr>
    <td>弹性伸缩</td>
    <td>Autoscaling</td>
    <td>N/A</td>
  </tr>
  <tr>
    <td>服务发现</td>
    <td>KubeDNS / CoreDNS</td>
    <td>Spring Cloud Eureka</td>
  </tr>
  <tr>
    <td>配置中⼼</td>
    <td>ConfigMap / Secret</td>
    <td>Spring Cloud Config</td>
  </tr>
  <tr>
    <td>服务⽹关</td>
    <td>Ingres Controler</td>
    <td>Spring Cloud Zul</td>
  </tr>
  <tr>
    <td>负载均衡</td>
    <td>Load Balancer</td>
    <td>Spring Cloud Ri bon</td>
  </tr>
  <tr>
    <td>服务安全</td>
    <td>RBAC API</td>
    <td>Spring Cloud Security</td>
  </tr>
  <tr>
    <td>跟踪监控</td>
    <td>Metrics API / Dashboard</td>
    <td>Spring Cloud Turbine</td>
  </tr>
  <tr>
    <td>降级熔断</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


N/A Spring Cloud Hystrix

<table>
  <tr>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td>服务发现</td>
    <td>Eureka</td>
    <td>Zokeper</td>
    <td>Consul</td>
    <td>Nacos</td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>负载均衡</td>
    <td>Ri bon</td>
    <td>LoadBalan</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>服务⽹关</td>
    <td>Zul</td>
    <td>cer Zul2</td>
    <td>Gateway</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>降级熔断</td>
    <td>Hystrix</td>
    <td>Sentinel</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>配置中⼼</td>
    <td>Config</td>
    <td>Nacos</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>服务调⽤</td>
    <td>OpenFeig</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


n

<table>
  <tr>
    <th> </th>
    <th>SpringCloud</th>
    <th>HTG 基于SringCloud微服务技 术选型</th>
  </tr>
  <tr>
    <td>服务发现</td>
    <td>Spring Cloud Eureka</td>
    <td>Spring Cloud Alibaba Nacos</td>
  </tr>
  <tr>
    <td>配置中⼼</td>
    <td>Spring Cloud Config</td>
    <td>Nacos</td>
  </tr>
  <tr>
    <td>服务⽹关</td>
    <td>Spring Cloud Zul</td>
    <td>GateWay</td>
  </tr>
  <tr>
    <td>负载均衡</td>
    <td>Spring Cloud Ri bon</td>
    <td>Spring Cloud Ri bon</td>
  </tr>
  <tr>
    <td>降级熔断</td>
    <td>Spring Cloud Hystrix</td>
    <td>Sentinel</td>
  </tr>
  <tr>
    <td>服务调⽤</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


Spring Cloud OpenFeign Spring Cloud OpenFeign

<table>
  <tr>
    <th> </th>
    <th>HTG 基于SringCloud微服务技术选型</th>
  </tr>
  <tr>
    <td>服务发现</td>
    <td>Spring Cloud Alibaba Nacos</td>
  </tr>
  <tr>
    <td>配置中⼼</td>
    <td>Spring Cloud Alibaba Nacos</td>
  </tr>
  <tr>
    <td>服务⽹关</td>
    <td>Spring Cloud GateWay</td>
  </tr>
  <tr>
    <td>负载均衡</td>
    <td>Spring Cloud Ri bon</td>
  </tr>
  <tr>
    <td>降级熔断</td>
    <td>Spring Cloud Alibaba Sentinel</td>
  </tr>
  <tr>
    <td>服务调⽤</td>
    <td> </td>
  </tr>
</table>


Spring Cloud OpenFeign

