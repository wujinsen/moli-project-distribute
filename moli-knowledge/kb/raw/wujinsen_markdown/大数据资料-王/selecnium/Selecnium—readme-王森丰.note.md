本地开发测试：

- 1、创建⼯程ApTestDemo
- 2、导⼊jar包：


![image 1](<Selecnium—readme-王森丰.note_images/imageFile1.png>)

![image 2](<Selecnium—readme-王森丰.note_images/imageFile2.png>)

- 3、创建clas：SeleniumTest3 package ap.test.demo.selenium;


import java.net.MalformedURLException; import java.net.URL; import org.openqa.selenium.By; import org.openqa.selenium.WebDriver; import org.openqa.selenium.WebElement; import org.openqa.selenium.firefox.FirefoxDriver; import org.openqa.selenium.remote.DesiredCapabilities; import org.openqa.selenium.remote.RemoteWebDriver; import org.openqa.selenium.suport.ui.ExpectedCondition;

import org.openqa.selenium.suport.ui.WebDriverWait; public clas SeleniumTest3 {

/*

- * @throws MalformedURLException
- */ public static void main(String[] args) throws MalformedURLException {


/ 如果你的 FireFox 没有安装在默认⽬录，那么必须在程序中设置 System.setProperty("webdriver.firefox.bin", "F:\Program Files\firefox\Mozila

Firefox\firefox.exe"); / 创建⼀个 FireFox 的浏览器实例 WebDriver driver = new FirefoxDriver(); / 让浏览器访问 Baidu driver.get(" "); / ⽤下⾯代码也可以实现 / driver.navigate().to(" "); / 获取 ⽹⻚的 title System.out.println("1 Page title is: " + driver.getTitle(); / 通过 id 找到 input 的 DOM WebElement element = driver.findElement(By.id("kw");

htp:/ w.baidu.com

htp:/ w.baidu.com

/ 输⼊关键字 element.sendKeys("zTre"); / 提交 input 所在的 form

element.submit(); / 通过判断 title 内容等待搜索⻚⾯加载完毕，间隔10秒 (new WebDriverWait(driver, 10).until(new ExpectedCondition<Bolean>() { public Bolean aply(WebDriver d) {

return d.getTitle().toLowerCase().endsWith("ztre"); }

}); / 显示搜索结果⻚⾯的 title

System.out.println("2 Page title is: " + driver.getTitle(); /关闭浏览器

driver.quit(); }

}

- 4、运⾏


调⽤远程浏览器：Selecnium grid

- 1、准备：

- 1、需要两台机⼦
- 2、两台机⼦分别安装好JDK环境
- 3、两台机⼦需要从 下载selenium-serverstandalone-*.jar包


- 2、启动grid：


htp:/code.gogle.com/p/selenium/downloads/list

Grid需要⼀台机⼦做为主节点，然后其它机⼦做为⼦节点连接到这个主节点上来。所以⾸先 要启动主节点。

- 2.1启动主节点： 选⼀台机⼦做为主节点。打开命令⾏，cd⾄selenium-server-standalone-*.jar包的⽬录下，然 后⽤下⾯的命令启动主节点服务： java -jar selenium-server-standalone-2.32.0.jar -role hub 默认启动默认端⼝为 4。如果要改这个端⼝，可以再上⾯的命令后⾯加上 -port X。启 动完后，你可以⽤浏览 器 打开 这个⽹址查看主节点的 状态。
- 2.2启动⼦节点：


htp:/localhost: 4/grid/console

先另⼀台机⼦做为⼦节点。同样打开命令⾏，cd⾄selenium-server-standalone-*.jar包的⽬录 下，然后⽤下⾯的命令启动次节点服务： #显然，可以启动多个Node：

java -jar selenium-server-standalone-2.32.0.jar -port 5 -role node -hub

htp:/172.24.1 2.29  4/grid/register

- java -jar selenium-server-standalone-2.32.0.jar -port 56 -role node -hub
- java -jar selenium-server-standalone-2.32.0.jar -port 57 -role node -hub


htp:/172.24.1 2.29  4/grid/register

htp:/172.24.1 2.29  4/grid/register

其中192.168.40.24为主节点机⼦的ip地址，可以使⽤ipconfig命令在命令⾏查看得到。上⾯命 令默认启动 5端⼝，可使⽤-port 更改。启动完成连接到主节点后，可以在主节点机⼦上 ， ⽹址查看到这个⼦节点状态。使⽤同样的⽅法，可以链 接其它的⼦节点

htp:/localhost: 4/grid/console

- 3、在主节点服务器上创建clas：SeleniumTest2


package ap.test.demo.selenium;

import java.net.MalformedURLException; import java.net.URL; import org.openqa.selenium.By; import org.openqa.selenium.WebDriver; import org.openqa.selenium.WebElement; import org.openqa.selenium.firefox.FirefoxDriver; import org.openqa.selenium.remote.DesiredCapabilities; import org.openqa.selenium.remote.RemoteWebDriver; import org.openqa.selenium.suport.ui.ExpectedCondition; import org.openqa.selenium.suport.ui.WebDriverWait; import java.net.MalformedURLException; import java.net.URL; import org.openqa.selenium.WebDriver; import org.openqa.selenium.remote.DesiredCapabilities; import org.openqa.selenium.remote.RemoteWebDriver; public clas SeleniumTest2 {

/*

- * @throws Exception
- */ public static void main(String[] args) throws Exception {


- creatNode1();
- creatNode2();
- creatNode3();


} public static void creatNode1() throws Exception{

DesiredCapabilities test = DesiredCapabilities.firefox(); test.setCapability("firefox_binary","D:\Program Files (x86)\Mozila

Firefox\firefox.exe");

htp:/172.24.10.234  5/wd/ hub

WebDriver driver = new RemoteWebDriver(new URL(" "),test);

driver.get(" ");

htp:/ w.baidu.com

- System.out.println("1 Page title is: " + driver.getTitle(); / 通过 id 找到 input 的 DOM


WebElement element = driver.findElement(By.id("kw"); System.out.println(element);

/ 输⼊关键字

element.sendKeys("zTre"); / 提交 input 所在的 form element.submit();

/ 通过判断 title 内容等待搜索⻚⾯加载完毕，间隔10秒 (new WebDriverWait(driver, 10).until(new ExpectedCondition<Bolean>() { public Bolean aply(WebDriver d) { System.out.println(d.getTitle().toLowerCase();

return d.getTitle().toLowerCase().startsWith("ztre"); }

}); / 显示搜索结果⻚⾯的 title

- System.out.println("2 Page title is: " + driver.getTitle(); /关闭浏览器


driver.quit();

} public static void creatNode2() throws Exception{

DesiredCapabilities test = DesiredCapabilities.firefox(); test.setCapability("firefox_binary","D:\Program Files (x86)\Mozila

Firefox\firefox.exe"); WebDriver driver = new RemoteWebDriver(new URL("

htp:/172.24.10.234  56/wd/ hub

"),test); driver.get(" "); System.out.println("1 Page title is: " + driver.getTitle();

htp:/ w.baidu.com

/ 通过 id 找到 input 的 DOM

WebElement element = driver.findElement(By.id("kw"); System.out.println(element);

/ 输⼊关键字

element.sendKeys("zTre"); / 提交 input 所在的 form element.submit();

/ 通过判断 title 内容等待搜索⻚⾯加载完毕，间隔10秒 (new WebDriverWait(driver, 10).until(new ExpectedCondition<Bolean>() { public Bolean aply(WebDriver d) { System.out.println(d.getTitle().toLowerCase(); return d.getTitle().toLowerCase().startsWith("ztre");

}

}); / 显示搜索结果⻚⾯的 title

System.out.println("2 Page title is: " + driver.getTitle();

/关闭浏览器 driver.quit();

} public static void creatNode3() throws Exception{

DesiredCapabilities test = DesiredCapabilities.firefox(); test.setCapability("firefox_binary","D:\Program Files (x86)\Mozila

Firefox\firefox.exe");

htp:/172.24.10.234  57/wd/h ub

WebDriver driver = new RemoteWebDriver(new URL(" "),test);

driver.get(" ");

htp:/ w.baidu.com

- System.out.println("1 Page title is: " + driver.getTitle(); / 通过 id 找到 input 的 DOM

WebElement element = driver.findElement(By.id("kw"); System.out.println(element);

/ 输⼊关键字

element.sendKeys("zTre"); / 提交 input 所在的 form element.submit();

/ 通过判断 title 内容等待搜索⻚⾯加载完毕，间隔10秒 (new WebDriverWait(driver, 10).until(new ExpectedCondition<Bolean>() { public Bolean aply(WebDriver d) { System.out.println(d.getTitle().toLowerCase();

return d.getTitle().toLowerCase().startsWith("ztre"); }

}); / 显示搜索结果⻚⾯的 title

- System.out.println("2 Page title is: " + driver.getTitle(); /关闭浏览器


driver.quit(); }

}

值得注意的是： WebDriver dr = new RemoteWebDriver(new URL(" "),test);

htp:/192.168.40.67  5/wd/hub

这⼀句中的192.168.40.67为次节点的ip地址。

