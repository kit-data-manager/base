# KIT Data Manager Base Modules
[![Travis](https://img.shields.io/travis/rust-lang/rust.svg)](https://travis-ci.org/kit-data-manager/base) 

[![Github Releases](https://img.shields.io/github/downloads/atom/atom/latest/total.svg)](https://github.com/kit-data-manager/base/releases)

KIT Data Manager is a generic repository architecture especially focussing on research data. This repository 
contain all sources necessary to build the base modules of KIT Data Manager needed to implement custom
research data repository systems. 

Currently, this software repository is intended to be used to publish the sources of final releases of 
KIT Data Manager. If you like to contribute please don't hesitate to contact us.

## How to build

In order to build KIT Data Manager you'll need:

* Java SE Development Kit 8 or higher
* Apache Maven 3.3+

After obtaining the sources change to the folder where the sources are located and just call:

```
user@localhost:/home/user/KITDM/base$ mvn install -DskipTests=true
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

## More Information

* [Project homepage](http://datamanager.kit.edu/)
* [Manual](http://datamanager.kit.edu/dama/manual/index.html)
* [Bugtracker](http://datamanager.kit.edu/bugtracker/thebuggenie/)

## License

KIT Data Manager is licensed under the Apache License, Version 2.0.


