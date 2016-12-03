#!/usr/bin/env bash

PROJECT_ID="mcc-2016-g13-p2"


echo 'Installing dependencies...'

export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
echo "deb https://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
apt-get update && apt-get -y install google-cloud-sdk kubectl docker.io

RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Dependencies installed successfully.'
else
    (>&2 echo 'ERROR: Dependencies could not be installed.')
    exit $RESULT
fi


echo 'Setting up Docker ans building container...'

groupadd docker
gpasswd -a ${USER} docker
sudo -u ${USER} newgrp docker
docker build -t gcr.io/$PROJECT_ID/backend:v1 .

RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo 'Docker container set up successfully.'
else
    (>&2 echo 'ERROR: Docker container could not be set up.')
    exit $RESULT
fi


gcloud auth activate-service-account tt-822@mcc-2016-g13-p2.iam.gserviceaccount.com --key-file=./key/mcc-2016-g13-p2-94921abc7259.json

gcloud docker -- push gcr.io/$PROJECT_ID/backend:v1
