# Markdown 插图鉴权 · 前端对接说明（T22 F1）

> **读者**：meiling-ui 前端。后端 **R0 ✅**（`GET /kb/raw/asset`、`GET /kb/wiki/asset`）；**F1 ✅**；**F2 ✅**（`POST /kb/wiki/asset`）。  
> **总览**：[knowledge-workbench-frontend.md](knowledge-workbench-frontend.md) §1.2–§1.3  
> **HTTP 契约**：[KNOWLEDGE_API.md](KNOWLEDGE_API.md) §8.0  
> **回迁背景**：[wujinsen-wiki-image-remediation-prd.md](../product/wujinsen-wiki-image-remediation-prd.md)

---

## 1. 现象与根因

| 现象 | 根因 |
|------|------|
| 浏览页 `## 原文插图（wujinsen）` 下空白 | markdown 含 `/KnowledgeServer/kb/raw/asset?...`，`<img src>` **不带** `Authorization` → 401 |
| annex 页 `![](assets/imageFile1.png)` 不显示 | 相对路径未解析为 `/kb/wiki/asset`，或未鉴权拉图 |
| 「附件」按钮在浏览页仍可上传 | 与 §1.2 定案不符，应迁到 Wiki 编辑页 |

**结论**：Sync 与回迁脚本正常；**必须在 markdown 渲染层**对 kb asset URL 做带 token 的请求。

---

## 2. 涉及页面

| 页面 | 路由 | 必须接 `KbMarkdownImage` |
|------|------|---------------------------|
| 文档浏览 | `knowledge/browse/index` | ✅ 正文渲染 |
| 文档管理 / 预览 | `knowledge/documents/index`、`KbDocPreviewModal` | ✅ |
| Wiki 编辑 | `knowledge/wiki/edit` | ✅ 预览区 + **F2「插入图片」**（写 `.assets/` + 插 markdown） |
| 智能问答引用块 | `knowledge/ask/index` | ✅ 若展示 `content` markdown |

---

## 3. 后端 URL 形态（markdown 里会出现）

### 3.1 Raw 直链（R2a · D 档）

```markdown
![image 1](/KnowledgeServer/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Hadoop/用户画像.note_images/imageFile1.png)
```

| 参数 | 说明 |
|------|------|
| `path` | 相对 `kb/raw/`，已 URL encode |
| `spaceId` | 可选；省略时后端默认 `enterprise-kb` |

**请求**：`GET {VITE_API_BASE_URL}/KnowledgeServer/kb/raw/asset?spaceId=&path=`  
**响应**：二进制流（非 `MoliResult`），`Content-Type: image/png` 等。

### 3.2 Wiki annex 相对路径（R2b · A 档）

```markdown
![image 1](assets/imageFile1.png)
```

**解析规则**（渲染时由**当前页 context** 补全）：

```text
GET {base}/KnowledgeServer/kb/wiki/asset
  ?spaceId={currentSpaceId}
  &slug={currentDocumentSlug}      # 如 bigdata/annex-mac搭建hadoop集群
  &rel=assets/imageFile1.png
```

annex 页：`slug` = 当前文档 slug（含分类前缀）。  
hub 页正文若将来内嵌 `assets/`（少见），`slug` = hub 自身 slug。

### 3.3 已是绝对 kb 路径

以下两种等价，**原样保留 path/query**，仅改走鉴权 fetch：

```text
/KnowledgeServer/kb/wiki/asset?slug=...&rel=...
/kb/raw/asset?path=...
/kb/wiki-moli/asset?...          # 文档别名，同 wiki/asset
```

### 3.4 不要处理的 src

| src | 行为 |
|-----|------|
| `http(s)://` 外链 | 普通 `<img>` |
| `data:` | 普通 `<img>` |
| `/kb/attachment/{id}` | 走附件下载 API（见 §6） |

---

## 4. 组件契约：`KbMarkdownImage`

### 4.1 Props

```typescript
/** 嵌入 markdown 渲染器；替换默认 <img> 或 md 插件 image 渲染 */
interface KbMarkdownImageProps {
  /** markdown 解析出的 src（可能是相对 assets/ 或绝对 /KnowledgeServer/...） */
  src: string
  alt?: string
  title?: string
  /** 当前浏览/编辑文档上下文 */
  spaceId: string
  /** kb_document.slug 或 wiki 全路径 slug，如 bigdata/hadoop-生态入门 */
  documentSlug: string
  /** 默认 import.meta.env.VITE_API_BASE_URL + '/KnowledgeServer' */
  apiBase?: string
}
```

### 4.2 URL 解析 `resolveKbAssetUrl(src, ctx)`

```typescript
const KB_RAW_ASSET = /^(\/KnowledgeServer)?\/kb\/raw\/asset\?/i
const KB_WIKI_ASSET = /^(\/KnowledgeServer)?\/kb\/(?:wiki-moli\/)?asset\?/i

function resolveKbAssetUrl(src: string, ctx: KbMarkdownImageProps): string | null {
  const base = ctx.apiBase ?? `${import.meta.env.VITE_API_BASE_URL}/KnowledgeServer`

  // 1) 已是 raw/wiki asset 绝对 URL → 规范化 base 后返回
  if (KB_RAW_ASSET.test(src) || KB_WIKI_ASSET.test(src)) {
    const pathAndQuery = src.replace(/^\/KnowledgeServer/i, '')
    return `${base}${pathAndQuery.startsWith('/') ? '' : '/'}${pathAndQuery.replace(/^\//, '')}`
  }

  // 2) 相对 assets/ → wiki asset
  if (src.startsWith('assets/') || src.startsWith('./assets/')) {
    const rel = src.replace(/^\.\//, '')
    const q = new URLSearchParams({
      spaceId: ctx.spaceId,
      slug: ctx.documentSlug,
      rel,
    })
    return `${base}/kb/wiki/asset?${q.toString()}`
  }

  return null // 非 kb asset，交还默认 img
}
```

### 4.3 鉴权拉图（推荐 blob）

**禁止**：`<img src="/KnowledgeServer/kb/raw/asset?...">`（浏览器不带 token）。

**推荐**：与现有 `http.ts` 共用 axios/fetch，带 `Authorization` 头：

```typescript
import { getToken } from '@/utils/auth' // 与现有 API 一致

async function fetchKbAssetBlob(resolvedUrl: string): Promise<string> {
  const res = await fetch(resolvedUrl, {
    headers: { Authorization: getToken() },
  })
  if (!res.ok) throw new Error(`kb asset ${res.status}`)
  const blob = await res.blob()
  return URL.createObjectURL(blob)
}
```

**Vue 3 组件骨架**：

```vue
<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps<KbMarkdownImageProps>()
const blobUrl = ref<string>('')
let objectUrl: string | null = null

watch(
  () => [props.src, props.spaceId, props.documentSlug] as const,
  async () => {
    if (objectUrl) URL.revokeObjectURL(objectUrl)
    const resolved = resolveKbAssetUrl(props.src, props)
    if (!resolved) return
    try {
      objectUrl = await fetchKbAssetBlob(resolved)
      blobUrl.value = objectUrl
    } catch {
      blobUrl.value = '' // 可显示 broken placeholder
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (objectUrl) URL.revokeObjectURL(objectUrl)
})
</script>

<template>
  <img v-if="blobUrl" :src="blobUrl" :alt="alt" :title="title" class="kb-md-image" />
  <span v-else class="kb-md-image--error">[图片加载失败]</span>
</template>
```

### 4.4 接入 markdown 渲染器

以 **markdown-it / vditor 预览 / 现有 MdPreview** 为例：

```typescript
// markdown-it
md.renderer.rules.image = (tokens, idx) => {
  const token = tokens[idx]
  const src = token.attrGet('src') ?? ''
  const alt = token.content
  // 若 resolveKbAssetUrl(src, ctx) 非 null → 输出占位，由 Vue 挂载 KbMarkdownImage
  // 或在 Vue 层对 .markdown-body img[src*="kb/raw/asset"] 做 post-process 替换
}
```

**最低成本落地**（浏览页）：

1. 渲染完 markdown HTML 后，`querySelectorAll('img')`  
2. `src` 匹配 `/kb/raw/asset`、`/kb/wiki/asset` 或 `assets/` → 用 `KbMarkdownImage` 替换节点  
3. 传入当前页的 `spaceId` + `documentSlug`（来自 `GET /kb/page` 或 search 结果）

---

## 5. Context 从哪来

| 数据源 | `spaceId` | `documentSlug` |
|--------|-----------|----------------|
| `GET /kb/page?id=` / search 列表项 | `record.spaceId` | `record.slug`（含 `bigdata/xxx`） |
| Wiki 编辑预览 | form `spaceId` | form `slug` |
| annex 页 | 同上 | annex 自身 slug，如 `bigdata/annex-mac搭建hadoop集群` |

**注意**：`GET /kb/page` 返回的 `content` 是 DB 快照；T22 回迁后需已 Sync。slug 字段必须与 wiki 文件路径一致。

---

## 6. 与 MinIO 附件分工（§1.2 同步改）

| 能力 | UI 位置 | API |
|------|---------|-----|
| **正文插图** | markdown 内 `KbMarkdownImage` | `/kb/raw/asset`、`/kb/wiki/asset` |
| **页附件 pdf/zip** | **仅 Wiki 编辑页** | `POST/GET/DELETE /kb/attachment/*` |

**浏览页改法**：

```typescript
// 删除或隐藏 browse 页的上传组件
// 保留：
const attachments = await listAttachments(documentId) // GET /kb/attachment/list
// 下载：window.open 或 fetch blob + Authorization
`${apiBase}/kb/attachment/${id}`

// editor 可见：
router.push({
  path: '/knowledge/wiki/edit',
  query: { slug, spaceId, documentId },
})
// 文案：「在编辑页管理附件 →」（勿写「在前端页面管理附件」）
```

---

## 6.1 F2 · Wiki 编辑插入插图 ✅

| 项 | 说明 |
|----|------|
| 组件 | `KbWikiImageInsert.vue`（编辑 tab 工具栏） |
| API | `POST /KnowledgeServer/kb/wiki/asset` · FormData：`spaceId`、`slug`、`file` |
| 前置 | wiki `.md` **须已保存**（`exists=true`）；否则按钮禁用并提示「请先保存」 |
| 落盘 | `{slug}.assets/img-{ts}-{hex}.png` |
| 插入 | 光标处写入 `![alt](assets/….png)`（相对路径，预览走 F1） |
| 大小 | 默认 5MB · `kb.wiki.asset-max-bytes` / `VITE_KB_WIKI_ASSET_MAX_MB` |

```typescript
import { uploadKbWikiAssetApi } from '@/api/knowledge'

const res = await uploadKbWikiAssetApi(spaceId, slug, file)
if (res.code === 10000) insertAtCursor(res.data.markdown + '\n')
```

---

## 7. 验收清单

### 7.1 插图（F1）

- [ ] 打开 `bigdata/hadoop-生态入门`：D 档 `## 原文插图` 下 **至少 1 张图可见**
- [ ] 打开 `bigdata/annex-mac搭建hadoop集群`：annex 内 **多张** `assets/imageFileN.png` 可见
- [ ] DevTools Network：asset 请求 **带** `Authorization`，状态 **200**，非裸 `<img>` 401
- [ ] viewer 账号（无 edit）也能看图（空间读权限）
- [ ] 外链 `https://` 图片仍正常
- [ ] 组件 unmount 后 `revokeObjectURL`，无内存泄漏

### 7.1b 编辑插入（F2）

- [ ] Wiki 编辑 · 编辑 tab ·「插入图片」→ 选 png → 光标处出现 `![](assets/img-….png)`
- [ ] 预览 tab 可见刚插入的图
- [ ] 未保存新页时按钮禁用；保存后可上传
- [ ] 超 5MB 或非图片格式有 Toast 提示

### 7.2 附件（§1.2）

- [ ] 浏览页 **无** upload/delete
- [ ] 编辑页 upload → list → download → delete 闭环
- [ ] 浏览页 editor 有「在编辑页管理附件 →」

### 7.3 后端自测（前端联调前）

```bash
curl -H "Authorization: <token>" -o /tmp/t.png \
  "http://127.0.0.1:8090/kb/raw/asset?spaceId=900000000000000001&path=wujinsen_markdown/BigData/Hadoop/用户画像.note_images/imageFile1.png"
```

---

## 8. 常见问题

| 问题 | 处理 |
|------|------|
| 图仍 401 | 检查 `Authorization` 是否与 `http.ts` 一致（SessionId token） |
| annex 404 | `slug` 是否含分类前缀；`rel` 是否 `assets/xxx.png` |
| path 含中文 | markdown 里已 encode；**不要**二次 encode |
| 图片太多卡顿 | 同页 blob 缓存 Map<resolvedUrl, blobUrl>；滚动 lazy load（P2） |
| hub 页堆多个插图节 | 内容问题，非前端；后续 R2c 合并（见回迁 PRD） |

---

## 9. 文件建议（meiling-ui）

| 路径 | 说明 |
|------|------|
| `src/components/knowledge/KbMarkdownImage.vue` | 鉴权拉图组件 |
| `src/utils/kbAssetUrl.ts` | `resolveKbAssetUrl` + `fetchKbAssetBlob` |
| `src/composables/useKbMarkdownRender.ts` | 统一注入 image 渲染 |
| 改 `KnowledgeBrowseView.vue` / `KbDocPreviewModal.vue` | 传入 spaceId + slug |

---

## 10. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-05 | 初稿：T22 F1、`KbMarkdownImage` 契约、URL 解析、附件入口交叉引用 |
