package com.gettaxi.gt_places.models;

import java.util.ArrayList;

public class AutocompleteResult {
	private String status;
	private ArrayList<Prediction> predictions;
	
	public ArrayList<Prediction> getPredictions() {
		return predictions;
	}
	
	public String getStatus() {
		return status;
	}
	
}
