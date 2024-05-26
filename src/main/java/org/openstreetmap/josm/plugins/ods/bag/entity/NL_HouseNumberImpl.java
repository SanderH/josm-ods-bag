package org.openstreetmap.josm.plugins.ods.bag.entity;

import java.util.Objects;

import org.openstreetmap.josm.plugins.ods.domains.buildings.HouseNumber;

public class NL_HouseNumberImpl implements NL_HouseNumber {
    private final String houseNumberPrefix;
    private final Integer houseNumber;
    private final Character houseLetter;
    private final String houseNumberExtra;
    private final String fullHouseNumber;
    
    /**
     * Create an instance from the separate components.
     * 
     * @param houseNumberPrefix
     * @param houseNumber
     * @param houseLetter
     * @param houseNumberExtra
     */
    public NL_HouseNumberImpl(String houseNumberPrefix, Integer houseNumber,
            Character houseLetter, String houseNumberExtra) {
        super();
        this.houseNumberPrefix = houseNumberPrefix;
        this.houseNumber = houseNumber;
        this.houseLetter = houseLetter;
        this.houseNumberExtra = houseNumberExtra;
        this.fullHouseNumber = buildFullHouseNumber();
    }

    public NL_HouseNumberImpl(String fullHouseNumber) {
        this.fullHouseNumber = fullHouseNumber;
        Components components = parseFullHouseNumber(fullHouseNumber);
        this.houseNumberPrefix = components.houseNumberPrefix;
        this.houseNumber = components.houseNumber;
        this.houseLetter = components.houseLetter;
        this.houseNumberExtra = components.houseNumberExtra;
    }
    @Override
    public Integer getMainHouseNumber() {
        return houseNumber;
    }

    @Override
    public String getFullHouseNumber() {
        return fullHouseNumber;
    }

    @Override
    public String getHouseNumberPrefix() {
        return houseNumberPrefix;
    }

    @Override
    public Integer getHouseNumber() {
        return houseNumber;
    }

    @Override
    public Character getHouseLetter() {
        return houseLetter;
    }

    @Override
    public String getHouseNumberExtra() {
        return houseNumberExtra;
    }
    
    /**
     * Build the FullHousenumber String from the separate components.
     * 
     * @return
     */
    private String buildFullHouseNumber() {
        StringBuilder sb = new StringBuilder(10);
        if (getHouseNumberPrefix() != null) {
            sb.append(getHouseNumberPrefix());
        }
        if (getHouseNumber() != null) {
            sb.append(getHouseNumber());
        }
        if (getHouseLetter() != null) {
            sb.append(getHouseLetter());
        }
        if (getHouseNumberExtra() != null) {
            sb.append("-").append(getHouseNumberExtra());
        }
        return sb.toString();
    }
    /**
     * Parse the FullHouseNumber String to deduce the separate components
     */
    private Components parseFullHouseNumber(String s) {
        Components components = new Components();
        int i = 0;
        int start = 0;
        while (i<s.length() && Character.isLetter(s.charAt(i))) {
            i++;
        }
        if (i > start) {
            components.houseNumberPrefix = s.substring(0, i);
        }
        start = i;
        while (i<s.length() && Character.isDigit(s.charAt(i))) {
            i++;
        }
        if (i > start) {
            components.houseNumber = Integer.valueOf(s.substring(start, i));
        }
        if (i >= s.length()) return components;
        if (Character.isAlphabetic(s.charAt(i))) {
            components.houseLetter = s.charAt(i);
            i++;
        }
        if (i >= s.length()) return components;
        if (s.charAt(i) == '-' || s.charAt(i) == ' ') {
            i++;
        }
        if (i < s.length()) {
            components.houseNumberExtra = s.substring(i);
        }
        return components;
    }

    @Override
    public int compareTo(HouseNumber o) {
        if (o instanceof NL_HouseNumber) {
            return compareTo((NL_HouseNumber)o);
        }
        return -1;
    }
    
    private int compareTo(NL_HouseNumber hn2) {
        
        int result = compare(this.houseNumberPrefix, hn2.getHouseNumberPrefix());
        if (result == 0) {
            result = compare(this.houseNumber, hn2.getHouseNumber());
        }
        if (result == 0) {
            result = compare(this.houseLetter, hn2.getHouseLetter());
        }
        if (result == 0) {
            result = compare(this.houseNumberExtra, hn2.getHouseNumberExtra());
        }
        return result;
    }
    
    /**
     * Helper method to null safe compare 2 Comparables.
     * null is considered equal to null
     * Any non-null is considered bigger than null
     * 
     * @param <T> The class that implements Comparable
     * @param o1 The first object to compare
     * @param o2 The second object to compare
     * @return 0 if both objects are equal. -1 if o1 is less than o2
     *      1 if o1 is greater than o2
     */
    private <T extends Comparable<T>> int compare(T o1, T o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        }
        return o2 == null ? 1 : o1.compareTo(o2);
    }

    public String toString() {
        return getFullHouseNumber();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(houseNumber, houseNumberPrefix, houseLetter == null ? null : Character.toUpperCase(houseLetter), houseNumberExtra);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NL_HouseNumber)) return false;
        NL_HouseNumber hnr = (NL_HouseNumber) obj;
        return Objects.equals(houseNumber, hnr.getHouseNumber())
                && Objects.equals(houseLetter == null ? null : Character.toUpperCase(houseLetter), hnr.getHouseLetter() == null ? null : Character.toUpperCase(hnr.getHouseLetter()))
                && Objects.equals(houseNumberExtra, hnr.getHouseNumberExtra())
                && Objects.equals(houseNumberPrefix, hnr.getHouseNumberPrefix());
    }

    private class Components {
        String houseNumberPrefix = null;
        Integer houseNumber = null;
        Character houseLetter = null;
        String houseNumberExtra = null;
    }
}
