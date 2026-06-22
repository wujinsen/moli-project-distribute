前⽂说的怎么⽤htp⽅式连接到gitlab，这次我们来试试 SH⽅式。

主要有这么⼏步：⽣成key，添加key到服务，创建连接

- 1.⽣成key 我们⽤的是eclipse⾃带的⽣成key的⼯具，windows->preferences，找到 SH2.


![image 1](<eclipse SSH方式连接GitLab.note_images/imageFile1.png>)

在key management处点⽣成RSAkey

![image 2](<eclipse SSH方式连接GitLab.note_images/imageFile2.png>)

后⾯输⼊key的说明和密码，密码也可以空着。点save private key. 把⽣成的key⽂件存到⽤户⽬录 的.sh⽬录下。（像第⼀张图中 SH2 Home指定的⽬录）

![image 3](<eclipse SSH方式连接GitLab.note_images/imageFile3.png>)

会⽣成两个⽂件，⼀个id_rsa是私钥，⼀个id_rsa.pub是公钥。

- 2.发布公钥到服务器 ⽤记事本打开id_rsa.pub⽂件，能看到如图的示的类似内容，把它们复制下来。


![image 4](<eclipse SSH方式连接GitLab.note_images/imageFile4.png>)

⽤你的⽤户登陆到gitlab，profiles->sh keys->adsh kay。给⽤户添加全局的公钥⽂件。

![image 5](<eclipse SSH方式连接GitLab.note_images/imageFile5.png>)

把刚才复制的内容贴到⻚⾯上，ad key.

![image 6](<eclipse SSH方式连接GitLab.note_images/imageFile6.png>)

# 3.连接到服务器 这⾥我们是在⼀个新的环境下建⽴到gitlab的连接，打开git repositories，点clone⼀个git库

![image 7](<eclipse SSH方式连接GitLab.note_images/imageFile7.png>)

这⾥的URL输⼊在gitlab的项⽬中显示的连接。这⾥要解释下这个连接的内容。

第⼀个git，git@server :git/gitest.git，是在gitlab所在的那个linux系统中，⽤来管理git库的⼀个 linux系统⽤户，默认这个⽤户的密码是空的。所以下图中下⾯的密码处为空。

@后⾯是服务器地址git@server:git/gitest.git

:后⾯,git@server :git/gitest.git ,是创建这个项⽬的那个gitlab⽤户的⽤户名，这⾥我们的⽤户名 也是git

/后⾯，git@server :git/gitest.git，就是你的项⽬名。

.git是后缀

![image 8](<eclipse SSH方式连接GitLab.note_images/imageFile8.png>)

如果第⼀次与服务器建⽴连接。会寻问是否保存服务器上的公钥信息，点yes。

![image 9](<eclipse SSH方式连接GitLab.note_images/imageFile9.png>)

在这之后会提示你输⼊你在建⽴key⽂件时输⼊的密码。next.就能看到已经选择的⼀个分枝了。

![image 10](<eclipse SSH方式连接GitLab.note_images/imageFile10.png>)

next，同步⽂件，在git repositories就能看到这个同步下来的库了。

![image 11](<eclipse SSH方式连接GitLab.note_images/imageFile11.png>)

在项⽬名上点右键，import projects，把同步下来的项⽬导⼊到eclipse

![image 12](<eclipse SSH方式连接GitLab.note_images/imageFile12.png>)

完成。

问题：有时会出现⽣成的key⽂件不能正常使⽤，在连接服务时会问⼏次密码后中断，可以尝试重 启eclipse

