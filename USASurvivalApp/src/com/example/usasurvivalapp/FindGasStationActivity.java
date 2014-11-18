package com.example.usasurvivalapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.usasurvivalapp.my_gas_feed.model.Station;

public class FindGasStationActivity extends Activity {

	StationsAdapter adapter = null;

	private static final String productionAPIDomain = "api.mygasfeed.com";
	private static final String productionAPIKey = "os28ajqvrv";
	private static final String developmentAPIDomain = "devapi.mygasfeed.com";
	private static final String developmentAPIKey = "rfej9napna";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_findgasstations);

		final ListView listView = (ListView) findViewById(R.id.listView);
		adapter = new StationsAdapter(new LinkedList<Station>(),
				getApplication());
		listView.setAdapter(adapter);

		// /stations/radius/(Latitude)/(Longitude)/(distance)/(fuel type)/(sort
		// by)/apikey.json
		/*
		 * Arguments: Latitude Longitude Distance - A number (miles) of the
		 * radius distance of stations according to the user's geo location Fuel
		 * Type - Argument types: reg,mid,pre or diesel. Which type of gas
		 * prices that will be returned. Sort By (Distance or Price) - Type
		 * arguments: price or distance. Gas stations will be sorted according
		 * to the argument. apikey - An Api key
		 */
		String example = "http://devapi.mygasfeed.com/stations/radius/33.770050/-118.193739/3/reg/price/rfej9napna.json";
		new RequestTask().execute(example);

	}

	class RequestTask extends AsyncTask<String, String, List<Station>> {

		private final ProgressDialog dialog = new ProgressDialog(
				FindGasStationActivity.this);

		@Override
		protected List<Station> doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			LinkedList<Station> stations = null;
			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();

					// parse json!
					// TODO status
					// TODO geoLocation

					// Stations
					JSONObject obj = new JSONObject(out.toString());
					JSONArray jsonArrayStations = obj.getJSONArray("stations");

					System.out.println("jsonArrayStations "
							+ jsonArrayStations.length());
					stations = new LinkedList<Station>();
					for (int i = 0; i < jsonArrayStations.length(); i++) {
						Station station = convertStation(jsonArrayStations
								.getJSONObject(i));
						stations.add(station);
					}

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// TODO Handle problems..
			} catch (JSONException e) {
				// TODO Handle problems..
			} catch (IOException e) {
				// TODO Handle problems..
			}
			return stations;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setMessage("Searching gas-stations...");
			dialog.show();
		}

		@Override
		protected void onPostExecute(List<Station> result) {
			super.onPostExecute(result);
			dialog.dismiss();

			adapter.setItemList(result);
			adapter.notifyDataSetChanged();
		}

		private Station convertStation(JSONObject obj) throws JSONException {
			String country = obj.getString("country");
			String reg_price = obj.getString("reg_price");
			String mid_price = obj.getString("mid_price");
			String pre_price = obj.getString("pre_price");
			String diesel_price = obj.getString("diesel_price");
			String address = obj.getString("address");
			String diesel = obj.getString("diesel");
			String id = obj.getString("id");
			String lat = obj.getString("lat");
			String lng = obj.getString("lng");
			String station = obj.getString("station");
			String region = obj.getString("region");
			String city = obj.getString("city");
			String reg_date = obj.getString("reg_date");
			String mid_date = obj.getString("mid_date");
			String pre_date = obj.getString("pre_date");
			String diesel_date = obj.getString("diesel_date");
			String distance = obj.getString("distance");
			// TODO check reg_price, reg_date
			return new Station(id, country, reg_price, address, diesel, lat,
					lng, station, region, city, reg_date, distance);
		}
	}

	public class StationsAdapter extends ArrayAdapter<Station> {

		private List<Station> itemList;
		private Context context;

		public StationsAdapter(List<Station> itemList, Context ctx) {
			super(ctx, android.R.layout.simple_list_item_1, itemList);
			this.itemList = itemList;
			this.context = ctx;
		}

		public int getCount() {
			if (itemList != null)
				return itemList.size();
			return 0;
		}

		public Station getItem(int position) {
			if (itemList != null)
				return itemList.get(position);
			return null;
		}

		public long getItemId(int position) {
			if (itemList != null)
				return itemList.get(position).hashCode();
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			v = null;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.listviewitem_station, null);
			}

			final Station s = itemList.get(position);
			TextView text = (TextView) v
					.findViewById(R.id.listViewItemTextViewStation);
			text.setText(s.getStation());

			TextView text1 = (TextView) v
					.findViewById(R.id.listViewItemTextViewAddress);
			text1.setText(s.getAddress());

			TextView text2 = (TextView) v
					.findViewById(R.id.listViewItemTextViewDistance);
			text2.setText(s.getDistance());

			TextView text3 = (TextView) v
					.findViewById(R.id.listViewItemTextViewRegPrice);
			text3.setText(s.getPrice());

			Button b = (Button) v.findViewById(R.id.listViewItemButtonGoNavi);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW,
							Uri.parse("google.navigation:q=" + s.lat + ","
									+ s.lng));
					startActivity(i);
				}
			});

			return v;

		}

		public List<Station> getItemList() {
			return itemList;
		}

		public void setItemList(List<Station> itemList) {
			this.itemList = itemList;
		}

	}
}