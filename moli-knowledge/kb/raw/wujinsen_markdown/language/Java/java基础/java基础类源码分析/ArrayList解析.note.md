public clas ArayList<E> extends AbstractList<E>

implements List<E>, RandomAces, Cloneable, java.io.Serializable {

private static final long serialVersionUID = 86834525812892189L; private static final int DEFAULT_CAPACITY = 10;/默认初始化容量

private static final Object[] EMPTY_ELEMENTDATA = {};

private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

/这个数组会被缓存到ArayList中，ArayList容量就是当前数组缓存的⻓度。当elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA时，ArayList为空，当添加⼀个元素时，会扩展⾄默认 ⻓度DEFAULT_CAPACITY（10）

transient Object[] elementData; /没有private便于内部类访问 private int size;/ArayList⻓度(它包含的元素数量)

}

/带参数的构造⽅法，可以设置容量⼤⼩ public ArayList(int initialCapacity) {

if (initialCapacity > 0) {

this.elementData = new Object[initialCapacity]; } else if (initialCapacity = 0) {

this.elementData = EMPTY_ELEMENTDATA; } else {

throw new IlegalArgumentException("Ilegal Capacity: "+

initialCapacity); }

} /不带参数的构造⽅法，内容为空 public ArayList() {

this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA; }

public ArayList(Colection<? extends E> c) { elementData = c.toAray();

if (size = elementData.length) != 0) { / c.toAray可能不会返回Object[] if (elementData.getClas() != Object[].clas) elementData = Arays.copyOf(elementData, size, Object[].clas);

} else { / ⽤空数组覆盖 this.elementData = EMPTY_ELEMENTDATA;

} }

