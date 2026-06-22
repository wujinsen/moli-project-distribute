js表单提交和submit提交的区别

![image 1](<js表单提交和submit提交的区别.note_images/imageFile1.png>)

复制代码

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1strict.dtd"> <html xmlns="http://www.w3.org/1999/xhtml"> <head> <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> <title>⽆标题⽂档</title> </head>

<body> <script>

function test() {

document.getElementById("myform").submit(); alert(11);

} </script> <form name="myfrom" id="myform" method="get" action="b.php"> <input type="text" name="pwd" value="" /> <input type="submit" name="sub" value="111" /> <input type="button" name="btn" value="btn" onclick="test()" /> </form> </body> </html>

![image 2](<js表单提交和submit提交的区别.note_images/imageFile2.png>)

复制代码

注意：get⽅式提交表单时 action⾥⾯不能⽤url传值, post则可以这样传

js提交和submit按钮提交的区别:

- 1. js提交表单时不会带上 submit 按钮的值(因为没有被单击) 所有浏览器
- 2. input 回⻋提交 w3c浏览器会带上submit按钮的值，ie6则不会带


解决办法：增加⼀个hidden域,⽤这个来判断，⽆论⽤哪种⽅式提交都会有值

submit按钮上绑定提交事件:

即:<input type="submit" name="btn" value="btn" onclick="test()" /> 都会带上submit的值, ⽤js提交都检测不到onsubmit状态 w3c： 提交⼀次 ie6: 分两次提交，先js在form提交

解决办法：如果按钮为submit则 检测时⽤onsubmit事件检测 如果按钮为button，则检测通过后在触发submit事件

⼀定不要⽤js提交表单，然后⼜⽤onsubmit去检测

# 单纯的⽤js提交表单, alert, ff下阻塞表单的提交，⽽其他浏览

