# 茉莉マイクロサービス — 技術スタック

**Languages / 语言 / 言語**: [中文](../zh-CN/TECH_STACK.md) | [English](../en/TECH_STACK.md) | [日本語](TECH_STACK.md)

> moli-project-distribute の技術体系、バージョン要件、コンポーネント責務を説明します。

---

## 1. 概要

**Spring Cloud + Spring Cloud Alibaba** ベース。サービス登録・API ゲートウェイ・設定管理・サーキットブレーカー・RPC 呼び出しに対応。MySQL、Redis、可観測性コンポーネントでユーザーセンター・注文・BI を支えます。

---

## 2. マイクロサービスアーキテクチャ

| 機能 | コンポーネント | 説明 |
|------|---------------|------|
| サービス発見 | **Nacos Discovery** | Nacos へ登録；ゲートウェイがサービス名でルーティング |
| 設定センター | **Nacos Config** | `bootstrap.yml` 等の集中管理・動的更新 |
| API ゲートウェイ | **Spring Cloud Gateway** | 統一入口（`moli-gateway`） |
| ロードバランシング | **Spring Cloud Ribbon** | Nacos 連携クライアント LB |
| サーキットブレーカー | **Sentinel** | 限流・熔断・降格 |
| サービス呼び出し | **Spring Cloud Dubbo** | サービス間呼び出しは Dubbo RPC に統一；外部トラフィックはゲートウェイ経由の HTTP/REST |

### 呼び出し方式

- **サービス間はすべて Dubbo**：高性能なバイナリ RPC で HTTP 非公開のため、内部 API が外部から直接呼ばれるリスクを回避。
  - プロバイダ：ユーザーセンター `UserServerProvider`（`@DubboService(version="1.0.0", group="moli")`）。
  - コンシューマ：`order-server` / `bi-server` は `ShiroConfig` で `@DubboReference` し、`ShiroRealm` へ注入してログイン認証。
  - レジストリ：Dubbo は `spring-cloud://` で Nacos に登録し、Spring Cloud と共用。
- **外部トラフィックは HTTP/REST**：ブラウザ/`meiling-ui` → ゲートウェイ → 各サービス Controller、統一 `MoliResult<T>` レスポンス。
- 備考：初期サンプルの OpenFeign（`UserCenterClient`）は、内部呼び出しのために REST を公開しないよう削除。詳細は `docs/ja/ARCHITECTURE.md`。

---

## 3. コアバージョン

### 3.1 Spring エコシステム

| コンポーネント | バージョン | 備考 |
|---------------|-----------|------|
| Java | **JDK 1.8** | `maven.compiler.source/target = 8` |
| Spring Boot | **2.3.12.RELEASE** | 親 POM |
| Spring Cloud | **Hoxton.SR12** | Boot 2.3.x 対応 |
| Spring Cloud Alibaba | **2.2.7.RELEASE** | Nacos、Sentinel、Dubbo |

### 3.2 Alibaba ミドルウェア（実行時）

| コンポーネント | バージョン | 用途 |
|---------------|-----------|------|
| Nacos | **2.0.3** | レジストリ + 設定 |
| Sentinel | **1.8.1** | 流量制御・熔断 |
| Seata | **1.3.0** | 分散トランザクション（計画） |
| RocketMQ | **4.6.1** | メッセージキュー（計画） |

---

## 4. データストレージとキャッシュ

| 種別 | 技術 | バージョン | 説明 |
|------|------|-----------|------|
| RDBMS | **MySQL** | **8.0.3** | `mysql-connector-java` |
| キャッシュ | **Redis** | **5.0.13** | Jedis + Spring Data Redis |
| オブジェクトストレージ | **MinIO** | 7.0.2 | `io.minio:minio` |
| コネクションプール | **Druid** | 1.1.14 | プール + 監視 |
| ORM | **MyBatis + MyBatis-Plus** | 3.4.2 | CRUD 拡張 |

---

## 5. セキュリティ・ジョブ・ツール

| カテゴリ | 技術 | バージョン | 説明 |
|---------|------|-----------|------|
| セキュリティ | **Apache Shiro** | 1.4.2 | `shiro-redis` 分散 Session |
| Token | **java-jwt** | 3.8.2 | JWT 認証 |
| ジョブ | **XXL-JOB** | — | 計画 |
| API ドキュメント | **Swagger** | 2.9.2 | インターフェース文書 |
| JSON | **Fastjson** | 1.2.46 / 1.2.70 | シリアライズ |
| Excel | **EasyExcel** | 2.2.10 | インポート/エクスポート |
| ユーティリティ | **Lombok** | 1.18.6 | ボイラープレート削減 |
| バリデーション | **Hibernate Validator** | 6.1.6.Final | JSR-303/380 |

> RBAC モデル・認証フロー：[RBAC.md](RBAC.md)

---

## 6. 可観測性

| 機能 | 技術 | 説明 |
|------|------|------|
| ログ分析 | **ELK** | 集中ログ分析 |
| トレーシング | **SkyWalking** | 分散トレーシング |
| 監視 | **Prometheus + Grafana** | メトリクスとダッシュボード |

---

## 7. モジュール依存

| モジュール | 主要依存 |
|-----------|---------|
| moli-gateway | Nacos Discovery、Spring Cloud Gateway |
| moli-user-center-server | Nacos、Sentinel、Dubbo、MyBatis-Plus、Shiro、Redis、MinIO |
| moli-user-center-client | Nacos Discovery、Spring Cloud Dubbo、`UserCenterServer` 契約、Shiro 統合 |
| moli-order-server | Nacos、Sentinel、Dubbo、MyBatis-Plus、Shiro（client モジュール） |
| moli-bi-server | Nacos、Dubbo、Shiro（client モジュール） |

---

## 8. 環境要件

| 項目 | 要件 |
|------|------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| Nacos Server | 2.0.3 |
| MySQL | 8.0.3 |
| Redis | 5.0.13 |

**前提条件：** Nacos 起動済み；MySQL・Redis 利用可能；Dubbo レジストリ到達可能。

---

## 9. バージョン互換性

| スタック | バージョン |
|---------|-----------|
| Boot + Cloud + Alibaba | 2.3.12 + Hoxton.SR12 + 2.2.7 |
| Nacos Client | Nacos Server 2.0.x 対応 |
| Dubbo Spring Cloud | `spring-cloud-starter-dubbo` で管理 |

---

## 10. 参考リンク

- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba](https://github.com/alibaba/spring-cloud-alibaba/wiki)
- [Nacos](https://nacos.io/docs/latest/what-is-nacos/)
- [Sentinel](https://sentinelguard.io/zh-cn/docs/introduction.html)
- [Apache Dubbo](https://dubbo.apache.org/zh/)
