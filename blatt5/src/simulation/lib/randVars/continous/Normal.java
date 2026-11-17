package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/**
 * Gamma distributed random variable.
 */
public class Normal extends RandVar {
	private boolean cacheEmpty;
	private double mu;
	private double sigma2;
	private double cache;

	/**
	 * Constructor 1
	 * @param rng random number generator
	 */
	public Normal(RNG rng) {
		this(rng, 0, 1);
	}
	
	/**
	 * Constructor 2
	 * @param rng random number generator
	 * @param mean mean 
	 * @param var variance
	 */
	public Normal(RNG rng, double mean, double var) {
		super(rng);
		setMeanAndStdDeviation(mean, Math.sqrt(var));		
	}

	/**
	 * @see RandVar#getMean()
	 */
	@Override
	public double getMean() {
		return mu;
	}

	/**
	 * @see RandVar#getRV()
	 */
	@Override
	public double getRV() {
        if (!cacheEmpty) {
            cacheEmpty = true;
            return cache;
        }else{
            double v1 = 2 * rng.rnd() - 1;
            double v2 = 2 * rng.rnd() - 1;
            double w;
    
            while ((w = v1 * v1 + v2 * v2) > 1) {
                v1 = 2 * rng.rnd() - 1;
                v2 = 2 * rng.rnd() - 1;
            }
    
            double y = Math.sqrt(-2 * Math.log(w) / w );
            cache = mu + Math.sqrt(sigma2) * v2 * y;
            cacheEmpty = false;
            return mu + Math.sqrt(sigma2) * v1 * y;
        }
	}

	/**
	 * @see RandVar#getType()
	 */
	@Override
	public String getType() {
		return "Normal";
	}

	/**
	 * @see RandVar#getVariance()
	 */
	@Override
	public double getVariance() {
		return sigma2;
	}

	/**
	 * @see RandVar#setMean(double)
	 */
	@Override
	public void setMean(double m) {
		setMeanAndStdDeviation(m,getStdDeviation());
	}

	/**
	 * @see RandVar#setMeanAndStdDeviation(double, double)
	 */
	@Override
	public void setMeanAndStdDeviation(double m, double s) {
        cacheEmpty = true;
        mu = m;    
        sigma2 = s * s;
	}

	/**
	 * @see RandVar#setStdDeviation(double)
	 */
	@Override
	public void setStdDeviation(double s) {
		setMeanAndStdDeviation(getMean(),s);
	}

	@Override
	public String toString() {
		return super.toString() + 
			"\tparameters:\n" +
			"\t\tmu: " + mu + "\n" +
			"\t\tsigma2: " + sigma2 + "\n";
	}	
	
}
