package com.lpoezy.nexpa.objects;

import android.graphics.Bitmap;

public class Announcement {
	private boolean isMine;
	// BROAD_ID, BROADCAST_TYPE, BROADCAST_FROM,
	// BROADCAST_MESSAGE, BROADCAST_DATE, BROADCAST_LOCATION_LONG,
	// BROADCAST_LOCATION_LAT, BROADCAST_LOCATION_LOCAL, BROADCAST_REACH,
	// BROADCAST_STATUS

	//private long id;
	//private int type;
	private String from;
	private String body;
	private String date;
	//private long locLongitude;
	//private long locLatitude;
	private String locLocal;
	private int reach;
	private String itemId;
	private Bitmap DP;
	//private int status;
	
	public Announcement(){}

//	public Announcement(long id, int type, int from, String body, String date, long locLongitude, long locLatitude,
//			String locLocal, int reach, int status) {
//
//		this.id = id;
//		this.type = type;
//		this.from = type;
//		this.body = body;
//		this.date = date;
//		this.locLongitude = locLongitude;
//		this.locLatitude = locLatitude;
//		this.locLocal = locLocal;
//		this.reach = reach;
//		this.status = status;
//	}

	public Announcement(String from, String message, String date,
						String locLocal, int reach, boolean isMine) {


		this.from = from;
		this.body = message;
		this.date = date;

		this.locLocal = locLocal;
		this.reach = reach;
		this.isMine = isMine;

	}


	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLocLocal() {
		return locLocal;
	}

	public void setLocLocal(String locLocal) {
		this.locLocal = locLocal;
	}

	public void setDP(Bitmap DP) {
		this.DP = DP;
	}
	public int getReach() {
		return reach;
	}

	public void setReach(int reach) {
		this.reach = reach;
	}

	public boolean isMine() {
		return isMine;
	}

	public void setIsMine(boolean isMine) {
		this.isMine = isMine;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}


	public Bitmap getDP() {
		return DP;
	}
}
