package com.gettaxi.gt_places.models;

import java.util.ArrayList;


//According to google api document
//https://developers.google.com/places/documentation/search
public class PlacesResult {
	
	private ArrayList<Place> results;
	
	private String status;
	
	private ArrayList<String> html_attributions;
	
	public PlacesResult ()
	{
		setResults(new ArrayList<Place>());
	}

	public void setResults(ArrayList<Place> results) {
		this.results = results;
	}

	public ArrayList<Place> getResults() {
		return results;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setHtml_attributions(ArrayList<String> html_attributions) {
		this.html_attributions = html_attributions;
	}

	public ArrayList<String> getHtml_attributions() {
		return html_attributions;
	}
}
