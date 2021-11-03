#!/bin/bash

RELAY_POD=( `kubectl get pods -n fybrik-blueprints | grep relay | awk '{print $1}'` )

kubectl exec -it $RELAY_POD -n fybrik-blueprints -- /bin/bash
