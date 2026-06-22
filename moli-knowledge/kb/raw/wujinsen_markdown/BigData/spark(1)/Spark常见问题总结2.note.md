# Co monSparkTroubleshoting

BY RUSEL SPITZER - OCTOBER 29, 2014 | 0 COMENTS Spark is an excelent tol to use with Apache Casandra and thanks to the DataStax OS Spark Casandra Conector it couldnʼt be easier. But, as with any new system, there are some gotchas that can hold up new users. Here is a quick list of comon problems and how to solve them! (Note: While most of this is geared towards DataStax Enterprise Spark Standalone users, this advice should be aplicable to most Apache Spark users as wel!) Update 2015-06-10: As a folow-on to this blog post, se

Zen and the Art of Spark Maintenance

which dives deper into the i ner workings of Spark and how you can shape your aplication to take advantage of interactions betwen Spark and Casandra.

Initial job has not acepted any resources : Investigating the cluster state

This is by far the most comon first eror that a new Spark user wil se when atempting to run a new aplication. Our new and excited Spark user wil atempt to start the shel or run their own aplication and be met with the folowing mesage

WARN TaskSchedulerImpl: Initial job has not accepted any resources; check your cluster uito ensure that workers are registered and have sufficient memory This mesage wil pop up any time an aplication is requesting more resources from the cluster than the cluster can curently provide. What resources you might ask? Wel Spark is only l oking for two things: Cores and Ram. Cores represents the number of open executor slots that your cluster provides for execution. Ram refers to the amount of fre Ram required on any worker runing your aplication. Note for both of these resources the maximum value is not your Systemʼs max, it is the max as set by the your Spark configuration. To se the curent state of your cluster (and itʼs fre resources) check out the UI at SparkMasterIP 7080 (DSE users can find their SparkMaster URI using dsetol sparkmaster.)

In the above example is a picture of my Spark UI runing on my Macbok (localhost:7080). You can se one of the Spark Shel aplications is curently waiting. I caused this situation by starting the Spark Shel in 2 diferent terminals. The first Spark shel has consumed al the available cores in the system leaving the second shel waiting for resources. Until the first spark shel is terminated and its resources are released, al other aps wil display the above warning. For more details on how to read the Spark UI check the section below.

The short term solution to this problem is to make sure you arenʼt requesting more resources from your cluster than exist or to shut down any aps that are unecesarily using resources. If you ned to run multiple Spark aps simultaneously then youʼl ned to adjust the amount of cores being used by each ap.

Aplication isnʼt using al of the Cores: How to set the Cores used by a Spark Ap

There are two important system variables that control how many cores a particular aplication wil use in Apache Spark. The first is set on the Spark Master,spark.deploy.defaultCores. You can set this as -Dspark.deploy.defaultCores in the comand line of your Spark Master startup script (In DSE l ok in resources/spark/conf/spark-env.sh). This option wil set the default number of cores to be used by al aplications started through this master. This means that if I had set spark.deploy.defaultCores=3 in my above example everything would have ben fine. Each Spark Shel would have reserved 3 cores for execution and the two jobs could run simultaneously. The second variable for controling number of cores alows us to overide the master default and set the number of cores on a per ap basis. This variable, spark.cores.max, can be set progra maticaly or as a comand line JVM option. Progra maticaly it is set by ading a keyvalue pair to the SparkConf object used for creating your spark context. On the comand-line it is set using -Dspark.cores.max=N.

Spark Executor OM: How to set Memory Parameters on Spark

Once a ap is runing the next most likely eror you wil se is an OM on a spark executor. Spark is an extremely powerful tol for doing in-memory computation but itʼs power comes with some sharp edges. The most comon cause for an executor OMʼing is that the aplication is trying to cache or load to much information into memory. Depending on your use case there are several solutions to this:

Increase the paralelism of your job. Try increasing the number of partitions in your job. By spliting the work into smaler sets of data les information wil have to be resident in memory at a given time. For a Spark Casandra Conector job this would mean decreasing the split size variable. The variable, spark.casandra.input.split.size, can be set either on the comand line as above or in the SparkConf object. For other RD types l ok into their apiʼs to determine exactly how they determine partition size.

Increase the storage fraction variable, spark.storage.memoryFraction. This can be set as above on either the comand line or in the SparkConf object. This variable sets exactly how much of the JVM wil be dedicated to the caching and storage of RDʼs. You can set it as a value betwen 0 and 1, describing what portion of executor JVM memory wil be dedicated for caching RDs. If you have a job that wil require very litle shufle memory but wil utilize a lot of cached RDʼs increase this variable (example: Caching an RD then performing agregates on it.)

If al else fails you may just ned aditional ram on each worker. For DSE users adjust your spark-env.sh (or dse.yaml file in DSE 4.6) file to increase SPARK_MEM reserved for Spark jobs. You wil ned to restart your workers for these new memory limits to take efect (dse sparkworker restart.) Then increase the amount of ram the aplication requests by seting spark.executor.memory variable either on the comand line or in the SparkConf object.

Shark Server/ Long Runing Aplication Metadata Cleanup

As Spark aplications run they create metadata objects which are stored in memory indefinitely by default. For Spark Streaming jobs you are forced to set the variable spark.cleaner.tl to clean out these objects and prevent an OM. On other long lived projects you must set this yourself. For those users of Shark Server this is especialy important. For DSE users (4.5.x) you can set this property in your shark-env.sh file for Shark Server deployments. This wil let you have a long runing Shark proceses without worying about a suden OM. To check if this isue is causing your OMs l ok in your heap dumps for a large number of scheduler.ShufleMapTasks andscheduler.ResultTask objects. To set this you would end up modifying your SPARK_JAVA_OPTS variable like this

export SPARK_JAVA_OPTS +="-Dspark.kryoserializer.buffer.mb=10 Dspark.cleaner.ttl=43200"

Clas Not Found: Claspath Isues

Another comon isue is seing clas not defined when compiling Spark programs this is a slightly confusing topic because spark is actualy runing several JVMʼs when it executes your proces and the path must be corect for each of them. Usualy this comes down to corectly pasing around dependencies to the executors. Make sure that when runing you include a fat Jar containing al of your dependencies, (I recomend using sbt asembly) in the SparkConf object used to make your Spark Context. You should end up writing a line like this in your spark aplication:

val conf = new SparkConf().setAppName(appName).setJars(Seq(System.getProperty("user.dir")

+ "/target/scala-2.10/sparktest.jar")) This should fix the vast majority of clas not found problems. Another option is to place your dependencies on the default claspath on al of the worker nodes in the cluster. This way you wonʼt have to pas around a large jar. The only other major isue with clas not found isues stems from diferent versions of the libraries in use. For example if you donʼt use identical versions of the comon libraries in your aplication and in the spark server you wil end up with claspath isues. This can ocur when you compile against one version of a library (like Spark 1.1.0) and then atempt to run against a cluster with a diferent or out of date version (like Spark 0.9.2). Make sure that you are matching your library versions to whatever is being loaded onto executor claspaths. A comon example of this would be compiling against an alpha build of the Spark Casandra Conector then atempting to run using claspath references to an older version.

## What is hapening : A Brief Tour of The Spark UI

Once your job has started and itʼs not throwing any exceptions you may want to get a picture of what's going on. Al of the information about the curent state of the aplication is available on the

. Here is a brief walkthrough starting with the initial scren>. If you are runing on AWS or GCE you may find it useful to set SPARK_PUBLIC_DNS=PUBLIC_IP for each of the nodes in your cluster. This wil cause make the links work corectly and not just conect to the internal provider IP adreses.

Spark UI

![image 1](<Spark常见问题总结2.note_images/imageFile1.png>)

You should se something very similar to this when acesing the UI page for your spark cluster. In the uper left (1) youʼl se the cluster wide over statistics showing exactly what resources are available. These numbers are agregates for al of the workers and runing jobs in the cluster. Starting at the Workers line (2) weʼl se what action is actualy taking place on our cluster at the moment. First listed is exactly what workers are curently runing and how utilized they are. You can se here on a node by node basis how much memory is fre and how many cores are available. This is significant because the uper bound on how much memory an aplication can use is set at this level. For example a job which requests 8 GB of ram can only run on workers which have at least 8 GB of fre ram. If any particular node has hung or is not apearing on the worker list try runing dsetol sparkworker restart on that node to restart the worker proces. (Note: you may se dead workers on this page if you have recently restarted nodes or spark worker proceses, this is not an isue the master just hasnʼt fuly confirmed that the old worker is gone.) Below we that we can se the curently runing spark aplications (3). Since iʼm curently runing an instance of the Spark Shel you can se an entry for that listed as runing. A line wil apear here for every Spark Context object that is created comunicating with this master. Since the spark context for the Spark Shel is created on startup and doesnʼt close until the shel is closed, we should se this listed as long as we are runing Spark Shel comands. In the Completed Aplications section (4), we can se Iʼve shut down that Spark Shel from earlier that was waiting for resources. In Spark 1.0+ you can enable event loging which wil enable you to se the aplication detail for past aplications but I havenʼt for this example. This means we can only l ok into the state of curently runing aplications. To pek into the ap we can click on the aplications name (“Spark shel” directly to the right of the 3) and weʼl be taken to the Ap Detail page.

Here we can se the various Stages that this aplication has completed. For this particular example Iʼve run the folowing comands.

sc.parallelize(1 until 10000).countsc.parallelize(1 until 10000).map( x

=> (x%30,x)).groupByKey().count

You can se each Spark Transformation (map) and Action (count) is reported as a separate stage. We can se that in this case, each of the stages has already completed sucesfuly. We can se for each stage exactly how many Tasks it was broken into and how many of them are curently complete. The number of Tasks here is the maximum level of paralelism that can be acomplished for that stage. The Tasks wil be handed out to available executors, so if there are only 2 tasks but 4 executors (cores) then that stage can only ever be run on 2 cores at the same time. Adjusting how many tasks are created is dependent on the underlying RD and the nature of the transformation you are runing, be sure to check your RDʼs api to determine how many partitions/tasks wil be created. To actualy se the details on how a particular stage was acomplished click on the “Description” field for that stage to go to the Stage Detail page.

This page gives us the nity grity of how our stage was actualy acomplished. At the botom we se a list of every task, on what machine/or core it was run and how long it tok. This is a key place to l ok to find tasks that failed, on what node they failed and get information about where botlenecks are. The Sumary at the top of the page shows us the sumary of al of the entries of the botom of the page, making it easy to se if there are outliers that may have ben runing slow for some reason. Use this page to debug performance isues with your tasks. Letʼs take a quick detour to the “Storage Tab” at the top of this scren. This wil take us to a scren which l oks like this.

Here we can se al the RDʼs curently stored and by clicking on the “RD Name” link we can se exactly on which nodes data is being stored. This is helpful when trying to understand exactly whether or not your RDʼs are in memory or on disk. For this example I ran two quick RD operations from the Spark Shel

val rdd1 = sc.cassandraTable("ascii_ks","ascii_cs").map( row => row.getString("pkey") ).cache()// Cache tells Spark to save this rdd in memory after loading itrdd1.countval rdd2 = sc.cassandraTable("bigint_ks","bigint_cs").map( row => row.getString("pkey") ).cache()rdd2.count Cache comands indicate that spark neds to kep these rdʼs in memory. This wil not cause the RD to be instantly be cached, instead it wil be cached the next time it is loaded into memory. Thats al for my most frequent troubleshoting tips, hopefuly weʼl be able to enhance this guide and provide even more formal documentation for Spark. Check back son!

