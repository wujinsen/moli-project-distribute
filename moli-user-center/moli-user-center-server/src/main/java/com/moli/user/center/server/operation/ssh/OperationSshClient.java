package com.moli.user.center.server.operation.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.server.operation.config.OperationSshProperties;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * SSH/SFTP 客户端封装（SVR-13~16）。
 * <p>连接偏好：auto 内网优先→公网回退；inner 仅内网；public 仅公网。私钥/密码由调用方解密后传入。</p>
 */
@Component
public class OperationSshClient {

    private static final Logger log = LoggerFactory.getLogger(OperationSshClient.class);

    @Resource
    private OperationSshProperties sshProperties;
    @Resource
    private OperationSecretSupport secretSupport;

    /**
     * 建立 SSH 会话，按连接偏好尝试候选主机，返回首个可连通的会话。
     */
    public OperationSshSession connect(OperationServerInfo server) {
        if (server.getSshAuthType() == null) {
            throw OperationBizException.sshNotConfigured(server.getId());
        }
        List<String> hosts = resolveHosts(server);
        if (hosts.isEmpty()) {
            throw new BaseException("服务器未配置可用 IP（内网/公网）");
        }
        int port = server.getSshPort() != null ? server.getSshPort() : sshProperties.getDefaultPort();
        String user = StringUtils.isNotBlank(server.getSshUser()) ? server.getSshUser() : sshProperties.getDefaultUser();
        String plainKey = secretSupport.resolvePlain(server.getSshPrivateKey());
        String plainPass = secretSupport.resolvePlain(server.getSshPassphrase());

        BaseException last = null;
        for (String host : hosts) {
            try {
                Session session = openSession(host, port, user, server.getSshAuthType(), plainKey, plainPass);
                return new OperationSshSession(session, host);
            } catch (Exception e) {
                last = new BaseException("连接 " + host + ":" + port + " 失败: " + e.getMessage());
                log.warn("SSH connect failed host={} port={} : {}", host, port, e.getMessage());
            }
        }
        throw last != null ? last : new BaseException("SSH 连接失败");
    }

    private Session openSession(String host, int port, String user, int authType,
                                String plainKey, String plainPass) throws Exception {
        JSch jsch = new JSch();
        if (authType == OperationSshAuthType.PRIVATE_KEY) {
            if (StringUtils.isBlank(plainKey)) {
                throw new BaseException("私钥为空");
            }
            byte[] pass = StringUtils.isNotBlank(plainPass) ? plainPass.getBytes(StandardCharsets.UTF_8) : null;
            jsch.addIdentity("moli-ops", plainKey.getBytes(StandardCharsets.UTF_8), null, pass);
        }
        Session session = jsch.getSession(user, host, port);
        if (authType == OperationSshAuthType.PASSWORD) {
            if (StringUtils.isBlank(plainPass)) {
                throw new BaseException("密码为空");
            }
            session.setPassword(plainPass);
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", sshProperties.isStrictHostKeyChecking() ? "yes" : "no");
        // 大文件 SFTP 传输时防止 NAT/防火墙因空闲断开
        config.put("ServerAliveInterval", "30");
        // 提升 SFTP 吞吐（mwiede/jsch）
        config.put("max_input_buffer_size", "131072");
        session.setConfig(config);
        session.connect(sshProperties.getConnectTimeoutMs());
        return session;
    }

    /**
     * 执行命令，逐行回调 stdout/stderr（合并），返回退出码与完整输出。
     */
    public OperationSshCommandResult exec(OperationSshSession session, String command, Consumer<String> onLine) {
        ChannelExec channel = null;
        StringBuilder all = new StringBuilder();
        try {
            channel = (ChannelExec) session.getSession().openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(null);
            channel.setPty(false);
            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            channel.connect(sshProperties.getConnectTimeoutMs());

            long deadline = System.currentTimeMillis() + sshProperties.getCommandTimeoutSeconds() * 1000L;
            byte[] buf = new byte[4096];
            StringBuilder lineBuf = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int n = in.read(buf, 0, buf.length);
                    if (n < 0) {
                        break;
                    }
                    appendChunk(all, lineBuf, new String(buf, 0, n, StandardCharsets.UTF_8), onLine);
                }
                while (err.available() > 0) {
                    int n = err.read(buf, 0, buf.length);
                    if (n < 0) {
                        break;
                    }
                    appendChunk(all, lineBuf, new String(buf, 0, n, StandardCharsets.UTF_8), onLine);
                }
                if (channel.isClosed()) {
                    if (in.available() > 0 || err.available() > 0) {
                        continue;
                    }
                    break;
                }
                if (System.currentTimeMillis() > deadline) {
                    throw new BaseException("命令执行超时: " + command);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BaseException("命令执行被中断");
                }
            }
            if (lineBuf.length() > 0) {
                flushLine(lineBuf, onLine);
            }
            return new OperationSshCommandResult(channel.getExitStatus(), all.toString());
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("命令执行失败: " + e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private void appendChunk(StringBuilder all, StringBuilder lineBuf, String chunk, Consumer<String> onLine) {
        all.append(chunk);
        if (onLine == null) {
            return;
        }
        for (int i = 0; i < chunk.length(); i++) {
            char c = chunk.charAt(i);
            if (c == '\n') {
                flushLine(lineBuf, onLine);
            } else if (c != '\r') {
                lineBuf.append(c);
            }
        }
    }

    private void flushLine(StringBuilder lineBuf, Consumer<String> onLine) {
        onLine.accept(lineBuf.toString());
        lineBuf.setLength(0);
    }

    /**
     * 远端文件是否存在。
     */
    public boolean sftpExists(OperationSshSession session, String remotePath) {
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) session.getSession().openChannel("sftp");
            sftp.connect(sshProperties.getConnectTimeoutMs());
            sftp.lstat(remotePath);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (sftp != null && sftp.isConnected()) {
                sftp.disconnect();
            }
        }
    }

    /**
     * 上传本地文件到远端（比 InputStream 流式 put 快，接近 scp 吞吐）。
     */
    public void sftpPutFile(OperationSshSession session, Path localPath, String remotePath, SftpProgressMonitor progress) {
        ChannelSftp sftp = null;
        try {
            sftp = openSftp(session);
            ensureRemoteDir(sftp, remotePath);
            String local = toSftpLocalPath(localPath);
            if (progress != null) {
                sftp.put(local, remotePath, progress, ChannelSftp.OVERWRITE);
            } else {
                sftp.put(local, remotePath, ChannelSftp.OVERWRITE);
            }
        } catch (Exception e) {
            throw new BaseException("SFTP 上传失败: " + e.getMessage());
        } finally {
            disconnectQuietly(sftp);
        }
    }

    /**
     * 上传流到远端路径（自动创建父目录），progress 可空。小文件/内存内容用；大文件请用 {@link #sftpPutFile}。
     */
    public void sftpPut(OperationSshSession session, InputStream data, String remotePath, SftpProgressMonitor progress) {
        ChannelSftp sftp = null;
        try {
            sftp = openSftp(session);
            ensureRemoteDir(sftp, remotePath);
            if (progress != null) {
                sftp.put(data, remotePath, progress, ChannelSftp.OVERWRITE);
            } else {
                sftp.put(data, remotePath, ChannelSftp.OVERWRITE);
            }
        } catch (Exception e) {
            throw new BaseException("SFTP 上传失败: " + e.getMessage());
        } finally {
            disconnectQuietly(sftp);
        }
    }

    private ChannelSftp openSftp(OperationSshSession session) throws Exception {
        ChannelSftp sftp = (ChannelSftp) session.getSession().openChannel("sftp");
        sftp.connect(sshProperties.getConnectTimeoutMs());
        return sftp;
    }

    private static void disconnectQuietly(ChannelSftp sftp) {
        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
        }
    }

    /** JSch 在 Windows 上要求正斜杠绝对路径。 */
    private static String toSftpLocalPath(Path localPath) {
        return localPath.toAbsolutePath().normalize().toString().replace('\\', '/');
    }

    /**
     * 上传文本内容（脚本等）并设置可执行位。
     */
    public void sftpPutText(OperationSshSession session, String content, String remotePath, boolean executable) {
        String normalized = content.replace("\r\n", "\n").replace("\r", "\n");
        sftpPut(session, new ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8)), remotePath, null);
        if (executable) {
            exec(session, "chmod +x " + shellQuote(remotePath), null);
        }
    }

    private void ensureRemoteDir(ChannelSftp sftp, String remotePath) throws Exception {
        int idx = remotePath.lastIndexOf('/');
        if (idx <= 0) {
            return;
        }
        String dir = remotePath.substring(0, idx);
        String[] segments = dir.split("/");
        StringBuilder cur = new StringBuilder();
        for (String seg : segments) {
            if (seg.isEmpty()) {
                continue;
            }
            cur.append('/').append(seg);
            try {
                sftp.lstat(cur.toString());
            } catch (Exception notExist) {
                try {
                    sftp.mkdir(cur.toString());
                } catch (Exception ignored) {
                    // 可能因权限受限，交由后续 put 报错
                }
            }
        }
    }

    /**
     * 单引号安全包裹，避免路径注入。
     */
    public static String shellQuote(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private List<String> resolveHosts(OperationServerInfo server) {
        List<String> hosts = new ArrayList<>();
        String pref = StringUtils.defaultIfBlank(server.getConnPref(), "auto");
        String inner = StringUtils.trimToNull(server.getInnerIp());
        String pub = StringUtils.trimToNull(server.getIp());
        if ("inner".equalsIgnoreCase(pref)) {
            if (inner != null) {
                hosts.add(inner);
            }
        } else if ("public".equalsIgnoreCase(pref)) {
            if (pub != null) {
                hosts.add(pub);
            }
        } else {
            if (inner != null) {
                hosts.add(inner);
            }
            if (pub != null && !pub.equals(inner)) {
                hosts.add(pub);
            }
        }
        return hosts;
    }
}
