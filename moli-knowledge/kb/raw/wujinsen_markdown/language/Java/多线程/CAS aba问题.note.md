ABA解决⽅案:加版本号 AtomicMarkableReference 可以解决,使⽤bolean变量⸺表示引⽤变量是否被更改过,不关⼼中间变 量变化了⼏次 AtomicStampedReference 也可以解决,其中的构造⽅法中initialStamp（时间戳）⽤来唯⼀标识引⽤变 量,引⽤变量中途被更改了⼏次

