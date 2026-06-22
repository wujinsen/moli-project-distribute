我的⼯作空间：d:workspace web项⽬名称： x 在eclipse配置完tomcat后，发布到的路径是 d:\workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebaps 下 这个路径太深了，受不了 我想使⽤tomcat的默认路径 例如我的tomcat安装在d:\tomcat下 默认的发布路径是d:\tomcat\webaps下 如何在eclipse中进⾏修改呢？

答案在下⾯

- 1：找到 Server ⾯板，右击当前的那个 Tomcat，先 remove 掉其中所有的⼯程
- 2：再右击那个 Tomcat，选择 Clean. 清空⼀下
- 3：双击那个 Tomcat，会打开属性⾯板，找到左边第⼆个 Server Locations，你那个单选框选中的应 该是第⼀个，你选择第三个，在 Server Path 中输⼊你想要的路径后，保存即可以了


也可以选择第⼆个是tomcat的安装路径

当然也可以通过配置tomcat进⾏发布

修改server.xml

在</Host>前⾯加上

<Context docBase="D:\apache-tomcat-6.0.20\webaps\Test" path="/Test" reloadable="true" source="org.eclipse.jst.j e.server:Test"/>

path指的是访问路径： :8080/Test

htp:/localhost

docBase是tomcat路径

