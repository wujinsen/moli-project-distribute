令和６年度

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


擬似言語の記述形式（基本情報技術者試験，応用情報技術者試験用）

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

問１ 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。

関数 maximum は，異なる三つの整数を引数で受け取り，そのうちの最大値を返す。

〔プログラム〕

![](<2024r06_fe_kamoku_b_qs_images/imageFile1.png>)

解答群
- **ア** x ＞ y
- **イ** x ＞ y and x ＞ z
- **ウ** x ＞ y and y ＞ z
- **エ** x ＞ z
- **オ** x ＞ z and z ＞ y
- **カ** z ＞ y


問２ 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。

関数 convDecimal は，引数として与えられた，“0”と“1”だけから成る，1 文字 以上の文字列を，符号なしの 2 進数と解釈したときの整数値を返す。例えば，引数と して“10010”を与えると 18 が返る。

関数 convDecimal が利用する関数 int は，引数で与えられた文字が“0”なら整数 値 0 を返し，“1”なら整数値 1 を返す。

〔プログラム〕

![](<2024r06_fe_kamoku_b_qs_images/imageFile2.png>)

解答群
- **ア** result ＋ int(binary の (length － i ＋ 1)文字目の文字)
- **イ** result ＋ int(binary の i文字目の文字)
- **ウ** result × 2 ＋ int(binary の (length － i ＋ 1)文字目の文字)
- **エ** result × 2 ＋ int(binary の i文字目の文字)


問３ 次のプログラム中の に入れる正しい答えを，解答群の中から選べ。こ こで，配列の要素番号は 1 から始まる。

図 1 に示すグラフの頂点には，1 から順に整数で番号が付けられている。グラフは 無向グラフであり，各頂点間には高々一つの辺がある。一つの辺は両端の頂点の番号 を要素にもつ要素数 2 の整数型の配列で表現できる。例えば，{1，3} は頂点 1 と頂 点 3 を端点とする辺を表す。グラフ全体は，グラフに含まれる辺を表す要素数 2 の配 列を全て格納した配列（以下，辺の配列という）で表現できる。辺の配列の要素数は グラフの辺の個数と等しい。図 1 のグラフは整数型配列の配列{{1, 3}, {1, 4}, {3, 4}, {2, 4}, {4, 5}}と表現できる。

![](<2024r06_fe_kamoku_b_qs_images/imageFile3.png>)

関数 edgesToMatrix は，辺の配列を隣接行列に変換する。隣接行列とは，グラフに 含まれる頂点の個数と等しい行数及び列数の正方行列で，i 行 j 列の成分は頂点 i と 頂点 j を結ぶ辺があるときに 1 となり，それ以外は 0 となる。行列の対角成分は全て 0 で，無向グラフの場合は対称行列になる。図 1 のグラフを表現する隣接行列を図 2 に示す。

![](<2024r06_fe_kamoku_b_qs_images/imageFile4.png>)

関数 edgesToMatrix は，引数 edgeList で辺の配列を，引数 nodeNum でグラフの頂 点の個数をそれぞれ受け取り，隣接行列を表す整数型の二次元配列を返す。

〔プログラム〕

![](<2024r06_fe_kamoku_b_qs_images/imageFile5.png>)

解答群

- **ア** adjMatrix[u, u] ← 1
- **イ** adjMatrix[u, u] ← 1adjMatrix[v, v] ← 1
- **ウ** adjMatrix[u, v] ← 1
- **エ** adjMatrix[u, v] ← 1adjMatrix[v, u] ← 1
- **オ** adjMatrix[v, u] ← 1
- **カ** adjMatrix[v, v] ← 1


問４ 次の記述中の に入れる正しい答えを，解答群の中から選べ。ここで， 配列の要素番号は 1 から始まる。

関数 merge は，昇順に整列された整数型の配列 data1 及び data2 を受け取り，これ らを併合してできる昇順に整列された整数型の配列を返す。

関数 merge を merge({2, 3}, {1, 4}) として呼び出すと，/*** α ***/ の行は 。

![](<2024r06_fe_kamoku_b_qs_images/imageFile6.png>)

解答群

- **ア** 実行されない
- **イ** 1 回実行される
- **ウ** 2 回実行される
- **エ** 3 回実行される


問５ 次のプログラム中の ～ に入れる正しい答えの組合せを， 解答群の中から選べ。ここで，配列の要素番号は 1 から始まる。

一度の注文で購入された商品のリストを，注文ごとに記録した注文データがある。 表に，注文データの例を示す。

表 注文データの例

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">注文番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">購入された商品のリスト</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A, B, D</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A, D</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">4</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A, B, E</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">6</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C, E</td>
  </tr>
</table>


注文データから，商品 x と商品 y とが同一の注文で購入されやすい傾向を示す関連 度 Lxy を，次の式で計算する。

![](<2024r06_fe_kamoku_b_qs_images/imageFile7.png>)

手続 putRelatedItem は，大域変数 orders に格納された注文データを基に，引数で 与えられた商品との関連度が最も大きい商品のうちの一つと，その関連度を出力する。 プログラムでは，商品は文字列で表し，注文は購入された商品の配列，注文データは 注文の配列で表している。注文データには 2 種類以上の商品が含まれるものとする。 また，注文データにある商品以外の商品が，引数として与えられることはないものと する。

![](<2024r06_fe_kamoku_b_qs_images/imageFile8.png>)

### 解答群

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">b</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">c</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayK[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayM[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">allItemsの要素数</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayK[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayM[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ordersの要素数</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayK[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayM[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">otherItemsの要素数</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayM[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayK[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">allItemsの要素数</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">オ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayM[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayK[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ordersの要素数</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">カ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayM[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">arrayK[i]</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">otherItemsの要素数</td>
  </tr>
</table>


### 〔 メ モ 用 紙 〕

問６ A 社は従業員 450 名の商社であり，昨年から働き方改革の一環として，在宅でのテ レワークを推進している。A 社のシステム環境を図 1 に示す。

<div style="border:1px solid #ccc;padding:8px 12px;margin:8px 0;line-height:1.5;white-space:normal;word-wrap:break-word;">
・従業員には，一人に 1 台デスクトップ PC（以下，社内 PC という）を貸与している。 ・従業員が利用するシステムには，自社で開発し A 社に設置している業務システムのほか

に，次の二つの SaaS（以下，二つの SaaS を A 社利用クラウドサービスという）があ る。

- 1. メール機能，チャット機能及びクラウドストレージ機能をもつグループウェア （以下，A 社利用グループウェアという）
- 2. オンライン会議サービス


・テレワークでは，従業員の個人所有 PC（以下，私有 PC という）の業務利用（BYOD）を 許可している。

・テレワークでは，社内 PC 及び私有 PC のそれぞれに専用のアプリケーションソフトウェ ア（以下，専用アプリという）を導入し，社内 PC のデスクトップから私有 PC に画面転 送を行うリモートデスクトップ方式を採用している。

・専用アプリには，リモートデスクトップから PC へのファイルのダウンロード及びファ イル，文字列，画像などのコピー＆ペーストを禁止する機能（以下，保存禁止機能とい う）があり，A 社では私有 PC に対して当該機能を有効にしている。

・業務システムには，社内 PC のデスクトップから利用者 ID 及びパスワードを入力してロ グインしている。

・A 社利用クラウドサービスへのログインは，A 社利用クラウドサービス側の設定によっ て A 社の社内ネットワークからだけ可能になるように制限している。ログインには利用 者 ID 及びパスワードを用いている。


</div>


図 1 A 社のシステム環境（抜粋）

テレワークの定着が進むにつれて，社内 PC からインターネットへの接続が極端に 遅くなり，業務に支障をきたしているので改善できないかと，従業員から問合せがあ った。A 社の社内ネットワークとインターネットとの間の通信量を調査したところ， テレワーク導入前に比べ，業務時間帯で顕著に増加していることが判明した。そのた め，情報システム部では，テレワークで A 社利用クラウドサービスに接続する場合に は，A 社の社内ネットワークも社内 PC も介さずに直接接続することを可能にするネ ットワークの設定変更を実施することにした。

設定変更に当たり，情報セキュリティ上の問題がないかを A 社の情報セキュリティ リーダーである B さんが検討したところ，幾つか問題があることが分かった。その一 つは，A 社利用クラウドサービスへの不正アクセスのリスクが増加することである。 そこで B さんは，リスクを低減するために，情報システム部に対策を依頼することに した。

設問 次の対策のうち，情報システム部に依頼することにしたものはどれか。解答群の うち，最も適切なものを選べ。

解答群
- **ア** A 社の社内ネットワークから A 社利用クラウドサービスへの通信を監視する。
- **イ** A 社の社内ネットワークと A 社利用クラウドサービスとの間の通信速度を制限する。
- **ウ** A 社利用クラウドサービスに A 社外から接続する際の認証に 2 要素認証を導入 する。
- **エ** A 社利用クラウドサービスのうち，A 社利用グループウェアだけを直接接続の 対象とする。
- **オ** 専用アプリの保存禁止機能を無効にする。


試験問題に記載されている会社名又は製品名は，それぞれ各社の商標又は登録商標です。 なお，試験問題では，TM 及び ® を明記していません。

©2024 独立行政法人情報処理推進機構

