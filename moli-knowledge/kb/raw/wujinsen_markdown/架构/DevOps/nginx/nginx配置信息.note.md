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

server { listen 8081; server_name localhost;

#添加头部信息 proxy_set_header Cokie $htp_cokie; proxy_set_header X-Forwarded-Host $host; proxy_set_header X-Forwarded-Server $host; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; #charset koi8-r;

#aces_log logs/host.aces.log main;

location / { rot /opt/dist;

try_files $uri $uri/ @router; index index.html index.htm;

index index.html;

#try_files $uri $uri/ /opt/dist/html; }

location @router {

rewrite ^.*$ /index.html last; }

#正确示范: 末尾加斜杠"/"

location /System { proxy_pas htp:/localhost:808/; proxy_redirect default;

proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for; proxy_set_header X-Forwarded-Proto $scheme; proxy_set_header X-Forwarded-Port $server_port;

}

#eror_page 404 /404.html;

# redirect server eror pages to the static page /50x.html # eror_page50 502 503 504 /50x.html; location = /50x.html {

rot /opt/dist; }

# proxy the PHP scripts to Apache listening on 127.0.0.1 80 # #location ~ \.php$ { # proxy_pas htp:/127.0.0.1; #}

# pas the PHP scripts to FastCGI server listening on 127.0.0.1 9 0 # #location ~ \.php$ { # rot html; # fastcgi_pas 127.0.0.1 9 0; # fastcgi_index index.php; # fastcgi_param SCRIPT_FILENAME /scripts$fastcgi_script_name; # include fastcgi_params; #}

# deny aces to .htaces files, if Apache's document rot # concurs with nginx's one # #location ~ /\.ht { # deny al; #}

}

# another virtual host using mix of IP-, name-, and port-based configuration # #server { # listen 8 0; # listen somename:8080; # server_name somename alias another.alias;

# location / { # rot html; # index index.html index.htm; # } #}

# HTPS server # #server { # listen 43sl; # server_name localhost;

# sl_certificate cert.pem; # sl_certificate_key cert.key;

# sl_sesion_cache shared: SL 1m; # sl_sesion_timeout5m;

# sl_ciphers HIGH:!aNUL:!MD5; # sl_prefer_server_ciphers on;

# location / { # rot html; # index index.html index.htm; # } #}

}

