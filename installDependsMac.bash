#!/bin/bash


read -p "Choose DB (1 for MonetDB, 2 for CouchDB or 3 for both): " database

if [ "$database" = "1" ] || [ "$database" = "3" ]; then
        
	echo "installation coucdhDB"
	curl -O https://dl.bintray.com/apache/couchdb/mac/1.6.1/Apache-CouchDB-1.6.1.zip
	unzip Apache-CouchDB-1.6.1.zip
	chmod 777 -R Apache\ CouchDB.app
	sudo mv  Apache\ CouchDB.app /Applications

fi

if [ "$database" = "2"] || [ "$database" = "3" ]; then

	echo "installation monetDB"
	xcode-select --install
	ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
	brew install monetdb

cat << EOF > $HOME/.monetdb
user=monetdb
password=monetdb
language=sql
EOF

	export DOTMONETDBFILE=$HOME/.monetdb
	echo 'export DOTMONETDBFILE=$HOME/.monetdb' >> $HOME/.bashrc

	monetdbd create $HOME/myMONETDB
	sudo chmod 777 -R  $HOME/myMONETDB
	monetdbd start $HOME/myMONETDB
	monetdb create demo
	monetdb release demo
        user="feta"
        pass="feta"

cat << EOF > test.sql
CREATE USER "$user" WITH PASSWORD '$pass' NAME 'FETA Explorer' SCHEMA "sys";
CREATE SCHEMA "$user" AUTHORIZATION "$user";
ALTER USER "$user" SET SCHEMA "$user";
CREATE TABLE test (id int, data varchar(30));
INSERT INTO test VALUES (2, 'geard');
SELECT * from test;
CREATE TABLE $user.test2 (id int, data varchar(30));
INSERT INTO $user.test2 VALUES (2, 'geard');
SELECT * from $user.test2;
EOF

	mclient -d demo < test.sql

fi

echo "installation justniffer"
curl -O https://sourceforge.net/projects/justniffer/files/justniffer_0.5.14.tar.gz
unzip justniffer_0.5.14.tar.gz
chmod 777 -R justniffer_0.5.14.tar.gz
gunzip -c justniffer_0.5.14.tar.gz
cd justniffer_0.5.14
./configure 
make 
sudo make install
