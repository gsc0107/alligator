package com.github.lindenb.alligator.bio;

public class GeneticCode
	{
	private static final GeneticCode STANDARD=new GeneticCode(
		"Standard", "FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG"
		);

	private static final GeneticCode MITOCHONDRIAL=new GeneticCode(
			"Mitochondrial", "FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSS**VVVVAAAADDEEGGGG"
			);
	
		
	private String name;
	private String ncbiTable;
	
    public GeneticCode(String name,String ncbiTable)
    	{
    	this.name=name;
    	this.ncbiTable=ncbiTable;
    	}
		
		
	/** get the genetic-code table (NCBI data) */ 
	protected String getNCBITable()
		{
		return this.ncbiTable;
		}
	
	/** convert a base to index */
	private int base2index(char c)
		{
		switch(c)
			{
			case 'T': case 't': return 0;
			case 'C': case 'c': return 1;
			case 'A': case 'a': return 2;
			case 'G': case 'g': return 3;
			default: return -1;
			}
		}
	/** translate cDNA to aminoacid */
	public char translate(char b1,char b2,char b3)
		{
		int base1= base2index(b1);
		int base2= base2index(b2);
		int base3= base2index(b3);
		if(base1==-1 || base2==-1 || base3==-1)
			{
			return '?';
			}
		else
			{
			return getNCBITable().charAt(base1*16+base2*4+base3);
			}
		}
	
	public String getName()
		{
		return name;
		}
	
	@Override
	public String toString() {
		return name;
		}
	
	/** get the standard genetic code */
	public static GeneticCode getStandard()
		{
		return STANDARD;
		}
	
	/** get the mitochondrial genetic code */
	public static GeneticCode getMitochondrial()
		{
		return MITOCHONDRIAL;
		}
	
	}