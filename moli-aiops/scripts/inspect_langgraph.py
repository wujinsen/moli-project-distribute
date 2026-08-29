"""开发辅助：确认 LangGraph 的 interrupt / Command / checkpointer API 形态。"""

from __future__ import annotations

import inspect


def main() -> None:
    import langgraph
    from langgraph.graph import END, START, StateGraph
    from langgraph.types import Command, interrupt

    print("langgraph:", getattr(langgraph, "__version__", "?"))
    print("StateGraph.compile:", inspect.signature(StateGraph.compile))
    print("interrupt:", inspect.signature(interrupt))
    print("Command fields:", getattr(Command, "__dataclass_fields__", Command).keys()
          if hasattr(Command, "__dataclass_fields__") else dir(Command)[:20])
    print("START/END:", START, END)

    from langgraph.checkpoint.sqlite import SqliteSaver

    print("SqliteSaver:", SqliteSaver)
    print("SqliteSaver.from_conn_string:", inspect.signature(SqliteSaver.from_conn_string))

    compiled_methods = [
        m for m in dir(StateGraph(dict).compile()) if not m.startswith("_")
    ]
    print("compiled graph methods:", compiled_methods)


if __name__ == "__main__":
    main()
