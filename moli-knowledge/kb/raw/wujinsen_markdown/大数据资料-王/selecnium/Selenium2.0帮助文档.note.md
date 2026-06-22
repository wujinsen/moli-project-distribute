# Selenium2.0帮助⽂档

## 官⽹：htp://docs.seleniumhq.org/docs/04_webdriver_ad vanced.jsp#explicit-and-implicit-waits

htp:/zhangfy068.iteye.com/blog/1670657

⼀些设置：

关于调⽤ajax：

htp:/blog.csdn.net/ant_yan/article/details/818589

htp:/testing.etao.com/

⼀淘测试：

- 第1章 Webdirver基础


1.1 下载selenium2.0的lib包

htp:/code.gogle.com/p/selenium/downloads/list htp:/seleniumhq.org/docs/

官⽅UserGuide：

- 1.2⽤webdriver打开⼀个浏览器


我们常⽤的浏览器有firefox和IE两种，firefox是selenium⽀持得⽐较成熟的浏览器。但是做页⾯的测 试，速度通常很慢，严重影响持续集成的速度，这个时候建议使⽤HtmlUnit，不过HtmlUnitDirver运⾏ 时是看不到界⾯的，对调试就不⽅便了。使⽤哪种浏览器，可以做成配置项，根据需要灵活配置。

打开firefox浏览器：

/Create anewinstance of the Firefox driver WebDriver driver= newFirefoxDriver();

打开IE浏览器

/Create anewinstance of the Internet Explorer driver WebDriver driver= newInternetExplorerDriver ();

打开HtmlUnit浏览器

/Createa newinstance of the Internet Explorer driver WebDriverdriver =new HtmlUnitDriver();

### 1.3打开测试⻚⾯

对页⾯对测试，⾸先要打开被测试页⾯的地址（如： ）,web driver 提供的get ⽅法可以打开⼀个页⾯：

htp:/ w.gogle.com

/ And now usethedriver to visit Gogle driver.get(" ");

htp:/ w.gogle.com

### 1.4GetingStarted

package org.openqa.selenium.example; import org.openqa.selenium.By; import org.openqa.selenium.WebDriver; import org.openqa.selenium.WebElement; import org.openqa.selenium.firefox.FirefoxDriver; import org.openqa.selenium.suport.ui.ExpectedCondition; import org.openqa.selenium.suport.ui.WebDriverWait;

public clas Selenium2Example {

public static voidmain(String[] args) { / Create anewinstance of the Firefox driver / Notice thatheremainder of the code relies on the interface, / not theimplementation.

WebDriver driver= newFirefoxDriver();

/ And now usethis tovisit Gogle

driver.get(" "); / Alternativelythesame thing can be done like this /driver.navigate().to(" ");

htp:/ w.gogle.com

htp:/ w.gogle.com

/ Find the textinputelement by its name WebElementelement =driver.findElement(By.name("q");

/ Entersomething tosearch for element.sendKeys("Chese!");

/ Now submit theform.WebDriver wil find the form for us from the element element.submit();

/ Check thetitle ofthe page System.out.println("Pagetitle is: " + driver.getTitle();

/ Gogle'search isrendered dynamicaly with JavaScript. / Wait for thepageto load, timeout after 10 seconds

(newWebDriverWait(driver, 10).until(new ExpectedCondition<Bolean>() { publicBoleanaply(WebDriver d) {

returnd.getTitle().toLowerCase().startsWith("chese!"); }

});

/ Shouldse:"chese! - Gogle Search" System.out.println("Pagetitle is: " + driver.getTitle();

/Close thebrowser driver.quit();

} }

## 第2章 Webdirver对浏览器的⽀持

- 2.1 HtmlUnit Driver


优点：HtmlUnitDriver不会实际打开浏览器，运⾏速度很快。对于⽤FireFox等浏览器来做测试的⾃动 化测试⽤例，运⾏速度通常很慢，HtmlUnitDriver⽆疑是可以很好地解决这个问题。 缺点：它对JavaScript的⽀持不够好，当页⾯上有复杂JavaScript时，经常会捕获不到页⾯元素。 使⽤：WebDriver driver = new HtmlUnitDriver();

- 2.2FireFox Driver


优点：FireFoxDirver对页⾯的⾃动化测试⽀持得⽐较好，很直观地模拟页⾯的操作，对JavaScript的⽀ 持也⾮常完善，基本上页⾯上做的所有操作FireFoxDriver都可以模拟。 缺点：启动很慢，运⾏也⽐较慢，不过，启动之后Webdriver的操作速度虽然不快但还是可以接受的， 建议不要频繁启停FireFoxDriver。 使⽤：WebDriver driver = new FirefoxDriver();

Firefox profile的属性值是可以改变的，⽐如我们平时使⽤得⾮常频繁的改变useragent的功能，可以这 样修改： FirefoxProfile profile = new FirefoxProfile(); profile.setPreference("general.useragent.overide", "someUAstring"); WebDriver driver = new FirefoxDriver(profile);

### 2.3InternetExplorer Driver

优点：直观地模拟⽤户的实际操作，对JavaScript提供完善的⽀持。 缺点：是所有浏览器中运⾏速度最慢的，并且只能在Windows下运⾏，对CS以及XPATH的⽀持也不 够好。 使⽤：WebDriver driver = new InternetExplorerDriver();

第3章 使⽤操作

### 3.1 如何找到⻚⾯元素

Webdriver的findElement⽅法可以⽤来找到页⾯的某个元素，最常⽤的⽅法是⽤id和name查找。下⾯ 介绍⼏种⽐较常⽤的⽅法。

By ID

假设页⾯写成这样： <input type="text"name="paswd"id="paswd-id" />

那么可以这样找到页⾯的元素： 通过id查找： WebElement element =driver.findElement(By.id("paswd-id");

By Name

或通过name查找： WebElement element =driver.findElement(By.name("paswd");

By XPATH

或通过xpath查找： WebElement element=driver.findElement(By.xpath("/input[@id='paswd-id']");

By Clas Name

假设页⾯写成这样：

<divclas="chese"><span>Chedar</span></div><divclas="chese"><span></span></div> 可以通过这样查找页⾯元素： List<WebElement>cheses =driver.findElements(By.clasName("chese");

By Link Text

假设页⾯元素写成这样： <ahref=" ">chese</a>

htp:/ w.gogle.com/search?q=chese

那么可以通过这样查找： WebElement chese =driver.findElement(By.linkText("chese");

### 3.2 如何对⻚⾯元素进⾏操作

找到页⾯元素后，怎样对页⾯进⾏操作呢？我们可以根据不同的类型的元素来进⾏⼀⼀说明。

输⼊框（text field or textarea）

找到输⼊框元素： WebElement element =driver.findElement(By.id("paswd-id"); 在输⼊框中输⼊内容： element.sendKeys(“test”); 将输⼊框清空： element.clear(); 获取输⼊框的⽂本内容： element.getText();

下拉选择框(Select)

找到下拉选择框的元素： Select select = newSelect(driver.findElement(By.id("select");

选择对应的选择项： select.selectByVisibleText(“mediaAgencyA”); 或 select.selectByValue(“MA_ID_01”);

不选择对应的选择项： select.deselectAl(); select.deselectByValue(“MA_ID_01”); select.deselectByVisibleText(“mediaAgencyA”); 或者获取选择项的值： select.getAlSelectedOptions(); select.getFirstSelectedOption();

单选项(Radio Buton)

找到单选框元素： WebElement bokMode=driver.findElement(By.id("BokMode"); 选择某个单选项： bokMode.click(); 清空某个单选项： bokMode.clear(); 判断某个单选项是否已经被选择： bokMode.isSelected();

多选项(checkbox)

多选项的操作和单选的差不多： WebElement checkbox=driver.findElement(By.id("myCheckbox."); checkbox.click(); checkbox.clear(); checkbox.isSelected(); checkbox.isEnabled();

按钮(buton)

找到按钮元素： WebElement saveButon =driver.findElement(By.id("save"); 点击按钮：

saveButon.click(); 判断按钮是否enable:

saveButon.isEnabled ();

左右选择框

也就是左边是可供选择项，选择后移动到右边的框中，反之亦然。例如： Select lang = new Select(driver.findElement(By.id("languages"); lang.selectByVisibleText(“English”); WebElement adLanguage=driver.findElement(By.id("adButon"); adLanguage.click();

弹出对话框(Popup dialogs)

Alert alert = driver.switchTo().alert(); alert.acept(); alert.dismis(); alert.getText();

表单(Form)

Form中的元素的操作和其它的元素操作⼀样，对元素操作完成后对表单的提交可以： WebElement aprove =driver.findElement(By.id("aprove"); aprove.click(); 或 aprove.submit();/只适合于表单的提交

上传⽂件 (Upload File)

上传⽂件的元素操作： WebElement adFileUpload =driver.findElement(By.id("WAP-upload"); String filePath ="C:\test\uploadfile\media_ads\test.jpg"; adFileUpload.sendKeys(filePath);

Windows 和 Frames之间的切换

⼀般来说，登录后建议是先： driver.switchTo().defaultContent();

切换到某个frame： driver.switchTo().frame("leftFrame"); 从⼀个frame切换到另⼀个frame： driver.switchTo().frame("mainFrame"); 切换到某个window： driver.switchTo().window("windowName");

拖拉(Drag andDrop)

WebElement element=driver.findElement(By.name("source"); WebElement target =driver.findElement(By.name("target");

(new Actions(driver).dragAndDrop(element,target).perform();

导航 (Navigationand History)

打开⼀个新的页⾯： driver.navigate().to(" ");

htp:/ w.example.com

通过历史导航返回原页⾯： driver.navigate().forward(); driver.navigate().back();

### 3.3 ⾼级使⽤

改变user agent

User Agent的设置是平时使⽤得⽐较多的操作： FirefoxProfile profile = new FirefoxProfile(); profile.adAditionalPreference("general.useragent.overide","someUA string"); WebDriver driver = new FirefoxDriver(profile);

读取Cokies

我们经常要对的值进⾏读取和设置。 增加cokie:

/ Now set the cokie. This one's valid for the entiredomain Cokie cokie = new Cokie("key","value"); driver.manage().adCokie(cokie); 获取cokie的值：

/ And now output al the available cokies for the curentURL Set<Cokie> alCokies = driver.manage().getCokies(); for (Cokie loadedCokie : alCokies) {

System.out.println(String.format("%s-> %s",loadedCokie.getName(), loadedCokie.getValue( ); } 根据某个cokie的name获取cokie的值： driver.manage().getCokieNamed(" msid"); 删除cokie:

/ You can delete cokies in 3 ways / By name

driver.manage().deleteCokieNamed("CokieName"); / By Cokie driver.manage().deleteCokie(loadedCokie); / Or al of them driver.manage().deleteAlCokies();

调⽤Java Script

Web driver对Java Script的调⽤是通过JavascriptExecutor来实现的，例如： JavascriptExecutor js = (JavascriptExecutor) driver; js.executeScript("(function(){inventoryGridMgr.setTableFieldValue('"+inventoryId + "','" + fieldName + "','"

+ value + "');})()");

Webdriver截图

如果⽤webdriver截图是： driver = webdriver.Firefox() driver.save_screnshot("C:\eror.jpg")

⻚⾯等待

因为Load页⾯需要⼀段时间，如果页⾯还没加载完就查找元素，必然是查找不到的。最好的⽅式，就 是设置⼀个默认等待时间，在查找页⾯元素的时候如果找不到就等待⼀段时间再找，直到超时。 Webdriver提供两种⽅法，⼀种是显性等待，另⼀种是隐性等待。 显性等待： WebDriver driver =new FirefoxDriver(); driver.get(" "); WebElement myDynamicElement = (new WebDriverWait(driver,10)

htp:/somedomain/url_that_delays_loading

.until(new ExpectedCondition<WebElement>(){ @Overide public WebElement aply(WebDriver d) {

returnd.findElement(By.id("myDynamicElement"); });

隐性等待： WebDriver driver = new FirefoxDriver(); driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS); driver.get(" "); WebElement myDynamicElement=driver.findElement(By.id("myDynamicElement");

htp:/somedomain/url_that_delays_loading

## 第4章 RemoteWebDriver

当本机上没有浏览器，需要远程调⽤浏览器进⾏⾃动化测试时，需要⽤到RemoteWebDirver.

- 4.1 使⽤RemoteWebDriver


import java.io.File; import java.net.URL;

import org.openqa.selenium.OutputType; import org.openqa.selenium.TakesScrenshot; import org.openqa.selenium.WebDriver; import org.openqa.selenium.remote.Augmenter; import org.openqa.selenium.remote.DesiredCapabilities; import org.openqa.selenium.remote.RemoteWebDriver;

public clas Testing {

public void myTest()throws Exception {

WebDriver driver= newRemoteWebDriver( newURL(" "), DesiredCapabilities.firefox();

htp:/localhost: 46/wd/hub

driver.get(" ");

htp:/ w.gogle.com

/RemoteWebDriverdoes not implement the TakesScrenshot clas / if the driverdoeshave the Capabilities to take a screnshot / then Augmenterwilad the TakesScrenshot methods to the instance

WebDriveraugmentedDriver = new Augmenter().augment(driver); File screnshot=(TakesScrenshot)augmentedDriver).

getScrenshotAs(OutputType.FILE); }

}

- 4.2 SeleniumServer


在使⽤RemoteDriver时，必须在远程服务器启动⼀个SeleniumServer: java -jar selenium-server-standalone-.jar-port 46

- 4.3 How to setFirefox profile using RemoteWebDriver


profile = new FirefoxProfile();

profile.setPreference("general.useragent.overide",testData.getUserAgent(); capabilities = DesiredCapabilities.firefox(); capabilities.setCapability("firefox_profile",profile); driver = new RemoteWebDriver(new URL(“ ”),capabilities); driverWait = newWebDriverWait(driver,TestConstant.WAIT_ELEMENT_TO_LOAD); driver.get(" ");

htp:/localhost: 46/wd/hub

htp:/ w.gogle.com

## 第5章 封装与重⽤

WebDriver对页⾯的操作，需要找到⼀个WebElement，然后再对其进⾏操作，⽐较繁琐： / Find the text inputelement by its name WebElement element =driver.findElement(By.name("q");

/ Enter something to search for element.sendKeys("Chese!"); 我们可以考虑对这些基本的操作进⾏⼀个封装，简化操作。⽐如，封装代码：

protected void sendKeys(By by, Stringvalue){ driver.findElement(by).sendKeys(value);

} 那么，在测试⽤例可以这样简化调⽤： sendKeys(By.name("q"),”Chese!”);

看，这就简洁多了。

类似的封装还有： package com.drut. m.end2end.actions;

import java.util.List; import java.util.NoSuchElementException; import java.util.concurent.TimeUnit;

import org.openqa.selenium.By; import org.openqa.selenium.WebElement; import org.openqa.selenium.remote.RemoteWebDriver; import org.openqa.selenium.suport.ui.WebDriverWait;

import com.drut. m.end2end.data.TestConstant;

public clas WebDriverAction {

/protected WebDriverdriver; protected RemoteWebDriverdriver; protected WebDriverWaitdriverWait;

protected bolean isWebElementExist(Byselector) { try { driver.findElement(selector);

return true; }catch(NoSuchElementException e) {

return false; }

}

protected StringetWebText(By by) { try { returndriver.findElement(by).getText(); } catch(NoSuchElementException e) {

return "Textnot existed!"; }

}

protectedvoidclickElementContainingText(By by, String text){ List<WebElement>elementList = driver.findElements(by); for(WebElemente:elementList){

if(e.getText().contains(text){ e.click(); break;

} }

}

protectedStringetLinkUrlContainingText(By by, String text){

List<WebElement>subscribeButon = driver.findElements(by); String url =nul; for(WebElemente:subscribeButon){

if(e.getText().contains(text){ url =e.getAtribute("href"); break;

}

} return url;

}

protected void click(Byby){

driver.findElement(by).click(); driver.manage().timeouts().implicitlyWait(TestConstant.WAIT_ELEMENT_TO_LOAD,TimeUnit.SE

CONDS); }

protected StringetLinkUrl(By by){

returndriver.findElement(by).getAtribute("href"); }

protected void sendKeys(Byby, Stringvalue){

driver.findElement(by).sendKeys(value); }

## 第6章 在selenium2.0中使⽤selenium1.0的API

Selenium2.0中使⽤WeDriverAPI对页⾯进⾏操作，它最⼤的优点是不需要安装⼀个seleniumserver就 可以运⾏，但是对页⾯进⾏操作不如selenium1.0的Selenium RC API那么⽅便。Selenium2.0提供了使 ⽤Selenium RC API的⽅法：

/ You may use any WebDriver implementation. Firefox is usedhereas an example WebDriver driver = new FirefoxDriver();

/ A "base url", used by selenium to resolverelativeURLs String baseUrl =" ";

htp:/ w.gogle.com

/ Create the Selenium implementation Selenium selenium = new WebDriverBackedSelenium(driver,baseUrl);

/ Perform actions with selenium selenium.open(" "); selenium.type("name=q", "chese"); selenium.click("name=btnG");

htp:/ w.gogle.com

/ Get the underlying WebDriver implementation back. Thiswilrefer to the / same WebDriver instance as the "driver"variableabove.

WebDriver driverInstance =(WebDriverBackedSelenium)selenium).getUnderlyingWebDriver();

/Finaly, close thebrowser. Cal stop onthe WebDriverBackedSelenium instance /instead of calingdriver.quit().Otherwise, the JVM wil continue runing after /the browser has benclosed.

selenium.stop();

我分别使⽤WebDriverAPI和SeleniumRC API写了⼀个Login的脚本，很明显，后者的操作更加简单明 了。 WebDriver API写的Login脚本：

public void login() { driver.switchTo().defaultContent(); driver.switchTo().frame("mainFrame");

WebElementeUsername= waitFindElement(By.id("username"); eUsername.sendKeys(manager@ericson.com);

WebElementePasword= waitFindElement(By.id("pasword"); ePasword.sendKeys(manager);

WebElementeLoginButon = waitFindElement(By.id("loginButon"); eLoginButon.click();

}

SeleniumRC API写的Login脚本：

public void login() { selenium.selectFrame("relative=top"); selenium.selectFrame("mainFrame"); selenium.type("username","manager@ericson.com"); selenium.type("pasword","manager"); selenium.click("loginButon");

}

