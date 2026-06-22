#!/usr/bin/env python3
"""茉莉知识库 · 轻量 Viewer（零依赖，标准库实现）

五个页签：
  - Query 问答：定作用域 → 选页 → 选模型出生成式带引用答案，否则出检索式高亮片段（/api/ask）。
  - 浏览：渲染 wiki 页 + 反向链接（/api/tree, /api/page）。
  - 关系图谱：力导向图（/api/graph）。
  - 体检：断链 / 孤儿页 / 缺来源（/api/lint）。
  - 提炼（ingest 类，用 LLM）：按 tag 聚类主题 → 生成「同主题枢纽页」或「文章↔面试题跨类型对照」；
    无 key 时回退结构脚手架；枢纽页可「保存为草稿」写入 wiki（不覆盖已存在文件）。
    端点：/api/topics、/api/hub、/api/compare、POST /api/save_draft。

LLM key：复制 llm_config.example.json 为 llm_config.json 填 key，或设对应环境变量。

运行：
    python kb/tools/serve.py            # 默认 http://127.0.0.1:8765
    python kb/tools/serve.py --port 9000
"""
from __future__ import annotations

import argparse
import copy
import json
import os
import re
import sys
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse, parse_qs

KB_ROOT = Path(__file__).resolve().parent.parent          # .../kb
WIKI_DIR = KB_ROOT / "wiki"
TOOLS_DIR = Path(__file__).resolve().parent

TYPE_ORDER = ["guide", "service", "concept", "article", "interview", "output", "meta"]
TYPE_LABEL = {
    "guide": "操作指导",
    "service": "微服务",
    "concept": "概念",
    "article": "技术文章",
    "interview": "面试题",
    "output": "问答回写",
    "meta": "目录/日志",
}

# ---------------------------------------------------------------------------
# LLM provider（全部 OpenAI 兼容：POST {base_url}/chat/completions）
#   key 来源优先级：llm_config.json 里的 api_key > 环境变量 key_env
#   复制 llm_config.example.json 为 llm_config.json 并填 key 即可启用。
# ---------------------------------------------------------------------------

DEFAULT_PROVIDERS = {
    "deepseek-v3": {
        "label": "DeepSeek-V3", "base_url": "https://api.deepseek.com",
        "model": "deepseek-chat", "key_env": "DEEPSEEK_API_KEY",
    },
    "qwen-plus": {
        "label": "Qwen-Plus", "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1",
        "model": "qwen-plus", "key_env": "DASHSCOPE_API_KEY",
    },
    "qwen2.5-72b": {
        "label": "Qwen2.5-72B", "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1",
        "model": "qwen2.5-72b-instruct", "key_env": "DASHSCOPE_API_KEY",
    },
    "glm-4-flash": {
        "label": "GLM-4-Flash", "base_url": "https://open.bigmodel.cn/api/paas/v4",
        "model": "glm-4-flash", "key_env": "ZHIPU_API_KEY",
    },
    "glm-4-air": {
        "label": "GLM-4-Air", "base_url": "https://open.bigmodel.cn/api/paas/v4",
        "model": "glm-4-air", "key_env": "ZHIPU_API_KEY",
    },
}

SYSTEM_PROMPT = (
    "你是茉莉企业知识库的问答助手。只能依据用户提供的【知识库页】内容作答，严禁编造。\n"
    "要求：\n"
    "1) 用中文、条理清晰，先给结论再给要点；\n"
    "2) 每个关键结论后用 [[页slug]] 标注来源（slug 见每页标题中的 [[..]]）；\n"
    "3) 若所给页无法回答，明说「知识库暂无相关内容」并建议应补充哪些资料；\n"
    "4) 提炼要点，不要整页复述。"
)

HUB_SYSTEM = (
    "你是企业知识库的资深编辑，负责把同一主题下零散的页整理成一个『枢纽页 (concept hub)』草稿。\n"
    "严格依据所给页内容，不要编造。输出 Markdown，结构如下：\n"
    "1) 一句话定义该主题；\n"
    "2) 「要点地图」：列出该主题的核心子点（无序列表）；\n"
    "3) 「页角色」：逐一说明每个页讲什么、是什么类型，并用 [[slug]] 链接（slug 见每页标题中的 [[..]]）；\n"
    "4) 「覆盖盲区」：按常识该主题应有、但当前页缺失的点；\n"
    "5) 「疑似矛盾/过时」：页之间若有冲突或过时信息则指出，没有写『无』。\n"
    "精炼，不要整页复述。不要输出 frontmatter（保存时会自动补）。"
)

COMPARE_SYSTEM = (
    "你是企业知识库编辑，做同一主题下『文章 ↔ 面试题』的跨类型对照分析。严格依据所给页，不编造。\n"
    "输出 Markdown：先一句话总览；然后一个表格，列为：要点 | 文章是否覆盖 | 面试题是否考查 | 备注；\n"
    "表后再给三节：\n"
    "- 「可据文章出题」：文章讲了但面试题没考的点；\n"
    "- 「待补文章」：面试题考了但文章没展开的点；\n"
    "- 「疑似矛盾/过时」：没有写『无』。\n"
    "所有对页的引用用 [[slug]]（slug 见每页标题中的 [[..]]）。"
)


def load_providers() -> dict:
    provs = copy.deepcopy(DEFAULT_PROVIDERS)
    cfg = TOOLS_DIR / "llm_config.json"
    if cfg.exists():
        try:
            data = json.loads(cfg.read_text(encoding="utf-8"))
            for pid, p in (data.get("providers") or {}).items():
                provs.setdefault(pid, {})
                provs[pid].update(p)
        except Exception as e:
            print("⚠️ 解析 llm_config.json 失败：", e)
    return provs


PROVIDERS = load_providers()


def resolve_key(p: dict):
    if p.get("api_key"):
        return p["api_key"]
    env = p.get("key_env")
    if env and os.environ.get(env):
        return os.environ[env]
    return None


def list_providers():
    out = []
    for pid, p in PROVIDERS.items():
        out.append({
            "id": pid, "label": p.get("label", pid), "model": p.get("model", ""),
            "available": bool(resolve_key(p)),
        })
    return out


def call_llm(p: dict, messages, timeout: int = 90) -> str:
    key = resolve_key(p)
    url = p["base_url"].rstrip("/") + "/chat/completions"
    payload = {"model": p["model"], "messages": messages,
               "temperature": p.get("temperature", 0.3), "stream": False}
    req = urllib.request.Request(url, data=json.dumps(payload).encode("utf-8"), method="POST")
    req.add_header("Content-Type", "application/json")
    req.add_header("Authorization", "Bearer " + key)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        data = json.loads(resp.read().decode("utf-8"))
    return data["choices"][0]["message"]["content"]


def build_context(pages: dict, citations, budget: int = 12000) -> str:
    parts, used = [], 0
    for c in citations:
        p = pages.get(c["slug"])
        if not p:
            continue
        chunk = f"## 页：[[{c['slug']}]]（{p['title']}）\n{p['body'].strip()}\n"
        if used + len(chunk) > budget:
            chunk = chunk[: max(0, budget - used)]
        parts.append(chunk)
        used += len(chunk)
        if used >= budget:
            break
    return "\n".join(parts)

# ---------------------------------------------------------------------------
# frontmatter 解析（最小 YAML 子集：标量 / 内联列表 [a, b] / 块列表 - item）
# ---------------------------------------------------------------------------

def parse_frontmatter(text: str):
    if not text.startswith("---"):
        return {}, text
    end = text.find("\n---", 3)
    if end == -1:
        return {}, text
    raw = text[3:end].strip("\n")
    body = text[end + 4:].lstrip("\n")
    meta: dict = {}
    key = None
    for line in raw.splitlines():
        if not line.strip():
            continue
        if line.startswith(("  - ", "- ")) and key:          # 块列表项
            meta.setdefault(key, [])
            if isinstance(meta[key], list):
                meta[key].append(_strip(line.split("-", 1)[1]))
            continue
        m = re.match(r"^([A-Za-z_][\w-]*):\s*(.*)$", line)
        if not m:
            continue
        key, val = m.group(1), m.group(2).strip()
        if val == "":
            meta[key] = []                                    # 等待后续块列表
        elif val.startswith("[") and val.endswith("]"):
            inner = val[1:-1].strip()
            meta[key] = [_strip(x) for x in inner.split(",")] if inner else []
        else:
            meta[key] = _strip(val)
    return meta, body


def _strip(s: str) -> str:
    return s.strip().strip('"').strip("'").strip()


# ---------------------------------------------------------------------------
# 加载 wiki 页
# ---------------------------------------------------------------------------

def load_pages() -> dict:
    pages: dict = {}
    if not WIKI_DIR.exists():
        return pages
    for path in WIKI_DIR.rglob("*.md"):
        try:
            text = path.read_text(encoding="utf-8")
        except Exception:
            continue
        meta, body = parse_frontmatter(text)
        slug = meta.get("slug") or path.stem
        name = path.stem
        is_special = name in ("index", "log")
        ptype = meta.get("type") or ("meta" if is_special else "concept")
        title = meta.get("title") or _first_h1(body) or name
        tags = meta.get("tags") if isinstance(meta.get("tags"), list) else []
        rel = path.relative_to(WIKI_DIR).as_posix()
        # slug 唯一键：index/log 用文件名，其它用 slug
        key = name if is_special else slug
        pages[key] = {
            "slug": key,
            "title": title,
            "type": ptype,
            "tags": tags,
            "status": meta.get("status", ""),
            "sources": meta.get("sources", []),
            "related": meta.get("related", []),
            "path": rel,
            "body": body,
            "is_special": is_special,
        }
    return pages


def _first_h1(body: str):
    for line in body.splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return None


# ---------------------------------------------------------------------------
# 中文友好的关键词切分：拉丁词 + CJK 二元组（bigram）
# ---------------------------------------------------------------------------

def _is_cjk(ch: str) -> bool:
    return "\u4e00" <= ch <= "\u9fff"


def build_terms(query: str):
    q = query.strip().lower()
    if not q:
        return []
    terms = set()
    if len(q) >= 2:
        terms.add(q)                                          # 完整查询串
    for w in re.findall(r"[a-z0-9]+", q):                     # 拉丁词
        if len(w) >= 2:
            terms.add(w)
    cjk = re.findall(r"[\u4e00-\u9fff]+", q)                  # CJK 二元组
    for seg in cjk:
        if len(seg) == 1:
            terms.add(seg)
        for i in range(len(seg) - 1):
            terms.add(seg[i:i + 2])
    return list(terms)


def score_page(page: dict, terms) -> int:
    if not terms:
        return 0
    title = page["title"].lower()
    tags = " ".join(page["tags"]).lower()
    body = page["body"].lower()
    score = 0
    for t in terms:
        score += title.count(t) * 5
        score += tags.count(t) * 3
        score += body.count(t) * 1
    return score


def make_snippet(page: dict, terms, width: int = 140) -> str:
    body = page["body"]
    low = body.lower()
    pos = -1
    for t in terms:
        i = low.find(t)
        if i != -1 and (pos == -1 or i < pos):
            pos = i
    if pos == -1:
        snippet = body[:width]
    else:
        start = max(0, pos - width // 3)
        snippet = body[start:start + width]
    snippet = re.sub(r"\s+", " ", snippet).strip()
    # 高亮
    for t in sorted(terms, key=len, reverse=True):
        snippet = re.sub("(" + re.escape(t) + ")", r"<mark>\1</mark>", snippet, flags=re.IGNORECASE)
    return ("…" if pos > 0 else "") + snippet + "…"


def search(pages: dict, query: str, type_filter=None):
    terms = build_terms(query)
    results = []
    for p in pages.values():
        if p["is_special"]:
            continue
        if type_filter and p["type"] not in type_filter:
            continue
        s = score_page(p, terms)
        if s > 0:
            results.append((s, p))
    results.sort(key=lambda x: x[0], reverse=True)
    out = []
    for s, p in results:
        out.append({
            "slug": p["slug"], "title": p["title"], "type": p["type"],
            "tags": p["tags"], "score": s, "snippet": make_snippet(p, terms),
        })
    return out, terms


# ---------------------------------------------------------------------------
# 链接分析：wikilink / related / edges.jsonl → 图谱 / 反向链接 / Lint
# ---------------------------------------------------------------------------

def wikilinks_in(body: str):
    return [m.split("|")[0].strip() for m in re.findall(r"\[\[([^\]]+)\]\]", body)]


def load_edges():
    edges = []
    f = WIKI_DIR / "graph" / "edges.jsonl"
    if not f.exists():
        return edges
    for line in f.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            e = json.loads(line)
            e["from_slug"] = str(e.get("from", "")).split("/")[-1]
            e["to_slug"] = str(e.get("to", "")).split("/")[-1]
            edges.append(e)
        except Exception:
            continue
    return edges


def build_links(pages: dict):
    """返回 (slugs, out_links, in_links, typed, broken)。
    typed[(src,tgt)] = 关系类型；broken = 指向不存在页的 wikilink。"""
    slugs = {p["slug"] for p in pages.values() if not p["is_special"]}
    out = {s: set() for s in slugs}
    typed = {}
    broken = []
    for p in pages.values():
        if p["is_special"]:
            continue
        s = p["slug"]
        for tgt in wikilinks_in(p["body"]):
            if tgt in slugs:
                out[s].add(tgt)
                typed.setdefault((s, tgt), "links_to")
            else:
                broken.append({"page": s, "target": tgt})
        for tgt in (p.get("related") or []):
            if tgt in slugs and tgt != s:
                out[s].add(tgt)
                typed.setdefault((s, tgt), "relates_to")
    for e in load_edges():               # edges.jsonl 类型优先级最高
        s, t = e["from_slug"], e["to_slug"]
        if s in slugs and t in slugs:
            out[s].add(t)
            typed[(s, t)] = e.get("type", "relates_to")
    inb = {s: set() for s in slugs}
    for s, tgts in out.items():
        for t in tgts:
            inb[t].add(s)
    return slugs, out, inb, typed, broken


def graph_data(pages: dict):
    slugs, out, inb, typed, _ = build_links(pages)
    nodes = [{"id": p["slug"], "title": p["title"], "type": p["type"],
              "deg": len(out[p["slug"]]) + len(inb[p["slug"]])}
             for p in pages.values() if not p["is_special"]]
    links = [{"source": s, "target": t, "type": ty} for (s, t), ty in typed.items()]
    return {"nodes": nodes, "links": links}


def lint_data(pages: dict):
    slugs, out, inb, typed, broken = build_links(pages)
    title = {p["slug"]: p["title"] for p in pages.values()}
    orphans = sorted(s for s in slugs if not inb[s])
    no_sources = sorted(p["slug"] for p in pages.values()
                        if not p["is_special"] and not (p.get("sources")))
    return {
        "broken": [{"page": b["page"], "title": title.get(b["page"], b["page"]),
                    "target": b["target"]} for b in broken],
        "orphans": [{"slug": s, "title": title.get(s, s)} for s in orphans],
        "no_sources": [{"slug": s, "title": title.get(s, s)} for s in no_sources],
        "counts": {"pages": len(slugs), "broken": len(broken),
                   "orphans": len(orphans), "no_sources": len(no_sources)},
    }


def backlinks_of(pages: dict, slug: str):
    _, _, inb, _, _ = build_links(pages)
    title = {p["slug"]: p["title"] for p in pages.values()}
    return [{"slug": s, "title": title.get(s, s)} for s in sorted(inb.get(slug, []))]


# ---------------------------------------------------------------------------
# Query 作用域识别（对应 AGENTS.md §5 的「先定作用域」）
# ---------------------------------------------------------------------------

def detect_scope(query: str):
    q = query.lower()
    exclude = []
    if re.search(r"(不要|别|排除).{0,4}(面试|八股)", q):
        exclude.append("interview")
    if re.search(r"面试|八股|突击|怎么答", q):
        return ["interview"], exclude, "命中『面试题』意图 → 限 type:interview"
    if re.search(r"方案|解决|最佳实践|优化|调优|排查", q):
        return ["article", "concept"], exclude, "命中『方案/最佳实践』意图 → 限 type:article + concept"
    if re.search(r"怎么|如何|启动|部署|配置|登录|操作|步骤|开通", q):
        return ["guide", "service"], exclude, "命中『怎么操作』意图 → 限 type:guide + service"
    return None, exclude, "未识别明确类型 → 全库检索"


def answer_query(pages: dict, query: str, provider_id: str = None):
    scope, exclude, reason = detect_scope(query)
    results, _ = search(pages, query, scope if scope else None)
    if exclude:
        results = [r for r in results if r["type"] not in exclude]
    citations = results[:8]
    base = {
        "query": query, "scope": scope or "全部类型", "exclude": exclude,
        "scope_reason": reason, "citations": citations,
        "provider": provider_id or "", "mode": "retrieval", "model": "",
    }
    prov = PROVIDERS.get(provider_id) if provider_id else None
    if prov:
        base["model"] = prov.get("model", "")
    # 选了 provider 且有 key 且有命中页 → 生成式
    if prov and resolve_key(prov) and citations:
        try:
            ctx = build_context(pages, citations[:6])
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": f"问题：{query}\n\n可用知识库页（只能依据这些作答）：\n\n{ctx}"},
            ]
            base["answer"] = call_llm(prov, messages)
            base["mode"] = "generative"
            return base
        except Exception as e:
            detail = e
            if isinstance(e, urllib.error.HTTPError):
                try:
                    detail = f"HTTP {e.code}: {e.read().decode('utf-8')[:300]}"
                except Exception:
                    detail = f"HTTP {e.code}"
            base["answer"] = (f"⚠️ 调用 {prov.get('label')} 失败：{detail}\n\n"
                              f"已回退为检索式：\n\n" + _compose_retrieval_answer(query, citations))
            return base
    # 检索式（未选模型 / 缺 key / 无命中）
    note = ""
    if prov and not resolve_key(prov):
        note = f"> （{prov.get('label')} 未配置 key，回退检索式。填好 `llm_config.json` 或设环境变量后重试。）\n\n"
    base["answer"] = note + _compose_retrieval_answer(query, citations)
    return base


def _compose_retrieval_answer(query: str, citations):
    if not citations:
        return "知识库暂无相关内容。建议先 ingest 相关源（raw/ 下投喂文档）后重试。"
    lines = [f"围绕「{query}」，在知识库中检索到 {len(citations)} 个相关页（按相关度排序）："]
    for i, c in enumerate(citations, 1):
        lines.append(f"{i}. [[{c['slug']}]]（{TYPE_LABEL.get(c['type'], c['type'])}）")
    lines.append("")
    lines.append("> 当前为**检索式**结果：选出最相关的页并高亮命中片段，点引用可读原文。")
    lines.append("> 接入 LLM 后，这里会变成基于以上页内容的**生成式带引用答案**。")
    return "\n".join(lines)


# ---------------------------------------------------------------------------
# Ingest 类功能：同主题聚类 → 枢纽页 / 跨类型（文章↔面试题）对照（用 LLM）
# ---------------------------------------------------------------------------

TYPE_DIR = {
    "guide": "guides", "service": "services", "concept": "concepts",
    "article": "articles", "interview": "interview", "output": "outputs",
}


def topic_clusters(pages: dict):
    """按 tag 聚类（≥2 页才算一个主题）。标记是否可做文章↔面试题对照。"""
    by_tag: dict = {}
    for p in pages.values():
        if p["is_special"]:
            continue
        for t in (p.get("tags") or []):
            by_tag.setdefault(t, []).append(p)
    out = []
    for tag, ps in by_tag.items():
        if len(ps) < 2:
            continue
        types = sorted({p["type"] for p in ps})
        out.append({
            "topic": tag, "count": len(ps), "types": types,
            "comparable": ("interview" in types) and (("article" in types) or ("concept" in types)),
            "pages": [{"slug": p["slug"], "title": p["title"], "type": p["type"]}
                      for p in sorted(ps, key=lambda x: x["slug"])],
        })
    out.sort(key=lambda x: (-x["count"], x["topic"]))
    return out


def _pages_with_tag(pages: dict, topic: str, types=None):
    res = [p for p in pages.values()
           if not p["is_special"] and topic in (p.get("tags") or [])
           and (not types or p["type"] in types)]
    res.sort(key=lambda x: x["slug"])
    return res


def _llm_err(e) -> str:
    detail = e
    if isinstance(e, urllib.error.HTTPError):
        try:
            detail = f"HTTP {e.code}: {e.read().decode('utf-8')[:300]}"
        except Exception:
            detail = f"HTTP {e.code}"
    return f"⚠️ 调用 LLM 失败：{detail}"


def _provider_note(prov) -> str:
    if prov is None:
        return ("> （未选模型，以下为**非生成式结构脚手架**：仅汇总页结构与链接，未做 LLM 提炼。"
                "选模型后可生成完整草稿。）\n\n")
    if not resolve_key(prov):
        return (f"> （{prov.get('label')} 未配置 key，以下为**结构脚手架**；"
                f"填好 `llm_config.json` 后可生成完整草稿。）\n\n")
    return ""


def _hub_outline(topic: str, members) -> str:
    lines = [f"# {topic}（枢纽页 · 草稿）", "", f"主题「{topic}」下共 {len(members)} 个页：", ""]
    by_type: dict = {}
    for p in members:
        by_type.setdefault(p["type"], []).append(p)
    for t in TYPE_ORDER:
        if t in by_type:
            lines.append(f"## {TYPE_LABEL.get(t, t)}")
            for p in by_type[t]:
                lines.append(f"- [[{p['slug']}]] — {p['title']}")
            lines.append("")
    lines.append("## 覆盖盲区 / 待补充")
    lines.append("- （选模型后由 LLM 提炼）")
    return "\n".join(lines)


def _compare_outline(topic: str, arts, itvs) -> str:
    lines = [f"# {topic} · 文章↔面试题 对照（草稿）", ""]
    lines.append(f"**文章/概念页（{len(arts)}）**：")
    for p in arts:
        lines.append(f"- [[{p['slug']}]]（{TYPE_LABEL.get(p['type'], p['type'])}）")
    lines.append("")
    lines.append(f"**面试题页（{len(itvs)}）**：")
    for p in itvs:
        lines.append(f"- [[{p['slug']}]]")
    lines.append("")
    lines.append("> 选模型后，这里会生成『要点覆盖对照表 + 出题建议 + 待补文章 + 矛盾检测』。")
    return "\n".join(lines)


def synthesize_hub(pages: dict, topic: str, provider_id: str = None):
    members = _pages_with_tag(pages, topic)
    base = {"topic": topic, "mode": "retrieval", "provider": provider_id or "", "model": "",
            "members": [{"slug": p["slug"], "title": p["title"], "type": p["type"]} for p in members]}
    if not members:
        base["draft"] = f"知识库暂无标签为「{topic}」的页。"
        return base
    prov = PROVIDERS.get(provider_id) if provider_id else None
    if prov:
        base["model"] = prov.get("model", "")
    if prov and resolve_key(prov):
        ctx = build_context(pages, [{"slug": p["slug"]} for p in members], budget=14000)
        msgs = [{"role": "system", "content": HUB_SYSTEM},
                {"role": "user", "content": f"主题：{topic}\n\n相关页（含类型，只能依据这些）：\n\n{ctx}"}]
        try:
            base["draft"] = call_llm(prov, msgs)
            base["mode"] = "generative"
            return base
        except Exception as e:
            base["draft"] = _llm_err(e) + "\n\n" + _hub_outline(topic, members)
            return base
    base["draft"] = _provider_note(prov) + _hub_outline(topic, members)
    return base


def synthesize_compare(pages: dict, topic: str, provider_id: str = None):
    arts = _pages_with_tag(pages, topic, ("article", "concept"))
    itvs = _pages_with_tag(pages, topic, ("interview",))
    base = {"topic": topic, "mode": "retrieval", "provider": provider_id or "", "model": "",
            "articles": [{"slug": p["slug"], "title": p["title"], "type": p["type"]} for p in arts],
            "interviews": [{"slug": p["slug"], "title": p["title"]} for p in itvs]}
    if not arts or not itvs:
        base["draft"] = (f"主题「{topic}」缺少可对照的两类内容：文章/概念 {len(arts)} 个、"
                         f"面试题 {len(itvs)} 个。两类各至少需 1 个才能对照。")
        return base
    prov = PROVIDERS.get(provider_id) if provider_id else None
    if prov:
        base["model"] = prov.get("model", "")
    if prov and resolve_key(prov):
        ctx = build_context(pages, [{"slug": p["slug"]} for p in (arts + itvs)], budget=15000)
        msgs = [{"role": "system", "content": COMPARE_SYSTEM},
                {"role": "user", "content": f"主题：{topic}\n\n【文章/概念】与【面试题】页如下：\n\n{ctx}"}]
        try:
            base["draft"] = call_llm(prov, msgs)
            base["mode"] = "generative"
            return base
        except Exception as e:
            base["draft"] = _llm_err(e) + "\n\n" + _compare_outline(topic, arts, itvs)
            return base
    base["draft"] = _provider_note(prov) + _compare_outline(topic, arts, itvs)
    return base


def save_draft(payload: dict):
    """把草稿写入 wiki/<typedir>/<slug>.md。不覆盖已存在文件。返回 (code, dict)。"""
    ptype = payload.get("type", "concept")
    slug = (payload.get("slug") or "").strip()
    title = payload.get("title") or slug
    topic = payload.get("topic") or ""
    md_body = payload.get("markdown") or ""
    if not slug:
        return 400, {"error": "slug 不能为空"}
    if "/" in slug or "\\" in slug or ".." in slug:
        return 400, {"error": "slug 含非法字符（不能含 / \\ ..）"}
    sub = TYPE_DIR.get(ptype, "concepts")
    target = WIKI_DIR / sub / (slug + ".md")
    if target.exists():
        return 409, {"error": f"已存在同名页：{target.relative_to(WIKI_DIR).as_posix()}，不覆盖。请改 slug 或手动合并。"}
    if not md_body.lstrip().startswith("---"):
        import datetime
        today = datetime.date.today().isoformat()
        tags = f"[{topic}]" if topic else "[]"
        fm = ("---\n"
              f"title: {title}\n"
              f"slug: {slug}\n"
              f"type: {ptype}\n"
              "status: draft\n"
              f"tags: {tags}\n"
              "sources: []\n"
              "related: []\n"
              f"created: {today}\n"
              f"updated: {today}\n"
              "---\n\n")
        md_body = fm + md_body
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(md_body, encoding="utf-8")
    return 200, {"ok": True, "path": target.relative_to(WIKI_DIR).as_posix(), "slug": slug}


# ---------------------------------------------------------------------------
# HTTP
# ---------------------------------------------------------------------------

class Handler(BaseHTTPRequestHandler):
    def _send(self, code, body, ctype="application/json; charset=utf-8"):
        data = body.encode("utf-8") if isinstance(body, str) else body
        self.send_response(code)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, *args):
        pass

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        qs = parse_qs(parsed.query)
        if path == "/" or path == "/index.html":
            return self._send(200, INDEX_HTML, "text/html; charset=utf-8")
        if path == "/api/tree":
            return self._send(200, json.dumps(self._tree(), ensure_ascii=False))
        if path == "/api/page":
            slug = (qs.get("slug") or [""])[0]
            return self._page(slug)
        if path == "/api/search":
            q = (qs.get("q") or [""])[0]
            tf = qs.get("type")
            res, terms = search(load_pages(), q, tf)
            return self._send(200, json.dumps({"query": q, "terms": terms, "results": res}, ensure_ascii=False))
        if path == "/api/providers":
            return self._send(200, json.dumps(list_providers(), ensure_ascii=False))
        if path == "/api/graph":
            return self._send(200, json.dumps(graph_data(load_pages()), ensure_ascii=False))
        if path == "/api/lint":
            return self._send(200, json.dumps(lint_data(load_pages()), ensure_ascii=False))
        if path == "/api/ask":
            q = (qs.get("q") or [""])[0]
            provider = (qs.get("provider") or [""])[0]
            return self._send(200, json.dumps(answer_query(load_pages(), q, provider), ensure_ascii=False))
        if path == "/api/topics":
            return self._send(200, json.dumps(topic_clusters(load_pages()), ensure_ascii=False))
        if path == "/api/hub":
            topic = (qs.get("topic") or [""])[0]
            provider = (qs.get("provider") or [""])[0]
            return self._send(200, json.dumps(synthesize_hub(load_pages(), topic, provider), ensure_ascii=False))
        if path == "/api/compare":
            topic = (qs.get("topic") or [""])[0]
            provider = (qs.get("provider") or [""])[0]
            return self._send(200, json.dumps(synthesize_compare(load_pages(), topic, provider), ensure_ascii=False))
        return self._send(404, json.dumps({"error": "not found"}))

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path == "/api/save_draft":
            try:
                n = int(self.headers.get("Content-Length") or 0)
                raw = self.rfile.read(n).decode("utf-8") if n else ""
                payload = json.loads(raw) if raw else {}
            except Exception as e:
                return self._send(400, json.dumps({"error": f"bad request: {e}"}, ensure_ascii=False))
            code, res = save_draft(payload)
            return self._send(code, json.dumps(res, ensure_ascii=False))
        return self._send(404, json.dumps({"error": "not found"}))

    def _tree(self):
        pages = load_pages()
        groups: dict = {}
        for p in pages.values():
            groups.setdefault(p["type"], []).append({
                "slug": p["slug"], "title": p["title"], "tags": p["tags"], "status": p["status"],
            })
        ordered = []
        for t in TYPE_ORDER:
            if t in groups:
                items = sorted(groups[t], key=lambda x: x["slug"])
                ordered.append({"type": t, "label": TYPE_LABEL.get(t, t), "items": items})
        return {"groups": ordered, "count": len([p for p in pages.values() if not p["is_special"]])}

    def _page(self, slug):
        pages = load_pages()
        p = pages.get(slug)
        if not p:
            return self._send(404, json.dumps({"error": f"page not found: {slug}"}, ensure_ascii=False))
        return self._send(200, json.dumps({
            "slug": p["slug"], "title": p["title"], "type": p["type"], "tags": p["tags"],
            "status": p["status"], "sources": p["sources"], "related": p["related"],
            "path": p["path"], "markdown": p["body"],
            "backlinks": backlinks_of(pages, slug),
        }, ensure_ascii=False))


INDEX_HTML = r"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>茉莉知识库 · Viewer</title>
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
<style>
  :root{--bg:#0f1115;--panel:#171a21;--panel2:#1d212b;--line:#2a2f3a;--fg:#e6e8ec;--mut:#9aa3b2;--acc:#6aa6ff;--acc2:#7ee787;--mark:#ffd86b}
  *{box-sizing:border-box}
  body{margin:0;font:14px/1.6 -apple-system,"Segoe UI",Roboto,"Microsoft YaHei",sans-serif;background:var(--bg);color:var(--fg);height:100vh;overflow:hidden}
  .app{display:grid;grid-template-columns:280px 1fr;grid-template-rows:52px 1fr;height:100vh}
  header{grid-column:1/3;display:flex;align-items:center;gap:14px;padding:0 16px;background:var(--panel);border-bottom:1px solid var(--line)}
  header .logo{font-weight:700;color:var(--acc2)}
  header .tabs{display:flex;gap:6px;margin-left:8px}
  header .tab{padding:5px 12px;border-radius:7px;cursor:pointer;color:var(--mut)}
  header .tab.on{background:var(--panel2);color:var(--fg)}
  header .count{margin-left:auto;color:var(--mut);font-size:12px}
  aside{background:var(--panel);border-right:1px solid var(--line);overflow:auto;padding:10px}
  aside .grp{margin-bottom:6px}
  aside .grp h4{margin:12px 6px 4px;font-size:11px;letter-spacing:.06em;color:var(--mut);text-transform:uppercase}
  aside .item{padding:6px 9px;border-radius:7px;cursor:pointer;color:var(--fg);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
  aside .item:hover{background:var(--panel2)}
  aside .item.on{background:#243049;color:var(--acc)}
  main{overflow:auto;padding:0}
  .view{display:none;height:100%}
  .view.on{display:block}
  /* 浏览 */
  .doc{max-width:860px;margin:0 auto;padding:26px 30px}
  .doc .crumb{color:var(--mut);font-size:12px;margin-bottom:10px}
  .doc .tags span{display:inline-block;background:var(--panel2);border:1px solid var(--line);color:var(--mut);border-radius:12px;padding:1px 9px;margin:0 6px 8px 0;font-size:12px}
  .md h1{font-size:24px;border-bottom:1px solid var(--line);padding-bottom:8px}
  .md h2{font-size:19px;margin-top:26px;border-bottom:1px solid var(--line);padding-bottom:5px}
  .md h3{font-size:16px}
  .md code{background:#0b0d12;border:1px solid var(--line);border-radius:5px;padding:1px 5px;font-size:13px}
  .md pre{background:#0b0d12;border:1px solid var(--line);border-radius:8px;padding:12px;overflow:auto}
  .md pre code{border:0;padding:0}
  .md table{border-collapse:collapse;width:100%;margin:12px 0}
  .md th,.md td{border:1px solid var(--line);padding:7px 10px;text-align:left}
  .md th{background:var(--panel2)}
  .md blockquote{border-left:3px solid var(--acc);margin:12px 0;padding:4px 14px;color:var(--mut);background:var(--panel)}
  .md a{color:var(--acc);text-decoration:none}
  .md a:hover{text-decoration:underline}
  .wl{color:var(--acc2);cursor:pointer;border-bottom:1px dashed var(--acc2)}
  .wl.dead{color:#ff7b72;border-bottom-color:#ff7b72}
  /* Query */
  .ask{max-width:880px;margin:0 auto;padding:24px 30px}
  .ask h2{margin:0 0 4px}
  .ask .sub{color:var(--mut);margin-bottom:16px}
  .askbar{display:flex;gap:10px}
  .askbar input{flex:1;background:var(--panel);border:1px solid var(--line);color:var(--fg);border-radius:9px;padding:11px 14px;font-size:15px}
  .askbar select{background:var(--panel);border:1px solid var(--line);color:var(--fg);border-radius:9px;padding:0 10px;font-size:13px;cursor:pointer}
  .askbar button{background:var(--acc);color:#06122b;border:0;border-radius:9px;padding:0 20px;font-weight:700;cursor:pointer}
  .mode{display:inline-block;font-size:12px;color:var(--mut);margin:8px 0 0;padding:2px 10px;border:1px solid var(--line);border-radius:12px}
  .mode.gen{color:var(--acc2);border-color:var(--acc2)}
  .chips{margin:12px 0;color:var(--mut);font-size:13px}
  .chips b{color:var(--acc2)}
  .scope{margin:14px 0;padding:10px 14px;background:var(--panel);border:1px solid var(--line);border-left:3px solid var(--mark);border-radius:8px}
  .scope .k{color:var(--mark)}
  .answer{margin:14px 0;padding:16px;background:var(--panel);border:1px solid var(--line);border-radius:10px}
  .cite{margin-top:14px}
  .cite .c{background:var(--panel);border:1px solid var(--line);border-radius:10px;padding:12px 14px;margin-bottom:10px;cursor:pointer}
  .cite .c:hover{border-color:var(--acc)}
  .cite .c .h{display:flex;gap:8px;align-items:center}
  .cite .c .h .t{font-weight:600;color:var(--acc)}
  .cite .c .h .badge{font-size:11px;color:var(--mut);border:1px solid var(--line);border-radius:10px;padding:0 8px}
  .cite .c .h .sc{margin-left:auto;color:var(--mut);font-size:12px}
  .cite .c .sn{color:var(--mut);font-size:13px;margin-top:6px}
  mark{background:var(--mark);color:#1a1300;padding:0 2px;border-radius:3px}
  .empty{color:var(--mut);padding:40px;text-align:center}
  /* 反向链接 */
  .backlinks{max-width:860px;margin:30px auto 0;padding:14px 0 0;border-top:1px solid var(--line)}
  .backlinks h3{font-size:14px;color:var(--mut);margin:0 0 8px}
  .backlinks .wl{margin-right:10px}
  /* 图谱 */
  .graphwrap{position:relative;width:100%;height:100%}
  #svg{width:100%;height:100%;display:block}
  .legend{position:absolute;top:12px;right:14px;background:var(--panel);border:1px solid var(--line);border-radius:8px;padding:8px 12px;font-size:12px}
  .legend .row{display:flex;align-items:center;gap:6px;margin:3px 0;color:var(--mut)}
  .legend .dot{width:10px;height:10px;border-radius:50%}
  /* 体检 */
  .lintwrap{max-width:900px;margin:0 auto;padding:24px 30px}
  .lsum{color:var(--mut);margin-bottom:18px}
  .lsec{margin-bottom:22px}
  .lsec h3{font-size:15px;display:flex;align-items:center;gap:8px}
  .lsec .n{font-size:12px;border-radius:10px;padding:0 9px}
  .lsec .n.bad{background:#5a1d1d;color:#ff9b8a}
  .lsec .n.good{background:#1d3a24;color:var(--acc2)}
  .litem{background:var(--panel);border:1px solid var(--line);border-radius:8px;padding:9px 12px;margin:7px 0;cursor:pointer}
  .litem:hover{border-color:var(--acc)}
  .litem .dead{color:#ff7b72}
  .ok{color:var(--acc2);padding:6px 2px}
  .ex{cursor:pointer;color:var(--acc);margin-right:14px}
</style>
</head>
<body>
<div class="app">
  <header>
    <span class="logo">茉莉知识库</span>
    <div class="tabs">
      <div class="tab on" data-v="ask">Query 问答</div>
      <div class="tab" data-v="browse">浏览</div>
      <div class="tab" data-v="graph">关系图谱</div>
      <div class="tab" data-v="lint">体检</div>
      <div class="tab" data-v="forge">提炼</div>
    </div>
    <span class="count" id="count"></span>
  </header>
  <aside id="tree"></aside>
  <main>
    <div class="view" id="v-browse">
      <div class="doc" id="doc"><div class="empty">← 左侧选择一个页面</div></div>
    </div>
    <div class="view" id="v-graph">
      <div class="graphwrap"><svg id="svg"></svg>
        <div class="legend" id="legend"></div>
      </div>
    </div>
    <div class="view" id="v-lint">
      <div class="lintwrap" id="lintout"><div class="empty">加载中…</div></div>
    </div>
    <div class="view" id="v-forge">
      <div class="ask">
        <h2>提炼 <span style="font-size:13px;color:var(--mut)">（同主题枢纽页 / 文章↔面试题 对照 · 用 LLM）</span></h2>
        <div class="sub">选一个<b>主题</b>（按 tag 聚类，≥2 页）→ 生成<b>枢纽页草稿</b>或<b>跨类型对照</b>。选模型则 LLM 提炼，否则出结构脚手架。</div>
        <div class="askbar">
          <select id="topic" title="选择主题（tag）" style="flex:1"></select>
          <select id="prov2" title="选择模型"></select>
          <button id="genHub">生成枢纽页</button>
          <button id="genCmp" style="background:var(--acc2)">跨类型对照</button>
        </div>
        <div id="forgeout"><div class="empty">选主题后点上方按钮</div></div>
      </div>
    </div>
    <div class="view on" id="v-ask">
      <div class="ask">
        <h2>Query 问答 <span style="font-size:13px;color:var(--mut)">（检索式 · 选页 + 高亮引用）</span></h2>
        <div class="sub">提问后，系统先识别<b>作用域</b>（搜哪些类型）→ 选出相关页 → 选模型则出<b>生成式带引用答案</b>，否则出检索式高亮片段。</div>
        <div class="askbar">
          <input id="q" placeholder="例如：怎么给新员工开通权限？ / mysql 性能优化方案 / 秒杀怎么扣库存" />
          <select id="prov" title="选择回答模型"></select>
          <button id="go">提问</button>
        </div>
        <div class="chips">试试：
          <span class="ex">怎么给新员工开通权限</span>
          <span class="ex">秒杀怎么扣库存</span>
          <span class="ex">网关有没有鉴权</span>
          <span class="ex">token 是怎么生成的</span>
        </div>
        <div id="askout"></div>
      </div>
    </div>
  </main>
</div>
<script>
const $=s=>document.querySelector(s);
let TREE=null;
function md(text){
  // 先把 [[slug|label]] / [[slug]] 转成可点 span
  text=text.replace(/\[\[([^\]|]+)(?:\|([^\]]+))?\]\]/g,(m,slug,label)=>{
    slug=slug.trim();label=(label||slug).trim();
    return `<span class="wl" data-slug="${slug}">${label}</span>`;
  });
  if(window.marked){marked.setOptions({gfm:true,breaks:false});return marked.parse(text);}
  return '<pre>'+text.replace(/</g,'&lt;')+'</pre>';
}
function bindWiki(root){
  root.querySelectorAll('.wl').forEach(el=>{
    const slug=el.dataset.slug;
    if(TREE && !TREE.slugs.has(slug)) el.classList.add('dead');
    el.onclick=()=>openPage(slug);
  });
}
async function loadTree(){
  const t=await (await fetch('/api/tree')).json();
  TREE=t; TREE.slugs=new Set();
  let html='';
  for(const g of t.groups){
    html+=`<div class="grp"><h4>${g.label}</h4>`;
    for(const it of g.items){TREE.slugs.add(it.slug);
      html+=`<div class="item" data-slug="${it.slug}" title="${it.title}">${it.title}</div>`;}
    html+='</div>';
  }
  $('#tree').innerHTML=html;
  $('#count').textContent=t.count+' 页';
  $('#tree').querySelectorAll('.item').forEach(el=>el.onclick=()=>openPage(el.dataset.slug));
}
async function openPage(slug){
  switchView('browse');
  $('#tree').querySelectorAll('.item').forEach(el=>el.classList.toggle('on',el.dataset.slug===slug));
  const r=await fetch('/api/page?slug='+encodeURIComponent(slug));
  if(!r.ok){$('#doc').innerHTML='<div class="empty">页面不存在：'+slug+'</div>';return;}
  const p=await r.json();
  const tags=(p.tags||[]).map(t=>`<span>${t}</span>`).join('');
  const back=(p.backlinks||[]).map(b=>`<span class="wl" data-slug="${b.slug}">${b.title}</span>`).join('')
    ||'<span style="color:var(--mut)">无（孤儿页）</span>';
  $('#doc').innerHTML=`<div class="crumb">${p.path} · ${p.type}${p.status?(' · '+p.status):''}</div>
    <div class="tags">${tags}</div><div class="md">${md(p.markdown)}</div>
    <div class="backlinks"><h3>反向链接（谁引用了本页）</h3><div>${back}</div></div>`;
  bindWiki($('#doc'));
  $('main').scrollTop=0;
}
const TYPECOL={guide:'#6aa6ff',service:'#7ee787',concept:'#ffd86b',article:'#c391f5',interview:'#ff9b72',output:'#79c0ff',meta:'#9aa3b2'};
const TYPELABEL={guide:'操作指导',service:'微服务',concept:'概念',article:'技术文章',interview:'面试题',output:'问答回写'};
function toSvg(svg,e){const r=svg.getBoundingClientRect();return {x:e.clientX-r.left,y:e.clientY-r.top};}
function loadGraph(){
  const svg=document.getElementById('svg');
  const W=svg.clientWidth||900,H=svg.clientHeight||600;
  fetch('/api/graph').then(r=>r.json()).then(g=>{
    const nodes=g.nodes.map(n=>({...n,x:W/2+(Math.random()-.5)*320,y:H/2+(Math.random()-.5)*320,vx:0,vy:0}));
    const idx={};nodes.forEach(n=>idx[n.id]=n);
    const links=g.links.filter(l=>idx[l.source]&&idx[l.target]).map(l=>({s:idx[l.source],t:idx[l.target],type:l.type}));
    const NS='http://www.w3.org/2000/svg';svg.innerHTML='';
    const gL=document.createElementNS(NS,'g'),gN=document.createElementNS(NS,'g');
    svg.appendChild(gL);svg.appendChild(gN);
    const lineEls=links.map(()=>{const e=document.createElementNS(NS,'line');e.setAttribute('stroke','#2a2f3a');e.setAttribute('stroke-width','1.4');gL.appendChild(e);return e;});
    const nodeEls=nodes.map(n=>{
      const grp=document.createElementNS(NS,'g');grp.style.cursor='pointer';
      const r=9+Math.min(9,n.deg*1.4);
      const c=document.createElementNS(NS,'circle');c.setAttribute('r',r);c.setAttribute('fill',TYPECOL[n.type]||'#888');c.setAttribute('stroke','#0f1115');c.setAttribute('stroke-width','2');
      const t=document.createElementNS(NS,'text');t.textContent=n.title;t.setAttribute('font-size','11');t.setAttribute('fill','#e6e8ec');t.setAttribute('x',r+3);t.setAttribute('y','4');
      grp.appendChild(c);grp.appendChild(t);grp.onclick=()=>openPage(n.id);
      grp.onmousedown=(ev)=>{ev.preventDefault();const mv=e2=>{const pt=toSvg(svg,e2);n.x=pt.x;n.y=pt.y;n.vx=0;n.vy=0;n.pin=true;};const up=()=>{n.pin=false;document.removeEventListener('mousemove',mv);document.removeEventListener('mouseup',up);};document.addEventListener('mousemove',mv);document.addEventListener('mouseup',up);};
      gN.appendChild(grp);return grp;
    });
    let alpha=1;
    (function tick(){
      for(let i=0;i<nodes.length;i++)for(let j=i+1;j<nodes.length;j++){
        const a=nodes[i],b=nodes[j];let dx=a.x-b.x,dy=a.y-b.y;let d2=dx*dx+dy*dy||1,d=Math.sqrt(d2);
        const f=4500/d2,fx=dx/d*f,fy=dy/d*f;a.vx+=fx;a.vy+=fy;b.vx-=fx;b.vy-=fy;
      }
      links.forEach(l=>{let dx=l.t.x-l.s.x,dy=l.t.y-l.s.y,d=Math.sqrt(dx*dx+dy*dy)||1;const f=(d-100)*0.02,fx=dx/d*f,fy=dy/d*f;l.s.vx+=fx;l.s.vy+=fy;l.t.vx-=fx;l.t.vy-=fy;});
      nodes.forEach(n=>{n.vx+=(W/2-n.x)*0.002;n.vy+=(H/2-n.y)*0.002;if(!n.pin){n.vx*=0.85;n.vy*=0.85;n.x+=n.vx*alpha;n.y+=n.vy*alpha;n.x=Math.max(50,Math.min(W-60,n.x));n.y=Math.max(24,Math.min(H-24,n.y));}});
      links.forEach((l,i)=>{lineEls[i].setAttribute('x1',l.s.x);lineEls[i].setAttribute('y1',l.s.y);lineEls[i].setAttribute('x2',l.t.x);lineEls[i].setAttribute('y2',l.t.y);});
      nodes.forEach((n,i)=>nodeEls[i].setAttribute('transform',`translate(${n.x},${n.y})`));
      alpha*=0.985;if(alpha>0.02)requestAnimationFrame(tick);
    })();
    const used=[...new Set(nodes.map(n=>n.type))];
    $('#legend').innerHTML=used.map(t=>`<div class="row"><span class="dot" style="background:${TYPECOL[t]||'#888'}"></span>${TYPELABEL[t]||t}</div>`).join('');
  });
}
function loadLint(){
  fetch('/api/lint').then(r=>r.json()).then(d=>{
    const c=d.counts;
    const sec=(title,items,render,ok)=>`<div class="lsec"><h3>${title} <span class="n ${items.length?'bad':'good'}">${items.length}</span></h3>${items.length?items.map(render).join(''):`<div class="ok">${ok}</div>`}</div>`;
    $('#lintout').innerHTML=`
      <div class="lsum">共 ${c.pages} 页 · 断链 ${c.broken} · 孤儿页 ${c.orphans} · 缺来源 ${c.no_sources}</div>
      ${sec('断链（指向不存在的页）',d.broken,b=>`<div class="litem" data-slug="${b.page}"><b>${b.title}</b> → <span class="dead">[[${b.target}]]</span></div>`,'无断链')}
      ${sec('孤儿页（无任何入链）',d.orphans,o=>`<div class="litem" data-slug="${o.slug}">${o.title}</div>`,'无孤儿页')}
      ${sec('缺来源（frontmatter sources 为空）',d.no_sources,o=>`<div class="litem" data-slug="${o.slug}">${o.title}</div>`,'来源齐全')}`;
    $('#lintout').querySelectorAll('.litem[data-slug]').forEach(el=>el.onclick=()=>openPage(el.dataset.slug));
  });
}
async function loadProviders(){
  try{
    const ps=await (await fetch('/api/providers')).json();
    let opts='';
    for(const p of ps){
      opts+=`<option value="${p.id}" ${p.available?'':'disabled'}>${p.label}${p.available?'':'（缺key）'}</option>`;
    }
    $('#prov').innerHTML='<option value="">检索式（不调用LLM）</option>'+opts;
    if($('#prov2')) $('#prov2').innerHTML='<option value="">不调用LLM（脚手架）</option>'+opts;
  }catch(e){$('#prov').innerHTML='<option value="">检索式</option>';}
}
let TOPICS=[];
async function loadTopics(){
  try{
    TOPICS=await (await fetch('/api/topics')).json();
    $('#topic').innerHTML=TOPICS.length
      ? TOPICS.map(t=>`<option value="${t.topic}">${t.topic}（${t.count}页${t.comparable?' · 可对照':''}）</option>`).join('')
      : '<option value="">（暂无 ≥2 页的主题）</option>';
  }catch(e){$('#topic').innerHTML='<option value="">加载失败</option>';}
}
async function runForge(kind){
  const topic=$('#topic').value; if(!topic){return;}
  const prov=$('#prov2').value;
  $('#forgeout').innerHTML='<div class="empty">'+(prov?'模型生成中…（首次可能稍慢）':'汇总中…')+'</div>';
  const ep=kind==='hub'?'hub':'compare';
  const url='/api/'+ep+'?topic='+encodeURIComponent(topic)+(prov?('&provider='+encodeURIComponent(prov)):'');
  const r=await (await fetch(url)).json();
  const gen=r.mode==='generative';
  const badge=gen?('🤖 '+(r.model||'模型')+' 生成'):'🧩 结构脚手架（未调用 LLM）';
  const members=kind==='hub'?(r.members||[]):[...(r.articles||[]),...(r.interviews||[])];
  const chips=members.map(m=>`<span class="wl" data-slug="${m.slug}">${m.title}</span>`).join(' ')||'无';
  const canSave=kind==='hub'&&!(TOPICS.find(t=>t.topic===topic)&&false);
  const saveBtn=kind==='hub'
    ? `<button id="saveHub" style="background:var(--acc);color:#06122b;border:0;border-radius:9px;padding:8px 16px;font-weight:700;cursor:pointer">保存为草稿 → concepts/${topic}.md</button>`
    : '';
  $('#forgeout').innerHTML=`<div class="mode ${gen?'gen':''}">${badge}</div>
     <div class="chips">主题 <b>${r.topic}</b> · 相关页：${chips}</div>
     <div class="answer md">${md(r.draft||'')}</div>
     <div style="margin-top:12px">${saveBtn}</div>`;
  bindWiki($('#forgeout'));
  if(kind==='hub'){const sb=$('#saveHub');if(sb)sb.onclick=()=>saveHub(r.topic,r.draft);}
}
async function saveHub(topic,draft){
  if(!confirm('保存为 concepts/'+topic+'.md（status: draft，不覆盖已存在文件）。继续？'))return;
  const r=await fetch('/api/save_draft',{method:'POST',headers:{'Content-Type':'application/json'},
    body:JSON.stringify({type:'concept',slug:topic,title:topic,topic:topic,markdown:draft})});
  const j=await r.json();
  if(r.ok&&j.ok){alert('已保存：'+j.path);await loadTree();openPage(j.slug);}
  else alert('保存失败：'+(j.error||('HTTP '+r.status)));
}
async function doAsk(q){
  switchView('ask');
  $('#q').value=q;
  const prov=$('#prov').value;
  $('#askout').innerHTML='<div class="empty">'+(prov?'模型思考中…（首次可能稍慢）':'检索中…')+'</div>';
  const url='/api/ask?q='+encodeURIComponent(q)+(prov?('&provider='+encodeURIComponent(prov)):'');
  const r=await (await fetch(url)).json();
  const cites=(r.citations||[]).map(c=>`
    <div class="c" data-slug="${c.slug}">
      <div class="h"><span class="t">${c.title}</span>
        <span class="badge">${c.type}</span><span class="sc">score ${c.score}</span></div>
      <div class="sn">${c.snippet}</div>
    </div>`).join('');
  const ex=(r.exclude&&r.exclude.length)?` · 排除 <b>${r.exclude.join(', ')}</b>`:'';
  const gen=r.mode==='generative';
  const badge=gen?('🤖 '+(r.model||'模型')+' 生成'):'🔎 检索式（未调用 LLM）';
  $('#askout').innerHTML=`
    <div class="mode ${gen?'gen':''}">${badge}</div>
    <div class="scope"><span class="k">作用域</span>：${Array.isArray(r.scope)?r.scope.join(' + '):r.scope}${ex}<br/>
      <span style="color:var(--mut)">${r.scope_reason}</span></div>
    <div class="answer md">${md(r.answer)}</div>
    <div class="cite">${cites||'<div class="empty">无命中</div>'}</div>`;
  bindWiki($('#askout'));
  $('#askout').querySelectorAll('.cite .c').forEach(el=>el.onclick=()=>openPage(el.dataset.slug));
}
function switchView(v){
  document.querySelectorAll('.tab').forEach(t=>t.classList.toggle('on',t.dataset.v===v));
  document.querySelectorAll('.view').forEach(el=>el.classList.remove('on'));
  $('#v-'+v).classList.add('on');
  if(v==='graph') loadGraph();
  if(v==='lint') loadLint();
  if(v==='forge' && !TOPICS.length) loadTopics();
}
document.querySelectorAll('.tab').forEach(t=>t.onclick=()=>switchView(t.dataset.v));
$('#genHub').onclick=()=>runForge('hub');
$('#genCmp').onclick=()=>runForge('compare');
$('#go').onclick=()=>{const q=$('#q').value.trim();if(q)doAsk(q);};
$('#q').addEventListener('keydown',e=>{if(e.key==='Enter'){const q=$('#q').value.trim();if(q)doAsk(q);}});
document.querySelectorAll('.ex').forEach(el=>el.onclick=()=>doAsk(el.textContent));
loadTree();
loadProviders();
</script>
</body>
</html>"""


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--port", type=int, default=8765)
    ap.add_argument("--host", default="127.0.0.1")
    args = ap.parse_args()
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass
    pages = load_pages()
    print(f"茉莉知识库 Viewer  |  KB: {KB_ROOT}")
    print(f"已加载 {len([p for p in pages.values() if not p['is_special']])} 个 wiki 页")
    print("LLM provider:")
    for p in list_providers():
        flag = "OK  " if p["available"] else "-- 缺key"
        print(f"  [{flag}] {p['label']}  ({p['model']})")
    print(f"打开浏览器访问: http://{args.host}:{args.port}")
    ThreadingHTTPServer((args.host, args.port), Handler).serve_forever()


if __name__ == "__main__":
    main()
