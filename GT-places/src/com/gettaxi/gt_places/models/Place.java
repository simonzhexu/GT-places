package com.gettaxi.gt_places.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//According to google api document
//https://developers.google.com/places/documentation/search
public class Place {
	@Expose
	@SerializedName("formatted_address")
	private String formatted_address; //529 Kent Street, Sydney NSW, Australia
	@Expose
	private Geometry geometry;
	@Expose
	private String icon; //http://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png
	@Expose
	private String id;	//827f1ac561d72ec25897df088199315f7cbbc8ed
	@Expose
	private String name; //Tetsuya's
	@Expose
	private float rating; //4.10
	
	private String vicinity;
	
	public Place()
	{
		
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public String getIcon() {
		return icon;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}


	public float getRating() {
		return rating;
	}


	public String getFormatted_address() {
		return formatted_address;
	}

	public String getVicinity() {
		return vicinity;
	}
	
}
