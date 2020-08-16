package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.BukkitMethods;

public class ExplosionParticle {
    private static final ExplosionParticle[] PARTICLES;

    private final Enum particle;

    static {
        PARTICLES = new ExplosionParticle[BukkitMethods.getParticles().length];
        for (int i = 0; i < PARTICLES.length; i++) {
            PARTICLES[i] = new ExplosionParticle(BukkitMethods.getParticles()[i]);
        }
    }
    private ExplosionParticle(Enum particle) {
        this.particle = particle;
    }

    @Override
    public String toString() {
        return particle.name();
    }

    public static ExplosionParticle getParticle(String particle) {
        for(ExplosionParticle p : PARTICLES) {
            if(p.toString().equals(particle.toUpperCase()))
                return p;
        }
        return null;
    }
    public static ExplosionParticle[] getParticles() {
        return PARTICLES;
    }
}
