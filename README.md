# everyonecounts-geoserver
This repositors contains a geoserver plugin to offer data from the platform https://www.everyonecounts.de as a WFS layer.

To run it:

```
git clone https://github.com/ManuelB/everyonecounts-geoserver.git
mvn install
cd geoserver
mvn wildfly:run
# Afterwards go to http://localhost:8080/geoserver (Username: admin Password: admin) You should be able to create a everyone counts datastore
```
