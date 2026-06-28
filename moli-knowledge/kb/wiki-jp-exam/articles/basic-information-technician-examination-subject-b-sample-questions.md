---
title: 基本情報技術者試験 科目 B サンプル問題
slug: basic-information-technician-examination-subject-b-sample-questions
type: article
status: active
tags: [基本情報技術者試験, 科目B, サンプル問題]
sources: [raw/fe/fe_kamoku_b_set_sample_qs.md]
created: 2026-06-28
updated: 2026-06-28
related: []
---

# 基本情報技術者試験 科目 B サンプル問題

## 試験情報

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

## 擬似言語の記述形式

擬似言語を使用した問題では，各問題文中に注記がない限り，次の記述形式が適用されているものとする。

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">記述形式</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">説明</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">○手続名又は関数名</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">手続又は関数を宣言する。</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">型名: 変数名</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">変数を宣言する。</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">/* 注釈 */</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" rowspan="2">注釈を記述する。</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">// 注釈</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">変数名 ← 式</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">変数に式の値を代入する。</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">手続名又は関数名(引数,<br>…)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">手続又は関数を呼び出し，引数を受け渡す。</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">if (条件式1)処理1<br><br>elseif<br>(条件式2)処理2<br>elseif<br>(条件式n)処理nelse処理n ＋ 1endif</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">条件分岐を行う。</td>
  </tr>
</table>