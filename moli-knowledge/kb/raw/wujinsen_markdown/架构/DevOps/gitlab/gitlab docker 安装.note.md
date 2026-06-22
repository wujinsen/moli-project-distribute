内存最好4个G，不然访问会出现502错误

1.通过docker安装gitlab-ce，需要先有docker 安装gitlab-ce:

For macOS users, use the userʼs$HOME/gitlab directory: export GITLAB_HOME=$HOME/gitlab

sudo docker run-detach \

- -hostname gitlab.example.com \

- -publish 43  43-publish 80 80-publish 2 2 \

- -name gitlab \

- -restart always \

- -volume $GITLAB_HOME/docker/gitlab/config:/etc/gitlab \

- -volume $GITLAB_HOME/docker/gitlab/logs:/var/log/gitlab \

- -volume $GITLAB_HOME/docker/gitlab/data:/var/opt/gitlab \ gitlab/gitlab-ce:latest


上⾯的参数说明： hostname按照⾃⼰的需要改 volume的冒号前⾯为物理机器上的实际⽬录，需提前建好，然后改为正确路径。冒号后⾯为挂载点， 不要改 publish的三个端⼝映射⾃⼰看情况来，⼀般⾃⼰⽹内使⽤，光⼀个80就好了，443和22都需要额外配 置数字证书什么的 安装完成之后，可以在portainer中的【containers】中查看所有的容器状态。

