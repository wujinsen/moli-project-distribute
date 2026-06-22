$("table :checkbox:first").change(function(){ $(this).closest("table")

.find(":checkbox:not(:first)")

.prop("checked", this.checked); });

<html> <head> <script type="text/javascript" src="http://code.jquery.com/jquery-1.4.4.min.js"></script> <script type="text/javascript"> $(function(){

$('#selectAll').click(function(){

/使⽤atr只能执⾏ 次

$('input[type=checkbox]').attr('checked', $(this).attr('checked'));

/ 使⽤prop则完美实现全选和反选 }); $("input[name='check']").prop("checked", $(this).prop("checked");

}); </script> </head> <body>

<<inputinput typetype=="checkbox""checkbox" id/>="selectAll" />全选 <input type="checkbox" /> <input type="checkbox" /> <input type="checkbox" /> <input type="checkbox" /> <input type="checkbox" />

</body> </html>

