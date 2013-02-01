package com.github.lindenb.alligator.bio.ncbi.gene;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class LoadNcbiGene
	{
	private Connection connection=null;
	
	private LoadNcbiGene()
		{
		
		}
	
    /** parse Gene doc */
    private void parseDoc(
            XMLEventReader reader,
            Document dom,
            Element root)
        throws XMLStreamException
        {
        while(reader.hasNext())
            {
            XMLEvent evt=reader.nextEvent();
            if(evt.isEndElement())
                {
                return;
                }
            else if(evt.isStartElement())
                {
                Element node=dom.createElement(evt.asStartElement().getName().getLocalPart());
                root.appendChild(node);
                Iterator<?> iter=evt.asStartElement().getAttributes();
                while(iter.hasNext())
                    {
                    Attribute att=(Attribute)iter.next();
                    node.setAttribute(att.getName().getLocalPart(), att.getValue());
                    }
                parseDoc(reader,dom,node);
                }
            else if(evt.isCharacters())
                {
                root.appendChild(dom.createTextNode(evt.asCharacters().getData()));
                }
            }
        }
    
    private void cleanup(Node node)
    	{
    	boolean hasChild=false;
    	for(Node c1=node.getFirstChild();
    			 c1!=null;
    			 c1=c1.getNextSibling()
    		)
    		{
    		if(c1.getNodeType()==Node.ELEMENT_NODE)
    			{
    			hasChild=true;
    			cleanup(c1);
    			}
    		}
    	if(hasChild)
    		{
    		Node c1=node.getFirstChild();
    		while(c1!=null)
    			{
    			Node rm=c1;
				c1=c1.getNextSibling();
    			if(rm.getNodeType()==Node.TEXT_NODE &&
    			   Text.class.cast(rm).getData().trim().isEmpty())
    				{
    				node.removeChild(rm);
    				}
    			}
    		}
    	}
    
	public void run(Connection con,InputStream in)
		throws Exception
		{
        TransformerFactory trFactory=TransformerFactory.newInstance();
        Transformer tr=trFactory.newTransformer();
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		
		DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(false);
		documentBuilderFactory.setCoalescing(true);
		documentBuilderFactory.setIgnoringComments(true);
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder domBuilder=documentBuilderFactory.newDocumentBuilder();
		
		//xpath
		 XPathFactory xf=XPathFactory.newInstance();
		 XPath xpath=xf.newXPath();
        //create XPATH expressions
        XPathExpression idExpr=xpath.compile("/Entrezgene/Entrezgene_track-info/Gene-track/Gene-track_geneid");
        XPathExpression locusExpr=xpath.compile("/Entrezgene/Entrezgene_gene/Gene-ref/Gene-ref_locus/text()");
        XPathExpression ensemblExpr=xpath.compile("/Entrezgene/Entrezgene_gene/Gene-ref/Gene-ref_db/Dbtag[Dbtag_db='Ensembl']/Dbtag_tag/Object-id/Object-id_str/text()");
        XPathExpression synonyms=   xpath.compile("/Entrezgene/Entrezgene_gene/Gene-ref/Gene-ref_syn/Gene-ref_syn_E/text()");

      
        
        
		//create xml stream factory
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        XMLEventReader reader= xmlInputFactory.createXMLEventReader(in);
        long name_ids=0;
        while(reader.hasNext())
            {
            XMLEvent evt=reader.nextEvent();
            if(!evt.isStartElement()) continue;
            //it is a gene
            if(!evt.asStartElement().getName().getLocalPart().equals("Entrezgene")) continue;
            Document dom=domBuilder.newDocument();
            Element root=dom.createElement("Entrezgene");
            dom.appendChild(root);
            //get the whole record as DOM
            parseDoc(reader,dom,root);
            cleanup(dom);
            
            long geneid=Long.parseLong((String)idExpr.evaluate(root, XPathConstants.STRING));
            
            
            Set<String> names=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

            NodeList L=(NodeList)synonyms.evaluate(root, XPathConstants.NODESET);
            for(int i=0;i< L.getLength();++i)
            	{
            	names.add(L.item(i).getNodeValue());
            	}
            L=(NodeList)ensemblExpr.evaluate(root, XPathConstants.NODESET);
            for(int i=0;i< L.getLength();++i)
	        	{
	        	names.add(L.item(i).getNodeValue());
	        	}
            L=(NodeList)locusExpr.evaluate(root, XPathConstants.NODESET);
            for(int i=0;i< L.getLength();++i)
	        	{
	        	names.add(L.item(i).getNodeValue());
	        	}

            
                      
            
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            GZIPOutputStream gzos=new GZIPOutputStream(baos);
            tr.transform(new DOMSource(dom), new StreamResult(gzos));
            gzos.finish();
            gzos.close();
            byte array[]=baos.toByteArray();
            
            StringWriter sw=new StringWriter();
            tr.transform(new DOMSource(dom), new StreamResult(sw));
            
            System.out.println(""+geneid+" "+array.length+" "+sw.toString().length()+" "+((double)sw.toString().length()/array.length));
            
            
            PreparedStatement pstmt=connection.prepareStatement(
            	"insert into NCBI.gene(id,gzcontent) values(?,?)");
            pstmt.setLong(1, geneid);
            ByteArrayInputStream bais=new ByteArrayInputStream(array);
            pstmt.setBlob(2, bais,array.length);
            pstmt.executeUpdate();
            bais.close();
            
            pstmt=connection.prepareStatement(
                	"insert into NCBI.NAME2GENE(id,gene_id,name) values(?,?,?)");
            for(String name:names)
            	{
            	pstmt.setLong(1,++name_ids);
                pstmt.setLong(2, geneid);
                pstmt.setString(3,name);
                pstmt.executeUpdate();
            	}
           
           }
		}
	
	
	public void run(String[] args) throws Exception
		{
		String dbURL=null;
		int optind=0;
		while(optind< args.length)
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
			else if(args[optind].startsWith("--"))
				{
				optind++;
				break;
				}
			else if(args[optind].equals("--"))
				{
				optind++;
				break;
				}
			else if(args[optind].startsWith("-"))
				{
				System.err.println("Unknown option "+args[optind]);
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
			System.err.println("Illegal number of arguments.");
			System.exit(-1);
			}
		Class.forName("org.apache.derby.jdbc.ClientDriver");
		Connection con=DriverManager.getConnection(dbURL);
		con.setAutoCommit(false);
		run(con,System.in);
		con.commit();
		con.close();
		}
	
	public static void main(String[] args)
		throws Exception
		{
		LoadNcbiGene app=new LoadNcbiGene();
		app.run(args);
		}

	}
