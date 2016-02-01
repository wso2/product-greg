#!/bin/sh

THE_CLASSPATH=
PROGRAM_NAME=../src/org/wso2/carbon/registry/samples/populator/CleanUp.java
CLASS_NAME=org.wso2.carbon.registry.samples.populator.CleanUp
PROP="-Dcarbon.home=../../../../"
cd src
for i in `ls ../../../../repository/components/plugins/*.jar`
  do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
javac  -classpath ".:${THE_CLASSPATH}" -source 1.7 -target 1.7 $PROGRAM_NAME
java   -classpath ".:${THE_CLASSPATH}" "$PROP" $CLASS_NAME 9443 localhost


