背景

由于gitlab默认是没有上下⽂的，但是对于只有⼀个DNS服务器+nginx或haproxy做代理的情况下，没 有上下⽂配置是不科学的。为此，我们必须给gitlab⼀个上下⽂（⽐如：/gitlab)

操作

假设docker 端⼝映射关系

docker run -p 80 80 1 修改 gitlab.rb

. external_url 'htp:/example.org:8138'

.

- 1
- 2
- 3 这时候重启服务会发现gitlab怎么都⽆法访问了


gitlab-ctl reconfigure gitlab-ctl restart

- 1
- 2 解决


当更改gitlab.rb中的external_url参数时，会产⽣副作⽤（官⽅⽂档中没有⾮常清楚地说明！），nginx 现在将在放置在htp:/example.org中的端⼝上运⾏：8138，为此我们需要修改docker的端⼝映射关系

docker run -p 80 8138. 1

⸻—

# 版权声明：本⽂为CSDN博主「hrbeant」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原 ⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/hrbeant/article/details/10438 09

