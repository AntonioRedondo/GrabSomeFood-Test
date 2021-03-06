/*
 * 2014-2015 (C) Antonio Redondo
 * http://antonioredondo.com
 * https://github.com/AntonioRedondo/GrabSomeFood-Test
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package com.foodittest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DbMeals implements BaseColumns {
	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	public DbMeals() {}
	
	public static final String TABLE_NAME = "meals";
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String PRICE = "price";
	public static final String IMAGE_URL = "imageUrl";
	public static final String TAGS = "tags";
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ", ";
	
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " ("
		+ _ID + " INTEGER PRIMARY KEY" + COMMA_SEP
		+ ID + TEXT_TYPE + COMMA_SEP
		+ NAME + TEXT_TYPE + COMMA_SEP
		+ DESCRIPTION + TEXT_TYPE + COMMA_SEP
		+ PRICE + TEXT_TYPE + COMMA_SEP
		+ IMAGE_URL + TEXT_TYPE + COMMA_SEP
		+ TAGS + TEXT_TYPE + " )";
	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
	
	
	
	
	
	public static class Helper extends SQLiteOpenHelper {
		public static final int DATABASE_VERSION = 1; // If the database schema changes, the database version must be increased
		public static final String DATABASE_NAME = "meals.db";
		
		public Helper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_ENTRIES);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// This database is only a cache for online data, so its upgrade policy is to simply to discard the data and start over
			db.execSQL(SQL_DELETE_ENTRIES);
			onCreate(db);
		}
		
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onUpgrade(db, oldVersion, newVersion);
		}
	}
}