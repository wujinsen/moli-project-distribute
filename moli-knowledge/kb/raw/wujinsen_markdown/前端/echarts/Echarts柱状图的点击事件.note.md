Echarts柱状图的点击事件

最近在做⼀些图表统计的功能，⽤到了百度的开源图表软件Echatrs，不得不提的是： 不但上⼿简单⽽且扩展功能也是⼗分强⼤。在使⽤的过程中也遇到了不少问题，可能由于 有关Echatrs的资料并不是很⻬全，所以查找资料的过程也是相当曲折的，所以还是⾃⼰就 遇到的问题总结⼀下吧。

点击柱状图跳转⻚⾯的功能：

找到你的⽣成Option事件的⽅法，在其下⾯添加以下代码。 var chart = ec.init(document.getElementById(id)); chart.setOption(Option);

//下⾯是需要添加的⽅法内容 //点击柱状图跳转相应⻚⾯的功能，其中param.name参数为横坐标的值 var ecConfig = require('echarts/config'); function eConsole(param) {

if (typeof param.seriesIndex != 'undefined') { switch (param.name) {

case "新浪": window.location.href = "http://www.sina.com"; window.open("http://www.sina.com", "_blank");//在新⻚⾯打开 break; case "百度": window.location.href = "http://www.baidu.com"; break; case "腾讯": window.location.href = "http://www.qq.com"; break;

default:

break; }

}

} chart.on(ecConfig.EVENT.CLICK, eConsole);

以上，可以获取点击事件的参数，实现跳转的功能。

此外param参数包含的内容有： param.seriesIndex：系列序号（series中当前图形是第⼏个图形第⼏个，从0开始计

数）

param.dataIndex：数值序列（X轴上当前点是第⼏个点，从0开始计数） param.seriesName：legend名称 param.name：X轴值 param.data：Y轴值 param.value：Y轴值 param.type：点击事件均为click

