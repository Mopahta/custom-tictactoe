package com.company;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;


public class Main {
    private static int cpuMove;
    private static byte[] secretKey;
    private static int userMove;

    public static void makeMove(String[] choices){
        SecureRandom random = new SecureRandom();
        secretKey = new byte[32];
        random.nextBytes(secretKey);
        Random choiceRandom = new Random();
        cpuMove = choiceRandom.nextInt(choices.length);
        try {
            byte[] hmacSha256 = HMAC.calcHmacSha256(secretKey, choices[cpuMove].getBytes("UTF-8"));
            String hmac = new String(hmacSha256);
            System.out.print("HMAC: ");
            for (byte b: hmacSha256){
                System.out.printf("%X", b);
            }
            System.out.println();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static void checkArgs(String [] args){
        if (args.length == 0){
            System.out.println("No arguments.");
        }
        else if (args.length % 2 == 0){
            System.out.println("The number of choices is even. Provide odd amount of choices.");
            System.exit(0);
        }
        else if (args.length < 3){
            System.out.println("Not enough arguments.");
        }
        for (int i = 0; i < args.length; i++){
            for (int j = i+1; j < args.length; j++){
                if (args[i].equals(args[j])){
                    System.out.printf("Same arguments: %s, %d and %d", args[i], i+1, j+1);
                    System.exit(0);
                }
            }
        }
    }

    public static void main(String[] args) {
        checkArgs(args);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        makeMove(args);

        WinCondition winCondition = new WinCondition(args);
        boolean correctInput = false;
        while(!correctInput) {
            System.out.println("Available options:");
            int i = 0;
            for (; i < args.length; i++) {
                System.out.printf("%d. %s\n", i + 1, args[i]);
            }
            System.out.printf("%d. Choices Table\n", i + 1);
            System.out.println("0. Exit");
            System.out.println("Enter your option: ");
            try {
                int input = Integer.parseInt(reader.readLine());

                if (input >= 0 && input <= i + 1){
                    if (input == 0){
                        System.exit(0);
                    }
                    else if (input == i + 1){
                        winCondition.printWinCondition();
                    }
                    else {
                        userMove = input - 1;
                        correctInput = true;
                    }
                }
                else {
                    System.out.println("Number not in range of available options.");
                }

            }
            catch (NumberFormatException | IOException e){
                System.out.println("Incorrect input.");
            }
        }
        System.out.printf("Your move: %s\n", args[userMove]);
        System.out.printf("Computer move: %s\n", args[cpuMove]);

        if (userMove == cpuMove){
            System.out.println("Tie!");
        }
        else if (winCondition.getWinState(args[userMove], args[cpuMove])){
            System.out.println("You win!");
        }
        else{
            System.out.println("Computer wins!");
        }
        System.out.print("HMAC key: ");
        for (int i = secretKey.length-1; i >= 0; i--){
            System.out.printf("%X", secretKey[i]);
        }
    }

}

class winTableRow {
    String choice;
    boolean isWinnable;
}

class WinCondition {
    public static Hashtable<String, winTableRow[]> winTable = new Hashtable<String, winTableRow[]>();
    public WinCondition(String[] choices){
        for (int i = 0; i < choices.length; i++){
            winTableRow[] tableRow = new winTableRow[choices.length];
            int k = 0;
            for (int j = 0; j < choices.length; j++){
                winTableRow tblRow = new winTableRow();
                tblRow.choice = choices[j];
                tblRow.isWinnable = (j-i <= choices.length/2) && (j > i) || (i > j) && (i-j > choices.length/2);
                tableRow[j] = tblRow;
            }
            winTable.put(choices[i], tableRow);
        }
    }
    private String[] createHeaders(winTableRow[] row){
        String[] headers = new String[row.length+1];
        headers[0] = "";
        for (int i = 0; i < row.length; i++){
            headers[i+1] = row[i].choice;
        }
        return headers;
    }
    private void printHeaders(String[] headers){
        for (String s: headers){
            System.out.format("%12s", s);
        }
        System.out.println();
    }
    public void printWinCondition(){
        winTableRow[] firstRow = winTable.elements().nextElement();

        String[] headers = createHeaders(firstRow);
        printHeaders(headers);

        for (int i = 1; i < headers.length; i++){
            firstRow = winTable.get(headers[i]);
            System.out.printf("%12s", firstRow[i-1].choice);
            for (int j = 0; j < firstRow.length; j++){
                if (i-1 != j) {
                    if (firstRow[j].isWinnable){
                        System.out.printf("%12s", headers[i]);
                    }
                    else{
                        System.out.printf("%12s", firstRow[j].choice);
                    }
                }
                else{
                    System.out.printf("%12s", "-");
                }
            }
            System.out.println();
        }
    }
    public boolean getWinState(String userMove, String cpuMove){
        winTableRow[] row = winTable.get(userMove);
        for (int i = 0; i <= row.length; i++){
            if (cpuMove.equals(row[i].choice)){
                return row[i].isWinnable;
            }
        }
        return false;
    }

}

class HMAC{
    static public byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256 = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return hmacSha256;
    }
}
