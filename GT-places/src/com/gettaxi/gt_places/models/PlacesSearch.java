package com.gettaxi.gt_places.models;

import com.google.android.maps.GeoPoint;

//According to google api document
//https://developers.google.com/places/documentation/search

public class PlacesSearch {
	private final GeoPoint centerPoint;
	private static final String prefix = "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String apiKeyPrefix = "key=";
	public static final String apiKey = "AIzaSyAyIX6267Eyr22KftAYhC4nH4OQP3VkUIM";
	private static final String locationPrefix = "location=";
	private static final String radiusPrefix = "radius=";
	private static final String radius = "1000";
	private static final String sensorPrefix = "sensor=";
	private boolean sensor;
	
	public PlacesSearch (GeoPoint centerPoint, boolean sensor)
	{
		this.centerPoint = new GeoPoint(
				centerPoint.getLatitudeE6(), 
				centerPoint.getLongitudeE6()
				);
		this.sensor = sensor;
	}
	
	public String generateURL()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(apiKeyPrefix);
		sb.append(apiKey);
		
		sb.append("&");
		
		sb.append(locationPrefix);
		sb.append(
				String.valueOf(centerPoint.getLatitudeE6()/1E6) + "," +
				String.valueOf(centerPoint.getLongitudeE6()/1E6)
				);
		
		sb.append("&");
		
		sb.append(radiusPrefix);
		sb.append(radius);
		
		sb.append("&");
		
		sb.append(sensorPrefix);
		sb.append(sensor ? "true" : "false");
		
		return sb.toString();
	}
}
