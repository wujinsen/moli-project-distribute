现在的Solidity中⽂⽂档，要么翻译的太烂，要么太旧，决定重新翻译下。 尤其点名批评极客学院名为《Solidity官⽅⽂档中⽂版》的翻译，机器翻译的都⽐它好，⼤家还是别看 了。

# 写在前⾯

Solidity是以太坊智能合约编程语⾔，阅读本⽂前，你应该对以太坊、智能合约有所了解， 如果你还不了解，建议你先看 Solidity教程会是⼀系列⽂章，本⽂是第⼀篇：介绍Solidity的变量类型。 Solidity 系列完整的⽂章列表请查看 。 本⽂前半部分是参考Solidity官⽅⽂档（当前最新版本：0.4.20）进⾏翻译，后半部分是结合实际合约 代码实例说明类型的使⽤（仅针对 订阅⽤户）。

以太坊是什么

分类-Solidity

专栏

# 类型

Solidity是⼀种静态类型语⾔，意味着每个变量（本地或状态变量）需要在编译时指定变量的类型（或 ⾄少可以推倒出类型）。Solidity提供了⼀些基本类型可以⽤来组合成复杂类型。 Solidity类型分为两类：

值类型(Value Type) - 变量在赋值或传参时，总是进⾏值拷⻉。 引⽤类型(Reference Types)

值类型(Value Type)

值类型包含:

布尔类型(Boleans) 整型(Integers) 定⻓浮点型(Fixed Point Numbers) 定⻓字节数组(Fixed-size byte arays) 有理数和整型常量(Rational and Integer Literals) 字符串常量（String literals） ⼗六进制常量（Hexadecimal literals） 枚举(Enums) 函数类型(Function Types) 地址类型(Adres) 地址常量(Adres Literals)

及 (Adres)有单独的博⽂，请点击查看。

函数类型 地址类型

# 布尔类型(Boleans)

布尔(bol):可能的取值为常量值true和false。 布尔类型⽀持的运算符有：

！逻辑⾮

& 逻辑与 | 逻辑或 = 等于

!= 不等于

注意：运算符 &和 |是短路运算符，如f(x)|g(y)，当f(x)为真时，则不会继续执⾏g(y)。

# 整型(Integers)

int/uint: 表示有符号和⽆符号不同位数整数。⽀持关键字uint8 到 uint256 (以8步进)， uint 和 int 默认对应的是 uint256 和 int256。 ⽀持的运算符：

⽐较运算符： <=, < , =, !=, >=, > (返回布尔值：true 或 false) 位操作符： &，|，^(异或)，~（位取反） 算术操作符：+，-，⼀元运算-，⼀元运算+，， /, %(取 余 数 ), *（幂）, < (左移位), >(右移位)

说明：

- 1.
- 2.
- 3.
- 4.


整数除法总是截断的，但如果运算符是字⾯量（字⾯量稍后讲)，则不会截断。 整数除0会抛异常。 移位运算的结果的正负取决于操作符左边的数。x < y 和 x2*y 是相等， x > y 和 x / 2*y 是相 等的。 不能进⾏负移位，即操作符右边的数不可以为负数，否则会抛出运⾏时异常。

注意：Solidity中，右移位是和除等价的，因此右移位⼀个负数，向下取整时会为0，⽽不像其他语⾔ ⾥为⽆限负⼩数。

# 定⻓浮点型（Fixed Point Numbers）

注意：定⻓浮点型 Solidity（发⽂时）还不完全⽀持，它可以⽤来声明变量，但不可以⽤来赋值。 fixed/ufixed: 表示有符号和⽆符号的固定位浮点数。关键字为ufixedMxN 和 ufixedMxN。

- M表示这个类型要占⽤的位数，以8步进，可为8到256位。

- N表示⼩数点的个数，可为0到80之前 ⽀持的运算符：


⽐较运算符： <=, < , =, !=, >=, > (返回布尔值：true 或 false) 算术操作符：+，-，⼀元运算-，⼀元运算+，， /, %(取 余 数 )

注 意 ： 它 和 ⼤ 多 数 语 ⾔ 的 float和 double不 ⼀ 样 ， *M是表示整个数占⽤的固定位数，包含整数部分和⼩ 数部分。因此⽤⼀个⼩位数（M较⼩）来表示⼀个浮点数时，⼩数部分会⼏乎占⽤整个空间。

# 定⻓字节数组(Fixed-size byte arays)

关键字有：bytes1, bytes2, bytes3, …, bytes32。（以步⻓1递增） byte代表bytes1。 ⽀持的运算符：

⽐较符: <=, <, =, !=, >=, > (返回bol） 位操作符: &, |, ^ (按位异或)，~（按位取反）, < (左移位), > (右移位) 索引（下标）访问: 如果x是bytesI，当0 <= k < I ，则x[k]返回第k个字节（只读）。

移位运算和整数类似，移位运算的结果的正负取决于操作符左边的数，且不能进⾏负移位。 如可以-5<1, 不可以5<-1 成员变量：

.length：表示这个字节数组的⻓度（只读）。

# 变⻓（动态分配⼤⼩）字节数组（Dynamicaly-sized byte aray）

bytes:动态分配⼤⼩字节数组, 参⻅Arays,不是值类型! string:动态分配⼤⼩UTF8编码的字符类型,参看Arays。不是值类型!

根据经验： bytes⽤来存储任意⻓度的字节数据，string⽤来存储任意⻓度的(UTF-8编码)的字符串数据。 如果⻓度可以确定，尽量使⽤定⻓的如byte1到byte32中的⼀个，因为这样更省空间。

# 有理数和整型常量(Rational and Integer Literals)

也有⼈把Literals翻译为字⾯量 整型常量是有⼀系列0-9的数字组成，10进制表示，⽐如：8进制是不存在的，前置0在Solidity中是⽆ 效的。 10进制⼩数常量（Decimal fraction literals）带了⼀个.， 在.的两边⾄少有⼀个数字，有效的表示 如:1.,.1 和 1.3. 科学符号也⽀持，基数可以是⼩数，指数必须是整数， 有效的表示如:2e10,-2e10,2e-10,2.5e1。 数字常量表达式本身⽀持任意精度，也就是可以不会运算溢出，或除法截断。但当它被转换成对应的 ⾮常量类型，或者将他们与⾮常量进⾏运算，则不能保证精度了。 如：(2*80 + 1) - 2*80的结果为1（uint8整类) ，尽管中间结果已经超过计算机字⻓。另外：.5 * 8 的结果是4，尽管有⾮整形参与了运算。 只要操作数是整形，整型⽀持的运算符都适⽤于整型常量表达式。 如果两个操作数是⼩数，则不允许进⾏位运算，指数也不能是⼩数。 注意： Solidity对每⼀个有理数都有⼀个数值常量类型。整数常量和有理数常量从属于数字常量。所有的数字 常表达式的结果都属于数字常量。所以1 + 2和2 + 1都属于同样的有理数的数字常量3 警告： 整数常量除法，在早期的版本中是被截断的，但现在可以被转为有理数了，如5/2的值为 2.5 注意： 数字常量表达式，⼀旦其中含有常量表达式，它就会被转为⼀个⾮常量类型。下⾯代码中表达式的结 果将会被认为是⼀个有理数：

<table>
  <tr>
    <th>1</th>
    <th>uint128 a = 1;</th>
  </tr>
</table>


2 uint128 b = 2.5 + a + 0.5;

上述代码编译不能通过，因为b会被编译器认为是⼩数型。

# 字符串常量

字符串常量是指由单引号，或双引号引起来的字符串 (“fo” or ‘barʼ)。字符串并不像C语⾔，包含结束 符，”fo”这个字符串⼤⼩仅为三个字节。和整数常量⼀样，字符串的⻓度类型可以是变⻓的。字符串 可以隐式的转换为byte1,…byte32 如果适合，也会转为bytes或string。 字符串常量⽀持转义字符，⽐如\n，\xN，\u N。其中\xN表示16进制值，最终转换合适的字 节。⽽\u N表示Unicode编码值，最终会转换为UTF8的序列。

# ⼗六进制常量（Hexadecimal literals）

⼗六进制常量，以关键字hex打头，后⾯紧跟⽤单或双引号包裹的字符串，内容是⼗六进制字符串，如 hex”012f”。 它的值会⽤⼆进制来表示。 ⼗六进制常量和字符串常量类似，也可以转换为字节数组。

# 枚举（Enums）

在Solidity中，枚举可以⽤来⾃定义类型。它可以显示的转换与整数进⾏转换，但不能进⾏隐式转换。 显示的转换会在运⾏时检查数值范围，如果不匹配，将会引起异常。枚举类型应⾄少有⼀名成员。下 ⾯是⼀个枚举的例⼦：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br></th>
    <th>pragma solidity ^0.4.0; contract test {<br><br>enum ActionChoices { GoLeft, GoRight, GoStraight,<br><br>SitStil } ActionChoices choice; ActionChoices constant defaultChoice =<br><br>ActionChoices.GoStraight; function setGoStraight() { choice = ActionChoices.GoStraight; }<br><br>/ Since enum types are not part of the ABI, the signature of "getChoice"<br><br>/ wil automaticaly be changed to "getChoice() returns (uint8)"<br><br>/ for al maters external to Solidity. The integer type used is just<br><br>/ large enough to hold al enum values, i.e. if you have more values,<br><br>/ `uint16` wil be used and so on. function getChoice() returns (ActionChoices) { return choice; } function getDefaultChoice() returns (uint) { return uint(defaultChoice); }</th>
  </tr>
</table>


}

# 代码实例

通过合约代码实例说明类型的使⽤，请订阅 查看。

区块链技术

# 参考⽂档

Solidity官⽅⽂档-类型 深⼊浅出区块链

- 系统学习区块链，打造最好的区块链技术博客。 我的 为各位解答区块链技术问题，欢迎加⼊讨论。

知识星球

