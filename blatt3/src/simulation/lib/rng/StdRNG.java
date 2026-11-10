package simulation.lib.rng;

import java.util.Random;

/**
 * Standard random number generator
 */
public class StdRNG extends RNG {
	Random rng;
	
	public StdRNG() {
		super();
	}
	/**
	 * Constructor with current milliseconds as seed
	 * @param currentTimeMillis
	 */
	public StdRNG(long currentTimeMillis) {
		super(currentTimeMillis);
	}

	/**
	 * @see RNG#setSeed(long)
	 */
	@Override
	public void setSeed(long seed) {
		if (rng == null) {
			rng = new Random(seed);
		} else {
			rng.setSeed(seed);
		}
	}

	/**
	 * @see RNG#rnd()
	 */
	@Override
	public double rnd() {
		return rng.nextDouble();
	}
}
