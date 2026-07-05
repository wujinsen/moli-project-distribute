---
title: Hadoop2.6.0eclipse插件制作与安装.note（原文插图 annex）
slug: annex-Hadoop2.6.0eclipse插件制作与安装
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/Hadoop2.6.0eclipse插件制作与安装.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

⼀、制作

- 1、⾸先从git下载源码
- 2、编译准备 win7编译⾸先需要安装jdk、ant、Eclipse Eclipse的版本： Version: 3.9.1.201308190730Build id: 3.9.0.201308190730也就是版本Version: 3.9.1（这⾥貌似没有什么⽤）

- 3、编译 E:\hadop2x-eclipse-plugin\src\contrib\eclipse-plugin>antjar -Dversion=2.6.0 -Declipse.home=E:\eclipseDhadop.home=D:\hbl_study\hadop2\hadop-2.6.0 参数说明： 1.Dversion是你编译的版本，此版本为2.6.02.Declipse.home是Eclipse安装⽬录3.Dhadop.home是Hadop安装⽬ 录 编译后插件位置build/contrib/eclipse-plugin⽬录下产⽣⼀个hadop-eclipse-plugin-2.6.0.jar⽂件 ⼆、安装


# htps:/github.com/winghc/hadop2x-eclipse-plugin

![image 1](assets/imageFile1.png)

1.把插件放⼊plugins⽂件夹然后打开win7下Eclipse，将hadop-eclipse-plugin-2.6.0.jar插件放到Eclipse安装⽬录 的plugins⽂件夹下，我这⾥的路径是D:\hbl_download\eclipse\plugins其实放到这⾥⾯已经完成⼤部分了。2.重启 Eclipse通过window-》preference找到下图所⽰，选择Hadop安装⽬录3.创建New hadop location通过下⾯操 作，单击other弹出show view对话框，然后单击map/reduce location看到⼩象，如下图右键New hadop location4.配置New hadop location 其中5020是Map/Reduce(V2) Master默认端⼜，9 0为配置⽂件中DFS Master配置的端⼜，查看hdfs-site.xml中 类似下⾯的配置：

5.查看效果配置完毕，查看效果
