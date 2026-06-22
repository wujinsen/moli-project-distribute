这⾥只记录学习 Selenium WebDriver 的过程，尤其是运⾏时可能出现的问题，学习 java 与 Selenium WebDriver 配合的⽅法。

# ⼀、下载⽂件

htp:/seleniumhq.org/download/

先要去官⽹（ ）下载必需的⽂件：

Selenium IDE （专⻔⽤于 FireFox 测试的独⽴界⾯，可以录制测试步骤，但我更倾向于写代码做标准的功能测试）

Selenium Server （可以输⼊指令控制、可以解决跨域的 js 问题，等到后⾯学到了再讲吧）

The Internet Explorer Driver Server （专⻔⽤于IE测试的）

Selenium Client Drivers （可以找到你熟悉的语⾔，例如我选择的 Java）

Third Party Browser Drivers NOT SUPORTED/DEVELOPED by seleniumhq（第三⽅开发的 Selenium 插件，第⼀个就是 Chrome 的，否则你就没办法测试 Chrome 了）

其他的，就根据你⾃⼰的需要寻找吧，⽬前这些⾜够我⽤了。

# ⼆、安装 & 运⾏

貌似摆弄新东⻄时，只有 “Helo World” 蹦出来以后，我们这些初学者才会感到情绪稳定，那就赶紧开 始吧。

对于初期打算直接⽤编程⽅式制作测试⽤例的情况来说，Selenium IDE、Selenium Server 都可以不⽤ 安装执⾏。 英语好的朋友可以直接看官⽹的⽂档（ ）就能够开始使⽤了。 看中⽂的，就继续听我唠叨：

htp:/seleniumhq.org/documentation/

- 【1. 建⽴ Maven ⼯程】 Selenium ⽀持 maven ⼯程，这会让你的⼯作更加简便。


htp:/seleniumhq.org/docs/0 3_webdriver.html#seting-up-a-selenium-webdriver-project

⽤ Eclipse 建个 Maven 的⼯程，建成后，直接修改 pom.xml，（参考： ）

查看源码打印?

<table>
  <tr>
    <th>01<br><br></th>
    <th>< project xmlns<br><br>= "<br><br>" xmlns:xsi<br><br>= "<br><br>"<br><br>http://maven.apache.org/POM/4. 0.0<br><br>http://www.w3.org/2001/XMLSchema<br><br>-instance</th>
  </tr>
</table>


<table>
  <tr>
    <th>02<br><br></th>
    <th>xsi:schemaLocation<br><br>= "<br><br>" ><br><br>http://maven.apache.org/POM/4. 0.0<br><br>http://maven.apache.org/xsd/maven4.0.0.xsd</th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>< modelVersion >4.0.0</ modelVersion ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>< groupId >Selenium2Test</ groupId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>< artifactId >Selenium2Test</ artifactId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>< version >1.0</ version ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>< dependencies ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th>< dependency ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th>< groupId >org.seleniumhq.selenium</ groupId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>< artifactId >selenium-java</ artifactId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>< version >2.25.0</ version ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th></ dependency ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>< dependency ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>< groupId >com.opera</ groupId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>< artifactId >operadriver</ artifactId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th></ dependency ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th></ dependencies ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th>< dependencyManagement ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>< dependencies ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>20</th>
    <th>< dependency ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th>< groupId >com.opera</ groupId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th>< artifactId >operadriver</ artifactId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>23</th>
    <th>< version >0.16</ version ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th>< exclusions ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>< exclusion ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>26</th>
    <th>< groupId >org.seleniumhq.selenium</ groupId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th>< artifactId >selenium-remote-driver</ artifactId ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>28</th>
    <th></ exclusion ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>29</th>
    <th></ exclusions ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>30</th>
    <th></ dependency ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>31</th>
    <th></ dependencies ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>32</th>
    <th></ dependencyManagement ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>33</th>
    <th></ project ><br><br></th>
  </tr>
</table>


pom.xml 修改保存后，eclipse 会⾃动把需要的 jar 包下载完成。

- 【2. 测试 FireFox】 Selenium 最初就是在 FireFox 上做起来的插件，所以我们先来搭建 FireFox 的环境。 确保你正确安装了 FireFox 后，就可以直接编写 java 代码测试喽。


在 leson1 ⽬录下建⽴ ExampleForFireFox.java （因为国内不少朋友访问 gogle 的时候会出问题，所以我就把代码中的 gogle 变成 baidu 了）

## 查看源码打印?

<table>
  <tr>
    <th>01</th>
    <th>package lesson1;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>import org.openqa.selenium.By;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>import org.openqa.selenium.WebDriver;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>import org.openqa.selenium.WebElement;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>import org.openqa.selenium.firefox.Firefox<br><br>Driver;</th>
  </tr>
</table>


<table>
  <tr>
    <th>07<br><br></th>
    <th>import org.openqa.selenium.support.ui.Expe<br><br>ctedCondition;</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th>import org.openqa.selenium.support.ui.WebD<br><br>riverWait;</th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>public class ExampleForFireFox {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>11<br><br></th>
    <th>public static void main(String[]<br><br>args) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th>// 如果你的 FireFox 没有安装在默认⽬ 录，那么必须在程序中设置<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>// System.setProperty("webdriver.firefo x.bin", "D:\\Program Files\\Mozilla Firefox\\firefox.exe");<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>// 创建⼀个 FireFox 的浏览器实例</th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>WebDriver driver = new FirefoxDriver();<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>// 让浏览器访问 Baidu</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th>driver.get( " " );<br><br>http://www.baidu.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>// ⽤下⾯代码也可以实现</th>
  </tr>
</table>


<table>
  <tr>
    <th>20<br><br></th>
    <th>// driver.navigate().to(" ");<br><br>http://w ww.baidu.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th>// 获取 ⽹⻚的 title</th>
  </tr>
</table>


<table>
  <tr>
    <th>23<br><br></th>
    <th>System.out.println( "1 Page<br><br>title is: "<br><br>+ driver.getTitle());</th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>// 通过 id 找到 input 的 DOM</th>
  </tr>
</table>


<table>
  <tr>
    <th>26<br><br></th>
    <th>WebElement element =<br><br>driver.findElement(By.id( "kw" ));<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>28</th>
    <th>// 输⼊关键字</th>
  </tr>
</table>


<table>
  <tr>
    <th>29</th>
    <th>element.sendKeys( "zTree" );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>30</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>31</th>
    <th>// 提交 input 所在的 form</th>
  </tr>
</table>


<table>
  <tr>
    <th>32</th>
    <th>element.submit();</th>
  </tr>
</table>


<table>
  <tr>
    <th>33</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>34<br><br></th>
    <th>// 通过判断 title 内容等待搜索⻚⾯加载完 毕，间隔10秒</th>
  </tr>
</table>


<table>
  <tr>
    <th>35</th>
    <th>( new WebDriverWait(driver, 10 )).until( new ExpectedCondition<Boolean>() {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>36<br><br></th>
    <th>public Boolean<br><br>apply(WebDriver d) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>37</th>
    <th>return d.getTitle().toLowerCase().endsWit<br><br>h( "ztree" );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>38</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>39</th>
    <th>});</th>
  </tr>
</table>


<table>
  <tr>
    <th>40</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>41</th>
    <th>// 显示搜索结果⻚⾯的 title</th>
  </tr>
</table>


<table>
  <tr>
    <th>42</th>
    <th>System.out.println( "2 Page<br><br>title is: "<br><br>+ driver.getTitle());</th>
  </tr>
</table>


<table>
  <tr>
    <th>43</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>44</th>
    <th>//关闭浏览器</th>
  </tr>
</table>


<table>
  <tr>
    <th>45</th>
    <th>driver.quit();</th>
  </tr>
</table>


<table>
  <tr>
    <th>46</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>47</th>
    <th>}</th>
  </tr>
</table>


普通情况下，直接运⾏代码就可以看到会⾃动弹出 FireFox 窗⼝，访问 baidu.com，然后输⼊关键字并 查询，⼀切都是⾃动完成的。

错误提醒：

- 1）Exception in thread "main" org.openqa.selenium.WebDriverException: Canot find firefox binary in PATH. Make sure firefox is instaled. 出现这个错误，是说明你的 FireFox ⽂件并没有安装在默认⽬录下，这时候需要在最开始执⾏： System.setProperty 设置环境变量 "webdriver.firefox.bin" 将⾃⼰机器上 FireFox 的正确路径设置完 毕后即可。
- 2）Exception in thread "main" org.openqa.selenium.UnsuportedComandException: Bad request


出现这个错误，很有意思。 查了⼀下 有⼈说应该是 hosts 出现了问题，加上⼀个 127.0.0.1 localhost 就⾏了，但我的 hosts 上肯定有这个玩意，为啥也会出现这个问题呢？

经过调试，发现 127.0.0.1 localhost 的设置必须要在 hosts ⽂件的最开始，⽽且如果后⾯有其他设置 后，也不要再出现同样的 127.0.0.1 localhost ，只要有就会出错。（因为我为了⽅便访问 gogle 的⽹ 站，专⻔加⼊了 smarthosts 的内容，导致了 localhost 的重复） 【3. 测试 Chrome】 Chrome 虽然不是 Selenium 的原配，但是没办法，她太⽕辣了，绝对不能抛下她不管的。 把 ExampleForFireFox.java 稍微修改就可以制作出⼀个 ExampleForChrome.java ，直接把 new FireFoxDriver() 修改为 new ChromeDriver() 你会发现还是⾏不通。

错误如下：

- 1）Exception in thread "main" java.lang.IlegalStateException: The path to the driver executable must be set by the webdriver.chrome.driver system property; for more information, se

The latest version can be downloaded from

这应该是找不到 chrome 的⽂件，好吧，利⽤ System.setProperty ⽅法添加路径，这⾥要注意，是 “webdriver.chrome.driver” 可不是“webdriver.chrome.bin”

设置路径后还是会报错：

- 2）[6416 4580 1204/173852 EROR:gpu_info_colector_win.c(91)] Can't retrieve a valid WinSAT asesment. 这个貌似是因为 Selenium ⽆法直接启动 Chrome 导致的，必须要通过前⾯咱们下载 Chrome 的第三 ⽅插件 ChromeDriver，去看第⼀个错误中提示给你的 ⽹址：


htp:/code.gogle.com/p/selenium/wiki/ChromeDriver.

htp:/code.gogle.com/p/chromedriver/downloads/list

htp:/code.gogle.com/p/selenium/wiki/ ChromeDriver

按照⼈家给的例⼦来修改我们的测试代码吧：

查看源码打印?

<table>
  <tr>
    <th>01</th>
    <th>package lesson1;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>import java.io.File;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>import java.io.IOException;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>import org.openqa.selenium.By;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>import org.openqa.selenium.WebDriver;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th>import org.openqa.selenium.WebElement;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>09<br><br></th>
    <th>import org.openqa.selenium.chrome.ChromeDr<br><br>iverService;</th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>import org.openqa.selenium.remote.DesiredC<br><br>apabilities;</th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>import org.openqa.selenium.remote.RemoteWe<br><br>bDriver;</th>
  </tr>
</table>


<table>
  <tr>
    <th>12<br><br></th>
    <th>import org.openqa.selenium.support.ui.Expe<br><br>ctedCondition;</th>
  </tr>
</table>


<table>
  <tr>
    <th>13<br><br></th>
    <th>import org.openqa.selenium.support.ui.WebD<br><br>riverWait;</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>public class ExampleForChrome {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th>public static void main(String[]<br><br>args) throws IOException {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>// 设置 chrome 的路径</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th>System.setProperty(</th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>"webdriver.chrome.driver" ,<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>20</th>
    <th>"C:\\Documents and Settings\\sq\\Local Settings\\Application Data\\Google\\Chrome\\Application\\c hrome.exe"<br><br>);</th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th>// 创建⼀个 ChromeDriver 的接⼝，⽤于 连接 Chrome<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th>@SuppressWarnings ( "deprecation" )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>23</th>
    <th>ChromeDriverService service = new ChromeDriverService.Builder()<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th>.usingChromeDriverExecutable(</th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>new File(<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>26<br><br></th>
    <th>"E:\\Selenium WebDriver\\chromedriver_win_23.0.124 0.0\\chromedriver.exe"<br><br>))</th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th>.usingAnyFreePort().build();</th>
  </tr>
</table>


<table>
  <tr>
    <th>28</th>
    <th>service.start();</th>
  </tr>
</table>


<table>
  <tr>
    <th>29</th>
    <th>// 创建⼀个 Chrome 的浏览器实例</th>
  </tr>
</table>


<table>
  <tr>
    <th>30</th>
    <th>WebDriver driver = new RemoteWebDriver(service.getUrl(),<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>31</th>
    <th>DesiredCapabilities.chrome());</th>
  </tr>
</table>


<table>
  <tr>
    <th>32</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>33</th>
    <th>// 让浏览器访问 Baidu</th>
  </tr>
</table>


<table>
  <tr>
    <th>34</th>
    <th>driver.get( " " );<br><br>http://www.baidu.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>35</th>
    <th>// ⽤下⾯代码也可以实现</th>
  </tr>
</table>


<table>
  <tr>
    <th>36<br><br></th>
    <th>// driver.navigate().to(" ");<br><br>http://w ww.baidu.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>37</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>38</th>
    <th>// 获取 ⽹⻚的 title</th>
  </tr>
</table>


<table>
  <tr>
    <th>39</th>
    <th>System.out.println( "1 Page<br><br>title is: "<br><br>+ driver.getTitle());</th>
  </tr>
</table>


<table>
  <tr>
    <th>40</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>41</th>
    <th>// 通过 id 找到 input 的 DOM</th>
  </tr>
</table>


<table>
  <tr>
    <th>42<br><br></th>
    <th>WebElement element =<br><br>driver.findElement(By.id( "kw" ));<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>43</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>44</th>
    <th>// 输⼊关键字</th>
  </tr>
</table>


<table>
  <tr>
    <th>45</th>
    <th>element.sendKeys( "zTree" );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>46</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>47</th>
    <th>// 提交 input 所在的 form</th>
  </tr>
</table>


<table>
  <tr>
    <th>48</th>
    <th>element.submit();</th>
  </tr>
</table>


<table>
  <tr>
    <th>49</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>50</th>
    <th>// 通过判断 title 内容等待搜索⻚⾯加载完 毕，间隔10秒<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>51</th>
    <th>( new WebDriverWait(driver, 10 )).until( new ExpectedCondition<Boolean>() {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>52</th>
    <th>public Boolean<br><br>apply(WebDriver d) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>53<br><br></th>
    <th>return d.getTitle().toLowerCase().endsWit<br><br>h( "ztree" );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>54</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>55</th>
    <th>});</th>
  </tr>
</table>


<table>
  <tr>
    <th>56</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>57</th>
    <th>// 显示搜索结果⻚⾯的 title</th>
  </tr>
</table>


<table>
  <tr>
    <th>58<br><br></th>
    <th>System.out.println( "2 Page<br><br>title is: "<br><br>+ driver.getTitle());</th>
  </tr>
</table>


<table>
  <tr>
    <th>59</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>60</th>
    <th>// 关闭浏览器</th>
  </tr>
</table>


<table>
  <tr>
    <th>61</th>
    <th>driver.quit();</th>
  </tr>
</table>


<table>
  <tr>
    <th>62</th>
    <th>// 关闭 ChromeDriver 接⼝</th>
  </tr>
</table>


<table>
  <tr>
    <th>63</th>
    <th>service.stop();</th>
  </tr>
</table>


<table>
  <tr>
    <th>64</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>65</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>66</th>
    <th>}</th>
  </tr>
</table>


运⾏⼀下看看，是不是⼀切OK了？

补充：仔细看了⼀下官⽹的介绍：Chrome Driver is maintained / suported by the Chromium project iteslf. 看来如果使⽤ new ChromeDriver() 的话，应该要安装 Chromium ⽽不是 Chrome，我现在懒得折腾了，有兴趣的童鞋可 以试验⼀下。

【4. 测试 IE】 想逃避 IE 吗？？ 作为前端开发，IE 你是必须要⾯对的，冲吧！ 其实你会发现， Selenium 主要也就是针对 FireFox 和 IE 来制作的，所以把 FireFox 的代码修改为 IE 的，那是相当的容易，只需要简单地两步：

- 1）把 ExampleForFireFox.java 另存为 ExampleForIE.java
- 2）把 WebDriver driver = new FirefoxDriver(); 修改为 WebDriver driver = new InternetExplorerDriver();
- 3）⼀般⼤家的 IE都是默认路径吧，所以也就不⽤设置 property 了


查看源码打印?

<table>
  <tr>
    <th>01</th>
    <th>package lesson1;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>import org.openqa.selenium.By;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>import org.openqa.selenium.WebDriver;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>import org.openqa.selenium.WebElement;<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>import org.openqa.selenium.ie.InternetExpl<br><br>orerDriver;</th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>import org.openqa.selenium.support.ui.Expe<br><br>ctedCondition;</th>
  </tr>
</table>


<table>
  <tr>
    <th>08<br><br></th>
    <th>import org.openqa.selenium.support.ui.WebD<br><br>riverWait;</th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>public class ExampleForIE {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>public static void main(String[]<br><br>args) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th>// 如果你的 FireFox 没有安装在默认⽬ 录，那么必须在程序中设置<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>13<br><br></th>
    <th>// System.setProperty("webdriver.firefo x.bin", "D:\\Program Files\\Mozilla Firefox\\firefox.exe");</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>// 创建⼀个 FireFox 的浏览器实例</th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>WebDriver driver = new InternetExplorerDriver();<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>// 让浏览器访问 Baidu</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th>driver.get( " " );<br><br>http://www.baidu.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>// ⽤下⾯代码也可以实现</th>
  </tr>
</table>


<table>
  <tr>
    <th>20<br><br></th>
    <th>// driver.navigate().to(" ");<br><br>http://w ww.baidu.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th>// 获取 ⽹⻚的 title</th>
  </tr>
</table>


<table>
  <tr>
    <th>23</th>
    <th>System.out.println( "1 Page<br><br>title is: "<br><br>+ driver.getTitle());</th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>// 通过 id 找到 input 的 DOM</th>
  </tr>
</table>


<table>
  <tr>
    <th>26<br><br></th>
    <th>WebElement element =<br><br>driver.findElement(By.id( "kw" ));<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>28</th>
    <th>// 输⼊关键字</th>
  </tr>
</table>


<table>
  <tr>
    <th>29</th>
    <th>element.sendKeys( "zTree" );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>30</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>31</th>
    <th>// 提交 input 所在的 form</th>
  </tr>
</table>


<table>
  <tr>
    <th>32</th>
    <th>element.submit();</th>
  </tr>
</table>


<table>
  <tr>
    <th>33</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>34<br><br></th>
    <th>// 通过判断 title 内容等待搜索⻚⾯加载完 毕，间隔10秒</th>
  </tr>
</table>


<table>
  <tr>
    <th>35</th>
    <th>( new WebDriverWait(driver, 10 )).until( new ExpectedCondition<Boolean>() {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>36<br><br></th>
    <th>public Boolean<br><br>apply(WebDriver d) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>37</th>
    <th>return d.getTitle().toLowerCase().endsWit<br><br>h( "ztree" );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>38</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>39</th>
    <th>});</th>
  </tr>
</table>


<table>
  <tr>
    <th>40</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>41</th>
    <th>// 显示搜索结果⻚⾯的 title</th>
  </tr>
</table>


<table>
  <tr>
    <th>42</th>
    <th>System.out.println( "2 Page<br><br>title is: "<br><br>+ driver.getTitle());</th>
  </tr>
</table>


<table>
  <tr>
    <th>43</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>44</th>
    <th>// 关闭浏览器</th>
  </tr>
</table>


<table>
  <tr>
    <th>45</th>
    <th>driver.quit();</th>
  </tr>
</table>


<table>
  <tr>
    <th>46</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>47</th>
    <th>}</th>
  </tr>
</table>


运⾏⼀下，是不是 so easy？

⼊⻔⼯作完成，现在完全可以利⽤ java 代码，让 Selenium ⾃动执⾏我们设置好的测试⽤例了，不 过 .这仅仅是个开始。

