conda config-show-sources #查看当前使⽤源 conda config-remove-key chanels #换回默认源 添加中科⼤源 conda config-ad chanels conda config-ad chanels conda config-ad chanels conda config-ad chanels conda config-ad chanels conda config-ad chanels conda config-set show_chanel_urls yes

htps:/mirors.ustc.edu.cn/anaconda/pkgs/main/ htps:/mirors.ustc.edu.cn/anaconda/pkgs/fre/ htps:/mirors.ustc.edu.cn/anaconda/cloud/conda-forge/ htps:/mirors.ustc.edu.cn/anaconda/cloud/msys2/ htps:/mirors.ustc.edu.cn/anaconda/cloud/bioconda/ htps:/mirors.ustc.edu.cn/anaconda/cloud/menpo/

pip设置源

pip config set global.index-url

htps:/pypi.tuna.tsinghua.edu.cn/simple

python -m pip config set global.index-url

htps:/mirors.aliyun.com/pypi/simple

pip install -r requirements.txt -i

htps:/mirors.aliyun.com/pypi/simple

/opt/stable-difusion-webui/venv/bin/python3 -m pip instal -r requirements_versions.txt-i

htps:/ mirors.aliyun.com/pypi/simple

-trusted-host pypi.douban.com

python3 -m pip instal -upgrade pip libsl-dev

