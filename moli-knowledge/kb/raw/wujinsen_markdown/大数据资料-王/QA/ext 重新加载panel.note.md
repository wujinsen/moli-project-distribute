调⽤它的重新配置⽅法：两个参数，1、store数据 2、columns列头

this.reconfigure(this.store,this.columns);

json转换

# 出现java.lang.NoClasDefFoundEror: net/sf/ezmorph/Morpher

JAVA

.netJavajsonJavaScriptApache

JSON 即 JavaScript Object Natation，它是⼀种轻量级的数据交换格式，⾮常适合于服务器与 JavaScript 的交互。本⽂将快速讲解 JSON 格式，并通过代码示例演示如何分别在客户端和服务器端 进⾏ JSON 格式数据的处理。 Json必需的包 comons-htpclient-3.1.jar comons-lang-2.4.jar comons-loging-1.1.1.jar json-lib-2.2.3-jdk13.jar ezmorph-1.0.6.jar comons-colections-3.2.1.jar 出现java.lang.NoClasDefFoundEror: net/sf/ezmorph/Morpher错误是因为没有导⼊ezmorph.jar⽂件 或版本不对。 出现java.lang.NoClasDefFoundEror: org/apache/comons/colections/map/ListOrderedMap错误 是因为没有导⼊comons-colections.jar⽂件或版本不对。 Java代码转换成json代码

- 1. List集合转换成json代码 List list = new ArayList(); list.ad( "first" ); list.ad( "second" ); JSONAray jsonAray2 = JSONAray.fromObject( list );
- 2. Map集合转换成json代码 Map map = new HashMap(); map.put("name", "json"); map.put("bol", Bolean.TRUE); map.put("int", new Integer(1); map.put("ar", new String[] { "a", "b" });


map.put("func", "function(i){ return this.ar; }");JSONObject json = JSONObject.fromObject(map);3. Bean转 换 成 json代 码 JSONObject jsonObject = JSONObject.fromObject(new JsonBean();4. 数 组 转 换 成 json代 码 bolean[] bolAray = new bolean[] { true, false, true };JSONAray jsonAray1 = JSONAray.fromObject(bolAray);5. ⼀ 般 数 据 转 换 成 json代 码 JSONAray jsonAray3 = JSONAray.fromObject("['json','is','easy']" );6. beans转 换 成 json代 码 List list = new ArayList();JsonBean2 jb1 = new

- JsonBean2();jb1.setCol(1);jb1.setRow(1);jb1.setValue("x");JsonBean2 jb2 = new
- JsonBean2();jb2.setCol(2);jb2.setRow(2);jb2.setValue(");list.ad(jb1);list.ad(jb2);JSONAray ja = JSONAray.fromObject(list);


