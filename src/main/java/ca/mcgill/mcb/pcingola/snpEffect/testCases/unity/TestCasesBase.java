package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.Random;

import org.junit.After;
import org.junit.Before;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;

/**
 * Base class for some test cases
 *
 * @author pcingola
 */
public class TestCasesBase {

	protected boolean debug = false;
	protected boolean verbose = false || debug;

	// Parameters for creating fake genome
	protected int randSeed;
	protected String genomeName;
	protected boolean addUtrs;
	protected boolean onlyPlusStrand;
	protected int maxGeneLen;
	protected int maxTranscripts;
	protected int maxExons;
	protected int minExons;

	protected Random rand;
	protected Config config;
	protected Genome genome;
	protected Chromosome chromosome;
	protected Gene gene;
	protected Transcript transcript;
	protected SnpEffectPredictor snpEffectPredictor;
	protected String chromoSequence = "";
	protected char chromoBases[];
	protected CodonTable codonTable;

	@After
	public void after() {
		config = null;
		codonTable = null;
		genome = null;
		chromosome = null;
		gene = null;
		transcript = null;
		snpEffectPredictor = null;
		chromoBases = null;
		chromoSequence = null;
	}

	@Before
	public void before() {
		init();
		initSnpEffPredictor();
	}

	protected void init() {
		randSeed = 20141128;
		genomeName = "testCase";
		addUtrs = false;
		onlyPlusStrand = true;
		maxGeneLen = 1000;
		maxTranscripts = 1;
		maxExons = 5;
		minExons = 1;

		initRand();
	}

	protected void initRand() {
		rand = new Random(randSeed);
	}

	/**
	 * Create a predictor
	 */
	protected void initSnpEffPredictor() {
		// Create a config and force out snpPredictor
		if (config == null || config.getGenome() == null || !config.getGenome().getGenomeName().equals(genomeName)) {
			config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		}

		// Initialize factory
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositive(onlyPlusStrand); // WARNING: We only use positive strand here (the purpose is to check HGSV notation, not to check annotations)
		sepf.setAddUtrs(addUtrs);
		sepf.setMinExons(minExons);

		// Create predictor
		snpEffectPredictor = sepf.create();

		// Update config
		config.setSnpEffectPredictor(snpEffectPredictor);
		config.getSnpEffectPredictor().setSpliceRegionExonSize(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMin(0);
		config.getSnpEffectPredictor().setSpliceRegionIntronMax(0);

		// Chromosome sequence
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		chromosome = sepf.getChromo();
		genome = config.getGenome();
		codonTable = genome.codonTable();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();

		// Create genomic sequences
		genome.getGenomicSequences().addGeneSequences(chromosome.getId(), chromoSequence);
	}

	/**
	 * Prepend first's exons sequence with a given one 
	 */
	protected void prependSequenceToFirstExon(String prepend) {
		Exon firstEx = transcript.sortedStrand().get(0);
		String seq = firstEx.getSequence();
		firstEx.setSequence(prepend + seq);
		transcript.resetCdsCache();
	}

}
