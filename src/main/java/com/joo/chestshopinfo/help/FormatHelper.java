package com.joo.chestshopinfo.help;

public class FormatHelper {

    public static String formatBuyPrice(double price) {
        String buyPrice;
        if (price == 1) { // Wenn das Item nur 1 Eskone kostet
            buyPrice = "eine Eskone";
        } else if (price == 0) {
            buyPrice = "umsonst";
        } else { // ansonsten "Zahl + Eskonen"
            buyPrice = String.valueOf(price) + " Eskonen";
        }
        return buyPrice;
    }
}
