linux版本

- 1. 更改redis.conf配置

# requirepass foobared 去掉注释，foobared改为 ⾃⼰的password , 我测试的时候⽤的是默认的 foobared

- 2.启动redis

/usr/local/redis/src/redis-server -p 6371

- 3.测试


/usr/local/redis/src/redis-cli -p 6371 -a foobared

redis>set a b

(error) ERR operation not permitted (-a 设置这个时候没起作⽤ )

redis>auth foobared

OK

redis>set a b

OK

redis>quit

这时,再

/usr/local/bin/redis-cli -a foobared

redis > set a b

OK

wimdows版本

AUTH pasword 通过设置配置⽂件中 requirepass 项的值(使⽤命 令 CONFIG SET requirepass password )，可以使⽤密码来保护 Redis 服务器。 如果开启了密码保护的话，在每次连接 Redis 服务器之后，就要使⽤ AUTH 命令解锁，解锁之后 才能使⽤其他 Redis 命令。 如果 AUTH 命令给定的密码 password 和配置⽂件中的密码相符的话，服务器会返回 OK 并开始 接受命令输⼊。 另⼀⽅⾯，假如密码不匹配的话，服务器将返回⼀个错误，并要求客户端需重新输⼊密码。

因为 Redis ⾼性能的特点，在很短时间内尝试猜测⾮常多个密码是有可能的，因此请确保使⽤的 密码⾜够复杂和⾜够⻓，以免遭受密码猜测攻击。

可⽤版本：

>= 1.0.0

时间复杂度： O(1) 返回值： 密码匹配时返回 OK ，否则返回⼀个错误。

# 设置密码

redis> CONFIG SET requirepas secret_pasword# 将密码设置为 secret_pasword OK

redis> QUIT # 退出再连接，让新密码对客户端⽣效

[huangz@mypad]$ redis

redis> PING # 未验证密码，操作被拒绝 (eror) ER operation not permited

redis> AUTH wrong_pasword_testing # 尝试输⼊错误的密码 (eror) ER invalid pasword

redis> AUTH secret_pasword # 输⼊正确的密码 OK

redis> PING # 密码验证成功，可以正常操作命令了 PONG

# 清空密码

redis> CONFIG SET requirepas" # 通过将密码设为空字符来清空密码 OK

redis> QUIT

重启服务端

$ redis # 重新进⼊客户端

redis> PING # 执⾏命令不再需要密码，清空密码操作成功 PONG

