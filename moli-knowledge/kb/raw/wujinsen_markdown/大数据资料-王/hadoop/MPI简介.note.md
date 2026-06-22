MPI（Message Passing Interface）是消息传递并⾏程序设计的标准之⼀，当前通⽤的是MPI1.1规范。正在制定的MPI2.0规范除⽀持消息传 递外，还⽀持MPI的I/O规范和进程管理规范。MPI正成为并⾏程序设计事实上的⼯业标准。

MPI的实现包括MPICH、LAM、IBM MPL等多个版本，最常⽤和稳定的是MPICH，曙光天潮系列 的MPI以MPICH为基础进⾏了定制和优化。

MPICH含三层结构，最上层是MPI的API，基本是点到点通信，和在点到点通信基础上构造的集群 通信（Colective Comunication）；中间层是ADI层（Abstract Device Interface），其中device可 以简单地理解为某⼀种底层通信库，ADI就是对各种不同的底层通信库的不同接⼝的统⼀标准；底层是 具体的底层通信库，例如⼯作站机群上的p4通信库、曙光1 0上的NX库、曙光3 0上的BCL通信库 等。

MPICH的1.0.12版本以下都采⽤第⼀代ADI接⼝的实现⽅法，利⽤底层device提供的通信原语和有 关服务函数实现所有的ADI接⼝，可以直接实现，也可以依靠⼀定的模板间接实现。⾃1.0.13版本开 始，MPICH采⽤第⼆代ADI接⼝。

我们将MPICH移植到曙光3 0⾼效通信库BCL(Basic Comunication Library)上(简称 MPI_BCL)。MPI_BCL的接⼝标准与MPICH版本1.1完全⼀致，满⾜MPI1.1标准。同时，也⽀持ch_p4的 通信库，即利⽤TCP/IP通信机制。从⽹络硬件⻆度说，MPI_BCL针对系统⽹络，MPI_ch_p4针对⾼速 以太⽹。

- 1.MPI的程序设计 MPI1.1标准基于静态加载，即所有进程在加载完以后就全部确定，直⾄整个程序结束才终⽌，在程

序运⾏期间没有进程的创建和结束。⼀个MPI程序的所有进程形成⼀个缺省的组，这个组被MPI预先规 定的Comunicator MPI_COM_WORLD所确定。

MPI环境的初始化和结束流程如下：在调⽤MPI例程之前，各个进程都应该执⾏MPI_INIT，接着调 ⽤MPI_COM_SIZE获取缺省组(group)的⼤⼩，调⽤MPI_COM_RANK获取调⽤进程在缺省组中的 逻辑编号（从0开始）。然后，进程可以根据需要，向其它节点发送消息或接收其它节点的消息，经常 调⽤的函数是MPI_SEND和MPI_RECV。最后，当不需要调⽤任何MPI例程后，调⽤MPI_FINALIZE消 除MPI环境，进程此时可以结束，也可以继续执⾏与MPI⽆关的语句。

上⾯提到的六个函数：MPI_INIT，MPI_COM_SIZE，MPI_COM_RANK，MPI_SEND， MPI_RECV，MPI_FINALIZE 实际上构成了编写⼀个完整的MPI程序所需例程的最⼩集。

- 2.MPI的⼏个重要特征 下⾯分别介绍MPI的⼏个重要特征：Comunicator（通信空间）、Group（进程组）、


Context_id（上下⽂标识）、Data Types（数据类型）。 MPI提供Comunicator来指定通信操作的上下⽂，提供了通信操作的执⾏空间。在某个通信空间（或 上下⽂）中发送的消息必须在相同的空间中接收，不同空间中的消息互不⼲扰。定义⼀个 Comunicator，也就指定了⼀组共享该空间的进程，这些进程组成了该Comunicator的Group。

Comunicator通过其特征属性Context_id来区分，同⼀个进程不同的Comunicator有不同的 Context_id。因此Context_id是另⼀个区分消息的标志。

MPI引⼊消息的Data Type属性的⽬的有两个：⼀是⽀持异构系统计算；⼆是允许消息来⾃不连续 的或类型不⼀致的存储区，例如，可以传送数组的⼀列，或传送⼀个结构值，⽽该结构的每个元素的 类型不同。Data Types定义了消息中不连续的数据项及其可能不同的数据类型。Data Type由应⽤程序 在执⾏时通过基本的数据类型创建。

- 3.消息 ⼀个消息相当于⼀封信，消息内容相当于信本身，消息的接收者相当于信封上的内容。因此通常

将前者称为消息的bufer, 后者称为消息的envelop。 bufer: mesage adres, count, datatype; envelop: proces id, mesage tag,comunicator 在MPI以前的⼤多数通信系统中，消息bufer通常仅由bufer的地址和⻓度决定（例如曙光1 0上

的NX通信系统），那么在MPI的消息格式中为什么要引⼊Data Type呢？这有两个主要原因：

⽀持异构计算：不同系统有不同的数据表示。解决这⼀问题的⽅法是预先定义⼀些基本数据类 型，MPI实现过程中对这些类型进⾏转换，例如转换为XDR格式，接收时进⾏反转。

派⽣的数据类型（Derived Data Types）：允许消息来⾃于不连续的和类型不⼀致的存储区域。

- 4.MPI应⽤程序的编译 Include⽂件


C语⾔应⽤程序应有 #include "mpi.h" 若使⽤ c编译，命令⾏应有：

- -I/cluster/mpi/net/include (net版)
- -I/cluster/bcl/include -I/cluster/rms/include -I/cluster/sdr/include -


I/cluster/mpi/mesh/include (mesh版) Fortran语⾔应⽤程序应有 include 'mpif.h' 若使⽤f7编译, 命令⾏应有：

- -I/cluster/mpi/net/include (net版)
- -I/cluster/bcl/include -I/cluster/rms/include -I/cluster/sdr/include -


I/cluster/mpi/mesh/include (mesh版) MPI库⽂件 C语⾔

C语⾔程序编译时需作下述链接：

- -L/cluster/mpi/net/lib -lmpi -lbsd (net版)
- -L/cluster/mpi/mesh/lib -L/cluster/bcl/lib -L/cluster/rms/lib -L/cluster/sdr/lib -lmpi -lbcl -


lrms -lsdr (mesh版)

数学函数库还应链接： -lm Fortran语⾔

Fortran编译时应作下述链接：

- -L/cluster/mpi/net/lib -lmpi -lbsd (net版)
- -L/cluster/mpi/mesh/lib -L/cluster/bcl/lib -L/cluster/rms/lib -L/cluster/sdr/lib -lmpi -lbcl -


lrms -lsdr (mesh版) mpif7和mpi c

MPI提供了两个⼯具(mpif7和mpi c)来简化MPI应⽤程序的编译。⽤户可以直接地使⽤命令⾏⽅

式mpi c或mpif7来编译C或Fortran程序，编译⽅式与 c和f7完全⼀致。如： mpif7 -c fo.f mpi c -c fo.c mpif7 -o fo fo.o mpi c -o fo fo.o

有时链接时需⼀些特殊库, 应在链接时注明。使⽤mpi c和mpif7省略了有关MPI的路径设置。

- 5.MPI应⽤程序的运⾏ 应⽤程序编译好后，使⽤mpirun命令运⾏MPI应⽤程序。mpirun命令完整的格式如下：


mpirun [-h|-?|-help] [-sz size|-sz hXw] [-np nprocs] [-pl polname] <progname [argument]>

各个选项的值由⽤户从命令⾏中显示地指定，选项的含义如下：

- -h
- -?
- -help：显示帮助信息。
- -sz <size| hXw> 指定物理节点的数⽬。有两种指定形式，⼀是直接指定size值，另⼀种是指定物理节点的矩形域的


⻓和宽。size值和h*w的值如果超过所在pol的节点数，sz项的值取pol的节点数，h*w值取整个 pol。两者的缺省值分别为所在pol的节点数和整个pol。

-np <nprocs> ⽤户期望运⾏的进程数。进程数与实际申请的物理节点数没有任何联系，因为允许⼀个节点上运

⾏同⼀个应⽤的多个进程。如果未指定，取实际sz项的值。

-pl polname 应⽤程序执⾏的pol。应⽤程序的每次执⾏能且只能在⼀个pol中执⾏。缺省值为系统为⽤户设

置的缺省的pol名（每个⽤户在创建时已⾃⾏指定或系统分配了⼀个缺省的pol）。

在运⾏选项后，是⽤户的程序名。该可执⾏⽂件必须在所指定的或缺省的pol中的所有节点上能 找到，并且与启动节点上的路径⼀致。⽤户程序名后的⼀切字符串都视为其参数（不包括被shel解释 的重定向等，对shel解释的⼀些特殊字符，如需作为参数，应作相应的转换）。因此运⾏选项与⽤户 程序名有先后的顺序，先运⾏选项，后⽤户程序名和参数。

