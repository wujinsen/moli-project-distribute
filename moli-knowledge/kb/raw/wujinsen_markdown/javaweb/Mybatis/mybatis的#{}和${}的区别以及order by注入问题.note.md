前⾔略,直奔主题.. #{}相当于jdbc中的preparedstatement ${}是输出变量的值 你可能说不明所以,不要紧我们看2段代码:

String sql = "select * from admin_domain_location order by ?"; PreparedStatement st = con.prepareStatement(sql); st.setString(1, "domain_id"); System.out.println(st.toString());

ResultSet rs = st.executeQuery(); while(rs.next()){ System.out.println(rs.getString("domain_id")); }

输出结果:

com.mysql.jdbc.PreparedStatement@1fa1ba1: select * from admin_domain_location order by 'domain_id'

- 3

- 4

- 5 2

- 6 这是个jdbc的preparedstatement例⼦,不要吐槽我这么写是否合法,这⾥只是为了说明问题. 以上例⼦有得出以下信息: 1) order by后⾯如果采⽤预编译的形式动态输⼊参数,那么实际插⼊的参数是 ⼀个字符串,例⼦中是:order by 'domain_id' 2)输出结果并没有排序,从sql语句中的形式我们也可以推测出此sql语句根本也不合法(正常应该是 order by domain_id )


修改以上代码如下:

String input = "domain_id"; String sql = "select * from admin_domain_location order by "+input; PreparedStatement st = con.prepareStatement(sql); System.out.println(st.toString()); ResultSet rs = st.executeQuery(); while(rs.next()){

System.out.println(rs.getString("domain_id"));

} 输出结果:

com.mysql.jdbc.PreparedStatement@1fa1ba1: select * from admin_domain_location order by domain_id

- 2

- 3

- 4

- 5


- 6 此次我们直接把⼀个变量的值拼接sql语句,从结果可以看出来:


- 1)sql语句拼接正常

- 2)查询结果排序正常 你可能要问这和#{}与${}有什么关系.. 上⾯已经说过#{}相当于jdbc的preparedstatement,所以以上的第⼀个例⼦就相当于#{},那么第⼆个例⼦ 就⾃然⽽然指的是${}的情况. 你可能说思维还是有些凌乱,不要紧我们来看第三个例⼦: String sql = "select * from admin_domain_location where domain_id=?"; PreparedStatement st = con.prepareStatement(sql); st.setString(1, "2"); System.out.println(st.toString()); ResultSet rs = st.executeQuery(); while(rs.next()){


System.out.println(rs.getString("domain_id"));

} ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝ String input = "2"; String sql = "select * from admin_domain_location where domain_id='"+input+"'"; PreparedStatement st = con.prepareStatement(sql); System.out.println(st.toString()); ResultSet rs = st.executeQuery(); while(rs.next()){

System.out.println(rs.getString("domain_id"));

} 输出结果都为： com.mysql.jdbc.PreparedStatement@12bf560: select * from admin_domain_location where domain_id='2' 2

这第三个例⼦虽然说的是#{}和${}通⽤的问题,也就是说在此种情况下#{}和${}是通⽤的,只不过需要些⼩ 的转换.如例⼦中需要⼿动 拼接单引号 ' ' 到变量值的前后,确保sql语句正常. 简单说#{}是经过预编译的,是安全的,⽽${}是未经过预编译的,仅仅是取变量的值,是⾮安全的,存在sql注 ⼊. 这⾥先说⼀下只能${}的情况,从我们前⾯的例⼦中也能看出,order by是肯定只能⽤${}了,⽤#{}会多个' '导致sql语句失效.此外还有⼀个like 语句后也需要⽤${},简单想⼀下 就能明⽩.由于${}仅仅是简单的取值,所以以前sql注⼊的⽅法适⽤此处,如果我们order by语句后⽤了${}, 那么不做任何处理的时候是存在sql注⼊危险的.你说怎么防⽌,那我只 能悲惨的告诉你,你得⼿动处理过滤⼀下输⼊的内容,如判断⼀下输⼊的参数的⻓度是否正常(注⼊语句⼀ 般很⻓),更精确写查询⼀下输⼊的参数是否在预期的参数集合中..

