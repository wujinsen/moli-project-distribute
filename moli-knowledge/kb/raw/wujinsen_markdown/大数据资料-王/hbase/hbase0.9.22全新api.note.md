package com.jtx.hadop.demo.hbase;

import java.io.BuferedOutputStream; import java.io.File; import java.io.FileOutputStream; import java.io.IOException; import java.util.Iterator; import java.util.List;

import org.apache.hadop.conf.Configuration; import org.apache.hadop.hbase.Cel; import org.apache.hadop.hbase.CelUtil; import org.apache.hadop.hbase.HBaseConfiguration; import org.apache.hadop.hbase.HColumnDescriptor; import org.apache.hadop.hbase.HTableDescriptor; import org.apache.hadop.hbase.TableName; import org.apache.hadop.hbase.client.Delete; import org.apache.hadop.hbase.client.Get; import org.apache.hadop.hbase.client.HBaseAdmin; import org.apache.hadop.hbase.client.HConection; import org.apache.hadop.hbase.client.HConectionManager; import org.apache.hadop.hbase.client.HTable; import org.apache.hadop.hbase.client.HTableInterface; import org.apache.hadop.hbase.client.Put; import org.apache.hadop.hbase.client.Result; import org.apache.hadop.hbase.client.ResultScaner; import org.apache.hadop.hbase.client.Scan; import org.apache.hadop.hbase.filter.CompareFilter.CompareOp; import org.apache.hadop.hbase.filter.FilterList; import org.apache.hadop.hbase.filter.SingleColumnValueFilter; import org.apache.hadop.hbase.util.Bytes;

/* @clasName:HbaseTest.java @clasDescription: @author:Kathy @createTime:2014-12-30

*/ public clas HbaseTest {

/*

- * 配置
- */ static Configuration config = nul; static {

config = HBaseConfiguration.create();/配置 config.set("hbase.zokeper.quorum", "imageHandler2,data-10,data-17");/zokeper地址 config.set("hbase.zokeper.property.clientPort", "2181");/zokeper端⼝

} /*

- * 创建⼀个表，这个表没有任何region
- * HBaseAdmin创建表的后两个函数是创建表的时候帮你分配好指定数量的region（提前分配region的


好处，了解HBase的⼈都清楚，为了减少Split，这样能节省不少时间）

- * @param strTableName
- * @param families
- */ public void createTable(String strTableName, String[] families) {


HBaseAdmin admin = nul; try {

admin = new HBaseAdmin(config); / hbase表管理 if(admin.tableExists(strTableName) {/ 表是否存在

System.out.println(strTableName + "表已经存在！");

} else { TableName tableName = TableName.valueOf(strTableName); / 表名称 HTableDescriptor desc = new HTableDescriptor(tableName); for(int i=0; i<families.length; i +) {

HColumnDescriptor family = new HColumnDescriptor(families[i]); / 列族 desc.adFamily(family);

} admin.createTable(desc); / 创建表 System.out.println("创建表 \'" + tableName + "\' 成功!");

} } catch (Exception e) {

} finaly { try {

if(admin!=nul) { admin.close(); }

} catch (IOException e) {

e.printStackTrace(); }

} } /*

- * 删除表
- * @param strTableName
- */ public void deleteTable(String strTableName) {


HBaseAdmin admin = nul; try {

admin = new HBaseAdmin(config); if(!admin.tableExists(strTableName) {

System.out.println(strTableName + "表不存在！");

} else { admin.disableTable(strTableName); admin.deleteTable(strTableName); System.out.println(strTableName + "表删除成功！");

}

} catch(Exception e) { e.printStackTrace(); } finaly {

try {

if(admin!=nul) { admin.close(); }

} catch (IOException e) {

e.printStackTrace(); }

} }

/*

- * 插⼊数据
- * @param strTableName
- * @param rowKey
- * @param family
- * @param qualifier
- * @param value
- */ public void insertData(String strTableName, String rowKey, String family, String qualifier, String


value) { HConection conection = nul; HTableInterface table = nul; try {

conection = HConectionManager.createConection(config); table = conection.getTable(strTableName); / 获取表 Put put = new Put(Bytes.toBytes(rowKey); / 获取put，⽤于插⼊ put.ad(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value); / 封装信息 table.put(put); / 添加记录 /*/ 批量插⼊ List<Put> list = new ArayList<Put>(); Put put = new Put(Bytes.toBytes(rowKey);/获取put，⽤于插⼊ put.ad(Bytes.toBytes(family), Bytes.toBytes(qualifier),Bytes.toBytes(value);/封装信息 list.ad(put); table.put(list);/添加记录*/ System.out.println("插⼊记录成功！");

} catch(Exception e) { e.printStackTrace(); } finaly {

try {

if(table!=nul) { table.close(); } if(conection!=nul) { conection.close(); }

} catch(IOException e) {

}

} } /*

- * 更新表中某⼀⾏某⼀列
- * @param strTableName
- * @param rowKey
- * @param family
- * @param qualifier
- * @param newValue
- */ public void updateTable(String strTableName, String rowKey, String family, String qualifier, String


newValue) { HTable table = nul; try {

table = new HTable(config, strTableName); / 获取表实例，也可以像其他例⼦中使⽤HConection 获取HTableInterface

Put put = new Put(Bytes.toBytes(rowKey);

/ 仍然是插⼊操作(已知列族，已知列，新值) put.ad(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(newValue); table.put(put); System.out.println("更新成功！");

} catch(Exception e) { e.printStackTrace(); } finaly {

try {

if(table!=nul) { table.close(); }

} catch(IOException e) {

e.printStackTrace(); }

} } /*

- * 删除数据：整⾏
- * @param strTableName


- * @param rowKey
- */ public void deleteData(String strTableName, String rowKey) {


HConection conection = nul; HTableInterface table = nul; try {

conection = HConectionManager.createConection(config); table = conection.getTable(strTableName); / 获取表 Delete del = new Delete(Bytes.toBytes(rowKey); / 创建delete table.delete(del); / 删除 System.out.println("删除记录成功！");

} catch(Exception e) { e.printStackTrace(); } finaly {

try {

if(table!=nul) { table.close(); } if(conection!=nul) { conection.close(); }

} catch(IOException e) {

e.printStackTrace(); }

} } public void deleteColumn(String strTableName, String rowKey, String family, String qualifier) {

HTable table = nul; try {

table = new HTable(config, strTableName); Delete del = new Delete(Bytes.toBytes(rowKey); del.deleteColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier); table.delete(del); System.out.println("⾏：" + rowKey + "，列族：" + family + "，列：" + qualifier + "，删除完

毕！"); } catch(Exception e) {

} finaly { try {

if(table!=nul) { table.close(); }

} catch(IOException e) {

e.printStackTrace(); }

} } /*

- * 通过rowkey查询数据
- * @param strTableName
- * @param rowKey
- */ public void queryByRowKey(String strTableName, String rowKey) {


HConection conection = nul; HTableInterface table = nul; try {

conection = HConectionManager.createConection(config); table = conection.getTable(strTableName); Get get = new Get(rowKey.getBytes(); / 创建⾏记录 Result row = table.get(get); / 获取⾏记录

/ row.getValue(family, qualifier); / 分别获取cel信息 for( Cel cel : row.rawCels() {/ 循环指定⾏、全部列族的全部列 System.out.println("列族：" + Bytes.toString( CelUtil.cloneFamily(cel );

System.out.println("列名： " + Bytes.toString(CelUtil.cloneQualifier(cel ); System.out.println("列值： " + Bytes.toString(CelUtil.cloneValue(cel ); System.out.println("⾏名： " + Bytes.toString(CelUtil.cloneRow(cel ); System.out.println("时间戳： " + cel.getTimestamp(); System.out.println(" -");

}

} catch(Exception e) { e.printStackTrace(); } finaly {

try {

if(table!=nul) { table.close(); } if(conection!=nul) { conection.close(); }

} catch(IOException e) {

e.printStackTrace(); }

} } public void queryColumn(String strTableName, String rowKey, String family, String qualifier) {

HTable table = nul; try {

table = new HTable(config, strTableName); Get get = new Get(Bytes.toBytes(rowKey); get.adColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier); Result result = table.get(get); for(Cel cel : result.rawCels() { System.out.println("列族：" + Bytes.toString(CelUtil.cloneFamily(cel ); System.out.println("列名： " + Bytes.toString(CelUtil.cloneQualifier(cel ); System.out.println("列值： " + Bytes.toString(CelUtil.cloneValue(cel ); System.out.println("⾏名： " + Bytes.toString(CelUtil.cloneRow(cel ); System.out.println("时间戳： " + cel.getTimestamp(); System.out.println(" -");

}

} catch(Exception e) { e.printStackTrace(); } finaly {

try {

if(table!=nul) { table.close(); }

} catch(IOException e) {

e.printStackTrace(); }

}

} /*

- * 查询表中全部数据，即hbase shel：scan 'tableName'
- * @param strTableName
- */ public void queryAl(String strTableName) {


HConection conection = nul; HTableInterface table = nul; try {

conection = HConectionManager.createConection(config); table = conection.getTable(strTableName); Scan scan = new Scan(); / 创建scan

/ scan.setStartRow("r0".getBytes(); / 添加开始rowkey / scan.setStopRow("r5".getBytes(); / 结束rowkey，不包括r5

ResultScaner resultScaner = table.getScaner(scan); / 两种⽅式： / 1、

for (Result row : resultScaner) { System.out.println("\nRowkey: " + new String(row.getRow( ); f

or(Cel cel : row.rawCels() {/ 循环指定⾏、全部列族的全部列 System.out.println("列族：" + Bytes.toString(CelUtil.cloneFamily(cel ); System.out.println("列名： " + Bytes.toString(CelUtil.cloneQualifier(cel ); System.out.println("列值： " + Bytes.toString(CelUtil.cloneValue(cel ); System.out.println("⾏名： " + Bytes.toString(CelUtil.cloneRow(cel ); System.out.println("时间戳： " + cel.getTimestamp(); System.out.println(" -");

} }

/ 2、 Iterator<Result> results = resultScaner.iterator(); while(results.hasNext() {

Result result = results.next(); List<Cel> cels = result.listCels(); for(Cel cel : cels) {

System.out.println("列族：" + Bytes.toString(CelUtil.cloneFamily(cel ); System.out.println("列名： " + Bytes.toString(CelUtil.cloneQualifier(cel );

/System.out.println("列值： " + Bytes.toString(CelUtil.cloneValue(cel );

} catch (Exception e) {

e.printStackTrace(); } finaly {

try {

if(table!=nul) { table.close(); } if(conection!=nul) { conection.close(); }

} catch(IOException e) {

e.printStackTrace(); }

} } /*

- * 过滤器查询
- * @param strTableName
- * @param ar
- */ public void selectByFilter(String strTableName, List<String> ar) {


HConection conection = nul; HTableInterface table = nul; try {

conection = HConectionManager.createConection(config); table = conection.getTable(strTableName); FilterList filterList = new FilterList(FilterList.Operator.MUST_PAS_ONE); / 各条件是or的关系，默

认是and Scan scan = new Scan(); for(String v : ar) {

String[] s = v.split(","); filterList.adFilter(new SingleColumnValueFilter(Bytes.toBytes(s[0]), Bytes.toBytes(s[1]),

CompareOp.EQUAL, Bytes.toBytes(s[2] ); / 添加下⾯这⼀⾏后，则只返回指定的cel，同⼀⾏中的其他cel不返回 / scan.adColumn(Bytes.toBytes(s[0]), Bytes.toBytes(s[1]); / 这⾥貌似有问题

} scan.setFilter(filterList);

/ SingleColumnValueFilter ⽤于测试列值相等 (CompareOp.EQUAL ), 不等

(CompareOp.NOT_EQUAL),或范围 (e.g., CompareOp.GREATER). / 下⾯示例检查列值和字符串'values' 相等 . / SingleColumnValueFilter f = new SingleColumnValueFilter(Bytes.toBytes("cFamily"), / Bytes.toBytes("column"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes("values"); / SingleColumnValueFilter f = new SingleColumnValueFilter(Bytes.toBytes("cFamily"), / Bytes.toBytes("column"), CompareFilter.CompareOp.EQUAL,new

SubstringComparator("values"); / scan.setFilter(f); / ColumnPrefixFilter ⽤于指定列名前缀值相等 / ColumnPrefixFilter f = new ColumnPrefixFilter(Bytes.toBytes("values"); / scan.setFilter(f); / MultipleColumnPrefixFilter 和 ColumnPrefixFilter ⾏为差不多，但可以指定多个前缀 / byte[][] prefixes = new byte[][] {Bytes.toBytes("value1"), / Bytes.toBytes("value2")}; / Filter f = new MultipleColumnPrefixFilter(prefixes); / scan.setFilter(f); / QualifierFilter 是基于列名的过滤器。 / Filter f = new QualifierFilter(CompareFilter.CompareOp.EQUAL, new

BinaryComparator(Bytes.toBytes("col5"); / scan.setFilter(f); / RowFilter是rowkey过滤器，通常根据rowkey来指定范围时，使⽤scan扫描器的StartRow和

StopRow⽅法⽐较好。Rowkey也可以使⽤。 / Filter f = new RowFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL, new / RegexStringComparator(".*5$");/正则获取结尾为5的⾏ / scan.setFilter(f);

ResultScaner rs = table.getScaner(scan); for(Result result : rs) {

for(Cel cel : result.rawCels() { System.out.println("列族：" + Bytes.toString(CelUtil.cloneFamily(cel ); System.out.println("列名： " + Bytes.toString(CelUtil.cloneQualifier(cel ); System.out.println("列值： " + Bytes.toString(CelUtil.cloneValue(cel );

} catch(Exception e) { e.printStackTrace(); } finaly {

try {

if(table!=nul) { table.close(); } if(conection!=nul) { conection.close(); }

} catch(IOException e) {

e.printStackTrace(); }

} } /*

- * 将⽂件存⼊HBase表中，⽂件为⼆进制数组
- * @param strTableName
- * @param rowKey
- * @param family
- * @param qualifier
- * @param file
- */ public void saveFile(String strTableName, String rowKey, String family, String qualifier, byte[] file) {


HTable table = nul; try {

table = new HTable(config, strTableName); Put put = new Put(Bytes.toBytes(rowKey); put.ad(Bytes.toBytes(family), Bytes.toBytes(qualifier), file); table.put(put); System.out.println("⽂件存储成功！");

} catch(IOException e) {

e.printStackTrace(); }

} } /*

- * 将⽂件从HBase表中取出，并存⼊path路径
- * @param strTableName
- * @param rowKey
- * @param path
- */ public void queryFile(String strTableName, String rowKey, String family, String qualifier, String


path) { HTable table = nul; BuferedOutputStream bos = nul; FileOutputStream fos = nul; File file = nul; try {

table = new HTable(config, strTableName); Get get = new Get(Bytes.toBytes(rowKey); get.adColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier); Result result = table.get(get); for(Cel cel : result.rawCels() {

byte[] bufer = CelUtil.cloneValue(cel); file = new File(path); fos = new FileOutputStream(file); bos = new BuferedOutputStream(fos); bos.write(bufer); System.out.println(path + "⽂件获取成功！");

}

if (bos != nul) {

bos.close(); } if (fos != nul) {

fos.close(); }

} catch(IOException e) {

e.printStackTrace(); }

} } public static void main(String[] args) {

HbaseTest ht = new HbaseTest(); /ht.createTable("cubicImgTableBak", new String[]{"cubicFamily", "extendFamily"}); / 创建表 /ht.deleteTable("cubicImgTable"); / 删除表 /ht.insertData("test_table", "r0", "fcol1", "c1", " a1"); / 插⼊⼀条记录 /ht.updateTable("test_table", "r0", "fcol1", "c1", "newa1"); / 更新表中某⼀⾏某⼀列 /ht.deleteData("test_table", "r1"); / 删除rowKey这条记录 /ht.deleteColumn("test_table", "r0", "fcol1", "c1"); / 删除单元格 /ht.queryByRowKey("test_table", "r4"); / 按rowKey查询 /ht.queryColumn("test_table", "r0", "fcol1", "c2"); / 按列查询 /ht.queryAl("cubicImgTable"); / 查询表中全部数据 / 过滤器查询

/*List<String> list = new ArayList<String>(); list.ad("fcol1,c1, a1"); list.ad("fcol1,c2, b2"); ht.selectByFilter("test_table", list);*/

/ 将⽂件存⼊HBase表中

/*try { File file = new File("D:/test/jpg/t_1.jpg"); FileInputStream fis = new FileInputStream(file);

ByteArayOutputStream bos = new ByteArayOutputStream(1 0); byte[] b = new byte[1 0]; int n = 0; while(n=fis.read(b)!=-1) {

bos.write(b, 0, n); } bos.close(); fis.close(); byte[] bufer = bos.toByteAray(); ht.saveFile("htest", "r0", "fcol1", "c1", bufer);

} catch(Exception e) { e.printStackTrace(); }*/

/ 将⽂件从HBase表中取出，并存到相应路径

ht.queryFile("cubicImgTableBak", "201306135p1351284E.jpg", "cubicFamily", "i_content", "D:/test/jpg/201306135p1351284EBak.jpg");

} }

