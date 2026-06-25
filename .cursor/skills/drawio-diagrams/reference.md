# draw.io XML 片段参考

> 仅当 SKILL.md 不够用时查阅。复制后改 id、坐标、文案。

## 实体框（ER / 组件）

```xml
<mxCell id="entity_kb_space"
  value="kb_space&#xa;─────────────&#xa;id PK&#xa;space_code UK&#xa;space_name"
  style="rounded=1;whiteSpace=wrap;html=1;align=left;verticalAlign=top;spacingLeft=10;spacingTop=6;fontSize=11;fillColor=#dae8fc;strokeColor=#6c8ebf;fontStyle=1;"
  vertex="1" parent="1">
  <mxGeometry x="980" y="90" width="220" height="130" as="geometry" />
</mxCell>
```

## 分层泳道（数据管道）

```xml
<mxCell id="L0" value="L0 · 原始数据层 RAW（只读）"
  style="swimlane;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;startSize=28;fontStyle=1;rounded=1;"
  vertex="1" parent="1">
  <mxGeometry x="40" y="70" width="1620" height="100" as="geometry" />
</mxCell>
<!-- 子节点 parent="L0"，不要用 HTML 堆在 L0 的 value 里 -->
<mxCell id="raw1" value="kb/raw/ ..."
  style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#d6b656;"
  vertex="1" parent="L0">
  <mxGeometry x="30" y="40" width="200" height="50" as="geometry" />
</mxCell>
```

## 关系线（逻辑外键）

```xml
<mxCell id="r1" value="1:N"
  style="edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;dashed=1;strokeColor=#6c8ebf;endArrow=block;endFill=1;fontSize=10;"
  edge="1" parent="1" source="kb_space" target="kb_document">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

## MySQL 圆柱

```xml
<mxCell id="mysql" value="MySQL moli&#xa;kb_document ..."
  style="shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=15;fillColor=#ffe6cc;strokeColor=#d79b00;"
  vertex="1" parent="1">
  <mxGeometry x="720" y="180" width="140" height="100" as="geometry" />
</mxCell>
```

## 禁止写法（会乱码）

```xml
<!-- BAD: swimlane + stackLayout + 多行 HTML 在 value -->
<mxCell id="bad" value="&lt;b&gt;kb_space&lt;/b&gt;&lt;hr&gt;id PK&lt;br&gt;..."
  style="swimlane;childLayout=stackLayout;..." ... />
```
