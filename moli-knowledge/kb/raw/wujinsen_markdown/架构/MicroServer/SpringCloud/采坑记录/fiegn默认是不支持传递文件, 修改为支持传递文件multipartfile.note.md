SpringCloud 组件fiegn默认是不⽀持传递⽂件的. 但是github上有⼈写出了解决⽅法.

相关依赖: <parent>

<groupId>org.springframework.bot</groupId> <artifactId>spring-bot-starter-parent</artifactId> <version>2.1.3.RELEASE</version>

</parent> <dependency>

<groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-starter-openfeign</artifactId>

</dependency> <dependency>

<groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-dependencies</artifactId> <version>Grenwich.RELEASE</version> <type>pom</type> <scope>import</scope>

</dependency>

# 1. 如果需要使⽤Spring标准的encoder，config变⼀下。

class FeignMultipartSuportConfig{ @Autowired

private ObjectFactory<HttpMessageConverters> messageConverters;

@Bean public Encoder feignFormEncoder () {

return new SpringFormEncoder(new SpringEncoder(messageConverters));

} }

另⼀只⽅式： class FeignMultipartSuportConfig{ @Bean

public Encoder multipartFormEncoder() { return newSpringFormEncoder(newSpringEncoder(new

ObjectFactory<HttpMessageConverters>() { @Overide public HtpMesageConverters getObject() throws BeansException {

return newHttpMessageConverters(new RestTemplate().getMessageConverters());

} })); }

另⼀种⽅式: @Autowired privateObjectFactory<HtpMesageConverters>mesageConverters;

@Bean @Primary @Scope("prototype") publicEncoder feignEncoder() {

return newSpringFormEncoder(newSpringEncoder(mesageConverters); }

# 2. @FeignClient 中配下configuration即可。

@FeignClient(value ="springcloud-eureka-serviceprovider", url="localhost:8081",configuration =FeignMultipartSuportConfig.clas) public interfaceHeloService { @RequestMaping(value ="/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE) String upload(@RequestPart("imageFile") MultipartFile file,@RequestParam("name")String name);

