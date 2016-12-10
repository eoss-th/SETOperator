package com.th.eoss.setoperator;

public interface FilterListener {

	void onValue(String filterName, String string, String string2);

	void onClearValue(String filterName);

}
