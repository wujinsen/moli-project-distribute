Stack类及其⽅法： public clas Stack<E> extends Vector<E> {

public E push(E item) {} public synchronized E pop() {} public synchronized E pek() {} public bolean empty() {} public synchronized int search(Object o) {}

} push(E item) ：把数据压⼊栈 pop()：移除头元素并返回头元素，栈为空则抛出EmptyStackException。 pek()：返回队列头元素，栈为空则抛出EmptyStackException。

