我们可以⽤ 的defaultServlet来处理静态⽂件，也可⽤Spring框架来处理静态⽂件。使⽤ Spring来处理，可以在配置中加⼊以下代码： <mvc:default-servlet-handler/> 这样spring会⽤默认的Servlet来响应静态⽂件，(DefaultServletHttpRequestHandler在容器启动是会使 ⽤主流web容器默认servlet的名称列表⾃动查找容器的默认servlet，包括Tomcat, Jetty, Glassfish, JBoss, Resin, WebLogic, and WebSphere。)，如果为默认servlet配置了新的名称，或者这个容器 servlet名字不在spring列表中是，必须显式配置默认servlet的名字，如下：

Web服务器

<mvc:default-servlet-handler default-servlet-name="customServlet"/> 或者使⽤mvc:resources⽅式来处理，如下：

<mvc:resources mapping="/images/**" location="/images/" /> 使⽤<mvc:resources/>元素把images/**映射到ResourceHttpRequestHandler进⾏处理，location指定 静态资源的位置

