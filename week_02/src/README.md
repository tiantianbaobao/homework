###GC 
-  常用的GC算法都有哪些
    -  新生代（复制）
    -  老年代（标记清除、标记整理）
-  GC时如何进行GCRoot的选定？ -> OOPMap
   -  选定之后何时何地进行GC处理呢？ -> 安全点
   -  如何并发进行可达性分析（CMS和G1） -> 三色标记法(增量更新、原始快照（SATB）)
   -  当出现跨代的对象引用时HotSpot是如何处理的？->记忆集（常用的实现方式是卡表）
-  G1和CMS的对比
   -  实现的方式
   -  引入的版本
   -  优缺点
   -  测试对比
-  SerialGC、ParallelGC、 CMS、G1、ZGC、Shenandoah

-  采用`SerialGC`的参数
>  最大内存、初始化堆内存、使用XXGC、关闭自适应大小、开启GC日志 
```java
java -Xmx256m -Xms256m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx512m -Xms512m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx1024m -Xms1024m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx2048m -Xms2048m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx4096m -Xms4096m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
```  
-  采用`ParallelGC`
````java
java -Xmx256m -Xms256m -XX:+UseParallelGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx512m -Xms512m -XX:+UseParallelGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx1024m -Xms1024m -XX:+UseParallelGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx2048m -Xms2048m -XX:+UseParallelGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx4096m -Xms4096m -XX:+UseParallelGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
````
-  使用`CMS`
````java
java -Xmx256m -Xms256m -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx512m -Xms512m -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx1024m -Xms1024m -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx2048m -Xms2048m -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx4096m -Xms4096m -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
````
-  使用`G1`
````java
java -Xmx256m -Xms256m -XX:+UseG1 -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx512m -Xms512m -XX:+UseG1 -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx1024m -Xms1024m -XX:+UseG1 -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx2048m -Xms2048m -XX:+UseG1 -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
java -Xmx4096m -Xms4096m -XX:+UseG1 -XX:+PrintGCDetails -XX:-UseAdaptiveSizePolicy GCAnalysize
````

####总结
-  根据不同的GC收集器进行测试发现，并行GC下生成的对象最多，意味着吞吐量越好，当Xmx在1g~3g时CMS和G1表现差不多，当Xmx逐渐增大时，G1优势逐步显现出来
-  根据`jmap -heap PID`去看，在未指定`Xmn`且关闭`UseAdaptiveSizePolicy`时
    -  ParallelGC的年轻代的最大空间为整个堆空间1/3
    -  CMS的年轻代的最大空间略小于最大堆空间的1/3，实际的计算公式为64M * GCThreads * 13/10
    -  G1由于采用了域的概念，初始化域的大小个数为2048个，新生代的大小可配，默认配置为堆最低5%，最多60%
    
-  GCThreads的配置