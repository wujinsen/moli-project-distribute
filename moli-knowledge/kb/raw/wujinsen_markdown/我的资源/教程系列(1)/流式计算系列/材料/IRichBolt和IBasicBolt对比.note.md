作为storm的使⽤者，有两件事情要做以更好的利⽤storm的可靠性特征。 ⾸先，在你⽣成⼀个新的 tuple的时候要通知storm; 其次，完成处理⼀个tuple之后要通知storm。 这样storm就可以检测整个 tuple树有没有完成处理，并且通知源spout处理结果。storm提供了⼀些简洁的api来做这些事情。 由⼀个tuple产⽣⼀个新的tuple称为： anchoring。你发射⼀个新tuple的同时也就完成了⼀次 anchoring。看下⾯这个例⼦： 这个bolt把⼀个包含⼀个句⼦的tuple分割成每个单词⼀个tuple。

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


publicclas SplitSentence implements IRichBolt { Output Colector _colector;

publicvoid prepare(Map conf, TopologyContext context, OutputColector colector) {

_colector = colector; }

publicvoid execute(Tuple tuple) { String sentence = tuple.getString(0); for(String word: sentence.split(" ") {

_colector.emit(tuple,newValues(word);

} _colector.ack(tuple);

}

publicvoid cleanup() { }

publicvoid declareOutputFields(OutputFieldsDeclarer declarer) {

declarer.declare(newFields("word"); }

}

我们通过anchoring来构造这个tuple树，最后⼀件要做的事情是在你处理完当个tuple的时候告诉 storm, 通过OutputColector类的ack和fail⽅法来做，如果你回过头来看看SplitSentence的例⼦， 你 可以看到“句⼦tuple”在所有“单词tuple”被发出之后调⽤了ack。

你可以调⽤OutputColector 的fail⽅法去⽴即将从消息源头发出的那个tuple标记为fail， ⽐如你查询了 数据库，发现⼀个错误，你可以⻢上fail那个输⼊tuple， 这样可以让这个tuple被快速的重新处理， 因 为你不需要等那个timeout时间来让它⾃动fail。 每个你处理的tuple， 必须被ack或者fail。因为storm追踪每个tuple要占⽤内存。所以如果你不ack/fail 每⼀个tuple， 那么最终你会看到OutOfMemory错误。 ⼤多数Bolt遵循这样的规律：读取⼀个tuple；发射⼀些新的tuple；在execute的结束的时候ack这个 tuple。这些Bolt往往是⼀些过滤器或者简单函数。Storm为这类规律封装了⼀个BasicBolt类。如果⽤ BasicBolt来做， 上⾯那个SplitSentence可以改写成这样：

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.


publicclas SplitSentence implements IBasicBolt { publicvoid prepare(Map conf,

TopologyContext context) { }

publicvoid execute(Tuple tuple,

BasicOutputColector colector) { String sentence = tuple.getString(0); for(String word: sentence.split(" ") { colector.emit(newValues(word); }

}

publicvoid cleanup() {

}

publicvoid declareOutputFields(

OutputFieldsDeclarer declarer) { declarer.declare(newFields("word");

} }

这个实现⽐之前的实现简单多了， 但是功能上是⼀样的。 发送到BasicOutputColector的tuple会⾃动和输⼊tuple相关联，⽽在execute⽅法结束的时候那个输⼊ tuple会被⾃动ack的。 我们编写的时候使⽤IBasicBolt最⽅便了。或者 extends BaseBasicBolt类

