# Maven 打包 · 开发 vs 生产

> 全仓版本由根 `pom.xml` 的 **`${revision}`** 统一管理（CI 友好）。  
> **日常默认**：`1.0.0-SNAPSHOT`（见 `.mvn/maven.config`）。  
> **生产发版**：构建时传入 `-Drevision=1.0.0-RELEASE`，**不必**改 pom 文件。

---

## 先分清两件事

| 维度 | 开发 | 生产 |
|------|------|------|
| **Maven 版本**（jar 文件名） | `1.0.0-SNAPSHOT` | `1.0.0-RELEASE` |
| **运行环境**（连哪套库） | `dev` | `pro` |

`dev` / `pro` **不在** `mvn package` 里指定，由服务器 `conf/moli-*.env` 的 `SPRING_PROFILES_ACTIVE=pro` 或启动参数 `--spring.profiles.active=pro` 控制。

---

## 开发打包（默认 SNAPSHOT）

在仓库根目录：

```powershell
cd D:\work\moli_project\moli-project-distribute
mvn clean package -DskipTests
```

v1 最小三件套（user-center + gateway + knowledge）：

```powershell
mvn clean -pl moli-user-center/moli-user-center-server,moli-gateway,moli-knowledge/moli-knowledge-server -am package -DskipTests
```

产物示例：

| 服务 | JAR 路径 |
|------|----------|
| user-center | `moli-user-center/moli-user-center-server/target/moli-user-center-server-1.0.0-SNAPSHOT.jar` |
| gateway | `moli-gateway/target/moli-gateway-1.0.0-SNAPSHOT.jar` |
| knowledge | `moli-knowledge/moli-knowledge-server/target/moli-knowledge-server-1.0.0-SNAPSHOT.jar` |

本地 IDE 直接 Run 时通常无需打包；命令行启动示例：

```powershell
java -jar moli-gateway\target\moli-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## 生产打包（RELEASE）

**PowerShell** 中 `-Drevision=...` 必须加引号，否则会被拆成多个参数：

```powershell
cd D:\work\moli_project\moli-project-distribute

# v1 最小三件套（推荐上线前使用）
mvn clean "-Drevision=1.0.0-RELEASE" -pl moli-user-center/moli-user-center-server,moli-gateway,moli-knowledge/moli-knowledge-server -am package -DskipTests
```

全量模块：

```powershell
mvn clean "-Drevision=1.0.0-RELEASE" package -DskipTests
```

**Linux / macOS / Git Bash**（引号可选）：

```bash
mvn clean -Drevision=1.0.0-RELEASE \
  -pl moli-user-center/moli-user-center-server,moli-gateway,moli-knowledge/moli-knowledge-server \
  -am package -DskipTests
```

产物示例：

| 服务 | JAR |
|------|-----|
| user-center | `moli-user-center-server-1.0.0-RELEASE.jar` |
| gateway | `moli-gateway-1.0.0-RELEASE.jar` |
| knowledge | `moli-knowledge-server-1.0.0-RELEASE.jar` |

上传与启停见 [`deploy/上线流程.md`](../../deploy/上线流程.md) §5–§6（AWS）或 [`deploy/腾讯云上线流程.md`](../../deploy/腾讯云上线流程.md) 对应章节。

---

## 版本号维护

| 场景 | 做法 |
|------|------|
| 日常开发 | 保持根 `pom.xml` `<revision>1.0.0-SNAPSHOT</revision>` 与 `.mvn/maven.config` 一致 |
| 生产构建 | 仅命令行 `-Drevision=1.0.0-RELEASE`，或 CI 注入同名参数 |
| 下一版本 | 改为 `1.0.1-SNAPSHOT` / `1.0.1-RELEASE`，并打 git tag |

发版后建议：`git tag v1.0.0-RELEASE`（或 `v1.0.0`），便于回滚对照。

---

## 运维脚本

[`deploy/linux/moli-service.sh`](../../deploy/linux/moli-service.sh) **不写死**版本号，按 `${JAR_PREFIX}-*.jar` 通配查找（如 `moli-gateway-*.jar`）。  
因此 SNAPSHOT 与 RELEASE jar 均可被识别；**同一目录勿同时保留两个版本**，以免脚本按修改时间选错包。

---

## 相关

- 根 [`pom.xml`](../../pom.xml) · [`flatten-maven-plugin`](../../pom.xml)（install/deploy 时展平 `${revision}`）
- [`deploy/README.md`](../../deploy/README.md) · [v1-release-runbook.md](v1-release-runbook.md)

---

## IntelliJ IDEA 同步失败（Dubbo 1.0.0-SNAPSHOT）

若 Maven Sync 报 `Could not find org.apache.dubbo:dubbo-spring-boot-autoconfigure:1.0.0-SNAPSHOT`，是 IDEA 未正确解析 `${revision}` / BOM，把项目版本当成了 Dubbo 版本。**命令行 `mvn compile` 通常仍正常。**

**处理顺序：**

1. 在仓库根目录执行一次全量安装（写入本地 `~/.m2`）：
   ```powershell
   mvn install -DskipTests
   ```
2. IDEA：**Maven 工具窗口 → Reload All Maven Projects**（刷新图标）。
3. **Settings → Build, Execution, Deployment → Build Tools → Maven → Runner**：勾选 **Delegate IDE build/run actions to Maven**（可选）；确认 **JRE** 与项目 JDK 一致。
4. 仍失败时，在 **Maven → Importing** 中确认已启用 **`.mvn/maven.config`**（含 `-Drevision=1.0.0-SNAPSHOT`）；或于 Runner **VM Options** 手动加：`-Drevision=1.0.0-SNAPSHOT`。
5. **File → Invalidate Caches → Invalidate and Restart**（最后手段）。

父 POM 已显式锁定 `dubbo.version=2.7.13`，Reload 后应拉取 Central 上的正式包，而非 `spring-snapshot` 里的 SNAPSHOT。
