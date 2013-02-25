package com.github.lindenb.alligator.model;

import com.github.lindenb.alligator.bio.ucsc.hg19.KnownGene;
import com.github.lindenb.bdbutils.binding.TupleSerializableBinding;
import com.github.lindenb.bdbutils.bio.interval.AbstractBedDatabaseWrapper;
import com.github.lindenb.bdbutils.bio.interval.TidBinPos;
import com.github.lindenb.bdbutils.db.DatabaseWrapper;
import com.github.lindenb.bdbutils.sort.LongSorter;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class KnownGeneDatabase extends DatabaseWrapper<Long, KnownGene>
	{
	private NameDatabase nameDatabase=new NameDatabase();
	private BinDatabase binDatabase=new BinDatabase();
	
	public class NameDatabase
		extends SecondaryInternalDatabaseWrapper<String>
		{
		public NameDatabase()
			{
			setName("hg19_knownGene_name");
			setKeyBinding(new StringBinding());
			}
		@Override
		public SecondaryConfig createDefaultConfig()
			{
			SecondaryConfig cfg= super.createDefaultConfig();
			cfg.setKeyCreator(new SecondaryKeyCreator()
				{
				@Override
				public boolean createSecondaryKey(
						SecondaryDatabase db,
						DatabaseEntry k,
						DatabaseEntry v,
						DatabaseEntry result
						)
					{
					KnownGene kg=getDataBinding().entryToObject(v);
					getKeyBinding().objectToEntry(kg.getName(), result);
					return true;
					}
				});
			return cfg;
			}

		}
	
	public class BinDatabase
		extends AbstractBedDatabaseWrapper<Long, KnownGene>
		{
		public BinDatabase()
			{
			setName("hg19_knownGene_bin");
			}
		@Override
		protected TidBinPos extractTidBinPos(Long k, KnownGene v)
			{
			return null;
			}
	
		}
	
	public KnownGeneDatabase()
		{
		this.setName("hg19_knownGene");
		this.setKeyBinding(new LongBinding());
		this.setDataBinding(new TupleSerializableBinding<KnownGene>()
			{
			@Override
			protected KnownGene newInstance()
				{
				return new KnownGene();
				}
			});
		}
	
	
	
	@Override
	public DatabaseConfig createDefaultConfig()
		{
		DatabaseConfig cfg= super.createDefaultConfig();
		cfg.setKeyPrefixing(true);
		cfg.setBtreeComparator(LongSorter.class);
		return cfg;
		}
	
	}
