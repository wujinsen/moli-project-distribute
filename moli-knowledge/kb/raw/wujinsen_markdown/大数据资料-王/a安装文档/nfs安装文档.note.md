- 1.yum -y instal nfs-utils rpcbind

- 2.mkdir /home/hadop/nfstest

- 3.vi /etc/exports #增加⼀⾏： /home/hadop/nfstest/ *(insecure,rw,no_rot_squash,no_al_squash,sync)

- 4.exportfs -r

- 5.service rpcbind start service nfs start

- 6.客户端挂载使⽤ mkdir /home/hadop/nfstest showmount -e 192.168.137.39 mount -t nfs 192.168.137.39:/home/hadop/nfstest /home/hadop/nfstest


