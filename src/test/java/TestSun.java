import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestSun {
    private Sun s;

    @Before
    public void setUp() {
        s = new Sun();
    }

    @Test
    public void testAverageBehavior() {
        double t;
        double[] res;
        double avgDistance = 0;
        double avgDerivative = 0;
        int nsteps = 1000;
        for(int i=0; i<nsteps; i++) {
            t = 365.25 * i/(nsteps-1);
            res = s.apparentLongitude(t);
            avgDistance += res[0];
            avgDerivative += res[2];
        }
        avgDerivative /= nsteps;
        avgDistance /= nsteps;

        assertEquals("Average distance Sun-Earth", avgDistance, 1.0, 1e-3);
        assertEquals("Average derivative solar motion", Math.toDegrees(avgDerivative), 360/365.25, 1e-4);
    }
}
