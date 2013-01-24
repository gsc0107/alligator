package com.github.lindenb.alligator.sql;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public abstract class AbstractAlligatorDao
	{
	private Connection con=null;
	private PreparedStatement pstmt=null;
	private ResultSet row=null;
	private JAXBContext jaxbCtxt=null;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	
	
	protected AbstractAlligatorDao(DataSource ds)
		throws SQLException
		{
		this.con=ds.getConnection();
		}
	
	protected Set<String> fillJABXPackages(Set<String> set)
		{
		return set;
		}
	
	protected JAXBContext getJAXBContext() throws JAXBException
		{
		if(this.jaxbCtxt==null)
			{
			Set<String> packs=fillJABXPackages(new HashSet<String>());
			StringBuilder b=new StringBuilder();
			for(String pack:packs)
				{
				if(b.length()!=0) b.append(":");
				b.append(pack);
				}
			
			this.jaxbCtxt=JAXBContext.newInstance(
			    b.toString()
			  	);
			}
		return this.jaxbCtxt;
		}
	
	public Marshaller getMarshaller()  throws JAXBException
		{
		if(marshaller==null) marshaller=getJAXBContext().createMarshaller();
		return marshaller;
		}
	public Unmarshaller getUnmarshaller()  throws JAXBException
		{
		if(unmarshaller==null) unmarshaller=getJAXBContext().createUnmarshaller();
		return unmarshaller;
		}

	
	@Override
	protected void finalize() throws Throwable
		{
		this.close();
		super.finalize();
		}
	
	protected void closeResultSet()
		{
		if(row!=null)
			{
			try { this.row.close();}
			catch(SQLException err) {}
			this.row=null;
			}
		}

	
	protected void closeStmt()
		{
		closeResultSet();
		if(pstmt!=null)
			{
			try { this.pstmt.close();}
			catch(SQLException err) {}
			this.pstmt=null;
			}
		}
	
	public void close()
		{
		closeStmt();
		if(con!=null)
			{
			try { this.con.close();}
			catch(SQLException err) {}
			this.con=null;
			}
		}
	
	
	@Override
	public String toString()
		{
		return getClass().getName();
		}
	}
