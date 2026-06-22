HMaster：管理HRegion server的上线下线，管理HRegion的分配，管理table的增删改查。DML HRegion server：包含多个HRegion，与⽤户交互，对数据进⾏增删改查。 DL HRegion：对应table中的Region，管理多个Hstore。 Hstore：对应table中的Column Family。

![image 1](<HBase原理--------------------1遍.note_images/imageFile1.png>)

HRegion server： 管理⼀些列的hregion对象。每⼀个HRegion server对应⼀个HLog HRegion：由多个Hstore组成，对应table中的Region。 Hstore：对应table中的Column Family。每个Column Family是⼀个⽂件，每⼀列是Column Family⽂件中的⼀条数据

![image 2](<HBase原理--------------------1遍.note_images/imageFile2.png>)

![image 3](<HBase原理--------------------1遍.note_images/imageFile3.png>)

![image 4](<HBase原理--------------------1遍.note_images/imageFile4.png>)

