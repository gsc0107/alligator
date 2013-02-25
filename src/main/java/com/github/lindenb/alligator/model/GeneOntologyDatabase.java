package com.github.lindenb.alligator.model;


import java.util.HashSet;
import java.util.Set;

import com.github.lindenb.bdbutils.binding.JAXBBinding;
import com.github.lindenb.bdbutils.util.DefaultDictionary;
import com.github.lindenb.bdbutils.util.Dictionary;

public class GeneOntologyDatabase
	extends AbstractOntDatabase<generated.org.geneontology.rdf.RDF.Term>
	{
	private static final Dictionary GO_DICTIONARY=new DefaultDictionary(new String[]{
			"go",// 0
			"http://www.geneontology.org/dtds/go.dtd#",// 1
			"rdf",// 2
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#",// 3
			"RDF",// 4
			"term",// 5
			"about",// 6
			"accession",// 7
			"name",// 8
			"synonym",// 9
			"definition",// 10
			"is_a",// 11
			"resource",// 12
			"dbxref",// 13
			"parseType",// 14
			"database_symbol",// 15
			"reference",// 16
			"comment",// 17
			"regulates",// 18
			"negatively_regulates",// 19
			"part_of",// 20
			"positively_regulates"// 21
			},true);
	
	
	public static class GoBinding
		extends JAXBBinding<generated.org.geneontology.rdf.RDF.Term>
		{
		public GoBinding()
			{
			super(generated.org.geneontology.rdf.RDF.Term.class,GO_DICTIONARY);
			}
		}
	
	public GeneOntologyDatabase()
		{
		setDataBinding(new GoBinding());
		}
	
	@Override
	public Set<String> extractParents(generated.org.geneontology.rdf.RDF.Term t)
		{
		Set<String> h=new HashSet<String>();
		for(generated.org.geneontology.go.IsA sub:t.getIsA())
			{
			h.add(sub.getResource());
			}
		return h;
		}

	@Override
	public String extractURI(generated.org.geneontology.rdf.RDF.Term t)
		{
		return t.getAbout();
		}

	}
