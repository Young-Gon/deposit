package com.seedit.deposit.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.seedit.deposit.util.CLog;

/**
 * Created by yg102 on 2016-07-13.
 */
public class UserDBHelper  extends SQLiteOpenHelper
{
	private static final String TABLE_NAME = "user";


	public enum scheme{
		_id("INTEGER","PRIMARY KEY AUTOINCREMENT"),
		name("TEXT","UNIQUE"),
		deposit("INTEGER"),
		inst_dt("INTEGER");

		private String m_type;
		private String m_option;

		scheme(String _type)
		{
			m_type=_type;
		}

		scheme(String _type,String _option)
		{
			this(_type);
			m_option=_option;
		}

		public String getType()
		{
			return m_type;
		}

		public String getOption()
		{
			return m_option;
		}

		public String toSchema()
		{
			String strSchema=name()+" "+m_type;
			if(m_option!=null)
				strSchema=strSchema+" "+m_option;

			return strSchema;
		}
	}

	public UserDBHelper(Context context, SQLiteDatabase.CursorFactory factory, int version)
	{
		super(context, TABLE_NAME, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		scheme[] enums= scheme.values();
		StringBuilder sb=new StringBuilder("CREATE TABLE "+ TABLE_NAME +" ("+enums[0].toSchema());
		for(int i=1;i<enums.length;i++)
			sb.append(", "+enums[i].toSchema());
		sb.append(");");

		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}

	public Cursor selectItem(SQLiteDatabase db, String feild, String where)
	{
		String strQuery="SELECT "+feild+" FROM "+TABLE_NAME;
		if (!TextUtils.isEmpty(where))
		{
			strQuery+=" WHERE "+where;
		}

		CLog.v("query=" + strQuery);

		return db.rawQuery(strQuery,null);
	}

	public int insertItem(SQLiteDatabase db, String strName, int deposit, long inst_dt)
			throws SQLiteConstraintException
	{
		String strQuery = "INSERT INTO " + TABLE_NAME + " ("+
				scheme.name+", "+
				scheme.deposit+", "+
				scheme.inst_dt+
				") VALUES (\""+
				strName+"\", "+
				deposit+", "+
				inst_dt+")";

		CLog.v("query=" + strQuery);

		db.execSQL(strQuery);

		Cursor cursor = selectItem(db, "_id",
				scheme.name + "=\"" + strName + "\" AND " +
						scheme.deposit + "=" + deposit + " AND " +
						scheme.inst_dt + "=" + inst_dt
		);

		cursor.moveToNext();
		return cursor.getInt(0);
	}

	public void updateItem(SQLiteDatabase db, int id, int deposit, long inst_dt)
			throws SQLiteConstraintException
	{
		String strQuery="UPDATE "+TABLE_NAME+" SET "+
				scheme.deposit+"="+deposit+","+
				scheme.inst_dt+"="+inst_dt+
				" WHERE "+
				scheme._id+"="+id;

		CLog.v("query=" + strQuery);

		db.execSQL(strQuery);
	}

	public void updateItem(SQLiteDatabase db, int id, String name, int deposit, long inst_dt)
			throws SQLiteConstraintException
	{
		String strQuery="UPDATE "+TABLE_NAME+" SET "+
				scheme.name+"=\""+name+"\","+
				scheme.deposit+"="+deposit+","+
				scheme.inst_dt+"="+inst_dt+
				" WHERE "+
				scheme._id+"="+id;

		CLog.v("query=" + strQuery);

		db.execSQL(strQuery);
	}

	public void deleteItem(SQLiteDatabase db, int id)
	{
		String strQuery = "DELETE FROM "+TABLE_NAME+" WHERE _id="+id;
		CLog.v("query=" + strQuery);

		db.execSQL(strQuery);
	}
}
