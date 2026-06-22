# Unsupported major.minor version 51.0解决办法

www.111cn.net 编辑:swteen 来源:转载

我使⽤的是Eclipse-jee-indigo + JDK 1.6.23环境，结果使⽤时出现Unsupported major.minor version 51.0错误提 ⽰，下⾯我来介绍Unsupported major.minor version 51.0错误的解决办法

今天偶然间同事遇到⼀个问题，也加深了⾃⼰对eclipse中build path和java compiler compliance level的理 解。

解决Unsupported major.minor version 51.0错误

最近新安装使⽤了jdk7，编译了⼀些类替换到原来正常运⾏的项⽬中，替换之后发⽣了Unsupported major.minor version 51.0错误。经过⽹上搜索发现了问题产⽣的原因：⽤jdk7编译的class⽂件放到基于 jdk6运⾏在tomcat之中，就会报这个错。 解决起来也很⽅便：打开exclipse中项⽬上的属性—java compiler–选择⼀个合适的版本后重新编译即 可。 具体步骤 解决：项⽬------>右键------>属性------>Java Compiler------>Compiler Compliance Level------>选择你使⽤ 的JDK版本------>应⽤。

![image 1](<Unsupported major.minor version 51.0解决办法.note_images/imageFile1.png>)

## 总结：不同的JDK版本使⽤的major.minor不同，所以会导致这个错误。在项⽬中要使⽤当前电脑配置 的JDK版本，切忌张冠李戴。

