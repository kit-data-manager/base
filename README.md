# KIT Data Manager Base Modules

KIT Data Manager is a generic repository architecture especially focussing on research data. This repository 
contain all sources necessary to build the base modules of KIT Data Manager needed to implement custom
research data repository systems. 

Currently, this software repository is intended to be used to publish the sources of final releases of 
KIT Data Manager. If you like to contribute please don't hesitate to contact us.

## How to build

In order to build KIT Data Manager you'll need:

* Java SE Development Kit 7
* Apache Maven 3

After obtaining the sources change to the folder where the sources are located and just call:

```
user@localhost:/home/user/KITDM/base$ mvn install
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]
[INFO] KIT Data Manager
[INFO] Commons
[INFO] Authorization
[INFO] MetaDataManagement
[INFO] MDM-Core
[INFO] DataOrganization
[...]
user@localhost:/home/user/KITDM/base$
```

After a couple of minutes everything is done.

##### Using JDK8 for building KIT Data Manager 1.1

 If you plan to build KIT Data Manager version 1.1 using JDK 8 you have to set the property `maven.javadoc.skip` inside the main `pom.xml` to `true`. Otherwise, [Doclint](http://openjdk.java.net/jeps/172) will make the build fail due to several minor JavaDoc issues not fixed for version 1.1. However, this will be fixed 
 in the next version of KIT Data Manager.

## More Information

* [Project homepage](http://kitdatamanager.net/index.php/kit-data-manager)
* [Manual](http://kitdatamanager.net/dama/manual/index.html)
* [Bugtracker](http://kitdatamanager.net/bugtracker/thebuggenie/)

## License

KIT Data Manager is licensed under the Apache License, Version 2.0.


