package com.github.lindenb.alligator.bio.ucsc.hg19;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
//import javax.xml.bind.annotation.XmlType;


@Entity(name="knownGene")
@Table(name="knownGene",schema="hg19")
@XmlRootElement(name="knownGene")
@NamedQueries({
	@NamedQuery(
			name="hg19.knownGene.findByGeneSymbol",
			query="SELECT G FROM kgXRef X, knownGene G  where G.name=X.kgId and X.geneSymbol= :geneSymbol"
			),
	@NamedQuery(
			name="hg19.knownGene.findByName",
			query="SELECT G FROM knownGene G  where G.name= :name"
			)
	,
	@NamedQuery(
			name="hg19.knownGene.findByPosition",
			query="SELECT G FROM knownGene G  where G.chromosome= :chrom and NOT(G.txEnd <= :start OR  G.txStart > :end ) "
			)
	})
//@XmlType(propOrder = { "chromosome", "txStart", "txtEnd","strand","cdsStart","cdsEnd","name"})
public class KnownGene
	implements Serializable
	{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String name;
	private String chrom;
	private char _strand;
	private int txStart;
	private int txEnd;
	private int cdsStart;
	private int cdsEnd;
	private int _exonStarts[];
	private int _exonEnds[];
	private String proteinId;
	private String alignId;
	
	
	public static abstract class Interval
		{
		private KnownGene gene;
		protected int index;
		public abstract String getName();
		public abstract int getStart();
		public abstract int getEnd();
		protected Interval(KnownGene gene,int index)
			{
			this.gene=gene;
			this.index=index;
			}
		@XmlTransient
		protected KnownGene getGene()
			{
			return this.gene;
			}
		@Override
		public String toString() {
			return getName();
			}
		}
	
	@XmlRootElement(name="exon")
	public static class Exon extends Interval
		{
		public Exon()
			{
			super(null,-1);
			}
		public Exon(KnownGene gene,int index)
			{
			super(gene,index);
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
	
	
	@Transient
	public String getBuild()
		{
		return "hg19";
		}
	
	@Id
	@XmlAttribute(name="id")
	public long getId()
		{
		return id;
		}
	
	public void setId(long id) {
		this.id = id;
		}
	
	/** returns knownGene ID */
	@Column(name="name")
	public String getName()
		{
		return this.name;
		}
	
	public void setName(String name)
		{
		this.name = name;
		}
	
	/** returns chromosome name */
	@Column(name="chrom")
	public String getChromosome()
		{
		return this.chrom;
		}
	
	public void setChromosome(String chrom)
		{
		this.chrom = chrom;
		}
	
	
	/** returns the strand */
	@Column(name="strand")
	public String getStrand()
		{
		return String.valueOf(_strand);
		}
	
	public void setStrand(String strand)
		{
		this._strand = strand.charAt(0);
		}
	
	@XmlTransient
	@Transient
	public boolean isForward()
    	{
    	return getStrand().equals("+");
    	}
	
	@Column(name="txStart")
	public int getTxStart()
		{
		return this.txStart;
		}

	public void setTxStart(int txStart)
		{
		this.txStart = txStart;
		}
	
	@Column(name="txEnd")
	public int getTxEnd()
		{
		return this.txEnd;
		}
	
	public void setTxEnd(int txEnd)
		{
		this.txEnd = txEnd;
		}
	
	@Column(name="cdsStart")
	public int getCdsStart()
		{
		return this.cdsStart;
		}
	
	public void setCdsStart(int cdsStart)
		{
		this.cdsStart = cdsStart;
		}
	
	@Column(name="cdsEnd")
	public int getCdsEnd()
		{
		return this.cdsEnd;
		}
	
	public void setCdsEnd(int cdsEnd)
		{
		this.cdsEnd = cdsEnd;
		}
	

	@Transient
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
	@Column(name="exonStarts")
	public String getExonStarts() {
		return join(_exonStarts);
	}

	public void setExonStarts(String exonStarts)
		{
        this._exonStarts=split(exonStarts);
		}

	@XmlTransient
	@Column(name="exonEnds")
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
		return new Exon(this,index);
		}
	
	@Transient
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
	
 }