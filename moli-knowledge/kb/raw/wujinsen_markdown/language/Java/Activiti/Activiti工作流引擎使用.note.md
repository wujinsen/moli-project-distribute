Activiti⼯作流引擎使⽤

- 1.简单介⼯作流引擎与Activiti

对于⼯作流引擎的解释请参考百度百科：⼯作流引擎

- 1.1 我与⼯作流引擎

在第⼀家公司⼯作的时候主要任务就是开发OA系统，当然基本都是有⼯作流的⽀持，不过当时使 ⽤的⼯作流引擎是公司⼀些⽜⼈开发的（据说是⽤⼀个开源的引擎修改的），名称叫CoreFlow； 功能相对Activiti来说⽐较弱，但是能满⾜⽇常的使⽤，当然也有不少的问题所以后来我们只能修 改引擎的代码打补丁。

现在是我⼯作的第⼆家公司，因为要开发ERP、OA等系统需要使⽤⼯作流，在项⽬调研阶段我先 搜索资料选择使⽤哪个开源⼯作流引擎，最终确定了Activiti5并基于公司的架构做了⼀些 DEMO。

- 1.2 Activiti与JBPM5？

对于Activiti、jBPM4、jBPM5我们应该如何选择，在InfoQ上有⼀篇⽂章写的很好，从⼤的层⾯ ⽐较各个引擎之间的差异，请参考⽂章：纵观jBPM：从jBPM3到jBPM5以及Activiti5

- 1.3 Activiti资料


官⽹：http://www.activiti.org/ 下载：http://www.activiti.org/download.html 版本：Activiti的版本是从5开始的，因为Activiti是使⽤jBPM4的源码；版本发布：两个⽉发布⼀ 次。 Eclipse Plugin: http://activiti.org/designer/update/ Activit中⽂群：5435716

- 2.初次使⽤遇到问题收集


因为Activiti刚刚退出不久所以资料⽐较空缺，中⽂资料更是少的可怜，所以开始的时候⼀头雾⽔ （虽然之前⽤过⼯作流，但是感觉差距很多），⽽且官⽅的⼿册还不是很全⾯；所以我把我在学 习使⽤的过程遇到的⼀些疑问都罗列出来分享给⼤家；以下⼏点是我遇到和想到的，如果你还有 什么疑问可以在评论中和我交流再补充。

- 2.1 部署流程图后中⽂乱码


乱码是⼀直缠绕着国⼈的问题，之前各个技术、⼯具出现乱码的问题写过很多⽂章，这⾥也不例 外……，Activiti的乱码问题在流程图中。

流程图的乱码如下图所示：

![image 1](<Activiti工作流引擎使用.note_images/imageFile1.png>)

解决办法有两种：

## 2.1.1 修改源代码⽅式

修改源码

org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas

在构造⽅法

public ProcessDiagramCanvas(int width, int height)

中有⼀⾏代码是设置字体的，默认是⽤ Arial 字体，这就是乱码产⽣的原因，把字改为本地的中 ⽂字体即可，例如：

Font font = new Font("WenQuanYi Micro Hei", Font.BOLD, 11);

当然如果你有配置⽂件读取⼯具那么可以设置在*.properties⽂件中，我就是这么做的：

Font font = new Font(PropertyFileUtil.get("activiti.diagram.canvas.font"), Font.BOLD, 11);

## 2.1.2 使⽤压缩包⽅式部署

Activiti⽀持部署*.bpmn20.xml、bar、zip格式的流程定义。

使⽤Activit Deisigner⼯具设计流程图的时候会有三个类型的⽂件:

.activiti设计⼯具使⽤的⽂件

.bpmn20.xml设计⼯具⾃动根据.activiti⽂件⽣成的xml⽂件

.png流程图图⽚

解决办法就是把xml⽂件和图⽚⽂件同时部署，因为在单独部署xml⽂件的时候Activiti会⾃动⽣ 成⼀张流程图的图⽚⽂件，但是这样在使⽤的时候坐标和图⽚对应不起来……

所以把xml和图⽚同时部署的时候Activiti⾃动关联xml和图⽚，当需要获取图⽚的时候直接返回 部署时压缩包⾥⾯的图⽚⽂件，⽽不是Activiti⾃动⽣成的图⽚⽂件

- 2.1.2.1 使⽤⼯具打包Bar⽂件

右键项⽬名称然后点击“Create deployment artifacts”，会在src⽬录中创建deployment⽂ 件夹，⾥⾯包含*.bar⽂件.

- 2.1.2.2 使⽤Ant脚本打包Zip⽂件


这也是我们采⽤的办法，你可以⼿动选择xml和png打包成zip格式的⽂件，也可以像我们⼀样采 ⽤ant target的⽅式打包这两个⽂件。

<?xml version="1.0" encoding="UTF-8"?> <project name="foo">

<property name="workflow.definition" value="foo-commoncore/src/main/resources/diagrams" />

<property name="workflow.deployments" value="foo-commoncore/src/main/resources/deployments" />

<target name="workflow.package.oa.leave"> <echo>打包流程定义及流程图::OA-请假</echo> <zip destfile="${workflow.deployments}/oa/leave.zip"

basedir="${workflow.definition}/oa/leave" update="true"

includes="*.xml,*.png" /> </target>

</project>

这样当修改流程定义⽂件后只要运⾏ant命令就可以打包了：

ant workflow.package.oa.leave

现在部署bar或者zip⽂件查看流程图图⽚就不是乱码了，⽽是你的压缩包⾥⾯的png⽂件。

- 2.2 使⽤引擎提供的Form还是⾃定义业务Form


## 2.2.1 引擎提供的Form

定义表单的⽅式在每个Task标签中定义extensionElements和activiti:formProperty即 可，到达这个节点的时候可以通过API读取表单元素。

Activiti官⽅的例⼦使⽤的就是在流程定义中设置每⼀个节点显示什么样的表单哪些字段需要显 示、哪些字段只读、哪些字段必填。

但是这种⽅式仅仅适⽤于⽐较简单的流程，对于稍微复杂或者⻚⾯需要业务逻辑的判断的情况就 不适⽤了。

对于数据的保存都是在引擎的表中，不利于和其他表的关联、对整个系统的规划也不利！

## 2.2.2 ⾃定义业务Form

这种⽅式应该是⼤家⽤的最多的了，因为⼀般的业务系统业务逻辑都会⽐较复杂，⽽且数据库中 很多表都会有依赖关系，表单中有很多状态判断。

例如我们的系统适⽤jQuery UI作为UI，有很多javascript代码，⻚⾯的很多操作需要特殊处理 （例如：多个选项的互斥、每个节点根据类型和操作⼈显示不同的按钮）；基本每个公司都有⼀ 套⾃⼰的UI⻛格，要保持多个系统的操作习惯⼀致只能使⽤⾃定义表单才能满⾜。

#### 2.3 业务和流程的关联⽅式

这个问题在群⾥⾯很多⼈都问过，这也是我刚刚开始迷惑的地⽅；

后来看了以下API发现RuntimeService有两个⽅法：

- 2.3.1 startProcessInstanceByKey


javadoc对其说明：

startProcessInstanceByKey(String processDefinitionKey, Map variabes)

Starts a new process instance in the latest version of the process definition with the given key

其中businessKey就是业务ID，例如要申请请假，那么先填写登记信息，然后（保存+启动流 程），因为请假是单独设计的数据表，所以保存后得到实体ID就可以把它传给 processInstanceBusinessKey⽅法启动流程。当需要根据businessKey查询流程的时候就 可以通过API查询:

runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(processInstanceB usinessKey, processDefinitionKey);

建议数据库冗余设计：在业务表设计的时候添加⼀列：PROCESS_INSTANCE_ID varchar2(64)，在流程启动之后把流程ID更新到业务表中，这样不管从业务还是流程都可以查 询到对⽅！

特别说明： 此⽅法启动时⾃动选择最新版本的流程定义。

- 2.3.2 startProcessInstanceById

javadoc对其说明：

startProcessInstanceById(String processDefinitionId, String businessKey, Map variables)

Starts a new process instance in the exactly specified version of the process definition with the given id.

processDefinitionId：这个参数的值可以通过 repositoryService.createProcessDefinitionQuery()⽅法查询，对应数据库： ACT_RE_PROCDEF；每次部署⼀次流程定义就会添加⼀条数据，同名的版本号累加。

特别说明： 此可以指定不同版本的流程定义，让⽤户多⼀层选择。

- 2.3.3 如何选择


### 建议使⽤startProcessInstanceByKey，特殊情况需要使⽤以往的版本选择使⽤ startProcessInstanceById。

- 2.4 同步⽤户数据


这个问题也是⽐较多的⼈询问过，Activiti⽀持对任务分配到：指定⼈、指定组、两者组合，⽽这 些⼈和组的信息都保存在ACT_ID..表中，有⾃⼰的⽤户和组(⻆⾊)管理让很多⼈不知所措了； 原因是因为每个系统都会存在⼀个权限管理模块（维护：⽤户、部⻔、⻆⾊、授权），不知道该 怎么和Activiti同步。

## 2.4.1 建议处理⽅式

Activiti有⼀个IdentityService接⼝，通过这个接⼝可以操控Activiti的ACT_ID_*表的数据， ⼀般的做法是⽤业务系统的权限管理模块维护⽤户数据，当进⾏CRUD操作的时候在原有业务逻 辑后⾯添加同步到Activiti的代码；例如添加⼀个⽤户时同步Activiti User的代码⽚段：

/**

- * 保存⽤户信息 并且同步⽤户信息到activiti的identity.User，同时设置⻆⾊

- * @param user

- * @param roleIds

- */


public void saveUser(User user, List<Long> roleIds, boolean synToActiviti) { accountManager.saveEntity(user); String userId = user.getId().toString();

if (synToActiviti) {

List<org.activiti.engine.identity.User> activitiUsers = identityService.createUserQuery().userId(userId).list();

if (activitiUsers.size() == 1) { //更新信息 org.activiti.engine.identity.User activitiUser =

activitiUsers.get(0);

activitiUser.setFirstName(user.getName()); activitiUser.setLastName(""); activitiUser.setPassword(user.getPassword()); activitiUser.setEmail(user.getEmail()); identityService.saveUser(activitiUser);

// 删除⽤户的membership List<Group> activitiGroups =

identityService.createGroupQuery().groupMember(userId).list(); for (Group group : activitiGroups) {

identityService.deleteMembership(userId, group.getId());

}

// 添加membership for (Long roleId : roleIds) {

Role role = roleManager.getEntity(roleId); identityService.createMembership(userId,

role.getEnName());

}

} else {

org.activiti.engine.identity.User newUser =

identityService.newUser(userId); newUser.setFirstName(user.getName()); newUser.setLastName(""); newUser.setPassword(user.getPassword()); newUser.setEmail(user.getEmail()); identityService.saveUser(newUser);

// 添加membership for (Long roleId : roleIds) {

Role role = roleManager.getEntity(roleId); identityService.createMembership(userId,

role.getEnName());

} }

}

}

删除操作也和这个类似！

不管从业务系统维护⽤户还是从Activiti维护，肯定要确定⼀⽅，然后CRUD的时候同步到对⽅， 如果需要同步多个⼦系统那么可以再调⽤WebService实现。

- 2.5 流程图设计⼯具⽤什么

Activiti提供了两个流程设计⼯具，但是⾯向对象不同。

Activiti Modeler，⾯向业务⼈员，使⽤开源的BPMN设计⼯具 ，使⽤BPMN描述业务流 程图

Signavio

Eclipse Designer，⾯向开发⼈员，Eclipse的插件，可以让开发⼈员定制每个节点的属性（ID、 Name、Listener、Attr等）

2.5.1 我们的⽅式

可能你会惊讶，因为我们没有使⽤Activiti Modeler，我们认为⽤Viso已经能表达流程图的意思 了，⽽且项⽬经理也是技术出身，和开发⼈员也容易沟通。

⽬前这个项⽬是第⼀个使⽤Activiti的，开始我们在需求调研阶段使⽤Viso设计流程图，利⽤ 设计和客户沟通，确定后由负责流程的开发⼈员⽤Eclipse Designer设计得到 bpmn20.xml，最后部署。

泳道 流程图

- 2.6 Eclipse Designer存在的问题


这个插件有⼀个很讨厌的Bug⼀直未修复，安装了插件后Eclipse的复制和粘帖快捷键会被更换为 (Ctrl+Insert、Shift+Insert)；Bug描述请⻅：

### Activit Forums中报告的Bug Jira的登记

所以最后我们只能单独开⼀个安装了Eclipse Designer的Eclipse专⻔⽤来设计流程图，这样就不 影响正常使⽤Eclipse JAVAEE了。

# 3.配置

- 3.1 集成Spring

对于和Spring的集成Activiti做的不错，简单配置⼀些Bean代理即可实现，但是有两个和事务相 关的地⽅要提示：

配置processEngineConfiguration的时候属性transactionManager要使⽤和业务功能的 同⼀个事务管理Bean，否则事务不同步。 对于实现了org.activiti.engine.delegate包中的接⼝的类需要被事务控制的实现类需要被Spring 代理，并且添加事务的Annotation或者在xml中配置，例如:

/**

- * 创建缴费流程的时候⾃动创建实体

*

- * @author HenryYan

- */


@Service @Transactional publicclass CreatePaymentProcessListener implementsExecutionListener {

.... }

?

- 4.使⽤单元测试


单元测试均使⽤Spring的AbstractTransactionalJUnit4SpringContextTests作为 SuperClass，并且在测试类添加：

##### @ContextConfiguration(locations = { "/applicationContext-test.xml"}) @RunWith(SpringJUnit4ClassRunner.class)

?

虽然Activiti也提供了测试的⼀些超类，但是感觉不好⽤，所以⾃⼰封装了⼀些⽅法。

### 代码请转移：https://gist.github.com/2182847

- 4.1 验证流程图设计是否正确

代码请转移：https://gist.github.com/2182869

- 4.2 业务对象和流程关联测试

代码请转移：

- 5.各种状态的任务查询以及和业务对象关联


我们⽬前分为4中状态：未签收、办理中、运⾏中、已完成。

查询到任务或者流程实例后要显示在⻚⾯，这个时候需要添加业务数据，最终结果就是业务和流 程的并集，请参考6.2。

https://gist.github.com/2182973

- 5.1 未签收(Task)


此类任务针对于把Task分配给⼀个⻆⾊时，例如部⻔领导，因为部⻔领导⻆⾊可以指定多个⼈所 以需要先签收再办理，术语：抢占式

对应的API查询：

/**

- * 获取未签收的任务查询对象

- * @param userId ⽤户ID

- */


@Transactional(readOnly = true) publicTaskQuery createUnsignedTaskQuery(String userId) {

TaskQuery taskCandidateUserQuery = taskService.createTaskQuery().processDefinitionKey(getProcessDefKey())

.taskCandidateUser(userId); returntaskCandidateUserQuery;

}

?

- 5.2 办理中(Task)


此类任务数据类源有两种:

签收后的，5.1中签收后就应该为办理中状态 节点指定的是具体到⼀个⼈，⽽不是⻆⾊

对应的API查询：

/**

- * 获取正在处理的任务查询对象

- * @param userId ⽤户ID

- */


@Transactional(readOnly = true) publicTaskQuery createTodoTaskQuery(String userId) {

TaskQuery taskAssigneeQuery = taskService.createTaskQuery().processDefinitionKey(getProcessDefKey()).taskAssignee(use rId);

returntaskAssigneeQuery; }

?

#### 5.3 运⾏中(ProcessInstance)

说⽩了就是没有结束的流程，所有参与过的⼈都应该可以看到这个实例，但是Activiti的API没有 可以通过⽤户查询的⽅法，这个只能⾃⼰⽤hack的⽅式处理了，我⽬前还没有处理。

### 从表ACT_RU_EXECUTION中查询数据。

对应的API查询：

/**

- * 获取未经完成的流程实例查询对象

- * @param userId ⽤户ID

- */


@Transactional(readOnly = true) publicProcessInstanceQuery createUnFinishedProcessInstanceQuery(String userId) {

ProcessInstanceQuery unfinishedQuery = runtimeService.createProcessInstanceQuery().processDefinitionKey(getProcessDefKey())

.active();

returnunfinishedQuery; }

?

- 5.4 已完成(HistoricProcessInstance)


已经结束的流程实例。

### 从表ACT_HI_PROCINST中查询数据。

/**

- * 获取已经完成的流程实例查询对象

- * @param userId ⽤户ID

- */


@Transactional(readOnly = true) publicHistoricProcessInstanceQuery createFinishedProcessInstanceQuery(String userId) {

HistoricProcessInstanceQuery finishedQuery = historyService.createHistoricProcessInstanceQuery()

.processDefinitionKey(getProcessDefKey()).finished(); returnfinishedQuery;

}

?

- 5.5 查询时和业务关联


提示：之前在业务对象添加了PROCESS_INSTANCE_ID字段

思路：现在可以利⽤这个字段查询了，不管是Task还是ProcessInstance都可以得到流程实例 ID，可以根据流程实例ID查询实体然后把流程对象设置到实体的⼀个属性中由Action或者 Controller输出到前台。

### https://gist.github.com/2183557

代码请参考：

# 6.UI及截图

结合实际业务描述⼀个业务从开始到结束的过程，对于迷惑的同学看完豁然开朗了；这⾥使⽤请 假作为例⼦。

- 6.1 单独⼀个列表负责申请


这样的好处是申请和流程办理分离开处理，列表显示未启动流程的请假记录（数据库 PROCESS_INSTANCE_ID为空）。

申请界⾯的截图：

![image 2](<Activiti工作流引擎使用.note_images/imageFile2.png>)

- 6.2 流程状态


![image 3](<Activiti工作流引擎使用.note_images/imageFile3.png>)

- 6.3 流程跟踪


图⽚⽅式显示当前节点：

![image 4](<Activiti工作流引擎使用.note_images/imageFile4.png>)

列表形式显示流程流转过程:

![image 5](<Activiti工作流引擎使用.note_images/imageFile5.png>)

- 6.3.1 当前节点定位JS

Java代码请移步：

Javascript思路：先通过Ajax获取当前节点的坐标，在指定位置添加红⾊边框，然后加载图 ⽚。

代码移步：

- 7.开启Logger

- 8.结束


https://gist.github.com/2183712

https://gist.github.com/2183804

添加log4j的jar 设置log4j.logger.java.sql=DEBUG

- 1.
- 2.


之前就想写这篇⽂章，现在终于完成了，花费了⼏个⼩时，希望能节省你⼏天的时间。

请读者仔细阅读Activiti的⽤户⼿册和Javadoc。

