# GaiaNominalScanningLaw

This project calculates the sky coverage for ESA's Gaia satellite. This satellite orbits the L<sub>2</sub> Lagrange point of Earth. 
It scans the sky using a clever combination of three rotations:
- a rotation Ω (spin) around an axis perpendicular to the lines of sight of its two telescopes, with a period of 6 hours;
- a precession ν of this rotation axis around the line between the spacecraft and the Sun, with a period of 63 days;
- the yearly motion of the Sun as seen from Earth (ecliptic latitude β<sub>s</sub> = 0, ecliptic latitude λ<sub>s</sub>).

The combination of these rotations yields a full coverage of the sky: this is called the GAIA nominal scanning law.

## Calculation
Lennart Lindegren's technical note
[Calculating the GAIA Nominal Scanning law](http://www.astro.lu.se/~lennart/Astrometry/TN/Gaia-LL-035-20010219-Calculating-the-GAIA-Nominal-Scanning-Law.pdf)
describes a method to crunch the numbers. It involves the integration of two coupled differential equations that relate
the spin phase Ω(t) and the precession phase ν(t) with their derivatives. The nominal longitude of the Sun λ<sub>s</sub>(t)
is considered a given, and can be approximated using the first terms of a series expansion of Kepler's equation.

So given the apparent solar longitude λ<sub>s</sub>, the angle between the direction to the Sun and the precession axis ξ,
the precession rate S, the spin rate ω, and the initial values Ω<sub>0</sub> and ν<sub>0</sub>, the scanning law is completely
determined.

The values of Ω(t) and ν(t) can be used to compute the directions of the line of sight of the instrument's two telescopes; 
this is done quite easily using quaternions.

The resulting directions are binned into the pixels of a HealPIX map  (see the [HealPIX site](http://healpix.sourceforge.net))
with Nside=512, which translates to 3.14 million pixels.


## Results
When the data is projected using a Hammer projection, and plotted in 4000 by 2000 pixels, this is
the resulting density map, shown in ICRS coordinates:

![GAIA Nominal Scanning Law: sky coverage of Gaia](gaia_nsl_density.png?raw=true "The GAIA Nominal Scanning Law: sky coverage of Gaia")

Figure 1: The GAIA Nominal Scanning Law: sky coverage of Gaia.

The visualization used leans heavily on that used by Berry Holl, in his [video of Gaia's sky coverage](https://youtu.be/lRhe2grA9wE).

## Limitations

The orbital dynamics of the satellite around the L2 point is neglected. Furthermore, the solar longitude data is only an
approximation.