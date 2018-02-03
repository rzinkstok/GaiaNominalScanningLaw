import healpix.essentials.HealpixBase;
import healpix.essentials.Scheme;
import healpix.essentials.Vec3;
import healpix.essentials.Pointing;
import healpix.essentials.RangeSet;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public class HealPixWrapper {
    // Simple wrapper for HealPixBase
    // Works with Apache commons math Vector3D and SphericalCoordinates
    private HealpixBase h;
    private int nside;
    private long npix;
    private boolean debug;

    public HealPixWrapper(int nside, Scheme scheme_in) throws Exception {
        this(nside, scheme_in, false);
    }

    public HealPixWrapper(int nside, Scheme scheme_in, boolean debug) throws Exception {
        h = new HealpixBase(nside, scheme_in);
        this.nside = nside;
        this.npix = h.getNpix();
        this.debug = debug;
    }

    public long getNpix() {
        return npix;
    }

    public int getNside() {
        return nside;
    }

    public long vect2pix(Vector3D v) throws Exception {
        Vec3 vv = new Vec3(v.getX(), v.getY(), v.getZ());
        return h.vec2pix(vv);
    }

    public Vector3D pix2vect(long pix) throws Exception {
        Vec3 vv = h.pix2vec(pix);
        return new Vector3D(vv.x, vv.y, vv.z);
    }

    public long ang2pix(SphericalCoordinates sc) throws Exception {
        // SC: theta is polar angle, phi is angle from north pole
        // Pointing: phi is polar angle, theta is angle from north pole
        // Pointing(theta, phi) -> Pointing(sc.getPhi(), sc.getTheta())
        Pointing p = new Pointing(sc.getPhi(), sc.getTheta());
        return h.ang2pix(p);
    }

    public SphericalCoordinates pix2ang(long pix) throws Exception {
        // Pointing: phi is polar angle, theta is angle from north pole
        // SC: theta is polar angle, phi is angle from north pole
        // SphericalCoordinates(1, theta, phi) -> SphericalCoordinates(1.0, p.phi, p.theta)
        Pointing p = h.pix2ang(pix);
        return new SphericalCoordinates(1.0, p.phi, p.theta);
    }

    public RangeSet queryTriangle(Vector3D p1, Vector3D p2, Vector3D p3) throws Exception {
        Pointing[] tri = new Pointing[3];
        tri[0] = new Pointing(Math.PI/2.0 - p1.getDelta(), p1.getAlpha());
        tri[1] = new Pointing(Math.PI/2.0 - p2.getDelta(), p2.getAlpha());
        tri[2] = new Pointing(Math.PI/2.0 - p3.getDelta(), p3.getAlpha());

        if(debug) {
            System.out.println("");
            System.out.println("Pointing1: " + tri[0].phi + ", " + tri[0].theta);
            System.out.println("Pointing2: " + tri[1].phi + ", " + tri[1].theta);
            System.out.println("Pointing3: " + tri[2].phi + ", " + tri[2].theta);
        }
        RangeSet res = h.queryPolygonInclusive(tri, 64);

        if(debug) {
            System.out.println("Pixels: " + res.toString());
            System.out.println("Point1: " + h.ang2pix(tri[0]));
            System.out.println("Point2: " + h.ang2pix(tri[1]));
            System.out.println("Point3: " + h.ang2pix(tri[2]));
        }
        return res;
    }

    public RangeSet queryRectangle(Vector3D[] points) throws Exception {
        Pointing[] rect = new Pointing[4];
        for(int i=0; i<4; i++) {
            rect[i] = new Pointing(Math.PI / 2.0 - points[i].getDelta(), points[i].getAlpha());
        }

        if(debug) {
            System.out.println("");
            System.out.println("Pointing1: " + rect[0].phi + ", " + rect[0].theta);
            System.out.println("Pointing2: " + rect[1].phi + ", " + rect[1].theta);
            System.out.println("Pointing3: " + rect[2].phi + ", " + rect[2].theta);
            System.out.println("Pointing4: " + rect[3].phi + ", " + rect[3].theta);
        }
        RangeSet res = h.queryPolygonInclusive(rect, 8);
        if(debug) System.out.println("Pixels: " + res.toString());

        return res;
    }
}
