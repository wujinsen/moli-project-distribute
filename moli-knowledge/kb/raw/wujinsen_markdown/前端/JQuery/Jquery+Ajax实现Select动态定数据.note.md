接着上次的⾼级查询。下拉框中的字段都是在前台写好的。这对于系统的灵活性来说，是很⼤的⼀个 弊端。

解决思路：

在数据库中建⽴类型字典式表。将下拉框需要添加的项，在数据库表⾥中⽂、英⽂名称对应起来。 下拉框动态绑定数据库表中需要字段。

<div id="bgDiv" style="display:none;"></div>

<a class="btn-lit" href="javascript:" onclick="bgDiv.style.display='inline';advancedQuery.style.display='';addItems()"><span>⾼级查 询</span></a>

在⾼级查询单击事件中，除了显示查询框外，添加下拉框绑定字段的函数。此处为addItems().

实现代码：

<script type="text/javascript"> //动态绑定下拉框项

function addItems() {

$.ajax({ url: "addItem.ashx/GetItem", //后台webservice⾥的⽅法名称 type: "post", dataType: "json", contentType: "application/json", traditional: true, success: function (data) {

for (var i in data) { var jsonObj =data[i]; var optionstring = ""; for (var j = 0; j < jsonObj.length; j++) {

optionstring += "<option value=\"" + jsonObj[j].ID + "\" >" +

jsonObj[j].chinesename + "</option>"; } $("#dpdField1").html("<option value='请选择'>请选择...</option>

"+optionstring);

}

}, error: function (msg) {

alert("出错了！"); }

}); };

</script>

后台代码：

public void ProcessRequest(HttpContext context) {

//context.Response.ContentType = "text/plain"; //context.Response.Write("Hello World"); GetItem(context);

} public void GetItem(HttpContext context) {

string ReturnValue = string.Empty; BasicInformationFacade basicInformationFacade = new BasicInformationFacade();

//实例化基础信息外观 DataTable dt = new DataTable(); dt = basicInformationFacade.itemsQuery(); //根据查询条件获取结果 ReturnValue = DataTableJson(dt); context.Response.ContentType = "text/plain"; context.Response.Write(ReturnValue); //return ReturnValue;

}

#region dataTable转换成Json格式 /// <summary> /// dataTable转换成Json格式 /// </summary> /// <param name="dt"></param> /// <returns></returns> public string DataTableJson(DataTable dt) {

StringBuilder jsonBuilder = new StringBuilder(); jsonBuilder.Append("{\""); jsonBuilder.Append(dt.TableName.ToString()); jsonBuilder.Append("\":["); for (int i = 0; i < dt.Rows.Count; i++) {

jsonBuilder.Append("{"); for (int j = 0; j < dt.Columns.Count; j++) {

jsonBuilder.Append("\""); jsonBuilder.Append(dt.Columns[j].ColumnName); jsonBuilder.Append("\":\""); jsonBuilder.Append(dt.Rows[i][j].ToString()); jsonBuilder.Append("\",");

} jsonBuilder.Remove(jsonBuilder.Length - 1, 1); jsonBuilder.Append("},");

} jsonBuilder.Remove(jsonBuilder.Length - 1, 1); jsonBuilder.Append("]"); jsonBuilder.Append("}"); return jsonBuilder.ToString();

} #endregion

利⽤Ajax、json给前台⻚⾯中的select绑定数据源。后台通过两个函数，分别获得数据库表的数据，将 数据转为Json格式返回给前台。前台在接收数据后，将数据进⾏解析，获得下拉框中需要绑定的字 段。在绑定时，下拉框的每⼀项都分别绑定⼀个⽂本、value值。⽂本⽤于显示，供⽤户选择。value 值，是⽤户选择了某个字段，取得这个字段的value值，进⾏后台的查询字段。

