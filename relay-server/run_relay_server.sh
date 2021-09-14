#!/bin/sh

cd /root

java --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED -Dlogback.configurationFile=file:logging.xml -cp "app-all.jar" relay.server.RelayServer -h 0.0.0.0 -p 12232 -rh $1 -rp $2
