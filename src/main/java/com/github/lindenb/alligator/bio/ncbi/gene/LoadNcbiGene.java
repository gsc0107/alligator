package com.github.lindenb.alligator.bio.ncbi.gene;

import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
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
import org.w3c.dom.Text;

public class LoadNcbiGene
	{
	private Connection connection;
	
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
    	for(Node c1=node.getFirstChild();c1!=null;c1=c1.getNextSibling())
    		{
    		if(c1.getNodeType()==Node.ELEMENT_NODE)
    			{
    			hasChild=true;
    			cleanup(node);
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
    
	public void run(InputStream in)
		throws XMLStreamException,SQLException
		{
        TransformerFactory trFactory=TransformerFactory.newInstance();
        Transformer tr=trFactory.newTransformer();
		
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
        XPathExpression locusExpr=xpath.compile("/Entrezgene/Entrezgene_gene/Gene-ref/Gene-ref_locus");
        XPathExpression descExpr=xpath.compile("/Entrezgene/Entrezgene_gene/Gene-ref/Gene-ref_desc");
        XPathExpression refGeneExpr=xpath.compile("/Entrezgene/Entrezgene_locus/Gene-commentary/Gene-commentary_products/Gene-commentary[Gene-commentary_heading='Reference']/Gene-commentary_accession");
        XPathExpression ensemblExpr=xpath.compile("/Entrezgene/Entrezgene_gene/Gene-ref/Gene-ref_db/Dbtag[Dbtag_db='Ensembl']/Dbtag_tag/Object-id/Object-id_str");

        
        
		//create xml stream factory
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        XMLEventReader reader= xmlInputFactory.createXMLEventReader(in);
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
            
            //get locus name with xpath
            String locus=(String)locusExpr.evaluate(root, XPathConstants.STRING);
            
            String ensembl=(String)ensemblExpr.evaluate(root, XPathConstants.STRING);
            String refGene=(String)refGeneExpr.evaluate(root, XPathConstants.STRING);
          
            Set<String> names=new HashSet<String>();
            
            
            StringWriter sw=new StringWriter();
            tr.transform(new DOMSource(dom), new StreamResult(sw));
            
            
            PreparedStatement pstmt=connection.prepareStatement(
            	"insert into NCBIGENE.gene(id,content) values(?,?)");
            pstmt.setLong(1, 0L);
            pstmt.setString(2, sw.toString());
            pstmt.executeUpdate();
            
            pstmt=connection.prepareStatement(
                	"insert into NCBIGENE.geneName(gene_id,name) values(?,?)");
            for(String name:names)
            	{
                pstmt.setLong(1, 0L);
                pstmt.setString(2,name);
                pstmt.executeUpdate();
            	}
           
           }
		}
	}
