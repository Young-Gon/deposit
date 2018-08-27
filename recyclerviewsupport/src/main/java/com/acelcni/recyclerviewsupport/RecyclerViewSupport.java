package com.acelcni.recyclerviewsupport;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import junit.framework.Assert;

/**
 * Created by yg102 on 2016-07-13.
 */
public class RecyclerViewSupport
{

	protected RecyclerView m_recyclerView;
	protected View m_progress;
	protected View m_emptyView;
	protected TextView m_emptyMessage;
	protected RecyclerView.LayoutManager m_layoutManager;

	private Activity m_parentsActivity;

	private RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount)
		{
			super.onItemRangeInserted(positionStart, itemCount);
			onChanged();
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount)
		{
			super.onItemRangeRemoved(positionStart, itemCount);
			onChanged();
		}

		@Override
		public void onChanged() {
			RecyclerView.Adapter<?> adapter =  m_recyclerView.getAdapter();
			if(adapter != null && m_emptyView != null) {
				if(adapter.getItemCount() == 0) {
					m_emptyView.setVisibility(View.VISIBLE);
					if(m_progress!=null)
						m_progress.setVisibility(View.GONE);
					m_recyclerView.setVisibility(View.GONE);
				}
				else {
					m_emptyView.setVisibility(View.GONE);

					if(m_progress!=null)
						m_progress.setVisibility(View.GONE);
					m_recyclerView.setVisibility(View.VISIBLE);
				}
			}
			else if(adapter==null && m_emptyView!=null)
			{
				m_emptyView.setVisibility(View.GONE);
				if(m_progress!=null)
					m_progress.setVisibility(View.VISIBLE);
				m_recyclerView.setVisibility(View.GONE);
			}
		}
	};

	public static RecyclerViewSupport getInstance(Activity activity)
	{
		return new RecyclerViewSupport(activity);
	}

	public RecyclerViewSupport(Activity activity)
	{
		m_parentsActivity = activity;

		m_recyclerView = (RecyclerView) activity.findViewById(android.R.id.list);
		m_progress = activity.findViewById(android.R.id.progress);
		m_emptyView = activity.findViewById(android.R.id.empty);
		m_emptyMessage=(TextView)activity.findViewById(android.R.id.message);
		m_recyclerView.setHasFixedSize(true);
	}

	public void setAdapter(@NonNull RecyclerView.Adapter adapter)
	{
		m_layoutManager= m_recyclerView.getLayoutManager();
		Assert.assertNotNull(m_layoutManager);

		m_recyclerView.setAdapter(adapter);

		if(adapter != null)
		{
			adapter.registerAdapterDataObserver(emptyObserver);

			if(m_progress!=null)
				m_progress.setVisibility(View.GONE);
			emptyObserver.onChanged();
		}
		else
		{
			if(m_progress!=null)
				m_progress.setVisibility(View.VISIBLE);

			if(m_emptyView!=null)
				m_emptyView.setVisibility(View.GONE);

		}
	}

	public RecyclerView.Adapter getAdapter()
	{
		return m_recyclerView.getAdapter();
	}

	public void setEmptyMessage(CharSequence msg)
	{
		m_emptyMessage.setText(msg);
	}

	public RecyclerView getRecyclerView()
	{
		return m_recyclerView;
	}

	public View getProgress()
	{
		return m_progress;
	}

	public View getEmptyView()
	{
		return m_emptyView;
	}

	public TextView getEmptyMessage()
	{
		return m_emptyMessage;
	}

}
