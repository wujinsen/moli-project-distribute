Java NIO (New IO) is an alternative IO API for Java (from Java 1.4), meaning alternative to the standard and API's. Java NIO oﬀers a diﬀerent way of working with IO than the standard IO API's.

Java IO Java Networking

# Java NIO: Channels and Buﬀers

In the standard IO API you work with byte streams and character streams. In NIO you work with channels and buﬀers. Data is always read from a channel into a buﬀer, or written from a buﬀer to a channel.

# Java NIO: Non-blocking IO

Java NIO enables you to do non-blocking IO. For instance, a thread can ask a channel to read data into a buﬀer. While the channel reads data into the buﬀer, the thread can do something else. Once data is read into the buﬀer, the thread can then continue processing it. The same is true for writing data to channels.

# Java NIO: Selectors

Java NIO contains the concept of "selectors". A selector is an object that can monitor multiple channels for events (like: connection opened, data arrived etc.). Thus, a single thread can monitor multiple channels for data. How all this works is explained in more detail in the next text in this series - the Java NIO overview.

Next:

Java NIO Overview

