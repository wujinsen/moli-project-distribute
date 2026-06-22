<html> <head> <title> jquery each使⽤ </title> <script type='text/javascript' src='jquery-1.8.2.min.js'></script>

<script type="text/javascript"> $(document).ready(function(){ //进⾏遍历⼀维数组

- var arr1 = [ "aaa", "bbb", "ccc" ]; //同c语⾔不同时变量值使⽤[] ⽽不是{} $.each(arr1, function(i,val){ alert(i+"前⾯是下标后⾯是值"+val); });

//遍历⼆维数组

- var arr2 = [['a', 'aa', 'aaa'], ['b', 'bb', 'bbb'], ['c', 'cc', 'ccc']]; $.each(arr2, function(i, item){


alert(i+"前⾯是下标后⾯是值"+item); }); $.each(arr2, function(i, item){ $.each(item,function(j,val){

alert(j+"前⾯是下标后⾯是值"+val); });

});

//循环遍历dom对象 $.each($("input:hidden"), function(i,val){ alert(val); //为dom对象 alert(i); //为下标0 1 2 alert(val.name); alert(val.value); });

//结果和上⾯⼀样 $("input:hidden").each(function(i,val){ alert(i); alert(val); alert(val.name); alert(val.value); });

});

</script> </head> <body>

- <input name="aaa" type="hidden" value="111" />

- <input name="bbb" type="hidden" value="222" />

- <input name="ccc" type="hidden" value="333" />

- <input name="ddd" type="hidden" value="444"/> </body> </html>


