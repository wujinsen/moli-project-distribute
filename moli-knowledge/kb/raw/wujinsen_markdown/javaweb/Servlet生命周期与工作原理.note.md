Servlet⽣命周期分为三个阶段：

- 1，初始化阶段 调⽤init()⽅法

- 2，响应客户请求阶段 调⽤service()⽅法

- 3，终⽌阶段 调⽤destroy()⽅法


Servlet初始化阶段： 在下列时刻Servlet容器装载Servlet：

- 1，Servlet容器启动时⾃动装载某些Servlet，实现它只需要在web.XML⽂件中的<Servlet></Servlet>之间添加如下代

码：

- 2，在Servlet容器启动后，客户⾸次向Servlet发送请求

- 3，Servlet类⽂件被更新后，重新装载Servlet


<table>
  <tr>
    <th><loadon-startup>1</loadon-startup></th>
  </tr>
</table>


Servlet被装载后，Servlet容器创建⼀个Servlet实例并且调⽤Servlet的init()⽅法进⾏初始化。在Servlet的整个⽣命周期内， init()⽅法只被调⽤⼀次。

Servlet⼯作原理：

⾸先简单解释⼀下Servlet接收和响应客户请求的过程，⾸先客户发送⼀个请求，Servlet是调⽤service()⽅法对请求进⾏响应的， 通过源代码可⻅，service()⽅法中对请求的⽅式进⾏了匹配，选择调⽤doGet,doPost等这些⽅法，然后再进⼊对应的⽅法中调⽤逻辑 层的⽅法，实现对客户的响应。在Servlet接⼝和GenericServlet中是没有doGet,doPost等等这些⽅法的，HttpServlet中定义了这些 ⽅法，但是都是返回error信息，所以，我们每次定义⼀个Servlet的时候，都必须实现doGet或doPost等这些⽅法。

每⼀个⾃定义的Servlet都必须实现Servlet的接⼝，Servlet接⼝中定义了五个⽅法，其中⽐较重要的三个⽅法涉及到Servlet的⽣ 命周期，分别是上⽂提到的init(),service(),destroy()⽅法。GenericServlet是⼀个通⽤的，不特定于任何协议的Servlet,它实现了 Servlet接⼝。⽽HttpServlet继承于GenericServlet，因此HttpServlet也实现了Servlet接⼝。所以我们定义Servlet的时候只需要继 承HttpServlet即可。

Servlet接⼝和GenericServlet是不特定于任何协议的，⽽HttpServlet是特定于HTTP协议的类，所以HttpServlet中实现了 service()⽅法，并将请求ServletRequest,ServletResponse强转为HttpRequest和HttpResponse。

<table>
  <tr>
    <th>public void service(ServletRequest req,ServletResponse res) throws ServletException,IOException<br><br>{ HtpRequest request; HtpResponse response;<br><br>try { req = (HtpRequest)request; res = (HtpResponse)response; }catch(ClasCastException e) { throw new ServletException("non-HTP request response"); } service(request,response);</th>
  </tr>
</table>


}

代码的最后调⽤了HTTPServlet⾃⼰的service(request,response)⽅法，然后根据请求去调⽤对应的doXXX⽅法，因为 HttpServlet中的doXXX⽅法都是返回错误信息，

<table>
  <tr>
    <th>protected void doGet(HtpServletRequest res,HtpServletResponse resp) throws ServletException,IOException<br><br>{ String protocol = req.getProtocol(); String msg = IStrings.getString("htp.method_get_not_suported"); if(protocol.equals("1.1") { resp.sendError(HtpServletResponse.SC.METHOD.NOT.ALOWED,msg); } esle { resp.sendError(HtpServletResponse.SC_BAD_REQUEST,msg); }</th>
  </tr>
</table>


}

所以需要我们在⾃定义的Servlet中override这些⽅法！ 源码⾯前，了⽆秘密！

--------------------------------------------------------------------------------------------------------------------------------Servlet响应请求阶段：

对于⽤户到达Servlet的请求，Servlet容器会创建特定于这个请求的ServletRequest对象和ServletResponse对象，然后调⽤ Servlet的service⽅法。service⽅法从ServletRequest对象获得客户请求信息，处理该请求，并通过ServletResponse对象向客户返 回响应信息。

对于Tomcat来说，它会将传递过来的参数放在⼀个Hashtable中，该Hashtable的定义是：

<table>
  <tr>
    <th>private Hashtable<String String[]> paramHashStringArray = new Hashtable<String String[]>();</th>
  </tr>
</table>


这是⼀个String-->String[]的键值映射。 HashMap线程不安全的，Hashtable线程安全。

----------------------------------------------------------------------------------------------------------------------------------Servlet终⽌阶段：

当WEB应⽤被终⽌，或Servlet容器终⽌运⾏，或Servlet容器重新装载Servlet新实例时，Servlet容器会先调⽤Servlet的 destroy()⽅法，在destroy()⽅法中可以释放掉Servlet所占⽤的资源。

----------------------------------------------------------------------------------------------------------------------------------Servlet何时被创建：

- 1，默认情况下，当WEB客户第⼀次请求访问某个Servlet的时候，WEB容器将创建这个Servlet的实例。

- 2，当web.xml⽂件中如果<servlet>元素中指定了<load-on-startup>⼦元素时，Servlet容器在启动web服务器时，将按照顺


序创建并初始化Servlet对象。

注意：在web.xml⽂件中，某些Servlet只有<serlvet>元素，没有<servlet-mapping>元素，这样我们⽆法通过url的⽅式访问 这些Servlet，这种Servlet通常会在<servlet>元素中配置⼀个<load-on-startup>⼦元素，让容器在启动的时候⾃动加载这些 Servlet并调⽤init()⽅法，完成⼀些全局性的初始化⼯作。

Web应⽤何时被启动：

- 1，当Servlet容器启动的时候，所有的Web应⽤都会被启动

- 2，控制器启动web应⽤


-----------------------------------------------------------------------------------------------------------------------------------

-----------Servlet与JSP的⽐较：

有许多相似之处，都可以⽣成动态⽹⻚。

JSP的优点是擅⻓于⽹⻚制作，⽣成动态⻚⾯⽐较直观，缺点是不容易跟踪与排错。 Servlet是纯Java语⾔，擅⻓于处理流程和业务逻辑，缺点是⽣成动态⽹⻚不直观。

