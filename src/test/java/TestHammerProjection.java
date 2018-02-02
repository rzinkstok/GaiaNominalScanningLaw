import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestHammerProjection {
    private HammerProjection p;

    @Before
    public void setUp()
    {
        p = new HammerProjection(0);
    }

    @Test
    public void testProject() {
        double[] s = {0, 0};
        double[] res = p.projectLongLat(s[0], s[1]);
        double[] ref = new double[] {0, 0};
        assertArrayEquals("Origin projection", res, ref, 1e-10);

        s = new double[] {0, 89.999999999};
        res = p.projectLongLat(s[0], s[1]);
        ref = new double[] {0, p.getYRange()[1]};
        assertArrayEquals("North pole projection", res, ref, 1e-10);

        s = new double[] {0, -89.999999999};
        res = p.projectLongLat(s[0], s[1]);
        ref = new double[] {0, p.getYRange()[0]};
        assertArrayEquals("South pole projection", res, ref, 1e-10);

        s = new double[] {-180, 0};
        res = p.projectLongLat(s[0], s[1]);
        ref = new double[] {p.getXRange()[0], 0};
        assertArrayEquals("Left side projection", res, ref, 1e-10);

        s = new double[] {180, 0};
        res = p.projectLongLat(s[0], s[1]);
        ref = new double[] {p.getXRange()[1], 0};
        assertArrayEquals("Right side projection", res, ref, 1e-10);

        s = new double[] {0, 60};
        res = p.projectLongLat(s[0], s[1]);
        ref = new double[] {0, Math.sqrt(2)/p.getYRange()[1]};
        assertArrayEquals("60 degrees north projection", res, ref, 1e-10);
    }

    @Test
    public void testInverseProject() {
        double[] s = {0, 0};
        double[] d = p.projectLongLat(s[0], s[1]);
        double[] r = p.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Origin inverse projection", r, s, 1e-10);

        s = new double[] {0, 89.99};
        d = p.projectLongLat(s[0], s[1]);
        r = p.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("North pole inverse projection", r, s, 1e-10);

        s = new double[] {0, -89.99};
        d = p.projectLongLat(s[0], s[1]);
        r = p.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("South pole inverse projection", r, s, 1e-10);

        s = new double[] {-180, 0};
        d = p.projectLongLat(s[0], s[1]);
        r = p.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Left side inverse projection", r, s, 1e-10);

        s = new double[] {180, 0};
        d = p.projectLongLat(s[0], s[1]);
        r = p.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Right side inverse projection", r, s, 1e-10);

        s = new double[] {0, 60};
        d = p.projectLongLat(s[0], s[1]);
        r = p.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("60 degrees north inverse projection", r, s, 1e-10);

        s = new double[] {34.5, 74.1};
        d = p.projectLongLat(s[0], s[1]);
        r = p.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Some random inverse projection", r, s, 1e-10);
    }
}
