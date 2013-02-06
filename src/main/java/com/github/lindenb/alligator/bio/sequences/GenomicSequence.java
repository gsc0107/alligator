package com.github.lindenb.alligator.bio.sequences;

import net.sf.picard.reference.ReferenceSequenceFile;
import net.sf.samtools.SAMSequenceRecord;

import com.github.lindenb.alligator.lang.AbstractCharSequence;

/**
 * GenomicSequence
 */
public class GenomicSequence
	extends AbstractCharSequence
	{
	private static final int DEFAULT_BUFFER_HALF_SIZE=50000;
	private String chrom;
	private byte array[];
	private int chromStart0=-1;
	private ReferenceSequenceFile refseq;
	private SAMSequenceRecord samSeqRec=null;
	private int bufferSize=DEFAULT_BUFFER_HALF_SIZE;
	
	/**
	 * GenomicSequence
	 * @param refseq
	 * @param chrom
	 */
	public GenomicSequence(ReferenceSequenceFile refseq,String chrom)
		{	
		this.chrom=chrom;
		this.array=null;
		this.refseq=refseq;
		this.samSeqRec=this.refseq.getSequenceDictionary().getSequence(chrom);
		if(this.samSeqRec==null)
			{
			throw new IllegalArgumentException(
				"chrom '"+chrom+"' not in dictionary"
				);
			}
		}
	
	public void setBufferSize(int bufferSize)
		{
		this.bufferSize = bufferSize;
		}
	
	public int getBufferSize()
		{
		return bufferSize;
		}
	
	public String getChrom()
		{
		return chrom;
		}
	
	@Override
	public int length()
		{
		return this.samSeqRec.getSequenceLength();
		}
	
	@Override
	public char charAt(int index0)
		{
		if(index0 < 0|| index0 >=length())
			{
			throw new IndexOutOfBoundsException("index:"+index0);
			}
		if(array==null || chromStart0>index0 || chromStart0==-1 || chromStart0+array.length<= index0)
			{
			this.chromStart0 = Math.max(index0-getBufferSize(),0);
			this.array = this.refseq.getSubsequenceAt(
					this.chrom,
					this.chromStart0+1,
					Math.min(this.chromStart0+2*getBufferSize(),this.samSeqRec.getSequenceLength())
					).getBases();
			}
		return (char)(array[index0-chromStart0]);
		}

	}
