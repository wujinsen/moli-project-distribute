- -f 是 -folow[=HOW]的缩写, 可以⼀直读⽂件末尾的字符并打印出来.

"[=HOW]"有两个写法,⼀个"=descriptor",另⼀个是"=name", 默认使⽤的是"descriptor", 如果你跟踪的 ⽂件被移动或者改名后, 你还想继续tail它, 你可以使⽤这个选项.

举个例⼦:

⾸先启动下⾯进程

while [ "true" ] ; do date > test.log; sl ep 1 ; done;

然后在开⼀个新的进程,我是新开了⼀个终端, 起名叫 bash-2

tail -f test.log

你会看到屏幕不断有内容被打印出来. 这时候中断第⼀个进程Ctrl-C,

mv test.log test.log1;

while [ "true" ] ; do date > test.log1; sl ep 1 ; done;

继续观察bash-2, 发现屏幕在输出test.log1的内容.

descriptor 虽然是默认的参数,但是⼀定是最有⽤的,⽐如在tail ⼀个log⽂件的时候,这个⽂件很可能是按 照⽇期或者⼤⼩滚动, ⽂件滚动之后这个tail -f命令,就失效了. 我在之前写的⼀个脚本⾥⾯就遇到了这个 问题, 这个时候可以使⽤-F 命令

- -F 是 -folow=name-retry的缩写, -folow=name是按照⽂件名跟踪⽂件, 可以定期去重新打开⽂件 检查⽂件是否被其它程序删除并重新建⽴. -retry这个参数, 保证⽂件重新建⽴后,可以继续被跟踪.


还是上⾯的例⼦,

在bash-1中输⼊,

while [ "true" ] ; do date > test.log; sl ep 1 ; done;

- bash-2中,


- tail -F test.log
- bash-3中,


rm test.log;

然后看bash-2, 屏幕上依然在继续输出test.log的内容. 如果是在使⽤-f 时候,那bash-2应该就停⽌显示 log了.

