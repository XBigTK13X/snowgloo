#! /bin/bash

cd web-client
yarn build
cd ..
rm -rf ./web-server/src/web-build
cp -r ./web-client/build ./web-server/src/web-build
