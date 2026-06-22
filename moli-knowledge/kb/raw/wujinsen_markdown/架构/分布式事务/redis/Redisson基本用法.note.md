htps:/ w.cnblogs.com/cjsblog/p/1273205.html

![image 1](<Redisson基本用法.note_images/imageFile1.png>)

# 1. Redi son

Redison是Redis官⽅推荐的Java版的Redis客户端。它提供的功能⾮常多，也⾮常强⼤，此处我们只 ⽤它的分布式锁功能。

htps:/github.com/redison/redison

- 1.1. 基本⽤法

- 1 <dependency>

- 2 <groupId>org.redisson</groupId>

- 3 <artifactId>redisson</artifactId>

- 4 <version>3.11.1</version>

- 5 </dependency>


- 1.2. Distributed locks and synchronizers RedisonClient中提供了好多种锁，还有其它很多实⽤的⽅法


![image 2](<Redisson基本用法.note_images/imageFile2.png>)

![image 3](<Redisson基本用法.note_images/imageFile3.png>)

- 1.2.1. Lock 默认，⾮公平锁 最简洁的⼀种⽅法


![image 4](<Redisson基本用法.note_images/imageFile4.png>)

指定超时时间

![image 5](<Redisson基本用法.note_images/imageFile5.png>)

异步

![image 6](<Redisson基本用法.note_images/imageFile6.png>)

- 1.2.2 Fair Lock
- 1.2.3 MultiLock
- 1.2.4 RedLock


![image 7](<Redisson基本用法.note_images/imageFile7.png>)

![image 8](<Redisson基本用法.note_images/imageFile8.png>)

![image 9](<Redisson基本用法.note_images/imageFile9.png>)

![image 10](<Redisson基本用法.note_images/imageFile10.png>)

## 1.3. 示例 pom.xml

- 1 <?xml version="1.0" encoding="UTF-8"?>

- 2 <project xmlns="http://maven.apache.org/POM/4.0.0"

xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"

- 3 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0

http://maven.apache.org/xsd/maven-4.0.0.xsd">

- 4 <modelVersion>4.0.0</modelVersion>

- 5 <parent>

- 6 <groupId>org.springframework.boot</groupId>

- 7 <artifactId>spring-boot-starter-parent</artifactId>

- 8 <version>2.1.6.RELEASE</version>

- 9 <relativePath/> <!-- lookup parent from repository -->

- 10 </parent>

- 11 <groupId>com.cjs.example</groupId>

- 12 <artifactId>cjs-redisson-example</artifactId>

- 13 <version>0.0.1-SNAPSHOT</version>

- 14 <name>cjs-redisson-example</name>

- 15

- 16 <properties>

- 17 <java.version>1.8</java.version>

- 18 </properties>

- 19

- 20 <dependencies>

- 21 <dependency>

- 22 <groupId>org.springframework.boot</groupId>

- 23 <artifactId>spring-boot-starter-data-jpa</artifactId>

- 24 </dependency>

- 25 <dependency>

- 26 <groupId>org.springframework.boot</groupId>

- 27 <artifactId>spring-boot-starter-data-redis</artifactId>

- 28 </dependency>

- 29 <dependency>

- 30 <groupId>org.springframework.boot</groupId>

- 31 <artifactId>spring-boot-starter-web</artifactId>

- 32 </dependency>

- 33

- 34 <!-- https://github.com/redisson/redisson#quick-start -->

- 35 <dependency>


- 36 <groupId>org.redisson</groupId>

- 37 <artifactId>redisson</artifactId>

- 38 <version>3.11.1</version>

- 39 </dependency>

- 40

- 41

- 42 <dependency>

- 43 <groupId>org.apache.commons</groupId>

- 44 <artifactId>commons-lang3</artifactId>

- 45 <version>3.9</version>

- 46 </dependency>

- 47 <dependency>

- 48 <groupId>com.alibaba</groupId>

- 49 <artifactId>fastjson</artifactId>

- 50 <version>1.2.58</version>

- 51 </dependency>

- 52 <dependency>

- 53 <groupId>org.apache.commons</groupId>

- 54 <artifactId>commons-pool2</artifactId>

- 55 <version>2.6.2</version>

- 56 </dependency>

- 57

- 58 <dependency>

- 59 <groupId>mysql</groupId>

- 60 <artifactId>mysql-connector-java</artifactId>

- 61 <scope>runtime</scope>

- 62 </dependency>

- 63 <dependency>

- 64 <groupId>org.projectlombok</groupId>

- 65 <artifactId>lombok</artifactId>

- 66 <optional>true</optional>

- 67 </dependency>

- 68 </dependencies>

- 69

- 70 <build>

- 71 <plugins>

- 72 <plugin>


- 73 <groupId>org.springframework.boot</groupId>

- 74 <artifactId>spring-boot-maven-plugin</artifactId>

- 75 </plugin>

- 76 </plugins>

- 77 </build>

- 78

- 79 </project> aplication.yml


- 1 server:

- 2 port: 8080

- 3 spring:

- 4 application:

- 5 name: cjs-redisson-example

- 6 redis:

- 7 cluster:

- 8 nodes: 10.0.29.30:6379, 10.0.29.95:6379, 10.0.29.205:6379

- 9 lettuce:

- 10 pool:

- 11 min-idle: 0

- 12 max-idle: 8

- 13 max-active: 20

- 14 datasource:

- 15 url: jdbc:mysql://127.0.0.1:3306/test

- 16 username: root

- 17 password: 123456

- 18 driver-class-name: com.mysql.cj.jdbc.Driver

- 19 type: com.zaxxer.hikari.HikariDataSource RedisonConfig.java


- 1 package com.cjs.example.lock.config;

- 2

- 3 import org.redisson.Redisson;

- 4 import org.redisson.api.RedissonClient;

- 5 import org.redisson.config.Config;

- 6 import org.springframework.context.annotation.Bean;

- 7 import org.springframework.context.annotation.Configuration;

- 8

- 9 /**

- 10 * @author ChengJianSheng

- 11 * @date 2019-07-26

- 12 */

- 13 @Configuration

- 14 public class RedissonConfig {

- 15

- 16 @Bean

- 17 public RedissonClient redissonClient() {

- 18 Config config = new Config();

- 19 config.useClusterServers()

- 20 .setScanInterval(2000)

- 21 .addNodeAddress("redis://10.0.29.30:6379", "redis://10.0.29.95:6379")

- 22 .addNodeAddress("redis://10.0.29.205:6379");

- 23

- 24 RedissonClient redisson = Redisson.create(config);

- 25

- 26 return redisson;

- 27 }

- 28

- 29 } CourseServiceImpl.java


- 1 package com.cjs.example.lock.service.impl;

- 2

- 3 import com.alibaba.fastjson.JSON;

- 4 import com.cjs.example.lock.constant.RedisKeyPrefixConstant;

- 5 import com.cjs.example.lock.model.CourseModel;

- 6 import com.cjs.example.lock.model.CourseRecordModel;

- 7 import com.cjs.example.lock.repository.CourseRecordRepository;

- 8 import com.cjs.example.lock.repository.CourseRepository;

- 9 import com.cjs.example.lock.service.CourseService;

- 10 import lombok.extern.slf4j.Slf4j;

- 11 import org.apache.commons.lang3.StringUtils;

- 12 import org.redisson.api.RLock;

- 13 import org.redisson.api.RedissonClient;

- 14 import org.springframework.beans.factory.annotation.Autowired;

- 15 import org.springframework.data.redis.core.HashOperations;

- 16 import org.springframework.data.redis.core.StringRedisTemplate;

- 17 import org.springframework.stereotype.Service;

- 18

- 19 import java.util.concurrent.TimeUnit;

- 20

- 21 /**

- 22 * @author ChengJianSheng

- 23 * @date 2019-07-26

- 24 */

- 25 @Slf4j

- 26 @Service

- 27 public class CourseServiceImpl implements CourseService {

- 28

- 29 @Autowired

- 30 private CourseRepository courseRepository;

- 31 @Autowired

- 32 private CourseRecordRepository courseRecordRepository;

- 33 @Autowired

- 34 private StringRedisTemplate stringRedisTemplate;

- 35 @Autowired

- 36 private RedissonClient redissonClient;

- 37


- 38 @Override

- 39 public CourseModel getById(Integer courseId) {

- 40

- 41 CourseModel courseModel = null;

- 42

- 43 HashOperations<String, String, String> hashOperations =

stringRedisTemplate.opsForHash();

- 44

- 45 String value =

hashOperations.get(RedisKeyPrefixConstant.COURSE, String.valueOf(courseId));

- 46

- 47 if (StringUtils.isBlank(value)) {

- 48 String lockKey = RedisKeyPrefixConstant.LOCK_COURSE +

courseId;

- 49 RLock lock = redissonClient.getLock(lockKey);

- 50 try {

- 51 boolean res = lock.tryLock(10, TimeUnit.SECONDS);

- 52 if (res) {

- 53 value =

hashOperations.get(RedisKeyPrefixConstant.COURSE, String.valueOf(courseId));

- 54 if (StringUtils.isBlank(value)) {

- 55 log.info("从数据库中读取");

- 56 courseModel =

courseRepository.findById(courseId).orElse(null);

- 57

hashOperations.put(RedisKeyPrefixConstant.COURSE, String.valueOf(courseId), JSON.toJSONString(courseModel));

- 58 }

- 59 }

- 60 } catch (InterruptedException e) {

- 61 e.printStackTrace();

- 62 } finally {

- 63 lock.unlock();

- 64 }

- 65 } else {


- 66 log.info("从缓存中读取");

- 67 courseModel = JSON.parseObject(value, CourseModel.class);

- 68 }

- 69

- 70 return courseModel;

- 71 }

- 72

- 73 @Override

- 74 public void upload(Integer userId, Integer courseId, Integer

studyProcess) {

- 75

- 76 HashOperations<String, String, String> hashOperations =

stringRedisTemplate.opsForHash();

- 77

- 78 String cacheKey = RedisKeyPrefixConstant.COURSE_PROGRESS + ":"

+ userId;

- 79 String cacheValue = hashOperations.get(cacheKey,

String.valueOf(courseId));

- 80 if (StringUtils.isNotBlank(cacheValue) && studyProcess <=

Integer.valueOf(cacheValue)) {

- 81 return;

- 82 }

- 83

- 84 String lockKey = "upload:" + userId + ":" + courseId;

- 85

- 86 RLock lock = redissonClient.getLock(lockKey);

- 87

- 88 try {

- 89 lock.lock(10, TimeUnit.SECONDS);

- 90

- 91 cacheValue = hashOperations.get(cacheKey,

String.valueOf(courseId));

- 92 if (StringUtils.isBlank(cacheValue) || studyProcess >

Integer.valueOf(cacheValue)) {

- 93 CourseRecordModel model = new CourseRecordModel();

- 94 model.setUserId(userId);

- 95 model.setCourseId(courseId);


- 96 model.setStudyProcess(studyProcess);

- 97 courseRecordRepository.save(model);

- 98 hashOperations.put(cacheKey, String.valueOf(courseId),

String.valueOf(studyProcess));

- 99 }

- 100

- 101 } catch (Exception ex) {

- 102 log.error("获取所超时！", ex);

- 103 } finally {

- 104 lock.unlock();

- 105 }

- 106

- 107 }

- 108 } StockServiceImpl.java


- 3 import com.cjs.example.lock.constant.RedisKeyPrefixConstant;

- 4 import com.cjs.example.lock.service.StockService;

- 5 import org.apache.commons.lang3.StringUtils;

- 6 import org.springframework.beans.factory.annotation.Autowired;

- 7 import org.springframework.data.redis.core.HashOperations;

- 8 import org.springframework.data.redis.core.StringRedisTemplate;

- 9 import org.springframework.stereotype.Service;

- 10

- 11 /**

- 12 * @author ChengJianSheng

- 13 * @date 2019-07-26

- 14 */

- 15 @Service

- 16 public class StockServiceImpl implements StockService {

- 17

- 18 @Autowired

- 19 private StringRedisTemplate stringRedisTemplate;

- 20

- 21 @Override

- 22 public int getByProduct(Integer productId) {

- 23 HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();

- 24 String value = hashOperations.get(RedisKeyPrefixConstant.STOCK, String.valueOf(productId));

- 25 if (StringUtils.isBlank(value)) {

- 26 return 0;

- 27 }

- 28 return Integer.valueOf(value);

- 29 }

- 30

- 31 @Override

- 32 public boolean decrease(Integer productId) {

- 33 int stock = getByProduct(productId);

- 34 if (stock <= 0) {

- 35 return false;


- 36 }

- 37 HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();

- 38 hashOperations.put(RedisKeyPrefixConstant.STOCK, String.valueOf(productId), String.valueOf(stock - 1));

- 39 return true;

- 40 }

- 41 } OrderServiceImpl.java


- 3 import com.cjs.example.lock.model.OrderModel;

- 4 import com.cjs.example.lock.repository.OrderRepository;

- 5 import com.cjs.example.lock.service.OrderService;

- 6 import com.cjs.example.lock.service.StockService;

- 7 import lombok.extern.slf4j.Slf4j;

- 8 import org.redisson.api.RLock;

- 9 import org.redisson.api.RedissonClient;

- 10 import org.springframework.beans.factory.annotation.Autowired;

- 11 import org.springframework.stereotype.Service;

- 12

- 13 import java.util.Date;

- 14 import java.util.UUID;

- 15 import java.util.concurrent.TimeUnit;

- 16

- 17 /**

- 18 * @author ChengJianSheng

- 19 * @date 2019-07-30

- 20 */

- 21 @Slf4j

- 22 @Service

- 23 public class OrderServiceImpl implements OrderService {

- 24

- 25 @Autowired

- 26 private StockService stockService;

- 27 @Autowired

- 28 private OrderRepository orderRepository;

- 29 @Autowired

- 30 private RedissonClient redissonClient;

- 31

- 32 /**

- 33 * 乐观锁

- 34 */

- 35 @Override

- 36 public String save(Integer userId, Integer productId) {

- 37 int stock = stockService.getByProduct(productId);


- 38 log.info("剩余库存：{}", stock);

- 39 if (stock <= 0) {

- 40 return null;

- 41 }

- 42

- 43 // 如果不加锁，必然超卖

- 44

- 45 RLock lock = redissonClient.getLock("stock:" + productId);

- 46

- 47 try {

- 48 lock.lock(10, TimeUnit.SECONDS);

- 49

- 50 String orderNo = UUID.randomUUID().toString().replace("-", "").toUpperCase();

- 51

- 52 if (stockService.decrease(productId)) {

- 53

- 54 OrderModel orderModel = new OrderModel();

- 55 orderModel.setUserId(userId);

- 56 orderModel.setProductId(productId);

- 57 orderModel.setOrderNo(orderNo);

- 58 Date now = new Date();

- 59 orderModel.setCreateTime(now);

- 60 orderModel.setUpdateTime(now);

- 61 orderRepository.save(orderModel);

- 62

- 63 return orderNo;

- 64 }

- 65

- 66 } catch (Exception ex) {

- 67 log.error("下单失败", ex);

- 68 } finally {

- 69 lock.unlock();

- 70 }

- 71

- 72 return null;

- 73 }


- 74

- 75 } OrderModel.java


- 1 package com.cjs.example.lock.model;

- 2

- 3 import lombok.Data;

- 4

- 5 import javax.persistence.*;

- 6 import java.io.Serializable;

- 7 import java.util.Date;

- 8

- 9 /**

- 10 * @author ChengJianSheng

- 11 * @date 2019-07-30

- 12 */

- 13 @Data

- 14 @Entity

- 15 @Table(name = "t_order")

- 16 public class OrderModel implements Serializable {

- 17

- 18 @Id

- 19 @GeneratedValue(strategy = GenerationType.IDENTITY)

- 20 private Integer id;

- 21

- 22 @Column(name = "order_no")

- 23 private String orderNo;

- 24

- 25 @Column(name = "product_id")

- 26 private Integer productId;

- 27

- 28 @Column(name = "user_id")

- 29 private Integer userId;

- 30

- 31 @Column(name = "create_time")

- 32 private Date createTime;

- 33

- 34 @Column(name = "update_time")

- 35 private Date updateTime;

- 36 } 数据库脚本.sql


- 1 SET NAMES utf8mb4;

- 2 SET FOREIGN_KEY_CHECKS = 0;

- 3

- 4 -- ----------------------------

- 5 -- Table structure for t_course

- 6 -- ----------------------------

- 7 DROP TABLE IF EXISTS `t_course`;

- 8 CREATE TABLE `t_course` (

- 9 `id` int(11) NOT NULL AUTO_INCREMENT,

- 10 `course_name` varchar(64) NOT NULL,

- 11 `course_type` tinyint(4) NOT NULL DEFAULT '1',

- 12 `start_time` datetime NOT NULL,

- 13 PRIMARY KEY (`id`)

- 14 ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

- 15

- 16 -- ----------------------------

- 17 -- Table structure for t_order

- 18 -- ----------------------------

- 19 DROP TABLE IF EXISTS `t_order`;

- 20 CREATE TABLE `t_order` (

- 21 `id` int(11) NOT NULL AUTO_INCREMENT,

- 22 `order_no` varchar(256) CHARACTER SET latin1 NOT NULL,

- 23 `user_id` int(11) NOT NULL,

- 24 `product_id` int(11) NOT NULL,

- 25 `create_time` datetime NOT NULL,

- 26 `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

- 27 PRIMARY KEY (`id`)

- 28 ) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

- 29

- 30 -- ----------------------------

- 31 -- Table structure for t_user_course_record

- 32 -- ----------------------------

- 33 DROP TABLE IF EXISTS `t_user_course_record`;

- 34 CREATE TABLE `t_user_course_record` (

- 35 `id` int(11) NOT NULL AUTO_INCREMENT,

- 36 `user_id` int(11) NOT NULL,


- 37 `course_id` int(11) NOT NULL,

- 38 `study_process` int(11) NOT NULL,

- 39 PRIMARY KEY (`id`)

- 40 ) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4;

- 41

- 42 SET FOREIGN_KEY_CHECKS = 1;


- 1.4 ⼯程结构
- 1.5 Redis集群创建


htps:/github.com/chengjiansheng/cjs-redison-example

![image 11](<Redisson基本用法.note_images/imageFile11.png>)

![image 12](<Redisson基本用法.note_images/imageFile12.png>)

![image 13](<Redisson基本用法.note_images/imageFile13.png>)

![image 14](<Redisson基本用法.note_images/imageFile14.png>)

![image 15](<Redisson基本用法.note_images/imageFile15.png>)

![image 16](<Redisson基本用法.note_images/imageFile16.png>)

![image 17](<Redisson基本用法.note_images/imageFile17.png>)

![image 18](<Redisson基本用法.note_images/imageFile18.png>)

## 1.6 测试 测试/course/upload

![image 19](<Redisson基本用法.note_images/imageFile19.png>)

### 测试/order/create

![image 20](<Redisson基本用法.note_images/imageFile20.png>)

![image 21](<Redisson基本用法.note_images/imageFile21.png>)

![image 22](<Redisson基本用法.note_images/imageFile22.png>)

# 2. Spring Integration

⽤法与Redison类似

- 1 <dependency>

- 2 <groupId>org.springframework.boot</groupId>

- 3 <artifactId>spring-boot-starter-integration</artifactId>

- 4 </dependency>

- 5 <dependency>

- 6 <groupId>org.springframework.integration</groupId>

- 7 <artifactId>spring-integration-redis</artifactId>

- 8 </dependency>


- 1 package com.kaishustory.base.conf;

- 2

- 3 import org.springframework.context.annotation.Bean;

- 4 import org.springframework.context.annotation.Configuration;

- 5 import

org.springframework.data.redis.connection.RedisConnectionFactory;

- 6 import org.springframework.integration.redis.util.RedisLockRegistry;

- 7

- 8 /**

- 9 * @author ChengJianSheng

- 10 * @date 2019-07-30

- 11 */

- 12 @Configuration

- 13 public class RedisLockConfig {

- 14

- 15 @Bean

- 16 public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {

- 17 return new RedisLockRegistry(redisConnectionFactory, "asdf")

- 18 }

- 19

- 20 }


- 1 @Autowired

- 2 private RedisLockRegistry redisLockRegistry;

- 3

- 4 public void save(Integer userId) {

- 5

- 6 String lockKey = "order:" + userId;

- 7

- 8 Lock lock = redisLockRegistry.obtain(lockKey);

- 9 try {

- 10 lock.lock();

- 11

- 12 //todo

- 13

- 14 } finally {

- 15 lock.unlock();

- 16 }

- 17

- 18 }


# 3. 其它

htps:/github.com/redison/redison/wiki/8.-Distributed-locks-and-synchronizers htps:/ w.cnblogs.com/cjsblog/p/9831423.html

