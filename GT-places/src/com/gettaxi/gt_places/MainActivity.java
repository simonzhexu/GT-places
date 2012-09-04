package com.gettaxi.gt_places;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.gettaxi.gt_places.models.AutocompleteResult;
import com.gettaxi.gt_places.models.Place;
import com.gettaxi.gt_places.models.PlaceDetails;
import com.gettaxi.gt_places.models.PlacesResult;
import com.gettaxi.gt_places.models.PlacesSearch;
import com.gettaxi.gt_places.models.Prediction;
import com.gettaxi.gt_places.models.ReverseGeocodeResult;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.gson.Gson;

public class MainActivity extends MapActivity {

	// Android-Query (AQuery) is a light-weight library for doing asynchronous
	// tasks and manipulating UI elements in Android.
	// AQuery is cool, check out http://code.google.com/p/android-query/
	private AQuery aq;
	private static final String TAG = "GT-places";
	private static final int INITIAL_ZOOM_LEVEL = 15;
	protected static final int AUTO_COMPLETE_MIN_LENGTH = 5;
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay myLocationOverlay;
	GeoPoint centerPoint;
	private String centerAddress;
	private String centerName = "Center";
	private PlacesResult placesResult;
	private AutocompleteResult autocompleteResult;
	private String reverseGeocodePrefix = "http://maps.googleapis.com/maps/api/geocode/json?sensor=true&components=locality&latlng=";
	private String AutocompleteRequestPrefix = "https://maps.googleapis.com/maps/api/place/autocomplete/json?sensor=false&types=geocode&key="
			+ PlacesSearch.apiKey + "&input=";
	private String placesDetailPrefix = "https://maps.googleapis.com/maps/api/place/details/json?key="
			+ PlacesSearch.apiKey + "&sensor=false&reference=";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		aq = new AQuery(this);
		MapView tempMapView = (MapView) this.findViewById(R.id.mapview);
		if (tempMapView != null) {
			this.mapView = tempMapView;
			this.mapController = mapView.getController();
		}
	}

	// **************************************************************************
	// This part handles some UI
	// **************************************************************************
	private void navigateToLocation() {
		mapController.animateTo(centerPoint);
		if (mapView.getZoomLevel() < INITIAL_ZOOM_LEVEL) {
			mapController.setZoom(INITIAL_ZOOM_LEVEL);
		}
	}

	private void displayLoading() {
		Toast.makeText(this, R.string.main_activity_loading_places,
				Toast.LENGTH_LONG).show();
	}

	private void finishLoading() {

	}

	private void displayError(String error) {
		Toast.makeText(this, error, Toast.LENGTH_LONG).show();
	}

	protected void hideSearch() {
		aq.id(R.id.auto_completion_list).gone();
		aq.id(R.id.content).visible();
	}

	protected void showSearch() {
		aq.id(R.id.auto_completion_list).visible();
		aq.id(R.id.content).gone();

	}

	private void showAddress() {
		aq.id(R.id.addressTitle).visible();
		aq.id(R.id.addressTitle).text(centerAddress);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		menu.findItem(R.id.menu_current_location).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem arg0) {
						getCurrentLocation();
						return false;
					}
				});

		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		// Configure the search info and add any event listeners
		configureSearchView(searchView);
		return true;
	}

	private void configureSearchView(final SearchView searchView) {

		searchView.setOnCloseListener(new OnCloseListener() {
			@Override
			public boolean onClose() {
				hideSearch();
				return false;
			}
		});

		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String arg0) {
				// do auto complete
				if (arg0.length() > AUTO_COMPLETE_MIN_LENGTH) {
					showSearch();
					fetchAutocompletes(arg0);
				}
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String arg0) {
				// do search, omitted due to auto completion capability
				// may have the need to be implemented in real application
				return false;
			}
		});

	}

	// **************************************************************************
	// This part handles current location fetch and reverse geocode
	// **************************************************************************
	public void getCurrentLocation() {
		Toast.makeText(this, R.string.main_activity_getting_location_toast,
				Toast.LENGTH_SHORT).show();
		this.myLocationOverlay = new MyLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
		mapView.getOverlays().add(myLocationOverlay);

		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				final GeoPoint myLocationPoint = myLocationOverlay
						.getMyLocation();
				runOnUiThread(new Runnable() {
					public void run() {
						centerPoint = myLocationPoint;
						navigateToLocation();
						reverseGeocode(centerPoint);
						fetchNearbyPlaces(centerPoint, true);
					}
				});
			}
		});
	}

	public void reverseGeocode(GeoPoint geopoint) {
		String geoPointString = String
				.valueOf((float) geopoint.getLatitudeE6() / 1E6)
				+ ","
				+ String.valueOf((float) geopoint.getLongitudeE6() / 1E6);
		String url = this.reverseGeocodePrefix + geoPointString;

		this.aq.ajax(url, byte[].class, this, "reverseGeocodeCallback");

	}

	public void reverseGeocodeCallback(String url, byte[] result,
			AjaxStatus status) {
		if (result != null && status.getCode() == 200) {
			Gson gson = new Gson();
			String resultString = new String(result);
			// pass this to activity level reference
			ReverseGeocodeResult reverseGeocodeResult = gson.fromJson(
					resultString, ReverseGeocodeResult.class);
			if (reverseGeocodeResult != null
					&& reverseGeocodeResult.getStatus().equals("OK")
					&& reverseGeocodeResult.getResults() != null) {
				this.centerAddress = reverseGeocodeResult.getResults().get(0)
						.getFormatted_address();
				showAddress();
			} else {
				displayError(reverseGeocodeResult.getStatus());
			}
		} else {
			displayError(status.getError());
		}
	}

	// **************************************************************************
	// This part handles places list search
	// **************************************************************************
	private void fetchNearbyPlaces(GeoPoint centerPoint, boolean useSensor) {
		displayLoading();
		hideSearch();
		PlacesSearch placesSearch = new PlacesSearch(centerPoint, useSensor);
		String URL = placesSearch.generateURL();
		Log.i(TAG, URL);

		this.aq.ajax(placesSearch.generateURL(), byte[].class, this,
				"placesCallback");
	}

	public void placesCallback(String url, byte[] result, AjaxStatus status) {
		if (result != null && status.getCode() == 200) {
			Gson gson = new Gson();
			String resultString = new String(result);
			// pass this to activity level reference
			placesResult = gson.fromJson(resultString, PlacesResult.class);
			displayPlacesResult();
		} else {
			displayError(status.getError());
		}
	}

	private void displayPlacesResult() {
		if (placesResult == null || placesResult.getResults().size() == 0) {
			Toast.makeText(this, R.string.main_activity_no_places_found_error,
					Toast.LENGTH_SHORT).show();
		} else {
			// populates the bottom list
			ArrayList<String> placesResultTextList = new ArrayList<String>();
			final ArrayList<GeoPoint> placesResultGeoPoints = new ArrayList<GeoPoint>();
			for (Place place : placesResult.getResults()) {
				placesResultTextList.add(place.getName());
				GeoPoint placeGeoPoint = new GeoPoint(Math.round((float) (place
						.getGeometry().getLocation().getLat() * 1E6)),
						Math.round((float) (place.getGeometry().getLocation()
								.getLng() * 1E6)));
				placesResultGeoPoints.add(placeGeoPoint);
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, placesResultTextList);
			aq.id(R.id.place_result_list).getListView().setAdapter(adapter);
			aq.id(R.id.place_result_list).visible();
			aq.id(R.id.place_result_list).getListView()
					.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							mapView.getController().setZoom(16);
							mapView.getController().animateTo(
									placesResultGeoPoints.get(arg2));
							mapView.getController().setZoom(18);
							AlertDialog.Builder dialog = new AlertDialog.Builder(
									MainActivity.this);
							dialog.setTitle(placesResult.getResults().get(arg2)
									.getName());
							dialog.setMessage(placesResult.getResults()
									.get(arg2).getVicinity());
							dialog.show();
						}
					});

			// populates the map with pins
			List<Overlay> mapOverlays = mapView.getOverlays();
			mapOverlays.clear();
			Drawable placeDrawable = this.getResources().getDrawable(
					R.drawable.ic_map_marker);
			CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(
					placeDrawable, this);
			for (int i = 0; i < placesResult.getResults().size(); i++) {
				Place place = placesResult.getResults().get(i);
				itemizedOverlay.addOverlay(new OverlayItem(
						placesResultGeoPoints.get(i), place.getName(), place
								.getVicinity()));
			}

			mapOverlays.add(itemizedOverlay);

			// add center point icon
			Drawable centerDrawable = this.getResources().getDrawable(
					R.drawable.ic_map_marker_center);
			CustomItemizedOverlay centerOverlay = new CustomItemizedOverlay(
					centerDrawable, this);
			centerOverlay.addOverlay(new OverlayItem(this.centerPoint,
					this.centerName, this.centerAddress));
			mapOverlays.add(centerOverlay);

		}
	}

	// **************************************************************************
	// This part handles autocomplete address search
	// **************************************************************************
	private void fetchAutocompletes(String input) {
		String url = this.AutocompleteRequestPrefix + URLEncoder.encode(input);

		this.aq.ajax(url, byte[].class, this, "autocompleteCallback");
	}

	public void autocompleteCallback(String url, byte[] result,
			AjaxStatus status) {
		if (result != null && status.getCode() == 200) {
			Gson gson = new Gson();
			String resultString = new String(result);
			// pass this to activity level reference
			autocompleteResult = gson.fromJson(resultString,
					AutocompleteResult.class);
			displayAutocompleteResult();
		} else {
			displayError(status.getError());
		}
	}

	private void displayAutocompleteResult() {
		ArrayList<String> autocompleteTextList = new ArrayList<String>();
		for (Prediction prediction : autocompleteResult.getPredictions()) {
			autocompleteTextList.add(prediction.getDescription());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, autocompleteTextList);
		aq.id(R.id.auto_completion_list).getListView().setAdapter(adapter);
		aq.id(R.id.auto_completion_list).getListView()
				.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						autocompletionSelected(arg2);
					}
				});
	}

	protected void autocompletionSelected(int arg2) {
		String url = this.placesDetailPrefix
				+ this.autocompleteResult.getPredictions().get(arg2)
						.getReference();
		this.aq.ajax(url, byte[].class, this, "placesDetailCallback");
	}

	// **************************************************************************
	// This part handles place detail
	// **************************************************************************
	public void placesDetailCallback(String url, byte[] result,
			AjaxStatus status) {
		if (result != null && status.getCode() == 200) {
			Gson gson = new Gson();
			String resultString = new String(result);
			// pass this to activity level reference
			PlaceDetails placeDetails = gson.fromJson(resultString,
					PlaceDetails.class);
			if (placeDetails.getStatus().equals("OK")) {
				if (placeDetails != null && placeDetails.getResult() != null) {
					Toast.makeText(this,
							placeDetails.getResult().getFormatted_address(),
							Toast.LENGTH_LONG).show();
					centerPoint = new GeoPoint(
							Math.round((float) (placeDetails.getResult()
									.getGeometry().getLocation().getLat() * 1E6)),
							Math.round((float) (placeDetails.getResult()
									.getGeometry().getLocation().getLng() * 1E6)));
					navigateToLocation();
					this.centerAddress = placeDetails.getResult()
							.getFormatted_address();
					showAddress();
					fetchNearbyPlaces(centerPoint, false);
				} else {
					displayError(this.getString(R.string.main_activity_no_places_found_error));
				}
			} else {
				displayError(placeDetails.getStatus());
			}
		} else {
			displayError(status.getError());
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
