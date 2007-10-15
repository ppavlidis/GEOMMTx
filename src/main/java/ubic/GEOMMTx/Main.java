package ubic.GEOMMTx;

import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import ubic.GEOMMTx.mappers.BirnLexMapper;
import ubic.GEOMMTx.mappers.DiseaseOntologyMapper;
import ubic.GEOMMTx.mappers.FMALiteMapper;
import ubic.gemma.model.common.description.BibliographicReference;
import ubic.gemma.model.expression.bioAssay.BioAssay;
import ubic.gemma.model.expression.experiment.ExperimentalDesign;
import ubic.gemma.model.expression.experiment.ExperimentalFactor;
import ubic.gemma.model.expression.experiment.ExpressionExperiment;
import ubic.gemma.model.expression.experiment.ExpressionExperimentService;
import ubic.gemma.persistence.PersisterHelper;
import ubic.gemma.util.AbstractSpringAwareCLI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class Main extends AbstractSpringAwareCLI {

	private Text2Owl text2Owl;

	Map<String, Set<String>> seen;

	Model model;

	String currentShortName;

	Cache memoryOnlyCache;

	public Main() {
		seenText = new HashMap<String, Integer>();
		// 1000 is 18 minutes - one experiment takes 9 minutes
		CacheManager singletonManager = CacheManager.create();

		memoryOnlyCache = new Cache("testCache", 25, false, false, 5000, 1500);
		singletonManager.addCache(memoryOnlyCache);

		model = ModelFactory.createDefaultModel();
	}
	
	

	@Override
	protected void buildOptions() {
	}

	public static void main(String[] args) {
		Main p = new Main();

		// DocumentRange t = null;

		try {
			Exception ex = p.doWork(args);
			if (ex != null) {
				ex.printStackTrace();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Exception doWork(String[] args) {
		long totaltime = System.currentTimeMillis();
		Exception err = processCommandLine(
				"Expression experiment bibref finder ", args);
		if (err != null)
			return err;

		ExpressionExperimentService ees = (ExpressionExperimentService) this
				.getBean("expressionExperimentService");
		Collection<ExpressionExperiment> experiments = ees.loadAll();

		seen = new HashMap<String, Set<String>>();

		long time = System.currentTimeMillis();
		text2Owl = null;

		text2Owl = new Text2Owl();
		text2Owl.addMapper(new BirnLexMapper());
		text2Owl.addMapper(new FMALiteMapper());
		text2Owl.addMapper(new DiseaseOntologyMapper());

		System.out.println("Total initialization time:"
				+ (System.currentTimeMillis() - time) / 1000 + "s");

		int c = 0;
		for (ExpressionExperiment experiment : experiments) {

			log.info("Experiment number:" + ++c + " of " + experiments.size());
			// if (c++ > 10)
			// break;
			time = System.currentTimeMillis();

			ees.thawLite(experiment);

			String shortName = experiment.getShortName();
			currentShortName = shortName;

			// getName()
			System.out.println("getName():" + experiment.getName());
			printAndShow(experiment.getName(), "  ", shortName);
			doRDF(experiment.getName(), "getName");

			// getDescription()
			System.out.println("getDescription():"
					+ experiment.getDescription());
			printAndShow(experiment.getDescription(), "  ", shortName);
			doRDF(experiment.getDescription(), "getDescription");

			// Primary Publication
			BibliographicReference ref = experiment.getPrimaryPublication();
			if (ref != null) {
				System.out.println("ref.getTitle():" + ref.getTitle());
				printAndShow(ref.getTitle(), "    ", shortName);
				doRDF(ref.getTitle(), "ref.getTitle");

				System.out.println("ref.getAbstractText():"
						+ ref.getAbstractText());
				if (ref.getAbstractText() != null) {
					printAndShow(ref.getAbstractText(), "    ", shortName);
					doRDF(ref.getAbstractText(), "ref.getAbstractText");
				}
			}

			// Secondary Publications
			Collection<BibliographicReference> others = experiment
					.getOtherRelevantPublications();
			if (others != null) {
				for (BibliographicReference other : others) {
					System.out.println("other.getTitle():" + other.getTitle());
					printAndShow(other.getTitle(), "    ", shortName);
					doRDF(other.getTitle(), "other.getTitle");

					System.out.println("other.getAbstractText():"
							+ other.getAbstractText());
					if (other.getAbstractText() != null) {
						printAndShow(other.getAbstractText(), "    ", shortName);
						doRDF(other.getAbstractText(), "other.getAbstractText");
					}
				}
			}

			// Factors
			ExperimentalDesign design = experiment.getExperimentalDesign();
			if (design != null) {

				System.out.println("design.getDescription():"
						+ design.getDescription());
				printAndShow(design.getDescription(), "    ", shortName);
				doRDF(design.getDescription(), "design.getDescription");

				// log.info("desc:"+
				// experiment.getExperimentalDesign().getDescription());

				Collection<ExperimentalFactor> factors = design
						.getExperimentalFactors();
				if (factors != null) {
					for (ExperimentalFactor factor : factors) {
						System.out.println("factor.getName():"
								+ factor.getName());
						printAndShow(factor.getName(), "    ", shortName);
						doRDF(factor.getName(), "factor.getName");

						System.out.println("factor.getDescription():"
								+ factor.getDescription());
						printAndShow(factor.getDescription(), "    ", shortName);
						doRDF(factor.getDescription(), "factor.getDescription");
					}
				}
			}

			// System.out.println( "getSource():" + experiment.getSource() );
			// //null

			// iterate BioAssays
			for (BioAssay ba : experiment.getBioAssays()) {
				if (ba.getName() != null) {
					System.out.println("bioassay.getName():" + ba.getName());
					printAndShow(ba.getName(), "    ", shortName);
					doRDF(ba.getName(), "ba.getName");
				}

				if (ba.getDescription() != null) {
					System.out.println("bioassay.getDescription():"
							+ ba.getDescription());
					printAndShow(ba.getDescription(), "    ", shortName);
					doRDF(ba.getDescription(), "ba.getDescription");
				}
			}
			System.out.println("--------------------------------------------");
			log.info(((System.currentTimeMillis() - time) / 1000)
					+ "s for whole experiment, writing out to save memory");
			try {
				model.write(new FileWriter(c+".rdf"));
				model = ModelFactory.createDefaultModel();
			} catch (Exception e) {
				e.printStackTrace();
			}


		}

		/*
		 * String[] examples = { "Genetic alterations in mouse medulloblastomas
		 * and generation of tumors from cerebellar granule neuron precursors",
		 * "Serum stimulation of fibroblasts with cycloheximide" }; for ( String
		 * example : examples ) { System.out.println( example ); }
		 */
		printSeen();

		int ones = 0;
		int other = 0;
		for (int occurances : seenText.values()) {
			if (occurances == 1)
				ones++;
			else
				other = other + occurances;
		}
		log.info("ones:" + ones);
		log.info("other:" + other);
		log.info("Seen with cache:" + seenWithEch);
		log.info("number of times I found it:"
				+ memoryOnlyCache.getMemoryStoreHitCount());

		System.out.println("Total time:"
				+ (System.currentTimeMillis() - totaltime) / 1000 + "s");
		return null;
	}

	public void printSeen() {
		for (String key : seen.keySet()) {
			Set<String> datasets = seen.get(key);
			System.out.print(datasets.size() + "|" + key + "|");
			for (String dataset : datasets) {
				System.out.print(dataset + ".");
			}
			System.out.println();
		}
	}

	// model.write( new FileWriter( "RDFfromText2Owl.main.rdf" ) );
	/*
	 * desc becomes the URI, the text results are attatched to
	 */
	private Map<String, Integer> seenText;

	private int seenWithEch = 0;

	public void doRDF(String text, String desc) {
		text = text.replaceAll("Source GEO sample is GSM[0-9]+", "");
		text = text.replaceAll("Last updated [(]according to GEO[)].+[\\d]{4}",
				"");
		Integer seen = seenText.get(text);
		if (seen == null)
			seenText.put(text, 1);
		else {
			seenText.put(text, seen + 1);
			// log.info(text);
		}

		if (memoryOnlyCache.get(text) == null) {
			memoryOnlyCache.put(new Element(text, "x"));
		} else {
			seenWithEch++;
		}

		String shortName = currentShortName;
		String cleanText = desc.replaceAll("[()]", "");
		String thisObjectURI = "http://www.purl.org/GEO/" + shortName + "#"
				+ cleanText;
		String GEOObjectURI = "http://www.purl.org/GEO/" + shortName + "#"
				+ shortName;
		Resource GEOObject = model.createResource(GEOObjectURI);
		Resource root = model.createResource(thisObjectURI);

		GEOObject.addProperty(model
				.createProperty("http://www.purl.org/leon/umls#describedBy"),
				root);

		if (text2Owl == null)
			return;

		model = text2Owl.processText(text, root);
	}

	public void printAndShow(String text, String indent, String shortName) {
		// get rid of dupes

		// Collection<String> URIs = new CopyOnWriteArraySet<String>();
		// URIs.addAll( text2Owl.processText( text ) );
		// for ( String s : URIs ) {
		// // lets record what we've seen
		// Set<String> datasets = seen.get( s );
		// if ( datasets == null ) {
		// datasets = new HashSet<String>();
		// seen.put( s, datasets );
		// }
		// datasets.add( shortName );
		//
		// System.out.println( indent + s );
		// }
		// System.out.println( indent + "(" + ( ( System.currentTimeMillis() -
		// time ) / 1000 ) + "s)" );
	}

	protected void processOptions() {
		super.processOptions();
	}
}