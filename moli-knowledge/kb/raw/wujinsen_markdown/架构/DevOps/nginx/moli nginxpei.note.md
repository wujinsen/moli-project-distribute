userrot; worker_proceses 2;

#eror_log logs/eror.log; #eror_log logs/eror.log notice; #eror_log logs/eror.log info;

#pid logs/nginx.pid;

events {

worker_conections 1024; }

htp { include mime.types; default_type aplication/octet-stream;

#log_formatmain '$remote_adr - $remote_user [$time_local] "$request" ' # '$status $body_bytes_sent "$htp_referer" ' # '"$htp_user_agent" "$htp_x_forwarded_for"';

#aces_log logs/aces.log main;

sendfile on; #tcp_nopush on;

#kepalive_timeout0; kepalive_timeout65;

#gzip on;

#server { # listen 8087; # server_name localhost;

# #添加头部信息 # proxy_set_header Cokie $htp_cokie; # proxy_set_header X-Forwarded-Host $host; # proxy_set_header X-Forwarded-Server $host; # proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; # #charset koi8-r; # # #aces_log logs/host.aces.log main; # # # location / { # rot /opt/htg-vuepres/dist; #你vue打包dist的在Linux的地址 # try_files $uri $uri/ /index.html; # index index.html index.htm; # } # }

server { listen 8080; server_name localhost; #添加头部信息 proxy_set_header Cokie $htp_cokie; proxy_set_header X-Forwarded-Host $host; proxy_set_header X-Forwarded-Server $host; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for;

location / { rot /opt/project/dist; #你vue打包dist的在Linux的地址 try_files $uri $uri/ /index.html; index index.html index.htm;

}

}

server { listen 80; server_name localhost; #添加头部信息

proxy_set_header Cokie $htp_cokie;

proxy_set_header X-Forwarded-Host $host; proxy_set_header X-Forwarded-Server $host; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; #charset koi8-r;

#aces_log logs/host.aces.log main;

location / { rot /opt/wjs-vuepres/dist; #你vue打包dist的在Linux的地址 try_files $uri $uri/ /index.html; index index.html index.htm;

}

# # location / { # rot /opt/ruoyi-vue/dist/; #你vue打包dist的在Linux的地址 # try_files $uri $uri/ /index.html; # index index.html index.htm; # } # # location /prod-api/{ #vue访问后端接⼝地址 # proxy_set_header Host $htp_host; # proxy_set_header X-Real-IP $remote_adr; # proxy_set_header REMOTE-HOST $remote_adr; # proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; # proxy_pas htp:/49.23.58.141 8080/; # }

# location / { # rot /opt/ruoyi-vue/dist; # # try_files $uri $uri/ @router; # try_files $uri $uri/ /index.html;

# index index.html index.htm; # index index.html; # # #try_files $uri $uri/ /opt/dist/html; # } # location @router { # rewrite ^.*$ /index.html last; # } # #正确示范: 末尾加斜杠"/" # location /prod-api { # proxy_pas htp:/localhost:8080/; # proxy_redirect of; # default # # proxy_set_header Host $htp_host; # proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; # proxy_set_header X-Forwarded-Proto $scheme; # proxy_set_header X-Forwarded-Port $server_port; # }

# location ~ \.(gif|jpg|png|htm|html|cs|js|flv|ico|swf)(.*) { # expires -1; # ad_header Cache-Control no-store; #}

# location ~ \.(gif|jpg|png|htm|html|cs|js|flv|ico|swf)(.*) { # proxy_pas htp:/49.23.58.141 808/; # 如果没有缓存则通过proxy-pas转向请求 # proxy_redirect of; # proxy_set_header Host $host; # proxy_cache cache_one; # proxy_cache_valid 20 302 1h; #对不同的HTP状态设置不同的缓存时间，h⼩时,d天数 # proxy_cache_valid 301 1d; # proxy_cache_valid any 1m; # expires 30d; # }

#eror_page 404 /404.html;

# redirect server eror pages to the static page /50x.html #

eror_page50 502 503 504 /50x.html; location = /50x.html {

rot /opt; }

}

server { listen 8201; server_name localhost; #添加头部信息 proxy_set_header Cokie $htp_cokie; proxy_set_header X-Forwarded-Host $host; proxy_set_header X-Forwarded-Server $host; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; #charset koi8-r;

#aces_log logs/host.aces.log main;

location / { rot /opt/project/dist/; #你vue打包dist的在Linux的地址 try_files $uri $uri/ /index.html; index index.html index.htm;

}

location /System/{ #vue访问后端接⼝地址 proxy_set_header Host $htp_host; proxy_set_header X-Real-IP $remote_adr; proxy_set_header REMOTE-HOST $remote_adr; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; proxy_pas htp:/49.23.58.141 8086/;

}

location /Crm/{ #vue访问后端接⼝地址 proxy_set_header Host $htp_host; proxy_set_header X-Real-IP $remote_adr; proxy_set_header REMOTE-HOST $remote_adr;

proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; proxy_pas htp:/49.23.58.141 808/;

}

}

# another virtual host using mix of IP-, name-, and port-based configuration # #server { # listen 8 0; # listen somename:8080; # server_name somename alias another.alias;

# location / { # rot html; # index index.html index.htm; # } #}

# HTPS server # server {

listen 43sl; server_name localhost;

sl_certificate /rot/sl_card/1_ w.wujinsen.com_bundle.crt; sl_certificate_key /rot/sl_card/2_ w.wujinsen.com.key;

sl_sesion_cache shared: SL 1m; sl_sesion_timeout5m;

sl_ciphers HIGH:!aNUL:!MD5; sl_prefer_server_ciphers on;

# location / { # rot /opt/vuepres-moli/dist; #你vue打包dist的在Linux的地址

# try_files $uri $uri/ /index.html; # index index.html index.htm; # }

location / { rot /opt/htg-crm-web/dist/; #你vue打包dist的在Linux的地址 try_files $uri $uri/ /index.html; index index.html index.htm;

}

location /System/{ #vue访问后端接⼝地址 proxy_set_header Host $htp_host; proxy_set_header X-Real-IP $remote_adr; proxy_set_header REMOTE-HOST $remote_adr; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; proxy_pas htp:/49.23.58.141 808/;

} }

}

