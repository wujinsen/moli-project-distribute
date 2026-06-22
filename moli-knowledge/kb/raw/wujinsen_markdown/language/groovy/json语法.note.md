- 1

- 2 def aaa = '''

- 3 {

- 4 "name": "wangwu",

- 5 "sex":2

- 6 }

- 7 ''';

- 8 def jsonSlurper = new JsonSlurperClassic()

- 9 //json对象转换为json字符串

- 10 def bbb = jsonSlurper.parseText(aaa)

- 11 println('bbb:' + bbb)

- 12

- 13 def tokenUrl = "http://123.249.98.192:8900/bbb";

- 14

- 15 def toJson = {

- 16 input ->

- 17 groovy.json.JsonOutput.toJson(input)

- 18 }

- 19 //json字符串转化为json对象

- 20 def ccc = toJson(bbb)

- 21 println('ccc:' + ccc)

- 22

- 23 def requestBody = ["name":"wangwu","sex":1]

- 24 //转化为json对象

- 25 def fff = groovy.json.JsonOutput.toJson(requestBody)

- 26 println fff

- 27 def props = readJSON text: '{ "name": "zhangsan" }'

- 28 println props


- 1 bbb:[sex:2, name:wangwu]

- 2 ccc: {"sex":2,"name":"wangwu"}

- 3 requestBody: [name:wangwu, sex:1]

- 4


