---
title: Jenkins CI 入门
slug: jenkins-ci入门
type: concept
status: active
tags: [Jenkins, CI, DevOps]
sources:
 - raw/wujinsen_markdown/架构/DevOps/jenkins/Jenkins安装.note.md
 - raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins自动部署jar包（maven.note.md
 - raw/wujinsen_markdown/架构/DevOps/jenkins/jenkins pipeline 脚本.note.md
related: [git协作指南, docker部署指南, linux-运维基础, k8s入门与容器编排]
created: 2026-06-22
updated: 2026-06-22
---

# Jenkins CI 入门

**CI**：代码提交后自动构建、测试、打包，减少「在我机器能跑」问题。可选用 Jenkins 对父 POM 与各 `*-server` 模块做流水线。

## 1. 核心概念

| 概念 | 说明 |
|------|------|
| Job / Pipeline | 一次构建流水线（Declarative/Jenkinsfile） |
| Agent | 执行节点（Linux + JDK8 + Maven） |
| Stage | 阶段：Checkout → Build → Test → Deploy |
| Artifact | 产出 jar/war |

## 2. 典型 Pipeline（Maven 多模块）

```groovy
pipeline {
 agent any
 stages {
 stage('Checkout') { steps { checkout scm } }
 stage('Build') {
 steps {
 sh 'cd moli-distribute-parent && mvn clean install -DskipTests'
 sh 'cd moli-user-center && mvn clean package -DskipTests'
 }
 }
 stage('Archive') {
 steps { archiveArtifacts '**/target/*.jar' }
 }
 }
}
```

按需并行编译 order/bi/knowledge/gateway。

## 3. 部署方式

| 方式 | 说明 |
|------|------|
| SSH 传 jar + restart | 传统单机，见 [[ops/linux-运维基础]] |
| Docker 镜像 | build 镜像 push 仓库，见 |
| K8s | 更新 Deployment 镜像，见 [[ops/k8s入门与容器编排]] |

## 4. 安装要点

- Linux：`java -jar jenkins.war` 或 Docker 官方镜像。
- 插件：Git、Pipeline、Maven Integration、Credentials。
- 与 SonarQube 集成可选（代码质量门禁）。

## 相关

 · [[nginx反向代理与前端部署指南]]
