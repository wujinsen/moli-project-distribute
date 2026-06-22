Feign 作为 Spring Cloud 中 RPC ⼯具，利⽤注解来描述接⼝，简化了 Java HTTP Client 的 调⽤过程，隐藏了实现细节。 本⽂将介绍利⽤ Feign 上传⽂件的⼏个常⻅问题。

如何上传⼀个/组⽂件

如何上传多种⽂件

MultipartFile 参数不能为空问题

未提供 MultipartFile 参数接⼝报 no multipart boundary was found 问题

⼩编整理的⼀整套系统的Ja va学习教程从最基础的⾯向对象到框架再到项⽬实战的学习资 料都有整理，送给每⼀位⼩伙伴, 有想学习Ja va编程的，或是转⾏，或是⼤学⽣，还有⼯作 中想提升⾃⼰能⼒的，正在学习的⼩伙伴欢迎加⼊学习。 加Q群：69769,9179（招募中） 如何上传⼀个/组⽂件 OpenFeign 默认不⽀持⽂件参数，但提供了 feign-form 拓展⼯具，这⾥简单拓展下官⽅ Demo。 引⼊ io.github.openfeign.form:feign-form:3.8.0 和 io.github.openfeign.form:feign-formspring:3.8.0 maven依赖，注⼊ SpringFormEncoder ，在 @FeignClient 中配下 configuration即可。 @FeignClient(value = "cms-service", configuration = CmsService.MyConfig.class) public interface CmsService {

// 也可以使⽤MultipartFile[]上传多个⽂件 @PostMapping(value = "upload", consumes =

MediaType.MULTIPART_FORM_DATA_VALUE)

void upload(@RequestPart("file") MultipartFile file); class MyConfig {

@Bean

public Encoder feignFormEncoder() { return new SpringFormEncoder();

} }

} 如果需要使⽤Spring标准的encoder，config变⼀下。

class MyConfig { @Autowired

private ObjectFactory<HttpMessageConverters> messageConverters;

@Bean public Encoder feignFormEncoder () {

return new SpringFormEncoder(new SpringEncoder(messageConverters));

}

} 需要特别注意 feign-form 和 OpenFeign 版本之间的关系，官⽅描述如下：

![image 1](<Spring Cloud Feign 上传文件的常见问题.note_images/imageFile1.png>)

如何上传多种⽂件 如下，假设有file1、file2两个⽂件，且不是数组。 @FeignClient(value = "cms-service") public interface CmsService {

@PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

void upload(@RequestPart("file1") MultipartFile file1, (@RequestPart("file2") MultipartFile

file2); } 在应⽤启动时处理CmsService时，就会直接报错：

IllegalStateException: Method has too many Body parameters Feign 不⽀持多个body参数。本身⼀次上传多个⽂件场景少⻅，改为每次传⼀个就好。

MultipartFile 参数不能为空 假设有MultipartFile类型参数，但 required 设置为 false 。 @FeignClient(value = "cms-service") public interface CmsService {

@PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

void upload(@RequestPart(value = "file", required = false) MultipartFile file); } 会报错： Caused by: java.lang.IllegalArgumentException: Body parameter 6 was null 这个问题 Body parameter was null problem when MultipartFile param is null 最近我在 Github问过，解释是 Feign不⽀持这种特性，如果有需要，可以通过设置多个API解决，例 ⼦如下： public interface MailClient {

@PostMapping("/send", consumes = MediaType.APPLICATION_FORM_URL_ENCODED)

void send(@RequestParam("message") String message); @PostMapping("/send", consumes =

MediaType.MULTIPART_FORM_DATA_VALUE)

void send(@RequestParam("message") String message, @RequestPart("attachment") MultipartFile

file); }

no multipart boundary was found 问题 上次⼀步的例⼦会引发新的问题，假设⼀个接⼝提供了两个参数。

@RestController public class FileController {

@PostMapping("/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

void upload(@RequestParam("message") String message, @RequestPart("attachment") MultipartFile

file); } 但使⽤时未提供MultipartFile类型参数。 @FeignClient(value = "cms-service") public interface CmsService {

@PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

void upload(@RequestParam("message") String message,

); } 将会报如下错误： org.apache.tomcat.util.http.fileupload.FileUploadException: the request was rejected because no multipart boundary was found 这是因为Feign只有存在MultipartFile类型参数时才会设置 boundary。在Feign的 MultipartFormContentProcessor 中，其中有⼀点就专⻔是⽤来添加boundary。

public void process(...) throws EncodeException { String boundary = long.toHexString(System.currentTimeMillis());

# ...

output.write("--").write(boundary).write("-").write("rn");

String contentTypeHeaderValue =

this.getSupportedContentType().getHeader() + "; charset=" + charset.name() + "; boundary=" + boundary;

template.header("Content-Type", new String[] {contentTypeHeaderValue}); } 如果⾃⼰处理的话，可以在 RequestInterceptor 的实现类中模拟上⾯的⽅法，为 multipart/form-data 格式⾃定义⼀个boundary。

⼩结 本⽂是⼀遍⼯具使⽤帖，⼩结⼀下，传⽂件注意⼏个点：

Feign 不⽀持多个body参数，body参数也不能为空

特别注意 feign-form 的版本

若对Feign源码感兴趣，可看看 Spring Cloud 源码学习之 Feign。

![image 2](<Spring Cloud Feign 上传文件的常见问题.note_images/imageFile2.png>)

