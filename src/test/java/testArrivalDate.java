import lbvi.Utils.ArrivalDate;
import lbvi.Utils.FilePath;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class testArrivalDate {
    @Test
    @DisplayName("Samples should be non-negative")
    void samplesAreNonNegative() {
        int lower = 79, mode = 110, upper = 130;

        boolean allNonNegative = IntStream.generate(() -> ArrivalDate.arrivalDate(lower, mode, upper))
                .limit(5_000)  // plenty for a quick sanity check
                .allMatch(x -> x >= 0);

        assertTrue(allNonNegative, "All arrival dates should be >= 0");
    }

    @Test
    @DisplayName("Empirical mean ≈ expected triangular mean")
    void sampleMeanMatchesExpectedWithinTolerance() {
        int lower = 79, mode = 110, upper = 130;

        // Theoretical E[lambda] for Triangular(a, c, b) is (a + c + b) / 3
        double expectedMean = (lower + mode + upper) / 3.0;

        IntSummaryStatistics stats = IntStream.generate(() -> ArrivalDate.arrivalDate(lower, mode, upper))
                .limit(50_000)  // large enough to make the mean stable
                .summaryStatistics();

        double empiricalMean = stats.getAverage();

        // Generous ±3 window to keep the test stable across runs/platforms
        assertTrue(empiricalMean > expectedMean - 3 && empiricalMean < expectedMean + 3,
                "Empirical mean " + empiricalMean + " not within tolerance of expected " + expectedMean);
    }

    @Test
    @DisplayName("Invalid (a, c, b) ordering throws")
    void invalidParametersThrow() {
        // mode < lower
        assertThrows(NumberIsTooSmallException.class,
                () -> ArrivalDate.arrivalDate(79, 50, 130));

        // mode > upper
        assertThrows(NumberIsTooLargeException.class,
                () -> ArrivalDate.arrivalDate(79, 140, 130));

        // lower >= upper
        assertThrows(NumberIsTooLargeException.class,
                () -> ArrivalDate.arrivalDate(79, 80, 79));
    }


    public static void main(String[] args) throws IOException {
        String outPath = FilePath.getFileName("arrivalDate_samples.csv", false);
        int lowerArrivalDate = 95; int modeArrivalDate = 113; int upperArrivalDate = 144;
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(outPath))) {
            w.write("run,sample_index,value\n");  // tidy header
            for (int i=0; i<10; i++) {
                for (int j = 0; j < 15000; j++) {
                    int sample = ArrivalDate.arrivalDate(lowerArrivalDate, modeArrivalDate, upperArrivalDate);
                    w.write(i + "," + j + "," + sample + "\n");
                }
            }
            System.out.println("Wrote CSV: arrivalDate_samples.csv");
        }
    }

}
