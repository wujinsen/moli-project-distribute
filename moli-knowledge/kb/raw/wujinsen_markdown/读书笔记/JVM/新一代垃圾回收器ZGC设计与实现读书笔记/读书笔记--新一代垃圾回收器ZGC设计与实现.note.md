1.1垃圾回收算法

引⽤计数法:

可达性分析法:

rot thread frame 线程针栈 symbol dictionary 符号表 string table 字符串表 object synchronizer 对象监视器 universe 元数据对象

垃圾回收算法: 实现⽅式：copy(复制), mark-swep(标记-清除), mark-compact(标记-压缩) 回收⽅式：串⾏回收、并⾏回收、并发回收 内存管理分类: 代管理、⾮代管理 垃圾回收器: 串⾏回收器、并⾏回收器、CMS、G1、ZGC、 Shenandoah 串⾏执⾏: 应⽤程序和垃圾回收器交替运⾏ 并⾏执⾏: 应⽤程序和垃圾回收器交替运⾏ 并发执⾏: 应⽤程序和垃圾回收器同时运⾏

串⾏回收: mutator STW(Stop The World) 新⽣代通常采⽤复制算法，⽼年代通常采⽤标记压缩算法

![image 1](<读书笔记--新一代垃圾回收器ZGC设计与实现.note_images/imageFile1.png>)

# 没有mutator运⾏的区间都是STW

