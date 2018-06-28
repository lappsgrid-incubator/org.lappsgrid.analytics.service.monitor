# org.lappsgrid.analytics.service.monitor
Queries the LAPPS Grid service manager instances for usage statistics

## Usage

```bash
$> java -jar lappsgrid-analytics.jar -[vh] --user USER --password PASSWORD --year dddd [--brandeis|--vassar]
```

### Options

* **-u** | **--user**<br/>the admin user for the service manager instance.  The admin user is the *langrid* user defined in the service_manager.xml file in Tomcat.
* **-p** | **--password**<br/>the password for the admin user
* **-y** | **--year**<br/>the year to collect statistics for
* **--vassar** | **--brandeis**<br/>the service manager instance to query
* **-v** | **--version**<br/>prints the application version number and exits
* **-h** | **--help**<br/>prints a brief help screen

