package com.github.lindenb.alligator.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.github.lindenb.alligator.bio.ucsc.hg19.Chromosomes;
import com.github.lindenb.alligator.bio.ucsc.hg19.Snp137;
import com.github.lindenb.bdbutils.binding.TupleSerializableBinding;
import com.github.lindenb.bdbutils.bio.interval.AbstractBedDatabaseWrapper;
import com.github.lindenb.bdbutils.bio.interval.BedSegment;
import com.github.lindenb.bdbutils.db.DatabaseWrapper;
import com.github.lindenb.bdbutils.db.EnvironmentWrapper;
import com.github.lindenb.bdbutils.db.SecondaryDatabaseWrapper;
import com.github.lindenb.bdbutils.sort.IntSorter;
import com.github.lindenb.bdbutils.sort.LongSorter;
import com.github.lindenb.bdbutils.util.BerkeleyDbUtils;
import com.github.lindenb.bdbutils.util.Pair;
import com.github.lindenb.bdbutils.util.Timer;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;

public class Model extends EnvironmentWrapper
	{
	private static final boolean TRANSACTIONAL=true;
	class Snp137DB extends DatabaseWrapper<Long, Snp137>
		{
		class Rs2Snp extends SecondaryDatabaseWrapper<Integer,Long,Snp137>
			{
			public Rs2Snp()
				{
				setKeyBinding(new IntegerBinding());
				}
			void open(Transaction txn)
				{
				SecondaryConfig cfg=new SecondaryConfig();
				cfg.setAllowPopulate(true);
				cfg.setAllowCreate(Snp137DB.this.getDatabase().getConfig().getAllowCreate());
				cfg.setTransactional(Snp137DB.this.getDatabase().getConfig().getTransactional());
				cfg.setReadOnly(Snp137DB.this.getDatabase().getConfig().getReadOnly());
				cfg.setBtreeComparator(IntSorter.class);
				cfg.setSortedDuplicates(true);
				cfg.setKeyCreator(new SecondaryKeyCreator()
					{
					@Override
					public boolean createSecondaryKey(SecondaryDatabase secondary,
							DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
						{
						int rsid=Rs2Snp.this.getDataBinding().entryToObject(data).getRsId();
						IntegerBinding.intToEntry(rsid, result);
						return true;
						}
					});
				super.open(txn,"rs2snp137",Snp137DB.this, cfg);
				}
			}
		
		class BinIndex extends AbstractBedDatabaseWrapper<Long,Snp137>
			{
			void open(Transaction txn)
				{
				SecondaryConfig cfg=new SecondaryConfig();
				
				cfg.setAllowPopulate(true);
				cfg.setAllowCreate(Snp137DB.this.getDatabase().getConfig().getAllowCreate());
				cfg.setTransactional(Snp137DB.this.getDatabase().getConfig().getTransactional());
				cfg.setReadOnly(Snp137DB.this.getDatabase().getConfig().getReadOnly());
				cfg.setKeyCreator(new SecondaryKeyCreator()
					{
					@Override
					public boolean createSecondaryKey(SecondaryDatabase secondary,
							DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
						{
						BedSegment o=BinIndex.this.entryToData(data).getSegment();
						BinIndex.this.keyToEntry(o.toTidBinPos(), result);
						return true;
						}
					});
				super.open(txn,"Rs2Pos",Snp137DB.this, cfg);
				}
			}
		
		Rs2Snp rs2snp=new Rs2Snp();
		BinIndex binIndex=new BinIndex();
		
		public Snp137DB()
			{
			setKeyBinding(new LongBinding());
			setDataBinding(new TupleSerializableBinding<Snp137>() {
				@Override
				protected Snp137 newInstance() {
					return new Snp137();
					}
				});

			}
		public void open(Transaction txn)
			{
			DatabaseConfig cfg=new DatabaseConfig();
			cfg.setAllowCreate(true);
			cfg.setTransactional(TRANSACTIONAL);
			cfg.setBtreeComparator(LongSorter.class);
			cfg.setReadOnly(false);
			open(Model.this.getEnvironment(),txn,"snp137",cfg);
			//this.rs2snp.open(txn);
			//this.binIndex.open(txn);
			}
		
		@Override
		public void close() {
			this.rs2snp.close();
			this.binIndex.close();
			super.close();
			}
		
		}
	/** berkeleydb DB home */
	private File dbHome;
	/** SNP 137 */
	private Snp137DB snp137;
	
	public Model(File dbHome)
		{
		this.dbHome=dbHome;
		this.snp137=new Snp137DB();
		super.databases.add(this.snp137);
		}
	
	
	
	void openDatabases(Transaction txn)
		{
		this.snp137.open(txn);
		}
	
	public void open() throws IOException
		{
		BerkeleyDbUtils.createDbHomeIfNotExist(this.dbHome);
		EnvironmentConfig cfg=new EnvironmentConfig();
		cfg.setTransactional(TRANSACTIONAL);
		cfg.setAllowCreate(true);
		cfg.setReadOnly(false);
		cfg.setCachePercent(80);
		cfg.setConfigParam(EnvironmentConfig.LOG_FILE_MAX,"250000000");
		//cfg.setLocking(false);
		
		cfg.setLoggingHandler(new Handler() {
			
			@Override
			public void publish(LogRecord record) {
				System.out.println(record.getMessage());	
				}
			
			@Override
			public void flush() {
				
			}
			
			@Override
			public void close() throws SecurityException {
				
			}
		});
		cfg.getLoggingHandler().setLevel(Level.ALL);
		super.open(this.dbHome, cfg);
		}
	
	private void loadDbsnp(String[] args)
			throws Exception
		{
		open();
		Timer t=new Timer();
		t.setBdbHome(dbHome);
		t.setExpectedSize(56248699);
		Transaction txn=null;
		snp137.open(txn);
		
		DatabaseEntry data=new DatabaseEntry();
		DatabaseEntry key=new DatabaseEntry();
		long record_id=0;
		Pattern pat=Pattern.compile("[\t]");
		InputStream instream=null;
		//new URL("http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/snp137.txt.gz").openStream()
		instream=new FileInputStream("/home/lindenb/src/cardioserve/snp137.txt.gz");
		BufferedReader r=new BufferedReader(new InputStreamReader(new GZIPInputStream(instream)));
		String line;
		Transaction txn2=(TRANSACTIONAL?this.getEnvironment().beginTransaction(null, null):null);
		
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
				txn2.commit();
				System.gc();
				System.out.println(t.toString());
				txn2=(TRANSACTIONAL?this.getEnvironment().beginTransaction(null, null):null);
				}
			//if(record_id>100000) break;
			}
		r.close();
		
		snp137.rs2snp.open(txn);
		snp137.binIndex.open(txn);

		
		/*
		for(Iterator<Pair<Long,Snp137> > iter=this.snp137.iterator(txn, null);
				iter.hasNext();
				)
				{
				System.out.println(iter.next());	
				}*/
		System.out.println(this.snp137.rs2snp.getAll(txn2,140194106, null));
		
		if(txn2!=null) txn2.commit();
		
		if(!this.snp137.isEmpty(txn))
			{
			long last_id=this.snp137.getLastKey(txn);
			txn2=(TRANSACTIONAL?this.getEnvironment().beginTransaction(null, null):null);
			System.err.println("Delete from "+record_id+" to "+last_id);
			this.snp137.delete(txn2,record_id+1,last_id,true);
			txn2.commit();
			}
		else
			{
			System.err.println("Sn,p is empty ?");	
			}
		
		
		if(txn!=null) txn.commit();

		this.snp137.close();
		getEnvironment().cleanLog();
		close();
		System.out.println(t.toString());
		}
	
	
	
	public static void main(String[] args)
		throws Exception
		{
		System.setProperty("http.proxyHost", "cache.ha.univ-nantes.fr");
		System.setProperty("http.proxyPort", "3128");
		new Model(new File("/home/lindenb/src/cardioserve/tmp.bdbd.home")).loadDbsnp(args);
		}
}
