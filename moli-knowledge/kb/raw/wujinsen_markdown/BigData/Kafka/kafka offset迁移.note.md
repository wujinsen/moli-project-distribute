在早前的kafka版本中（0.8.0），offset是被存储在zookeeper中的。

到当前版本（0.8.2）为⽌，kafka同时⽀持offset存储在zookeeper和offset manager（broker） 中。

从官⽅的说明来看，未来offset的zookeeper存储将会被弃⽤。因此现有的基于kafka的项⽬如果今 后计划保持更新的话，可以考虑在合适的时候将offset迁移到kafka broker上。

以下是迁移步骤：

在consume config中，修改offsets.storage=kafka并且dual.commit.enabled=true。第⼀个修 改不⽤解释。第⼆个配置项的意思是，同时提交offset到zookeeper和offset manager上，这是 为了保证在迁移过程中offset不会丢失。 rolling restart consumers并且确认运⾏正常。这时已经完成offset的迁移⼯作。 修改consumer config，dual.commit.enabled=false。双重提交offset会带来额外的开销，在完 成迁移⼯作之后最好把这项配置关闭。 rolling restart consumers

- 1.
- 2.
- 3.
- 4.


