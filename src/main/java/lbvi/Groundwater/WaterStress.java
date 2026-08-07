package lbvi.Groundwater;

public enum WaterStress {
    ABOVENORMAL(1, "Above Normal"),
    NORMAL(2,       "Normal"),
    BELOWNORMAL(3,  "Below Normal"),
    SEVEREDROUGHT(4,"Severe Drought");

    private final int    code;
    private final String label;

    WaterStress(int code, String label) {
        this.code  = code;
        this.label = label;
    }

    public int    getCode()  { return code; }
    public String getLabel() { return label; }

    // Reverse lookup by integer code (e.g. 1–4)
    public static WaterStress fromCode(int code) {
        for (WaterStress ws : values()) {
            if (ws.code == code) return ws;
        }
        throw new IllegalArgumentException("Unknown WaterStress code: " + code);
    }

    // Reverse lookup by CSV label (case-insensitive)
    public static WaterStress fromLabel(String label) {
        String normalized = label.trim();
        for (WaterStress ws : values()) {
            if (ws.label.equalsIgnoreCase(normalized)) return ws;
        }
        throw new IllegalArgumentException("Unknown WaterStress label: " + label);
    }
}
