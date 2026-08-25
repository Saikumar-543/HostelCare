package service;

import enums.ComplaintCategory;
import enums.Priority;

/**
 * Suggests a starting priority for a complaint using simple, explainable
 * Java rules (no AI/ML - see spec section 6). Admin can always override.
 */
public class PriorityService {

    private PriorityService() { }

    public static Priority suggest(ComplaintCategory category, String description) {
        String text = description == null ? "" : description.toLowerCase();

        // Keyword-based escalation first - danger/urgent language always wins.
        if (containsAny(text, "spark", "shock", "fire", "smoke", "exposed wire", "short circuit")) {
            return Priority.CRITICAL;
        }
        if (category == ComplaintCategory.FOOD_HYGIENE &&
            containsAny(text, "insect", "worm", "foreign material", "food poisoning", "rotten", "spoiled")) {
            return Priority.CRITICAL;
        }

        switch (category) {
            case ELECTRICAL:
                return Priority.HIGH;
            case PLUMBING:
                return containsAny(text, "major", "flooding", "burst", "no water", "leak")
                        ? Priority.HIGH : Priority.MEDIUM;
            case WATER_SUPPLY:
                return Priority.HIGH;
            case BATHROOM:
                return Priority.HIGH;
            case FOOD_HYGIENE:
                return Priority.HIGH;
            case FOOD_QUALITY:
            case DINING_HALL:
                return Priority.MEDIUM;
            case WASHING_MACHINE:
            case WIFI:
                return Priority.MEDIUM;
            case SECURITY:
                return containsAny(text, "broken lock", "cctv", "unsafe") ? Priority.HIGH : Priority.MEDIUM;
            case CLEANING:
            case HOUSEKEEPING:
            case FURNITURE:
            case LAUNDRY:
            case ROOM:
            case COMMON_AREA:
            case OTHER:
            default:
                return Priority.LOW;
        }
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
