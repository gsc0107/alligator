package com.github.lindenb.alligator.bio.sequences;

import java.util.List;

import com.github.lindenb.alligator.lang.AbstractCharSequence;

public class RNASequence
	extends AbstractCharSequence
	{
	private List<Integer> genomicPositions;
	private GenomicSequence genomic;
	private char strand;
	public RNASequence(
		GenomicSequence genomic,
		List<Integer> genomicPositions,
		char strand)
		{
		this.genomic=genomic;
		this.strand=strand;
		this.genomicPositions=genomicPositions;
		}
	
	private char complement(char c)
		{
		switch(c)
			{
			case 'A':case 'a': return 'T';
			case 'T':case 't': return 'A';
			case 'G':case 'g': return 'C';
			case 'C':case 'c': return 'G';
			default: return c;
			}
		}
	
	@Override
	public char charAt(int i)
		{
		char c=genomic.charAt(this.genomicPositions.get(i));
		return (strand=='+'?c:complement(c));
		}
	@Override
	public int length()
		{
		return genomicPositions.size();
		}
	}
