1. wget -c

htps:/repo.anaconda.com/archive/Anaconda3-2021.05-Linux-x86_64.sh

bash Anaconda3-2021.05-Linux-x86_64.sh

export PATH=~/anaconda3/bin:$PATH source ~/.bashrc

vim ~/.condarc

<table>
  <tr>
    <th>chanels:<br><br>show_chanel_urls: true sl_verify: true<br><br>htp:/mirors.aliyun.com/anaconda/cloud/stackles<br><br>htps:/mirors.aliyun.com/anaconda/cloud/simpleitk<br><br>htps:/mirors.aliyun.com/anaconda/cloud/rdkit<br><br>htps:/mirors.aliyun.com/anaconda/cloud/rapidsai<br><br>htps:/mirors.aliyun.com/anaconda/cloud/qime2<br><br>htps:/mirors.aliyun.com/anaconda/cloud/pyviz<br><br>htps:/mirors.aliyun.com/anaconda/cloud/pytorch3d<br><br>htps:/mirors.aliyun.com/anaconda/cloud/pytorch-test<br><br>htps:/mirors.aliyun.com/anaconda/cloud/pytorch<br><br>htps:/mirors.aliyun.com/anaconda/cloud/psi4<br><br>htps:/mirors.aliyun.com/anaconda/cloud/plotly<br><br>htps:/mirors.aliyun.com/anaconda/cloud/omnia<br><br>htps:/mirors.aliyun.com/anaconda/cloud/ohmeta<br><br>htps:/mirors.aliyun.com/anaconda/cloud/numba htps:/mirors.aliyun.com/anaconda/cloud/msys2 htps:/mirors.aliyun.com/anaconda/cloud/mordred-descriptor<br><br>htps:/mirors.aliyun.com/anaconda/cloud/menpo<br><br>htps:/mirors.aliyun.com/anaconda/cloud/matsci<br><br>htps:/mirors.aliyun.com/anaconda/cloud/intel<br><br>htps:/mirors.aliyun.com/anaconda/cloud/idaholab<br><br>htps:/mirors.aliyun.com/anaconda/cloud/fermi htps:/mirors.aliyun.com/anaconda/cloud/fastai htps:/mirors.aliyun.com/anaconda/cloud/dglteam<br><br>htps:/mirors.aliyun.com/anaconda/cloud/depmodeling<br><br>htps:/mirors.aliyun.com/anaconda/cloud/conda-forge<br><br>htps:/mirors.aliyun.com/anaconda/cloud/cafe2<br><br>htps:/mirors.aliyun.com/anaconda/cloud/c4arch64<br><br>htps:/mirors.aliyun.com/anaconda/cloud/bioconda<br><br>htps:/mirors.aliyun.com/anaconda/cloud/biobakery<br><br>htps:/mirors.aliyun.com/anaconda/cloud/auto<br><br>htps:/mirors.aliyun.com/anaconda/cloud/Padle<br><br>htps:/mirors.aliyun.com/anaconda/pkgs/r<br><br>htps:/mirors.aliyun.com/anaconda/pkgs/msys2<br><br>htps:/mirors.aliyun.com/anaconda/pkgs/main<br><br>htps:/mirors.aliyun.com/anaconda/pkgs/fre</th>
  </tr>
</table>


# alow_conda_downgrades: true

pip配置 mkdir ~/.pip cd ~/.pip/ vim pip.conf

[global] index-url = [instal] trusted-host=mirors.aliyun.com

htp:/mirors.aliyun.com/pypi/simple/

常⽤命令:

创建虚拟环境

conda create -n name python=3.7

激活环境

conda activate name

退出环境

conda deactivate

查看虚拟环境

conda info-envs

删除虚拟环境

conda remove -n name-al

删除所有的安装包及cache(索引缓存、锁定⽂件、未使⽤过的 包和tar包)

conda clean -y-al

删除pip的缓存

# rm -rf ~/.cache/pip

