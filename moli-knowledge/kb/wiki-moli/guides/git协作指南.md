---


title: Git 协作指南
slug: git协作指南
type: guide
status: active
tags: [Git, 协作, DevOps]
sources:
  - raw/wujinsen_markdown/架构/Git/You have not concluded your merge (MERGE_HEAD exists) git拉取失败.note.md
  - raw/wujinsen_markdown/架构/Git/git拒绝push.note.md
related: [项目文档总览]
created: 2026-06-22
updated: 2026-06-22
---

# Git 协作指南

茉莉多模块 Maven  monorepo，日常协作以 **feature 分支 + MR/PR** 为主。

## 1. 日常流程

```bash
git fetch origin
git checkout -b feature/xxx origin/master   # 或 main
# 开发 ...
git add <files>
git commit -m "feat: 简述原因"
git push -u origin feature/xxx
# 在 GitLab/GitHub 提 MR
```

## 2. 常用命令

| 场景 | 命令 |
|------|------|
| 看状态 | `git status` · `git log --oneline -10` |
| 暂存未完成工作 | `git stash` / `git stash pop` |
| 撤销工作区 | `git checkout -- <file>` |
| 改最后一次提交说明 | 未 push 时 `git commit --amend`（团队规范允许时） |
| 大文件 | 勿提交 >100MB；用 Git LFS 或外存 |

## 3. 常见问题

| 错误 | 处理 |
|------|------|
| `MERGE_HEAD exists` | 完成或中止合并：`git merge --continue` / `git merge --abort` |
| push 被拒绝 | 先 `git pull --rebase` 再 push；禁止 force push 主分支 |
| 冲突 | 编辑冲突文件 → `git add` → `commit` |
| IDEA 与命令行混用 | 统一 credential helper，HTTPS 记住凭据或改 SSH |

## 4. 与 CI 配合

推送触发 `moli-knowledge/kb/wiki/ops/jenkins-ci入门.md` Pipeline：编译 `mvn clean package`、可选 Sonar、打 jar 部署。

## 5. 知识库 wiki 维护

Ingest 只改 `wiki/**`，`raw/` 只读；变更通过 Git 审查，见 [[项目文档总览]]。

## 相关

`moli-knowledge/kb/wiki/ops/jenkins-ci入门.md` · `moli-knowledge/kb/wiki/ops/k8s入门与容器编排.md`
