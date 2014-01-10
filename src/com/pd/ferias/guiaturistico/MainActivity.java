package com.pd.ferias.guiaturistico;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pd.ferias.guiaturistico.R;

/**
 *  This class is used to search places using Places API using keywords like police,hospital etc.
 * 
 * @author Ezequely
 * @Date   07/1/2014
 *
 */
public class MainActivity extends Activity {

	private final String TAG = getClass().getSimpleName();
	private GoogleMap mMap;
	private String[] places;
	private LocationManager locationManager;
	private Location loc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initCompo();
		places = getResources().getStringArray(R.array.places);
		currentLocation();
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(ArrayAdapter.createFromResource(
				this, R.array.places, android.R.layout.simple_list_item_1),
				new ActionBar.OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						Log.e(TAG,
								places[itemPosition].toLowerCase().replace("-",
										"_"));
						if (loc != null) {
							mMap.clear();
							new GetPlaces(MainActivity.this,
									places[itemPosition].toLowerCase().replace(
											"-", "_").replace(" ", "_")).execute();
						}
						return true;
					}

				});
		
	}	
	
		
	private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> implements OnMyLocationButtonClickListener {

		private ProgressDialog dialog;
		private Context context;
		private String places;
		
		public GetPlaces(Context context, String places) {
			this.context = context;
			this.places = places;
		}

		@Override
		protected void onPostExecute(ArrayList<Place> result) {
			super.onPostExecute(result);
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            
            /*mMap.
            LatLng lat = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().title("My Location").position(lat)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));*/
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			for (int i = 0; i < result.size(); i++) {
				mMap.addMarker(new MarkerOptions()
						.title(result.get(i).getName())
						.position(
								new LatLng(result.get(i).getLatitude(), result
										.get(i).getLongitude()))
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
						.snippet(result.get(i).getVicinity()));
			}
			GPSTracker gps = new GPSTracker(getApplicationContext());
			Location my = gps.getLocation();
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(new LatLng(my.getLatitude(),my.getLongitude())) // Sets the center of the map to
											// Mountain View
					.zoom(15) // Sets the zoom
					.tilt(30) // Sets the tilt of the camera to 30 degrees
					.build(); // Creates a CameraPosition from the builder
			
			mMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage("Loading..");
			dialog.isIndeterminate();
			dialog.show();
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... arg0) {
			PlacesService service = new PlacesService(
					"AIzaSyCLghUWbmJuXdn-9uvfXFHI0WgwjpA1wIw");
			ArrayList<Place> findPlaces = service.findPlaces(loc.getLatitude(), // 28.632808
					loc.getLongitude(), places); // 77.218276

			for (int i = 0; i < findPlaces.size(); i++) {

				Place placeDetail = findPlaces.get(i);
				Log.e(TAG, "places : " + placeDetail.getName());
			}
			return findPlaces;
		}

		@Override
		public boolean onMyLocationButtonClick() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	private void initCompo() {
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void currentLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		String provider = locationManager
				.getBestProvider(new Criteria(), false);

		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null) {
			locationManager.requestLocationUpdates(provider, 0, 0, listener);
		} else {
			loc = location;
			new GetPlaces(MainActivity.this, places[0].toLowerCase().replace(
					"-", "_")).execute();
			Log.e(TAG, "location : " + location);
		}

	}

	private LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e(TAG, "location update : " + location);
			loc = location;
			locationManager.removeUpdates(listener);
		}
	};
	
}
