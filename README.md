# REST-API-APP-SparkJava
Welcome to the AddressBook - a SparkJava-based REST API application.

## System Context Diagram
![System Context Diagram](doco/img/ContextDiagram.jpeg)</br>
The property file, _AddressBook.properties_, contains a property _DEFAULT_DB_XML_FILE_ that points the default database named _default-db.xml_ to be loaded when no external data base(s) is/are through -d option. See Section "Execution and testing locally" for command syntax. </br>
The file _endPoints.json_ records a list of created endpoints that is rendered into HTML form through _Apache Velocity_ template engine. So is _releaseNote.json_ for a list of release note.

## Class Diagram
![Class Diagram](doco/img/ClassDiagram.jpeg)

## Language, framework, template and tool
The REST API application is built upon:
   * _Java v1.8.0_05_, 
   * _SparkJava v2.7.2_ - an open-source web application framework that is embedded with web server _Jetty_, and 
   * _Apache Veolcity_ - a SparkJava-supported template engine to handle message's presentation to fulfill model-view-controller (MVC) design pattern.</br>

Meanwhile, auto software building tool _Gradle v4.10.2_ has been utilised. File _build.gradle_ is for configuring _Gradle_.

## Execution and testing locally
Once extracted onto a working directory, change into that directory, then execute following command:</br>
***java -Dserver.port=8001 -jar build/libs/addressbook-all-1.6.0.jar***</br>
Where the options are</br>
-h to print help message.</br>
-d for XML-based database files to load. If omitted, _~/db/default-db.xml_ inside the jar file would be loaded by default.</br>

Then through a web browser, open following URL for testing:</br>
[http://localhost:8001/addressbook](http://localhost:8001/addressbook)

## Hosting remotely
The application is currently being hosted by www.heroku.com with following URL<br/>
[rest-api-app-sparkjava.herokuapp.com/addressbook](https://rest-api-app-sparkjava.herokuapp.com/addressbook)

### Deployment to Heroku
Unlike locally deployment, there are more details to emphasise to the remote server.</br> 
File _Procfile_, whose content is shown below, serves a critial purpose on how deployment should be treated by server _Heroku_.
___web: java $JAVA_OPTS -Dserver.port=$PORT -jar build/libs/addressbook-all-1.6.0.jar___

It critically tells _Heroku_ server that
   * the deployment is a web application,
   * to execute the application through the specified jar file, and
   * the port numner is to be assigned by the server through property _server.port_.