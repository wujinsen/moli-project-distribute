# 求素数:

<table>
  <tr>
    <th>public static void main(String[] args) {<br><br>/程序打印出从10到20的所有素数并求和； int Sum = 0; for (int i = 1; i < 201; i +) {<br><br>bolean flag = true; /*内嵌了⼀个for循环，作⽤是⽤10到20之间的每⼀个数，从2⼀直除到它本身，如果等于0<br><br>的话，那么就不属于素数，就把flag置为false*/ for (int j = 2; j <= i - 1; j +) { if (i % j = 0) /能整除说明不是素数 flag = false;<br><br>} if (flag) {/只有当flag为true的时候，才会这⾥⾛。<br><br>Sum = Sum + i;/每⼀次循环都让sum加上这个素数i，然后重新赋值给sum System.out.print(i+",");<br><br>}<br><br>} System.out.println("所有素数总和为："+Sum);<br><br>}</th>
  </tr>
</table>


