package lbvi;
import java.util.Random;
import org.apache.commons.math3.distribution.NormalDistribution;


public class test {

    public static double sampleInverseGaussian(double mu, double lamda, Random random) {
        //create a standard normal distribution
        NormalDistribution standardNormal = new NormalDistribution(mu, lamda);
        //Step 1: Generate a uniform number and convert it to a standard normal sample using inverseCumulativeProbability
        double u1 = random.nextDouble();
        double v = standardNormal.inverseCumulativeProbability(u1);

        //step 2: compute y = v^2
        double y = v * v;

        //Step 3: Compute candidate x using the Michael-Schucany-Haas formula.
        double muSquared =mu * mu;
        double sqrtTerm = Math.sqrt(4 * mu + muSquared * y * y);
        double x = mu + (muSquared * y) / (2 * lamda) - (mu / (2 * lamda)) * sqrtTerm;

        //Step 4: Generate another uniform random number for the acceptance step
        double u2 = random.nextDouble();

        //Accept or reject the candidate x.
        if (u2 <= mu / (mu +x)) {
            return x;
        } else {
            return muSquared / x;
        }
    }

    public static void main(String[] args) {
        //Initialize a random generator
        Random random = new Random();
        //Example parameters for the inverse Gaussian (Wald) distribution
        double mu = 2.66; //mean
        double lamda = 0.33; //shape

        //Generate and display a smaple
        double sample = sampleInverseGaussian(mu, lamda, random);
        System.out.println("Smapled from Imverse Gaussian (Wald): " + sample);
    }


}
