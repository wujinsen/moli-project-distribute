cut是⼀个选取命令，就是将⼀段数据经过分析，取出我们想要的。⼀般来说，选取信息通常是针对 “⾏”来进⾏分析的，并不是整篇信息分析的。

- （1）其语法格式为： cut [-bn] [file] 或 cut [-c] [file] 或 cut [-df] [file] 使⽤说明 cut 命令从⽂件的每⼀⾏剪切字节、字符和字段并将这些字节、字符和字段写⾄标准输出。 如果不指定 File 参数，cut 命令将读取标准输⼊。必须指定 -b、-c 或 -f 标志之⼀。 主要参数

- -b ：以字节为单位进⾏分割。这些字节位置将忽略多字节字符边界，除⾮也指定了 -n 标志。
- -c ：以字符为单位进⾏分割。
- -d ：⾃定义分隔符，默认为制表符。
- -f ：与-d⼀起使⽤，指定显示哪个区域。
- -n ：取消分割多字节字符。仅和 -b 标志⼀起使⽤。如果字符的最后⼀个字节落在由 -b 标志的 List 参 数指示的<br />范围之内，该字符将被写出；否则，该字符将被排除。


- （2）cut⼀般以什么为依据呢? 也就是说，我怎么告诉cut我想定位到的剪切内容呢? cut命令主要是接受三个定位⽅法： 第⼀，字节（bytes），⽤选项-b 第⼆，字符（characters），⽤选项-c 第三，域（fields），⽤选项-f
- （3）以“字节”定位 举个例⼦吧，当你执⾏ps命令时，会输出类似如下的内容： [rocrocket@rocrocket progra ming]$ who rocrocket :0 209-01-081 07

- rocrocket pts/0 209-01-081 23 (:0.0)
- rocrocket pts/1 209-01-08 14 15 (:0.0) 如果我们想提取每⼀⾏的第3个字节，就这样： [rocrocket@rocrocket progra ming]$ who|cut -b 3 c c c


- （4） 如果“字节”定位中，我想提取第3，第4、第5和第8个字节，怎么办?


-b⽀持形如3-5的写法，⽽且多个定位之间⽤逗号隔开就成了。看看例⼦吧： [rocrocket@rocrocket progra ming]$ who|cut -b 3-5,8 croe croe croe

但有⼀点要注意，cut命令如果使⽤了-b选项，那么执⾏此命令时，cut会先把-b后⾯所有的定位进⾏ 从⼩到⼤排序，然后再提取。可不能颠倒定位的顺序哦。这个例⼦就可以说明这个问题： [rocrocket@rocrocket progra ming]$ who|cut -b 8,3-5 croe croe croe

- （5） 还有哪些类似“3-5”这样的⼩技巧，列举⼀下吧! [rocrocket@rocrocket progra ming]$ who rocrocket :0 209-01-081 07

- rocrocket pts/0 209-01-081 23 (:0.0)
- rocrocket pts/1 209-01-08 14 15 (:0.0) [rocrocket@rocrocket progra ming]$ who|cut -b -3 roc roc roc [rocrocket@rocrocket progra ming]$ who|cut -b 3crocket :0 209-01-081 07


- crocket pts/0 209-01-081 23 (:0.0)
- crocket pts/1 209-01-08 14 15 (:0.0) 想必你也看到了，-3表示从第⼀个字节到第三个字节，⽽3-表示从第三个字节到⾏尾。如果你细⼼， 你可以看到这两种情况下，都包括了第三个字节“c”。 如果我执⾏who|cut -b -3,3-，你觉得会如何呢？答案是输出整⾏，不会出现连续两个重叠的c的。 看：


- [rocrocket@rocrocket progra ming]$ who|cut -b -3,3rocrocket :0 209-01-081 07

- rocrocket pts/0 209-01-081 23 (:0.0)
- rocrocket pts/1 209-01-08 14 15 (:0.0)


（6）给个以字符为定位标志的最简单的例⼦吧! 下⾯例⼦你似曾相识，提取第3，第4，第5和第8个字符：

- [rocrocket@rocrocket progra ming]$ who|cut -c 3-5,8 croe croe croe 不过，看着怎么和-b没有什么区别啊？莫⾮-b和-c作⽤⼀样? 其实不然，看似相同，只是因为这个例⼦ 举的不好，who输出的都是单字节字符，所以⽤-b和-c没有区别，如果你提取中⽂，区别就看出来 了，来，看看中⽂提取的情况： [rocrocket@rocrocket progra ming]$ cat cut_ch.txt




星期⼀ 星期⼆ 星期三 星期四

- [rocrocket@rocrocket progra ming]$ cut -b 3 cut_ch.txt
- [rocrocket@rocrocket progra ming]$ cut -c 3 cut_ch.txt ⼀ ⼆ 三 四 看到了吧，⽤-c则会以字符为单位，输出正常；⽽-b只会傻傻的以字节（8位⼆进制位）来计算，输出 就是乱码。 既然提到了这个知识点，就再补充⼀句，如果你学有余⼒，就提⾼⼀下。 当遇到多字节字符时，可以使⽤-n选项，-n⽤于告诉cut不要将多字节字符拆开。例⼦如下： [rocrocket@rocrocket progra ming]$ cat cut_ch.txt |cut -b 2


[rocrocket@rocrocket progra ming]$ cat cut_ch.txt |cut -nb 2 [rocrocket@rocrocket progra ming]$ cat cut_ch.txt |cut -nb 1,2,3 星 星 星 星

- （7）域是怎么回事呢？解释解释:) 为什么会有“域”的提取呢，因为刚才提到的-b和-c只能在固定格式的⽂档中提取信息，⽽对于⾮固定 格式的信息则束⼿⽆策。这时候“域”就派上⽤场了。如果你观察过/etc/paswd⽂件，你会发现，它并 不像who的输出信息那样具有固定格式，⽽是⽐较零散的排放。但是，冒号在这个⽂件的每⼀⾏中都 起到了⾮常重要的作⽤，冒号⽤来隔开每⼀个项。 我们很幸运，cut命令提供了这样的提取⽅式，具体的说就是设置“间隔符”，再设置“提取第⼏个域”， 就OK了！ 以/etc/paswd的前五⾏内容为例：


- [rocrocket@rocrocket progra ming]$ cat /etc/paswd|head -n 5 rot:x:0 0:rot:/rot:/bin/bash bin:x:1 1:bin:/bin:/sbin/nologin daemon:x:2 2:daemon:/sbin:/sbin/nologin adm:x:3 4:adm:/var/adm:/sbin/nologin lp:x:4 7:lp:/var/spol/lpd:/sbin/nologin [rocrocket@rocrocket progra ming]$ cat /etc/paswd|head -n 5|cut -d : -f 1 rot bin daemon adm lp 看到了吧，⽤-d来设置间隔符为冒号，然后⽤-f来设置我要取的是第⼀个域，再按回⻋，所有的⽤户名 就都列出来了！呵呵 有成就感吧！ 当然，在设定-f时，也可以使⽤例如3-5或者4-类似的格式： [rocrocket@rocrocket progra ming]$ cat /etc/paswd|head -n 5|cut -d : -f 1,3-5 rot:0 0:rot bin:1 1:bin daemon:2 2:daemon adm:3 4:adm lp:4 7:lp [rocrocket@rocrocket progra ming]$ cat /etc/paswd|head -n 5|cut -d : -f 1,3-5,7 rot:0 0:rot:/bin/bash bin:1 1:bin:/sbin/nologin daemon:2 2:daemon:/sbin/nologin adm:3 4:adm:/sbin/nologin lp:4 7:lp:/sbin/nologin [rocrocket@rocrocket progra ming]$ cat /etc/paswd|head -n 5|cut -d : -f -2 rot:x bin:x daemon:x adm:x lp:x
- （8）如果遇到空格和制表符时，怎么分辨呢？我觉得有点乱，怎么办？ 有时候制表符确实很难辨认，有⼀个⽅法可以看出⼀段空格到底是由若⼲个空格组成的还是由⼀个制 表符组成的。 [rocrocket@rocrocket progra ming]$ cat tab_space.txt


- this is tab finish. this is several space finish. [rocrocket@rocrocket progra ming]$ sed -n l tab_space.txt this is tab\tfinish.$ this is several space finish.$ 看到了吧，如果是制表符（TAB），那么会显示为\t符号，如果是空格，就会原样显示。 通过此⽅法即可以判断制表符和空格了。 注意，上⾯sed -n后⾯的字符是L的⼩写字⺟哦，不要看错。
- （9）我应该在cut -d中⽤什么符号来设定制表符或空格呢? 其实cut的-d选项的默认间隔符就是制表符，所以当你就是要使⽤制表符的时候，完全就可以省略-d选 项，⽽直接⽤－f来取域就可以了。 如果你设定⼀个空格为间隔符，那么就这样： [rocrocket@rocrocket progra ming]$ cat tab_space.txt |cut -d ' ' -f 1 this this 注意，两个单引号之间可确实要有⼀个空格哦，不能偷懒。 ⽽且，你只能在-d后⾯设置⼀个空格，可不许设置多个空格，因为cut只允许间隔符是⼀个字符。 [rocrocket@rocrocket progra ming]$ cat tab_space.txt |cut -d ' ' -f 1 cut: the delimiter must be a single character Try `cut -help' for more information.
- （10）cut有哪些缺陷和不⾜？ 猜出来了吧？对，就是在处理多空格时。 如果⽂件⾥⾯的某些域是由若⼲个空格来间隔的，那么⽤cut就有点麻烦了，因为cut只擅⻓处理“以⼀ 个字符间隔”的⽂本内容


