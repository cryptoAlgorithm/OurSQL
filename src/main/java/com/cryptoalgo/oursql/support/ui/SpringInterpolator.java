package com.cryptoalgo.oursql.support.ui;

import javafx.animation.Interpolator;

import java.util.ArrayList;

/**
 * An interpolate that simulates interpolation with spring physics.
 */
public class SpringInterpolator extends Interpolator {
    private final ArrayList<Double> values = new ArrayList<>();

    public SpringInterpolator(double stiffness, double mass, double damping) {
        this(stiffness, mass, damping, 0.05);
    }

    /**
     * Create a spring interpolator with custom values
     * @param stiffness Stiffness of spring in simulation
     * @param mass Mass of object to simulate
     * @param damping Dampening to use for simulation
     * @param precision Stop simulation when (x-target) and v are both less than this value
     */
    public SpringInterpolator(
        double stiffness,
        double mass,
        double damping,
        double precision
    ) {
        // Precompute all values first
        // Spring Length, set to 0 for simplicity
        final var springLength = 1;

        // Object position and velocity.
        var x = 0.0;
        var v = 0.0;

        // Spring stiffness, in kg / s^2
        final var k = -stiffness;

        // Damping constant, in kg / s
        final var d = -damping;

        final var rate = 1.0/100;

        // Add initial x value
        values.add(x);
        while (Math.abs(x-springLength) > precision || Math.abs(v) > precision) {
            var Fspring = k * (x - springLength);
            var Fdamping = d * v;

            var a = (Fspring + Fdamping) / mass;
            v += a * rate;
            x += v * rate;

            values.add(x);
        }
    }

    @Override
    protected double curve(double t) {
        return values.get((int) Math.floor(t*(values.size()-1)));
    }
}
