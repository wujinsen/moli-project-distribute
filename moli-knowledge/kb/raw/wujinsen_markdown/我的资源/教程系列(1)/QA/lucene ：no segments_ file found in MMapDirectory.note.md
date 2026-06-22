org.apache.lucene.index.IndexNotFoundException: no segments* file found in :\ maoxiangyi\workspace\learnLucene\index lockFactory= :\maoxiangyi\works pace\learnLucene\index: files: [write.lock] at org.apache.lucene.index.SegmentInfos$FindSegmentsFile.run(SegmentInfos.java:864)

MapDirectory@E NativeFSLockFactory@E

at org.apache.lucene.index.StandardDirectoryReader.open(StandardDirectoryReader.java:53) at org.apache.lucene.index.DirectoryReader.open(DirectoryReader.java:67) at cn.itcast.lucene.learnLucene.util.LuceneUtil.<clinit>(LuceneUtil.java: 3) at cn.itcast.lucene.learnLucene.LuceneUtilTest.cretaeIndex(LuceneUtilTest.java:34) at sun.reflect.NativeMethodAcesorImpl.invoke0(Native Method) at sun.reflect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:62)

at sun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:43) at java.lang.reflect.Method.invoke(Method.java:497) at org.junit.runers.model.FrameworkMethod$1.runReflectiveCal(FrameworkMethod.java:47) at org.junit.internal.runers.model.ReflectiveCalable.run(ReflectiveCalable.java:12) at org.junit.runers.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java: 4) at org.junit.internal.runers.statements.InvokeMethod.evaluate(InvokeMethod.java:17) at org.junit.runers.ParentRuner.runLeaf(ParentRuner.java:271) at org.junit.runers.BlockJUnit4ClasRuner.runChild(BlockJUnit4ClasRuner.java:70) at org.junit.runers.BlockJUnit4ClasRuner.runChild(BlockJUnit4ClasRuner.java:50) at org.junit.runers.ParentRuner$3.run(ParentRuner.java:238)

- at org.junit.runers.ParentRuner$1.schedule(ParentRuner.java:63) at org.junit.runers.ParentRuner.runChildren(ParentRuner.java:236) at org.junit.runers.ParentRuner.aces$ 0(ParentRuner.java:53)
- at org.junit.runers.ParentRuner$2.evaluate(ParentRuner.java: 29) at org.junit.runers.ParentRuner.run(ParentRuner.java:309) at org.eclipse.jdt.internal.junit4.runer.JUnit4TestReference.run(JUnit4TestReference.java:50) at org.eclipse.jdt.internal.junit.runer.TestExecution.run(TestExecution.java:38)


at org.eclipse.jdt.internal.junit.runer.RemoteTestRuner.runTests(RemoteTestRuner.java:459)

at org.eclipse.jdt.internal.junit.runer.RemoteTestRuner.runTests(RemoteTestRuner.java:675) at org.eclipse.jdt.internal.junit.runer.RemoteTestRuner.run(RemoteTestRuner.java:382) at org.eclipse.jdt.internal.junit.runer.RemoteTestRuner.main(RemoteTestRuner.java:192)

-

两种解决⽅法：

- 1，将索引库的writer.lock⽂件删除掉
- 2，在初始化IndexWriter时，先提交下（建议使⽤这个⽅法）


