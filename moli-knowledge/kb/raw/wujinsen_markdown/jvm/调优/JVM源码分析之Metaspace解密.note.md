# 概述

metaspace，顾名思义，元数据空间，专⻔⽤来存元数据的，它是jdk8⾥特有的数据结构⽤来替代 perm，这块空间很有⾃⼰的特点，前段时间公司这块的问题太多了，主要是因为升级了中间件所致， 看到⼤家讨论来讨论去，看得出很多⼈对metaspace还是模棱两可，不是很了解它，因此我觉得有必 要写篇⽂章来介绍⼀下它，解开它神秘的⾯纱，当我们再次碰到它的相关问题的时候不会再感到束⼿ ⽆策。 通过这篇⽂章，你将可以了解到

为什么会有metaspace metaspace的组成 metaspace的VM参数 jstat⾥我们应该关注metaspace的哪些值

# 为什么会有metaspace

metaspace的由来⺠间已有很多传说，不过我这⾥只谈我⾃⼰的理解，因为我不是oracle参与这块的开 发者，所以对其真正的由来不怎么了解。 我们都知道jdk8之前有perm这⼀整块内存来存klas等信息，我们的参数⾥也必不可少地会配置-

X PermSize以及-X MaxPermSize来控制这块内存的⼤⼩，jvm在启动的时候会根据这些配置来分配 ⼀块连续的内存块，但是随着动态类加载的情况越来越多，这块内存我们变得不太可控，到底设置多 ⼤合适是每个开发者要考虑的问题，如果设置太⼩了，系统运⾏过程中就容易出现内存溢出，设置⼤ 了⼜总感觉浪费，尽管不会实质分配这么⼤的物理内存。基于这么⼀个可能的原因，于是metaspace 出现了，希望内存的管理不再受到限制，也不要怎么关注元数据这块的 OM问题，虽然到⽬前来看， 也并没有完美地解决这个问题。 或许从JVM代码⾥也能看出⼀些端倪来，⽐如MaxMetaspaceSize默认值很⼤， CompressedClassSpaceSize默认也有1G，从这些参数我们能猜到metaspace的作者不希望出现它相关的

OM问题。

# metaspace的组成

metaspace其实由两⼤部分组成

Klas Metaspace NoKlas Metaspace

Klas Metaspace就是⽤来存klas的，klas是我们熟知的clas⽂件在jvm⾥的运⾏时数据结构，不过 有点要提的是我们看到的类似A.clas其实是存在heap⾥的，是java.lang.Clas的⼀个对象实例。这块 内存是紧接着Heap的，和我们之前的perm⼀样，这块内存⼤⼩可通过-XX:CompressedClassSpaceSize 参数来控制，这个参数前⾯提到了默认是1G，但是这块内存也可以没有，假如没有开启压缩指针就不 会有这块内存，这种情况下klas都会存在NoKlas Metaspace⾥，另外如果我们把-Xmx设置⼤于32G 的话，其实也是没有这块内存的，因为会这么⼤内存会关闭压缩指针开关。还有就是这块内存最多只 会存在⼀块。

NoKlas Metaspace专⻔来存klas相关的其他的内容，⽐如method，constantPol等，这块内存是由 多块内存组合起来的，所以可以认为是不连续的内存块组成的。这块内存是必须的，虽然叫做NoKlas Metaspace，但是也其实可以存klas的内容，上⾯已经提到了对应场景。 Klas Metaspace和NoKlas Mestaspace都是所有clasloader共享的，所以类加载器们要分配内存， 但是每个类加载器都有⼀个SpaceManager，来管理属于这个类加载的内存⼩块。如果Klas Metaspace⽤完了，那就会 OM了，不过⼀般情况下不会，NoKlas Mestaspace是由⼀块块内存慢慢 组合起来的，在没有达到限制条件的情况下，会不断加⻓这条链，让它可以持续⼯作。

# metaspace的⼏个参数

如果我们要改变metaspace的⼀些⾏为，我们⼀般会对其相关的⼀些参数做调整，因为metaspace的 参数本身不是很多，所以我这⾥将涉及到的所有参数都做⼀个介绍，也许好些参数⼤家都是有误解的

UseLargePagesInMetaspace InitialBotClasLoaderMetaspaceSize MetaspaceSize MaxMetaspaceSize CompresedClasSpaceSize MinMetaspaceExpansion MaxMetaspaceExpansion MinMetaspaceFreRatio MaxMetaspaceFreRatio

UseLargePagesInMetaspace

默认false，这个参数是说是否在metaspace⾥使⽤LargePage，⼀般情况下我们使⽤4KB的page size，这个参数依赖于UseLargePages这个参数开启，不过这个参数我们⼀般不开。

InitialBotClasLoaderMetaspaceSize

64位下默认4M，32位下默认 20K，metasapce前⾯已经提到主要分了两⼤块，Klas Metaspace以 及NoKlas Metaspace，⽽NoKlas Metaspace是由⼀块块内存组合起来的，这个参数决定了NoKlas Metaspace的第⼀个内存Block的⼤⼩，即2*InitialBotClasLoaderMetaspaceSize，同时为 botstrapClasLoader的第⼀块内存chunk分配了InitialBotClasLoaderMetaspaceSize的⼤⼩

MetaspaceSize

默认20.8M左右(x86下开启c2模式)，主要是控制metaspaceGC发⽣的初始阈值，也是最⼩阈值，但 是触发metaspaceGC的阈值是不断变化的，与之对⽐的主要是指Klas Metaspace与NoKlas Metaspace两块comited的内存和。

MaxMetaspaceSize

默认基本是⽆穷⼤，但是我还是建议⼤家设置这个参数，因为很可能会因为没有限制⽽导致 metaspace被⽆⽌境使⽤(⼀般是内存泄漏)⽽被OS Kil。这个参数会限制metaspace(包括了Klas Metaspace以及NoKlas Metaspace)被comited的内存⼤⼩，会保证comited的内存不会超过这 个值，⼀旦超过就会触发GC，这⾥要注意和MaxPermSize的区别，MaxMetaspaceSize并不会在jvm 启动的时候分配⼀块这么⼤的内存出来，⽽MaxPermSize是会分配⼀块这么⼤的内存的。

## CompresedClasSpaceSize

默认1G，这个参数主要是设置Klas Metaspace的⼤⼩，不过这个参数设置了也不⼀定起作⽤，前提是 能开启压缩指针，假如-Xmx超过了32G，压缩指针是开启不来的。如果有Klas Metaspace，那这块 内存是和Heap连着的。

## MinMetaspaceExpansion

MinMetaspaceExpansion和MaxMetaspaceExpansion这两个参数或许和⼤家认识的并不⼀样，也许很 多⼈会认为这两个参数不就是内存不够的时候，然后扩容的最⼩⼤⼩吗？其实不然 这两个参数和扩容其实并没有直接的关系，也就是并不是为了增⼤comited的内存，⽽是为了增⼤触 发metaspace GC的阈值 这两个参数主要是在⽐较特殊的场景下救急使⽤，⽐如gcLocker或者should_concurrent_collect的⼀ 些场景，因为这些场景下接下来会做⼀次GC，相信在接下来的GC中可能会释放⼀些metaspace的内 存，于是先临时扩⼤下metaspace触发GC的阈值，⽽有些内存分配失败其实正好是因为这个阈值触顶 导致的，于是可以通过增⼤阈值暂时绕过去 默认 32.8K，增⼤触发metaspace GC阈值的最⼩要求。假如我们要救急分配的内存很⼩，没有达到 MinMetaspaceExpansion，但是我们会将这次触发metaspace GC的阈值提升 MinMetaspaceExpansion，之所以要⼤于这次要分配的内存⼤⼩主要是为了防⽌别的线程也有类似的 请求⽽频繁触发相关的操作，不过如果要分配的内存超过了MaxMetaspaceExpansion，那 MinMetaspaceExpansion将会是要分配的内存⼤⼩基础上的⼀个增量

## MaxMetaspaceExpansion

默认5.2M，增⼤触发metaspace GC阈值的最⼤要求。假如说我们要分配的内存超过了 MinMetaspaceExpansion但是低于MaxMetaspaceExpansion，那增量是MaxMetaspaceExpansion， 如果超过了MaxMetaspaceExpansion，那增量是MinMetaspaceExpansion加上要分配的内存⼤⼩ 注：每次分配只会给对应的线程⼀次扩展触发metaspace GC阈值的机会，如果扩展了，但是还不能分 配，那就只能等着做GC了

## MinMetaspaceFreRatio

MinMetaspaceFreRatio和下⾯的MaxMetaspaceFreRatio，主要是影响触发metaspaceGC的阈值 默认40，表示每次GC完之后，假设我们允许接下来metaspace可以继续被comit的内存占到了被 comit之后总共comited的内存量的MinMetaspaceFreRatio%，如果这个总共被comited的量 ⽐当前触发metaspaceGC的阈值要⼤，那么将尝试做扩容，也就是增⼤触发metaspaceGC的阈值，不 过这个增量⾄少是MinMetaspaceExpansion才会做，不然不会增加这个阈值

这个参数主要是为了避免触发metaspaceGC的阈值和gc之后comited的内存的量⽐较接近，于是将 这个阈值进⾏扩⼤ ⼀般情况下在gc完之后，如果被comited的量还是⽐较⼤的时候，换个说法就是离触发 metaspaceGC的阈值⽐较接近的时候，这个调整会⽐较明显 注：这⾥不⽤gc之后used的量来算，主要是担⼼可能出现comited的量超过了触发metaspaceGC的 阈值，这种情况⼀旦发⽣会很危险，会不断做gc，这应该是jdk8在某个版本之后才修复的bug

MaxMetaspaceFreRatio

默认70，这个参数和上⾯的参数基本是相反的，是为了避免触发metaspaceGC的阈值过⼤，⽽想对这 个值进⾏缩⼩。这个参数在gc之后comited的内存⽐较⼩的时候并且离触发metaspaceGC的阈值⽐ 较远的时候，调整会⽐较明显

# jstat⾥的metaspace字段

我们看GC是否异常，除了通过GC⽇志来做分析之外，我们还可以通过jstat这样的⼯具展示的数据来分 析，前⾯我公众号⾥有篇⽂章介绍了jstat这块的实现，有兴趣的可以到我的公众号你假笨⾥去翻阅下 jstat的这篇⽂章。 我们通过jstat可以看到metaspace相关的这么⼀些指标，分别是M，CCS，MC，MU，CCSC，CCSU，MCMN， MCMX，CCSMN，CCSMX 它们的定义如下：

column { header "^M^" /* Metaspace - Percent Used */ data (1-((sun.gc.metaspace.capacity - sun.gc.metaspace.used)/sun.gc.metaspace.capacity)) * 100 align right width 6 scale raw format "0.00"

} column {

header "^CCS^" /* Compressed Class Space - Percent Used */ data (1-((sun.gc.compressedclassspace.capacity -

sun.gc.compressedclassspace.used)/sun.gc.compressedclassspace.capacity)) * 100 align right width 6 scale raw format "0.00"

}

column { header "^MC^" /* Metaspace Capacity - Current */ data sun.gc.metaspace.capacity align center

width 6 scale K format "0.0"

} column {

header "^MU^" /* Metaspae Used */ data sun.gc.metaspace.used align center

width 6 scale K format "0.0"

}

column { header "^CCSC^" /* Compressed Class Space Capacity - Current */ data sun.gc.compressedclassspace.capacity width 8 align right scale K format "0.0"

} column {

header "^CCSU^" /* Compressed Class Space Used */ data sun.gc.compressedclassspace.used width 8 align right scale K format "0.0"

} column {

header "^MCMN^" /* Metaspace Capacity - Minimum */ data sun.gc.metaspace.minCapacity scale K

align right width 8 format "0.0"

} column {

header "^MCMX^" /* Metaspace Capacity - Maximum */ data sun.gc.metaspace.maxCapacity scale K align right width 8 format "0.0"

} column {

header "^CCSMN^" /* Compressed Class Space Capacity - Minimum */ data sun.gc.compressedclassspace.minCapacity scale K align right width 8 format "0.0"

} column {

header "^CCSMX^" /* Compressed Class Space Capacity - Maximum */ data sun.gc.compressedclassspace.maxCapacity scale K align right width 8 format "0.0"

}

我这⾥对这些字段分类介绍下

MC & MU & CSC & CSU

MC表示Klas Metaspace以及NoKlas Metaspace两者总共comited的内存⼤⼩，单位是KB，虽 然从上⾯的定义⾥我们看到了是capacity，但是实质上计算的时候并不是capacity，⽽是 comited，这个是要注意的 MU这个⽆可厚⾮，说的就是Klas Metaspace以及NoKlas Metaspace两者已经使⽤了的内存⼤⼩

CSC表示的是Klas Metaspace的已经被comit的内存⼤⼩，单位也是KB CSU表示Klas Metaspace的已经被使⽤的内存⼤⼩

M & CS

M表示的是Klas Metaspace以及NoKlas Metaspace两者总共的使⽤率，其实可以根据上⾯的四个 指标算出来，即( CSU+MU)/( CSC+MC)

CS表示的是NoKlas Metaspace的使⽤率，也就是 CSU/ CSC算出来的

PS：所以我们有时候看到M的值达到了90%以上，其实这个并不⼀定说明metaspace⽤了很多了，因 为内存是慢慢comit的，所以我们的分⺟是慢慢变⼤的，不过当我们comited到⼀定量的时候就不 会再增⻓了

MCMN & MCMX & CSMN & CSMX

MCMN和 CSMN这两个值⼤家可以忽略，⼀直都是0

MCMX表示Klas Metaspace以及NoKlas Metaspace两者总共的reserved的内存⼤⼩，⽐如默认 情况下Klas Metaspace是通过CompresedClasSpaceSize这个参数来reserved 1G的内存， NoKlas Metaspace默认reserved的内存⼤⼩是2* InitialBotClasLoaderMetaspaceSize

CSMX表示Klas Metaspace reserved的内存⼤⼩

综上所述，其实看metaspace最主要的还是看MC，MU，CCSC，CCSU这⼏个具体的⼤⼩来判断metaspace 到底⽤了多少更靠谱 本来还想写metaspace内存分配和GC的内容，不过那块说起来⼜是⼀个⽐较⼤的话题，因为那块⼤家 看起来可能会⽐较枯燥，有机会再写

