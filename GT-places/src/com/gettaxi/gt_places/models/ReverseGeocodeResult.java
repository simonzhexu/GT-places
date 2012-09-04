package com.gettaxi.gt_places.models;

import java.util.ArrayList;

public class ReverseGeocodeResult {
	private String status;
	private ArrayList<AddressComponents> results;

	public ArrayList<AddressComponents> getResults() {
		return results;
	}

	public String getStatus() {
		return status;
	}
}
