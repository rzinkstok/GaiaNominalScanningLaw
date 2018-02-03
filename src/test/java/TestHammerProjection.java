import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestHammerProjection {
    private HammerProjection p1, p2;

    @Before
    public void setUp()
    {
        p1 = new HammerProjection(0);
        p2 = new HammerProjection(0, true);
    }

    @Test
    public void testCreation() {
        assertEquals("Center longitude", p1.getCenterLongitude(), 0.0, 1e-10);
        assertArrayEquals("XRange", p1.getXRange(), new double[] {-2*Math.sqrt(2), 2* Math.sqrt(2)}, 1e-10);
        assertArrayEquals("YRange", p1.getYRange(), new double[] {-Math.sqrt(2), Math.sqrt(2)}, 1e-10);
    }

    @Test
    public void testProject() {
        double[] s = {0, 0};
        double[] res = p1.projectLongLat(s[0], s[1]);
        double[] ref = new double[] {0, 0};
        assertArrayEquals("Origin projection", res, ref, 1e-10);

        s = new double[] {0, 89.999999999};
        res = p1.projectLongLat(s[0], s[1]);
        ref = new double[] {0, p1.getYRange()[1]};
        assertArrayEquals("North pole projection", res, ref, 1e-10);

        s = new double[] {0, -89.999999999};
        res = p1.projectLongLat(s[0], s[1]);
        ref = new double[] {0, p1.getYRange()[0]};
        assertArrayEquals("South pole projection", res, ref, 1e-10);

        s = new double[] {-180, 0};
        res = p1.projectLongLat(s[0], s[1]);
        ref = new double[] {p1.getXRange()[0], 0};
        assertArrayEquals("Left side projection", res, ref, 1e-10);

        s = new double[] {180, 0};
        res = p1.projectLongLat(s[0], s[1]);
        ref = new double[] {p1.getXRange()[1], 0};
        assertArrayEquals("Right side projection", res, ref, 1e-10);

        s = new double[] {0, 60};
        res = p1.projectLongLat(s[0], s[1]);
        ref = new double[] {0, Math.sqrt(2)/p1.getYRange()[1]};
        assertArrayEquals("60 degrees north projection", res, ref, 1e-10);

        s = new double[] {540, 0};
        res = p1.projectLongLat(s[0], s[1]);
        ref = new double[] {p1.getXRange()[1], 0};
        assertArrayEquals("Longitude over range projection", res, ref, 1e-10);

        s = new double[] {-540, 0};
        res = p1.projectLongLat(s[0], s[1]);
        ref = new double[] {p1.getXRange()[0], 0};
        assertArrayEquals("Longitude under range projection", res, ref, 1e-10);
    }

    @Test
    public void testInverseProject() {
        double[] s = {0, 0};
        double[] d = p1.projectLongLat(s[0], s[1]);
        double[] r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Origin inverse projection", r, s, 1e-10);

        s = new double[] {0, 89.99};
        d = p1.projectLongLat(s[0], s[1]);
        r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("North pole inverse projection", r, s, 1e-10);

        s = new double[] {0, -89.99};
        d = p1.projectLongLat(s[0], s[1]);
        r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("South pole inverse projection", r, s, 1e-10);

        s = new double[] {-180, 0};
        d = p1.projectLongLat(s[0], s[1]);
        r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Left side inverse projection", r, s, 1e-10);

        s = new double[] {180, 0};
        d = p1.projectLongLat(s[0], s[1]);
        r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Right side inverse projection", r, s, 1e-10);

        s = new double[] {0, 60};
        d = p1.projectLongLat(s[0], s[1]);
        r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("60 degrees north inverse projection", r, s, 1e-10);

        s = new double[] {34.5, 74.1};
        d = p1.projectLongLat(s[0], s[1]);
        r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Some random inverse projection", r, s, 1e-10);

        s = new double[] {Float.NaN, Float.NaN};
        d = new double[] {p1.getXRange()[0], p1.getYRange()[0]};
        r = p1.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Out of range inverse projection", r, s, 1e-10);
    }

    @Test
    public void testInverseLongitude() {
        double[] s = new double[] {0, 89.999999999};
        double[] res = p2.projectLongLat(s[0], s[1]);
        double[] ref = new double[] {0, p2.getYRange()[1]};
        assertArrayEquals("North pole projection inverse longitude", res, ref, 1e-10);

        s = new double[] {0, -89.999999999};
        res = p2.projectLongLat(s[0], s[1]);
        ref = new double[] {0, p2.getYRange()[0]};
        assertArrayEquals("South pole projection inverse longitude", res, ref, 1e-10);

        s = new double[] {-180, 0};
        res = p2.projectLongLat(s[0], s[1]);
        ref = new double[] {-p2.getXRange()[0], 0};
        assertArrayEquals("Left side projection inverse longitude", res, ref, 1e-10);

        s = new double[] {180, 0};
        res = p2.projectLongLat(s[0], s[1]);
        ref = new double[] {-p2.getXRange()[1], 0};
        assertArrayEquals("Right side projection inverse longitude", res, ref, 1e-10);

        s = new double[] {34.5, 74.1};
        double[] d = p2.projectLongLat(s[0], s[1]);
        double[] r = p2.inverseProjectLongLat(d[0], d[1]);
        assertArrayEquals("Some random inverse projection inverse longitude", r, s, 1e-10);
    }
}

