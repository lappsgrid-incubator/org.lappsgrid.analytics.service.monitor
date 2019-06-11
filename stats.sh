#!/usr/bin/env bash
set -eu

source ~/.secret/service-manager.sh

#if [[ -z "$1" ]] ; then
#	year=`date | awk '{print $6}'`
#else
#	year=$1
#fi

server=$1
start=$2
end=$3

JAVA="java -jar target/lappsgrid-analytics.jar"

function vassar_stats() {
    $JAVA --user $SERVICE_MANAGER_USERNAME --password $SERVICE_MANAGER_PASSWORD --vassar --start $1 --end $2
}

function brandeis_stats() {
    $JAVA --user $BRANDEIS_SM_USER --password $BRANDEIS_SM_PASS --brandeis --start $1 --end $2
}

case $server in
    vassar)
        vassar_stats $start $end
        ;;
    brandeis)
        brandeis_stats $start $end
        ;;
    both)
        vassar_stats $start $end
        brandeis_stats $start $end
        ;;
    *)
        echo "Unknown option $server"
        ;;
esac


#$JAVA --user eldrad --password eldrad --brandeis --year $year --output stats/$year



