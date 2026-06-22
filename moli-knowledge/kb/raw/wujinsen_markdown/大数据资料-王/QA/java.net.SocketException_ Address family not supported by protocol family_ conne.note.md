htp:/stackoverflow.com/questions/16373906/adres-family-not-suported-by-protocol-f amily-socketexception-on-a-specific

查了下

stackoverflow上说是因为Java7会⾃动使⽤IPv6进⾏连接，修改⽅法是 添加jvm参数 Djava.net.preferIPv4Stack=true，该参数会强制jvm⾛IPv4

