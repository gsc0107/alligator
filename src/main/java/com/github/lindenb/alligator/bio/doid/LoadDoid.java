package com.github.lindenb.alligator.bio.doid;


import generated.org.diseaseontology.owl.Term;
import generated.org.diseaseontology.owl.Term.SubClassOf;
import generated.org.diseaseontology.rdf.RDF;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;




public class LoadDoid
	{
	private Logger LOG=Logger.getLogger("cardioserve");
	
  

    
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
				 "generated.org.diseaseontology.rdf:generated.org.diseaseontology.owl"
				 );
		  Unmarshaller unmarshaller=jaxbCtxt.createUnmarshaller();
		  Marshaller marshaller=jaxbCtxt.createMarshaller();
		  marshaller.setProperty("jaxb.formatted.output", false);
		  marshaller.setProperty("jaxb.fragment", true);
	
		  RDF dofile=(RDF)((javax.xml.bind.JAXBElement<RDF>)unmarshaller.unmarshal(new java.io.File(args[optind]))).getValue();
		  LOG.info("loading DOID...");
		  long id_generator=0;
		  con.setAutoCommit(false);
		  PreparedStatement pstmt=con.prepareStatement(
				  "insert into DISEASEONTOLOGY.TERM(id,uri,content) values(?,?,?)"
				  );
		  for(Term term:dofile.getTerms())
		  	 {
			  ++id_generator;
			  pstmt.setLong(1, id_generator);
			  pstmt.setString(2, term.getAbout());
			  node2id.put(term.getAbout(), id_generator);
			 
			StringWriter sw=new StringWriter();
			marshaller.marshal(new JAXBElement<Term>(
					new QName("http://www.w3.org/2002/07/owl#", "Class","owl"),
					Term.class, term), sw);
			sw.flush();
			pstmt.setString(3,sw.toString());
			pstmt.executeUpdate();
			if(id_generator%100==0)
				{
				LOG.info(term.getAbout()+" "+id_generator);
				}
			//
		  	
			for(SubClassOf isa: term.getSubClassOf())
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
				  "insert into DISEASEONTOLOGY.TERM2CHILD(id,parent_id,child_id) values(?,?,?)"
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
		new LoadDoid().run2(args);
		}
}
