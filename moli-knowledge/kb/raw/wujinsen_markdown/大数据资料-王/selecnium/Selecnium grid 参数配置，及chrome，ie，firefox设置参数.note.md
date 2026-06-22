Hub 和rc Session失效机制： Rc启动浏览器后，会建⽴内部的session机制，在session未失效前这个资源将被占⽤，所以这⾥会有⼀个 失效机制： l调⽤Driver.quit()后 l浏览器在没有任何操作或者已经关闭后，时间超过timeout设置的值时

当以上两种情况发⽣的时候，本session将失效。

场景运⾏环境限制： l⼀个rc多个不同浏览器运⾏ l⼀个rc不能同时启动多个相同浏览器运⾏ l本机多个rc不可以启动多个相同浏览器运⾏

⼆、Selenium grid环境依赖 JDK 5+ 三、Selenium grid关键字描述

浏览器驱动 Selenium使浏览器动起来，并不是那么简单，⽽是通过⼀个浏览器驱动使其动起来。每种浏览器 selenium团队都为其开发了⼀个驱动程序。这个驱动程序起着桥接的作⽤，连接着浏览器和 selenium。 四、Seleniumgrid命令列表 l Hub启动命令： n java -jar D:\AutoService\selenium-server-standalone-2.24.1.jar -role hub l Rc启动命令： n java -jar D:\AutoService\selenium-server-standalone-2.24.1.jar -role node -hub

htp:/host:port/gr id/register

l加⼊⽇志功能： n java -jar D:\AutoService\selenium-server-standalone-2.24.1.jar -role hub -log路径

l⾃定义浏览器名称： n Rc启动命令加 -browser browserName=chrome,platform=ANY n ⽬前，浏览器设置只⽀持默认列表中的关键字 n browserName

u android, u chrome, u Firefox u Htmlunit u internet explorer u Iphone

u Opera

n Platform n WINDOWS n LINUX n MAC

l设置浏览器驱动所在⽬录： n Rc启动命令加-Dwebdriver.chrome.driver=D:\AutoService\chromedriver.exe

l设置客户端超时时间： n hub启动命令加-timeout 20 n 实际上是不需要设置timeout这个参数，由于在使⽤webdriver时在关闭浏览器时，将会使sesion关 闭。

四、各⼤驱动配置描述 （⼀）chromeDriver l chromeDriver

Chrome Driver会单独的启动⼀个监听端⼜来进⾏通信，当selenium发过来请求时，会往本端⼜发 送请求来通信，所有的selenium浏览器驱动程序都遵循这⼀机制来实现。chromeDriver是⼀个可执⾏ ⽂件，⽤于selenium和浏览 器传输数据。 l 要求

![image 1](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile1.png>)

Selenium的要求为把chrome浏览器安装到指定的⽬录。上图为安装⽬录要求。 l ⼊门

![image 2](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile2.png>)

上图为创建⼀个chrome实例，不过在这之前，需要明确是否chrome的驱动程序是否存在于你的path ⾥⾯或者可以通过webdriver.chrome.driver来设置。

l 指定参数启动chrome

![image 3](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile3.png>)

上图代码为指定以窗⼜最⼤化启动chrome，chrome.switches⽤于在chrome启动时 给chrome设置参数。 加载chrome拓展：

![image 4](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile4.png>)

加载⽤户设置：

![image 5](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile5.png>)

设置chrome的安装⽬录：

![image 6](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile6.png>)

设置⽹络代理：

![image 7](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile7.png>)

l ⽇常问题

n 当使⽤RemoteWebDriver时，出现【get the The path to the chromedriver executable must be set by the webdriver.chrome.driver system property 】问题时，⾸先要检查chrome是否在系统变量路径⾥⾯或者是否在调 ⽤时设置webdriver.chrome.driver这个系统属性。 n Cokie只能获取键值对 n 在富⽂本⽂档⾥⾯不能输⼊⽂字 n 不能指定⾃定义的配置 n HTML5的接⼜未实现

（⼆）IEDriver l Internet explorer

IEDriver 确实是⼀个头疼的事情，官⽅的IE selenium驱动在IE6,7,8,9下测试过，并 且对于着这样的组 合windows xp，windows vista and windows7. IEDriver分为两个版本： n 32位版本 n 64位版本

很显然，在选择版本时需要按照机器的类型进⾏选择，如果硬件和操作系统为64位， 请选择64 位的驱动，反之选择32位。

l命令参数

n -port:指定驱动程序监听的端⼜ n -host:指定驱动程序的IP地址，默认为本地

n--log-level：指定⽇志级别 n--log-file:指定⽇志的完整的⽬录和⽇志⽂件名字

l必须的配置 n 初始化下载并且添加到运⾏环境驱动⽂件 n 在window vista 和window 7下⾯，需要设置IE的保护模式在相同的值 n IE缩放需要正确的设置为10%，否则会出现坐标问题 l IE本地事件 IEDriver通过本地事件模拟浏览器操作，他属于系统级别的事件，但是在这种情况会出现窗⼜焦点和⿏ 标悬浮问题。

l浏览器焦点

在多个浏览器竞争浏览器焦点的问题上⼀直都是⼀个问题，⽽并没有好的解决⽅案，⽬前来说，这 其实是⼀个优先级的问题，主流的观点为先来先得，先来的先执⾏完了才能执⾏后者。

l⿏标悬浮事件

当你的⿏标指针在窗⼜边界时，⿏标悬浮效果⽆法体现，因为IE在这个时候会做重复事件的碰撞 检测，⽽⿏标悬浮效果是⾮常的短暂的，所以导致了效果⽆法体现。⽬前webDriver开发团队⽆法解决 这个问题。

（三）firefoxDriver

l firefoxDiver

n firefoxDiver包括在 selenium-server-stanalone.jar ⾥⾯，也就是不需要做额外的配置和额外的⽂件下载。驱 动程序会携带⼀个xpi⽂件，当启动驱动程序的时候会添加到⽕狐拓展程序列表中。 l重要参数 可以通过System.setProperty或者-DpropertyName value 来设置 nWebdriver.firefox.bin：设置⽕狐的安装⽬录 nwebdriver.firefox.profile :设置当启动⽕狐时的⽤户数据配置。默认启动⼀个webdrive的匿名 配置 nWebdriver.log.file:javascript的输出⽇志⽂件⽬录 nWebdriver.firefox.logfile：输出 stdout/stderr 类型的⽇志到⽇志⽂件⾥⾯ nwebdriver.reap_profile：确定是否可以删除零时⽂件和配置 l运⾏firebug

![image 8](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile8.png>)

通过下载⼀个⽕狐插件和以上代码实现运⾏时firebug。 l -Beta- load fast preference

![image 9](<Selecnium grid 参数配置，及chrome，ie，firefox设置参数.note_images/imageFile9.png>)

此参数⽤于在页⾯未加载完全之前，调⽤click和get操作，但是本特性会导致相关的异常，因为会某⼀ 些元素未初始化的状态。并且本参数只有⽕狐拥有，其他浏览器不拥有本参数特性。

