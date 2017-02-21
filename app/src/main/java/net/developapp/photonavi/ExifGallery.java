package net.developapp.photonavi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ExifGallery extends Activity {
	private GoogleMap googleMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photomap);
		

		ImageView prev = (ImageView) findViewById(R.id.preview);

		//intent で取得したギャラリー画像を表示
		Bundle bundle = getIntent().getExtras();
		Uri uri = (Uri) bundle.get("extra_uri");
		InputStream in = null;
		try {
			in = getContentResolver().openInputStream(uri);
			Bitmap mBitmap = BitmapFactory.decodeStream(in);
			in.close();
			prev.setImageBitmap(mBitmap);
			
			ContentResolver contentResolver = ExifGallery.this.getContentResolver();
			String[] columns = { MediaStore.Images.Media.DATA };
			Cursor cursor = contentResolver.query(uri, columns, null, null, null);
			cursor.moveToFirst();
			String path = cursor.getString(0);
			cursor.close();
			
			TextView dateText = (TextView) findViewById(R.id.date);
			ExifInterface exifInterface = new ExifInterface(path);
			//日付け
			String dates = String.format("date: %s",
					exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
			dateText.setText(dates);
			
			// 緯度経度
			TextView latlngText = (TextView) findViewById(R.id.latlng);

			float[] latlong = new float[2];
			exifInterface.getLatLong(latlong);
			String info = String.format("latlong: %f, %f", latlong[0],
					latlong[1]);
			latlngText.setText(info);
			
			// 地図表示
			double lat = latlong[0];
			double lng = latlong[1];
			CameraPosition cameraPos = new CameraPosition.Builder()
					.target(new LatLng(lat, lng)).zoom(15.5f).bearing(0)
					.build();
			googleMap = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPos));
			
			//座標を格納
			LatLng location = new LatLng(lat, lng);

			//marker 設定
			MarkerOptions options = new MarkerOptions();
			options.position(location);
			googleMap.addMarker(options);
			
			// 住所

			TextView addressText =(TextView)findViewById(R.id.address);
			String addressvalue = null;
			Geocoder gccoder = new Geocoder(this, Locale.getDefault());
			try {
				List<Address> listAddress = gccoder.getFromLocation(lat,
						lng, 1);
				if (!listAddress.isEmpty()) {
					Address address = listAddress.get(0);
					StringBuilder sb = new StringBuilder();
					for (int i = 1; i <= address.getMaxAddressLineIndex(); i++) {
						String addressLine = address.getAddressLine(i);
						sb.append(addressLine);
						//.append("\n");
					}
					addressvalue = sb.toString();
					addressText.setText(addressvalue);
				}
			} catch (IOException e) {
				// TODO: handle exception
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	//navi button
	public void navimap(View view){
		TextView navitarget =(TextView)findViewById(R.id.address);
		
		String addressnavi =navitarget.getText().toString();

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("geo:0,0?q=" + addressnavi));
		startActivity(intent);
	}

}
