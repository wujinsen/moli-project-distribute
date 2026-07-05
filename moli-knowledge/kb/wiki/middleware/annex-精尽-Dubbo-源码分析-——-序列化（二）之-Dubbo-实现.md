---
title: 精尽 Dubbo 源码分析 —— 序列化（二）之 Dubbo 实现.note（原文插图 annex）
slug: annex-精尽-Dubbo-源码分析-——-序列化（二）之-Dubbo-实现
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/源码分析/芋道源码/精尽 Dubbo 源码分析 —— 序列化（二）之 Dubbo 实现.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

本⽂基于 Dubbo 2.6.1 版本，望知悉。

# 1. 概述

本⽂分享基于 Dubbo ⾃⼰实现的序列化拓展。要实现序列化的⾼性能，需要考虑两⽅⾯：

序列化和反序列化速度快

从传输⻆度，数据压缩效果好，即序列化后的数据量体积⼩

旁⽩君：从以下开始，Dubbo 指的是 Dubbo 序列化拓展，⽽不是 Dubbo PRC 框架。请注意。

《⽤户指南 —— 性能测试报告》 《在Dubbo中使⽤⾼效的Java序列化（Kryo和FST）》

在 和 中，我 们可以看到，Dubbo 是⼀种相对优秀的实现⽅式。虽然，在最新版本的 Dubbo 项⽬中，dubboserialize 模块已经去除了 Dubbo 序列化的实现，猜测因为引⼊ Kryo 和 FST ，相⽐来说更优秀。 当然，即使如此，艿艿觉得了解下 Dubbo 序列化是如何实现的，是⼀种⾮常棒的眼界提升，特别是序 列化的数据压缩，在很多场景下都会使⽤，例如 Lucene 的数据存储。 下⾯，我们跟着代码，⼀起愉快的玩耍把。本⽂涉及代码如下：

![image 1](assets/imageFile1.png)

类图

写的有点匆忙，也有点着急，如果有错误，或者不清晰的地⽅，请往死⾥ 抽（告诉）我。哈哈哈。

# 2. GenericDataFlags

Dubbo ，是⼀种有序、紧凑的序列化⽅式。如下是序列化后的⼆进制数据流的示意图：

![image 2](assets/imageFile2.png)

⼆进制数据流

不同于 JSON / XML 等序列化⽅式，⽆需序列化每个属性名。通过 Builder 对象，创建每个类的序 列化和反序列化的具体代码。

这样，我们就避免了属性名的序列化，提升了速度，减少了数据的体积。

当然，反过来说，如果对象发⽣了变化( 增加或删除属性 )，可能会出现 Client 和 Server 序列 化的不兼容，因为属性的顺序发⽣了变化。

属性值和属性值之间⽆间隔，通过属性值的标志位 Flag 保证，也就是本⼩节要分享的 GenericDataFlags 。

com.alibaba.dubbo.common.serialize.support.dubbo.GenericData Flags

下⾯，我们来看看 ，通⽤数据标记位枚举，代码如下：

<table>
  <tr>
    <th>publicinterfaceGenericDataFlags{ /prefix thre bits byte VARINT= 0, / 0 数字<br><br>OBJECT =(byte) 0x80; /-128 对象 /varint tag<br><br>byte VARINT8= VARINT, VARINT16 = VARINT | 1, VARINT24 = VARINT |2, VARINT32 = VARINT | 3;<br><br>byte VARINT40 = VARINT | 4, VARINT48= VARINT | 5, VARINT56 =VARINT | 6, VARINT64 = VARINT | 7;<br><br>/varint contants VARINT_NF = VARINT | 10,VARINT_NE = VARINT | 1, VARINT_ND = VARINT |12;<br><br>byte VARINT_NC = VARINT | 13,VARINT_NB = VARINT | 14, VARINT_NA = VARINT |15, VARINT_N9 = VARINT | 16;<br><br>byte VARINT_N8 = VARINT | 17,VARINT_N7 = VARINT | 18, VARINT_N6 = VARINT |19, VARINT_N5 = VARINT | 20;<br><br>byte VARINT_N4 = VARINT | 21,VARINT_N3 = VARINT | 2, VARINT_N2 = VARINT |23, VARINT_N1 = VARINT | 24;<br><br>byte VARINT_0 = VARINT | 25,VARINT_1 = VARINT | 26, VARINT_2= VARINT | 27,VARINT_3 = VARINT | 28;<br><br>byte VARINT_4 = VARINT | 29,VARINT_5 = VARINT | 30, VARINT_6= VARINT | 31,VARINT_7 = VARINT | 32;<br><br>byte VARINT_8 = VARINT | 3,VARINT_9 = VARINT | 34, VARINT_A= VARINT | 35,VARINT_B = VARINT | 36;<br><br>byte VARINT_C = VARINT | 37,VARINT_D = VARINT | 38, VARINT_E= VARINT | 39,VARINT_F = VARINT | 40;<br><br>byte VARINT_10 = VARINT | 41,VARINT_1 = VARINT | 42, VARINT_12 = VARINT |43, VARINT_13 = VARINT | 4;<br><br>byte VARINT_14 = VARINT | 45,VARINT_15 = VARINT | 46, VARINT_16 = VARINT |47, VARINT_17 = VARINT | 48;<br><br>byte VARINT_18 = VARINT | 49,VARINT_19 = VARINT | 50, VARINT_1A = VARINT |51, VARINT_1B = VARINT | 52;<br><br>byte VARINT_1C = VARINT | 53,VARINT_1D = VARINT | 54, VARINT_1E = VARINT | 5, VARINT_1F = VARINT | 56;<br><br>/object tag<br><br>byte OBJECT_REF = OBJECT | 1,OBJECT_STREAM = OBJECT | 2,OBJECT_BYTES = OBJECT | 3;<br><br>byte OBJECT_VALUE = OBJECT |4, OBJECT_VALUES = OBJECT | 5, OBJECT_MAP = OBJECT | 6;<br><br>byte OBJECT_DESC = OBJECT | 10, OBJECT_DESC_ID = OBJECT | 1; /object constants byte OBJECT_NUL = OBJECT | 20, OBJECT_DUMY = OBJECT | 21;<br><br></th>
  </tr>
</table>


}

😜 是不是有点⼀脸懵逼？！我们把枚举做⼀次规整，如下图所示：

![image 3](assets/imageFile3.png)

协议整理

在每个属性值( 即 field )的⾸个 Byte 位，称为标志位 Flag 。⽬前我们分成两⼤类( 图中，绿⾊部分 )：

Varint ，变⻓数字，占⽤ Byte 值的 [0, 128) 区间。

Object ，对象，占⽤ Byte 值的 [-128, 0) 区间。

标志位 Flag 根据⽤途，可以分成两种类型（注意，值是不重叠的）：

Tag ，标签( 图中，橙⾊部分 )。

以 VarInt 举例⼦，数字分成 BYTE、SHORT、INT、LONG 四种数据类型。 通过标记位， 表示数字占⽤多少 Byte ，从⽽实现变⻓，节省 Byte 的占⽤。例如，属性值类型 为 Long ，但是值是 100L ，那么只需要要 1 Byte( 标记位为 VARINT8 ) + 1 Byte( 100L ) = 2 Byte 。

当然，这种⽅式也有缺点，对于⼤整数，会多占⽤⼀个标记位，例 如 Integer.MAX_VALUE 。从统计上来说，业务系统更多的是⼩整数。所以，这个缺点也 是能够接受的。

CONSTANTS ， 枚举( 图中，⻩⾊部分 )，⽤于常⽤属性值。

以 Varint 举例⼦，在业务系统中，[ -15, 31 ] 是⾮常常⽤。通过枚举，进⼀步减少数据 提及，提升序列化速度。所以 Varint 的⼆进制数据流示意图如下：

![image 4](assets/imageFile4.png)

⼆进制数据流

可能有胖友会问，上⾯只提到了数字怎么序列化，那么对象怎么序列化呢？我们以 POJO 为例⼦，简 单说下。实际上，我们可以把对象理解成⼀个属性值的集合，通过下⾯会看到的 Builder 类，⽣成该对 象的序列化和反序列化的过程的代码即可。 当然，对象不仅仅有 POJO ，还有 MAP，数组等等，下⾯我们都会看到具体的处理代码。 🙂 嗯，哔哔了这么多，让我们愉快的开始看代码把。

# 3. Data

## 3.1 GenericDataOutput ，实现

com.alibaba.dubbo.common.serialize.support.dubbo.GenericDataOutput

DataOutput，GenericDataFlags 接⼝，Dubbo 数据输出实现类。

- 3.1.1 构造⽅法


<table>
  <tr>
    <th>/*<br><br>* 默认 {@link #mCharBuf} ⼤⼩<br><br>*/ private static final int CHAR_BUF_SIZE = 256; /*<br><br>* 序列化字符串的临时结果的 Bufer 数组，⽤于 {@link #writeUTF(String)}中。<br><br>*/ private final char[] mCharBuf = new char[CHAR_BUF_SIZE]; /*<br><br>* 序列化 Varint 的临时结果的 Bufer 数组，⽤于 {@link #writeVarint32(int)} 和 {@link #writeVarint64(long)} 中。<br><br>*/ private final byte[] mTemp = new byte[9]; /*<br><br>* 序列化结果的 Bufer 数组<br><br>*/ private final byte[] mBufer; /*<br><br>* {@link #mBufer} 容量⼤⼩<br><br>*/ private final int mLimit; /*<br><br>* {@link #mBufer} 当前写⼊位置<br><br>*/ private int mPosition = 0; /*<br><br>* 结果输出<br><br>*/<br><br><br></th>
  </tr>
</table>


private final OutputStream mOutput; mCharBuf 属性，序列化字符串的临时结果的 Buffer 数组，⽤于 #writeUTF(String v) ⽅法 中。

#CHAR_BUF_SIZE 静态属性，默认⼤⼩。

mTemp 属性，序列化 Varint 的临时结果的 Buffer 数组，⽤ 于 #writeVarint32(int) 和 #writeVarint64(long) ⽅法中。

数组⼤⼩为 9 ，因为 Varint 最⼤占⽤ 9 字节，Tag( 1 Byte ) + Long( 8 Bytes ) 。

mBuffer 属性，序列化结果的 Buffer 数组。 mPosition 属性，当前写⼊位置。 mLimit 属性，容量⼤⼩。 mOutput 属性，结果输出，mBuffer => mOutput 中。

- 3.1.2 writeBool
- 3.1.3 writeByte


<table>
  <tr>
    <th>@Overide public void writeBol(bolean v) throws IOException {<br><br>write0(v ? VARINT_1 : VARINT_0);<br><br></th>
  </tr>
</table>


}

通过 1 表示 TRUE ，0 表示 FALSE 。占⽤ 1 Byte ，使⽤ CONSTANTS( VARINT_1、VARINT_0 ) 即可。

调⽤ #write0(byte b) ⽅法，写⼊ mBuffer 中。代码如下：

<table>
  <tr>
    <th>protectedvoidwrite0(byte b)throws IOException{<br><br>/ 超过 mBufer 容量上限，刷⼊ mOutput 中 if (mPosition =mLimit){<br><br>flushBufer(); }<br><br>/ 写⼊ mBufer 中。 mBufer[mPosition+] = b;<br><br></th>
  </tr>
</table>


}

#flushBuffer() ⽅法，代码如下：

<table>
  <tr>
    <th>@Overide public void flushBufer() throwsIOException {<br><br>if (mPosition > 0) { / 写⼊ mOutput mOutput.write(mBufer, 0, mPosition);<br><br>/ 重置当前写⼊位置 mPosition = 0;<br><br>}<br><br></th>
  </tr>
</table>


}

<table>
  <tr>
    <th>1: @Overide<br><br>2 public voidwriteByte(byte v) throws IOException {<br><br>3: switch(v) {<br><br>4: /TODO 【8034】为什么没有负数的枚举<br><br>5: /符合 Varint枚举值，写⼊对应的枚举值<br><br>6: case 0:<br><br>7: write0(VARINT_0);<br><br>8: break;<br><br>9: / . 省略中间，[1, 30] 重复的 case 处理<br><br>10: case 31: 1: wite0(VARINT_1F);<br><br><br>12: break;<br><br>13: /不符合 Varint 枚举值，写⼊ Tag +具体值<br><br>14: default:<br><br>15: / 写⼊ VARINT8<br><br>16: write0(VARINT8);<br><br>17: / 写⼊ BYTE 具体值 8 write0(v);<br><br><br>19: }<br><br></th>
  </tr>
</table>


20: }

- 第 4 ⾏：// TODO 【8034】为什么没有负数的枚举

- 第 5 ⾄ 12 ⾏：符合 Varint CONSTANTS ，写⼊对应的 CONSTANTS。


第 13 ⾄ 19 ⾏：不符合 Varint CONSTANTS ，调⽤两次 #write0(byte b) ⽅法，写 ⼊ VARINT8 + 具体值 v。

- 3.1.4 writeShort


<table>
  <tr>
    <th>@Overide public void writeShort(short v)throws IOException {<br><br>writeVarint32(v);<br><br></th>
  </tr>
</table>


}

调⽤ #writeVarint32(int v) ⽅法，写⼊。代码如下：

<table>
  <tr>
    <th>1:privatevoidwriteVarint32(int v)throws IOException{<br><br>2: switch (v) {<br><br>3: / 符合 Varint 枚举值，写⼊对应的枚举值<br><br>4: case-15:<br><br>5 write0(VARINT_NF);<br><br>6: break;<br><br>7: / . 省略中间，[-14,30]重复的 case 处理<br><br>8: case31:<br><br>9: wite0(VARINT_1F);<br><br>10: break; 1: / 不符合 Varint 枚举值，写⼊ Tag+ 具体值<br><br><br>12: default:<br><br>13: int t = v, /值<br><br>14: ix = 0; /当前写⼊位置<br><br>15: byte[] b = mTemp;<br><br>16: / 顺序读取字节，存到 mTemp 中<br><br>17: while (true) {<br><br>18: b[+ix] =(byte)(v & 0xf); / ⼤于等于 128时，会截取到最⾼位的 1 ，变成负数。<br><br>19: if (v >= 8) =0) {/ ⽆可读字节<br><br><br>0: break;<br><br>1: }<br><br>2 }<br><br><br>23:<br><br>24: if (t > 0) {/ 正数<br><br>25: / [ 0a e2=>0ae2 0 ] [ 92 => 92 0]<br><br>26: / 最后⼀次取余，⼤于等于 128 时，在 (byte) 转换后，变成了负数，需要补⼀个 0 的 BYTE 到 mTemp 中，否则反序列化后会被误认为负数。<br><br><br>7: if ([ix]< 0) {<br><br>8 b[+ix] =0;<br><br><br>29: }<br><br>30: } else {/ 负数<br><br>31: / [ 01f f f=> 01f ] [ e0f f f => e0 ]<br><br>32: / 负数使⽤补码表示，⾼位是⼤量的 1 ，需要去除。<br><br><br>3: / 另外，LONG的位数⽐ INT 更多，所以，相同数字，LONG 型会⽐ INT 型更多，例如<br><br>long v =-62L 和 intv = -62 。<br><br>4 while (b[ix] = (byte) 0xf & b[ix -1]< 0) {<br><br>5 ix-;<br><br>6: }<br><br>7: }<br><br><br>38:<br><br>39: / 写⼊ Tag ，到⾸ Byte 位<br><br>40: b[0] = (byte)(VARINT+ ix - 1);<br><br>41: / 写⼊ Tag + Bytes到 mBufer中<br><br><br>2 write0(b, 0, ix +1);<br><br>3 }<br><br><br></th>
  </tr>
</table>


4: }

第 5 ⾄ 12 ⾏：符合 Varint CONSTANTS ，写⼊对应的 CONSTANTS。

第 11 ⾄ 43 ⾏：不符合 Varint CONSTANTS ，写⼊ TAG + 具体值 。

第 16 ⾄ 22 ⾏：顺序循环读取每个字节，存到 b 数组中。

第 18 ⾏：先 0xff 做 %256 取余，获取到⼀个字节。再 (byte) 转换成 BYTE 值。因为， BYTE 数据范围为 [-128, 127] ，所以取余的结果为 [128, 255] 范围时，则会被⾼位 截取，变成负数。例如，255 会变成 -1 。也因此，反序列化时，需要做⼀次 0xff | 操 作，来补⻬⾼位的 1。

- 第 18 ⾏：b[++ix] ，先增加 ix 的值，在写⼊ b 数组中。因为，⾸ Byte 位为 TAG 。

- 第 19 ⾄ 21 ⾏：⽆可读字节，结束循环。


第 24 ⾄ 29 ⾏：最后⼀次取余，⼤于等于 128 时，在 (byte) 转换后，变成了负数，需要补⼀ 个 0 到 b 中，否则反序列化后会被误认为负数。例如：v = 255 。

第 30 ⾄ 37 ⾏：负数使⽤补码表示，⾼位是⼤量的 1 ，需要循环去除。另外，LONG 的位数⽐ INT 更多，所以，相同数字，LONG 型会⽐ INT 型更多，例如 long v = -662L 和 int v = -662 。示例如下：

<table>
  <tr>
    <th>INT<br><br>-10-3-1-1 LONG<br><br></th>
  </tr>
</table>


-10-3 -1-1-1-1 -1 -1

x

涉及⼤量的位操作，不熟悉的胖友，请 Google 复习下⼤学课程。😈

第 40 ⾏：写⼊ TAG ，到 b 的⾸位。

第 42 ⾏：调⽤ #write0(byte[] b, int off, int le) ⽅法，批量写⼊ mBuffer 中。 代码如下：

<table>
  <tr>
    <th>protectedvoidwrite0(byte[] b,int of,int len)throws IOException{ int rem =mLimit- mPosition; / 未超过 mBufer 容量上限，批量写⼊ mBufer 中<br><br>if (rem >len) { System.araycopy(b, of, mBufer, mPosition, len); mPosition +=len;<br><br>} else {<br><br>/ 部分批量写满 mBufer中 System.araycopy(b, of, mBufer, mPosition, rem); mPosition = mLimit;<br><br>/ 刷⼊ mOutput中 flushBufer(); of +=rem; / 新的开始位置 len -=rem; / 新的⻓度<br><br>/ 未超过 mBufer 容量上限，批量写⼊ mBufer中<br><br>if (mLimit >len){ System.araycopy(b,of, mBufer, 0,len); mPosition= len;<br><br>/ 超过 mBufer容量上限，批量写⼊ mOutput 中 }else{<br><br>mOutput.write(b, of, len); }<br><br>}<br><br></th>
  </tr>
</table>


}

- 3.1.5 writeInt
- 3.1.6 writeLong


<table>
  <tr>
    <th>@Overide public void writeInt(int v) throws IOException {<br><br>writeVarint32(v);<br><br></th>
  </tr>
</table>


}

<table>
  <tr>
    <th>@Overide public void writeInt(int v) throws IOException {<br><br>writeVarint64(v);<br><br></th>
  </tr>
</table>


}

调⽤ #writeVarint64(long v) ⽅法，写⼊。代码如下：

<table>
  <tr>
    <th>1:privatevoidwriteVarint64(long v)throws IOException{<br><br>2: /数据范围在 INT 内<br><br>3 inti =(int) v;<br><br>4 if(v =i) {<br><br>5: writeVarint32(i);<br><br>6: /数据范围在 LONG 内，不符合 Varint枚举值，写⼊ Tag + 具体值。和 writeVarint32是⼀致 的<br><br>7: } else {<br><br>8 longt = v;<br><br>9: intix = 0;<br><br><br>0: byte[] b = mTemp;<br><br>1:<br>2 while (true) {<br><br>3 b[+ix] = (byte) (v &0xf);<br><br>4 if (v >= 8) = 0)<br><br>5 break;<br><br>6: }<br><br>7: 8 if (t > 0) {<br><br><br>19: / [ 0a e2 =>0a e2 0] [ 92 => 92 0 ]<br><br>0: if (b[ix] < 0)<br><br>1: b[+ix] =0;<br><br>2 } else {<br><br>3 / [ 01f f f => 01f ] [ e0f f f=>e0 ]<br><br>4 while (b[ix] = (byte)0xf & b[ix - 1] <0)<br><br>5 ix-;<br><br>6: }<br><br>7: 8 b[0]= (byte) (VARINT + ix- 1);<br><br><br>29 write0(b, 0, ix +1); 0: }<br><br></th>
  </tr>
</table>


31: }

第 2 ⾄ 5 ⾏：当 v 数据范围在 INT 内时，调⽤ #writeVarint32(int v) 处理。

第 7 ⾄ 30 ⾏：当 v 数据范围在 LONG 内时， 不符合 Varint CONSTANTS ，写⼊ TAG + 具体 值，和 #writeVarint32(int v) 后半段的代码是⼀致的。

- 3.1.7 writeFloat
- 3.1.8 writeDouble
- 3.1.9 writeUInt


<table>
  <tr>
    <th>@Overide public void writeFloat(loat v)throws IOException {<br><br>writeVarint32(Float.floatToRawIntBits(v);<br><br></th>
  </tr>
</table>


}

<table>
  <tr>
    <th>@Overide public void writeDouble(ouble v)throws IOException {<br><br>writeVarint64(Double.doubleToRawLongBits(v);<br><br></th>
  </tr>
</table>


}

<table>
  <tr>
    <th>publicvoidwriteUInt(int v)throws IOException{<br><br>byte tmp; /循环写⼊<br><br>while (true) { /获得最后 7 Bits tmp= (byte)(v & 0x7f); /⽆后续的 Byte，修改 tmp⾸ Bit为 1 ，写⼊ mBufer 中，并结束。<br><br>if (v >= 7) = 0){ write0(byte) (tmp | 0x80); return;<br><br>/有后续的 Byte，写⼊ mBufer 中 } else {<br><br>write0(tmp); }<br><br>}<br><br></th>
  </tr>
</table>


}

UInt ，Unsingned Int ，⽆符号整数( 正数 )。被⽤于表示字符串、数组的⻓度。序列化时，和上⽂ 我们看到的 Varint ⼀样，也是变⻓数字，但是⽅式不同。因为是正整数，所以可以使⽤ Byte 最⾼ 位的 1 ，原来⽤来表示负数，现在来表示是否有后续的 BYTE ，也是正整数的⼀部分。

🙂 代码已经添加注释，胖友⾃⼰看看哈。

推荐阅读 ，⾥⾯ Varint 和 UInt ⼀样采⽤最⾼位来表示是否有后续数 字，但是更加强⼤通⽤，使⽤ Zag 算法解决负数问题，在 Protobuf 中采⽤该⽅式。😈 ⽂章中的 Varint 和本⽂我们看到的 Varint 不同。如果胖友对 Protobuf 的实现感兴趣，推荐阅读

《数值压缩存储⽅法Varint》

《 Protocol B uffer 序列化原理⼤揭秘 - 为什么Protocol Buffer性能这么好？》

。

- 3.1.10 writeBytes


<table>
  <tr>
    <th>1: @Overide<br><br>2: public voidwriteBytes(byte[] b) throws IOException {<br><br>3: / NUL，使⽤ OBJECT_NUL 写⼊ mBufer<br><br>4 if (b = nul) {<br><br>5: write0(OBJECT_NUL);<br><br>6: / 其他，写⼊ mBufer<br><br>7: } else{<br><br>8 writeBytes(b, 0,b.length);<br><br>9: }<br><br><br>0: }<br><br>1: 2@Overide<br><br><br>13: public voidwriteBytes(byte[] b, intof, intlen) throws IOException{<br><br>14: / 空数组，使⽤ OBJECT_DUMY 写⼊ mBufer 5 if (len = 0) {<br><br><br>16: write0(OBJECT_DUMY);<br><br>17: / 数组⾮空，写⼊ OBJECT_BYTES + Length+ 具体数据到 mBufer 8 } else{<br><br><br>19: write0(OBJECT_BYTES);<br><br>0: writeUInt(len); / UInt<br><br>1: write0(b, of, len);<br><br>2 }<br><br><br></th>
  </tr>
</table>


23: }

![image 5](assets/imageFile5.png)

writeBytes

### 3.1.11 writeUTF

- 1: @Overide

- 2: public voidwriteUTF(String v) throws IOException {

- 3: / NUL，使⽤ OBJECT_NUL 写⼊ mBufer

- 4 if (v = nul) {

- 5 write0(OBJECT_NUL);

- 6: } else{

- 7: /空字符串，使⽤ OBJECT_DUMY 写⼊ mBufer

- 8 ntlen = v.length();

- 9: if(len = 0) {

- 10: write0(OBJECT_DUMY); 1: /字符串⾮空，写⼊ OBJECT_BYTES + Length +具体数据到 mBufer


- 12: }else {

- 13: / 写⼊ OBJECT_BYTES 到 mBufer 中

- 14: write0(OBJECT_BYTES);

- 15: / 写⼊ Length到 mBufer 中


- 6: writeUInt(len);

- 7:


- 18: int of= 0,

- 19: limit = mLimit - 3, / -3 的原因，因为若 Char 在 [2048,6536) 范围内，需要占⽤三个 字节，事先⽆法得知。


- 0: size;

- 1: char[] buf =mCharBuf;

- 2: do {


- 23: / 读取数量，不超过 CHAR_BUF_SIZE 上限，同时不超过可读上限

- 24: size= Math.min(len - of, CHAR_BUF_SIZE);

- 25: / 读取字符串到 buf 中 6: v.getChars(of, of + size,buf, 0);


- 27:

- 28: / 写⼊数据到 mBufer 中

- 29 for(inti = 0; i< size; i +) {

- 30: charc = buf[i];

- 31: / Java Character 数据范围为 [0, 6535]

- 32: if (mPosition> limit) { 3: if (c < 0x80) {/ [0, 128) ASCI 码


34: / 0X80 => 10 0 0 0取七位 [0, 64)

- 5

- 6: write0(byte) c);


- 37: }else if(c < 0x80) {/[128, 2048)

- 38: / 0xC0 =>1 0 0 0取六位 [0, 32)

- 39: / 0x80 => 10 0 0 0取七位 [0, 64)


- 0:

- 1: / 0x1F => 0 01 11

- 2 / 0x3F => 01 11

- 3

- 4 te0( te) (0xC0 | (c > 6) & 0x1F);

- 5 write0(byte) (0x80 | (c & 0x3F);


- 46: }else {/ [2048, 6536)

- 47: / 0xE0 =>1 10 0 0取五位 [0, 15]

- 48: / 0x80 => 10 0 0 0取七位 [0, 63]

- 49: / 0x80 => 10 0 0 0取七位 [0, 63] 0:


51: / 0x0F => 0 011

- 2 / 03 01 11

- 3 / 0x3F => 01 11

- 4

- 5 te0( te) (0xE0 | ( > 12) & 0x0F);

- 6: rite0( te) (0x0 | (c > 6) & 0x3F);

- 7: write0(byte) (0x80 | (c & 0x3F);

- 8 }


59 } else {

- 0: if (c < 0x80) {

- 1: mBufer[mPosition+] =(byte) c;

- 2 }else if(c < 0x80) {


63 m femoitio + ( te) (0xC0| (c > 6) & 0x1F);

- 4 mBufer[mPosition+] =(byte) (0x80|(c & 0x3F);

- 5 }else {

- 6: m fer[moitio +] ( te) (0xE0 ( > 12) & 0x0F);


67: m fer[moitio +] ( te) (0x80| (c > 6) & 0x3F);

8 mBufer[mPosition+] =(byte) (0x80|(c & 0x3F); 69: }

- 0: }

- 1: }


- 72:

- 73: / 计算 buf新的开始读取位置。


- 4 of+= size;

- 5 }while(of< len);

- 6: }

- 7: }


78: }

字符串和字节数组，⼆进制数据流的结构是⼀致的，差异点在字符串的每个字符，写⼊ 到 mBuffer 中，即【第 18 ⾄ 75 ⾏】。

第 19 ⾏：-3 的原因，因为每个字符最多需要占⽤三个字节，事先⽆法得知。⽽【第 32 ⾄ 58 ⾏】 和【第 59 ⾄ 69 ⾏】，逻辑上是⼀致的，相⽐来说【第 32 ⾄ 58 ⾏】的 #write0(byte b) ⽅ 法，多⼀个判断，考虑到性能，就分成了两段的逻辑，也就因此，多了这⾥的 -3 。

第 22 ⾄ 25 ⾏：循环读取字符到 buf 中。因为每次读取有 CHAR_BUF_SIZE 最⼤限制，所以超过 时，需要多次读取。读取完⼀批，处理完⼀批，不断重⽤ buf 数组。

第 28 ⾄ 71 ⾏：写⼊ buf 到 mBuffer 中。因为 Java Character 的数据范围为 [0, 65535] ， 超过 BYTE上限。所以写⼊每个字符时，分成三种情况：

第 33 ⾄ 36 ⾏：[0, 128) ，占⽤⼀个字符。取七位，2 的 七次⽅为 128 ，从⽽满⾜数据范 围。

第 37 ⾄ 45 ⾏：[128, 2048) ，占⽤两个字符。取六位、七位，2 的⼗三次⽅为 2048 ，从 ⽽满⾜数据范围。

第 46 ⾄ 58 ⾏：[2048, 65536) ，占⽤三个字符。取五位、七位、七位，2 的⼗九次⽅为 65536 ，从⽽满⾜数据范围。

为什么⾸位取的不同的位数呢？在反序列化时，可以根据⾸位数的⾼位来判断，到底完整的字 符，占⽤了⼏个字节。🙂 或者，我们可以理解成，这是⼀种针对当前场景实现的变⻓整数。

第 74 ⾏：计算 buf 新的开始读取位置。

## 3.2 GenericDataInput ，实现

com.alibaba.dubbo.common.serialize.support.dubbo.GenericDataInput

DataInput，GenericDataFlags 接⼝，Dubbo 数据输⼊实现类。

- 3.2.1 构造⽅法
- 3.2.2 readBool


<table>
  <tr>
    <th>/*<br><br>* 空字符串<br><br>*/ private static final String EMPTY_STRING ="; /*<br><br>* 空字节数组<br><br>*/ private static final byte[] EMPTY_BYTES = {}; /*<br><br>* 输⼊流<br><br>*/ private final InputStream mInput; /*<br><br>* 读取 Bufer 数组<br><br>*/ private final byte[] mBufer; /*<br><br>* {@link #mBufer} 当前读取位置<br><br>*/ private intmRead = 0; /*<br><br>* {@link #mBufer} 最⼤可读取位置<br><br>*/ private intmPosition =0;<br><br><br>public GenericDataInput(InputStreamis) { this(is, 1024);<br><br>} public GenericDataInput(InputStreamis, int bufSize) {<br><br>Input = is; mBufer= new byte[bufSize];<br><br></th>
  </tr>
</table>


}

mBuffer 属性，读取 Buffer 数组。 mRead 属性，当前读取位置。 mPosition 属性，最⼤可读取位置。 mInput 属性，输⼊流。

<table>
  <tr>
    <th>@Overide public bolean readBol() throwsIOException {<br><br>/ 读取字节 byte b = read0();<br><br>/ 判断 true /false switch (b) {<br><br>case VARINT_0: / false return false;<br><br>case VARINT_1: / true return true;<br><br><br>default: / ⾮法<br><br>throw new IOException("Tag eror, expect BYTE_TRUE|BYTE_FALSE, but get " + b); }<br><br></th>
  </tr>
</table>


}

调⽤ #read0() ⽅法，读取字节。代码如下：

<table>
  <tr>
    <th>protectedbyteread0()throws IOException{<br><br>/ 读取到达上限，从 mInput读取到 mBufer 中。 if (mPosition = mRead) {<br><br>filBufer(); }<br><br>/ 从 mBufer 中，读取字节。 return mBufer[mPosition+];<br><br></th>
  </tr>
</table>


}

#fillBuffer() ⽅法，代码如下：

<table>
  <tr>
    <th>privatevoidfilBufer()throws IOException{<br><br>/重置 mPosition mPosition = 0;<br><br>/读取 mInput 到 mBufer 中 mRead =mInput.read(mBufer); /未读取到，抛出 EOFException 异常<br><br>if(mRead = -1) { mRead = 0; throw new EOFException();<br><br>}<br><br></th>
  </tr>
</table>


}

- 3.2.3 readByte


<table>
  <tr>
    <th>1: @Overide<br><br>2: public bytereadByte() throws IOException {<br><br>3: / 读取字节<br><br>4 byte b= read0();<br><br>5: switch(b) {<br><br>6: /不符合 Varint 枚举值，读取字节返回<br><br>7: case VARINT8:<br><br>8: return read0();<br><br>9: /符合 Varint枚举值，返回对应的值<br><br><br>0: case VARINT_0:<br><br>1: return 0;<br><br><br>12: / . 省略中间，[1, 30] 重复的 case处理<br><br>13: case VARINT_1F:<br><br>14: return 31;<br><br>15: default: /⾮法，抛出 IOException 异常<br><br><br>6: throw new IOException("Tag eror, expect VARINT, but get " + b);<br><br>7: }<br><br><br></th>
  </tr>
</table>


18: }

第 4 ⾏：调⽤ #read0() ⽅法，读取字节。

第 6 ⾄ 8 ⾏：不符合 Varint CONSTANTS ，调⽤ #read0() ⽅法，读取字节返回。

第 9 ⾄ 14 ⾏：符合 Varint CONSTANTS，返回对应的值。

- 3.2.4 readShort


<table>
  <tr>
    <th>@Overide public short readShort() throwsIOException {<br><br>return (short) readVarint32();<br><br></th>
  </tr>
</table>


}

调⽤ #readVarint32() ⽅法，读取。代码如下：

<table>
  <tr>
    <th>1:privateintreadVarint32()throws IOException{<br><br>2: /读取⾸位 Byte 字节<br><br>3 byte b= read0();<br><br>4 /<br><br>5: switch(b) {<br><br>6: /不符合 Varint 枚举值，读取 Tag+ 具体值<br><br>7: case VARINT8:<br><br>8 return read0();<br><br>9: case VARINT16: {<br><br>10: byte b1 = read0(), b2 = read0();<br><br><br>1: return (short) (b1 & 0xf) |<br><br>2 (b2 &0xf) < 8);<br><br>3 }<br><br>4: case VARINT24: {<br><br><br>15 byte b1 = read0(), b2 = read0(), b3=read0();<br><br>16: int ret = (b1& 0xf) | 7: (2 xf) < 8) |<br><br><br>18: (b3 &0xf) < 16);<br><br>19: if (b3 < 0){/ 补⻬负数的⾼位<br><br><br>0: return ret| 0xf 0;<br><br>1: }<br><br>2 return ret;<br><br>3 }<br><br><br>24: case VARINT32: {<br><br>25 byte b1 = read0(), b2 = read0(), b3=read0(), b4 = read0(); 6: return(b1&0xf) |<br><br><br>27: (2 xf) < 8) | 8 (3 f) < 16) |<br><br>29 (b4 &0xf) < 24);<br><br>30: }<br><br>31: /符合 Varint 枚举值，返回对应的值<br><br><br>2: case VARINT_NF:<br><br>3: return -15;<br><br><br>34: / . 省略中间，[-14,30] 重复的 case 处理<br><br>5: case VARINT_1F: 36: return 31;<br><br>7: default:<br><br>8 throw new IOException("Tag eror,expect VARINT, but get " +b);<br><br><br>39 }<br><br></th>
  </tr>
</table>


40: }

第 3 ⾏：调⽤ #read0() ⽅法，读取⾸位 Byte 字节。

第 6 ⾄ 30 ⾏：不符合 Varint CONSTANTS，读取 TAG + 具体值。 & 0xff 操作，补回被截取最⾼的 1，从⽽恢复原数，对应 #writeVarint32(int v) ⽅法 的【第 18 位】。 | 0xff000000 操作，补⻬负数的⾼位，对应 #writeVarint32(int v) ⽅法的【第 30 ⾄ 36 位】。

第 31 ⾄ 36 ⾏：符合 Varint CONSTANTS，返回对应的值。

- 3.2.5 readInt


<table>
  <tr>
    <th>@Overide public int readInt()throwsIOException {<br><br>return readVarint32();<br><br></th>
  </tr>
</table>


}

- 3.2.6 readLong
- 3.2.7 readFloat
- 3.2.8 readDouble
- 3.2.9 readUInt


<table>
  <tr>
    <th>@Overide public long readLong() throwsIOException {<br><br>return readVarint64();<br><br></th>
  </tr>
</table>


}

#readVarint64() 和 #readVarint32() 基本⼀致，胖友⾃⼰查看。

<table>
  <tr>
    <th>@Overide public float readFloat() throwsIOException {<br><br>return Float.intBitsToFloat(readVarint32();<br><br></th>
  </tr>
</table>


}

<table>
  <tr>
    <th>@Overide public double readDouble() throwsIOException {<br><br>return Double.longBitsToDouble(readVarint64();<br><br></th>
  </tr>
</table>


}

<table>
  <tr>
    <th>publicintreadUInt()throws IOException{ /读取字节 byte tmp = read0(); / ⽤于暂存当前读取结果 /【第⼀次】 if(tmp < 0) {/负数，意味着⽆后续 return tmp&0x7f;<br><br>} int ret = tmp & 0x7f; / 最终结果<br><br>/【第⼆次】<br><br>if (tmp = read0() < 0) {/ 负数，意味着⽆后续 ret |= (tmp &0x7f) < 7; / 拼接 tmp + ret }else{<br><br>ret |= tmp <7; /【第三次】 if (tmp =read0()< 0) {/ 负数，意味着⽆后续<br><br>ret |=(tmp & 0x7f) < 14; }else {<br><br>ret |=tmp < 14; / 【第四次】 if (tmp= read0() < 0) {/负数，意味着⽆后续 ret |= (tmp& 0x7f) < 21;<br><br>/ 【第五次】5* 7 >32 ，所以可以结束 } else{<br><br>t tmp < 21;<br><br>ret |= (read0() & 0x7f) <28; }<br><br>}<br><br>} returnret;<br><br></th>
  </tr>
</table>


}

- 3.2.10 readBytes


<table>
  <tr>
    <th>@Overide public byte[] readBytes() throws IOException {<br><br>/ 读取字节 byte b = read0(); switch (b) {<br><br>case OBJECT_BYTES: / 数组⾮空<br><br>return read0(readUInt(); case OBJECT_NUL: / NUL<br><br>return nul; case OBJECT_DUMY: / 数组为空<br><br>return EMPTY_BYTES; default:<br><br>throw new IOException("Tag eror, expect BYTES|BYTES_NUL|BYTES_EMPTY, but get"<br><br>+b); }<br><br></th>
  </tr>
</table>


}

#read0(int len) ⽅法，批量读取字节。代码如下：

<table>
  <tr>
    <th>protectedbyte[] read0(int len)throws IOException { int rem =mRead - mPosition; byte[] ret = new byte[len];<br><br>/ 未超过 mBufer 剩余可读取，批量写⼊ mBufer中。mBufer => ret<br><br>if (len <= rem) { System.araycopy(mBufer, mPosition, ret, 0,len); mPosition += len;<br><br>} else {<br><br>/ 部分批量写⼊ ref 中。mBufer=> ret System.araycopy(mBufer, mPosition, ret, 0,rem); mPosition = mRead;<br><br>len -= rem; int read, pos = rem; /新的 ret 读取起点<br><br>/ mInput => ret<br><br>while(len > 0) { read = mInput.read(ret, pos, len); if (read = -1) {<br><br>throw newEOFException();<br><br>} pos += read; / 新的 ret 读取起点 len -= read;<br><br>}<br><br>} return ret;<br><br></th>
  </tr>
</table>


}

- 3.2.11 readUTF


<table>
  <tr>
    <th>1: @Overide<br><br>2: public StringreadUTF() throws IOException {<br><br>3: / 读取字节<br><br>4 byte b= read0();<br><br>5: switch(b) {<br><br>6: /字符串⾮空<br><br>7: case OBJECT_BYTES:<br><br>8: / 读取⻓度<br><br>9: int len= readUInt();<br><br>10: / 反序列化出字符串 1: StringBuilder sb = newStringBuilder();<br><br><br>12: for (int i = 0;i < len;i +) {<br><br>13: / 读取⾸位<br><br>14: byte b1 = read0();<br><br>15: if (b1 & 0x80) =0){/ [0, 128) ASCI码<br><br>16: sb.apend(char)b1);<br><br>17: } else if (b1 & 0xE0) = 0xC0) {/ [128,2048) 8 byte b2= read0(;<br><br><br>19: sb.apend(char) (b1 & 0x1F) < 6)|(b2 & 0x3F);<br><br>20: } else if (b1 & 0xF0) = 0xE0) {/ [2048,6536)<br><br>21: byte b2= read0(), b3 =read0();<br><br><br>2 sb.apend(char) (b1 & 0x0F) < 12)| (b2 &0x3F) <6) | (b3 &0x3F);<br><br>3 } else<br><br>4 throw new UTFDataFormatException("Badutf-8 encoding at" + b1);<br><br>5 }<br><br>6: return sb.toString();<br><br>7: /NUL<br><br>8: case OBJECT_NUL:<br><br><br>29: return nul;<br><br>30: /字符串为空<br><br><br>1: cas OBJECT_DUMY:<br><br>2 return EMPTY_STRING;<br><br>3: default:<br><br><br>34: throw new IOException("Tag eror, expect BYTES|BYTES_NUL|BYTES_EMPTY,but get " + b);<br><br>5 }<br><br></th>
  </tr>
</table>


36: }

第 13 ⾄ 25 ⾏：反序列化每个字符，通过⾸位数的⾼位来判断，到底完整的字符，占⽤了⼏个字 节:

第 15 ⾄ 16 ⾏：数据范围是 [0, 128) ，最⼤数 127 的⼆进制为 01 11 11 11 ，使⽤ & 0x80 运算后，会等于 0 。

第 17 ⾄ 19 ⾏：数据范围是 [128, 2048) ，因为⾸位数取六位，并且使⽤ 0xC0 | 运算后， 所以⼆进制为 11 XX XX XX ，使⽤ & 0xE0 运算后，会等于 0XC0 。

【第 15 ⾄ 16 ⾏】如果使⽤ & 0x80 运算， 不会等于 0 ，不符合要求。

第 20 ⾄ 22 ⾏：数据范围是 [2048, 65536) ，因为⾸位数取五位，并且使⽤ 0xE0 | 运算 后，所以⼆进制为 11 1X XX XX ，使⽤ & 0xF0 运算后，会等于 0xE0 。

【第 15 ⾄ 16 ⾏】如果使⽤ & 0x80 运算， 不会等于 0 ，不符合要求。

【第 17 ⾄ 19 ⾏】如果使⽤ & 0xE0 运算，不会等于 0xC0 ，不符合要求。

# 4. Object

## 4.1 GenericObjectOutput ，实现

com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectOutput

ObjectOutput 接⼝，继承 GenericObjectOutput 类，Dubbo 对象输出实现类。

- 4.1.1 构造⽅法
- 4.1.2 writeObject


<table>
  <tr>
    <th>/*<br><br>* 对象是否允许不实现 {@link java.io.Serializable} 接⼝<br><br>*/ private final bolean isAlowNonSerializable; /*<br><br>* 类描述匹配器<br><br>*/ private ClasDescriptorMaper mMaper; /*<br><br>* 循环引⽤集合<br><br>*<br><br>* KEY ：对象<br><br>* VALUE ：引⽤编号<br><br>*/<br><br><br></th>
  </tr>
</table>


private Map<Object, Integer> mRefs = new ConcurentHashMap<Object,Integer>(); isAllowNonSerializable 属性，对象是否允许不实现 Serializable 接⼝。Dubbo 序列化⽆需强 制实现 Serializable 接⼝。考虑到通⽤性，默认 false 不允许。 mMapper 属性，类描述匹配器。通过该匹配器，可以将类描述，转换成对应的描述编号，从⽽加速 序列化的速度，减少体积，类似 Kryo 的注册。默认使⽤ Builder 的 DEFAULT_CLASS_DESCRIPTOR_MAPPER 实现类。 mRefs 属性，循环引⽤集合，和 FastJSON 的 上，概念是⼀致的。在 AbstractObjectBuilder 中，我们会看到循环引⽤的实现。

《循环引⽤》

<table>
  <tr>
    <th>1: @Overide<br><br>2 @SupresWarnings({"unchecked", "rawtypes"})<br><br>3: public voidwriteObject(Object obj) throws IOException {<br><br>4: / NUL，使⽤ OBJECT_NUL 写⼊ mBufer<br><br>5 if (obj = nul) {<br><br>6: write0(OBJECT_NUL);<br><br>7: return;<br><br>8: }<br><br>9: / 空对象，使⽤ OBJECT_DUMY 写⼊ mBufer<br><br><br>0: Clas<?> c = obj.getClas();<br><br>1: if (c = Object.clas) {<br><br>2 write0(OBJECT_DUMY);<br><br><br>13: } else{<br><br>14: /获得类描述<br><br>15: String desc = ReflectUtils.getDesc(c);<br><br>16: /查询类描述编号<br><br>17: intindex = mMaper.getDescriptorIndex(desc);<br><br>18: /不存在，使⽤ OBJECT_DESC + 类描述 写⼊ mBufer<br><br>19: if(index < 0) { 0: write0(OBJECT_DESC);<br><br><br>21: writeUTF(desc);<br><br>2: /存在，使⽤ OBJECT_DESC_ID + 类描述编号 写⼊ mBufer<br><br>3 }else {<br><br>4 0(OBJECT_DESC_ID);<br><br>5 writeUInt(index);<br><br><br>26: }<br><br>27: /获得类对应的序列化 Builder<br><br>28: Builder b = Builder.register(c, isAlowNonSerializable);<br><br>29: /序列化到 mBufer 中<br><br><br>0: b.writeTo(obj, this);<br><br>1: }<br><br><br></th>
  </tr>
</table>


32: }

【第⼀种】第 4 ⾄ 8 ⾏：对象为 NULL ，写⼊ OBJECT_NULL 到 mBuffer 。

【第⼆种】第 9 ⾄ 12 ⾏：对象为 Object 类型，写⼊ OBJECT_DUMMY 到 mBuffer 。

【第三种】第 13 ⾄ 30 ⾏：对象⾮空，写⼊ 类描述 + 对象 到 mBuffer 。

第 15 ⾏：调⽤ ReflectUtils#getDesc(c) ⽅法，获得类描述。代码如下：

<table>
  <tr>
    <th>publicstatic StringgetDesc(Clas<?> c){ StringBuilder ret = new StringBuilder();<br><br>/ Aray<br><br>while(c.isAray() { ret.apend('['); c= c.getComponentType();<br><br>} / 基本类型<br><br>if (c.isPrimitive(){ String t= c.getName(); if("void".equals(t) ret.apend(JVM_VOID); else (" olean".equals(t) ret.apend(JVM_BOLEAN); else ("byte".e als(t) ret.ape ( BYTE); else ("char".equals(t) ret.apend(JVM_CHAR); else ("duble".equals(t) ret.apend(JVM_DOUBLE); elsef ("float".equals(t) ret.apend(JVM_FLOAT); else i ("int".equals(t) ret.apend(JVM_INT); else ("long".equals(t) ret.apend(JVM_LONG); else if ("short".equals(t) ret.apend(JVM_SHORT);<br><br>/ 类 } else{<br><br>e pe 'L'); ret.ape (c.getName().replace('.','/'); ret.apend(';');<br><br>} returnret.toString();<br><br></th>
  </tr>
</table>


}

x

第 17 ⾏：调⽤ ClassDescriptorMapper#getDescriptorIndex(desc) ⽅法，获得类 描述编号。

【不存在】第18 ⾄ 21 ⾏：写⼊ OBJECT_DESC + 类描述(字符串) 到 mBuffer 中。

【已存在】第 22 ⾄ 26 ⾏：写⼊ OBJECT_DESC_ID + 类描述编号(编号) 到 mBuffer 中。

🙂 很明显，第⼆种的性能和体积都更好。当然，需要保证 Server 和 Client 的类描述编号是 ⼀致的。⼤多数情况下，我们只注册常⽤的数据类型到 ClassDescriptorMapper 中。

第 28 ⾏：调⽤ Builder#register(Class<T> c, boolean isAllowNonSerializable) ⽅法，获得类对应的 Builder 对象。

第 30 ⾏：调⽤ Builder#writeToT obj, GenericObjectOutput out) ⽅法，序列化 对象到 GenericObjectOutput 中的输出流。为什么可以这么做？看完 「6. Builder」 的分享，胖 友就会找到答案。🙂 卖个⼩关⼦。

- 4.1.3 addRef


<table>
  <tr>
    <th>/*<br><br>* 添加循环引⽤<br><br>*<br><br>* @param obj对象<br><br>*/ public void adRef(Object obj) {<br><br><br>mRefs.put(obj, mRefs.size() /* 引⽤编号 */);<br><br></th>
  </tr>
</table>


}

- 4.1.4 getRef


<table>
  <tr>
    <th>/*<br><br>* 获得循环引⽤编号<br><br>*<br><br>* @param obj 对象<br><br>* @return 引⽤编号<br><br>*/ public int getRef(Object obj) {<br><br><br>Integer ref= mRefs.get(obj); if (ref = nul) {<br><br>return -1;<br><br>} return ref;<br><br></th>
  </tr>
</table>


}

## 4.2 GenericObjectInput ，实现

com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectInput

ObjectInput 接⼝，继承 GenericDataInput 类，Dubbo 对象输⼊实现类。

- 4.2.1 构造⽅法
- 4.2.2 readObject


<table>
  <tr>
    <th>/*<br><br>* {@link #skipAny()} 空对象<br><br>*/ private static Object SKI PED_OBJECT = new Object(); /*<br><br>* 类描述匹配器<br><br>*/ private ClasDescriptorMapermMaper; /*<br><br>* 循环引⽤数组<br><br>*/<br><br><br></th>
  </tr>
</table>


private List<Object> mRefs = new ArayList<Object>();

<table>
  <tr>
    <th>1: @Overide<br><br>2 public ObjectreadObject() throws IOException {<br><br>3: Stringdesc;<br><br>4: / 读取字节<br><br>5 byte b= read0();<br><br>6: switch(b) {<br><br>7: case OBJECT_NUL: / NUL<br><br>8: return nul;<br><br>9: case OBJECT_DUMY: / 空对象<br><br>10: return new Object();<br><br><br>1: case OBJECT_DESC: {/ 类描述<br><br>2 desc = readUTF();<br><br>3 break;<br><br><br>14: }<br><br>15: case OBJECT_DESC_ID: {/ 类描述编号<br><br>16: / 读取类描述编号<br><br>17: int index = readUInt();<br><br>18: / 获得类描述<br><br>19: desc = mMaper.getDescriptor(index);<br><br><br>0: if (desc = nul) {<br><br>1: throw new IOException("Can notfind desc id:"+ index);<br><br>2 }<br><br>3 break;<br><br>4 }<br><br>5: default:<br><br><br>26: throw new IOException("Flageror,expect OBJECT_NUL|OBJECT_DUMY|OBJECT_DESC|OBJECT_DESC_ID,get" + b);<br><br>7: }<br><br>28: try {<br><br>29: /获得类<br><br>30: Clas<?> c = ReflectUtils.desc2clas(desc);<br><br>31: /获得类对应的序列化 Builder<br><br>32: /反序列化成对象返回<br><br><br>3 return Builder.register(c).parseFrom(this);<br><br>4 } catch(ClasNotFoundException e) {<br><br>5 thrownew IOException("Read object failed, clas notfound. " + StringUtils.toString(e);<br><br>6: }<br><br><br></th>
  </tr>
</table>


37: }

第 30 ⾏：调⽤ ReflectUtils#desc2class(desc) ⽅法，获得类。

第 33 ⾏：调⽤ Builder#register(Class<T> c, boolean isAllowNonSerializable) ⽅法，获得类对应的 Builder 对象。

第 33 ⾏：调⽤ Builder#parseFrom(GenericObjectInput) ⽅法，反序列化成对象返回。

- 4.2.3 addRef


<table>
  <tr>
    <th>/*<br><br>* 添加循环引⽤<br><br>*<br><br>* @param obj对象<br><br>*/ public void adRef(Object obj) {<br><br><br>mRefs.ad(obj);<br><br></th>
  </tr>
</table>


}

- 4.2.4 getRef
- 4.2.5 skipAny 【TODO 8035】1、已经限制的⼤⼩，这块代码没⽤了啊？！ 胖友可先⽆视这个⽅法。


<table>
  <tr>
    <th>/*<br><br>* 获得循环引⽤<br><br>*<br><br>* @param index 引⽤编号<br><br>* @return 对象<br><br>* @throws IOException 当发⽣ IO 异常时<br><br>*/ public ObjectgetRef(int index) throws IOException {<br><br><br>if (index< 0| index >= mRefs.size() {<br><br>return nul; }<br><br>/ 获得对象 Object ret =mRefs.get(index); / 在 skyAny() 设置 if (ret = SKI PED_OBJECT) { thrownew IOException("Ref ski ped-object.");<br><br>} return ret;<br><br></th>
  </tr>
</table>


}

# 5. ClassDescriptorMapper

com.alibaba.dubbo.common.serialize.support.dubbo.ClassDescriptorMapper ，类 描述匹配器接⼝。⽅法如下：

<table>
  <tr>
    <th>/ 根据类描述编号，获得类描述 String getDescriptor(int index);<br><br>/ 根据类描述，获得类描述编号<br><br></th>
  </tr>
</table>


int getDescriptorIndex(String desc);

- 5.1 DEFAULT_CLASS_DESCRIPTOR_MAPPER DEFAULT_CLASS_DESCRIPTOR_MAPPER 是 Builder 的内部属性。


<table>
  <tr>
    <th>/*<br><br>* 类描述数组<br><br>*/ private static final List<String> mDescList = new ArayList<String>(); /*<br><br>* 类描述映射<br><br>*/ private static final Map<String, Integer> mDescMap = new ConcurentHashMap<String, Integer> (); /*<br><br>* ClasDescriptorMaper默认实现类<br><br>*/ public static ClasDescriptorMaper DEFAULT_CLAS_DESCRIPTOR_MAPER = new ClasDescriptorMaper() {<br><br><br>@Overide public String getDescriptor(int index) {<br><br>if (index < 0|index>= mDescList.size() {<br><br>return nul;<br><br>} return mDescList.get(index);<br><br>} @Overide public int getDescriptorIndex(String desc) {<br><br>Integer ret = mDescMap.get(desc); return ret = nul ? -1: ret;<br><br>}<br><br></th>
  </tr>
</table>


};

在 Builder 的 static 代码块，会初始化 mDescMap 属性，代码如下：

<table>
  <tr>
    <th>static { adesc olean[].clas); adescbyte cas ad scca[].clas); ad scshort[].clas); ad scint[].clas); ad sclong[].clas); ad scfl at[].clas); adDesc(double[].clas); adesc olean.clas); adescByte.clas); adescCaracter.clas); ad scShort.clas); adescInteer.clas); ad scLongclas ad scFl at.clas); adDesc(Double.clas); ad sc .clas); adDesc(String[].clas); ad scArayList.clas); ad sc asMap.clas); adescHashSet.clas); adDescDate.clas); adDescaa.sl.Date.clas); adescaa.sl. e.clas); adescaa.sql.Timestamp.clas); adescaa.til.Li edList.clas); adescaa. il.i edasMap.clas); adDesc(java.util.LinkedHashSet.clas);<br><br>/ . 省略⽆关代码<br><br>} private static void adDesc(Clas<?> c) {<br><br>String desc = ReflectUtils.getDesc(c); / 例如，java.lang.Byte 为 Ljava/lang/Byte; / 添加到集合中 int index = mDescList.size(); escList.ad(desc); mDescMap.put(desc, index);<br><br></th>
  </tr>
</table>


}

# 6. Builder

com.alibaba.dubbo.common.serialize.support.dubbo.Builder

，实现 GenericDataFlags 接⼝，对象序列化代码构建器抽象类。功能如下：

a. a. a.

类的序列化和反序列化的抽象定义。 提供常⽤类( 例如 Integer 、Long 、Map 等等 )的 Builder 实现类。 基于 Javassist ⾃动实现⾃定义类( 例如 User 、Student 等等 )的 Builder 实现类。

🙂 ⼤体的类结构，如下图所示：

![image 6](assets/imageFile6.png)

类图

## 6.1 抽象⽅法

<table>
  <tr>
    <th>/*<br><br>* @return Builder 对应的类<br><br>*/ abstract public Clas<T> getType(); /*<br><br>* 序列化对象到 GenericObjectOutput 中的输出流。<br><br>*<br><br>* @param obj 对象<br><br>* @param out GenericObjectOutput 对象<br><br>* @throws IOException 当发⽣ IO 异常时。<br><br>*/ abstract public void writeTo(T obj, GenericObjectOutput out) throws IOException;<br><br><br>/ ↑ 调⽤上⾯⽅法 public void writeTo(T obj, OutputStreamos) throws IOException { / 将 OutputStream 封装成 GenericObjectOutput 对象 GenericObjectOutput out= new GenericObjectOutput(os); / 写⼊ writeTo(obj, out); / 刷⼊ out.flushBufer();<br><br>} /*<br><br>* 反序列化 GenericObjectInput 成对象<br><br>*<br><br>* @param in GenericObjectInput对象<br><br>* @return 对象<br><br>* @throws IOException 当 IO发⽣异常时<br><br>*/ abstract public T parseFrom(GenericObjectInputin) throws IOException;<br><br><br>/ ↑ 调⽤上⾯⽅法 public T parseFrom(InputStream is) throws IOException { return parseFrom(new GenericObjectInput(is); / 将 InputStream封装成 GenericObjectInput 对<br><br>象 }<br><br>/ ↑ 调⽤上⾯⽅法 public T parseFrom(byte[] b) throws IOException { return parseFrom(new UnsafeByteArayInputStream(b); / 将 byte[]封装成 InputStream 对象<br><br></th>
  </tr>
</table>


}

三个抽象⽅法：

对应类 序列化 反序列化

- 6.2 register


<table>
  <tr>
    <th>/*<br><br>* 实现 Serializable 接⼝的类的 Builder 对象缓存<br><br>*/ private static final Map<Clas<?>, Builder<?> BuilderMap =new ConcurentHashMap<Clas<? >, Builder<?>(); /*<br><br>* 未实现 Serializable 接⼝的类的 Builder 对象缓存<br><br>*/ private staic final Map<Clas<?>, Builder<?> nonSerializableBuilderMap = new ConcurentHashMap<Clas<?>, Builder<?>();<br><br><br>1: public static <T> Builder<T> register(Clas<T> c, bolean isAlowNonSerializable) {<br><br>2: / Object 类，或者接⼝，使⽤ GenericBuilder<br><br>3 if (c = Object.clas| c.isInterface() {<br><br>4 return (Builder<T>) GenericBuilder;<br><br>5: }<br><br>6: / Aray 类型，使⽤ GenericArayBuilder<br><br>7: if (c = Object[].clas) {<br><br>8 return (Builder<T>) GenericArayBuilder;<br><br>9: }<br><br>10:<br><br><br>1: / 获得 Builder 对象<br><br>2 Builder<T> b = (Builder<T>) BuilderMap.get(c);<br><br>3 if (nul != b) {<br><br>4 return b;<br><br>5 }<br><br><br>16:<br><br>17: / 要求实现 Serializable 接⼝，但是并未实现，则抛出 IlegalStateException异常 8 bolean isSerializable = Serializable.clas.isAsignableFrom(c);<br><br><br>19: if (!isAlowNonSerializable & !isSerializable) { 0: throw new IlegalStateException("Serialized clas " + c.getName() + 21: " must implement java.io.Serializable (dubocodec seting: isAlowNonSerializable<br><br>=false)"); 2 }<br><br>23:<br><br>24: / 获得 Builder 对象<br><br><br>5 b = (Builder<T>) nonSerializableBuilderMap.get(c);<br><br>6: if (nul != b) {<br><br>7: return b;<br><br>8 }<br><br><br>29:<br><br>30: / 不存在，使⽤ Javasist ⽣成对应的 Builder 类，并进⾏创建 Builder 对象。 1: b = newBuilder(c);<br><br><br>32:<br><br>3: / 添加到 Builder 对象缓存中<br><br>4 if (isSerializable) {<br><br>5 BuilderMap.put(c, b);<br><br>6: } else {<br><br>7: nonSerializableBuilderMap.put(c,b);<br><br>8 }<br><br><br>39 0: return b;<br><br></th>
  </tr>
</table>


#### 41: }

代码⽐较易懂，胖友看下注释哈。⽐较奇怪的是 c.isInterface() 的判断，为什么使 ⽤ GenericBuilder对象。在 「6.4 GenericBuilder」 会看到答案。

- 6.3 常⽤数据类型的 Builder 实现 在 static 代码块，初始化了常⽤数据类型的 Builder 实现，代码如下图：


![image 7](assets/imageFile7.png)

常⽤数据类型的 Builder 实现 代码⽐较简单，胖友点击 ，⾃⼰查看。这⾥我们就以 HashMap 的 Builder 举例⼦，代码如下：

链接

<table>
  <tr>
    <th>register(HashMap.clas,new Builder<HashMap>() {<br><br>@Overide public Clas<HashMap> getType() {<br><br>return HashMap.clas;<br><br>} @Overide public void writeTo(HashMap obj, GenericObjectOutput out) throws IOException {<br><br>/ NUL ，写⼊ OBJECT_NUL 到 mBufer 中 if (obj = nul) {<br><br>out.write0(OBJECT_NUL); / HashMap ⾮空<br><br>} else {<br><br>/ 写⼊ OBJECT_MAP 到 mBufer 中 out.write0(OBJECT_MAP);<br><br>/ 写⼊ Length(Map ⼤⼩) 到 mBufer 中 out.writeUInt(obj.size(); / 写⼊ KV 到 mBufer 中<br><br>for (Map.Entryentry : (Set<Map.Entry>) obj.entrySet(){<br><br>t. rte et(etry.etKey(); out.writeObject(entry.getValue(); }<br><br>}<br><br>} @Overide public HashMap parseFrom(GenericObjectInput in) throws IOException {<br><br>/ 读取⾸位字节<br><br>byte b = in.read0(); / NUL ，返回 nul<br><br>if (b = OBJECT_NUL) { return nul;<br><br>} if (b != OBJECT_MAP) {<br><br>throw new IOException("Input format eror, expect OBJECT_NUL|OBJECT_MAP, get" + b + ".");<br><br>}<br><br>/ 读取 Length(Map ⼤⼩) int len = in.readUInt();<br><br>/ 循环读取 KV 到 HashMap HashMap ret = new HashMap(len); for (int i = 0;i < len; i +) {<br><br>ret.put(in.readObject(), in.readObject();<br><br>} return ret;<br><br>}<br><br></th>
  </tr>
</table>


});

- 6.4 GenericBuilder GenericBuilder ，实现 Builder 接⼝，通⽤ Object 的 Builder 对象。代码如下：


<table>
  <tr>
    <th>staticfinal Builder<Object> GenericBuilder =new Builder<Object>() {<br><br>@Overide public Clas<Object> getType() {<br><br>return Object.clas;<br><br>} @Overide public void writeTo(Object obj, GenericObjectOutput out) throws IOException {<br><br>out.writeObject(obj);<br><br>} @Overide public Object parseFrom(GenericObjectInput in) throws IOException {<br><br>return in.readObject(); }<br><br></th>
  </tr>
</table>


};

适⽤于所有对象。为什么这么说呢？我们以 #writeTo(Object obj, GenericObjectOutput out) ⽅法，举例⼦。在该⽅法中，会调⽤ GenericObjectOutput#writeObject(obj) ⽅法，那 么在这个过程中，会获得 obj对象，真正的 Builder 对象，从⽽序列化。如下图所示：

![image 8](assets/imageFile8.png)

常⽤数据类型的 Builder 实现

- 6.5 SerializableBuilder SerializableBuilder 属性，实现 Builder 接⼝，通⽤ Serializable 的 Builder 对象，使⽤ Java 原 ⽣序列化⽅式实现。⽬前使⽤在：


Throwable 对象。

带有 transient 修饰符属性的 Serializable 实现类。

因为是⼴泛匹配，所以不适合调⽤ #register(Class<T> c, boolean isAllowNonSerializable) ⽅法，进⾏注册。⽽是在 #newObjectBuilder(Class<?> c) ⽅ 法，通过硬编码判断匹配返回。 实现代码如下：

staticfinal Builder<Serializable> SerializableBuilder =new Builder<Serializable>() {

@Overide public Clas<Serializable> getType() {

return Serializable.clas;

} @Overide public void writeTo(Serializable obj, GenericObjectOutput out) throws IOException {

/ NUL ，写⼊ OBJECT_NUL 到 mBufer 中 if (obj = nul) {

out.write0(OBJECT_NUL); / ⾮空 } else {

/ 写⼊ OBJECT_STREAM 到 mBufer 中 out.write0(OBJECT_STREAM);

/ 使⽤ compactjava 序列化实现，进⾏序列化 UnsafeByteArayOutputStream bos = new UnsafeByteArayOutputStream(); CompactedObjectOutputStream os = new CompactedObjectOutputStream(bos);

owriteObject(obj);

sflush(); os.close();

byte[] b = bos.toByteAray();

/ 写⼊ Length( 字节数组⻓度 ) 到 mBufer 中 out.writeUInt(b.length);

/ 写⼊ 字节数组 到 mBufer 中 out.write0(b, 0, b.length);

}

} @Overide public Serializable parseFrom(GenericObjectInput in)throws IOException {

/ 读取⾸位字节

byte b = in.read0(); / NUL ，返回 nul

if (b = OBJECT_NUL) { return nul;

} if (b != OBJECT_STREAM) {

throw new IOException("Input format eror, expect OBJECT_NUL|OBJECT_STREAM, get " + b + ".");

}

/ 使⽤ compactjava 序列化实现，进⾏反序列化 UnsafeByteArayInputStream bis = new

UnsafeByteArayInputStream(in.read0(in.readUInt( ); CompactedObjectInputStream ois = new CompactedObjectInputStream(bis); try{

return (Serializable) ois.readObject(); } catch (ClasNotFoundException e) {

throw new IOException(StringUtils.toString(e); }

}

};

- 6.6 AbstractObjectBuilder AbstractObjectBuilder ，实现 Builder 接⼝，Builder 抽象类。主要实现了循环引⽤对象的⽀持。代码 如下：


@Overide public void writeTo(Tobj, GenericObjectOutput out) throws IOException {

/ NUL ，写⼊ OBJECT_NUL 到 mBufer 中 if (obj = nul){

out.write0(OBJECT_NUL); } else {

/ 读取循环引⽤对象编号 int ref= out.getRef(obj); if (ref< 0){/ 不存在

/ 添加到循环引⽤中，从⽽获得编号。下次在写⼊相等对象时，可使⽤循环引⽤编号的⽅式。 out.adRef(obj);

/ 写⼊ OBJECT 到 mBufer 中 out.write0(OBJECT);

/ 写⼊ 对象 到 mBufer 中。 writeObject(obj,out);

}else {/ 存在

/ 写⼊ OBJECT_REF到 mBufer 中 out.write0(OBJECT_REF);

/ 写⼊ 循环引⽤对象编号 到 mBufer 中 out.writeUInt(ref);

} }

} @Overide public T parseFrom(GenericObjectInput in) throws IOException{

/ 读取⾸位字节 byte b = in.read0(); switch (b) { / 对象

case OBJECT:{ / 创建对象

T ret = newInstance(in);

/ 添加到循环引⽤中，从⽽获得编号。下次在读取到循环引⽤对象编号时，可直接获取到该对 象。

in.adRef(ret);

/ 反序列化 GenericObjectInput 到对象 readObject(ret, in);

/ 返回

return ret; }

/ 循环引⽤对象编号 case OBJECT_REF: / 读取循环引⽤对象编号 / 获得对应的对象

return (T) in.getRef(in.readUInt(); / NUL，返回 nul

case OBJECT_NUL:

return nul; default:

throw newIOException("Input format eror, expectOBJECT|OBJECT_REF|OBJECT_NUL, get " + b);

} }

和 Builder 提供的三个抽象⽅法⼀⼀对应，AbstractObjectBuilder 也定义了三个抽象⽅法：

<table>
  <tr>
    <th>/*<br><br>* 创建 Builder 对应类的对象<br><br>*<br><br>* @param in GenericObjectInput 对象<br><br>* @return 对应类的对象<br><br>* @throws IOException 当 IO发⽣异常时<br><br>*/ abstract protected T newInstance(GenericObjectInput in) throwsIOException; /*<br><br>* 序列化对象到 GenericObjectOutput 中的输出流。<br><br>*<br><br>* @param obj 对象<br><br>* @param out GenericObjectOutput 对象<br><br>* @throws IOException 当 IO发⽣异常时<br><br>*/ abstract protected void writeObject(T obj, GenericObjectOutputout) throws IOException; /*<br><br>* 反序列化 GenericObjectInput 到对象<br><br>*<br><br>* @param ret 对象。<br><br>* 该对象在 {@link #parseFrom(GenericObjectInput)} 中，调⽤ {@link #newInstance(GenericObjectInput)} 创建<br><br>* @param in GenericObjectInput 对象<br><br>* @throws IOException 当 IO发⽣异常时<br><br>*/<br><br><br></th>
  </tr>
</table>


abstract protected void readObject(T ret, GenericObjectInput in) throws IOException;

- 6.6.1 GenericArrayBuilder GenericArrayBuilder ，实现 AbstractObjectBuilder 抽象类，通⽤数组( Array ) 的 Builder 对象。 代码如下：


<table>
  <tr>
    <th>staticfinal Builder<Object[]> GenericArayBuilder =new AbstractObjectBuilder<Object[]>() {<br><br>@Overide public Clas<Object[]> getType() {<br><br>return Object[].clas;<br><br>} @Overide protected Object[] newInstance(GenericObjectInput in) throws IOException {<br><br>/ 读取数组⻓度，并创建数组对象 return new Object[in.readUInt()];<br><br>} @Overide protected void readObject(Object[] ret, GenericObjectInput in) throws IOException {<br><br>/ 循环读取每个对象到 ret 中 for(int i = 0; i < ret.length; i +) {<br><br>ret[i] = in.readObject(); }<br><br>} @Overide protected void writeObject(Object[] obj, GenericObjectOutput out) throws IOException {<br><br>/ 写⼊ Length( 数组⼤⼩ ) 到 mBufer out.writeUInt(obj.length);<br><br>/ 循环写⼊每个对象到 mBufer 中 for(Object item : obj) {<br><br>out.writeObject(item); }<br><br>}<br><br></th>
  </tr>
</table>


};

因为 GenericArrayBuilder 实现 AbstractObjectBuilder 抽象类，所以，若数组中有相等的元 素，可以使⽤循环引⽤的功能，从⽽提升解析速度，降低体积。

- 6.6.2 其他⼦类 在 #newObjectBuilder(Class<?> c) 中，基于 Javassist ⾃动实现每个类的 Builder 类，实现的 就是 AbstractObjectBuilder 抽象类。 6.5 newBuilder


<table>
  <tr>
    <th>privatestatic <T>Builder<T>newBuilder(Clas<T> c){<br><br>/基础类型，已经内置相应的 Builder 实现类，抛出 RuntimeException异常。因为，已经在 GenericDataInput 和 GenericDataOutput 实现。<br><br>if(c.isPrimitive() { thrownew RuntimeException("Cannot create builderfor primitive type: " + c);<br><br>} if(loger.isInfoEnabled()<br><br>loger.info("create Builder forclas: " + c); Builder<?> builder;<br><br>/创建 Aray Builder 对象 if(c.isAray() {<br><br>builder = newArayBuilder(c); /创建 Object Builder 对象<br><br>} else { builder = newObjectBuilder(c);<br><br>} return (Builder<T>) builder;<br><br></th>
  </tr>
</table>


}

- 6.5.1 newObjectBuilder #newObjectBuilder(Class<?> c) ，基于 Javassist ⾃动实现每个类的 Builder 类( 继 承 AbstractObjectBuilder抽象类 )，并创建对应的 Builder 对象。代码超级冗⻓，⽼艿艿已经添加好 了详细的代码注释，胖友点击 ⾃⼰查看。 实现原理，简单的说，其实就是，循环类的每个属性，拼接对应的序列化和反序列化的过程的代码字 符串，最终提交给 Javassist ⽣成类。 良⼼如我，如下是⼀个示例：


链接

Student 和 Info 类 ：

<table>
  <tr>
    <th>package com.alibaba.dubo.comon.serialize.dubo; / .省略 import<br><br>publicclas YunaiBuilderTest { public static clas Student implements Serializable {<br><br>ui rin username; public String pasword;<br><br>i nfo info1; public Info info2; public Student student; public final int a = 3;<br><br>} public static clas Info implements Serializable {<br><br>public String key; }<br><br></th>
  </tr>
</table>


}

Student 对应的 Builder 类：

![image 9](assets/imageFile9.png)

Student 对应的 Builder 类

Info 对象的 Builder 类：

![image 10](assets/imageFile10.png)

Info 对应的 Builder 类

- 6.5.2 newEnumBuilder #newEnumBuilder(Class<?> c) ，基于 Javassist ⾃动实现每个类的 Builder 类( 继承 Builder 接 ⼝ )，并创建对应的 Builder 对象。代码⽐较易懂，⽼艿艿已经添加好了详细的代码注释，胖友点击

⾃⼰查看。 实现原理，粗暴的说，序列化使⽤ enum#name() ⽅法，反序列化使⽤ Enum#valueOf(Class<T> enumType, String name) ⽅法。

- 6.5.3 newArrayBuilder #newArrayBuilder(Class<?> c) ，基于 Javassist ⾃动实现每个类的 Builder 类( 继 承 Builder 接⼝ )，并创建对应的 Builder 对象。代码⽐较易懂，⽼艿艿已经添加好了详细的代码注 释，胖友点击 ⾃⼰查看。 实现原理，直接的说，循环数组的每个元素，拼接对应的序列化和反序列化的过程的代码字符串，最 终提交给 Javassist ⽣成类。 🙂 ⽐较有意思的是，多维数组的处理，例如 int[][][] 。胖友可以想想。实际，也是⽐较简单的。


链 接

链接

# 666. 彩蛋

⼤四( 2012 )的时候，写了⾃⼰的序列化实现 ，基于 Protobuf 的配置⽂ 件 proto ，读取，⽣成序列化和反序列化的静态类，基本零优化。 现在回头看了 Dubbo 序列化的实现，还是收益良多。美滋滋。

Ludaima_Protobuf
