package com.seedit.deposit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acelcni.recyclerviewsupport.RecyclerViewSupport;
import com.acelcni.recyclerviewsupport.adapter.ArrayListRecycleViewAdapter;
import com.seedit.deposit.data.UserData;
import com.seedit.deposit.db.UserDBHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
	private static final int TITLE_MODE_SAVE=0;
	private static final int TITLE_MODE_USE=1;
	private String[] dialogTitle = new String[]{"적립", "사용"};

	private RecyclerViewSupport m_recyclerviewSupporter;
	private UserDBHelper m_dbHelper;
	private SQLiteDatabase m_database;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_recyclerviewSupporter = RecyclerViewSupport.getInstance(this);
		m_recyclerviewSupporter.setEmptyMessage("사용자가 없습니다.\n\n우측 상단의 \'추가\' 버튼을 눌러\n사용자를 추가해 보세요");
		m_recyclerviewSupporter.getRecyclerView().setLayoutManager(new LinearLayoutManager(this));


		m_dbHelper = new UserDBHelper(this, null, 1);
		m_database = m_dbHelper.getReadableDatabase();

		Cursor cursor = m_dbHelper.selectItem(m_database, "*", null);
		ArrayList<UserData> list = new ArrayList<>();
		while (cursor.moveToNext())
		{
			UserData data = new UserData(cursor);

			list.add(data);
		}
		cursor.close();

		ArrayListRecycleViewAdapter adapter = new ArrayListRecycleViewAdapter<>(R.layout.item_user, list, UserViewHolder.class);

		m_recyclerviewSupporter.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.add_user, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_add:
				Intent intent = new Intent(this, DetailActivity.class);

				startActivityForResult(intent,DetailActivity.INTENT_ID);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy()
	{
		m_database.close();
		m_dbHelper.close();

		super.onDestroy();
	}

	public void showAlertDialog(final int mode,final UserData userData)
	{
		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);

		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(dialogTitle[mode])
				.setView(editText)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						String strMoney = editText.getText().toString();
						if (TextUtils.isEmpty(strMoney))
						{
							return;
						}

						if (mode == TITLE_MODE_SAVE)
							userData.deposit += Integer.parseInt(strMoney);
						else
							userData.deposit -= Integer.parseInt(strMoney);

						m_dbHelper.updateItem(m_database, userData.id, userData.deposit, System.currentTimeMillis());

						ArrayListRecycleViewAdapter adapter= (ArrayListRecycleViewAdapter) m_recyclerviewSupporter.getAdapter();
						adapter.notifyItemChanged(adapter.getList().indexOf(userData));
					}
				})
				.create();

		editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				String strMoney = editText.getText().toString();
				if (TextUtils.isEmpty(strMoney))
				{
					return true;
				}

				if (mode == TITLE_MODE_SAVE)
					userData.deposit += Integer.parseInt(strMoney);
				else
					userData.deposit -= Integer.parseInt(strMoney);

				dialog.dismiss();
				m_dbHelper.updateItem(m_database, userData.id, userData.deposit, System.currentTimeMillis());

				ArrayListRecycleViewAdapter adapter= (ArrayListRecycleViewAdapter) m_recyclerviewSupporter.getAdapter();
				adapter.notifyItemChanged(adapter.getList().indexOf(userData));
				return true;
			}
		});
		Window window = dialog.getWindow();
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		dialog.show();
	}

	private void deleteItem(UserData userData)
	{
		m_dbHelper.deleteItem(m_database, userData.id);

		ArrayListRecycleViewAdapter adapter= (ArrayListRecycleViewAdapter) m_recyclerviewSupporter.getAdapter();
		int position=adapter.getList().indexOf(userData);

		adapter.getList().remove(position);
		if(adapter.getItemCount()==1)
		{
			adapter.notifyItemChanged(0);
		}
		adapter.notifyItemRemoved(position);
		adapter.notifyItemRangeChanged(position, adapter.getItemCount());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// 사용자가 추가 되거나 수정 되거나 삭제 될 경우 갱신
		if(resultCode==RESULT_CANCELED)
			return;

		UserData userData = data.getParcelableExtra(DetailActivity.INTENT_KEY_USER_DATA);
		ArrayListRecycleViewAdapter<UserData> adapter = (ArrayListRecycleViewAdapter) m_recyclerviewSupporter.getAdapter();
		switch (resultCode)
		{
			case R.id.action_apply:  // 추가
				adapter.add(userData);
				if(adapter.getItemCount()==2)
				{
					adapter.notifyItemChanged(0);
				}
				break;
			case R.id.action_modify:  // 수정
				ArrayList<UserData> list = adapter.getList();
				//for(UserData item: list)
				for(int i=0;i<list.size();i++)
				{
					UserData item = list.get(i);
					if (item.id == userData.id)
					{
						item.name = userData.name;
						item.deposit = userData.deposit;
						item.inst_dt = userData.inst_dt;
						adapter.notifyItemChanged(i);
						break;
					}
				}
				break;
			case R.id.action_delete: // 삭제
				list = adapter.getList();
				for(int i=0;i<list.size();i++)
				{
					UserData item = list.get(i);
					if(item.id==userData.id)
					{
						list.remove(item);
						if(adapter.getItemCount()==1)
						{
							adapter.notifyItemChanged(0);
						}
						adapter.notifyItemRemoved(i);
						adapter.notifyItemRangeChanged(i, adapter.getItemCount());
						break;
					}
				}
				break;
		}
	}

	public class UserViewHolder extends ArrayListRecycleViewAdapter.BindViewHolder<UserData> implements View.OnLongClickListener
	{
		private final TextView txtName;
		private final TextView txtDeposit;
		private final LinearLayout txtHelp;

		public UserViewHolder(View itemView,int viewType)
		{
			super(itemView,viewType);
			// TODO viewType==1 인경우 처리

			txtName = (TextView) itemView.findViewById(R.id.name);
			txtDeposit = (TextView) itemView.findViewById(R.id.deposit);
			txtHelp= (LinearLayout) itemView.findViewById(R.id.help);
			itemView.findViewById(R.id.use).setOnClickListener(this);
			itemView.findViewById(R.id.save).setOnClickListener(this);

			itemView.setOnLongClickListener(this);
		}

		@Override
		public void bind(UserData item, int position)
		{
			txtName.setText(item.name);
			txtDeposit.setText(String.format("%,d 원",item.deposit));
			if(m_recyclerviewSupporter.getAdapter().getItemCount()==1)
				txtHelp.setVisibility(View.VISIBLE);
			else
				txtHelp.setVisibility(View.GONE);
		}

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
				case R.id.use:
					showAlertDialog(TITLE_MODE_USE, item);
					break;
				case R.id.save:
					showAlertDialog(TITLE_MODE_SAVE,item);
					break;
				default:
					Intent intent = new Intent(v.getContext(), DetailActivity.class);
					intent.putExtra(DetailActivity.INTENT_KEY_USER_DATA, item);

					((Activity)getContext()).startActivityForResult(intent,DetailActivity.INTENT_ID);
			}
		}

		@Override
		public boolean onLongClick(View v)
		{
			new AlertDialog.Builder(getContext()).setMessage("'"+item.name+"'님을 삭제하시겠습니까?")
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							((MainActivity) getContext()).deleteItem(item);
						}
					})
					.setNegativeButton(android.R.string.cancel, null)
					.show();

			return true;
		}
	}
}
