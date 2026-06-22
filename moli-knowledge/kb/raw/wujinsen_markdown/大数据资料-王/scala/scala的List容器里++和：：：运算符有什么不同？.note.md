我看了官⽅⽂档 ，我的理解是： +是new了⼀个List然后把左右的操作 数(两个List)各⾃copy了⼀份放到new List⾥； :似乎是原地操作没有开辟新空间 .我不确定是不是这 样，Wan望⼤家不吝赐教 分享

Scala Standard Library 2.1.3

添加评论

Leo Liang chenying

，學習 Scala 中知乎⽤户、知乎⽤户、 赞同编程类问题还是到 StackOverflow 去问⽐较靠谱。我搜索了⼀下，就找到了跟你问的⼀

Scala list concatenation, : vs +

样的问题，并且有⾼票答案：

简单的说，两个算符（其实是函数）的效果是⼀模⼀样的。 :是 List 专有的函数，后来Scala 的 colection API 重新设计，加⼊了 +函数， +定义在 trait TraversableLike 中，更加通⽤，但是 List 中 已经存在的 :是不能删除的，要保留兼容性。

