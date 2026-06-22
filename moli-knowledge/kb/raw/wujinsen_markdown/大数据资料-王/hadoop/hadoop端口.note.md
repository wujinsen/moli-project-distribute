hadop端⼝：

- -A INPUT -p tcp -m tcp-dport 9 0 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 901 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 5090 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 5060 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 50470 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 5020 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 50475 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 5075 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 5010 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 5070 -j ACEPT
- -A INPUT -p tcp -m tcp-dport 5030 -j ACEPT

hbase端⼝：

- -A INPUT -p tcp -m tcp-dport 6 0-j ACEPT

- -A INPUT -p tcp -m tcp-dport 6010 -j ACEPT


- zokeper端⼝：
- -A INPUT -p tcp -m tcp-dport 2181-j ACEPT

- -A INPUT -p tcp -m tcp-dport 2 8 -j ACEPT

- -A INPUT -p tcp -m tcp-dport 3 8 -j ACEPT

kafka端⼝：

- -A INPUT -p tcp -m tcp-dport 9092 -j ACEPT

storm端⼝：

- -A INPUT -p tcp -m tcp-dport 670-j ACEPT

- -A INPUT -p tcp -m tcp-dport 6701-j ACEPT

- -A INPUT -p tcp -m tcp-dport 6702-j ACEPT

- -A INPUT -p tcp -m tcp-dport 6703-j ACEPT


