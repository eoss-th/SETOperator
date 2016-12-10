package bean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class StockQuote {
	
	static Logger logger = Logger.getLogger("EOSS-TH");
	
	private static NumberFormat nf = new DecimalFormat("#,###");
	//12 Mar 2013 22:59:55
	private static DateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm:ss", Locale.US);
	
	private Date date;
	private boolean isMarketOpen;
	
	private double last;
	private double changedValue;
	private double changedPercent;
	private double prior;
	private double open;
	private double high;
	private double low;
	private int volumn;
	private int value;
	private double avg;
	private double ceiling;
	private double floor;
	private double bid;
	private int bidAmount;
	private double offer;
	private int offerAmount;
	
	private XD xd;
	
	public static class XD {
		
		public String dateString;
		
		public String dvd;
		
		public XD(String dateString, String dvd) {
			this.dateString = dateString;
			this.dvd = dvd;
		}
		
	}
	
	public static XD loadXD(String symbol) {
		try {
			URL url = new URL("http://www.set.or.th/set/companyrights.do?symbol=" + URLEncoder.encode(symbol, "UTF-8"));
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			
			String line=null, nextLine;
			while (true) {
				
				nextLine = in.readLine();
				
				if (nextLine==null) return null;
				
				if (nextLine.contains("XD")) break;
				
				line = nextLine;				
			}
			String dateString = line.substring(line.indexOf(">") + 1, line.lastIndexOf("<"));
			String value;
			while (true) {
				line = in.readLine();
				if (line==null) return null;
				if (line.contains("Dividend")) {
					line = in.readLine();
					if (line==null) return null;
					value = line.substring(line.indexOf(">") + 1, line.lastIndexOf("<"));
					break;
				}
			}
			
			return new XD(dateString, value);			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Date:" + df.format(date));
		sb.append("\n");
		sb.append("Market:" + isMarketOpen);
		sb.append("\n");
		sb.append("Last:" + last);
		sb.append("\n");
		sb.append("changedValue:" + changedValue);
		sb.append("\n");
		sb.append("changedPercent:" + changedPercent);
		sb.append("\n");
		sb.append("prior:" + prior);
		sb.append("\n");
		sb.append("open:" + open);
		sb.append("\n");
		sb.append("high:" + high);
		sb.append("\n");
		sb.append("low:" + low);
		sb.append("\n");
		sb.append("volumn:" + volumn);
		sb.append("\n");
		sb.append("value:" + value);
		sb.append("\n");
		sb.append("avg:" + avg);
		sb.append("\n");
		sb.append("ceiling:" + ceiling);
		sb.append("\n");
		sb.append("floor:" + floor);
		sb.append("\n");
		sb.append("bid:" + bid);
		sb.append("\n");
		sb.append("bidAmount:" + bidAmount);
		sb.append("\n");
		sb.append("offer:" + offer);
		sb.append("\n");
		sb.append("offerAmount:" + offerAmount);
		
		return sb.toString();
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isMarketOpen() {
		return isMarketOpen;
	}

	public void setMarketOpen(boolean isMarketOpen) {
		this.isMarketOpen = isMarketOpen;
	}

	public double getLast() {
		return last;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public double getChangedValue() {
		return changedValue;
	}

	public void setChangedValue(double changedValue) {
		this.changedValue = changedValue;
	}

	public double getChangedPercent() {
		return changedPercent;
	}

	public void setChangedPercent(double changedPercent) {
		this.changedPercent = changedPercent;
	}

	public double getPrior() {
		return prior;
	}

	public void setPrior(double prior) {
		this.prior = prior;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public int getVolumn() {
		return volumn;
	}

	public void setVolumn(int volumn) {
		this.volumn = volumn;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public double getAvg() {
		return avg;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}

	public double getCeiling() {
		return ceiling;
	}

	public void setCeiling(double ceiling) {
		this.ceiling = ceiling;
	}

	public double getFloor() {
		return floor;
	}

	public void setFloor(double floor) {
		this.floor = floor;
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public int getBidAmount() {
		return bidAmount;
	}

	public void setBidAmount(int bidAmount) {
		this.bidAmount = bidAmount;
	}

	public double getOffer() {
		return offer;
	}

	public void setOffer(double offer) {
		this.offer = offer;
	}

	public int getOfferAmount() {
		return offerAmount;
	}

	public void setOfferAmount(int offerAmount) {
		this.offerAmount = offerAmount;
	}

	private static void log(String name, Exception e) {
		logger.severe(name + ":" + e.getMessage());
	}
	
	public static StockQuote create(String symbol) throws Exception {
		
		URL url = new URL("http://marketdata.set.or.th/mkt/stockquotation.do?symbol=" + URLEncoder.encode(symbol, "UTF-8"));
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(url.openStream()));
		
		StockQuote quote = new StockQuote();
		
		String line;
		while (true) {
			
			line = in.readLine();
			if (line==null) break;
			
			if (line.contains("Bid Price")) {
				
				in.readLine();
				line = in.readLine().trim();
				
				try {
				quote.bid = Double.parseDouble(line.split(" ")[0]);	
				line = in.readLine().trim();
				quote.bidAmount = nf.parse(line).intValue();
				} catch (Exception e) {
					log("Bid Price", e);
				}
			}
			
			if (line.contains("Offer Price")) {
				
				in.readLine();
				line = in.readLine().trim();
				try {
				quote.offer = Double.parseDouble(line.split(" ")[0]);	
				line = in.readLine().trim();
				quote.offerAmount = nf.parse(line).intValue();
				} catch (Exception e) {
					log("Offer Price", e);
				}
			}
			
			if (line.contains("Last Update")) {
				line = line.trim();
				try {
				quote.date = df.parse(line.substring(12, line.indexOf("<")));
				quote.isMarketOpen = in.readLine().trim().substring(16).equals("Closed") == false;				
				} catch (Exception e) {
					log("Last Update", e);
				}
			}
			
			if (line.contains("<td>Last</td>")) {
				
				do {
					line = in.readLine().trim();
				} while (line.startsWith("-") == false && line.contains("</font>")==false);
				
				try {
				quote.last = Double.parseDouble(line.substring(0, line.indexOf("</strong>")));
				} catch (Exception e) {
					log("Last", e);
				}
			}
			
			if (line.contains("<td>Change</td>")) {
				
				do {
					line = in.readLine().trim();
				} while (line.startsWith("-") == false && line.contains("</font>")==false);
				
				try {
					if (line.startsWith("-"))
						quote.changedPercent = 0;						
					else if (line.startsWith("<font"))
						quote.changedValue = Double.parseDouble(line.substring(line.indexOf(">") + 1, line.indexOf("</")));
					else
						quote.changedValue = Double.parseDouble(line.substring(0, line.indexOf("</")));
							
				} catch (Exception e) {
					log("Change", e);
				}
			}
			
			if (line.contains("<td>%Change</td>")) {
				
				do {
					line = in.readLine().trim();
				} while (line.startsWith("-") == false && line.contains("</font>")==false);
				
				try {
					if (line.startsWith("-"))
						quote.changedPercent = 0;				
					else if (line.startsWith("<font"))
						quote.changedPercent = Double.parseDouble(line.substring(line.indexOf(">") + 1, line.indexOf("</")));
					else
						quote.changedPercent = Double.parseDouble(line.substring(0, line.indexOf("</")));
				} catch (Exception e) {
					log("%Change", e);
				}
			}
			
			if (line.contains("<td>Prior</td>")) {
				
				line = in.readLine().trim();
				try {
				quote.prior = Double.parseDouble(line.substring(4, line.indexOf("</")));	
				} catch (Exception e) {
					log("Prior", e);
				}
			}
			
			if (line.contains("<td>Open</td>")) {
				
				line = in.readLine().trim();
				try {
				quote.open = Double.parseDouble(line.substring(4, line.indexOf("</")));	
				} catch (Exception e) {
					log("Open", e);
				}
			}
			
			if (line.contains("<td>High</td>")) {
				
				line = in.readLine().trim();
				try {
				quote.high = Double.parseDouble(line.substring(4, line.indexOf("</")));	
				} catch (Exception e) {
					log("High", e);
				}
			}
			
			if (line.contains("<td>Low</td>")) {
				
				line = in.readLine().trim();
				try {
				quote.low = Double.parseDouble(line.substring(4, line.indexOf("</")));	
				} catch (Exception e) {
					log("Low", e);
				}
			}
			
			if (line.contains("(Shares)")) {
				
				line = in.readLine().trim();
				try {
				quote.volumn = nf.parse(line.substring(4, line.indexOf("</"))).intValue();	
				} catch (Exception e) {
					log("Shares", e);
				}
			}
			
			if (line.contains("('000 Baht)")) {
				
				line = in.readLine().trim();
				try {
				quote.value = nf.parse(line.substring(4, line.indexOf("</"))).intValue();	
				} catch (Exception e) {
					log("Value", e);
				}
			}
			
			if (line.contains("<td>Average Price</td>")) {
				
				line = in.readLine().trim();
				try {
				quote.avg = Double.parseDouble(line.substring(4, line.indexOf("</")));	
				} catch (Exception e) {
					log("Average Price", e);
				}
			}
			
			if (line.contains("<td>Ceiling</td>")) {
				
				line = in.readLine().trim();
				try {
				quote.ceiling = Double.parseDouble(line.substring(4, line.indexOf("</")));	
				} catch (Exception e) {
					log("Ceiling", e);
				}
			}
			
			if (line.contains("<td>Floor</td>")) {
				
				line = in.readLine().trim();
				try {
				quote.floor = Double.parseDouble(line.substring(4, line.indexOf("</")));	
				} catch (Exception e) {
					log("Floor", e);
				}
			}
		}
				
		return quote;	
	}
	
	public static void main(String [] args) throws Exception {
/**
 *  	
A	AREEYA PROPERTY PUBLIC COMPANY LIMITED	SET

AAV	ASIA AVIATION PUBLIC COMPANY LIMITED	SET

ABICO	ABICO HOLDINGS PUBLIC COMPANY LIMITED	SET

ACAP	ACAP ADVISORY PUBLIC COMPANY LIMITED	mai

ADAM	ADAMAS INCORPORATION PUBLIC COMPANY LIMITED	mai

ADVANC	ADVANCED INFO SERVICE PUBLIC COMPANY LIMITED	SET

AEONTS	AEON THANA SINSAP (THAILAND) PUBLIC COMPANY LIMITED	SET

AF	AIRA FACTORING PUBLIC COMPANY LIMITED	mai

AFC	ASIA FIBER PUBLIC COMPANY LIMITED	SET

AGE
 */
		XD xd;
		xd = StockQuote.loadXD("A");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("AAV");
		if (xd!=null)
			System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("ABICO");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("ACAP");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("ADAM");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("ADVANC");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("AEONTS");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("AF");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("AFC");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
		xd = StockQuote.loadXD("AGE");
		if (xd!=null)
		System.out.println(xd.dateString + ":" + xd.dvd);
		
	}

}
