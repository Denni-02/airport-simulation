package mbpmcsn.process.rvg;

import mbpmcsn.desbook.Rngs;

final class Commons {
	static final double exponential(double m, Rngs rngs) {
		return (-m * Math.log(1.0 - rngs.random()));
	}
}
