# CalFuzzer: An Extensible Active Testing Framework for Concurrent Programs

## Introduction

Active testing has recently been introduced to effectively test concurrent programs. Active testing can quickly discover real data races, deadlocks, and atomicity violations. Active testing works in two phases. It first uses imprecise off-the-shelf static or dynamic program analyses to identify potential concurrency bugs, such as data races, deadlocks, and atomicity violations. In the second phase, active testing uses the reports from these imprecise analyses to explicitly control the underlying scheduler of the concurrent program to accurately and quickly discover real concurrency bugs, if any, with very high probability and little overhead. CalFuzzer implements an extensible framework for active testing of Java programs.


## System Requirements

Windows or Linux or Mac OS X. You need pre-installed Sun’s JDK 1.5 for Windows or Linux, or Apple’s latest JDK for Mac OS X. You also need Apache’s ANT (http://ant.apache.org/) for building and running your code.

## Installation
    git clone ...
    cd calfuzzer
    ant
    
## Test Drive
Run the following to commands to see RaceFuzzer and DeadlockFuzzer in action.
    
    ant -f run.xml racefuzzer
    ant -f run.xml deadlockfuzzer

## How to write a custom analysis?

Check out the tool paper activetool.pdf. We have also created a simple [homework assignment](http://sp09.pbwiki.com/RaceFuzzer-Homework) to guide you through the RaceFuzzer implementation.

See

    calfuzzer/src/javato/activetesting/HybridAnalysis.java
    calfuzzer/src/javato/activetesting/RaceFuzzerAnalysis.java

for an implementation of RaceFuzzer. Note that this is not the optimized implementation reported in the PLDI’08 paper. See the targets “racefuzzer” and target “test_sor” in run.xml for details on how to invoke RaceFuzzer.

Similarly, see

    calfuzzer/src/javato/activetesting/IGoodlockAnalysis.java
    calfuzzer/src/javato/activetesting/DeadlockFuzzerAnalysis.java

for an implementation of DeadlockFuzzer. Note that this is not the optimized implementation reported in the PLDI’09 paper. See the target “deadlockfuzzer” and target “test_6” in run.xml for details on how to invoke DeadlockFuzzer.

Similarly, see
    
    calfuzzer/src/javato/activetesting/PAtomicityAnalysis.java
    calfuzzer/src/javato/activetesting/RaceFuzzerAnalysis.java

for an implementation of AtomFuzzer. Note that this is not the implementation reported in the FSE’09 paper, but a combination of the techniques reported in FSE 09 and CAV 09. See the target “atomfuzzer” and target “test_atomfuzzer15” in run.xml for details on how to invoke AtomFuzzer

## Reference

 *  Pallavi Joshi, M. Naik, C.-S. Park, and K. Sen, "An Extensible Active Testing Framework for Concurrent Programs," in Proc. 21st International Conference on Computer Aided Verification (CAV'09), 2009. 

 *  Pallavi Joshi, C.-S. Park, K. Sen, and M. Naik, "A Randomized Dynamic Program Analysis Technique for Detecting Real Deadlocks," in Proc. ACM SIGPLAN Conference on Programming Language Design and Implementation (PLDI'09), 2009. 

 *  Chang-Seo Park and K. Sen, "Randomized Active Atomicity Violation Detection in Concurrent Programs," in Proc. 16th International Symposium on Foundations of Software Engineering (FSE'08), 2008. 

 * Koushik Sen, "Race Directed Random Testing of Concurrent Programs," in Proc. ACM SIGPLAN Conference on Programming Language Design and Implementation (PLDI'08), 2008, pp. 11-21. 

 *  Koushik Sen, "Effective Random Testing of Concurrent Programs," in Proc. 22nd IEEE/ACM nternational Conference on Automated Software Engineering (ASE'07), 2007, pp. 323-332. 
 