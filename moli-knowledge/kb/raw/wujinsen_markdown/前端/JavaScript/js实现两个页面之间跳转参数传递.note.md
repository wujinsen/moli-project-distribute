# js实现两个⻚⾯之间跳转参数传递

html在设计时，规定跳转地址后加"?"表示从此开始为跟随⻚⾯地址跳转的参数。 有时候，我们希望获得相应的跳转前⻚⾯⾥的内容，这时候我们就可以考虑将内容以参数形式放到地

址中传过来，这⾥我建议将参数以变量形式传递。 代码案例如下：

- ⻚⾯A： <!DOCTYPE html> <html lang="en"> <head>

<meta charset="UTF-8"> <title>direct1.html</title>

</head> <body>

<div><a id="a1" href="direct2.html">点击此处</div> <script>

var a1=document.getElementById("a1"); var person={

name:"Zhuxingyu", age:18

}; a1.href=a1.href+"?"+person.name;

</script> </body> </html>

- ⻚⾯B <!DOCTYPE html> <html lang="en"> <head>


<meta charset="UTF-8"> <title>direct2.html</title>

</head> <body>

<script> var getInfo=window.location.search.slice(window.location.search.lastIndexOf("?")+1);

console.log(getInfo);

</script> </body> </html>

这样做有两点需要注意，第⼀，放在地址后的参数只能以字符串格式来传递，我尝试过传递对 象，现对象被解析成object后字符串化了，不能识别;第⼆，多次返回direct1⻚⾯跳转会重复加⼊参 数，你可以在direct2中通过正则来截取想要的那部分。

