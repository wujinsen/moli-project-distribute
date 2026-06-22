在分析⽹站的客户来源经常会要求根据客户的ip地址去判断客户国家位置或者城市位置。当然要做到这 ⼀步，你要有⼀个详细的ip地址库。

ip地址库中⼀般是通过ip地址转换来的数字（⻓整数）来划分国家或者城市。ip地址库数据库的⼀般格 式为：

... startIpLongNumber endIpLongNumber CountryName CountryCode CityName

...

⽽在应⽤中需要将客户的ip地址字符串转成⼀个⻓整数，然后才能到地址库中去查找。

下⾯就给出ip->long , long->ip的转换⽅法。

Java代码 /*

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.


- * ip地址转成整数.
- * @param ip
- * @return
- */ publicstaticlong ip2long(String ip) {


String[] ips = ip.split("[.]"); long num = 16 7216L*Long.parseLong(ips[0]) + 6536L*Long.parseLong(ips[1]) + 256*

Long.parseLong(ips[2]) + Long.parseLong(ips[3]);

return num; }

/*

- * 整数转成ip地址.
- * @param ipLong
- * @return
- */ publicstatic String long2ip(long ipLong) {


/long ipLong = 1037591503; long mask[] = {0x 0F,0x 0F0,0x0F 0,0xF 0}; long num = 0;

- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.


StringBufer ipInfo = new StringBufer(); for(int i=0;i<4;i +){

num = (ipLong & mask[i])>(i*8); if(i>0) ipInfo.insert(0,"."); ipInfo.insert(0,Long.toString(num,10);

} return ipInfo.toString();

}

通过上⾯这2个⽅法，就可以很⽅便的将⼀个ip地址字符串折算为⼀个long数字；或者将⼀个long数字 还原成⼀个ip地址字符串。

