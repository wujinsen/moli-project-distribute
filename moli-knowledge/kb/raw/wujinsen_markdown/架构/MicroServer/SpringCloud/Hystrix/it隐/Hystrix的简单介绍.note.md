最近⼏天我直在探索Netflix Hystrix library,领会到了这个优秀类库提供的特性。

引⽤Hystrix⽹站的原话：

Hystrix is a latency and fault tolerance library designed to isolate points of access to remote systems, services and 3rd party libraries, stop cascading failure and enable resilience in complex distributed systems where failure is inevitable.

Hystrix通过控制那些访问远程系统、服务和第三⽅库的节点，从⽽对延迟和故障提供更强⼤的容错 能⼒，阻⽌故障的连锁反应

，并允许你快速失败并迅速恢复。

这⾥有很多要分析的关键词，然⽽体验Hystrix的最佳⽅式就是亲⼿做⼀个例⼦来试试。

⼀个不可预测的服务

考虑⼀个服务，⼀个携带json结构信息并且返回⼀个确认的奇怪服务。 {

"id":"1", "payload": "Sample Payload", "throw_exception":false, "delay_by": 0

}

这个服务有⼀个payload属性，但是额外多带了两个属性。 delay_by 在延迟到达指定的毫秒数后返回⼀个确认响应。 throw_exceptions 在指定的延迟后导致异常

下⾯是响应例⼦： {

"id":"1", "received":"Sample Payload", "payload":"Reply Mesage"

}

这⾥是我的 ，可以下载示例代码。在这个例⼦中我⽤到了 ,示例代码处理请 求的部分⾮常简洁。来看看 这个类库⽤在这是多好⽤吧：

github地址 Netflix Karyon2

import com.netflix.governator.anotations.Configuration; import rx.Observable; import service1.domain.Mesage; import service1.domain.MesageAcknowledgement;

import java.util.concurent.TimeUnit;

public clas MesageHandlerServiceImpl implements MesageHandlerService {

@Configuration("reply.mesage") private String replyMesage;

public Observable<MesageAcknowledgement> handleMesage(Mesage mesage) { loger.info("About to Acknowledge"); return Observable.timer(mesage.getDelayBy(), TimeUnit.MI LISECONDS)

.map(l -> mesage.isThrowException()

.map(throwException -> { if (throwException) {

throw new RuntimeException("Throwing an exception!");

} return new MesageAcknowledgement(mesage.getId(), mesage.getPayload(),

replyMesage);

}); }

}

在这⾥，我们就有了⼀个可以响应随意的延迟和失败的候选服务了。

服务的客户端

Netflix Feign

现在看⼀下客户端，我们⽤ 来进⾏调⽤，这是另⼀个很棒的类库，需通过注解⽅式来使 ⽤： package agregate.service;

import agregate.domain.Mesage; import agregate.domain.MesageAcknowledgement; import feign.RequestLine;

public interface RemoteCalService { @RequestLine("POST /mesage") MesageAcknowledgement handleMesage(Mesage mesage);

}

在这⼏⾏中，它创建了⼀个代理，其实现了使⽤配置的接⼝。

RmoteCalService remoteCalService = Feign.builder()

.encoder(new JacksonEncoder() .decoder(new JacksonDecoder() .target(RemoteCalService.clas, "htp:/127.0.0.1  89");

我有多个针对这个远程客户端的委托调⽤，所有的都在下⾯这个url中 http://localhost:8888/noHystrix?message=Hello&delay_by=0&throw_exception=false 下⾯第⼀个例⼦是没有使⽤Hystrix的例⼦。

没有使⽤Hystrix的例⼦

在第⼀个例⼦中，考虑不⽤Hystrix来调⽤这个远程服务，如果我尝试以以下⽅式调⽤ http://localhost:8888/noHystrix?message=Hello&delay_by=5000&throw_exception=false或 http://localhost:8888/noHystrix?message=Hello&delay_by=5000&throw_exception=true， 在这两种实例中⽤户对服务的请求都会在等待5秒后才会收到响应。

有些事情在这⾥就⽴即很明显了：

- 1.如果服务响应缓慢，那么客户对服务的请求就会被强制等待到服务返回。

- 2.在⾼负载下，很有可能所有处理⽤户请求的线程资源被耗竭，⽽不能响应⽤户的进⼀步请求。

- 3.如果服务抛出异常，客户端不能很好的处理。


Hystrix命令包装远程调⽤

在上⼀个例⼦中，我⽤50个⽤户进⾏了⼀下压⼒测试，得到结果如下：

=

=

- Global Information > request count 50 (OK=50 KO=0 ) > min response time 507 (OK=507KO=- ) > max response time 3408 (OK=3408 KO=- ) > mean response time 1797 (OK=1797 KO=- ) > std deviation 8760 (OK=8760KO=- ) > response time 50th percentile 19532 (OK=19532 KO=- ) > response time 75th percentile 24386 (OK=24386 KO=- ) > mean requests/sec 1.425 (OK=1.425 KO=- )

基本上是5秒延迟，在到了75%的时候，延迟竟然达到了25秒。现在来看⼀下⽤Hystrix命令包装后 调⽤的结果：

=

=

- Global Information > request count 50 (OK=50 KO=0 ) > min response time 1 (OK=1 KO=- ) > max response time 1014 (OK=1014KO=- ) > mean response time 2 (OK=2 KO=- ) > std deviation 141 (OK=141 KO=- ) > response time 50th percentile 2 (OK=2 KO=- ) > response time 75th percentile 2 (OK=2 KO=- ) > mean requests/sec 48.123 (OK=48.123 KO=- )

奇怪的是，测试到了75%的时候时间竟然是2毫秒！这怎么可能呢！然⽽⽤了Hystrix提供的优秀⼯具 后， 结果已经很明显了。现在是本次测试的Hystrix控制台视图。

![image 1](<Hystrix的简单介绍.note_images/imageFile1.png>)

这⾥的前10个请求已经超时，任何超过Hystrix默认时间1秒的，⼀但前10个交易失败后，命令就会堵塞 其他客户对远程服务的请求。 ，因此会有较低的响应时间。为什么这些交易没有显示失败呢，这是因为这⾥会有⼀个反馈，优雅的 告诉⽤户请求失败了。 作者：supercrsky 发表于2015/10/13 16:10:46 原⽂链接

