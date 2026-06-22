<table>
  <tr>
    <th>chf是前⽂的⼀个FileChanel<br><br>ByteBufer buf = ByteBufer.alocate(50); chf.read(buf); buf.flip(); byte [] byt = new byte[10]; buf.get(byt); 然后就出来了 ： Exception in thread "main" java.nio.BuferUnderflowException at java.nio.HeapByteBufer.get(Unknown Source) at java.nio.ByteBufer.get(Unknown Source) at Sample.main(Sample.java: 2)<br><br>请问这怎么办 更多 分享到： 相关主题推荐： 相关帖⼦推荐：<br><br>0<br><br>exceptionthread异常<br><br>extjs trePanel抛出异常，⾼⼿看看。<br><br>c+调⽤cplex的问题<br><br>⾕歌⼩bug<br><br>主线程被卡死，⿏标等⽆法动作！请教各位哪⾥出问题了？<br><br>socket<br><br>接收IOS端发送的16进制图⽚data数据流 没法重新绘制成图⽚<br><br>由于 AdresFilter 在 EndpointDispatcher 不匹配，To 为“”的消息⽆法在接收⽅处理。<br><br>try catch 使⽤问题</th>
  </tr>
  <tr>
    <td>对我有⽤[0]丢个板砖[0]引⽤ |举报 | 管理</td>
  </tr>
</table>


回复次数：5

<table>
  <tr>
    <th rowspan="2">MiceRice 等级：<br><br>2<br><br>关注 ldh91<br><br>![image 1](<ByteBuffer写入数组BufferUnderflowException异常.note_images/imageFile1.png>)</th>
    <th>得分：0回复于： 2012-01-201 42 1<br><br>你的ByteBufer才50，但是你buf.get(byt)这⾥⾯ 的字节数组⻓度是10，ByteBufer表示它搞不定 了。<br><br>#1</th>
  </tr>
  <tr>
    <td>管理<br><br>CSDN投诉事项说明<br><br>引⽤</td>
  </tr>
</table>


13 对我有⽤[0]丢个板砖[0] |举报 |

<table>
  <tr>
    <th rowspan="2">艾姆喔替 等级：<br><br>关注 motLovejava<br><br>![image 2](<ByteBuffer写入数组BufferUnderflowException异常.note_images/imageFile2.png>)</th>
    <th>得分：0回复于： 2012-01-201 56 34 我知道那个 改过 ByteBufer 跟 byte的⼤⼩<br><br>不管谁⼤谁⼩ 都出那个异常<br><br>#2</th>
  </tr>
  <tr>
    <td>管理<br><br>如果您对CSDN论坛有意⻅和建议 请直接在本帖 指教<br><br>引⽤</td>
  </tr>
</table>


# 对我有⽤[0]丢个板砖[0] |举报 |

得分：0回复于： 2012-01-20 12 50 57 Java code

?

ByteBuffer buf = ByteBuffer.allocat chf.read(buf); //这句话抛的异常 buf.flip(); byte [] byt = newbyte [ 100 ] buf.get(byt);

参考如下代码 Java code

?

publicstaticvoid main(String

FileChannel channel = n nel();

// 字节⽅式写⼊ channel.write(ByteBuffer.

s()));

关注 lost_guy_in_scut

channel.close();

st_u__sut 等级：

// 根据FileInputStream获得 channel = new FileInpu // ByteBuffer分配空间,16个字 // 这⾥需要知道 byte是1字节 //

![image 3](<ByteBuffer写入数组BufferUnderflowException异常.note_images/imageFile3.png>)

是还是必须记住的。 ByteBuffer buff = ByteBuf // 字节数组数据装⼊buff， channel.read(buff); // 反转此缓冲区 buff.flip(); byte [] byt = newbyte System.out.println(buff.g

FileChannel }

对我有⽤[0]丢个板砖[0]引⽤ |举报 | 管理

得分：0回复于： 2012-01-20 12 52 40 少⼀段代码，忘记关闭了。 Java code

?

publicstaticvoid main(String[] arg

FileChannel channel = n nel();

// 字节⽅式写⼊ channel.write(ByteBuffer.

s()));

channel.close();

// 根据FileInputStream获得 channel = new FileInpu // ByteBuffer分配空间,16个字 // 这⾥需要知道 byte是1字节 //

关注 lost_guy_in_scut

st_u__sut 等级：

![image 4](<ByteBuffer写入数组BufferUnderflowException异常.note_images/imageFile4.png>)

是还是必须记住的。 ByteBuffer buff = ByteBuf // 字节数组数据装⼊buff， channel.read(buff); // 反转此缓冲区 buff.flip(); byte [] byt = newbyte

System.out.println(buff.get channel.close();

}

