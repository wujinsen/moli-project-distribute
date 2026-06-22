DataDaoImpl.java 4.03KB

TableDaoImpl.java 2.41KB

## 批量导⼊

### ⽣成⼀个要导⼊的⽂件：

public static void main(String[] args) throws IOException { long startTime = System.curentTimeMilis(); File dataFile = getFile(); FileWriter writer = nul; try {

writer = new FileWriter(dataFile); int timeCount = 1; int resourceCount = 1; for (int j = 0; j < timeCount; j +) {

for (int i = 0; i < resourceCount; i +) {

UID uid = UID.randomUID(); String rowKey = uid.toString() + "_" + timeStamp; Random random = new Random(); String cpuLoad = String.valueOf(random.nextDouble()

.substring(0, 4); String memory = String.valueOf(random.nextDouble()

.substring(0, 4); StringBuilder builder = new StringBuilder(); builder.apend(rowKey).apend("\t").apend(cpuLoad)

.apend("\t").apend(memory).apend("\t").apend(uid.toString().apend("\t").

apend(timeStamp); writer.apend(builder.toString(); if (i + 1) * (j + 1) < timeCount * resourceCount) {

writer.apend("\r"); }

} }

} catch (IOException e) {

e.printStackTrace(); } finaly {

writer.close(); }

}

private static File getFile() { File ﬁle = new File(PATH); if (!ﬁle.exists()) {

try { ﬁle.createNewFile(); } catch (IOException e) {

e.printStackTrace(); }

} return ﬁle;

}

## 批量导⼊

⽂件格式⼤致如下： 然后将⽂件上传到HDFS中， hadoop fs -put /home/admin/Desktop/data.txt /test 转换成HFile格式存储 hadoop jar hbase-version.jar importtsv Dimporttsv.columns=HBASE_ROW_KEY,c1,c2 -Dimporttsv.bulk.output=tmp hbase_table hdfs_ﬁle ⽣成HFile⽂件。其中c1,c2是列名，格式为:列族：列名然后，导⼊到HBase中： hadoop jar hbase-version.jar completebulkload /user/hadoop/tmp/cf hbase_table 这⾥的路径都是hdfs的路径。

# 开发⽅⾯—预先切分表

public void createTable(String tableName, String[] familys) throws IOException { HBaseAdmin admin = new HBaseAdmin(conﬁg);

if (admin.tableExists(tableName)) { System.out.println(tableName + " is already exists,Please create another table!"); } else {

HTableDescriptor desc = new HTableDescriptor(tableName);

for (int i = 0; i < familys.length; i++) { HColumnDescriptor family = new HColumnDescriptor(familys[i]); desc.addFamily(family);

}

int regionnum=3; admin.createTable(desc,”startkey”.getBytes(),”endkey”.getBytes(), regionnum); System.out.println("Create table \'" + tableName + "\' OK!");

}

} public void createTable(String tableName, String[] familys) throws IOException {

HBaseAdmin admin = new HBaseAdmin(conﬁg);

if (admin.tableExists(tableName)) { System.out.println(tableName + " is already exists,Please create another table!"); } else {

HTableDescriptor desc = new HTableDescriptor(tableName);

for (int i = 0; i < familys.length; i++) { HColumnDescriptor family = new HColumnDescriptor(familys[i]); desc.addFamily(family);

}

byte[][] regions = new byte[][] { Bytes.toBytes("A"), Bytes.toBytes("D")};

/表⽰有三个region分别放⼊key：

- /[1] start key: , end key: A
- /[2] start key: A, end key: D
- /[3] start key: D, end key: admin.createTable(desc,regions); System.out.println("Create table \'" + tableName + "\' OK!");


} }

# 开发⽅⾯

- 1、将表放到RegionServer的缓存中，保证在读取的时候被cache命中: HColumnDescriptor.setInMemory(true)
- 2、设置表中数据的最⼤版本，如果只需要保存最新版本的数据，那么可以设置 setMaxVersions(1)


- HColumnDescriptor.setMaxVersions(int maxVersions)
- 3、设置 tl HColumnDescriptor.setTimeToLive(int timeToLive) 对于相对不太重要的数据，可以在Put/Delete操作时，通过调⽤

Put.setWriteToWAL(false)或Delete.setWriteToWAL(false)函数， 放弃写WAL⽇志，从⽽提⾼数据写⼊的性能

- 4、批量写 HTable.put(List<Put>)
- 5、⾃动flush关闭 HTable.setAutoFlush(false)

可以将HTable写客户端的⾃动ﬂush关闭，这样可以批量写⼊数据到HBase，⽽不是有⼀条 put就执⾏⼀次更新， 只有当put填满客户端写缓存时，才实际向HBase服务端发起写请求。默认情况下auto ﬂush是开启的。 该⽅法与HTable.setWriteBuﬀerSize(writeBuﬀerSize)以及HTable. ﬂushCommits();⼀起 使⽤。

- 6、设置HTable客户端的写bufer⼤⼩ HTable.setWriteBuﬀerSize(writeBuﬀerSize)

-----设置HTable客户端的写buﬀer⼤⼩，如果新设置的buﬀer⼩于当前写buﬀer中的数据 时，buﬀer将会被ﬂush到服务端。 其中，writeBuﬀerSize的单位是byte字节数，可以根据实际写⼊数据量的多少来设置该 值。

- 7、设置HBase scaner⼀次从服务端抓取的数据条数 HTable.setScannerCaching(int scannerCaching)


-----设置HBase scanner⼀次从服务端抓取的数据条数，默认情况下⼀次⼀条。通过将 此值设置成⼀个合理的值， 可以减少scan过程中next()的时间开销，代价是scanner需要通过客户端的内存来维持这些 被cache的⾏记录。

HTable.get(List<Get>)-----批量读

