前⾔： 逛开源社区的时候⽆意发现的，⽤了⼀段时间，觉得还可以，特此推荐⼀下。 lombok 提供了简单的注解的形式来 帮助我们简化消除⼀些必须有但显得很臃肿的 java 代码。特别是相对于 POJO，光说不做不是我的⻛格，先来看看吧。 lombok 的官⽅⽹址：

htp:/projectlombok.org/

lombok 其实到这⾥我就介绍完了，开个玩笑，其实官⽹上有 lombok 三分四⼗九秒的视频讲解，⾥⾯ 讲的也很清楚了，⽽且还有⽂档可以参考。 在这⾥我就不扯太多，先来看⼀下 lombok 的安装，其实这个官⽹视频上也有讲到啦

lombok 安装 使⽤ lombok 是需要安装的，如果不安装，IDE 则⽆法解析 lombok 注解。先在官⽹下载最新版本的 JAR 包， 现在是 0.1.2 版本，我⽤的是 0.1.0 第⼀次使⽤的时候我下载的是最新版本的，也就是我现在⽤的 0.1.0，到现在已经更新 了两个版本，更新的好快啊 . .1. 双击下载下来的 JAR 包安装 lombok 我选择这种⽅式安装的时候提示没有发现任何 IDE，所以我没安装成功，我是⼿动安装的。如果你想以这种⽅式安装，请参考官⽹的视频。2.eclipse / myeclipse ⼿动安装 lombok 1. 将 lombok.jar 复制到 myeclipse.ini / eclipse.ini 所在的⽂件夹⽬录下 2. 打开 eclipse.ini / myeclipse.ini，在最 后⾯插⼊以下两⾏并保存： -Xbotclaspath/a:lombok.jar -javagent:lombok.jar 3.重启 eclipse / myeclipselombok 注解：

lombok 提供的注解不多，可以参考官⽅视频的讲解和官⽅⽂档。 Lombok 注解在线帮助⽂档： 下⾯介绍⼏个我常⽤的 lombok 注解：

htp:/projectlombok.org/features/index.

@Data ：注解在类上；提供类所有属性的 geting 和 seting ⽅法，此外还提供了equals、 canEqual、hashCode、toString ⽅法

@Seter：注解在属性上；为属性提供 seting ⽅法

@Geter：注解在属性上；为属性提供 geting ⽅法

@Log4j ：注解在类上；为类提供⼀个 属性名为log 的 log4j ⽇志对象

@NoArgsConstructor：注解在类上；为类提供⼀个⽆参的构造⽅法

@AlArgsConstructor：注解在类上；为类提供⼀个全参的构造⽅法

下⾯是简单示例

1.不使⽤ lombok 的⽅

案 1 2publicclas Person { 3 4 private String id; 5 private String name; 6 private String identity; 7 private Loge r log = Loger.getLoger(Person.clas); 8 9 public Person() {10 1 }12 13 public Person(String id, String n ame, String identity) {14 this.id

= id;15 this.name = name;16 this.identity = identity;17 }18 19 public String getId() {20 return id;2 1 } 2 23 public String getName() {24 return name;25 }26 27 public String getIdentity() {28 return id entity;29 }30 31 publicvoid setId(String id) {32 this.id = id; 3 }34 35 publicvoid setName(String name) {36 this.name = name;37 }38 39 publicvoid setIdentity(String identity) {40 this.identity = identity;41 } 42}43 2.使⽤ lombok 的⽅

案 1 2@Data 3@Log4j 4@NoArgsConstructor 5@AlArgsConstructor 6publicclas Person { 7 8 private String id; 9 private String name;10private String identity;1 12}13上⾯的两个 java 类，从作⽤上来看，它们的效果是⼀

样的，相⽐较之下，很明显，使⽤ lombok 要简洁许多，特别是在类的属性较多的情况下，同时也避免 了修改字段名字时候忘记修改⽅法名所犯的低级错误。最后需要注意的是，在使⽤ lombok 注解的时候 记得要导⼊ lombok.jar 包到⼯程

