---
title: fe_kamoku_b_set_sample_qs3
slug: fe_kamoku_b_set_sample_qs3
type: article
status: active
tags: []
sources:
 - raw/school/fe/fe_kamoku_b_set_sample_qs3.md
related:
 []
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

擬似言語の記述形式（基本情報技術者試験用）

擬似言語を使用した問題では，各問題文中に注記がない限り，次の記述形式が適用され ているものとする。

〔擬似言語の記述形式〕

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
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">選択処理を示す。<br>条件式を上から評価し，最初に真になった条件式に<br>対応する処理を実行する。以降の条件式は評価せ<br>ず，対応する処理も実行しない。どの条件式も真に<br>ならないときは，処理n<br>＋<br>1を実行する。<br>各処理は，0<br>以上の文の集まりである。<br>elseif<br>と処理の組みは，複数記述することがあり，<br>省略することもある。<br>else と処理 n<br>＋<br>1の組みは一つだけ記述し，省略す<br>ることもある。</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">while (条件式)処理endwhile</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">前判定繰返し処理を示す。<br>条件式が真の間，処理を繰返し実行する。<br>処理は，0<br>以上の文の集まりである。</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">do処理while (条件式)</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">後判定繰返し処理を示す。<br>処理を実行し，条件式が真の間，処理を繰返し実行<br>する。<br>処理は，0<br>以上の文の集まりである。</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">for (制御記述)処理endfor</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">繰返し処理を示す。<br>制御記述の内容に基づいて，処理を繰返し実行する。<br>処理は，0<br>以上の文の集まりである。</td>
 </tr>
</table>

〔演算子と優先順位〕

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" colspan="2">演算子の種類</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">演算子</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">優先度</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" colspan="2">式</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> .</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">高</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" colspan="2">単項演算子</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">not ＋ －</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" rowspan="5"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" rowspan="5">二項演算子</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">乗除</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">mod × ÷</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">加減</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">＋ －</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">関係</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">≠ ≦ ≧ ＜ ＝ ＞</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">論理積</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">and</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">論理和</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">or</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">低</td>
 </tr>
</table>

注記 演算子 . は，メンバ変数又はメソッドのアクセスを表す。 演算子 mod は，剰余算を表す。

〔論理型の定数〕 true，false

〔配列〕

配列の要素は，“[”と“]”の間にアクセス対象要素の要素番号を指定することでア クセスする。なお，二次元配列の要素番号は，行番号，列番号の順に“,”で区切って 指定する。

“{”は配列の内容の始まりを，“}”は配列の内容の終わりを表す。ただし，二次元 配列において，内側の“{”と“}”に囲まれた部分は，1 行分の内容を表す。

〔未定義，未定義の値〕

変数に値が格納されていない状態を，“未定義”という。変数に“未定義の値”を代 入すると，その変数は未定義になる。

問１ 次の記述中の に入れる正しい答えを，解答群の中から選べ。

プログラムを実行すると，“ ”と出力される。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile1.png>)

解答群

- **ア** 1,2
- **イ** 1,3
- **ウ** 2,1
- **エ** 2,3
- **オ** 3,1
- **カ** 3,2

問２ 次のプログラム中の ～ に入れる正しい答えの組合せを， 解答群の中から選べ。

関数 fizzBuzz は，引数で与えられた値が，3 で割り切れて 5 で割り切れない場合 は“3 で割り切れる”を，5 で割り切れて 3 で割り切れない場合は“5 で割り切れ る”を，3 と 5 で割り切れる場合は“3 と 5 で割り切れる”を返す。それ以外の場合 は“3 でも 5 でも割り切れない”を返す。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile2.png>)

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
c

</div>

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
c

</div>

解答群

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">b</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">c</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3 と 5</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3 と 5</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3 と 5</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3 と 5</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">オ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3 と 5</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
 </tr>
</table>

問３ 次の記述中の に入れる正しい答えを，解答群の中から選べ。ここで， 配列の要素番号は 1 から始まる。

関数 makeNewArray は，要素数 2 以上の整数型の配列を引数にとり，整数型の配列 を返す関数である。関数 makeNewArray を makeNewArray({3, 2, 1, 6, 5, 4})として 呼び出したとき，戻り値の配列の要素番号 5 の値は となる。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile3.png>)

解答群
- **ア** 5
- **イ** 6
- **ウ** 9
- **エ** 11
- **オ** 12
- **カ** 17
- **キ** 21

問４ 次のプログラム中の ～ に入れる正しい答えの組合せを， 解答群の中から選べ。

関数 gcd は，引数で与えられた二つの正の整数 num1 と num2 の最大公約数を，次 の(1)～(3) の性質を利用して求める。

- (1) num1 と num2 が等しいとき，num1 と num2 の最大公約数は num1 である。
- (2) num1 が num2 より大きいとき，num1 と num2 の最大公約数は，(num1 － num2) と num2 の最大公約数と等しい。
- (3) num2 が num1 より大きいとき，num1 と num2 の最大公約数は，(num2 － num1) と num1 の最大公約数と等しい。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile4.png>)

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
c

</div>

解答群

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">b</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">c</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">if (x ≠ y)</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">x ＜ y</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">endif</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">if (x ≠ y)</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">x ＞ y</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">endif</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">while (x ≠ y)</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">x ＜ y</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">endwhile</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">while (x ≠ y)</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">x ＞ y</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">endwhile</td>
 </tr>
</table>

問５ 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。

関数 calc は，正の実数 x と y を受け取り， x2＋y2 の計算結果を返す。関数 calc が使う関数 pow は，第 1 引数として正の実数 a を，第 2 引数として実数 b を受け取 り，a の b 乗の値を実数型で返す。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile5.png>)

解答群
- **ア** (pow(x, 2) ＋ pow(y, 2)) ÷ pow(2, 0.5)
- **イ** (pow(x, 2) ＋ pow(y, 2)) ÷ pow(x, y)
- **ウ** pow(2, pow(x, 0.5)) ＋ pow(2, pow(y, 0.5))
- **エ** pow(pow(pow(2, x), y), 0.5)
- **オ** pow(pow(x, 2) ＋ pow(y, 2), 0.5)
- **カ** pow(x, 2) × pow(y, 2) ÷ pow(x, y)
- **キ** pow(x, y) ÷ pow(2, 0.5)

問６ 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。

関数 rev は 8 ビット型の引数 byte を受け取り，ビットの並びを逆にした値を返す。 例えば，関数 rev を rev(01001011) として呼び出すと，戻り値は 11010010 となる。

なお，演算子 ∧ はビット単位の論理積，演算子 ∨ はビット単位の論理和，演算 子 >> は論理右シフト，演算子 << は論理左シフトを表す。例えば，value >> n は value の値を n ビットだけ右に論理シフトし，value << n は value の値を n ビット だけ左に論理シフトする。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile6.png>)

解答群
- **ア** r ← (r << 1) ∨ (rbyte ∧ 00000001) rbyte ← rbyte >> 1
- **イ** r ← (r << 7) ∨ (rbyte ∧ 00000001) rbyte ← rbyte >> 7
- **ウ** r ← (rbyte << 1) ∨ (rbyte >> 7) rbyte ← r
- **エ** r ← (rbyte >> 1) ∨ (rbyte << 7) rbyte ← r

問７ 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。

関数 factorial は非負の整数 n を引数にとり，その階乗を返す関数である。非負 の整数 n の階乗は n が 0 のときに 1 になり，それ以外の場合は 1 から n までの整数 を全て掛け合わせた数となる。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile7.png>)

解答群
- **ア** (n － 1) × factorial(n)
- **イ** factorial(n － 1)
- **ウ** n
- **エ** n × (n － 1)
- **オ** n × factorial(1)
- **カ** n × factorial(n － 1)

### 〔 メ モ 用 紙 〕

問８ 次の記述中の に入れる正しい答えを，解答群の中から選べ。

優先度付きキューを操作するプログラムである。優先度付きキューとは扱う要素 に優先度を付けたキューであり，要素を取り出す際には優先度の高いものから順番 に取り出される。クラス PrioQueue は優先度付きキューを表すクラスである。クラ ス PrioQueue の説明を図に示す。ここで，優先度は整数型の値 1，2，3 のいずれか であり，小さい値ほど優先度が高いものとする。

手続 prioSched を呼び出したとき，出力は の順となる。

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">コンストラクタ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">説明</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">PrioQueue</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">空の優先度付きキューを生成する。</td>
 </tr>
</table>

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">メソッド</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">戻り値</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">説明</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">enqueue(文字列型:<br>s, 整数型:<br>prio)</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">なし</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">優先度付きキューに，文字列<br>s を要素と<br>して，優先度<br>prio<br>で追加する。</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">dequeue</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">文字列型</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">優先度付きキューからキュー内で最も優<br>先度の高い要素を取り出して返す。最も<br>優先度の高い要素が複数あるときは，そ<br>のうちの最初に追加された要素を一つ取<br>り出して返す。</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">size</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">整数型</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">優先度付きキューに格納されている要素<br>の個数を返す。</td>
 </tr>
</table>

図 クラス PrioQueue の説明

○prioSched PrioQueue: prioQueue ← PrioQueue

- prioQueue.enqueue("A", 1)prioQueue.enqueue("B", 2)prioQueue.enqueue("C", 2)prioQueue.enqueue("D", 3)prioQueue.dequeue /* 戻り値は使用しない */prioQueue.dequeue /* 戻り値は使用しない */prioQueue.enqueue("D", 3)
- prioQueue.enqueue("B", 2)prioQueue.dequeue /* 戻り値は使用しない */prioQueue.dequeue /* 戻り値は使用しない */prioQueue.enqueue("C", 2)prioQueue.enqueue("A", 1)while (prioQueue.size が 0 と等しくない)

prioQueue.dequeue の戻り値を出力 endwhile

解答群

- **ア** “A”，“B”，“C”，“D”
- **イ** “A”，“B”，“D”，“D”
- **ウ** “A”，“C”，“C”，“D”
- **エ** “A”，“C”，“D”，“D”

問９ 次の記述中の に入れる正しい答えを，解答群の中から選べ。ここで， 配列の要素番号は 1 から始まる。

手続 order は，図の 2 分木の，引数で指定した節を根とする部分木をたどりなが ら，全ての節番号を出力する。大域の配列 tree が図の 2 分木を表している。配列 tree の要素は，対応する節の子の節番号を，左の子，右の子の順に格納した配列で ある。例えば，配列 tree の要素番号 1 の要素は，節番号 1 の子の節番号から成る配 列であり，左の子の節番号 2，右の子の節番号 3 を配列｛2，3｝として格納する。

手続 order を order(1)として呼び出すと， の順に出力される。

1

2 3

4 5 6 7

8 9 10 11 12 13 14

- 注記 1 ○の中の値は節番号である。注記 2 子の節が一つの場合は，左の子の節とする。図 プログラムが扱う 2 分木

大域: 整数型配列の配列: tree ← {{2, 3}, {4, 5}, {6, 7}, {8, 9},

{10, 11}, {12, 13}, {14}, {}, {}, {}, {}, {}, {}, {}} // {}は要素数0の配列

○order(整数型: n) if (tree[n]の要素数 が 2 と等しい)

- order(tree[n][1])nを出力order(tree[n][2])

elseif (tree[n]の要素数 が 1 と等しい) order(tree[n][1]) nを出力

else

nを出力 endif

解答群

- **ア** 1，2，3，4，5，6，7，8，9，10，11，12，13，14
- **イ** 1，2，4，8，9，5，10，11，3，6，12，13，7，14
- **ウ** 8，4，9，2，10，5，11，1，12，6，13，3，14，7
- **エ** 8，9，4，10，11，5，2，12，13，6，14，7，3，1

問10 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。

手続 delNode は，単方向リストから，引数 pos で指定された位置の要素を削除す る手続である。引数 pos は，リストの要素数以下の正の整数とする。リストの先頭 の位置を 1 とする。

クラス ListElement は，単方向リストの要素を表す。クラス ListElement のメン バ変数の説明を表に示す。ListElement 型の変数はクラス ListElement のインスタン スの参照を格納するものとする。大域変数 listHead には，リストの先頭要素の参照 があらかじめ格納されている。

表 クラス ListElement のメンバ変数の説明

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">メンバ変数</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">型</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">説明</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">val</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">文字型</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">要素の値</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">next</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ListElement</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">次の要素の参照<br>次の要素がないときの状態は未定義</td>
 </tr>
</table>

〔プログラム〕 大域: ListElement: listHead // リストの先頭要素が格納されている

![](<fe_kamoku_b_set_sample_qs_images/imageFile8.png>)

解答群
- **ア** listHead
- **イ** listHead.next
- **ウ** listHead.next.next
- **エ** prev
- **オ** prev.next
- **カ** prev.next.next

問11 次の記述中の に入れる正しい答えを，解答群の中から選べ。ここで， 配列の要素番号は 1 から始まる。

関数 binSort を binSort として呼び出すと，戻り値の配列には未定 義の要素は含まれておらず，値は昇順に並んでいる。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile9.png>)

解答群

- **ア** {2, 6, 3, 1, 4, 5}
- **イ** {3, 1, 4, 4, 5, 2}
- **ウ** {4, 2, 1, 5, 6, 2}
- **エ** {5, 3, 4, 3, 2, 6}

### 〔 メ モ 用 紙 〕

問12 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。こ こで，配列の要素番号は 1 から始まる。

関数 simRatio は，引数として与えられた要素数 1 以上の二つの文字型の配列 s1 と s2 を比較し，要素数が等しい場合は，配列の並びがどの程度似ているかの指標と して，（要素番号が同じ要素の文字同士が一致する要素の組みの個数 ÷ s1の要素 数）を実数型で返す。例えば，配列の全ての要素が一致する場合の戻り値は 1，いず れの要素も一致しない場合の戻り値は 0 である。

なお，二つの配列の要素数が等しくない場合は，－1 を返す。 関数 simRatio に与える s1，s2 及び戻り値の例を表に示す。プログラムでは，配

列の領域外を参照してはならないものとする。

表 関数 simRatio に与える s1，s2 及び戻り値の例

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">s1</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">s2</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">戻り値</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"a", "p",<br>"p", "l", "e"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"a", "p",<br>"p", "l", "e"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"a", "p",<br>"p", "l", "e"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"a", "p",<br>"r", "i", "l"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">0.4</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"a", "p",<br>"p", "l", "e"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"m", "e",<br>"l", "o", "n"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">0</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"a", "p",<br>"p", "l", "e"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">{"p", "e",<br>"n"}</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">－1</td>
 </tr>
</table>

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile10.png>)

解答群

- **ア** s1[i] ≠ s2[cnt]
- **イ** s1[i] ≠ s2[i]
- **ウ** s1[i] ＝ s2[cnt]
- **エ** s1[i] ＝ s2[i]

関数 search は，引数 data で指定された配列に，引数 target で指定された値が含 まれていればその要素番号を返し，含まれていなければ －1 を返す。data は昇順に 整列されており，値に重複はない。

関数 search には不具合がある。例えば，data の 場合は，無限ループ になる。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile11.png>)

解答群
- **ア** 要素数が 1 で，target がその要素の値と等しい
- **イ** 要素数が 2 で，target が data の先頭要素の値と等しい
- **ウ** 要素数が 2 で，target が data の末尾要素の値と等しい
- **エ** 要素に－1 が含まれている

要素数が 1 以上で，昇順に整列済みの配列を基に，配列を特徴づける五つの値を 返すプログラムである。

関数 summarize を summarize({0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1})として呼び出すと，戻り値は である。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile12.png>)

解答群

- **ア** {0.1, 0.3, 0.5, 0.7, 1}
- **イ** {0.1, 0.3, 0.5, 0.8, 1}
- **ウ** {0.1, 0.3, 0.6, 0.7, 1}
- **エ** {0.1, 0.3, 0.6, 0.8, 1}
- **オ** {0.1, 0.4, 0.5, 0.7, 1}
- **カ** {0.1, 0.4, 0.5, 0.8, 1}
- **キ** {0.1, 0.4, 0.6, 0.7, 1}
- **ク** {0.1, 0.4, 0.6, 0.8, 1}

問15 次の記述中の と に入れる正しい答えの組合せを，解答群 の中から選べ。

三目並べにおいて自分が勝利する可能性が最も高い手を決定する。次の手順で， ゲームの状態遷移を木構造として表現し，根以外の各節の評価値を求める。その結 果，根の子の中で最も評価値が高い手を，最も勝利する可能性が高い手とする。自 分が選択した手を○で表し，相手が選択した手を×で表す。

〔手順〕

- (1) 現在の盤面の状態を根とし，勝敗がつくか，引き分けとなるまでの考えられる 全ての手を木構造で表現する。
- (2) 葉の状態を次のように評価する。

- ① 自分が勝ちの場合は 10② 自分が負けの場合は－10③ 引き分けの場合は 0

- (3) 葉以外の節の評価値は，その節の全ての子の評価値を基に決定する。① 自分の手番の節である場合，子の評価値で最大の評価値を節の評価値とする。② 相手の手番の節である場合，子の評価値で最小の評価値を節の評価値とする。

ゲームが図の最上部にある根の状態のとき，自分が選択できる手は三つある。そ のうち A が指す子の評価値は であり，B が指す子の評価値は である。

根の状態

× × ×

自分の手番

A B

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
</table>

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
</table>

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
</table>

相手の手番

勝ち 評価値 10

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
</table>

× × ×

× × ×

× × ×

自分の手番

×

×

×

負け 評価値 －10

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
</table>

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
</table>

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">×</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
</table>

引き分け 評価値 0

勝ち 評価値 10 引き分け 評価値 0

図 三目並べの状態遷移

解答群

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">b</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">0</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">－10</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">0</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">0</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">10</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">－10</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">10</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">0</td>
 </tr>
</table>

問16 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。二 つの には，同じ答えが入る。ここで，配列の要素番号は 1 から始まる。

Unicode の符号位置を，UTF-8 の符号に変換するプログラムである。本問で数値の 後ろに“(16)”と記載した場合は，その数値が 16 進数であることを表す。

Unicode の各文字には，符号位置と呼ばれる整数値が与えられている。UTF-8 は， Unicode の文字を符号化する方式の一つであり，符号位置が 800(16) 以上 FFFF(16) 以下の文字は，次のように 3 バイトの値に符号化する。

3 バイトの長さのビットパターンを 1110xxxx 10xxxxxx 10xxxxxx とする。ビット パターンの下線の付いた“x”の箇所に，符号位置を 2 進数で表した値を右詰めで格 納し，余った“x”の箇所に，0 を格納する。この 3 バイトの値が UTF-8 の符号であ る。

例えば，ひらがなの“あ”の符号位置である 3042(16) を 2 進数で表すと 11000001000010 である。これを，上に示したビットパターンの“x”の箇所に右詰め で格納すると，1110xx11 10000001 10000010 となる。余った二つの“x”の箇所に 0 を格納すると，“あ”の UTF-8 の符号 11100011 10000001 10000010 が得られる。

関数 encode は，引数で渡された Unicode の符号位置を UTF-8 の符号に変換し，先 頭から順に 1 バイトずつ要素に格納した整数型の配列を返す。encode には，引数と して，800(16) 以上 FFFF(16) 以下の整数値だけが渡されるものとする。

〔プログラム〕

![](<fe_kamoku_b_set_sample_qs_images/imageFile13.png>)

解答群
- **ア** ((4 － i) × 2)
- **イ** (2 の (4 － i)乗)
- **ウ** (2 の i 乗)
- **エ** (i × 2)
- **オ** 2
- **カ** 6
- **キ** 16
- **ク** 64
- **ケ** 256

問17 製造業の A 社では，EC サイト（以下，A 社の EC サイトを A サイトという）を使用 し，個人向けの製品販売を行っている。A サイトは，A 社の製品やサービスが検索可 能で，ログイン機能を有しており，あらかじめ A サイトに利用登録した個人（以下， 会員という）の氏名やメールアドレスといった情報（以下，会員情報という）を管 理している。A サイトは，B 社の PaaS で稼働しており，PaaS 上の DBMS とアプリケー ションサーバを利用している。

A 社は，A サイトの開発，運用を C 社に委託している。A 社と C 社との間の委託契 約では，Web アプリケーションプログラムの脆

ぜい

弱性対策は，C 社が実施するとしてい る。

最近，A 社の同業他社が運営している Web サイトで脆弱性が悪用され，個人情報が 漏えいするという事件が発生した。そこで A 社は，セキュリティ診断サービスを行 っている D 社に，A サイトの脆弱性診断を依頼した。脆弱性診断の結果，対策が必要 なセキュリティ上の脆弱性が複数指摘された。図 1 に D 社からの指摘事項を示す。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
- 項番 1 A サイトで利用しているアプリケーションサーバの OS に既知の脆弱性があり，脆弱性 を悪用した攻撃を受けるおそれがある。
- 項番 2 A サイトにクロスサイトスクリプティングの脆弱性があり，会員情報を不正に取得され るおそれがある。
- 項番 3 A サイトで利用している DBMS に既知の脆弱性があり，脆弱性を悪用した攻撃を受ける おそれがある。

</div>

図 1 D 社からの指摘事項

設問 図 1 中の各項番それぞれに対処する組織の適切な組合せを，解答群の中から選 べ。

解答群

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">項番 1</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">項番 2</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">項番 3</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">オ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">カ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">キ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ク</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ケ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">コ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C 社</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B 社</td>
 </tr>
</table>

問18 A 社は IT 開発を行っている従業員 1,000 名の企業である。総務部 50 名，営業部 50 名で，ほかは開発部に所属している。開発部員の 9 割は客先に常駐している。現 在，A 社における PC の利用状況は図 1 のとおりである。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
- 1 A 社の PC ・総務部員，営業部員及び A 社オフィスに勤務する開発部員には，会社が用意した PC（以

下，A 社 PC という）を一人 1 台ずつ貸与している。 ・客先常駐開発部員には，A 社 PC を貸与していないが，代わりに客先常駐開発部員が A 社 オフィスに出社したときに利用するための共用 PC を用意している。

- 2 客先常駐開発部員の業務システム利用 ・客先常駐開発部員が休暇申請，経費精算などで業務システムを利用するためには共用 PC

を使う必要がある。

- 3 A 社の VPN 利用 ・A 社には，VPN サーバが設置されており，営業部員が出張時に A 社 PC からインターネッ

ト経由で社内ネットワークに VPN 接続し，業務システムを利用できるようになってい る。規則で，VPN 接続には A 社 PC を利用すると定められている。

</div>

図 1 A 社における PC の利用状況

A 社では，客先常駐開発部員が業務システムを使うためだけに A 社オフィスに出社 するのは非効率的であると考え，客先常駐開発部員に対して個人所有 PC の業務利用 （BYOD）と VPN 接続の許可を検討することにした。

設問 客先常駐開発部員に，個人所有 PC からの VPN 接続を許可した場合に，増加する 又は新たに生じると考えられるリスクを二つ挙げた組合せは，次のうちどれか。 解答群のうち，最も適切なものを選べ。 (一) VPN 接続が増加し，可用性が損なわれるリスク (二) 客先常駐開発部員が A 社 PC を紛失するリスク (三) 客先常駐開発部員がフィッシングメールの URL をクリックして個人所有 PC

がマルウェアに感染するリスク (四) 総務部員が個人所有 PC を VPN 接続するリスク (五) マルウェアに感染した個人所有 PC が社内ネットワークに VPN 接続され，マ

ルウェアが社内ネットワークに拡散するリスク

解答群

- **ア** (一)，(二)
- **イ** (一)，(三)
- **ウ** (一)，(四)
- **エ** (一)，(五)
- **オ** (二)，(三)
- **カ** (二)，(四)
- **キ** (二)，(五)
- **ク** (三)，(四)
- **ケ** (三)，(五) コ (四)，(五)

問19 A 社は従業員 200 名の通信販売業者である。一般消費者向けに生活雑貨，ギフト商 品などの販売を手掛けている。取扱商品の一つである商品 Z は，Z 販売課が担当して いる。

〔Z 販売課の業務〕

現在，Z 販売課の要員は，商品 Z についての受注管理業務及び問合せ対応業務を行 っている。商品 Z についての受注管理業務の手順を図 1 に示す。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
商品 Z の顧客からの注文は電子メールで届く。

- (1) 入力 販売担当者は，届いた注文（変更，キャンセルを含む）の内容を受注管理システム 1）（以

下，J システムという）に入力し，販売責任者 2）に承認を依頼する。

- (2) 承認 販売責任者は，注文の内容と J システムへの入力結果を突き合わせて確認し，問題がなけ

れば承認する。問題があれば差し戻す。

</div>

- 注 1） A 社情報システム部が運用している。利用者は，販売責任者，販売担当者などである。注 2） Z 販売課の課長 1 名だけである。図 1 受注管理業務の手順

〔J システムの操作権限〕 Z 販売課では，J システムについて，次の利用方針を定めている。 ［方針 1］ ある利用者が入力した情報は，別の利用者が承認する。 ［方針 2］ 販売責任者は，Z 販売課の全業務の情報を閲覧できる。

J システムでは，業務上必要な操作権限を利用者に与える機能が実装されている。 この度，商品 Z の受注管理業務が受注増によって増えていることから，B 社に一部

を委託することにした（以下，商品 Z の受注管理業務の入力作業を行う B 社従業員 を商品 Z の B 社販売担当者といい，商品 Z の B 社販売担当者の入力結果を閲覧して， 不備があれば A 社に口頭で差戻しを依頼する B 社従業員を商品 Z の B 社販売責任者 という）。

委託に当たって，Z 販売課は情報システム部に J システムに関する次の要求事項を 伝えた。

- ［要求 1］ B 社が入力した場合は，A 社が承認する。［要求 2］ A 社の販売担当者が入力した場合は，現状どおりに A 社の販売責任者が承認する。

上記を踏まえ，情報システム部は今後の各利用者に付与される操作権限を表 1 に まとめ，Z 販売課の情報セキュリティリーダーである C さんに確認をしてもらった。

表 1 操作権限案

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" rowspan="2">付与される操作権限<br>利用者</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" colspan="3">J システム</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">閲覧</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">入力</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">承認</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">（省略）</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">○</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">○</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">Z<br>販売課の販売担当者</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">（省略）</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">（省略）</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">（省略）</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"><div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
a1
</div>
</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">〇</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"><div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
a2
</div>
</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">〇</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">〇</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 </tr>
</table>

注記 ○は，操作権限が付与されることを示す。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
a 1

</div>

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
a 2

</div>

設問 表 1 中の

， に入れる字句の適切な組合せを，a に関す る解答群の中から選べ。

a に関する解答群

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a 1</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a 2</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">Z<br>販売課の販売責任者</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品 Z の B<br>社販売責任者</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">Z<br>販売課の販売責任者</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品 Z の B<br>社販売担当者</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品 Z の B<br>社販売責任者</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">Z<br>販売課の販売責任者</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品 Z の B<br>社販売責任者</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品 Z の B<br>社販売担当者</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">オ</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品 Z の B<br>社販売担当者</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品 Z の B<br>社販売責任者</td>
 </tr>
</table>

問20 A 社は栄養補助食品を扱う従業員 500 名の企業である。A 社のサーバ及びファイア ウォール（以下，FW という）を含む情報システムの運用は情報システム部が担当し ている。

ある日，内部監査部の監査があり，FW の運用状況について情報システム部の B 部 長が図 1 のとおり説明したところ，表 1 に示す指摘を受けた。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
・FW を含め，情報システムの運用は，情報システム部の運用チームに所属する 6 名の運用担 当者が担当している。

・FW の運用には，FW ルールの編集，操作ログの確認，並びに編集後の FW ルールの確認及び 操作の承認（以下，編集後の FW ルールの確認及び操作の承認を操作承認という）の三つ がある。

・FW ルールの編集は事前に作成された操作指示書に従って行う。 ・FW の機能には，FW ルールの編集，操作ログの確認，及び操作承認の三つがある。 ・FW ルールの変更には，FW ルールの編集と操作承認の両方が必要である。操作承認の前に

操作ログの確認を行う。 ・FW の利用者 ID は各運用担当者に個別に発行されており，利用者 ID の共用はしていない。 ・FW では，機能を利用する権限を運用担当者の利用者 ID ごとに付与できる。 ・現在は，6 名の運用担当者とも全権限を付与されており，運用担当者は FW のルールの編集

後，編集を行った運用担当者が操作に誤りがないことを確認し，操作承認をしている。 ・FW へのログインにはパスワードを利用している。パスワードは 8 文字の英数字である。 ・FW の運用では，運用担当者の利用者 ID ごとに，ネットワークを経由せずコンソールでロ

グインできるかどうか，ネットワークを経由してリモートからログインできるかどうかを 設定できる。

・FW は，ネットワークを経由せずコンソールでログインした場合でも，ネットワークを経由 してリモートからログインした場合でも，同一の機能を利用できる。 ・FW はサーバルームに設置されており，サーバルームにはほかに数種類のサーバも設置され ている。 ・運用担当者だけがサーバルームへの入退室を許可されている。

</div>

図 1 FW の運用状況

表 1 内部監査部からの指摘

<table style="border-collapse:collapse;width:auto;max-width:100%;">
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">指摘</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">指摘内容</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">指摘 1</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">FW<br>の運用の作業の中で，職務が適切に分離されていない。</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">指摘 2</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">（省略）</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">指摘 3</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">（省略）</td>
 </tr>
 <tr>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">指摘 4</td>
 <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">（省略）</td>
 </tr>
</table>

B 部長は表 1 の指摘に対する改善策を検討することにした。

設問 表 1 中の指摘 1 について，FW ルールの誤った変更を防ぐための改善策はどれか。 解答群のうち，最も適切なものを選べ。

解答群
- **ア** Endpoint Detection and Response（EDR）をコンソールに導入し，監視を強 化する。
- **イ** FW での運用担当者のログインにはパスワード認証の代わりに多要素認証を導 入する。
- **ウ** FW のアクセス制御機能を使って，運用担当者をコンソールからログインできる者，リモートからログインできる者に分ける。
- **エ** FW の運用担当者を 1 人に限定する。
- **オ** 運用担当者の一部を操作ログの確認だけをする者とし，それらの者には操作

ログの確認権限だけを付与する。 カ 運用担当者を，FW ルールの編集を行う者，操作ログを確認し，操作承認をす

る者に分け，それぞれに必要最小限の権限を付与する。 キ 作業を行う運用担当者を，曜日ごとに割り当てる。

試験問題に記載されている会社名又は製品名は，それぞれ各社の商標又は登録商標です。 なお，試験問題では，TM 及び ® を明記していません。

©2022 独立行政法人情報処理推進機構 333
