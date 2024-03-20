#!/bin/bash

set -e -u

mvn clean package
cp target/many-producers-0.0.1-jar-with-dependencies.jar producer/
cd producer
docker build . -t many-producers-image:0.0.1
