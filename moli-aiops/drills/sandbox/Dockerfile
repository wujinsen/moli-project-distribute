# 故障演练沙箱节点。
#
# 装 sshd 是为了让诊断平台走与生产完全相同的取证路径——本地调试和打 EC2 真机
# 用同一套代码，不维护第二个 transport。
#
# 构建：docker compose -f drills/sandbox/docker-compose.yml build
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
        openssh-server \
        python3 \
        procps \
        iproute2 \
        net-tools \
        psmisc \
        coreutils \
        curl \
        stress-ng \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /run/sshd /var/log/moli /opt/moli/services /var/run/moli \
    && echo 'root:aiops' | chpasswd \
    && sed -i 's/^#\?PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config \
    && sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config \
    && sed -i 's/^#\?UseDNS.*/UseDNS no/' /etc/ssh/sshd_config

COPY systemctl /usr/local/bin/systemctl
COPY services/ /opt/moli/services/
COPY entrypoint.sh /entrypoint.sh

# 这些脚本在 Windows 上编辑时会带 CRLF，内核解析 shebang 时会把 \r 算进解释器路径
# 而报 "no such file or directory"。构建期统一剥掉，省得依赖每个人的 git autocrlf 设置。
RUN sed -i 's/\r$//' /usr/local/bin/systemctl /entrypoint.sh /opt/moli/services/* \
    && chmod +x /usr/local/bin/systemctl /entrypoint.sh /opt/moli/services/*

EXPOSE 22
ENTRYPOINT ["/entrypoint.sh"]
