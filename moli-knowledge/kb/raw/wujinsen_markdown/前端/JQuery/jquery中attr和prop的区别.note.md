在⾼版本的jquery引⼊prop⽅法后，什么时候该⽤prop？什么时候⽤attr？它们两个之间有什么区别？这些问题就出现了。 关于它们两个的区别，⽹上的答案很多。这⾥谈谈我的⼼得，我的⼼得很简单：

对于HTML元素本身就带有的固有属性，在处理时，使⽤prop⽅法。 对于HTML元素我们⾃⼰⾃定义的DOM属性，在处理时，使⽤attr⽅法。

上⾯的描述也许有点模糊，举⼏个例⼦就知道了。

<a href="http://www.baidu.com" target="_self" class="btn">百度</a>

这个例⼦⾥<a>元素的DOM属性有“href、target和class"，这些属性就是<a>元素本身就带有的属性，也是W3C标准⾥就 包含有这⼏个属性，或者说在IDE⾥能够智能提示出的属性，这些就叫做固有属性。处理这些属性时，建议使⽤prop⽅法。

<a href="#" id="link1" action="delete">删除</a>

这个例⼦⾥<a>元素的DOM属性有“href、id和action”，很明显，前两个是固有属性，⽽后⾯⼀个“action”属性是我们⾃⼰ ⾃定义上去的，<a>元素本身是没有这个属性的。这种就是⾃定义的DOM属性。处理这些属性时，建议使⽤attr⽅法。使⽤ prop⽅法取值和设置属性值时，都会返回undefined值。

再举⼀个例⼦：

- <input id="chk1" type="checkbox" />是否可⻅

- <input id="chk2" type="checkbox" checked="checked" />是否可⻅ 像checkbox，radio和select这样的元素，选中属性对应“checked”和“selected”，这些也属于固有属性，因此需要使⽤ prop⽅法去操作才能获得正确的结果。


- $("#chk1").prop("checked") == false

- $("#chk2").prop("checked") == true 如果上⾯使⽤attr⽅法，则会出现：


- $("#chk1").attr("checked") == undefined

- $("#chk2").attr("checked") == "checked" 全⽂完。


