# Community Data Access

**CDA** is a Data Access tool that offers an abstraction layer to accessing data within CDF and CDE dashboards

**CDA** is one of the _tools_ of the **CTools** family and it is shipped within Pentaho BA Server

#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://raw.githubusercontent.com/pentaho/maven-parent-poms/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

This is a maven project, and to build it use the following command
```
mvn clean install
```
The build result will be a Pentaho Plugin located in *assemblies/cda/target/cda-**.zip*. Then, this package can be dropped inside your system folder.


For issue tracking and bug report please use http://jira.pentaho.com/browse/CDA. Its master branch is built upon commit merges in Jenkins Continuous Integration located in http://ci.pentaho.com/job/cda-plugin/
