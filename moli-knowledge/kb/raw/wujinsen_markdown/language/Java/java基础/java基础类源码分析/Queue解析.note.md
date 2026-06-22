Queue接⼝及其⽅法： public interface Queue<E> extends Colection<E> {

bolean ad(E e); bolean ofer(E e); E remove(); E pol(); E element(); E pek();

} pek()：返回队列头元素 ，队列为空返回nul element()：返回队列头元素，队列为空抛出 NoSuchElementException pol(): 移除队头元素并返回头元素，队列为空返回nul remove(): y移除队列头元素并返回头元素，队列为空返回抛出NoSuchElementException Queue 队列是典型的FIFO容器，LinkedList提供了⽅法⽀持队列的⾏为，并且它实现了Queue接⼝。 LinkedList类的实现⽅式： public clas LinkedList<E>

extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, java.io.Serializable

Deque接⼝继承了Queue接⼝

public clas QueueDemo {

public static void main(String[] args) { Queue<Integer> queue = new LinkedList<Integer>(); Random random = new Random(47); for (int i = 0; i < 10; i +)

queue.ofer(random.nextInt(i + 10); printQ(queue); Queue<Character> qc = new LinkedList<Character>(); for (char c : "Brontosaurus".toCharAray()

qc.ofer(c); printQ(qc);

}

public static void printQ(Queue queue) {

while (queue.pek() != nul)/ pek()返回队列的头元素，如果队列为空则返回nul System.out.print(queue.remove() + " ");/remove() 返回队列的头元素 System.out.println();

} }

