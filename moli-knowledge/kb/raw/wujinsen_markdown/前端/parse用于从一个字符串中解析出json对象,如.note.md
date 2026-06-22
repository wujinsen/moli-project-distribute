在JS中将JSON的字符串解析成JSON数据格式，⼀般有两种⽅式：

- 1.⼀种为使⽤eval()函数。
- 2. 使⽤Function对象来进⾏返回解析。


使⽤eval函数来解析，并且使⽤jquery的each⽅法来遍历

⽤jquery解析JSON数据的⽅法,作为jquery异步请求的传输对象，jquery请求后返回的结果是json对象, 这⾥考虑的都是服务器返回JSON形式的字符串的形式，对于利⽤JSONObject等插件封装的JSON对 象，与此亦是⼤同⼩异，这⾥不再做说明。 这⾥⾸先给出JSON字符串集，字符串集如下：

代码如下: 复制代码 代码如下:

var data=" { rot: [ {name:'1',value:'0'},

- {name:'6101',value:'北京市'},
- {name:'6102',value:'天津市'},
- {name:'6103',value:'上海市'},
- {name:'6104',value:'重庆市'},
- {name:'6105',value:'渭南市'},
- {name:'6106',value:'延安市'},
- {name:'6107',value:'汉中市'},
- {name:'6108',value:'榆林市'},
- {name:'6109',value:'安康市'}, {name:'610',value:'商洛市'} ] }";


这⾥以jquery异步获取的数据类型⸺json对象和字符串为依据，分别介绍两种⽅式获取到的结果处理 ⽅式。

- 1.对于服务器返回的JSON字符串，如果jquery异步请求没做类型说明，或者以字符串⽅式接受，那么 需要做⼀次对象化处理，⽅式不是太麻烦，就是将该字符串放于eval()中执⾏⼀次。这种⽅式也适合以 普通javascipt⽅式获取json对象，以下举例说明：


var dataObj=eval("("+data+")");/转换为json对象 为什么要 eval这⾥要添加 “("("+data+")");/”呢？

原因在于：eval本身的问题。 由于json是以”{}”的⽅式来开始以及结束的，在JS中，它会被当成⼀个语 句块来处理，所以必须强制性的将它转换成⼀种表达式。

加上圆括号的⽬的是迫使eval函数在处理JavaScript代码的时候强制将括号内的表达式（expresion） 转化为对象，⽽不是作为语句（statement）来执⾏。举⼀个例⼦，例如对象字⾯量{}，如若不加外层 的括号，那么eval会将⼤括号识别为JavaScript代码块的开始和结束标记，那么{}将会被认为是执⾏了 ⼀句空语句。所以下⾯两个执⾏结果是不同的： 复制代码 代码如下:

alert(eval("{}"); / return undefined alert(eval("({})");/ return object[Object]

对于这种写法，在JS中，可以到处看到。

如: (function() {}(); 做闭包操作时等。

复制代码 代码如下:

alert(dataObj.rot.length);/输出rot的⼦对象数量 $.each(dataObj.rot,fucntion(idx,item){ if(idx=0){ return true; }

/输出每个rot⼦对象的名称和值 alert("name:"+item.name+",value:"+item.value); })

注：对于⼀般的js⽣成json对象，只需要将$.each()⽅法替换为for语句即可，其他不变。

- 2.对于服务器返回的JSON字符串，如果jquery异步请求将type（⼀般为这个配置属性）设为“json”， 或者利⽤$.getJSON()⽅法获得服务器返回，那么就不需要eval（）⽅法了，因为这时候得到的结果已 经是json对象了，只需直接调⽤该对象即可，这⾥以$.getJSON⽅法为例说明数据处理⽅法： 复制代码 代码如下:


$.getJSON("htp:/ w.phpzixue.cn/",{param:"gaoyusi"},function(data){ /此处返回的data已经是json对象 /以下其他操作同第⼀种情况

$.each(data.rot,function(idx,item){ if(idx=0){ return true;/同countinue，返回false同break } alert("name:"+item.name+",value:"+item.value); }); });

这⾥特别需要注意的是⽅式1中的eval()⽅法是动态执⾏其中字符串（可能是js脚本）的，这样很容易会 造成系统的安全问题。所以可以采⽤⼀些规避了eval()的第三⽅客户端脚本库，⽐如JSON in JavaScript就提供了⼀个不超过3k的脚本库。

第⼆种解析⽅式就是使⽤Function对象来完成，它的典型应⽤就是在JQUERY中的AJAX⽅法下的 suces等对于返回数据data的解析 复制代码 代码如下:

var json='{"name":"CJ","age":18}'; data =(new Function(","return "+json)();

此时的data就是⼀个会解析成⼀个 json对象了.

