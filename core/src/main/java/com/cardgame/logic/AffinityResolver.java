package com.cardgame.logic;

import com.cardgame.data.AffinityType;

/**
 * Resolves damage multipliers based on the "Scissor" (affinity) system.
 */
public final class AffinityResolver {
    
    private AffinityResolver() {}

    /**
     * @return the damage multiplier (0.5f, 1.0f, or 1.5f)
     */
    public static float getMultiplier(AffinityType attacker, AffinityType defender) {
        if (attacker == null || defender == null) return 1.0f;
        
        switch (attacker) {
            case BLADE:
                if (defender == AffinityType.CHEMICAL) return 1.5f;
                if (defender == AffinityType.ELECTRIC) return 0.5f;
                break;
            case CHEMICAL:
                if (defender == AffinityType.BLUNT) return 1.5f;
                if (defender == AffinityType.MEDICAL) return 0.5f;
                break;
            case MEDICAL:
                if (defender == AffinityType.CHEMICAL) return 1.5f;
                // MEDICAL vs BLUNT is defined by BLUNT vs MEDICAL = 1.5, so medical has no strong attack vs blunt?
                // The table says:
                // BLUNT vs MEDICAL -> 1.5 (medical takes more)
                break;
            case BLUNT:
                if (defender == AffinityType.MEDICAL) return 1.5f;
                if (defender == AffinityType.CHEMICAL) return 0.5f;
                break;
            case ELECTRIC:
                if (defender == AffinityType.BLADE) return 1.5f;
                break;
            case NEUTRAL:
            default:
                return 1.0f;
        }
        
        return 1.0f;
    }
}
