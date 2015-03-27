#!/bin/sh
ROOT_DIR=`dirname $0`/..
cd $ROOT_DIR
TASK=$1
COMPONENT=$2

if [ -z "$TASK" ] || [ -z "$COMPONENT" ]; then
    echo "Usage: ./forever.sh [TASK] [COMPONENT] "
    exit 1
fi

function start_eva_status {
    pythonParser=/Users/lucasmf/.pyenv/versions/2.7.8/bin/python
    evaStatusServerFile=libexec/evaStatusService/evaStatusService.py
    current=`date +'%Y%m%d%H%M'`
    nohup $pythonParser $evaStatusServerFile > log/evaStatusServer-$current 2>&1&
    evaStatusServerPid=$!
    echo "evaStatus server running on process ID: $evaStatusServerPid, port: 5000"
    echo "log output at log/evaStatusServer-$current"
    echo $evaStatusServerPid > pid/evaStatusServerPid.txt
}

function start_forever {
    current=`date +'%Y%m%d%H%M'`
    nohup mvn spring-boot:run > log/foreverServer-$current 2>&1&
    foreverServerPid=$!
    echo "forever server running on process ID: $foreverServerPid, port: 8443"
    echo "log output at log/foreverServer-$current"
    echo $foreverServerPid > pid/foreverServerPid.txt

}

function stop_eva_status {
    evaStatusServerPid=`cat pid/evaStatusServerPid.txt`
    kill -9 "$evaStatusServerPid"
    echo "killed evaStatus server running on process ID: $evaStatusServerPid"
}

function stop_forever {
     foreverServerPid=`cat pid/foreverServerPid.txt`
     kill -9 "$foreverServerPid"
     echo "killed forever server running on process ID: $foreverServerPid"
}

function start_all {
    start_eva_status
    start_forever
}

function stop_all {
    stop_forever
    stop_eva_status
}

CMD="$TASK"_"$COMPONENT"
$CMD
