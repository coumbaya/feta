#!/bin/bash

echo "installation coucdhDB"
curl -0 https://dl.bintray.com/apache/couchdb/mac/1.6.1/Apache-CouchDB-1.6.1.zip
unzip Apache-CouchDB-1.6.1.zip
chmod 777 -R Apache\ CouchDB.app
sudo mv  Apache\ CouchDB.app /Applications

echo "installation monetDB"
xcode-select --install
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
brew install monetdb

echo "installation justniffer"
curl -0 https://sourceforge.net/projects/justniffer/files/justniffer_0.5.14.tar.gz
unzip justniffer_0.5.14.tar.gz
chmod 777 -R justniffer_0.5.14.tar.gz
cd justniffer_0.5.14
