package com.github.lindenb.alligator.lang;

public abstract class AbstractCharSequence implements CharSequence
	{
	
	@Override
	public int hashCode()
		{
		return getString().hashCode();
		}
	
	@Override
	public CharSequence subSequence(int start, int end)
		{
		StringBuilder b=new StringBuilder(end-start);
		while(start<end)
			{
			b.append(charAt(start++));
			}
		return b;
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

	}
