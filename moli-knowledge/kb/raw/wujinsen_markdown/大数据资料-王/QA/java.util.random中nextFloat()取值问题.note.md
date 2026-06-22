- 1 nextFloat()返回的是0.0-1.0之间的随机数，取值范围是在

- 2 [0.0，1.0）还是[0.0，1.0]或者（0.0，1.0）

- 3

- 4 “[”为包含

- 5 “）”为不包含


我有更好的答案

分享到：

# 1条回答

- 1 是[0.0，1.0），测试⽅法

- 2 public static void main(String[] args) {

- 3 float f;

- 4 do {

- 5 f = new Random().nextFloat();

- 6 if (f == 1.0f) {

- 7 System.out.println(1);

- 8 }

- 9 System.out.println("不会为1");

- 10 }while(f != 1.0f);

- 11 System.out.println("js");

- 12 }


