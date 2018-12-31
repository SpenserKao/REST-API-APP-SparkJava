# REST-API-APP-SparkJava
Welcome to the AddressBook - a SparkJava-based REST API application.

## System Context Diagram
![System Context Diagram](doco/img/ContextDiagram.jpeg)

## Class Diagram
![Class Diagram](doco/img/ClassDiagram.jpeg)
=======
## System context diagram
![System Context Diagram](doco/img/ContextDiagram.jpeg)

## Class diagram
![Class Diagram](doco/img/ClassDiagram.jpeg)

## Language, framework, template and tool
The REST API application is built upon:
   * _Java v1.8.0_05_, 
   * _SparkJava v2.7.2_ - an open-source web application framework that is embedded with web server _Jetty_, and 
   * _Apache Veolcity_ - a SparkJava-supported template engine to handle message's presentation to fulfill model-view-Controller (MVC) design pattern.</br>

Meanwhile, auto software building tool _Gradle v4.10.2_ has been utilised.

## Execution and testing locally
Once extracted onto a working directory, change into that directory, then execute following command:</br>
***java -Dserver.port=8001 -jar build/libs/addressbook-all-1.5.1.jar***</br>
Where the options are</br>
-h to print help message.</br>
-d for XML-based database files to load. If omitted, _~/db/default-db.xml_ inside the jar file would be loaded by default.</br>

Then through a web browser, open following URL for testing:</br>
[http://localhost:8001/addressbook](http://localhost:8001/addressbook)

## Hosting remotely
The application is currently being hosted by www.heroku.com with following URL<br/>
[rest-api-app-sparkjava.herokuapp.com/addressbook](https://rest-api-app-sparkjava.herokuapp.com/addressbook)
