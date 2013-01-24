package com.github.lindenb.alligator.bio.ucsc.hg19;

import com.github.lindenb.alligator.lang.AbstractCharSequence;

public class GenomicSequence
	extends AbstractCharSequence
	{
	private String chrom;
	private byte array[];
	private int chromStart0;
	
	public GenomicSequence(byte array[],String chrom,int chromStart0)
		{	
		this.chrom=chrom;
		this.array=array;
		this.chromStart0=chromStart0;
		}
	
	public String getChrom()
		{
		return chrom;
		}
	public int getChromStart()
		{
		return chromStart0;
		}
	public int getChromEnd()
		{
		return getChromStart()+array.length;
		}
	
	@Override
	public int length()
		{
		return getChromEnd();
		}
	
	@Override
	public char charAt(int index0)
		{
		if(index0 < getChromStart() || index0 >=getChromEnd())
			{
			throw new IndexOutOfBoundsException("index:"+index0);
			}
		return (char)(array[index0-chromStart0]);
		}

	}
