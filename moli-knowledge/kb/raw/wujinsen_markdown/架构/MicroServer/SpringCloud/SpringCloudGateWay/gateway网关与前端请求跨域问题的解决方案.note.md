htps:/ w.jb51.net/article/217497.htm

# gateway⽹关与前端请求跨域问题的解决⽅案

更新时间：2021年07⽉15⽇ 14 03 38 作者：锦瑟思年华

这篇⽂章主要介绍了gateway⽹关与前端请求跨域问题的解决⽅案，具有很好的参考价值，希望对⼤家 有所帮助。如有错误或未考虑完全的地⽅，望不吝赐教 gateway⽹关与前端请求的跨域问题 最近因项⽬需要，引⼊了gateway⽹关。可是发现将前端请求的端⼝指向⽹关后，⽤postman发送请求 是正常的，⽤浏览器⻚⾯点击请求会出现跨域问题。今天就记录⼀下⾃⼰是怎么解决的。 第⼀种 直接在yml⽂件中配置

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br>9<br><br><br>111111111122012345678901<br><br></th>
    <th>spring: application: name: service-getway<br><br>cloudgat:eway: globalcors: cors-configurations:<br><br>'[/**]': # 允许携带认证信息 # 允许跨域的源(⽹站域<br><br>名/ip)，设置*为全部<br><br># 允许跨域请求⾥的head字段， 设置*为全部<br><br># 允许跨域的method， 默认为<br><br>GET和OPTIONS，设置*为全部 # 跨域允许的有效期 allow-credentials: true allowed-originPatterns:<br><br>"*"<br><br>allowed-headers: "*" allowed-methods:<br><br>-- OPTIONGET S<br><br>- POST max-age: 3 00<br><br></th>
  </tr>
</table>


6

允许跨域的源(⽹站域名/ip)，设置*为全部，也可以指定ip或者域名。 第⼆种 写⼀个WebCrosFilter过滤器实现Filter，在doFilter⽅法中这样编写

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br>9<br><br><br>1111101234<br><br></th>
    <th>public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {<br><br>HttpServletResponse res = (HttpServletResponse)response;<br><br>HttpServletRequest req = (HttpServletRequest)request;<br><br>res.setHeader("AccessControl-Allow-Origin", req.getHeader("Origin"));<br><br>res.setHeader("AccessControl-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");<br><br>res.setHeader("AccessControl-Max-Age", "3600");<br><br>res.setHeader("AccessControl-Allow-Headers", req.getHeader("Access-ControlRequest-Headers"));<br><br>res.setHeader("Access-<br><br>Control-Allow-Credentials", "true");<br><br>if (req.getMethod().equals(RequestMetho d.OPTIONS.name())) {<br><br>res.setStatus(HttpStatus<br><br>.OK.value());<br><br>} else {<br><br>filterChain.doFilter(req uest, response);<br><br>} }<br><br></th>
  </tr>
</table>


### 再然后在编写⼀个配置类

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br>9<br><br><br>111012<br><br></th>
    <th>@Configuration public class WebFilterConfig {<br><br>@Bean public FilterRegistrationBean<br><br>webCrossFilterRegistration() {<br><br>FilterRegistrationBean registration = new FilterRegistrationBean();<br><br>registration.setFilter(new WebCrossFilter());<br><br>registration.addUrlPatterns( "/**");<br><br>registration.addInitParamete r("paramName", "paramValue");<br><br>registration.setName("webCro ssFilter");<br><br>return registration; }<br><br>}</th>
  </tr>
</table>


将WebCrosFilter注册到spring容器中，这样就解决了跨域问题。

![image 1](<gateway网关与前端请求跨域问题的解决方案.note_images/imageFile1.png>)

建议在⽹关写了cros后，服务就不需要再写了。 gateway⽹关统⼀解决跨域 ⽹上有很多种解决跨域问题的，只有这种⽤起来最简单。 通过修改配置⽂件的⽅式来解决 只需要在 aplication.yml 配置⽂件中添加红⾊框的配置：

<table>
  <tr>
    <th>![image 2](<gateway网关与前端请求跨域问题的解决方案.note_images/imageFile2.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br>9<br><br><br>1111111111201234567890<br><br></th>
    <th>spring: application: name: app-gateway<br><br>cloudnac:os: discovery:<br><br>server-addr: localhost:8848 gateway:<br><br>globalcors: corsConfigurations:<br><br>'[/**]': allowedHeaders: "*" allowedOrigins: "*" allowCredentials: true allowedMethods:<br><br>- GET<br><br>- POST<br><br>- DELETE<br><br>- PUT<br><br>- OPTION<br></th>
  </tr>
</table>


最后需要注意⼀点，既然是在⽹关⾥边来解决跨域问题的，就不能在下流的服务⾥边再重复引⼊解决 跨域的配置了。 否则会导致跨域失效，报跨域的问题。 以上为个⼈经验，希望能给⼤家⼀个参考，也希望⼤家多多⽀持脚本之家。 您可能感兴趣的⽂章:

gateway⽹关接⼝请求的校验⽅式

深⼊剖析⽹关gateway原理

