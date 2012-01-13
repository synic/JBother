package com.valhalla.misc;

public class SimpleXOR {
    public static String encrypt(String text, String key) {
        if (text == null)
            return "";
        String result = "";
        while (key.length() < text.length()) {
            key += key;
        }

        key = key.substring(0, text.length());

        byte[] t = text.getBytes();
        byte[] k = key.getBytes();

        for (int i = 0; i < t.length; i++) {
            int e = (int) (t[i] ^ k[i]);
            String hex = Integer.toHexString(e);
            result += " " + hex;
        }

        return result.substring(1);
    }

    public static String decrypt(String text, String key) {
        if (text == null)
            return "";
        String[] ar = text.split(" ");
        while (key.length() < ar.length) {
            key += key;
        }

        key = key.substring(0, ar.length);
        String result = "";

        byte[] t = new byte[ar.length];
        for (int i = 0; i < ar.length; i++) {
            try {
                t[i] = (byte) Integer.parseInt(ar[i], 16);
            } catch (NumberFormatException ex) {
                return "";
            }
        }

        byte[] k = key.getBytes();

        for (int i = 0; i < t.length; i++) {
            int e = (int) (t[i] ^ k[i]);

            result += (char) e;
        }

        return result;
    }
}