htp:/ w.liuxiao.org/2017/02/git-处理-github-不允许上传超过-10mb-⽂件的问题/

Git Large File Storage

最近在使⽤ Github 时候遇到⼀个问题，有⼀些过⼤的⽂件不允许上传，例如：

remote: error: File Examples/iOSDemo/Pods/dependency/libg2o.a is 440.12 MB; this exceeds GitHub's file size limit of 100.00 MB

但有的时候我们还是需要上传这些⼤⽂件，这时候怎样做呢？

- 1、移除错误缓存 ⾸先应该移除所有错误的 cache，对于⽂件：

对于⽂件夹：

例如对于我的例⼦就是这样的：

- 2、重新提交： 编辑最后提交信息：

修改 log 信息后保存返回。 重新提交：

PS：如果上⾯的步骤仍然⽆法解决问题，则可以运⾏如下命令删除有关某个⽂件的push操作：

- 3、将⼤⽂件加⼊ Git Large File Storage：


<table>
  <tr>
    <th>1</th>
    <th>git rm --cached path_of_a_giant_file</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>git rm --cached -r path_of_a_giant_dir</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>git rm --cached -r Examples/iOSDemo/Pods/dependency/libg2o.a</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>git commit --amend</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>git push</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>git filter-branch -f --index-filter 'git rm -cached --ignore-unmatch YOUR-FILE'</th>
  </tr>
</table>


- 1）⾸先安装 git-lfs Mac 安装：


<table>
  <tr>
    <th>1</th>
    <th>brew install git-lfs</th>
  </tr>
</table>


Ubuntu 安装： 下载 https://github.com/git-lfs/git-lfs/releases 合适的版本例如 Linux AMD64，解压后进⼊⽬录直接运 ⾏安装脚本：

<table>
  <tr>
    <th>1</th>
    <th>sudo ./install.sh</th>
  </tr>
</table>


- 2）将想要保存的⼤⽂件 “路径” 或者 “类型” 添加进 track：


<table>
  <tr>
    <th>1</th>
    <th>git lfs track "name_of_a_giant_file"</th>
  </tr>
</table>


例如对于我的例⼦就是这样的：

<table>
  <tr>
    <th>1</th>
    <th>git lfs track "libg2o.a"</th>
  </tr>
</table>


需要注意的是这⾥⾯仅能添加类型的扩展名或者⽂件名作为跟踪⽅式，不可以添加路径或者⽬录进 ⾏跟踪。

- 4、将想要保存的⼤⽂件正常添加进 git：

或者：

例如对于我的例⼦就是这样的：

- 5、正常进⾏提交&推送：


<table>
  <tr>
    <th>1</th>
    <th>git add path_of_a_giant_file</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>git add extension_name_of_giant_files</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>git add Examples/iOSDemo/Pods/dependency/libg2o.a</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br>2<br></th>
    <th>git commit -m "Add design file" git push origin master</th>
  </tr>
</table>


补充技巧： 提交以后出错再进⾏上⾯的步骤可能⽐较麻烦，如果你已知⾃⼰提交的版本库中确实存在⼀些⼤于 100MB 的⽂件，不妨先搜索：

<table>
  <tr>
    <th>1</th>
    <th>find ./ -size +100M</th>
  </tr>
</table>


然后将这些⽂件移除，等待其他⽂件提交完后再复制回来，这样只需要从步骤3的操作开始就可以了。 常⻅问题： 1、错误：fatal error: unexpected signal during runtime execution

goroutine 23 [chan receive]: github.com/github/git-lfs/lfs.ScanRefsToChan.func2(0xc8200d4540, 0xc8200c6000, 0xc8200d45a0) /Users/rick/go/src/github.com/github/git-lfs/lfs/scanner.go:153 +0x4e created by github.com/github/git-lfs/lfs.ScanRefsToChan /Users/rick/go/src/github.com/github/git-lfs/lfs/scanner.go:160 +0x30c

出现这个问题通常是由于 go 引擎未安装或者版本太⽼（1.5.1及以下版本在 Mac 上⾯有未知错误）， 或者 git-lfs 版本太⽼。如果没有安装 go，可使⽤如下命令安装：

<table>
  <tr>
    <th>1</th>
    <th>brew install git-lfs</th>
  </tr>
</table>


然后使⽤如下命令升级：

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>brew update brew upgrad go brew upgrad git-lfs</th>
  </tr>
</table>


e

然后使⽤如下命令查看：

<table>
  <tr>
    <th>1</th>
    <th>git-lfs version</th>
  </tr>
</table>


我这⾥的版本号如下，如果你⽐我的版本⾼就对了，否则可以尝试卸载之前安装的 go 和 git-lfs 重新安 装：

git-lfs/1.5.5 (GitHub; darwin amd64; go 1.7.4)

参考⽂献：

- [1]
- [2]


https://help.github.com/enterprise/11.10.340/user/articles/working-with-large-files/ https://git-lfs.github.com/

