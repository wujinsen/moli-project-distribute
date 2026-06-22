这是⼀个简洁的神奇的密码强度验证例⼦！

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.


<script type="text/javascript"> var $ = function(v){return document.getElementById(v);} function isSecurity(v){

if (v.length < 3) { iss.reset();return;} var lv = -1;

if (v.match(/[a-z]/ig)){lv++;} if (v.match(/[0-9]/ig)){lv++;} if (v.match(/(.[^a-z0-9])/ig)){lv++;} if (v.length < 6 && lv > 0){lv--;} iss.reset(); switch(lv) {

- case 0: iss.level0(); break;

- case 1: iss.level1(); break;

- case 2: iss.level2(); break; default: iss.reset(); }


} var iss = {

color:["CC0000","FFCC33","66CC00","CCCCCC"], text:["弱","中","强"], width:["50","100","150","10"], reset:function(){ $("B").style.backgroundColor = iss.color[3]; $("B").style.width = iss.width[3];

- $("A").innerHTML = ""; }, level0:function(){

- $("B").style.backgroundColor = iss.color[0];


- $("B").style.width = iss.width[0];

- $("A").innerHTML = "密码较弱"; }, level1:function(){

- $("B").style.backgroundColor = iss.color[1];


- $("B").style.width = iss.width[1];

- $("A").innerHTML = "中"; }, level2:function(){

- $("B").style.backgroundColor = iss.color[2];


- $("B").style.width = iss.width[2]; $("A").innerHTML = "⾼强"; }


} </script> <table border="0" style="border-collapse:collapse;">

- 52.
- 53.
- 54.
- 55.
- 56.
- 57.


<tr> <td>密码:<input type="password" size=50 maxlength=20 onkeyup="isSecurity(this.value);"></td> <td bgcolor="#EEEEEE" id="B"></td> <td id="A">密码强度检测</td>

</tr> </table>

