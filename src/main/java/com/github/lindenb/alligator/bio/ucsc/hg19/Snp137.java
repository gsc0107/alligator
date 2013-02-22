package com.github.lindenb.alligator.bio.ucsc.hg19;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.github.lindenb.bdbutils.binding.TupleSerializable;
import com.github.lindenb.bdbutils.binding.TupleSerializableBinding;
import com.github.lindenb.bdbutils.bio.interval.BedSegment;
import com.github.lindenb.bdbutils.sort.SorterTupleSerializable;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

@XmlRootElement(name="Snp137")
@Entity(name="Snp137")
@Table(name="snp137",schema="hg19")
@NamedQueries({
	@NamedQuery(
			name="hg19.snp137.findByRs",
			query="SELECT c FROM Snp137 c where c.rsId= :rsId"),
		@NamedQuery(
				name="hg19.snp137.findByPosition",
				query="SELECT c FROM Snp137 c where c.chrom= :chrom and c.bin IN (:bin) and NOT( c.chromEnd <= :start  OR c.chromStart>=:end)")
	})

public class Snp137 implements Serializable,TupleSerializable
	{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private int rsId;
	private BedSegment segment=new BedSegment();
	



	public Snp137()
		{
		
		}
	
	
	
	
	public Snp137(long id, int rsId, BedSegment segment) {
		super();
		this.id = id;
		this.rsId = rsId;
		this.segment = segment;
	}




	@Override
	public void readFromTupleInput(TupleInput in)
		{
		this.id=in.readLong();
		this.rsId=in.readInt();
		this.segment.readFromTupleInput(in);
		}
	
	@Override
	public void writeToTupleOutpout(TupleOutput out)
		{
		out.writeLong(this.id);
		out.writeInt(this.rsId);
		this.segment.writeToTupleOutpout(out);
		}
	
	@Id
	@XmlTransient
	@Column(name="id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	
	
	
	public BedSegment
		getSegment()
		{
		return segment;
		}
	
	@XmlTransient
	@Column(name="rs_id")
	public int getRsId() {
		return rsId;
	}
	public void setRsId(int rsId) {
		this.rsId = rsId;
	}
	
	@Transient
	public String getName()
		{
		return "rs"+getRsId();
		}
	
	
	
	@Override
	public String toString() {
		return "Snp137 [rsId=" + rsId + ", "+segment+ "]";
	}
	
	
	}
