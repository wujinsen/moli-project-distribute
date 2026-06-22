摘要：单点登录（ SO）的技术被越来越⼴泛地运⽤到各个领域的软件系统当中。本⽂从业务的⻆度 分析了单点登录的需求和应⽤领域；从技术本身的⻆度分析了单点登录技术的内部机制和实现⼿段， 并且给出Web-SO和桌⾯ SO的实现、源代码和详细讲解；还从安全和性能的⻆度对现有的实现技术 进⾏进⼀步分析，指出相应的⻛险和需要改进的⽅⾯。本⽂除了从多个⽅⾯和⻆度给出了对单点登录 （ SO）的全⾯分析，还并且讨论了如何将现有的应⽤和 SO服务结合起来，能够帮助应⽤架构师和 系统分析⼈员从本质上认识单点登录，从⽽更好地设计出符合需要的安全架构。 关键字： SO, Java, J2E, JAS

- 1 什么 是 单 点 登 陆 单点登录（Single Sign On），简称为 SO，是⽬前⽐较流⾏的企业业务整合的解决⽅案之⼀。 SO的 定义是在多个应⽤系统中，⽤户只需要登录⼀次就可以访问所有相互信任的应⽤系统。 较⼤的企业内部，⼀般都有很多的业务⽀持系统为其提供相应的管理和IT服 务。例如财务系统为财务 ⼈员提供财务的管理、计算和报表服务；⼈事系统为⼈事部⻔提供全公司⼈员的维护服务；各种业务 系统为公司内部不同的业务提供不同的 服务等等。这些系统的⽬的都是让计算机来进⾏复杂繁琐的计 算⼯作，来替代⼈⼒的⼿⼯劳动，提⾼⼯作效率和质量。这些不同的系统往往是在不同的时期建设起 来 的，运⾏在不同的平台上；也许是由不同⼚商开发，使⽤了各种不同的技术和标准。如果举例说国 内⼀著名的IT公司（名字隐去），内部共有60多个业务系统，这些系统包括两个不同版本的SAP的ERP 系统，12个不同类型和版本的数据库系统，8个不同类型和版本的操作系统，以及使⽤了3种不同的防 ⽕墙技术，还有数⼗种互相不能兼容的协议和标准，你相信吗？不要怀疑，这种情况其实⾮常普遍。 每⼀个应⽤系统在运⾏了数年以后，都会成为不可替换的企业IT架构的⼀部分，如下图所示。


随 着企业的发展，业务系统的数量在不断的增加，⽼的系统却不能轻易的替换，这会带来很多的开 销。其⼀是管理上的开销，需要维护的系统越来越多。很多系统的数 据是相互冗余和重复的，数据的 不⼀致性会给管理⼯作带来很⼤的压⼒。业务和业务之间的相关性也越来越⼤，例如公司的计费系统 和财务系统，财务系统和⼈事系 统之间都不可避免的有着密切的关系。 为了降低管理的消耗，最⼤限度的重⽤已有投资的系统，很多企业都在进⾏着企业应⽤集成（EAI）。 企业应⽤集成可以在不同层⾯上进⾏：例如在数据存储层⾯上的“数据⼤集中”，在传输层⾯上的“通⽤ 数据交换平台”，在应⽤层⾯上的“业务流程整合”，和⽤ 户界⾯上的“通⽤企业⻔户”等等。事实上，还 ⽤⼀个层⾯上的集成变得越来越重要，那就是“身份认证”的整合，也就是“单点登录”。 通常来说，每个单独的系统都会有⾃⼰的安全体系和身份认证系统。整合以前，进⼊每个系统都需要 进⾏登录，这样的局⾯不仅给管理上带来了很⼤的困难，在安全⽅⾯也埋下了重⼤的隐患。下⾯是⼀ 些著名的调查公司显示的统计数据：

⽤户每天平均 16 分钟花在身份验证任务上 - 资 料 来 源 ： IDS

频繁的 IT ⽤户平均有 21 个密码 - 资 料 来 源 ： NTA Monitor Pasword Survey

49% 的⼈写下了其密码，⽽ 67% 的⼈很少改变它们

每 79 秒出现⼀起身份被窃事件 - 资 料 来 源 ： National Smal Busines Travel Asoc

全球欺骗损失每年约 12B - 资 料 来 源 ： Com Fraud Control Asoc

到 207 年，身份管理市场将成倍增⻓⾄ $4.5B - 资 料 来 源 ： IDS

使⽤“单点登录”整合后，只需要登录⼀次就可以进⼊多个系统，⽽不需要重新登录，这不仅仅带来了 更好的⽤户体验，更重要的是降低了安全的⻛险和管理的消耗。请看下⾯的统计数据：

提⾼ IT 效率：对于每 1 0 个受管⽤户，每⽤户可节省$70K

帮助台呼叫减少⾄少1/3，对于 10K 员⼯的公司，每年可以节省每⽤户 $75，或者合计 $648K

⽣产⼒提⾼：每个新员⼯可节省 $1K，每个⽼员⼯可节省 $350 资 料 来 源 ： Giga

ROI 回报：7.5 到 13 个⽉ 资 料 来 源 ： Gartner

另外，使⽤“单点登录”还是SOA时代的需求之⼀。在⾯向服务的架构中，服务和服务之间，程序和程序 之间的通讯⼤量存在，服务之间的安全认证是SOA应⽤的难点之⼀，应此建⽴“单点登录”的系统体系能 够⼤⼤简化SOA的安全问题，提⾼服务之间的合作效率。

- 2 单 点 登 陆 的 技 术 实 现 机 制 随着 SO技术的流⾏， SO的产品也是满天⻜扬。所有著名的软件⼚商 都提供了相应的解决⽅案。在这⾥我并不想介绍⾃⼰公司（Sun Microsystems）的产品，⽽是对 SO 技术本身进⾏解析，并且提供⾃⼰开发这⼀类产品的⽅法和简单演示。有关我写这篇⽂章的⽬的，请 参考我的博客（


htp:/yuwang81.blog.sohu.com/3184816.html

）。 单 点登录的机制其实是⽐较简单的，⽤⼀个现实中的例⼦做⽐较。颐和园是北京著名的旅游景点，也 是我常去的地⽅。在颐和园内部有许多独⽴的景点，例如“苏州 街”、“佛⾹阁”和“德和园”，都可以在 各个景点⻔⼝单独买票。很多游客需要游玩所有德景点，这种买票⽅式很不⽅便，需要在每个景点⻔ ⼝排队买票，钱包拿 进拿出的，容易丢失，很不安全。于是绝⼤多数游客选择在⼤⻔⼝买⼀张通票 （也叫套票），就可以玩遍所有的景点⽽不需要重新再买票。他们只需要在每个景点⻔ ⼝出示⼀下刚 才买的套票就能够被允许进⼊每个独⽴的景点。 单点登录的机制也⼀样，如下图所示，当⽤户第⼀次访问应⽤系统1的时候，因为还没有登录，会被引 导到认证系统中进⾏登录（1）；根据⽤户提供的登录信息，认证系统进⾏身份效验，如果通过效验， 应该返回给⽤户⼀个认证的凭据－－ticket（2）；⽤户再访问别的应⽤的时候（3，5）就会将这个 ticket带上，作为⾃⼰认证的凭据，应⽤系统接受到请求之后会把ticket送到认证系统进⾏效验，检查 ticket的合法性（4，6）。如果通过效验，⽤户就可以在不⽤再次登录的情况下访问应⽤系统2和应⽤ 系统3了。

从上⾯的视图可以看出，要实现 SO，需要以下主要的功能：

所有应⽤系统共享⼀个身份认证系统。统⼀的认证系统是 SO的前提之⼀。认证系统的主要功能是 将⽤户的登录信息和⽤户信息库相⽐较，对⽤户进⾏登录认证；认证成功后，认证系统应该⽣成统 ⼀的认证标志（ticket），返还给⽤户。另外，认证系统还应该对ticket进⾏效验，判断其有效性。

所有应⽤系统能够识别和提取ticket信息要实现 SO的功能，让⽤户只登录⼀次，就必须让应⽤系统 能够识别已经登录过的⽤户。应⽤系统应该能对ticket进⾏识别和提取，通过与认证系统的通讯，能 ⾃动判断当前⽤户是否登录过，从⽽完成单点登录的功能。

上⾯的功能只是⼀个⾮常简单的 SO架构，在现实情况下的 SO有着更加复杂的结构。有两点需要指 出的是：

单⼀的⽤户信息数据库并不是必须的，有许多系统不能将所有的⽤户信息都集中存储，应该允许⽤ 户信息放置在不同的存储中，如下图所示。事实上，只要统⼀认证系统，统⼀ticket的产⽣和效验， ⽆论⽤户信息存储在什么地⽅，都能实现单点登录。

![image 1](<java实现简单的单点登录.note_images/imageFile1.png>)

统⼀的认证系统并不是说只有单个的认证服务器，如下图所示，整个系统可以存在两个以上的认证 服务器，这些服务器甚⾄可以是不同的产品。认证服务器之间要通过标准的通讯协议，互相交换认 证信息，就能完成更⾼级别的单点登录。如下图，当⽤户在访问应⽤系统1时，由第⼀个认证服务器 进⾏认证后，得到由此服务器产⽣的ticket。当他访问应⽤系统4的时候，认证服务器2能够识别此 ticket是由第⼀个服务器产⽣的，通过认证服务器之间标准的通讯协议（例如SAML）来交换认证信 息，仍然能够完成 SO的功能。

![image 2](<java实现简单的单点登录.note_images/imageFile2.png>)

- 3 WEB-SO的 实 现 随着互联⽹的⾼速发展，WEB应⽤⼏乎统治了绝⼤部分的软件应⽤系统，因此 WEB-SO是 SO应⽤当中最为流⾏。WEB-SO有其⾃身的特点和优势，实现起来⽐较简单易⽤。很 多商业软件和开源软件都有对WEB-SO的实现。其中值得⼀提的是OpenSO （


htps:/openso.dev.java.net

），为⽤Java实现WEB-SO提供架构指南和服务指南，为⽤户⾃⼰来实 现WEB-SO提供了理论的依据和实现的⽅法。 为什么说WEB-SO⽐较容易实现呢？这是有WEB应⽤⾃身的特点决定的。

众所周知，Web协议（也就是HTP）是⼀个⽆状态的协议。⼀个Web应⽤由很多个Web⻚⾯组成，每 个⻚⾯都有唯⼀的URL来定义。⽤户在浏览器的地址栏输⼊⻚⾯的URL，浏览器就会向Web Server去 发送请求。如下图，浏览器向Web服务器发送了两个请求，申请了两个⻚⾯。这两个⻚⾯的请求是分 别使⽤了两个单独的HTP连接。所谓⽆状态的协议也就是表现在这⾥，浏览器和Web服务器会在第⼀ 个请求完成以后关闭连接通道，在第⼆个请求的时候重新建⽴连接。Web服务器并不区分哪个请求来 ⾃哪个客户端，对所有的请求都⼀视同仁，都是单独的连接。这样的⽅式⼤⼤区别于传统的 （Client/Server）C/S结构,在那样的应⽤中，客户端和服务器端会建⽴⼀个⻓时间的专⽤的连接通道。 正是因为有了⽆状态的特性，每个连接资源能够很快被其他客户端所重⽤，⼀台Web服务器才能够同 时服务于成千上万的客户端。

但是我们通常的应⽤是有状态的。先不⽤提不同应⽤之间的 SO，在同⼀个应⽤中也需要保存⽤户的 登录身份信息。例如⽤户在访问⻚⾯1的时候进⾏了登录，但是刚才也提到，客户端的每个请求都是单 独的连接，当客户再次访问⻚⾯2的时候，如何才能告诉Web服务器，客户刚才已经登录过了呢？浏览 器和服务器之间有约定：通过使⽤cokie技术来维护应⽤的状态。Cokie是可以被Web服务器设置的 字符串，并且可以保存在浏览器中。如下图所示，当浏览器访问了⻚⾯1时，web服务器设置了⼀个 cokie，并将这个cokie和⻚⾯1⼀起返回给浏览器，浏览器接到cokie之后，就会保存起来，在它访 问⻚⾯2的时候会把这个cokie也带上，Web服务器接到请求时也能读出cokie的值，根据cokie值的 内容就可以判断和恢复⼀些⽤户的信息状态。

Web-SO完全可以利⽤Cokie结束来完成⽤户登录信息的保存，将浏览器中的Cokie和上⽂中的 Ticket结合起来，完成 SO的功能。 为了完成⼀个简单的 SO的功能，需要两个部分的合作：

- 1.
- 2.


统⼀的身份认证服务。 修改Web应⽤，使得每个应⽤都通过这个统⼀的认证服务来进⾏身份效验。

3.1 Web SO 的样例根据上⾯的原理，我⽤J2E的技术（JSP和Servlet）完成了⼀个具有Web-

SO的简单样例。样例包含⼀个身份认证的服务器和两个简单的Web应⽤，使得这两个 Web应⽤通过 统⼀的身份认证服务来完成Web-SO的功能。此样例所有的源代码和⼆进制代码都可以从⽹站地址

htp:/gceclub.sun.com.cn/wangyu/

下载。 样例下载、安装部署和运⾏指南：

Web-SO的样例是由三个标准Web应⽤组成，压缩成三个zip⽂件，从 htp:/gceclub.sun.com.cn/wangyu/web-so htp:/gceclub.sun.com.cn/wangyu/web-so/ SOAuth.zip

/中下载。其中 SOAuth（ ）是身份认证服务； SOWebDemo1（ ）和 SOWebDemo2（

- htp:/gceclub.sun.com.cn/wangyu/web-so/ SOWebDemo1.zip


- htp:/gceclub.sun.com.cn/wangyu/web-so/ SOWebDemo2.zip


）是两个⽤来演示单点登录的Web 应⽤。这三个Web应⽤之所以没有打成war包，是因为它们不能直接部署，根据读者的部署环境需要作 出⼩⼩的修改。样例部署和运⾏的环境有⼀定的要求，需要符合Servlet2.3以上标准的J2E容器才能运 ⾏（例如Tomcat5,Sun Aplication Server 8, Jbos 4等）。另外，身份认证服务需要JDK1.5的运⾏环 境。之所以要⽤JDK1.5是因为笔者使⽤了⼀个线程安全的⾼性能的Java集合类“ConcurentMap”，只 有在JDK1.5中才有。

这三个Web应⽤完全可以单独部署，它们可以分别部署在不同的机器，不同的操作系统和不同的 J2E的产品上，它们完全是标准的和平台⽆关的应⽤。但是有⼀个限制，那两台部署应⽤ （demo1、demo2）的机器的域名需要相同，这在后⾯的章节中会解释到cokie和domain的关系以 及如何制作跨域的WEB-SO

解压缩 SOAuth.zip⽂件，在/WEB-INF/下的web.xml中请修改“domai name”的属性以反映实际的 应⽤部署情况，domai name需要设置为两个单点登录的应⽤（demo1和demo2）所属的域名。这 个domai name和当前 SOAuth服务部署的机器的域名没有关系。我缺省设置的是“.sun.com”。如 果你部署demo1和demo2的机器没有域名，请输⼊IP地址或主机名（如localhost），但是如果使⽤ IP地址或主机名也就意味着demo1和demo2需要部署到⼀台机器上了。设置完后，根据你所选择的 J2E容器，可能需要将 SOAuth这个⽬录压缩打包成war⽂件。⽤“jar -cvf SOAuth.war

SOAuth/”就可以完成这个功能。

解压缩 SOWebDemo1和 SOWebDemo2⽂件，分别在它们/WEB-INF/下找到web.xml⽂件，请修 改其中的⼏个初始化参数<init-param>

<param-name>SOServiceURL</param-name> <param-value> </param-value> </init-param> <init-param> <param-name>SOLoginPage</param-name> <param-value> </param-value> </init-param> 将其中的 SOServiceURL和 SOLoginPage修改成部署 SOAuth应⽤的机器名、端⼝号以及根路径 （缺省是 SOAuth）以反映实际的部署情况。设置完后，根据你所选择的J2E容器，可能需要将

htp:/wangyu.prc.sun.com:8080/ SOAuth/ SOAuth

htp:/wangyu.prc.sun.com:8080/ SOAuth/login.jsp

SOWebDemo1和 SOWebDemo2这两个⽬录压缩打包成两个war⽂件。⽤“jar -cvf SOWebDemo1.war SOWebDemo1/”就可以完成这个功能。

请输⼊第⼀个web应⽤的测试URL（test.jsp）,例如 SOWebDemo1/test.jsp，如果是第⼀次访问，便会⾃动跳转到登录界⾯，如下图 htp:/wangyu.prc.sun.com:8080/

使⽤系统⾃带的三个帐号之⼀登录（例如，⽤户名：wangyu,密码：wangyu），便能成功的看到 test.jsp的内容：显示当前⽤户名和欢迎信息。

htp:/wangyu.prc.sun.c om:8080/

请接着在同⼀个浏览器中输⼊第⼆个web应⽤的测试URL（test.jsp）,例如

SOWebDemo2/test.jsp。你会发现，不需要再次登录就能看到test.jsp的内容，同样是 显示当前⽤户名和欢迎信息，⽽且欢迎信息中明确的显示当前的应⽤名称（demo2）。

![image 3](<java实现简单的单点登录.note_images/imageFile3.png>)

# 3.2 WEB-SO代码讲解

- 3.2.1身份认证服务代码解析 Web-SO的源代码可以从⽹站地址 下 载。身份认证服务是⼀个标准的web应⽤，包括⼀个名为 SOAuth的Servlet，⼀个login.jsp⽂件和⼀ 个failed.html。身份认证的所有服务⼏乎都由 SOAuth的Servlet来实现了；login.jsp⽤来显示登录的⻚ ⾯（如果发现⽤户还没有登录过）；failed.html是⽤来显示登录失败的信息（如果⽤户的⽤户名和密码 与信息数据库中的不⼀样）。


htp:/gceclub.sun.com.cn/wangyu/web-so/webso_src.zip

SOAuth的代码如下⾯的列表显示，结构⾮常简单，先看看这个Servlet的主体部分：

package DesktopSO; i o aa.io.*;

taa.net.*; taa.text.*;

i taa.ti.*; i ortaa.util.concurent.*;

rtava.ervet.*; import javax.servlet. tp.*; public clas SOAuth extends HtpServlet {

static priate oncurent ap acounts; static private ConcurentMap SOIDs; Stingcokiename="WangYuDesktopSOID"; String domai name;

public void init(ServletConfig config) throws ServletException { super.init(config); domai name config.getInitarameter("domai name"); cokiename = config.getInitParameter("cokiename");

SOIDs = new ConcurentHashMap(); acounts=new ConcurentHashMap(); acunt ut"wangyu", "wangyu"); acut put"paul", "paul"); acounts.put("carol", "carol");

} protected void procesRequest(HtpServletRequest request, HtpServletResponse response) thr

PrintWriter out = response.getWriter();

tring action = request.getParameter("action"); String result="failed"; if (action=nul) {

handlerFromLogin(request,response);

} else if (action.equals("authcokie"){ String myCokie = request.getParameter("cokiename"); if (myCokie != nul) result = authCokie(myCokie);

utprint(result); out.close();

} else if (action.equals("authuser") { result=authNameAndPaswd(request,response);

utprint(result); out.close();

} else if (action.equals("logout") { String myCokie = request.getParameter("cokiename"); logout(myCokie); out.close();

}

} .

}

从代码很容易看出， SOAuth就是⼀个简单的Servlet。其中有两个静态成员变量：acounts和

SOIDs，这两个成员变量都使⽤了JDK1.5中线程安全的MAP类： ConcurentMap，所以这个样例⼀ 定要JDK1.5才能运⾏。Acounts⽤来存放⽤户的⽤户名和密码，在init()的⽅法中可以看到我给系统添 加了三个合法的⽤户。在实际应⽤中，acounts应该是去数据库中或LDAP中获得，为了简单起⻅，在 本样例中我使⽤了ConcurentMap在内存中⽤程序创建了三个⽤户。⽽ SOIDs保存了在⽤户成功的登 录后所产⽣的cokie和⽤户名的对应关系。它的功能显⽽易⻅：当⽤户成功登录以后，再次访问别的 系统，为了鉴别这个⽤户请求所带的cokie的有效性，需要到 SOIDs中检查这样的映射关系是否存 在。 在主要的请求处理⽅法procesRequest()中，可以很清楚的看到 SOAuth的所有功能

- 1.
- 2.
- 3.
- 4.


如果⽤户还没有登录过，是第⼀次登录本系统，会被跳转到login.jsp⻚⾯（在后⾯会解释如何跳 转）。⽤户在提供了⽤户名和密码以后，就会⽤handlerFromLogin()这个⽅法来验证。 如果⽤户已经登录过本系统，再访问别的应⽤的时候，是不需要再次登录的。因为浏览器会将第 ⼀次登录时产⽣的cokie和请求⼀起发送。效验cokie的有效性是 SOAuth的主要功能之⼀。

SOAuth还能直接效验⾮login.jsp⻚⾯过来的⽤户名和密码的效验请求。这个功能是⽤于⾮web应 ⽤的 SO，这在后⾯的桌⾯ SO中会⽤到。

SOAuth还提供logout服务。

下⾯看看⼏个主要的功能函数：

private void handlerFromLogin(HtpServletRequest request, HtpServletResponse response) throw trng username = request.getParameter("username"); tr g pasword = request.getParameter("pasword");

String pas = (String)acounts.get(username); if (pas=nul)|(!pas.equals(pasword)

getServletContext().getRequestDispatcher("/failed.html").forward(request, response);

else { tr g gotoURL = request.getParameter("goto"); tring newID = createUID(); SOIDs.put(newID, username);

Cokie wangyu = new Cokie(cokiename, newID); an eDomain(domai name); ag eMaxAge(6 0);

wan ueValue(newID); wangyu.setPath("/"); response.adCokie(wangyu); System.out.println("login suces, goto back url:" + gotoURL); if (gotoURL != nul) {

PrintWriter out = response.getWriter(); response.sendRedirect(gotoURL); out.close();

} }

}

handlerFromLogin()这个⽅法是⽤来处理来⾃login.jsp的登录请求。它的逻辑很简单：将⽤户输⼊的⽤ 户名和密码与预先设定好的⽤户集合（存放在acounts中）相⽐较，如果⽤户名或密码不匹配的话， 则返回登录失败的⻚⾯（failed.html），如果登录成功的话，需要为⽤户当前的sesion创建⼀个新的 ID，并将这个ID和⽤户名的映射关系存放到 SOIDs中，最后还要将这个ID设置为浏览器能够保存的 cokie值。 登录成功后，浏览器会到哪个⻚⾯呢？那我们回顾⼀下我们是如何使⽤身份认证服务的。⼀般来说我 们不会直接访问身份服务的任何URL，包括login.jsp。身份服务是⽤来保护其他应⽤服务的，⽤户⼀般 在访问⼀个受 SOAuth保护的Web应⽤的某个URL时，当前这个应⽤会发现当前的⽤户还没有登录， 便强制将也⻚⾯转向 SOAuth的login.jsp，让⽤户登录。如果登录成功后，应该⾃动的将⽤户的浏览器 指向⽤户最初想访问的那个URL。在handlerFromLogin()这个⽅法中，我们通过接收“goto”这个参数来 保存⽤户最初访问的URL，成功后便重新定向到这个⻚⾯中。 另外⼀个要说明的是，在设置cokie的时候，我使⽤了⼀个setMaxAge(6 0)的⽅法。这个⽅法是⽤ 来设置cokie的有效期，单位是秒。如果不使⽤这个⽅法或者参数为负数的话，当浏览器关闭的时 候，这个cokie就失效了。在这⾥我给了很⼤的值（1 0分钟），导致的⾏为是：当你关闭浏览器 （或者关机），下次再打开浏览器访问刚才的应⽤，只要在1 0分钟之内，就不需要再登录了。我这 样做是下⾯要介绍的桌⾯ SO中所需要的功能。 其他的⽅法更加简单，这⾥就不多解释了。 3.2.2具有 SO功能的web应⽤源代码解析 要实现WEB-SO的功能，只有身份认证服务是不够的。这点很显然，要想使多个应⽤具有单点登录的 功能，还需要每个应⽤本身的配合：将⾃⼰的身份认证的服务交给⼀个统⼀的身份认证服务－

SOAuth。 SOAuth服务中提供的各个⽅法就是供每个加⼊ SO的Web应⽤来调⽤的。 ⼀般来说，Web应⽤需要 SO的功能，应该通过以下的交互过程来调⽤身份认证服务的提供的认证服 务：

Web应⽤中每⼀个需要安全保护的URL在访问以前，都需要进⾏安全检查，如果发现没有登录（没 有发现认证之后所带的cokie），就重新定向到 SOAuth中的login.jsp进⾏登录。

登录成功后，系统会⾃动给你的浏览器设置cokie，证明你已经登录过了。

当你再访问这个应⽤的需要保护的URL的时候，系统还是要进⾏安全检查的，但是这次系统能够发 现相应的cokie。

有了这个cokie，还不能证明你就⼀定有权限访问。因为有可能你已经logout,或者cokie已经过期 了，或者身份认证服务重起过，这些情况下，你的cokie都可能⽆效。应⽤系统拿到这个cokie， 还需要调⽤身份认证的服务，来判断cokie时候真的有效，以及当前的cokie对应的⽤户是谁。

如果cokie效验成功，就允许⽤户访问当前请求的资源。

以上这些功能，可以⽤很多⽅法来实现：

在每个被访问的资源中（JSP或Servlet）中都加⼊身份认证的服务，来获得cokie，并且判断当前 ⽤户是否登录过。不过这个笨⽅法没有⼈会⽤:-)。

可以通过⼀个controler，将所有的功能都写到⼀个servlet中，然后在URL映射的时候，映射到所有 需要保护的URL集合中（例如*.jsp，/security/*等）。这个⽅法可以使⽤，不过，它的缺点是不能重 ⽤。在每个应⽤中都要部署⼀个相同的servlet。

Filter是⽐较好的⽅法。符合Servlet2.3以上的J2E容器就具有部署filter的功能。（Filter的使⽤可以 参考JavaWolrd的⽂章 ） Filter是⼀个具有很好的模块化，可重⽤的编程API，⽤在 SO正合适不过。本样例就是使⽤⼀个 filter来完成以上的功能。

htp:/ w.javaworld.com/javaworld/jw-06-201/jw-062-filters.html

package SO; i o aa.io.*;

taa.net.*; i taa.util.*;

taa.text.*; rtava.ervet.*; prtava.ervet.htp.*;

rtjavax.servlet.*; importor.apache.comon.htpcient.*; import org.apache.comons.htpclient.methods.GetMethod; public clas SOFilter implements Filter {

ri te FilterConfig filterConfig = nul; priate String cokieName="WangYuDesktopSOID";

rivte Stri SServiceURL= " "; private String SOLoginPage= " "; public void init(FilterConfig filterConfig) {

htp:/wangyu.prc.sun.com:8080/ SOAuth/ SOAuth htp:/wangyu.prc.sun.com:8080/ SOAuth/login.jsp

this.filterConfig = filterConfig; if (filterConfig != nul) {

if (debug) {

log("SOFilter:Initializing filter"); }

} cokieName = filterConfig.getInitParameter("cokieName");

SOServiceURL = filterConfig.getInitParameter("SOServiceURL"); SOLoginPage = filterConfig.getInitParameter("SOLoginPage");

} . .

}

以上的初始化的源代码有两点需要说明：⼀是有两个需要配置的参数 SOServiceURL和

SOLoginPage。因为当前的Web应⽤很可能和身份认证服务（ SOAuth）不在同⼀台机器上，所以需 要让这个filter知道身份认证服务部署的URL，这样才能去调⽤它的服务。另外⼀点就是由于身份认证 的服务调⽤是要通过htp协议来调⽤的（在本样例中是这样设计的，读者完全可以设计⾃⼰的身份服 务，使⽤别的调⽤协议，如RMI或SOAP等等），所有笔者引⽤了apache的comons⼯具包（详细信 息情访问apache 的⽹站 ），其中的“htpclient”可以⼤ ⼤简化htp调⽤的编程。 下⾯看看filter的主体⽅法doFilter():

htp:/jakarta.apache.org/comons/index.html

public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException if (debug) log("SOFilter:doFilter()"); HtpServletRequest request = (HtpServletRequest) req; HtpServletResponse response = (HtpServletResponse) res;

tri result="failed"; String url = request.getRequestURL().toString(); String qstring = request.getQueryString(); if (qstring = nul) qstring =";

/检查htp请求的head是否有需要的cokie String cokieValue ="; javax.servlet.htp.Cokie[] diskCokies = request.getCokies(); if (diskCokies != nul) {

for (int i = 0; i < diskCokies.length; i +) { if(diskCokies[i].getName().equals(cokieName){ cokieValue = diskCokies[i].getValue();

/如果找到了相应的cokie则效验其有效性 result = SOService(cokieValue); if (debug) log("found cokies!");

} }

} if (result.equals("failed") {/效验失败或没有找到cokie，则需要登录

response.sendRedirect(SOLoginPage+"?goto="+url);

} else if (qstring.indexOf("logout") > 1) {/logout服务 if (debug) log("logout action!"); logoutService(cokieValue); response.sendRedirect(SOLoginPage+"?goto="+url);

} else {/效验成功 request.setAtribute("SOUser",result); Throwable problem = nul; try {

chain.doFilter(req, res);

} catch(Throwable t) { problem = t; t.printStackTrace();

} if (problem != nul) {

if (problem intnceof ServletException) throw (ServletException)problem; if (problem instanceof IOException) throw (IOException)problem; sendProcesingEror(problem, res);

} }

}

doFilter()⽅法的逻辑也是⾮常简单的，在接收到请求的时候，先去查找是否存在期望的cokie值，如 果找到了，就会调⽤ SOService(cokieValue)去效验这个cokie的有效性。如果cokie效验不成功或 者cokie根本不存在，就会直接转到登录界⾯让⽤户登录；如果cokie效验成功，就不会做任何阻 拦，让此请求进⾏下去。在配置⽂件中，有下⾯的⼀个节点表示了此filter的URL映射关系：只拦截所 有的jsp请求。 <filter-maping><filter-name>SOFilter</filter-name><url-patern>*.jsp</url-patern></filtermaping>

下⾯还有⼏个主要的函数需要说明：

private String SOService(String cokievalue) throws IOException { String authAction = "?action=authcokie&cokiename="; HtpClient htpclient = new HtpClient); GetMethod htpget = new GetMethod(SOServiceURL+authAction+cokievalue); try {

htpclient.executeMethod(htpget); String resut = htpget.getResponseBodyAsString(); return result;

} finaly {

htpget.releaseConection(); }

} private void logoutService(String cokievalue) throws IOException {

String authAction = "?action=logout&cokiename="; HtpClient htpclient = new HtpClient); GetMethod htpget = new GetMethod(SOServiceURL+authAction+cokievalue); try {

htpclient.executeMethod(htpget); htpget.getResponseBodyAsString();

} finaly {

htpget.releaseConection(); }

}

这两个函数主要是利⽤apache中的htpclient访问 SOAuth提供的认证服务来完成效验cokie和logout 的功能。 其他的函数都很简单，有很多都是我的IDE（NetBeans）替我⾃动⽣成的。

- 4 当 前 ⽅ 案 的 安 全 局 限 性 当前这个WEB-SO的⽅案是⼀个⽐较简单的雏形，主要是⽤来演示 SO的概念和说明 SO技术的实现 ⽅式。有很多⽅⾯还需要完善，其中安全性是⾮常重要的⼀个⽅⾯。 我们说过，采⽤ SO技术的主要⽬的之⼀就是加强安全性，降低安全⻛险。因为采⽤了 SO，在⽹络 上传递密码的次数减少，⻛险降低是显然的，但是当前的⽅案却有其他的安全⻛险。由于cokie是⼀ 个⽤户登录的唯⼀凭据，对cokie的保护措施是系统安全的重要环节：


cokie的⻓度和复杂度在本⽅案中，cokie是有⼀个固定的字符串（我的姓名）加上当前的时间 戳。这样的cokie很容易被伪造和猜测。怀有恶意的⽤户如果猜测到合法的cokie就可以被当作已 经登录的⽤户，任意访问权限范围内的资源

cokie的效验和保护在本⽅案中，虽然密码只要传输⼀次就够了，可cokie在⽹络中是经常传来传 去。⼀些⽹络探测⼯具（如snif, snop,tcpdump等）可以很容易捕获到cokie的数值。在本⽅案 中，并没有考虑cokie在传输时候的保护。另外对cokie的效验也过于简单，并不去检查发送 cokie的来源究竟是不是cokie最初的拥有者，也就是说⽆法区分正常的⽤户和仿造cokie的⽤ 户。

当其中⼀个应⽤的安全性不好，其他所有的应⽤都会受到安全威胁因为有 SO，所以当某个处 于 SO的应⽤被黒客攻破，那么很容易攻破其他处于同⼀个 SO保护的应⽤。这些安全漏洞在商业 的 SO解决⽅案中都会有所考虑，提供相关的安全措施和保护⼿段，例如Sun公司的Aces Manager，cokie的复杂读和对cokie的保护都做得⾮常好。另外在OpneSO （

htps:/openso.dev.java.net

）的架构指南中也给出了部分安全措施的解决⽅案。

- 5 当 前 ⽅ 案 的 功 能 和 性 能 局 限 性 除了安全性，当前⽅案在功能和性能上都需要很多的改进：
- 6 桌 ⾯ SO的 实 现 从WEB-SO的概念延伸开，我们可以把 SO的技术拓展到整个桌⾯的应⽤，不仅仅局限在浏览器。


当前所提供的登录认证模式只有⼀种：⽤户名和密码，⽽且为了简单，将⽤户名和密码放在内存当 中。事实上，⽤户身份信息的来源应该是多种多样的，可以是来⾃数据库中，LDAP中，甚⾄于来⾃ 操作系统⾃身的⽤户列表。还有很多其他的认证模式都是商务应⽤不可缺少的，因此 SO的解决⽅ 案应该包括各种认证的模式，包括数字证书，Radius， SafeWord ，MemberShip，SecurID等多种 ⽅式。最为灵活的⽅式应该允许可插⼊的JAS框架来扩展身份认证的接⼝

我们编写的Filter只能⽤于J2E的应⽤，⽽对于⼤量⾮Java的Web应⽤，却⽆法提供 SO服务。

在将Filter应⽤到Web应⽤的时候，需要对容器上的每⼀个应⽤都要做相应的修改，重新部署。⽽更 加流⾏的做法是Agent机制：为每⼀个应⽤服务器安装⼀个agent，就可以将 SO功能应⽤到这个应 ⽤服务器中的所有应⽤。

当前的⽅案不能⽀持分别位于不同domain的Web应⽤进⾏ SO。这是因为浏览器在访问Web服务器 的时候，仅仅会带上和当前web服务器具有相同domain名称的那些cokie。要提供跨域的 SO的解 决⽅案有很多其他的⽅法，在这⾥就不多说了。Sun的Aces Manager就具有跨域的 SO的功能。 另外，Filter的性能问题也是需要重视的⽅⾯。因为Filter会截获每⼀个符合URL映射规则的请求，获 得cokie，验证其有效性。这⼀系列任务是⽐较消耗资源的，特别是验证cokie有效性是⼀个远程 的htp的调⽤，来访问 SOAuth的认证服务，有⼀定的延时。因此在性能上需要做进⼀步的提⾼。 例如在本样例中，如果将URL映射从“.jsp”改成“/*”，也就是说filter对所有的请求都起作⽤，整个应 ⽤会变得⾮常慢。这是因为，⻚⾯当中包含了各种静态元素如gif图⽚，cs样式⽂件，和其他html 静态⻚⾯，这些⻚⾯的访问都要通过filter去验证。⽽事实上，这些静态元素没有什么安全上的需 求，应该在filter中进⾏判断，不去效验这些请求，性能会好很多。另外，如果在filter中加上⼀定的 cache，⽽不需要每⼀个cokie效验请求都去远端的身份认证服务中执⾏，性能也能⼤幅度提⾼。

另外系统还需要很多其他的服务，如在内存中定时删除⽆⽤的cokie映射等等，都是⼀个严肃的解 决⽅案需要考虑的问题。

SO的概念和原则都没有改变，只需要再做⼀点点的⼯作，就可以完成桌⾯ SO 的应⽤。

桌⾯ SO和WEB-SO⼀样，关键的技术也在于如何在⽤户登录过后保存登录的凭据。在WEB-SO 中，登录的凭据是靠浏览器的cokie机制来完成的；在桌⾯应⽤中，可以将登录的凭证保存到任何地 ⽅，只要所有 SO的桌⾯应⽤都共享这个凭证。从⽹站可以下载⼀个简单的桌⾯ SO的样例

( 和全部源码（

htp:/gceclub.sun.com.cn/wangyu/desktop-so/desktopso.zip) htp:/gceclub.sun.c om.cn/wangyu/desktop-so/desktopso_src.zip

），虽然简单，但是它具有桌⾯ SO⼤多数的功能， 稍微加以扩充就可以成为⾃⼰的解决⽅案。

- 6.1桌⾯样例的部署
- 6.2桌⾯样例的运⾏ 样例程序包含三个简单的Java控制台程序，这三个程序单独运⾏都需要登录。如果运⾏第⼀个命叫 “GameSystem”的程序，提示需要输⼊⽤户名和密码：

效验成功以后，便会显示当前登录的⽤户的基本信息等等。

这时候再运⾏第⼆个桌⾯Java应⽤（mailSystem）的时候，就不需要再登录了，直接就显示出来刚才 登录的⽤户。

第三个应⽤是logout，运⾏它之后，⽤户便退出系统。再访问的时候，⼜需要重新登录了。请读者再制 裁执⾏完logout之后，重新验证⼀下前两个应⽤的 SO：先运⾏第⼆个应⽤，再运⾏第⼀个，会看到相 同的效果。 我们的样例并没有在这⾥停步，事实上，本样例不仅能够和在⼏个Java应⽤之间 SO，还能和浏览器 进⾏ SO，也就是将浏览器也当成是桌⾯的⼀部分。这对⼀些⾏业有着不⼩的吸引⼒。 这时候再打开Mozila浏览器，访问以前提到的那两个WEB应⽤，会发现只要桌⾯应⽤如果登录过， Web应⽤就不⽤再登录了，⽽且能显示刚才登录的⽤户的信息。读者可以在⼏个桌⾯和Web应⽤之间 进⾏登录和logout的试验，看看它们之间的 SO。

- 6.3桌⾯样例的源码分析


- 1.
- 2.
- 3.
- 4.
- 5.


运⾏此桌⾯ SO需要三个前提条件：a) WEB-SO的身份认证应⽤应该正在运⾏，因为我们在桌⾯

SO当中需要⽤到统⼀的认证服务b) 当前桌⾯需要运⾏Mozila或Netscape浏览器，因为我们将 ticket保存到mozila的cokie⽂件中c) 必须在JDK1.4以上运⾏。（WEB-SO需要JDK1.5以上） 解开desktopso.zip⽂件，⾥⾯有两个⽬录bin和lib。 bin⽬录下有⼀些脚本⽂件和配置⽂件，其中config.properties包含了三个需要配置的参数：a)

SOServiceURL要指向WebSO部署的身份认证的URLb) SOLoginPage要指向WebSO部署的 身份认证的登录⻚⾯URLc) cokiefilepath要指向当前⽤户的mozila所存放cokie的⽂件 在bin⽬录下还有⼀个login.conf是⽤来配置JAS登录模块，本样例提供了两个，读者可以任意选 择其中⼀个（也可以都选），再重新运⾏程序，查看登录认证的变化 在bin下的运⾏脚本可能需要作相应的修改a) 如果是在unix下，各个jar⽂件需要⽤“:”来隔开，⽽不 是“;”b) java 运⾏程序需要放置在当前运⾏的路径下，否则需要加上java的路径全名。

桌⾯ SO的样例使⽤了JAS（要了解JAS的详细的信息请参考 ）。 JAS是对PAM（Plugable Authentication Module）的Java实现，来完成 Java应⽤可插拔的安全认证 模块。使⽤JAS作为Java应⽤的安全认证模块有很多好处，最主要的是不需要修改源代码就可以更换 认证⽅式。例如原有的Java应⽤如果使⽤JAS的认证，如果需要应⽤ SO，只需要修改JAS的配置 ⽂件就⾏了。现在在流⾏的J2E和其他 Java的产品中，⽤户的身份认证都是通过JAS来完成的。在 样例中，我们就展示了这个功能。请看配置⽂件login.conf

htp:/java.sun.com/products/j as

DesktopSO { des o so.sare.PaswordLoginModule required; desktopso.share.DesktopSOLoginModule required;

};

当我们注解掉第⼆个模块的时候，只有第⼀个模块起作⽤。在这个模块的作⽤下，只有test⽤户（密码 是12345）才能登录。当我们注解掉第⼀个模块的时候，只有第⼆个模块起作⽤，桌⾯ SO才会起作 ⽤。 所有的Java桌⾯样例程序都是标准JAS应⽤，熟悉JAS的程序员会很快了解。JAS中主要的是登录 模块（LoginModule）。下⾯是 SO登录模块的源码：

public clas DesktopSOLoginModule implements LoginModule {

. rivte Stri SServiceURL ="; riate String SOLoginPage =";

private static String cokiefilepath =";

.

在config.properties的⽂件中，我们配置了它们的值：

htp:/wangyu.prc.sun.com:8080/ SOAuth/ SOAuth htp:/wangyu.prc.sun.com:8080/ SOAuth/login.jsp

SOServiceURL= SOLoginPage=

cokiefilepath=C:\Documents and Setings\yw137672\Aplication Data\Mozila\Profiles\default\

SOServiceURL和 SOLoginPage成员变量指向了在Web-SO中⽤过的身份认证模块： SOAuth，这 就说明在桌⾯系统中我们试图和Web应⽤共⽤⼀个认证服务。⽽cokiefilepath成员变量则泄露了⼀个 “天机”：我们使⽤了Mozila浏览器的cokie⽂件来保存登录的凭证。换句话说，和Mozila共⽤了⼀个 保存登录凭证的机制。之所以⽤Mozila是应为它的Cokie⽂件格式简单，很容易编程访问和修改任意 的Cokie值。（我试图解析Internet Explorer的cokie⽂件但没有成功。） 下⾯是登录模块DesktopSOLoginModule的主体：login()⽅法。逻辑也是⾮常简单：先⽤Cokie来登 陆，如果成功，则直接就进⼊系统，否则需要⽤户输⼊⽤户名和密码来登录系统。

public bolean login() throws LoginException{ try {

if (Cokielogin() return true; } catch (IOException ex) { ex.printStackTrace();

} if (paswordlogin() return true; throw new FailedLoginException();

}

下⾯是Cokielogin()⽅法的实体，它的逻辑是：先从Cokie⽂件中获得相应的Cokie值，通过身份效 验服务效验Cokie的有效性。如果cokie有效就算登录成功；如果不成功或Cokie不存在，⽤cokie 登录就算失败。

public bolean Cokielogin() throws LoginException,IOException { String cokieValue="; int cokieIndex =foundCokie(); if (cokieIndex<0) return false; else

cokieValue = getCokieValue(cokieIndex); username = cokieAuth(cokieValue); if (! username.equals("failed") {

loginSuces = true; return true;

} return false;

}

⽤⽤户名和密码登录的⽅法要复杂⼀些，通过Calback的机制和屏幕输⼊输出进⾏信息交互，完成⽤ 户登录信息的获取；获取信息以后通过userAuth⽅法来调⽤远端 SOAuth的服务来判定当前登录的有 效性。

public bolean paswordlogin() throws LoginException { / / Since we ned input from a user, we ned a calback handler

if (calbackHandler = nul) { throw new LoginException("No CalbackHandler defined");

} Calback[] calbacks = new Calback[2]; calbacks0] = new NameCalback("Username"); calbacks[1] = new PaswordCalback("Pasword", false);

/ / Cal the calback handler to get the username and pasword

try { calbackHandler.handle(calbacks); usename =(NameCalback)calbacks[0]).getName(); char[] temp =(PaswordCalback)calbacks[1]).getPasword(); pasword = new char[temp.length]; System.araycopy(temp, 0, pasword, 0, temp.length);

(PaswordCalback)calbacks[1]).clearPasword(); } catch (IOException ioe) {

throw new LoginException(ioe.toString(); } catch (UnsuportedCalbackException uce) {

throw new LoginException(uce.toString(); }

ystem.out.println(); String authresult ="; try {

authresult = userAuth(username, pasword); } catch (IOException ex) { ex.printStackTrace();

} if (! authresult.equals("failed") {

loginSuces= true; clearPasword(); try {

updateCokie(authresult); } catch (IOException ex) { ex.printStackTrace();

} return true;

}

loginSuces = false; username = nul; clearPasword(); System.out.println( "Login: PaswordLoginModule FAIL" ); throw new FailedLoginException();

}

CokieAuth和userAuth⽅法都是利⽤apahce的htpclient⼯具包和远程的 SOAuth进⾏htp连接，获取 服务。

private String cokieAuth(String cokievalue) throws IOException{ String result = "failed";

HtpClient htpclient = new HtpClient); GetMethod htpget = new GetMethod(SOServiceURL+Action1+cokievalue);

try { htpclient.executeMethod(htpget); result = htpget.getResponseBodyAsString();

} finaly { htpget.releaseConection();

} return result;

} private String userAuth(String username, char[] pasword) throws IOException{

tri result = "failed"; String paswd= new String(pasword); HtpClient htpclient = new HtpClient); GetMethod htpget = new GetMethod(SOServiceURL+Action2+username+"&pasword="+pa paswd = nul;

try { htpclient.executeMethod(htpget); result = htpget.getResponseBodyAsString();

} finaly { htpget.releaseConection();

} return result;

}

还有⼀个地⽅需要补充说明的是，在本样例中，⽤户名和密码的输⼊都会在屏幕上显示明⽂。如果希 望⽤掩码形式来显示密码，以提⾼安全性，请参考：

htp:/java.sun.com/developer/technicalArticles/ Security/pwordmask/

- 7 真 正 安 全 的 全 ⽅ 位 SO解 决 ⽅ 案 ：Kerberos 我们的样例程序（桌⾯ SO和WEB-SO）都有⼀个共性：要想将⼀个应⽤集成到我们的 SO解决⽅案 中，或多或少的需要修改应⽤程序。Web应⽤需要配置⼀个我们预制的filter；桌⾯应⽤需要加上我们 桌⾯ SO的JAS模块（⾄少要修改JAS的配置⽂件）。可是有很多程序是没有源代码和⽆法修改的， 例如常⽤的远程通讯程序telnet和ftp等等⼀些操作系统⾃⼰带的常⽤的应⽤程序。这些程序是很难修改 加⼊到我们的 SO的解决⽅案中。事实上有⼀种全⽅位的 SO解决⽅案能够解决这些问题，这就是 Kerberos协议（RFC 1510）。Kerberos是⽹络安全应⽤标准 ( )，由MIT学校发明，被主流的操作系统所采⽤。在采⽤kerberos的平台 中，登录和认证是由操作系统本身来维护，认证的凭证也由操作系统来保存，这样整个桌⾯都可以处 于同⼀个 SO的系统保护中。操作系统中的各个应⽤（如ftp,telnet）只需要通过配置就能加⼊到 SO 中。另外使⽤Kerberos最⼤的好处在于它的安全性。通过密钥算法的保证和密钥中⼼的建⽴，可以做 到⽤户的密码根本不需要在⽹络中传输，⽽传输的信息也会⼗分的安全。


htp:/web.mit.edu/kerberos/

⽬前⽀持Kerberos的操作系统包括Solaris, windows,Linux等等主流的平台。只不过要搭建⼀个 Kerberos的环境⽐较复杂，KDC（密钥分发中⼼）的建⽴也需要相当的步骤。Kerberos拥有⾮常成熟 的API，包括Java的API。使⽤Java Generic Security Services(GS) API并且使⽤JAS中对Kerberos 的⽀持（详细信息请参⻅Sun的Java&Kerberos教程 j2se/1.5.0/docs/guide/security/jgs/tutorials/index.html），要将我们这个样例改造成对Kerberos的⽀ 持也是不难的。 值得⼀提的是在JDK6.0 （ ）当中直接就包含了对 GS的⽀持，不需要单独下载GS的包。

htp:/java.sun.com/

htp:/ w.java.net/download/jdk6

- 8 总 结 本⽂的主要⽬的是阐述 SO的基本原理，并提供了⼀种实现的⽅式。通过对源代码的分析来掌握开发


SO服务的技术要点和充分理解 SO的应⽤范围。但是，本⽂仅仅说明了身份认证的服务，⽽另外⼀ 个和身份认证密不可分的服务 -权限效验，却没有提到。要开发出真正的 SO的产品，在功能上、性 能上和安全上都必须有更加完备的考虑。

