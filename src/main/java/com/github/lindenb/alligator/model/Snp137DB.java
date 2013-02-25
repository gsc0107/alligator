package com.github.lindenb.alligator.model;

import com.github.lindenb.alligator.bio.ucsc.hg19.Snp137;
import com.github.lindenb.bdbutils.binding.TupleSerializableBinding;
import com.github.lindenb.bdbutils.bio.interval.AbstractBedDatabaseWrapper;
import com.github.lindenb.bdbutils.bio.interval.BedSegment;
import com.github.lindenb.bdbutils.db.DatabaseWrapper;
import com.github.lindenb.bdbutils.db.SecondaryDatabaseWrapper;
import com.github.lindenb.bdbutils.sort.IntSorter;
import com.github.lindenb.bdbutils.sort.LongSorter;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;

public class Snp137DB extends DatabaseWrapper<Long, Snp137>
	{
	/* secondary tables rs2snp */
	private Rs2Snp rs2snp=new Rs2Snp();
	/* secondary tables binindex */
	private BinIndex binIndex=new BinIndex();


	
	public class Rs2Snp extends SecondaryDatabaseWrapper<Integer,Long,Snp137>
		{
		public Rs2Snp()
			{
			setKeyBinding(new IntegerBinding());
			setName("rs2snp137");
			}
		@Override
		public SecondaryConfig createDefaultConfig()
			{
			SecondaryConfig cfg=super.createDefaultConfig();
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
			return cfg;
			}
		
		void open(Transaction txn)
			{
			SecondaryConfig cfg=createDefaultConfig();
			cfg.setAllowCreate(Snp137DB.this.getDatabase().getConfig().getAllowCreate());
			cfg.setTransactional(Snp137DB.this.getDatabase().getConfig().getTransactional());
			cfg.setReadOnly(Snp137DB.this.getDatabase().getConfig().getReadOnly());
			super.open(txn,Snp137DB.this, cfg);
			}
		}

	class BinIndex extends AbstractBedDatabaseWrapper<Long,Snp137>
		{
		public BinIndex()
			{
			setName("snp137_binindex");
			}
		@Override
		public SecondaryConfig createDefaultConfig()
			{
			SecondaryConfig cfg=super.createDefaultConfig();
			cfg.setBtreeComparator(IntSorter.class);
			cfg.setSortedDuplicates(true);
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
			return cfg;
			}

		
		void open(Transaction txn)
			{
			SecondaryConfig cfg=createDefaultConfig();
			cfg.setAllowCreate(Snp137DB.this.getDatabase().getConfig().getAllowCreate());
			cfg.setTransactional(Snp137DB.this.getDatabase().getConfig().getTransactional());
			cfg.setReadOnly(Snp137DB.this.getDatabase().getConfig().getReadOnly());
			super.open(txn,Snp137DB.this, cfg);
			}
		}

public Snp137DB()
	{
	setName("snp137");
	setKeyBinding(new LongBinding());
	setDataBinding(new TupleSerializableBinding<Snp137>() {
		@Override
		protected Snp137 newInstance() {
			return new Snp137();
			}
		});

	}
public Rs2Snp getRs2snp()
	{
	return rs2snp;
	}

public BinIndex getBinIndex()
	{
	return binIndex;
	}


@Override
public DatabaseConfig createDefaultConfig()
	{
	DatabaseConfig cfg=super.createDefaultConfig();
	cfg.setBtreeComparator(LongSorter.class);
	cfg.setKeyPrefixing(true);
	cfg.setNodeMaxEntries(1024);
	return cfg;
	}

public void open(Model m,Transaction txn,OpenMode mode)
	{
	DatabaseConfig cfg=createDefaultConfig();
	cfg.setAllowCreate(mode!=OpenMode.WEB);
	cfg.setTransactional(true);
	cfg.setReadOnly(mode!=OpenMode.BUILD_SNP137);
	open(m.getEnvironment(),txn,cfg);
	this.rs2snp.open(txn);
	this.binIndex.open(txn);
	}

@Override
public void close() {
	this.rs2snp.close();
	this.binIndex.close();
	super.close();
	}

}
