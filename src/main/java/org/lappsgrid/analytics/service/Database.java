package org.lappsgrid.analytics.service;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *  This is a simple ad-hoc datastore until we set up a proper database
 *  with Spring Boot.
 */
public class Database
{
	private Map<String,List<Record>> userIndex;
	private Map<String,List<Record>> serviceIndex;
	private Predicate<Record> byUser;

	public Database()
	{
		userIndex = new HashMap<>(64);
		serviceIndex = new HashMap<>(64);
	}

	public void add(Record record) {
		add(userIndex, record.getUser(), record);
		add(serviceIndex, record.getService(), record);
	}

	public List<Record> findByUser(String user) {
		return userIndex.get(user);
	}

	public List<Record> findByService(String service) {
		return serviceIndex.get(service);
	}

	public List<Record> findByUserAndService(String user, String service) {
		List<Record> records = findByService(service);
		if (records == null) {
			return null;
		}
		List<Record> result = new ArrayList<>();
		for (Record record : records) {
			if (user.equals(record.getUser())) {
				result.add(record);
			}
		}
		return records.stream().filter(r -> r.getUser().equals(user)).collect(Collectors.toList());
	}

	public void write(OutputStream stream) {
		PrintStream out;
		if (stream instanceof PrintStream) {
			out = (PrintStream) stream;
		}
		else {
			out = new PrintStream(stream);
		}

		long total = 0;
		for (Map.Entry<String,List<Record>> entry : serviceIndex.entrySet()) {
			List<Record> records = entry.getValue();
			for (Record record : records)
			{
				total += record.getCount();
				out.printf("%s,%s,%d\n", record.getUser(), record.getService(), record.getCount());
			}
		}
		out.printf("Total service calls: %d\n", total);
	}

	protected void add(Map<String,List<Record>> index, String key, Record record) {
		List<Record> records = index.get(key);
		if (records == null) {
			records = new ArrayList<>();
			index.put(key, records);
		}
		records.add(record);
	}
}
