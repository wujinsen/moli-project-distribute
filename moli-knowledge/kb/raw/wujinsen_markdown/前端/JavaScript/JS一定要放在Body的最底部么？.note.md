# JS⼀定要放在Body的最底部么？

时间 2016-01-12 02:00:02

写代码的⼩德⼦

http://delai.me/code/js-and-performance/ JavaScript CSS

### 原 ⽂

主题

⼀、从⼀个⾯试题说起

⾯试前端的时候我喜欢问⼀些看上去是常识的问题。⽐如：为什么⼤家普遍把 <script src=""> </script> 这样的代码放在body最底部？ （ 为了 沟 通 效 率 ， 我 会 提 前 和 对 ⽅ 约 定 所 有 的 讨论 都 以 chrome为例 ） 应聘者⼀般会回答：因为浏览器⽣成Dom树的时候是⼀⾏⼀⾏读HTML代码的，script标签放在最后 ⾯就不会影响前⾯的⻚⾯的渲染。 我很鸡贼地接着问：既然Dom树完全⽣成好后⻚⾯才能渲染出来，浏览器⼜必须读完全部HTML才能 ⽣成完整的Dom树，script标签不放在body底部是不是也⼀样？ 留 ⼀ 段 空 ⽩ 让 你 先 想 ⼀ 想 “⻚⾯渲染出来了” 指的是什么？ 严格来说，我的最后⼀问是有歧义的：我们需要统⼀⼀下什么叫我们经常挂在嘴边的“⻚⾯渲染出来 了” —— 指的是是 “⾸屏显示出来了” 还是 “⻚⾯完整地加载好了”（后⾯统称StepC） ？如果指的是 ⾸屏显示出来了，那么问题⼜来了：假设⽹⻚⾸屏有图⽚，这⾥的“⾸屏” 指的是 “显示了全部图⽚的 ⾸屏”（后⾯统称StepB） 还是 “没有图⽚的⾸屏”（后⾯统称StepA）。 确定清楚 “⻚⾯渲染出来了” 指的是 StepA、StepB、StepC 中的哪⼀个是⾮常关键的（虽然⾄今还 没有⼀个应聘者尝试这么做过），如果 “⻚⾯渲染出来了” 指的是 StepC，那么我的最后⼀问的答案 是肯定的——script标签不放在body底部不会拖慢⻚⾯完整地加载好的时间。 显然，我们往往更关⼼⾸屏时间，所以，如果 “⻚⾯渲染出来了” 特指“没有图⽚的⾸屏”，那我的最后 ⼀问变成了下⾯这样，⼜该如何回答呢？ 既然Dom树完全⽣成好后才能显示“没有图⽚的⾸屏”，浏览器⼜必须读完全部HTML才能⽣成完整的 Dom树，script标签不放在body底部是不是也⼀样？ 陷阱 然⽽上⾯的问题还是存在⼀个陷阱—— 既然Dom树完全⽣成好后才能显示“没有图⽚的⾸屏” 这句话是带欺骗性 的，“没有图⽚的⾸屏”并不以“完整的Dom树”为必要条件。也就是说： 在⽣成Dom树的过程中只要 某些条件具备了，“没有图⽚的⾸屏”就能显示出来。 所以，抛开这些歧义和陷阱，我的问题变成了： script标签的位置会影响⾸屏时间么？ 然⽽答案并不是那么显⽽易⻅，这得从浏览器的渲染机制说起。 （ 再 ⼀ 次 说 明 ： 本 ⽂ 所 说 的 浏 览 器 都 是 指 chrome）

⼆、浏览器的渲染机制

⾸先，我们需要了解⼏个概念：

- 1、 ：Document Object Model，浏览器将HTML解析成树形的数据结构，简称DOM。


DOM

- 2、 ：CSS Object Model，浏览器将CSS代码解析成树形的数据结构。

- 3、DOM 和 CSSOM 都是以 Bytes → characters → tokens → nodes → object model. 这样的⽅式 ⽣成最终的数据。如下图所示： DOM树的构建过程是⼀个深度遍历过程：当前节点的所有⼦节点都构建好后才会去构建当前节点的下 ⼀个兄弟节点。

- 4、 ：DOM 和 CSSOM 合并后⽣成 Render Tree，如下图： Render Tree 和DOM⼀样，以多叉树的形式保存了每个节点的css属性、节点本身属性、以及节点的 孩⼦节点。 注意：display:none 的节点不会被加⼊Render Tree，⽽visibility: hidden 则会，所以，如果某个 节点最开始是不显示的，设为display:none是更优的。具体可以看 浏览器的渲染过程：


CSSOM

Render Tree

这⾥

- 1、 Create/Update DOM And request css/image/js ：浏览器请求到HTML代码后，在⽣ 成DOM的最开始阶段（应该是 Bytes → characters 后），并⾏发起css、图⽚、js的请求，⽆论他 们是否在HEAD⾥。 注意：发起js⽂件的下载request并不需要DOM处理到那个script节点，⽐如：简单的正则匹配就能做 到这⼀点，虽然实际上并不⼀定是通过正则：）。这是很多⼈在理解渲染机制的时候存在的误区

- 2、 Create/Update Render CSSOM ：CSS⽂件下载完成，开始构建CSSOM

- 3、 Create/Update Render Tree ：所有CSS⽂件下载完成，CSSOM构建结束后，和 DOM ⼀ 起⽣成 Render Tree。

- 4、 Layout ：有了Render Tree，浏览器已经能知道⽹⻚中有哪些节点、各个节点的CSS定义以及 他们的从属关系。下⼀步操作称之为 Layout ，顾名思义就是计算出每个节点在屏幕中的位置。

- 5、 Painting ：Layout后，浏览器已经知道了哪些节点要显示（which nodes are visible）、每 个节点的CSS属性是什么（their computed styles）、每个节点在屏幕中的位置是哪⾥ （geometry）。就进⼊了最后⼀步： Painting ，按照算出来的规则，通过显卡，把内容画到屏幕 上。 以上五个步骤前3个步骤之所有使⽤ “Create/Update” 是因为DOM、CSSOM、Render Tree都可能 在第⼀次Painting后⼜被更新，⽐如JS修改了DOM或者CSS属性。 Layout 和 Painting 也会被重复执⾏，除了DOM、CSSOM更新的原因外，图⽚下载完成后也需要调 ⽤Layout 和 Painting来更新⽹⻚。 看Timeline，⼀⽬了然 我把扒了⼀段有赞PC⾸⻚的代码到本地，通过Node跑起来。Node作为Server端， 对 /js/jquery.js 做了延时2s返回的处理，并且把 <script src="http://127.0.0.1:8080/js/jquery.js"></script> 放到导航栏的下⾯，结果是这样的：


从上⾯的Timeline我们可以看出：

⾸屏时间和DomContentLoad事件没有必然的先后关系 所有CSS尽早加载是减少⾸屏时间的最关键

js的下载和执⾏会阻塞Dom树的构建，所以script标签放在⾸屏范围内的HTML代码段⾥会可能影 响⾸屏的内容。 普通script标签放在body底部，做与不做async或者defer处理，都不会影响⾸屏时间，但影响 DomContentLoad和load的时间，进⽽影响依赖他们的代码的执⾏的开始时间。

## 三、问题的答案

回到前⾯的问题： script标签的位置会影响⾸屏时间么？ 答案是：不影响但有可能截断⾸屏的内容，使其只显示上⾯⼀部分

## 四、再进⼀步

所以，总算弄清楚这个众所周知的常识了。但设计开发中可能会遇到难以把所有的js放到⻚⾯最底部的 情形。⽐如：你的⻚⾯是分模块来写的，每⼀个模块都有⾃⼰的html、js甚⾄css，当把这些模块凑到 ⼀个⻚⾯中的时候就会出现js⾃然⽽然地出现在HTML中间部分。 我们也遇到了这样的问题，所以就做了⼀个开源项⽬： —— A small loader that load CSS/JS in best way for page performance 简单好⽤。 回答下题⽬中所提到的问题，JS⼀定要放在Body的最底部么？ 如果⽤了Tiny-Loader，JS可以不放 在Body最底部。

Tiny-Loader

