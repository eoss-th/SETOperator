package com.th.eoss.setoperator;

public interface SETOperatorListener {
	
	void onWatch(String symbol);

	void onExpired();

}
