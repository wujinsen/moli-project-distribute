RestController对应代码：

@RequestMapping(value = "/info/email/{email}",method = RequestMethod.GET)

public User getInfoByEmail(@PathVariable String email){ User user = userRepository.findByEmail(email); return user;

}

请求方式： http://localhost:8080/api/v1/user/info/email/

dujc1018@gmail.com

这里给个全局的解决方案： import org.springframework.context.annotation.Configuration; import org.springframework.web.servlet.config.annotation.PathMatchConfigurer; import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**

- * Created by Administrator on 2017/7/10 0010.

- */


@Configuration public class WebConfig extends WebMvcConfigurerAdapter {

@Override public void configurePathMatch(PathMatchConfigurer configurer) {

configurer.setUseSuffixPatternMatch(false); }

}

通过这个问题，发现需要对WebMvcConfigurerAdapter做一些了解，一些问题可以从源头就解决掉

htp:/blog.netgl o.com/2015/05/19/spring-bot-avoid-pathvariable-parameters-geting-truncated

-on-dots/

个⼈⽤这个没效果

