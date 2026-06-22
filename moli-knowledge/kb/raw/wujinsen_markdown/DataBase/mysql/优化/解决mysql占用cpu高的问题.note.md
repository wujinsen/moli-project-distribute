- 1.使⽤top找出cpu占⽤率⾼的proces
- 2. pidstat -t -p <mysqld_pid> 1:

- 3. select * from performance_schema.threads where THREAD_OS_ID = 32053


![image 1](<解决mysql占用cpu高的问题.note_images/imageFile1.png>)

![image 2](<解决mysql占用cpu高的问题.note_images/imageFile2.png>)

