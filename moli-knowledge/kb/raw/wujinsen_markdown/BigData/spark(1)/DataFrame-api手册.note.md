Action操作 defcolect(): Aray[Row]

Returns an aray that contains al of s in this .

Row DataFrame defcolectAsList(): List[Row]

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


Returns a Java list that contains al of s in this .

Row DataFrame defcount(): Long

Returns the number of rows in the .

DataFrame defdescribe(cols: String*): DataFrame

Computes statistics for numeric columns, including count, mean, stdev, min, and max.

deffirst(): Row

Returns the first row.

defhead(): Row

Returns the first row.

defhead(n: Int): Aray[Row]

Returns the first n rows.

defshow(numRows: Int, truncate: Bolean): Unit DataFrame defshow(truncate: Bolean): Unit

Displays the in a tabular form.

Displays the top 20 rows of in a tabular form.

DataFrame defshow(): Unit

Displays the top 20 rows of in a tabular form.

DataFrame defshow(numRows: Int): Unit

Displays the in a tabular form.

DataFrame deftake(n: Int): Aray[Row]

Returns the first n rows in the .

DataFrame deftakeAsList(n: Int): List[Row]

Returns the first n rows in the as a list.

DataFrame

# Basic DataFrame functions

- 1.
- 2.
- 3.
- 4.


Experimentaldefas[U](implicit arg0: Encoder[U]): Dataset[U]

Converts this to a strongly-typed containing objects of the specified type, U .

DataFrame Dataset defcache(): DataFrame.this.type

Persist this with the default storage level ( MEMORY_AND_DISK ).

DataFrame defcolumns: Aray[String]

Returns al column names as an aray.

defdtypes: Aray[(String, String)]

Returns al column names and their data types as an aray.

- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.


defexplain(extended: Bolean): Unit

Prints the plans (logical and physical) to the console for debuging purposes.

defisLocal: Bolean

Returns true if the collect and take methods can be run localy (without any Spark executors).

defpersist(newLevel: StorageLevel): DataFrame.this.type DataFrame defpersist(): DataFrame.this.type DataFrame defprintSchema(): Unit

Persist this with the given storage level.

Persist this with the default storage level ( MEMORY_AND_DISK ).

Prints the schema to the console in a nice tre format.

defregisterTempTable(tableName: String): Unit DataFrame defschema: StructType DataFrame deftoDF(colNames: String*): DataFrame DataFrame deftoDF(): DataFrame

Registers this as a temporary table using the given name.

Returns the schema of this .

Returns a new with columns renamed.

Returns the object itself.

defunpersist(): DataFrame.this.type DataFrame defunpersist(blocking: Bolean): DataFrame.this.type DataFrame

Mark the as non-persistent, and remove al blocks for it from memory and disk.

Mark the as non-persistent, and remove al blocks for it from memory and disk.

# Language Integrated Queries

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


defag(expr: Column, exprs: Column*): DataFrame DataFrame defag(exprs: Map[String, String]): DataFrame

Agregates on the entire without groups.

(Java-specific) Agregates on the entire without groups.

DataFrame defag(exprs: Map[String, String]): DataFrame

(Scala-specific) Agregates on the entire without groups.

DataFrame defag(agExpr: (String, String), agExprs: (String, String)*): DataFrame

(Scala-specific) Agregates on the entire without groups.

DataFrame defalias(alias: Symbol): DataFrame

(Scala-specific) Returns a new with an alias set.

DataFrame defalias(alias: String): DataFrame

Returns a new with an alias set.

DataFrame defaply(colName: String): Column

- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.


Selects column based on the column name and return it as a .

Column defas(alias: Symbol): DataFrame

(Scala-specific) Returns a new with an alias set.

DataFrame defas(alias: String): DataFrame DataFrame

Returns a new with an alias set.

defcol(colName: String): Column

Selects column based on the column name and return it as a .

Column defcube(col1: String, cols: String*): GroupedData

Create a multi-dimensional cube for the curent using the specified columns, so we can run agregation on them.

DataFrame

defcube(cols: Column*): GroupedData

Create a multi-dimensional cube for the curent using the specified columns, so we can run agregation on them.

DataFrame

defdistinct(): DataFrame

Returns a new that contains only the unique rows from this .

DataFrame DataFrame defdrop(col: Column): DataFrame

Returns a new with a column droped.

DataFrame defdrop(colName: String): DataFrame DataFrame defdropDuplicates(colNames: Aray[String]): DataFrame DataFrame defdropDuplicates(colNames: Seq[String]): DataFrame DataFrame

Returns a new with a column droped.

Returns a new with duplicate rows removed, considering only the subset of columns.

(Scala-specific) Returns a new with duplicate rows removed, considering only the subset of columns.

defdropDuplicates(): DataFrame

Returns a new that contains only the unique rows from this .

DataFrame DataFrame defexcept(other: DataFrame): DataFrame

Returns a new containing rows in this frame but not in another frame.

DataFrame defexplode[A, B](inputColumn: String, outputColumn: String)(f: (A) ⇒ TraversableOnce[B]) (implicit arg0: scala.reflect.api.JavaUniverse.TypeTag[B]): DataFrame

(Scala-specific) Returns a new where a single column has ben expanded to zero or more rows by the provided function.

DataFrame

defexplode[A <: Product](input: Column*)(f: (Row) ⇒ TraversableOnce[A]) (implicit arg0: scala.reflect.api.JavaUniverse.TypeTag[A]): DataFrame

(Scala-specific) Returns a new where each row has ben expanded to zero or more rows by the provided function.

DataFrame

- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.


deffilter(conditionExpr: String): DataFrame

Filters rows using the given SQL expresion.

deffilter(condition: Column): DataFrame

Filters rows using the given condition.

defgroupBy(col1: String, cols: String*): GroupedData DataFrame defgroupBy(cols: Column*): GroupedData DataFrame defintersect(other: DataFrame): DataFrame DataFrame defjoin(right: DataFrame, joinExprs: Column, joinType: String): DataFrame DataFrame defjoin(right: DataFrame, joinExprs: Column): DataFrame DataFrame defjoin(right: DataFrame, usingColumns: Seq[String], joinType: String): DataFrame DataFrame defjoin(right: DataFrame, usingColumns: Seq[String]): DataFrame DataFrame defjoin(right: DataFrame, usingColumn: String): DataFrame DataFrame defjoin(right: DataFrame): DataFrame

Groups the using the specified columns, so we can run agregation on them.

Groups the using the specified columns, so we can run agregation on them.

Returns a new containing rows only in both this frame and another frame.

Join with another , using the given join expresion.

I ner join with another , using the given join expresion.

Equi-join with another using the given columns.

I ner equi-join with another using the given columns.

I ner equi-join with another using the given column.

Cartesian join with another .

DataFrame deflimit(n: Int): DataFrame

Returns a new by taking the first n rows.

DataFrame defna: DataFrameNaFunctions DataFrameNaFunctions deforderBy(sortExprs: Column*): DataFrame DataFrame deforderBy(sortCol: String, sortCols: String*): DataFrame DataFrame defrandomSplit(weights: Aray[Double]): Aray[DataFrame] DataFrame defrandomSplit(weights: Aray[Double], sed: Long): Aray[DataFrame] DataFrame defrepartition(partitionExprs: Column*): DataFrame

Returns a for working with mising data.

Returns a new sorted by the given expresions.

Returns a new sorted by the given expresions.

Randomly splits this with the provided weights.

Randomly splits this with the provided weights.

Returns a new partitioned by the given partitioning expresions preserving the existing number of partitions.

DataFrame

- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.


defrepartition(numPartitions: Int, partitionExprs: Column*): DataFrame DataFrame

Returns a new partitioned by the given partitioning expresions into numPartitions .

defrepartition(numPartitions: Int): DataFrame DataFrame defrolup(col1: String, cols: String*): GroupedData DataFrame

Returns a new that has exactly numPartitions partitions.

Create a multi-dimensional rolup for the curent using the specified columns, so we can run agregation on them.

defrolup(cols: Column*): GroupedData

Create a multi-dimensional rolup for the curent using the specified columns, so we can run agregation on them.

DataFrame

defsample(withReplacement: Bolean, fraction: Double): DataFrame DataFrame defsample(withReplacement: Bolean, fraction: Double, sed: Long): DataFrame DataFrame defselect(col: String, cols: String*): DataFrame

Returns a new by sampling a fraction of rows, using a random sed.

Returns a new by sampling a fraction of rows.

Selects a set of columns.

defselect(cols: Column*): DataFrame

Selects a set of column based expresions.

defselectExpr(exprs: String*): DataFrame

Selects a set of SQL expresions.

defsort(sortExprs: Column*): DataFrame DataFrame defsort(sortCol: String, sortCols: String*): DataFrame DataFrame defsortWithinPartitions(sortExprs: Column*): DataFrame DataFrame defsortWithinPartitions(sortCol: String, sortCols: String*): DataFrame DataFrame defstat: DataFrameStatFunctions DataFrameStatFunctions defunionAl(other: DataFrame): DataFrame DataFrame defwhere(conditionExpr: String): DataFrame

Returns a new sorted by the given expresions.

Returns a new sorted by the specified column, al in ascending order.

Returns a new with each partition sorted by the given expresions.

Returns a new with each partition sorted by the given expresions.

Returns a for working statistic functions suport.

Returns a new containing union of rows in this frame and another frame.

Filters rows using the given SQL expresion.

- 56.
- 57.
- 58.


defwhere(condition: Column): DataFrame

Filters rows using the given condition.

defwithColumn(colName: String, col: Column): DataFrame DataFrame

Returns a new by ading a column or replacing the existing column that has the same name.

defwithColumnRenamed(existingName: String, newName: String): DataFrame DataFrame

Returns a new with a column renamed.

# Output Operations

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


Experimentaldefwrite: DataFrameWriter

Interface for saving the content of the out into external storage.

DataFrame defcreateJDBCTable(url: String, table: String, alowExisting: Bolean): Unit DataFrame definsertInto(tableName: String): Unit

Save this to a JDBC database at url under the table name table .

Ads the rows from this RD to the specified table.

definsertInto(tableName: String, overwrite: Bolean): Unit

Ads the rows from this RD to the specified table, optionaly overwriting the existing data.

definsertIntoJDBC(url: String, table: String, overwrite: Bolean): Unit DataFrame defsave(source: String, mode: SaveMode, options: Map[String, String]): Unit

Save this to a JDBC database at url under the table name table .

(Scala-specific) Saves the contents of this DataFrame based on the given data source, specified by mode, and a set of options

SaveMode defsave(source: String, mode: SaveMode, options: Map[String, String]): Unit SaveMode

Saves the contents of this DataFrame based on the given data source, specified by mode, and a set of options.

defsave(path: String, source: String, mode: SaveMode): Unit

Saves the contents of this DataFrame to the given path based on the given data source and specified by mode.

SaveM ode

defsave(path: String, source: String): Unit

Saves the contents of this DataFrame to the given path based on the given data source, using SaveMode.ErorIfExists as the save mode.

defsave(path: String, mode: SaveMode): Unit

Saves the contents of this DataFrame to the given path and specified by mode, using the default data source configured by spark.

SaveMode

defsave(path: String): Unit

Saves the contents of this DataFrame to the given path, using the default data source configured by spark.

- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


defsaveAsParquetFile(path: String): Unit

Saves the contents of this as a parquet file, preserving the schema.

DataFrame defsaveAsTable(tableName: String, source: String, mode: SaveMode, options: Map[String, Stri ng]): Unit

(Scala-specific) Creates a table from the the contents of this DataFrame based on a given data source, specified by mode, and a set of options.

SaveMode defsaveAsTable(tableName: String, source: String, mode: SaveMode, options: Map[String, Stri ng]): Unit

Creates a table at the given path from the the contents of this DataFrame based on a given data source, specified by mode, and a set of options.

SaveMode

ExperimentaldefsaveAsTable(tableName: String, source: String, mode: SaveMode): Unit

Creates a table at the given path from the the contents of this DataFrame based on a given data source, specified by mode, and a set of options.

SaveMode defsaveAsTable(tableName: String, source: String): Unit

Creates a table at the given path from the the contents of this DataFrame based on a given data source and a set of options, using SaveMode.ErorIfExists as the save mode.

defsaveAsTable(tableName: String, mode: SaveMode): Unit

Creates a table from the the contents of this DataFrame, using the default data source configured by spark.

defsaveAsTable(tableName: String): Unit

Creates a table from the the contents of this DataFrame.

# RD Operations

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


defcoalesce(numPartitions: Int): DataFrame DataFrame defflatMap[R](f: (Row) ⇒ TraversableOnce[R])(implicit arg0: ClasTag[R]): RD[R] DataFrame

Returns a new that has exactly numPartitions partitions.

Returns a new RD by first aplying a function to al rows of this , and then flatening the results.

defforeach(f: (Row) ⇒ Unit): Unit

Aplies a function f to al rows.

defforeachPartition(f: (Iterator[Row]) ⇒ Unit): Unit

Aplies a function f to each partition of this .

DataFrame defjavaRD: JavaRD[Row]

Returns the content of the as a JavaRD of s.

DataFrame Row defmap[R](f: (Row) ⇒ R)(implicit arg0: ClasTag[R]): RD[R]

Returns a new RD by aplying a function to al rows of this DataFrame.

defmapPartitions[R](f: (Iterator[Row]) ⇒ Iterator[R])(implicit arg0: ClasTag[R]): RD[R]

Returns a new RD by aplying a function to each partition of this DataFrame.

- 8.
- 9.
- 10.


lazy valrd: RD[Row]

Represents the content of the as an RD of s.

DataFrame Row deftoJSON: RD[String]

Returns the content of the as a RD of JSON strings.

DataFrame deftoJavaRD: JavaRD[Row]

Returns the content of the as a JavaRD of s.

DataFrame Row

# Ungrouped

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


defexplain(): Unit

Prints the physical plan to the console for debuging purposes.

definputFiles: Aray[String]

Returns a best-efort snapshot of the files that compose this DataFrame.

valqueryExecution: QueryExecution valsqlContext: SQLContext deftoString(): String deftransform[U](t: (DataFrame) ⇒ DataFrame): DataFrame

Concise syntax for chaining custom transformations.

deftoSchemaRD: DataFrame

