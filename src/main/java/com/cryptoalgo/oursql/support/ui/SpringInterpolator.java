package com.cryptoalgo.oursql.support.ui;

import javafx.animation.Interpolator;

import java.util.ArrayList;

public class SpringInterpolator extends Interpolator {
    private final ArrayList<Double> values = new ArrayList<>();

    public SpringInterpolator(double stiffness, double mass, double damping) {
        this(stiffness, mass, damping, 0.05);
    }

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
            System.out.println(x);
        }
        System.out.println(values);
    }

    @Override
    protected double curve(double t) {
        return values.get((int) Math.floor(t*(values.size()-1)));
    }
}
