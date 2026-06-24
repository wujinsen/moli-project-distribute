令和７年度

# 基本情報技術者試験 科目 A 公開問題

<table style="border-collapse:collapse;width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">問題番号</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">問１～問20</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">選択方法</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">全問必須</td>
  </tr>
</table>


注意事項

- 1. 実際の試験は60問で構成されますが，そのうちの20問を公開しています。
- 2. 問題に関する質問にはお答えできません。文意どおり解釈してください。


問１ 大規模言語モデルを用いた自然言語処理において，事前学習済みのモデルに対して 行う，ファインチューニングに関する記述として，最も適切なものはどれか。

- **ア** 強化学習を行い，最適な結果が得られるようにする。
- **イ** 事前学習と同じデータを繰り返し用いて学習を行い，モデルの精度を高めるよう


にする。

- **ウ** 大量のテキストデータを用いて学習を行い，モデルの精度を高めるようにする。
- **エ** 特定のデータを用いて追加で学習を行い，目的とするタスクに適用できるように


する。

問２ 浮動小数点形式で表現された数値の演算結果における丸め誤差の説明はどれか。

- **ア** 演算結果がコンピュータの扱える最大値を超えることによって生じる誤差である。
- **イ** 数表現のけた数に限度があるので，最下位けたより小さい部分について四捨五入


- や切上げ，切捨てを行うことによって生じる誤差である。
- **ウ** 乗除算において，指数部が小さい方の数値の仮数部の下位部分が失われることに よって生じる誤差である。
- **エ** 絶対値がほぼ等しい数値の加減算において，上位の有効数字が失われることによ って生じる誤差である。


問３ 図の木構造は 2 分探索木である。a～g の値の大小関係として，適切なものはどれ か。ここで，a～g の値は重複しないものとする。

![](<2025r07_fe_kamoku_a_qs_images/imageFile1.png>)

- **ア** a ＜ b ＜ d ＜ e ＜ c ＜ f ＜ g
- **イ** d ＜ b ＜ e ＜ a ＜ f ＜ c ＜ g
- **ウ** d ＜ e ＜ f ＜ g ＜ b ＜ c ＜ a
- **エ** g ＜ f ＜ c ＜ e ＜ d ＜ b ＜ a


問４ MTBF は 4,000 時間，MTTR は 1,000 時間の装置がある。今後の 6 年間は，予防保守 によって MTBF を前年に比べて毎年 100 時間ずつ改善し，遠隔保守によって MTTR を前 年に比べて毎年 100 時間ずつ改善していく計画である。6 年経過後の稼働率は幾らか。

- **ア** 0.88
- **イ** 0.90
- **ウ** 0.92
- **エ** 0.94


問５ ローコード開発ツールを用いたソフトウェア開発の説明はどれか。

- **ア** アプリケーションソフトウェアの開発基盤の上で，用意された部品やテンプレー トを GUI を用いた操作で組み合わせたり，必要に応じて一部の処理のソースコード を記述したりすることによって，アプリケーションソフトウェアを作成する。
- **イ** アプリケーションソフトウェアの開発基盤の上で，用意された部品やテンプレー トを GUI を用いた操作で組み合わせるだけで，ソースコードを記述せずに，アプリ ケーションソフトウェアを作成する。


- **ウ** アプリケーションソフトウェアの定型的な枠組みを参照して，独自の処理のソー スコードを記述することによって，アプリケーションソフトウェアを作成する。
- **エ** 利用者がシステムを利用して行う作業を自動化ツールに代行させるために，利用


者によるシステムの操作手順をツールに登録する。

問６ “商品”表に対する SQL 文と同じ結果が得られる SELECT 文はどれか。

商品

<table style="border-collapse:collapse;width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">商品 ID</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">商品名称</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">仕入先 ID</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">単価</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">S001</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">冷蔵庫</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">M001</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">155,000</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">S002</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">食器洗い機</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">M002</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">85,000</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">S003</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">電子レンジ</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">M003</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">78,000</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">S004</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">炊飯器</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">M003</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">32,000</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">S005</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">コーヒーメーカー</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">M004</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">15,000</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">S006</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">ホットプレート</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">M004</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">12,000</td>
  </tr>
</table>


〔SQL 文〕 SELECT * FROM 商品 WHERE 仕入先 ID IN ('M002', 'M004')

- **ア** SELECT * FROM 商品 WHERE 仕入先 ID = 'M002' AND 仕入先 ID = 'M004'
- **イ** SELECT * FROM 商品 WHERE 仕入先 ID = 'M002'


INTERSECT SELECT * FROM 商品 WHERE 仕入先 ID = 'M004'

- **ウ** SELECT * FROM 商品 WHERE 仕入先 ID = 'M002' OR 仕入先 ID = 'M004'
- **エ** SELECT * FROM 商品 WHERE 仕入先 ID BETWEEN 'M002' AND 'M004'


問７ 1G バイトの動画データを 40M ビット／秒の回線を使用してダウンロードしたとこ ろ，5 分掛かった。このときの回線利用率はおよそ何％か。ここで，ダウンロード時 には動画データに 20％の制御情報が付加されるものとする。

- **ア** 10
- **イ** 53
- **ウ** 67
- **エ** 80


問８ HTTP と HTTPS を比較した場合において，HTTPS だけがもつ特徴を示したものはどれ か。

- **ア** cookie に保存されている情報を用いたセッション管理が可能である。
- **イ** ID とパスワードによって利用者の認証を行うことが可能である。
- **ウ** Web ブラウザでキャッシュさせることによって通信量を減らすことが可能である。
- **エ** 通信相手先サーバをサーバ証明書によって確認することが可能である。


たい

問９ 暗号の危殆

化に該当するものはどれか。

ア ある CA でデジタル証明書の署名に使っている公開鍵のデジタル証明書の有効期

- 限が切れた。
- **イ** ある暗号アルゴリズムの秘密鍵が不正アクセスによって漏えいした。
- **ウ** あるハッシュ関数においてハッシュ値が同じになるデータの組みを現実的な時間


内で発見する方法が見つかった。 エ あるランサムウェアの一種で暗号化されたファイルの復号鍵が公開された。

- 問10 WAF の説明はどれか。


- **ア** Web サイトに対するアクセス内容を監視し，攻撃とみなされるパターンを検知し たときに当該アクセスを遮断する。
- **イ** Wi-Fi アライアンスが認定した無線 LAN の暗号化方式の規格であり，AES 暗号に 対応している。
- **ウ** 様々なシステムの動作ログを一元的に蓄積，管理し，セキュリティ上の脅威とな る事象をいち早く検知，分析する。
- **エ** ファイアウォール機能を有し，マルウェア対策機能，侵入検知機能などの複数の セキュリティ機能を連携させ，統合的に管理する。


- 問11 E-R モデルにおけるエンティティの特徴はどれか。

- **ア** エンティティとインスタンスとは，1 対 1 の対応関係をとる。
- **イ** エンティティとなり得るものは，物的に実現するものである。
- **ウ** エンティティは，特性を表すための属性（アトリビュート）をもつ。
- **エ** 異なった種類のエンティティ間の関係は，主として状態遷移として表現される。


- 問12 オブジェクト指向プログラミングの特徴のうち，異なるクラスのオブジェクトを同 一のインタフェースで操作したときに，操作対象クラスに応じた異なる動作を可能に することを何と呼ぶか。

- **ア** 委譲
- **イ** 継承
- **ウ** コンポジション
- **エ** 多相性


- 問13 アジャイル開発手法の一つであるスクラムにおいて，プロダクトバックログアイテ ムの内容や並び順を決定する役割をもつのは誰か。


- **ア** 開発者
- **イ** 顧客
- **ウ** スクラムマスタ
- **エ** プロダクトオーナ


- 問14 図は，あるプロジェクトの作業 A ～ I とその作業日数を表している。このプロジェ クトの最短所要日数は何日か。

![](<2025r07_fe_kamoku_a_qs_images/imageFile2.png>)

- **ア** 27
- **イ** 28
- **ウ** 29
- **エ** 31


- 問15 サーバ室の物理的な安全対策の状況について，情報セキュリティ管理基準（平成 28 年）に照らして，情報セキュリティ監査を行って判明した状況のうち，監査人が， 指摘事項として監査報告書に記載すべきものはどれか。


ア サーバが設置されている施設の無人領域では，営業時間中でも，警報装置が作動

- するようになっている。
- **イ** サーバ室に非常口，避難器具，誘導灯などを設置している。
- **ウ** 社外からサーバ室へ直接出入りするドアを設置しているが，出入りを考慮して常


時施錠していない。 エ 場所が分からないように，サーバ室の所在を室外に表示していない。

- 問16 データマイニングの手法の一つであって，POS などの蓄積データから“一緒に買わ れる商品”の組合せを発見する分析手法はどれか。

- **ア** 3C 分析
- **イ** ABC 分析
- **ウ** コンジョイント分析
- **エ** マーケットバスケット分析


- 問17 インターネット上の生成 AI サービスを利用する際に，オプトアウトを設定するこ とはどのような場合に有効か。

ア 個々の利用者が，自身が生成 AI から得た情報に対して，著作権を主張したい場

- 合
- **イ** 個々の利用者が入力した情報を，生成 AI の学習に利用させたくない場合
- **ウ** 個々の利用者が入力した情報を，生成 AI を通じて，他の利用者にも知ってほし


い場合 エ 生成 AI から得た情報の信ぴょう性を高めたい場合

- 問18 物販事業において，ロングテールをビジネスとして成功させるために必要な施策は どれか。


- **ア** 多くの有名ブランド店が出店するショッピングモールの構築
- **イ** 交通の利便性が高い地域に対する，生活必需品を広く浅く取りそろえた出店計画
- **ウ** 店舗で購入した商品を近隣地域に無償で配送するサービスの実施
- **エ** 豊富な品ぞろえと，在庫コストや配送費用を抑えるための大規模な物流センタの


構築や活用

- 問19 表の条件で喫茶店を開業したい。月 10 万円の利益を出すためには，1 客席当たり 1 日平均何人の客が必要か。

<table style="border-collapse:collapse;width:100%;">
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">客 1 人当たりの売上高</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">500 円</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">客 1 人当たりの変動費</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">100 円</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">固定費</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">300,000 円／月</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">1 か月の営業日数</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">20 日</td>
  </tr>
  <tr>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">客席数</td>
    <td style="border:1px solid #ccc;padding:6px 10px;vertical-align:top;">10 席</td>
  </tr>
</table>


- **ア** 3.75
- **イ** 4
- **ウ** 4.2
- **エ** 5


- 問20 カーボンフットプリントの説明として，適切なものはどれか。


- **ア** 温室効果ガスの排出量から吸収量と除去量を差し引いた合計をゼロにする取組
- **イ** 原材料調達から廃棄・リサイクルに至るまでのライフサイクル全体を通して排出


される温室効果ガスの排出量を，CO2 量に換算して，その値を商品やサービスに表 示すること

- **ウ** 自動車のエンジンから排出される一酸化炭素，窒素酸化物や炭化水素類などの大 気汚染物質の排出量の定め
- **エ** 商品がどのような場所で作られて，流通し，販売されているかを把握するための 仕組み


## 〔 メ モ 用 紙 〕

試験問題に記載されている会社名又は製品名は，それぞれ各社の商標又は登録商標です。 なお，試験問題では，TM 及び ® を明記していません。

©2025 独立行政法人情報処理推進機構

