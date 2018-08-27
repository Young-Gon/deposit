package com.seedit.deposit.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.acelcni.recyclerviewsupport.adapter.ArrayListRecycleViewAdapter;
import com.seedit.deposit.db.UserDBHelper;

import java.util.Date;

/**
 * Created by yg102 on 2016-07-14.
 */
public class UserData extends ArrayListRecycleViewAdapter.ViewType implements Parcelable
{
	public int id;
	public String name;
	public int deposit;
	public Date inst_dt;

	public UserData(Cursor cursor)
	{
		id = cursor.getInt(cursor.getColumnIndex(UserDBHelper.scheme._id.name()));
		name = cursor.getString(cursor.getColumnIndex(UserDBHelper.scheme.name.name()));
		deposit = cursor.getInt(cursor.getColumnIndex(UserDBHelper.scheme.deposit.name()));
		inst_dt = new Date(cursor.getInt(cursor.getColumnIndex(UserDBHelper.scheme.inst_dt.name())));
	}

	protected UserData(Parcel in)
	{
		id = in.readInt();
		name = in.readString();
		deposit = in.readInt();
	}

	public UserData()
	{

	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeInt(deposit);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public static final Creator<UserData> CREATOR = new Creator<UserData>()
	{
		@Override
		public UserData createFromParcel(Parcel in)
		{
			return new UserData(in);
		}

		@Override
		public UserData[] newArray(int size)
		{
			return new UserData[size];
		}
	};

	@Override
	public int getViewType()
	{
		if (name == null)
			return 1;

		return 0;
	}

}
