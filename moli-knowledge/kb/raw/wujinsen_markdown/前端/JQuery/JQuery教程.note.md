JQuery严格来讲并不复杂，JQuery的核⼼原理 = DOM解析。请尽量记下。 JQuery实际上是⼀个JavaScript框架技术，利⽤JQuery可以轻松的实现⻚⾯数据选择、简单的DOM操作，JQuery最早的时候是

由⼀个美国⼈提出的 —— John Resig，后来也是经过了全世界各个JavaScript的⾼⼿进⾏不断完善的，可以说到今天为⽌所有出现的前 台框架就以JQuery的⽣命⼒是最顽强的。

JQuery之中有⼀个主要的设计原则“WRITER LES DO MORE”，写更少的代码做更多的事情。⽤户可以直接登录 “htp:/jquery.com”上下载JQuery的相应开发包。

那么为什么需要去使⽤JQuery呢？下⾯通过⼀个简单的程序来做⼀个分析。 范例：取得⼀个⽂本框的输⼊数据 —— 原始的⽅式实现

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function show() { alert(document.getElementById("info").value) ; }<br><br></script> </head> <body><br><br>输⼊内容：<input type="text" name="info" id="info"> <input type="buton" value="显示内容" onclick="show()"><br><br></body></th>
  </tr>
</table>


</html>

但是如果按照以上的⽅式编写，是使⽤了最原始的技术，但是经过⻓时间的代码编写应该会发现到处都去使⽤ “document.getElementById()”实在是⼀种折磨。 范例：利⽤JQuery取得

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function show() { alert($("#info").val() ; }<br><br></script> </head> <body><br><br>输⼊内容：<input type="text" name="info" id="info"> <input type="buton" value="显示内容" onclick="show()"><br><br></body></th>
  </tr>
</table>


</html>

通过以上的代码可以发现使⽤“$("#id内容")”实际上代表的就是根据ID取得指定的元素，⽽后的val()函数主要是取得输⼊的内容。 使⽤JQuery除了可以⽅便的取得⽂本框之中的数据之外，也可以取得⼀些标记内的数据。

范例：取得指定标记内的数据

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(document).ready(function() { alert($("#info").html() ; }) ;<br><br></script> </head> <body><br><br><div id="info"> <h2>赵冬努⼒学习，当⽼师</h2> </div> </body></th>
  </tr>
</table>


</html>

本程序给出的“$(document)”表示的是整个的⽂档对象，⽽本次使⽤的ready()函数就类似于onload事件⼀样，但是在整个⻚⾯都 加载完成之后才进⾏触发的。

但是对于以上的代码也有另外⼀种写法。

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { alert($("#info").html() ; }) ;</th>
  </tr>
</table>


</script>

这样的形式是在⼯作之中使⽤最多的⼀种形式。

## 3.2、选择器（重点）

选择器实际上是JQuery的最⼤特⾊，使⽤选择器可以⽅便的进⾏各个操作元素内容的取得，但是在JQuery之中所提供的选择器⾮ 常的强⼤。

- 3.2.1、基本选择器


基本选择器提供了根据元素ID、元素名称、cs样式的取得⽅式，提供的选择器有如下⼏种。

<table>
  <tr>
    <th>No.</th>
    <th>基本选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>#id</td>
    <td>根据指定的元素id取得数据，就 相当于 “document.getElementById() ”函数<br><br>· 范例： $("#msg").atr("title")； · 范例：$("#msg").val()。</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>2</td>
    <td>element</td>
    <td>根据指定的元素取得数据，例 如：⼀组“<input>”<br><br>· 范例： $("input").atr("value"," a") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>.clas</td>
    <td>根据指定的cs取得元素数据 · 范例：</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>*</td>
    <td>$(".init").atr("value"," a") ; 取得全部的元素<br><br>· 范例： $("*").atr("value"," a") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>5</td>
    <td>元素,元素,…</td>
    <td>取得指定个元素的对象 · 范例：$("div,span").text("</td>
    <td>元素集合</td>
  </tr>
</table>


<h1> a</h1>") ;

范例：根据id取得数据

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function showFun() { alert("title = " + $("#msg").atr("title") + "，value = " + $("#msg").atr("value") ; alert("title = " + $("#msg").atr("title") + "，value = " + $("#msg").val() ; }<br><br></script> </head> <body><br><br><input type="text" name="msg" id="msg" title="请输⼊信息" value="王鹏当⽼师，我看⾏！"> <input type="buton" value="显示" onclick="showFun()"><br><br></body></th>
  </tr>
</table>


</html>

既然以上的操作之中可以通过“atr(属性名称)”函数取得了元素的属性，那么实际上也可以利⽤“atr(属性名称,内容)”函数设置属 性内容。 范例：设置指定元素的内容

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function showFun() { $("#msg").atr("title" , "好好学习天天向上！") ; $("#msg").atr("value" , " a") ; }<br><br></script> </head> <body><br><br><input type="text" name="msg" id="msg" title="请输⼊信息" value="王鹏当⽼师，我看⾏！"> <input type="buton" value="显示" onclick="showFun()"><br><br></body></th>
  </tr>
</table>


</html>

如果使⽤DOM完成以上的功能，那么是⾮常麻烦的，应该取得元素，⽽后设置属性的内容，但是在JQuery之中，DOM操作变得如 此的简单。 范例：取得所有的输⼊元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function showFun() { $("input").atr("value"," a") ; }<br><br></script> </head> <body><br><br>⽤户名：<input type="text" name="userid" id="userid"><br> 年&nbsp;&nbsp;龄：<input type="text" name="age" id="age"><br> 学&nbsp;&nbsp;校：<input type="text" name="schol" id="schol"><br> <input type="buton" value="显示" onclick="showFun()"><br><br></body></th>
  </tr>
</table>


</html>

这个时候如果要想进⾏数据的清空操作，那么就变得⾮常容易了，只需要取得全部的input之后，进⾏value属性为空字符串的设置 即可。

实际上以上的选择操作如果划分到复选框上是很好⽤的，全选。 范例：实现⼀个简单的全选钮

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function selectInst(ckd) { $("input").atr("checked",ckd) ; }<br><br></script> </head> <body><br><br>王鹏的爱好：<br> <input type="checkbox" value="抽烟" id="inst" name="inst">抽烟<br> <input type="checkbox" value="喝酒" id="inst" name="inst">喝酒<br> <input type="checkbox" value="扣脚" id="inst" name="inst">扣脚<br> <input type="checkbox" value="嫖X" id="inst" name="inst">嫖X<br> <input type="checkbox" value="欺负赵冬" id="inst" name="inst">欺负赵冬<br> <input type="checkbox" value="帮助⼩忽" id="inst" name="inst">帮助⼩忽<br> <input type="checkbox" onclick="selectInst(this.checked)">全选<br><br></body></th>
  </tr>
</table>


</html>

范例：根据样式选择

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function showFun() { $(".init").atr("value"," a") ; }<br><br></script> </head> <body><br><br>⽤户名：<input type="text" name="userid" id="userid" clas="init"><br> 年&nbsp;&nbsp;龄：<input type="text" name="age" id="age"><br> 学&nbsp;&nbsp;校：<input type="text" name="schol" id="schol" clas="init"><br> <input type="buton" value="显示" onclick="showFun()"><br><br></body></th>
  </tr>
</table>


</html>

所以对于基本选择器之中最重要的就是“#ID”、“元素”、“.CLAS”是最容易混淆的。 范例：使⽤“*”表示所有的元素

<table>
  <tr>
    <th><script type="text/javascript"> function showFun() { $("*").atr("value"," a") ; }</th>
  </tr>
</table>


</script>

范例：选择指定的元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div,span").text("<h1> a</h1>") ; }) ;<br><br></script> </head> <body><br><br><div id="msg"></div> <span id="info"></span><br><br></body></th>
  </tr>
</table>


</html>

使⽤“text()”函数主要功能是设置⽂本节点的，设置两个元素之间的内容的。但是使⽤text()函数⽆法设置包含有html代码的⽂本。 如果要想处理元素标记使⽤“html()”函数。

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("div,span").html("<h1> a</h1>") ; }) ;</th>
  </tr>
</table>


</script>

对于在JQuery之中html()与text()的区别？ · 两个函数都可以设置元素之中的显示内容； · text()函数只能够设置普通的⽂本数据，如果有html数据也将转换为⽂本数据； · html()函数如果设置的有html元素，那么就将元素进⾏DOM的处理。

### 3.2.2、⾼级选择器

基本选择器如果说只能够站在⼀个平⾯上选择，那么⾼级选择器就可以进⾏⼦元素的操作。

<table>
  <tr>
    <th>No.</th>
    <th>⾼级选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>祖先元素 后代元素</td>
    <td>根据祖先元素匹配所有的后代元 素（祖先与后代） · 范例：$("div</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>⽗元素 > ⼦元素</td>
    <td>td").atr("clas","suces") ; 根据⽗元素匹配所有的⼦元素 （⽗⼦关系）<br><br>· 范例：</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>previous + next</td>
    <td>$("div>span").text(" a") ; 匹配所有在previous元素之后 的相邻元素，可以使⽤“next()” 表示<br><br>· 范例： $("div+span").text(" a") ; · 范例：</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>previous ~ siblings</td>
    <td>$("div").next().text(" a") ; 匹配previous之后的所有兄弟 元素<br><br>· 范例： $("div~span").text(" a") ; · 范例：</td>
    <td>元素集合</td>
  </tr>
</table>


("div").nextAl().text(" a") ;

范例：取得“祖先元素 后代元素”

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div td").atr("clas","suces") ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><table border="1"> <tr><br><br><td>Oracle - 0</td><br><br><td>Java - 0</td><br><br><td>Android - 0</td> </tr> <tr><br><br><td>Oracle - 1</td> <td>Java - 1</td><br><br><td>Android - 1</td> </tr> <tr><br><br><td>Oracle - 2</td> <td>Java - 2</td><br><br><td>Android - 2</td> </tr> </table><br><br><br><br><br><br><br></div> <table border="1"><br><br><tr> <td>王鹏当⽼师</td> <td>赵冬当⽼师</td> <td>⼩⾊贵当⽼师</td> </tr><br><br></table> </body></th>
  </tr>
</table>


</html>

如果直接使⽤的是“td（$("td").attr("class","success") ;）”，就表示所有的td元素的clas都要进⾏更改。⽽如果使⽤ 了“$("div td").attr("class","success") ;”操作就表示取得所有在div元素之中定义的td元素，定位更加准确。 范例：观察⽗⼦元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div>span").text(" a") ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><span>HELO ONE</span> </div> <span>HELO TWO</span> </body></th>
  </tr>
</table>


</html>

这个时候只有在“div”元素下的⼦元素“span”可以进⾏内容的设置，但是不在“<div>”元素下的内容将⽆法使⽤。 范例：兄弟节点

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div+span").text(" a") ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><span>HELO ONE</span><br> </div> <span>HELO TWO</span><br> <span>HELO THRE</span> </body></th>
  </tr>
  <tr>
    <td></html><br><br><script type="text/javascript"> $(function() { / ⾃动执⾏ $("div").next().text(" a") ; }) ;</td>
  </tr>
</table>


</script>

此时通过“$("div+span")”只能够找到div之后的第⼀个兄弟节点，如果要想找到div之后的所有兄弟节点，那么就需要更改选择

器。 范例：选择所有兄弟节点

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("div~span").text(" a") ; }) ;</th>
  </tr>
  <tr>
    <td></script><br><br><script type="text/javascript"> $(function() { / ⾃动执⾏ $("div").nextAl().text(" a") ; }) ;</td>
  </tr>
</table>


</script>

### 3.2.3、索引选择器

索引选择器有⼀个最⼤的好处是可以根据多个元素的索引进⾏定位。

<table>
  <tr>
    <th>No.</th>
    <th>索引选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>first()、:first</td>
    <td>获取第⼀个元素<br><br>· 范例： $("li:first").atr("clas","suc es") ;；<br><br>· 范例： $("li").first().atr("clas","suc</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>2</td>
    <td>last()、:last</td>
    <td>ces") ; 获取最后⼀个元素<br><br>· 范例： $("li:last").atr("clas","suce<br><br>s") ;； · 范例：<br><br>$("li").last().atr("clas","suc</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:not(selector)</td>
    <td>ces") ; 获取除给定元素之外的所有元素<br><br>· 范例： $("input:not(#uid)").atr("cla</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:even</td>
    <td>s","suces") ;<br><br>表示获取所有索引值为偶数的元 素，索引号从0开始<br><br>· 范例： $("li:even").atr("clas","suc</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>5</td>
    <td>:od</td>
    <td>es") ; 表示获取所有索引值为奇数的元 素，索引号从0开始<br><br>· 范例： $("li:od").atr("clas","suc</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>6</td>
    <td>:eq(index)</td>
    <td>es") ; 取得指定索引号的元素<br><br>· 范例： $("li:eq(1)").atr("clas","suc ces") ;；</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>7</td>
    <td>:gt(index)</td>
    <td>取得所有⼤于指定索引号的元素 · 范例： $("li:gt(2)").atr("clas","suc</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>8</td>
    <td>:lt(index)</td>
    <td>es") ; 取得所有⼩于指定索引号的元素<br><br>· 范例： $("li:lt(2)").atr("clas","suc es") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>9</td>
    <td>:header</td>
    <td>获取所有标题类型的元 素，例如：h1、h2、 .<br><br>· 范例： $(":header").atr("clas</td>
    <td> </td>
  </tr>
</table>


s","suces") ;

范例：取得第⼀个元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("li:first").atr("clas","suces") ; $("li:first").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><ol> <li>今天是星期⼀ <li>今天是星期⼆ <li>今天是星期三 <li>今天是星期四 <li>今天是星期五 <li>今天是星期六 <li>今天是星期七 </ol> </div> </body></th>
  </tr>
  <tr>
    <td></html><br><br><script type="text/javascript"> $(function() { / ⾃动执⾏ $("li").first().atr("clas","suces") ; $("li").first().text("每天的话题就是：今天中午吃什么？") ; ; }) ;</td>
  </tr>
</table>


</script>

范例：验证最后的数据

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("li:last").atr("clas","suces") ; $("li").last().text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><ol> <li>今天是星期⼀ <li>今天是星期⼆ <li>今天是星期三 <li>今天是星期四 <li>今天是星期五 <li>今天是星期六 <li>今天是星期七 </ol> </div> </body></th>
  </tr>
</table>


</html>

范例：排除指定的元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("input:not(#uid)").atr("clas","suces") ; $("input:not(#uid)").val("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <form id="f"><br><br><input type="text" value="HELO ONE" id="uid"> <input type="text" value="HELO TWO" id="name"><br><br></form> </body></th>
  </tr>
</table>


</html>

范例：控制所有偶数的操作

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("li:even").atr("clas","suces") ; $("li:even").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><ol> <li>今天是星期⼀ <li>今天是星期⼆ <li>今天是星期三 <li>今天是星期四 <li>今天是星期五 <li>今天是星期六 <li>今天是星期七 </ol> </div> </body></th>
  </tr>
</table>


</html>

控制奇数只需要修改为“od”即可。

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("li:od").atr("clas","suces") ; $("li:od").text("每天的话题就是：今天中午吃什么？") ; ; }) ;</th>
  </tr>
</table>


</script>

范例：让第⼆个索引选中

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("li:eq(1)").atr("clas","suces") ; $("li:eq(1)").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><ol> <li>今天是星期⼀ <li>今天是星期⼆ <li>今天是星期三 <li>今天是星期四 <li>今天是星期五 <li>今天是星期六 <li>今天是星期七 </ol> </div> </body></th>
  </tr>
</table>


</html>

范例：索引号⼩于2

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("li:lt(2)").atr("clas","suces") ; $("li:lt(2)").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <div id="msg"><br><br><ol> <li>今天是星期⼀ <li>今天是星期⼆ <li>今天是星期三 <li>今天是星期四 <li>今天是星期五 <li>今天是星期六 <li>今天是星期七 </ol> </div> </body></th>
  </tr>
</table>


</html>

索引值⼤于2

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("li:gt(2)").atr("clas","suces") ; $("li:gt(2)").text("每天的话题就是：今天中午吃什么？") ; ; }) ;</th>
  </tr>
</table>


</script>

范例：操作标题信息

<table>
  <tr>
    <th><html> head><br><br><title>JQuery Demo</title> meta charset="UTF-8"><br><br><script type="text/javascript" src="js/jquery-1.10.2.js"></script> link rel="styleshet" type="text/cs" href="cs/form.cs"><br><br><script type="text/javascript"> $(function() {/ ⾃动执⾏ $(":header").atr("clas","suces") ; $(":header").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br>script> /head> body><br><br><div id="msg">helo</div><br><br><h1>helo</h1><br><h2><span>helo</span></h2> body><br></th>
  </tr>
</table>


</html>

### 3.2.4、内容选择器

以上的操作都是针对元素的，但是选择器之中也提供有针对于内容的。

<table>
  <tr>
    <th>No.</th>
    <th>基本选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:contains(⽂本)</td>
    <td>取得包含有指定⽂本内容的元素 · 范例：$("table<br><br>td:contains('Java')").atr("cla</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:empty</td>
    <td>s","suces") ;<br><br>取得不包含⼦元素或者是任何⽂ 本的空元素<br><br>· 范例： $("span:empty").atr("clas",</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:has(selector)</td>
    <td>"suces") ; 获取包含指定元素的所有⽗元素<br><br>· 范例： $("span:has(h1)").atr("clas" ,"suces") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:parent</td>
    <td>取得所有包含⼦元素或者⽂本的 元素<br><br>· 范例： $("h2:parent").atr("clas","s</td>
    <td>元素集合</td>
  </tr>
</table>


uces") ;

范例：判断是否有指定的⽂本

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("table td:contains('Java')").atr("clas","suces") ; $("table td:contains('Java')").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <table border="1"><br><br><tr><br><br><td>AJava - 0</td><br><br><td>AOracle - 0</td><br><br><td> Android - 0</td> </tr> <tr><br><br><td>AJava - 1</td> <td>AOracle - 1</td><br><br><td> Android - 1</td> </tr> <tr><br><br><td>AJava - 2</td> <td>AOracle - 2</td><br><br><td> Android - 2</td> </tr><br><br><br><br><br><br><br></table> </body></th>
  </tr>
</table>


</html>

此时进⾏匹配的是所有每⼀列的数据内容，contains()就相当于模糊的查询操作。 范例：使⽤empty判断

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("span:empty").atr("clas","suces") ; $("span:empty").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <span></span> <span><h1>今天周四了</h1></span> <div>Helo World</div> </body></th>
  </tr>
</table>


</html>

只有第⼀个“<span>”是没有任何⼦元素的，所以只有第⼀个span元素设置了内容。 范例：使⽤has()

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("span:has(h1)").atr("clas","suces") ; $("span:has(h1)").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <span><h2>你好</h2></span> <span><h1>今天周四了</h1></span> <div>Helo World</div> </body></th>
  </tr>
</table>


</html>

范例：取得指定的⽗元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("h2:parent").atr("clas","suces") ; $("h2:parent").text("每天的话题就是：今天中午吃什么？") ; ; }) ;<br><br></script> </head> <body> <span><h2>你好</h2></span> <span><h1>今天周四了</h1></span> <div>Helo World</div> </body></th>
  </tr>
</table>


</html>

### 3.2.5、可⻅性选择器

在进⾏层操作的时候最容易就是⻅到层隐藏，所以在JQuery⾥⾯提供获得隐藏或显示的元素。

<table>
  <tr>
    <th>No.</th>
    <th>可⻅性选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:hi den</td>
    <td>获取所有不可⻅的元素，包含有 层或者是input （type=hi den）<br><br>· 范例： $("div:hi den").show() ; · 范例： $("*:hi den").atr("type","tex</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:visible</td>
    <td>t") ; 获取所有可⻅的元素<br><br>· 范例： $("div:visible").atr("clas","s</td>
    <td>元素集合</td>
  </tr>
</table>


uces") ;

范例：设置层的可⻅性

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div:hi den").atr("clas","suces") ; $("div:hi den").text("每天的话题就是：今天中午吃什么？") ; $("div:hi den").show() ; }) ;<br><br></script> </head> <body> <div style="display: none;">HELO ONE</div> <div style="display: none;">HELO TWO</div> <div style="display: block;">HELO THRE</div> </body></th>
  </tr>
</table>


</html>

范例：定义⼀个隐藏域

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("*:hi den").atr("type","text") ; $("input:hi den").atr("type","text") ; }) ;<br><br></script> </head> <body><br><br><input type="hi den" value=" a"> </body></th>
  </tr>
</table>


</html>

此时是⾸先取得了隐藏域，⽽后⼿⼯进⾏type属性的设置将其修改为了text（⽂本操作）。 范例：取得所有的可⻅元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div:visible").atr("clas","suces") ; $("div:visible").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: none;">HELO ONE</div> <div style="display: block;">HELO TWO</div> <div style="display: block;">HELO THRE</div><br><br></body></th>
  </tr>
</table>


</html>

此时会设置所有的可件元素的样式与⽂本信息。

- 3.2.6、属性选择器


之前都是针对于元素进⾏选择，那么下⾯也可以针对于属性的内容进⾏过滤。

<table>
  <tr>
    <th>No.</th>
    <th>基本选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>[atribute]</td>
    <td>取得包含指定属性的元素 · 范例： $("div[id]:visible").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>[atribute=value]</td>
    <td>","suces") ; 取得指定属性为指定内容的元素<br><br>· 范例：<br><br>$("div[id=msgA]:visible") .atr("clas","suces") ; · 范例：<br><br>$("input[type=text]").atr("cl</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>[atribute!=value]</td>
    <td>as","suces") ; 取得指定属性不是指定内容的元 素<br><br>· 范例： $("div[id!=msgA]").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>[atribute^=value]</td>
    <td>s","suces") ; 取得指定属性以指定内容开始的 元素<br><br>· 范例： $("div[id^=msg]").atr("clas ","suces") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>5</td>
    <td>[atribute$=value]</td>
    <td>取得指定属性以指定内容结束的 元素<br><br>· 范例： $("div[id$=A]").atr("clas","</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>6</td>
    <td>[atribute*=value]</td>
    <td>suces") ; 取得指定属性包含指定内容的元 素<br><br>· 范例： $("div[id*=m]").atr("clas","</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>7</td>
    <td>[atribute,atribute,.]</td>
    <td>suces") ; 包含多个属性的元素<br><br>· 范例：$("div[id=msgA] [title=heloA]")</td>
    <td>元素集合</td>
  </tr>
</table>


.atr("clas","suces") ;

范例：取得具有指定id属性的元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div[id]:visible").atr("clas","suces") ; $("div[id]:visible").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA">HELO ONE</div><br><div style="display: block;" id="msgB">HELO TWO</div> <div style="display: block;">HELO THRE</div><br><br><br></body></th>
  </tr>
</table>


</html>

范例：取得指定属性的元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div[id=msgA]:visible").atr("clas","suces") ; $("div[id=msgA]:visible").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA">HELO ONE</div><br><div style="display: block;" id="msgB">HELO TWO</div> <div style="display: block;">HELO THRE</div><br><br><br></body></th>
  </tr>
</table>


</html>

范例：下⾯设置⽂本输⼊组件的内容

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("input[type=text]").atr("clas","suces") ; $("input[type=text]").val("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><input type="text"> <input type="checkbox"> <input type="radio"><br><br></body></th>
  </tr>
</table>


</html>

范例：取得不是指定内容属性元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div[id!=msgA]").atr("clas","suces") ; $("div[id!=msgA]").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA">HELO ONE</div><br><div style="display: block;" id="msgB">HELO TWO</div> <div style="display: block;">HELO THRE</div><br><br><br></body></th>
  </tr>
</table>


</html>

范例：找到所有id属性之中以“msg”开头的所有元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div[id^=msg]").atr("clas","suces") ; $("div[id^=msg]").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA">HELO ONE</div><br><div style="display: block;" id="msgB">HELO TWO</div> <div style="display: block;">HELO THRE</div><br><br><br></body></th>
  </tr>
</table>


</html>

范例：以指定名称结束

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div[id$=A]").atr("clas","suces") ; $("div[id$=A]").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA">HELO ONE</div><br><div style="display: block;" id="msgB">HELO TWO</div> <div style="display: block;">HELO THRE</div><br><br><br></body></th>
  </tr>
</table>


</html>

范例：找到所有id属性之中包含有m

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div[id*=m]").atr("clas","suces") ; $("div[id*=m]").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA">HELO ONE</div><br><div style="display: block;" id="msgB">HELO TWO</div> <div style="display: block;" id="a">HELO THRE</div><br><br><br></body></th>
  </tr>
</table>


</html>

范例：设置两个属性

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("div[id=msgA][title=heloA]").atr("clas","suces") ; $("div[id=msgA][title=heloA]").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA" title="heloA">HELO ONE</div><br><div style="display: block;" id="msgB" title="heloB">HELO TWO</div> <div style="display: block;" id="a">HELO THRE</div><br><br><br></body></th>
  </tr>
</table>


</html>

### 3.2.7、⼦元素选择器

⽗元素下可以有多个⼦元素，那么针对于这多个⼦元素进⾏选择。

<table>
  <tr>
    <th>No.</th>
    <th>基本选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:nth-child (eq|od|even|index)</td>
    <td>获取每个⽗元素下特定位置的元 素，所以从1开始<br><br>· 范例：$("li:nthchild(2)").atr("clas","suce s") ;<br><br>· 范例：$("li:nthchild(od)").atr("clas","suc ces") ;<br><br>· 范例：$("li:nthchild(even)").atr("clas","su</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:first-child</td>
    <td>ces") ;<br><br>获取每个⽗元素下的第⼀个⼦元 素<br><br>· 范例：$("li:firstchild").atr("clas","suces" ) ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:last-child</td>
    <td>获取每个⽗元素下的最后⼀个⼦ 元素<br><br>· 范例：$("li:lastchild").atr("clas","suces"</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:only-child</td>
    <td>) ; 获取只有⼀个⼦元素的元素<br><br>· 范例：$("li:onlychild").atr("clas","suces"</td>
    <td>元素集合</td>
  </tr>
</table>


) ;

范例：取得指定位置的元素

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("li:nth-child(2)").atr("clas","suces") ; $("li:nth-child(2)").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA" title="heloA"> <ul><br><br><li>星期1</li><br><li>星期2</li><br><li>星期3</li><br><li>星期4</li><br><li>星期5</li><br><li>星期6</li> <li>星期0</li> </ul> </div><br><br><br></body></th>
  </tr>
</table>


</html>

范例：设置奇数索引

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("li:nth-child(od)").atr("clas","suces") ; $("li:nth-child(od)").text("每天的话题就是：今天中午吃什么？") ; }) ;</th>
  </tr>
</table>


</script>

范例：设置偶数索引

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("li:nth-child(even)").atr("clas","suces") ; $("li:nth-child(even)").text("每天的话题就是：今天中午吃什么？") ; }) ;</th>
  </tr>
</table>


</script>

范例：取得第⼀个⼦元素

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("li:first-child").atr("clas","suces") ; $("li:first-child").text("每天的话题就是：今天中午吃什么？") ; }) ;</th>
  </tr>
</table>


</script>

范例：取得最后⼀个⼦元素

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("li:last-child").atr("clas","suces") ; $("li:last-child").text("每天的话题就是：今天中午吃什么？") ; }) ;</th>
  </tr>
</table>


</script>

范例：设置只有⼀个⼦元素的样式

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("li:only-child").atr("clas","suces") ; $("li:only-child").text("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA" title="heloA"> <ul> <li>星期1</li> </ul> </div> </body></th>
  </tr>
</table>


</html>

### 3.2.8、表单属性选择器

选择器⾥⾯也可以针对于表单之中的属性进⾏选择，常⻅的选择操作如下。

<table>
  <tr>
    <th>No.</th>
    <th>表单属性选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:enabled</td>
    <td>所有可以使⽤的表单元素 · 范例： $("input:enabled").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:disabled</td>
    <td>s","suces") ; 所有不可⽤的表单元素<br><br>· 范例： $("input:disabled").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:checked</td>
    <td>s","suces") ; 获取所有被选中的元素<br><br>· 范例： $("input[type=radio]:checke</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:selected</td>
    <td>d").val() 获取所有被选中的option元素<br><br>· 范例：$("select</td>
    <td>元素集合</td>
  </tr>
</table>


option:selected").val()

范例：操作所有可⽤表单

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ $("input:enabled").atr("clas","suces") ; $("input:enabled").val("每天的话题就是：今天中午吃什么？") ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA" title="heloA"> ⽤户名：<input type="text" disabled><br> 密码：<input type="text" disabled><br> 年龄：<input type="text"><br> </div> </body></th>
  </tr>
</table>


</html>

范例：取得所有不可⽤表单

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { / ⾃动执⾏ $("input:disabled").atr("clas","suces") ; $("input:disabled").val("每天的话题就是：今天中午吃什么？") ; }) ;</th>
  </tr>
</table>


</script>

范例：设置被选中 —— radio和checked

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ alert($("input[type=radio]:checked").val() ; alert($("input[type=checkbox]:checked").val() ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA" title="heloA"> 性别：<input type="radio" checked value="男">男<input type="radio" value="⼥">⼥<br> 兴趣：<input type="checkbox" checked value="抽烟">抽烟<input type="checkbox" checked value="喝酒">喝酒 </div> </body></th>
  </tr>
</table>


</html>

范例：下拉列表选中

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / ⾃动执⾏ alert($("select option:selected").val() ; }) ;<br><br></script> </head> <body><br><br><div style="display: block;" id="msgA" title="heloA"> 城市： <select> <option value="beijing" selected>北京</option> <option value="tianjin">天津</option> </select> </div> </body></th>
  </tr>
</table>


</html>

以上就是所有给出的基本选择器的使⽤，实际上发现都是针对于元素的取得。

## 3.3、DOM操作（重点）

在之前的选择器实际上是属于DOM取得的过程，但是JQuery之中也很好的⽀持了DOM的相关操作。

- 3.3.1、⻚⾯的属性操作


属性是每⼀个元素之中都会多少的定义的，⽽在JQuery⾥⾯可以轻松的进⾏各种属性操作。

- 1、 设置属性 如果要进⾏属性的设置使⽤：“atr(属性名称，属性内容)”，或者对于属性内容也可以通过⼀个函数的⽅式返回。 如果单独使⽤“atr(属性)”的话表示的是取得指定属性的内容。


操作形式： · 设置单个属性：$("#zdpic").attr("src","images/b.jpg") ; · 设置多个属性：$("#zdpic").attr({"src":"images/b.jpg","width":"500","height":"500"}) ;

范例：操作属性

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function setImage() { $("#zdpic").atr({"src":"images/b.jpg","width":"50","height":"50"}) ; $("#note").atr("value",function() { return "A⼗⼤不靠谱⻘年 ⸺ 赵冬的故事" ; }) ; }<br><br></script> </head> <body> <div id="content"><br><br><img id="zdpic" src="images/a.jpg" width="20" height="20"> <input type="text" id="note"><br><br></div> <div id="but"><br><br><input type="buton" value="设置图⽚" onclick="setImage()"> </div> </body></th>
  </tr>
</table>


</html>

- 2、 删除属性 如果要进⾏属性的删除操作使⽤“removeAtr(属性名称)”即可。

范例：删除属性

- 3、 ⽂字的操作 · 取得或设置html元素：html()、html(val)； · 取得或设置⽂本数据：text()、text(val)； 但是以上的操作都只是适合于⽂字显示，⽽如果针对于⽂本元素那么就不适合了，要使⽤val(val)语法完成。


<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function setImage() { $("#zdpic").removeAtr("width") ; $("#zdpic").removeAtr("height") ; $("#zdpic").removeAtr("src") ; }<br><br></script> </head> <body> <div id="content"><br><br><img id="zdpic" src="images/a.jpg" width="20" height="20"> </div> <div id="but"><br><br><input type="buton" value="设置图⽚" onclick="setImage()"> </div> </body></th>
  </tr>
</table>


</html>

范例：设置和取得内容

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function(){ $("div>input").val($("div>input").val() + " - A") ; }) ;<br><br></script> </head> <body> <div id="but"><br><br><input type="buton" value="设置图⽚"> </div> </body></th>
  </tr>
</table>


</html>

以上的⼏个属性操作完成之后，针对于cs也可以进⾏配置，有如下的⼏个配置函数： · 设置⼀个单独的cs样式：cs(name , value)； · 增加已经存在的cs样式：adClas(样式 , …)； · 删除样式：removeClas(样式 , …)。

范例：操作样式

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function setCls() { $("input[type=text]").adClas("suces") ; } function delCls() { $("input[type=text]").removeClas("suces") ; }<br><br></script> </head> <body><br><br><input type="text" value="A"><br> <input type="buton" value="设置样式" onclick="setCls()"> <input type="buton" value="删除样式" onclick="delCls()"><br><br></body></th>
  </tr>
</table>


</html>

如果使⽤cs()函数可以编写⼀个样式。

<table>
  <tr>
    <th><script type="text/javascript"> function setCls() { $("span").cs("background","# 0") ; }</th>
  </tr>
</table>


</script>

这些内容虽然是由美⼯提供的，但是开发⼈员应该会⾃⼰切换。

### 3.3.2、⻚⾯的元素操作

⻚⾯元素的动态效果是最为常⻅的，那么下⾯将通过⼀系列的JQuery提供的函数完成。

- 1、 元素追加 · apend(content)：表示在指定的元素之后进⾏元素的追加；


<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { $("div").apend("<h1>HELO WORLD</h1>") ; }) ;<br><br></script> </head> <body><br><br><div>世界，你好！</div> </body></th>
  </tr>
</table>


</html>

在对应的函数之中还有⼀个apendTo()表示的是将⼀个指定的元素加⼊到指定的元素之后。

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { $("input[type=buton]").apendTo($("div") ; }) ;<br><br></script> </head> <body><br><br><div>世界，你好！</div> <span>A，你好</span> <input type="buton" value="Push Me"><br><br></body></th>
  </tr>
</table>


</html>

· prepend(content)：将内容向前追加

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { $("div").prepend("<h1>Helo World</h1>") ; }) ;<br><br></script> </head> <body><br><br><div>世界，你好！</div> <span>A，你好</span> <input type="buton" value="Push Me"><br><br></body></th>
  </tr>
</table>


</html>

同样也提供有⼀个prependTo()函数，表示将⼀个元素加到指定的元素之前。

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { $("input[type=buton]").prependTo($("div") ; }) ;</th>
  </tr>
</table>


</script>

- 2、 元素上追加内容 · after(content)：在所选元素之后追加内容； · before(content)：在所选元素之前追加内容；


范例：设置追加元素

<table>
  <tr>
    <th><script type="text/javascript"> $(function() { $("span").after("<h1>HELO WORLD</h1>") ; }) ;</th>
  </tr>
  <tr>
    <td></script><br><br><script type="text/javascript"> $(function() { $("span").before("<h1>HELO WORLD</h1>") ; }) ;</td>
  </tr>
</table>


</script>

以上的操作只是另外⼀种追加的形式。与之前的区别在于⼀个是弟弟元素的添加，另外⼀个是⼦元素的增加，使⽤最多的⼀定是⼦ 元素追加，于是下⾯完成⼀个简单的顺序列表操作。 范例：实现顺序列表

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { var data = new Array("王鹏","赵冬","占⽟⻁") ; var ulElement = $("<ul></ul>") ; / 创建节点 for (var x = 0 ; x < data.length ; x +) { ulElement.apend($("<li>" + data[x] + "</li>") ; } $("div").apend("<h1>每天折磨⽼师的⼈</h1>") ; $("div").apend(ulElement) ; }) ;<br><br></script> </head> <body><br><br><div></div> </body></th>
  </tr>
</table>


</html>

范例：实现下拉列表

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function setName() { var data = new Array("王鹏","赵冬","占⽟⻁") ;<br><br>/ 删除select元素之中option⼦元素索引⼤于0的所有内容 $("select>option:gt(0)").remove() ; for(var x = 0 ; x < data.length ; x +) { $("#name").apend("<option value=\" + x + "\">" + data[x] + "</option>") ; } }<br><br></script> </head> <body> <div> <select id="name"> <option value="> = 请选择⼗⼤候补讨厌⼈群 =</option> </select> </div> <input type="buton" value="填充数据" onclick="setName()"><br><br></body></th>
  </tr>
</table>


</html>

范例：创建表格

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function setName() { var data = new Array("王鹏","赵冬","占⽟⻁") ;<br><br>/ 删除select元素之中option⼦元素索引⼤于0的所有内容 $("select>option:gt(0)").remove() ; for(var x = 0 ; x < data.length ; x +) { $("#name").apend("<option value=\" + x + "\">" + data[x] + "</option>") ; } } function insertRow() { var tr = $("<tr></tr>") ; / 创建tr元素 tr.apend("<td>" + $("#name").val() + "</td>") ; tr.apend("<td>" + $("#age").val() + "</td>") ; tr.apend("<td><input type=\"buton\" value=\"删除\" onclick=\"deleteRow(this)\"></td>") ; $("#mytab").apend(tr) ; resetData() ; } function resetData() { / 清空数据 $("input[type=text]").val(") ; } function deleteRow(ctr) { $(ctr.parentNode.parentNode).remove() ; }<br><br></script> </head> <body><br><br><div id="inputDataDiv"> 姓名：<input type="text" id="name" name="name"><br> 年龄：<input type="text" id="age" name="age"><br> <input type="buton" value="增加" onclick="insertRow()"> <input type="buton" value="重置" onclick="resetData()"> </div> <div id="dataShowDiv"> <table border="1" id="mytab" width="80%"> <tr> <td>姓名</td> <td>年龄</td> <td>操作</td> </tr> <tr> <td>赵冬</td> <td>38</td> <td><input type="buton" value="删除" onclick="deleteRow(this)"></td> </tr> </table> </div> </body></th>
  </tr>
</table>


</html>

实际上以上增加表格⾏的代码还可以继续简化。

<table>
  <tr>
    <th>function insertRow() { $("#mytab").apend("<tr></tr>") ; $("#mytab tr:last").apend("<td>" + $("#name").val() + "</td>"<br><br>+ "<td>" + $("#age").val() + "</td>"<br><br>+ "<td><input type=\"buton\" value=\"删除\" onclick=\"deleteRow(this)\"></td>") ;</th>
  </tr>
</table>


}

⾄少此时对于DOM的操作应该算是简单了。

- 3、 复制元素：


· clone()，将⼀个元素进⾏完整的复制操作

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function showHelo() { alert("Helo World .") ; } function cloneElement() { $("div").after($("#myDiv").clone() ; }<br><br></script> </head> <body><br><br><div id="myDiv" onclick="showHelo()">点我</div> <input type="buton" value="复制元素" onclick="cloneElement()"><br><br></body></th>
  </tr>
</table>


</html>

- 4、 包裹标签 原本某⼀个元素之内已经存在了某些元素，但是现在希望可以将其内容再使⽤某些元素包裹起来。

· wrap(元素)：使⽤特定的元素进⾏内容的包裹； 范例：实现元素包裹

- 5、 删除元素 从某⼀特定的元素上删除内容：remove()。


<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function setWrap() { $("div").wrap("<h1></h1>") ; }<br><br></script> </head> <body><br><br><div id="myDiv">helo world</div> <div id="myDiv">helo world</div> <div id="myDiv">helo world</div> <input type="buton" value="复制元素" onclick="setWrap()"><br><br></body></th>
  </tr>
</table>


</html>

范例：观察元素删除

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function deleteAl() { $("tr:gt(0)").remove() ; }<br><br></script> </head> <body><br><br><table border="1" width="70%"> <tr><br><br><td>姓名</td> <td>年龄</td> </tr> <tr> <td>赵冬</td> <td>16</td> </tr> <tr> <td>王鹏</td> <td>18</td> </tr> <tr> <td>占⽟⻁</td> <td>26</td> </tr> </table> <input type="buton" value="删除所有⾏" onclick="deleteAl()"><br><br></body></th>
  </tr>
</table>


</html>

- 6、 清空元素 · empty()可以将所选择元素的所有后代元素消灭掉。
- 7、 each()函数 在JQuery之中，可以使⽤each()函数实现⼀个循环操作，例如：在取得的是⼀组数据的时候。


<table>
  <tr>
    <th><script type="text/javascript"> function deleteAl() { $("table").empty() ; }</th>
  </tr>
</table>


</script>

范例：观察each()函数

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { $("div").each(function(ind){ / ind是元素的索引<br><br>/ this就表示当前的元素对象 $(this).text($(this).text() + $(this).atr("title") ; }) }) ;<br><br></script> </head> <body><br><br><div title="WP">第⼀捣乱分⼦：</div> <div title="ZD">第⼆捣乱分⼦：</div> <div title="ZYH">第三捣乱分⼦：</div><br><br></body></th>
  </tr>
</table>


</html>

如果返回的是元素的集合操作，不想进⾏统⼀的处理⽅式，⽽想进⾏依次迭代操作就可以通过each()函数完成。

## 3.4、⻚⾯动态效果（重点）

所谓的⻚⾯动态效果是可以动态的进⾏事件的操作绑定。在之前使⽤过⼀个ready()就属于⼀个简单的载⼊事件

- 1、 ready()函数 使⽤ready()就表示⼀个动态的数据载⼊事件，但是对于ready()功能的编写在JQuery之中提供了四种⽅案。

· 写法⼀：$(function(){…})

· 写法⼆：明确调⽤ready()

· 写法三：利⽤jQuery()代替$

· 写法四：利⽤jQuery()代替$

- 2、 bind()操作 bind()属于事件的动态绑定操作，下⾯⾸先通过⼀个简单的程序来观察。


<table>
  <tr>
    <th>$(function() { $("div").each(function(ind){ / ind是元素的索引<br><br>/ this就表示当前的元素对象 $(this).text($(this).text() + $(this).atr("title") ; })</th>
  </tr>
</table>


}) ;

<table>
  <tr>
    <th>$(document).ready(function() { $("div").each(function(ind){ / ind是元素的索引<br><br>/ this就表示当前的元素对象 $(this).text($(this).text() + $(this).atr("title") ; })</th>
  </tr>
</table>


}) ;

<table>
  <tr>
    <th>jQuery(document).ready(function() { alert("helo") ;</th>
  </tr>
</table>


}) ;

<table>
  <tr>
    <th>jQuery(function() { alert("helo") ;</th>
  </tr>
</table>


}) ;

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function(){ $("input[type=buton]").bind("click",function(){ alert("Helo World .") ; }) ; }) ;<br><br></script> </head> <body><br><br><input type="buton" value="Push Me"> </body></th>
  </tr>
</table>


</html>

如果现在觉得⽤bind()函数进⾏单击事件处理⽐较麻烦，那么可以直接使⽤click完成。

<table>
  <tr>
    <th><script type="text/javascript"> $(function(){ $("input[type=buton]").click(function(){ alert("Helo World .") ; }) ; }) ;</th>
  </tr>
</table>


</script>

以上只是针对于⼀个组件设置了单击事件，那么也可以针对于多个组件⼀起进⾏事件设置。

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function(){ $("input[type=buton],div,body").click(function(){ alert("Helo World .") ; }) ; }) ;<br><br></script> </head> <body><br><br><div>按我</div> <input type="buton" value="Push Me"><br><br></body></th>
  </tr>
</table>


</html>

此时针对于多个组件进⾏了内容的设置。但是此时如果有⼀个简单的计数操作，会发现如果调⽤的是按钮的执⾏事件，那么会计数 两次，这个实际上就属于JQuery之中⽐较麻烦的冒泡事件。

但如果要想阻⽌冒泡事件可以增加⼀⾏语句。

<table>
  <tr>
    <th><script type="text/javascript"> $(function(){ var count = 0 ; / 做⼀个统计数据 $("input[type=buton],#but,body").click(function(){ count + ; $("#showDiv").text("点击次数：" + count) ; event.stopPropagation() ; / 阻⽌冒泡事件<br><br>}) ; }) ;</th>
  </tr>
</table>


</script>

- 3、 unbind()操作 既然现在可以绑定事件，那么也可以取消事件的绑定操作。


· unbind(type , [function])。 范例：观察事件的绑定与取消

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function bindFun() { / 执⾏事件绑定操作 $("#showDiv").bind("click",function() { showHelo() ; }) ; } function unbindFun() { $("#showDiv").unbind("click") ; } function showHelo() { alert("Helo World .") ; }<br><br></script> </head> <body><br><br><div id="showDiv">操作事件</div> <input type="buton" value="绑定事件" onclick="bindFun()"> <input type="buton" value="取消绑定事件" onclick="unbindFun()"><br><br></body></th>
  </tr>
</table>


</html>

对于绑定事件⽽⾔，在之前只是简单的绑定了单击事件，实际上还有各种事件都可以进⾏绑定。 范例：绑定其它事件

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function bindFun() { / 执⾏事件绑定操作 $("input[type=text]").bind({ click : function() { $("div").text("绑定单击事件") ; } , focus : function() { $("div").text("绑定焦点事件") ; } , change : function() { $("div").text("绑定修改事件") ; } }) ; }<br><br></script> </head> <body><br><br><div></div> <input type="text" value=" a"> <input type="buton" value="绑定事件" onclick="bindFun()"><br><br></body></th>
  </tr>
</table>


</html>

这个时候如果采⽤这种绑定事件的⽅式是很好的解决了DOM操作之中⽆法设置按钮事件的缺陷。

- 4、 元素切换操作 · hover()此函数的主要功能是在⿏标操作过程之中，⾃动执⾏切换样式的功能。
- 5、 设置⼀次执⾏事件 如果使⽤之前的bind()函数发现事件会处理多次，那么如果说现在只希望某⼀事件处理⼀次，⼀次之后就失效了，就可以利⽤

“one()”函数完成。 范例：设置⼀次事件

- 6、 设置触发事件 · tri ger(type ,事件处理)，设置某⼀事件⽽后发⽣的改变


<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { $("#title").hover( function() { / ⿏标经过 $("#content").show(20) ; },function() { / ⿏标离开 $("#content").hide(20) ; }) ; }) ;<br><br></script> </head> <body><br><br><div id="title">赵冬的保证书</div> <div id="content" style="display:none;"> <img src="images/b.jpg" width="30" height="30"> </div> </body></th>
  </tr>
</table>


</html>

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / 执⾏事件绑定操作 $("input").one("click",function(){ alert("Helo World .") ; }) ; }) ;<br><br></script> </head> <body><br><br><input type="buton" value="绑定事件" id="msg"> </body></th>
  </tr>
</table>


</html>

范例：设置触发事件

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / 执⾏事件绑定操作 $("input").tri ger("select") ; / ⾃动执⾏select() $("input").bind("click",function() { alert("触发单击事件。") ;<br><br>}) ; }) ;<br><br></script> </head> <body><br><br><input type="text" value=" a"> </body></th>
  </tr>
</table>


</html>

本程序在执⾏的时候会默认执⾏选择全部的操作。

## 3.5、动画与特效（理解）

- 1、 在之前学习过两个函数已经可以简单的实现效果： · show()：显示的，可以设置⼀个参数作为显示的时间，相当于层的显示； · hide()：隐藏的，设置⼀个参数作为隐藏的时间，相当于层的隐藏；
- 2、 滑动动画效果 · slideDown()：向下放⼤⾼度； · slideUp()：向下缩⼩⾼度；


范例：设置滑动操作

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / 执⾏事件绑定操作 $("input[value=slideUp]").bind("click", function() { $("img").slideUp(3 0) ; }) ; $("input[value=slideDown]").bind("click",function(){ $("img").slideDown(3 0) ; }) ; }) ;<br><br></script> </head> <body><br><br><input type="buton" value="slideUp"> <input type="buton" value="slideDown"> <div><img src="images/a.jpg"></div><br><br></body></th>
  </tr>
</table>


</html>

以上的操作实际上只是在进⾏⼀个⾼度的拉伸，但是在很多时候往往希望可以⾃动的进⾏拉伸。

· slideTogle()函数，可以实现⾃动的拉伸切换。 范例：拉伸切换

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / 执⾏事件绑定操作 $("input[value=slide]").bind("click",function(){ $("img").slideTogle(3 0) ; }) ; }) ;<br><br></script> </head> <body><br><br><input type="buton" value="slide"> <div><img src="images/a.jpg"></div><br><br></body></th>
  </tr>
</table>


</html>

实际上slideTogle()函数就可以实现slideUp()和slideDown()两个函数的功能。⽽且它可以⾃动的进⾏判断。

- 3、 淡⼊、淡出效果 · fadeIn()：淡⼊操作； · fadeOut()：淡出操作。


<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / 执⾏事件绑定操作 $("input[value=fadeIn]").bind("click",function(){ $("img").fadeIn(3 0) ; }) ; $("input[value=fadeOut]").bind("click",function(){ $("img").fadeOut(3 0) ; }) ; }) ;<br><br></script> </head> <body><br><br><input type="buton" value="fadeIn"> <input type="buton" value="fadeOut"> <div><img src="images/a.jpg" width="80" height="60"></div><br><br></body></th>
  </tr>
</table>


</html>

现在也可以根据⼀个按钮实现这种淡⼊淡出的操作。 · fadeTo()函数。

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>$(function() { / 执⾏事件绑定操作 $("select[id=fadeVal]").bind("change",function(){ $("img").fadeTo(3 0,$(this).val() ; }) ; }) ;<br><br></script> </head> <body><br><br><select id="fadeVal"> <option value="0.2">0.2</option> <option value="0.4">0.4</option> <option value="0.6">0.6</option><br><br><option value="0.8">0.8</option><br><option value="1.0">1.0</option> </select> <div><img src="images/a.jpg" width="80" height="60"></div><br><br><br></body></th>
  </tr>
</table>


</html>

⼀般这样的事情都是留给美⼯去做的，我们就是⼀个选择器、DOM操作。

## 3.6、AJAX动态操作（核⼼）

AJAX操作不⽤说很⼤的代码都是重复的，所以这⼀操作在JQuery之中就被彻底的解决掉了，在JQuery⾥⾯提供两个简单的AJAX 操作⽅法：

· POST提交：$.post(请求地址，传递参数，回调函数，返回的数据类型)： · GET提交：$.get(请求地址，传递参数，回调函数，返回的数据类型)：

那么对于传递参数必须以JSON格式传递，使⽤“{}”声明，⾥⾯包含有许多的“key:value”的数据结构；⽽对于返回的数据类型主要 就三类：text、json、xml。

- 3.6.1、返回⽂本数据


为了简单的演示AJAX操作，那么下⾯⾸先定义⼀个JSP⻚⾯：get_text.jsp。

<table>
  <tr>
    <th><%@ page pageEncoding="UTF-8" %></th>
  </tr>
</table>


${param.uid='a'?true:false}

本程序只是使⽤了⼀个简单的EL进⾏参数内容的判断。 范例：前台将使⽤JQuery之中⽀持的AJAX进⾏操作

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function checkUid(u) { if(u !=") { $.post("get_text.jsp",{uid:u},function(obj) { if(obj.trim() = "false") { $("#uid").adClas("suces") ; } else { $("#uid").adClas("fail") ; } },"text") ; } }<br><br></script> </head> <body><br><br><input type="text" name="uid" id="uid" clas="init" onblur="checkUid(this.value)"> <input type="submit" value="注册"><br><br></body></th>
  </tr>
</table>


</html>

因为现在返回的数据之中包含有空格数据，所以必须⾸先使⽤“trim()”进⾏处理。

### 3.6.2、返回JSON数据

下⾯利⽤JSON返回点城市数据，并且将这些数据添加到select下拉列表框之中。 范例：定义get_json.jsp⻚⾯

<table>
  <tr>
    <th><%@ page pageEncoding="UTF-8" %> {"alCitys":[{"id":10,"title":"北京"}, {"id":20,"title":"上海"},</th>
  </tr>
</table>


{"id":30,"title":"⼴州"}]}

范例：将json数据添加到下拉列表框

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function getCity() { $("#city option:gt(0)").remove() ; $.post("get_json.jsp",{},function(obj) { var alCitys = obj.alCitys ; for (var x = 0 ; x < alCitys.length ; x +) { $("#city").apend("<option value=\"+alCitys[x].id+"\">"+alCitys[x].title+"</option>") ; } },"json") ; }<br><br></script> </head> <body><br><br><select id="city"> <option value="0"> = 请选择所在城市 =</option> </select> <input type="buton" value="读取城市信息" onclick="getCity()"><br><br></body></th>
  </tr>
</table>


</html>

在⼯作之中使⽤最多的就是以上两类。

- 3.6.3、返回XML数据


范例：定义返回xml的JSP⻚⾯

<table>
  <tr>
    <th><%@ page pageEncoding="UTF-8" %> <%@ page contentType="text/xml" %> <alCitys><br><br><city> <id>10</id> <title>北京</title> </city> <city> <id>20</id> <title>上海</title> </city> <city> <id>30</id> <title>⼴州</title> </city></th>
  </tr>
</table>


</alCitys>

范例：在JSP之中解析xml⽂档

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <script type="text/javascript"><br><br>function getCity() { $("#city option:gt(0)").remove() ; $.post("get_xml.jsp",{},function(obj) { var citys = obj.getElementsByTagName("city") ; for (var x = 0 ; x < citys.length ; x +) { var id = citys[x].getElementsByTagName("id")[0].firstChild.nodeValue; var txt = citys[x].getElementsByTagName("title")[0].firstChild.nodeValue; ; $("#city").apend("<option value=\"+id+"\">"+txt+"</option>") ; } },"xml") ; }<br><br></script> </head> <body><br><br><select id="city"> <option value="0"> = 请选择所在城市 =</option> </select> <input type="buton" value="读取城市信息" onclick="getCity()"><br><br></body></th>
  </tr>
</table>


</html>

DOM解析⾮常的重要，不得不会，虽然现在的开发使⽤它很少了，但是依然很重要。

## 3.7、JQuery验证框架

⻓期以来最受折磨的是验证，但是有了JQuery验证框架之中，验证不再是头疼问题了，⽽是⼀种简单的享受。 范例：基础表单

<table>
  <tr>
    <th><html> <head> <title>JQuery Demo</title> <meta charset="UTF-8"> <script type="text/javascript" src="js/jquery-1.10.2.js"></script> <script src="js/jquery.validationEngine-en.js" type="text/javascript"></script> <script src="js/jquery.validationEngine.js" type="text/javascript"></script> <link rel="styleshet" type="text/cs" href="cs/form.cs"> <link rel="styleshet" href="cs/validationEngine.jquery.cs" type="text/cs"> <link rel="styleshet" href="cs/template.cs" type="text/cs"> <script type="text/javascript"><br><br>$(function(){ $("#myform").validationEngine(); }); </script><br><br></head> <body> <form action=" id="myform" name="myform"> </form> </body></th>
  </tr>
</table>


</html>

所有需要验证的标记的表单都通过clas标记。

<table>
  <tr>
    <th><td><input type="text" name="userid" id="userid"</th>
  </tr>
</table>


clas="validate[required] text-input" ></td>

# 4、总结

- 1、 JQuery是⼀个⾮常强⼤的前台技术框架，适合于各类⼈群使⽤，包括开发者、美⼯设计⼈员。
- 2、 JQuery之中的选择器好好熟悉⼀下；


- 1、 基本选择器：
- 2、 ⾼级选择器


<table>
  <tr>
    <th>No.</th>
    <th>基本选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>#id</td>
    <td>根据指定的元素id取得数据，就 相当于 “document.getElementById() ”函数<br><br>· 范例： $("#msg").atr("title")； · 范例：$("#msg").val()。</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>2</td>
    <td>element</td>
    <td>根据指定的元素取得数据，例 如：⼀组“<input>”<br><br>· 范例： $("input").atr("value"," a") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>.clas</td>
    <td>根据指定的cs取得元素数据 · 范例：</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>*</td>
    <td>$(".init").atr("value"," a") ; 取得全部的元素<br><br>· 范例： $("*").atr("value"," a") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>5</td>
    <td>元素,元素,…</td>
    <td>取得指定个元素的对象 · 范例：$("div,span").text("</td>
    <td>元素集合</td>
  </tr>
</table>


<h1> a</h1>") ;

<table>
  <tr>
    <th>No.</th>
    <th>⾼级选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>祖先元素 后代元素</td>
    <td>根据祖先元素匹配所有的后代元 素（祖先与后代） · 范例：$("div</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>⽗元素 > ⼦元素</td>
    <td>td").atr("clas","suces") ; 根据⽗元素匹配所有的⼦元素 （⽗⼦关系）<br><br>· 范例：</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>previous + next</td>
    <td>$("div>span").text(" a") ; 匹配所有在previous元素之后 的相邻元素，可以使⽤“next()” 表示<br><br>· 范例： $("div+span").text(" a") ; · 范例：</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>previous ~ siblings</td>
    <td>$("div").next().text(" a") ; 匹配previous之后的所有兄弟 元素<br><br>· 范例： $("div~span").text(" a") ; · 范例：</td>
    <td>元素集合</td>
  </tr>
</table>


("div").nextAl().text(" a") ;

- 3、 索引选择器


<table>
  <tr>
    <th>No.</th>
    <th>索引选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>first()、:first</td>
    <td>获取第⼀个元素<br><br>· 范例： $("li:first").atr("clas","suc es") ;；<br><br>· 范例： $("li").first().atr("clas","suc</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>2</td>
    <td>last()、:last</td>
    <td>ces") ; 获取最后⼀个元素<br><br>· 范例： $("li:last").atr("clas","suce<br><br>s") ;； · 范例：<br><br>$("li").last().atr("clas","suc</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:not(selector)</td>
    <td>ces") ; 获取除给定元素之外的所有元素<br><br>· 范例： $("input:not(#uid)").atr("cla</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:even</td>
    <td>s","suces") ;<br><br>表示获取所有索引值为偶数的元 素，索引号从0开始<br><br>· 范例： $("li:even").atr("clas","suc</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>5</td>
    <td>:od</td>
    <td>es") ; 表示获取所有索引值为奇数的元 素，索引号从0开始<br><br>· 范例： $("li:od").atr("clas","suc</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>6</td>
    <td>:eq(index)</td>
    <td>es") ; 取得指定索引号的元素<br><br>· 范例： $("li:eq(1)").atr("clas","suc ces") ;；</td>
    <td>单个元素</td>
  </tr>
  <tr>
    <td>7</td>
    <td>:gt(index)</td>
    <td>取得所有⼤于指定索引号的元素 · 范例： $("li:gt(2)").atr("clas","suc</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>8</td>
    <td>:lt(index)</td>
    <td>es") ; 取得所有⼩于指定索引号的元素<br><br>· 范例： $("li:lt(2)").atr("clas","suc es") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>9</td>
    <td>:header</td>
    <td>获取所有标题类型的元 素，例如：h1、h2、 .<br><br>· 范例： $(":header").atr("clas</td>
    <td> </td>
  </tr>
</table>


s","suces") ;

- 4、 内容选择器


<table>
  <tr>
    <th>No.</th>
    <th>内容选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:contains(⽂本)</td>
    <td>取得包含有指定⽂本内容的元素 · 范例：$("table<br><br>td:contains('Java')").atr("cla</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:empty</td>
    <td>s","suces") ;<br><br>取得不包含⼦元素或者是任何⽂ 本的空元素<br><br>· 范例： $("span:empty").atr("clas",</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:has(selector)</td>
    <td>"suces") ; 获取包含指定元素的所有⽗元素<br><br>· 范例： $("span:has(h1)").atr("clas" ,"suces") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:parent</td>
    <td>取得所有包含⼦元素或者⽂本的 元素<br><br>· 范例： $("h2:parent").atr("clas","s</td>
    <td>元素集合</td>
  </tr>
</table>


uces") ;

- 5、 可⻅性选择器
- 6、 属性选择器


<table>
  <tr>
    <th>No.</th>
    <th>可⻅性选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:hi den</td>
    <td>获取所有不可⻅的元素，包含有 层或者是input （type=hi den） · 范例： $("div:hi den").show() ; · 范例： $("*:hi den").atr("type","tex</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:visible</td>
    <td>t") ; 获取所有可⻅的元素<br><br>· 范例： $("div:visible").atr("clas","s</td>
    <td>元素集合</td>
  </tr>
</table>


uces") ;

<table>
  <tr>
    <th>No.</th>
    <th>属性选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>[atribute]</td>
    <td>取得包含指定属性的元素 · 范例： $("div[id]:visible").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>[atribute=value]</td>
    <td>","suces") ; 取得指定属性为指定内容的元素<br><br>· 范例：<br><br>$("div[id=msgA]:visible") .atr("clas","suces") ; · 范例：<br><br>$("input[type=text]").atr("cl</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>[atribute!=value]</td>
    <td>as","suces") ; 取得指定属性不是指定内容的元 素<br><br>· 范例： $("div[id!=msgA]").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>[atribute^=value]</td>
    <td>s","suces") ; 取得指定属性以指定内容开始的 元素<br><br>· 范例： $("div[id^=msg]").atr("clas ","suces") ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>5</td>
    <td>[atribute$=value]</td>
    <td>取得指定属性以指定内容结束的 元素<br><br>· 范例： $("div[id$=A]").atr("clas","</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>6</td>
    <td>[atribute*=value]</td>
    <td>suces") ; 取得指定属性包含指定内容的元 素<br><br>· 范例： $("div[id*=m]").atr("clas","</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>7</td>
    <td>[atribute,atribute,.]</td>
    <td>suces") ; 包含多个属性的元素<br><br>· 范例：$("div[id=msgA] [title=heloA]")</td>
    <td>元素集合</td>
  </tr>
</table>


.atr("clas","suces") ;

- 7、 ⼦元素选择器


<table>
  <tr>
    <th>No.</th>
    <th>⼦元素选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:nth-child (eq|od|even|index)</td>
    <td>获取每个⽗元素下特定位置的元 素，所以从1开始<br><br>· 范例：$("li:nthchild(2)").atr("clas","suce s") ;<br><br>· 范例：$("li:nthchild(od)").atr("clas","suc ces") ;<br><br>· 范例：$("li:nthchild(even)").atr("clas","su</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:first-child</td>
    <td>ces") ;<br><br>获取每个⽗元素下的第⼀个⼦元 素<br><br>· 范例：$("li:firstchild").atr("clas","suces" ) ;；</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:last-child</td>
    <td>获取每个⽗元素下的最后⼀个⼦ 元素<br><br>· 范例：$("li:lastchild").atr("clas","suces"</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:only-child</td>
    <td>) ; 获取只有⼀个⼦元素的元素<br><br>· 范例：$("li:onlychild").atr("clas","suces"</td>
    <td>元素集合</td>
  </tr>
</table>


) ;

- 8、 表单属性选择器


<table>
  <tr>
    <th>No.</th>
    <th>表单属性选择器</th>
    <th>功能描述</th>
    <th>返回值</th>
  </tr>
  <tr>
    <td>1</td>
    <td>:enabled</td>
    <td>所有可以使⽤的表单元素 · 范例： $("input:enabled").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>2</td>
    <td>:disabled</td>
    <td>s","suces") ; 所有不可⽤的表单元素<br><br>· 范例： $("input:disabled").atr("clas</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>3</td>
    <td>:checked</td>
    <td>s","suces") ; 获取所有被选中的元素<br><br>· 范例： $("input[type=radio]:checke</td>
    <td>元素集合</td>
  </tr>
  <tr>
    <td>4</td>
    <td>:selected</td>
    <td>d").val() 获取所有被选中的option元素 · 范例：$("select</td>
    <td>元素集合</td>
  </tr>
</table>


option:selected").val()

jquery操作select(增加，删除，清空)

http://huapengpeng1989412.blog.163.com/blog/static/58828754201342841940720/

jQuery获取Select选择的Text和Value:

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>$("#select_id").change(function(){/code.}); /为Select添加 事件，当选择其中⼀项时触发<br><br>var checkText=$("#select_id").find("option:selected").text(); /获取Select选择的text<br><br>var checkValue=$("#select_id").val(); /获取Select选择的 Value<br><br>var checkIndex=$("#select_id ").get(0).selectedIndex; /获取 Select选择的索引值<br><br>var maxIndex=$("#select_id option:last").atr("index"); /获取 最⼤的索引值</th>
  </tr>
</table>


Select

jQuery添加/删除Select的Option项：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br></th>
    <th>$("#select_id").apend("<option value='Value'>Text</option>"); /为Select追加⼀个Option(下拉 项)<br><br>$("#select_id").prepend("<option value='0'>请选择 </option>"); /为Select插⼊⼀个Option(第⼀个位置)<br><br>$("#select_id option:last").remove(); /删除Select中索引值最 ⼤Option(最后⼀个)<br><br>$("#select_id option[index='0']").remove(); /删除Select中索 引值为0的Option(第⼀个)<br><br>$("#select_id option[value='3']").remove(); /删除Select中 Value='3'的Optiona<br><br>$("#select_id option[text='4']").remove(); /删除Select中</th>
  </tr>
</table>


Text='4'的Optiona

内容清空：

<table>
  <tr>
    <th>1</th>
    <th>$("#charCity").empty();</th>
  </tr>
</table>


http://blog.csdn.net/nairuohe/article/details/6307367

每⼀次操作select的时候，总是要出来翻⼀下资料，不如⾃⼰总结⼀下，以后就翻这⾥了。 ⽐如<select class="selector"></select>

- 1、设置value为pxx的项选中 $(".selector").val("pxx");

- 2、设置text为pxx的项选中 $(".selector").find("option[text='pxx']").attr("selected",true);

这⾥有⼀个中括号的⽤法，中括号⾥的等号的前⾯是属性名称，不⽤加引号。很多时候，中括号的 运⽤可以使得逻辑变得很简单。

- 3、获取当前选中项的value $(".selector").val();

- 4、获取当前选中项的text


$(".selector").find("option:selected").text();

这⾥⽤到了冒号，掌握它的⽤法并举⼀反三也会让代码变得简洁。

很多时候⽤到select的级联，即第⼆个select的值随着第⼀个select选中的值变化。这在jquery中是 ⾮常简单的。 如：

![image 1](<JQuery教程.note_images/imageFile1.png>)

复制代码

$(".selector1").change(function(){ // 先清空第⼆个 $(".selector2").empty();

// 实际的应⽤中，这⾥的option⼀般都是⽤循环⽣成多个了 var option = $("<option>").val(1).text("pxx"); $(".selector2").append(option);

});

![image 2](<JQuery教程.note_images/imageFile2.png>)

复制代码

http://www.cnblogs.com/yaoshiyou/archive/2010/08/24/1806939.html

jQuery获取Select选择的Text和Value: 语法解释：

$("#select_id").change(function(){//code...}); //为Select添加事件，当选择其中⼀项时触发 var checkText=$("#select_id").find("option:selected").text(); //获取Select选择的Text var checkValue=$("#select_id").val(); //获取Select选择的Value var checkIndex=$("#select_id ").get(0).selectedIndex; //获取Select选择的索引值 var maxIndex=$("#select_id option:last").attr("index"); //获取Select最⼤的索引值

jQuery设置Select选择的 Text和Value: 语法解释：

$("#select_id ").get(0).selectedIndex=1; //设置Select索引值为1的项选中 $("#select_id ").val(4); // 设置Select的Value值为4的项选中 $("#select_id option[text='jQuery']").attr("selected", true); //设置Select的Text值为jQuery的项选中

jQuery添加/删除Select的Option项： 语法解释：

![image 3](<JQuery教程.note_images/imageFile3.png>)

复制代码

$("#select_id").append("<option value='Value'>Text</option>"); //为Select追加⼀个Option(下拉项) $("#select_id").prepend("<option value='0'>请选择</option>"); //为Select插⼊⼀个Option(第⼀个位置) $("#select_id option:last").remove(); //删除Select中索引值最⼤Option(最后⼀个) $("#select_id option[index='0']").remove(); //删除Select中索引值为0的Option(第⼀个) $("#select_id option[value='3']").remove(); //删除Select中Value='3'的Option $("#select_id option[text='4']").remove(); //删除Select中Text='4'的Option

![image 4](<JQuery教程.note_images/imageFile4.png>)

复制代码

http://www.cnblogs.com/SAL2928/archive/2008/10/28/1321285.html jquery radio取值，checkbox取值，select取值，radio选中，checkbox选中，select选中，及其相 关 获 取⼀组radio被选中项的值 var item = $('input[name=items][checked]').val(); 获 取select被选中项的⽂本 var item = $("select[name=items] option[selected]").text(); select下拉框的第⼆个元素为当前选中值 $('#select_id')[0].selectedIndex = 1; radio单选组的第⼆个元素为当前选中值 $('input[name=items]').get(1).checked = true; 获取值： ⽂本框，⽂本区域：$("#txt").attr("value")； 多选框 checkbox：$("#checkbox_id").attr("value")； 单选组radio： $("input[type=radio][checked]").val(); 下拉框select： $('#sel').val(); 控制表单元素： ⽂本框，⽂本区域：$("#txt").attr("value",'');//清空内容 $("#txt").attr("value",'11');//填充内容 多选框checkbox： $("#chk1").attr("checked",'');//不打勾 $("#chk2").attr("checked",true);//打勾 if($("#chk1").attr('checked')==undefined) //判断是否已经打勾 单选组 radio： $("input[type=radio]").attr("checked",'2');//设置value=2的项⽬为当前选中 项 下拉框 select： $("#sel").attr("value",'-sel3');//设置value=-sel3的项⽬为当前选中项 $("<option value='1'>1111</option><option value='2'>2222</option>").appendTo("#sel")//添加下拉框的option $("#sel").empty()；//清空下拉框

