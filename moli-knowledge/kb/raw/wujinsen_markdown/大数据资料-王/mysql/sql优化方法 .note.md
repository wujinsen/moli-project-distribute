- 1、Select语句优化要点


- (1) 对于⼤数据量的求和应避免使⽤单⼀的sum命令处理，可采⽤group by⽅式与其结合，有时其效 率可提⾼⼏倍甚⾄百倍。例如，银⾏常

要进⾏帐户的总帐与明细帐⼀致性核对(总分核 对)，数据量⼤，原采⽤单⼀的sum命令与while 语句结合来完成，现改⽤以下group by

⽅式后效率⼤相径庭。 /＊将定期表中所有数据按机构，储种统计户数，余额置临时表中并建索引＊/

select zh[1,9] jg,zh[19,20]cz,count(＊)hs,sum(ye)sumye from satdq where bz=″0″ group by zh[1,9],zh [19,20] into temp satdq_sum; create index satdq_suml on satdq_sum(jg,cz); (帐号zh的前9位为机构编码，第19⾄20位为储

种)

- (2) 最具有限制性的条件放在前⾯，⼤值在前，⼩值在后。 如：where col<=1 0 and col>=1 效率⾼ where col>=1 and col<=1 0 效率低
- (3)避免⼦查询与相关查询。 如：where zh in (select zh from table where xm matches ″＊ 1＊″) 可将其编为declare cursor 的

⼀while循环来处理。

- (4)避免会引起磁盘读写的rowid操作。 在where⼦句中或select语句中，⽤rowid要产⽣磁盘读写，是⼀个物理过程，会影响性能。


如原为： declare ps2 cursor for select ＊,rowid into b,id from satmxhz where zh[1,9]=vjgbm and bz=″0″ order by zh; open ps2; fetch ps2; while (sqlca.sqlcode=0){ … update satmxhz set

sbrq=b.sbrq, ye=b.ye, lxjs=b.lxjs, wdbs=wdbs＋1, dac=dac where rowid=id;

… fetch ps2; } 改为： declare ps2 cursor for select ＊ into b from satmxhz where zh [1,9]=vjgbm and bz=″0″ for update of sbrq,ye,lxjs,wdbs,dac; open ps2; fetch ps2; while (sqlca.sqlcode=0){

…

update satmxhz set

sbrq=b.sbrq, ye =b.ye, lxjs=b.lxjs, wdbs=b.wdbs, dac=dac where curent of ps2; … fetch ps2; }

- (5)where⼦句中变量顺序应与索引字键顺序相同。 如：create index putlsz_idx on putlsz(zh ,rq,lsh) 索引字键顺序：⾸先是帐号zh，其次是⽇期rq,最后是流⽔号lsh, 所以where⼦句变量顺序应是where zh=″ 1″and rq=″06/06/1 9″and lsh<1 0,不应是where

lsh<1 0 and rq=″06/06/1 9″ and zh =″ 1″等⾮索引字键顺序。

- (6)⽤=替代matches的操作。 如：where zh matches ″3067860＊″应⽤where zh[1,9]=″3067860″替代。
- (7)通过聚族索引cluster index提⾼效率。
- (8)避免使⽤order by,group by,该操作需⽣成临时表⽽影响效率，可⽤视图来处理，视图的引⼊能控


制⽤户的存取，提⾼效率。

- 2、insert语句优化要点


- (1)采⽤insert cursor或put替代insert； 如：wr_satmx () begin work; prepare insert_mx from ″insert into satmx values(?,?,?,?,?,?,?,?,?,?,?,?)″； declare mx_cur cursor for insert_mx; open mx_cur; declare cur_mxh cursor for select ＊ into bmxh from satmxh for update; open cur_mxh; fetch cur_mxh; while (sqlca.sqlcode=0){ put mx_cur from bmxh.zh ,bmxh,rq,bmxh,l sh,bmxh,jym, bmx,pzhm,bmxh.bz,bmxh,fse, bmxh.ye,bmxh.bdlsh,bmxh.bd rq,bmxh.czy,bmxh.dybz; delete from satmxh where curent of cur_mxh; fetch cur_mxh; } close mx_cur; close cur_mxh; comit work; 以上⼀段程序是将satmxh表中记录转移到satmx表中，虽然可⽤ begin work; insert into satmx select ＊ from satmxh; dele te from satmxh; comit work;


- 四⾏程序即可实现，但若表中记录多的话，其运⾏效率远远不如前者的处理⽅式，因为insert cursor 是先在共享内存缓存中处理，刷新时写⼊磁盘的，所以上载数据速度最快，但其缺点是必须编程实 现。
- (2)避免加⻓锁、⻓事务操作，这在处理⼤数据量时其优劣尤为突出，在能保证数据⼀致性的前提下应 将⻓事务分解为⼩事务来处理。


如将前⾯例题数据分不同⽹点机构进⾏转移，避免⻓事务，可⼤⼤提⾼运⾏效率。

wr_satmx(): database workdb; declare cur_jgl cursor with hold for select jgbm,jgmc intovjgbm,vjgmc from putjgbm order

by jgbm open cur_jgl;

fetch cur_jgl; while(sqlca.sqlcode=0){ begin work; prepare insert_mx from ″insert into satmx values(?,?，?,?,?,?,?,?,?,?,?,?)″; declare mx_cur

cursor for insert_mx open mx_cur declare cur_mxh cursor for select ＊ into bmxh from satmxh where zh [1,9]=vjgbm for update; open cur_mxh; fetch cur_mxh; while (sqlca.sqlcode=0){ put mx_cur from bmxh.zh,bmxh.rq,bmxh.lsh,bmxh,jym, bmx.pzhm,bmxh.bz,bmxh.fse,

bmxh.ye,bmxh.bdlsh,bmxh.bd rq,bmxh.czy,bmxh.dybz; delete from satmxh where curent of cur_mxh; fetch cur_mxh; } close mx_cur; close cur_mxh; comit work; fetch cor_jgl; } close cur_jgl; close database;

- (3)宿主变量应在执⾏insert操作前转换为表结构描述的数据类型，避免insert语句操作时不同数据类

型⾃动转换⽽影响其效率。

- (4)对表的insert操作很频繁时，可以将index fil factor降低⼀些，采⽤row lock 代替page lock。


- 3、update语句优化要点

- (1)⽤⼦串代替matches,避免使⽤不从第⼀个开始的⼦串。 如where a matches ″ab＊″采⽤where a [1,2]=″ab″代替；避免使⽤如b[5,6]的⼦串。
- (2)避免加⻓锁修改，避免⻓事务处理，例⼦参⻅insert的语句优化(2)⽅式。


- 4.delete语句优化要点


- (1)⽤drop table,create table和create index代替delete from table，能快速清理并释放表空间。
- (2)避免⻓事务处理，例⼦参⻅insert的语句优化(2)⽅式。
- (3)使⽤关联(⽗⼦)删除cascading delete。
- (4)编写程序使⽤delete cursor删，⽽不采⽤delete from table where…的⽅式。例⼦参⻅insert的语


句优化(1)⽅式。

