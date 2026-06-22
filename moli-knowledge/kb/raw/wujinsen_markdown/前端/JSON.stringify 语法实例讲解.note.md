语法： JSON.stringify(value [, replacer] [, space])

value：是必选字段。就是你输⼊的对象，⽐如数组，类等。 replacer：这个是可选的。它⼜分为2种⽅式，⼀种是数组，第⼆种是⽅法。

情况⼀：replacer为数组时，通过后⾯的实验可以知道，它是和第⼀个参数value有关系的。⼀般 来说，系列化后的结果是通过键值对来进⾏表示的。 所以，如果此时第⼆个参数的值在第⼀个存在，那 么就以第⼆个参数的值做key，第⼀个参数的值为value进⾏表示，如果不存在，就忽略。

情况⼆：replacer为⽅法时，那很简单，就是说把系列化后的每⼀个对象（记住是每⼀个）传进⽅ 法⾥⾯进⾏处理。

space：就是⽤什么来做分隔符的。

- 1）如果省略的话，那么显示出来的值就没有分隔符，直接输出来 。

- 2）如果是⼀个数字的话，那么它就定义缩进⼏个字符，当然如果⼤于10 ，则默认为10，因为最

⼤值为10。

- 3）如果是⼀些转义字符，⽐如“\t”，表示回⻋，那么它每⾏⼀个回⻋。

- 4）如果仅仅是字符串，就在每⾏输出值的时候把这些字符串附加上去。当然，最⼤⻓度也是10个


字符。 下⾯⽤实例说明；

1）只有第⼀个参数的情况下

![image 1](<JSON.stringify 语法实例讲解.note_images/imageFile1.png>)

- //1 var student = new Object(); student.name = "Lanny"; student.age = "25"; student.location = "China"; var json = JSON.stringify(student); alert(json); //alert(student);


![image 2](<JSON.stringify 语法实例讲解.note_images/imageFile2.png>)

结果如下图：

![image 3](<JSON.stringify 语法实例讲解.note_images/imageFile3.png>)

有些⼈可能会怀疑JSON.stringify的作⽤。那假如，我们不要这个函数，⽽直接alert(student)， 结果如下：

![image 4](<JSON.stringify 语法实例讲解.note_images/imageFile4.png>)

这次意识到JSON.stringify的作⽤了吧。 2）第⼆个参数存在，并且第⼆个参数还是function的时候

![image 5](<JSON.stringify 语法实例讲解.note_images/imageFile5.png>)

- //2 var students = new Array() ;


- students[0] = "onepiece";

- students[1] = "naruto";

- students[2] = "bleach"; var json = JSON.stringify(students,switchUpper); function switchUpper(key, value) {


return value.toString().toUpperCase();

} alert(json); /*下⾯这种⽅式也可以 var json = JSON.stringify(students, function (key,value) { return value.toString().toUpperCase()}); alert(json);

*/

![image 6](<JSON.stringify 语法实例讲解.note_images/imageFile6.png>)

运⾏结果如下：

![image 7](<JSON.stringify 语法实例讲解.note_images/imageFile7.png>)

- 3）第⼆个参数存在，并且第⼆个参数不是function，⽽是数组的时候


![image 8](<JSON.stringify 语法实例讲解.note_images/imageFile8.png>)

- //3

- var stuArr1 = new Array() ;

- stuArr1[0] = "onepiece";

- stuArr1[1] = "naruto";

- stuArr1[2] = "bleach";


- var stuArr2 = new Array();


- stuArr2[0] = "1";

- stuArr2[1] = "2"; var json = JSON.stringify(stuArr1,stuArr2) alert(json);


运⾏结果如下：

第⼆个参数被忽略了，只是第⼀个参数被系列化了。

- 4）如果第⼀个参数是对象，第⼆个参数是数组的情况


- //4 var stuObj = new Object(); stuObj.id = "20122014001"; stuObj.name = "Tomy"; stuObj.age = 25;


![image 9](<JSON.stringify 语法实例讲解.note_images/imageFile9.png>)

![image 10](<JSON.stringify 语法实例讲解.note_images/imageFile10.png>)

![image 11](<JSON.stringify 语法实例讲解.note_images/imageFile11.png>)

var stuArr = new Array();

- stuArr[0] = "id";

- stuArr[1] = "age";

- stuArr[2] = "addr";//这个stuObj对象⾥不存在。


var json = JSON.stringify(stuObj,stuArr);

//var json = JSON.stringify(stuObj,stuArr,1000); //var json = JSON.stringify(stuObj,stuArr,'\t'); //var json = JSON.stringify(stuObj,stuArr,'OK ');

alert(json);

![image 12](<JSON.stringify 语法实例讲解.note_images/imageFile12.png>)

运⾏结果如下：

![image 13](<JSON.stringify 语法实例讲解.note_images/imageFile13.png>)

第三个参数为数字时候的输出结果：

![image 14](<JSON.stringify 语法实例讲解.note_images/imageFile14.png>)

第三个参数为转义字符\t的时候输出的结果：

![image 15](<JSON.stringify 语法实例讲解.note_images/imageFile15.png>)

第三个参数为字符串时候的输出结果：

![image 16](<JSON.stringify 语法实例讲解.note_images/imageFile16.png>)

参考资料：

http://www.jb51.net/article/29893.htm

parse⽤于从⼀个字符串中解析出json对象,如

var str = '{"name":"huangxiaojian","age":"23"}' 结果： JSON.parse(str)

Object

- 1.
- 2.
- 3.


age: "23" name: "huangxiaojian" __proto__: Object

注意：单引号写在{}外，每个属性名都必须⽤双引号，否则会抛出异常。

stringify()⽤于从⼀个对象解析出字符串，如

var

a = {a:1,b:2} 结果： JSON.stringify(a)

"{"a":1,"b":2}"

