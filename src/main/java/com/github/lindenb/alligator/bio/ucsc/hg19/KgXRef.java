package com.github.lindenb.alligator.bio.ucsc.hg19;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="kgXRef")
@Entity(name="kgXRef")
@Table(name="kgXref",schema="hg19")
@NamedQueries({
	@NamedQuery(
			name="hg19.kgxref.findByGeneSymbol",
			query="SELECT c FROM kgXRef c where c.geneSymbol= :geneSymbol")
	})
public class KgXRef
implements Serializable
	{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String kgID;
	private String mRNA;
	private String spID;
	private String spDisplayID;
	private String geneSymbol;
	private String refseq;
	private String protAcc;
	private String description;
	private String rfamAcc;
	private String tRnaName;

	
	
	@Id
	@XmlAttribute(name="id")
	public long getId()
		{
		return id;
		}
	
	public void setId(long id) {
		this.id = id;
		}

	public String getKgId() {
		return kgID;
	}

	public void setKgId(String kgID) {
		this.kgID = kgID;
	}

	public String getmRNA() {
		return mRNA;
	}

	public void setmRNA(String mRNA) {
		this.mRNA = mRNA;
	}

	public String getSpID() {
		return spID;
	}

	public void setSpID(String spID) {
		this.spID = spID;
	}

	public String getSpDisplayID() {
		return spDisplayID;
	}

	public void setSpDisplayID(String spDisplayID) {
		this.spDisplayID = spDisplayID;
	}

	public String getGeneSymbol() {
		return geneSymbol;
	}

	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}

	public String getRefseq() {
		return refseq;
	}

	public void setRefseq(String refseq) {
		this.refseq = refseq;
	}

	public String getProtAcc() {
		return protAcc;
	}

	public void setProtAcc(String protAcc) {
		this.protAcc = protAcc;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRfamAcc() {
		return rfamAcc;
	}

	public void setRfamAcc(String rfamAcc) {
		this.rfamAcc = rfamAcc;
	}

	public String gettRnaName() {
		return tRnaName;
	}

	public void settRnaName(String tRnaName) {
		this.tRnaName = tRnaName;
	}

	
	@Override
	public String toString()
		{
		return "KgXRef [id=" + id + ", kgID=" + kgID + ", mRNA=" + mRNA
				+ ", spID=" + spID + ", spDisplayID=" + spDisplayID
				+ ", geneSymbol=" + geneSymbol + ", refseq=" + refseq
				+ ", protAcc=" + protAcc + ", description=" + description
				+ ", rfamAcc=" + rfamAcc + ", tRnaName=" + tRnaName + "]";
		}
	
	
	
	}
