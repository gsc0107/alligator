package com.github.lindenb.alligator.bio.go;

import generated.org.geneontology.go.Go;
import generated.org.geneontology.rdf.RDF.Term;
import generated.uk.ac.ebi.goa.Association;
import generated.uk.ac.ebi.goa.Associations;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.annotation.Resource;
import javax.ejb.Stateless;

import com.github.lindenb.cardioserve.core.dao.CardioServeDao;

@Path("/go")
@Stateless
public class GoService
	{
	@Resource(name="jdbc/cardioserve")
	private javax.sql.DataSource myDB;
	
	//@PersistenceContext(unitName = "cardioserveEM")
	//private EntityManager em;
	
	//private javax.xml.bind.JAXBContext jaxbCtxt;
	
	public abstract class RDFOutput
		implements StreamingOutput
		{
		private CardioServeDao dao=null;
		public RDFOutput()
			{
			
			}
		
		public CardioServeDao getDao() throws SQLException
			{
			if(dao==null) this.dao=new CardioServeDao(myDB);
			return dao;
			}
		
		void printOne(XMLStreamWriter out, long term_id) throws IOException,XMLStreamException,JAXBException,SQLException
			{
			Term t=getDao().getGoTermById(term_id);
			JAXBElement<Term> jxb= new JAXBElement<Term>(
					new QName(Go.NS,"term","go"),
					Term.class,
					t
					);
			getDao().getMarshaller().marshal(jxb, out);
			}
		
		void printDescendants(XMLStreamWriter out,Set<Long> seen, long root_id) throws SQLException,IOException,XMLStreamException,JAXBException
			{
			if(!seen.add(root_id)) return;//alread in set
			printOne(out,root_id);
			for(Long child_id:getDao().getChildIdByGoTermId(root_id))
				{
				printDescendants(out,seen,child_id);
				}
			}
		
		void printBody(XMLStreamWriter out) throws IOException,XMLStreamException,JAXBException,SQLException
			{
			
			}
		
		@Override
		public void write(OutputStream out) throws IOException,
				WebApplicationException
			{
			try {
				XMLOutputFactory xmlfactory= XMLOutputFactory.newInstance();
				xmlfactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
				XMLStreamWriter w= xmlfactory.createXMLStreamWriter(out);
				w.writeStartDocument("UTF-8","1.0");
				w.writeStartElement("rdf","RDF","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
				//w.writeAttribute("xmlns", XMLConstants.XML_NS_URI, "rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
				w.writeAttribute("xmlns", XMLConstants.XML_NS_URI, "go",Go.NS);
				getDao().getMarshaller().setProperty(Marshaller.JAXB_FRAGMENT,true);
				printBody(w);
				w.writeEndElement();
				w.writeEndDocument();
				w.flush();
				w.close();
				} 
			catch (XMLStreamException e)
				{
				throw new WebApplicationException(e);
				}
			catch (JAXBException e)
				{
				throw new WebApplicationException(e);
				}
			catch (SQLException e)
				{
				throw new WebApplicationException(e);
				}
			finally
				{
				if(dao!=null) dao.close();
				dao=null;
				}
			}
		}
	
	
	public GoService()
		{
		
		}
	

	
	/*
	private TermEntity getTermEntityByUri(String uri)
		{
		if(em==null) throw new NullPointerException();
		if(uri==null || uri.isEmpty()) return null;
		if(uri.startsWith("GO:")) uri="http://www.geneontology.org/go#"+uri;
		
		List<?> L=em.createNamedQuery("go.find.term.by.uri").
			setParameter("uri", uri).
			getResultList();
		return L.isEmpty()?null:TermEntity.class.cast(L.get(0));
		}*/

	
	
	
	@GET
	@Path("/show/{id}")
	@Produces("text/xml")
	public Response getTerm(@PathParam("id") final String id)
		throws WebApplicationException
		{
		RDFOutput res=new RDFOutput()
			{
			@Override
			void printBody(XMLStreamWriter out) throws SQLException,IOException,JAXBException,XMLStreamException {
				
				long root_id=getDao().getGoTermIdByUri(id);
				if(root_id!=-1)
					{
					printOne(out, root_id);
					}
				}
			};
	return Response.ok(res).build();
	}
	
	@GET
	@Path("/descendants/{id}")
	@Produces("text/xml")
	public Response getDescendants(@PathParam("id") final String id)
		throws WebApplicationException
		{
		RDFOutput res=new RDFOutput()
			{
			@Override
			void printBody(XMLStreamWriter out) throws SQLException,IOException,JAXBException,XMLStreamException {
				long root_id=getDao().getGoTermIdByUri(id);
				if(root_id!=-1)
					{
					Set<Long> seen=new HashSet<Long>();
					printDescendants(out, seen, root_id);
					}
				}
			};
		return Response.ok(res).build();
		}
	private void recursivegoa(
		CardioServeDao dao,
		XMLStreamWriter w,
		Set<Long> seen,
		long root_id
		) throws XMLStreamException,SQLException,JAXBException
		{
		if(!seen.add(root_id)) return;//already in set
		Term t=dao.getGoTermById(root_id);

		for(Long associd: dao.getGoaAssociationIdsByGo(t.getAccession()))
			{
			Association assoc=dao.getGoaAssociationById(associd);
			JAXBElement<Association> jxb= new JAXBElement<Association>(
					new QName(Go.NS,"association","goa"),
					Association.class,
					assoc
					);
			dao.getMarshaller().marshal(jxb, w);
			}
		for(Long child_id:dao.getChildIdByGoTermId(root_id))
			{
			recursivegoa(dao,w,seen,child_id);
			}
		}
	
	@GET
	@Path("/annotations/{id}")
	@Produces("text/xml")
	public Response getAnnotations(@PathParam("id") final String id)
		throws WebApplicationException
		{
		StreamingOutput res=new StreamingOutput()
			{
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				CardioServeDao dao=null;
				try {
					dao=new CardioServeDao(myDB);
					XMLOutputFactory xmlfactory= XMLOutputFactory.newInstance();
					xmlfactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
					XMLStreamWriter w= xmlfactory.createXMLStreamWriter(out);
					w.writeStartDocument("UTF-8","1.0");
					w.writeStartElement("goa","associations",Associations.NS);
					dao.getMarshaller().setProperty(Marshaller.JAXB_FRAGMENT,true);
					long root_id=dao.getGoTermIdByUri(id);
					if(root_id!=-1)
						{
						Set<Long> seen=new HashSet<Long>();
						recursivegoa(dao,w, seen, root_id);
						}
					w.writeEndElement();
					w.writeEndDocument();
					w.flush();
					w.close();
					} 
				catch (XMLStreamException e)
					{
					throw new WebApplicationException(e);
					}
				catch (JAXBException e)
					{
					throw new WebApplicationException(e);
					}
				catch (SQLException e)
					{
					throw new WebApplicationException(e);
					}
				finally
					{
					if(dao!=null) dao.close();
					}
				}
			};
		return Response.ok(res).build();
		}
	
	
}
