⼀直在学习 ,碰到了很多问题，碰到了很多关于i +和 +i的难题，以及最经典的String str = "abc" 共创建了⼏个对象的疑难杂症。 知道有⼀⽇知道了java的反汇编 命令 javap。现将学习记录做⼀⼩ 结，以供⾃⼰以后翻看。如果有错误的地⽅，请指正

Java

- 1.javap是什么： where options include:

- -c Disasemble the code
- -claspath <pathlist> Specify where to find user clas files
- -extdirs <dirs> Overide location of instaled extensions
- -help Print this usage mesage
- -J<flag> Pas <flag> directly to the runtime system
- -l Print line number and local variable tables
- -public Show only public clases and members
- -protected Show protected/public clases and members
- -package Show package/protected/public clases and members (default)
- -private Show al clases and members
- -s Print internal type signatures
- -botclaspath <pathlist> Overide location of clas files loaded by the botstrap clas loader
- -verbose Print stack size, number of locals and args for met hods If verifying, print reasons for failure 以上为百度百科⾥对它的描述，只是介绍了javap的⼀些参数和使⽤⽅法，⽽我们要⽤的就是这⼀个：c Disasemble the code。 明确⼀个问题：javap是什么？⽹上有⼈称之为 反汇编器，可以查看java编译器为我们⽣成的字节码。 通过它，我们可以对照源代码和字节码，从⽽了解很多编译器内部的⼯作。


- 2.初步认识javap 从⼀个最简单的例⼦开始：


![image 1](<javap -c命令详解.note_images/imageFile1.png>)

这个例⼦中，我们只是简单的声明了两个int型变量并赋上初值。下⾯我们看看javap给我们带来了什 么：（当然执⾏javap命令前，你得⾸先配置好⾃⼰的环境，能⽤javac编译通过了，即：javac TestJavap.java)

![image 2](<javap -c命令详解.note_images/imageFile2.png>)

我们只看（⽅便起⻅，将注释写到每句后⾯）

Code: 0: iconst_2 /把2放到栈顶 1: istore_1 /把栈顶的值放到局部变量1中，即i中 2: iconst_3 /把3放到栈顶 3: istore_2 /把栈顶的值放到局部变量1中，即j中 4: return 是不是很简单？（当然，估计需要点 的知识） ，那我们就补点java的关于堆栈的知识： 对于 int i = 2;⾸先它会在栈中创建⼀个变量为i的引⽤，然后查找有没有字⾯值为2的地址，没找到，就 开辟⼀个存放2这个字⾯值的地址，然后将i指向2的地址。 看了这段话，再⽐较下上⾯的注释，是不是完全吻合？ 为了验证上⾯这⼀说法，我们继续实验：

数据结构

![image 3](<javap -c命令详解.note_images/imageFile3.png>)

我们将 i 和 j的值都设为2。按照以上理论，在声明j的时候，会去栈中招有没有字⾯值为2的地址，由于 在栈中已经有2这个字⾯值，便将j直接指向2的地址。这样，就出现了i与j同时均指向2的情况。

拿出javap -c进⾏反编译：结果如下：

![image 4](<javap -c命令详解.note_images/imageFile4.png>)

Code: 0: iconst_2 /把2放到栈顶 1: istore_1 /把栈顶的值放到局部变量1中，即i中 2: iconst_2 /把2放到栈顶 3: istore_2 /把栈顶的值放到局部变量2中，即j中(i 和 j同时指向2) 4: return 虽然这⾥说i和j同时指向2，但这⾥不等于说i和j指向同⼀块地址（java是不允许程序员直接修改堆栈中 的数据的，所以就不要想着，我是不是可以修改栈中的2，那样岂不是i和j的值都会变化。另：在编译 器内部，遇到j=2；时，它就会重新搜索栈中是否有2的字⾯值，如果没有，重新开辟地址存放2的值； 如果已经有了，则直接将j指向这个地址。因此,就算j另被赋值为其他值，如j=4,j值的改变不会影响到i 的值。） 再来⼀个例⼦：

![image 5](<javap -c命令详解.note_images/imageFile5.png>)

还是javap -c

![image 6](<javap -c命令详解.note_images/imageFile6.png>)

Code:

- 0: iconst_2 /把2放到栈顶
- 1: istore_1 /把栈顶的值放到局部变量1中，即i中
- 2: iload_1 /把i的值放到栈顶，也就是说此时栈顶的值是2
- 3: istore_2 /把栈顶的值放到局部变量2中，即j中
- 4: return


看到这⾥是不是有点明确了？

既然我们对javap有了⼀定的了解，那我们就开始⽤它来解决⼀些实际的问题： 1.i +和 +i的问题

![image 7](<javap -c命令详解.note_images/imageFile7.png>)

反编译结果为

![image 8](<javap -c命令详解.note_images/imageFile8.png>)

Code:

- 0: iconst_1
- 1: istore_1
- 2: inc 1, 1 /这个个指令，把局部变量1，也就是i，增加1，这个指令不会导致栈的变化，i此时变成


2了 5: iconst_1

- 6: istore_2
- 7: inc 2, 1/这个个指令，把局部变量2，也就是j，增加1，这个指令不会导致栈的变化，j此时变成


2了 10: return

可以看出， +在前在后，在这段代码中，没有任何不同。 我们再看另⼀段代码：

![image 9](<javap -c命令详解.note_images/imageFile9.png>)

反编译结果：

![image 10](<javap -c命令详解.note_images/imageFile10.png>)

Code:

- 0: iconst_1
- 1: istore_1
- 2: iload_1
- 3: inc 1, 1 /局部变量1（即i）加1变为2，注意这时栈中仍然是1，没有改变


- 6: istore_1 /把栈顶的值放到局部变量1中，即i这时候由2变成了1
- 7: iconst_1
- 8: istore_2
- 9: inc 2, 1 /局部变量2（即j）加1变为2，注意这时栈中仍然是1，没有改变


- 12: iload_2 /把局部变量2（即j）的值放到栈顶，此时栈顶的值变为2


- 13: istore_2 /把栈顶的值放到局部变量2中，即j这时候真正由1变成了2
- 14: return


是否看明⽩了？ 如果这个看明⽩了，那么下⾯的⼀个问题应该就是迎刃⽽解了：

![image 11](<javap -c命令详解.note_images/imageFile11.png>)

m = m +；这句话，java虚拟机执⾏时是这样的： m的值加了1，但这是栈中的值还是0， ⻢上栈中的 值覆盖了m，即m变成0，因此不管循环多少次，m都等于0。 如果改为m = +m; 程序运⾏结果就是10了。。。

