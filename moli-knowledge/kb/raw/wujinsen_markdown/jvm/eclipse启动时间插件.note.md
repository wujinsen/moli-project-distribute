Eclipse插件开发

1. 下载并安装jdk和eclipse 这⾥强调⼀下: 需要下载Eclipse for RCP and RAP Developers, 否则⽆法新 建Plug-in Development 项⽬.2. 新建项⽬ 安装好之后打开eclipse, 点击 File->NewProject。选择 Plug-in Project，点击Next。新建⼀个名为com.developer.showtime的项⽬,所有参数采⽤默认值.

- 3. 在com.developer.showtime项⽬的src下新建⼀个类:ShowTime,代码如下:

package com.developer.showtime;import org.eclipse.jface.dialogs.MesageDialog;import org.eclip se.swt.widgets.Display;import org.eclipse.swt.widgets.Shel;import org.eclipse.ui.IStartup;publiccla

s ShowTime implements IStartup { publicvoid earlyStartup() { Display.getDefault().syncExec (new Runable() { publicvoid run() { long eclipseStartTime = Long.parseLong(System.getProperty("eclipse.startTime");

long costTime = System.curentTimeMilis() - eclipseStartTime; Shel shel = Display.ge tDefault().getActiveShel(); String mesage = "Eclipse start in " + costTime + "ms";

MesageDialog.openInformation(shel, "Information", mesage); } }); }

- 4. 修改plugin.xml⽂件如下:

<?xml version="1.0" encoding="UTF-8"?> <?eclipse version="3.4"?> <plugin><extension point="org.eclipse.ui.startup"> <startup clas="com.developer.sho wtime.ShowTime"/></extension> </plugin>

- 5. 试运⾏ 右键点击Run as -> Eclipse Aplication. 此时会运⾏⼀个eclipse, 启动之后就能显⽰启动所需时间.
- 6. 导出插件. 右键Export -> Deployable plug-ins and fragments. 在Directory中输⼊需要导出的路径, 点击finish后 会在该⽬录下产⽣⼀个plugins的⽬录, ⾥⾯就是插件包: com.developer.showTime_1.0.0.2010161216.jar. 把这个包复制到eclipse⽬录下的plugin⽬录下. 然后 再启动eclipse 便可以看到eclipse启动所花的时间.


