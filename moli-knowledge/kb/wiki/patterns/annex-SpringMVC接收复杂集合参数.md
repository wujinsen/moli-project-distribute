---
title: SpringMVC接收复杂集合参数.note（原文插图 annex）
slug: annex-SpringMVC接收复杂集合参数
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/Spring/SpringMVC/SpringMVC接收复杂集合参数.note.md
related: [spring框架中的设计模式]
created: 2026-07-05
updated: 2026-07-05
---

Spring MVC在接收集合请求参数时，需要在Controller⽅法的集合参数⾥前添加@RequestBody，⽽ @RequestBody默认接收的enctype (MIME编码)是application/json，因此发送POST请求时需要设置请求报 ⽂头信息，否则Spring MVC在解析集合请求参数时不会⾃动的转换成JSON数据再解析成相应的集合。 以下列举接收List<String>、List<User>、List<Map<String,Object>>、User[]、User(bean⾥⾯包含List)⼏ 种较为复杂的集合参数示例：

接收List<String>集合参数：

- 1、⻚⾯js代码：

Js代码

- 2、Controller⽅法：


![image 1](assets/imageFile1.png)

var idList = new Array();

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
- 17.


- idList.push(“1”);

- idList.push(“2”);

- idList.push(“3”); var isBatch = false; $.ajax({


type: "POST", url: "<%=path%>/catalog.do?fn=deleteCatalogSchemes", dataType: 'json', data: {"idList":idList,"isBatch":isBatch}, success: function(data){

…

}, error: function(res){

… }

});

Java代码

![image 2](assets/imageFile2.png)

@Controller @RequestMapping("/catalog.do") public class CatalogController {

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


@RequestMapping(params = "fn=deleteCatalogSchemes") @ResponseBody public AjaxJson deleteCatalogSchemes(@RequestParam("idList[]") List<String> idList,Boolean i

sBatch) {

… }

}

接收List<User>、User[]集合参数：

- 1、User实体类：

Java代码

- 2、⻚⾯js代码： Js代码

- 3、Controller⽅法： Java代码


![image 3](assets/imageFile3.png)

public class User {

- 1.
- 2.
- 3.
- 4.
- 5.


private String name; private String pwd; //省略getter/setter

}

![image 4](assets/imageFile4.png)

var userList = new Array(); userList.push({name: "李四",pwd: "123"}); userList.push({name: "张三",pwd: "332"}); $.ajax({

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


type: "POST", url: "<%=path%>/catalog.do?fn=saveUsers", data: JSON.stringify(userList),//将对象序列化成JSON字符串 dataType:"json", contentType : 'application/json;charset=utf-8', //设置请求头信息 success: function(data){

…

}, error: function(res){

… }

});

![image 5](assets/imageFile5.png)

@Controller @RequestMapping("/catalog.do") public class CatalogController {

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


@RequestMapping(params = "fn=saveUsers") @ResponseBody public AjaxJson saveUsers(@RequestBody List<User> userList) {

… }

}

如果想要接收User[]数组，只需要把saveUsers的参数类型改为@RequestBody User[] userArray就⾏了。

# 接收List<Map<String,Object>>集合参数：

1、⻚⾯js代码（不需要User对象了）： Js代码

![image 6](assets/imageFile6.png)

var userList = new Array(); userList.push({name: "李四",pwd: "123"}); userList.push({name: "张三",pwd: "332"}); $.ajax({

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


type: "POST", url: "<%=path%>/catalog.do?fn=saveUsers", data: JSON.stringify(userList),//将对象序列化成JSON字符串 dataType:"json", contentType : 'application/json;charset=utf-8', //设置请求头信息 success: function(data){

…

}, error: function(res){

… }

});

2、Controller⽅法： Java代码

![image 7](assets/imageFile7.png)

@Controller @RequestMapping("/catalog.do") public class CatalogController {

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


@RequestMapping(params = "fn=saveUsers") @ResponseBody public AjaxJson saveUsers(@RequestBody List<Map<String,Object>> listMap) {

… }

}

接收User(bean⾥⾯包含List)集合参数：

- 1、User实体类： Java代码

- 2、⻚⾯js代码：


![image 8](assets/imageFile8.png)

public class User { private String name; private String pwd; private List<User> customers;//属于⽤户的客户群 //省略getter/setter

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


}

Js代码

![image 9](assets/imageFile9.png)

var customerArray = new Array(); customerArray.push({name: "李四",pwd: "123"}); customerArray.push({name: "张三",pwd: "332"}); var user = {};

- 1.
- 2.
- 3.
- 4.


user.name = "李刚"; user.pwd = "888"; user.customers = customerArray; $.ajax({

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
- 17.
- 18.
- 19.
- 20.


type: "POST", url: "<%=path%>/catalog.do?fn=saveUsers", data: JSON.stringify(user),//将对象序列化成JSON字符串 dataType:"json", contentType : 'application/json;charset=utf-8', //设置请求头信息 success: function(data){

…

}, error: function(res){

… }

});

3、Controller⽅法： Java代码

![image 10](assets/imageFile10.png)

@Controller @RequestMapping("/catalog.do") public class CatalogController {

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


@RequestMapping(params = "fn=saveUsers") @ResponseBody public AjaxJson saveUsers(@RequestBody User user) { List<User> customers = user.getCustomers(); …

} }
