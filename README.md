# Overview

This repository provides the replication package for the paper "A Large-scale Study on API Misuses in the Wild" submitted to ICST 2021.

It includes two modules:

(1) **PatternCollection**, which collects patterns from bug-fixing commits by using the following commands:
```
$cd BID3/PatternCollection/FixRuleMiner
$mvn compile
$mvn exec:java -Dexec.args="PATH/TO/DATA PATH/TO/RESULT ThreadNumber" (Example: $mvn exec:java -Dexec.args="../ExampleData/ ./ 1")
```
There will be a *ExampleResult.json* under the folder *FixRuleMiner*

We also put all patterns of our paper in (https://github.com/BID3/BID3/tree/master/PatternCollection/PaperResults)

(2) **APIMisuseDetection**, which detects potential misuses in specific projects by using the following commands:

```
$cd BID3/APIMisuseDetection/MisuseDetection/
$mvn compile
$mvn exec:java -Dexec.args="PATH/TO/PROJECT" (Example: $mvn exec:java -Dexec.args="../ExampleProjects/zookeeper/")
```

There will be a *Results.json* under the folder *MisuseDetection*

