现在使⽤命令 ssh-keygen -t rsa ⽣成ssh，默认是以新的格式⽣成，id_rsa的第⼀⾏变成了“BEGIN OPENSSH PRIVATE KEY” ⽽不在是“BEGIN RSA PRIVATE KEY”，此时⽤来msyql、MongoDB，配 置ssh登陆的话，可能会报 “Resource temporarily unavailable. Authentication by key (/Users/youname/.ssh/id_rsa) failed (Error -16). (Error #35)” 提示资源不可⽤，这就是id_rsa 格式不对 造成的 解决⽅法（⼀）： 使⽤ ssh-keygen -m PEM -t rsa -b 4096 来⽣成

- -m 参数指定密钥的格式，PEM（也就是RSA格式）是之前使⽤的旧格式
- -b：指定密钥⻓度；
- -e：读取openssh的私钥或者公钥⽂件；
- -C：添加注释；
- -f：指定⽤来保存密钥的⽂件名；
- -i：读取未加密的ssh-v2兼容的私钥/公钥⽂件，然后在标准输出设备上显示openssh兼容的私钥/公钥；
- -l：显示公钥⽂件的指纹数据；
- -N：提供⼀个新密语；
- -P：提供（旧）密语；
- -q：静默模式；
- -t：指定要创建的密钥类型


