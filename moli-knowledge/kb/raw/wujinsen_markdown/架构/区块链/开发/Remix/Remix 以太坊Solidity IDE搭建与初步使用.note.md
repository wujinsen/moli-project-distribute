# 以太坊：

因为以太坊为开源社区，虽然东⻄很优秀，但是组件⼗分的杂乱，因此⾸先简单介绍下以太坊的⼀些 常⽤组件：

- 1. Geth: Geth是由以太坊基⾦会提供的官⽅客户端软件，⽤Go编程语⾔编写的。

- 2. Parity： Parity 是对以太坊协议的另⼀个很好的实现，并且是⽤Rust编程语⾔编写的。 这是⼀个由⼀家名为 Parity Inc的公司来维护的⾮官⽅客户端。任何⼈都可以实现这个客户端软件，并加⼊以太坊⽹络。

- 3. Solidity： Solidity是⽤于编写在以太坊区块链上运⾏的智能合约的最流⾏的编程语⾔。 它是⼀种⾼级语⾔，当编 译转换为 EVM 字节码。 这与 Java ⾮常相似，其中有诸如 Scala，Grovy，Clojure，JRuby等JVM语 ⾔。所有这些编译都⽣成在JVM（Java虚拟机）中运⾏的字节码。

- 4.Trufle： Trufle 和 Embark 是⽤于开发以太坊 DAps的两个最常⽤的框架。 它们抽象出在区块链上编译和部署 合同的许多复杂的东⻄。

- 5. Web3.js： javascript库，可以⽤来与⼀个节点进⾏交互。 由于它是⼀个 JavaScript 库，您可以使⽤它来构建基于 Web的daps。

- 6 Mix: 以太坊早期IDE，⽤于合约的编写测试等，现已经停⽌更新，项⽬组也合并⼊Remix。

- 7. Remix： 以太坊官⽅推荐的智能合约开发IDE，适合新⼿，可以在浏览器中快速部署测试智能合约。


我以前的⽂章中有介绍过Geth和Trufle的部署，有需求的可以到区块链分类⾥⾯找找。

# Remix配置：

声明：本⽂的⼀切配置都是基于Unbuntu的。

1.安装Remix：

官⽅地址: htps:/github.com/ethereum/browser-solidity

安装步骤：

[html]

view plain copy

- 1.
- 2.
- 3.
- 4.


git clone https://github.com/ethereum/browser-solidity cd browser-solidity npm install npm run prepublish

启动命令：

[html]

view plain copy

- 1.


npm start

![image 1](<Remix 以太坊Solidity IDE搭建与初步使用.note_images/imageFile1.png>)

访问Remix： htp:/127.0.0.1 8080 界⾯如下：

![image 2](<Remix 以太坊Solidity IDE搭建与初步使用.note_images/imageFile2.png>)

⾄此，Remix算是配置完成了。

## 2.初步使⽤：

- 1.当前的solidity版本，如上图截图所示为0.4.9，这个默认⽤的是当前最新的release版本：

- 2. 点击下拉框，可以选择不同的版本，包括还未成熟的最新构建版本，或者是之前的版本等。 选择 release版本，如下图所示的这些 ：


![image 3](<Remix 以太坊Solidity IDE搭建与初步使用.note_images/imageFile3.png>)

![image 4](<Remix 以太坊Solidity IDE搭建与初步使用.note_images/imageFile4.png>)

### 3. 合约⾃动编译后⽣成:

![image 5](<Remix 以太坊Solidity IDE搭建与初步使用.note_images/imageFile5.png>)

### 4. 点击create,会在内存中将该智能合约创建⼀个实例，即将下⾯的web3 deploy代码部署在虚拟的内 存中:

![image 6](<Remix 以太坊Solidity IDE搭建与初步使用.note_images/imageFile6.png>)

