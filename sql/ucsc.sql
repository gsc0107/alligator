

--
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--


drop schema hg19 RESTRICT;
create schema hg19;
set schema hg19;

drop index key_knownGene_name ;

drop index key_knownGene_chrom ;

drop index key_knownGene_chrom_2 ;

drop index key_knownGene_protein ;

drop index key_knownGene_align ;


drop table knownGene;

create table knownGene
	(
	id INT not null primary key,
	name varchar(255) NOT NULL,chrom varchar(255) NOT NULL,strand clob NOT NULL,txStart int NOT NULL,txEnd int NOT NULL,cdsStart int NOT NULL,cdsEnd int NOT NULL,exonCount int NOT NULL,exonStarts clob NOT NULL,exonEnds clob NOT NULL,proteinID varchar(40) NOT NULL,alignID varchar(255) NOT NULL
	);

create  index key_knownGene_name ON knownGene(name);

create  index key_knownGene_chrom ON knownGene(chrom,txStart);

create  index key_knownGene_chrom_2 ON knownGene(chrom,txEnd);

create  index key_knownGene_protein ON knownGene(proteinID);

create  index key_knownGene_align ON knownGene(alignID);

drop index key_kgXref_kgID ;

drop index key_kgXref_mRNA ;

drop index key_kgXref_spID ;

drop index key_kgXref_spDisplayID ;

drop index key_kgXref_geneSymbol ;

drop index key_kgXref_refseq ;

drop index key_kgXref_protAcc ;

drop index key_kgXref_rfamAcc ;

drop index key_kgXref_tRnaName ;


drop table kgXref;

create table kgXref
	(
	id INT not null primary key,
	kgID varchar(255) NOT NULL,mRNA varchar(255) NOT NULL,spID varchar(255) NOT NULL,spDisplayID varchar(255) NOT NULL,geneSymbol varchar(255) NOT NULL,refseq varchar(255) NOT NULL,protAcc varchar(255) NOT NULL,description clob NOT NULL,rfamAcc varchar(255) NOT NULL,tRnaName varchar(255) NOT NULL
	);

create  index key_kgXref_kgID ON kgXref(kgID);

create  index key_kgXref_mRNA ON kgXref(mRNA);

create  index key_kgXref_spID ON kgXref(spID);

create  index key_kgXref_spDisplayID ON kgXref(spDisplayID);

create  index key_kgXref_geneSymbol ON kgXref(geneSymbol);

create  index key_kgXref_refseq ON kgXref(refseq);

create  index key_kgXref_protAcc ON kgXref(protAcc);

create  index key_kgXref_rfamAcc ON kgXref(rfamAcc);

create  index key_kgXref_tRnaName ON kgXref(tRnaName);

