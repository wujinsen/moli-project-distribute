history

History命令主要⽤于显示历史指令记录内容, 下达历史纪录中的指令 。

- 1>History命令语法： [test@linux]# history [n] [test@linux]# history [-c] [test@linux]# history [-raw] histfiles 参数： n：数字,要列出最近的 n 笔命令列表

- -c ：将⽬前的shel中的所有 history 内容全部消除
- -a ：将⽬前新增的history 指令新增⼊ histfiles 中，若没有加 histfiles ， 则预设写⼊ ~/.bash_history
- -r：将 histfiles 的内容读到⽬前这个 shel 的 history 记忆中
- -w ：将⽬前的 history 记忆内容写⼊ histfiles Linux系统当你在shel(控制台)中输⼊并执⾏命令时，shel会⾃动把你的命令记录到历史列表中，⼀般 保存在⽤户⽬录下的.bash_history⽂件中。默认保存1 0条，你也可以更改这个值。 如果你键⼊ history, history会向你显示你所使⽤的前1 0个历史命令，并且给它们编了号，你会看到 ⼀个⽤数字编号的列表快速从屏幕上卷过。你可能不需要查看1 0个命令中的所有项⽬, 当然你也可以 加⼊数字来列出最近的 n 笔命令列表。 linux中history命令不仅仅让我们可以查询历史命令⽽已. 我们还可以利⽤相关的功能来帮我们执⾏命 令。


- 2>运⾏特定的历史命令 history会列出bash保存的所有历史命令，并且给它们编了号，我们可以使⽤“叹号接编号”的⽅式运⾏ 特定的历史命令. 语法说明: [test@linux]# [!number][!comand] [!] 参数说明： number ：第⼏个指令的意思； comand ：指令的开头⼏个字⺟


- ! ：上⼀个指令的意思！
- 3>History命令实战 列出所有的历史记录： [test@linux] # history 只列出最近10条记录： [test@linux] # history 10 (注,history和10中间有空格) 使⽤命令记录号码执⾏命令,执⾏历史清单中的第 9条命令 [test@linux] #! 9 (!和 9中间没有空格) 重复执⾏上⼀个命令 [test@linux] #! 执⾏最后⼀次以rpm开头的命令(!? ?代表的是字符串,这个String可以随便输，Shel会从最后⼀条历史 命令向前搜索，最先匹配的⼀条命令将会得到执⾏。) [test@linux] #!rpm 逐屏列出所有的历史记录： [test@linux]# history | more ⽴即清空history当前所有历史命令的记录 [test@linux] #history -c 除了使⽤history命令,在 shel 或 GUI 终端提示下，你也可以使⽤上下⽅向键来翻阅命令历史(向下箭头 会向前翻阅)，直到你找到所需命令为⽌。这可以让我们很⽅便地编辑前⾯的某⼀条命令，⽽不⽤重复 输⼊类似的命令。 History命令的⽤途确实很⼤！但需要⼩⼼安全的问题!尤其是 rot 的历史纪录档案，这是⿊客们的最 爱！因为不⼩⼼的 rot 会将很多的重要资料在执⾏的过程中会被纪录在 ~/.bash_history 当中，如果这 个档案被解析的话，后果不堪设想！


