package com.github.lindenb.alligator.sql;


import java.sql.SQLException;
import javax.sql.DataSource;

public class AlligatorDao
	extends AbstractAlligatorDao
	{
	public AlligatorDao(DataSource ds) throws SQLException
		{
		super(ds);
		}

	}
