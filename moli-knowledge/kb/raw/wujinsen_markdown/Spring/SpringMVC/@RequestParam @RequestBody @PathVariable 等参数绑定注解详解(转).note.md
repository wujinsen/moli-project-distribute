# 引⾔：

接上⼀篇⽂章，对@RequestMapping进⾏地址映射讲解之后，该篇主要讲解request 数据到handler method 参数数据的绑定所⽤到的注解和什么情形下使⽤；

# 简介：

handler method 参数绑定常⽤的注解,我们根据他们处理的Request的不同内容部分分为四类：（主要 讲解常⽤类型）

- A、处理requet uri 部分（这⾥指uri template中variable，不含queryString部分）的注解： @PathVariable;

- B、处理request header部分的注解： @RequestHeader, @CookieValue;

- C、处理request body部分的注解：@RequestParam, @RequestBody;

- D、处理attribute类型是注解： @SessionAttributes, @ModelAttribute;


- 1、 @PathVariable 当使⽤@RequestMapping URI template 样式映射时， 即 someUrl/{paramId}, 这时的paramId可通 过 @Pathvariable注解绑定它传过来的值到⽅法的参数上。 示例代码：

@Controller @RequestMapping("/owners/{ownerId}") public class RelativePathUriTemplateController {

@RequestMapping("/pets/{petId}") public void findPet(@PathVariable String ownerId, @PathVariable String petId, Model model) {

// implementation omitted }

}

上⾯代码把URI template 中变量 ownerId的值和petId的值，绑定到⽅法的参数上。若⽅法参数名称和 需要绑定的uri template中变量名称不⼀致，需要在@PathVariable("name")指定uri template中的名 称。

- 2、 @RequestHeader、@CookieValue @RequestHeader 注解，可以把Request请求header部分的值绑定到⽅法的参数上。 示例代码：


![image 1](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile1.png>)

![image 2](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile2.png>)

这是⼀个Request 的header部分：

Host localhost:8080 Accept text/html,application/xhtml+xml,application/xml;q=0.9 Accept-Language fr,en-gb;q=0.7,en;q=0.3 Accept-Encoding gzip,deflate Accept-Charset ISO-8859-1,utf-8;q=0.7,*;q=0.7 Keep-Alive 300

![image 3](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile3.png>)

@RequestMapping("/displayHeaderInfo.do") public void displayHeaderInfo(@RequestHeader("Accept-Encoding") String encoding,

@RequestHeader("Keep-Alive") long keepAlive) {

//...

}

![image 4](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile4.png>)

上⾯的代码，把request header部分的 Accept-Encoding的值，绑定到参数encoding上了， KeepAlive header的值绑定到参数keepAlive上。

@CookieValue 可以把Request header中关于cookie的值绑定到⽅法的参数上。 例如有如下Cookie值：

JSESSIONID=415A4AC178C59DACE0B2C9CA727CDD84

参数绑定的代码：

@RequestMapping("/displayHeaderInfo.do") public void displayHeaderInfo(@CookieValue("JSESSIONID") String cookie) {

//...

}

即把JSESSIONID的值绑定到参数cookie上。

- 3、@RequestParam, @RequestBody @RequestParam


- A） 常⽤来处理简单类型的绑定，通过Request.getParameter() 获取的String可直接转换为简单类型 的情况（ String--> 简单类型的转换操作由ConversionService配置的转换器来完成）；因为使⽤ request.getParameter()⽅式获取参数，所以可以处理get ⽅式中queryString的值，也可以处理post ⽅式中 body data的值；

- B）⽤来处理Content-Type: 为 application/x-www-form-urlencoded编码的内容，提交⽅式GET、 POST； C) 该注解有两个属性： value、required； value⽤来指定要传⼊值的id名称，required⽤来指示参数 是否必须绑定； 示例代码：


![image 5](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile5.png>)

@Controller @RequestMapping("/pets") @SessionAttributes("pet") public class EditPetForm {

// ...

@RequestMapping(method = RequestMethod.GET) public String setupForm(@RequestParam("petId") int petId, ModelMap model) {

Pet pet = this.clinic.loadPet(petId); model.addAttribute("pet", pet); return "petForm";

}

// ...

![image 6](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile6.png>)

@RequestBody 该注解常⽤来处理Content-Type: 不是application/x-www-form-urlencoded编码的内容，例如 application/json, application/xml等； 它是通过使⽤HandlerAdapter 配置的HttpMessageConverters来解析post data body，然后绑定到相应 的bean上的。 因为配置有FormHttpMessageConverter，所以也可以⽤来处理 application/x-www-form-urlencoded 的内容，处理完的结果放在⼀个MultiValueMap<String, String>⾥，这种情况在某些特殊需求下使 ⽤，详情查看FormHttpMessageConverter api; 示例代码：

@RequestMapping(value = "/something", method = RequestMethod.PUT) public void handle(@RequestBody String body, Writer writer) throws IOException {

writer.write(body); }

- 4、@SessionAttributes, @ModelAttribute @SessionAttributes: 该注解⽤来绑定HttpSession中的attribute对象的值，便于在⽅法中的参数⾥使⽤。 该注解有value、types两个属性，可以通过名字和类型指定要使⽤的attribute 对象；


示例代码：

@Controller @RequestMapping("/editPet.do") @SessionAttributes("pet") public class EditPetForm {

// ... }

@ModelAttribute 该注解有两个⽤法，⼀个是⽤于⽅法上，⼀个是⽤于参数上； ⽤于⽅法上时： 通常⽤来在处理@RequestMapping之前，为请求绑定需要从后台查询的model； ⽤于参数上时： ⽤来通过名称对应，把相应名称的值绑定到注解的参数bean上；要绑定的值来源于：

- A） @SessionAttributes 启⽤的attribute 对象上；

- B） @ModelAttribute ⽤于⽅法上时指定的model对象；


- C） 上述两种情况都没有时，new⼀个需要绑定的bean对象，然后把request中按名称对应的⽅式把值 绑定到bean中。


⽤到⽅法上@ModelAttribute的示例代码：

![image 7](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile7.png>)

// Add one attribute // The return value of the method is added to the model under the name "account" // You can customize the name via @ModelAttribute("myAccount")

@ModelAttribute public Account addAccount(@RequestParam String number) {

return accountManager.findAccount(number); }

![image 8](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile8.png>)

这种⽅式实际的效果就是在调⽤@RequestMapping的⽅法之前，为request对象的model⾥put （“account”， Account）；

⽤在参数上的@ModelAttribute示例代码：

@RequestMapping(value="/owners/{ownerId}/pets/{petId}/edit", method = RequestMethod.POST) public String processSubmit(@ModelAttribute Pet pet) {

}

⾸先查询 @SessionAttributes有⽆绑定的Pet对象，若没有则查询@ModelAttribute⽅法层⾯上是否绑 定了Pet对象，若没有则将URI template中的值按对应的名称绑定到Pet对象的各属性上。

# 补充讲解：

问题： 在不给定注解的情况下，参数是怎样绑定的？ 通过分析AnnotationMethodHandlerAdapter和RequestMappingHandlerAdapter的源代码发现，⽅法 的参数在不给定参数的情况下： 若要绑定的对象时简单类型： 调⽤@RequestParam来处理的。 若要绑定的对象时复杂类型： 调⽤@ModelAttribute来处理的。 这⾥的简单类型指java的原始类型(boolean, int 等)、原始类型对象（Boolean, Int等）、String、Date 等ConversionService⾥可以直接String转换成⽬标对象的类型；

下⾯贴出AnnotationMethodHandlerAdapter中绑定参数的部分源代码：

![image 9](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile9.png>)

private Object[] resolveHandlerArguments(Method handlerMethod, Object handler, NativeWebRequest webRequest, ExtendedModelMap implicitModel) throws Exception {

Class[] paramTypes = handlerMethod.getParameterTypes(); Object[] args = new Object[paramTypes.length];

for (int i = 0; i < args.length; i++) { MethodParameter methodParam = new MethodParameter(handlerMethod, i); methodParam.initParameterNameDiscovery(this.parameterNameDiscoverer); GenericTypeResolver.resolveParameterType(methodParam, handler.getClass()); String paramName = null; String headerName = null; boolean requestBodyFound = false; String cookieName = null; String pathVarName = null; String attrName = null; boolean required = false; String defaultValue = null; boolean validate = false; Object[] validationHints = null; int annotationsFound = 0; Annotation[] paramAnns = methodParam.getParameterAnnotations();

for (Annotation paramAnn : paramAnns) {

if (RequestParam.class.isInstance(paramAnn)) { RequestParam requestParam = (RequestParam) paramAnn; paramName = requestParam.value(); required = requestParam.required(); defaultValue = parseDefaultValueAttribute(requestParam.defaultValue()); annotationsFound++;

} else if (RequestHeader.class.isInstance(paramAnn)) {

RequestHeader requestHeader = (RequestHeader) paramAnn; headerName = requestHeader.value(); required = requestHeader.required(); defaultValue = parseDefaultValueAttribute(requestHeader.defaultValue()); annotationsFound++;

} else if (RequestBody.class.isInstance(paramAnn)) {

requestBodyFound = true; annotationsFound++;

} else if (CookieValue.class.isInstance(paramAnn)) {

CookieValue cookieValue = (CookieValue) paramAnn; cookieName = cookieValue.value(); required = cookieValue.required(); defaultValue = parseDefaultValueAttribute(cookieValue.defaultValue()); annotationsFound++;

else if (PathVariable.class.isInstance(paramAnn)) { PathVariable pathVar = (PathVariable) paramAnn; pathVarName = pathVar.value(); annotationsFound++;

} else if (ModelAttribute.class.isInstance(paramAnn)) {

ModelAttribute attr = (ModelAttribute) paramAnn; attrName = attr.value(); annotationsFound++;

} else if (Value.class.isInstance(paramAnn)) {

defaultValue = ((Value) paramAnn).value();

} else if (paramAnn.annotationType().getSimpleName().startsWith("Valid")) {

validate = true; Object value = AnnotationUtils.getValue(paramAnn); validationHints = (value instanceof Object[] ? (Object[]) value : new Object[]

{value});

} }

if (annotationsFound > 1) {

throw new IllegalStateException("Handler parameter annotations are exclusive choices - " +

"do not specify more than one such annotation on the same parameter: " + handlerMethod);

}

if (annotationsFound == 0) {// 若没有发现注解

Object argValue = resolveCommonArgument(methodParam, webRequest); //判断WebRquest 是否可赋值给参数

if (argValue != WebArgumentResolver.UNRESOLVED) { args[i] = argValue;

} else if (defaultValue != null) {

args[i] = resolveDefaultValue(defaultValue);

} else {

Class<?> paramType = methodParam.getParameterType(); if (Model.class.isAssignableFrom(paramType) ||

Map.class.isAssignableFrom(paramType)) { if (!paramType.isAssignableFrom(implicitModel.getClass())) {

throw new IllegalStateException("Argument [" + paramType.getSimpleName() + "] is of type " +

"Model or Map but is not assignable from the actual model. You may need to switch " +

"newer MVC infrastructure classes to use this argument.");

} args[i] = implicitModel;

} else if (SessionStatus.class.isAssignableFrom(paramType)) {

args[i] = this.sessionStatus;

} else if (HttpEntity.class.isAssignableFrom(paramType)) {

args[i] = resolveHttpEntityRequest(methodParam, webRequest);

} else if (Errors.class.isAssignableFrom(paramType)) {

throw new IllegalStateException("Errors/BindingResult argument declared " +

"without preceding model attribute. Check your handler method signature!");

} else if (BeanUtils.isSimpleProperty(paramType)) {// 判断是否参数类型是否是简单类型，

若是在使⽤@RequestParam⽅式来处理,否则使⽤@ModelAttribute⽅式处理 paramName = "";

} else {

attrName = ""; }

} }

if (paramName != null) { args[i] = resolveRequestParam(paramName, required, defaultValue, methodParam,

webRequest, handler); } else if (headerName != null) {

args[i] = resolveRequestHeader(headerName, required, defaultValue, methodParam,

webRequest, handler); } else if (requestBodyFound) {

args[i] = resolveRequestBody(methodParam, webRequest, handler);

} else if (cookieName != null) {

args[i] = resolveCookieValue(cookieName, required, defaultValue, methodParam,

webRequest, handler); } else if (pathVarName != null) {

args[i] = resolvePathVariable(pathVarName, methodParam, webRequest, handler);

} else if (attrName != null) {

WebDataBinder binder =

resolveModelAttribute(attrName, methodParam, implicitModel, webRequest, handler);

boolean assignBindingResult = (args.length > i + 1 && Errors.class.isAssignableFrom(paramTypes[i + 1]));

if (binder.getTarget() != null) { doBind(binder, webRequest, validate, validationHints, !assignBindingResult);

args[i] = binder.getTarget(); if (assignBindingResult) {

args[i + 1] = binder.getBindingResult(); i++;

} implicitModel.putAll(binder.getBindingResult().getModel());

} }

return args; }

![image 10](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile10.png>)

RequestMappingHandlerAdapter中使⽤的参数绑定，代码稍微有些不同，有兴趣的同仁可以分析 下，最后处理的结果都是⼀样的。

示例：

![image 11](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile11.png>)

@RequestMapping ({"/", "/home"}) public String showHomePage(String key){

logger.debug("key="+key);

return "home"; }

![image 12](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile12.png>)

这种情况下，就调⽤默认的@RequestParam来处理。

![image 13](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile13.png>)

@RequestMapping (method = RequestMethod.POST) public String doRegister(User user){ if(logger.isDebugEnabled()){ logger.debug("process url[/user], method[post] in "+getClass()); logger.debug(user);

}

return "user"; }

![image 14](<@RequestParam @RequestBody @PathVariable 等参数绑定注解详解(转).note_images/imageFile14.png>)

这种情况下，就调⽤@ModelAttribute来处理。

# 参考⽂档：

1、 Spring Web Doc： spring-3.1.0/docs/spring-framework-reference/html/mvc.html

