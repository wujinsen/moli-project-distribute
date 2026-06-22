进⼊死信队列的三种情况:

- 1.消息被否定确认，使⽤ channel.basicNack 或 channel.basicReject ，并且此时requeue 属 性被设置为false。

- 2.消息在队列的存活时间超过设置的⽣存时间（ TL)时间。

- 3.消息队列的消息数量已经超过最⼤队列⻓度。


