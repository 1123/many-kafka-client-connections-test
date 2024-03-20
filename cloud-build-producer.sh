#!/bin/bash

set -u -e

mvn clean package
cp target/many-producers-0.0.1-jar-with-dependencies.jar producer
cd producer
gcloud builds submit --config cloudbuild.yaml
