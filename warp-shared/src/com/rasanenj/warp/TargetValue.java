package com.rasanenj.warp;

/**
 * @author Janne Rasanen
 */
public enum TargetValue {
    others, tertiary, secondary, primary ;

    public float getValueModifier() {
        return (this.ordinal() + 1);
    }

    public String toString() {
        switch (this) {
            case others:
                return "";
            case tertiary:
                return "3";
            case secondary:
                return "2";
            case primary:
                return "1";
            default:
                return "Unexpected target value";
        }
    }
}
