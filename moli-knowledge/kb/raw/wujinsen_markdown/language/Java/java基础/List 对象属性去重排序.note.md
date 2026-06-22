public clastest2 { public static voidmain(String[] args) { /*

- * 有 ⼀ 个 List<User> list 放 了五 个 对 象 ： user1、user2、user3、user4、user5 User有 三个 属 性 Id、name、age 其 中 user2的 记 录 ⼤ 概 是 这 样 ： “10”,"abc",20; user3的 记 录 ⼤ 概 是 这 样 ： “10”， “def”,20; 请 问 怎 么 才 能 只 保 留 user2和 user3中 的 ⼀ 个 对 象 ， 并 将 其 中 的 name合 并 到 新 对 象 中 ， 新 对 象 如 “10”， “abcdef”， 20 这 只 是 举 个 例 ⼦ ， 实 际 中 有 可 能 user4和 user5与 此 类 似 ， 如 果有 id相 同 的 两个 对 象 ， 则 对 其 进 ⾏ 合 并 ， 只 保 留 ⼀ 个 对 象 ， 求 ⼀ 个 通 ⽤ 的 ⽅ 法 ， 能 筛 选 出 对 象 集 合 中 某 些 相 同 ID的 两个 对 象 ， 将 其 合 并 仍保 留 在 原 list中

- *@paramargs

- */ /list有 序 可 重 复 、set⽆ 序 不 可 重 复 、mapkey不 允 许 重 复 ， key相 同 的 后 ⾯ 的 value会 把 前 ⾯ 的 覆 盖 掉 /List存 放 的 数据 ， 默 认 是 按 照 放 ⼊ 时 的 顺 序 存 放 的 ， ⽐ 如 依 次 放 ⼊ A、B、C， 则 取 得 时 候 ， 则 也 是 A、B、C的 顺 序


List<User> list =newArrayList<>(); list.ad(newUser(1,"a",20);

- list.ad(newUser(1,"a",20);

- list.ad(newUser(2,"a",20);

- list.ad(newUser(3,"b",20);

- list.ad(newUser(1,"c",20);

list.ad(newUser(4,"d",20);

- list.ad(newUser(2,"e",20); list.ad(newUser(1,"a",20);




/* for (User user : list) { System.out.println(user.toString();

} System.out.println();*/

list =mySort(list); for(User user : list) {

System.out.println(user.toString(); }

}

public staticList<User> mySort(List<User> list) { HashMap<Integer, User> tempMap =newHashMap<>(); for(User user : list) {

intkey = user.getId();

/ containsKey(Object key) 该 ⽅ 法 判 断 Map集 合 对 象 中 是 否 包 含 指 定 的 键 名 。如 果 Map集 合 中 包 含 指 定 的 键 名 ， 则 返 回 true， 否 则 返 回 false

/ containsValue(Object value) value： 要 查 询 的 Map集 合 的 指 定 键 值 对 象 .如 果 Map集 合 中 包 含 指 定 的 键 值 ， 则 返 回 true， 否 则 返 回 false

if(tempMap.containsKey(key) { User tempUser =newUser(key, tempMap.get(key).getName() + user.getName(),

tempMap.get(key).getAge();/user.getAge();

/HashMap是 不 允 许 key重 复 的 ， 所 以 如 果有 key重 复 的 话 ， 那 么 前 ⾯ 的 value会 被 后 ⾯ 的 value覆 盖

tempMap.put(key, tempUser); }else{

tempMap.put(key, user); }

} List<User> tempList =newArrayList<>(); for(intkey : tempMap.keySet() {

tempList.ad(tempMap.get(key);

} returntempList;

}

}

clasUser { private intid; privateStringname; private intage;

publicUser() { }

publicUser(intid, String name,intage) { super(); this.id= id; this.name= name; this.age= age;

}

public intgetId() {

returnid; }

public voidsetId(intid) {

this.id= id; }

publicString getName() {

returnname; }

public voidsetName(String name) {

this.name= name; }

public intgetAge() {

returnage; }

public voidsetAge(intage) {

this.age= age; }

@Override publicString toString() {

return"User [id="+id+", name="+name+", age="+age+"]"; }

}

public clastest { public static voidmain(String[] args) { List<Student> list =newArrayList<Student>();

/创 建 3个 学 ⽣ 对 象 ， 年 龄 分别 是 20、19、21， 并 将 他们依 次 放 ⼊ List中

- Student s1 =newStudent();

- s1.setAge(20);

- s1.setName("葛⼤");

Student s2 =newStudent();

- s2.setAge(19);

s2.setName("张杰"); Student s3 =newStudent(); s3.setAge(21);

- s3.setName("宝爷");






- list.ad(s1);

- list.ad(s2);

- list.ad(s3);


System.out.println("排序前："+list);

Colections.sort(list,newComparator<Student>(){

/*

- * int compare(Student o1, Student o2) 返 回 ⼀ 个 基 本 类 型 的 整 型 ，

- * 返 回 负 数 表 示 ： o1 ⼩ 于 o2，

- * 返 回 0 表 示 ： o1和 o2相 等 ，

- * 返 回 正 数 表 示 ： o1⼤ 于 o2。

- */ public intcompare(Student o1, Student o2) {


/按 照 学 ⽣ 的 年 龄 进 ⾏ 升 序 排 列 ;<是 降 序

/*if(o1.getAge() > o2.getAge(){ return 1;

} if(o1.getAge() = o2.getAge(){

return 0;

} return -1; */ returno1.getAge()-o2.getAge();/升 序

/ return o2.getAge()-o1.getAge();/降 序

- / return o1.getName().compareTo(o2.getName() ;/ 按 照 姓 名升 序

- / return o2.getName().compareTo(o1.getName() ;/ 按 照 姓 名 降 序 }


}); System.out.println("排序后："+list);

}

}

clasStudent { private intage; privateStringname; public intgetAge() {

returnage; }

public voidsetAge(intage) {

this.age= age; }

publicString getName() {

returnname; }

public voidsetName(String name) {

this.name= name; }

@Override publicString toString() {

return"Student [age="+age+", name="+name+"]"; }

}

