package org.lappsgrid.analytics.service;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import jp.go.nict.langrid.client.RequestAttributes;
import jp.go.nict.langrid.client.soap.SoapClientFactory;
import jp.go.nict.langrid.commons.util.CalendarUtil;
import jp.go.nict.langrid.repackaged.net.arnx.jsonic.JSON;
import jp.go.nict.langrid.service_1_2.AccessLimitExceededException;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.NoAccessPermissionException;
import jp.go.nict.langrid.service_1_2.ServiceConfigurationException;
import jp.go.nict.langrid.service_1_2.UnknownException;
import jp.go.nict.langrid.service_1_2.foundation.Order;
import jp.go.nict.langrid.service_1_2.foundation.servicemonitor.ServiceMonitorService;
import jp.go.nict.langrid.service_1_2.foundation.servicemonitor.UserAccessEntry;
import jp.go.nict.langrid.service_1_2.foundation.servicemonitor.UserAccessEntrySearchResult;
import jp.go.nict.langrid.service_1_2.foundation.typed.Period;
import jp.go.nict.langrid.service_1_2.foundation.servicemanagement.ServiceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command line entry point.
 */
@Command(name="lappsgrid-analytics",
		sortOptions = false,
		descriptionHeading = "%nDescription:%n%n",
		parameterListHeading = "%nParameters:%n",
		optionListHeading = "%nOptions:%n",
		footer="\nCopyright 2018 The Language Applications Grid\n",
		description = "Gathers usage statistics for the services registers to a LAPPS Grid service manager instance.")
public class ServiceMonitor implements Runnable
{
	protected ServiceMonitorService service;
	protected Database db;

	@Option(names={"-u", "--user"}, paramLabel = "USER", required = true, description = "service manager user")
	private String username = null;

	@Option(names={"-p", "--password"}, paramLabel = "PASSWORD", required = true, description="password for the user")
	private String password = null;

	@Option(names="--vassar", description = "query the Vassar service manager")
	private boolean vassar = false;

	@Option(names="--brandeis", description = "query the Brandeis server")
	private boolean brandeis = false;

	@Option(names={"-s", "--start"}, paramLabel = "dd-MM-yyyy", required = true, description = "start date.")
	private String startDate;

	@Option(names={"-e", "--end"}, paramLabel="dd-MM-yyyy", required = true, description="end date.")
	private String endDate;

	@Option(names={"-o", "--output"}, paramLabel="path", description = "directory where output file will be written.")
	private File destination = null;

	@Option (names={"-v", "--version"}, description = "prints the app version number and exits.", versionHelp = true)
	private boolean showVersion = false;

	@Option(names={"-h", "--help"}, description = "show this usage message", usageHelp = true)
	private boolean showHelp = false;

	private String url = null;
	private String server = "";

	public void run()
	{
		if (showHelp) {
			CommandLine.usage(this, System.out);
			return;
		}
		if (showVersion) {
			System.out.println("Lappsgrid Service Montory v" + Version.getVersion());
			System.out.println("Copyright 2018 The Language Applications Grid. All rights reserved.");
			System.out.println("");
			return;
		}

		if (vassar && brandeis) {
			System.out.println("ERROR: Only one server may be queried at a time");
			return;
		}
		if (vassar) {
			url = "http://vassar.lappsgrid.org";
			server = "vassar";
		}
		else if (brandeis) {
			url = "http://eldrad.cs-i.brandeis.edu:8080/service_manager";
			server = "brandeis";
		}
		else {
			System.out.println("No server was specified.");
			CommandLine.usage(this, System.out);
			return;
		}

		try
		{
			act();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}


	private void act() throws MalformedURLException, UnknownException, InvalidParameterException, ServiceConfigurationException, AccessLimitExceededException, NoAccessPermissionException, ParseException, FileNotFoundException
	{
		db = new Database();

		SoapClientFactory f = new SoapClientFactory();
		service = f.create(
				ServiceMonitorService.class,
				new URL(url + "/services/ServiceMonitor")
		);

		RequestAttributes attr = (RequestAttributes)service;
		attr.setUserId(username);
		attr.setPassword(password);

		ServiceLister lister = new ServiceLister();
		Map<String,Object> elements = lister.list(url, username, password);
		List<ServiceEntry> entries = (List<ServiceEntry>) elements.get("elements");
		if (entries == null) {
			System.out.println("No entries returned");
			return;
		}

		for (ServiceEntry entry : entries) {
			String fullId = entry.getServiceId();
			String[] parts = fullId.split(":");
			String grid = parts[0];
			String id = parts[1];
			stats(id, startDate, endDate);
		}
		OutputStream out = System.out;
		if (destination != null) {
			out = new FileOutputStream(destination);
		}
		db.write(out);
	}

	/**
	 * Collect statistic for a single service in a given year.
	 *
	 * @param name the service we are collecting stats for
	 * @param startDate string representation of the start date
	 * @param endDate string representation of the end date
	 * @throws UnknownException
	 * @throws InvalidParameterException
	 * @throws ServiceConfigurationException
	 * @throws AccessLimitExceededException
	 * @throws NoAccessPermissionException
	 */
	protected void stats(String name, String startDate, String endDate) throws ParseException, UnknownException, InvalidParameterException, ServiceConfigurationException, AccessLimitExceededException, NoAccessPermissionException
	{
		Calendar start = parseTime(startDate);
		Calendar end = parseTime(endDate);

		UserAccessEntrySearchResult result = service.sumUpUserAccess(0, 100, name, start, end, Period.YEAR.toString(), new Order[0]);
		UserAccessEntry[] entries = result.getElements();

		for (UserAccessEntry entry : entries) {
			Record record = new Record(entry.getUserId(), entry.getServiceId(), entry.getAccessCount());
			db.add(record);
		}
	}

	protected Calendar parseTime(String time) throws ParseException
	{
		SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date.parse(time));
		return calendar;
	}

	public static void main(String[] args) {
		CommandLine.run(new ServiceMonitor(), args);
	}
}
