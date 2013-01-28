package com.github.lindenb.alligator.webapp.services;

import java.io.File;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import net.sf.picard.reference.IndexedFastaSequenceFile;
import net.sf.picard.reference.ReferenceSequence;

@Path("/sequences")
@Stateless
public class SequenceServices
	{
	@GET
	@Path("/hg19/x")
	@Produces("text/plain")
	public String getSequenceAsText()
			throws WebApplicationException
		{
		try {
			IndexedFastaSequenceFile refseq=new IndexedFastaSequenceFile(new File("/home/lindenb/src/cardioserve/genomes/hg19.fa"));
			ReferenceSequence chr=refseq.getSubsequenceAt("chr22",100000,120000);
			if(chr==null) return "???";
			return new String(chr.getBases());
		} catch (Exception e) {
			throw new WebApplicationException(e);
			}
		}
	}
