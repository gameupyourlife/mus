package simulation.lib.randVars.continous;

import simulation.lib.randVars.RandVar;
import simulation.lib.rng.RNG;

/**
 * Random variable, gamma distributed.
 */
public class Gamma extends RandVar {
	
	private double alpha;
	private double beta;
	private double b1;
	private double a2;
	private double b2;
	private double q2;
	private double theta2;
	private double d2;

	/**
	 * Constructor 1
	 * @param rng random number generator
	 */
	public Gamma(RNG rng) {
		this(rng, 1, 1);
	}
	
	/**
	 * Constructor 2
	 * @param rng random number generator
	 * @param alpha alpha value
	 * @param beta beta value
	 */
	public Gamma(RNG rng, double alpha, double beta) {
		super(rng);
		this.alpha = alpha;
		this.beta = beta;
		init();
	}

	/**
	 * Initializes the random variable
	 */
	private void init() {
		b1 = (Math.exp(1.0) + alpha) /Math.exp(1.0);
		a2 = (1 / Math.sqrt(2 * alpha - 1));
		b2 = alpha - Math.log(4.0);
		q2 = alpha + 1 / a2;
		theta2 = 4.5;
		d2 = 1 + Math.log(theta2);
	}

	/**
	 * @see RandVar#getMean()
	 */
	@Override
	public double getMean() {
		return alpha*beta;
	}

	/**
	 * @see RandVar#getRV()
	 */
	@Override
	public double getRV() {
		return beta * getTransformedGammaAlpha1();
	}

	/**
	 * Get transformed gamma alpha1
	 * @return transformed gamma alpha1
	 */
	private double getTransformedGammaAlpha1() {
		if (alpha < 1 ){
			// LK p. 463
			double p, y, u2, u1;
			u1 = rng.rnd();
			p = b1 * u1;
			u2 = rng.rnd();			
			if(p <= 1){
				y = Math.pow(p,(1/alpha));
				if(u2 <= Math.exp(-y)){
					return y;
				}else{
					return getTransformedGammaAlpha1();
				}
			}else{
				y = -Math.log((b1 - p) / alpha);
				if(u2 <= Math.pow(y,(alpha - 1))){
					return y;
				}else{
					return getTransformedGammaAlpha1();
				}
			}
		}else{
			// LK p. 464
			double u1, u2, v, y, z, w;

			u1 = rng.rnd();
			u2 = rng.rnd();
			v = a2 * Math.log(u1 / (1 - u1));
			y = alpha * Math.exp(v);
			z = u1 * u1 * u2;
			w = b2 + (q2 * v) - y;
			
			if((w + d2 - theta2 * z) >= 0 ){
				return y;
			}else{
				if(w >= Math.log(z)){
					return y;
				}else{
					return getTransformedGammaAlpha1();
				}
			}
		}
	}

	/**
	 * @see RandVar#getType()
	 */
	@Override
	public String getType() {
		return "Gamma";
	}

	/**
	 * @see RandVar#getVariance()
	 */
	@Override
	public double getVariance() {
		return alpha * beta * beta;
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
		alpha = m*m/s/s;
		beta  = m/alpha;
		init();
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
			"\t\talpha: " + alpha + "\n" +
			"\t\tbeta: " + beta;
	}	
}
