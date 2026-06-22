redis-monitor

htps:/github.com/youkeihi n/redis-monitor

安装Python-2.7.13

执⾏ ./configure 脚本 make make install

执⾏以上操作后，Python会安装在 /usr/local/bin ⽬录中，Python库安装在/usr/local/lib/pythonXX，XX为你使⽤的Python 的版本号。

安装pippip是⼀个安装和管理 Python 包的⼯具

tar -xzvf pip-1.5.4.tar.gz # cd pip-1.5.4 # python setup.py install

python setup.py install 执⾏报错缺少依赖包：ImportError: No module named setuptools

wget http://pypi.python.org/packages/source/s/setuptools/setuptools-0.6c11.tar.gz tar zxvf setuptools-0.6c11.tar.gz cd setuptools-0.6c11 python setup.py build python setup.py install

安装完pip执⾏： pip -V 现实版本号，安装完毕。

- 1.
- 2.
- 3.


⾸先安装Flask Web需要得库

pip install -r requirements.txt

运⾏web程序即可

./run_monitor

本地打开127.0.0.1:7259就可以看完⽹⻚了。截图⻅下⽅！

⼀：python2.7.12安装

## 1：获取python2.7.12

wget

## https://www.python.org/ftp/python/2.7.12/Python-2.7.12.tgz

## 2：确保安装gcc zlib zlib-devel python-devel libffi-devel openssl openssl-devel如果没有请 使⽤yum安装 gcc zlib zlib-devel python-devel libﬃ-devel openssl openssl-devel

## gcc 为编译时使⽤，如果不安装make会报错 zlib 为安装setuptools时使⽤，否则会有如下报错：

RuntimeError: Compression requires the (missing) zlib module openssl 如果不安装的话⽤pip安装模块的时候会报错，显示ssl module不可⽤之类的。。。

- 3: tar xvf Python-2.7.12.tgz


- 4：cd Python-2.7.12

- 5：sudo ./conﬁgure --preﬁx=/usr/local/python2.7 --with-zlib=/usr/local/include

- 6：make && make install

- 7：cd /usr/local/python2.7/include/python2.7/

- 8：拷⻉头⽂件到标准⽬录，防⽌安装类似ansible找不到头⽂件


sudo cp -a ./* /usr/local/bin/include/

- 9：cd /usr/bin/

- 10：重命名旧版本

sudo mv python python2.6

- 11：修改yum命令的python位置,防⽌yum的⽆法使⽤

sudo sed -i ‘s@#!/usr/bin/python@#!/usr/bin/python2.6@‘ /usr/bin/yum

- 12：设置新安装python的软连接

sudo ln -s /usr/local/python2.7/bin/python /usr/bin/python

- 13：查看是否安装成功


![image 1](<安装Python.note_images/imageFile1.png>)

==========================================================================

=============

# ⼆：pip安装

## 1：获取pip9.0.1

wget

https://pypi.python.org/packages/11/b6/abcb525026a4be042b486df43905d6893fb04f05 aac21c32c638e939e447/pip-9.0.1.tar.gz#md5=35f01da33009719497f01a4ba69d63c9

--nocheck-certiﬁcate

- 2：解压

tar xvf pip-9.0.1.tar.gz

- 3：设置环境变量


~/.bash_proﬁle添加如下内容

PYTHON_HOME=/usr/local/python2.7 PATH=$PATH:$HOME/bin:$PYTHON_HOME/bin

source ~/.bash_proﬁle

- 4:做⼀个软连接防⽌sudo pip command not found 出现


which pip #先⽤which 看⼀下pip的路径 sudo ln -s /usr/local/python2.7/bin/pip /usr/bin/pip

## 5：安装

sudo python setup.py install

## 6：选取国内pip源安装软件

创建⽂件及⽂件夹在⽤户家⽬录下 ~/.pip/pip.conf 添加如下内容： [global] index-url =http://pypi.douban.com/simple

- 7：可以安装模块了


pip install PyYAML --trusted-host pypi.douban.com

==========================================================================

=============

# 三：安装新版本之后的pip ssl错误

SSLError: Can‘t connect to HTTPS URL because the SSL module is not available

--------------------------------------------------------------------------

---注：此⽅法摘抄⾃⽹络，但并没有解决我的问题，所以重置了⼀下虚拟机，从第⼀步 安装的时候，确保所有依赖安装上，就不会出现各种各样的问题了。

--------------------------------------------------------------------------

-----

问题原因： curl的证书太⽼了需要下载最新的证书： 下载最新的证书⽂件

wget http://curl.haxx.se/ca/cacert.pem

更名为ca-bundle.crt放置到默认⽬录

mv cacert.pem ca-bundle.crt

mv ca-bundle.crt /etc/pki/tls/certs/

# yum出现“No module named yum”错误解决⽅法

安装了⼀个yum-downloadonly，发现yum⽆法使⽤，报错信息如下

-----------------------------------------------------------------------------------------------There was a problem importing one of the modules required to run yum. The error leading to this problem was: No module named yum

Python

------------------------------------------------------------------------------------------------

![image 2](<安装Python.note_images/imageFile2.png>)

解决办法 查看系统使⽤的python版本 ls /usr/bin |grep python

<table>
  <tr>
    <th>![image 3](<安装Python.note_images/imageFile3.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 4](<安装Python.note_images/imageFile4.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 5](<安装Python.note_images/imageFile5.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 6](<安装Python.note_images/imageFile6.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 7](<安装Python.note_images/imageFile7.png>)</th>
  </tr>
</table>


修改yum⽂件 #vi /usr/bin/yum 将 #!/usr/bin/python 修改为 #!/usr/bin/python2.6

查看是否有yum模块： ls /usr/lib/python2.6/site-packages/yum

