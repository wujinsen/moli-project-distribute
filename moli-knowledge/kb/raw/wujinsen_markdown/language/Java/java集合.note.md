![image 1](<java集合.note_images/imageFile1.png>)

# List和Set: List允许重复，Set不允许重复

HashSet: HashSet基于HashMap实现，⽆容量限制 HashSet是⾮线程安全 HashSet():

创建⼀个HashMap对象 ad(E)

调⽤HashMap的put(Object,Object)⽅法,key是要增加的元素，value是之前已经创建的⼀个的Object对象 remove(E)

调⽤HashMap的remove(E)⽅法 contains(E)

调⽤HashMap的containsKey(E)⽅法

iterator() 调⽤HashMap的keySet的iterator⽅法 HashSet不⽀持通过get(int)获取指定位置的元素，只能⾃⾏通过iterator⽅法获取

TreSet 实现⽅式: TreSet基于TreMap实现，对排序的⽀持 TreSet() 创建⼀个TreMap对象 ad(E)

调⽤TreMap的put(Object, Object)⽅法，key是要增加的元素，value是之前已经创建的⼀个final的

Object对象 remove(E)

调⽤TreMap的remove(object) iterator()

调⽤TreMap的navigableKeySet的iterator⽅法 TreSet和HashSet⼀样基于TreMap实现，⽀持排序 TreSet是⾮线程安全

HashMapHashMap是基于hashing的原理，我们使⽤put(key, value)存储对象到HashMap中，使⽤get(key)从 HashMap中获取对象。当我们给put()⽅法传递键和值时，我们先对键调⽤hashCode()⽅法，返回的hashCode⽤于找 到bucket位置来储存Entry对象。

