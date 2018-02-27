# I decided to choose pretty common concurrent pattern: producer/consumer with blocking queue.
# I assume that in real world this application can be horizontally scaled using some external cloud queuing solution.
# So in fact this is all about developing performant Consumer.

# playing with different jvm options gave me the best performance/resources utilization using this jvm_opts:
java -jar -XX:MaxPermSize=128m -XX:+UseParNewGC -XX:MaxNewSize=256m -XX:NewSize=256m -Xms256m -Xmx768m 
-XX:SurvivorRatio=128 -XX:MaxTenuringThreshold=0 -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled 
-XX:+CMSPermGenSweepingEnabled build/libs/pex-test-1.0-SNAPSHOT.jar

All 1000 urls were processed within ~3:20sec on 4 core machine.

![jvisualvm resources utilization](https://ibb.co/jYvqJH)
