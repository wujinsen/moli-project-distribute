- 1.清理前内存使⽤情况 fre -m
- 2.开始清理 echo 1 > /proc/sys/vm/drop_caches
- 3.清理后内存使⽤情况 fre -m
- 4.完成!查看内存条数命令： dmidecode | grep -A16 "Memory Device$"


![image 1](<linux 内存清理 释放命令 清理linux内存.note_images/imageFile1.png>)

![image 2](<linux 内存清理 释放命令 清理linux内存.note_images/imageFile2.png>)

![image 3](<linux 内存清理 释放命令 清理linux内存.note_images/imageFile3.png>)

