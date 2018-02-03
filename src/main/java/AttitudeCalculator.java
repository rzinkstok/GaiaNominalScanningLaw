import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public class AttitudeCalculator {
    private double epsilon;
    private double solarAspectAngle;
    private double gamma;
    private double fovAlong;
    private double fovAcross;
    private Vector3D currentPrecessionVector;
    private Vector3D currentScanDirectionVector;

    public AttitudeCalculator(double solarAspectAngle, double epsilon) {
        // Gaia attitude calculation
        // Based on the description in Lindegren's memo SAG-LL-30
        // solarAspectAngle is the angle between satellite spin axis and direction to the Sun in rad
        // epsilon is the angle between ecliptic and equator in rad

        this.epsilon = epsilon;
        this.solarAspectAngle = solarAspectAngle;
        this.gamma = Math.toRadians(106.5);   // Angle between telescope arms
        this.fovAlong = Math.toRadians(1.7);  // Size of the FoV of one telescope in scan direction
        this.fovAcross = Math.toRadians(0.6); // Size of the FoV of one telescope normal to scan direction
    }

    public AttitudeCalculator(double solarAspectAngle) {
        // Use a default value for the angle of the ecliptic
        this(solarAspectAngle, Math.toRadians(23.0 + 26.0/60 + 21.448/(60*60)));
    }

    public double getGamma() {
        return gamma;
    }

    private Quaternion quaternionFromAngleAndVector(double angle, Vector3D vector) {
        // Invert the angle to counteract the strange rotation convention of Apache commons math
        return new Quaternion(Math.cos(-angle/2.0), vector.scalarMultiply(Math.sin(-angle/2.0)).toArray());
    }

    public SphericalCoordinates[] calculateDirections(double solarLongitude, double nu, double omega) {
        // Rotate to the ecliptic plane
        Quaternion q0 = quaternionFromAngleAndVector(epsilon, Vector3D.PLUS_I);
        // Rotate line of sight towards current Sun
        Quaternion q1 = quaternionFromAngleAndVector(solarLongitude, Vector3D.PLUS_K);
        // Rotate precession axis around the Sun to its current position
        Quaternion q2 = quaternionFromAngleAndVector(nu - Math.PI/2.0, Vector3D.PLUS_I);
        // Rotate precession axis to the correct angle away from the Sun
        Quaternion q3 = quaternionFromAngleAndVector(Math.PI/2.0 - solarAspectAngle, Vector3D.PLUS_J);
        // Rotate the line of sight around the spin axis to its current position
        Quaternion q4 = quaternionFromAngleAndVector(omega, Vector3D.PLUS_K);

        // Calculate combined rotation
        Quaternion q01 = q1.multiply(q0);
        Quaternion q12 = q2.multiply(q01);
        Quaternion q123 = q3.multiply(q12);
        Quaternion q1234 = q4.multiply(q123);

        // Apply to initial vector (x axis)
        Rotation r = new Rotation(q1234.getQ0(), q1234.getQ1(), q1234.getQ2(), q1234.getQ3(), true);
        currentPrecessionVector = r.applyTo(Vector3D.PLUS_K);    // spin axis is 3rd axis of instrument frame
        currentScanDirectionVector = r.applyTo(Vector3D.PLUS_I); // central direction for the two lines of sight is the 1st axis of the instrument frame

        // Convert to spherical coordinates
        SphericalCoordinates sc1 = new SphericalCoordinates(currentPrecessionVector);
        SphericalCoordinates sc2 = new SphericalCoordinates(currentScanDirectionVector);

        return new SphericalCoordinates[] {sc1, sc2};
    }

    public Vector3D[][] calculateFoVs() {
        // Calculates the fields of view of both telescopes
        Vector3D[][] fovs = new Vector3D[2][4];

        Rotation r1 = new Rotation(currentPrecessionVector, gamma/2.0, RotationConvention.VECTOR_OPERATOR);
        Rotation r2 = new Rotation(currentPrecessionVector, -gamma/2.0, RotationConvention.VECTOR_OPERATOR);
        Vector3D leading = r1.applyTo(currentScanDirectionVector);
        Vector3D following = r2.applyTo(currentScanDirectionVector);

        fovs[0] = createFieldOfView(currentPrecessionVector, leading);
        fovs[1] = createFieldOfView(currentPrecessionVector, following);

        return fovs;
    }

    private Vector3D[] createFieldOfView(Vector3D precessionAxisVector, Vector3D scanDirectionVector) {
        // Build FoV for scanDirection
        Rotation r1 = new Rotation(precessionAxisVector, fovAlong/2.0, RotationConvention.VECTOR_OPERATOR);
        Vector3D tempv1 = r1.applyTo(scanDirectionVector);
        Vector3D tempAxis1 = tempv1.crossProduct(precessionAxisVector);
        Rotation r11 = new Rotation(tempAxis1, fovAcross/2.0, RotationConvention.VECTOR_OPERATOR);
        Rotation r12 = new Rotation(tempAxis1, -fovAcross/2.0, RotationConvention.VECTOR_OPERATOR);
        Vector3D p11 = r11.applyTo(tempv1);
        Vector3D p12 = r12.applyTo(tempv1);

        Rotation r2 = new Rotation(precessionAxisVector, -fovAlong/2.0, RotationConvention.VECTOR_OPERATOR);
        Vector3D tempv2 = r2.applyTo(scanDirectionVector);
        Vector3D tempAxis2 = tempv2.crossProduct(precessionAxisVector);
        Rotation r21 = new Rotation(tempAxis2, -fovAcross/2.0, RotationConvention.VECTOR_OPERATOR);
        Rotation r22 = new Rotation(tempAxis2, fovAcross/2.0, RotationConvention.VECTOR_OPERATOR);
        Vector3D p21 = r21.applyTo(tempv2);
        Vector3D p22 = r22.applyTo(tempv2);

        return new Vector3D[] {p11, p12, p21, p22};
    }
}
