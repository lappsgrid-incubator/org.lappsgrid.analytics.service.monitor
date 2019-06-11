# org.lappsgrid.analytics.service.monitor
Queries the LAPPS Grid service manager instances for usage statistics

## Usage

```bash
$> java -jar lappsgrid-analytics.jar -[vh] --user USER --password PASSWORD [--brandeis|--vassar] [--output PATH] --start dd-MM-yyyy --end dd-MM-yyyy
```

### Options

* **-u** | **--user**<br/>the admin user for the service manager instance.  The admin user is the *langrid* user defined in the service_manager.xml file in Tomcat.
* **-p** | **--password**<br/>the password for the admin user
* **-s** | **--start**<br/>start date
* **-e** | **--end**<br/>end date
* **-o** | **--output**<br/>output file. If no output file is specified the statistics are written to System.out
* **--vassar** | **--brandeis**<br/>the service manager instance to query
* **-v** | **--version**<br/>prints the application version number and exits
* **-h** | **--help**<br/>prints a brief help screen

