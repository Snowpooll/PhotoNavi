package net.developapp.photonavi;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener,LocationListener,GoogleApiClient.ConnectionCallbacks {
	
	private static final int REQ_CAMERA = 0x00;
	private static final int REQ_GALLERY = 0x00;
	private Uri mUri;
	
	// googlemap object
	private GoogleMap googleMap =null;

	private LocationClient mLocationClient = null;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.currentmap);
		
		String providers = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (providers.indexOf("gps", 0) < 0) {
			AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
			alertDlg.setTitle("位置情報の設定ができていません");
			alertDlg.setMessage("このアプリを使うにはGPSが必要になります");
			alertDlg.setPositiveButton("位置情報をONにする",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					});
			alertDlg.setNegativeButton("キャンセル",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							// 何もしないので空白でOK
						}
					});
			alertDlg.create().show();
		} 
		
		findViewById(R.id.linkGallery).setOnClickListener(
				new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						Intent intent = new Intent();
						//Intent intent = new Intent(MainActivity.this,ExifGallery.class);
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(intent, REQ_GALLERY);
						
					}
				});

		googleMap =((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		if(googleMap !=null){ 
			googleMap.setMyLocationEnabled(true); 
		}
		 mLocationClient=new LocationClient(getApplicationContext(), this, this);
		 if(mLocationClient !=null){
			 mLocationClient.connect();
		 }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/*if (id == R.id.action_settings) {
			return true;
		}*/
		if(id == R.id.action_camera){

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			String path = Environment.getExternalStorageDirectory()+"/"+"capture.jpg";
			mUri =Uri.fromFile(new File(path));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
			startActivity(intent);
			return true;
		}
		if(id == R.id.action_send){
			Intent i = new Intent(getApplicationContext(),SendMail.class);
			startActivity(i);
		}

		//option で location setting
		if(id ==R.id.action_location){
			Intent intent =new Intent();
			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQ_GALLERY && resultCode == RESULT_OK){
			Uri uri =data.getData();
			

			Intent intent2 =new Intent(this,ExifGallery.class);
			intent2.putExtra("extra_uri",uri);
			startActivity(intent2);
		}
	}





	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLocationClient.requestLocationUpdates(REQUEST,this);
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		CameraPosition cameraPosition = new CameraPosition.Builder() .target(new LatLng(location.getLatitude(),
				location.getLongitude())).zoom(15.5f) .bearing(0).build();
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
