package com.gatedev.amountcircleselector;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gatedev.amountcircleselector.CustomCircleAmountSelector.OnTapListener;
import com.gatedev.amountcircleselector.CustomCircleAmountSelector.OnValueChangedListener;
import com.vb.amountcircleselector.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			float[] values = {10, 20, 30, 70, 50, 85};
			
			final CustomCircleAmountSelector comp = (CustomCircleAmountSelector) rootView.findViewById(R.id.comp);
			comp.setValues(values);
			comp.setProgressColor(getResources().getColor(android.R.color.holo_blue_dark));
			comp.setUnderstrokeColor(getResources().getColor(android.R.color.darker_gray));
			comp.setShowLines(true);
			comp.setShowProgress(true);
			comp.setListener(new OnValueChangedListener() {
				
				@Override
				public void onValueChanged(float value) {
					comp.setUpperText("Value: " + value);
				}
				
			});
			comp.setTapListener(new OnTapListener() {
				
				@Override
				public void onTap(float value) {
					Toast.makeText(getActivity(), "Do something!", Toast.LENGTH_SHORT).show();
				}
				
			});
			comp.setLowerText("TAP HERE!");
			comp.setLowerTextColor(Color.BLACK);
			comp.setUpperTextColor(Color.WHITE);
			
			Bitmap handler = BitmapFactory.decodeResource(getResources(), R.drawable.prelievo_drag_b);
			comp.setHandlerBitmap(handler);
			
			return rootView;
		}
	}
}
