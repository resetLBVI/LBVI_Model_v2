package lbvi;

import lbvi.Groundwater.WaterStress;

public enum VegState {
    VERYHEALTHY(4), HEALTHY(3), STRESS(2), SEVERELYSTRESSED(1), DEAD(0);

    private final int code;
    VegState(int code) {
        this.code = code;
    }

    //Getter
    public int getCode() {
        return code;
    }

    // Reverse lookup: integer → enum
    public static VegState fromCode(int code) {
        for (VegState level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown veg state code: " + code);
    }
}
