htps:/ w.cnblogs.com/brady-wang/p/1060132.html

访问gitlab，出现：502 GitLab在使⽤的过程中，会开启80端⼝，如果80端⼝被其他的应⽤程序占⽤，则GitLab的该项服务不 能使⽤，所以访问GitLab会失败。⼤多数皆是此问题。 还要注意gitlab还要使⽤8080端⼝，因此要注意可以把gitlab端⼝改为别的⽆服务占⽤的端⼝。 改gitlab端⼝:

- 1.vim /etc/gitlab/gitlab.rb unicorn['port'] = 9090 nginx['listen_port'] = 909
- 2.vim /var/opt/gitlab/gitlab-rails/etc/unicorn.rb listen “127.0.0.1 9090”, :tcp_nopush => true 修改默认的gitlab nginx的web服务80端
- 3.vim /var/opt/gitlab/nginx/conf/gitlab-htp.conf listen *:909;
- 4.重启配置: sudo gitlab-ctl reconfigure
- 5.重新启动gitlab gitlab-ctl restart


