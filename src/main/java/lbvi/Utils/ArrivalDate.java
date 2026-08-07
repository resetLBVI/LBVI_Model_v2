package lbvi.Utils;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.TriangularDistribution;


/**
 * This class provide two helper methods that helps to determine the arrival date each breeding season for each LBVI
 * agent.
 * (1) triangularRandom(): The method samples a random number from a triangular distribution defined by a minimum
 * value a, mode c, and maximum b. The formula used is
 * if U < c-a/ b-a, then x = a +sqrt(U(b-a)(c-a))
 * else, x = b - sqrt(1-U)(b-a)(b-c))
 * (2) Then the random number x will be used to as the parameter lambda in a Poisson distribution. The Knuth's algorithm
 * can help to generate a random date that follow this poisson distribution for the agent
 * Below is the probability density function (PDF) of the triangular distribution
 *
 * And below is the cumulative distribution function (CDF)
 */

public class ArrivalDate {

    public static int arrivalDate(int lower, int mode, int upper) {
        // Create an instance of TriangularDistribution
        TriangularDistribution triangularDistribution = new TriangularDistribution(lower, mode, upper);
        // Generate a random sample from this triangular distribution
        double lambda = triangularDistribution.sample();
//        System.out.println("Random sample from the triangular distribution: " + lambda);
        // Use the sampled lambda as the mean for a Poisson distribution
        PoissonDistribution poissonDistribution = new PoissonDistribution(lambda);
        int arrivalDate = (int) (poissonDistribution.sample());
//        System.out.println("Sampled value from PoissonDistribution with lambda = " + lambda + ": " + arrivalDate);
        return arrivalDate;
    }

}
