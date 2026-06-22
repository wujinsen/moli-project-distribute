总结⼀下⾃⼰使⽤jackson处理对象与JSON之间相互转换的⼼得。

jackson是⼀个⽤Java编写的，⽤来处理JSON格式数据的类库，它速度⾮常快，⽬前来看使⽤很⼴泛，逐渐替代了Gson 和json-lib。

如果直接引⼊jar包，可以访问这个地址下载http://jackson.codehaus.org/1.9.11/jackson-all1.9.11.jar

如果使⽤maven构建项⽬，加⼊下⾯的依赖

<dependency> <groupId>org.codehaus.jackson</groupId> <artifactId>jackson-mapper-asl</artifactId> <version>1.9.11</version>

</dependency> ⽆代码⽆真相，为了最简单的说明，我直接上代码。

public class User { private String name; private Gender gender; private List<Account> accounts; 省略get和set⽅法

... }

public enum Gender { MALE, FEMALE

}

public class Account { private Integer id; private String cardId; private BigDecimal balance;

private Date date; 省略get和set⽅法

... }

public static void main(String[] args) throws Exception { User user = new User(); user.setName("菠萝⼤象"); user.setGender(Gender.MALE); List<Account> accounts = new ArrayList<Account>(); Account account = new Account();

- account.setId(1); account.setBalance(BigDecimal.valueOf(1900.2)); account.setCardId("423335533434"); account.setDate(new Date()); accounts.add(account); account = new Account();

- account.setId(2); account.setBalance(BigDecimal.valueOf(5000)); account.setCardId("625444548433"); account.setDate(new Date());


accounts.add(account); user.setAccounts(accounts);

ObjectMapper mapper = new ObjectMapper(); mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE); String json = mapper.writeValueAsString(user); System.out.println("Java2Json: "+json); user = mapper.readValue(json, User.class); System.out.println("Json2Java: "+mapper.writeValueAsString(user));

}

mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);这是辅助设置，控制 格式化输出。

之前使⽤的mapper.getSerializationConfig().setXxx⽅法现在很多都已经被标注为@Deprecated了，因此请⼤ 家使⽤上⾯的⽅式处理。

SerializationConfig.Feature枚举⾥⾯还有很多其它的设置项，⽐如⽇期，⽐如要不要输出null值等等。其它的还 有：

org.codehaus.jackson.JsonGenerator.Feature.* org.codehaus.jackson.JsonParser.Feature.*

让我们来看看输出结果，两次转换之后，打印出来的字符串应该是⼀样的：

![image 1](<jackson-mapper-asl总结一下自己使用jackson处理对象与JSON之间相互转换的心得。.note_images/imageFile1.png>)

OK，果然结果是⼀致的，⼤家现在应该会使⽤jackson进⾏Java与Json的互相转换了吧？恩，现在再考虑⼀种情况，如 果想将List<User>的JSON字符串反转为泛型，应该怎么做呢？

想这样：mapper.readValue(json, List<User>.class)？这可是错误的，这⾥的参数是Class<T> valueType， valueType是Class<T>类的对象。如上⾯所示User.class 就是Class<User>类的对象。因此要想获得泛型的集合类型需 要通过其它办法： /**

- * 获取泛型的Collection Type

- * @param jsonStr json字符串

- * @param collectionClass 泛型的Collection

- * @param elementClasses 元素类型

- */


public static <T> T readJson(String jsonStr, Class<?> collectionClass, Class<?>... elementClasses) throws Exception {

ObjectMapper mapper = new ObjectMapper(); JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass,

elementClasses);

return mapper.readValue(jsonStr, javaType); }

定义⼀个List<User>，向⾥⾯添加两次user，先调⽤writeValueAsString⽅法打印出json，再调⽤readJson⽅

法，这不仅可以转换泛型List<T>，还可以⽤于其它集合，⽐如Map<K,V>等等。 List<User> list = readJson(json, List.class, User.class); ObjectMapper可以让对象与JSON之间相互转换，除此之外Jackson还提供了JsonGenerator 和JsonParser 这两个

类，它们可以更细粒度的处理序列化与反序列化。调⽤ObjectMapper的writeValueAsString和readValue⽅法，最终 还是会交给JsonGenerator 和JsonParser 去处理，对此还有疑惑的话，可以去看看这两个⽅法的源码。

本⽂为菠萝⼤象原创，如要转载请注明出处。http://www.blogjava.net/bolo

