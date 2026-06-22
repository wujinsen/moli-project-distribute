不常去contribute，所以⽼是忘记如何提交fix，特地重新记录下，以免忘记。

- 1. Fork别⼈的代码repo

- 2. clone⾃⼰的fork到本地，进⾏修改 在⾃⼰的Repositories⾥⾯找到刚才fork出来的Repo. 因为毕竟不是原作者，要定期去update最新的代码，所以最好不要直接提交到⾃⼰的master branch 上，⽽是在github上单独创建⼀个branch。

然后在右侧找到SSH clone URL，在本地的命令⾏⾥⾯执⾏

PS: 也可以直接在github上修改，然后⻚⾯内Commit。量不⼤时，这样做⽐较⽅便。只要⽹速给⼒……

- 3. 提交修改到github


![image 1](<github如何贡献源代码.note_images/imageFile1.png>)

![image 2](<github如何贡献源代码.note_images/imageFile2.png>)

![image 3](<github如何贡献源代码.note_images/imageFile3.png>)

[python] view plain copy

print?

- 1.
- 2.


git commit -a git push

- 4. 创建pull request 让作者codereview

点击Pull Requests -> New pull request

点击Edit 左侧选取作者的master branch （⼀般是这样，除⾮有单独的branch给开发⽤），右侧选⾃⼰的repo的 新branch 再点击 Click to create a pull request for this comparison 填⼊信息提交就OKl了。

- 5. Update 作者最新的代码 同样在⾃⼰的repo⾥⾯，新建⼀个pull request,左侧是⾃⼰的，右侧是原作者的branch，创建后提交就 OK了。

- 6. rollback repository到某个commit


![image 4](<github如何贡献源代码.note_images/imageFile4.png>)

![image 5](<github如何贡献源代码.note_images/imageFile5.png>)

github上有时我们想rollback某个branch到某个commit，直接在本地执⾏

[python]

view plain copy print?

- 1.
- 2.
- 3.


git checkout <target_branch> git reset --hard <commit_id> git push -f

