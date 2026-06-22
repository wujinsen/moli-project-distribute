htps:/ w.aboutyun.com/forum.php?mod=viewthread&tid=310

<table>
  <tr>
    <th>可以试试这⼏个⽅法：<br><br>1.性能满⾜的情况下，尽量设置'sink.shufle-by-partition.enable'=true<br>2.如果设置了'sink.shufle-by-partition.enable'=false，建议使⽤Flink 1.12版本的⾃动合并⼩⽂件功 能。<br>3.设置合理的checkpoint周期，业务允许的情况下，可以加⼤checkpoint周期，减少⽣成⽂件的数 量。<br><br><br>产⽣⼩⽂件的情况，但是⽆法完全避免，根据实际情况定期合并⼩⽂件。</th>
  </tr>
</table>


4.可以最⼤限度降低Flink

