前⾔

在CSDN上转悠经常看到有⽹友寻求PowerDesigner相关资料的帖⼦，Baidu,Gogle上找找还真很少；同时也有不少⽹友发来Email询 问相关PowerDesigner问题或索要相关资料的，故下定决⼼制作本⽂档。折腾⼆⼗多天，终于输出了现在的⽂档，其中绝⼤部分内容都 是依照PowerDesigner⾃带的帮助⽂档翻译过来，乐意啃英⽂的朋友最好还是看其”原汁”教程，同时本⽂档仅⽤于帮助分析设计⼈员更 快熟悉掌握PowerDesigner的使⽤⽅法，不包含分析设计⽅⾯的理论，所以要作好系统的分析设计⼯作还是需要⽤户深厚的项⽬实践功 底。

起初想尽量按照PowerDesigner⾃带帮助⽂档完整地进⾏，尝试了⼀上午的⼯作之后这种⽅案马上就被我否决，原因有⼆：1.内容太 多，⼯作量太多。2.原帮助⽂档特别周全，个⼈觉得可以在内容上作很⼤程度的压缩。姑决定按原帮助⽂档写，同时加⼊⾃⼰⽬前正在 做的技术论坛分析设计过程以便于理解。 对本⽂档内容的⼏点说明：

- 1． 本⽂档只包括PowerDesigner部分内容（RQM,Report,CDM,PDM），内容不够全⾯。
- 2． 内容尽量简略，⼀些相同或类似操作过程尽量不再重复。
- 3． 部分术语参考了飞思科技产品研发中⼼监制电⼦⼯业出版社的《PowerDesigner数据库系统分析设计与应⽤》。
- 4． 暂时没有包含 OM,XML,BPM,ILM等模型内容，我将会在后期陆续更新。 版本说明：我使⽤的是PowerDesigner Trial 1英⽂版，因此⽂档中⼀些菜单，按钮名称也⽤英⽂写出（因当⼼⾃⼰译出的名称和中


⽂版上的名称不⼀致⽽造成理解不便），若是给使⽤中⽂版的朋友带来不便，我在这说声”抱歉”了!同时由于各版本不同部分操作可能会 有所区别。

这⾥要感谢在我进⾏翻译⼯作期间给我发送Email关注的⽹友，感谢⼀直⽀持我的朋友们！由于第⼀次做翻译⼯作，限于⽔平有限， ⽂档中肯定存在很多不⾜和错误之处，衷⼼欢迎各位⽹友指点迷津，期望得到您的指导！

Email:dingchungao@gmail.com dingchungao@126.com Q  30982401 Blog:htp:\feiren1421.cnblogs.com

Slash

206.8.31

需求模型

PowerDesigner.1363评估版 为了更好的将原⽂含义再现，不加⼊我个⼈语⾔习惯，我尽量按照原⽂档内容翻译。

环境简介

Workspace

左边的资源浏览窗⼜Browser提供当前的Workspace层次结构，根节点为Workspace节点，Workspace中可以包含⽬录(Folder)， 模型(Model)，多模型报告(Multi-ModelReport),其中模型可以各种系统⽀持的模型类型。

⼀般我们将欲构建的⽬标系统的各种模型，⽂档及报告放在同⼀Workspace中，以便于模型设计与管理。

Workspace定义了使⽤PowerDesigner建模时的信息集合，PowerDesgner⼯作时只能有⼀个Workspace处于打开状态。要新建 Workspace必须先将当前Workspace关闭，如以下操作：右击当前Workspace->选择”Close”,这样即完成了原Workspace的关闭，同时 也⾃动创建了新的Workspace，只是新Workspace中还没有内容。接下来就可以在其中添加⾃⼰想要新建的模型了。

需求模型基础(Requirement modelbasics)

Requirements Model(RQM)是⼀种⽂档式模型，它通过准确恰当地列出，解释开发过程程中需要实现的功能⾏为来描述待开发项 ⽬。你可以为开发过程中需要使⽤到的各种结构化技术⽂档（功能或技术规格说明书，测试计划）⽽使⽤RequirementsModel.

Requirements Model以下⾯两种视图呈现（⽽不是以图表形式）： 需求⽂档视图 对⼀系列公共属性进⾏编号 可编辑⾏矩阵 单元格代表了当前需求与设计对象，外部⽂件或其它需求的联系

Requirements Model允许你可以： 对⼀结构化技术⽂档建⽴需求模型 检查现有或引⼊的模型 对需求和设计对象(其它类型模型)建⽴联系

对其它设计对象建⽴需求模型，或反之通过需求模型建⽴其它设计类型 从需求模型⽣成或更新MS Word⽂档,提供⽤户⼀符合需求模型的MS Word⽂档 从现有MS Word⽂档⽣成或更新相应的需求模型

各对象之间关系如下图所⽰：

Requirements Model应该包括如下特定对象(Object)：

<table>
  <tr>
    <th>Object</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>Requirement</td>
    <td>功能⾏为的名称或内容，可以是⽗级或⼦级需求的⼀部分，它应该在被指派给⽤户或群 (Groups)前被准确定义说明</td>
  </tr>
  <tr>
    <td>Glosary term</td>
    <td>⽤于需求模型中的词汇，它应该被正确定义说明以避免误解，建⽴⼀定的通⽤规则</td>
  </tr>
  <tr>
    <td>User</td>
    <td>⾄少与某⼀需求有关的个⼈实体</td>
  </tr>
  <tr>
    <td>Group</td>
    <td>⾄少与某⼀需求有关的⽤户(user)群体</td>
  </tr>
</table>


由于Requirements Model中没有图表，以上各对象均没有与之对应的图象符号。需求是以图表视图形式列出，可编辑矩阵视图显⽰ 出需求和各设计对象，外部⽂件或其它需求之间的联系。

需求建模环境包括⼀系列定义不同模型内容和⾏为的参数和设定选项，你可以通过在建⽴模型时，使⽤默认选项建⽴模型后或建⽴模 型模版时进⾏设置。 菜单栏—>选择”Tols” Model Options，可见以下模型选项对话框，现在可以进⾏你喜欢的设置了。

定义模型属性

在打开相应模型⽂件后，选择菜单栏中Model->ModelProperties,或在左边树性对象浏览器中选中对应模型，双击/右键->选择 Properties，均可进⼊Properties设置区间，如下图：

接下来就可以进⾏你想要的设置了！

新建Requirements Model 下⾯以我⾃⼰最近的项⽬过程为例逐步讲解各过程： 项⽬简介：这是个类似动⽹或CSDN的论坛系统，参考了它们的功能设计，主要⽤于本⼈练习N层架构的学习。 建⽴需求模型： 建⽴完成的需求视图

⾸先我们要新建⼀Workspace作为整个系统各种模型，⽂档与报告信息集合。

启动PowerDesigner,这时会默认打开⼀个Workspace，单击⿏标右键->选择”Close”,这样我们完成了关闭原来Workspace，同时新 建Workspace的⼯作。接下来就是在其中添加各种模型了。 新建Requirements Model 点击File->New或⿏标右键单击Workspace->New->RequirementModel可以看到新建模型属性选项框如下：

选择左边RequirementsModel,其它为默认设置，确定,OK！ 下⾯我们对新建的RQM进⾏先进⾏⼀些基本属性设置： 在资源浏览窗⼜中右键单击刚建好的RQM->Properties或直接双击对应RQM,直接进去模型属性设置Model Properties,如下图所⽰：

现在你可以进⾏⾃⼰想要设置了。这⾥我们将Name,Co ment分别进⾏基本设置，同时系统默认Name和Code是⼀致的，Name⽤ 来进⾏分析描述，为了形象明了可以使⽤中⽂，⽽Code则和后期的具体设计有关，如⽤于编码设计，⼀般多⽤英⽂加数字等标准命名 (仅供参考)。

同时我们可以看到在新建RQM时也⾃动建⽴了⼀个模型视图(View),接下来我们就要对该视图(View)进⾏编辑以建⽴需求模型，根据 前⾯需求模型简介介绍的相关RQM视图知识，需求模型可以⽤⽂档视图的形式表⽰，后续的⼤部分⼯作只有对View进⾏编辑就OK了！ 先看看完成后的需求视图吧！

这⾥的各系统需求是按层次排列的，这样也使需求⽂档视图能和标准的层次化Word/rtf⽂档能进⾏相互转换。可以通过视图上⽅的⼯ 具栏进⾏全⾯的需求模型建设。

添加需求(Requirement)： 点击需求⽂档视图⼯具栏上”Insert a Row”⼯具或点击需求⽂档视图的空⽩区 这样⼀个预先默认⾃定义的需求已经添加在⽂档视图中，如下所⽰：

编辑需求属性 双击需求TitleID左边的箭头(arrow)或单击需求⽂档视图⼯具栏最左边的Properties⼯具即进⼊属性属性编辑。 其中除了TitleID栏之外每栏都处于可编辑状态的。

注：箭头所在⾏为选中⾏

属性各栏⽬对应着⽂档视图中的各可编辑栏。这⾥我们可以设置各需求的详细内容和描述信息，⽐如标题(Title),需求描述 (Description),优先级(Priority),风险(Risk),状态(Status),⼯作量(Workload)等详细内容。详细设置信息请参考⽰例⽂件。

若要更改⽂档视图中的可见栏⽬，可以通过单击需求⽂档视图⼯具栏中Customize Columnsand Filter⼯具，进⼊

现在可以选择您想要显⽰的栏⽬了。 这样我们就基本上完成了系统需求的设计过程，依此多次操作完成如下系统需求⽂档视图基本框架：

后⾯的⼯作就是对其中各Requirement做进⼀步的细化，对各需求模块做更为细致的划分，即分层细化，这样也和层次化的⽂档吻 合。这⾥我们以对FunctionalRequements的设计为例进⾏讲解，先看看细化完成后的需求⽂档视图(部分)：

现在让我们开始吧！ ⽅法⼀： 需求⽂档视图，选中FunctionalRequirements->点击视图⼯具栏”Insert Sub-Object”⼯具(⽽不是”Inserta Row”⼯具),这样就在 Functional Requirements中插⼊了⼀个⼦对象。 ⽅法⼆： 于左边资源管理窗⼜Requirements⽬录下右键单击相应需求名称->New->Requirement即可。如下图：

现在只要对新插⼊的⼦对象进⾏详细的内容编辑设计即可，同样地我们也可以对各⼦对象通过再次添加⼦对象作进⼀步的细化⼯作。 如果要提升或降低某部分的需求层次，则可以通过⼯具栏中的Promote和Demote来实现调整。

定义Users和Groups Users（⽤户） 指在⼀个需求模型中⾄少和⼀个已定义需求有关的⼈的集合。 Groups（组） 指专属于开发进程中⼀个或多个⽅⾯的⽤户类别。每个⽤户组要与需求模型中⾄少⼀个已定义需求有关。 新建User/Group

在资源浏览窗⼜中，右键单击模型名称（图标） ->New User/Group,打开User或Group属性窗⼜，输⼊相应名称和代码名， 确定即完成新建。 同样也可在菜单栏选择”Model” Users/Groups完成新建过程。 下⼀步是将相应的User与Group联系，添加进Group中，打开相应的Group属性，选择Group Users属性栏

点击属性⼯具栏中”Ad Objects”⼯具，从中选择您要添加的User对象，当然只有在您已经建⽴了相应的User对象时才会显⽰User成员 列表。

现在选择您需要添加的User对象，确定就可以了。 建⽴Busines rules(业务规则)

业务规则是对为了满⾜业务需求，模型应该包括的特定内容或关于如何构建模型⽅⾯的描述清单。在这⾥的⽰例模型中，我们要定义 关于论坛积分制度的业务规则，具体业务规则内容见参考⽂档。

在Requirement Model状态下，PowerDesgner默认Busins为不可⽤状态，为此我们需要通过新建Extended modeldefinition（扩 展模型定义）来激活Busines rules。 步骤如下： 选择菜单栏”Model” Extended ModelDefinitions，这时打开List of Extended Model Definitions，通过选择其⼯具栏中”Ad a Row”⼯具，如下图：

点击Aply即在资源浏览窗⼜中添加Extended ModelDefinitions⽬录。 在资源浏览器中打开Extended Model Definitions⽬录，双击相应扩展模型定义左边图标

即打开Extended ModelDefinition Propreties

现在可以在右边输⼊extended modeldefinition的Name,Code等信息。

选择左边窗⼜中”Profile”⽬录，右键单击在上下⽂菜单中选择”Ad Metaclases…”，这时可以看到MetaclasSelection对话框，选 择PdCo mon页，在Metaclas选择列表中选定BusinesRule

点击OK,现在可以在Profile⽬录下看到BusinesRule了，点击OK!已经完成了BusinesRule的激活。 完成上述激活步骤后我们就可以执⾏Busines Rules的新建了。

在资源浏览器窗⼜中右键单击当前需求模型->选择”New”,或通过选择菜单栏上Model,你可以看到Busines Rule(s)选项了，选择执 ⾏，设定详细业务规则属性内容就OK了，⽰例模型中我们完成了三个关于论坛积分制度⽅⾯的业务规则，可以查看参考⽂档，不再赘 述！ 接下来我们为⽰例模型添加术语表(glosary term) 选择菜单栏Model->Glosaryterms,进⼊Listof Glosary terms对话框

选择⼯具栏上”Ad a Row”⼯具，进⾏glosary term编辑。 或通过资源浏览器中也同样能执⾏添加术语操作。

若⽬标系统⽐较⼤，功能较多，也可以通过在系统模型中添加⽂件夹(package)来⽅便管理，也能使整个模型更清晰，具有层次性。

到这我们就已经基本完成了整个需求模型，接下来让我们来与word⽂档协调⼯作且⽣成内容全⾯的需求报告⽂档。

从需求模型⽣成Word⽂档

资源浏览窗⼜中，右键单击当前模型名称或图标->选择”Export as WordDocument” 或在菜单栏中选择Tols->Exportas Word Document.，这时⽂档⽣成就开始执⾏，输出窗⼜会显⽰对当前模型的检验信息，这⾥我们 对其中的Warning就忽略不作考虑了。 ⽚刻后会弹出

选择空⽩⽂档，单击确定，你可以看到⽂档输出了！ ⽣成的⽂档如

其中红⾊部分⽂字表⽰与当前模型联接的信息，如果已经确定需求模型，要⽣成最终⽂档作为分析成果，可以通过在MS Word菜单 栏上选择”Requirements”->Detachthe Document from the Requirements Model，这样就实现了最终⽂档与需求模型的分离，同时⽣ 成的⽂档也没有那些红⾊的联接信息了。

在没有将⽂档与模型分离时，我们还可能在PowerDesigner中对需求模型进⾏修改，这时我们可以对⽂档执⾏更新操作，同时对符合 层次化标准的Word⽂档，也可以将其转化为相应的需求模型。

需求模型的个⼈见解就到此为⽌，要申明的是：以上内容只是对PowerDesigner提供的需求建模功能的⼤概说明，其中太多细节还需 ⽇后使⽤过程中慢慢掌握。

⽣成模型报告

⽂档⽣成Report

个⼈觉得有必要将Report（⽂档⽣成）提前讲解，毕竟软件⼯程的任何阶段都会输出相应⽂档，PowerDesigner⽀持⽣成RTF和 HTML两种格式⽂档。

下⾯以刚完成的⽰例论坛系统的需求模型为例讲解。

PowerDesigner提供对Report的操作有关于Report TemplateEditor(报告模板编辑器)，Report Template(报告模板)，Report Editor(报告编辑器)，Multi-Model ReportEditor（多模型报告编辑器），Report Language Editor（报告语⾔编辑器）

- 1． 使⽤Report TemplateEditor（报告模板编辑器） 打开Report TemplateEditor（报告模板编辑器）


- （1）选择Tols->Resources->ReportTemplates,可以打开List of Report Templates（报告模板列表），列表显⽰出当前系统中存在 的报告模板，如下图⽰：
- （2）在Type（类型）下拉列表中选择相应的模板类型，可⽤模板中会显⽰对应您选择模板类型的模板，同时您也可以通过单击模板列 表⼯具栏上的New⼯具新建您所需要的模板。
- （3）选择相应模板
- （4）通过单击⼯具栏上Properties⼯具或直接双击所选定模板，进⼊相应模板属性编辑器。其中左边Available items为可⽤项⽬，右边 Template items为当前模板中项⽬，表⽰出该报告模板的结构。


现在你可以对该报告模板进⾏编辑修改！

也可以通过选择Model->Reports打开List of Reports（报告列表），再选择报告列表⼯具栏上Manage ReportTemplates（管理报告模 板）⼯具打开List of Report Templates（报告模板列表）。

- 2． 标准报告模板（Standard reportemplates） PowerDesigner默认⾃带了⼀系列的标准报告模板，其模板安装⽬录在Sybase\PowerDesignerTrial 1\Resource Files\Report

Templates下。 其中每种类型的模板都包含三种类型的标准模板，如下表所⽰：

- 3． 创建报告模板 报告模板是⼀种可以⽤来快速⽣成报告⽂档的⽂件，你可以使⽤PowerDesigner⾃带的⼀些标准模板或创建你⾃⼰的模板。创建模板时 需要指明你在报告中需要包含的信息，同时也可以通过选择⼀种你想要语⾔⽤以显⽰报告⽂档。


<table>
  <tr>
    <th>模板类型</th>
    <th>命名规则</th>
    <th>所⽣成报告内容</th>
  </tr>
  <tr>
    <td>Ful</td>
    <td>modelFULlanguage.RTP</td>
    <td>⽬录，所有主要的模型项⽬</td>
  </tr>
  <tr>
    <td>Standard</td>
    <td>modelSTDlanguage.RTP</td>
    <td>⽬录，模型图，包图和⼤部分List对象</td>
  </tr>
  <tr>
    <td>List</td>
    <td>modelLISlanguage.RTP</td>
    <td>标题对象，所有的List对象</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


- （1） 选择Tols->Resources->ReportTemplates,打开了List of Report Templates窗⼜
- （2） ⼯具栏中New⼯具，即打开Report TemplateType窗⼜
- （3） 输⼊相应的模板名称，选择语⾔种类同时在模板列表中选择模板类别
- （4） 单击OK即进去Report TemplateEditor（报告模板编辑器），现在你可以将想要在模板中显⽰的项⽬进⾏添加调整了。
- （5） 完成模板编辑后，选择File->Save,就可以将你所编辑好的报告模板保存为.rtp⽂件。


使⽤报告编辑器（Using the ReportEditors） 1．创建模型报告

你可以通过使⽤报告编辑器创建模型报告和多模型编辑器创建多模型报告，但是当你要创建报告时，在当前workspace中必须打开⾄ 少⼀个模型且要有⼀个默认⽣成节点。 这⾥我们对前期需求模型创建报告⽂档作为⽰例。

- （1）Model->Reports,打开List of Reports窗⼜
- （2）单击⼯具栏上New⼯具，弹出New Report对话框，输⼊对应名称，选择语⾔类别和报告模板。
- （3）单击OK,即完成报告新建⼯作，这⾥我选择的报告模板为None,接下来我们对报告内容及节点进⾏编辑。

如上图对⽬标报告⽂档内容进⾏编辑，报告节点设计完毕后就可以⽣成html或rtf报告了。

- （4）选择Report⾯板中的Generate HTML或Generate RTF即可⽣成相应格式报告⽂档，若要预览⽂档，可以选择⾯板中的Print Preview⼯具。


最终⽣成的html⽂档如下图⽰：

创建多模型报告（Multi-Model Report）

多模型报告能够通过使⽤Section在同⼀个报告中包含不同类型模型中的对象，将不同模型结合起来提供⼀个全局视⾓的综合报告。 但是每个Section只能是⼀种模型类型，并且只能使⽤⼀个模板类型，被使⽤模型必须在当前workspace中处于打开状态。

- （1）在当前workspace中打开⼀些需要参与多模型报告的模型，选择菜单栏中File->New, 在弹出的新建窗⼜中选择Multi-Model Report。 或通过右键单击当前Workspace->New->Multi-ModelReport亦能完成多模型报告的新建。
- （2）弹出新建多模型报告窗⼜。
- （3）输⼊报告名称，选择语⾔种类，同时在Model name的下拉框中选择⼀个Section将要描述的模型。同时根据可以根据需要在 Report template下拉列表中选择相应的报告模板。
- （4）点击OK,确认操作！这时就已经打开多模型报告编辑窗⼜，如下图⽰：
- （5）基本的多模型报告框架已经建好，下⼀步就是对其中Section进⾏设置编辑即可。根据需要加⼊不同模型创建适当Section，基本操 作与普通单模型报告类似！


处理Section

每个报告⽂件⾄少要包含⼀个Section，通过使⽤Section可以使模型设计者将⽬标模型分为⼏个不同部分，便于分析模型各部分功 能，因此恰当地使⽤Section可以让报告⽂档更加清晰，具有层次性。可以通过两种⽅式创建Section（节）:

- 1. 创建⼀个空⽩Section
- 2. 创建⼀个基于模板（Template）的Section 当你创建新Section时，模板列属性默认被设置为None,且应⽤模板选项框被⾃动选取。 创建Section


- （1） 在Report Editor编辑窗⼜下，选择Report->Sections,即弹出List of ReportSections窗⼜，其中Section列表包含⼀默认⽣成 的Section.
- （2） 输⼊Section名称，如果没有更改输⼊名称系统将会在报告项⽬⾯板（Report Items）中使⽤默认名称。
- （3） 如果当前报告为多模型报告，则可以在模型栏（Model column）中选择对应模型类型，多模型报告可能包括多种模型类型的 Section,如 OM,PDM,CDM等，但必须这些模型在当前Workspace中都处于打开状态。若当前报告为单模型报告，则Model列为不可 选，系统⾃动设置为当前模型。
- （4） 单击模板栏（Template）,选择需要的模板类型，可选项有None,FulRequirement report, List Requirement report,Standard Requirement report。若选择None则创建空⽩Section.,同时Aply Template选项框为默认选取状态。
- （5） 以上操作已经完成对当前Section的设置，要再次添加Scetion则通过选取⼯具栏上Ad a Row或Insert a Row⼯具添加新 Section,同时再次执⾏（1） -（4）步骤设置其属性。
- （6） 单击OK，现在已经完成多个Section的创建。其中每个Section在报告编辑器（Report Editor）中显⽰为Report Items⾯板底 部的Tab页中，如下图所⽰：


将报告中Section创建为模板 经常我们需要将已经设计好的Section供以后在其他模型中使⽤，为此我们可以将创建好的Section保存为模板。

- （1） 单击需要保存的Section的Tab页（在Report Items⾯板底部的Tab页）
- （2） 选择⼯具栏上Report->CreateTemplate From Section,打开报告模板编辑器（Report Template Editor）页⾯，则原来在报告 项⽬（Report Items）中显⽰的项⽬（Items）这时显⽰在模板项⽬⾯板中。
- （3） 确认模板项⽬后，选择菜单栏上File->Save,即打开保存⽂件对话框。
- （4） 输⼊相应模板名称，单击保存即可。


使⽤报告语⾔编辑器（Using the ReportLanguage Editor）

通过使⽤报告语⾔编辑器可以创建和修改报告语⾔的源⽂件（Resource files），报告语⾔源⽂件是以XRL为后缀的XML格式⽂件， 其中包含了报告中所有可打印⽂本和它们的⼀些默认数据，报告语⾔源⽂件保存在中⼼区域且能够被任何模型报告共享使⽤，从⽽保证 了数据⼀致性，节省了⽤户创建编辑报告⽂档的时间。PowerDesigner在安装⽬录\Sybase\PowerDesignerTrial 1\Resource Files\Report Languages下⾃带了⼀系列的报告语⾔源⽂件。我们也可以通过使⽤报告语⾔编辑器（Report LanguageEditor）创建符 合⾃⼰需求的⽂档报告源⽂件。

打开报告语⾔编辑器

- （1） 选择菜单栏上Tols->Resources->ReportLanguages,即打开报告语⾔列表（List of Report Languages）窗⼜，其中显⽰出 当前系统具有的所有报告语⾔列表。
- （2） 选择某种报告语⾔
- （3） 单击⼯具栏上Properties⼯具，或双击该⾏，打开Report LanguageProperties窗⼜


同样你也可以通过报告编辑器打开报告语⾔编辑窗⼜：选择菜单栏上Report->EditCurrent Language，不过这时打开的语⾔种类是针对 当前选择语⾔。

报告语⾔编辑器（Report Language Editor）由两个不同部分组成：根据语⾔类别和实体导航的左侧⽬录树与显⽰相关信息的右侧树 型视图。

左边⽬录树主要包含以下三个部分

<table>
  <tr>
    <th>类别</th>
    <th>描述</th>
    <th>翻译⽤途</th>
  </tr>
  <tr>
    <td>对象属性</td>
    <td>包含每个模型中所有和对象相关联的字符串，如对象 属性的名称，代码</td>
    <td>Cards,checks,list中对象属性的名称翻译</td>
  </tr>
  <tr>
    <td>报告标题</td>
    <td>包含每个模型中所有与报告项⽬相关联的字符串，如 组织单位注释等</td>
    <td>所有报告项⽬的标题翻译</td>
  </tr>
  <tr>
    <td>值映射</td>
    <td>包含所有与⽤于属性数值的关键字相关联的字符串， 如未定义，或不存在等</td>
    <td>Cards,checks,lists中对象属性数值的关键字翻译</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


Object Atrbutes和Report Titles分别包含PowerDesigner每个模型特定特征的种类。Value Maping类别则包含具有⼀个标准⼊⼜的 ⼦类别： Forms：Cards和Checks中的对象属性的关键字。 Lists：Lists中的对象属性的关键字。 PowerDesigner提供⾃带的报告语⾔源⽂件还是很符合语⾔习惯的，⼀般来说不⽤进⾏更改订制，但选择中⽂模板时会出现⼀些问题， ⽐较常见的就是如PrimaryKey,Foreign Key等翻译存在⼀些差异。下⾯以简体中⽂模板对我们⽰例系统的PDM建⽴系统数据字典报告⽂ 件。 已经完成的⽰例PDM关系图如下：

下⾯对每个数据表和各数据表的字段⽣成数据字典：

- （1）为了⽅便演⽰，我们选择新建空⽩的报告模板，只将表格清单（List of Tables），表格列清单（List of TableColumns表%PARENT%的列清单）和关系图表（Diagram）添加⾄报告项⽬⾯板（Report Items）。
- （2）右键选择List of TableColumns-表%PARENT%的列清单->Layout,弹出要显⽰对象列表。
- （3）在列表中选择需要显⽰的对象。 这时直接⽣成RTF⽂档，看看⽂档效果。


看到上述⽂档效果估计很多朋友都会很失望的，没关系，现在让我们⼀步步来完善！

- （1） 选择菜单栏上Tols->Resources->ReportLanguages…打开List of Report Languages（报告语⾔列表）窗⼜，这⾥我们选择 双击Simplified Chinese,以打开报告语⾔属性窗⼜。
- （2） 选择Object Atributes\PhysicalData Model\Column\Primary,将Value中”主要的”改为”主键”。
- （3） 选择ObjectAtributes\Physical Data Model\Column\ForeignKey，将Value”外来键”改为”外键”
- （4） 选择ReportTitles\Physical Data Model\Table\Columns list，将Value”表格%PARENT%的专栏清单”改为” 表%PARENT%的 列清单”.
- （5） 双击报告项⽬⾯板中的”Table-表格%ITEM%”对象，在弹出的编辑窗⼜中将”表格%ITEM%”改为”表%ITEM%”，如下图：
- （6） 当然还可以通过报告源⽂件编辑器进⾏其它报告项⽬显⽰⽅⾯的更改，同时也可以使⽤如其它常⽤软件中的查找替换功能，可 以在报告语⾔属性窗⼜找到相应⼯具。不过这时执⾏的是全局替换，使⽤前应⼩⼼。
- （7） 通常在进⾏报告语⾔属性进⾏更改之后，为了保证软件⾃带的报告语⾔源⽂件（.xrl⽂件）不发⽣变更，可以选择”Save As…” 命令。不过必须在语⾔报告属性窗⼜中执⾏，如下图：
- （8） 调整各属性列宽度，右键单击报告项⽬（Report Items）⾯板中”List of TableColumns-表%PARENT%的列清单”，在弹出菜 单中选择”Layout”，打开List Layout窗⼜，如下图：

现在调整Width列的数值就⾏了，⽀持百分⽐和实际宽度两种属性。现在可以看看⽣成的⽂档了，如下图⽰：

为了使显⽰效果更简洁点，不妨将其中⼤部分的FALSE不显⽰，TRUE也只⽤T代换，为此我们需要将系统的TRUE和FALSE进⾏转换， 需要在报告语⾔属性中更改映射表。

- （9） 在报告语⾔属性窗⼜中选择”ValuesMaping\Lists\Standard”,添加True和FALSE映射即可，如下图：


现在在进⾏⽂档⽣成，基本上满⾜正式⽂档要求。

当然关于模型报告还有很多细节问题，这⾥不能做到⼀⼀分析，可以在⽇后实际使⽤中慢慢发掘，毕竟运⽤才是关键！好了，这⼀⼩ 节就到此为⽌！

概念数据模型CDM(Conceptual Database Model)

以下我们要完成对⽰例论坛系统的数据库设计⼯作，⾸先让我们建⽴⽬标系统的概念数据模型(CDM)。 在进⾏相关CDM演⽰之前，让我先简要介绍概念数据模型（CDM）的相关概念。我们进⾏数据库设计时，⼀般都是概念层次

（Conceptual level）开始的。在概念层次上，你⽆须考虑数据库的实际物理执⾏细节。概念模型（CDM）描述了与任何软件或数据存 储系统⽆关的数据库整体逻辑结构，通常包含了与物理数据库⽆关的数据对象，提供了⼀种对⽤于运⾏企业或业务⾏为的形象化的表达 ⽅式。

CDM功能：

- （1）通过创建实体关系图表（E-R）来描述数据的组织结构。
- （2）能够校验数据设计的合理性。
- （3）⽣成指定了相应物理实现数据库的物理数据模型（PDM）
- （4）能够⽣成⽤UML标准描述CDM中对象的⾯向对象模型（ OM）


- （5）为在不同的设计阶段创建另⼀个模型版本，可以⽣成概念数据模型（CDM）


关于Palete⼯具⾯板中含义简介：

新建CDM

- （1） 选择File->New，打开New窗⼜，在左边模型选择列中选中Conceptual DataModel,单击OK，即确认创建概念数据模型。
- （2） 双击资源浏览窗⼜中新创建的CDM名称图标，打开CDM模型属性窗⼜，进⾏相关属性信息设置。如下图：


对刚创建的CDM进⾏详细之前有必要先说说有关实体属性命名问题。

PowerDesigner默认在CDM中不能存在相同名称的实体属性，这也是考虑到可能产⽣的⼀些如主键外键等名称冲突问题，但当我们 进⾏实际数据库设计时，可能会多次使⽤相同数据项（DataItem）便于理解各实体。为此需要对更改PowerDesigner相关设置。软件默 认为DataItem不能重复使⽤(重名)，需要进⾏以下操作： 选择Tols->Model Options,

在Model Seting设置⽬录中，将Data Item下的Unique Code取消选中即可，系统默认将Unique Code和Alow Reuse均选中。

同时该设置均是⾯向特定模型的，即针对当前模型有效，若希望在其它模型中也有此命名设置，则需要重新进⾏设置。不过在Check Model时，如果选择全部Check，则依旧会报DataItem重名的错误信息，这时需要我们在⼈为检查确认数据项⽆误时，可以在选择不对 DataItem不检查，如下图⽰：

各种数据类型对应匹配(这⾥只给出与SQL Server中的常⽤对应类型，其它DBMS可以使⽤类似处理)

实体及各类关系

实体（Entity）

- （1） 在新创建的CDM中，选择Palete⼯具⾯板中的Entity⼯具，再在模型区域淡季⿏标左键，即添加了⼀个实体图符。
- （2） 单击⿏标右键或单击⾯板中Pointer⼯具，使⿏标处于选择图形状态。
- （3） 双击新创建的实体图符，打开实体属性窗⼜，输⼊实体名称和代码。
- （4） 单击OK，即完成实体创建过程。 继续上述操作，创建多个实体，分别设置为不同名称，具体信息参考⽰例⽂档。


实体创建完成后资源浏览窗⼜中层次结构如下所⽰：

现在编辑各实体的详细内容，如属性组，实体间关系等。

实体属性（Entity Atributes）

- （1） 以User实体为例，打开实体User属性窗⼜，进⼊Atributes属性页，如下图⽰：
- （2） 单击属性窗⼜⼯具栏中Ad a Row⼯具，即在属性实体属性列表中添加了⼀个属性，同时设置该属性相关信息，如数据类型， 是否为主标识符，是否不可为空等。
- （3） 详细设置新添加的属性为UserID,作为系统唯⼀标识区别的⽤户编号，同时选择P,M,数据类型（DataType）选择Integer。如下 图：


- （4）对属性列进⾏更为详细的设置，可以通过单击对应属性列左边箭头，进⼊AtributeProperties窗⼜，可以进⾏更为精确详细的设 置，如数据上下限，精度等。如下图：
- （5）同时若要更改实体属性列表中显⽰的相关选项可以通过单击⼯具栏中Customize Columnsand Filter⼯具以打开Customize Columns and Filter窗⼜


只要在列表中选择想要显⽰的项⽬即可完成设置。

标识符（Identifiers）

标识符是能够⽤于唯⼀标识实体的每条记录的⼀个实体属性或实体属性的集合，CDM中的标识符等同于PDM中的主键（Primary Key）或候选键（Alternate Key）。每个实体⾄少要有⼀个标识符，若⼀个实体中只存在⼀个标识符，它就⾃动被默认指派为该实体的 主标识符（Primary Identifier）。 指定相应标识符

- （1） 在双击图表中对应实体以显⽰实体属性窗⼜。
- （2） 在当前实体属性窗⼜中选择Identifiers属性栏，如下所⽰：
- （3） 可以通过单击⼯具栏上Property ⼯具或双击所要选择的标识符栏，进⼊标识符属性编辑窗⼜。
- （4） 选择Atributes属性，可以看到当前标识符所关联的属性列表，如下图：
- （5） 单击⼯具栏中Ad Atributes⼯具，即可以进⾏为当前标识符添加属性。


关系（Relationship）

关系（Relationship）表⽰实体间的连接。如在⼀个⼈⼒资源管理系统的CDM中，员⼯是团队中的成员，关系”Member”连接了员⼯ （Employe）和团队（Team），这种关系表述了每个雇员在团队中⼯作且每个团队都由员⼯组成。 建⽴关系（Relationship）这⾥以⽤户实体（User）和帖⼦实体（Post）为例

- （1） 在Palete⾯板中左键单击Relationship⼯具
- （2） 在实体User上单击⿏标左键，按住不放，拖拽⿏标⾄实体Post上后才松开，这样即建⽴了User和Post之间的Relationship.
- （3） 单击⿏标右键或左键单击Palete⾯板上的Pointer⼯具，使⿏标返回⾄选择状态。
- （4） 双击图表中的刚建⽴的两实体之间关系（Relationship）以打开关系属性窗⼜，便于对关系进⾏详细定义。
- （5） 输⼊相应的Name和Code，选择Detail选项，进⼊如下属性编辑页：
- （6） 选择One-Many选项，因为User和Post为”⼀对多”关系，且每⼀条Post均对应有User，因此User to Post⾓⾊的基数 （Cardinality）下拉列表中选择”0,n”,在Post to User⾓⾊的基数列表中选择”1,。同时Role name中输⼊相应的⾓⾊名称。
- （7） 确定修改后，单击OK,即可在模型图表中显⽰新建的Relationship。
- （8） 若要⾃定义关系显⽰信息，可通过选择菜单栏中Tols->DisplayPreferences，打开Display Preferences窗⼜，在左边树型菜 单中选择Object->Relationship，这时即可在右侧选择你所要显⽰的项⽬了。


当然你也可以选择其它节点，实现对图符的显⽰属性设置。

各种类型关系（Relationship） 这部分是⽐较令⼈头疼的，不太好懂，需要投⼊较多时间研究。

⾃反关系（Reflexiverelationship） 是⼀种实体和它⾃⾝的关系。这⾥⽤员⼯的管理概念来表述管理⼈员管理员⼯，同时管理⼈员也属于员⼯范畴。

- （1） 左键单击Palete⾯板中Relationship⼯具
- （2） 在实体内单击⿏标左键且按住不放，将⿏标拖放⾄实体旁的空⽩位置后松开⿏标。
- （3） 再次单击实体即成功创建⾃反关系。 不过这时⾃反关系的图符不太雅观，可以通过先选定需要更改的图符，然后选择DisplayPreferences->Format，单击Modify以打开


Symbol Format窗⼜，然后更改Line Style属性中的Corners下拉框中选项

确认修改后，最后在单击DisplayPreferences窗⼜的OK按纽后会弹出Change Formats选择对话框，若只要将修改应该⾄当前的⾃反图 符，只需选择所选定图符（Selected symbols）即可。

依赖关系（Dependent relationship） ⽀配关系（Dominantrelationship） 强制关系（Mandatoryrelationship）

以上其它关系不再赘述，需要在实际使⽤过程加以运⽤才能加深进⼀步的理解，同时以上知识点和关系型数据库的理论知识密切相 关，PowerDesigner的这些功能只是对应于这些理论的⼀种运⽤映射。

关系（Asociation）

Asociation也是⼀种实体间的连接，在Merise模型⽅法学理论中，Asociation是⼀种⽤于连接分别代表明确定义的对象的不同实 体，这种连接仅仅通过另⼀个实体不能很明确地表达，⽽通过”事件（Event）”连接来表⽰。下⾯通过⽰例论坛系统的⽤户实体（User） 和论坛栏⽬（ForumColumn）实体的Asociation来讲解。⽰例论坛系统中通过⼀个Asociation来表⽰⽬标系统中论坛栏⽬对应的版主 关系，包括了属性创建时间（DateCreated）⽤于记录版主添加的时间。

创建Asociation

- （1） 在Palete⾯板中单击Asociation Link⼯具
- （2） 在实体User内单击⿏标左键且按住不放，拖放⿏标⾄另⼀实体ForumColumn上，松开⿏标左键，即在两实体间创建了 Asociation。如下图：
- （3） 双击模型图表中刚创建的Asociation图符以打开AsociationProperties窗⼜。
- （4） 输⼊Asociation的Name和Code，选择Atributes属性页，添加实体属性DateCreated，并设置相关属性，如下图：
- （5） 同时可以通过在模型图表中双击相应的Asociation Link来打开Asociation LinkProperties来编辑连接属性：


按类似⽅法可以创建论坛栏⽬实体（ForumColumn）和⾓⾊实体（Role）之间的Asociation。

继承（Inheritance）

Inheritance允许你定义⼀个实体为另⼀个更⼀般（常规）的特例。涉及到继承的实体之间有着共同相似的特征，但却是不同的。超 类（或⽗类）指那些包含共同特征的更⼀般的类，⽽特例则被成为⼦类型，包含了⼀些更为具体和特殊的特例。 关于继承⽅⾯的例⼦不少，稍具有⾯向对象观念的都应该能够理解，不再赘述。 ⽽PowerDesigner中关于继承⽅⾯的操作过程在这只作简要介绍：

- （1） 在Palete⾯板中单击Inheritance⼯具
- （2） 左键单击⼦类型，按住⿏标不放，拖放⾄⿏标⾄⽗类型实体图符中，松开⿏标，即完成了⼀个Inheritance Link的创建
- （3） 要再次添加另⼀⼦实体时，可以单击Inheritance⼯具，从半圆形图处拖动⿏标⾄另⼀⼦类型实体，然后松开⿏标即可。
- （4） 双击新创建的继承图符或实体之间的连接线即可打开弹出InheritanceProperties编辑窗⼜。
- （5） 输⼊相应Name和Code，完成基本设置，单击OK，即完成创建过程。


现在已经基本上完成了⽬标系统的概念建模过程，为此下⼀步我们需要校验已经设计好的模型，便于能够正确地转换为物理数据模型 （PDM）。 检验模型（Check）

- （1） 选择Tols->CheckModels，打开CheckModel Parameters窗⼜，如下图： 在这你可以对需要Check的项⽬进⾏⾃定义选择。
- （2） 确认选择后，单击OK,则PowerDesigner开始对模型进⾏检验。
- （3） 完成检验后，PowerDesigner会将检验结果在输出列表中显⽰出来


我们可以根据所列出的错误信息对模型进⾏修改，错误信息分别有Error,Warning,Automatic correction三种，同时只要经过检验后没有 Error⼀类的错误信息，我们就可以将该CDM转化为对应PDM。

⽣成PDM

当你从⼀个CDM⽣成PDM时，PowerDesigner将CDM中的对象和数据类型转换为PDM对象和当前DBMS⽀持的数据类型。 PDM转换概念对象到物理对象的对象关系如下表：

<table>
  <tr>
    <th>CDM对象</th>
    <th>在PDM中⽣成的对象</th>
    <th>备注</th>
  </tr>
  <tr>
    <td>实体（Entity）</td>
    <td>表（Table）</td>
    <td> </td>
  </tr>
  <tr>
    <td>实体属性（Entity Atribute）</td>
    <td>列Table Column）</td>
    <td> </td>
  </tr>
  <tr>
    <td>主标识符（Primary Identifier）</td>
    <td>根据是否为依赖关系确定是主键或外键</td>
    <td> </td>
  </tr>
  <tr>
    <td>标识符（Identifier）</td>
    <td>候选键（Alternate key）</td>
    <td> </td>
  </tr>
  <tr>
    <td>关系（Relationship）</td>
    <td>引⽤（Reference）</td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


同⼀个表中的两列不能有相同的名称，如果因为外键迁移⽽导致列名冲突，PowerDesigner会⾃动对迁移列重命名，新列名由原始实 体名的前三个字母加属性的代码名组成。主标识符在⽣成PDM中的主键和外键，⾮主标识符则对应⽣成候选键。

在PDM中⽣成的键类型取决于CDM中⽤于定义⼀个Relationship的基数和依赖类型。

- 1． ⾮依赖性⼀对多关系（Independent one-to-many relationships）

在⾮依赖性关系中，”⼀”端的实体主标识符将转化为：

- （1） 由关系中”⼀（one）”端的实体⽣成的表的主键（Primary key）
- （2） 由关系中”多（many）”端的实体⽣成的表的外键（Foreign key）。 如下图所⽰：


CDM中Independentone-to-many relationship

⽣成的PDM中的Independentone-to-many relationship

- 2． 依赖性⼀对多关系（Dependentone-to-many relationships）


在依赖性关系中，被依赖端的主标识符转化为主键，依赖端则产⽣⼀个与被依赖端主标识符同名称的字段同时作为同时作为依赖端的 主键和外键，如果依赖端实体中已经存在主标识符转化为主键，则该键同主键共同组成主键，同时作为外键。 CDM中Dependentone-to-many relationship

⽣成的PDM中的Dependentone-to-many relationship

- 3． ⾮依赖性多对多关系（Independent many-to-many relationships） 在⾮依赖性多对多关系中，各实体的主标识符（Primary key）迁移⾄⼀个新⽣成的连接表中都作为外键，同时共同组成这个新连接

表的主键，各实体的主标识符也转化为其所⽣成表的主键（Primary key）。下图所⽰CDM，每个雇员可以是⼀个或多个团队的成员，同 时每个团队也可能包含⼀个或多个的雇员。

CDM中Dependentone-to-many relationship

⽣成的PDM中的Dependentone-to-many relationship

- 4． ⾮依赖性⼀对⼀关系（Independent one-to-one relationships） 在⾮依赖性⼀对⼀关系中，如果没有定义⽀配⾓⾊（Dominant role）的⽅向，则各实体的主标识符均⾃动迁移转化为另⼀实体⽣成


的表的外键。

个⼈觉得在⽣成PDM过程中有关⽣成主键，外键等问题⽐较棘⼿，我⾃⼰在⽣成该⽰例论坛系统的PDM时就遇到这⽅⾯问题，后来 在多次对⼀些设计得⽐较优秀的开源系统进⾏反向⼯程，然后慢慢研究借鉴，发现⾃⼰在设计过程的⼀些问题，因此觉得这⽅⾯只有多 多研究才能逐渐得⼼应⼿。 准备差不多了，开始⽣成我们需要的PDM。

- （1） 选择菜单栏上Tols->GeneratePhysical Data Model弹出PDM Generation Options窗⼜，如下图：
- （2） 选择Generate PhysicalData Midel，在DBMS下拉列表中选择相应的DBMS，输⼊新物理模型的Name和Code.
- （3） 若单击Configure ModelOptions则进⼊Model Options窗⼜，可以设置新物理模型的详细属性。
- （4） 选择PDM GenerationOptions中的Detail页，设置⽬标PDM的属性细节。
- （5） 单击Selection页，选择需要进⾏转化的对象。
- （6） 确认各项设置后，单击确定。即⽣成相应的PDM模型。 ⽣成PDM后，我们可能还会对前⾯的CDM进⾏更改，若要将所做的更改与所⽣成的PDM保持⼀致，这时可以对已有PDM进⾏更新。


这时操作也很简单，Tols->GeneratePhysical Data Model，在打开的PDM Generation Options窗⼜中选择Update existing Physical Data Model，并通过Select model下拉框选择将要更新的PDM。如下图：

最后我们在CDM部分的⼯作应该就是根据所建⽴的概念模型⽣成⽂档了，⽂档是作为设计成果的输出，也⽤于开发⼩组成员交流的 媒介，其重要性不能忽视。这⽅⾯我们可以参考前⾯⽣成报告（Report）⽅⾯的内容。

物理数据模型

PDM基础

PDM是⽤于定义详细定义物理结构和数据查询的数据库设计⼯具。你可以在PDM中使⽤不同类型的图表，这取决于你所要设计的⽬

标数据库的类型。当今关于数据库⽅⾯⽐较热门的话题莫过于数据仓库，数据集市，OLAP，数据挖掘等内容了。⽽PowerDesigner对这 ⼏⽅⾯的设计都有很好的⽀持，分别⽀持了操作型数据库，数据仓库或数据集市，OLAP等类型数据库系统。相信⼤家都应该有所了解， 关于这⼏个概念就不再赘述，本⼩节内容主要是涉及操作型数据库的专题。

PDM DBMS PowerDesigner能够⽤于创建多种不同类型的DBMS，对于每种类型的DBMS，都包含⼀个标准定义⽂件⽤于在PowerDesigner和 DBMS中确定关联⽽提供⼀套接⼜。你可以修改装载在PowerDesigner中DBMS，对于每个你将要修改的初始DBMS，你都可以创建⼀个 相应的新DBMS。

新建PDM

你可以通过三种⽅式新建PDM

- （1） 直接创建新PDM
- （2） 使⽤模板创建新PDM
- （3） 通过现有基础创建新PDM，现有元素包括：数据库的反向⼯程，引⼊⼀Erwin模型，从现有CDM或 OM⾃动⽣成，从V6版本 的数据仓库分析模型迁移等。 下⾯只简要讲解概述其中⼀种PDM的创建过程：


- （1） 选择New，即打开创建模型选项窗⼜，如下图：
- （2）选择New model单选框。
- （3）选择左边模型列表中Physical DataModel，同时在DBMS下拉列表中选择相应类型DBMS（当然你也可以在后⾯的过程中更改 DBMS类型），
- （4）在First diagram中选择Physical Diagram,其中列表中MultidimensionalDiagram选项⽤于创建多维（Multidimensional）数据模 型。
- （5）单击”确认”，即完成PDM创建过程。


业务规则概念

业务规则是业务进程需要遵从的⼀些规则，它们可能是政府法令，客户需求或者内部的⼀些⽅针规范。业务规则通常来⾃于简单的观 测，如”客户可以通过拨打免费热线下订单”，⽽在设计过程中，我们就就需要将该过程分解成更加详细的描述。如当下订单时客户需要 提供什么样的信息或根据客户的信⽤度来判定客户能够订购多少产品。

业务规则能够规划并将模型⽂档化。如规则”⼀个雇员仅属于⼀个部门”可以帮你图形化地在⼀个雇员和⼀个部门之间建⽴联系。 业务规则⽤⼀种不易⽤图形化表达的信息补充模型图形，如有些规则以公式或验证规则的形式来表达⼀些特殊的物理概念，⽽这些技

术表达⽅式通常不能通过图形化形式显⽰出来。也可以将业务规则和PDM中具体对象联系起来，如果建⽴了验证规则与列或域之间的联 系，你就可以通过业务验证规则来检查参数。

创建业务规则（Busines rule）

- （1） 选择Model->BusinesRules，打开Listof Busines Rules窗⼜，列表显⽰当前模型中存在的业务规则，如下图：
- （2） 单击⼯具栏中Ad a Row⼯具或单击列表中⼀个空⽩⾏，即添加⼀个新业务规则。
- （3） 输⼊相应的Name和Code，单击Aply，提交业务规则的新建。
- （4） 双击所选择的业务规则或单击⼯具栏上Properties⼯具，打开业务规则属性（Busines RuleProperties）窗⼜，如下图所⽰：
- （5） Type下拉列表中选择相应的业务规则⽅式，待选类别有定义（Definition），事实（Fact），公式（Formula）,需求 （Requirement），验证（Validation），约束（Constraint）。但只有验证（Validation）和约束（Constraint）类型的业务规则才能 ⽣成到数据库中。


- （6） 选择 Expresion属性窗⼜，有两种类型的业务规则表达式，分别为Client和Server。其中Server部分为可以⽣成到数据库中， ⽽Client部分则仅⽤于模型⽂档的⽣成。
- （7） 设置完毕，单击”确认”，完成业务规则创建过程。


在PDM中应⽤业务规则

- （1） 在当前模型图表中双击将要应⽤业务规则的对象，以打开该对象属性窗⼜。
- （2） 选择Rules属性，列表中显⽰应⽤⾄该对象上的业务规则列表。
- （3） 单击⼯具栏中Ad Objects⼯具以显⽰业务规则列表。如下图：
- （4） 选择你想要添加的应⽤于该对象的业务规则，单击OK。
- （5） 在对象属性框中单击OK，即完成业务规则应⽤，若添加的是约束规则或验证规则，你可以通过Preview选项看到业务规则⽣成 的数据库代码。


建⽴物理图表（Physical Diagram） 由于PowerDesigner中CDM和PDM的很多操作类似，因此在后⾯的讲解中尽量简化⼀些操作细节。下⾯以PowerDesigner⾃带的⽰例模 型Project Management为例讲解，该⽂件位于安装⽬录Sybase\PowerDesignerTrial 1\Examples下的project.pdm⽂件。

域（Domain）

域（Domain）可以帮助你确定模型中的信息类型。域定义了⼀组对列可⽤的数值，对列应⽤域可以简化对不同表中列的数据类型标 准化⼯作。 创建域

- （1） 选择Model->Domains以打开域列表（List of Domains）窗⼜。
- （2） 单击⼯具栏中Ad a Row⼯具，新建域。
- （3） 输⼊相应的Name和Code，这⾥我们输⼊Identifier和ID。
- （4） 单击Aply提交Domain的创建，单击⼯具栏中Properties⼯具以打开Domain Properties窗⼜，如下图：
- （5） 选择数据类型（Data type），设置Length等属性，同时可以选择Standard Checks属性页以编辑详细约束。
- （6） 单击OK，确认修改。 这样就已经完成域Identifier的创建过程。


修改Domain属性

- （1） 选择Model->Domains以打开域列表（List of Domains）。
- （2） 单击你想要修改的域对象，使⽬标域对象处于选择状态。
- （3） 双击该域对象或单击⼯具栏中Properties⼯具以打开域属性（Domain Properties）窗⼜，现在可以对域属性进⾏更改了。
- （4） 确认更改后，点击”应⽤”，若该域已经被某些列使⽤，则会弹出下列窗⼜：

该窗⼜提⽰询问是否想要修改应⽤了该域的列的域属性。你可以选择你想要更新哪些使⽤该Domain的模型元素。

- （5） 单击”确认”，完成修改。 同时我们也可以设置强制执⾏Domain的修改，即在域（Domain）定义发⽣更改时，数据类型就会强制⾃动改变。步骤如下：选择 Tols->ModelOptions，打开Model Options窗⼜，选择左边树形⼦菜单中Column&Domain选项，如下图：


选择Enforcenon-divergence，再选择相应模型元素即可，这样每次Domain发⽣更改后，对应使⽤了该Domain的模型元素就⾃动发⽣ 更改。

创建表（Table）

- （1） 左键单击Palete⾯板中Table⼯具
- （2） 左键单击模型图表空⽩区域以在模型图表中新建Table图符。
- （3） 单击⿏标右键或单击Palete⾯板中Pointer⼯具，使⿏标处于选择状态。
- （4） 左键双击模型图表中刚创建的Table图符以打开Table属性窗⼜，如下图：
- （5） 输⼊相应表的名称和代码。
- （6） 其中Number选项为物理数据库中表的记录的⼤概估计，⽤于后述的估计数据库的⼤⼩规模；Generate选项表⽰是否在物理数 据库中⽣成该表。这⾥我们将Number设置为1 0，勾选上Generate。
- （7） 单击”确定”，即完成表Employe的创建。


添加编辑列

- （1） 打开表Employe的属性窗⼜，选择Columns属性页，如下所⽰：
- （2） 这⾥我们需要使⽤域Identifier，前⾯我们已经完成了域的创建⼯作，这⾥可以直接应⽤于列中。先设置Columns属性列表中显 ⽰Domain，单击⼯具栏上Customize Columnsand Filter⼯具，弹出Customize Columns and Filter窗⼜，在列表中选择Domain，如 下图：
- （3） 单击OK,则这时Columns属性页中显⽰Domain属性。
- （4） 编辑列Employe属性，在Domain下拉列表中选择Identifier，如下图：
- （5）单击确认，即将域（Domain）Identifier应⽤⾄该列。


定义引⽤（Reference）

引⽤（Reference）是⼀个⽗表（parent table）和⼦表（child table）之间的连接，它定义了在数据表各列⽤于主键，候选键，外 键或⽤户指定列之间的完整性约束。当两个表中的数据列通过了引⽤（Reference）连接时，⼦表中的该列的每个值都对应了⽗表中对应 列的⼀个相同的值。

在⼀对引⽤关系中，数据列之间通过连接（Join）连接，根据在主键/候选键中列的数⽬，指定列的数⽬，⼀个引⽤关系中可能包含 ⼀个或多个连接（Join）。

建⽴引⽤（Reference）

- （1） 选择Palete⾯板中Reference⼯具
- （2） 在模型图表区域，左键单击⼦表图符并按住⿏标不放，拖动⿏标⾄⽗表图符，松开⿏标，即在两表之间建⽴了引⽤关系。
- （3） 单击Palete⾯板中Pointer⼯具或单击⿏标右键使⿏标处于选择状态。
- （4） 双击模型图表中的引⽤（Reference）连接图符以打开引⽤属性（ReferenceProperties）窗⼜，如下图：
- （5） 输⼊相应的引⽤（Reference）Name和Code。 （６）定义引⽤连接（Join），选择Joins属性页，如下图所⽰：


（７）在Parent key选项列表中选择相应的⽗表键，此时列表中显⽰出当前连接（Join）所连接的⽗表列和对应的⼦表列。 （８）现在可以对对应于每个⽗表列（Parent TableColumn）的⼦表列（Child Table Column）进⾏选择更改。

重建引⽤（Rebuildingreferences）

有时我们进⾏反向⼯程时可能不会将所有的对象都添加进去，这时可能会遇到引⽤冲突问题，即在已经添加进反向⼯程的项⽬中可能 包含⼀些具有引⽤关系的表，⽽这些引⽤关系与没有添加进反向⼯程中。这时我们可以借助PowerDesigner提供的重建引⽤ （Rebuildingreferences）功能，对引⽤进⾏选择重建。 （１） 选择Tols->RebuildObjects->Rebuild References，弹出重建引⽤窗⼜。

（２） 在General属性页中选择重建引⽤⽅式（其中Delete and rebuild⽅式为删除所有现有引⽤，再根据匹配键列创建新的引⽤； Perserve⽅式为保留所有现有引⽤，且根据匹配键列建⽴新的引⽤）。 （３） 选择Selection属性页，根据需要选择你要重建引⽤的表。 （４） 确认选择后，单击”确认”按钮，若你选择的重建⽅式为Delete and rebuild，则会弹出如下确认对话框：

（５） 确认重建，单击”是(Y)”即可完成重建引⽤。

引⽤完整性（ReferentialIntegrity） 引⽤完整性是管理数据主键，候选键和外键之间数据⼀致性的⼀系列规则，它定义了当你更新或删除⽗表中的⼀个被引⽤的列，或从

⽗表中删除⼀条包含被引⽤的列的数据记录时要发⽣的动作。有以下两种⽅式实现引⽤完整性： Declarative 引⽤完整性通过详细引⽤来定义，当引⽤⽣成⽬标DBMS，它评估引⽤的正确性并⽣成相应的错误消息。 Usingtri gers 通过在引⽤属性窗⼜中定义的基于完整性约束的触发器（Tri ger）来实现引⽤完整性约束。触发器⽤于衡量引⽤的

正确性并⽣成适当的⽤户⾃定义错误信息。 注：对于⽬标数据库你可以作为⽣成⽬标数据库的选项⽽定义引⽤完整性，但并不是所有的类型的DBMS都⽀持使⽤引⽤完整性作为⽣ 成数据库的选项，此时当你⽣成这些类型DBMS的SQL脚本时，其中不会包含引⽤完整性的定义。

定义引⽤完整性 （１） 双击模型图表中的引⽤图符以打开引⽤属性窗⼜。 （２） 选择Integrity属性页，显⽰出其中⽤于引⽤完整性约束的⼀系列可设置选项，如下图：

（３） 上图中各设置项⽬这⾥就不再多说。

视图（View）

为图表中已选定对象创建视图（View） （１） 在模型图表中选择⼀个以上的表（Table）或视图（View） （２） 选择Tols->CreateView，这时可以看到已经模型图表中会出现⼀个视图图符，其中显⽰出了所选定的表和视图中所有字段，如 下图：

（３） 双击刚创建的视图图符以打开视图属性（View Properties）窗⼜。

（４） 输⼊相应的Name和Code,同时可以通过不同属性页对视图属性进⾏进⼀步详细设计。 （５） 单击”确认”，完成视图创建过程。

先建⽴空⽩视图，再选择所需表和视图 （１） 选择Tols->Create View，打开如下选择窗⼜：

（２） 在列表中选择你所要添加的表和视图，单击OK，确认添加即可完成创建，这时在模型视图中出现视图图符。其它操作略。

为视图使⽤扩展依赖（Extended Dependencies）

扩展依赖是物理图表对象之间的连接，这些连接有助于让模型对象之间的关系更加清晰，⽽不会被PowerDesigner解释或检查，主要 是⽤于⽂档化⽽建⽴的。 （１） 选择Palete⾯板中Link/ExtentedDependency⼯具 （２） 在模型图表中先单击⽬标视图，拖放⿏标⾄其有关联的表和视图上，即将视图和表之间建⽴了联系，如下图⽰：

注：使⽤扩展依赖仅仅是为了⽂档化模型，使对象关系更为清晰⽽已。

定义视图⽣成次序

你可以通过使⽤扩展依赖来定义视图的⽣成次序，扩展依赖是PDM对象之间的⾃由连接，这种连接能够使模型对象之间的关系更加 清晰。正如前⾯内容所说，通常这些连接都仅仅⽤于⽂档⽣成⽽不会被PowerDesigner解释和检查。然⽽，如果指定视图之间的扩展依 赖的Stereotype为《DBCreateAfter》时，则这些连接在⽣成数据库脚本时也会被解析。

如果创建⼀个⾃反，循环的扩展依赖，且其Stereotype⽅式为《DBCreateAfter》，则在检验模型时会提⽰错误信息。如果忽略该错 误，则视图会依照字母顺序创建，如果不考虑其⽣成次序，数据库在⽣成视图时又可能会导致错误。

例如，我们为表STORE创建视图DEPARTMENT STORE，其SQL Script为：selectSTORE.STOR_ID,STORE.CITY,SOTRE.STATE from STORE。同时为了仅显⽰公司库存数据的⼀部分我们需要创建在视图DEPARTMENT STORE基础上创建另⼀视图COMPUTER COUNTER取出其中⼀部分信息。

在默认情况下会按字母顺序⽣成视图，则COMPUTER COUNTER会⽣成失败，由于它所依赖的视图DEPARTMENT STORE还没有被 ⽣成。为解决这个问题，你可以在这两个视图之间创建Steretype⽅式为《DBCreateAfter》的扩展依赖，如下图：

注：《DBCreateAfter》选项只有在两个视图对象之间才有效，扩展依赖所连接的两对象皆为视图（View）时才可选 《DBCreateAfter》，否则为不可选状态。 步骤： （１） 选择Palete⾯板中Link/ExtendedDependeny⼯具 （２） 左键单击依赖视图（Dependent View）拖动⿏标⾄另⼀视图上，松开⿏标，即在两视图之间建⽴了扩展依赖连接，如下图：

（３） 双击其中的扩展依赖连接线线，弹出视图属性窗⼜的扩展依赖属性页，列表中显⽰相关扩展依赖。 （４） 在对应的扩展依赖⾏单击Steretype列的选择箭头，选择《DBCreateAfter》即可。

从上图中可以看到，在列表第⼀⾏的Stereotype下拉列表中没有《DBCreateAfter》选项，只有视图之间的扩展依赖连接才⽀持 《DBCreateAfter》选项。

查询（Query）

我们通过对表和视图进⾏查询，再通过对查询所得表进⾏关系运算来构建视图，PowerDesigner中通过视图属性窗⼜中SQL Query 属性页详细定义每个查询，每个SELECT查询都显⽰在Query的下拉列表中。其中每个视图可以有⼀个或多个查询组成，各查询之间通过 关系运算（如Union，Union Al，Intersect等）构成整个视图。

编辑定义查询（Query） （１） 在模型图表中双击视图图符以打开视图属性窗⼜。 （２） 选择SQL Query属性页，如下图：

（３） 若要新建查询，则单击Query下拉列表右边的Ad a Query⼯具，同时也可以通过单击Ad a Query右边的箭头选择各个查询之 间的连接关系，如下图⽰：

（４） 在单击Ad a Query⼯具后弹出查询属性（Query Properties）窗⼜，如下图：

这时通过选择不同的属性页可以对查询进⾏详细编辑。

（５） 接（３）步，同时可以单击视图属性（View Properties）窗⼜中Query下拉列表右边的Edit with SQLEditor⼯具以打开SQL Editor，如下图：

现在可以很轻松地对该视图的查询进⾏编写订制了。

这⾥只是简要介绍PowerDesigner中关于Query⽅⾯的内容，实际上涉及内容⾮常多，不能⼀⼀列举，相信熟悉数据库操作的朋友应 该能很快掌握。

在物理模型中定义视图引⽤（View References）

视图引⽤是⽗表／视图和⼦表／视图之间的连接，也包括⽗字段和⼦字段之间的连接（Join），视图引⽤（View Reference）可以 在两个视图或视图和表之间创建，⽽不能⽤于连接两个表（Table），视图引⽤不会⽣成在⽬标数据库中，

创建视图引⽤（View Reference） （１） 在Palete⾯板中单击Reference⼯具。 （２） 在模型图表中，单击⼦表或⼦视图并按住⿏标，拖动⿏标⾄⽗表或⽗视图区域中，松开⿏标。 （３） 单击Palete⾯板中Pointer⼯具或单击⿏标右键，使⿏标处于选择状态。 （４） 在模型图表中双击刚创建的引⽤图符以打开视图引⽤属性（View ReferenceProperties）窗⼜，如下图：

（５） 输⼊Name和Code后，选择Joins属性页，现在可以根据⼯具栏中各⼯具对其中连接（Join）设置。 （６） 确认设置，单击”确定”。

触发器（Tri gers）和存储过程（Procedures）

注：后述内容不再包含过多的数据库设计理论知识。 触发器（Tri gers） 触发器模板（Tri ger templates）

触发器模板是对创建触发器的⼀种预先定义形式，PowerDesigner集成了针对各种所⽀持的DBMS的⼀系列模板，

使⽤触发器（Using tri gers） 触发器是⼀种能灵活地⽤于数据表管理和修改的⼯具，其中还可以包含变量，针对不同类型的DBMS能定义相同类型的⼀些触发器。

⾃动创建触发器

我们可以为模型中⼀个或多个数据表⾃动创建触发器（tri gers），这些触发器的创建是针对那些定义了引⽤完整性的引⽤ （References），以及那些值依赖于⼀个Sequence的列。

那些使⽤了触发器模板但是已经被修改过，以及⽤户定义的触发器在重建触发器的时候都不会被创建，所有通过RebuildingTri gers 重建的触发器都是根据DBMS和⽤户⾃定义触发器模板⽽创建的，其中所有已经被选择的数据表都会被创建INSERT触发器，⽽UPDATE 和DELETE触发器则会根据相应触发器的引⽤完整性来确定创建。当你更改⽬标DBMS类别，如从Sybase转变为Oracle或IBM DB2，这 时我们定义的触发器就需要重新创建。 你可以选择以下两种模式重新创建触发器：

Delete和Rebuild 删除已经存在的触发器（不包括⽤户⾃定义触发器）然后根据引⽤完整性和数据列的Sequence重新创建触发器。 Preserve 保留现有⽤户已经定义的触发器然后根据引⽤完整性和数据列的Sequence重新创建其它触发器。

创建触发器操作步骤： （１） 选择Tols->RebuildObjects->Rebuild Tri gers。即打开Rebuild Tri gers窗⼜，在General属性页中显⽰出现有触发器模板的 名称。

（２） 如果你想要删除现有的触发器再重建，请选择Delete and Rebuild单选框；或选择Preserve单选框确保重建触发器时能够保存现 有触发器。 （３） 同时在General属性页中选择你想要创建触发器。 （４） 展开所选择的触发器节点。 （５） 展开相应的触发器模板节点，其中显⽰出包含在该触发器模板定义中的模板项。 （６） 选择你想要包含在⽬标触发器中的模板项（template item）。 （７） 同样展开其它节点进⾏相同操作以选择需要包含的模板项。 （８） 选择Selection属性页，会列出当前模型中的数据表（Table）。 （９） 在列表中选择你希望创建触发器的表，单击”确认”，完成创建过程。

⼿动创建触发器 （１） 在模型图形中双击你想要创建触发器的表的图符，弹出对应表属性窗⼜。 （２） 选择Tri gers属性页，显⽰出已经定义的触发器列表。 （３） 单击⼯具栏中Ad a Row⼯具，即在列表中添加了⼀个Tri ger。 （４） 输⼊相应的Name和Code，单击”应⽤”，提交触发器的创建。 （５） 双击刚创建完的Tri ger⾏，弹出触发器属性窗⼜（Tri ger Properties）。 （６） 打开Definition属性页，可以在模板下拉列表中选择相应的触发器模板，如下图：

你也可以通过选择下拉列表中”None”选项创建触发器，这时不使⽤任何触发器模板，你需要在⽂本框输⼊详细的触发器定义。 （７） 进⾏详细的触发器定义内容修改。 （８） 在Order下拉列表中选择你希望该触发器执⾏的顺序。 （９） 单击”确认”完成创建定义过程。

定义存储过程（Stored Procedures）与函数（Functions）

为存储过程和函数定义模板 （１） 选择Database->EditCurrent DBMS以打开DBMS属性窗⼜，如下图⽰：

（２） 展开DBMS树形视图中的Script节点，继续展开Objects节点。 （３） 展开Procedure节点。 （４） 单击CustomProc条⽬以编辑存储过程模板，或单击CustomFunc条⽬以编辑函数模板。 （５） 输⼊你想要进⾏的更改，单击”确认”即完成模板定义过程。

创建存储过程和函数 （１） 单击Palete⾯板中Procedure⼯具。 （２） 在模型图表空⽩区域单击⿏标左键，即在图表中添加了⼀个Procedure图符。 （３） 单击Palete⾯板中的Pointer⼯具或单击⿏标右键使⿏标⿏标处于选择状态。 （４） 双击刚添加的图符，弹出存储过程或函数属性窗⼜，如下图：

（５） 输⼊相应的Name和Code后，选择Definition属性页，在下拉列表中选择需要创建的类别：Procedure或Function。 （６） 在⽂本框中输⼊详细的Procedure或Function定义信息，你也可以通过使⽤⼯具栏中的⼀些脚本项来编辑定义。 （７） 单击”确认”，完成存储过程或函数的创建过程。 当然，您也可以同时使⽤菜单栏上Model->Procedure来完成创建过程，这⾥不再赘述。

将存储过程与表关联

如果当前DBMS⽀持存储过程的话，你可以使存储过程与数据表关联，该特性允许更新表或从表中读取数据。当我们将PDM向 OM 转换时，与数据表关联的存储过程就会转化为所⽣成的类中的Stereotype为《procedure》的操作。通过将存储过程与数据表相关联， 你可以定义所⽣成的类中的操作。

当我们将 OM转化为PDM时，Stereotype为《procedure》的类操作将转化为与最终⽣成的表相关联的存储过程。

步骤： （１） 打开⽬标数据表的属性窗⼜。 （２） 选择Procedure属性页。 （３） 单击⼯具栏中Ad Objects⼯具以打开对象选择（Selection）窗⼜. （４） 在现有存储过程列表中选择你所需要关联该表的存储过程，单击”OK”，则相应存储过程已经添加进存储过程列表中。 （５） 单击”确认”完成关联过程。

⽣成触发器和存储器脚本

在PowerDesigner中，我们可以通过直接使⽤ODBC或间接使⽤脚本创建或修改数据库触发器和存储过程，使⽤脚本时将⽣成脚本⽂ 件，若使⽤ODBC创建或修改数据库触发器和存储过程则需要直接与DBMS连接。

定义存储过程的⽣成顺序

这⽅⾯的内容同前⾯讲解的关于视图创建顺序问题相似，我们可以通过使⽤扩展依赖（Extended Dependency）定义⽣成存储过程 的次序，扩展依赖是PDM对象之间的⾃由连接，这些连接使模型对象之间的关系更加清楚。通常这些连接不会被PowerDesigner解释或 检查，⽽仅仅是⽤于⽣成⽂档。然⽽，如果你指派⼀个存储过程之间的扩展依赖的Stereotype为《DBCreateAfter》，那么在⽣成数据 库过程中该扩展依赖也会被分析。

扩展依赖所起始的存储过程是依赖过程⽽连接终点的过程为影响（存储）过程,这时依赖过程可能使⽤到影响过程，所以影响过程需 要在依赖过程之前⽣成，否则PowerDesigner会按照字母顺序⽣成脚本，则可能会导致错误。这时我们就需要在依赖过程和影响过程之 间设定相应的⽣成顺序。

步骤： （１） 先需要建⽴存储过程之间的扩展依赖关系，单击Palete⾯板中Link/ExtendedDependency⼯具。 （２） 在模型图表中单击依赖（存储）过程图符，拖动⿏标⾄影响（存储）过程，松开⿏标，则建⽴两存储过程之间的扩展依赖关系， 如下图：

（３） 单击Palete⾯板中的Pointer⼯具或单击⿏标右键使⿏标处于选择状态。 （４） 在图形模型中双击依赖（存储）过程或双击扩展依赖图符以打开存储过程属性窗⼜。 （５） 选择ExtendedDependencies属性页，在Stereotype下拉列表中选择《DBCreateAfter》选项，如下图：

（６） 单击”确定”，完成创建过程。

⽣成触发器和存储过程脚本 （１） 选择Database->GenerateTri gers & Procedures，弹出Tri gers and Procedures Generation属性窗⼜，如下图：

（２） 选择相应的⽂件保存路径，输⼊对应⽣成的脚本名称，选择Script Generation选项。 （３） 设置相应的参数。 （４） 选择Options属性页，选择相应的选项。 （５） 选择Selection属性页，选择需要对⽣成触发器和存储过程的模型；如过你需要为属于某⼀特殊⽤户的表⽣成触发器脚本，可以 在⽤户下拉列表中选择相应的⽤户；选择需要⽣成触发器和存储过程的对应数据表。 （６） 单击”确认”，PowerDesigner随即开始⽣成脚本，Output窗⼜会显⽰⽣成过程信息，脚本最后⽣成结果会在Result窗⼜中显⽰， 其中列出脚本⽂件的路径。

直接在数据库中⽣成触发器 PowerDesigner能够通过ODBC连接数据源以直接在数据库中⽣成触发器。 步骤： （１） 选择Database->GenerateTri gers & Procedures。 （２） 在Tri gers and ProceduresGeneration窗⼜中的Generation选项中选择ODBC Generation单选框。 （３） 经过其它类似⽣成脚本的设置后，单击”确认”。 （４） 随即弹出Conect to an ODBC Source对话框，如下图：

（５） 在下拉列表中选择相应的机器数据源或选择⼀个数据源⽂件。 （６） 输⼊⽤户ID和pasword。 （７） 单击”Conect”，消息窗⼜会显⽰对应的⽣成过程信息。 （８） 单击”确认”，完成创建过程。

数据库的创建与修改

前⾯也已经接触过相关与数据库操作例⼦,PowerDesigner中我们可以直接⽣成数据库脚本(Script),也可以直接通过ODBC与相关 DBMS中数据源相连接,从⽽可以利⽤PowerDesigner提供的强⼤功能实现数据库的创建和修改操作．

通过ODBC操作数据库 步骤： （１） 选择Database->Conect，弹出Conect to an ODBCSource窗⼜，如下图：

（２） 在上⾯窗⼜中你可以对所想要连接的数据源进⾏设置，如果想重新设置数据源，可以通过单击窗⼜中Setup按纽；若是选择File data source选项框，则打开数据⽂件(.sql)选择窗⼜；同时若需要设置数据源连接属性，则可通过单击Ad按纽对可选数据源进⾏设 置，弹出如下窗⼜：

（３） 设置完毕，单击”确定”，返回Conect to an ODBCsource窗⼜，输⼊相应的登录信息，单击”Conect”按纽，完成ODBC数据 源连接设置．

定制脚本

有时我们需要对所设计的数据库脚本添加⼀些诸如版权，创建⽇期，注释等信息，对于⼀些⼤型系统还可能需要对相应数据表视图等 对象也添加相应的信息，这也在某种程度上⽅便了开发成员之间的交流．下⾯我们开始定制过程：

为数据库创建添加Begin and EndScripts （１） 选择Model->Model Properties，打开Model Properties窗⼜，如下图：

（２） 在General属性页中单击Database⽂本框旁的Create⼯具，弹出DatabaseProperties窗⼜，如下图：

（３） 输⼊相应的Name和Code，选择Script属性页，现在可以通过切换Begin和End页定制对应信息． 同理我们可以通过打开数据表的属性窗⼜－＞选择”Script”属性页为数据表插⼊Begin and EndScripts，具体操作不再赘述．

⽣成SQL脚本 （１） 选择Database->GenerateDatabase，弹出Database Generation窗⼜，其中包含⽣成数据库的各种参数选项，如下图：

（２） 选择相应的脚本⽂件存放⽬录，并输⼊相应的脚本⽂件名称． （３） 在Generation选项栏中选择Script general单选框，确认⽣成数据库⽅式为直接⽣成脚本⽂件． （４） 勾选上One file on，表⽰所⽣成脚本将包含于⼀个⽂件中，否则PowerDesigner会为⽣成的每个不同表格都单独⽣成⼀个脚本⽂ 件． （５） 调整设置当前Tables & Views属性页中各选项参数． （６） 同样通过选择不同属性页分别设置Keys &Indexs,Database,Options等⽣成脚本参数． （７） 选择Selection属性页，如下图：

（８） 在对象列表中选择需要⽣成脚本的对象． （９） 单击”确定”，完成⽣成脚本配置过程． PowerDesigner开始执⾏脚本⽣成过程，这时输出窗⼜会显⽰相应的⽣成过程信息，最后弹出Result窗⼜，如下图：

这时让我们利⽤已经⽣成的脚本⽂件来创建数据库，这⾥我们使⽤的DBMS是MS SQLServer2 0．现在我们新建⼀数据库FreZone， 在查询分析器中打开当前新建的FreZone数据库下，执⾏刚⽣成的脚本⽂件，发现查询分析器输出窗⼜报告以下错误信息：

⽽在左边树型资源窗⼜中刷新，发现已经成功创建了相应的数据库对象．让我们回头看看所⽣成的脚本⽂件内容：

这时应该差不多清楚了，所⽣成脚本中先删除所有的外键，同时检查原数据库中是否存在相同对象，若存在则将其删除，所以当我们 第⼀次执⾏脚本时数据库中并不存在相应数据库对象，才会报告”不存在 ….”等错误信息，同时每个执⾏过程后都加”go”，确保某段 代码没能成功执⾏时不会影响后续代码的执⾏．

直接创建数据库（使⽤ODBC） （１） 选择Database->GenerateDatabase． （２） 选择相应的脚本存储⽬录，输⼊相应脚本名称． （３） 在Generation选项栏中选择ODBC generation单选框． （４） 后续其它参数设置同前⾯⽣成脚本⽂件类似，不再赘述． （５） 单击”确定”按纽，会弹出Conect to an ODBCData Source窗⼜，如下图：

（６） 选中Machine datasource单选框后在下拉列表中选择对应的数据源；或选中File data source并选取对应数据库⽂件． （７） 输⼊对应⽤户登录信息，单击”Conect”，确认连接，PowerDesigner随即开始脚本⽣成进程，完毕后弹出Execute SQL Query 窗⼜，如下图：

（８） 单击”Run”，即开始数据库创建过程．

在PDM中进⾏反向⼯程（Reverse Enginering）

从脚本⽂件反向⼯程数据对象 步骤： （１） 新建PDM，这⾥⽰例演⽰选择的DBMS为MS SQLServer2 0。 （２） 选择Database->ReverseEnginer Database，弹出Database Reverse Enginering窗⼜，如下图所⽰：

（３） 选择Using script files单选框，即为从脚本⽂件执⾏反向⼯程。

（４） 单击⼯具栏上Ad Files⼯具，选择要进⾏反向⼯程的脚本⽂件；同时你也可以添加多个脚本⽂件，但需要注意的是：应该将触 发器或存储过程脚本⽂件放在数据表脚本⽂件后⾯，因为反向⼯程时需要先执⾏数据表脚本。因此需要正确调整各种脚本⽂件顺序，否 则可能导致触发器不能正确地进⾏反向⼯程。 （５） 在Options属性页中设置反向⼯程选项。 （６） 单击”确定”，完成创建过程。 这时会弹出进度窗⼜显⽰当前反向⼯程进度，完成后会在当前PDM中产⽣相应的模型对象。

从ODBC数据源反向⼯程 （１） 新建PDM，这⾥也将DBMS设置为MS SQLServer2 0。 （２） 选择Database->ReverseEnginer Database，弹出Database Reverse Enginering窗⼜。 （３） 这时我们不再选择Using script files，⽽是选择Using an ODBC datasource单选框，即设置是从ODBC数据源进⾏反向⼯程。 （４） 单击Conect to an ODBCSource⼯具以显⽰Conect to an ODBC Source对话框，如下图：

（５） 单击Machine datasource单选框，从数据源下拉列表中选择相应要进⾏反向⼯程的数据源；者选择File data source单选框，通 过Select a File DSN⼯具以在相应⽬录选择相应DSN⽂件。 （６） 输⼊对应⽤户ID和密码。 （７） 单击Conect，这样所选定的ODBC数据源就会在Database ReverseEnginering窗⼜中，如下图：

（８） 可以选择Reverse usingadministratorʼs permi sions选项框以使⽤数据库管理员⾝份进⾏反向⼯程。 （９） 接下来通过选择Options和Target Models属性设置反向⼯程选项。 （１０） 单击”确定”，即开始执⾏反向⼯程过程，弹出ODBC ReverseEnginering窗⼜，其中列出相应的对象，其中只有数据 表和触发器是被默认选中的，如下图：

（１１） 同时还可以通过⼯具上⼀些快捷⼯具对其中要进⾏反向⼯程的数据库对象进⾏订制。 （１２） 选择你想要添加进反向⼯程的数据库对象，可切换不同的Tab页进⾏选择，确认选择后，单击”OK”，完成创建设置。 可以看到PowerDesigner已经开始反向⼯程过程，同时输出窗⼜会显⽰出当前进程信息。 同时若当前PDM中已经存在模型对象，这时进⾏反向⼯程，则可以根据需要进⾏模型合并，覆盖等操作。

总结

<table>
  <tr>
    <th>同时需要提⼏点：PowerDesigner提供的反向⼯程功能（Visio也提供了类似功能）很不错，如当我们研究⼀较⼤系统的成型代码 时，⾸先需要了解其中设计⽅案，这时可以先对其数据库进⾏反向⼯程⽣成对应的PDM；反向⼯程也使异构数据库之间的转化更为简 单。同时为了更快捷的学习PowerDesigner，也可进⾏各种模型的转化，从⽽更快掌握PowerDesigner的强⼤功能。<br><br>以上只是简要介绍了PowerDesigner的部分功能，其它如⽔平分割，垂直分割，合并等⽅⾯数据库优化内容就不再罗嗦了，<br><br>PowerDesigner只是为提供的⼀种使分析设计⼈员更加便捷的⼯具，实际需要设计出良好的数据库系统好需要⽤户掌握扎实的相关理 论知识，关系代数，元组演算，域演算等；同时需要具备相关规范化理论⽅⾯的基础知识（范式，函数依赖，属性闭包等），同时针 对不同的⽬标系统灵活选⽤不同的设计⽅案！</th>
  </tr>
</table>


