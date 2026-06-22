- 1、录制脚本中包含中⽂，出现乱码怎么办？

Server

Web

⽇志

- 2、录制到的脚本是空⽩的
- 3、插⼊⽂本检查点步骤时，使⽤web_reg_find，通常TextPfx和TextSfx中会包 含双引号，需要进⾏转义（⽤斜杠），例如：
- 4、使⽤web_image_check插⼊图⽚检查点时需要主要设置Run-Time Seting中 的Enable Image and text check选项：
- 5、 往往需要准备⼤批量的数据，⼤批量数据的⽣成⽅法有很多种，常⻅ 的有：


把录制选项中的Suport charset选中UTF-8 录制脚本后，切换到树视图中，打开相应的脚本⻚⾯。在右侧的PageView中录制的脚本呈现中⽂版 式，但是当切换到 Response中，所有的中⽂全部换成的乱码，如“勌缞仫訆”。 原因是服务器端没有把响应的编码设置为gb2312 在 IS中找到 .Config⽂件，在<system.web>….</system.web>节加⼊<globalization requestEncoding="gb2312" responseEncoding="gb2312" fileEncoding="gb2312"/>后再次录制脚 本，乱码变中⽂。

HTP协议，如果录制的时候产⽣乱码则在Recording Options -> Advanced -> Suport charset中勾 选UTF-8。 如果回放的时候 中出现乱码则在Run-time Setings -> Internet Protocol Preference -> Advanced Options弹出对话框中的General列表中选择Convert from/to UTF-8为Yes

有可能是由于录制的URL地址采⽤的是localhost的问题，改成分配的IP地址或127.0.0.1试试。

web_reg_find("Search=Body", "SaveCount=Welcome", "TextPfx=欢迎<a clas=\"drop\" id=\"viewpro\" nMouseOver=\"showMenu(this.id)\">", "TextSfx=</a>", LAST);

使⽤web_find函数插⼊⽂本检查点也⼀样要做此设置

# 性能测试

（1）编写 语句来插⼊数据

SQL

（2）使⽤DataFactory等专业的数据⽣成⼯具 （3）通过 录制回放的⽅式重复执⾏⽣成⼤批量数据

LoadRuner

Tips: 使⽤DataFactory插⼊nchar数据类型的数据时会出现空值的情况，可以先修改 的数据类型设 置，插完数据后再改回来

数据库

- 6、在录制脚本的过程中插⼊注释，录制后查看和理解脚本会更加⽅便。
- 7、LoadRuner回放脚本时，在浏览器显示的中⽂是乱码


- 解决办法（1）： ⾸先设置Run-Time Setings – Browser – Browser Emulation – User-Agent 然后设置IE： 查看－编码－钩上“⾃动选择”和Unicode（UTF-8）。
- 解决办法（2）： 使⽤lr_convert_string_encoding函数来转换编码 下⾯是⼀个使⽤的例⼦： lr_convert_string_encoding(lr_eval_string(" {ReplyContents}"),LR_ENC_SYSTEM_LOCALE,LR_ENC_UTF8,"ReplyMesage");


web_submit_data("postreply.aspx_2", "Action= ", "Method=POST", "EncType=multipart/form-data", "RecContentType=text/xml", "Referer= ", "Snapshot=t9.inf", "Mode=HTML", ITEMDATA, "Name=iconid", "Value=0", ENDITEM, "Name=title", "Value=", ENDITEM, "Name=wysiwyg", "Value=0", ENDITEM, "Name=checkbox", "Value=0", ENDITEM,

htp:/127.0.0.1/postreply.aspx?infloat=1&topicid=2&inajax=1

htp:/127.0.0.1/showtopic-2.aspx

"Name=mesage", "Value=[localimg=180,12]1[/localimg]\r\n\r\n{ReplyMesage}",

ENDITEM, "Name=e_mediatyperadio", "Value=on", ENDITEM, "Name=sl_atachdesc", "Value=", ENDITEM, "Name=atachid", "Value=", ENDITEM, "Name=atachdesc", "Value=", ENDITEM, "Name=localid", "Value=", ENDITEM, "Name=atachdesc", "Value=", ENDITEM,

- "Name=localid", "Value=1", ENDITEM, "Name=atachdesc", "Value=", ENDITEM,
- "Name=localid", "Value=2", ENDITEM, "Name=emailnotify", "Value=on", ENDITEM, "Name=postreplynotice", "Value=on", ENDITEM, "Name=postfile", "Value=", "File=Yes", ENDITEM, "Name=postfile", "Value=D: \图⽚收集 \It's about time.JPG", "File=Yes", ENDITEM, "Name=postfile", "Value=", "File=Yes", ENDITEM, "Name=uploadalowmax", "Value=10", ENDITEM, "Name=uploadalowtype", "Value=jpg,gif", ENDITEM, "Name=thumbwidth", "Value=30", ENDITEM, "Name=thumbheight", "Value=250", ENDITEM, "Name=noinsert", "Value=0", ENDITEM, LAST);


- 8、可以在LR测试脚本的⽬录中找到参数⽂件，直接修改参数⽂件
- 9、LR9.x启动VUGen时提示"Failed to conect to server"


The parameter file is stored in the script. directory as .dat file extension. It can be opened with a simple text editor like Notepad as it is stored in pure text format. Therefore, you can manipulate the files via the Notepad. You can also use ofice aplications such as Microsoft Excel or OpenOfice.org Calc to work on the file. This greatly the amount of work required for maintaining a long list of data.

After upgrading to LoadRuner 9.x I started geting a window popup which said "Failed to conect to server" every time I opened virtual user generator (vugen.exe).

The solution to this was to close virtual user generator and delete the [vugen.ini] file. vugen.ini is found in the %systemrot% folder on your PC (usualy C:\WINDOWS or C:\WI NT).

Windows

Once you've deleted the file, virtual user generator wil open without any isues and create a new vugen.ini file automaticaly.

# 10、怎样抓取有相同左右边界的动态value？

怎样抓取有相同左右边界的动态value？例如： stateID="d7lg0ehmj km6uin3s4boei7oq"> stateID="cvopakp46ftsf8mh6l37ti3ubm"> stateID="bv9mja8gtgr39dibm5t9163re"> web_reg_save_param⾥的ORD应该怎样设置？

ORD: Indicates the ordinal position or instance of the match. The default instance is 1. If you specify "Al," it saves the parameter values in an aray.

例⼦： char outFlightParam[50]; / The name of the parameter for corelation char outFlightParamVal[50]; / The formated value of outFlightParam web_reg_save_param("outFlightVal",

"LB=outboundFlight value=", "RB=>", "ORD=AL", "SaveLen=18", LAST);

web_submit_form("reservations.pl", "Snapshot=t4.inf", ITEMDATA, "Name=depart", "Value=London", ENDITEM, "Name=departDate", "Value=1/20/203", ENDITEM,

"Name=arive", "Value=New York", ENDITEM, "Name=returnDate", "Value=1/21/203", ENDITEM, "Name=numPasengers", "Value=1", ENDITEM, "Name=roundtrip", "Value=<OF>", ENDITEM, "Name=seatPref", "Value=None", ENDITEM, "Name=seatType", "Value=Coach", ENDITEM,

- "Name=findFlights.x", "Value=83", ENDITEM,
- "Name=findFlights.y", "Value=16", ENDITEM, LAST);


sprintf(outFlightParam, "{outFlightVal_%s}", lr_eval_string("{outFlightVal_count}"); sprintf(outFlightParamVal, "Value=%s",

lr_eval_string(outFlightParam); lr_mesage("The value argument is : %s", outFlightParamVal);

web_submit_form("reservations.pl_2", "Snapshot=t5.inf", ITEMDATA, "Name=outboundFlight",outFlightParamVal, ENDITEM,

- "Name=reserveFlights.x", "Value=92", ENDITEM,
- "Name=reserveFlights.y", "Value=10", ENDITEM, LAST);


# 1、运⾏场景时提示“Step download timeout (120 seconds) has expired when downloading resource(s)”

vuser_init.c(12): Eror -2728: Step download timeout (120 seconds) has expired when downloading non-resource(s)（出现个别，可以忽略） vuser_init.c(12): Eror -2727: Step download timeout (120 seconds) has expired when downloading resource(s). Set the "Step Timeout caused by resources is a warning" Run-Time Seting to Yes/No to have this mesage as a warning/eror, respectively

如果觉得下载⼀个⻚⾯超过2分钟不是错误的话，可以在Run-Time设置中选择Preferences>Options，修改Step download timeout(sec)的时间 或者把“Step timeout caused by resources is a warning”设置为Yes，这样下载资源超时也只是作为警 告，不作为错误提示，但是对于⾮资源的下载超时，则总是会提示错误的

- 12、⽤strtok函数分割字符串
- 13、LoadRuner没有购买webservice协议的license，只有htp的，可不可以完 全⽤htp协议模拟webservice？


需要在loadruner⾥⾯获得“15”（下⾯红⾊⾼亮的部分），并做成关联参数。 /Body response 内容： <BODY><; PRE>/OK[8,7,5,15,6,5,0,4,0,3,0,3,2,0,0,0,1

⽤web_reg_save_param取出“8,7,5,15,6,5,0,4,0,3,0,3,2,0,0,0,1”这⼀段，然后⽤strtok函数切割出⼀个 个数字，第四个数字就是要找的值

例如： extern char * strtok(char * string, const char * delimiters ); / Explicit declaration char separators[] = ","; char * token; lr_save_string("1,2,3,4,5,6","str"); token = (char *)strtok(lr_eval_string("{str}"), separators); / Get the first token if (!token) {

lr_output_mesage ("No tokens found in string!"); return( -1 );

}

while (token != NUL ) {/ While valid tokens are returned lr_output_mesage ("%s", token ); token = (char *)strtok(NUL, separators); / Get the next token

}

可以，参考：

htp:/blog.testsautomation.com/209/01/web-services-performance-using-loadruner/ htp:/blog.testsautomation.com/209/05/validating-web-service-response-with-xpath/

- 14、在场景设置中不忽略思考时间,但是在查看响应时间的时候怎样让LR⾃动在响 应时间⾥减去思考时间？
- 15、LoadRuner在运⾏过程中停掉1半虚拟⽤户
- 16、LoadRuner录制不了任何东⻄


在analysis中找到了设置是否在报告中包含思考时间的地⽅做相应的设置即可：

Another isue that Iʼm facing from time to time is that LoadRuner stops 50% of runing users without any notification, isue, eror mesage etc. Iʼve find out that it hapens only when runing with log level set to ‘always send a mesageʼ. Because of that, I sugest to run with log level set to ‘Send mesage only when eror ocurʼ.

Sometimes LoadRuner is not recording anything while browsing using IE. I have no idea why but the fastest solution is to restart whole LR. Maybe some of you have god explanation for that?

- 17、测试RTMP协议应该在LoadRuner选择什么协议来录制？
- 18、如何在LoadRuner中运⾏QTP脚本？


⽤flex协议

有这⼏个函数可⽤： flex_rtmp_conect Conects a client to an RTMP server and sets conection options. flex_rtmp_disconect Disconects a client from an RTMP server. flex_rtmp_send Sends mesages to an RTMP server. flex_rtmp_receive Receives responses from an RTMP server

Flex can record and replay scripts involving RTMP (Real Time Mesaging Protocol). In order to enable RTMP simulation, you must configure the recording options for the Flex protocol. To enable RTMP:

- 1 Open the Recording Options dialog box by selecting Tols > Recording Options or clicking the Options buton in the Start Recording dialog box.
- 2 In the Network > Port Maping node click Options.
- 3 Set the Send-Receive bufer size threshold to 150.


1、运⾏准备：

- 1）勾选QTP的Tols-Options-Run的"Alow other Mercury products to run tests and

components"

- 2）录制需要在lr中运⾏的QTP脚本，并且在QTP脚本中设置事务，Services.StartTransaction


"start"与Services.EndTransaction "start" 2、运⾏QTP脚本 在LR中运⾏时选择QTP脚本，为QTP脚本存放⽬录下⽂件扩展名为.usr的⽂件。 注：LR中运⾏QTP脚本时，只能有⼀个Vuser,否则将报错： The load generator is curently runing the maximum number of Vusers of this type

- 19、在LR中如何忽略Socket接收数据的验证
- 20、LoadRuner9.5的Controler中不能添加Apache的监控
- 21、VB Vuser开发ADO脚本，提示“user-defined type not defined”


在LR中对Socket进⾏性能测试时，LR会⾃⼰判断lrs_receive回来的数据的⻓度，⽽如果⻓度不符的话 会有时间延迟的情况(这是性能测试完全不能接受的事情)，如果做到这⼀点呢，经过反复尝试，发现⼀ 种简单的⽅法(⽤*代替具体的⻓度)：

类似于将： recv buf1 12 "Helo, Deny"

改为： recv buf1 * "Helo, Deny"

⼀切OK。

在C:\Program Files\HP\LoadRuner\dat\online_graphs中找到online_resource_graphs.rmd⽂件，修 改[Apache]部分中的EnableInUI为1

想在VB Vuser写⼊模拟数据操作的过程，然后在VB Vuser⾥定义了这个全局变量 Private m_Con As ADODB.Conection '连接对象 Private m_Reco As ADODB.Recordset '结果集 但是在VB Vuser中不识别这个对象，报出user-defined type not defined

需要在Run-Time 设置中的VBA部分把ADO的库选上 如果⽤VB Script虚拟⽤户来开发就不要，直接⽤CreateObject来创建ADO对象即可

2、loadruner9.5录制脚本时出现 c:\PROGRA~1\MICROS~1\ofice12\Groveutil.DL时出错内存位置访问⽆效

Ofice207的问题，IE加载项禁⽤Grove GFSBrowser Helper 组件

- 23、LR⾃带的例⼦端⼝号怎么修改？
- 24、⽤Web_reg_find查找中⽂字符串时查找不到
- 25、替代IP Wizard的脚本


LR⾃带的例⼦端⼝号是1080,我怎么样把这个端⼝设置我⾃⼰想⽤的端⼝号808,在什么地⽅设置

在LR安装⽬录下，找到Xitami.config⽂件，找到portbase，可以修改它（默认是1 0）； 默认的端⼝号是portbase+80； 要把端⼝号改成808，就把portionbase改为808，保存之后就是了（808=808+80）。

脚本⽂件⾥有个default.cfg ，⾥⾯有个参数是 UTF8InputOutput ，将其值改为0

htp:/hi.baidu.com/higko/blog/item/39 b21bc3d76dcac6e751c.html

LoadRuner⾃带的“IP Wizard”⽤起来⾮常麻烦，要不停的点，重要的是最后还必须重启系统⽣效。 于是乎写个脚本替代之：

- 1. 假设客户端IP为 192.168.10.31
- 2. 假设服务端IP为 192.168.10.10
- 3. 需要模拟的IP为 10.19.120.12


那么，客户端提供添加虚拟IP的BAT脚本：

netsh interface ip ad adres 本地连接 10.19.120.12 25.25.0.0 对应的删除设置为： netsh interface ip del adres 本地连接 10.19.120.12

对应服务器添加虚拟路由的Shel脚本： route ad -host 10.19.120.12 gw 192.168.10.31 删除路由的脚本： route del -host 10.19.120.12 gw 192.168.10.31

这样就⾮常⽅便了，不⽤重启任何机器，执⾏脚本就⽣效，再执⾏脚本就取消。

