# Elasticsearch 7 : 关于 Index、Type、Document

含义

Index：索引。 Type：类型。 Document：⽂档。 ⽂档是 JSON 类型的

与 MySQL 类⽐ 可以将 ES 中的这三个概念和 MySQL 类⽐：

Index 对应 MySQL 中的 Database； Type 对应 MySQL 中的 Table； Document 对应 MySQL 中表的记录。 ⼀个MySQL实例中可以创建多个 Database，⼀个Database中可以创建多个Table。 从 ES 7.0 开始，Type 被废弃 在 7.0 以及之后的版本中 Type 被废弃了。⼀个 index 中只有⼀个默认的 type，即 _doc。

ES 的Type 被废弃后，库表合⼀，Index 既可以被认为对应 MySQL 的 Database，也可以认为对应 table。

也可以这样理解：

ES 实例：对应 MySQL 实例中的⼀个 Database。 Index 对应 MySQL 中的 Table 。 Document 对应 MySQL 中表的记录

⸻版权声明：本⽂为CSDN博主「⻜翔的⼩码」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上 原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/ q_34789780/article/details/15126368

