在eth0的⽂件中加⼊DNS /etc/sysconfig/network-scripts/ ifcfg-eth0 的内容： # Intel Corporation 82540EM Gigabit Ethernet ControlerDEVICE=eth0BOTPROTO=noneONBOT=yesHWADR=08  0 27:c:64 86NETMASK= 25.25.25.0IPADR=192.168.0.15GATEWAY=192.168.0.1TYPE=EthernetUSERCTL=noIPV6INIT= noPERDNS=no #我在这添加了DNS后，/etc/ resolv.conf⽂件内容保存下来了 DNS1=202.96.128.86DNS2=202.96.128.16DNS3=8.8.8.8

