---
title: RocketMQ 源码分析 —— 事务消息.note（原文插图 annex）
slug: annex-RocketMQ-源码分析-——-事务消息
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/源码分析/RocketMQ/RocketMQ 源码分析 —— 事务消息.note.md
related: [rocketmq-事务消息实践]
created: 2026-07-05
updated: 2026-07-05
---

# 1. 概述

必须必须必须 前置阅读内容：

《事务消息（阿⾥云）》

# 2. 事务消息发送

- 2.1Producer发送事务消息


活动图如下（结合 核⼼代码 理解）：

![image 1](assets/imageFile1.png)

Producer发送事务消息

实现代码如下：

- 1: / ⬇ ⬇ ⬇ 【DefaultMQProducerImpl.java】
- 2: /*
- 3: * 发送事务消息
- 4: *
- 5: * @param msg 消息
- 6: * @param tranExecuter 【本地事务】执⾏器
- 7: * @param arg 【本地事务】执⾏器参数
- 8: * @return 事务发送结果
- 9: * @throws MQClientException 当 Client 发⽣异常时


- 0: */
- 1: public TransactionSendResult sendMesageInTransaction(final Mesage msg, final

LocalTranactionExecuter tranExecuter, final Object arg)

- 2 throws MQClientException {
- 3 if (nul = tranExecuter) {
- 4 throw new MQClientException("tranExecutor is nul", nul);
- 5 }
- 6: Validators.checkMesage(msg, this.defaultMQProducer);


- 17:
- 18: / 发送【Half消息】
- 19: SendResult sendResult;
- 20: MesageAcesor.putProperty(msg, MesageConst.PROPERTY_TRANSACTION_PREPARED, "true");
- 21: MesageAcesor.putProperty(msg, MesageConst.PROPERTY_PRODUCER_GROUP, this.defaultMQProducer.getProducerGroup();


- 2 try {
- 3 sendResult = this.send(msg);
- 4 } catch (Exception e) {
- 5 throw new MQClientException("send mesage Exception", e);
- 6: }


- 27:
- 28: / 处理发送【Half消息】结果
- 29 LocalTransactionState localTransactionState = LocalTransactionState.UNKNOW; 0: Throwable localException = nul;


- 31: switch (sendResult.getSendStatus() {
- 32: / 发送【Half消息】成功，执⾏【本地事务】逻辑 3: case SEND_OK: {


- 34: try {
- 35: if (sendResult.getTransactionId() != nul) {/ 事务编号。⽬前开源版本暂时没⽤到，猜想ONS在 使⽤。


- 6: msg.putUserProperty("_transactionId_", sendResult.getTransactionId();
- 7: }


- 38:
- 39: / 执⾏【本地事务】逻辑


- 0: localTransactionState = tranExecuter.executeLocalTransactionBranch(msg, arg);
- 1: if (nul = localTransactionState) {
- 2 localTransactionState = LocalTransactionState.UNKNOW;
- 3 }
- 4
- 5 if (localTransactionState != LocalTransactionState.COMIT_MESAGE) {
- 6: log.info("executeLocalTransactionBranch return {}", localTransactionState);
- 7: log.info(msg.toString();
- 8


49: } catch (Throwable e) {

- 0: log.info("executeLocalTransactionBranch exception", e);
- 1: log.info(msg.toString();
- 2 localException = e;
- 3
- 4 }
- 5: break;


56: / 发送【Half消息】失败，标记【本地事务】状态为回滚

- 7: case FLUS_DISK_TIMEOUT:
- 8: FLUSH_SLAVE_TIMEOUT:


59: case SLAVE_NOT_AVAILABLE:

- 0: localTransactionState = LocalTransactionState.ROLBACK_MESAGE;
- 1: break;
- 2: defult:
- 3 break;
- 4 }


65:

- 6: / 结束事务：提交消息 COMIT / ROLBACK
- 7: try {
- 8 this.endTransaction(sendResult, localTransactionState, localException);


- 69: } catch (Exception e) {
- 70: log.warn("local transaction execute " + localTransactionState + ", but end broker transaction failed", e);


1: }

- 72:
- 73: / 返回【事务发送结果】


- 4 TransactionSendResult transactionSendResult = new TransactionSendResult();
- 5 tansat nSendResult.setSendStatus(sendResult.getSendStatus();
- 6: transati nendResult.setMesageQueue(sendResult.getMesageQueue();
- 7: transati nendResult.setMsgId(sendResult.getMsgId();
- 8 tansat nendResult.setQueueOfset(sendResult.getQueueOfset();


79: transactionendResult.setTransactionId(sendResult.getTransactionId();

- 0: transactionSendResult.setLocalTransactionState(localTransactionState);
- 1: return transactionSendResult;
- 2 }
- 3


- 84: /*
- 85: * 结束事务：提交消息 COMIT / ROLBACK
- 86: *
- 87: * @param sendResult 发送【Half消息】结果 8: * @param localTransactionState 【本地事务】状态


- 89: * @param localException 执⾏【本地事务】逻辑产⽣的异常
- 90: * @throws RemotingException 当远程调⽤发⽣异常时
- 91: * @throws MQBrokerException 当 Broker 发⽣异常时
- 92: * @throws InteruptedException 当线程中断时
- 93: * @throws UnknownHostException 当解码消息编号失败是


- 4 */
- 5 public void endTransaction(/
- 6: final SendResult sendResult, /
- 7: final LocalTransactionState localTransactionState, /


98: final Throwable localException) throws RemotingException, MQBrokerException,

InteruptedException, UnknownHostException { 9: / 解码消息编号 0: final MesageId id;

11: if (sendResult.getOfsetMsgId() != nul) { 102: id = MesageDecoder.decodeMesageId(sendResult.getOfsetMsgId();

- 3 } else {
- 4 id = MesageDecoder.decodeMesageId(sendResult.getMsgId();
- 5 }


- 106:
- 107: / 创建请求 8 String transactionId = sendResult.getTransactionId();


109: final String brokerAdr = this.mQClientFactory.findBrokerAdresInPublish(sendResult.getMesageQueue().getBrokerNam e();

- 10: EndTransactionRequestHeader requestHeader = new EndTransactionRequestHeader(); 1: requestHeader.setTransactionId(transactionId);

12requestHeader.setComitLogOfset(id.getOfset();

- 13 switch (localTransactionState) {
- 14: case COMIT_MESAGE: 15requestHeader.setComitOrRolback(MesageSysFlag.TRANSACTION_COMIT_TYPE);


- 16: break;
- 17: case ROLBACK_MESAGE: 18requestHeader.setComitOrRolback(MesageSysFlag.TRANSACTION_ROLBACK_TYPE); 19: break;


- 0: case UNKNOW:


- 11: requestHeader.setComitOrRolback(MesageSysFlag.TRANSACTION_NOT_TYPE); 2brek; 3:defult: 4break; 5}


- 6: request eader.setProducerGroup(this.defaultMQProducer.getProducerGroup();
- 7: request eader.setTranStateTableOfset(sendResult.getQueueOfset(); 8requestHeder.setMsgId(sendResult.getMsgId();


- 129: String remark = localException != nul ? ("executeLocalTransactionBranch exception: " + localException.toString() : nul;
- 130:
- 131: / 提交消息 COMIT / ROLBACK。！！！通信⽅式为：Oneway！！！
- 132: this.mQClientFactory.getMQClientAPImpl().endTransactionOneway(brokerAdr, requestHeader, remark, this.defaultMQProducer.getSendMsgTimeout(); 13: }


- 2.2Broker处理结束事务请求


🦅 查询请求的消息，进⾏提交 / 回滚。实现代码如下：

- 1: / ⬇ ⬇ ⬇ 【EndTransactionProcesor.java】
- 2: public RemotingComand procesRequest(ChanelHandlerContext ctx, RemotingComand request) throws RemotingComandException {
- 3 inal RemotingComand response = RemotingComand.createResponseComand(nul);
- 4: final EndTransactionRequestHeader requestHeader = (EndTransactionRequestHeader) request.decodeComandCustomHeader(EndTransactionRequestHeader.clas);
- 5:
- 6: / 省略代码 =》打印⽇志（只处理 COMIT / ROLBACK）
- 7:
- 8: / 查询提交的消息
- 9: final MesageExt msgExt = this.brokerControler.getMesageStore().l okMesageByOfset(requestHeader.getComitLogOf set();
- 10: if (msgExt != nul) { 1: / 省略代码 =》校验消息


- 12:
- 13: / ⽣成消息 4 MesageExtBrokerI ner msgI ner = this.endMesageTransaction(msgExt);


- 15: msgI ner.setSysFlag(MesageSysFlag.resetTransactionValue(msgI ner.getSysFlag(), requestHeader.getComitOrRolback( );


- 6: sgI ner.setQueueOfset(requestHeader.getTranStateTableOfset();
- 7: msgI ner.setPreparedTransactionOfset(requestHeader.getComitLogOfset(); 8msgI ner.setStoreTimestamp(msgExt.getStoreTimestamp();


19: if (MesageSysFlag.TRANSACTION_ROLBACK_TYPE = requetHeader.getComitOrRolback() {

- 0: msgI ner.setBody(nul);
- 1: }
- 2:


23: / 存储⽣成消息 4inal MesageStore mesageStore = this.brokerControler.getMesageStore(); 5 final PutMesageResult putMesageResult = mesageStore.putMesage(msgI ner);

- 26:
- 27: / 处理存储结果 8 if (putMesageResult != nul) {


29 switch (putMesageResult.getPutMesageStatus() {

- 0: / Suces
- 1: case PUT_OK:
- 2: US_DISK_TIMEOUT:
- 3: FLUSH_SLAVE_TIMEOUT:
- 4: case SLAVE_NOT_AVAILABLE: 5esponse.seCode(ResponseCode.SUCES); 6: response.setRemark(nul); 7: brek; 8/ Failed


39: case CREATE_MAPEDFILE_FAILED:

- 0: response.setCode(ResponseCode.SYSTEM_EROR);
- 1: response.setRemark("create maped file failed."); 2break;


- 3: MESAGE_I LEGAL:
- 4: case PROPERTIES_SIZE_EXCEDED: 5esponse.seCode(ResponseCode.MESAGE_I LEGAL);


46: response.setRemark("the mesage is ilegal, maybe msg body or properties length not matched. msg body length limit 128k, msg properties length limit 32k.");

7: break; 48: case SERVICE_NOT_AVAILABLE:

- 3. 事务消息回查

49esponse.seCode(ResponseCode.SERVICE_NOT_AVAILABLE);

- 0: response.setRemark("service not available now.");
- 1: break;
- 2: case OS_PAGECACHE_BUSY: 3esponse.seCode(ResponseCode.SYSTEM_EROR);


- 4 response.setRemark("OS page cache busy, please try another machine"); 5break;


- 6: case UNKNOWN_EROR:
- 7: response.setCode(ResponseCode.SYSTEM_EROR); 8response.setRemark("UNKNOWN_EROR");


59brek;

- 0: default:
- 1: response.setCode(ResponseCode.SYSTEM_EROR);
- 2 response.setRemark("UNKNOWN_EROR DEFAULT"); 3break;


- 4 }
- 5
- 6: return response;
- 7: } else { 8esponse.seCode(ResponseCode.SYSTEM_EROR);


69: response.setRemark("store putMesage return nul");

- 0: }
- 1: } else { 2esponse.seCode(ResponseCode.SYSTEM_EROR);


- 3 response.setRemark("find prepared transaction mesage failed");
- 4 return response;
- 5 } 6: 7: return response;


78: }

- 2.3Broker⽣成 ConsumeQueue


🦅 事务消息，提交（COMMIT）后才⽣成 ConsumeQueue。

<table>
  <tr>
    <th>1: / ⬇ ⬇ ⬇ 【DefaultMesageStore.java】<br>2: public void doDispatch(DispatchRequest req) {<br>3: / ⾮事务消息 或 事务提交消息 建⽴ 消息位置信息 到 ConsumeQueue<br>4 final int tranTye = MesageSysFlag.getTransactionValue(req.getSysFlag();<br>5: switch (tranType) {<br>6: case MesageSysFlag.TRANSACTION_NOT_TYPE: / ⾮事务消息<br>7: case MesageSysFlag.TRANSACTION_COMIT_TYPE: / 事务消息COMIT<br>8: DefaultMesageStore.this.putMesagePositionInfo(req.getTopic(), req.getQueueId(), req.getComitLogOfset(), req.getMsgSize(),<br>9: req.getTagsCode(), req.getStoreTimestamp(), req.getConsumeQueueOfset();<br>10: break; 1: case MesageSysFlag.TRANSACTION_PREPARED_TYPE: / 事务消息PREPARED<br><br><br>12: case MesageSysFlag.TRANSACTION_ROLBACK_TYPE: / 事务消息ROLBACK 3break;<br><br>14: }<br>15: / 省略代码 =》 建⽴ 索引信息 到 IndexFile<br></th>
  </tr>
</table>


16: }

【事务消息回查】功能曾经开源过，⽬前（V4.0.0）暂未开源。如下是该功能的开源情况：

<table>
  <tr>
    <th>版本</th>
    <th>【事务消息回查】</th>
    <th> </th>
  </tr>
  <tr>
    <td>官⽅V3.0.4 ~ V3.1.4</td>
    <td>基于 ⽂件系统 实现</td>
    <td>已开源</td>
  </tr>
  <tr>
    <td> </td>
    <td>实现</td>
    <td>未完全开源</td>
  </tr>
</table>


官⽅V3.1.5 ~ V4.0.0 基于 数据库

我们来看看两种情况下是怎么实现的。

# 3.1Broker发起【事务消息回查】

## 3.1.1官⽅V3.1.4：基于⽂件系统 仓库地址：

htps:/github.com/YunaiV/rocketmq-3.1.9/tre/release_3.1.4

相较于普通消息，【事务消息】多依赖如下三个组件：

TransactionStateService ：事务状态服务，负责对【事务消息】进⾏管理，包括存储与更新事务 消息状态、回查事务消息状态等等。 TranStateTable ：【事务消息】状态存储。基于 MappedFileQueue 实现，默认存储路径 为 ~/store/transaction/statetable，每条【事务消息】状态存储结构如下：

<table>
  <tr>
    <th>第⼏位</th>
    <th>字段</th>
    <th>说明</th>
    <th>数据类型</th>
    <th>字节数</th>
  </tr>
  <tr>
    <td>1</td>
    <td>ofset</td>
    <td>ComitLog 物理 存储位置</td>
    <td>Long</td>
    <td>8</td>
  </tr>
  <tr>
    <td>2</td>
    <td>size</td>
    <td>消息⻓度</td>
    <td>Int</td>
    <td>4</td>
  </tr>
  <tr>
    <td>3</td>
    <td>timestamp</td>
    <td>消息存储时间，单 位：秒</td>
    <td>Int</td>
    <td>4</td>
  </tr>
  <tr>
    <td>4</td>
    <td>producerGroupHa</td>
    <td>producerGroup 求</td>
    <td>Int</td>
    <td>4</td>
  </tr>
  <tr>
    <td> </td>
    <td>sh</td>
    <td>HashCode 事务状态</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


5 state Int 4 TranRedoLog ：TranStateTable 重放⽇志，每次写操作 TranStateTable 记录重放⽇志。 当 Broker 异常关闭时，使⽤ TranRedoLog 恢复 TranStateTable。基于 ConsumeQueue 实现， Topic 为 TRANSACTION_REDOLOG_TOPIC_XXXX，默认存储路径 为 ~/store/transaction/redolog。

简单⼿绘逻辑图如下😈 ：

![image 2](assets/imageFile2.png)

Broker_V3.1.4_基于⽂件系统

## 3.1.1.1存储消息到 Co mitLog

🦅 存储【half消息】到 CommitLog 时，消息队列位置（queueOffset）使⽤ TranStateTable 最 ⼤物理位置（可写⼊物理位置）。这样，消息可以索引到⾃⼰对应的 TranStateTable 的位置和记 录。

核⼼代码如下：

<table>
  <tr>
    <th>1: / ⬇ ⬇ ⬇ 【DefaultApendMesageCalback.java】<br>2 clas DefaultApendMesageCalback implements ApendMesageCalback {<br>3: public ApendMesageResult doApend(final long fileFromOfset, final ByteBufer byteBufer, final int maxBlank, final Object msg) {<br>4: / .省略代码<br>5:<br>6: / 事务消息需要特殊处理<br>7: final int tranTye = MesageSysFlag.getTransactionValue(msgI ner.getSysFlag();<br>8: switch (tranType) {<br>9: case MesageSysFlag.TransactionPreparedType: / 消息队列位置（queueOfset）使⽤ TranStateTable 最⼤物理位置（可写⼊物理位置）<br>10: queueOfset = ComitLog.this.defaultMesageStore.getTransactionStateService().getTranStateTableOfset().ge t();<br><br><br>1: break;<br>2: case MesageSysFlag.TransactionRolbackType:<br>3 queueOfset = msgI ner.getQueueOfset(); 4break;<br><br><br>5: case esageysagTansactonNotType:<br>6: case MesageSysFlag.TransactionComitType:<br>7: default: 8break;<br><br><br>19: }<br>20:<br>21: / .省略代码<br><br><br>2<br>3 switch (tranType) {<br><br><br>24: case MesageSysFlag.TransactionPreparedType:<br>25: / 更新 TranStateTable 最⼤物理位置（可写⼊物理位置）<br>26: ComitLog.this.defaultMesageStore.getTransactionStateService().getTranStateTableOfset().in crementAndGet();<br><br><br>7: break;<br>8: case MesageSysFlag.TransactionRolbackType:<br><br><br>29break; 0: case MesageSysFlag.TransactionNotType:<br><br>31: case MesageSysFlag.TransactionComitType:<br>32: / 更新下⼀次的ConsumeQueue信息 3 ComitLog.this.topicQueueTable.put(key, +queueOfset); 4brek;<br><br><br>5:defult:<br>6: break;<br>7: }<br><br><br>38:<br>39: / 返回结果<br><br><br>0: return result;<br>1: }<br></th>
  </tr>
</table>


42: }

3.1.1.2写【事务消息】状态存储（TranStateTable）

🦅 处理【Half消息】时，新增【事务消息】状态存储（TranStateTable）。

🦅 处理【Comit / Rolback消息】时，更新 【事务消息】状态存储（TranStateTable） COMIT / ROLBACK。 🦅 每次写操作【事务消息】状态存储（TranStateTable），记录重放⽇志（TranRedoLog）。

核⼼代码如下：

1: / ⬇ ⬇ ⬇ 【DispatchMesageService.java】 2 private void doDispatch() { 3 if (!this.requestsRead.isEmpty() { 4: for (DispatchRequest req : this.requestsRead) { 5: 6: / .省略代码 7: 8: / 2、写【事务消息】状态存储（TranStateTable） 9: if (req.getProducerGroup() != nul) {

- 0: switch (tranType) {
- 1: case MesageSysFlag.TransactionNotType:
- 2 break;


- 13: case MesageSysFlag.TransactionPreparedType:
- 14: / 新增 【事务消息】状态存储（TranStateTable） 5 DefaultMesageStore.this.getTransactionStateService().apendPreparedTransaction(


16: req.getComitLogOfset(), req.getMsgSize(), (int) (req.getStoreTimestamp() / 1 0),

req.getProducerGroup().hashCode(); 7: break; 8: case esageysagTansactionComitType: 19: case MesageSysFlag.TransactionRolbackType: 20: / 更新 【事务消息】状态存储（TranStateTable） COMIT / ROLBACK

- 1: DefaultMesageStore.this.getTransactionStateService().updateTransactionState(
- 2: req.getTranStateTableOfset(), req.getPreparedTransactionOfset(),

req.getProducerGroup().hashCode(), tranType);

- 3 break;
- 4


- 25: }
- 26: / 3、记录 TranRedoLog


- 7: switch (tranType) {
- 8: case MesageSysFlag.TransactionNotType:


- 29 break;
- 30: case MesageSysFlag.TransactionPreparedType:
- 31: / 记录 TranRedoLog
- 32: DefaultMesageStore.this.getTransactionStateService().getTranRedoLog().putMesagePostionInf oWraper(


- 3: req.getComitLogOfset(), req.getMsgSize(),

TransactionStateService.PreparedMesageTagsCode,

- 4 req.getStoreTimestamp(), 0L);
- 5 break;
- 6: case esageysFlag.TransactionComitType:


- 37: case MesageSysFlag.TransactionRolbackType:
- 38: / 记录 TranRedoLog
- 39: DefaultMesageStore.this.getTransactionStateService().getTranRedoLog().putMesagePostionInf oWraper(


- 0: req.getComitLogOfset(), req.getMsgSize(), req.getPreparedTransactionOfset(),
- 1: req.getStoreTimestamp(), 0L);
- 2 break;
- 3
- 4 }


- 45:
- 46: / .省略代码
- 47: }


- 48: }
- 49: / ⬇ ⬇ ⬇ 【TransactionStateService.java】
- 50: /*
- 51: * 新增事务状态
- 52: *
- 53: * @param clOfset comitLog 物理位置
- 54: * @param size 消息⻓度


- 5: * @param timestamp 消息存储时间


- 56: * @param groupHashCode groupHashCode
- 57: * @return 是否成功 8 */


59 public bolean apendPreparedTransaction(/

- 0: final long clOfset,/
- 1: final int size,/
- 2 ina int timestamp,/
- 3 final int groupHashCode/
- 4 ) {
- 5 MapedFile mapedFile = this.tranStateTable.getLastMapedFile();
- 6: if (nul = mapedFile) {
- 7: log.eror("apendPreparedTransaction: create mapedfile eror.");
- 8 return false;


- 69: }
- 70:
- 71: / ⾸次创建，加⼊定时任务中


- 2 if (0 = mapedFile.getWrotePostion() {
- 3 this.adTimerTask(mapedFile);
- 4 }
- 5
- 6: tis. te fer pen.position(0);
- 7: this.byteBuferApend.limit(TStoreUnitSize);
- 8


79: / Comit Log Ofset

- 0: this.byteBuferApend.putLong(clOfset);
- 1: / Mesage Size
- 2 this.byteBuferApend.putInt(size);
- 3 / Timestamp
- 4 this.byteBuferApend.putInt(timestamp);
- 5 / Producer Group Hashcode
- 6: this.byteBuferApend.putInt(groupHashCode);
- 7: / Transaction State
- 8 this.byteBuferApend.putInt(MesageSysFlag.TransactionPreparedType);


89

- 0: return mapedFile.apendMesage(this.byteBuferApend.aray();
- 1: }
- 2


- 93: /*
- 94: * 更新事务状态
- 95: *
- 96: * @param tsOfset tranStateTable 物理位置
- 97: * @param clOfset comitLog 物理位置
- 98: * @param groupHashCode groupHashCode


- 9: * @param state 事务状态
- 10: * @return 是否成功
- 11: */ 102: public bolean updateTransactionState(


- 3 fnl l n ts f ,
- 4 fnl lon clOfset,
- 5 ina in groupHashCode,
- 6: final int state) {
- 7: SelectMapedBuferResult selectMapedBuferResult = this.findTransactionBufer(tsOfset);
- 8 if (selectMapedBuferResult != nul) {
- 09: try {
- 10: 1: / .省略代码：校验是否能够更新

- 12:
- 13: / 更新事务状态
- 14 selectMapedBuferResult.getByteBufer().putInt(TS_STATE_POS, state); 15} 16: catch (Exception e) { 17: log.eror("updateTransactionState exception", e); 18} 19: finaly {


- 0: selectMapedBuferResult.release();


- 11: }


- 2 }
- 3
- 4 return false;


125: }

## 3.1.1.3【事务消息】回查

🦅 TranStateTable 每个 MappedFile 都对应⼀个 Timer。Timer 固定周期（默认：60s）遍 历 MappedFile，查找【half消息】，向 Producer 发起【事务消息】回查请求。【事务消息】回 查结果的逻辑不在此处进⾏，在 ComitLog dispatch时执⾏。

实现代码如下：

- 1: / ⬇ ⬇ ⬇ 【TransactionStateService.java】
- 2: /*
- 3: * 初始化定时任务
- 4 */
- 5 private void initTimerTask() {
- 6: /
- 7: final List<MapedFile> mapedFiles = this.tranStateTable.getMapedFiles();
- 8: for (MapedFile mf : mapedFiles) {
- 9: this.adTimerTask(mf);


- 0: }
- 1: }
- 2


- 13: /*
- 14: * 每个⽂件初始化定时任务
- 15: * @param mf ⽂件 6: */ 7: private void adTimerTask(final MapedFile mf) { 8 this.timer.scheduleAtFixedRate(new TimerTask() {


- 19: private final MapedFile mapedFile = mf;
- 20: private final TransactionCheckExecuter transactionCheckExecuter = TransactionStateService.this.defaultMesageStore.getTransactionCheckExecuter();
- 21: private final long checkTransactionMesageAtleastInterval = TransactionStateService.this.defaultMesageStore.getMesageStoreConfig()


- 2 .getCheckTransactionMesageAtleastInterval();


23: private final bolean slave = TransactionStateService.this.defaultMesageStore.getMesageStoreConfig().getBrokerRole() = BrokerRole.SLAVE;

4 5 @Overide

- 26: public void run() {
- 27: / Slave不需要回查事务状态 8 if (slave) {


- 29 return;
- 30: }
- 31: / Check功能是否开启 2 if (!TransactionStateService.this.defaultMesageStore.getMesageStoreConfig() 3 .isCheckTransactionMesageEnable() { 4 return; 5 }


- 6:
- 7: try {
- 8 SelectMapedBuferResult selectMapedBuferResult = mapedFile.selectMapedBufer(0);


- 39: if (selectMapedBuferResult != nul) {
- 40: long preparedMesageCountInThisMapedFile = 0; / 回查的【half消息】数量 1: int i = 0;


42: try { 43: / 循环每条【事务消息】状态，对【half消息】进⾏回查

- 4 for (; i < selectMapedBuferResult.getSize(); i += TStoreUnitSize) {
- 5 selectMapedBuferResult.getByteBufer().position(i);
- 6:
- 7: / Comit Log Ofset
- 8 long clOfset = selectMapedBuferResult.getByteBufer().getLong();


49/ Mesage Size 50: int msgSize = selectMapedBuferResult.getByteBufer().getInt();

1: / Timestamp 2 int timestamp = selectMapedBuferResult.getByteBufer().getInt(); 3 / Producer Group Hashcode 4 int groupHashCode = selectMapedBuferResult.getByteBufer().getInt(); 5 / Transaction State 6: int tranType = selectMapedBuferResult.getByteBufer().getInt();

- 57:
- 58: / 已经提交或者回滚的消息跳过
- 59 if (tranType != MesageSysFlag.TransactionPreparedType) {


- 0: continue;
- 1: }


62: 63: / 遇到时间不符合最⼩轮询间隔，终⽌

4ong timestampLong = timestamp * 1 0;

- 5 long dif = System.curentTimeMilis() - timestampLong;
- 6: if (dif < checkTransactionMesageAtleastInterval) {
- 7: break;
- 8 }


69:

0: preparedMesageCountInThisMapedFile+; 71: 72: / 回查Producer

3 ry {

74: this.transactionCheckExecuter.gotoCheck(groupHashCode, getTranStateOfset(i), clOfset, msgSize);

- 5 } catch (Exception e) {
- 6: tranlog.warn("gotoCheck Exception", e);
- 7: }
- 8 }


79: 80: / ⽆回查的【half消息】数量，且遍历完，则终⽌定时任务

1: if (0 = preparedMesageCountInThisMapedFile/ 2 & i = mapedFile.getFileSize() {

83: tranlog.info("remove the transaction timer task, because no prepared mesage in this

mapedfile[{}]", mapedFile.getFileName(); 4 this.cancel(); 5 6: } finaly { 7: selectMapedBuferResult.release(); 8 }

- 89
- 90: tranlog.info("the transaction timer task execute over in this period, {} Prepared Mesage: {} Check Progres: {}/{}", mapedFile.getFileName(),/
- 91: preparedMesageCountInThisMapedFile, i / TStoreUnitSize, mapedFile.getFileSize() / TStoreUnitSize);


2 } else if (mapedFile.isFul()

93: tranlog.info("the mapedfile[{}] maybe deleted, cancel check transaction timer task", mapedFile.getFileName();

- 4 this.cancel();
- 5 return;
- 6: }
- 7: } catch (Exeption e) {
- 8 log.eror("check transaction timer task Exception", e);
- 9: } 0: }


11: 102:

3 private long getTranStateOfset(final long curentIndex) { 104: long ofset = (this.mapedFile.getFileFromOfset() + curentIndex) / TransactionStateService.TStoreUnitSize;

5 return ofset; 6: }

107: }, 1 0 * 60, this.defaultMesageStore.getMesageStoreConfig().getCheckTransactionMesageTimerInterval() );

8 } 109:

- 10: / 【DefaultTransactionCheckExecuter.java】 1: @Overide

- 12: public void gotoCheck(int producerGroupHashCode, long tranStateTableOfset, long

comitLogOfset,

- 13: int msgSize) {
- 14: / 第⼀步、查询Producer
- 15: final ClientChanelInfo clientChanelInfo =

this.brokerControler.getProducerManager().pickProducerChanelRandomly(producerGroupHash Code);

- 16: if (nul =lientChanelInfo) {
- 17: log.warn("check a producer transaction state, but not find any chanel of this group[{}]",


producerGroupHashCode); 18return; 19: }

120: 121: / 第⼆步、查询消息

- 12: SelectMapedBuferResult selectMapedBuferResult = this.brokerControler.getMesageStore().selectOneMesageByOfset(comitLogOfset, msgSize);

3 if (nul = selectMapedBuferResult) { 124: log.warn("check a producer transaction state, but not find mesage by comitLogOfset: {}, msgSize: ", comitLogOfset, msgSize);

5return; 6: }

- 127:
- 128: / 第三步、向Producer发起请求
- 129: final CheckTransactionStateRequestHeader requestHeader = new CheckTransactionStateRequestHeader();


- 0: requestHeader.setComitLogOfset(comitLogOfset);


11: requestHeader.setTranStateTableOfset(tranStateTableOfset); 132: this.brokerControler.getBroker2Client().checkProducerTransactionState(clientChanelInfo.getCh anel(), requestHeader, selectMapedBuferResult);

- 13: }




## 3.1.1.4初始化【事务消息】状态存储（TranStateTable）

🦅 根据最后 Broker 关闭是否正常，会有不同的初始化⽅式。

核⼼代码如下：

- 1: / ⬇ ⬇ ⬇ 【TransactionStateService.java】
- 2: /*
- 3: * 初始化 TranRedoLog
- 4: * @param lastExitOK 是否正常退出
- 5 */
- 6: public void recoverStateTable(final bolean lastExitOK) {
- 7: if (lastExitOK) {
- 8 this.recoverStateTableNormal();
- 9: } else {
- 10: / 第⼀步，删除State Table 1: this.tranStateTable.destroy();


- 12: / 第⼆步，通过RedoLog全量恢复StateTable 3 this.recreateStateTable(); 4 5 } 6:


17: /* 18: * 扫描 TranRedoLog 重建 StateTable 19: */

0: private void recreateStateTable() { 21: this.tranStateTable = new

MapedFileQueue(StorePathConfigHelper.getTranStateTableStorePath(defaultMesageStore

- 2 .getMesageStoreofig().getStorePathRotDir(), defaultMesageStore
- 3 .getMesageStoreConfig().getTranStateTableMapedFileSize(), nul);
- 4
- 5 final TreSet<Long> preparedItemSet = new TreSet<Long>();


26: 27: / 第⼀步，从头扫描RedoLog

8 final long minOfset = this.tranRedoLog.getMinOfsetInQuque(); 29 long procesOfset = minOfset;

0: while (true) {

31: SelectMapedBuferResult buferConsumeQueue = this.tranRedoLog.getIndexBufer(procesOfset);

- 2 if (buferConsumeQueue != nul) {
- 3 try {
- 4 l ng i = 0;
- 5 for (; i < buferConsumeQueue.getSize(); i += ConsumeQueue.CQStoreUnitSize) {
- 6: long ofsetMsg = buferConsumeQueue.getByteBufer().getLong();
- 7: int sizeMsg = buferConsumeQueue.getByteBufer().getInt();
- 8 long tagsCode = buferConsumeQueue.getByteBufer().getLong();


39

- 0: if (TransactionStateService.PreparedMesageTagsCode = tagsCode) {/ Prepared
- 1: preparedItemSet.ad(ofsetMsg);
- 2 } else {/ Comit/Rolback
- 3 preparedItemSet.remove(tagsCode);
- 4
- 5 } 6:


- 47: procesOfset += i;
- 48: } finaly {/ 必须释放资源
- 49 buferConsumeQueue.release();


- 0: }
- 1: } else {


52: break;

- 3
- 4 }
- 5: log.info("scan transaction redolog over, End ofset: {}, Prepared Transaction Count: {}",


procesOfset, preparedItemSet.size();

- 56:
- 57: / 第⼆步，重建StateTable 8 Iterator<Long> it = preparedItemSet.iterator();


59 while (it.hasNext() {

- 0: Long ofset = it.next();
- 1: MesageExt msgExt = this.defaultMesageStore.l okMesageByOfset(ofset);
- 2 if (msgExt != nul) {
- 3 this.apendPreparedTransaction(msgExt.getComitLogOfset(), msgExt.getStoreSize(),
- 4 (int) (msgExt.getStoreTimestamp() / 1 0),
- 5 msgExt.getProperty(MesageConst.PROPERTY_PRODUCER_GROUP).hashCode();
- 6: this.tranStateTableOfset.incrementAndGet();
- 7: }
- 8


69: } 0: 71: /* 72: * 加载（解析）TranStateTable 的 MapedFile 73: * 1. 清理多余 MapedFile，设置最后⼀个 MapedFile的写⼊位置(position 74: * 2. 设置 TanStateTable 最⼤物理位置（可写⼊位置）

5 */ 6: private void recoverStateTableNormal() {

7: final List<MapedFile> mapedFiles = this.tranStateTable.getMapedFiles();

78: if (!mapedFiles.isEmpty() { 79: / 从倒数第三个⽂件开始恢复

- 0: int index = mapedFiles.size() - 3;
- 1: if (index < 0) {
- 2 index = 0;
- 3 }
- 4
- 5 int mapedFileSizeLogics = this.tranStateTable.getMapedFileSize();
- 6: MapedFile mapedFile = mapedFiles.get(index);
- 7: ByteBufer byteBufer = mapedFile.sliceByteBufer();
- 8 log procesOfset = mapedFile.getFileFromOfset();


89 long mapedFileOfset = 0;

- 0: while (true) {
- 1: for (int i = 0; i < mapedFileSizeLogics; i += TStoreUnitSize) {
- 2
- 3 fnal long clOfset_read = byteBufer.getLong();
- 4 fina int size_read = byteBufer.getInt();
- 5 fina int timestamp_read = byteBufer.getInt();
- 6: final int groupHashCode_read = byteBufer.getInt();
- 7: final int state_read = byteBufer.getInt();
- 8
- 9: bolean stateOK = false; 0: switch (state_read) {


- 11: case MesageSysFlag.TransactionPreparedType:


- 2: case esageysagTansactionComitType:
- 3: case MesageSysFlag.TransactionRolbackType:
- 4 stateOK = true; 5brek; 6: default:


107: break;

8 } 109:

- 10: / 说明当前存储单元有效 1: if (clOfset_read >= 0 & size_read > 0 & stateOK) {

- 12 mapedFileOfset = i + TStoreUnitSize;
- 13 } else {
- 14 log.info("recover curent transaction state table file over, " + mapedFile.getFileName() + " "
- 15 + clOfset_read + " " + size_read + " " + timestamp_read);
- 16: break;
- 17: }
- 18 } 19:


120: / ⾛到⽂件末尾，切换⾄下⼀个⽂件

- 11: if (mapedFileOfset = mapedFileSizeLogics) {
- 12: index+;


- 123: if (index >= mapedFiles.size() {/ 循环while结束
- 124: log.info("recover last transaction state table file over, last maped file " + mapedFile.getFileName();
- 125: break;
- 126: } else {/ 切换下⼀个⽂件


- 7: mapedFile = mapedFiles.get(index);
- 8 byteBufer = mapedFile.sliceByteBufer();


29 procesOfset = mapedFile.getFileFromOfset(); 0: mapedFileOfset = 0; 11: log.info("recover next transaction state table file, " + mapedFile.getFileName();

- 2
- 3 } else {


134: log.info("recover curent transaction state table queue over " + mapedFile.getFileName() + " " + (procesOfset + mapedFileOfset);

5break;

- 6: }
- 7: }


138: 139: / 清理多余 MapedFile，设置最后⼀个 MapedFile的写⼊位置(position

0: procesOfset += mapedFileOfset; 11: this.tranStateTable.truncateDirtyFiles(procesOfset);

- 142:
- 143: / 设置 TanStateTable 最⼤物理位置（可写⼊位置） 4 this.tranStateTableOfset.set(this.tranStateTable.getMaxOfset() / TStoreUnitSize);


145: log.info("recover normal over, transaction state table max ofset: {}", this.tranStateTableOfset.get();

6: } 147: }

## 3.1.1.5补充

为什么 V3.1.5 开始，使⽤ 数据库 实现【事务状态】的存储？如下是来⾃官⽅⽂档的说明，可能是 ⼀部分原因：

RocketMQ 这种实现事务⽅式，没有通过 KV 存储做，⽽是通过 Ofset ⽅式，存在⼀个 显著缺陷，即通过 Ofset 更改数据，会令系统的脏⻚过多，需要特别关注。

- 3.1.2官⽅V4.0.0：基于数据库 仓库地址：


### htps:/github.com/apache/incubator-rocketmq

官⽅V4.0.0 暂时未完全开源【事务消息回查】功能，So 我们需要进⾏⼀些猜想，可能不⼀定正确😈 。 😆 我们来对⽐【官⽅V3.1.4：基于⽂件】的实现。

TransactionRecord ：记录每条【事务消息】。类似 TranStateTable。

<table>
  <tr>
    <th>TranStateTable</th>
    <th>TransactionRecord</th>
    <th> </th>
  </tr>
  <tr>
    <td>ofset</td>
    <td>ofset</td>
    <td> </td>
  </tr>
  <tr>
    <td>producerGroupHash</td>
    <td>producerGroup</td>
    <td> </td>
  </tr>
  <tr>
    <td>size</td>
    <td>⽆</td>
    <td>⾮必须字段：【事务消息】回查 时，使⽤ ofset 读取<br><br>获得。</td>
  </tr>
  <tr>
    <td>timestamp</td>
    <td>⽆</td>
    <td>ComitLog ⾮必须字段：【事务消息】回查 时，使⽤ ofset 读取<br><br>获得。</td>
  </tr>
  <tr>
    <td>state</td>
    <td>⽆</td>
    <td>ComitLog ⾮必须字段： 事务开始，增加 记录；事务结束，删除记录。</td>
  </tr>
</table>


另外，数据库本身保证了数据存储的可靠性，⽆需 TranRedoLog。

简单⼿绘逻辑图如下😈 ：

![image 3](assets/imageFile3.png)

Broker_V4.0.0_基于数据库

# 3.2Producer接收【事务消息回查】

顺序图如下：

![image 4](assets/imageFile4.png)

Producer接收【事务消息回查】

核⼼代码如下：

- 1: / ⬇ ⬇ ⬇ 【DefaultMQProducerImpl.java】
- 2: /*
- 3: * 检查【事务状态】状态
- 4: *
- 5: * @param adr broker地址
- 6: * @param msg 消息
- 7: * @param header 请求
- 8 */
- 9: @Overide
- 10: public void checkTransactionState(final String adr, final MesageExt msg, final CheckTransactionStateRequestHeader header) {


- 1: Runable request = new Runable() {
- 2 riateina String brokerAdr = adr;
- 3 iatei a MesageExt mesage = msg;
- 4 riateina CheckTransactionStateRequestHeader checkRequestHeader = header;


15: private final String group = DefaultMQProducerImpl.this.defaultMQProducer.getProducerGroup();

- 6:
- 7: @Overide
- 8 public void run() {


- 19: TransactionCheckListener transactionCheckListener = DefaultMQProducerImpl.this.checkListener();
- 20: if (transactionCheckListener != nul) {
- 21: / 获取事务执⾏状态 2 LocalTransactionState localTransactionState = LocalTransactionState.UNKNOW; 3 Throwable exception = nul; 4 try { 5 localTransactionState = transactionCheckListener.checkLocalTransactionState(mesage); 6: } catch (Throwable e) { 7: log.eror("Broker cal checkTransactionState, but checkLocalTransactionState exception", e); 8 exception = e;


29 }

- 30:
- 31: / 处理事务结果，提交消息 COMIT / ROLBACK


- 2 this.procesTransactionState(/
- 3 localTransactionState, /
- 4 grou, /
- 5 exception);
- 6: } else {
- 7: log.warn("checkTransactionState, pick transactionCheckListener by group[{}] failed", group);
- 8


39 } 0:

- 41: /*
- 42: * 处理事务结果，提交消息 COMIT / ROLBACK
- 43: * 4: * @param localTransactionState 【本地事务】状态


45: * @param producerGroup producerGroup 46: * @param exception 检查【本地事务】状态发⽣的异常

- 7: */
- 8 private void procesTransactionState(/


49inal LocalTransactionState localTransactionState, /

0: final String producerGroup, / 51: final Throwable exception) {

- 2 final EndTransactionRequestHeader thisHeader = new EndTransactionRequestHeader();
- 3 thisHeader.setComitLogOfset(checkRequestHeader.getComitLogOfset();
- 4 t sedersetProducerGroup(producerGroup);
- 5 thsHeader.setTranStateTableOfset(checkRequestHeader.getTranStateTableOfset();
- 6: thisHeader.setFromTransactionCheck(true);


57: 58: / 设置消息编号 59: String uniqueKey =

mesage.getProperties().get(MesageConst.PROPERTY_UNIQ_CLIENT_MESAGE_ID_KEYIDX);

- 0: if (uniqueKey = nul) {
- 1: uniqueKey = mesage.getMsgId();
- 2 }
- 3 thisHeader.setMsgId(uniqueKey);
- 4
- 5 thisHeader.setTransactionId(checkRequestHeader.getTransactionId();
- 6: switch (local ransactionState) {
- 7: case COMIT_MESAGE:
- 8 thisHeader.setComitOrRolback(MesageSysFlag.TRANSACTION_COMIT_TYPE);


69: break;

- 0: case ROLBACK_MESAGE:
- 1: thisHeader.setComitOrRolback(MesageSysFlag.TRANSACTION_ROLBACK_TYPE);
- 2 log.warn("when broker check, client rolback this transaction, {}", thisHeader);
- 3 break;
- 4: case UNKNOW:
- 5 thisHeader.setComitOrRolback(MesageSysFlag.TRANSACTION_NOT_TYPE);
- 6: log.warn("when broker check, client does not know this transaction state, {}", thisHeader);
- 7: brek;
- 8: defult:


79: break; 0: } 1: 2 String remark = nul; 3 if (exception != nul) {

84: remark = "checkLocalTransactionState Exception: " +

RemotingHelper.exceptionSimpleDesc(exception); 5 } 6:

87: try { 8: / 提交消息 COMIT / ROLBACK

89: DefaultMQProducerImpl.this.mQClientFactory.getMQClientAPImpl().endTransactionOneway(bro kerAdr, thisHeader, remark,

- 0: 3 0);
- 1: } catch (Exception e) {
- 2 log.eror("endTransactionOneway exception", e);
- 3
- 4
- 5 };


96: 97: / 提交执⾏

8 this.checkExecutor.submit(request); 9: }

10:

- 101: / ⬇ ⬇ ⬇ 【DefaultMQProducerImpl.java】
- 102: /*
- 103: * 【事务消息回查】检查监听器


4*/ 5 public interface TransactionCheckListener { 6: 107: /* 108: * 获取（检查）【本地事务】状态 109: *

10: * @param msg 消息 1: * @return 事务状态 12*/

- 13 LocalTransactionState checkLocalTransactionState(final MesageExt msg);
- 14
- 15: }


6. 彩蛋
