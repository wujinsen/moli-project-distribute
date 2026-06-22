npm使⽤国内镜像的⽅法

⼀.通过命令配置

- 1. 命令 npm config set registry https://registry.npm.taobao.org

- 2. 验证命令 npm config get registry 如果返回htps:/registry.npm.taobao.org，说明镜像配置成功。


⼆、通过使⽤cnpm安装

- 1. 安装cnpm npm install -g cnpm --registry=https://registry.npm.taobao.org

- 2. 使⽤cnpm cnpm install xxx


