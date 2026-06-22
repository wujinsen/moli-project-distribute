具体做法：

![image 1](<springboot + profile（不同环境读取不同配置）.note_images/imageFile1.png>)

不同环境的配置设置⼀个配置⽂件，例如：dev环境下的配置配置在application-dev.properties中；prod环境下的配置配置 在application-prod.properties中。 在application.properties中指定使⽤哪⼀个⽂件

# 1、application-dev.properties（dev环境下的配置）

![image 2](<springboot + profile（不同环境读取不同配置）.note_images/imageFile2.png>)

1 profile = dev_envrimont

# 2、application-prod.properties（prod环境下的配置）

![image 3](<springboot + profile（不同环境读取不同配置）.note_images/imageFile3.png>)

1 profile = prod_envrimont

# 3、application.properties

![image 4](<springboot + profile（不同环境读取不同配置）.note_images/imageFile4.png>)

- 1 spring.data.mongodb.uri=mongodb://192.168.22.110:27017/myfirstMongodb

- 2

- 3 #spring.profiles.active

- 4 spring.profiles.active=dev


说明：上边的配置表示使⽤dev环境下的配置。

注意：spring.data.mongodb.uri=mongodb://192.168.22.110:27017/myfirstMongodb该配置是applicationdev.properties没有的配置

# 4、Controller

![image 5](<springboot + profile（不同环境读取不同配置）.note_images/imageFile5.png>)

![image 6](<springboot + profile（不同环境读取不同配置）.note_images/imageFile6.png>)

- 1 @Autowired

- 2 private Environment env;

- 3

- 4 @RequestMapping("/testProfile")

- 5 public String testProfile(){

- 6 return env.getProperty("profile");

- 7 }


![image 7](<springboot + profile（不同环境读取不同配置）.note_images/imageFile7.png>)

测试：

上述代码执⾏后的结果是：dev_envrimont和mongodb://192.168.22.110:27017/myfirstMongodb 如果application.properties的配置改为：spring.profiles.active=prod，则结果是：prod_envrimont 如果application.properties的配置改为：spring.profiles.active=prod，⽽application.properties中也配置了 profile=xxx（不管该配置配置在spring.profiles.active=prod的上⽅还是下⽅），这个时候结果是：prod_envrimont 如果application.properties的配置改为：spring.profiles.active=prod，⽽application.properties中也配置了 profile=xxx（不管该配置配置在spring.profiles.active=prod的上⽅还是下⽅），但是application-prod.properties删掉 了profile = prod_envrimont，这个时候结果是：xxx

结论：

各个环境公共的配置写在application.properties中 各个模块独有的配置配置在⾃⼰的application-{xxx}.properties⽂件中 程序读取的时候优先读取application.properties中选中的profile的配置，若读不到才会从application.properties去 读

