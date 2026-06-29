# 基本情報技術者試験 科目 A サンプル問題

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">試験時間</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">90分</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">問題番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">問１～問60</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">選択方法</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">全問必須</td>
  </tr>
</table>


問題文中で共通に使用される表記ルール

各問題文中に注記がない限り，次の表記ルールが適用されているものとする。

１. 論理回路

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">図記号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">説明</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile1.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">論理積素子（AND）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile2.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">否定論理積素子（NAND）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile3.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">論理和素子（OR）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile4.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">否定論理和素子（NOR）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile5.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">排他的論理和素子（XOR）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile6.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">論理一致素子</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile7.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">バッファ</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile8.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">論理否定素子（NOT）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile9.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">スリーステートバッファ</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile10.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">素子や回路の入力部又は出力<br>部に示される<br>印は，論理状態<br>の反転又は否定を表す。</td>
  </tr>
</table>


## ２. 回路記号

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">図記号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">説明</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile11.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">抵抗（R）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile12.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">コンデンサ（C）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile13.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ダイオード（D）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile14.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">トランジスタ（Tr）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">![](<fe_kamoku_a_set_sample_qs_images/imageFile15.png>)</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">接地</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">+ －</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">演算増幅器</td>
  </tr>
</table>


問１ 負数を 2 の補数で表すとき，8 ビットの 2 進正数 n に対し－n を求める式はどれか。 ここで，＋は加算を表し，OR はビットごとの論理和，XOR はビットごとの排他的論理 和を表す。

- **ア** (n OR 10000000) ＋ 00000001
- **イ** (n OR 11111110) ＋ 11111111
- **ウ** (n XOR 10000000) ＋ 11111111
- **エ** (n XOR 11111111) ＋ 00000001


問２ 次の流れ図は，10 進整数 j（0 ＜ j ＜ 100）を 8 桁の 2 進数に変換する処理を表し ている。2 進数は下位桁から順に，配列の要素 NISHIN (1) から NISHIN (8) に格納さ れる。流れ図の a 及び b に入れる処理はどれか。ここで，j div 2 は j を 2 で割った 商の整数部分を，j mod 2 は j を 2 で割った余りを表す。

![](<fe_kamoku_a_set_sample_qs_images/imageFile16.png>)

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">a</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">b</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">j ← j div 2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">NISHIN (k) ← j<br>mod 2</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">j ← j mod 2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">NISHIN (k) ← j<br>div 2</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">NISHIN (k) ← j<br>div 2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">j ← j mod 2</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">NISHIN (k) ← j<br>mod 2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">j ← j div 2</td>
  </tr>
</table>


問３ P，Q，R はいずれも命題である。命題 P の真理値は真であり，命題 (not P ) or Q 及び命題 (not Q ) or R のいずれの真理値も真であることが分かっている。Q，R の 真理値はどれか。ここで，X or Y は X と Y の論理和，not X は X の否定を表す。

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">Q</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">R</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">偽</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">偽</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">偽</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">真</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">真</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">偽</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">真</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">真</td>
  </tr>
</table>


問４ 入力記号，出力記号の集合が｛0，1｝であり，状態遷移図で示されるオートマトン がある。0011001110 を入力記号とした場合の出力記号はどれか。ここで，入力記号 は左から順に読み込まれるものとする。また，S1 は初期状態を表し，遷移の矢印のラ ベルは，入力／出力を表している。

![](<fe_kamoku_a_set_sample_qs_images/imageFile17.png>)

- **ア** 0001000110
- **イ** 0001001110
- **ウ** 0010001000
- **エ** 0011111110


問５ 2 分探索木になっている 2 分木はどれか。

![](<fe_kamoku_a_set_sample_qs_images/imageFile18.png>)

問６ 配列 A が図 2 の状態のとき，図 1 の流れ図を実行すると，配列 B が図 3 の状態にな った。図 1 の a に入れる操作はどれか。ここで，配列 A，B の要素をそれぞれ A (i，j ) ，B (i，j ) とする。

![](<fe_kamoku_a_set_sample_qs_images/imageFile19.png>)

- **ア** B (7－i，7－j ) ← A (i，j )
- **イ** B (7－j，i ) ← A (i，j )
- **ウ** B (i，7－j ) ← A (i，j )
- **エ** B ( j，7－i ) ← A (i，j )


問７ 10 進法で 5 桁の数 a 1 a 2 a 3 a 4 a 5 を，ハッシュ法を用いて配列に格納したい。ハ ッシュ関数を mod ( a 1＋a 2＋a 3＋a 4＋a 5 ，13 ) とし，求めたハッシュ値に対応する 位置の配列要素に格納する場合，54321 は配列のどの位置に入るか。ここで， mod ( x ，13 ) は，x を 13 で割った余りとする。

![](<fe_kamoku_a_set_sample_qs_images/imageFile20.png>)

- **ア** 1
- **イ** 2
- **ウ** 7
- **エ** 11


問８ 自然数 n に対して，次のとおり再帰的に定義される関数 f (n) を考える。f (5) の 値はどれか。

f (n)： if n≦1 then return 1 else return n ＋ f (n－1)

- **ア** 6
- **イ** 9
- **ウ** 15
- **エ** 25


問９ プログラムのコーディング規約に規定する事項のうち，適切なものはどれか。

- **ア** 局所変数は，用途が異なる場合でもデータ型が同じならば，できるだけ同一の変 数を使うようにする。
- **イ** 処理性能を向上させるために，ループの制御変数には浮動小数点型変数を使用する。
- **ウ** 同様の計算を何度も繰り返すときは，関数の再帰呼出しを用いる。
- **エ** 領域割付け関数を使用するときは，割付けができなかったときの処理を記述する。


問10 外部割込みの原因となるものはどれか。

- **ア** ゼロによる除算命令の実行
- **イ** 存在しない命令コードの実行
- **ウ** タイマーによる時間経過の通知
- **エ** ページフォールトの発生



問11 メモリのエラー検出及び訂正に ECC を利用している。データバス幅 2n ビットに対 して冗長ビットが n ＋ 2 ビット必要なとき，128 ビットのデータバス幅に必要な冗長 ビットは何ビットか。



- **ア** 7
- **イ** 8
- **ウ** 9
- **エ** 10


問12 A ～ D を，主記憶の実効アクセス時間が短い順に並べたものはどれか。

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" rowspan="2"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;" colspan="3">キャッシュメモリ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">主記憶</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">有無</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">アクセス時間<br>（ナノ秒）</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ヒット率<br>（％）</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">アクセス時間<br>（ナノ秒）</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">なし</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">－</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">－</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">15</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">なし</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">－</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">－</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">30</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">C</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">あり</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">20</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">60</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">70</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">D</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">あり</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">10</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">90</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">80</td>
  </tr>
</table>


- **ア** A，B，C，D
- **イ** A，D，B，C
- **ウ** C，D，A，B
- **エ** D，C，A，B



問13 仮想化マシン環境を物理マシン 20 台で運用しているシステムがある。次の運用条 件のとき，物理マシンが最低何台停止すると縮退運転になるか。



〔運用条件〕

- (1) 物理マシンが停止すると，そこで稼働していた仮想マシンは他の全ての物理マ シンで均等に稼働させ，使用していた資源も同様に配分する。
- (2) 物理マシンが 20 台のときに使用する資源は，全ての物理マシンにおいて 70％ である。
- (3) 1 台の物理マシンで使用している資源が 90％を超えた場合，システム全体が縮 退運転となる。
- (4) (1) ～(3) 以外の条件は考慮しなくてよい。
- **ア** 2
- **イ** 3
- **ウ** 4
- **エ** 5


問14 図のように，1 台のサーバ，3 台のクライアント及び 2 台のプリンタが LAN で接続 されている。このシステムはクライアントからの指示に基づいて，サーバにあるデー タをプリンタに出力する。各装置の稼働率が表のとおりであるとき，このシステムの 稼働率を表す計算式はどれか。ここで，クライアントは 3 台のうちどれか 1 台が稼働 していればよく，プリンタは 2 台のうちどちらかが稼働していればよい。



![](<fe_kamoku_a_set_sample_qs_images/imageFile21.png>)

- **ア** ab3 c2
- **イ** a (1 － b3 ) (1 － c2 )
- **ウ** a (1 － b)3 (1 － c)2
- **エ** a (1 － (1 － b)3 ) (1 － (1 － c)2 )


問15 図の送信タスクから受信タスクに T 秒間連続してデータを送信する。1 秒当たりの 送信量を S，1 秒当たりの受信量を R としたとき，バッファがオーバフローしないバ ッファサイズ L を表す関係式として適切なものはどれか。ここで，受信タスクよりも 送信タスクの方が転送速度は速く，次の転送開始までの時間間隔は十分にあるものと する。

![](<fe_kamoku_a_set_sample_qs_images/imageFile22.png>)

- **ア** L ＜（ R － S ）× T
- **イ** L ＜（ S － R ）× T
- **ウ** L ≧（ R － S ）× T
- **エ** L ≧（ S － R ）× T



問16 インタプリタの説明として，適切なものはどれか。



- **ア** 原始プログラムを，解釈しながら実行するプログラムである。
- **イ** 原始プログラムを，推論しながら翻訳するプログラムである。
- **ウ** 原始プログラムを，目的プログラムに翻訳するプログラムである。
- **エ** 実行可能なプログラムを，主記憶装置にロードするプログラムである。


問17 三つの媒体 A ～ C に次の条件でファイル領域を割り当てた場合，割り当てた領域 の総量が大きい順に媒体を並べたものはどれか。

〔条件〕

- (1) ファイル領域を割り当てる際の媒体選択アルゴリズムとして，空き領域が最大の 媒体を選択する方式を採用する。
- (2) 割当て要求されるファイル領域の大きさは，順に 90，30，40，40，70，30 （M バイト）であり，割り当てられたファイル領域は，途中で解放されない。
- (3) 各媒体は容量が同一であり，割当て要求に対して十分な大きさをもち，初めは全 て空きの状態である。
- (4) 空き領域の大きさが等しい場合には，A，B，C の順に選択する。
- **ア** A，B，C
- **イ** A，C，B
- **ウ** B，A，C
- **エ** C，B，A



問18 ファイルシステムの絶対パス名を説明したものはどれか。

- **ア** あるディレクトリから対象ファイルに至る幾つかのパス名のうち，最短のパス名
- **イ** カレントディレクトリから対象ファイルに至るパス名
- **ウ** ホームディレクトリから対象ファイルに至るパス名エルートディレクトリから対象ファイルに至るパス名



問19 DRAM の特徴はどれか。



- **ア** 書込み及び消去を一括又はブロック単位で行う。
- **イ** データを保持するためのリフレッシュ操作又はアクセス操作が不要である。
- **ウ** 電源が遮断された状態でも，記憶した情報を保持することができる。
- **エ** メモリセル構造が単純なので高集積化することができ，ビット単価を安くできる。


問20 次のような注文データが入力されたとき，注文日が入力日以前の営業日かどうかを 検査するチェックはどれか。

注文データ

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">伝票番号<br>（文字）</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">注文日<br>（文字）</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品コード<br>（文字）</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">数量 （数値）</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">顧客コード<br>（文字）</td>
  </tr>
</table>


- **ア** シーケンスチェック
- **イ** 重複チェック
- **ウ** フォーマットチェック
- **エ** 論理チェック



問21 RDBMS におけるビューに関する記述のうち，適切なものはどれか。

- **ア** ビューとは，名前を付けた導出表のことである。
- **イ** ビューに対して，ビューを定義することはできない。
- **ウ** ビューの定義を行ってから，必要があれば，その基底表を定義する。
- **エ** ビューは一つの基底表に対して一つだけ定義できる。



問22 UML を用いて表した図の概念データモデルの解釈として，適切なものはどれか。



![](<fe_kamoku_a_set_sample_qs_images/imageFile23.png>)

- **ア** 従業員の総数と部署の総数は一致する。
- **イ** 従業員は，同時に複数の部署に所属してもよい。
- **ウ** 所属する従業員がいない部署の存在は許されない。
- **エ** どの部署にも所属しない従業員が存在してもよい。


問23 ビッグデータのデータ貯蔵場所であるデータレイクの特徴として，適切なものはど れか。

- **ア** あらゆるデータをそのままの形式や構造で格納しておく。
- **イ** データ量を抑えるために，データの記述情報であるメタデータは格納しない。
- **ウ** データを格納する前にデータ利用方法を設計し，それに沿ってスキーマをあらかじめ定義しておく。
- **エ** テキストファイルやバイナリデータなど，格納するデータの形式に応じてリポジ トリを使い分ける。



問24 関係モデルにおいて表 X から表 Y を得る関係演算はどれか。



X Y

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品名</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">価格</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">数量</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A01</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">カメラ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">13,000</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">20</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A02</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">テレビ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">58,000</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">15</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B01</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">冷蔵庫</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">65,000</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">8</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B05</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">洗濯機</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">48,000</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">10</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B06</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">乾燥機</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">35,000</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
  </tr>
</table>


<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">商品番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">数量</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A01</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">20</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A02</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">15</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B01</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">8</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B05</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">10</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B06</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
  </tr>
</table>


- **ア** 結合（join）
- **イ** 射影（projection）
- **ウ** 選択（selection）
- **エ** 併合（merge）


問25 IoT で用いられる無線通信技術であり，近距離の IT 機器同士が通信する無線 PAN （Personal Area Network）と呼ばれるネットワークに利用されるものはどれか。

- **ア** BLE（Bluetooth Low Energy）
- **イ** LTE（Long Term Evolution）
- **ウ** PLC（Power Line Communication）
- **エ** PPP（Point-to-Point Protocol）



問26 1.5 M ビット／秒の伝送路を用いて 12 M バイトのデータを転送するのに必要な伝送 時間は何秒か。ここで，伝送路の伝送効率を 50％とする。

- **ア** 16
- **イ** 32
- **ウ** 64
- **エ** 128



問27 TCP/IP を利用している環境で，電子メールに画像データなどを添付するための規 格はどれか。

- **ア** JPEG
- **イ** MIME
- **ウ** MPEG
- **エ** SMTP



問28 トランスポート層のプロトコルであり，信頼性よりもリアルタイム性が重視される 場合に用いられるものはどれか。



- **ア** HTTP
- **イ** IP
- **ウ** TCP
- **エ** UDP


問29 PC と Web サーバが HTTP で通信している。PC から Web サーバ宛てのパケットでは， 送信元ポート番号は PC 側で割り当てた 50001，宛先ポート番号は 80 であった。Web サーバから PC への戻りのパケットでのポート番号の組合せはどれか。

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">送信元（Web<br>サーバ）のポート番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">宛先（PC）の<br>ポート番号</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ア</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">80</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">50001</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">イ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">50001</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">80</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">ウ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">80 と 50001<br>以外からサーバ側で割り当てた番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">80</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">エ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">80 と 50001<br>以外からサーバ側で割り当てた番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">50001</td>
  </tr>
</table>



問30 緊急事態を装って組織内部の人間からパスワードや機密情報を入手する不正な行為 は，どれに分類されるか。



- **ア** ソーシャルエンジニアリングイトロイの木馬
- **ウ** 踏み台攻撃
- **エ** ブルートフォース攻撃


問31 ボットネットにおける C&C サーバの役割として，適切なものはどれか。

- **ア** Web サイトのコンテンツをキャッシュし，本来のサーバに代わってコンテンツを 利用者に配信することによって，ネットワークやサーバの負荷を軽減する。
- **イ** 外部からインターネットを経由して社内ネットワークにアクセスする際に，CHAP などのプロトコルを中継することによって，利用者認証時のパスワードの盗聴を防 止する。
- **ウ** 外部からインターネットを経由して社内ネットワークにアクセスする際に，時刻 同期方式を採用したワンタイムパスワードを発行することによって，利用者認証時 のパスワードの盗聴を防止する。
- **エ** 侵入して乗っ取ったコンピュータに対して，他のコンピュータへの攻撃などの不 正な操作をするよう，外部から命令を出したり応答を受け取ったりする。



問32 メッセージ認証符号の利用目的に該当するものはどれか。

- **ア** メッセージが改ざんされていないことを確認する。
- **イ** メッセージの暗号化方式を確認する。
- **ウ** メッセージの概要を確認する。
- **エ** メッセージの秘匿性を確保する。



問33 UPS の導入によって期待できる情報セキュリティ対策としての効果はどれか。



- **ア** PC が電力線通信（PLC）からマルウェアに感染することを防ぐ。
- **イ** サーバと端末間の通信における情報漏えいを防ぐ。
- **ウ** 電源の瞬断に起因するデータの破損を防ぐ。
- **エ** 電子メールの内容が改ざんされることを防ぐ。


問34 ファジングに該当するものはどれか。

- **ア** サーバに FIN パケットを送信し，サーバからの応答を観測して，稼働しているサ ービスを見つけ出す。
- **イ** サーバの OS やアプリケーションソフトウェアが生成したログやコマンド履歴な どを解析して，ファイルサーバに保存されているファイルの改ざんを検知する。
- **ウ** ソフトウェアに，問題を引き起こしそうな多様なデータを入力し，挙動を監視して，脆弱性を見つけ出す。
- **エ** ネットワーク上を流れるパケットを収集し，そのプロトコルヘッダやペイロード を解析して，あらかじめ登録された攻撃パターンと一致するものを検出する。



問35 マルウェアの動的解析に該当するものはどれか。



- **ア** 検体のハッシュ値を計算し，オンラインデータベースに登録された既知のマルウェアのハッシュ値のリストと照合してマルウェアを特定する。
- **イ** 検体をサンドボックス上で実行し，その動作や外部との通信を観測する。
- **ウ** 検体をネットワーク上の通信データから抽出し，さらに，逆コンパイルして取得したコードから検体の機能を調べる。
- **エ** ハードディスク内のファイルの拡張子とファイルヘッダの内容を基に，拡張子が 偽装された不正なプログラムファイルを検出する。


問36 SQL インジェクション攻撃による被害を防ぐ方法はどれか。

- **ア** 入力された文字が，データベースへの問合せや操作において，特別な意味をもつ 文字として解釈されないようにする。
- **イ** 入力に HTML タグが含まれていたら，HTML タグとして解釈されない他の文字列に 置き換える。
- **ウ** 入力に上位ディレクトリを指定する文字列（ . . / ）が含まれているときは受け 付けない。
- **エ** 入力の全体の長さが制限を超えているときは受け付けない。



問37 電子メールをドメイン A の送信者がドメイン B の宛先に送信するとき，送信者をド メイン A のメールサーバで認証するためのものはどれか。

- **ア** APOP
- **イ** POP3S
- **ウ** S/MIME
- **エ** SMTP-AUTH



問38 オブジェクト指向プログラムにおいて，データとメソッドを一つにまとめ，オブジ ェクトの実装の詳細をユーザから見えなくすることを何と呼ぶか。

- **ア** インスタンス
- **イ** カプセル化
- **ウ** クラスタ化
- **エ** 抽象化



問39 モジュール結合度が最も弱くなるものはどれか。



- **ア** 一つのモジュールで，できるだけ多くの機能を実現する。
- **イ** 二つのモジュール間で必要なデータ項目だけを引数として渡す。
- **ウ** 他のモジュールとデータ項目を共有するためにグローバルな領域を使用する。
- **エ** 他のモジュールを呼び出すときに，呼び出したモジュールの論理を制御するための引数を渡す。


問40 モジュールの内部構造を考慮することなく，仕様書どおりに機能するかどうかをテ ストする手法はどれか。

- **ア** トップダウンテスト
- **イ** ブラックボックステスト
- **ウ** ボトムアップテスト
- **エ** ホワイトボックステスト



問41 アジャイル開発のスクラムにおけるスプリントのルールのうち，適切なものはどれ か。

- **ア** スプリントの期間を決定したら，スプリントの 1 回目には要件定義工程を，2 回 目には設計工程を，3 回目にはコード作成工程を，4 回目にはテスト工程をそれぞ れ割り当てる。
- **イ** 成果物の内容を確認するスプリントレビューを，スプリントの期間の中間時点で 実施する。
- **ウ** プロジェクトで設定したスプリントの期間でリリース判断が可能なプロダクトインクリメントができるように，スプリントゴールを設定する。
- **エ** 毎回のスプリントプランニングにおいて，スプリントの期間をゴールの難易度に 応じて，1 週間から 1 か月までの範囲に設定する。



問42 プロジェクトライフサイクルの一般的な特性はどれか。



- **ア** 開発要員数は，プロジェクト開始時が最多であり，プロジェクトが進むにつれて 減少し，完了に近づくと再度増加する。
- **イ** ステークホルダがコストを変えずにプロジェクトの成果物に対して及ぼすことが できる影響の度合いは，プロジェクト完了直前が最も大きくなる。
- **ウ** プロジェクトが完了に近づくほど，変更やエラーの修正がプロジェクトに影響す る度合いは小さくなる。
- **エ** リスクは，プロジェクトが完了に近づくにつれて減少する。


問43 ソフトウェア開発の見積方法の一つであるファンクションポイント法の説明として， 適切なものはどれか。

- **ア** 開発規模が分かっていることを前提として，工数と工期を見積もる方法である。 ビジネス分野に限らず，全分野に適用可能である。
- **イ** 過去に経験した類似のソフトウェアについてのデータを基にして，ソフトウェア の相違点を調べ，同じ部分については過去のデータを使い，異なった部分は経験に 基づいて，規模と工数を見積もる方法である。
- **ウ** ソフトウェアの機能を入出力データ数やファイル数などによって定量的に計測し， 複雑さによる調整を行って，ソフトウェア規模を見積もる方法である。
- **エ** 単位作業項目に適用する作業量の基準値を決めておき，作業項目を単位作業項目 まで分解し，基準値を適用して算出した作業量の積算で全体の作業量を見積もる方 法である。



問44 アローダイアグラムの日程計画をもつプロジェクトの，開始から終了までの最少所 要日数は何日か。



![](<fe_kamoku_a_set_sample_qs_images/imageFile24.png>)

- **ア** 9
- **イ** 10
- **ウ** 11
- **エ** 12


問45 サービスマネジメントのプロセス改善におけるベンチマーキングはどれか。

- **ア** IT サービスのパフォーマンスを財務，顧客，内部プロセス，学習と成長の観点 から測定し，戦略的な活動をサポートする。
- **イ** 業界内外の優れた業務方法（ベストプラクティス）と比較して，サービス品質及 びパフォーマンスのレベルを評価する。
- **ウ** サービスのレベルで可用性，信頼性，パフォーマンスを測定し，顧客に報告する。
- **エ** 強み，弱み，機会，脅威の観点から IT サービスマネジメントの現状を分析する。



問46 ディスク障害時に，フルバックアップを取得してあるテープからディスクにデータ を復元した後，フルバックアップ取得時以降の更新後コピーをログから反映させてデ ータベースを回復する方法はどれか。

- **ア** チェックポイントリスタート
- **イ** リブート
- **ウ** ロールバック
- **エ** ロールフォワード



問47 経営者が社内のシステム監査人の外観上の独立性を担保するために講じる措置とし て，最も適切なものはどれか。



- **ア** システム監査人に IT に関する継続的学習を義務付ける。
- **イ** システム監査人に必要な知識や経験を定めて公表する。
- **ウ** システム監査人の監査技法研修制度を設ける。
- **エ** システム監査人の所属部署を内部監査部門とする。


問48 情報セキュリティ監査において，可用性を確認するチェック項目はどれか。

- **ア** 外部記憶媒体の無断持出しが禁止されていること
- **イ** 中断時間を定めた SLA の水準が保たれるように管理されていること
- **ウ** データ入力時のエラーチェックが適切に行われていること
- **エ** データベースが暗号化されていること



問49 テレワークで活用している VDI に関する記述として，適切なものはどれか。



- **ア** PC 環境を仮想化してサーバ上に置くことで，社外から端末の種類を選ばず自分 のデスクトップ PC 環境として利用できるシステム
- **イ** インターネット上に仮想の専用線を設定し，特定の人だけが利用できる専用ネッ トワーク
- **ウ** 紙で保管されている資料を，ネットワークを介して遠隔地からでも参照可能な電 子書類に変換・保存することができるツール
- **エ** 対面での会議開催が困難な場合に，ネットワークを介して対面と同じようなコミ ュニケーションができるツール


問50 投資案件において，5 年間の投資効果を ROI（Return On Investment）で評価した 場合，四つの案件 a～d のうち，最も ROI が高いものはどれか。ここで，割引率は考 慮しなくてもよいものとする。

a

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">年目</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">4</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">利益</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">15</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">30</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">45</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">30</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">15</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">投資額</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">100</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
  </tr>
</table>


b

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">年目</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">4</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">利益</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">105</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">75</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">45</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">15</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">0</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">投資額</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">200</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
  </tr>
</table>


c

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">年目</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">4</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">利益</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">60</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">75</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">90</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">75</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">60</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">投資額</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">300</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
  </tr>
</table>


d

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">年目</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">3</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">4</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">5</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">利益</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">105</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">105</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">105</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">105</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">105</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">投資額</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">400</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;"> </td>
  </tr>
</table>


- **ア** a
- **イ** b
- **ウ** c
- **エ** d



問51 国や地方公共団体が，環境への配慮を積極的に行っていると評価されている製品・ サービスを選んでいる。この取組を何というか。



- **ア** CSR
- **イ** エコマーク認定
- **ウ** 環境アセスメント
- **エ** グリーン購入


問52 コアコンピタンスの説明はどれか。

- **ア** 競合他社にはまねのできない自社ならではの卓越した能力
- **イ** 経営を行う上で法令や各種規制，社会的規範などを遵守する企業活動
- **ウ** 市場・技術・商品（サービス）の観点から設定した，事業の展開領域
- **エ** 組織活動の目的を達成するために行う，業務とシステムの全体最適化手法



問53 新しい事業に取り組む際の手法として，E.リースが提唱したリーンスタートアップ の説明はどれか。



- **ア** 国・地方公共団体など，公共機関の補助金・助成金の交付を前提とし，事前に詳 細な事業計画を検討・立案した上で，公共性のある事業を立ち上げる手法
- **イ** 市場環境の変化によって競争力を喪失した事業分野に対して，経営資源を大規模 に追加投入し，リニューアルすることによって，基幹事業として再出発を期す手法
- **ウ** 持続可能な事業を迅速に構築し，展開するために，あらかじめ詳細に立案された事業計画を厳格に遂行して，成果の検証や計画の変更を最小限にとどめる手法
- **エ** 実用最小限の製品・サービスを短期間で作り，構築・計測・学習というフィード バックループで改良や方向転換をして，継続的にイノベーションを行う手法


問54 IoT の応用事例のうち，HEMS の説明はどれか。

- **ア** 工場内の機械に取り付けたセンサで振動，温度，音などを常時計測し，収集した データを基に機械の劣化状態を分析して，適切なタイミングで部品を交換する。
- **イ** 自動車に取り付けたセンサで車両の状態，路面状況などのデータを計測し，ネットワークを介して保存し分析することによって，効率的な運転を支援する。
- **ウ** 情報通信技術や環境技術を駆使して，街灯などの公共設備や交通システムをはじ めとする都市基盤のエネルギーの可視化と消費の最適制御を行う。
- **エ** 太陽光発電装置などのエネルギー機器，家電機器，センサ類などを家庭内通信ネ ットワークに接続して，エネルギーの可視化と消費の最適制御を行う。



問55 ロングテールの説明はどれか。



- **ア** Web コンテンツを構成するテキストや画像などのデジタルコンテンツに，統合 的・体系的な管理，配信などの必要な処理を行うこと
- **イ** インターネットショッピングで，売上の全体に対して，あまり売れない商品群の 売上合計が無視できない割合になっていること
- **ウ** 自分の Web サイトやブログに企業へのリンクを掲載し，他者がこれらのリンクを 経由して商品を購入したときに，企業が紹介料を支払うこと
- **エ** メーカや卸売業者から商品を直接発送することによって，在庫リスクを負うこと なく自分の Web サイトで商品が販売できること


問56 CGM（Consumer Generated Media）の例はどれか。

- **ア** 企業が，経営状況や財務状況，業績動向に関する情報を，個人投資家向けに公開 する自社の Web サイト
- **イ** 企業が，自社の商品の特徴や使用方法に関する情報を，一般消費者向けに発信す る自社の Web サイト
- **ウ** 行政機関が，政策，行政サービスに関する情報を，一般市民向けに公開する自組 織の Web サイト
- **エ** 個人が，自らが使用した商品などの評価に関する情報を，不特定多数に向けて発 信するブログや SNS などの Web サイト



問57 製品 X 及び Y を生産するために 2 種類の原料 A，B が必要である。製品 1 個の生産 に必要となる原料の量と調達可能量は表に示すとおりである。製品 X と Y の 1 個当た りの販売利益が，それぞれ 100 円，150 円であるとき，最大利益は何円か。

<table style="border-collapse:collapse;width:auto;max-width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">原料</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">製品 X の 1 個<br>当たりの必要量</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">製品 Y の 1 個<br>当たりの必要量</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">調達可能量</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">A</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">100</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">B</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">1</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">2</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;white-space:normal;word-wrap:break-word;overflow-wrap:break-word;line-height:1.35;">80</td>
  </tr>
</table>


- **ア** 5,000
- **イ** 6,000
- **ウ** 7,000
- **エ** 8,000



問58 令和 2 年 4 月に 30 万円で購入した PC を 3 年後に 1 万円で売却するとき，固定資産 売却損は何万円か。ここで，耐用年数は 4 年，減価償却は定額法，定額法の償却率は 0.250，残存価額は 0 円とする。



- **ア** 6.0
- **イ** 6.5
- **ウ** 7.0
- **エ** 7.5


問59 売上高が 100 百万円のとき，変動費が 60 百万円，固定費が 30 百万円掛かる。変動 費率，固定費は変わらないものとして，目標利益 18 百万円を達成するのに必要な売 上高は何百万円か。

- **ア** 108
- **イ** 120
- **ウ** 156
- **エ** 180



問60 労働者派遣法に基づく，派遣先企業と労働者との関係（図の太線部分）はどれか。



![](<fe_kamoku_a_set_sample_qs_images/imageFile25.png>)

- **ア** 請負契約関係
- **イ** 雇用契約関係
- **ウ** 指揮命令関係
- **エ** 労働者派遣契約関係


試験問題に記載されている会社名又は製品名は，それぞれ各社の商標又は登録商標です。 なお，試験問題では，TM 及び ® を明記していません。

©2022 独立行政法人情報処理推進機構

