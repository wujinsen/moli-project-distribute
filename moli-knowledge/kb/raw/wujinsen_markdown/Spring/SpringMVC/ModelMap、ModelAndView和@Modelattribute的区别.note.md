## ⾸先介绍ModelMap和ModelAndView的作⽤

ModelMap

ModelMap对象主要⽤于传递控制⽅法处理数据到结果⻚⾯，也就是说我们把结果⻚⾯上需要的数据放 到ModelMap对象中即可，他的作⽤类似于request对象的setAttribute⽅法的作⽤，⽤来在⼀个请求过 程中传递处理的数据。通过以下⽅法向⻚⾯传递参数： addAttribute(String key,Object value); 在⻚⾯上可以通过el变量⽅式$key或者bboss的⼀系列 获取并展示modelmap中的数据。 modelmap本身不能设置⻚⾯跳转的url地址别名或者物理跳转地址，那么我们可以通过控制器⽅法的返 回值来设置跳转url地址别名或者物理跳转地址。

数据展示标签

ModelAndView

ModelAndView对象有两个作⽤： 作⽤⼀ 设置转向地址,如下所示（这也是ModelAndView和ModelMap的主要区别） ModelAndView view = new ModelAndView("path:ok");

作⽤⼆ ⽤于传递控制⽅法处理结果数据到结果⻚⾯，也就是说我们把需要在结果⻚⾯上需要的数据放 到ModelAndView对象中即可，他的作⽤类似于request对象的setAttribute⽅法的作⽤，⽤来在⼀个请求 过程中传递处理的数据。通过以下⽅法向⻚⾯传递参数： addObject(String key,Object value);

在⻚⾯上可以通过el变量⽅式$key或者bboss的⼀系列 获取并展示ModelAndView中的数 据。

数据展示标签

作⽤介绍完了后，接下来介绍使⽤⽅法

ModelMap

ModelMap的实例是由bboss mvc框架⾃动创建并作为控制器⽅法参数传⼊，⽤户⽆需⾃⼰创建。

Java代码

![image 1](<ModelMap、ModelAndView和@Modelattribute的区别.note_images/imageFile1.png>)

复制代码

![image 2](<ModelMap、ModelAndView和@Modelattribute的区别.note_images/imageFile2.png>)

收藏代码 public String xxxxmethod(String someparam,ModelMap model) {

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


//省略⽅法处理逻辑若⼲ //将数据放置到ModelMap对象model中,第⼆个参数可以是任何java类型 model.addAttribute("key",someparam);

......

- 7.
- 8.
- 9.


//返回跳转地址

return "path:handleok"; }

ModelAndView

ModelAndView的实例是由⽤户⼿动创建的，这也是和ModelMap的⼀个区别。

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


public ModelAndView xxxxmethod(String someparam) {

//省略⽅法处理逻辑若⼲ //构建ModelAndView实例，并设置跳转地址 ModelAndView view = new ModelAndView("path:handleok"); //将数据放置到ModelAndView对象view中,第⼆个参数可以是任何java类型 view.addObject("key",someparam);

...... //返回ModelAndView对象view

return view; }

@ModelAtribute

# ⼀、绑定请求参数到指定对象

Java代码

![image 3](<ModelMap、ModelAndView和@Modelattribute的区别.note_images/imageFile3.png>)

收藏代码

1.

public String test1(@ModelAttribute("user") UserModel user)

只是此处多了⼀个注解@ModelAtribute("user")，它的作⽤是将该绑定的命令对象以“user”为名称添加到模 型对象中供视图⻚⾯展示使⽤。我们此时可以在视图⻚⾯使⽤${user.username}来获取绑定的命令对象的属 性。 如请求参数包含“?username=zhang&pasword=123&workInfo.city=bj”⾃动绑定到user 中的workInfo属性的 city属性中。

Java代码

![image 4](<ModelMap、ModelAndView和@Modelattribute的区别.note_images/imageFile4.png>)

收藏代码 @RequestMapping(value="/model2/{username}") public String test2(@ModelAttribute("model") DataBinderTestModel model)

- 1.
- 2.


URI 模板变量也能⾃动绑定到命令对象中， 当你请求的URL 中包含 “bol=yes&schoInfo.specialty=computer&hobyList[0]=program&amp;amp;hobyList[1]=music&map[k ey1]=value1&map[key2]=value2&amp;state=blocked”会⾃动绑定到命令对象上。当URI模板变量和请求参 数同名时，URI模板变量具有⾼优先权。

# ⼆、暴露表单引⽤对象为模型数据

Java代码

![image 5](<ModelMap、ModelAndView和@Modelattribute的区别.note_images/imageFile5.png>)

收藏代码 /**

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


- * 设置这个注解之后可以直接在前端⻚⾯使⽤hb这个对象（List）集合

- * @return

- */


@ModelAttribute("hb") public List<String> hobbiesList(){

List<String> hobbise = new LinkedList<String>(); hobbise.add("basketball"); hobbise.add("football"); hobbise.add("tennis"); return hobbise;

}

JSP⻚⾯展示出来

Java代码

![image 6](<ModelMap、ModelAndView和@Modelattribute的区别.note_images/imageFile6.png>)

收藏代码 <br> 初始化的数据 ： ${hb } <br>

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.


<c:forEach items="${hb}" var="hobby" varStatus="vs">

<c:choose> <c:when test="${hobby == 'basketball'}"> 篮球<input type="checkbox" name="hobbies" value="basketball"> </c:when> <c:when test="${hobby == 'football'}">

⾜球<input type="checkbox" name="hobbies" value="football"> </c:when> <c:when test="${hobby == 'tennis'}">

⽹球<input type="checkbox" name="hobbies" value="tennis"> </c:when>

</c:choose>

17.

</c:forEach>

备注:

- 1、通过上⾯这种⽅式可以显示出⼀个集合的内容

- 2、上⾯的jsp代码使⽤的是JSTL，需要导⼊JSTL相关的jar包 <%@taglib prefix="c" uri="htp:/java.sun.com/jsp/jstl/core" %>


# 三、暴露@RequestMaping⽅法返回值为模型数据

Java代码

![image 7](<ModelMap、ModelAndView和@Modelattribute的区别.note_images/imageFile7.png>)

收藏代码

1.

public @ModelAttribute("user2") UserModel test3(@ModelAttribute("user2") UserModel user)

⼤家可以看到返回值类型是命令对象类型，⽽且通过@ModelAtribute("user2")注解，此时会暴露返回值到 模型数据（ 名字为user2 ） 中供视图展示使⽤

@ModelAtribute 注解的返回值会覆盖@RequestMaping 注解⽅法中的@ModelAtribute 注解的同名命令对 象

