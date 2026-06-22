- jquery禁⽤a标签⽅法1

$(document).ready(function () {

$("a").each(function () { var textValue = $(this).html(); if (textValue = "X概况"| textValue = "服务导航") {

$(this).cs("cursor", "default"); $(this).atr('href', '#'); /修改<a>的 href属性值为 # 这样状态栏不会显示链接地址 $(this).click(function (event) {

event.preventDefault(); / 如果<a>定义了 target="_blank“ 需要这句来阻⽌打开新⻚⾯ });

}

});

});

- jquery禁⽤a标签⽅法2

$('a.toltip').live('click', function(event) {

alert("抱歉,已停⽤！");

event.preventDefault();

});

- jquery禁⽤a标签⽅法3


$(function(){

$('.disableCs').removeAtr('href');/去掉a标签中的href属性

$('.disableCs').removeAtr('onclick');/去掉a标签中的onclick事件

}); jquery控制按钮的禁⽤与启⽤ 控制按钮为禁⽤：

查看源代码打印帮助

$('#buton').atr('disabled',"true");添加disabled属性

$('#buton').removeAtr("disabled"); 移除disabled属性

