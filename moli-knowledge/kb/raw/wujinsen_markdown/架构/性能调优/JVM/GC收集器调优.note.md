JVM性能调优实战⸺UseParalelGC

JVM性能调优实战⸺Concurent Mark Swep

JVM性能调优⸺G1

UseParalelGC

:

对于并发类型的GC来说，jvm默认开启了内存⾃动适配策略参数是UseAdaptiveSizePolicy。使⽤-X:UseAdaptiveSizePolicy来关闭jvm的⾃动适配策略。

-X TargetSurvivorRatio=80 -X MaxTenuringThreshold=15

