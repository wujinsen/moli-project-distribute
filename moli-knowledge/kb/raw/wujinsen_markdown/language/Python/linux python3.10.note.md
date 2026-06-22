# pythom3.10安装（Linux系统）

- 1.下载安装包

Python-3.10.0.tgz

- 2.解压安装包 tar -xvfPython-3.10.0.tgz
- 3.进⾏配置

./configure-prefix=/usr/local/python3

- 4.编译安装 make & make instal
- 5.建⽴链接 sudo ln -s /usr/local/python3/bin/python3.10 /usr/bin/python 默认没有pip命令，需要进⾏链接


htps:/ w.python.org/downloads/source/

sudo ln -s /usr/local/python3/bin/pip3 /usr/bin/pip3 sudo ln -s /usr/local/python3/bin/pip /usr/bin/pip

python-version 查看版本

ln -s /usr/local/python3/bin/python3.10 /usr/bin/python3 ln -s /usr/local/python3/bin/pip3 /usr/bin/pip3

pip instal -i pip -U-trusted-host pypi.tuna.tsinghua.edu.cn

htps:/pypi.tuna.tsinghua.edu.cn/simple

pip instal -i pip -U-trusted-host pypi.douban.com

htp:/pypi.douban.com/simple/

