#!/bin/bash
# 启动 sshd 与本节点声明的服务，然后守住前台。
set -eu

mkdir -p /var/log/moli /var/run/moli
ssh-keygen -A >/dev/null 2>&1 || true
/usr/sbin/sshd

for unit in ${SANDBOX_UNITS:-}; do
    /usr/local/bin/systemctl start "$unit" || echo "无法启动 $unit" >&2
done

echo "沙箱节点就绪：sshd + [${SANDBOX_UNITS:-无服务}]"
tail -f /dev/null
