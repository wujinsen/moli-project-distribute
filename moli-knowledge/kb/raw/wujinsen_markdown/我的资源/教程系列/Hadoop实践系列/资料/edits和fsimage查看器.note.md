在hadop中edits和fsimage是两个⾄关重要的⽂件，其中edits负责保存⾃最新检查点后命名空间的 变化，起着⽇志的作⽤，⽽fsimage则保存了最新的检查点信息。这个两个⽂件中的内容使⽤普通⽂本 编辑器是⽆法直接查看的，幸运的是hadop为此准备了专⻔的⼯具⽤于查看⽂件的内容，这些⼯具分 别为oev和oiv，可以使⽤hdfs调⽤执⾏。

oev是ofline edits viewer（离线edits查看器）的缩写，该⼯具只操作⽂件因⽽并不需要hadop集 群处于运⾏状态。该⼯具提供了⼏个输出处理器，⽤于将输⼊⽂件转换为相关格式的输出⽂件，可以 使⽤参数-p指定。⽬前⽀持的输出格式有binary（hadop使⽤的⼆进制格式）、xml（在不使⽤参数p 时的默认输出格式）和stats（输出edits⽂件的统计信息）。该⼯具⽀持的输⼊格式为binary和xml，其 中的xml⽂件为该⼯具使⽤xml处理器的输出⽂件。由于没有与stats格式对应的输⼊⽂件，所以⼀旦输 出为stats格式将不可以再转换为原有格式。⽐如输⼊格式为bianry，输出格式为xml，可以通过将输⼊ ⽂件指定为原来的输出⽂件，将输出⽂件指定为原来的输⼊⽂件实现binary和xml的转换，⽽stats则不 可以。该⼯具的具体使⽤语法为：

Usage: bin/hdfs oev [OPTIONS] -i INPUT_FILE -o OUTPUT_FILE Parse a Hadoop edits log file INPUT_FILE and save results in OUTPUT_FILE. Required command line arguments:

- -i,--inputFile <arg> edits file to process, xml ( case insensitive) ex

- -o,--outputFile <arg> Name of output file. If the specified file exists, Optional command line arguments:

- -p,--processor <arg> Select which type of processor to apply against ima


xml ( default , XML format), stats (prints statistics about edits file)

- -h,--help Display usage information and exit

- -f,--fix-txids Renumber the transaction IDs in the input,so that t

- -r,--recover When reading binary edit logs, use recovery mode. T

- -v,--verbose More verbose output, prints the input and output file this will dramatically increase processing time ( default is false


该⼯具使⽤的示例及输出⽂件的部分⽂件内容如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br><br><br>16</th>
    <th>$ hdfs oev -i edits_0000000000000000081- 0000000000000000089 -o<br><br>edits.xml <?xml version= "1.0" encoding= "UTF-8" ?> <EDITS><br><br><EDITS_VERSION>- 56 </EDITS_VERSION> <RECORD><br><br><OPCODE>OP_DELETE</OPCODE> <DATA><br><br><TXID> 88 </TXID> <LENGTH> 0 </LENGTH> <PATH>/user/hive/test</PATH> <TIMESTAMP> 1413794973949 </TIMESTAMP> <RPC_CLIENTID>a52277d8-a855-41ee-9ca2-<br><br>a5d0bc7d298a</RPC_CLIENTID><br><br><RPC_CALLID> 3 </RPC_CALLID> </DATA><br><br></RECORD> </EDITS><br><br></th>
  </tr>
</table>


在输出⽂件中，每个RECORD记录了⼀次操作，在该示例中执⾏的是删除操作。当edits⽂件破损进 ⽽导致hadop集群出现问题时，保存edits⽂件中正确的部分是可能的，可以通过将原有的bianry⽂件 转换为xml⽂件，并⼿动编辑xml⽂件然后转回bianry⽂件来实现。最常⻅的edits⽂件破损情况是丢失 关闭记录的部分（OPCODE为-1），关闭记录如下所示。如果在xml⽂件中没有关闭记录，可以在最后 正确的记录后⾯添加关闭记录，关闭记录后⾯的记录都将被忽略。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br></th>
    <th><RECORD> <OPCODE>- 1 </OPCODE> <DATA> </DATA><br><br></RECORD></th>
  </tr>
</table>


oiv是ofline image viewer的缩写，⽤于将fsimage⽂件的内容转储到指定⽂件中以便于阅读，该⼯ 具还提供了只读的WebHDFS API以允许离线分析和检查hadop集群的命名空间。oiv在处理⾮常⼤的 fsimage⽂件时是相当快的，如果该⼯具不能够处理fsimage，它会直接退出。该⼯具不具备向后兼容 性，⽐如使⽤hadop-2.4版本的oiv不能处理hadop-2.3版本的fsimage，只能使⽤hadop-2.3版本的 oiv。同oev⼀样，就像它的名称所提示的（ofline），oiv也不需要hadop集群处于运⾏状态。oiv具体 语法可以通过在命令⾏输⼊hdfs oiv查看。

oiv⽀持三种输出处理器，分别为Ls、XML和FileDistribution，通过选项-p指定。Ls是默认的处理 器，该处理器的输出与lsr命令的输出极其相似，以相同的顺序输出相同的字段，⽐如⽬录或⽂件的标 志、权限、副本数量、所有者、组、⽂件⼤⼩、修改⽇期和全路径等。与lsr不同的是，该处理器的输 出包含根路径/，另⼀个重要的不同是该处理器的输出不是按照⽬录名称和内容排序的，⽽是按照在 fsimage中的顺序显示。除⾮命名空间包含较少的信息，否则不太可能直接⽐较该处理器和lsr命令的输 出。Ls使⽤INode块中的信息计算⽂件⼤⼩并忽略-skipBlocks选项。示例如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br></th>
    <th>[hadoop @hadoop current]$ hdfs oiv -i fsimage_0000000000000000115 -o fsimage.ls [hadoop @hadoop current]$ cat fsimage.ls drwxr-xr-x - hadoop supergroup 14128326621620 / drwxr-xr-x - hadoop supergroup 14137950103720 /user drwxr-xr-x - hadoop supergroup 14140328488580 /user/hadoop drwxr-xr-x - hadoop supergroup 14116268812170 /user/hadoop/input drwxr-xr-x - hadoop supergroup 14137701389640 /user/hadoop/output<br><br></th>
  </tr>
</table>


# XML处理器输出fsimage的xml⽂档，包含了fsimage中的所有信息，⽐如inodeid等。该处理器的输 出⽀持XML⼯具的⾃动化处理和分析，由于XML语法格式的冗⻓，该处理器的输出也最⼤。示例如 下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br><br><br></th>
    <th>[hadoop @hadoop current]$ hdfs oiv -i<br><br>fsimage_0000000000000000115 -p XML -o fsimage.xml [hadoop @hadoop current]$ cat fsimage.xml <?xml version= "1.0" ?> <fsimage><br><br><NameSection> <genstampV1> 1000 </genstampV1> <genstampV2> 1004 </genstampV2> <genstampV1Limit> 0 </genstampV1Limit> <lastAllocatedBlockId> 1073741828 </lastAllocatedBloc<br><br>kId><br><br><txid> 115 </txid><br><br></NameSection> <INodeSection><br><br><lastInodeId> 16418 </lastInodeId> <inode><br><br><id> 16385 </id> <type>DIRECTORY</type> <name></name> <mtime> 1412832662162 </mtime> <permission>hadoop:supergroup:rwxr-xr-<br><br>x</permission><br><br><nsquota> 9223372036854775807 </nsquota> <dsquota>- 1 </dsquota><br><br></inode> <inode><br><br><id> 16386 </id> <type>DIRECTORY</type> <name>user</name> <mtime> 1413795010372 </mtime> <permission>hadoop:supergroup:rwxr-xr-<br><br>x</permission><br><br><nsquota>- 1 </nsquota> <dsquota>- 1 </dsquota><br><br></inode> </INodeSection><br><br></fsimage></th>
  </tr>
</table>


FileDistribution是分析命名空间中⽂件⼤⼩的⼯具。为了运⾏该⼯具需要通过指定最⼤⽂件⼤⼩和 段数定义⼀个整数范围[0,maxSize]，该整数范围根据段数分割为若⼲段[0, s[1], ., s[n-1], maxSize]， 处理器计算有多少⽂件落⼊每个段中（[s[i-1], s[i]），⼤于maxSize的⽂件总是落⼊最后的段中，即 s[n-1], maxSize。输出⽂件被格式化为由tab分隔的包含Size列和NumFiles列的表，其中Size表示段的 起始，NumFiles表示⽂件⼤⼩落⼊该段的⽂件数量。在使⽤FileDistribution处理器时还需要指定该处 理器的参数maxSize和step，若未指定默认为0。示例如下：

[hadoop @hadoop current]$ hdfs oiv -i fsimage_0000000000000000115 -o fs

FileDistribution maxSize 1000 step 5 [hadoop @hadoop current]$ cat fsimage.fd Processed 0 inodes. Size NumFiles 20971522 totalFiles = 2 totalDirectories = 11 totalBlocks = 2 totalSpace = 4112 maxFileSize = 1366

