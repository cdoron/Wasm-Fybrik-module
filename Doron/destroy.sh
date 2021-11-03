#!/bin/bash

helm delete wasm-fybrik-module -n fybrik-blueprints

PORT_FORWARD_PID=( `ps aux | grep port-forward | grep "kubectl port-forward relay" | grep -v grep | awk '{print $2}'` )
kill -9 $PORT_FORWARD_PID
