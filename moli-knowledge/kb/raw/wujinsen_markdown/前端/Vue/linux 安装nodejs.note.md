下载源码安装包 cd ~ wget htps:/nodejs.org/dist/v14.15.4/node-v14.15.4-linux-x64.tar.xz 解压并放⼊指定⽬录 tar -xf node-v14.15.4-linux-x64.tar.xz mv node-v14.15.4-linux-x64 /usr/local/node 建⽴软连接 cd /usr/bin ln -s /usr/local/node/bin/node node ln -s /usr/local/node/bin/npm npm 切换淘宝镜像两种⽅法

普通使⽤

npm config set registry

htps:/registry.npm.taobao.org

配置后可通过下⾯⽅式来验证是否成功

npm config get registry

htps:/registry.npmjs.org/

npm config set registry

