临时开启⽹卡：

<table>
  <tr>
    <th>[rot@linux ~]# ifup {interface} [rot@linux ~]# ifdown {interface}</th>
  </tr>
</table>


[rot@linux ~]# ifup eth0

或者

<table>
  <tr>
    <th>ifconfig ethx up ifconfig ethx down<br><br></th>
  </tr>
</table>


永久开启： 修改：ONBOT=yes

