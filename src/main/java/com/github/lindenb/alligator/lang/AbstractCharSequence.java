package com.github.lindenb.alligator.lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/** AbstractCharSequence */
public abstract class AbstractCharSequence
	implements CharSequence
	{
	private int _hash=0;
	/** synonym of length() */
	public final int getSize()
		{
		return length();
		}
	
	public boolean isEmpty()
		{
		return length()==0;
		}
	
	@Override
	public int hashCode()
		{
		if(this._hash==0)
			{
			int hash=0;
			final int n=this.length();
			for(int i=0;i< n;++i)
				{
				hash = 31*hash + (int)charAt(i);
				}
			this._hash=hash;
			}
		return this._hash;
		}
	
	@Override
	public CharSequence subSequence(int start, int end)
		{
		return new SubSequence(this, start, end);
		}
	
	public String getString()
		{
		StringBuilder b=new StringBuilder(length());
		for(int i=0;i< length();++i) b.append(charAt(i));
		return b.toString();
		}
	
	@Override
	public String toString()
		{
		return getString();
		}
	
	public void print(Writer w) throws IOException
		{
		for(int i=0;i< length();++i) w.write(charAt(i));
		}
	
	public void print(OutputStream w) throws IOException
		{
		for(int i=0;i< length();++i) w.write(charAt(i));
		}

	}
