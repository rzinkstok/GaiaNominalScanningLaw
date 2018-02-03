import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import healpix.essentials.Scheme;
import healpix.essentials.RangeSet;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public class HealPixDensityMapper {
    private int width;
    private int height;
    private double deltax;
    private double deltay;

    private Projection projection;
    private HealPixWrapper healpix;
    private int[] counts;
    private String outputFolder;
    private ArrayList<Long> previousStepPixels;
    private ArrayList<Long> currentPixels;

    public HealPixDensityMapper(int width, int height, Projection projection, int nside, String outputFolder) throws Exception {
        this.width = width;
        this.height = height;
        this.projection = projection;
        this.healpix = new HealPixWrapper(nside, Scheme.RING);
        this.outputFolder = outputFolder;
        if(!this.outputFolder.endsWith("/")) {
            this.outputFolder += "/";
        }

        double[] xRange = projection.getXRange();
        double[] yRange = projection.getYRange();

        deltax = (xRange[1] - xRange[0]) / width;
        deltay = (yRange[1] - yRange[0]) / height;

        counts = new int[(int)healpix.getNpix()];
        for(int i=0; i<healpix.getNpix(); i++) {
            counts[i] = 0;
        }
        previousStepPixels = new ArrayList<>();
        currentPixels = new ArrayList<>();
    }

    public void nextStep() {
        previousStepPixels = currentPixels;
        currentPixels = new ArrayList<>();
    }

    public void addRectangularArea(Vector3D[] rect) {
        for(long p: getPixelsForRectangle(rect).toArray()) {
            if(!previousStepPixels.contains(p)) {
                counts[(int)p] += 1;
            }
            currentPixels.add(p);
        }
    }

    public void drawMap(int n) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double x, y, theta, phi, ip[];
        long ipix;
        int c, max=0, min = counts[0], sum=0;
        int white = (255<<16) | (255<<8) | 255;

        for(int i = 0; i<counts.length; i++) {
            sum += counts[i];
            if(counts[i] > max) {
                max = counts[i];
            }
            if(counts[i] < min) {
                min = counts[i];
            }
        }

        System.out.println("Min value: " + min);
        System.out.println("Max value: " + max);
        System.out.println("Mean value: " + sum/((double)healpix.getNpix()));
        ColorMap cm = ColorMap.getJet(256);

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                x = projection.getXRange()[0] + i * deltax;
                y = projection.getYRange()[0] + j * deltay;

                ip = this.projection.inverseProjectThetaPhi(x, y);
                theta = ip[0];
                phi = ip[1];

                if(!Double.isNaN(theta) && !Double.isNaN(phi)) {
                    //ipix = pixtools.ang2pix(phi, theta); // PixTools needs phi (angle from north pole) first, then theta (in-plane angle)
                    ipix = healpix.ang2pix(new SphericalCoordinates(1, theta, phi));
                    c = (int)(255 * ((double)counts[(int)ipix]/max));
                    int col = cm.getColor(c);
                    img.setRGB(i, height - j - 1, col);
                }
                else {
                    img.setRGB(i, height - j - 1, white);
                }
            }
        }

        // Write the image to file
        System.out.println("\nWriting file...");
        try
        {
            File f = new File(outputFolder + "frame" + String.format("%06d", n) + ".jpg");
            boolean res = ImageIO.write(img, "png", f);
            System.out.println("Written file: " + res);
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
        }
    }

    private RangeSet getPixelsForRectangle(Vector3D[] rect) {
        RangeSet ipixes = new RangeSet();
        try {
            ipixes = healpix.queryRectangle(rect);
        }
        catch(Exception e) {
            System.out.println("Error: " + e);
        }

        return ipixes;
    }


}
