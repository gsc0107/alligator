package com.github.lindenb.alligator.model;

import java.util.HashSet;
import java.util.Set;

import com.github.lindenb.bdbutils.binding.SetBinding;
import com.github.lindenb.bdbutils.db.DatabaseWrapper;
import com.github.lindenb.bdbutils.db.TemporaryKeySet;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

public abstract class AbstractOntDatabase<T> extends DatabaseWrapper<String, T>
	{
	private Parent2child parent2child=new Parent2child();
	
	public class Parent2child extends DatabaseWrapper<String, Set<String> >
		{
		public Parent2child()
			{
			setKeyBinding(new StringBinding());
			setDataBinding(new SetBinding<String>(new StringBinding()));
			}
		
		@Override
		public String getName()
			{
			return AbstractOntDatabase.this.getName()+"_uri2child";
			}
		
		}
	
	protected AbstractOntDatabase()
		{
		setKeyBinding(new StringBinding());
		}
	
	@Override
	public DatabaseWrapper<String, T> open(Environment env, Transaction txn, DatabaseConfig dbConfig)
		{
		super.open(env, txn, dbConfig);
		DatabaseConfig cfg=dbConfig.clone();
		this.parent2child.open(env, txn, cfg);
		return this;
		}
	
	@Override
	public void close()
		{
		this.parent2child.close();
		super.close();
		}
	
	
	public boolean insert(Transaction txn, final T v)
		{
		String uri=extractURI(v);
		boolean inserted=put(txn,uri,v);
		if(inserted)
			{
			for(String parent:extractParents(v))
				{
				Set<String> child=parent2child.get(txn, parent, null);
				if(child==null) child=new HashSet<String>(1);
				child.add(uri);
				parent2child.put(txn, parent, child);
				}
			}
		
		return inserted;
		}
	
	private void recursiveChildrenUriFor(Transaction txn,TemporaryKeySet<String> set,String parent)
		{
		if(set.containsKey(txn, parent, null)) return;
		Set<String> children=parent2child.get(txn, parent, null);
		if(children==null) return;
		set.put(txn, parent);
		for(String child: children)
			{
			recursiveChildrenUriFor(txn,set,child);
			}
		}	
	
	public TemporaryKeySet<String> getChildrenUri(Transaction txn,String uri)
		{
		TemporaryKeySet<String> set=TemporaryKeySet.createTemporaryKeySet(getDatabase().getEnvironment(), txn, getKeyBinding());
		recursiveChildrenUriFor(txn,set,uri);
		return set;
		}	
	
	public abstract Set<String> extractParents(T t);
	public abstract String extractURI(T t);
	}
