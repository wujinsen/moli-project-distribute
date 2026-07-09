# 茉莉マイクロサービス（moli-project-distribute）

**Languages / 语言 / 言語**: [中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.12-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Hoxton.SR12-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## プロジェクト概要

**茉莉マイクロサービス**（moli-project-distribute）は、**Spring Cloud + Spring Cloud Alibaba** ベースの分散マイクロサービスサンプルです。API ゲートウェイ、サービス登録・設定、RPC/HTTP 呼び出し、認証・認可、データ永続化などのエンタープライズ機能をカバーします。

ユーザーセンターを基盤とし、注文・BI などの業務モジュールを提供します。Spring Cloud の学習、二次開発、プロジェクトの足場として利用できます。

### 主な機能

- **統一 API ゲートウェイ** — Spring Cloud Gateway によるパスベースルーティング
- **登録・設定センター** — Nacos によるサービス発見と集中設定管理
- **サービス呼び出し** — 外部は Gateway + HTTP/REST；サービス間は Spring Cloud Dubbo RPC
- **トラフィック保護** — Sentinel によるサーキットブレーカー・降格・限流
- **セキュリティ** — Apache Shiro + Redis 分散 Session + JWT
- **データ層** — MySQL + MyBatis-Plus + Druid コネクションプール
- **拡張性** — Seata、RocketMQ、XXL-JOB、ELK、SkyWalking、Prometheus + Grafana（計画）

---

## プロジェクト構成

```
moli-project-distribute/
├── moli-distribute-parent/       # 親 POM、依存関係管理
├── moli-distribute-common/       # 共通ユーティリティ
├── moli-gateway/                 # API ゲートウェイ
├── moli-user-center/             # ユーザーセンター
│   ├── moli-user-center-common/
│   ├── moli-user-center-client/  # Dubbo 契約 + Shiro 統合（order/bi 向け）
│   └── moli-user-center-server/  # Shiro、Dubbo Provider
├── moli-order/
│   └── moli-order-server/
├── moli-ai/                      # BI（Nacos: bi-server）
│   └── moli-ai-server/
├── moli-knowledge/
│   └── moli-knowledge-server/
└── docs/                         # docs/README.md 参照
    ├── product/ design/ api/ test/ ops/ sql/
    └── zh-CN/ en/ ja/
```

### サービス一覧

| モジュール | サービス名 | デフォルトポート | 説明 |
|-----------|-----------|----------------|------|
| moli-gateway | `moli-gateway` | 21000 | 統一 API ゲートウェイ |
| moli-user-center-server | `user-center-server` | **8888** | ユーザー・ロール・メニュー・辞書 |
| moli-order-server | `order-server` | 8087 | 注文（秒殺含む）；Dubbo でユーザーセンター |
| moli-ai-server | `bi-server` | 1128 | BI スケルトン（v1 プレースホルダ） |
| moli-knowledge-server | `knowledge-server` | モジュール README 参照 | ナレッジベース / Ingest |

### ゲートウェイルート

| ルートプレフィックス | 転送先 |
|---------------------|--------|
| `/UserCenter/**` | `lb://user-center-server` |
| `/OrderServer/**` | `lb://order-server` |
| `/BiServer/**` | `lb://bi-server` |
| `/KnowledgeServer/**` | `lb://knowledge-server` |

> [docs/api/gateway-routes.md](docs/api/gateway-routes.md) 参照。

---

## 技術スタック

| 機能 | 技術 |
|------|------|
| サービス発見 | Spring Cloud Alibaba Nacos Discovery |
| 設定センター | Spring Cloud Alibaba Nacos Config |
| API ゲートウェイ | Spring Cloud Gateway |
| ロードバランシング | Spring Cloud Ribbon |
| サーキットブレーカー | Spring Cloud Alibaba Sentinel |
| サービス呼び出し | 外部 HTTP/REST（Gateway）+ サービス間 Spring Cloud Dubbo |
| データベース | MySQL |
| キャッシュ | Redis |
| オブジェクトストレージ | MinIO |
| セキュリティ | Apache Shiro |
| ジョブスケジューリング | XXL-JOB（計画） |
| コネクションプール | Alibaba Druid |
| 永続化 | MyBatis + MyBatis-Plus |
| ログ分析 | ELK |
| トレーシング | SkyWalking |
| 監視 | Prometheus + Grafana |

### コアバージョン

| コンポーネント | バージョン |
|---------------|-----------|
| JDK | 1.8 |
| Spring Boot | 2.3.12.RELEASE |
| Spring Cloud | Hoxton.SR12 |
| Spring Cloud Alibaba | 2.2.7.RELEASE |
| Nacos | 2.0.3 |
| Sentinel | 1.8.1 |
| MySQL | 8.0.3 |
| Redis | 5.0.13 |

> 詳細は [docs/ja/TECH_STACK.md](docs/ja/TECH_STACK.md) を参照。

---

## 環境要件

| 依存 | 推奨バージョン |
|------|---------------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| Nacos Server | 2.0.3 |
| MySQL | 8.0.3 |
| Redis | 5.0.13 |

---

## クイックスタート

### 1. クローン

```bash
git clone git@github.com:wujinsen/moli-project-distribute.git
cd moli-project-distribute
```

### 2. インフラ起動

1. **Nacos** を起動（デフォルト `http://127.0.0.1:8848`）
2. **MySQL** を起動し、DB（例：`moli`）を作成
3. **Redis** を起動

### 3. 設定変更

各モジュールの `bootstrap.yml` と `application-dev.yml` で Nacos・MySQL・Redis を設定。

### 4. ビルド

```bash
cd moli-distribute-parent && mvn clean install -DskipTests
cd ../moli-distribute-common && mvn clean install -DskipTests
cd ../moli-user-center && mvn clean package -DskipTests
```

### 5. サービス起動

1. `moli-user-center-server`
2. `moli-order-server`
3. `moli-ai-server`（任意・v1 スケルトン）
4. `moli-knowledge-server`（任意）
5. `moli-gateway`

ゲートウェイ経由でアクセス：

```
http://localhost:21000/UserCenter/...
http://localhost:21000/OrderServer/...
http://localhost:21000/BiServer/...
http://localhost:21000/KnowledgeServer/...
```

---

## 設定

- **環境切替**：`application.yml` の `spring.profiles.active`（`dev` / `test` / `pre`）
- **Nacos 名前空間**：環境ごとに `bootstrap.yml` で設定
- **Dubbo ポート**：ユーザーセンター `20881`、注文 `20882`

---

## RBAC 権限設計

ユーザーセンターは **RBAC（ロールベースアクセス制御）** を採用。Apache Shiro と Redis 分散 Session で認証・認可を実装。

### 権限モデル

```
ユーザー (SysUser) ──N:N──▶ ロール (SysRole) ──N:N──▶ メニュー (SysMenu)
                                                              │
                                                      perms（ボタン権限）
```

| 概念 | 説明 |
|------|------|
| ユーザー | ログインアカウント；`sys_user_role` でロールに紐付け |
| ロール | 権限集合；`sys_role_menu` でメニューに紐付け |
| メニュー | ディレクトリ(M)・ページ(C)・ボタン(F)；`perms` が API 権限 |
| 部門 | 組織構造（`SysDept`）；ロール権限とは独立 |

### 認証・認可

- **ログイン**：`POST /login` → Shiro でパスワード検証 → `token` + ユーザー + メニューツリー返却
- **メニュー認可**：ユーザーロールでメニュー集約；ユーザー名 `admin` は全メニュー
- **API 権限**：形式 `sys:モジュール:操作`（例：`sys:user:create`）；Shiro アノテーションは予約済み
- **サービス間**：`moli-user-center-client` モジュール；Dubbo でユーザー取得 + Redis セッション共有

| モジュール | パス | 機能 |
|-----------|------|------|
| ユーザー | `/user` | CRUD、ロール割当 |
| ロール | `/role` | CRUD、メニュー権限 |
| メニュー | `/menu` | CRUD、動的ルート |
| 部門 | `/dept` | 部門 CRUD |

> 詳細：[docs/ja/RBAC.md](docs/ja/RBAC.md)

---

## 関連ドキュメント

- [アーキテクチャ / 呼び出し / 認証（日本語）](docs/ja/ARCHITECTURE.md)
- [技術スタック（日本語）](docs/ja/TECH_STACK.md)
- [RBAC 設計（日本語）](docs/ja/RBAC.md)
- [Tech Stack (English)](docs/en/TECH_STACK.md)
- [RBAC (English)](docs/en/RBAC.md)
- [技术栈（中文）](docs/zh-CN/TECH_STACK.md)
- [RBAC（中文）](docs/zh-CN/RBAC.md)

---

## コントリビューション

1. リポジトリを Fork
2. フィーチャーブランチを作成（`git checkout -b feature/xxx`）
3. 変更をコミット（`git commit -m 'Add xxx'`）
4. ブランチをプッシュ（`git push origin feature/xxx`）
5. Pull Request を作成

---

## ライセンス

[Apache License 2.0](LICENSE) の下で公開。

Copyright 2026 wujinsen

---

## 作者

- **wujinsen** — [GitHub](https://github.com/wujinsen)

Issue によるフィードバックを歓迎します。
