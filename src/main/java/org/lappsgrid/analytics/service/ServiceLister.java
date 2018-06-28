package org.lappsgrid.analytics.service;

import jp.go.nict.langrid.client.RequestAttributes;
import jp.go.nict.langrid.client.soap.SoapClientFactory;
import jp.go.nict.langrid.service_1_2.AccessLimitExceededException;
import jp.go.nict.langrid.service_1_2.InvalidParameterException;
import jp.go.nict.langrid.service_1_2.NoAccessPermissionException;
import jp.go.nict.langrid.service_1_2.ServiceConfigurationException;
import jp.go.nict.langrid.service_1_2.UnknownException;
import jp.go.nict.langrid.service_1_2.foundation.MatchingCondition;
import jp.go.nict.langrid.service_1_2.foundation.Order;
import jp.go.nict.langrid.service_1_2.foundation.servicemanagement.ServiceEntry;
import jp.go.nict.langrid.service_1_2.foundation.servicemanagement.ServiceEntrySearchResult;
import jp.go.nict.langrid.service_1_2.foundation.servicemanagement.ServiceManagementService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ServiceLister
{

	public ServiceLister() {
	}

	Map<String,Object> list(String url, String username, String password) throws MalformedURLException, UnknownException, InvalidParameterException, ServiceConfigurationException, AccessLimitExceededException, NoAccessPermissionException
	{

		MatchingCondition[] conditions = new MatchingCondition[0];

		Order[] order = new Order[0];
		SoapClientFactory f = new SoapClientFactory();
		ServiceManagementService s = f.create(
				ServiceManagementService.class,
				new URL(url + "/services/ServiceManagement")
		);
		RequestAttributes attr = (RequestAttributes)s;
		attr.setUserId(username);
		attr.setPassword(password);

		// The data that will be rendered as JSON or passed to the html template.
		List<ServiceEntry> elements = new ArrayList();
		Map<String,Object> result = new HashMap();
		result.put("url", url);
		result.put("totalCount", 0);
		result.put("elements", elements);

		// The ServiceManager only allows us to fetch metadata for 100 services at a
		// time. So we have to be prepared to page through the entire list if more than
		// 100.
		int PAGE_SIZE = 100;
		int count = 0;
		ServiceEntrySearchResult more = s.searchServices(count, PAGE_SIZE, conditions, order, "ALL");
		while (more.getElements().length > 0) {
			ServiceEntry[] entries = more.getElements();
			for (ServiceEntry entry : entries) {
				elements.add(entry);
			}
			count += entries.length;
			more = s.searchServices(count, PAGE_SIZE, conditions, order, "ALL");
		}
		result.put("totalCount", count);
		return result;
	}
}

