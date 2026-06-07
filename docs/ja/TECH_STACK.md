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
| サービス呼び出し | **Dubbo** + **OpenFeign** | RPC と HTTP REST 呼び出し |

### 呼び出し方式

- **Dubbo**：高性能 RPC（`UserServerProvider`、`@DubboReference`）
- **OpenFeign**：REST 向け宣言型 HTTP クライアント（`UserCenterClient`）

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
| moli-user-center-client | Nacos Discovery、OpenFeign |
| moli-order-server | Nacos、Sentinel、Dubbo、OpenFeign、MyBatis-Plus |
| moli-bi-server | OpenFeign（ユーザーセンター呼び出し） |

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
