⾸先，对于String类有⼀点是毫⽆疑问的：对String对象的任何改变都不影响到原对象，相关的任何 change操作都会⽣成新的对象。 ⼀. String与StringBuilder、StringBuffer

- 1.1 三者之间的⽐较

- 1.1.1 添加字符串 string1 += string2;

String类后⾯添加字符串，在以上例⼦中，相当于将原有的string1变量指向的对象内容取出与 string2变量指向的对象作相加操作再存进另⼀个新的String对象当中，再让string1变量指向新⽣成 的对象。（其中string2也可以是⼀个字符串）。

StringBuilder str = new StringBuilder(string1); StringBuffer str = new StringBuffer(string1);

StringBuilder与StringBuffer类后⾯添加字符串，在以上例⼦中，并没有创建新的对象， append操作是在原有对象的基础上进⾏的，如果添加动作要执⾏多次的话，StringBuilder与 StringBuffer在全部过程中只创建了⼀个对象，所占的资源要⽐String⽅式⼩得多。

- 1.1.2 性能及使⽤场景 String、StringBuilder、StringBuffer三者的执⾏效率： StringBuilder > StringBuffer


> String

当然这个是相对的，不⼀定在所有情况下都是这样。⽐如String str = "hello"+ "world"的效率 就⽐ StringBuilder st = new StringBuilder().append("hello").append("world")要⾼。 （String类的str在编译期间被JVM优化成了"helloworld"）。

因此，这三个类是各有利弊，应当根据不同的情况来进⾏选择使⽤：

- 1.2 String与StringBuilder、StringBuffer之间的转换


当字符串相加操作或者改动较少的情况下，建议使⽤ String str="hello"这种形式； 当字符串相加操作较多的情况下，建议使⽤StringBuilder； 如果采⽤了多线程，则使⽤StringBuffer。

- 1.
- 2.
- 3.


# 1.2.1 String转换成StringBuilder与StringBuffer 有两种⽅法：

![image 1](<String与其他类型的转换.note_images/imageFile1.png>)

- 1 //法⼀

- 2 StringBuilder stringbuilder = New StringBuilder("abcd");

- 3 StringBuilder stringbuilder = New StringBuilder(str1);

- 4

- 5 StringBuffer stringbuffer = New StringBuffer("efgh");

- 6 StringBuffer stringbuffer = New StringBuffer(str2);

- 7 //法⼆

- 8 StringBuilder stringbuilder = New StringBuilder();

- 9 stringbuilder.append(str1);

- 10

- 11 StringBuffer stringbuffer = New StringBuffer();

- 12 stringbuffer.append(str2)


![image 2](<String与其他类型的转换.note_images/imageFile2.png>)

### 1.2.2 StringBuilder与StringBuffer转换成String

- String str1 = stringbuffer.toString();

- String str2 = stringbuilder.toString(); ⼆. String与int


- 2.1 将字串String转换成整数int 有两个⽅法:

- 1 //法⼀

- 2 int i = Integer.parseInt([String]);

- 3 int i = Integer.parseInt([String],[int radix]);

- 4 //法⼆

- 5 int i = Integer.valueOf(my_str).intValue();


- 2.2 将整数int转换成字串String 有三种⽅法：


![image 3](<String与其他类型的转换.note_images/imageFile3.png>)

- 1 //法⼀

- 2 String s = String.valueOf(i);

- 3 //法⼆

- 4 String s = Integer.toString(i);

- 5 //法三

- 6 String s = "" + i;


![image 4](<String与其他类型的转换.note_images/imageFile4.png>)

三. String与字符数组（char[]）

- 3.1 String转换成字符数组（char[]） 有两种⽅法：

- 1 //法⼀

- 2 char[] strChar = str.toCharArray();

- 3 //法⼆

- 4 int[] strChar = new int[str.length()];

- 5 for(int i = 0;i < str.length(); i++){

- 6 strChar[i] = (int)str.charAt(i);

- 7 }


- 3.2 字符数组（char[]）转换成String 有两种⽅法：


![image 5](<String与其他类型的转换.note_images/imageFile5.png>)

![image 6](<String与其他类型的转换.note_images/imageFile6.png>)

- 1 char data[] = {'h', 'e', 'l', 'l', 'o'};

- 2 //法⼀

- 3 String str1 = new String(data);

- 4 //法⼆

- 5 String str2 = String.valueOf(data); 四. String与byte[]


# 4.1 String转换成byte[]

- 1 byte[] midbytes=isoString.getBytes("UTF8");

- 2 //为UTF8编码

- 3 byte[] isoret = srt2.getBytes("ISO-8859-1");

- 4 //为ISO-8859-1编码,其中ISO-8859-1为单字节的编码


# 4.2 byte[]转换成String

- 1 String isoString = new String(bytes,"ISO-8859-1");

- 2 String srt2=new String(midbytes,"UTF-8"); 五. String[]与List


## 5.1 String[]转换成List 有以下三种⽅法，其实String[]可以是任意类型的数组。

![image 7](<String与其他类型的转换.note_images/imageFile7.png>)

- 1 //法⼀

- 2 String[] userid = {"aa","bb","cc"};

- 3 List<String> userList = Arrays.asList(userid);

- 4 //法⼆

- 5 String[] userid = {"aa","bb","cc"};

- 6 List<String> userList = new ArrayList<String>();

- 7 Collections.addAll(userList, userid);

- 8 //法三 最笨的⽅法

- 9 String[] userid = {"aa","bb","cc"};

- 10 List<String> userList = new ArrayList<String>(userid.length);

- 11 for(String uid: userid){

- 12 userList.add(uid);

- 13 }


![image 8](<String与其他类型的转换.note_images/imageFile8.png>)

- 5.2 List转换成String[] 有以下两种⽅法，其实String[]可以是任意类型的数组


![image 9](<String与其他类型的转换.note_images/imageFile9.png>)

- 1 //法⼀

- 2 List<String> strList = new ArrayList<String>();

- 3 strList.add("aa");

- 4 strList.add("bb");

- 5 Object[] objs = strList.toArray();

- 6 //如果要变成String数组，需要强转类型。

- 7 String[] strs = (String[]) strList.toArray();

- 8 //也可以指定⼤⼩：

- 9 final int size = strList.size();

- 10 String[] strs = (String[])strList.toArray(new String[size]);

- 11

- 12 //法⼆ 笨⽅法

- 13 List<String> strList = new ArrayList<String>();

- 14 strList.add("aa");

- 15 strList.add("bb");

- 16 String[] strs = new String[strList.size()];

- 17 Iterator iter = strList.iterator();

- 18 int i = 0;

- 19 while(iter.hasNext()){

- 20 strs[i] = (String) iter.next();

- 21 i++;

- 22 }


![image 10](<String与其他类型的转换.note_images/imageFile10.png>)

# 六. String与Date 详⻅：

http://www.cnblogs.com/bmbm/archive/2011/12/06/2342264.html

http://www.cnblogs.com/dolphin0520/p/3778589.html http://zhangyuefeng1983.blog.163.com/blog/static/1083372520126693524870/ http://www.cnblogs.com/bmbm/archive/2011/12/06/2342264.html

参考：

