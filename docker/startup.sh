#!/bin/sh
echo "begin"
export JVM_HOME='opt/oracle-server-jre'
export PATH=$JVM_HOME/bin:$PATH


# 程序名称
APP_NAME=dapeng-mesh

# 当前时间 20180906120201
CURR_DATE=`date +%Y%m%d%H%M%S`

# 程序所在目录
APP_DIR=`pwd`
dirname $0|grep "^/" >/dev/null
if [ $? -eq 0 ];then
   APP_DIR=`dirname $0`
else
    dirname $0|grep "^\." >/dev/null
    retval=$?
    if [ $retval -eq 0 ];then
        APP_DIR=`dirname $0|sed "s#^.#$APP_DIR#"`
    else
        APP_DIR=`dirname $0|sed "s#^#$APP_DIR/#"`
    fi
fi


# 日志目录
LOG_DIR=$APP_DIR/logs
if [ ! -d "$LOG_DIR" ]; then
        mkdir "$LOG_DIR"
fi

# JMX参数
JMX="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1091 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

# JVM参数
JVM_OPTS="-Dname=$APP_NAME -Dfile.encoding=UTF-8 -Dsun.jun.encoding=UTF-8 -Dio.netty.leakDetectionLevel=advanced -Xms512M -Xmx512M -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps -Xloggc:$LOG_DIR/gc-$APP_NAME-$CURR_DATE.log -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC -Dlog.dir=$APP_DIR/.."

# DP_SOA BASE路径
SOA_BASE="-Dsoa.base=$APP_DIR/../ -Dsoa.run.mode=native"

# SIGTERM  优雅关闭(graceful-shutdown)
pid=0
process_exit() {
 if [ $pid -ne 0 ]; then
  echo "graceful shutdown pid: $pid" > $LOG_DIR/pid.txt
  kill -SIGTERM "$pid"
  wait "$pid"
 fi
 exit 143; # 128 + 15 -- SIGTERM
}

# 捕捉信号
trap 'kill ${!};process_exit' SIGTERM

# 程序启动
nohup java -server $JVM_OPTS $SOA_BASE $DEBUG_OPTS $USER_OPTS  $E_JAVA_OPTS -jar $APP_DIR/dapeng-mesh-jar-with-dependencies.jar >> $LOG_DIR/console.log 2>&1 &

# 获取上一步程序启动的 PID
pid="$!"

echo "start pid: $pid" > $LOG_DIR/pid.txt


# fluent enable , fluent_bit_enable=true

fluentBitEnable="$fluent_bit_enable"
if [ "$fluentBitEnable" == "" ]; then
    fluentBitEnable="false"
fi

if [ "$fluentBitEnable" == "true" ]; then
   nohup sh /opt/fluent-bit/fluent-bit.sh >> $LOG_DIR/fluent-bit.log 2>&1 &
fi

# 阻塞
wait $pid
