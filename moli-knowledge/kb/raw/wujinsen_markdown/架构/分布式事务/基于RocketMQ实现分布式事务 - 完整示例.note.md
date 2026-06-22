htps:/zhuanlan.zhihu.com/p/15924952

之前我们说到，分布式事务是⼀个复杂的技术问题。没有通⽤的解决⽅案，也缺乏简单⾼效的⼿段。 不过，如果我们的系统不追求强⼀致性，那么最常⽤的还是最终⼀致性⽅案。今天，我们就基 于 RocketMQ来实现消息最终⼀致性⽅案的分布式事务。 本⽂代码不只是简单的demo，考虑到⼀些异常情况、幂等性消费和死信队列等情况，尽量向可靠业务 场景靠拢。 另外，在最后还有《RocketMQ技术内幕》⼀书中，关于分布式事务示例代码的错误流程分析，所以篇 幅较⻓，希望⼤家耐⼼观看。 ⼀、事务消息 在这⾥，笔者不想使⽤⼤量的⽂字赘述 RocketMQ事务消息的原理，我们只需要搞明⽩两个概念。

Half Mesage，半消息

暂时不能被 Consumer消费的消息。Producer已经把消息发送到 Broker端，但是此消息的状态被标记为不 能投递，处于这种状态下的消息称为半消息。事实上，该状态下的消息会被放在⼀个叫 做 RMQ_SYS_TRANS_HALF_TOPIC的主题下。 当 Producer端对它⼆次确认后，也就是 Commit之后，Consumer端才可以消费到；那么如果是Rollback， 该消息则会被删除，永远不会被消费到。

事务状态回查

我们想，可能会因为⽹络原因、应⽤问题等，导致Producer端⼀直没有对这个半消息进⾏确认，那么这 时候 Broker服务器会定时扫描这些半消息，主动找Producer端查询该消息的状态。 当然，什么时候去扫描，包含扫描⼏次，我们都可以配置，在后⽂我们再细说。 简⽽⾔之，RocketMQ事务消息的实现原理就是基于两阶段提交和事务状态回查，来决定消息最终是提交 还是回滚的。 在本⽂，我们的代码就以 订单服务、积分服务 为例。结合上⽂来看，整体流程如下：

![image 1](<基于RocketMQ实现分布式事务 - 完整示例.note_images/imageFile1.png>)

⼆、订单服务 在订单服务中，我们接收前端的请求创建订单，保存相关数据到本地数据库。

- 1、事务⽇志表 在订单服务中，除了有⼀张订单表之外，还需要⼀个事务⽇志表。 它的定义如下： CREATE TABLE `transaction_log` (

`id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '事务ID', `business` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '业务标识', `foreign_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '对应业务表中的

主键', PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

这张表专⻔作⽤于事务状态回查。当提交业务数据时，此表也插⼊⼀条数据，它们共处⼀个本地事务 中。通过事务ID查询该表，如果返回记录，则证明本地事务已提交；如果未返回记录，则本地事务可 能是未知状态或者是回滚状态。

- 2、TransactionMQProducer 我们知道，通过 RocketMQ发送消息，需先创建⼀个消息发送者。值得注意的是，如果发送事务消息，在 这⾥我们的创建的实例必须是 TransactionMQProducer。


public class TransactionProducer {

private String producerGroup = "order_trans_group"; private TransactionMQProducer producer;

//⽤于执⾏本地事务和事务状态回查的监听器 @Autowired OrderTransactionListener orderTransactionListener; //执⾏任务的线程池 ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 60,

TimeUnit.SECONDS, new ArrayBlockingQueue<>(50));

@PostConstruct public void init(){

producer = new TransactionMQProducer(producerGroup); producer.setNamesrvAddr("127.0.0.1:9876"); producer.setSendMsgTimeout(Integer.MAX_VALUE); producer.setExecutorService(executor); producer.setTransactionListener(orderTransactionListener); this.start();

} private void start(){

try { this.producer.start(); } catch (MQClientException e) {

e.printStackTrace(); }

} //事务消息发送 public TransactionSendResult send(String data, String topic) throws MQClientException {

Message message = new Message(topic,data.getBytes()); return this.producer.sendMessageInTransaction(message, null);

}

} 上⾯的代码中，主要就是创建事务消息的发送者。在这⾥，我们重点关注 OrderTransactionListener， 它负责执⾏本地事务和事务状态回查。

- 3、OrderTransactionListener


public class OrderTransactionListener implements TransactionListener {

@Autowired OrderService orderService;

@Autowired TransactionLogService transactionLogService;

Logger logger = LoggerFactory.getLogger(this.getClass());

@Override public LocalTransactionState executeLocalTransaction(Message message, Object o) {

logger.info("开始执⾏本地事务...."); LocalTransactionState state; try{

String body = new String(message.getBody()); OrderDTO order = JSONObject.parseObject(body, OrderDTO.class); orderService.createOrder(order,message.getTransactionId()); state = LocalTransactionState.COMMIT_MESSAGE; logger.info("本地事务已提交。{}",message.getTransactionId());

}catch (Exception e){ logger.info("执⾏本地事务失败。{}",e); state = LocalTransactionState.ROLLBACK_MESSAGE;

} return state;

}

@Override public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {

logger.info("开始回查本地事务状态。{}",messageExt.getTransactionId()); LocalTransactionState state; String transactionId = messageExt.getTransactionId(); if (transactionLogService.get(transactionId)>0){

state = LocalTransactionState.COMMIT_MESSAGE; }else {

state = LocalTransactionState.UNKNOW;

} logger.info("结束本地事务状态查询：{}",state); return state;

}

} 在通过 producer.sendMessageInTransaction发送事务消息后，如果消息发送成功，就会调⽤到这⾥的 executeLocalTransaction⽅法，来执⾏本地事务。在这⾥，它会完成订单数据和事务⽇志的插⼊。 该⽅法返回值 LocalTransactionState 代表本地事务状态，它是⼀个枚举类。

public enum LocalTransactionState { //提交事务消息，消费者可以看到此消息 COMMIT_MESSAGE, //回滚事务消息，消费者不会看到此消息 ROLLBACK_MESSAGE, //事务未知状态，需要调⽤事务状态回查，确定此消息是提交还是回滚 UNKNOW;

} 那么， checkLocalTransaction ⽅法就是⽤于事务状态查询。在这⾥，我们通过事务ID查询 transaction_log这张表，如果可以查询到结果，就提交事务消息；如果没有查询到，就返回未知状态。 注意，这⾥还涉及到另外⼀个问题。如果是返回未知状态，RocketMQ Broker服务器会以1分钟的间隔时 间不断回查，直⾄达到事务回查最⼤检测数，如果超过这个数字还未查询到事务状态，则回滚此消 息。 当然，事务回查的频率和最⼤次数，我们都可以配置。在 Broker 端，可以通过这样来配置它： brokerConfig.setTransactionCheckInterval(10000); //回查频率10秒⼀次 brokerConfig.setTransactionCheckMax(3); //最⼤检测次数为3

- 4、业务实现类


@Service public class OrderServiceImpl implements OrderService {

@Autowired OrderMapper orderMapper; @Autowired TransactionLogMapper transactionLogMapper; @Autowired TransactionProducer producer;

Snowflake snowflake = new Snowflake(1,1); Logger logger = LoggerFactory.getLogger(this.getClass());

//执⾏本地事务时调⽤，将订单数据和事务⽇志写⼊本地数据库 @Transactional @Override public void createOrder(OrderDTO orderDTO,String transactionId){

- //1.创建订单 Order order = new Order(); BeanUtils.copyProperties(orderDTO,order); orderMapper.createOrder(order);

- //2.写⼊事务⽇志 TransactionLog log = new TransactionLog(); log.setId(transactionId); log.setBusiness("order"); log.setForeignKey(String.valueOf(order.getId())); transactionLogMapper.insert(log);


logger.info("订单创建完成。{}",orderDTO); }

//前端调⽤，只⽤于向RocketMQ发送事务消息 @Override public void createOrder(OrderDTO order) throws MQClientException {

order.setId(snowflake.nextId()); order.setOrderNo(snowflake.nextIdStr()); producer.send(JSON.toJSONString(order),"order");

}

} 在订单业务服务类中，我们有两个⽅法。⼀个⽤于向RocketMQ发送事务消息，⼀个⽤于真正的业务数据 落库。 ⾄于为什么这样做，其实有⼀些原因的，我们后⾯再说。

- 5、调⽤


@RestController public class OrderController {

@Autowired OrderService orderService; Logger logger = LoggerFactory.getLogger(this.getClass());

@PostMapping("/create_order") public void createOrder(@RequestBody OrderDTO order) throws MQClientException {

logger.info("接收到订单数据：{}",order.getCommodityCode()); orderService.createOrder(order);

} }

- 6、总结 ⽬前已经完成了订单服务的业务逻辑。我们总结流程如下：


![image 2](<基于RocketMQ实现分布式事务 - 完整示例.note_images/imageFile2.png>)

考虑到异常情况，这⾥的要点如下：

第⼀次调⽤createOrder，发送事务消息。如果发送失败，导致报错，则将异常返回，此时不会涉及 到任何数据安全。

如果事务消息发送成功，但在执⾏本地事务时发⽣异常，那么订单数据和事务⽇志都不会被保存， 因为它们是⼀个本地事务中。

如果执⾏完本地事务，但未能及时的返回本地事务状态或者返回了未知状态。那么，会由Broker定 时回查事务状态，然后根据事务⽇志表，就可以判断订单是否已完成，并写⼊到数据库。

基于这些要素，我们可以说，已经保证了订单服务和事务消息的⼀致性。那么，接下来就是积分服务 如何正确的消费订单数据并完成相应的业务操作。 三、积分服务 在积分服务中，主要就是消费订单数据，然后根据订单内容，给相应⽤户增加积分。

- 1、积分记录表


CREATE TABLE `t_points` ( `id` bigint(16) NOT NULL COMMENT '主键', `user_id` bigint(16) NOT NULL COMMENT '⽤户id', `order_no` bigint(16) NOT NULL COMMENT '订单编号', `points` int(4) NOT NULL COMMENT '积分', `remarks` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '备注', PRIMARY KEY (`id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin; 在这⾥，我们重点关注order_no字段，它是实现幂等消费的⼀种选择。

- 2、消费者启动 @Component public class Consumer {

String consumerGroup = "consumer-group"; DefaultMQPushConsumer consumer;

@Autowired OrderListener orderListener;

@PostConstruct public void init() throws MQClientException {

consumer = new DefaultMQPushConsumer(consumerGroup); consumer.setNamesrvAddr("127.0.0.1:9876"); consumer.subscribe("order","*"); consumer.registerMessageListener(orderListener); consumer.start();

}

} 启动⼀个消费者⽐较简单，我们指定要消费的 topic 和监听器就好了。

- 3、消费者监听器


@Autowired PointsService pointsService; Logger logger = LoggerFactory.getLogger(this.getClass());

@Override public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,

ConsumeConcurrentlyContext context) { logger.info("消费者线程监听到消息。"); try{

for (MessageExt message:list) { logger.info("开始处理订单数据，准备增加积分...."); OrderDTO order = JSONObject.parseObject(message.getBody(), OrderDTO.class); pointsService.increasePoints(order);

} return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

}catch (Exception e){ logger.error("处理消费者数据发⽣异常。{}",e); return ConsumeConcurrentlyStatus.RECONSUME_LATER;

} }

} 监听到消息之后，调⽤业务服务类处理即可。处理完成则返回CONSUME_SUCCESS以提交，处理失败则返回 RECONSUME_LATER来重试。

- 4、增加积分 在这⾥，主要就是对积分数据⼊库。但注意，⼊库之前需要先做判断，来达到幂等性消费。


@Service public class PointsServiceImpl implements PointsService {

@Autowired PointsMapper pointsMapper;

Snowflake snowflake = new Snowflake(1,1); Logger logger = LoggerFactory.getLogger(this.getClass());

@Override public void increasePoints(OrderDTO order) {

//⼊库之前先查询，实现幂等 if (pointsMapper.getByOrderNo(order.getOrderNo())>0){

logger.info("积分添加完成，订单已处理。{}",order.getOrderNo());

}else{ Points points = new Points(); points.setId(snowflake.nextId()); points.setUserId(order.getUserId()); points.setOrderNo(order.getOrderNo()); Double amount = order.getAmount(); points.setPoints(amount.intValue()*10); points.setRemarks("商品消费共【"+order.getAmount()+"】元，获得积分"+points.getPoints()); pointsMapper.insert(points); logger.info("已为订单号码{}增加积分。",points.getOrderNo());

} }

}

- 5、幂等性消费 实现幂等性消费的⽅式有很多种，具体怎么做，根据⾃⼰的情况来看。 ⽐如，在本例中，我们直接将订单号和积分记录绑定在同⼀个表中，在增加积分之前，就可以先查询 此订单是否已处理过。 或者，我们也可以额外创建⼀张表，来记录订单的处理情况。 再者，也可以将这些信息直接放到redis缓存⾥，在⼊库之前先查询缓存。 不管以哪种⽅式来做，总的思路就是在执⾏业务前，必须先查询该消息是否被处理过。那么这⾥就涉 及到⼀个数据主键问题，在这个例⼦中，我们以订单号为主键，也可以⽤事务ID作主键，如果是普通 消息的话，我们也可以创建唯⼀的消息ID作为主键。

- 6、消费异常 我们知道，当消费者处理失败后会返回 RECONSUME_LATER ，让消息来重试，默认最多重试16次。 那，如果真的由于特殊原因，消息⼀直不能被正确处理，那怎么办 ？ 我们考虑两种⽅式来解决这个问题。 第⼀，在代码中设置消息重试次数，如果达到指定次数，就发邮件或者短信通知业务⽅⼈⼯介⼊处 理。


Logger logger = LoggerFactory.getLogger(this.getClass());

@Override public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list,

ConsumeConcurrentlyContext context) { logger.info("消费者线程监听到消息。"); for (MessageExt message:list) {

if (!processor(message)){

return ConsumeConcurrentlyStatus.RECONSUME_LATER; }

} return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

}

/**

- * 消息处理，第3次处理失败后，发送邮件通知⼈⼯介⼊

- * @param message

- * @return

- */


private boolean processor(MessageExt message){ String body = new String(message.getBody()); try {

logger.info("消息处理....{}",body); int k = 1/0; return true;

}catch (Exception e){

if(message.getReconsumeTimes()>=3){ logger.error("消息重试已达最⼤次数，将通知业务⼈员排查问题。{}",message.getMsgId()); sendMail(message); return true;

} return false;

} }

}

第⼆，等待消息重试最⼤次数后，进⼊死信队列。 消息重试最⼤次数默认是16次，我们也可以在消费者端设置这个次数。

consumer.setMaxReconsumeTimes(3);//设置消息重试最⼤次数 死信队列的主题名称是 %DLQ% + 消费者组名称，⽐如在订单数据中，我们设置了消费者组名： String consumerGroup = "order-consumer-group"; 那么这个消费者，对应的死信队列主题名称就是%DLQ%order-consumer-group

![image 3](<基于RocketMQ实现分布式事务 - 完整示例.note_images/imageFile3.png>)

如上图，我们还需要点击TOPIC配置，来修改⾥⾯的 perm 属性，改为 6 即可。

![image 4](<基于RocketMQ实现分布式事务 - 完整示例.note_images/imageFile4.png>)

最后就可以通过程序代码监听这个主题，来通知⼈⼯介⼊处理或者直接在控制台查看处理了。通过幂 等性消费和对死信消息的处理，基本上就能保证消息⼀定会被处理。 四、《RocketMQ技术内幕》中的代码示例 笔者⼿⾥有⼀本书《RocketMQ技术内幕》，在 9.4 章节有⼀段分布式事务的代码。 不过，笔者在看了之后，感觉它⾥⾯的流程是有问题的，会造成本地事务的不⼀致，下⾯我们就来分 析⼀下。 在这⾥，我们主要是关注书中订单业务服务类和事务监听器的流程。 在书中，订单下单伪代码如下：

public Map createOrder(){ Map result = new HashMap(); //执⾏下订单相关的业务流程，例如操作本地数据库落库相关代码 //⽣成事务消息唯⼀业务标识，将该业务标识组装到待发送的消息体中，⽅便消息端进⾏幂等消费。 //调⽤消息客户端API，发送事务prepare消息。 //返回结果，提交事务 return result;

}

上述是第⼀步，发送事务消息，接下来需要实现TransactionListener，实现执⾏本地事务与本地事务 回查。

public class OrderTransactionListenerImpl implements TransactionListener { @Override public LocalTransactionState executeLocalTransaction(Message message, Object o) {

//从消息体中获取业务唯⼀ID String bizUniNo = message.getUserProperty("bizUniNo"); //将bizUniNo⼊库，表名：t_message_transaction，表结构 bizUniNo（主键），业务类型。 return LocalTransactionState.UNKNOW;

}

@Override public LocalTransactionState checkLocalTransaction(MessageExt message) {

//从消息体中获取业务唯⼀ID String bizUniNo = message.getUserProperty("bizUniNo"); //如果本地事务表(t_message_transaction)存在记录，则认为提交；如果不存在返回未知。 //如果多次回查还是未查到消息，则回滚。 if (query(bizUniNo)>0){

return LocalTransactionState.COMMIT_MESSAGE; }else{

return LocalTransactionState.UNKNOW; }

} //查询数据库是否存在记录 public int query(String bizUniNo){

//select count(1) from t_message_transaction where biz_uni_no = #{bizUniNo} return 1;

} }

上⾯的代码是笔者在这本书⾥，抄录出来的，如果是按照这种做法， 实际上是有问题的，我们来分析 ⼀下。

- 1、下单异常 我们看上⾯的订单下单的伪代码，⾥⾯包含两个操作：订单⼊库和事务消息发送。 那么我们继续思考： - 如果订单⼊库的时候发⽣异常，这个没问题，因为事务消息也不会发送； - 如 果订单⼊库执⾏完毕，但发送事务消息报错。这个也没问题，订单数据会回滚； - 如果订单⼊库执⾏ 完毕，发送事务消息也没有报错。但返回的不是SEND_OK状态，这个是有问题的。


因为只有发送事务消息成功，并且发送状态为SEND_OK，才会执⾏监听器中的本地事务，向 t_message_transaction表写⼊事务⽇志。

那么就会造成⼀个现场：本地订单数据已经⼊库，但是由于没有返回SEND_OK状态，导致不会执⾏本地 事务中的事务⽇志。那么这条事务消息早晚会被回滚，最后的问题就是⽤户下单成功，但没有增加积 分。

- 2、本地事务执⾏异常 事实上，第⼀个问题也可以规避。那就是在发送完事务消息后，再判断下发送状态是不是SEND_OK，如 果不是的话，就通过抛异常的⽅式来回滚订单数据。 但是，还有第⼆个问题： 如果订单数据和事务消息发送都没有问题，但是在执⾏本地事务时，写⼊事务⽇志时发⽣异常怎么办 ？ 如果是这样，也会导致本地订单数据已经⼊库，但是事务⽇志没有写⼊，在事务状态回查的时候⼀直 查询不到此记录，最后只能回滚事务消息。最后的现象同样是⽤户下单成功，但没有增加积分。 但是在书中，作者有这样⼀段话： executeLocalTransaction，该⽅法主要设置本地事务状态，与业务代码在⼀个事务中。例如在 OrderService#createOrder中，只要本地事务提交成功，该⽅法也会提交成功。故在这⾥，主要是向 t_message_transaction添加⼀条记录，在事务回查时，如果存在记录，就认为该消息需要提交。 作者这段话的意思，我理解是说他们都处于⼀个本地事务中。如果createOrder⽅法执⾏成功，则 executeLocalTransaction⽅法也会执⾏成功；如果任何⼀⽅出错，都会回滚事务。 但是，我们从源码中分析的话，如果本地事务执⾏报错，订单数据是不会回滚的。

- 3、源码分析 ⾸先，我们要知道，executeLocalTransaction⽅法和createOrder⽅法确实在⼀个事务⾥。 这是因为executeLocalTransaction⽅法，是在发送事务消息之后，同步调⽤到的，所以它们在⼀个事务 ⾥。 我们来看源码中，事务消息发送的过程：


public TransactionSendResult sendMessageInTransaction(Message msg, LocalTransactionExecuter localTransactionExecuter, Object arg)throws MQClientException {

//发送事务消息返回结果 SendResult sendResult = null; //如果发送消息失败，抛出异常 try {

sendResult = this.send(msg); } catch (Exception var11) {

throw new MQClientException("send message Exception", var11);

} //初始化本地事务状态：未知状态 LocalTransactionState localTransactionState = LocalTransactionState.UNKNOW; Throwable localException = null; switch(sendResult.getSendStatus()) { //如果发送事务消息状态为send_ok case SEND_OK: try { //执⾏本地事务⽅法 if (transactionListener != null) {

this.log.debug("Used new transaction API"); localTransactionState = transactionListener.executeLocalTransaction(msg, arg);

}

} catch (Throwable var10) { this.log.info("executeLocalTransactionBranch exception", var10); this.log.info(msg.toString()); localException = var10;

} break;

//如果发送事务状态不是send_ok,该事务消息会被回滚 case FLUSH_DISK_TIMEOUT: case FLUSH_SLAVE_TIMEOUT: case SLAVE_NOT_AVAILABLE:

localTransactionState = LocalTransactionState.ROLLBACK_MESSAGE;

} //结束事务，就是根据本地事务状态，执⾏提交、回滚或暂不处理事务 try {

this.endTransaction(sendResult, localTransactionState, localException); } catch (Exception var9) {

this.log.warn("", var9);

} TransactionSendResult transactionSendResult = new TransactionSendResult(); transactionSendResult.setSendStatus(sendResult.getSendStatus()); transactionSendResult.setMessageQueue(sendResult.getMessageQueue()); transactionSendResult.setMsgId(sendResult.getMsgId()); transactionSendResult.setQueueOffset(sendResult.getQueueOffset()); transactionSendResult.setTransactionId(sendResult.getTransactionId()); transactionSendResult.setLocalTransactionState(localTransactionState);

return transactionSendResult;

} 上⾯的代码，就是发送事务消息的过程。我们重点来看，如果事务消息发送成功，并且返回状态为 SEND_OK，那么就去执⾏监听器中的executeLocalTransaction⽅法，这说明它们在⼀个事务中。 但是，在执⾏过程中，它⼿动捕获了 Throwable 异常。这就说明，即便执⾏本地事务失败，也不会触发 回滚的。 ⾄此，我们已经⾮常明确了，如果按照书⾥的流程来写代码，这块就会成为⼀个隐患点。 如果想规避这个问题，我们只能修改rocket-client中的代码，⽐如： try {

//执⾏本地事务⽅法 if (transactionListener != null) {

this.log.debug("Used new transaction API"); localTransactionState = transactionListener.executeLocalTransaction(msg, arg);

}

} catch (Throwable var10) { this.log.info("executeLocalTransactionBranch exception", var10); this.log.info(msg.toString()); localException = var10; throw new MQClientException(e.getMessage(),e);

} 笔者通过修改源码，并测试了⼀下，通过这种⼿动抛出异常的⽅式也是可以的。这样的话如果执⾏本 地事务的时候出错，也会回滚订单数据。 到这⾥，就能回答笔者本⽂2.4章节⾥的⼀个问题： 为什么在订单业务服务类中，需要有两个⽅法。⼀个⽤于向RocketMQ发送事务消息，⼀个⽤于真正的 业务数据落库。 总结 本⽂重点阐述了基于RocketMQ来实现最终⼀致性的分布式事务案例。 另外，也分享了关于《RocketMQ技术内幕》⼀书中，分布式事务示例代码，可能出现的异常问题。关 于这⼀点，也希望朋友们如果有不同看法，积极留⾔，共同交流。

