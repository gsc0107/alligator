package com.github.lindenb.alligator.webapp.services.ucsc;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.github.lindenb.alligator.bio.ucsc.hg19.*;

@Path("/ucsc/hg19")
@Stateless
public class UcscServices
	{
	@PersistenceContext(unitName = "alligatorEntityMgr")
	private EntityManager em;
	

	@Path("/knownGene/id/{id}")
	@GET
	public KnownGene getKnownGeneById(@PathParam("id")long id)
		{
		KnownGene kg=em.find(KnownGene.class, id);
		if(kg==null) throw new WebApplicationException(404);
		return kg;
		}
	private KgXRef _ignore=new KgXRef();
	

	@SuppressWarnings("unchecked")
	@Path("/knownGene/name/{name}")
	@GET
	public List<KnownGene> getKnownGeneByName(@PathParam("name")String name)
	 	{
		return em.createNamedQuery("hg19.knownGene.findByName").
			   setParameter("name", name).
			   getResultList();
	 	}
	
	@SuppressWarnings("unchecked")
	@Path("/knownGene/geneSymbol/{name}")
	@GET
	public List<KnownGene> getKnownGeneBygeneSymbol(@PathParam("name")String geneSymbol)
	 	{
		return em.createNamedQuery("hg19.knownGene.findByGeneSymbol").
			   setParameter("geneSymbol", geneSymbol).
			   getResultList();
	 	}
	
	@SuppressWarnings("unchecked")
	@Path("/knownGene/position")
	@GET
	public List<KnownGene> getKnownGeneByPosition(
		@QueryParam("chrom")String chrom,
		@QueryParam("start")int start,
		@QueryParam("end")int end
		)
	 	{
		return em.createNamedQuery("hg19.knownGene.findByPosition").
				   setParameter("chrom", chrom).
				   setParameter("start", start).
				   setParameter("end", end).
			   getResultList();
	 	}

	}
