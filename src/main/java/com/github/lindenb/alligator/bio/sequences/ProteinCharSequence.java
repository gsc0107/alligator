package com.github.lindenb.alligator.bio.sequences;

import com.github.lindenb.alligator.bio.GeneticCode;
import com.github.lindenb.alligator.lang.AbstractCharSequence;

public class ProteinCharSequence extends AbstractCharSequence
	{
	private RNASequence cDNA;
	private GeneticCode geneticCode;
	public ProteinCharSequence(GeneticCode geneticCode,RNASequence cDNA)
		{
		this.geneticCode=geneticCode;
		this.cDNA=cDNA;
		}
	
	public GeneticCode getGeneticCode()	
		{
		return geneticCode;
		}	
	
	@Override
	public char charAt(int i)
		{
		return getGeneticCode().translate(
			cDNA.charAt(i*3+0),
			cDNA.charAt(i*3+1),
			cDNA.charAt(i*3+2));
		}	
	
	@Override
	public int length()
		{
		return this.cDNA.length()/3;
		}
	}
