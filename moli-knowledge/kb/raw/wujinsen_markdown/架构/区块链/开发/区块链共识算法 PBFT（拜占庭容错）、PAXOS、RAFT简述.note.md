# 共识算法

区块链中最重要的便是共识算法，⽐特币使⽤的是POS（Prof of Work，⼯作量证明），以太币使⽤ 的是POS（Prof of Stake，股权证明）使得算理便的不怎么重要了，⽽今POS的变体DPOS （Delegated Prof of Stake，股份授权证明）进⼀步削减算⼒的浪费，同时也加强了区块链的安全 性。 不过，对于不需要货币体系的许可链或者私有链⽽⾔，绝对信任的节点，以及⾼效的需求上述共识算 法并不能够提供，因此对于这样的区块链，传统的⼀致性算法成为⾸选，PBFT（拜占庭容错）、 PAXOS、RAFT。

# PBFT（拜占庭容错）

基于拜占庭将军问题，⼀致性的确保主要分为这三个阶段：预准备（pre-prepare）、准备(prepare)和 确认(comit)。流程如下图所示：

![image 1](<区块链共识算法 PBFT（拜占庭容错）、PAXOS、RAFT简述.note_images/imageFile1.png>)

其中C为发送请求端，0123为服务端，3为宕机的服务端，具体步骤如下：

- 1. Request：请求端C发送请求到任意⼀节点，这⾥是0

- 2. Pre-Prepare：服务端0收到C的请求后进⾏⼴播，扩散⾄123

- 3. Prepare：123,收到后记录并再次⼴播，1->023，2->013，3因为宕机⽆法⼴播

- 4. Comit：0123节点在Prepare阶段，若收到超过⼀定数量的相同请求，则进⼊Comit阶段，⼴播 Comit请求

- 5.Reply：0123节点在Comit阶段，若收到超过⼀定数量的相同请求，则对C进⾏反馈


根据上述流程，在 N ≥ 3F + 1 的情況下⼀致性是可能解決，N为总计算机数，F为有问题的计算机总数

- N=4 F=0 时：


<table>
  <tr>
    <th> </th>
    <th>得到数据</th>
    <th>最终数据</th>
  </tr>
  <tr>
    <td>A</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>B</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>C</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>D</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
</table>


- N=4 F=1 时：

- N=4 F=2 时：


<table>
  <tr>
    <th> </th>
    <th>得到数据</th>
    <th>最终数据</th>
  </tr>
  <tr>
    <td>A</td>
    <td>1 1 1 0</td>
    <td>1</td>
  </tr>
  <tr>
    <td>B</td>
    <td>1 1 0 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>C</td>
    <td>1 0 1 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>D</td>
    <td>0 1 1 1</td>
    <td>1</td>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th>得到数据</th>
    <th>最终数据</th>
  </tr>
  <tr>
    <td>A</td>
    <td>1 1 0 0</td>
    <td>NA</td>
  </tr>
  <tr>
    <td>B</td>
    <td>1 0 0 1</td>
    <td>NA</td>
  </tr>
  <tr>
    <td>C</td>
    <td>0 0 1 1</td>
    <td>NA</td>
  </tr>
  <tr>
    <td>D</td>
    <td>0 1 1 0</td>
    <td>NA</td>
  </tr>
</table>


由此可以看出，拜占庭容错能够容纳将近1/3的错误节点误差，IBM创建的Hyperledger就是使⽤了该算 法作为共识算法。

# PAXOS

PAXOS是⼀种基于消息传递且具有⾼度容错特性的⼀致性算法。

算法本身⽤语⾔描述极其精简：

- phase 1


- a) proposer向⽹络内超过半数的aceptor发送prepare消息


- b) aceptor正常情况下回复promise消息


- phase 2


- a) 在有⾜够多aceptor回复promise消息时，proposer发送acept消息

- b) 正常情况下aceptor回复acepted消息


PAXOS中有三类⻆⾊Proposer、Aceptor及Learner，主要交互过程在Proposer和Aceptor之间，做 成图便如下图所示：

![image 2](<区块链共识算法 PBFT（拜占庭容错）、PAXOS、RAFT简述.note_images/imageFile2.png>)

其中1,2,3,4代表顺序。

以下图描述多Proposer的情况，T代表时间轴，图中仅画全⼀个Proposer与Aceptor的关系：

![image 3](<区块链共识算法 PBFT（拜占庭容错）、PAXOS、RAFT简述.note_images/imageFile3.png>)

A3在T1发出acepted给A1，然后在T2收到A5的prepare，在T3的时候A1才通知A5最终结果(税率 10%)。这⾥会有两种情况：

- 1. A5发来的N5⼩于A1发出去的N1，那么A3直接拒绝(reject)A5

- 2. A5发来的N5⼤于A1发出去的N1，那么A3回复promise，但带上A1的(N1, 10%) 最终A5也会接受10%


![image 4](<区块链共识算法 PBFT（拜占庭容错）、PAXOS、RAFT简述.note_images/imageFile4.png>)

上图描述，如果已经Promise⼀个更⼤的N，那么会直接Reject更⼩的N

![image 5](<区块链共识算法 PBFT（拜占庭容错）、PAXOS、RAFT简述.note_images/imageFile5.png>)

上述描述了，即使Promise了⼀个N，如果在未Acepted前，再收到⼀个更⼤的N，那么依旧会Reject 那个即使已经Promise的N

总流程图氪概括如下：

![image 6](<区块链共识算法 PBFT（拜占庭容错）、PAXOS、RAFT简述.note_images/imageFile6.png>)

PAXOS协议⽤于微信PaxosStore中，每分钟调⽤Paxos协议过程数⼗亿次量级。

# RAFT

RAFT核⼼思想很容易理解，如果数个数据库，初始状态⼀致，只要之后的进⾏的操作⼀致，就能保证 之后的数据⼀致。由此RAFT使⽤的是Log进⾏同步，并且将服务器分为三中⻆⾊：Leader， Folower，Candidate，相互可以互相转换。 RAFT从⼤的⻆度看，分为两个过程：

- 1. 选举Leader

- 2. Leader⽣成Log，并与Folower进⾏Headbeats同步


选举Leader

Folower⾃增当前任期，转换为Candidate，对⾃⼰投票，并发起RequestVote RPC，等待下⾯三种情 形发⽣；

- 1. 获得超过半数服务器的投票，赢得选举，成为Leader

- 2. 另⼀台服务器赢得选举，并接收到对应的⼼跳，成为Folower

- 3. 选举超时，没有任何⼀台服务器赢得选举，⾃增当前任期，重新发起选举


同步⽇志

Leader接受客户端请求，Leader更新⽇志，并向所有Folower发送Heatbeats，同步⽇志。所有 Folwer都有ElectionTimeout，如果在ElectionTimeout时间之内，没有收到Leader的Headbeats，则认 为Leader失效，重新选举Leader

流程图示：

![image 7](<区块链共识算法 PBFT（拜占庭容错）、PAXOS、RAFT简述.note_images/imageFile7.png>)

安全性保证

- 1. ⽇志的流向只有Leader到Folower，并且Leader不能覆盖⽇志

- 2. ⽇志不是最新者不能成为Candidate


动画演示RAFT：htp:/thesecretlivesofdata.com/raft/

# 总结

以上三种⼀致性算法仅仅只是核⼼思路⽽已，如果要具体实现当然还有很多⽅⾯需要进⼀步的完善。 以上三种算法都可以作为区块链的共识算法，并且部分公司已经开始使⽤，不过最出名的还应属IBM的 Hyperledger使⽤的PBFT共识算法。

<table>
  <tr>
    <th> </th>
    <th>得到数据</th>
    <th>最终数据</th>
  </tr>
  <tr>
    <td>A</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>B</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>C</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
  <tr>
    <td>D</td>
    <td>1 1 1 1</td>
    <td>1</td>
  </tr>
</table>


