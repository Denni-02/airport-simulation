package mbpmcsn.process.rvg;

import mbpmcsn.desbook.Rngs;

public final class ErlangGenerator implements RandomVariateGenerator {
	private final double k;
	private final double b;

	public ErlangGenerator(double k, double b) {
		this.k = k;
		this.b = b;
	}

	@Override
	public double generate(Rngs rngs) {
        double x = 0;

        for (long i = 0; i < k; i++) {
            x += Commons.exponential(b, rngs);
        }

        return x;
	}
}
