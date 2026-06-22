安装 E:\here\loadruner\[性能测试⼯具LR1.0].loadruner-

1\lruner\En\prerequisites\vc205_sp1_redist

我的电脑在安装UFT时，被要求需要卸载本机上安装的LoadRuner1，当LoadRuner1被卸载后，进 ⾏重新安装LoadRuner1时，会报缺少vc205_sp1_with_atl_fix_redist错误，类似下图所示：

![image 1](<计算机上缺少vc2005_sp1_whth_alt_fix_redist.note_images/imageFile1.png>)

![image 2](<计算机上缺少vc2005_sp1_whth_alt_fix_redist.note_images/imageFile2.png>)

由提示信息可知，这⾥是由于本机缺少该组件所致，解决⽅案就是安装此组件，可以去⽹上下载，当 然，我们完全没有必要这样做，在LoadRuner的安装包中，可以找到此组件，进⾏安装即可。 路径地址：找到安装程序⾃带的lruner\Chs\prerequisites\vc205_sp1_redist，双击运⾏ vcredist_x86.exe，再重新安装LoadRuner即可成功。

