令和５年度

# 基本情報技術者試験 科目 B 公開問題

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">問題番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">問１～問６</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">選択方法</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">全問必須</td>
  </tr>
</table>


注意事項

- 1. 実際の試験は20問で構成されますが，そのうちの6問を公開しています。2. 問題に関する質問にはお答えできません。文意どおり解釈してください。


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
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">() .</td>
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

問１ 次のプログラム中の と に入れる正しい答えの組合せを， 解答群の中から選べ。ここで，配列の要素番号は 1 から始まる。

関数 findPrimeNumbers は，引数で与えられた整数以下の，全ての素数だけを格納 した配列を返す関数である。ここで，引数に与える整数は 2 以上である。

〔プログラム〕

![](<2023r05_fe_kamoku_b_qs_images/imageFile1.png>)

解答群

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">b</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">maxNum</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">i ÷ j の余り<br>が 0 と等しい</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">maxNum</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">i ÷ j の商 が<br>1 と等しくない</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">maxNum ＋ 1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">i ÷ j の余り<br>が 0 と等しい</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">maxNum ＋ 1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">i ÷ j の商 が<br>1 と等しくない</td>
  </tr>
</table>


問２ 次の記述中の に入れる正しい答えを，解答群の中から選べ。

次のプログラムにおいて，手続 proc2 を呼び出すと， の順に出力され る。

〔プログラム〕

![](<2023r05_fe_kamoku_b_qs_images/imageFile2.png>)

解答群
- **ア** “A”，“B”，“B”，“C”
- **イ** “A”，“C”
- **ウ** “A”，“C”，“B”，“C”
- **エ** “B”，“A”，“B”，“C”
- **オ** “B”，“C”，“B”，“A”
- **カ** “C”，“B”
- **キ** “C”，“B”，“A”
- **ク** “C”，“B”，“A”，“C”


問３ 次の記述中の に入れる正しい答えを，解答群の中から選べ。ここで， 配列の要素番号は 1 から始まる。

次の手続 sort は，大域の整数型の配列 data の，引数 first で与えられた要素番号 から引数 last で与えられた要素番号までの要素を昇順に整列する。ここで，first ＜ last とする。手続 sort を sort(1, 5) として呼び出すと，/*** α ***/ の行を最 初に実行したときの出力は“ ”となる。

〔プログラム〕 大域: 整数型の配列: data ← {2, 1, 3, 5, 4}

![](<2023r05_fe_kamoku_b_qs_images/imageFile3.png>)

- **ア** 1 2 3 4 5
- **イ** 1 2 3 5 4
- **ウ** 2 1 3 4 5
- **エ** 2 1 3 5 4


問４ 次の記述中の に入れる正しい答えを，解答群の中から選べ。ここで， 配列の要素番号は 1 から始まる。

関数 add は，引数で指定された正の整数 value を大域の整数型の配列 hashArray に 格納する。格納できた場合は true を返し，格納できなかった場合は false を返す。 ここで，整数 value を hashArray のどの要素に格納すべきかを，関数 calcHash1 及び calcHash2 を利用して決める。

手続 test は，関数 add を呼び出して，hashArray に正の整数を格納する。手続 test の処理が終了した直後の hashArray の内容は， である。

〔プログラム〕 大域: 整数型の配列: hashArray

![](<2023r05_fe_kamoku_b_qs_images/imageFile4.png>)

- **ア** {－1, 3, －1, 18, 11}
- **イ** {－1, 11, －1, 3, －1}
- **ウ** {－1, 11, －1, 18, －1}
- **エ** {－1, 18, －1, 3, 11}
- **オ** {－1, 18, 11, 3, －1}


問５ 次のプログラム中の と に入れる正しい答えの組合せを， 解答群の中から選べ。ここで，配列の要素番号は 1 から始まる。

コサイン類似度は，二つのベクトルの向きの類似性を測る尺度である。関数 calcCosineSimilarity は，いずれも要素数が n(n≧1) である実数型の配列 vector1 と vector2 を受け取り，二つの配列のコサイン類似度を返す。配列 vector1 が {a1, a2, …, an}，配列 vector2 が {b1, b2, …, bn} のとき，コサイン類似度は次の数式で 計算される。ここで，配列 vector1 と配列 vector2 のいずれも，全ての要素に 0 が格 納されていることはないものとする。

![](<2023r05_fe_kamoku_b_qs_images/imageFile5.png>)

〔プログラム〕

![](<2023r05_fe_kamoku_b_qs_images/imageFile6.png>)

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">b</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">(vector1[i] ×<br>vector2[i])の正の<br>平方根</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">denominator ×<br>(tempの正の平方根)</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">(vector1[i] ×<br>vector2[i])の正の<br>平方根</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">denominator ＋<br>(tempの正の平方根)</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">(vector1[i] ×<br>vector2[i])の正の<br>平方根</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">tempの正の平方根</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">vector1[i] ×<br>vector2[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">denominator ×<br>(tempの正の平方根)</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">オ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">vector1[i] ×<br>vector2[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">denominator ＋<br>(tempの正の平方根)</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">カ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">vector1[i] ×<br>vector2[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">tempの正の平方根</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">キ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">vector1[i]の2乗</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">denominator ×<br>(tempの正の平方根)</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ク</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">vector1[i]の2乗</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">denominator ＋<br>(tempの正の平方根)</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ケ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">vector1[i]の2乗</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">tempの正の平方根</td>
  </tr>
</table>


問６ A 社は，放送会社や運輸会社向けに広告制作ビジネスを展開している。A 社は，人 事業務の効率化を図るべく，人事業務の委託を検討することにした。A 社が委託する 業務（以下，B 業務という）を図 1 に示す。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
・採用予定者から郵送されてくる入社時の誓約書，前職の源泉徴収票などの書類を PDF フ

ァイルに変換し，ファイルサーバに格納する。 （省略）


</div>


図 1 B 業務

委託先候補の C 社は，B 業務について，次のように A 社に提案した。 ・B 業務だけに従事する専任の従業員を割り当てる。 ・B 業務では，図 2 の複合機のスキャン機能を使用する。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
・スキャン機能を使用する際は，従業員ごとに付与した利用者 ID とパスワードをパネルに入

力する。 ・スキャンしたデータを PDF ファイルに変換する。 ・PDF ファイルを従業員ごとに異なる鍵で暗号化して，電子メールに添付する。 ・スキャンを実行した本人宛てに電子メールを送信する。 ・PDF ファイルが大きい場合は，PDF ファイルを添付する代わりに，自社の社内ネットワーク

上に設置したサーバ（以下，B サーバという）1)に自動的に保存し，保存先の URL を電子メ ールの本文に記載して送信する。


</div>


注 1) B サーバにアクセスする際は，従業員ごとの利用者 ID とパスワードが必要になる。

図 2 複合機のスキャン機能（抜粋）

A 社は，C 社と業務委託契約を締結する前に，秘密保持契約を締結した。その後，C 社に質問表を送付し，回答を受けて，業務委託での情報セキュリティリスクの評価を 実施した。その結果，図 3 の発見があった。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
・複合機のスキャン機能では，電子メールの差出人アドレス，件名，本文及び添付ファイル 名を初期設定 1)の状態で使用しており，誰がスキャンを実行しても同じである。 ・複合機のスキャン機能の初期設定情報はベンダーの Web サイトで公開されており，誰でも 閲覧できる。


</div>


注 1) 複合機の初期設定は C 社の情報システム部だけが変更可能である。

図 3 発見事項

そこで，A 社では，初期設定の状態のままでは A 社にとって情報セキュリティリス クがあり，初期設定から変更するという対策が必要であると評価した。

設問 対策が必要であると A 社が評価した情報セキュリティリスクはどれか。解答群の うち，最も適切なものを選べ。

解答群

- **ア** B 業務に従事する従業員が，攻撃者からの電子メールを複合機からのものと信 じて本文中にある URL をクリックし，フィッシングサイトに誘導される。その結 果，A 社の採用予定者の個人情報が漏えいする。
- **イ** B 業務に従事する従業員が，複合機から送信される電子メールをスパムメール と誤認し，電子メールを削除する。その結果，再スキャンが必要となり，B 業務 が遅延する。
- **ウ** 攻撃者が，複合機から送信される電子メールを盗聴し，添付ファイルを暗号化 して身代金を要求する。その結果，A 社が復号鍵を受け取るために多額の身代金 を支払うことになる。
- **エ** 攻撃者が，複合機から送信される電子メールを盗聴し，本文に記載されている URL を使って B サーバにアクセスする。その結果，A 社の採用予定者の個人情報 が漏えいする。


試験問題に記載されている会社名又は製品名は，それぞれ各社の商標又は登録商標です。 なお，試験問題では，TM 及び ® を明記していません。

©2023 独立行政法人情報処理推進機構

