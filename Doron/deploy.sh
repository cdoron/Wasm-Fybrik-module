#!/bin/bash

cd /home/cdoron/Mohammad/Wasm-Fybrik-module

kubectl create secret docker-registry docker-reg-cred \
   --docker-server=ghcr.io --docker-username=the-mesh-for-data \
   --docker-password=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 \
   -n fybrik-blueprints


helm install wasm-fybrik-module ./wasm-fybrik-module -n fybrik-blueprints
kubectl apply -f Doron/cm.yaml

sleep 3

RELAY_POD=( `kubectl get pods -n fybrik-blueprints | grep relay | awk '{print $1}'` )

kubectl port-forward $RELAY_POD 8000:12232 -n fybrik-blueprints &
