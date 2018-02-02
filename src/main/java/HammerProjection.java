public class HammerProjection implements Projection {
    private double centerTheta;
    private int longitudeSign;
    private double scale;

    // centerLongitude is supplied in degrees
    public HammerProjection(double centerLongitude) {
        this(centerLongitude, false);
    }

    public HammerProjection(double centerLongitude, boolean invertLongitude) {
        this.centerTheta = Math.toRadians(centerLongitude);
        if(invertLongitude) {
            this.longitudeSign = -1;
        }
        else {
            this.longitudeSign = 1;
        }
        this.scale = Math.sqrt(2);

    }

    // ensure theta within -PI, PI
    private double reduceTheta(double theta) {
        while(theta > Math.PI) {
            theta -= 2*Math.PI;
        }
        while(theta < -Math.PI) {
            theta += 2*Math.PI;
        }
        return theta;
    }

    public double getCenterLongitude() {
        return Math.toDegrees(centerTheta);
    }

    // Project using longitude and latitude in degrees
    public double[] projectLongLat(double longitude, double latitude) {
        return projectThetaPhi(Math.toRadians(longitude), Math.toRadians(90.0 - latitude));
    }

    // Project using theta (angle around equator) and phi (angle from north pole) in radians
    public double[] projectThetaPhi(double theta, double phi) {
        theta = reduceTheta(theta - centerTheta)/2.0;
        phi = Math.PI/2.0 - phi;

        double cl = Math.cos(theta);
        double sl = Math.sin(theta);
        double cp = Math.cos(phi);
        double sp = Math.sin(phi);
        double denom = Math.sqrt(1 + cp * cl);

        return new double[] {2 * longitudeSign * scale * cp * sl / denom, scale * sp / denom};
    }

    // Inverse project to longitude and latitude in degrees
    public double[] inverseProjectLongLat(double x, double y) {
        double [] thetaPhi = inverseProjectThetaPhi(x, y);
        return new double[] {Math.toDegrees(thetaPhi[0]), Math.toDegrees(Math.PI/2.0 - thetaPhi[1])};
    }

    // Inverse project to theta and phi in radians
    public double[] inverseProjectThetaPhi(double x, double y) {
        double xx = x * x / (4.0 * scale * scale);
        double yy = y * y / (scale * scale);
        if(xx + yy > 1.0) {
            return new double[] {Float.NaN, Float.NaN};
        }
        double z = Math.sqrt(1 - 0.5 * xx - 0.5 * yy);
        double theta = 2 * Math.atan2(longitudeSign * z * x, 2 * (2 * z * z - 1));
        double phi = Math.asin(z * y);

        theta += centerTheta;
        phi = 0.5 * Math.PI - phi;

        return new double[] {theta, phi};
    }

    public double[] getXRange() {
        return new double[] {-2 * scale, 2 * scale};
    }

    public double[] getYRange() {
        return new double[] {-scale, scale};
    }
}
