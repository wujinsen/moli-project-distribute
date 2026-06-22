本⽂将介绍如何操作DataFrame⾥⾯的数据和打印出DataFrame⾥⾯数据的模式

# 打印DataFrame⾥⾯的模式

在创建完DataFrame之后，我们⼀般都会查看⾥⾯数据的模式，我们可以通过printSchema函数来查 看。它会打印出列的名称和类型：

students.printSchema

root

|-- id: string (nullable = true)

|-- studentName: string (nullable = true)

|-- phone: string (nullable = true)

|-- email: string (nullable = true)

如果采⽤的是load⽅式参⻅DataFrame的，students.printSchema的输出则如下：

root

|-- id|studentName|phone|email: string (nullable = true)

# 对DataFrame⾥⾯的数据进⾏采样

打印完模式之后，我们要做的第⼆件事就是看看加载进DataFrame⾥⾯的数据是否正确。从新创建的 DataFrame⾥⾯采样数据的⽅法有很多种。我们来对其进⾏介绍。

最简单的就是使⽤show⽅法，show⽅法有四个版本：

- （1）、第⼀个需要我们指定采样的⾏数def show(numRows: Int)；

- （2）、第⼆种不需要我们指定任何参数，这种情况下，show函数默认会加载出20⾏的数据def

show()；

- （3）、第三种需要指定⼀个bolean值，这个值说明是否需要对超过20个字符的列进⾏截取def

show(truncate: Boolean)；

- （4）、最后⼀种需要指定采样的⾏和是否需要对列进⾏截断def show(numRows: Int,


truncate: Boolean)。实际上，前三个函数都是调⽤这个函数实现的。

Show函数和其他函数不同的地⽅在于其不仅会显示需要打印的⾏，⽽且还会打印出头信息，并且会直接 在默认的输出流打出(console)。来看看怎么使⽤吧：

students.show() //打印出20⾏

+---+-----------+--------------+--------------------+

| id|studentName| phone| email|

+---+-----------+--------------+--------------------+

- | 1| Burke|1-300-746-8446|ullamcorper.velit...|

- | 2| Kamal|1-668-571-5046| |

- | 3| Olga|1-956-311-1686|Aenean.eget.metus...|

- | 4| Belle|1-246-894-6340|vitae.aliquet.nec...|

- | 5| Trevor|1-300-527-4967| |

- | 6| Laurel|1-691-379-9921| |

- | 7| Sara|1-608-140-1995| |

- | 8| Kaseem|1-881-586-2689| |

- | 9| Lev|1-916-367-5608| |

- | 10| Maya|1-271-683-2698|accumsan.convalli...|

- | 11| Emi|1-467-270-1337| |

- | 12| Caleb|1-683-212-0896| |

- | 13| Florence|1-603-575-2444| |

- | 14| Anika|1-856-828-7883| |

- | 15| Tarik|1-398-171-2268| |

- | 16| Amena|1-878-250-3129| |

- | 17| Blossom|1-154-406-9596|Nunc.commodo.auct...|

- | 18| Guy|1-869-521-3230|senectus.et.netus...|

- | 19| Malachi|1-608-637-2772| |

- | 20| Edward|1-711-710-6552| |


pede.Suspendisse@...

dapibus.id@acturp...

adipiscing@consec...

Donec.nibh@enimEt...

cursus.et.magna@e...

Vivamus.nisi@ipsu...

est@nunc.com

Suspendisse@Quisq...

sit.amet.dapibus@...

euismod@ligulaeli...

turpis@felisorci.com

lorem.luctus.ut@s...

Proin.mi.Aliquam@...

lectus@aliquetlib...

+---+-----------+--------------+--------------------+

only showing top 20 rows

students.show(15)

+---+-----------+--------------+--------------------+

| id|studentName| phone| email|

+---+-----------+--------------+--------------------+

- | 1| Burke|1-300-746-8446|ullamcorper.velit...|

- | 2| Kamal|1-668-571-5046| |

- | 3| Olga|1-956-311-1686|Aenean.eget.metus...|

- | 4| Belle|1-246-894-6340|vitae.aliquet.nec...|

- | 5| Trevor|1-300-527-4967| |

- | 6| Laurel|1-691-379-9921| |

- | 7| Sara|1-608-140-1995| |

- | 8| Kaseem|1-881-586-2689| |

- | 9| Lev|1-916-367-5608| |

- | 10| Maya|1-271-683-2698|accumsan.convalli...|

- | 11| Emi|1-467-270-1337| |

- | 12| Caleb|1-683-212-0896| |

- | 13| Florence|1-603-575-2444| |

- | 14| Anika|1-856-828-7883| |

- | 15| Tarik|1-398-171-2268| |


pede.Suspendisse@...

dapibus.id@acturp...

adipiscing@consec...

Donec.nibh@enimEt...

cursus.et.magna@e...

Vivamus.nisi@ipsu...

est@nunc.com

Suspendisse@Quisq...

sit.amet.dapibus@...

euismod@ligulaeli...

turpis@felisorci.com

+---+-----------+--------------+--------------------+

only showing top 15 rows

students.show(true)

+---+-----------+--------------+--------------------+

| id|studentName| phone| email|

+---+-----------+--------------+--------------------+

- | 1| Burke|1-300-746-8446|ullamcorper.velit...|


- | 2| Kamal|1-668-571-5046| |

- | 3| Olga|1-956-311-1686|Aenean.eget.metus...|

- | 4| Belle|1-246-894-6340|vitae.aliquet.nec...|

- | 5| Trevor|1-300-527-4967| |

- | 6| Laurel|1-691-379-9921| |

- | 7| Sara|1-608-140-1995| |

- | 8| Kaseem|1-881-586-2689| |

- | 9| Lev|1-916-367-5608| |

- | 10| Maya|1-271-683-2698|accumsan.convalli...|

- | 11| Emi|1-467-270-1337| |

- | 12| Caleb|1-683-212-0896| |

- | 13| Florence|1-603-575-2444| |

- | 14| Anika|1-856-828-7883| |

- | 15| Tarik|1-398-171-2268| |

- | 16| Amena|1-878-250-3129| |

- | 17| Blossom|1-154-406-9596|Nunc.commodo.auct...|

- | 18| Guy|1-869-521-3230|senectus.et.netus...|

- | 19| Malachi|1-608-637-2772| |

- | 20| Edward|1-711-710-6552| |


pede.Suspendisse@...

dapibus.id@acturp...

adipiscing@consec...

Donec.nibh@enimEt...

cursus.et.magna@e...

Vivamus.nisi@ipsu...

est@nunc.com

Suspendisse@Quisq...

sit.amet.dapibus@...

euismod@ligulaeli...

turpis@felisorci.com

lorem.luctus.ut@s...

Proin.mi.Aliquam@...

lectus@aliquetlib...

+---+-----------+--------------+--------------------+

only showing top 20 rows

students.show(false)

+---+-----------+--------------+-----------------------------------------+

|id |studentName|phone |email |

+---+-----------+--------------+-----------------------------------------+

- |1 |Burke |1-300-746-8446| |

- |2 |Kamal |1-668-571-5046| |

- |3 |Olga |1-956-311-1686| |

- |4 |Belle |1-246-894-6340| |

- |5 |Trevor |1-300-527-4967| |

- |6 |Laurel |1-691-379-9921| |

- |7 |Sara |1-608-140-1995| |

- |8 |Kaseem |1-881-586-2689| |

- |9 |Lev |1-916-367-5608| |

- |10 |Maya |1-271-683-2698| |

- |11 |Emi |1-467-270-1337| |

- |12 |Caleb |1-683-212-0896| |

- |13 |Florence |1-603-575-2444| |

- |14 |Anika |1-856-828-7883| |

- |15 |Tarik |1-398-171-2268| |

- |16 |Amena |1-878-250-3129| |

- |17 |Blossom |1-154-406-9596| |

- |18 |Guy |1-869-521-3230| |

- |19 |Malachi |1-608-637-2772| |

- |20 |Edward |1-711-710-6552| |


ullamcorper.velit.in@ametnullaDonec.co.uk

pede.Suspendisse@interdumenim.edu

Aenean.eget.metus@dictumcursusNunc.edu

vitae.aliquet.nec@neque.co.uk

dapibus.id@acturpisegestas.net

adipiscing@consectetueripsum.edu

Donec.nibh@enimEtiamimperdiet.edu

cursus.et.magna@euismod.org

Vivamus.nisi@ipsumdolor.com

accumsan.convallis@ornarelectusjusto.edu

est@nunc.com

Suspendisse@Quisque.edu

sit.amet.dapibus@lacusAliquamrutrum.ca

euismod@ligulaelit.co.uk

turpis@felisorci.com

lorem.luctus.ut@scelerisque.com

Nunc.commodo.auctor@eratSed.co.uk

senectus.et.netus@lectusrutrum.com

Proin.mi.Aliquam@estarcu.net

lectus@aliquetlibero.co.uk

+---+-----------+--------------+-----------------------------------------+

only showing top 20 rows

students.show(10,false)

+---+-----------+--------------+-----------------------------------------+

|id |studentName|phone |email |

+---+-----------+--------------+-----------------------------------------+

- |1 |Burke |1-300-746-8446| |

- |2 |Kamal |1-668-571-5046| |

- |3 |Olga |1-956-311-1686| |

- |4 |Belle |1-246-894-6340| |

- |5 |Trevor |1-300-527-4967| |

- |6 |Laurel |1-691-379-9921| |

- |7 |Sara |1-608-140-1995| |

- |8 |Kaseem |1-881-586-2689| |

- |9 |Lev |1-916-367-5608| |

- |10 |Maya |1-271-683-2698| |


ullamcorper.velit.in@ametnullaDonec.co.uk

pede.Suspendisse@interdumenim.edu

Aenean.eget.metus@dictumcursusNunc.edu

vitae.aliquet.nec@neque.co.uk

dapibus.id@acturpisegestas.net

adipiscing@consectetueripsum.edu

Donec.nibh@enimEtiamimperdiet.edu

cursus.et.magna@euismod.org

Vivamus.nisi@ipsumdolor.com

accumsan.convallis@ornarelectusjusto.edu

+---+-----------+--------------+-----------------------------------------+

only showing top 10 rows

我们还可以使⽤head(n: Int)⽅法来采样数据，这个函数也需要输⼊⼀个参数标明需要采样的⾏数，⽽且 这个函数返回的是Row数组，我们需要遍历打印。当然，我们也可以使⽤head()函数直接打印，这个函数只 是返回数据的⼀⾏，类型也是Row。

students.head(5).foreach(println)

- [1,Burke,1-300-746-8446, ]

- [2,Kamal,1-668-571-5046, ]

- [3,Olga,1-956-311-1686, ]

- [4,Belle,1-246-894-6340, ]

- [5,Trevor,1-300-527-4967, ]


ullamcorper.velit.in@ametnullaDonec.co.uk

pede.Suspendisse@interdumenim.edu

Aenean.eget.metus@dictumcursusNunc.edu

vitae.aliquet.nec@neque.co.uk

dapibus.id@acturpisegestas.net

println(students.head())

[1,Burke,1-300-746-8446, ]

ullamcorper.velit.in@ametnullaDonec.co.uk

除了show、head函数。我们还可以使⽤first和take函数，他们分别调⽤head()和head(n)

println(students.first())

[1,Burke,1-300-746-8446, ]

ullamcorper.velit.in@ametnullaDonec.co.uk

students.take(5).foreach(println)

- [1,Burke,1-300-746-8446, ]

- [2,Kamal,1-668-571-5046, ]

- [3,Olga,1-956-311-1686, ]

- [4,Belle,1-246-894-6340, ]

- [5,Trevor,1-300-527-4967, ]


ullamcorper.velit.in@ametnullaDonec.co.uk

pede.Suspendisse@interdumenim.edu

Aenean.eget.metus@dictumcursusNunc.edu

vitae.aliquet.nec@neque.co.uk

dapibus.id@acturpisegestas.net

# 查询DataFrame⾥⾯的列

正如你所看到的，所有的DataFrame⾥⾯的列都是有名称的。Select函数可以帮助我们从DataFrame中 选择需要的列，并且返回⼀个全新的DataFrame，下⾯我将此进⾏介绍。

1、只选择⼀列。假如我们只想从DataFrame中选择email这列，因为DataFrame是不可变的 (i mutable)，所以这个操作会返回⼀个新的DataFrame： 1 val emailDataFrame: DataFrame = students.select("email") 现在我们有⼀个名叫emailDataFrame全新的DataFrame，⽽且其中只包含了email这列，让我们使⽤ show来看看是否是这样的： emailDataFrame.show(3)

+ -+ | email|

+ -+ |ulamcorper.velit.| | | |Aenean.eget.metus.|

pede.Suspendise@.

+ -+ only showing top 3 rows

2、选择多列。其实select函数⽀持选择多列。 val studentEmailDF = students.select("studentName", "email") studentEmailDF.show(5)

+ -+ -+ |studentName| email|

+ -+ -+ | Burke|ulamcorper.velit.| | Kamal| | | Olga|Aenean.eget.metus.| | Bele|vitae.aliquet.nec.| | Trevor| |

pede.Suspendise@.

dapibus.id@acturp.

+ -+ -+ only showing top 5 rows

需要主要的是，我们select列的时候，需要保证select的列是有效的，换句话说，就是必须保证 select的列是printSchema打印出来的。如果列的名称是⽆效的，将会出现 org.apache.spark.sql.AnalysisException异常，如下：

- 1 val studentEmailDF = students.select("studentName", "iteblog")
- 2 studentEmailDF.show(5)
- 3
- 4 Exception in thread "main" org.apache.spark.sql.AnalysisException: canot resolve 'iteblog'giv en input columns id, studentName, phone, email; 根据条件过滤数据


现在我们已经知道如何在DataFrame中选择需要的列，让我们来看看如何根据条件来过滤 DataFrame⾥⾯的数据。对应基于Row的数据，我们可以将DataFrame看作是普通的Scala集合，然后 我们根据需要的条件进⾏相关的过滤，为了展示清楚，我在语句没后⾯都⽤show函数展示过滤的结 果。 students.filter("id > 5").show(7)

+-+ -+ -+ -+ | id|studentName| phone| email|

- | 6| Laurel|1-691-379-921| |
- | 7| Sara|1-608-140-195| |
- | 8| Kasem|1-81-586-2689| |
- | 9| Lev|1-916-367-5608| |
- | 10| Maya|1-271-683-2698|acumsan.convali .| | 1| Emi|1-467-270-137| |


adipiscing@consec. Donec.nibh@enimEt.

cursus.et.magna@e. Vivamus.nisi@ipsu.

est@nunc.com Suspendise@Quisq. sit.amet.dapibus@. euismod@ligulaeli .

- | 12| Caleb|1-683-212-0896| |
- | 13| Florence|1-603-575-2 4| |
- | 14| Anika|1-856-828-783| |
- | 15| Tarik|1-398-171-268| |


turpis@felisorci.com

+-+ -+ -+ -+ only showing top 10 rows

students.filter("studentName ='").show(7)

+-+ -+ -+ -+ | id|studentName| phone| email|

+-+ -+ -+ -+

- | 21| |1-598-439-7549|consectetuer.adip.| | 32| |1-184-895-9602| | | 45| |1-245-752-0481|Suspendise.eleif.| | 83| |1-858-810-204| | | 94| |1-43-410-7878|Praesent.eu.nula.|


acumsan.laoret@.

socis.natoque@eu.

+-+ -+ -+ -+ ∽⒁饪吹谝桓龉 擞锞洌 淙籭d被解析成String了，但是程序依然正确地做出了⽐较。我们也可以对 多个条件进⾏过滤： students.filter("studentName =' OR studentName = 'NUL'").show(7)

+-+ -+ -+ -+ | id|studentName| phone| email|

+-+ -+ -+ -+

- | 21| |1-598-439-7549|consectetuer.adip.| | 32| |1-184-895-9602| |


acumsan.laoret@. Donec@Inmipede.co.uk

- | 3| NUL|1-105-503-0141| | | 45| |1-245-752-0481|Suspendise.eleif.| | 83| |1-858-810-204| | | 94| |1-43-410-7878|Praesent.eu.nula.|


socis.natoque@eu.

我们还可以采⽤类SQL的语法对数据进⾏过滤： students.filter("SUBSTR(studentName,0,1) ='M'").show(7)

+-+ -+ -+ -+ | id|studentName| phone| email|

+-+ -+ -+ -+ | 10| Maya|1-271-683-2698|acumsan.convali .| | 19| Malachi|1-608-637-272| | | 24| Marsden|1-47-629-7528|Donec.dignisim.m.| | 37| Magy|1-910-87-6 7|facilisi.Sed.nequ.| | 61| Maxine|1-42-863-3041|aliquet.molestie.| | 7| Magy|1-613-147-4380| | | 97| Maxwel|1-607-205-1273| | +-+ -+ -+ -+ only showing top 7 rows 对DataFrame⾥⾯的数据进⾏排序

Proin.mi.Aliquam@.

pelentesque@mi.net metus.In@musAenea.

使⽤sort函数我们可以对DataFrame中指定的列进⾏排序： students.sort(students("studentName").desc).show(7) +-+ -+ -+ -+ | id|studentName| phone| email|

+-+ -+ -+ -+ | 50| Yasir|1-282-51- 45|eget.odio.Aliquam.|

- | 52| Xena|1-527-90-8606| |


in.faucibus.orci@. libero@arcuVestib. amet.risus.Donec@. lorem.lorem@non.net pelentesque@netu. non.bibendum.sed@.

- | 86| Xandra|1-67-708-5691| | | 43| Wynter|1-40-54-1851| |


- | 31| Walace|1-14-20-8159| | | 6| Vance|1-268-680-0857| | | 41| Tyrone|1-907-383-5293| |


+-+ -+ -+ -+ only showing top 7 rows 也可以对多列进⾏排序： students.sort("studentName", "id").show(10)

+-+ -+ -+ -+ | id|studentName| phone| email|

+-+ -+ -+ -+ | 21| |1-598-439-7549|consectetuer.adip.|

- | 32| |1-184-895-9602| | | 45| |1-245-752-0481|Suspendise.eleif.| | 83| |1-858-810-204| | | 94| |1-43-410-7878|Praesent.eu.nula.| | 91| Abel|1-530-527-7467| | | 69| Aiko|1-682-230-7013|turpis.vitae.puru.| | 47| Alma|1-747-382-675| | | 26| Amela|1-526-909-2605| |


acumsan.laoret@.

socis.natoque@eu.

urna@veliteu.edu

nec.enim@non.org in@vitaesodales.edu lorem.luctus.ut@s.

- | 16| Amena|1-878-250-3129| |


+-+ -+ -+ -+ only showing top 10 rows 从上⾯的结果我们可以看出，默认是按照升序进⾏排序的。我们也可以将上⾯的语句写成下⾯的： 1 students.sort(students("studentName").asc, students("id").asc).show(10)

对列进⾏重命名这两个语句运⾏的效果是⼀致的。

如果我们对DataFrame中默认的列名不感兴趣，我们可以在select的时候利⽤as对其进⾏重命名，下⾯ 的列⼦将studentName重命名为name，⽽email这列名字不变： students.select(students("studentName").as("name"), students("email").show(10)

+ -+ -+ | name| email|

+ -+ -+ | Burke|ulamcorper.velit.| | Kamal| | | Olga|Aenean.eget.metus.| | Bele|vitae.aliquet.nec.| | Trevor| | | Laurel| | | Sara| | | Kasem| | | Lev| | | Maya|acumsan.convali .|

pede.Suspendise@.

dapibus.id@acturp. adipiscing@consec. Donec.nibh@enimEt.

cursus.et.magna@e. Vivamus.nisi@ipsu.

+ -+ -+ only showing top 10 rows 将DataFrame看作是关系型数据表

DataFrame的⼀个强⼤之处就是我们可以将它看作是⼀个关系型数据表，然后在其上运⾏SQL查 询语句，只要我们进⾏下⾯两步即可实现：

（1）、将DataFrame注册成⼀张名为students的表： students.registerTempTable("students") （2）、然后我们在其上⽤标准的SQL进⾏查询： sqlContext.sql("select * from students where studentName!=' order by email desc").show(7)

+-+ -+ -+ -+ | id|studentName| phone| email|

+-+ -+ -+ -+

- | 87| Selma|1-601-30-409| | | 96| Chaning|1-984-18-753|vivera.Donec.tem.|


vulputate.velit@p.

- | 4| Bele|1-246-894-6340|vitae.aliquet.nec.| | 78| Fi n|1-213-781-6969| |


vestibulum.masa@.

- | 53| Kasper|1-15-575-9346| | | 63| Dylan|1-417-943-8961| | | 35| Cadman|1-43-642-5919| |


velit.eget@pedeCu. vehicula.aliquet@.

ut.lacus@adipisci .

+-+ -+ -+ -+ only showing top 7 rows 对两个DataFrame进⾏Join操作

前⾯我们已经知道如何将DataFrame注册成⼀张表，现在我们来看看如何使⽤普通的SQL对两个 DataFrame进⾏Join操作。

- 1、内联：内联是默认的Join操作，它仅仅返回两个DataFrame都匹配到的结果，来看看下⾯的例⼦：


- val students1 = sqlContext.csvFile(filePath = "E:\StudentPrep1.csv", useHeader =true, delimiter = '| ')
- val students2 = sqlContext.csvFile(filePath = "E:\StudentPrep2.csv", useHeader =true, delimiter = ' |') val studentsJoin = students1.join(students2, students1("id") = students2("id") studentsJoin.show(studentsJoin.count.toInt)


+-+ -+ -+ -+-+ -+ -+ -

-+ | id|studentName| phone| email| id| studentName| phone| email|

+-+ -+ -+ -+-+ -+ -+ -

-+

- | 1| Burke|1-30-746-846|ulamcorper.velit.| 1|BurkeDiferentName|1-30-746846|ulamcorper.velit.|
- | 2| Kamal|1-68-571-5046| | 2|KamalDiferentName|1-68-571-5046| |
- | 3| Olga|1-956-31-1686|Aenean.eget.metus.| 3| Olga|1-956-311686|Aenean.eget.metus.|
- | 4| Bele|1-246-894-6340|vitae.aliquet.nec.| 4|BeleDiferentName|1-246-8946340|vitae.aliquet.nec.|
- | 5| Trevor|1-30-527-4967| | 5| Trevor|1-30-5274967|dapibusDiferentE.|
- | 6| Laurel|1-691-379-921| | 6|LaurelInvalidPhone| 0| |
- | 7| Sara|1-608-140-195| | 7| Sara|1-608-140-195| |
- | 8| Kasem|1-81-586-2689| | 8| Kasem|1-81-586-2689| |
- | 9| Lev|1-916-367-5608| | 9| Lev|1-916-367-5608| |
- | 10| Maya|1-271-683-2698|acumsan.convali .| 10| Maya|1-271-6832698|acumsan.convali .|


pede.Suspendise@. pe de.Suspendise@.

dapibus.id@acturp.

adipiscing@consec. adipiscin g@consec.

Donec.nibh@enimEt. Donec.nibh @enimEt.

cursus.et.magna@e. cursus. et.magna@e.

Vivamus.nisi@ipsu. Vivamus.nisi@i psu.

+-+ -+ -+ -+-+ -+ -+ -

-+

- 2、右外联：在内连接的基础上，还包含右表中所有不符合条件的数据⾏，并在其中的左表列填写 NUL ，来看看下⾯的实例： val studentsRightOuterJoin = students1.join(students2, students1("id") =students2("id"), "right_ outer") studentsRightOuterJoin.show(studentsRightOuterJoin.count.toInt)


+ -+ -+ -+ -+-+ -+ -+-

-+ | id|studentName| phone| email| id| studentName| phone| email|

+ -+ -+ -+ -+-+ -+ -+-

-+

- | 1| Burke|1-30-746-846|ulamcorper.velit.| 1| BurkeDiferentName|1-30-746846|ulamcorper.velit.|


- | 2| Kamal|1-68-571-5046| | 2| KamalDiferentName|1-68-571-5046| |
- | 3| Olga|1-956-31-1686|Aenean.eget.metus.| 3| Olga|1-956-311686|Aenean.eget.metus.|
- | 4| Bele|1-246-894-6340|vitae.aliquet.nec.| 4| BeleDiferentName|1-246-8946340|vitae.aliquet.nec.|
- | 5| Trevor|1-30-527-4967| | 5| Trevor|1-30-5274967|dapibusDiferentE.|
- | 6| Laurel|1-691-379-921| | 6| LaurelInvalidPhone| 0| |
- | 7| Sara|1-608-140-195| | 7| Sara|1-608-140-195| |
- | 8| Kasem|1-81-586-2689| | 8| Kasem|1-81-586-2689| |
- | 9| Lev|1-916-367-5608| | 9| Lev|1-916-367-5608| |
- | 10| Maya|1-271-683-2698|acumsan.convali .| 10| Maya|1-271-6832698|acumsan.convali .| |nul| nul| nul| nul| 9|LevUniqueToSecondRD|1-916-367-5608|


pede.Suspendise@. p ede.Suspendise@.

dapibus.id@acturp.

adipiscing@consec. adipisc ing@consec.

Donec.nibh@enimEt. Donec.nibh @enimEt.

cursus.et.magna@e. cursu s.et.magna@e.

Vivamus.nisi@ipsu. Vivamus.nisi @ipsu.

Vivamus.nisi@i psu.

|

+ -+ -+ -+ -+-+ -+ -+-

-+

- 3、左外联：在内连接的基础上，还包含左表中所有不符合条件的数据⾏，并在其中的右表列填写 NUL ，同样我们来看看下⾯的实例： val studentsLeftOuterJoin = students1.join(students2, students1("id") =students2("id"), "left_out er") studentsLeftOuterJoin.show(studentsLeftOuterJoin.count.toInt)


+-+ -+ -+ -+ -+ -+ -+ -

-+ | id|studentName| phone| email| id| studentName| phone| email|

+-+ -+ -+ -+ -+ -+ -+ -

-+

- | 1| Burke|1-30-746-846|ulamcorper.velit.| 1|BurkeDiferentName|1-30-746846|ulamcorper.velit.|
- | 2| Kamal|1-68-571-5046| | 2|KamalDiferentName|1-68-571-5046| |


pede.Suspendise@. pe de.Suspendise@.

- | 3| Olga|1-956-31-1686|Aenean.eget.metus.| 3| Olga|1-956-311686|Aenean.eget.metus.|
- | 4| Bele|1-246-894-6340|vitae.aliquet.nec.| 4|BeleDiferentName|1-246-8946340|vitae.aliquet.nec.|
- | 5| Trevor|1-30-527-4967| | 5| Trevor|1-30-5274967|dapibusDiferentE.|
- | 6| Laurel|1-691-379-921| | 6|LaurelInvalidPhone| 0| |
- | 7| Sara|1-608-140-195| | 7| Sara|1-608-140-195| |
- | 8| Kasem|1-81-586-2689| | 8| Kasem|1-81-586-2689| |
- | 9| Lev|1-916-367-5608| | 9| Lev|1-916-367-5608| |
- | 10| Maya|1-271-683-2698|acumsan.convali .| 10| Maya|1-271-6832698|acumsan.convali .| | 1| iteblog| 9| |nul| nul| nul| nul|


dapibus.id@acturp.

adipiscing@consec. adipisci ng@consec.

Donec.nibh@enimEt. Donec.nibh @enimEt.

cursus.et.magna@e. cursus. et.magna@e.

Vivamus.nisi@ipsu. Vivamus.nisi@ ipsu.

iteblog@iteblog.com

+-+ -+ -+ -+ -+ -+ -+ -

-+

下⾯我来介绍如何将DataFrame保存到⼀个⽂件⾥⾯。前⾯我们加载csv⽂件⽤到了load函数，与之对 于的⽤于保存⽂件可以使⽤save函数。具体操作包括以下两步：将DataFrame保存成⽂件

1、⾸先创建⼀个map对象，⽤于存储⼀些save函数需要⽤到的⼀些属性。这⾥我将制定保存⽂件的 存放路径和csv的头信息。 val saveOptions = Map("header" -> "true", "path" -> "iteblog.csv")

为了基于学习的态度，我们从DataFrame⾥⾯选择出studentName和email两列，并且将 studentName的列名重定义为name。 val copyOfStudents = students.select(students("studentName").as("name"), students("email")

2、下⾯我们调⽤save函数保存上⾯的DataFrame数据到iteblog.csv⽂件夹中 copyOfStudents.write.format("com.databricks.spark.csv").mode(SaveMode.Overwrite).options(sav eOptions).save()

## mode函数可以接收的参数有Overwrite、Apend、Ignore和ErorIfExists。从名字就可以很好的理解， Overwrite代表覆盖⽬录下之前存在的数据；Apend代表给指定⽬录下追加数据；Ignore代表如果⽬录下已 经有⽂件，那就什么都不执⾏；ErorIfExists代表如果保存⽬录下存在⽂件，那么抛出相应的异常。

