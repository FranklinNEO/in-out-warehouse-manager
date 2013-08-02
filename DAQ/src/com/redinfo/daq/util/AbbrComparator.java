package com.redinfo.daq.util;

import java.util.Comparator;
import java.util.HashMap;

@SuppressWarnings("rawtypes")
public class AbbrComparator implements Comparator {

	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		@SuppressWarnings("unchecked")
		String str1 = ((HashMap<String, String>) arg0).get("abbr");
		@SuppressWarnings("unchecked")
		String str2 = ((HashMap<String, String>) arg1).get("abbr");
		return str1.compareTo(str2);
	}

}
