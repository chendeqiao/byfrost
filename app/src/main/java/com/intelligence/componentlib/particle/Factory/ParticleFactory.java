package com.intelligence.componentlib.particle.Factory;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.intelligence.componentlib.particle.Particle.Particle;

public abstract class ParticleFactory {
    public abstract Particle[][] generateParticles(Bitmap bitmap, Rect bound);
}
