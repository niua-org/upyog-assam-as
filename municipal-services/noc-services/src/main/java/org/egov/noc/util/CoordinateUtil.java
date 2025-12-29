package org.egov.noc.util;

import org.egov.noc.web.model.Noc;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Arrays;
import java.util.List;

@Component
public class CoordinateUtil {

    // List of direction keys to process
    private static final List<String> DIRECTIONS = Arrays.asList("EAST", "WEST", "NORTH", "SOUTH", "CENTER");

    /**
     * Converts coordinates in additionalDetails from decimal to DMS format for AAI NOC.
     */
    @SuppressWarnings("unchecked")
    public void convertCoordinatesForAAI(Noc noc) {
        if (noc == null || !(noc.getAdditionalDetails() instanceof Map)) {
            return;
        }

        Map<String, Object> details = (Map<String, Object>) noc.getAdditionalDetails();

        for (String direction : DIRECTIONS) {
            Object dirObj = details.get(direction);
            if (dirObj instanceof Map) {
                processCoordinateMap((Map<String, Object>) dirObj);
            }
        }
    }

    /**
     * Processes the latitude and longitude within a specific direction map.
     */
    private void processCoordinateMap(Map<String, Object> coordMap) {
        updateCoordinate(coordMap, "latitude");
        updateCoordinate(coordMap, "longitude");
    }

    private void updateCoordinate(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return;

        String formatted = convertToDmsString(value);
        if (formatted != null) {
            map.put(key, formatted);
        }
    }

    /**
     * Attempts to parse the object as a double and convert to DMS.
     * If parsing fails or it's already a string, returns the trimmed string.
     */
    private String convertToDmsString(Object coord) {
        if (coord instanceof Number) {
            return formatToDms(((Number) coord).doubleValue());
        }

        String strCoord = String.valueOf(coord).trim();
        if (strCoord.isEmpty()) return null;

        try {
            double decimal = Double.parseDouble(strCoord);
            return formatToDms(decimal);
        } catch (NumberFormatException e) {
            return strCoord; // Return original if not a valid number
        }
    }

    /**
     * Converts decimal degrees to "DD MM SS.SS" format.
     * Handles rounding rollovers (e.g., 60.00 seconds -> +1 minute).
     */
    private String formatToDms(double decimalDegrees) {
        double absValue = Math.abs(decimalDegrees);

        int degrees = (int) absValue;
        double minutesDecimal = (absValue - degrees) * 60.0;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60.0;

        // Round seconds to 2 decimal places to check for rollover
        seconds = Math.round(seconds * 100.0) / 100.0;

        if (seconds >= 60.0) {
            seconds = 0;
            minutes++;
        }
        if (minutes >= 60) {
            minutes = 0;
            degrees++;
        }

        return String.format("%02d %02d %.2f", degrees, minutes, seconds);
    }
}