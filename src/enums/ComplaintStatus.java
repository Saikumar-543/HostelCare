package enums;

/**
 * Complaint lifecycle status.
 * Workflow: SUBMITTED -> PENDING -> ASSIGNED -> IN_PROGRESS -> RESOLVED -> CLOSED
 */
public enum ComplaintStatus {
    SUBMITTED, PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED;

    /**
     * Returns true if moving from this status to {@code next} is a valid,
     * forward-only transition in the HostelCare workflow.
     */
    public boolean canTransitionTo(ComplaintStatus next) {
        if (next == null) return false;
        switch (this) {
            case SUBMITTED:   return next == PENDING;
            case PENDING:     return next == ASSIGNED;
            case ASSIGNED:    return next == IN_PROGRESS;
            case IN_PROGRESS: return next == RESOLVED;
            case RESOLVED:    return next == CLOSED;
            case CLOSED:      return false;
            default:          return false;
        }
    }

    public java.awt.Color getColor() {
        switch (this) {
            case SUBMITTED:   return new java.awt.Color(96, 125, 139);
            case PENDING:     return new java.awt.Color(251, 140, 0);
            case ASSIGNED:    return new java.awt.Color(30, 136, 229);
            case IN_PROGRESS: return new java.awt.Color(126, 87, 194);
            case RESOLVED:    return new java.awt.Color(67, 160, 71);
            case CLOSED:      return new java.awt.Color(97, 97, 97);
            default:          return java.awt.Color.GRAY;
        }
    }
}
