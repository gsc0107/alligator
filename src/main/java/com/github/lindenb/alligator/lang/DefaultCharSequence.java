package com.github.lindenb.alligator.lang;

public class DefaultCharSequence
	extends AbstractCharSequence
	{
	private CharSequence seq;
	public DefaultCharSequence(CharSequence seq)
		{
		this.seq=seq;
		}
	@Override
	public char charAt(int i)
		{
		return seq.charAt(i);
		}
	
	
	@Override
	public int length() {
		return seq.length();
		}

	}
