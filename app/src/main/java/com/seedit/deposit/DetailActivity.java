package com.seedit.deposit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.seedit.deposit.data.UserData;
import com.seedit.deposit.db.UserDBHelper;

import java.util.Date;

public class DetailActivity extends AppCompatActivity
{
	public static final int INTENT_ID=0;
	public static final String INTENT_KEY_USER_DATA = "user_data";

	private EditText m_textName;
	private EditText m_textDeposit;
	private UserData m_userData;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		m_textName = (EditText) findViewById(R.id.user_name);
		m_textDeposit= (EditText) findViewById(R.id.deposit);
		m_textDeposit.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				jobDone(m_userData==null?R.id.action_apply:R.id.action_modify);
				return true;
			}
		});

		m_userData = getIntent().getParcelableExtra(INTENT_KEY_USER_DATA);
		if (m_userData != null)
		{
			m_textName.append(m_userData.name);
			m_textDeposit.setText(m_userData.deposit + "");

			setTitle("사용자 수정 - " + m_userData.name);
		}

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setResult(RESULT_CANCELED);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if(m_userData==null)
			getMenuInflater().inflate(R.menu.apply, menu);
		else
			getMenuInflater().inflate(R.menu.modify, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(jobDone(item.getItemId()))
			return true;

		return super.onOptionsItemSelected(item);
	}

	public boolean jobDone(int action)
	{
		switch (action)
		{
			case R.id.action_modify:
			case R.id.action_apply:
				String strName = m_textName.getText().toString();
				String strDeposit = m_textDeposit.getText().toString();
				if(TextUtils.isEmpty(strName))
				{
					Toast.makeText(this,"사용자 이름을 입력하세요.",Toast.LENGTH_SHORT).show();
					m_textName.requestFocus();
					m_textName.selectAll();

					return true;
				}
				if(TextUtils.isEmpty(strDeposit))
				{
					strDeposit="0";
				}

				if(m_userData==null)
					m_userData = new UserData();

				m_userData.name=strName;
				m_userData.deposit = Integer.parseInt(strDeposit);
				m_userData.inst_dt=new Date(System.currentTimeMillis());

				UserDBHelper dbHelper = new UserDBHelper(this, null, 1);
				SQLiteDatabase database = dbHelper.getWritableDatabase();

				try
				{
					if(action==R.id.action_apply)
					{
						m_userData.id = dbHelper.insertItem(database, m_userData.name, m_userData.deposit, m_userData.inst_dt.getTime());
					}else
						dbHelper.updateItem(database,m_userData.id,m_userData.name,m_userData.deposit,m_userData.inst_dt.getTime());
				} catch (SQLiteConstraintException e)
				{
					Toast.makeText(this,"같은 사용자 명은 사용할 수 없습니다.",Toast.LENGTH_SHORT).show();
					m_textName.requestFocus();
					m_textName.selectAll();
					database.close();
					dbHelper.close();

					return true;
				}

				database.close();
				dbHelper.close();

				Intent intent = new Intent();
				intent.putExtra(INTENT_KEY_USER_DATA, m_userData);
				setResult(action, intent);
				finish();
				break;
			case R.id.action_delete:
				new AlertDialog.Builder(this).setMessage("'" + m_userData.name + "'님을 삭제하시겠습니까?")
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								UserDBHelper dbHelper = new UserDBHelper(DetailActivity.this, null, 1);
								SQLiteDatabase database = dbHelper.getWritableDatabase();

								dbHelper.deleteItem(database, m_userData.id);

								database.close();
								dbHelper.close();

								Intent intent = new Intent();
								intent.putExtra(INTENT_KEY_USER_DATA, m_userData);
								setResult(R.id.action_delete, intent);
								finish();
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
				break;
			default:
				return false;
		}
		return true;
	}
}
