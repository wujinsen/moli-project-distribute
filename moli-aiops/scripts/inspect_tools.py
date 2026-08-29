"""开发辅助：打印 MCP 工具注册结果，确认 schema 与 annotations 命名。"""

from __future__ import annotations

import sys
from pathlib import Path

import anyio

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from ops_mcp import mcp_server  # noqa: E402


def main() -> None:
    tools = anyio.run(mcp_server.server.list_tools)
    print(f"registered: {len(tools)}\n")
    first = tools[0]
    print("Tool fields:", sorted(type(first).model_fields))
    if first.annotations is not None:
        print("Annotation fields:", sorted(type(first.annotations).model_fields))
        print("Annotation dump:", first.annotations.model_dump())
    print()
    for tool in tools:
        schema = tool.input_schema or {}
        params = sorted((schema.get("properties") or {}).keys())
        ann = tool.annotations.model_dump() if tool.annotations else {}
        print(f"{tool.name:22} ann={ann}")
        print(f"{'':22} params={params}")


if __name__ == "__main__":
    main()
