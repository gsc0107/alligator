package com.github.lindenb.alligator.webapp.services;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javax.ejb.Stateless;
import javax.jws.WebParam;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import net.sf.picard.reference.IndexedFastaSequenceFile;
import net.sf.picard.reference.ReferenceSequence;
import net.sf.samtools.SAMSequenceRecord;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.github.lindenb.bio.Chromosome;
import com.github.lindenb.bio.Genomes;
import com.github.lindenb.bio.Reference;
import com.github.lindenb.config.Config;

@Path("/sequences")
@Stateless
public class SequenceServices
	{
	@Context ServletContext context;
	
	private abstract class AbstractSequenceResponse
		implements StreamingOutput
		{
		Reference reference;
		String build;
		String chrom;
		int start;
		int end;
		SAMSequenceRecord samSeqRec;
		IndexedFastaSequenceFile refSequenceFile;
		Config config;
		public AbstractSequenceResponse(String build, String chrom, int start,int end)
			{
			this.build = build;
			this.chrom = chrom;
			this.start = start;
			this.end = end;
			}
		
		private void error(String message)  throws IllegalArgumentException
			{
			throw new IllegalArgumentException(message);
			}	
		
		protected void check() throws IllegalArgumentException,WebApplicationException
			{
			if(build==null || build.isEmpty()) error("Unkown build "+build);
			if(this.start<0) error("start<0 "+this.start);
			if(this.start>this.end) error("start>end");
			
			Genomes genomes=(Genomes)WebApplicationContextUtils.getRequiredWebApplicationContext(context).getBean("genomes");
			if(genomes==null) throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			this.config=(Config)WebApplicationContextUtils.getRequiredWebApplicationContext(context).getBean("config");
			Reference ref=genomes.getGenomes().get(this.build);
			if(ref==null) error("unknown genome "+this.build);
			if(chrom==null || chrom.isEmpty()) throw new WebApplicationException(Status.BAD_REQUEST);
			Chromosome chr=ref.getChromosomeByName(chrom);
			if(chr==null) throw new WebApplicationException(Status.BAD_REQUEST);
			
			this.samSeqRec=refseq.getSequenceDictionary().getSequence(chr.getName());
			if(this.samSeqRec==null)  throw new WebApplicationException(Status.BAD_REQUEST);
			this.refSequenceFile=new IndexedFastaSequenceFile(new File(this.samSeqRec.getId()));

			}
		
		byte[] fetchBases() throws IOException
			{
			while(this.start<=this.end && this.start<=this.samSeqRec.getSequenceLength())
				{
				int stop=Math.min(
						this.start+this.config.getFastaQueryBufferSize(),
						this.samSeqRec.getSequenceLength()
						);
				ReferenceSequence sequence=this.refSequenceFile.getSubsequenceAt(this.samSeqRec.getId(),
						this.start,
						stop
						);
				byte array[]=sequence.getBases();
				start=stop+1;
				return array;
				}
			return null;
			}
		}
	
	private class FastaSequenceResponse
		extends AbstractSequenceResponse
		{

		public FastaSequenceResponse(String build, String chrom, int start,
				int end) throws WebApplicationException {
			super(build, chrom, start, end);
			}
		@Override
		public void write(OutputStream w) throws IOException
			{
			String title=">"+this.build+"|"+this.chrom+":"+this.start+"-"+this.end;
			w.write(title.getBytes());
			int n_printed=0;
			byte array[];
			while((array=fetchBases())!=null)
				{
				for(int i=0;i< array.length;++i)
					{
					if(n_printed%config.getFastaLineLength()==0) w.write('\n');
					w.write(array[i]);
					++n_printed;
					}
				}
			w.write('\n');
			w.flush();
			w.close();
			}
		}
	
	/** http://localhost:8080/cardioserve/rest/sequences/hg19/ */
	@GET
	@Path("/{build}/{chrom}:{start}-{end}.fa")
	@Produces("text/plain")
	public StreamingOutput getSequenceAsText(
			@PathParam("build") String build,
			@PathParam("chrom") String chrom,
			@PathParam("start") int start0,
			@PathParam("end") int end0)
			throws WebApplicationException
		{
		try {
			return new FastaSequenceResponse(build,chrom,start0,end0);
		} catch (Exception e) {
			throw new WebApplicationException(e);
			}
		}
	}
