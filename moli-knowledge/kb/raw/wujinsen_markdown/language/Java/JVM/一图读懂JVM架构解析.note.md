每个Java开发⼈员都知道字节码经由JRE（Java运⾏时环境）执⾏。但他们或许不知道JRE其实是由 Java虚拟机（JVM）实现，JVM分析字节码，解释并执⾏它。作为开发⼈员，了解JVM的架构是⾮常 重要的，因为它使我们能够编写出更⾼效的代码。本⽂中，我们将深⼊了解Java中的JVM架构和JVM 的各个组件。

## JVM

虚拟机是物理机的软件实现。Java的设计理念是WORA（Write Once Run Anywhere，⼀次编写随处 运⾏）。编译器将Java⽂件编译为Java .class⽂件，然后将.class⽂件输⼊到JVM中，JVM执⾏类⽂件 的加载和执⾏的操作。请看以下的JVM架构图：

![image 1](<一图读懂JVM架构解析.note_images/imageFile1.png>)

## JVM是如何⼯作的？

如上⾯架构图所示，JVM分为三个主要⼦系统：

- 1.
- 2.
- 3.


类加载器⼦系统（Class Loader Subsystem） 运⾏时数据区（Runtime Data Area） 执⾏引擎（Execution Engine）

- 1. 类加载器⼦系统


Java的动态类加载功能由类加载器⼦系统处理，处理过程包括加载和链接，并在类⽂件运⾏时，⾸次 引⽤类时就开始实例化类⽂件，⽽不是在编译时进⾏。

- 1.1 加载 Boot Strap类加载器，Extension类加载器和Application（类加载器是实现类加载过程的三个类加载 器。

- (1) Boot Strap类加载器：负责从引导类路径加载类，除了rt.jar，它具有最⾼优先级；

- (2) Extension 类加载器：负责加载ext⽂件夹（jre \ lib）中的类；

- (3) Application类加载器：负责加载应⽤程序级类路径，环境变量中指定的路径等信息。 上⾯的类装载器在加载类⽂件时遵循委托层次算法（Delegation Hierarchy Algorithm）。


- 1.2 链接

- (1) 验证（Verify）：字节码验证器将验证⽣成的字节码是否正确，如果验证失败，将提示验证错误；

- (2) 准备（Prepare）：对于所有静态变量，内存将会以默认值进⾏分配；

- (3) 解释（Resolve）：有符号存储器引⽤都将替换为来⾃⽅法区（Method Area）的原始引⽤。


- 1.3 初始化 这是类加载的最后阶段，所有的静态变量都将被赋予原始值，并且静态区块将被执⾏。


#### 2. 运⾏时数据区

运⾏时数据区可分为5个主要组件：

- (1) ⽅法区（Method Area）：所有的类级数据将存储在这⾥，包括静态变量。每个JVM只有⼀个⽅法 区，它是⼀个共享资源；

- (2) 堆区域（Heap Area）：所有对象及其对应的实例变量和数组将存储在这⾥。每个JVM也只有⼀个 堆区域。由于⽅法和堆区域共享多个线程的内存，所存储的数据不是线程安全的；

- (3) 堆栈区（Stack Area）：对于每个线程，将创建单独的运⾏时堆栈。对于每个⽅法调⽤，将在堆栈 存储器中产⽣⼀个条⽬，称为堆栈帧。所有局部变量将在堆栈内存中创建。堆栈区域是线程安全的， 因为它不共享资源。堆栈框架分为三个⼦元素：

- (4) PC寄存器（PC Registers）：每个线程都有单独的PC寄存器，⽤于保存当前执⾏指令的地址。⼀ 旦执⾏指令，PC寄存器将被下⼀条指令更新；

- (5) 本地⽅法堆栈（Native Method stacks）：本地⽅法堆栈保存本地⽅法信息。对于每个线程，将 创建⼀个单独的本地⽅法堆栈。


局部变量数组（Local Variable Array）：与⽅法相关，涉及局部变量，并在此存储相应的值 操作数堆栈（Operand stack）：如果需要执⾏任何中间操作，操作数堆栈将充当运⾏时⼯作空间 来执⾏操作 帧数据（Frame Data）：对应于⽅法的所有符号存储在此处。在任何异常的情况下，捕获的区块信 息将被保持在帧数据中；

#### 3 执⾏引擎

分配给运⾏时数据区的字节码将由执⾏引擎执⾏，执⾏引擎读取字节码并逐个执⾏。

- (1) 解释器：解释器更快地解释字节码，但执⾏缓慢。解释器的缺点是当⼀个⽅法被调⽤多次时，每次 都需要⼀个新的解释；

- (2) JIT编译器：JIT编译器消除了解释器的缺点。执⾏引擎将在转换字节码时使⽤解释器的帮助，但是 当它发现重复的代码时，将使⽤JIT编译器，它编译整个字节码并将其更改为本地代码。这个本地代码 将直接⽤于重复的⽅法调⽤，这提⾼了系统的性能。JIT的构成组件为：


中间代码⽣成器（Intermediate Code Generator）：⽣成中间代码 代码优化器（Code Optimizer）：负责优化上⾯⽣成的中间代码 ⽬标代码⽣成器（Target Code Generator）：负责⽣成机器代码或本地代码 分析器（Proﬁler）：⼀个特殊组件，负责查找热点，即该⽅法是否被多次调⽤；

- (3) 垃圾收集器(Garbage Collector)：收集和删除未引⽤的对象。可以通过调⽤“System.gc（）”触发 垃圾收集，但不能保证执⾏。JVM的垃圾回收对象是已创建的对象。 Java本机接⼝（JNI）：JNI将与本机⽅法库进⾏交互，并提供执⾏引擎所需的本机库。 本地⽅法库（Native Method Libraries）：它是执⾏引擎所需的本机库的集合。


The JVM Architecture Explained Jackson Joseraj

原⽂： 作者： 翻译：Daisy 责编：仲培艺

# The JVM Architecture Explained

An overview of the diﬀerent components of the JVM, along with a very useful diagram

Check out this to see how you can increase your productivity by skipping slow application redeploys and by implementing application profiling, as you code! Brought to you in partnership with .

8-step guide

ZeroTurnaround

Every Java developer knows that bytecode will be executed by JRE (Java Runtime Environment). But many doesn't know the fact that JRE is the implementation of Java Virtual Machine (JVM), which analyzes the bytecode, interprets the code, and executes it. It is very important as a developer that we should know the Architecture of the JVM, as it enables us to write code more efficiently. In this article, we will learn more deeply about the JVM architecture in Java and the different components of the JVM.

## What is the JVM?

A Virtual Machine is a software implementation of a physical machine. Java was developed with the concept of WORA (Write Once Run Anywhere), which runs on a VM. Thecompiler compiles the Java file into a Java .class file, then that .class file is input into the JVM, which Loads and executes the class file. Below is a diagram of the Architecture of the JVM.

JVM Architecture Diagram

![image 2](<一图读懂JVM架构解析.note_images/imageFile2.png>)

## How Does the JVM Work?

As shown in the above architecture diagram, the JVM is divided into three main subsystems:

- 1.
- 2.
- 3.


Class Loader Subsystem Runtime Data Area Execution Engine

### 1. Class Loader Subsystem

Java's functionality is handled by the class loader subsystem. It loads, links. and initializes the class file when it refers to a class for the first time at runtime, not compile time.

##### dynamic class loading

- 1.1 Loading Classes will be loaded by this component. Boot Strap class Loader, Extension class Loader, and Application class Loader are the three class loader which will help in achieving it.


- 1.


Boot Strap – Responsible for loading classes from the bootstrap classpath, nothing but rt.jar. Highest priority will be given to this loader.

ClassLoader

- 2.
- 3.


Extension ClassLoader – Responsible for loading classes which are inside ext folder(jre\lib). Application ClassLoader –Responsible for loading Application Level Classpath, path mentioned Environment Variable etc.

##### The above Class Loaders will follow Delegation Hierarchy Algorithm while loading the class files.

- 1.2 Linking

- 1.3 Initialization This is the final phase of Class Loading, here all will be assigned with the original values, and the will be executed.


- 1.
- 2.
- 3.


Verify – Bytecode verifier will verify whether the generated bytecode is proper or not if verification fails we will get the verification error. Prepare – For all static variables memory will be allocated and assigned with default values. Resolve – All symbolic memory references are replaced with the original references from Method Area.

##### static variables static block

### 2. Runtime Data Area

The Runtime Data Area is divided into 5 major components:

- 1.
- 2.
- 3.


Method Area – All the class level data will be stored here, including static variables. There is only one method area per JVM, and it is a shared resource. Heap Area – All the Objects and their corresponding instance variables and arrays will be stored here. There is also one Heap Area per JVM. Since the Method and Heap areas share memory for multiple threads, the data stored is not thread safe. Stack Area – For every thread, a separate runtime stack will be created. For every method call, one entry will be made in the stack memory which is called as Stack Frame. All local variables will be created in the stack memory. The stack area is thread safe since it is not a shared resource. The Stack Frame is divided into three subentities:

- a.
- b.
- c.


Local Variable Array – Related to the method how many local variables are involved and the corresponding values will be stored here. Operand stack – If any intermediate operation is required to perform, operand stackacts as runtime workspace to perform the operation. Frame data – All symbols corresponding to the method is stored here. In the case of anyexception, the catch block information will be maintained in the frame data.

- 4.
- 5.


PC Registers – Each thread will have separate PC Registers, to hold the address of current executing instruction once the instruction is executed the PC register will be updated with the next instruction. Native Method stacks – Native Method Stack holds native method information. For every thread, a separate native method stack will be created.

### 3. Execution Engine

The bytecode which is assigned to the Runtime Data Area will be executed by the Execution Engine. The Execution Engine reads the bytecode and executes it piece by piece.

- 1.
- 2.

- a.
- b.
- c.
- d.


- 3.


Interpreter – The interpreter interprets the bytecode faster, but executes slowly. The disadvantage of the interpreter is that when one method is called multiple times, every time a new interpretation is required. JIT Compiler – The JIT Compiler neutralizes the disadvantage of the interpreter. The Execution Engine will be using the help of the interpreter in converting byte code, but when it finds repeated code it uses the JIT compiler, which compiles the entire bytecode and changes it to native code. This native code will be used directly for repeated method calls, which improve the performance of the system.

Intermediate Code generator – Produces intermediate code Code Optimizer – Responsible for optimizing the intermediate code generated above Target Code Generator – Responsible for Generating Machine Code or Native Code Profiler – A special component, responsible for finding hotspots, i.e. whether the method is called multiple times or not.

Garbage Collector: Collects and removes unreferenced objects. Garbage Collection can be triggered by calling "System.gc()", but the execution is not guaranteed. Garbage collection of the JVM collects the objects that are created.

Java Native Interface (JNI): JNI will be interacting with the Native Method Libraries and provides the Native Libraries required for the Execution Engine. Native Method Libraries:It is a collection of the Native Libraries which is required for the Execution Engine.

The Java Zone is brought to you in partnership with . Check out this

ZeroTurnaround 8-s tep guide

to see how you can increase your productivity by skipping slow application redeploys and by implementing application profiling, as you code!

每个Java开发⼈员都知道字节码经由JRE（Java运⾏时环境）执⾏。但他们或许不知道JRE其实是由 Java虚拟机（JVM）实现，JVM分析字节码，解释并执⾏它。作为开发⼈员，了解JVM的架构是⾮常 重要的，因为它使我们能够编写出更⾼效的代码。本⽂中，我们将深⼊了解Java中的JVM架构和JVM 的各个组件。

## JVM

虚拟机是物理机的软件实现。Java的设计理念是WORA（Write Once Run Anywhere，⼀次编写随处 运⾏）。编译器将Java⽂件编译为Java .class⽂件，然后将.class⽂件输⼊到JVM中，JVM执⾏类⽂件 的加载和执⾏的操作。请看以下的JVM架构图：

![image 3](<一图读懂JVM架构解析.note_images/imageFile3.png>)

## JVM是如何⼯作的？

如上⾯架构图所示，JVM分为三个主要⼦系统：

- 1.
- 2.
- 3.


类加载器⼦系统（Class Loader Subsystem） 运⾏时数据区（Runtime Data Area） 执⾏引擎（Execution Engine）

- 1. 类加载器⼦系统


Java的动态类加载功能由类加载器⼦系统处理，处理过程包括加载和链接，并在类⽂件运⾏时，⾸次 引⽤类时就开始实例化类⽂件，⽽不是在编译时进⾏。

- 1.1 加载


Boot Strap类加载器，Extension类加载器和Application（类加载器是实现类加载过程的三个类加载 器。

- (1) Boot Strap类加载器：负责从引导类路径加载类，除了rt.jar，它具有最⾼优先级；

- (2) Extension 类加载器：负责加载ext⽂件夹（jre \ lib）中的类；

- (3) Application类加载器：负责加载应⽤程序级类路径，环境变量中指定的路径等信息。 上⾯的类装载器在加载类⽂件时遵循委托层次算法（Delegation Hierarchy Algorithm）。


- 1.2 链接

- (1) 验证（Verify）：字节码验证器将验证⽣成的字节码是否正确，如果验证失败，将提示验证错误；

- (2) 准备（Prepare）：对于所有静态变量，内存将会以默认值进⾏分配；

- (3) 解释（Resolve）：有符号存储器引⽤都将替换为来⾃⽅法区（Method Area）的原始引⽤。


- 1.3 初始化 这是类加载的最后阶段，所有的静态变量都将被赋予原始值，并且静态区块将被执⾏。


#### 2. 运⾏时数据区

运⾏时数据区可分为5个主要组件：

- (1) ⽅法区（Method Area）：所有的类级数据将存储在这⾥，包括静态变量。每个JVM只有⼀个⽅法 区，它是⼀个共享资源；

- (2) 堆区域（Heap Area）：所有对象及其对应的实例变量和数组将存储在这⾥。每个JVM也只有⼀个 堆区域。由于⽅法和堆区域共享多个线程的内存，所存储的数据不是线程安全的；

- (3) 堆栈区（Stack Area）：对于每个线程，将创建单独的运⾏时堆栈。对于每个⽅法调⽤，将在堆栈 存储器中产⽣⼀个条⽬，称为堆栈帧。所有局部变量将在堆栈内存中创建。堆栈区域是线程安全的， 因为它不共享资源。堆栈框架分为三个⼦元素：

- (4) PC寄存器（PC Registers）：每个线程都有单独的PC寄存器，⽤于保存当前执⾏指令的地址。⼀ 旦执⾏指令，PC寄存器将被下⼀条指令更新；

- (5) 本地⽅法堆栈（Native Method stacks）：本地⽅法堆栈保存本地⽅法信息。对于每个线程，将 创建⼀个单独的本地⽅法堆栈。


局部变量数组（Local Variable Array）：与⽅法相关，涉及局部变量，并在此存储相应的值 操作数堆栈（Operand stack）：如果需要执⾏任何中间操作，操作数堆栈将充当运⾏时⼯作空间 来执⾏操作 帧数据（Frame Data）：对应于⽅法的所有符号存储在此处。在任何异常的情况下，捕获的区块信 息将被保持在帧数据中；

#### 3 执⾏引擎

分配给运⾏时数据区的字节码将由执⾏引擎执⾏，执⾏引擎读取字节码并逐个执⾏。

- (1) 解释器：解释器更快地解释字节码，但执⾏缓慢。解释器的缺点是当⼀个⽅法被调⽤多次时，每次 都需要⼀个新的解释；

- (2) JIT编译器：JIT编译器消除了解释器的缺点。执⾏引擎将在转换字节码时使⽤解释器的帮助，但是 当它发现重复的代码时，将使⽤JIT编译器，它编译整个字节码并将其更改为本地代码。这个本地代码 将直接⽤于重复的⽅法调⽤，这提⾼了系统的性能。JIT的构成组件为：


中间代码⽣成器（Intermediate Code Generator）：⽣成中间代码

代码优化器（Code Optimizer）：负责优化上⾯⽣成的中间代码 ⽬标代码⽣成器（Target Code Generator）：负责⽣成机器代码或本地代码 分析器（Proﬁler）：⼀个特殊组件，负责查找热点，即该⽅法是否被多次调⽤；

- (3) 垃圾收集器(Garbage Collector)：收集和删除未引⽤的对象。可以通过调⽤“System.gc（）”触发 垃圾收集，但不能保证执⾏。JVM的垃圾回收对象是已创建的对象。 Java本机接⼝（JNI）：JNI将与本机⽅法库进⾏交互，并提供执⾏引擎所需的本机库。 本地⽅法库（Native Method Libraries）：它是执⾏引擎所需的本机库的集合。


