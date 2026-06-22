? TestOracle.java D:\itcast\⾼级版（下）\day39 PLSQL 存储过程 触发器\笔记2\TestOracle.java 4 C:\Users\senfeng\ApData\Local\Temp\TestOracle.java

package demo.test;

import java.sql.CalableStatement; import java.sql.Conection; import java.sql.ResultSet;

import oracle.jdbc.OracleCalableStatement; import oracle.jdbc.OracleTypes;

import org.junit.Test;

import demo.utils.JDBCUtils;

/*

- * 性能上：Statement < PreparedStatement < CalableStatement
- */ public clas TestOracle {


/*

- * 调⽤存储过程
- * create or replace procedure queryEmpInfo(eno in number, pename out varchar2, psal out number, pjob out varchar2)
- */ @Test public void testProcedure(){


/ {cal <procedure-name>[(<arg1>,<arg2>, .)]} String sql = "{cal queryEmpInfo(?,?,?,?)}"; /创建调⽤存储过程的sql语句 Conection con =nul; CalableStatement cal = nul; /类似于preparestatement，⽤来处理存

储过程的，给存储过程设置in参数以及接受返回值。 try {

con = JDBCUtils.getConection();

cal = con.prepareCal(sql);

/赋值 cal.setInt(1, 7839);

/对于out参数需要申明

- cal.registerOutParameter(2, OracleTypes.VARCHAR);
- cal.registerOutParameter(3, OracleTypes.NUMBER);
- cal.registerOutParameter(4, OracleTypes.VARCHAR);


/执⾏ cal.execute();

/取出结果

String ename = cal.getString(2); double sal = cal.getDouble(3); String job = cal.getString(4);

System.out.println(ename); System.out.println(sal); System.out.println(job);

} catch (Exception e) {

e.printStackTrace(); }finaly{

JDBCUtils.release(con, cal, nul); }

}

/*

- * 调⽤存储函数
- * create or replace function queryEmpIncome(eno in number)


return number

*/ @Test public void testFunction(){

/{?= cal <procedure-name>[(<arg1>,<arg2>, .)]}

String sql = "{?=cal queryEmpIncome(?)}"; /调⽤存数函数与调⽤存储过程类似，就 是多了⼀个返回值。

Conection con =nul;

CalableStatement cal = nul; try {

con = JDBCUtils.getConection(); cal = con.prepareCal(sql);

/赋值 /把返回值当out参数

cal.registerOutParameter(1, OracleTypes.NUMBER); cal.setInt(2, 7839);

/执⾏ cal.execute();

/取出结果 double income = cal.getDouble(1); System.out.println(income);

}catch (Exception e) {

e.printStackTrace(); }finaly{

JDBCUtils.release(con, cal, nul); }

}

/*

- * 调⽤out参数中有光标的存储过程
- */ @Test public void testCursor(){


/ {cal <procedure-name>[(<arg1>,<arg2>, .)]} String sql = "{cal MYPACKAGE.queryEmpList(?,?)}"; Conection con =nul; CalableStatement cal = nul; ResultSet rs = nul; try {

con = JDBCUtils.getConection(); cal = con.prepareCal(sql);

/赋值 cal.setInt(1, 10); cal.registerOutParameter(2, OracleTypes.CURSOR);

/执⾏ cal.execute();

/取出结果 rs =(OracleCalableStatement)cal).getCursor(2); while(rs.next(){

String ename = rs.getString("ename"); String job = rs.getString("empjob"); double sal = rs.getDouble("sal"); System.out.println(ename+" " + job + " " + s

JDBCUtils.java D:\itcast\⾼级版（下）\day39 PLSQL 存储过程 触发器\笔记2\JDBCUtils.java 3 C:\Users\senfeng\ApData\Local\Temp\JDBCUtils.java ? package demo.utils;

import java.sql.Conection; import java.sql.DriverManager; import java.sql.ResultSet; import java.sql.SQLException; import java.sql.Statement;

public clas JDBCUtils { private static String driver = "oracle.jdbc.OracleDriver"; private static String url = "jdbc:oracle:thin:@localhost:1521:orcl"; private static String user = "scot"; private static String pasword = "tiger";

/注册驱动 static{

try {

Clas.forName(driver); } catch (ClasNotFoundException e) {

throw new ExceptionInInitializerEror(e); }

}

public static Conection getConection(){ try {

return DriverManager.getConection(url,user,pasword);

} catch (SQLException e) { e.printStackTrace();

} return nul;

}

/*

- * jvm的参数：

*

- * java -Xms10M -Xmx20M HeloWorld
- */ public static void release(Conection con, Statement st,ResultSet rs){


if(rs != nul){ try {

rs.close(); } catch (SQLException e) {

e.printStackTrace(); }finaly{

rs = nul;/ ? }

} if(st != nul){

try {

st.close(); } catch (SQLException e) {

e.printStackTrace(); }finaly{

st = nul;/ ? }

} if(con != nul){

try {

con.cl

