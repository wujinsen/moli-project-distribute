HashMap的⼯作原理是近年来常⻅的Java⾯试题。⼏乎每个Java程序员都知道HashMap，都知 道哪⾥要⽤HashMap，知道HashTable和HashMap之间的区别，那么为何这道⾯试题如此特殊 呢？是因为这道题考察的深度很深。这题经常出现在⾼级或中⾼级⾯试中。投资银⾏更喜欢问这 个问题，甚⾄会要求你实现HashMap来考察你的编程能⼒。ConcurrentHashMap和其它同步集 合的引⼊让这道题变得更加复杂。让我们开始探索的旅程吧！

![image 1](<HashMap的工作原理.note_images/imageFile1.png>)

# 先来些简单的问题

“你⽤过HashMap吗？” “什么是HashMap？你为什么⽤到它？”

⼏乎每个⼈都会回答“是的”，然后回答HashMap的⼀些特性，譬如HashMap可以接受null 键值和值，⽽HashTable则不能；HashMap是⾮synchronized;HashMap很快；以及HashMap 储存的是键值对等等。这显示出你已经⽤过HashMap，⽽且对它相当的熟悉。但是⾯试官来个急 转直下，从此刻开始问出⼀些刁钻的问题，关于HashMap的更多基础的细节。⾯试官可能会问出 下⾯的问题：

“你知道HashMap的⼯作原理吗？” “你知道HashMap的get()⽅法的⼯作原理吗？”

你也许会回答“我没有详查标准的Java API，你可以看看Java源代码或者Open JDK。”“我可 以⽤Google找到答案。”

但⼀些⾯试者可能可以给出答案，“HashMap是基于hashing的原理，我们使⽤put(key, value)存储对象到HashMap中，使⽤get(key)从HashMap中获取对象。当我们给put()⽅法传 递键和值时，我们先对键调⽤hashCode()⽅法，返回的hashCode⽤于找到bucket位置来储存 Entry对象。”这⾥关键点在于指出，HashMap是在bucket中储存键对象和值对象，作为 Map.Entry。这⼀点有助于理解获取对象的逻辑。如果你没有意识到这⼀点，或者错误的认为仅 仅只在bucket中存储值的话，你将不会回答如何从HashMap中获取对象的逻辑。这个答案相当 的正确，也显示出⾯试者确实知道hashing以及HashMap的⼯作原理。但是这仅仅是故事的开 始，当⾯试官加⼊⼀些Java程序员每天要碰到的实际场景的时候，错误的答案频现。下个问题可 能是关于HashMap中的碰撞探测(collision detection)以及碰撞的解决⽅法：

“当两个对象的hashcode相同会发⽣什么？” 从这⾥开始，真正的困惑开始了，⼀些⾯试者会回 答因为hashcode相同，所以两个对象是相等的，HashMap将会抛出异常，或者不会存储它们。 然后⾯试官可能会提醒他们有equals()和hashCode()两个⽅法，并告诉他们两个对象就算

hashcode相同，但是它们可能并不相等。⼀些⾯试者可能就此放弃，⽽另外⼀些还能继续挺进， 他们回答“因为hashcode相同，所以它们的bucket位置相同，‘碰撞’会发⽣。因为HashMap使⽤ LinkedList存储对象，这个Entry(包含有键值对的Map.Entry对象)会存储在LinkedList中。”这

个答案⾮常的合理，虽然有很多种处理碰撞的⽅法，这种⽅法是最简单的，也正是HashMap的处 理⽅法。但故事还没有完结，⾯试官会继续问：

“如果两个键的hashcode相同，你如何获取值对象？” ⾯试者会回答：当我们调⽤get()⽅法，

HashMap会使⽤键对象的hashcode找到bucket位置，然后获取值对象。⾯试官提醒他如果有两 个值对象储存在同⼀个bucket，他给出答案:将会遍历LinkedList直到找到值对象。⾯试官会问因 为你并没有值对象去⽐较，你是如何确定确定找到值对象的？除⾮⾯试者知道HashMap在

LinkedList中存储的是键值对，否则他们不可能回答出这⼀题。

其中⼀些记得这个重要知识点的⾯试者会说，找到bucket位置之后，会调⽤

keys.equals()⽅法去找到LinkedList中正确的节点，最终找到要找的值对象。完美的答案！

许多情况下，⾯试者会在这个环节中出错，因为他们混淆了hashCode()和equals()⽅法。 因为在此之前hashCode()屡屡出现，⽽equals()⽅法仅仅在获取值对象的时候才出现。⼀些优 秀的开发者会指出使⽤不可变的、声明作final的对象，并且采⽤合适的equals()和hashCode() ⽅法的话，将会减少碰撞的发⽣，提⾼效率。不可变性使得能够缓存不同键的hashcode，这将提 ⾼整个获取对象的速度，使⽤String，Interger这样的wrapper类作为键是⾮常好的选择。

如果你认为到这⾥已经完结了，那么听到下⾯这个问题的时候，你会⼤吃⼀惊。“如果 HashMap的⼤⼩超过了负载因⼦(load factor)定义的容量，怎么办？”除⾮你真正知道 HashMap的⼯作原理，否则你将回答不出这道题。默认的负载因⼦⼤⼩为0.75，也就是说，当⼀ 个map填满了75%的bucket时候，和其它集合类(如ArrayList等)⼀样，将会创建原来HashMap ⼤⼩的两倍的bucket数组，来重新调整map的⼤⼩，并将原来的对象放⼊新的bucket数组中。 这个过程叫作rehashing，因为它调⽤hash⽅法找到新的bucket位置。

如果你能够回答这道问题，下⾯的问题来了：“你了解重新调整HashMap⼤⼩存在什么问题 吗？”你可能回答不上来，这时⾯试官会提醒你当多线程的情况下，可能产⽣条件竞争(race condition)。

当重新调整HashMap⼤⼩的时候，确实存在条件竞争，因为如果两个线程都发现HashMap 需要重新调整⼤⼩了，它们会同时试着调整⼤⼩。在调整⼤⼩的过程中，存储在LinkedList中的 元素的次序会反过来，因为移动到新的bucket位置的时候，HashMap并不会将元素放在 LinkedList的尾部，⽽是放在头部，这是为了避免尾部遍历(tail traversing)。如果条件竞争发⽣ 了，那么就死循环了。这个时候，你可以质问⾯试官，为什么这么奇怪，要在多线程的环境下使 ⽤HashMap呢？：）

热⼼的读者贡献了更多的关于HashMap的问题：

为什么String, Interger这样的wrapper类适合作为键？ String, Interger这样的 wrapper类作为HashMap的键是再适合不过了，⽽且String最为常⽤。因为String是不可变 的，也是final的，⽽且已经重写了equals()和hashCode()⽅法了。其他的wrapper类也有 这个特点。不可变性是必要的，因为为了要计算hashCode()，就要防⽌键值改变，如果键值 在放⼊时和获取时返回不同的hashcode的话，那么就不能从HashMap中找到你想要的对 象。不可变性还有其他的优点如线程安全。如果你可以仅仅通过将某个field声明成final就能 保证hashCode是不变的，那么请这么做吧。因为获取对象的时候要⽤到equals()和 hashCode()⽅法，那么键对象正确的重写这两个⽅法是⾮常重要的。如果两个不相等的对象 返回不同的hashcode的话，那么碰撞的⼏率就会⼩些，这样就能提⾼HashMap的性能。

1.

- 2.
- 3.


我们可以使⽤⾃定义的对象作为键吗？ 这是前⼀个问题的延伸。当然你可能使⽤任何对象作 为键，只要它遵守了equals()和hashCode()⽅法的定义规则，并且当对象插⼊到Map中之 后将不会再改变了。如果这个⾃定义对象时不可变的，那么它已经满⾜了作为键的条件，因 为当它创建之后就已经不能改变了。 我们可以使⽤CocurrentHashMap来代替HashTable吗？这是另外⼀个很热⻔的⾯试 题，因为ConcurrentHashMap越来越多⼈⽤了。我们知道HashTable是synchronized的， 但是ConcurrentHashMap同步性能更好，因为它仅仅根据同步级别对map的⼀部分进⾏上 锁。ConcurrentHashMap当然可以代替HashTable，但是HashTable提供更强的线程安全 性。看看这篇博客查看Hashtable和ConcurrentHashMap的区别。

我个⼈很喜欢这个问题，因为这个问题的深度和⼴度，也不直接的涉及到不同的概念。让我 们再来看看这些问题设计哪些知识点：

hashing的概念 HashMap中解决碰撞的⽅法 equals()和hashCode()的应⽤，以及它们在HashMap中的重要性 不可变对象的好处 HashMap多线程的条件竞争 重新调整HashMap的⼤⼩

# 总结

HashMap的⼯作原理

HashMap基于hashing原理，我们通过put()和get()⽅法储存和获取对象。当我们将键值对 传递给put()⽅法时，它调⽤键对象的hashCode()⽅法来计算hashcode，让后找到bucket位置 来储存值对象。当获取对象时，通过键对象的equals()⽅法找到正确的键值对，然后返回值对 象。HashMap使⽤LinkedList来解决碰撞问题，当发⽣碰撞了，对象将会储存在LinkedList的下 ⼀个节点中。 HashMap在每个LinkedList节点中储存键值对对象。

当两个不同的键对象的hashcode相同时会发⽣什么？ 它们会储存在同⼀个bucket位置的 LinkedList中。键对象的equals()⽅法⽤来找到键值对。

因为HashMap的好处⾮常多，我曾经在电⼦商务的应⽤中使⽤HashMap作为缓存。因为⾦ 融领域⾮常多的运⽤Java，也出于性能的考虑，我们会经常⽤到HashMap和 ConcurrentHashMap。你可以查看更多的关于HashMap和HashTable的⽂章。

个⼈总结:

“你知道HashMap的⼯作原理吗？” “你知道HashMap的get()⽅法的⼯作原理吗？”

“当两个对象的hashcode相同会发⽣什么？”

因为HashMap使⽤LinkedList存储对象，这个Entry(包含有键值对的Map.Entry对象)会存储在 LinkedList中

“如果两个键的hashcode相同，你如何获取值对象？”

当我们调⽤get()⽅法，HashMap会使⽤键对象的hashcode找到bucket位置，然后获取值对 象。

找到bucket位置之后，会调⽤keys.equals()⽅法去找到LinkedList中正确的节点，最终找到要 找的值对象。

