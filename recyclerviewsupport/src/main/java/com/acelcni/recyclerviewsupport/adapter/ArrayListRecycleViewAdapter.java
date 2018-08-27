package com.acelcni.recyclerviewsupport.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 이영곤 on 2015-08-19.
 */
public class ArrayListRecycleViewAdapter<T extends ArrayListRecycleViewAdapter.ViewType> extends RecyclerView.Adapter<ArrayListRecycleViewAdapter.BindViewHolder>
{
	protected ArrayList<T> m_list;
	private final int[] m_resID;
	private Class<? extends  ArrayListRecycleViewAdapter.BindViewHolder>[] m_vhClass;
	private ArrayList<T> m_originalList;
	private FilterListener m_filterListener;

	public ArrayListRecycleViewAdapter(@LayoutRes int resID, Class<? extends  ArrayListRecycleViewAdapter.BindViewHolder> vhClass)
	{
		this(resID, null,vhClass);
	}

	public ArrayListRecycleViewAdapter(@LayoutRes int resID, ArrayList<T> list, Class<? extends  ArrayListRecycleViewAdapter.BindViewHolder> vhClass)
	{
		this(new int[]{resID}, list, (Class<? extends  ArrayListRecycleViewAdapter.BindViewHolder>[]) Arrays.asList(vhClass).toArray());
	}

	public ArrayListRecycleViewAdapter(@LayoutRes int[] resID, Class<? extends  ArrayListRecycleViewAdapter.BindViewHolder>[] vhClass)
	{
		this(resID,null,vhClass);
	}

	public ArrayListRecycleViewAdapter(@LayoutRes int[] resID, ArrayList<T> list, Class<? extends  ArrayListRecycleViewAdapter.BindViewHolder>[] vhClass)
	{
		m_resID = resID;
		m_vhClass = vhClass;
		if(list==null)
			list=new ArrayList<>();

		m_list = list;
	}

	@Override
	public final ArrayListRecycleViewAdapter.BindViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater
				.from(parent.getContext())
				.inflate(m_resID[viewType], parent, false);

		return onCreateViewHolder(itemView,viewType);
	}

	private ArrayListRecycleViewAdapter.BindViewHolder onCreateViewHolder(View view, int viewType)
	{
		try {
			return (BindViewHolder) m_vhClass[viewType].getConstructor(View.class,int.class).newInstance(view,viewType);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			try
			{
				return (BindViewHolder) m_vhClass[viewType]
						.getConstructor(view.getContext().getClass(),View.class,int.class)
						.newInstance(view.getContext(),view,viewType);
			} catch (InstantiationException e1)
			{
				e1.printStackTrace();
			} catch (IllegalAccessException e1)
			{
				e1.printStackTrace();
			} catch (InvocationTargetException e1)
			{
				e1.printStackTrace();
			} catch (NoSuchMethodException e1)
			{
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public final int getItemCount()
	{
		return m_list.size();
	}

	@Override
	public final void onBindViewHolder(ArrayListRecycleViewAdapter.BindViewHolder holder, int position)
	{
		//onBindViewHolder(holder,getItem(position),position);
		holder.item=getItem(position);
		holder.bind(holder.item,position);
	}

	@Override
	public int getItemViewType(int position)
	{
		T item = getItem(position);
		return item.getViewType();
	}

	public T getItem(int position)
	{
		return m_list.get(position);
	}

	public void insert(T item, int position) {
		m_list.add(position, item);
		notifyItemInserted(position);
	}

	public void insert(List<T> list,int position)
	{
		m_list.addAll(position, list);
		notifyItemRangeInserted(position, list.size());
	}

	public void addAll(List<T> list)
	{
		insert(list, getItemCount());
	}

	public void add(T item)
	{
		insert(item, getItemCount());
	}

	public T remove(int position) {
		T item = m_list.remove(position);
		notifyItemRemoved(position);

		return item;
	}

	public void move(int from, int to)
	{
		T item = m_list.remove(from);
		m_list.add(to, item);
		notifyItemMoved(from, to);
	}

	public void clear() {
		int size = m_list.size();
		m_list.clear();
		notifyItemRangeRemoved(0, size);
	}

	public boolean contains(T item)
	{
		return m_list.contains(item);
	}

	public int indexOf(T item)
	{
		return m_list.indexOf(item);
	}

	public ArrayList<T> getList()
	{
		return m_list;
	}

	public List<T> filter(String query)
	{
		if(m_originalList==null)
			m_originalList=new ArrayList(m_list);

		List<T> filteredModelList = new ArrayList<>();
		for (T model : m_originalList) {
			if(m_filterListener==null)
			{
				String text = model.toString().toLowerCase();
				if (text.contains(query))
				{
					filteredModelList.add(model);
				}
			}
			else
			{
				if(m_filterListener.isFilter(model))
				{
					filteredModelList.add(model);
				}
			}
		}
		if(filteredModelList.size()==0)
			filteredModelList = m_list;

		return filteredModelList;
	}

	public void animateTo(List<T> models)
	{
		applyAndAnimateRemovals(models);
		applyAndAnimateAdditions(models);
		applyAndAnimateMovedItems(models);
	}

	private void applyAndAnimateRemovals(List<T> newModels) {
		for (int i = m_list.size() - 1; i >= 0; i--) {
			final T model = m_list.get(i);
			if (!newModels.contains(model)) {
				remove(i);
			}
		}
	}

	private void applyAndAnimateAdditions(List<T> newModels) {
		for (int i = 0, count = newModels.size(); i < count; i++) {
			final T model = newModels.get(i);
			if (!m_list.contains(model)) {
				insert(model, i);
			}
		}
	}

	private void applyAndAnimateMovedItems(List<T> newModels) {
		for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
			final T model = newModels.get(toPosition);
			final int fromPosition = m_list.indexOf(model);
			if (fromPosition >= 0 && fromPosition != toPosition) {
				move(fromPosition, toPosition);
			}
		}
	}

	public void setFilterListener(FilterListener<T> filterListener)
	{
		m_filterListener = filterListener;
	}

	/**
	 * item과 view를 연결 시켜 주는 viewHolder
	 * @param <T> item type
	 */
	public static abstract class BindViewHolder<T extends ArrayListRecycleViewAdapter.ViewType> extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		public T item;

		public BindViewHolder(View itemView,int viewType)
		{
			super(itemView);
			itemView.setOnClickListener(this);
		}

		public abstract void bind(T item,int position);

		@Override
		public void onClick(View v) {

		}

		public Context getContext()
		{
			return itemView.getContext();
		}
	}

	/**
	 * item의 viewType을 정의 하기 위해서 상속 받아야 하는 클레스
	 * 기본 viewType은 0
	 */
	public static class ViewType
	{
		public int getViewType()
		{
			return 0;
		}
	}

	interface FilterListener<T>
	{
		boolean isFilter(T item);
	}
}
