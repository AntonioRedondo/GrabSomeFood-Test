/*
 * 2014-2015 (C) Antonio Redondo
 * http://antonioredondo.com
 * https://github.com/AntonioRedondo/GrabSomeFood-Test
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package com.foodittest;

import android.app.Activity;
import android.os.Bundle;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityCheckout extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checkout);
		
		final Resources res = getResources();
		
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			
			float sSW = res.getConfiguration().smallestScreenWidthDp;
			
			LinearLayout l = (LinearLayout) findViewById(R.id.LParent);
			int statusBarHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
			int navigationBarHeight = 0;
			
			if (!ViewConfiguration.get(this).hasPermanentMenuKey()
					&& (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier("navigation_bar_height", "dimen", "android"));
				FrameLayout nb = (FrameLayout) findViewById(R.id.LNavigationBar);
				nb.setVisibility(View.VISIBLE);
				((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
			}
			
			l.setPadding(0, statusBarHeight, 0, navigationBarHeight);
		}
		
		// http://stackoverflow.com/questions/4790746/links-in-textview
		Linkify.addLinks((TextView) findViewById(R.id.TVCheckoutTitle), Linkify.WEB_URLS);
		Linkify.addLinks((TextView) findViewById(R.id.TVCheckoutText), Linkify.EMAIL_ADDRESSES);
		
		((Button) findViewById(R.id.BGooglePlay)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()))
							.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.google_play_app_site)))
							.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
				}
			}
		});
	}
}