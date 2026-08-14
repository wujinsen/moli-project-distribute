---
title: 怎么遍历二叉树.note（原文插图 annex）
slug: annex-怎么遍历二叉树
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/数据结构与算法/怎么遍历二叉树.note.md
related: [算法面试题精选]
created: 2026-07-05
updated: 2026-07-05
---

- 1.
- 2.


⾸先我们先拿⼀个例⼦来讲吧，这样⽐较形象直观。例⼦如下图所示

![image 1](assets/imageFile1.png)

怎么遍历⼆叉树

⼆叉树的遍历⼤概分为四种，分别是前序遍历，中序遍历，后序遍历，按层遍历，我们先讲⼀下 怎么前序遍历，就是先访问根节点 -左⼦树 -右⼦树，如下图所示

![image 2](assets/imageFile2.png)

怎么遍历⼆叉树

- 3. 中序遍历就是先访问左⼦树 -根节点 -右⼦树，这个顺序。遍历的结果如下图所示


![image 3](assets/imageFile3.png)

怎么遍历⼆叉树

- 4.


然后就是后序遍历，和前⾯都差不多就是先访问树的左⼦树 -右⼦树 -根节点按照这个顺序来把 序列写出来。结果如下图所示

![image 4](assets/imageFile4.png)

怎么遍历⼆叉树

- 5.


最后⼀种遍历就是按层遍历了，这⼀种遍历其实是最简单的，就是把⼀棵树从上到下，从左到右 依次写出来，结果如下图所示

![image 5](assets/imageFile5.png)

# 怎么遍历⼆叉树
