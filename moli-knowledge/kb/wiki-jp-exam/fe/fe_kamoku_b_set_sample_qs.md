---
title: fe_kamoku_b_set_sample_qs
slug: fe_kamoku_b_set_sample_qs
type: article
status: active
tags: []
sources:
  - raw/school/fe/fe_kamoku_b_set_sample_qs.md
related:
  - articles/basic-information-technician-examination-subject-b-sample-questions
  - articles/基本情報技術者試験 科目 B サンプル問題
  - articles/基本情報技術者試験 科目 B サンプル問題2
  - fe/fe_kamoku_b_set_sample_qs3
  - guides/日本語試験知识库说明
created: 2026-06-29
updated: 2026-06-29
---

# 基本情報技術者試験 科目 B サンプル問題

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">試験時間</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">100分</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">問題番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">問１～問20</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">選択方法</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">全問必須</td>
  </tr>
</table>

問17 製造業の A 社では，EC サイト（以下，A 社の EC サイトを A サイトという）を使用 し，個人向けの製品販売を行っている。A サイトは，A 社の製品やサービスが検索可 能で，ログイン機能を有しており，あらかじめ A サイトに利用登録した個人（以下， 会員という）の氏名やメールアドレスといった情報（以下，会員情報という）を管 理している。A サイトは，B 社の PaaS で稼働しており，PaaS 上の DBMS とアプリケー ションサーバを利用している。

A 社は，A サイトの開発，運用を C 社に委託している。A 社と C 社との間の委託契 約では，Web アプリケーションプログラムの脆

ぜい

弱性対策は，C 社が実施するとしてい る。

最近，A 社の同業他社が運営している Web サイトで脆弱性が悪用され，個人情報が 漏えいするという事件が発生した。そこで A 社は，セキュリティ診断サービスを行 っている D 社に，A サイトの脆弱性診断を依頼した。脆弱性診断の結果，対策が必要 なセキュリティ上の脆弱性が複数指摘された。図 1 に D 社からの指摘事項を示す。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
- 項番 1 A サイトで利用しているアプリケーションサーバの OS に既知の脆弱性があり，脆弱性 を悪用した攻撃を受けるおそれがある。
- 項番 2 A サイトにクロスサイトスクリプティングの脆弱性があり，会員情報を不正に取得され るおそれがある。
- 項番 3 A サイトで利用している DBMS に既