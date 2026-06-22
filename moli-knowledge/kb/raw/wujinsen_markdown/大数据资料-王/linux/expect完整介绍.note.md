expect是建⽴在tcl基础上的⼀个⼯具，它⽤来让⼀些需要交互的任务⾃动化地完成。我们⾸先从⼀个 简单的例⼦开始，如同在这⼀节⼀开始就提到的，我们想设置⼀个⾃动的⽂件下载程序。

我们看⼀看这样的⼀个例⼦脚本：#! /usr/bin/expect

spawn ftp 202.19.248.1

expect "Name"

send "ftp\r"

expect "Pasword:"

send "nothing\r"

expect "aply"

send "cd /pub/UNIX/Linux/remoteX\r"

expect "sucesful."

send "bin\r"

expect "set to I"

send "get exced5.zip\r"

expect "complete."

send "quit\r"

复制代码 这个是什么意思？呵呵，就是个⾃动下载程序。第⼀⾏说明这个程序应该调⽤/usr/bin/expect去执⾏， 然后的就是expect命令。

察看expect的⼿册⻚⾯(man expect)可以得到⼀个很⻓的expect说明，可惜其中关于expect的语法 仍然介绍的不够。⼀般来说， expect主要⽤在需要⾃动执⾏⼈机交互的过程中，例如fsck程序，这个 程序会不断地提问"yes/no"，像这样的命令就可以⽤expect 来完成。

spawn语句在expect脚本中⽤于启动⼀个新的进程，在我们的程序中，spawn ftp 202.19.248.1就 是去执⾏ftp程序，接下来，就是expect和send的指令对了。

每⼀对expect和send指令代表⼀个信息/回应。如果这样说不好理解的话，那么可以看⼀看ftp的具体 执⾏过程：

ftp 202.19.248.1

Conected to 202.19.248.1.

20 mail.asnc.edu.cn FTP server (BeroFTPD 1.3.3(3) Sun Feb 20 15 52 49 CST 2 0.

Name (202.19.248.1:wanghy):

显然，⼀旦连接成功，服务器会返回⼀个Name(202.19.248.1:wanghy):的字符串来要求客户给出 ⽤户名。expect语句简单地在返回信息中查询你给出的字符串，⼀旦成功就执⾏下⾯的命令，现在， expect " Name"已经成功地找到了Name字符串，接下来可以执⾏ send命令了。

send命令⽐expect命令更简单，它简单地向标准输⼊提交你设定的字符串，现在设置为 send "ftp\r"表示等到登录信息之后就给出⼀个输⼊ftp回⻋，也就是标准的登录过程。

下⾯的⾏与这些⾏完全⼀样，只是机械地等待服务器的回应，并且提交⾃⼰的输⼊。

要使⽤这个expect脚本，你只需要将它设置为可执⾏的属性，然后执⾏它，expect就会执⾏你需要的 服务。

由于expect是tcl的扩展，所以你在expect⽂件中可以象tcl脚本⼀样设置变量和程序流程。

现在我们看⼀看我们还能够如何改进我们的expect脚本。ftp命令可能会失败，⽐如远端的机器可能 会⽆法提供服务，或者在启动ftp命令时本地机器发⽣问题。为了处理这⼀类的问题，我们可以使⽤ expect的timeout选项来设置超时的话expect脚本⾃动退出：

#! /usr/bin/expect

spawn ftp 202.19.248.1

expect {

timeout exit

Conect

}

…

注意这⾥⾯使⽤的花括号。它的含义是使⽤⼀组并列表达式。使⽤并列表达式的主要原因是这样：如 果使⽤下⾯的指令对：

expect timeout

exit

那么由于expect脚本是顺序执⾏的，那么当程序执⾏到这个expect的时候就会阻塞，所以程序会⼀ 直等待到timeout然后退出。并列表达式则是相当于switch的⾏为，只要列出的⼏项内容有⼀项得到满 ⾜，expect命令就得到满⾜，于是程序可以正常执⾏。上⾯的脚本表示，如果连接ftp的时候发⽣了超 时，那么就退出，否则，⼀旦发现Conect应答，说明服务器已经正常了，那么就可以继续运⾏了。

我们可以看看⽤tcl能够对我们的expect脚本提供什么帮助。我们可以设置让expect脚本不断地连接远 端服务器的服务，直到正常建⽴连接开始，为此，我们可以把建⽴连接的命令放在⼀个循环⾥⾯，并 且根据回应的不同⾃动选择重新输⼊命令还是继续执⾏：

spawn ftp

while {1} {

expect "ftp>"

send "o 202.19.248.1\r"

expect {

"Conected" break

"refused" { sl ep 10};

}

}

这⾥使⽤了我们在tcl语⾔中讲到的while和break命令，熟悉C的读者应该很容易看出它的⾏为：不断 地等待ftp>提示符，在提示符下⾯发送连接远端服务器的命令，如果服务器回应是refused（连接失 败），就等待10秒钟，然后开始下⼀次循环；如果是Conected，那么就跳出循环执⾏下⾯的命令。 sl ep是expect的⼀个标准命令，表示暂停若⼲秒钟。

expect还⽀持许多更复杂的进程控制⽅式，如fork，disconect等等，你可以从⼿册⻚⾯中得到详细 的信息。另外，各种tcl运算符和流程控制命令，包括tcl函数也可以使⽤。

有些读者可能会问，如果expect执⾏的话是否控制台输⼊不能使⽤了，答案是否定的。expect命令 运⾏时，如果某个等待的信息没有得到，那么程序会阻塞在相应的expect语句处，这时，你在键盘上 输⼊的东⻄仍然可以正常地传递到程序中去，其实对于那些expect处理的信息，原则上你输⼊的内容 仍然有效，只是expect的反映太快，总是抢在你的前⾯“输⼊”就是了。知道了这⼀点之后，你就可能 写⼀个expect脚本，让expect⾃动处理来⾃ fscki的那些恶⼼的yes/no选项（我们介绍过，这些yes/no 其实完全是多余的，正常情况下你除了选择yes之外什么也⼲不了）。

缺省下，expect在标准输出（你的终端上）输出所有来⾃应⽤程序的回应信息，你可以⽤下⾯的两个 命令重定向这些信息：

log_file [⽂件名]

这个命令让expect在你设置的⽂件中记录输出信息。必须注意，这个选项并不影响控制台输出信息， 不过如果你通过crond设置expect脚本在半夜运⾏的话，你就确实可能需要这个命令来记录各种信息 了。例如：

log_file expect.log

log_user 0/1

这个选项设置是否显示输出信息，设置为1时是缺省值，为0 的话，expect将不产⽣任何输出信息， 或者说简单地过滤掉控制台输出。必须记住，如果你⽤log_user 0关闭了控制台输出，那么你同时也就 关闭了对记录⽂件的输出。

这⼀点很让⼈困扰，如果你确实想要记录expect的输出却不想让它在控制台上制造垃圾的话，你可以 简单地把expect的输出重定向到/dev/nul：

./test.exp > /dev/nul

你可以象下⾯这样使⽤⼀对fork和disconect命令。expect的disconect命令将使得相应的进程到后 台执⾏，输⼊和输出被重定向到/dev/nul：

if [fork]!=0 exit

disconect

fork命令会产⽣出⼀个⼦进程，⽽且它产⽣返回值，如果返回的是0，说明这是⼀个⼦进程，如果不 为0，那么是⽗进程。因此，执⾏了fork命令之后，⽗进程死亡⽽⼦进程被disconect命令放到后台执 ⾏。注意disconect命令只能对⼦进程使⽤。

