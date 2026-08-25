package enums;

/** Complaint priority levels, ordered from lowest to highest severity. */
public enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL;

    public java.awt.Color getColor() {
        switch (this) {
            case CRITICAL: return new java.awt.Color(211, 47, 47);   // red
            case HIGH:     return new java.awt.Color(245, 124, 0);   // orange
            case MEDIUM:   return new java.awt.Color(251, 192, 45);  // yellow
            case LOW:
            default:       return new java.awt.Color(56, 142, 60);   // green
        }
    }
}
