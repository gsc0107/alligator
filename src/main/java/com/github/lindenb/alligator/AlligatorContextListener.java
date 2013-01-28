package com.github.lindenb.alligator;



import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;

public class AlligatorContextListener implements ServletContextListener
	{
 
	
	@Override
	public void contextInitialized(ServletContextEvent ctx)
		{
		String configjson=ctx.getServletContext().getInitParameter("config.json");
		if(configjson==null) throw new RuntimeException("Cannot get init param");
		ObjectMapper mapper = new ObjectMapper();
		try {
			File f=new File(ctx.getServletContext().getRealPath(configjson));
			System.err.println(f);
			JsonNode node=mapper.readTree(f);
			ctx.getServletContext().setAttribute("alligator.config", node);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			throw new RuntimeException(e);
			}
		
		}
	
	@Override
	public void contextDestroyed(ServletContextEvent ctx)
		{
		ctx.getServletContext().removeAttribute("alligator.config");
		}
 
}