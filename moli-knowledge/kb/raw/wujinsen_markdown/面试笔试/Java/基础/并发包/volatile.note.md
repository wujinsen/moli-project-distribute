volatile特性： 可⻅性： 对⼀个volatile变量的读，总是能看到对这个volatile变量最后的写⼊ 原⼦性：对任意单个volatile变量的读/写具有原⼦性，volatile+复合操作不具原⼦性。

实现原理： 重排序分为编译器重排序和处理器重排序，为了实现volatile内存语义，J M会分别禁⽌这两种重排 序。

JSR-13增强volatile内存语义: 严格限制编译器和处理器对volatile变量与普通变量的重排序。

第⼀操作: volatile 写

write(){ int a = 2; /1 flag = true; /2

}

read(){ if(flag){ . /3 int i = a * a; /4

} }

