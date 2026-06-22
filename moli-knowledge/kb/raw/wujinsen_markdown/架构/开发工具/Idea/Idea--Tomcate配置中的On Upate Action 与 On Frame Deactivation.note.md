这两个选项的设置，依赖于 项⽬的部署⽅式 是 exploded

# 1、on update action

⾸先来看 on update action 相关的解释，从字⾯上理解 就是 ⼿⼯触发 update 动作的时候 做什么：

![image 1](<Idea--Tomcate配置中的On Upate Action 与 On Frame Deactivation.note_images/imageFile1.png>)

307031-2017021720315035-19217713.png

![image 2](<Idea--Tomcate配置中的On Upate Action 与 On Frame Deactivation.note_images/imageFile2.png>)

307031-2017021720530160-140 6569.png

update resources ---- 更新静态的资源，⽐如html,js,css等 运⾏模式和调试模式都是⽴即⽣效。 update classes and resources ---- 更新java,jsp和静态资源（ 1. java 修改后，会被编译成.class 然后覆盖到target/kao⽂件夹下，IDE调试 模式的情况下，⽴即⽣效。IDE运⾏模式下，不⽴即⽣效，需要redeployed才可⽣效。 2. jsp修改后，再次被访问的时候， 会⾃动更新，重新编译成.java---->.class 保存在tomcat的work⽬录下。由于是访问时才检测是否修改，是否需要重新编 译，所以 IDE 运⾏模式 和 IDE调试模式下，都是⽴即⽣效。刷新下⻚⾯就可）； redeployed ----- 重新部署，发布到 tomcat⾥，不重启tomcat，⽽是把原来的删掉，然后重新发布； restart server ----- 重启tomcat ----------------------

--------------------------------------------------------------------------------------------------

# 2、on frame deactivation

再来看on frame deactivation ，意思是 IDE 失活时 做什么，就是说 IDE 失去焦点时 做什么。

![image 3](<Idea--Tomcate配置中的On Upate Action 与 On Frame Deactivation.note_images/imageFile3.png>)

307031-201702172070932-151294650.png

Do nothing --------- 什么都不做update resources ---- 更新静态的资源，⽐如html,js,css等 运⾏模式和调试模式都 是⽴即⽣效。 update classes and resources ---- 更新java,jsp和静态资源 同上。 验证上⽅的结论看如下GIF： 静态 资源不⽤测试了，测试 On Update Action 的 （update classes and resources）在debug模式 与 运⾏模式下的差 别。 1.运⾏模式下，jsp是 update classes and resources 是⽴即⽣效的，但是.java⽂件修改后 并不会⽴即⽣效。

![image 4](<Idea--Tomcate配置中的On Upate Action 与 On Frame Deactivation.note_images/imageFile4.png>)

307031-20170217203430707-12898345.gif

2.debug调试模式下，jsp和java⽂件修改 都会⽴即⽣效：

![image 5](<Idea--Tomcate配置中的On Upate Action 与 On Frame Deactivation.note_images/imageFile5.png>)

307031-20170217170752925-192934185.gif

3.测试on frame deactivation 即IDE 失去焦点时⾃动做⼀些事， 我们测试 让其update classes and resources ，以调 试模式为例，运⾏模式除了java 修改不会⾃动⽣效，其它⼀致；

![image 6](<Idea--Tomcate配置中的On Upate Action 与 On Frame Deactivation.note_images/imageFile6.png>)

307031-201702172052614-27480149.gif

## 四、总结：

总结，这样设置是最佳的选择：

![image 7](<Idea--Tomcate配置中的On Upate Action 与 On Frame Deactivation.note_images/imageFile7.png>)

307031-20170217210601816-78672728.png

因为On frame deactivation IDE失去焦点的情况下 ⾃动触发，⽽开发过程中 可能需要查询资料 或 与⼈聊天 或⼲其它 事，IDE需要不停的失去焦点。每次失去焦点就⾃动触发 update ，CPU⼀下⼦ 费很多，电脑瞬间变慢，所以没有必要这 样，设置为Do nothing 最好，官⽅也默认的是 Do nothing。 On Update action ------- update classes and

resources ----- 运⾏模式下（jsp ⽴即⽣效，java 需要redeploy才可⽣效） On Update action ------- update classes and resources ----- 调试模式下（java、jsp 都⽴即⽣效） =================完结了！！！！！！！！这块 ⾮常灵活，这篇博客研究了⼀天 才写出来，很多时候 不知道 如何组织 如何写，才能更加 简单明了。

