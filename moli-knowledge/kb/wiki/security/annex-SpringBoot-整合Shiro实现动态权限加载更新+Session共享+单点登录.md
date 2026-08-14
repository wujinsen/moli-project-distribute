---
title: SpringBoot 整合Shiro实现动态权限加载更新+Session共享+单点登录.note（原文插图 annex）
slug: annex-SpringBoot-整合Shiro实现动态权限加载更新+Session共享+单点登录
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/安全框架/shiro/SpringBoot 整合Shiro实现动态权限加载更新+Session共享+单点登录.note.md
related: [shiro-鉴权体系]
created: 2026-07-05
updated: 2026-07-05
---

htps:/juejin.cn/post/684903872171868174

# SpringBot整合Shiro实现动态权限加载更新+Sesion共享+单 点登录

## ⼀.说明

Shiro是⼀个安全框架,项⽬中主要⽤它做认证,授权,加密,以及⽤户的会话管理,虽然Shiro没有SpringSecurity功 能更丰富,但是它轻量,简单,在项⽬中通常业务需求Shiro也都能胜任.

## ⼆.项⽬环境

MyBatis-Plus版本: 3.1.0 SpringBot版本:2.1.5 JDK版本:1.8 Shiro版本:1.4 Shiro-redis插件版本:3.1.0 数据表(SQL⽂件在项⽬中):数据库中测试号的密码进⾏了加密,密码皆为123456

<table>
  <tr>
    <th>数据表名</th>
    <th>中⽂表名</th>
    <th>备注说明</th>
  </tr>
  <tr>
    <td>sys_user</td>
    <td>系统⽤户表</td>
    <td>基础表</td>
  </tr>
  <tr>
    <td>sys_menu</td>
    <td>权限表</td>
    <td>基础表</td>
  </tr>
  <tr>
    <td>sys_role</td>
    <td>⻆⾊表</td>
    <td>基础表</td>
  </tr>
  <tr>
    <td>sys_role_menu</td>
    <td>⻆⾊与权限关系表</td>
    <td>中间表</td>
  </tr>
  <tr>
    <td> </td>
    <td>⽤户与⻆⾊关系表</td>
    <td>中间表</td>
  </tr>
</table>


sys_user_role

Maven依赖如下：

<dependencies>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-web</artifactId>

</dependency> <dependency>

<groupId>mysql</groupId> <artifactId>mysql-connector-java</artifactId> <scope>runtime</scope>

</dependency> <!-- AOP依 赖 ,⼀ 定 要 加 ,否 则 权 限 拦截 验 证 不 ⽣ 效 --> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-aop</artifactId>

</dependency> <!-- lombok插 件 --> <dependency>

<groupId>org.projectlombok</groupId> <artifactId>lombok</artifactId> <optional>true</optional>

</dependency> <!-- Redis --> <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-data-redis-reactive</artifactId>

</dependency> <!-- mybatisPlus 核 ⼼ 库 --> <dependency>

<groupId>com.baomidou</groupId> <artifactId>mybatis-plus-boot-starter</artifactId> <version>3.1.0</version>

</dependency> <!-- 引 ⼊ 阿 ⾥ 数据 库 连 接 池 --> <dependency>

<groupId>com.alibaba</groupId> <artifactId>druid</artifactId> <version>1.1.6</version>

</dependency> <!-- Shiro 核 ⼼ 依 赖 --> <dependency>

<groupId>org.apache.shiro</groupId> <artifactId>shiro-spring</artifactId> <version>1.4.0</version>

</dependency> <!-- Shiro-redis插 件 --> <dependency>

<groupId>org.crazycake</groupId> <artifactId>shiro-redis</artifactId> <version>3.1.0</version>

</dependency> <!-- StringUtilS⼯ 具 --> <dependency>

<groupId>org.apache.commons</groupId> <artifactId>commons-lang3</artifactId> <version>3.5</version>

</dependency> </dependencies> 复制代码

配置如下:

# 配 置 端 ⼝ server:

port: 8764

spring: # 配 置 数据 源 datasource:

driver-class-name: com.mysql.cj.jdbc.Driver url: jdbc:mysql://localhost:3306/my_shiro?

serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false

username: root password: root type: com.alibaba.druid.pool.DruidDataSource

# Redis数据 源 redis:

host: localhost port: 6379 timeout: 6000 password: 123456 jedis:

pool: max-active: 1000 # 连 接 池 最 ⼤ 连 接数 （ 使 ⽤ 负 值 表 示 没 有 限 制 ） max-wait: -1 # 连 接 池 最 ⼤ 阻 塞 等 待 时 间 （ 使 ⽤ 负 值 表 示 没 有 限 制 ） max-idle: 10 # 连 接 池 中 的 最 ⼤ 空 闲 连 接 min-idle: 5 # 连 接 池 中 的 最 ⼩ 空 闲 连 接

# mybatis-plus相 关 配 置 mybatis-plus:

# xml扫 描 ， 多 个 ⽬ 录 ⽤ 逗 号 或 者 分 号 分 隔 （ 告 诉 Mapper 所 对 应 的 XML ⽂ 件位 置 ） mapper-locations: classpath:mapper/*.xml # 以 下 配 置 均 有 默 认 值 ,可 以 不 设 置 global-config: db-config:

#主 键 类 型 AUTO:"数据 库 ID⾃ 增 " INPUT:"⽤ 户 输 ⼊ ID",ID_WORKER:"全 局 唯 ⼀ ID (数 字 类 型唯 ⼀ ID)", UUID:"全 局 唯

⼀ ID UUID"; id-type: auto #字 段 策 略 IGNORED:"忽 略 判 断 " NOT_NULL:"⾮ NULL 判 断 ") NOT_EMPTY:"⾮ 空 判 断 " field-strategy: NOT_EMPTY #数据 库 类 型 db-type: MYSQL

configuration: # 是 否 开 启 ⾃ 动 驼 峰 命名 规 则 映 射 :从 数据 库 列 名 到 Java属 性 驼 峰 命名 的 类 似 映 射 map-underscore-to-camel-case: true # 返 回 map时 true:当 查 询 数据 为 空 时 字 段 返 回 为 null,false:不 加 这 个 查 询 数据 为 空 时 ， 字 段 将 被 隐 藏 call-setters-on-nulls: true # 这 个 配 置 会 将 执 ⾏ 的 sql打 印 出 来 ， 在 开 发 或 测 试 的 时 候 可 以 ⽤ log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

复制代码

## ⼆.编写项⽬基础类

⽤户实体,Dao,Service等在这⾥省略,请参考源码 编写Exception类来处理Shiro权限拦截异常

/**

- * @Description ⾃ 定 义 异常

- * @Author Sans

- * @CreateTime 2019/6/15 22:56

- */


#### @ControllerAdvice public class MyShiroException {

/**

- * 处 理 Shiro权 限 拦截 异常

- * 如 果 返 回 JSON数据 格 式 请 加 上 @ResponseBody注 解

- * @Author Sans

- * @CreateTime 2019/6/15 13:35

- * @Return Map<Object> 返 回 结 果 集

- */


#### @ResponseBody @ExceptionHandler(value = AuthorizationException.class) public Map<String,Object> defaultErrorHandler(){

Map<String,Object> map = new HashMap<>(); map.put("403","权限不⾜"); return map;

}

} 复制代码

创建SHA256Util加密⼯具

/**

- * @Description Sha-256加 密 ⼯ 具

- * @Author Sans

- * @CreateTime 2019/6/12 9:27

- */


public class SHA256Util { /** 私 有构 造 器 **/ private SHA256Util(){}; /** 加 密 算 法 **/ public final static String HASH_ALGORITHM_NAME = "SHA-256"; /** 循 环 次 数 **/ public final static int HASH_ITERATIONS = 15; /** 执 ⾏ 加 密 -采 ⽤ SHA256和 盐 值 加 密 **/ public static String sha256(String password, String salt) {

return new SimpleHash(HASH_ALGORITHM_NAME, password, salt, HASH_ITERATIONS).toString(); }

} 复制代码

创建Spring⼯具

- * @Description Spring上下 ⽂ ⼯ 具 类

- * @Author Sans

- * @CreateTime 2019/6/17 13:40

- */


#### @Component public class SpringUtil implements ApplicationContextAware {

private static ApplicationContext context; /**

- * Spring在 bean初 始 化 后 会 判 断 是 不 是 ApplicationContextAware的 ⼦ 类

- * 如 果 该 类 是 ,setApplicationContext()⽅ 法 ,会 将容 器 中 ApplicationContext作为 参 数 传 ⼊ 进 去

- * @Author Sans

- * @CreateTime 2019/6/17 16:58

- */


@Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException

{

context = applicationContext;

} /**

- * 通过 Name返 回 指 定 的 Bean

- * @Author Sans

- * @CreateTime 2019/6/17 16:03

- */


public static <T> T getBean(Class<T> beanClass) {

return context.getBean(beanClass); }

} 复制代码

创建Shiro⼯具

- * @Description Shiro⼯ 具 类

- * @Author Sans

- * @CreateTime 2019/6/15 16:11

- */


public class ShiroUtils {

/** 私 有构 造 器 **/ private ShiroUtils(){}

private static RedisSessionDAO redisSessionDAO = SpringUtil.getBean(RedisSessionDAO.class);

/**

- * 获 取 当 前 ⽤ 户 Session

- * @Author Sans

- * @CreateTime 2019/6/17 17:03

- * @Return SysUserEntity ⽤ 户 信 息

- */


#### public static Session getSession() {

return SecurityUtils.getSubject().getSession(); }

/**

- * ⽤ 户 登 出

- * @Author Sans

- * @CreateTime 2019/6/17 17:23

- */


#### public static void logout() {

SecurityUtils.getSubject().logout(); }

/**

- * 获 取 当 前 ⽤ 户 信 息

- * @Author Sans

- * @CreateTime 2019/6/17 17:03

- * @Return SysUserEntity ⽤ 户 信 息

- */ public static SysUserEntity getUserInfo() {


return (SysUserEntity) SecurityUtils.getSubject().getPrincipal(); }

/**

- * 删 除 ⽤ 户 缓 存 信 息

- * @Author Sans

- * @CreateTime 2019/6/17 13:57

- * @Param username ⽤ 户 名 称

- * @Param isRemoveSession 是 否 删 除 Session

- * @Return void

- */


public static void deleteCache(String username, boolean isRemoveSession){ //从 缓 存 中 获 取 Session Session session = null; Collection<Session> sessions = redisSessionDAO.getActiveSessions(); SysUserEntity sysUserEntity; Object attribute = null; for(Session sessionInfo : sessions){

//遍 历 Session,找 到 该 ⽤ 户 名 称 对 应 的 Session attribute = sessionInfo.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY); if (attribute == null) {

#### continue;

} sysUserEntity = (SysUserEntity) ((SimplePrincipalCollection)

attribute).getPrimaryPrincipal();

if (sysUserEntity == null) { continue;

} if (Objects.equals(sysUserEntity.getUsername(), username)) {

session=sessionInfo; break;

}

} if (session == null||attribute == null) {

#### return;

} //删 除 session if (isRemoveSession) {

redisSessionDAO.delete(session);

} //删 除 Cache， 在 访 问 受 限 接 ⼝ 时 会 重 新授 权 DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager)

SecurityUtils.getSecurityManager(); Authenticator authc = securityManager.getAuthenticator(); ((LogoutAware) authc).onLogout((SimplePrincipalCollection) attribute);

}

} 复制代码

创建Shiro的SesionId⽣成器

- * @Description ⾃ 定 义 SessionId⽣ 成 器

- * @Author Sans

- * @CreateTime 2019/6/11 11:48

- */


#### public class ShiroSessionIdGenerator implements SessionIdGenerator {

/**

- * 实 现 SessionId⽣ 成

- * @Author Sans

- * @CreateTime 2019/6/11 11:54

- */


@Override public Serializable generateId(Session session) {

Serializable sessionId = new JavaUuidSessionIdGenerator().generateId(session); return String.format("login_token_%s", sessionId);

}

} 复制代码

## 三.编写Shiro核⼼类

创建Realm⽤于授权和认证

- * @Description Shiro权 限 匹 配 和 账 号 密 码 匹 配

- * @Author Sans

- * @CreateTime 2019/6/15 11:27

- */


public class ShiroRealm extends AuthorizingRealm { @Autowired private SysUserService sysUserService; @Autowired private SysRoleService sysRoleService; @Autowired private SysMenuService sysMenuService; /**

- * 授 权权 限

- * ⽤ 户 进 ⾏ 权 限 验 证 时 候 Shiro会 去 缓 存 中 找 ,如 果查 不 到 数据 ,会 执 ⾏ 这 个 ⽅ 法 去 查权 限 ,并 放 ⼊ 缓 存 中

- * @Author Sans

- * @CreateTime 2019/6/12 11:44

- */


@Override protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo(); SysUserEntity sysUserEntity = (SysUserEntity) principalCollection.getPrimaryPrincipal(); //获 取 ⽤ 户 ID Long userId =sysUserEntity.getUserId(); //这 ⾥ 可 以 进 ⾏ 授 权 和 处 理

Set<String> rolesSet = new HashSet<>(); Set<String> permsSet = new HashSet<>(); //查 询 ⻆ ⾊ 和 权 限 (这 ⾥ 根 据 业 务 ⾃ ⾏ 查 询 )

List<SysRoleEntity> sysRoleEntityList = sysRoleService.selectSysRoleByUserId(userId); for (SysRoleEntity sysRoleEntity:sysRoleEntityList) {

rolesSet.add(sysRoleEntity.getRoleName()); List<SysMenuEntity> sysMenuEntityList =

sysMenuService.selectSysMenuByRoleId(sysRoleEntity.getRoleId());

for (SysMenuEntity sysMenuEntity :sysMenuEntityList) {

permsSet.add(sysMenuEntity.getPerms()); }

} //将 查 到 的 权 限 和 ⻆ ⾊ 分别 传 ⼊ authorizationInfo中 authorizationInfo.setStringPermissions(permsSet); authorizationInfo.setRoles(rolesSet); return authorizationInfo;

}

/**

- * 身 份 认证

- * @Author Sans

- * @CreateTime 2019/6/12 12:36

- */


#### @Override

protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)

throws AuthenticationException { //获 取 ⽤ 户 的 输 ⼊ 的 账 号 . String username = (String) authenticationToken.getPrincipal(); //通过 username从 数据 库 中 查 找 User对 象 ， 如 果 找 到 进 ⾏ 验 证 //实 际项 ⽬ 中 ,这 ⾥ 可 以 根 据 实 际 情 况做 缓 存 ,如 果 不 做 ,Shiro⾃ ⼰ 也 是有时 间间隔 机 制 ,2分 钟 内 不 会 重 复 执 ⾏ 该 ⽅ 法 SysUserEntity user = sysUserService.selectUserByName(username); //判 断 账 号 是 否 存 在 if (user == null) {

throw new AuthenticationException();

} //判 断 账 号 是 否 被 冻 结 if (user.getState()==null||user.getState().equals("PROHIBIT")){

throw new LockedAccountException();

} //进 ⾏ 验 证 SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(

user, //⽤ 户 名 user.getPassword(), //密 码 ByteSource.Util.bytes(user.getSalt()), //设 置 盐 值 getName()

); //验 证 成 功 开 始 踢 ⼈ (清 除 缓 存 和 Session) ShiroUtils.deleteCache(username,true); return authenticationInfo;

}

} 复制代码

创建SesionManager类

- * @Description ⾃ 定 义 获 取 Token

- * @Author Sans

- * @CreateTime 2019/6/13 8:34

- */


public class ShiroSessionManager extends DefaultWebSessionManager { //定 义 常 量 private static final String AUTHORIZATION = "Authorization"; private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request"; //重 写 构 造 器 public ShiroSessionManager() {

super(); this.setDeleteInvalidSessions(true);

} /**

- * 重 写 ⽅ 法 实 现 从 请 求 头 获 取 Token便于 接 ⼝ 统 ⼀

- * 每次 请 求 进 来 ,Shiro会 去 从 请 求 头 找 Authorization这 个 key对 应 的 Value(Token)

- * @Author Sans

- * @CreateTime 2019/6/13 8:47

- */


@Override public Serializable getSessionId(ServletRequest request, ServletResponse response) {

String token = WebUtils.toHttp(request).getHeader(AUTHORIZATION); //如 果 请 求 头 中 存 在 token 则 从 请 求 头 中 获 取 token if (!StringUtils.isEmpty(token)) {

request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,

REFERENCED_SESSION_ID_SOURCE); request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, token); request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID,

Boolean.TRUE);

return token;

} else { // 这 ⾥ 禁 ⽤ 掉 Cookie获 取 ⽅ 式 // 按 默 认规 则 从 Cookie取 Token // return super.getSessionId(request, response); return null;

} }

} 复制代码

创建ShiroConfig配置类

- * @Description Shiro配 置类

- * @Author Sans

- * @CreateTime 2019/6/10 17:42

- */ @Configuration public class ShiroConfig {


private final String CACHE_KEY = "shiro:cache:"; private final String SESSION_KEY = "shiro:session:";

//Redis配 置 @Value("${spring.redis.host}") private String host; @Value("${spring.redis.port}") private int port; @Value("${spring.redis.timeout}") private int timeout; @Value("${spring.redis.password}") private String password;

/**

- * 开 启 Shiro-aop注 解 ⽀ 持

- * @Attention 使 ⽤ 代 理 ⽅ 式 所 以 需 要 开 启 代 码 ⽀ 持

- * @Author Sans

- * @CreateTime 2019/6/12 8:38

- */


@Bean public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager

securityManager) { AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new

AuthorizationAttributeSourceAdvisor(); authorizationAttributeSourceAdvisor.setSecurityManager(securityManager); return authorizationAttributeSourceAdvisor;

}

/**

- * Shiro基 础 配 置

- * @Author Sans

- * @CreateTime 2019/6/12 8:42

- */


@Bean public ShiroFilterFactoryBean shiroFilterFactory(SecurityManager securityManager){

ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean(); shiroFilterFactoryBean.setSecurityManager(securityManager); Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>(); // 注 意 过 滤 器 配 置 顺 序 不 能 颠 倒 // 配 置 过 滤 :不 会 被 拦截 的 链 接 filterChainDefinitionMap.put("/static/**", "anon");

filterChainDefinitionMap.put("/userLogin/**", "anon"); filterChainDefinitionMap.put("/**", "authc"); // 配 置 shiro默 认 登 录 界 ⾯ 地址 ， 前 后 端 分 离 中 登 录 界 ⾯ 跳 转 应 由 前 端 路 由 控 制 ， 后台 仅 返 回 json数据 shiroFilterFactoryBean.setLoginUrl("/userLogin/unauth"); shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap); return shiroFilterFactoryBean;

}

/**

- * 安 全 管 理 器

- * @Author Sans

- * @CreateTime 2019/6/12 10:34

- */ @Bean public SecurityManager securityManager() {


DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(); // ⾃ 定 义 Ssession管 理 securityManager.setSessionManager(sessionManager()); // ⾃ 定 义 Cache实 现 securityManager.setCacheManager(cacheManager()); // ⾃ 定 义 Realm验 证 securityManager.setRealm(shiroRealm()); return securityManager;

}

/**

- * 身 份 验 证 器

- * @Author Sans

- * @CreateTime 2019/6/12 10:37

- */


@Bean public ShiroRealm shiroRealm() {

ShiroRealm shiroRealm = new ShiroRealm(); shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher()); return shiroRealm;

}

/**

- * 凭 证 匹 配 器

- * 将密 码 校 验 交 给 Shiro的 SimpleAuthenticationInfo进 ⾏ 处 理 ,在 这 ⾥ 做 匹 配配 置

- * @Author Sans

- * @CreateTime 2019/6/12 10:48

- */


@Bean public HashedCredentialsMatcher hashedCredentialsMatcher() {

HashedCredentialsMatcher shaCredentialsMatcher = new HashedCredentialsMatcher(); // 散 列 算 法 :这 ⾥ 使 ⽤ SHA256算 法 ; shaCredentialsMatcher.setHashAlgorithmName(SHA256Util.HASH_ALGORITHM_NAME); // 散 列 的 次 数 ， ⽐ 如 散 列 两 次 ， 相 当 于 md5(md5(""));

shaCredentialsMatcher.setHashIterations(SHA256Util.HASH_ITERATIONS); return shaCredentialsMatcher;

}

/**

- * 配 置 Redis管 理 器

- * @Attention 使 ⽤ 的 是 shiro-redis开 源 插 件

- * @Author Sans

- * @CreateTime 2019/6/12 11:06

- */


@Bean public RedisManager redisManager() {

RedisManager redisManager = new RedisManager(); redisManager.setHost(host); redisManager.setPort(port); redisManager.setTimeout(timeout); redisManager.setPassword(password); return redisManager;

}

/**

- * 配 置 Cache管 理 器

- * ⽤ 于 往 Redis存 储 权 限 和 ⻆ ⾊ 标 识

- * @Attention 使 ⽤ 的 是 shiro-redis开 源 插 件

- * @Author Sans

- * @CreateTime 2019/6/12 12:37

- */


@Bean public RedisCacheManager cacheManager() {

RedisCacheManager redisCacheManager = new RedisCacheManager(); redisCacheManager.setRedisManager(redisManager()); redisCacheManager.setKeyPrefix(CACHE_KEY); // 配 置缓 存 的 话要 求 放 在 session⾥ ⾯ 的 实 体 类 必 须 有 个 id标 识 redisCacheManager.setPrincipalIdFieldName("userId"); return redisCacheManager;

}

/**

- * SessionID⽣ 成 器

- * @Author Sans

- * @CreateTime 2019/6/12 13:12

- */


@Bean public ShiroSessionIdGenerator sessionIdGenerator(){

return new ShiroSessionIdGenerator(); }

/**

- * 配 置 RedisSessionDAO


- * @Attention 使 ⽤ 的 是 shiro-redis开 源 插 件

- * @Author Sans

- * @CreateTime 2019/6/12 13:44

- */


@Bean public RedisSessionDAO redisSessionDAO() {

RedisSessionDAO redisSessionDAO = new RedisSessionDAO(); redisSessionDAO.setRedisManager(redisManager()); redisSessionDAO.setSessionIdGenerator(sessionIdGenerator()); redisSessionDAO.setKeyPrefix(SESSION_KEY); redisSessionDAO.setExpire(timeout); return redisSessionDAO;

}

/**

- * 配 置 Session管 理 器

- * @Author Sans

- * @CreateTime 2019/6/12 14:25

- */


@Bean public SessionManager sessionManager() {

ShiroSessionManager shiroSessionManager = new ShiroSessionManager(); shiroSessionManager.setSessionDAO(redisSessionDAO()); return shiroSessionManager;

}

} 复制代码

## 四.实现权限控制

Shiro可以⽤代码或者注解来控制权限,通常我们使⽤注解控制,不仅简单⽅便,⽽且更加灵活.Shiro注解⼀共有五 个:

<table>
  <tr>
    <th>注解名称</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>RequiresAuthentication</td>
    <td>使⽤该注解标注的类,⽅法等在访问时,当前</td>
  </tr>
  <tr>
    <td>RequiresGuest</td>
    <td>Subject必须在当前sesion中已经过认证. 使⽤该注解标注的类,⽅法等在访问时,当前 Subject可以是“gust”身份,不需要经过认证或者在</td>
  </tr>
  <tr>
    <td>RequiresUser</td>
    <td>原先的sesion中存在记录. 验证⽤户是否被记忆,有两种含义:⼀种是成功登录 的(subject.isAuthenticated()结果为true);另外⼀ 种是被记忆的(subject.isRemembered()结果为</td>
  </tr>
  <tr>
    <td>RequiresPermisions</td>
    <td>true).<br><br>当前Subject需要拥有某些特定的权限时,才能执⾏ 被该注解标注的⽅法.如果没有权限,则⽅法不会执</td>
  </tr>
  <tr>
    <td>RequiresRoles</td>
    <td>⾏还会抛出AuthorizationException异常.<br><br>当前Subject必须拥有所有指定的⻆⾊时,才能访问 被该注解标注的⽅法.如果没有⻆⾊,则⽅法不会执</td>
  </tr>
  <tr>
    <td>⼀般情况下我们在项⽬中做权限控制,使⽤最多的 是RequiresPermisions和RequiresRoles,允许存 在多个⻆⾊和权限,默认逻辑是AND,也就是同时拥 有这些才可以访问⽅法,可以在注解中以参数的形</td>
    <td>⾏还会抛出AuthorizationException异常.</td>
  </tr>
  <tr>
    <td>式设置成OR ` java</td>
    <td> </td>
  </tr>
  <tr>
    <td>示例</td>
    <td> </td>
  </tr>
  <tr>
    <td>/拥有⼀个⻆⾊就可以访问</td>
    <td> </td>
  </tr>
  <tr>
    <td>@RequiresRoles(value=</td>
    <td> </td>
  </tr>
  <tr>
    <td>{"ADMIN","USER"},logical = Logical.OR) /拥有所有权限才可以访问</td>
    <td> </td>
  </tr>
  <tr>
    <td>@RequiresPermisions(value= {"sys:user:info","sys:role:info"},logical =</td>
    <td> </td>
  </tr>
  <tr>
    <td>Logical.AND) `</td>
    <td> </td>
  </tr>
  <tr>
    <td>使⽤顺序:Shiro注解是存在顺序的,当多个注解在 ⼀个⽅法上的时候,会逐个检查,知道全部通过为 ⽌,默认拦截顺序是:RequiresRoles-<br><br>e iresPermisions-</td>
    <td> </td>
  </tr>
  <tr>
    <td>>RequiresAuthentication-></td>
    <td> </td>
  </tr>
</table>


### RequiresUser->RequiresGuest

<table>
  <tr>
    <th>` java</th>
    <th> </th>
  </tr>
  <tr>
    <td>示例</td>
    <td> </td>
  </tr>
  <tr>
    <td>/拥有ADMIN⻆⾊同时还要有sys:role:info权限</td>
    <td> </td>
  </tr>
  <tr>
    <td>@RequiresRoles(value={"ADMIN")</td>
    <td> </td>
  </tr>
  <tr>
    <td>@RequiresPermisions("sys:role:info")</td>
    <td> </td>
  </tr>
  <tr>
    <td>`</td>
    <td> </td>
  </tr>
  <tr>
    <td>创建UserRoleControler⻆⾊拦截测试类</td>
    <td> </td>
  </tr>
  <tr>
    <td>` java</td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
  </tr>
</table>


### /*

@Description ⻆⾊测试

@Author Sans

@CreateTime 2019/6/191 38

*/ @RestControler @RequestMaping("/role") public clas UserRoleControler {

@Autowired private SysUserService sysUserService; @Autowired private SysRoleService sysRoleService; @Autowired private SysMenuService sysMenuService; @Autowired private SysRoleMenuService sysRoleMenuService;

/**

- * 管理员⻆⾊测试接⼝

- * @Author Sans

- * @CreateTime 2019/6/19 10:38

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getAdminInfo") @RequiresRoles("ADMIN") public Map<String,Object> getAdminInfo(){

Map<String,Object> map = new HashMap<>(); map.put("code",200); map.put("msg","这⾥是只有管理员⻆⾊能访问的接⼝"); return map;

}

/**

- * ⽤户⻆⾊测试接⼝

- * @Author Sans

- * @CreateTime 2019/6/19 10:38

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getUserInfo") @RequiresRoles("USER") public Map<String,Object> getUserInfo(){

Map<String,Object> map = new HashMap<>(); map.put("code",200); map.put("msg","这⾥是只有⽤户⻆⾊能访问的接⼝"); return map;

}

/**

- * ⻆⾊测试接⼝

- * @Author Sans

- * @CreateTime 2019/6/19 10:38

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getRoleInfo") @RequiresRoles(value={"ADMIN","USER"},logical = Logical.OR) @RequiresUser public Map<String,Object> getRoleInfo(){

Map<String,Object> map = new HashMap<>(); map.put("code",200); map.put("msg","这⾥是只要有ADMIN或者USER⻆⾊能访问的接⼝"); return map;

}

/**

- * 登出(测试登出)

- * @Author Sans

- * @CreateTime 2019/6/19 10:38

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getLogout") @RequiresUser public Map<String,Object> getLogout(){

ShiroUtils.logout(); Map<String,Object> map = new HashMap<>(); map.put("code",200); map.put("msg","登出"); return map;

} 复制代码

}

创建UserMenuController权限拦截测试类 ``` java /**

- * @Description 权限测试

- * @Author Sans

- * @CreateTime 2019/6/19 11:38

- */


@RestController @RequestMapping("/menu") public class UserMenuController {

@Autowired private SysUserService sysUserService; @Autowired private SysRoleService sysRoleService; @Autowired private SysMenuService sysMenuService; @Autowired private SysRoleMenuService sysRoleMenuService;

/**

- * 获取⽤户信息集合

- * @Author Sans

- * @CreateTime 2019/6/19 10:36

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getUserInfoList") @RequiresPermissions("sys:user:info") public Map<String,Object> getUserInfoList(){

Map<String,Object> map = new HashMap<>(); List<SysUserEntity> sysUserEntityList = sysUserService.list(); map.put("sysUserEntityList",sysUserEntityList); return map;

}

/**

- * 获取⻆⾊信息集合

- * @Author Sans

- * @CreateTime 2019/6/19 10:37

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getRoleInfoList") @RequiresPermissions("sys:role:info") public Map<String,Object> getRoleInfoList(){

Map<String,Object> map = new HashMap<>(); List<SysRoleEntity> sysRoleEntityList = sysRoleService.list(); map.put("sysRoleEntityList",sysRoleEntityList); return map;

}

/**

- * 获取权限信息集合

- * @Author Sans

- * @CreateTime 2019/6/19 10:38

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getMenuInfoList") @RequiresPermissions("sys:menu:info") public Map<String,Object> getMenuInfoList(){

Map<String,Object> map = new HashMap<>(); List<SysMenuEntity> sysMenuEntityList = sysMenuService.list(); map.put("sysMenuEntityList",sysMenuEntityList); return map;

}

/**

- * 获取所有数据

- * @Author Sans

- * @CreateTime 2019/6/19 10:38

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/getInfoAll") @RequiresPermissions("sys:info:all") public Map<String,Object> getInfoAll(){

Map<String,Object> map = new HashMap<>(); List<SysUserEntity> sysUserEntityList = sysUserService.list(); map.put("sysUserEntityList",sysUserEntityList); List<SysRoleEntity> sysRoleEntityList = sysRoleService.list(); map.put("sysRoleEntityList",sysRoleEntityList); List<SysMenuEntity> sysMenuEntityList = sysMenuService.list(); map.put("sysMenuEntityList",sysMenuEntityList); return map;

}

/**

- * 添加管理员⻆⾊权限(测试动态权限更新)

- * @Author Sans

- * @CreateTime 2019/6/19 10:39

- * @Param username ⽤户ID

- * @Return Map<String,Object> 返回结果

- */


@RequestMapping("/addMenu") public Map<String,Object> addMenu(){

//添加管理员⻆⾊权限 SysRoleMenuEntity sysRoleMenuEntity = new SysRoleMenuEntity(); sysRoleMenuEntity.setMenuId(4L); sysRoleMenuEntity.setRoleId(1L); sysRoleMenuService.save(sysRoleMenuEntity);

//清除缓存 String username = "admin"; ShiroUtils.deleteCache(username,false); Map<String,Object> map = new HashMap<>(); map.put("code",200); map.put("msg","权限添加成功"); return map;

}

} 复制代码

创建UserLoginControler登录类

/**

- * @Description ⽤ 户 登 录

- * @Author Sans

- * @CreateTime 2019/6/17 15:21

- */ @RestController @RequestMapping("/userLogin") public class UserLoginController { /**
- * 登 录

- * @Author Sans

- * @CreateTime 2019/6/20 9:21

- */ @RequestMapping("/login") public Map<String,Object> login(@RequestBody SysUserEntity sysUserEntity){


Map<String,Object> map = new HashMap<>(); //进 ⾏ 身 份 验 证 try{

//验 证 身 份 和 登 陆 Subject subject = SecurityUtils.getSubject(); UsernamePasswordToken token = new UsernamePasswordToken(sysUserEntity.getUsername(),

sysUserEntity.getPassword()); //验 证 成 功 进 ⾏ 登 录 操 作 subject.login(token);

}catch (IncorrectCredentialsException e) { map.put("code",500); map.put("msg","⽤户不存在或者密码错误"); return map;

} catch (LockedAccountException e) { map.put("code",500); map.put("msg","登录失败，该⽤户已被冻结"); return map;

} catch (AuthenticationException e) { map.put("code",500); map.put("msg","该⽤户不存在"); return map;

} catch (Exception e) { map.put("code",500); map.put("msg","未知异常"); return map;

} map.put("code",0); map.put("msg","登录成功"); map.put("token",ShiroUtils.getSession().getId().toString()); return map;

} /**

- * 未 登 录


- * @Author Sans

- * @CreateTime 2019/6/20 9:22

- */


@RequestMapping("/unauth") public Map<String,Object> unauth(){

Map<String,Object> map = new HashMap<>(); map.put("code",500); map.put("msg","未登录"); return map;

} /**

- * 添 加 ⼀ 个 ⽤ 户 演 示 接 ⼝

- * 这 ⾥ 仅作为 演 示 不 加 任何 权 限 和 重 复 查 询 校 验

- * @Author Sans

- * @CreateTime 2020/1/6 9:22

- */


#### @RequestMapping("/testAddUser") public Map<String,Object> testAddUser(){

// 设 置 基 础 参 数 SysUserEntity sysUser = new SysUserEntity(); sysUser.setUsername("user1"); sysUser.setState("NORMAL"); // 随 机 ⽣ 成 盐 值 String salt = RandomStringUtils.randomAlphanumeric(20); sysUser.setSalt(salt); // 进 ⾏ 加 密 String password ="123456"; sysUser.setPassword(SHA256Util.sha256(password, sysUser.getSalt())); // 保 存 ⽤ 户 sysUserService.save(sysUser); // 保 存 ⻆ ⾊ SysUserRoleEntity sysUserRoleEntity = new SysUserRoleEntity(); sysUserRoleEntity.setUserId(sysUser.getUserId()); // 保 存 ⽤ 户 完 之 后 会 把 ID返 回 给 ⽤ 户 实 体 sysUserRoleService.save(sysUserRoleEntity); // 返 回 结 果 Map<String,Object> map = new HashMap<>(); map.put("code",0); map.put("msg","添加成功"); return map;

}

} 复制代码

## 五.POSTMAN测试

登录成功后会返回TOKEN,因为是单点登录,再次登陆的话会返回新的TOKEN,之前Redis的TOKEN就会失效了

![image 1](assets/imageFile1.png)

当第⼀次访问接⼝后我们可以看到缓存中已经有权限数据了,在次访问接⼝的时候,Shiro会直接去缓存中拿取 权限,注意访问接⼝时候要设置请求头.

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

ADMIN这个号现在没有sys:info:al这个权限的,所以⽆法访问getInfoAl接⼝,我们要动态分配权限后,要清掉缓 存,在访问接⼝时候,Shiro会去重新执⾏授权⽅法,之后再次把权限和⻆⾊数据放⼊缓存中

![image 4](assets/imageFile4.png)

访问添加权限测试接⼝,因为是测试,我把增加权限的⽤户ADMIN写死在⾥⾯了,权限添加后,调⽤⼯具类清掉缓 存,我们可以发现,Redis中已经没有缓存了

![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

再次访问getInfoAl接⼝,因为缓存中没有数据,Shiro会重新授权查询权限,拦截通过

![image 7](assets/imageFile7.png)

## 六.后续补充

github.com/ alexiyang/…

随着SpringBot 版本越来越⾼,Shrio-reids插件在2.2.1之后出现了不兼容的情况,相关详情移步到

解决办法:这⾥感谢github⼤佬manondidi,给出了修复版本.详情移步到 把POM中的 Shrio-redis部分替换为

github.com/manondidi/s…

<groupId>com.github.manondidi</groupId> <artifactId>shiro-redis</artifactId> <version>3.2.10</version> 复制代码

## 七.项⽬源码

码云: GitHub: 谢谢⼤家阅读,如果喜欢,请收藏点赞,多给些star,⽂章不⾜之处,也请给出宝贵意⻅.

gite.com/liselote/s… github.com/xuyulong201…

⽂章分类 后端 ⽂章标签

![image 8](assets/imageFile8.png)

Spring Bot

![image 9](assets/imageFile9.png)

Sans_
