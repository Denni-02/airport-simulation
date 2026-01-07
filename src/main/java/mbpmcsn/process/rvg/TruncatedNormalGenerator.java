package mbpmcsn.process.rvg;

import mbpmcsn.desbook.Rngs;
import mbpmcsn.desbook.Rvms;

public final class TruncatedNormalGenerator implements RandomVariateGenerator {
	private final double mean;
	private final double devstd;
	private final double lowerBound;
	private final double upperBound;

	public TruncatedNormalGenerator(double mean, double devstd, double lowerBound, double upperBound) {
		this.mean = mean;
		this.devstd = devstd;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public double generate(Rngs rngs) {
		Rvms rvms = new Rvms();

		double a = rvms.cdfNormal(mean, devstd, lowerBound - 1);
		double b = 1.0 - rvms.cdfNormal(mean, devstd, upperBound);
		double u = rvms.idfUniform(a,1.0-b, rngs.random());

		return rvms.idfNormal(mean, devstd, u);
	}
}
