---
title: spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note（原文插图 annex）
slug: annex-spring-cloud-Dalston.SR4-feign-实际开发中踩坑(一)
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/采坑记录/spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

本⽂采⽤的springcloud 版本 Dalston.SR4 所有例⼦以Dalston.SR4 版本为准 feign 作为 springcloud 微服务 内部通信的组件 还是有很多坑的

- 坑1、 Load balancer does not have available server for client 这是因为 默认的eureka 启动时 相关的服务端还没有来得及往 eureka 服务端注册 或者 eureka server中没有注册 相关的服务

- 坑2、看了很多 教程 说 feign 类上的 @RequestMaping 不会被加⼊ 接⼝映射 没错 想想也应该是 这样 但是为了解决这个问题 我在接⼝⽅法上加⼊映射路径是否可⾏呢 可以做个 测试 我这⾥有 2个服务 MESAGE-SERVICE 是服务提供者 SMS-SERVICE 是 服务消费者


feignware 单独模块 ⽤来统⼀提供 相互调⽤的api ⽣产中建议feign集中配置 ⼀个eureka server

![image 1](<spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note_images/imageFile1.png>)

MESAGE-SERVICE 提供接⼝ ：

@RestController @RequestMapping("/msg") public class UserController {

@PostMapping("/get") public User getUser(@RequestBody User user) {

System.out.println("i am message-service <<<<<<<<<<<<<<<<<<<<"); System.out.println(user); return user;

}

}

feignware : 这样写 直接加⼊ 类⼀级的/msg

@FeignClient(value = "MEMBER-SERVICE") public interface UserService {

@PostMapping("/msg/get")

User get(@RequestBody User user); }

SMS-SERVICE 调⽤接⼝:

@RestController @RequestMapping("/sms") public class RemoteController {

@Resource

private UserService userService; @GetMapping("/go") public User test(User user){

System.out.println(">>>>>>>>>>>"); User result=userService.get(user); System.out.println(result);

return result; }

}

流程是这样的 ：

![image 2](<spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note_images/imageFile2.png>)

执⾏结果： 请求成功

![image 3](<spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note_images/imageFile3.png>)

去掉了 feignware 中 /msg ⼀级后 测试 结果：

![image 4](<spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note_images/imageFile4.png>)

结果说明： feign 类上的 @RequestMaping 不会被加⼊ 接⼝映射 是对的 如果 想加⼊正好上⾯ 是个解决⽅案

- 坑3、 feign 调⽤是 post 请求 还是上⾯的例⼦ 测试看看 先说明⼀句 看到有的教程上说 feign只⽀持 @RequestMaping 这种说法是不对 上⾯已经有例⼦证明了这⼀点 起码 上 @GetMaping @PostMaping 是⽀持的 其他 springcloud版本没有试过


我们先三者都采⽤@GetMaping 看看结果：

![image 5](<spring cloud Dalston.SR4 feign 实际开发中踩坑(一).note_images/imageFile5.png>)

405 到底是哪⾥不⽀持 get呢 来⼏组测试： sms get feignware get mesage get 结果 405 sms get feignware post mesage get 结果 405 sms post feignware post mesage get 结果 405 sms post feignware get mesage post 结果 20 sms post feignware post mesage post 结果 20 sms get feignware post mesage post 结果 20 sms get feignware get mesage post 结果 20 结果总结⼀下 就是 @RequestBody 调⽤提供⽅ ⼀定要⽤post feign包 跟消费⽅ ⽆所谓
