前⾔：对于Web开发⼈员，弹出框和提示框的使⽤肯定不会陌⽣，⽐如常⻅的表格新增和编辑功能， ⼀般常⻅的主要有两种处理⽅式：⾏内编辑和弹出框编辑。在增加⽤户体验⽅⾯，弹出框和提示框起 着重要的作⽤，如果你的系统有⼀个友好的弹出提示框，⾃然能给⽤户很好的⻚⾯体验。前⾯⼏章介 绍了botstrap的⼏个常⽤组件，这章来看看botstrap⾥⾯弹出框和提示框的处理。总的来说，弹出提 示主要分为三种：弹出框、确定取消提示框、信息提示框。本篇就结合这三种类型分别来介绍下它们 的使⽤。 ⼀、Botstrap弹出框 使⽤过JQuery UI应该知道，它⾥⾯有⼀个dialog的弹出框组件，功能也很丰富。与jQuery UI的dialog 类似，Botstrap⾥⾯也内置了弹出框组件。打开botstrap ⽂档可以看到它的dialog是直接嵌⼊到 botstrap.js和botstrap.cs⾥⾯的，也就是说，只要我们引⼊了botstrap的⽂件，就可以直接使⽤ 它的dialog组件，是不是很⽅便。本篇我们就结合新增编辑的功能来介绍下botstrap dialog的使⽤。 废话不多说，直接看来它如何使⽤吧。

- 1、cshtml界⾯代码


<table>
  <tr>
    <th><div clas="modal fade" id="myModal" tabindex="-1" role="dialog" arialabeledby="myModalLabel"><br><br><div clas="modal-dialog" role="document"> <div clas="modal-content" <div clas="modal-header"><br><br><buton type="buton" clas="close" data-dismis="modal" aria-label="Close"><span ariahi den="true">×</span></buton><br><br><h4 clas="modal-title" id="myModalLabel">新增</h4> /div> <div clas="modal-body"><br><br><div clas="form-group"> <label for="txt_departmentname">部⻔名称</label> <input type="text" name="txt_departmentname" clas="form-control"<br><br>id="txt_departmentname" placeholder="部⻔名称"> /div><br><br><div clas="form-group"> <label for="txt_parentdepartment">上级部⻔</label> <input type="text" name="txt_parentdepartment" clas="form-control"<br><br>id="txt_parentdepartment" placeholder="上级部⻔"> /div><br><br><div clas="form-group"> <label for="txt_departmentlevel">部⻔级别</label> <input type="text" name="txt_departmentlevel" clas="form-control"<br><br>id="txt_departmentlevel" placeholder="部⻔级别"> /div><br><br><div clas="form-group"> <label for="txt_statu">描述</label> <input type="text" name="txt_statu" clas="form-control" id="txt_statu" placeholder="状<br><br>态"><br><br></div> /div><br><br><div clas="modal-foter"><br><br><buton type="buton" clas="btn btn-default" data-dismis="modal"><span clas="glyphicon glyphicon-remove" aria-hi den="true"></span>关闭</buton><br><br><buton type="buton" id="btn_submit" clas="btn btn-primary" data-dismis="modal"> <span clas="glyphicon glyphicon-flopy-disk" aria-hi den="true"></span>保存</buton><br><br></div> </div><br><br></div></th>
  </tr>
</table>


</div>

最外⾯的div定义了dialog的隐藏。我们重点来看看第⼆层的div <div clas="modal-dialog" role="document"> 这个div定义了dialog，对应的clas有三种尺⼨的弹出框，如下： <div clas="modal-dialog" role="document"> <div clas="modal-dialog modal-lg" role="document"> <div clas="modal-dialog modal-ful" role="document">

第⼀种表示默认类型的弹出框；第⼆种表示增⼤的弹出框；第三种表示满屏的弹出框。 role="document"表示弹出框的对象的当前的document。

- 2、js⾥⾯将dialog show出来。 默认情况下，我们的弹出框是隐藏的，只有在⽤户点击某个操作的时候才会show出来。来看看js⾥⾯ 是如何处理的吧：

对，你没有看错，只需要这⼀句就能show出这个dialog. $('#myModal').modal();

- 3、效果展示 新增效果


<table>
  <tr>
    <th>/注册新增按钮的事件 $("#btn_ad").click(function () { $("#myModalLabel").text("新增"); $('#myModal').modal();</th>
  </tr>
</table>


});

![image 1](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile1.png>)

编辑效果

![image 2](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile2.png>)

- 4、说明 弹出框显示后，点击界⾯上其他地⽅以及按Esc键都能隐藏弹出框，这样使得⽤户的操作更加友好。关 于dialog⾥⾯关闭和保存按钮的事件的初始化在项⽬⾥⾯⼀般是封装过的，这个我们待会来看。 ⼆、确认取消提示框 这种类型的提示框⼀般⽤于某些需要⽤户确定才能进⾏的操作，⽐较常⻅的如：删除操作、提交订单 操作等。


- 1、使⽤botstrap弹出框确认取消提示框 介绍这个组件之前，就得说说组件封装了，我们知道，像弹出框、确认取消提示框、信息提示框这些 东⻄项⽬⾥⾯肯定是多处都要调⽤的，所以我们肯定是要封装组件的。下⾯就来看看我们封装的缺乏 取消提示框。 ?


- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9


(function ($) {

window.Ewin = function () { var html = '<div id="[Id]" clas="modal fade"

role="dialog" aria-labeledby="modalLabel">' + ' di las"modal-dialog modal-sm">' +

- ' d clas" odalcontent">' + ' div clas="modal-header">' + '<buton type="buton" clas="close" data-

dismis="modal"><span ariahi den="true">×</span><span clas="sronly">Close</span></buton>' +

'<h4 clas="modal-title" id="modalLabel">

[Title]</h4>' + ' /div>' + ' div clas="modal-body">' + '<p>[Mesage]</p>' + ' /div>' + ' div clas="modal-foter">' + '<buton type="buton" clas="btn btn-default

cancel" data-dismis="modal">[BtnCancel] </buton>' +

'<buton type="buton" clas="btn btn-primary ok" data-dismis="modal">[BtnOk]</buton>'

+ ' ' ' ' ' ' + '</div>';

var dialogdHtml = '<div id="[Id]" clas="modal fade" role="dialog" arialabeledby="modalLabel">' +

- ' di las" odaldialog">' + ' d clas" odalcontent">' + ' div clas="modal-header">' + '<buton type="buton" clas="close" data-


- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


19

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


29

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


39

dismis="modal"><span ariahi den="true">×</span><span clas="sronly">Close</span></buton>' +

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


'<h4 clas="modal-title" id="modalLabel">

[Title]</h4>' + ' /div>' + ' div clas="modal-body">' + ' ' ' ' ' ' + '</div>';

49

- 0
- 1
- 2
- 3
- 4
- 5


r reg = new RegExp("\[([^\[\]*?)\]", 'igm');

ar generateId = function () { var date = new Date(); return 'mdl' + date.valueOf(); } var init = function (options) { options = $.extend({}, {

56

- 7
- 8


title: "操作提示", mesage: "提示内容", btnok: "确定", btncl: "取消", width: 20, auto: false }, options| {});

59

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


ar modalId = generateId(); var content = html.replace(reg, function (node,

key) { return { Id: modalId, Title: options.title, Mesage: options.mesage,

69

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


tnOk: options.btnok, BtnCancel: options.btncl

[key]; });

('body').apend(content); $('#' + modalId).modal({ width: otions.width, backdrop: 'static' }); $('#' + modalId).on('hide.bs.modal', function

79

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


(e) { $('body').find('#' + modalId).remove(); }); return modalId; }

return { alert: function (options) { if (typeof options = 'string') { options = { mesage: options

89

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9 0


; }

id = init(options); var modal = $('#' + id); modal.find('.ok').removeClas('btn-

suces').adClas('btn-primary'); modal.find('.cancel').hide(); return { id: id, on: function (calback) { if (calback & calback instanceof Function) { modal.find('.ok').click(function () {

- 11

- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 09
- 10 1


- 12
- 13
- 14


calback(true); });

}, hide: function (calback) { if (calback & calback instanceof Function) { modal.on('hide.bs.modal', function (e) { calback(e);

); }

- 15
- 16
- 17
- 18
- 19 0


; }, confirm: function (options) {

id = init(options); var modal = $('#' + id); modal.find('.ok').removeClas('btn-

- 11


- 2
- 3
- 4
- 5
- 6
- 7
- 8


primary').adClas('btn-suces'); modal.find('.cancel').show(); return { id: id, on: function (calback) { if (calback & calback instanceof Function) { modal.find('.ok').click(function () {

29

calback(true); });

0 11

modal.find('.cancel').click(function () { calback(false); });

- 2
- 3
- 4
- 5
- 6
- 7
- 8


}, hide: function (calback) { if (calback & calback instanceof Function) { modal.on('hide.bs.modal', function (e) { calback(e);

);

39

0 11

; }, dialog: function (options) { options = $.extend({}, { title: 'title', url: ', width: 80, height: 50, onReady: function () { }, onShown: function (e) { } }, options| {}); var modalId = generateId();

- 2
- 3
- 4
- 5
- 6
- 7
- 8


49

0 11

2 153

var content = dialogdHtml.replace(reg,

function (node, key) { return { Id: modalId, Title: options.title

[key]; }); $('body').apend(content); var target = $('#' + modalId); target.find('.modal-body').load(options.url); if (options.onReady() options.onReady.cal(target); ta tmodal(); target.on('shown.bs.modal', function (e) { if (options.onReady(e) options.onReady.cal(target, e); }); target.on('hide.bs.modal', function (e) { $('body').find(target).remove();

);

}(); })(jQuery);

不了解组件封装的朋友可以先看看相关⽂章。这⾥我们的确认取消提示框主要⽤到了confirm这个属性 对应的⽅法。还是来看看如何调⽤吧：

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br></th>
    <th>/注册删除按钮的事件 $("#btn_delete").click(function () { /取表格的选中⾏数据<br><br>var arselections = $("#tb_departments").botstrapTable('getSelec tions');<br><br>if (arselections.length <= 0) { toastr.warning('请选择有效数据'); return; }<br><br>Ewin.confirm({ mesage: "确认要删除选择的数<br><br>据吗？" }).on(function (e) { if (!e) { return; } $.ajax({ type: "ost", url: "/api/DepartmentApi/Delete", data: {": JSON.stringify(arselections) }, suces: function (data, status) { if (status = "suces") { toastr.suces('提交数据成功');<br><br>$("#tb_departments").botstrapTable('refresh') ;<br><br>}, eror: function () { toastr.eror('Eror'); }, complete: function () {<br><br>}</th>
  </tr>
</table>


});

mesage属性传⼊提示的信息，on⾥⾯注⼊点击按钮后的回调事件。 ⽣成的效果：

![image 3](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile3.png>)

- 2、botbox组件的使⽤ 在⽹上找botstrap的弹出组件时总是可以看到botbox这么⼀个东⻄，确实是⼀个很简单的组件，还 是来看看如何使⽤吧。 当然要使⽤它必须要添加组件喽。⽆⾮也是两种⽅式：引⼊源码和Nuget。


![image 4](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile4.png>)

接下来就是使⽤它了。⾸先当然是添加botbox.js的引⽤了。然后就是在相应的地⽅调⽤了。 ?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br><br><br>18</th>
    <th>$("#btn_delete").click(function () {<br><br>var arselections = $("#tb_departments").botstrapTable('getSelec tions');<br><br>if (arselections.length <= 0) { toastr.warning('请选择有效数据'); return; }<br><br>botbox.alert("确认删除", function () { var strResult ="; }) botbox.prompt("确认删除", function (result) { var strResult = result; }) botbox.confirm("确认删除", function (result) { var strResult = result; })</th>
  </tr>
</table>


});

效果展示：

![image 5](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile5.png>)

![image 6](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile6.png>)

![image 7](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile7.png>)

更多⽤法可以参⻅api。使⽤起来基本很简单。这个组件最⼤的特点就是和botstrap的⻛格能够很好的 保持⼀致。

- 3、在⽹上还找到⼀个效果⽐较炫⼀点的提示框：swetalert


![image 8](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile8.png>)

要使⽤它，还是⽼规矩：Nuget。

- （1）⽂档
- （2）在cshtml⻚⾯引⼊js和cs <link href="~/Styles/swetalert.cs" rel="styleshet" /> <script src="~/Scripts/swetalert.min.js"></script>
- （3）js使⽤


?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29</th>
    <th>swal({ title: "操作提示", /弹出框的title text: "确定删除吗？", /弹出框⾥⾯的提示⽂本 type: "warning", /弹出框类型 showCancelButon: true, /是否显示取消按钮 confirmButonColor: "#D6B5",/确定按钮颜<br><br>⾊ cancelButonText: "取消",/取消按钮⽂本 confirmButonText: "是的，确定删除！",/确定<br><br>按钮上⾯的⽂档 closeOnConfirm: true }, function () { $.ajax({ type: "post", url: "/Home/Delete", data: {": JSON.stringify(arselections) }, suces: function (data, status) { if (status = "suces") { toastr.suces('提交数据成功');<br><br>$("#tb_departments").botstrapTable('refresh') ;<br><br>}, eror: function () { toastr.eror('Eror'); }, complete: function () {<br><br>}</th>
  </tr>
</table>


});

- （4）效果展示：


![image 9](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile9.png>)

点击确定后进⼊回调函数：

![image 10](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile10.png>)

组件很多，⽤哪种园友没可以⾃⾏决定，不过博主觉得像⼀些互联⽹、电⼦商务类型的⽹站⽤ swetalert效果⽐较合适，⼀般的内部系统可能也⽤不上。 三、操作完成提示框

- 1、toastr.js组件 关于信息提示框，博主项⽬中使⽤的是toastr.js这么⼀个组件，这个组件最⼤的好处就是异步、⽆阻 塞，提示后可设置消失时间，并且可以将消息提示放到界⾯的各个地⽅。先来看看效果。


![image 11](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile11.png>)

显示在不同位置： top-center位置

![image 12](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile12.png>)

botom-left位置

![image 13](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile13.png>)

关于它的使⽤。

- （1）、引⼊js和cs
- （2）、js初始化

将这个属性值设置为不同的值就能让提示信息显示在不同的位置，如toast-botom-right表示下右、 toast-botom-center表示下中、toast-top-center表示上中等，更过位置信息请查看⽂档。

- （3）、使⽤


?

<table>
  <tr>
    <th>1<br>2<br></th>
    <th><link href="~/Content/toastr/toastr.cs" rel="styleshet" /><br><br>script src="~/Content/toastr/toastr.min.js"></th>
  </tr>
</table>


</script>

?

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th><script type="text/javascript"><br><br>toastr.options.positionClas = 'toast-botomright';</th>
  </tr>
</table>


</script>

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br><br><br>16</th>
    <th>/初始化编辑按钮 $("#btn_edit").click(function () {<br><br>var arselections = $("#tb_departments").botstrapTable('getSelec tions');<br><br>if (arselections.length > 1) { toastr.warning('只能选择⼀⾏进⾏编辑');<br><br>return; } if (arselections.length <= 0) { toastr.warning('请选择有效数据');<br><br>return; }<br><br>$('#myModal').modal();</th>
  </tr>
</table>


});

使⽤起来就如下⼀句： toastr.warning('只能选择⼀⾏进⾏编辑'); 是不是很简单 ~这⾥的有四种⽅法分别对应四种不同颜⾊的提示框。 toastr.suces('提交数据成功'); toastr.eror('Eror'); toastr.warning('只能选择⼀⾏进⾏编辑'); toastr.info('info'); 分别对应上图中的四种颜⾊的提示框。

- 2、Mesenger组件 在Botstrap中⽂⽹⾥⾯提到了⼀个alert组件：Mesenger。


![image 14](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile14.png>)

它的使⽤和toastr.js这个组件基本相似，只不过效果有点不太⼀样。我们还是来看看它是如何使⽤的。

- （1）效果展示 可以定位到⽹⻚的不同位置，例如下图中给出的下中位置、上中位置。


![image 15](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile15.png>)

![image 16](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile16.png>)

提示框的样式有三种状态：Suces、Eror、Info

![image 17](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile17.png>)

并且⽀持四种不同样式的提示框：Future、Block、Air、Ice

![image 18](<JS组件Bootstrap实现弹出框和提示框效果代码.note_images/imageFile18.png>)

- （2）组件使⽤以及代码示例 关于它的使⽤和toastr⼤同⼩异，⾸先引⼊组件：


?

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th><script src="~/Content/HubSpot-mesengera3df9a6/build/js/mesenger.js"></script><br><br><link href="~/Content/HubSpot-mesengera3df9a6/build/cs/mesenger.cs" rel="styleshet" /><br><br><link href="~/Content/HubSpot-mesengera3df9a6/build/cs/mesenger-theme-</th>
  </tr>
</table>


future.cs" rel="styleshet" />

初始化它的位置

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br></th>
    <th><script type="text/javascript"> $._mesengerDefaults = { extraClases: 'mesenger-fixed mesenger-<br><br>theme-future mesenger-on-botom mesenger-on-right'<br><br>}</th>
  </tr>
</table>


</script>

然后js⾥⾯使⽤如下：

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>$("#btn_delete").click(function () { $.globalMesenger().post({ mesage: "操作成功",/提示信息 type: 'info',/消息类型。eror、info、suces hideAfter: 2,/多⻓时间消失 showCloseButon:true,/是否显示关闭按钮 hideOnNavigate: true/是否隐藏导航 });</th>
  </tr>
</table>


});

如果提示框使⽤默认样式，也只有⼀句就能解决

?

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>$.globalMesenger().post({ mesage: "操作成功",/提示信息 type: 'info',/消息类型。eror、info、suces</th>
  </tr>
</table>


4 });

很简单很强⼤有⽊有 ~ 四、总结 以上花了⼏个⼩时时间整理出来的⼏种常⽤botstrap常⽤弹出和提示框的效果以及使⽤⼩结，希望对 ⼤家的学习有所帮助。

