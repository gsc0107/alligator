package com.github.lindenb.alligator.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.github.lindenb.alligator.bio.ucsc.hg19.Chromosomes;
import com.github.lindenb.alligator.bio.ucsc.hg19.Snp137;
import com.github.lindenb.bdbutils.bio.interval.BedSegment;
import com.github.lindenb.bdbutils.db.EnvironmentWrapper;
import com.github.lindenb.bdbutils.util.BerkeleyDbUtils;
import com.github.lindenb.bdbutils.util.Timer;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;

public class Model extends EnvironmentWrapper
	{
	/** berkeleydb DB home */
	private File dbHome;
	private OpenMode openMode;
	
	
	/** SNP 137 */
	private Snp137DB snp137;
	
	
	
	public Model(File dbHome)
		{
		this.dbHome=dbHome;
		this.snp137=new Snp137DB();
		}
	
	
	
	public void open(OpenMode mode) throws IOException
		{
		if(isOpen()) return;
		
		this.openMode=mode;
		BerkeleyDbUtils.createDbHomeIfNotExist(this.dbHome);
		EnvironmentConfig cfg=new EnvironmentConfig();
		cfg.setTransactional(true);
		cfg.setAllowCreate(mode!=OpenMode.WEB);
		cfg.setReadOnly(false);
		cfg.setCachePercent(80);
		cfg.setConfigParam(EnvironmentConfig.LOG_FILE_MAX,"250000000");
		
		if(mode!=OpenMode.WEB)
			{
			cfg.setConfigParam(EnvironmentConfig.ENV_RUN_CLEANER,  "false");
			cfg.setConfigParam(EnvironmentConfig.ENV_RUN_CHECKPOINTER, "false");
			cfg.setConfigParam(EnvironmentConfig.ENV_RUN_IN_COMPRESSOR, "false");
			}
		
		super.open(this.dbHome, cfg);
		}
	
	private void loadDbsnp(String[] args)
			throws Exception
		{
		open(OpenMode.BUILD_SNP137);
		Timer t=new Timer();
		t.setBdbHome(dbHome);
		t.setExpectedSize(56248699);
		
		
		DatabaseEntry data=new DatabaseEntry();
		DatabaseEntry key=new DatabaseEntry();
		long record_id=0;
		Pattern pat=Pattern.compile("[\t]");
		InputStream instream=null;
		//new URL("http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/snp137.txt.gz").openStream()
		instream=new FileInputStream("/home/lindenb/src/cardioserve/snp137.txt.gz");
		BufferedReader r=new BufferedReader(new InputStreamReader(new GZIPInputStream(instream)));
		String line;
		Transaction txn=null;//this.getEnvironment().beginTransaction(null, null);
		
		snp137.open(this,txn,OpenMode.BUILD_SNP137);
		Transaction txn2=this.getEnvironment().beginTransaction(null, null);
		
		while((line=r.readLine())!=null)
			{
			++record_id;
			String tokens[]=pat.split(line);
			Snp137 snp=new Snp137();
			BedSegment seg=new BedSegment(
					//Short.parseShort(tokens[0]),
					(byte)Chromosomes.getInstance().getIndex(tokens[1]),
					Integer.parseInt(tokens[2]),
					Integer.parseInt(tokens[3])
					);
			
			snp=new Snp137(record_id, Integer.parseInt(tokens[4].substring(2)),seg);
			
			this.snp137.getDataBinding().objectToEntry(snp, data);
			LongBinding.longToEntry(record_id, key);
			this.snp137.putEntries(txn2, key, data);
			t.insert();
			if(record_id%100000==0)
				{
				System.out.println(t.toString());
				txn2.commit();
				txn2=this.getEnvironment().beginTransaction(null, null);
				}
			//if(record_id>100000) break;
			}
		r.close();
		

		
		/*
		for(Iterator<Pair<Long,Snp137> > iter=this.snp137.iterator(txn, null);
				iter.hasNext();
				)
				{
				System.out.println(iter.next());	
				}*/
		System.out.println(this.snp137.getRs2snp().getAll(txn2,140194106, null));
		
		
		
		if(!this.snp137.isEmpty(txn2))
			{
			long last_id=this.snp137.getLastKey(txn2);
			System.err.println("Delete from "+record_id+" to "+last_id);
			this.snp137.delete(txn2,record_id+1,last_id,true);
			}
		else
			{
			System.err.println("Sn,p is empty ?");	
			}
		
		if(txn2!=null) txn2.commit();
		if(txn!=null) txn.commit();
		
		this.snp137.close();
		
		close();
		System.out.println(t.toString());
		}
	
	@Override
	public void close()
		{
		if(isOpen() && this.openMode!=OpenMode.WEB)
			{
			checkpointAndSync();
			getEnvironment().cleanLog();
			}
		super.close();
		}
	
	
	public static void main(String[] args)
		throws Exception
		{
		System.setProperty("http.proxyHost", "cache.ha.univ-nantes.fr");
		System.setProperty("http.proxyPort", "3128");
		new Model(new File("/home/lindenb/src/cardioserve/tmp.bdbd.home")).loadDbsnp(args);
		}
}
