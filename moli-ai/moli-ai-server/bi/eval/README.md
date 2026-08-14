# ChatBI NL2SQL 评测（AI-4 W8）

## 文件

| 文件 | 说明 |
|------|------|
| `nl2sql_testset.jsonl` | **50 题**（15 执行 + 8 拒答 NL + 27 validator 危险 SQL） |
| `baselines.json` | 门禁：`exec_accuracy ≥ 0.8` · `reject_accuracy = 1.0` |
| `eval_nl2sql.py` | 评测脚本 |

## 离线门禁（CI 默认可跑）

```powershell
cd moli-ai/moli-ai-server/bi/eval
python eval_nl2sql.py --validator-only --gate
```

等价于 `mvn test -Dtest=Nl2sqlTestsetValidatorTest` + baselines 校验。

## 全量 E2E（需栈）

```powershell
# ai-server(1128) + ai-agent(1130) + user-center(8888) + 权限 SQL 33
# 网关可选；无 gateway 时直连如下
$env:MOLI_AI_BASE = "http://127.0.0.1:1128"
$env:MOLI_LOGIN_BASE = "http://127.0.0.1:8888"
$env:MOLI_EVAL_USER = "admin"
$env:MOLI_EVAL_PASS = "123456"
python eval_nl2sql.py --gate
```

## testset 字段

```json
{"id":"a01","mode":"ask","question":"...","expect":"success","assert":{"sql_contains":["count"],"min_rows":1}}
{"id":"r01","mode":"ask","question":"...","expect":"rejected","assert":{"reject_codes":["REJECT_SEMANTIC"]}}
{"id":"v01","mode":"validator","sql":"SELECT * FROM ...","expect_reject":"REJECT_STAR_SELECT"}
```

契约：[`docs/design/bi-chatbi-nl2sql-contract.md`](../../../docs/design/bi-chatbi-nl2sql-contract.md) §4.5
