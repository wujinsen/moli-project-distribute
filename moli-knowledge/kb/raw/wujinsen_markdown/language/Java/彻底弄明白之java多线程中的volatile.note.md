## Java 语⾔提供了⼀种稍弱的同步机制,即 volatile 变量.⽤来确保将变量的更新操作 通知到其他线程,保证了新值能⽴即同步到主内存,以及每次使⽤前⽴即从主内存刷 新. 当把变量声明为volatile类型后,编译器与运⾏时都会注意到这个变量是共享的.

⼆. volatite 线程安全?

volatile 变量对所有线程是⽴即可⻅的,对 volatile 变量所有的写操作都能⽴即反应到 其他线程之中,换句话说:volatile 变量在各个线程中是⼀致的,所以基于 volatile 变量的运 算是线程安全的. 这句话论据貌似没有错,论点确实错的.

# 三. volatite 为什么是线程不安全的?

![image 1](<彻底弄明白之java多线程中的volatile.note_images/imageFile1.png>)

public class VolatileTest{

public static volatile int i;

public static void increase(){

i++; }

}

![image 2](<彻底弄明白之java多线程中的volatile.note_images/imageFile2.png>)

javap -c -l VolatileTest.class

![image 3](<彻底弄明白之java多线程中的volatile.note_images/imageFile3.png>)

public class VolatileTest { public static volatile int i;

public VolatileTest(); Code:

- 0: aload_0

- 1: invokespecial #1 // Method java/lang/Object."<init>":()V 4: return


LineNumberTable: line 1: 0

public static void increase(); Code:

0: getstatic #2 // Field i:I, 把i的值取到了操作栈顶,volatile保证了i值此 时是正确的.

- 3: iconst_1

- 4: iadd // increase,但其他线程此时可能已经把i值加⼤了好多

- 5: putstatic #2 // Field i:I ,把这个已经out of date的i值同步回主内存


中,i值被破坏了. 8: return LineNumberTable:

- line 6: 0

- line 7: 8


}

