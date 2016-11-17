docker build -t eu.gcr.io/mcc-2016-g13-p2/backend:latest .
gcloud docker -- push eu.gcr.io/mcc-2016-g13-p2/backend:latest .
kubectl set image deployment/backend backend=eu.gcr.io/mcc-2016-g13-p2/backend:latest