# FETA

A FEderated TrAcking System for the Linked Data

[1] Tracking Federated Queries in the Linked Data, Georges Nassopoulos, Patricia Serrano-Alvarado, Pascal Molli, Emmanuel Desmontils. Tracking Federated Queries in the Linked Data. [Research Report] LINA-University of Nantes. 2015. <hal-01187519>

# Install FETA dependencies

FETA is implemented in Java 1.7 and known to run on Debian GNU/Linux and OS X. In order to install packages and dependencies related to FETA you need to execute installFETAdepend.sh. This script will install:

   1. justniffer:Network TCP Packet Sniffer
   
        http://justniffer.sourceforge.net/

      Justniffer is a network protocol analyzer that captures network traffic and produces logs in a customized way, 
      can emulate Apache web server log files, track response times and extract all "intercepted" files from the HTTP 
      traffic.
      
      We use this tool, in order to capture both queries and their answers. You may create your own captures, while   
      at the same time running queries with FedX or ANAPSID query engines. In order to capture files, you need to run 
      the command:
      
      $ sudo justniffer -i eth0 -l "%request%request.timestamp%response%response.timestamp"
   
   2. CouchDB: A Database for the Web
   
        http://couchdb.apache.org/

      Apache CouchDB is a database that uses JSON for documents, JavaScript for MapReduce indexes, and regular HTTP 7       for its API. 
      
      This DB system is used to store FETA's federated log.
   
   3. monetDB: The column-store pioneer
      
        https://www.monetdb.org/Home

      MonetDB is an open source column-oriented database management system. 
   
      This is an alternative DB system used to store FETA's federated log.

# Run FETA

The first step, is to load the capture trace into a database, of your storage DB's choice:

-load or -l: for loading a new DB

--resetDB or -r: for reseting an existing DB

--systemDB or -s: for setting "couchDB" or "monetDB" system (by default "couchDB")

--nameDB or -n: for setting the DB name

Then, you can init FETA's deduction algorithm:

--inverseMap or -i: for enabling inverse mapping in "NestedLoopDetection", necessary for FedX

--sameConcept or -c: enabling "SameConcept/SameAs" and passing endpoints IP Addresses as argument

--setWinSlice or -ws for setting the maximum temporal distance between first and last subquery, defining DB slice, , by default 1000000 seconds

--setWinJoin or -wj for setting the maximum joinable window interval gap (Tjoin), by default 1000000 seconds

# Testing FETA with FedBench query collection traces

In order to test FETA's fonctionality you may use as input traces of FedBench queries of Cross Domaian and Life Science collections, captured by using either FedX or ANAPSID. In the directory <name> you can find traces of queries executed in isolation while in the directory <name> traces of queries executed in concurrency.

# About and Contact

ANAPSID was developed at University of Nantes as an ongoing academic effort. You can contact the current maintainers by email at georges.nassopoulos[at]etu[dot]univ-nantes[dot]fr.
