package com.github.lindenb.alligator.lang;

public class SubSequence extends DefaultCharSequence
	{
	private int beg;
	private int end;
	public SubSequence(final CharSequence delegate,int beg,int end)
		{
		super(delegate);
		this.beg=beg;
		this.end=end;
		}
	
	
	@Override
	public char charAt(int index)
		{
		return getDelegate().charAt(this.beg+index);
		}

	@Override
	public int length()
		{
		return this.end-this.beg;
		}
	}
