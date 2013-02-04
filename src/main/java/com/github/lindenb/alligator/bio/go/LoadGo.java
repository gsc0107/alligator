package com.github.lindenb.alligator.bio.go;

import generated.org.geneontology.go.Go;
import generated.org.geneontology.go.IsA;
import generated.org.geneontology.rdf.RDF.Term;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.github.lindenb.cardioserve.go.RelEntity;
import com.github.lindenb.cardioserve.go.TermEntity;




public class LoadGo
	{
	private Logger LOG=Logger.getLogger("cardioserve");
	private EntityManager em =null;
	
	private TermEntity getTermByUri(String uri)
		{
		List<?> L=em.createNamedQuery("go.find.term.by.uri").
			setParameter("uri", uri).
			getResultList();
		return L.isEmpty()?null:TermEntity.class.cast(L.get(0));
		}
	
	
	private RelEntity getRelation(String parent,String child)
		{
		List<?> L=em.createNamedQuery("go.find.relation").
			setParameter("parent", parent).
			setParameter("child", child).
			getResultList();
		return L.isEmpty()?null:RelEntity.class.cast(L.get(0));
		}

    
    private void run(String []args)throws Exception
		{
		String dbURL=null;
		int optind=0;
		while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println(" -u (jdbc.uri) ");
				return;
				}
			else if(args[optind].equals("-u") && optind+1 < args.length)
				{
				dbURL=args[++optind];
				}
			else if(args[optind].equals("--"))
				{
				optind++;
				break;
				}
			else if(args[optind].startsWith("-"))
				{
				System.err.println("Unnown option: "+args[optind]);
				return;
				}
			else
				{
				break;
				}
			++optind;
			}
		
		if(dbURL==null)
			{
			System.err.println("jdbc uri missing");
			System.exit(-1);
			}
		
		if(optind+1!=args.length)
			{
			System.err.println("Illegal number of arguments.");
			System.exit(-1);
			}
		LOG.info("Start EM");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("cardioserveEM");
	    this.em = factory.createEntityManager();
	    
		  javax.xml.bind.JAXBContext jaxbCtxt=javax.xml.bind.JAXBContext.newInstance(
				  	"generated.org.geneontology.go:generated.org.geneontology.rdf"
				  	);
		  Unmarshaller unmarshaller=jaxbCtxt.createUnmarshaller();
		  Marshaller marshaller=jaxbCtxt.createMarshaller();
		  marshaller.setProperty("jaxb.formatted.output", false);
		  marshaller.setProperty("jaxb.fragment", true);
	
		  Go go=(Go)((javax.xml.bind.JAXBElement<?>)unmarshaller.unmarshal(new java.io.File(args[optind]))).getValue();
		  LOG.info("loading GO...");
		  
		  
		  EntityTransaction txn=em.getTransaction();
		 txn.begin();
			 int count=0;
			 final int total=go.getRDF().getTerms().size();
			 long id_generator=0;
		   long now=System.currentTimeMillis();
		  for(Term term:go.getRDF().getTerms())
		  	 {
			  ++count;
			 TermEntity t=getTermByUri(term.getAbout());
			 if(t==null)
			 	{
				t=new TermEntity();
				t.setId(++id_generator);
				t.setUri(term.getAbout());
			 	}
			if(count%100==0)
				{
				long then=System.currentTimeMillis();
				LOG.info(t.getUri()+" count:"+count+"/"+total+" time:"+(then-now));
				now=then;
				}
			StringWriter sw=new StringWriter();
			marshaller.marshal(new JAXBElement<Term>(
					new QName(Go.NS, "term","go"),
					Term.class, term), sw);
			sw.flush();
			t.setXml(sw.toString());
			em.persist(t);
			//
		  	
			for(IsA isa: term.getIsA())
				{
				if(isa.getResource().equals(term.getAbout())) continue;
				
				TermEntity t2=getTermByUri(isa.getResource());
				if(t2==null)
				 	{
					t2=new TermEntity();
					t2.setId(++id_generator);
					t2.setUri(isa.getResource());
					t2.setXml("<term xmlns='"+Go.NS+"'/>");
					em.persist(t2);
					//em.flush();
				 	}
				RelEntity rel=getRelation(isa.getResource(),term.getAbout());
				if(rel!=null) continue;

				rel=new RelEntity();
				rel.setId(++id_generator);
				rel.setParent(t2);
				rel.setChild(t);
				if(t2.getUri().equals(t.getUri())) throw new RuntimeException();
				Set<RelEntity> children=t2.getChildren();
				children.add(rel);
				t2.setChildren(children);
				em.persist(rel);
				em.persist(t2);
				}
			em.flush();
		  	}
		 
	
		em.flush();
		txn.commit();
			
		this.em.close();
		}

    
    private void run2(String []args)throws Exception
		{
		String dbURL=null;
		int optind=0;
		while(optind<args.length)
			{
			if(args[optind].equals("-h"))
				{
				System.err.println(" -u (jdbc.uri) ");
				return;
				}
			else if(args[optind].equals("-u") && optind+1 < args.length)
				{
				dbURL=args[++optind];
				}
			else if(args[optind].equals("--"))
				{
				optind++;
				break;
				}
			else if(args[optind].startsWith("-"))
				{
				System.err.println("Unnown option: "+args[optind]);
				return;
				}
			else
				{
				break;
				}
			++optind;
			}
		
		if(dbURL==null)
			{
			System.err.println("jdbc uri missing");
			System.exit(-1);
			}
		
		if(optind+1!=args.length)
			{
			System.err.println("Illegal number of arguments.");
			System.exit(-1);
			}
		 Map<String,Long> node2id=new HashMap<String,Long>();
		 Map<String,Set<Long>> node2child=new HashMap<String,Set<Long>>();
		 Class.forName("org.apache.derby.jdbc.ClientDriver");
		 Connection con=DriverManager.getConnection(dbURL);
	    
		 javax.xml.bind.JAXBContext jaxbCtxt=javax.xml.bind.JAXBContext.newInstance(
				  	"generated.org.geneontology.go:generated.org.geneontology.rdf"
				  	);
		  Unmarshaller unmarshaller=jaxbCtxt.createUnmarshaller();
		  Marshaller marshaller=jaxbCtxt.createMarshaller();
		  marshaller.setProperty("jaxb.formatted.output", false);
		  marshaller.setProperty("jaxb.fragment", true);
	
		  Go go=(Go)((javax.xml.bind.JAXBElement<?>)unmarshaller.unmarshal(new java.io.File(args[optind]))).getValue();
		  LOG.info("loading GO...");
		  long id_generator=0;
		  con.setAutoCommit(false);
		  PreparedStatement pstmt=con.prepareStatement(
				  "insert into GENEONTOLOGY.TERM(id,uri,content) values(?,?,?)"
				  );
		  for(Term term:go.getRDF().getTerms())
		  	 {
			  ++id_generator;
			  pstmt.setLong(1, id_generator);
			  pstmt.setString(2, term.getAbout());
			  node2id.put(term.getAbout(), id_generator);
			 
			StringWriter sw=new StringWriter();
			marshaller.marshal(new JAXBElement<Term>(
					new QName(Go.NS, "term","go"),
					Term.class, term), sw);
			sw.flush();
			pstmt.setString(3,sw.toString());
			pstmt.executeUpdate();
			if(id_generator%100==0)
				{
				LOG.info(term.getAbout()+" "+id_generator);
				}
			//
		  	
			for(IsA isa: term.getIsA())
				{
				if(isa.getResource().equals(term.getAbout())) continue;
				Set<Long> set= node2child.get(isa.getResource());
				if(set==null)
					{
					set=new HashSet<Long>();
					node2child.put(isa.getResource(), set);
					}
				set.add(id_generator);
				}
		  	 }
		pstmt.close();
		pstmt=con.prepareStatement(
				  "insert into GENEONTOLOGY.TERM2CHILD(id,parent_id,child_id) values(?,?,?)"
				  );
		for(String uri:node2child.keySet())
			{
			Long parent_id=node2id.get(uri);
			if(parent_id==null) 
				{
				System.err.println("No id for "+uri);
				continue;
				}
			for(Long child:node2child.get(uri))
				{
				++id_generator;
				pstmt.setLong(1, id_generator);
				pstmt.setLong(2, parent_id);
				pstmt.setLong(3, child);
				pstmt.executeUpdate();
				}
			}
		pstmt.close();
		con.commit();
		con.close();
		}
    
	
	public static void main(String[] args) throws Exception
		{
		new LoadGo().run2(args);
		}
}
