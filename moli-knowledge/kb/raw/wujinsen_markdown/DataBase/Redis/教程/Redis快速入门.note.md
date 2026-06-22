关于protected-mode Exception in thread "main" redis.clients.jedis.exceptions.JedisDataException: DENIED Redis is runing in protected mode because protected mode is enabled, no bind adres was specified, no authentication pasword is requested to clients.

In this mode conections are only acepted from the l opback interface. If you want to conect from external computers to Redis you may adopt one of the folowing solutions:

- 1) Just disable protected mode sending the comand 'CONFIG SET protected-mode no' from the \ l opback interface by conecting to Redis from the same host the server is runing, however MAKE SURE Redis is not publicly acesible from internet if you do so. Use CONFIG REWRITE to make this change permanent.
- 2) Alternatively you can just disable the protected mode by editing the Redis configuration file,

and seting the protected modeoption to 'no', and then restarting the server.

- 3) If you started the server manualy just for testing, restart it with the '-protected-mode no' option.
- 4) Setup a bind adres or an authentication pasword. NOTE: You only ned to do one of the


above things in order for the server to start acepting conections from the outside.

