pip设置国内镜像源:

pip config set global.index-url

htps:/mirors.ustc.edu.cn/pypi/web/simple

pip config set global.index-url

htps:/pypi.tuna.tsinghua.edu.cn/simple

cp /rot/miniconda3/envs/sdw/lib/python3.10/lib-dynload/_bz2.cpython-310-x86_64-linux-gnu.so /usr/local/python3/lib/python3.10/lib-dynload/ cp /rot/miniconda3/envs/sdw/lib/li bz2.so.1.0 /usr/local/python3/lib/

遇到的坑： https://github.com无法下载问题 修改 modules/launch_utils.py 把⾥⾯的 https://github.com替换为https://root:123@github.com 就能正常下载

1 https://root:123@github.com/sczhou/CodeFormer.git

加上 -no-cache-dir /opt/stable-difusion-webui/venv/bin/python3 -m pip instal torch=2.0.1 torchvision=0.15.2extra-index-url -no-cache-dir

htps:/download.pytorch.org/whl/cu18

