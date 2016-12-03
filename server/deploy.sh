#!/usr/bin/env bash

PROJECT_ID="mcc-2016-g13-p2"
GCLOUD_ZONE="europe-west1-c"


echo 'Installing dependencies...'

CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
echo "deb https://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
apt-get update && apt-get -y install google-cloud-sdk kubectl docker.io make

RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Dependencies installed successfully.'
else
    (>&2 echo 'ERROR: Dependencies could not be installed.')
    exit $RESULT
fi


echo 'Setting up Docker and building container...'

groupadd docker
gpasswd -a ${USER} docker
sudo -u ${USER} newgrp docker
docker build -t eu.gcr.io/$PROJECT_ID/backend:v1 .

RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Docker container set up successfully.'
else
    (>&2 echo 'ERROR: Docker container could not be set up.')
    exit $RESULT
fi


echo 'Authenticating to Google Cloud and pushing Docker container...'

chown -R ${USER} ~/.config
gcloud auth activate-service-account tt-822@mcc-2016-g13-p2.iam.gserviceaccount.com --key-file=./key/mcc-2016-g13-p2-94921abc7259.json
gcloud docker -- push eu.gcr.io/$PROJECT_ID/backend:v1

RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Docker container pushed successfully.'
else
    (>&2 echo 'ERROR: Docker container could not be pushed to Google Cloud.')
    exit $RESULT
fi


echo 'Setting up cluster...'

gcloud config set compute/zone $GCLOUD_ZONE
gcloud container clusters create backend
gcloud config set container/use_client_certificate True
gcloud container clusters get-credentials backend
make -C cluster/sidecar/ add-replica
kubectl create -f cluster/backend.yaml

RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Cluster set up successfully.'
else
    (>&2 echo 'ERROR: Cluster could not be set up.')
    exit $RESULT
fi