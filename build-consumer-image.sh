#!/bin/bash

set -e -u

mvn clean package
cp target/many-producers-0.0.1-jar-with-dependencies.jar consumer/
cd consumer
docker build . -t many-consumers-image:0.0.1
