#!/bin/bash
cp target/Jayce*.jar Jayce.jar
java -jar \
--add-opens java.base/java.net=ALL-UNNAMED \
--add-opens java.base/java.util=ALL-UNNAMED \
--add-opens java.base/java.lang=ALL-UNNAMED \
--add-opens java.base/java.time=ALL-UNNAMED \
--add-opens java.base/java.lang.ref=ALL-UNNAMED \
--add-opens java.base/java.lang.invoke=ALL-UNNAMED \
--add-opens java.base/java.lang.reflect=ALL-UNNAMED \
Jayce.jar