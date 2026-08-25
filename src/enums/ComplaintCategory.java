package enums;

/** All complaint categories supported by HostelCare. */
public enum ComplaintCategory {
    ROOM("Room"),
    ELECTRICAL("Electrical"),
    PLUMBING("Plumbing"),
    WATER_SUPPLY("Water Supply"),
    BATHROOM("Bathroom"),
    FOOD_QUALITY("Food Quality"),
    FOOD_HYGIENE("Food Hygiene"),
    DINING_HALL("Dining Hall"),
    LAUNDRY("Laundry"),
    WASHING_MACHINE("Washing Machine"),
    WIFI("Wi-Fi"),
    CLEANING("Cleaning"),
    HOUSEKEEPING("Housekeeping"),
    FURNITURE("Furniture"),
    SECURITY("Security"),
    COMMON_AREA("Common Area"),
    OTHER("Other");

    private final String displayName;

    ComplaintCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
