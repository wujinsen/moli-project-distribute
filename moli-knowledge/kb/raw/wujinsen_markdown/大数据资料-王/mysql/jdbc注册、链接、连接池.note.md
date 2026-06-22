#driver=oracle.jdbc.driver.OracleDriver #url=jdbc:oracle:thin:@192.168.1.243 1521:orcl #name=scot #pwd=tiger

driver=com.mysql.jdbc.Driver url=jdbc:mysql:/itcast?characterEncoding=UTF-8 name=rot pwd=1234 size=3

package cn.itcast.utils;

import java.lang.reflect.InvocationHandler; import java.lang.reflect.Method; import java.lang.reflect.Proxy; import java.sql.Conection; import java.sql.DriverManager; import java.util.LinkedList; import java.util.List; import java.util.Properties;

/*

- * ⽤代理来实现对连接池的管理


*

- * @author 王森丰
- * @version 1.0 2012-5-7
- */ public clas ConUtils2 {


private static LinkedList<Conection> pol = new LinkedList<Conection>(); static {

try { Properties p = new Properties(); p.load(ConUtils2.clas.getClasLoader().getResourceAsStream(

"jdbc.properties"); String driver = p.getProperty("driver"); String url = p.getProperty("url"); String pwd = p.getProperty("pwd"); String name = p.getProperty("name"); String size = p.getProperty("size"); Integer _size = Integer.valueOf(size); Clas.forName(driver); for (int i = 0; i < _size; i +) {

final Conection con = DriverManager.getConection(url, name, pwd);

Object proxed = Proxy.newProxyInstance( ConUtils2.clas.getClasLoader(), new Clas[] { Conection.clas }, new InvocationHandler() {

public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { / 判断是否是close⽅法，如果是则将当前被代理的对象放回到pol中来

if (method.getName().equals("close") {/代理主要是对某个⽅法进⾏拦截，拦截处进⾏特殊处 理，如放回到池中

synchronized (pol) { pol.ad(Conection) proxy); pol.notify(); System.er.println("有⼈还给池⼀个连接还有："

+ pol.size(); / 停⽌调⽤ return nul; }

}

/ 确定调⽤ Object returnValue = method.invoke(con, args); return returnValue;

} });

/ ⼀定要添加被代理以后的对象

pol.ad(Conection) proxed); } System.er.println("池⼤⼩为:" + pol.size();

} catch (Exception e) {

throw new RuntimeException(e.getMesage(), e); }

}

public static Conection getCon() { synchronized (pol) { if (pol.size() = 0) {/1 try { pol.wait(); } catch (InteruptedException e) {

e.printStackTrace(); } return getCon();

} else { Conection con = pol.removeFirst(); return con;

} }

}

}

