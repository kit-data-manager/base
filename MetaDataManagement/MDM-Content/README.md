# KIT Data Manager - Enhanced Metadata Module

This sub-module contains the Enhanced Metadata Module of KIT Data Manager. This optional extension adds content metadata support to 
a basic KIT Data Manager installation. For adding the extension directly to custom builds its dependency can be simply uncommented 
in the pom.xml of the 'Core' Web application module of KIT Data Manager that can be found under 'Core/pom.xml'.

## How to build

For separate packaging of the Enhanced Metadata Module you'll need:

* Java SE Development Kit 7 or higher
* Apache Maven 3
* Build of the [KIT Data Manager 1.2 base](https://github.com/kit-data-manager/base) project

After building KIT Data Manager 1.2 base change to 'MetaDataManagement/MDM-Content' and call:

```
user@localhost:/home/user/KITDM/base/MetaDataManagement/MDM-Content$ mvn assembly:assembly -DskipTests=true
[INFO] Scanning for projects...
[...]
user@localhost:/home/user/KITDM/base/MetaDataManagement/MDM-Content$
```
Afterwards, you'll find the extension module at assembly/MDM-Content-1.2-default.zip

## More Information

* [Project homepage](http://datamanager.kit.edu/)
* [Manual](http://datamanager.kit.edu/dama/manual/index.html)
* [Bugtracker](http://datamanager.kit.edu/bugtracker/thebuggenie/)

For examples on how to work with the Enhanced Metadata Module please refer to the sample metadata extractors that can be found [here](https://github.com/kit-data-manager/base/tree/master/MetaDataManagement/MDM-Content/src/main/java/edu/kit/dama/mdm/content/impl).

## License

KIT Data Manager is licensed under the Apache License, Version 2.0.


