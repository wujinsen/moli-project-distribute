"Maximum upload size exceded; nested exception is java.lang.IlegalStateException: org.apache.tomcat.util.htp.fileupload.FileUploadBase$FileSizeLimitExcededException: The field file exceds its maximum permited size of 1048576 bytes ⾸先说mino好像并没有限制单个⽂件或者⼀次上传⽂件⼤⼩的限制。 出现这个问题的原因是springbot限制了上传⽂件的⼤⼩ 可以查看源码MultipartProperties类中 从源码中可以得知，maxFileSize，即最⼤⽂件⼤⼩，默认被限制为1MB，maxRequestSize即最⼤请求 ⼤⼩，默认被限制为10MB。 这个时候就需要在yml⽂件中设置。 ⼀开始不太清楚

![image 1](<上传文件到minio文件大小限制.note_images/imageFile1.png>)

但是重启之后还是报相同的错误发现⾃⼰的spring版本是2.0.4，然后上⽹上搜索了⼀下，配置下图

![image 2](<上传文件到minio文件大小限制.note_images/imageFile2.png>)

记住配置这个的时候⼀定要将第⼀张截图设置的内容之前的注释掉，不然这个也会不⽣效。 这个时候上传⽂件就会发现没有⼀点问题。

