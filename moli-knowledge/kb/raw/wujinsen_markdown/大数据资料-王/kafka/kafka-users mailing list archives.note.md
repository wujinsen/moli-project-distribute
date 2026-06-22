# Site indexL · ist index

<table>
  <tr>
    <th>Mesage view</th>
    <th>·<br><br>« Date » « Thread</th>
  </tr>
  <tr>
    <td>Top</td>
    <td>»<br><br>·<br><br>« Date » « Thread</td>
  </tr>
  <tr>
    <td>From</td>
    <td>» Neha Narkhede <neha.narkh.@gmail.com></td>
  </tr>
  <tr>
    <td>Subject</td>
    <td>Re: Geting LeaderNotAvailableException in console producer after increasing partitions</td>
  </tr>
  <tr>
    <td>Date</td>
    <td>from 4 to 16. Tue, 27 Aug 2013 16 52 24 GMT</td>
  </tr>
  <tr>
    <td colspan="2">1 As Guozhang said, your producer might give up sooner than the leader<br><br>2 election completes for the new topic. To confirm if your producer gave up<br><br>3 too soon, you can run the state change log merge tool for this topic and<br><br>4 see when the leader election finished for all partitions<br><br>5<br><br>6 ./bin/kafka-run-class.sh kafka.tools.StateChangeLogMerger --logs <location<br><br>7 to all state change logs> --topic <topic><br><br>8<br><br>9 Note that this tool requires you to give the state change logs for all<br><br>10 brokers in the cluster.<br><br>11<br><br>12<br><br>13 Thanks,<br><br>14 Neha<br><br>15<br><br>16<br><br>17 On Tue, Aug 27, 2013 at 9:45 AM, Guozhang Wang <wangguoz@gmail.com> wrote:<br><br>18<br><br>19 > Hello Rajasekar,<br><br>20 ><br><br>21 > In 0.8 producers keep a cache of the partition -> leader_broker_id map<br></td>
  </tr>
</table>


- 22 > which is used to determine to which brokers should the messages be sent.

- 23 > After new partitions are added, the cache on the producer has not populated

- 24 > yet hence it will throw this exception. The producer will then try to

- 25 > refresh its cache by asking the brokers "who are the leaders of these new

- 26 > partitions that I do not know of before". The brokers at the beginning also

- 27 > do not know this information, and will only get this information from

- 28 > controller which will only propagation the leader information after the

- 29 > leader elections have all been finished.

- 30 >

- 31 > If you set num.retries to 3 then it is possible that producer gives up too

- 32 > soon before the leader info ever propagated to producers, hence to

- 33 > producers also. Could you try to increase producer.num.retries and see if

- 34 > the producer can eventually succeed in re-trying?

- 35 >

- 36 > Guozhang

- 37 >

- 38 >

- 39 > On Tue, Aug 27, 2013 at 8:53 AM, Rajasekar Elango <relango@salesforce.com

- 40 > >wrote:

- 41 >

- 42 > > Hello everyone,

- 43 > >

- 44 > > We recently increased number of partitions from 4 to 16 and after that

- 45 > > console producer mostly fails with LeaderNotAvailableException and exits

- 46 > > after 3 tries:

- 47 > >

- 48 > > Here is last few lines of console producer log:

- 49 > >

- 50 > > No partition metadata for topic test-41 due to

- 51 > > kafka.common.LeaderNotAvailableException}] for topic [test-41]: class

- 52 > > kafka.common.LeaderNotAvailableException

- 53 > > (kafka.producer.BrokerPartitionInfo)

- 54 > > [2013-08-27 08:29:30,271] ERROR Failed to collate messages by topic,

- 55 > > partition due to: Failed to fetch topic metadata for topic: test-41

- 56 > > (kafka.producer.async.DefaultEventHandler)

- 57 > > [2013-08-27 08:29:30,271] INFO Back off for 100 ms before retrying send.

- 58 > > Remaining retries = 0 (kafka.producer.async.DefaultEventHandler)

- 59 > > [2013-08-27 08:29:30,372] INFO Secure sockets for data transfer is

- 60 > enabled

- 61 > > (kafka.producer.SyncProducerConfig)


- 62 > > [2013-08-27 08:29:30,372] INFO Fetching metadata from broker

- 63 > > id:0,host:localhost,port:6667,secure:true with correlation id 8 for 1

- 64 > > topic(s) Set(test-41) (kafka.client.ClientUtils$)

- 65 > > [2013-08-27 08:29:30,373] INFO begin ssl handshake for localhost/

- 66 > > 127.0.0.1:6667//127.0.0.1:36640 (kafka.security.SSLSocketChannel)

- 67 > > [2013-08-27 08:29:30,375] INFO finished ssl handshake for localhost/

- 68 > > 127.0.0.1:6667//127.0.0.1:36640 (kafka.security.SSLSocketChannel)

- 69 > > [2013-08-27 08:29:30,375] INFO Connected to localhost:6667:true for

- 70 > > producing (kafka.producer.SyncProducer)

- 71 > > [2013-08-27 08:29:30,380] INFO Disconnecting from localhost:6667:true

- 72 > > (kafka.producer.SyncProducer)

- 73 > > [2013-08-27 08:29:30,381] INFO Secure sockets for data transfer is

- 74 > enabled

- 75 > > (kafka.producer.SyncProducerConfig)

- 76 > > [2013-08-27 08:29:30,381] ERROR Failed to send requests for topics

- 77 > test-41

- 78 > > with correlation ids in [0,8] (kafka.producer.async.DefaultEventHandler)

- 79 > > kafka.common.FailedToSendMessageException: Failed to send messages after

- 80 > 3

- 81 > > tries.

- 82 > > at

- 83 > >

- 84 > >

> kafka.producer.async.DefaultEventHandler.handle(DefaultEventHandler.scala:90)

- 85

- 86 > > at kafka.producer.Producer.send(Producer.scala:74)

- 87 > > at

- 88 > kafka.producer.ConsoleProducer$.main(ConsoleProducer.scala:168)

- 89 > > at kafka.producer.ConsoleProducer.main(ConsoleProducer.scala)

- 90 > > [2013-08-27 08:29:30,383] INFO Shutting down producer

- 91 > > (kafka.producer.Producer)

- 92 > > [2013-08-27 08:29:30,384] INFO Closing all sync producers

- 93 > > (kafka.producer.ProducerPool)

- 94 > >

- 95 > >

- 96 > > Also, this happens only for new topics (we have auto.create.topic set to

- 97 > > true), If retry sending message to existing topic, it works fine. Is

- 98 > there

- 99 > > any tweaking I need to do to broker or to producer to scale based on

- 100 > number

- 101 > > of partitions?


<table>
  <tr>
    <th colspan="2">102 > ><br><br>103 > > --<br><br>104 > > Thanks in advance for help,<br><br>105 > > Raja.<br><br>106 > ><br><br>107 ><br><br>108 ><br><br>109 ><br><br>110 > --<br><br>111 > -- Guozhang<br><br>112 ><br><br>113<br><br>114<br></th>
  </tr>
  <tr>
    <td>Mime</td>
    <td>Unamed multipart/alternative (inline, None, 0 bytes)<br><br>Unamed text/plain</td>
  </tr>
  <tr>
    <td> </td>
    <td>(inline, None, 47 bytes)</td>
  </tr>
</table>


# View raw mesage

