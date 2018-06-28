package org.lappsgrid.analytics.service;

/**
 *
 */
public class Record
{
	private String user;
	private String service;
	private int count;

	private int hash;
	private String cache;

	public Record(String user, String service, int count)
	{
		this.user = user;
		this.service = service;
		this.count = count;
		this.cache = user + "," + service + "," + count;
		this.hash = cache.hashCode();
	}

	public String getUser()
	{
		return user;
	}

	public String getService()
	{
		return service;
	}

	public int getCount()
	{
		return count;
	}

	public String toString() {
		return cache;
	}

	public int hashCode() {
		return hash;
	}

	public boolean equals(Object object) {
		return cache.equals(object.toString());
	}
}
