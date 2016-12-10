#!/usr/bin/env bash
docker build -t eu.gcr.io/mcc-2016-g13-p2/backend .
gcloud docker -- push eu.gcr.io/mcc-2016-g13-p2/backend
kubectl set image deployment/backend-dpl backend=eu.gcr.io/mcc-2016-g13-p2/backend
echo 'Cluster updated.'
