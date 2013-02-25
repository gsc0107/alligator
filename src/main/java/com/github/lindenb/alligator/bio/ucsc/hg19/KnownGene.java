package com.github.lindenb.alligator.bio.ucsc.hg19;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
//import javax.xml.bind.annotation.XmlType;

import com.github.lindenb.bdbutils.binding.AbstractTupleSerializable;
import com.github.lindenb.bdbutils.binding.TupleSerializable;
import com.github.lindenb.bdbutils.bio.interval.TidBinPos;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


public class KnownGene
	extends AbstractTupleSerializable
	implements TupleSerializable
	{	
	private String name;
	private byte tid;
	private char _strand;
	private int txStart;
	private int txEnd;
	private int cdsStart;
	private int cdsEnd;
	private int _exonStarts[];
	private int _exonEnds[];
	private String proteinId;
	private String alignId;
	
	
	public abstract class Interval
		{
		protected int index;
		public abstract String getName();
		public abstract int getStart();
		public abstract int getEnd();
		protected Interval(int index)
			{
			this.index=index;
			}
		@XmlTransient
		protected KnownGene getGene()
			{
			return KnownGene.this;
			}
		@Override
		public String toString() {
			return getName();
			}
		}
	
	@XmlRootElement(name="exon")
	public class Exon extends Interval
		{
		public Exon(int index)
			{
			super(index);
			}
		@XmlAttribute(name="index")
		int getExonIndex()
			{
			if( getGene().isForward())
				{
				return (this.index+1);
				}
			else
				{
				return getGene().getExonCount()-this.index;
				}
			}
		
		@Override
		@XmlTransient
		public String getName()
			{
			return "Exon "+getExonIndex();
			}
		
		@Override
		@XmlElement(name="start")
		public int getStart() {
			return getGene()._exonStarts[this.index];
			}
		@Override
		@XmlElement(name="end")
		public int getEnd()
			{
			return getGene()._exonEnds[this.index];
			}
		

		}
	
	public KnownGene()
		{
		}
	
	
	public String getBuild()
		{
		return "hg19";
		}
	

	
	/** returns knownGene ID */
	public String getName()
		{
		return this.name;
		}
	
	public void setName(String name)
		{
		this.name = name;
		}
	
	public String getChromosome()
		{
		return Chromosomes.getInstance().get(getTid());
		}
	
	public byte getTid()
		{
		return this.tid;
		}
	
	public void setTid(byte tid)
		{
		this.tid = tid;
		}
	
	
	/** returns the strand */
	public char getStrand()
		{
		return _strand;
		}
	
	public void setStrand(char strand)
		{
		this._strand = strand;
		}
	
	@XmlTransient
	public boolean isForward()
    	{
    	return getStrand()=='+';
    	}
	
	public int getTxStart()
		{
		return this.txStart;
		}

	public void setTxStart(int txStart)
		{
		this.txStart = txStart;
		}
	
	public int getTxEnd()
		{
		return this.txEnd;
		}
	
	public void setTxEnd(int txEnd)
		{
		this.txEnd = txEnd;
		}
	
	public int getCdsStart()
		{
		return this.cdsStart;
		}
	
	public void setCdsStart(int cdsStart)
		{
		this.cdsStart = cdsStart;
		}
	
	public int getCdsEnd()
		{
		return this.cdsEnd;
		}
	
	public void setCdsEnd(int cdsEnd)
		{
		this.cdsEnd = cdsEnd;
		}
	

	@XmlAttribute(name="exon-count")
	public int getExonCount()
		{
		return (this._exonStarts==null?0:this._exonStarts.length);
		}
	
	private static String join(int array[])
		{
		if(array==null || array.length==0) return "";
		StringBuilder b=new StringBuilder();
		for(int i:array)
			{
			if(b.length()!=0) b.append(',');
			b.append(i);
			}
		return b.toString();
		}
	
	private static int[] split(String s)
		{
		if(s==null || s.isEmpty()) return new int[]{};
        String tokens[]=s.split("[,]");
        int array[]=new int[tokens.length];
        for(int i=0;i< tokens.length;++i)
        	{
        	array[i]=Integer.parseInt(tokens[i]);
        	}
        return array;
		}
	
	@XmlTransient
	public String getExonStarts() {
		return join(_exonStarts);
	}

	public void setExonStarts(String exonStarts)
		{
        this._exonStarts=split(exonStarts);
		}

	@XmlTransient
	public String getExonEnds()
		{
		return join(_exonEnds);
		}

	public void setExonEnds(String exonEnds)
		{
		 this._exonEnds=split(exonEnds);
		}

	public String getProteinId()
		{
		return proteinId;
		}

	public void setProteinId(String proteinId)
		{
		this.proteinId = proteinId;
		}

	public String getAlignId() {
		return alignId;
	}

	public void setAlignId(String alignId) {
		this.alignId = alignId;
	}


	public Exon getExon(int index)
		{
		return new Exon(index);
		}
	
	@XmlElementWrapper(name="exons")
	@XmlElement(name="exon")
	public  List<Exon> getExons()
		{
		List<Exon> L=new ArrayList<Exon>(getExonCount());
		for(int i=0;i< getExonCount();++i)
			{
			L.add(getExon(i));
			}
		return L;
		}
	@Override
	public void readFromTupleInput(TupleInput in)
		{
		this.name=in.readString();
		this.tid=in.readByte();
		this._strand=(char)in.readByte();
		this.txStart=in.readInt();
		this.txEnd=in.readInt();
		this.cdsStart=in.readInt();
		this.cdsEnd=in.readInt();
		int exonCount=in.readInt();
		this._exonStarts=new int[exonCount];
		this._exonEnds=new int[exonCount];
		for(int i=0;i< exonCount;++i)
			{
			this._exonStarts[i]=in.readInt();
			this._exonEnds[i]=in.readInt();
			}
		this.proteinId=in.readString();
		this.alignId=in.readString();
		}
	
	@Override
	public void writeToTupleOutpout(TupleOutput out)
		{
		out.writeString(this.name);
		out.writeByte(this.tid);
		out.writeByte((byte)this._strand);
		out.writeInt(this.txStart);
		out.writeInt(this.txEnd);
		out.writeInt(this.cdsStart);
		out.writeInt(this.cdsEnd);
		out.writeInt(this._exonStarts.length);
		for(int i=0;i< this._exonStarts.length;++i)
			{
			out.writeInt(this._exonStarts[i]);
			out.writeInt(this._exonEnds[i]);
			}
		out.writeString(this.proteinId);
		out.writeString(this.alignId);
		}
	
  public TidBinPos getTidBinPos()
	  {
	  return new TidBinPos(getTid(), TidBinPos.bin(this.getTxStart(), getTxEnd()), getTxStart());
	  }
	
 }