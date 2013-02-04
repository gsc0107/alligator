package com.github.lindenb.alligator.bio.goa;

import generated.uk.ac.ebi.goa.Association;
import generated.uk.ac.ebi.goa.Associations;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;


public class LoadGoa
{
private Logger LOG=Logger.getLogger("cardioserve");

private LoadGoa()
	{
	
	}
private void run2(String[] args) throws Exception
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
	
	if(optind!=args.length)
		{
		System.err.println("Illegal number of arguments "+optind+"/"+args.length);
		System.exit(-1);
		}
	 Class.forName("org.apache.derby.jdbc.ClientDriver");
	 Connection con=DriverManager.getConnection(dbURL);
    
	 javax.xml.bind.JAXBContext jaxbCtxt=javax.xml.bind.JAXBContext.newInstance(
			 	"generated.uk.ac.ebi.goa"
			  	);
	 Unmarshaller unmarshaller=jaxbCtxt.createUnmarshaller();
	 Marshaller marshaller=jaxbCtxt.createMarshaller();
	 marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);

	 con.setAutoCommit(false);
	  PreparedStatement pstmt=con.prepareStatement(
			  "insert into GOA.ASSOCIATION(id,goterm,genesymbol,content) values(?,?,?,?)"
			  );
	  
	  LOG.info("loading GOA...");
	  long id_generator=0;
	 XMLInputFactory factory = XMLInputFactory.newInstance();
	factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
	factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
	factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
	factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
	XMLEventReader r= factory.createXMLEventReader(System.in);
	while(r.hasNext())
		{	
		XMLEvent evt=r.peek();
		if(!evt.isStartElement() ||
			!evt.asStartElement().getName().getLocalPart().equals("association"))
			{
			r.next();
			continue;
			}
		Association assoc= unmarshaller.unmarshal(r, Association.class).getValue();
		++id_generator;
		pstmt.setLong(1, id_generator);
		pstmt.setString(2, assoc.getGoId());
		pstmt.setString(3, assoc.getObjectSymbol());
		
		StringWriter sw=new StringWriter();
		marshaller.marshal(new JAXBElement<Association>(
				new QName(Associations.NS, "association","goa"),
				Association.class, assoc), sw);
		sw.flush();
		pstmt.setString(4,sw.toString());
		pstmt.executeUpdate();
		
		if(id_generator%100==0)
			{
			System.out.println("["+id_generator+"]"+assoc.getObjectSymbol());
			marshaller.marshal(new JAXBElement<Association>(
					new QName(Associations.NS, "association","goa"),
					Association.class, assoc), System.out);
			 System.out.println();
			}
		}
	r.close();
	pstmt.close();
	con.commit();
	con.close();
	}

public static void main(String[] args) throws Exception
	{
	new LoadGoa().run2(args);
	}
}
