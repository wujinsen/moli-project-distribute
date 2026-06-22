gitlab-ctl start # 启动所有 gitlab 组件 gitlab-ctl stop # 停⽌所有 gitlab 组件 gitlab-ctl restart # 重启所有 gitlab 组件 gitlab-ctl status # 查看服务状态

gitlab-ctl reconfigure # 启动服务 gitlab-ctl show-config # 验证配置⽂件

gitlab-ctl tail # 查看⽇志

gitlab-rake gitlab:check SANITIZE=true-trace # 检查gitlab

vim /etc/gitlab/gitlab.rb# 修改默认的配置⽂件

