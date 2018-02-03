import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import healpix.essentials.RangeSet;
import healpix.essentials.Scheme;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


public class TestHealPixWrapper {
    private HealPixWrapper h;

    @Before
    public void setup() {
        try {
            h = new HealPixWrapper(1, Scheme.RING);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }
    }

    @Test
    public void testCreation() {
        assertEquals("NSide", h.getNside(), 1);
        assertEquals("NPix", h.getNpix(), 12);
    }

    @Test
    public void testVect2Pix() {
        for(int i=0; i<h.getNpix(); i++) {
            try {
                Vector3D v = h.pix2vect((long) i);
                assertEquals("Vect2Pix", h.vect2pix(v), (long)i);
            }
            catch(Exception e) {
                System.out.println("Error: "+e);
            }
        }
    }

    @Test
    public void testAng2Pix() {
        for(int i=0; i<h.getNpix(); i++) {
            try {
                SphericalCoordinates sc = h.pix2ang((long) i);
                assertEquals("Ang2Pix", h.ang2pix(sc), (long)i);
            }
            catch(Exception e) {
                System.out.println("Error: "+e);
            }
        }
    }

    @Test
    public void testQueryTriangle() {
        Vector3D p1 = new Vector3D(0, 0, 0);
        Vector3D p2 = new Vector3D(0, 0, 0);
        Vector3D p3 = new Vector3D(0, 0, 0);
        try {
            p1 = h.pix2vect(0);
            p2 = h.pix2vect(1);
            p3 = h.pix2vect(8);
        }
        catch(Exception e) {
            System.out.println("Error: "+e);
        }
        p1 = p1.add(new Vector3D(0, 0, 1)).normalize();
        RangeSet r = new RangeSet();
        try {
            r = h.queryTriangle(p1, p2, p3);
        }
        catch(Exception e) {
            System.out.println("Error: "+e);
        }
        assertEquals("Query triangle size", r.nval(), 5);
        assertTrue("Query triangle pix 1", r.contains(0));
        assertTrue("Query triangle pix 2", r.contains(1));
        assertTrue("Query triangle pix 3", r.contains(4));
        assertTrue("Query triangle pix 4", r.contains(5));
        assertTrue("Query triangle pix 5", r.contains(8));
    }

    @Test
    public void testQuertyRectangle() {
        Vector3D[] p = new Vector3D[4];
        try {
            p[0] = h.pix2vect(1);
            p[1] = h.pix2vect(2);
            p[2] = h.pix2vect(10);
            p[3] = h.pix2vect(9);
        }
        catch(Exception e) {
            System.out.println("Error: "+e);
        }
        RangeSet r = new RangeSet();
        try {
            r = h.queryRectangle(p);
        }
        catch(Exception e) {
            System.out.println("Error: "+e);
        }
        assertEquals("Query rectangle size", r.nval(), 7);
        assertTrue("Query rectangle pix 1", r.contains(1));
        assertTrue("Query rectangle pix 2", r.contains(2));
        assertTrue("Query rectangle pix 3", r.contains(5));
        assertTrue("Query rectangle pix 4", r.contains(6));
        assertTrue("Query rectangle pix 5", r.contains(7));
        assertTrue("Query rectangle pix 6", r.contains(9));
        assertTrue("Query rectangle pix 7", r.contains(10));
    }
}
