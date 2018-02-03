import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import static org.junit.Assert.*;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Before;
import org.junit.Test;


public class TestAttitudeCalculator {
    private AttitudeCalculator a;

    @Before
    public void setUp()
    {
        a = new AttitudeCalculator(Math.toRadians(90), Math.toRadians(0));
    }

    @Test
    public void testCreation() {
        SphericalCoordinates[] scs = a.calculateDirections(0, Math.PI/2.0, 0);
        SphericalCoordinates refPA = new SphericalCoordinates(Vector3D.PLUS_K);
        SphericalCoordinates refSD = new SphericalCoordinates(Vector3D.PLUS_I);
        assertEquals("Precession axis theta", scs[0].getTheta(), refPA.getTheta(), 1e-10);
        assertEquals("Precession axis phi", scs[0].getPhi(), refPA.getPhi(), 1e-10);
        assertEquals("Scan direction theta", scs[1].getTheta(), refSD.getTheta(), 1e-10);
        assertEquals("Scan directoon phi", scs[1].getPhi(), refSD.getPhi(), 1e-10);
    }

    @Test
    public void testCalculateFoVs() {
        SphericalCoordinates[] scs = a.calculateDirections(0, Math.PI/2.0, 0);
        Vector3D[][] fovs = a.calculateFoVs();

        Vector3D fov1center = fovs[0][0].add(fovs[0][1]).add(fovs[0][2]).add(fovs[0][3]).normalize();
        SphericalCoordinates fov1sc = new SphericalCoordinates(fov1center);
        assertEquals("FoV1 phi", fov1sc.getPhi(), Math.PI/2.0, 1e-10);
        assertEquals("FoV1 theta", fov1sc.getTheta(), a.getGamma()/2.0, 1e-10);

        Vector3D fov2center = fovs[1][0].add(fovs[1][1]).add(fovs[1][2]).add(fovs[1][3]).normalize();
        SphericalCoordinates fov2sc = new SphericalCoordinates(fov2center);
        assertEquals("FoV2 phi", fov2sc.getPhi(), Math.PI/2.0, 1e-10);
        assertEquals("FoV2 theta", fov2sc.getTheta(), -a.getGamma()/2.0, 1e-10);
    }
}