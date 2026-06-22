# Ack机制

Storm可以保证从Spout发出的每个数据都被完全处理，从Spout发出的数据可能会产⽣成千上万的数 据。⼀个Tuple被完全处理指：这个Tuple以及这个Tuple产⽣的所有Tuple都被成功处理。⽽⼀个Tuple 被认为处理失败是被是指在timeout时间内没有被成功处理（包括显⽰的fail和超时导致的失败）。这个 timeout时间可以通过Config.TOPOLOGY_MESAGE_TIMEOUT_SECS来设定。Timeout的默认时长为 30秒 Storm的Bolt有BsicBolt和RichBolt，在BasicBolt中，BasicOutputColector在emit数据的时候，会⾃ 动和输⼊的tuple相关联，⽽在execute⽅法结束的时候那个输⼊tuple会被⾃动ack。 在使⽤RichBolt时要实现ack，则需要在emit数据的时候，显⽰指定该数据的源tuple，即 colector.emit(oldTuple, newTuple);并且需要在execute执⾏成功后调⽤源tuple的ack进⾏ack。

需要说明的是，要实现ack机制，必须在spout发射tuple的时候指定mesageId。并且需要在spout 中对tuple进⾏缓存，对于ack的tuple则从缓存队列中删除，对于fail的tuple可以选择重发。不同的 Tuple可以绑定同⼀个mesageId，表明这多个Tuple对⽤户来说是同⼀个消息单元。 这个mesageId只是业务上为了我们⽅便区分是哪个Tuple返回来的，Storm内部并不对其进⾏处理。 因此，不同的Tuple绑定同⼀个mesageId时，在ack和fail中不能区分是哪个Tuple成功或失败，只知道 其绑定的mesageId。

# 调整可靠性 (Tuning Reliability)

acker task是⾮常轻量级的， 所以⼀个topology⾥⾯不需要很多acker。你可以通过Strom UI(id: -1)来 跟踪它的性能。 如果它的吞吐量看起来不正常，那么你就需要多加点acker了。 如果可靠性对你来说不是那么重要 — 你不太在意在⼀些失败的情况下损失⼀些数据， 那么你可以通过 不跟踪这些tuple树来获取更好的性能。不去跟踪消息的话会使得系统⾥⾯的消息数量减少⼀半， 因为 对于每⼀个tuple都要发送⼀个ack消息。并且它需要更少的id来保存下游的tuple， 减少带宽占⽤。 有三种⽅法可以去掉可靠性。第⼀是把Config.TOPOLOGY_ACKERS 设置成 0. 在这种情况下， storm 会在spout发射⼀个tuple之后马上调⽤spout的ack⽅法。也就是说这个tuple树不会被跟踪。 第⼆个⽅法是在tuple层⾯去掉可靠性。 你可以在发射tuple的时候不指定mesageid来达到不跟粽某个 特定的spout tuple的⽬的。 最后⼀个⽅法是如果你对于⼀个tuple树⾥⾯的某⼀部分到底成不成功不是很关⼼，那么可以在发射这 些tuple的时候unanchor它们。 这样这些tuple就不在tuple树⾥⾯， 也就不会被跟踪了

# Ack原理

Storm中有个特殊的task，他们负责跟踪spout发出的每⼀个Tuple的Tuple树。当acker发现⼀个Tuple 树已经处理完成了，它会发送⼀个消息给产⽣这个Tuple的那个task。Acker的跟踪算法是Storm的主要 突破之⼀，对任意⼤的⼀个Tuple树，它只需要恒定的20字节就可以进⾏跟踪。

Acker跟踪算法的原理：acker对于每个spout-tuple保存⼀个ack-val的校验值，它的初始值是0，然后 每发射⼀个Tuple或Ack⼀个Tuple时，这个Tuple的id就要跟这个校验值异或⼀下，并且把得到的值更 新为ack-val的新值。那么假设每个发射出去的Tuple都被ack了，那么最后ack-val的值就⼀定是0。 Acker就根据ack-val是否为0来判断是否完全处理，如果为0则认为已完全处理。

