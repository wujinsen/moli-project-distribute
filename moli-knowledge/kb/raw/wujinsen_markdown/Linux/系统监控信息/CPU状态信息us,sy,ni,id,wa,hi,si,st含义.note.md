使⽤系统命令top即可看到如下类似信息： Cpu(s): 0.0%us, 0.5%sy, 0.0%ni, 99.5%id, 0.0%wa, 0.0%hi, 0.0%si, 0.0%st 但不知什么含义？google之

I try to explain these: us: is meaning of "user CPU time" sy: is meaning of "system CPU time" ni: is meaning of" nice CPU time" id: is meaning of "idle" wa: is meaning of "iowait" hi：is meaning of "hardware irq" si : is meaning of "software irq" st : is meaning of "steal time"

中⽂翻译为：

us ⽤户空间占⽤CPU百分⽐ sy 内核空间占⽤CPU百分⽐ ni ⽤户进程空间内改变过优先级的进程占⽤CPU百分⽐ id 空闲CPU百分⽐ wa 等待输⼊输出的CPU时间百分⽐ hi 硬件中断 si 软件中断 st: 实时（来源http://bbs.chinaunix.net/thread-1958596-1-1.html）

