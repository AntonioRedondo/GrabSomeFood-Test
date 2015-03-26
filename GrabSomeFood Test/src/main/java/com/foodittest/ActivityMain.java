package com.foodittest;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.animation.Transformation;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMain extends Activity {
	private boolean orientationChanged, firstTimeDone, loading, refreshing;
	private int soTimeout, connectionTimeout, statusBarHeight, navigationBarHeight, sWidth, sHeight, orientation, sSW,
				lenghtArrayY = 20, lenghtArrayX = 6, animDuration = 250;
	private float sD;
	private String urlEndPoint;
	private String[][] mMeals;
	private List<String[]> mOrders = new ArrayList<String[]>();
	private List<Map<String, Object>> mMealsList = new ArrayList<Map<String, Object>>(), mOrdersList = new ArrayList<Map<String, Object>>();
	private FrameLayout mLLoading, mLLoadingText;
	private LinearLayout mLOops, mLRestaurantTitle;
	private TextView mTVOopsSmall, mTVRestaurantTitle, mTVNoOrders;
	private TextSwitcher mTSOrders;
	private Button mBRetry, mBCheckout;
	private ImageView mIVTopBarArrow;
	private ListView mLVMeals;
	private EnhancedListView mLVEOrders;
	private SimpleAdapter mSAMeals, mSAOrders;
	private LoaderManager.LoaderCallbacks<String[][]> loader;
	private SlidingUpPanelLayout mLSliding;
	private ImageLoader mIL;
    private DisplayImageOptions mILOptions;
	private DecimalFormat mFormat = new DecimalFormat("####0.00");
	private SwipeRefreshLayout mLSwipeRefresh;
	private RequestStatus mRS;
	private static enum RequestStatus {
		SUCCESS,
		CACHED,
		ACTIVITY_RESTARTED,
		PLACE_NOT_FOUND,
		NO_INTERNET,
		TIMEOUT,
		BAD_JSON_RETURNED,
		OUT_OF_MEMORY,
		NOT_SUCCESSFUL_HTTP_STATUS_CODE,
		IMAGE_NOT_DECODED,
		INTERNAL_PROBLEM
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		Resources res = getResources();
		statusBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.status_bar_height, C.dimen, C.android));
		sWidth = res.getDisplayMetrics().widthPixels;
		sHeight = res.getDisplayMetrics().heightPixels;
		sD = res.getDisplayMetrics().density;
		orientation = res.getConfiguration().orientation;
		sSW = res.getConfiguration().smallestScreenWidthDp; // screen smallestWidth does NOT change with screen rotation
		
		
		soTimeout = Integer.valueOf(getString(R.string.so_timeout));
		connectionTimeout = Integer.valueOf(getString(R.string.connection_timeout));
		urlEndPoint = getString(R.string.url_end_point);
		
		
		mLLoading = (FrameLayout) findViewById(R.id.LLoading);
		mLLoadingText = (FrameLayout) findViewById(R.id.LLoadingText);
		mLOops = (LinearLayout) findViewById(R.id.LOopsSmall);
		mTVOopsSmall = (TextView) findViewById(R.id.TVOopsSmall);
		mBRetry = (Button) findViewById(R.id.BRetry);
		mBRetry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doInitialLoad();
			}
		});
		mBCheckout = (Button) findViewById(R.id.BCheckout);
		mBCheckout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ActivityMain.this, ActivityCheckout.class));
			}
		});
		
		mIVTopBarArrow = (ImageView) findViewById(R.id.IVTopBarArrow);
		mLSliding = (SlidingUpPanelLayout) findViewById(R.id.LSliding);
		mLSliding.setPanelSlideListener(new PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				mIVTopBarArrow.setRotation(180*slideOffset);
			}
			
			@Override
			public void onPanelExpanded(View panel) {
			}
			
			@Override
			public void onPanelCollapsed(View panel) {
			}
			
			@Override
			public void onPanelAnchored(View panel) {
			}
			
			@Override
			public void onPanelHidden(View panel) {
			}
		});

		mLRestaurantTitle = (LinearLayout) findViewById(R.id.LRestaurantTitle);
		mTVRestaurantTitle = (TextView) findViewById(R.id.TVRestaurantTitle);
		
		mLVMeals = (ListView) findViewById(R.id.LVMeals);
		mLVMeals.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (mLVMeals.getFirstVisiblePosition() == 0)
					mLRestaurantTitle.setVisibility(View.VISIBLE);
				else mLRestaurantTitle.setVisibility(View.INVISIBLE);
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mLVMeals.getFirstVisiblePosition() == 0)
					mLRestaurantTitle.setVisibility(View.VISIBLE);
				else mLRestaurantTitle.setVisibility(View.INVISIBLE);
			}
		});
		
		mTSOrders = (TextSwitcher) findViewById(R.id.TSOrders);
		mTSOrders.setInAnimation(this, R.anim.text_change_in);
		mTSOrders.setOutAnimation(this, R.anim.text_change_out);
		TextView tv1 = new TextView(this), tv2 = new TextView(this);
		tv1.setTextColor(res.getColor(R.color.white));
		tv2.setTextColor(res.getColor(R.color.white));
		FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp2.gravity = Gravity.CENTER_VERTICAL;
		mTSOrders.addView(tv2, lp2);
		mTSOrders.addView(tv1, lp2);
		
		mTVNoOrders = (TextView) findViewById(R.id.TVNoOrders);
		mLVEOrders = (EnhancedListView) findViewById(R.id.LVEOrders);
		mLVEOrders.setDismissCallback(new OnDismissCallback() {
			@Override
			public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
				mOrders.remove(position);
				SQLiteDatabase db = new DbOrders.Helper(ActivityMain.this).getWritableDatabase();
				db.delete(DbOrders.TABLE_NAME, DbOrders.DATE + " = \"" + mOrdersList.remove(position).get(C.cardODate) + "\"", null);
				db.close();
				updateCountersTV();
				return null;
			}
		});
		mLVEOrders.enableSwipeToDismiss();
		mLSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.LSwipeRefresh);
		mLSwipeRefresh.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshing = true;
				doInitialLoad();
			}
			
		});
		mLSwipeRefresh.setSize(SwipeRefreshLayout.LARGE);
		mLSwipeRefresh.setProgressViewEndTarget(false, (int) (90*sD));
		mLSwipeRefresh.setDistanceToTriggerSync((int) (50*sD));
		mLSwipeRefresh.setColorSchemeResources(R.color.RedBG, R.color.RedBG, R.color.RedBG, R.color.RedBG);
		
		
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			SlidingUpPanelLayout.LayoutParams lP = (SlidingUpPanelLayout.LayoutParams) ((LinearLayout) findViewById(R.id.LSlidingContent)).getLayoutParams();
			
			if (!ViewConfiguration.get(this).hasPermanentMenuKey()
					&& (orientation == Configuration.ORIENTATION_PORTRAIT || sSW > 560)) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

				navigationBarHeight = res.getDimensionPixelSize(res.getIdentifier(C.navigation_bar_height, C.dimen, C.android));
				
				FrameLayout nb = (FrameLayout) findViewById(R.id.LShadowNB);
				nb.setVisibility(View.VISIBLE); // For SDK_INT 19-20 the bg is a transparent drawable
				((FrameLayout.LayoutParams) nb.getLayoutParams()).height = navigationBarHeight;
				
				lP.height = sHeight;
			}
			
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTVRestaurantTitle.getLayoutParams();
			int topM = lp.topMargin;
			int bottomM = lp.bottomMargin;
			int leftM = lp.leftMargin;
			int rightM = lp.rightMargin;
			lp.setMargins(leftM, topM + statusBarHeight, rightM, bottomM);
			
			lp = (LinearLayout.LayoutParams) ((LinearLayout) findViewById(R.id.LTopBar)).getLayoutParams();
			topM = lp.topMargin;
			bottomM = lp.bottomMargin;
			leftM = lp.leftMargin;
			rightM = lp.rightMargin;
			lp.setMargins(leftM, topM + statusBarHeight, rightM, bottomM);

			((FrameLayout) findViewById(R.id.LSwipeRefreshFL)).setPadding(0, (int) (statusBarHeight + 65*sD), 0, 0);
			
			mLSliding.setPanelHeight((int) (65*sD + statusBarHeight));
		}
		
		
		mIL = ImageLoader.getInstance();
		mIL.init(new ImageLoaderConfiguration.Builder(this).build());
		mILOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.bg_card_picture)
				.showImageOnFail(R.drawable.bg_card_picture)
				.displayer(new FadeInBitmapDisplayer(500))
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.build();
		
		
		loader = new LoaderManager.LoaderCallbacks<String[][]>() {
			@Override
			public AsyncTaskLoaderCustomised<String[][]> onCreateLoader(int id, Bundle args) {
				// Any primitive variable set here (or in the parent Activity) will be null after screen rotation.
				// Non-primitive variables may still work (or not), but it could represent a memory leak. Better use setters
				return new AsyncTaskLoaderCustomised<String[][]>(ActivityMain.this) {
					private HttpGet httpGet;
					private String[][] results;
					
					private Integer connectionTimeout;
					private Integer soTimeout;
					private String url;
					
					@Override
					public void setInputData(Object... o) {
						connectionTimeout = (Integer) o[0];
						soTimeout = (Integer) o[1];
						url = (String) o[2];
					}
					
					@Override
					protected void onStartLoading() { // Called when the activity comes from onResume()
						if (results != null) {
							deliverResult(results);
						}
					}
					
					// All the references to ActivityMain member variables in the below loadInBackground() method will be nullifyed after
					// a screen rotation, but they are used before that moment could happen and before AndroidHttpClient.execute() makes the thread wait.
					@Override
					public String[][] loadInBackground() {
						String[][] reqString = new String[lenghtArrayY + 1][lenghtArrayX];
						
						// http://stackoverflow.com/questions/2326767/how-do-you-check-the-internet-connection-in-android
						NetworkInfo activeNetwork = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
						if (activeNetwork == null || !activeNetwork.isConnected()) {
							reqString[0][0] = RequestStatus.NO_INTERNET.toString();
						} else {	
							AndroidHttpClient httpClient = AndroidHttpClient.newInstance(getString(R.string.user_agent));
							HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), connectionTimeout);
							HttpConnectionParams.setSoTimeout(httpClient.getParams(), soTimeout);
							
							httpGet = new HttpGet(url);
							try {
								BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient.execute(httpGet);
								
//								try { // For testing and debugging
//									Thread.sleep(4000);
//								} catch (InterruptedException e1) {
//									e1.printStackTrace();
//								}
								
								int statusCode = httpResponse.getStatusLine().getStatusCode();
								if (statusCode >= 200 && statusCode < 300) {
									try {										
										String entityContent = null;
										
										// http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
										@SuppressWarnings("resource")
										Scanner s = new Scanner(httpResponse.getEntity().getContent()).useDelimiter("\\A");
									    if(s.hasNext())
									    	entityContent = s.next();
									    s.close();
									    
										JSONArray meals = new JSONObject(entityContent).getJSONArray("meals");
										JSONObject meal;
										if (meals != null) {
											for(int n=1; n<reqString.length; ++n) { // n=0 is reserved for the request status
												meal = meals.getJSONObject(n-1);
												reqString[n][0] = meal.getString(C.id);
												reqString[n][1] = meal.getString(C.name);
												reqString[n][2] = meal.getString(C.description);
												reqString[n][3] = meal.getString(C.price);
												reqString[n][4] = meal.getString(C.primaryImageUrl);
												reqString[n][5] = meal.getJSONArray(C.tags).toString();
											}
											reqString[0][0] = RequestStatus.SUCCESS.toString();
										} else reqString[0][0] = RequestStatus.BAD_JSON_RETURNED.toString();
									} catch (JSONException e) {
										reqString[0][0] = RequestStatus.BAD_JSON_RETURNED.toString();
										e.printStackTrace();
									} catch (IllegalStateException e) {
										reqString[0][0] = RequestStatus.INTERNAL_PROBLEM.toString();
										e.printStackTrace();
									}
								} else reqString[0][0] = RequestStatus.NOT_SUCCESSFUL_HTTP_STATUS_CODE.toString();
							} catch (IOException e) { // InterruptedIOException (ConnectTimeoutException, SocketTimeoutException), UnknownHostException
								reqString[0][0] = RequestStatus.TIMEOUT.toString();
								e.printStackTrace();
							} catch (Exception e) {
								reqString[0][0] = RequestStatus.INTERNAL_PROBLEM.toString();
								e.printStackTrace();
							}
							httpClient.close();
						}
						return reqString;
					}
					
					@Override
					public void deliverResult(String[][] data) {
						results = data;
						if (!isReset())
							super.deliverResult(data);
					}
					
					@Override
					protected void onReset() { // Called also after the second time the Activity is destroyed
						if (httpGet !=null) {
							httpGet.abort();
							httpGet = null;
						}
						results = null;
						
						connectionTimeout = null;
						soTimeout = null;
						url = null;
					}
					
					@SuppressLint("NewApi")
					@Override
					protected boolean onCancelLoad() { // Called on Loader.cancelLoad()
						if (httpGet !=null)
							httpGet.abort();
						if (Build.VERSION.SDK_INT >= 16)
							return super.onCancelLoad();
						else return true;
					}
					
					@Override
					public void onCanceled(String[][] data) { // Called after Loader.cancelLoad()
						if (Build.VERSION.SDK_INT < 16)
							onCancelLoad();
					}
				};
				
			}
			
			@Override
			public void onLoadFinished(Loader<String[][]> loader, String[][] result) { // Won't be called if the activity is onPause state
				mLSwipeRefresh.setRefreshing(false);
				loading = false;
				mRS = RequestStatus.valueOf(result[0][0]);
				if (mRS == RequestStatus.SUCCESS) {
					mMeals = new String[lenghtArrayY][lenghtArrayX];
					for (int n=0; n<mMeals.length; ++n) {
						mMeals[n] = result[n+1];
						mMealsList.add(mapDataForMealsList(mMeals[n][0], mMeals[n][1], mMeals[n][2], "£" + mMeals[n][3], mMeals[n][4], mMeals[n][5]));
					}
					
					SQLiteDatabase db = new DbMeals.Helper(ActivityMain.this).getWritableDatabase();
					db.delete(DbMeals.TABLE_NAME, null, null);
					ContentValues values;
					for (int n=0; n<mMeals.length; ++n) {
						values = new ContentValues();
						values.put(DbMeals.ID, mMeals[n][0]);
						values.put(DbMeals.NAME, mMeals[n][1]);
						values.put(DbMeals.DESCRIPTION, mMeals[n][2]);
						values.put(DbMeals.PRICE, mMeals[n][3]);
						values.put(DbMeals.IMAGE_URL, mMeals[n][4]);
						values.put(DbMeals.TAGS, mMeals[n][5]);
						db.insert(DbMeals.TABLE_NAME, null, values);
					}
					db.close();
				}
				getLoaderManager().restartLoader(C.loaderId, null, ActivityMain.this.loader);
				doPostInitialLoad();
			}
			
			@Override
			public void onLoaderReset(Loader<String[][]> loader) {
			}
		};
		
		
		getLoaderManager().initLoader(C.loaderId, null, loader);
		
		
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			firstTimeDone = savedInstanceState.getBoolean(C.firstTimeDone);
			refreshing = savedInstanceState.getBoolean(C.refreshing);
			loading = savedInstanceState.getBoolean(C.loading);
			if (loading && !refreshing)
				return;
			if (savedInstanceState.getInt(C.orientation) != orientation)
				orientationChanged = true;
			String requestStatus = savedInstanceState.getString(C.requestStatus);
			if (requestStatus != null)
				mRS = RequestStatus.valueOf(requestStatus);
			if (savedInstanceState.getStringArray(C.mMeals0) != null) {
				mMeals = new String[lenghtArrayY][lenghtArrayX];
				for (int n=0; n<mMeals.length; ++n) {
					mMeals[n] = savedInstanceState.getStringArray(C.mMeals + n);
					mMealsList.add(mapDataForMealsList(mMeals[n][0], mMeals[n][1], mMeals[n][2], "£" + mMeals[n][3], mMeals[n][4], mMeals[n][5]));
				}
			}
			mOrders = (List<String[]>) savedInstanceState.getSerializable(C.mOrders);
			if (mOrders != null && mOrders.size() != 0)
				for (int n=0; n<mOrders.size(); ++n)
					mOrdersList.add(mapDataForOrdersList(mOrders.get(n)[0], mOrders.get(n)[1], mOrders.get(n)[2], mOrders.get(n)[3],
														 mOrders.get(n)[4], mOrders.get(n)[5], mOrders.get(n)[6], mOrders.get(n)[7]));
			doPostInitialLoad();
			
		} else doInitialLoad();
	}
	
	
	
	
	
	@Override
	public void onBackPressed() {
        if(mLSliding.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            mLSliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
		else super.onBackPressed();
	}
	
	
	
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(C.refreshing, refreshing);
		outState.putBoolean(C.loading, loading);
		outState.putBoolean(C.firstTimeDone, firstTimeDone);
		outState.putInt(C.orientation, orientation);
		outState.putSerializable(C.mOrders, (Serializable) mOrders);
		if(loading && !refreshing)
			return;
		if (mRS != null)
			outState.putString(C.requestStatus, mRS.name());
		if (mMeals == null)
			return;
		if ((mRS == RequestStatus.SUCCESS || mRS == RequestStatus.CACHED) && mMeals[1][1] != null)
			for (int n=0; n<mMeals.length; ++n)
				outState.putStringArray(C.mMeals + n, mMeals[n]);
	}
	
	
	
	
	
	private void doInitialLoad() {
		mLOops.setVisibility(View.GONE);
		mLLoadingText.setVisibility(View.VISIBLE);

		loading = true;
		((AsyncTaskLoaderCustomised<Object>) getLoaderManager().getLoader(C.loaderId)).setInputData(connectionTimeout, soTimeout, urlEndPoint);
		getLoaderManager().getLoader(C.loaderId).forceLoad();
	}
	
	
	
	
	
	private void doPostInitialLoad() {
		if (mRS == RequestStatus.SUCCESS || mRS == RequestStatus.CACHED) {
			
			mSAMeals = new SimpleAdapterCustomised(this, mMealsList, R.layout.card_meal,
					new String[] { C.cardTitle, C.cardText, C.cardPrice },
					new int[] { R.id.TVCardTitle, R.id.TVCardText, R.id.TVCardPrice });
			mLVMeals.setAdapter(mSAMeals);
			mSAOrders = new SimpleAdapter(this, mOrdersList, R.layout.card_order,
					new String[] { C.cardOTitle, C.cardOPrice, C.cardOQuantity },
					new int[] { R.id.TVCardOTitle, R.id.TVCardOPrice, R.id.TVCardOQuantity });
			mLVEOrders.setAdapter(mSAOrders);

			// -------------------- Retrieveing orders data -------------------- //
			if (!firstTimeDone) {
				SQLiteDatabase db = new DbOrders.Helper(this).getReadableDatabase();
				Cursor cursor = db.query(DbOrders.TABLE_NAME,
						new String[] { DbOrders.ID, DbOrders.NAME, DbOrders.DESCRIPTION, DbOrders.PRICE,
								DbOrders.IMAGE_URL, DbOrders.TAGS, DbOrders.QUANTITY, DbOrders.DATE },
						null, null, null, null, null);
				if (cursor.moveToFirst()) {
					do {
						mOrders.add(new String[] { cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
												   cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7) });
						mOrdersList.add(mapDataForOrdersList(cursor.getString(0), cursor.getString(1), cursor.getString(2), "£" + cursor.getString(3),
								   							 cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7)));
					} while (cursor.moveToNext());
				}
				cursor.close();
				db.close();
				firstTimeDone = true;
			}
			// ---------------------------------------- //
			
			updateCountersTV();
			
			if (refreshing && orientationChanged)
				mLSwipeRefresh.setRefreshing(true); // This is useless, it doesn't show the spinner
			if (orientationChanged)
				mLLoading.setVisibility(View.GONE);
			else {
				int animDur = animDuration;
				if (orientationChanged)
					animDur = 0;
				mLLoading.animate().setDuration(animDur).translationXBy(-sWidth*sD);
			}
			
		} else if (!refreshing) {
			if (mRS == RequestStatus.NO_INTERNET)
				mTVOopsSmall.setText(getString(R.string.oops_no_internet));
			else if (mRS == RequestStatus.TIMEOUT)
				mTVOopsSmall.setText(getString(R.string.oops_timeout));
			else if (mRS == RequestStatus.NOT_SUCCESSFUL_HTTP_STATUS_CODE)
				mTVOopsSmall.setText(getString(R.string.oops_http_not_successful_response));
			else if (mRS == RequestStatus.BAD_JSON_RETURNED)
				mTVOopsSmall.setText(getString(R.string.oops_bad_json_returned));
			else if (mRS == RequestStatus.INTERNAL_PROBLEM)
				mTVOopsSmall.setText(getString(R.string.oops_internal_problem));
			else mTVOopsSmall.setText(getString(R.string.oops_unknown_problem));
			
			// -------------------- Retrieveing cached meals data when no connection -------------------- //
			SQLiteDatabase db = new DbMeals.Helper(this).getReadableDatabase();
			Cursor cursor = db.query(DbMeals.TABLE_NAME,
								new String[] { DbMeals.ID, DbMeals.NAME, DbMeals.DESCRIPTION, DbMeals.PRICE, DbMeals.IMAGE_URL, DbMeals.TAGS },
								null, null, null, null, null);
			if (cursor.moveToFirst()) {
				mMeals = new String[lenghtArrayY][lenghtArrayX];
				int position;
				do {
					position = cursor.getPosition();
					if (position < lenghtArrayY) {
						mMeals[position][0] = cursor.getString(0); // id
						mMeals[position][1] = cursor.getString(1); // name
						mMeals[position][2] = cursor.getString(2); // description
						mMeals[position][3] = cursor.getString(3); // price
						mMeals[position][4] = cursor.getString(4); // primaryImageUrl
						mMeals[position][5] = cursor.getString(5); // tags
					}
					mMealsList.add(mapDataForMealsList(cursor.getString(0), cursor.getString(1), cursor.getString(2),
												"£" + cursor.getString(3), cursor.getString(4), cursor.getString(5)));
				} while (cursor.moveToNext());
				
				mRS = RequestStatus.CACHED;
				doPostInitialLoad();
			} else {
				mLLoadingText.setVisibility(View.GONE);
				mLOops.setVisibility(View.VISIBLE);
			}
			cursor.close();
			db.close();
		}
		// ---------------------------------------- //
		
		refreshing = false;
		orientationChanged = false;
	}
	
	
	
	
	// http://stackoverflow.com/questions/12596199/android-how-to-set-onclick-event-for-button-in-list-item-of-listview
	@SuppressLint("SimpleDateFormat")
	public void onAddButtonClicked(View v) {
		int position = mLVMeals.getPositionForView(v);
		Toast.makeText(ActivityMain.this, "'" + mMeals[position][1] + "' " + getString(R.string.added), Toast.LENGTH_SHORT).show();
		
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		SQLiteDatabase db = new DbOrders.Helper(ActivityMain.this).getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DbOrders.ID, mMeals[position][0]);
		values.put(DbOrders.NAME, mMeals[position][1]);
		values.put(DbOrders.DESCRIPTION, mMeals[position][2]);
		values.put(DbOrders.PRICE, mMeals[position][3]);
		values.put(DbOrders.IMAGE_URL, mMeals[position][4]);
		values.put(DbOrders.TAGS, mMeals[position][5]);
		values.put(DbOrders.QUANTITY, "1");
		values.put(DbOrders.DATE, date);
		db.insert(DbOrders.TABLE_NAME, null, values);
		db.close();
		
		mOrders.add(new String[] { mMeals[position][0], mMeals[position][1], mMeals[position][2], mMeals[position][3], 
								   mMeals[position][4], mMeals[position][5], "1", date });
		mOrdersList.add(mapDataForOrdersList(mMeals[position][0], mMeals[position][1], mMeals[position][2], "£" + mMeals[position][3],
											 mMeals[position][4], mMeals[position][5], "1", date));
		updateCountersTV();
	}
	
	
	
	
	
	public void onRemoveButtonClicked(View v) {
		int position = mLVEOrders.getPositionForView(v);
		mOrders.remove(position);
		SQLiteDatabase db = new DbOrders.Helper(ActivityMain.this).getWritableDatabase();
		db.delete(DbOrders.TABLE_NAME, DbOrders.DATE + " = \"" + mOrdersList.remove(position).get(C.cardODate) + "\"", null);
		db.close();
		updateCountersTV();
	}
	
	
	
	
	
	// http://stackoverflow.com/questions/4946295/android-expand-collapse-animation
	public void onExpandButtonClicked(View v) {
		final TextView tv = (TextView) v.findViewById(R.id.TVCardText);
		if (tv.getHeight() > (int)(43*sD)) {
		    final int initialHeight = tv.getMeasuredHeight();
		    final int diff = initialHeight - (int) (42*tv.getContext().getResources().getDisplayMetrics().density);
		    Animation a = new Animation() {
		        @Override
		        protected void applyTransformation(float interpolatedTime, Transformation t) {
	                tv.getLayoutParams().height = initialHeight - (int) (diff * interpolatedTime);
	                tv.requestLayout();
		        }

		        @Override
		        public boolean willChangeBounds() {
		            return true;
		        }
		    };
		    a.setDuration(250);
		    tv.startAnimation(a);
		} else {
		    tv.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		    final int targetHeight = v.getMeasuredHeight();
		    final int diff = (int) (170*sD);
		    Animation a = new Animation() {
		        @Override
		        protected void applyTransformation(float interpolatedTime, Transformation t) {
		            tv.getLayoutParams().height = (int) (42*sD) + (int) (diff *interpolatedTime);
		            tv.requestLayout();
		        }

		        @Override
		        public boolean willChangeBounds() {
		            return true;
		        }
		    };
		    a.setDuration(250);
		    tv.startAnimation(a);
		}
	}
	
	
	
	
	
	private void updateCountersTV() {
		mSAOrders.notifyDataSetChanged();
		if (mOrders.isEmpty()) {
			mTSOrders.setText(getString(R.string.nothing_yet));
			mBCheckout.setText(getString(R.string.checkout));
			mTVNoOrders.setVisibility(View.VISIBLE);
			mTVNoOrders.animate().setDuration(animDuration).alpha(1f).start();
			mLVEOrders.setVisibility(View.GONE);
			return;
		}
		mTSOrders.setText(String.valueOf(mOrders.size()) + " " + getString(R.string.plates));
		float total = 0;
		for(String[] s : mOrders)
			total += Float.valueOf(s[3]);
		mBCheckout.setText(getString(R.string.checkout) + ": £" + mFormat.format(total));
		mTVNoOrders.setAlpha(0);
		mTVNoOrders.setVisibility(View.GONE);
		mLVEOrders.setVisibility(View.VISIBLE);
	}
	
	
	
	
	
	
	private Map<String, Object> mapDataForMealsList(String id, String name, String description, String price, String imageUrl, String tags) {
		Map<String, Object> entry = new HashMap<String, Object>();
		entry.put(C.cardId, id);
		entry.put(C.cardTitle, name);
		entry.put(C.cardText, description);
		entry.put(C.cardPrice, price);
		entry.put(C.cardImageUrl, imageUrl);
		entry.put(C.cardTags, tags);
		return entry;
	}
	
	
	
	
	
	
	private Map<String, Object> mapDataForOrdersList(String id, String name, String description, String price,
					String imageUrl, String tags, String quantity, String date) {
		Map<String, Object> entry = new HashMap<String, Object>();
		entry.put(C.cardOId, id);
		entry.put(C.cardOTitle, name);
		entry.put(C.cardOText, description);
		entry.put(C.cardOPrice, price);
		entry.put(C.cardOImageUrl, imageUrl);
		entry.put(C.cardOTags, tags);
		entry.put(C.cardOQuantity, quantity);
		entry.put(C.cardODate, date);
		return entry;
	}
	
	
	
	
	
	class SimpleAdapterCustomised extends SimpleAdapter {
		public SimpleAdapterCustomised(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Tag tag = null;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.card_meal, parent, false);
				tag = new Tag();
				tag.flCard = view;
				tag.tvCardTitle = (TextView) view.findViewById(R.id.TVCardTitle);
				tag.tvCardText = (TextView) view.findViewById(R.id.TVCardText);
				tag.tvCardPrice = (TextView) view.findViewById(R.id.TVCardPrice);
				tag.ivCardPicture = (ImageView) view.findViewById(R.id.IVCardPicture);
				view.setTag(tag);
			} else  tag = (Tag) view.getTag();
			
			int p = (int) (25*sD);
			if (position == 0) {
				view.setPadding(p, (int) (90*sD), p, p);
			} else if (position == mMealsList.size()-1) {
				view.setPadding(p, 0, p, p + navigationBarHeight);
			} else view.setPadding(p, 0, p, p);
			tag.tvCardTitle.setText(mMeals[position][1]);
			tag.tvCardText.setText(mMeals[position][2]);
			tag.tvCardPrice.setText("£" + mMeals[position][3]);
			mIL.displayImage(mMeals[position][4], tag.ivCardPicture, mILOptions);
			return view;
		}
		
		class Tag {
			TextView tvCardTitle, tvCardText, tvCardPrice;
			ImageView ivCardPicture;
			View flCard;
		}
	}
	
	
	
	
	
	abstract class AsyncTaskLoaderCustomised<D> extends AsyncTaskLoader<D> {
		public AsyncTaskLoaderCustomised(Context context) {
			super(context);
		}
		
		/**
		 * Allows to set arbitrary parameters that can be used to compose an URL
		 * without the need to created a new different AsyncTaskLoader for every single load.
		 */
		public abstract void setInputData(Object... o);
		
	}
	
}
