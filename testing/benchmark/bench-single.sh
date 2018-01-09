#!/bin/bash

cd single-module
TIMEFORMAT='%3R';

#Fsbt test

test_fsbt(){
    echo "--------- Testing fsbt ---------"
    echo " "

    raw_compile_times=""
    incremental_compile_times=""

    (cd src/main/scala/root && ./generator.sh 0)

    for i in $(seq 1 10);do
        fsbt clean stop > /dev/null
        fsbt > /dev/null
        time fsbt compile
        res=$( (time fsbt compile ) 2>&1 | tail -1)
#        raw_compile_times+=" $res"
    done

    for i in $(seq 1 10);do
        (cd src/main/scala/root && ./generator.sh $i)
        time fsbt compile
#        res=$( (time fsbt compile) 2>&1 | tail -1)
#        incremental_compile_times+=" $res"
    done

    echo "fsbt raw compile times: $raw_compile_times"
    echo "fsbt inc compile times: $incremental_compile_times"
}

test_sbt(){
    echo "--------- Testing sbt ---------"
    echo " "

    raw_compile_times=""
    incremental_compile_times=""

    (cd src/main/scala/root && ./generator.sh 0)

    for i in $(seq 1 1);do
        sbt clean > /dev/null
        time sbt compile
#        res=$( (time sbt compile ) 2>&1 | tail -1)
#        raw_compile_times+=" $res"
    done

    for i in $(seq 1 10);do
        (cd src/main/scala/root && ./generator.sh $i)
        time sbt compile
        #incremental_compile_times+=" $res"
    done

    echo "sbt raw compile times: $raw_compile_times"
    echo "sbt inc compile times: $incremental_compile_times"
}

test_maven(){
    echo "--------- Testing maven ---------"
    echo " "

    raw_compile_times=""
    incremental_compile_times=""

    (cd src/main/scala/root && ./generator.sh 0)

    for i in $(seq 1 10);do
        mvn clean > /dev/null
        res=$( (time mvn compile ) 2>&1 | tail -1)
        raw_compile_times+=" $res"
    done

    for i in $(seq 1 10);do
        (cd src/main/scala/root && ./generator.sh $i)
        res=$( (time mvn compile) 2>&1 | tail -1)
        incremental_compile_times+=" $res"
    done

    echo "maven raw compile times: $raw_compile_times"
    echo "maven inc compile times: $incremental_compile_times"
}

test_gradle(){
    echo "--------- Testing gradle ---------"
    echo " "

    raw_compile_times=""
    incremental_compile_times=""

    (cd src/main/scala/root && ./generator.sh 0)

    for i in $(seq 1 10);do
        gradle clean > /dev/null
        res=$( (time gradle compileScala ) | tail -1)
        raw_compile_times+=" $res"
    done

    for i in $(seq 1 10);do
        (cd src/main/scala/root && ./generator.sh $i)
        res=$( (time gradle compileScala)  | tail -1)
        incremental_compile_times+=" $res"
    done

    echo "gradle raw compile times: $raw_compile_times"
    echo "gradle inc compile times: $incremental_compile_times"
}


test_maven
test_gradle
test_sbt
test_fsbt
