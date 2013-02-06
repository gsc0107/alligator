package com.github.lindenb.alligator.lang;

public class DefaultCharSequence
	extends AbstractCharSequence
	{
	private CharSequence delegate;
	public DefaultCharSequence(CharSequence seq)
		{
		this.delegate=seq;
		}
	
	protected CharSequence getDelegate() {
		return delegate;
		}
	
	@Override
	public char charAt(int i)
		{
		return getDelegate().charAt(i);
		}
	
	@Override
	public int length() {
		return getDelegate().length();
		}

	}
