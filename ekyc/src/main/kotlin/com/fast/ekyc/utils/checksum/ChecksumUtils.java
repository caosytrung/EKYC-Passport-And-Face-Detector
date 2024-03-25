package com.fast.ekyc.utils.checksum;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class ChecksumUtils {
    public static final List<String> ListHasecHashIndex = new
            ArrayList<>(Arrays.asList("SHA-512", "CRC32", "SHA-256", "MD5", "SHA1"));
    private String wrapper_length;// wrapper length
    private int firstHashIndex; //first hasecHashIndex index
    private int secHashIndex; // second hasecHashIndex index
    private String hour; //hour
    private String minute; //minute
    private String second; // second
    private int uuidStartIndex; // uuid substring start index
    private int uuidEndIndex; // uuid substring end index
    private String yy;
    private String mm;
    private String dd;
    private String value;
//    private final String uuid = "";


    protected String getRandomString(int maxLen) {
        String SALTCHARS = "abcdefghigklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < maxLen) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public String getTimeField(String pattern) {
        Date date = new Date();
        SimpleDateFormat hsdf = new SimpleDateFormat(pattern);
        return hsdf.format(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String generateChecksum(String uuid) {
//
        ChecksumUtils checksumUtils = new ChecksumUtils();
        checksumUtils.hour = getTimeField("HH");
        checksumUtils.minute = getTimeField("mm");
        checksumUtils.second = getTimeField("ss");
        List<Integer> randomList = new ArrayList<>();
        for (int i = 0; i < ListHasecHashIndex.size(); i++) {
            randomList.add(i);
        }
//nextInt(min, max +1)
//firstHashIndex != secHashIndex
        int randomNum = ThreadLocalRandom.current().nextInt(0,
                randomList.size());
        checksumUtils.firstHashIndex = randomList.get(randomNum);
        randomList.remove(randomNum);
        randomNum = ThreadLocalRandom.current().nextInt(0, randomList.size());
        checksumUtils.secHashIndex = randomList.get(randomNum);
//from 0 to 9
        String WL_first = String.valueOf(ThreadLocalRandom.current().nextInt(0,
                10));
//from 5 to 9
        String WL_sec = String.valueOf(ThreadLocalRandom.current().nextInt(5,
                10));
        checksumUtils.wrapper_length = WL_first + WL_sec;
//start & end index have 2 digits
//end - start >= 8
        int muuidStartIndex = ThreadLocalRandom.current().nextInt(0,
                uuid.length() - 8);
        String muuidStartIndexString = String.valueOf(muuidStartIndex);
        if (muuidStartIndex < 10) muuidStartIndexString = "0" + muuidStartIndexString;
        int muuidEndIndex = ThreadLocalRandom.current().nextInt(muuidStartIndex
                + 8, uuid.length());
        String muuidEndIndexString = String.valueOf(muuidEndIndex);
        if (muuidEndIndex < 10) muuidEndIndexString = "0" + muuidEndIndexString;
        checksumUtils.uuidStartIndex = muuidStartIndex;
        checksumUtils.uuidEndIndex = muuidEndIndex;
        checksumUtils.value = generateValue(checksumUtils, uuid);
        int wrapperLen = checksumUtils.wrapper_length.charAt(1) - '0';
        String finalChecksum = checksumUtils.wrapper_length +
                getRandomString(wrapperLen) + checksumUtils.firstHashIndex +
                checksumUtils.secHashIndex +
                getRandomString(wrapperLen) + checksumUtils.second +
                getRandomString(wrapperLen) +
                checksumUtils.hour + getRandomString(wrapperLen) +
                muuidStartIndexString + muuidEndIndexString + getRandomString(wrapperLen) +
                checksumUtils.value +
                getRandomString(wrapperLen) + checksumUtils.minute +
                getRandomString(wrapperLen);
        return finalChecksum;
    }

    //Iterate through checksum string
    public void extractFieldChecksum(String checkSum) {
//pattern [wrapper_length][WL01][firstHasecHashIndexIndex][secHashIndex][WL02][ii][WL03][hh][WL4][SI][EI][WL05][value][WL06[ss][WL07]
        int checkSumLen = checkSum.length();
        int firstIndex = 0;
        int lastIndex = checkSumLen - 1;
        this.wrapper_length = checkSum.substring(0, 2);
        int wrapperLen = this.wrapper_length.charAt(1) - '0';
        firstIndex = firstIndex + 2 + wrapperLen;
        this.firstHashIndex = checkSum.charAt(firstIndex) - '0';
        firstIndex += 1;
        this.secHashIndex = checkSum.charAt(firstIndex) - '0';
        firstIndex += 1;
        firstIndex += wrapperLen;
        this.second = checkSum.substring(firstIndex, firstIndex + 2);
        firstIndex += 2;
        firstIndex += wrapperLen;
        this.hour = checkSum.substring(firstIndex, firstIndex + 2);
        firstIndex += 2;
        firstIndex += wrapperLen;
        this.uuidStartIndex = Integer.parseInt(checkSum.substring(firstIndex,
                firstIndex + 2));
        firstIndex += 2;
        this.uuidEndIndex = Integer.parseInt(checkSum.substring(firstIndex,
                firstIndex + 2));
        firstIndex += 2;
        firstIndex += wrapperLen;
        lastIndex = lastIndex - wrapperLen;
        this.minute = checkSum.substring(lastIndex + 1 - 2, lastIndex + 1);
        lastIndex -= 2;
        lastIndex = lastIndex - wrapperLen;
        this.value = checkSum.substring(firstIndex, lastIndex + 1);
        System.out.println("Extracting field ...");
        System.out.println("wrapper_length: " + wrapper_length + "firstHasecHashIndexIndex:" + firstHashIndex + " secHashIndex:" +
                secHashIndex + " sec: " + second
                + " hour: " + hour + " Start index: " +
                uuidStartIndex + " End Index " + uuidEndIndex + " Minute: " +
                minute + " Value: " + value);
// CommonLogger.info("firstHasecHashIndexIndex " + getFirstHashIndex());
// CommonLogger.info("secHashIndex " + getSecHashIndex());
// CommonLogger.info("ii " + getSecond());
// CommonLogger.info("hh " + getHour());
// CommonLogger.info("SI " + getUuidStartIndex());
// CommonLogger.info("EI " + getUuidEndIndex());
// CommonLogger.info("ss " + getMinute());
// CommonLogger.info("value " + getValue());
    }


    public String getHasecHashIndexValue(String value, String algorithm) throws
            NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] firstMessageDigest = md.digest(value.getBytes());
        BigInteger firstNo = new BigInteger(1, firstMessageDigest);
        return firstNo.toString(16);
    }

    public int convert_crc32(byte[] data) {
        BitSet bitSet = BitSet.valueOf(data);
        int crc32 = 0xFFFFFFFF;
        for (int i = 0; i < data.length * 8; i++) {
            if (((crc32 >>> 31) & 1) != (bitSet.get(i) ? 1 : 0))
                crc32 = (crc32 << 1) ^ 0x04C11DB7;
            else
                crc32 = (crc32 << 1);
        }
        crc32 = Integer.reverse(crc32);
        return ~crc32;
    }

    //generating value from the rest fields of checksumUtils
    public String generateValue(ChecksumUtils checksumUtils, String uuid) {
        String rawValue = "";
        Calendar calendar = Calendar.getInstance();
        checksumUtils.yy =
                String.valueOf(calendar.get(Calendar.YEAR)).substring(2, 4);
        checksumUtils.mm =
                String.valueOf(calendar.get(Calendar.MONTH));
        checksumUtils.dd =
                String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        int BW = (checksumUtils.wrapper_length.charAt(0) - '0') % 2;
        if (BW == 0) {
            rawValue =
                    uuid.substring(checksumUtils.uuidStartIndex,
                            checksumUtils.uuidEndIndex + 1) + checksumUtils.yy + checksumUtils.mm +
                            checksumUtils.dd + checksumUtils.hour +
                            checksumUtils.second + checksumUtils.minute;
        } else {
            rawValue = checksumUtils.yy + checksumUtils.mm +
                    checksumUtils.dd + checksumUtils.hour + checksumUtils.second +
                    checksumUtils.minute +
                    uuid.substring(checksumUtils.uuidStartIndex,
                            checksumUtils.uuidEndIndex + 1);
        }
        try {
            String firstHashText = "";
            String secHashText = "";
            if (Objects.equals(ListHasecHashIndex.get(checksumUtils.firstHashIndex), "CRC32")) {
                byte[] rawValByte = rawValue.getBytes();
                firstHashText = Integer.toHexString(convert_crc32(rawValByte));
            } else firstHashText = getHasecHashIndexValue(rawValue,
                    ListHasecHashIndex.get(checksumUtils.firstHashIndex));
            if (Objects.equals(ListHasecHashIndex.get(checksumUtils.secHashIndex), "CRC32")) {
                byte[] firstHashTextBytes = firstHashText.getBytes();
                secHashText =
                        Integer.toHexString(convert_crc32(firstHashTextBytes));
            } else secHashText = getHasecHashIndexValue(firstHashText,
                    ListHasecHashIndex.get(checksumUtils.secHashIndex));
//            CommonLogger.info("Raw value acquired from checksum " +
//                    uuid.substring(checksumUtils.uuidStartIndex, checksumUtils.uuidEndIndex + 1));
//            CommonLogger.info("first Hash " + firstHashText);
//            CommonLogger.info("sec secHash " + secHashText);
            return secHashText;
        }
// For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validateChecksum(String checkSum, String uuid) {
        ChecksumUtils checksumUtils = new ChecksumUtils();
        checksumUtils.extractFieldChecksum(checkSum);
        Date currDate = new Date();
        Date sendDate = DateTimeUtils.atBeginningOfDay();
        sendDate = DateTimeUtils.addTime(
                sendDate,
                Integer.parseInt(checksumUtils.hour),
                Integer.parseInt(checksumUtils.minute),
                Integer.parseInt(checksumUtils.second)
        );

        double interval = Math.abs(DateTimeUtils.timeDiffInMillis(sendDate,
                currDate) * 1D / (1000 * 60));
        if (interval > 30 || interval < 0) {
            return false;
        }
        String hashedValue = generateValue(checksumUtils, uuid);
        return hashedValue.equals(checksumUtils.value);
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public static void main(String[] args) throws NoSuchAlgorithmException,
//            InterruptedException {
//        String uuid = "00008030-001C18893E018028";
//
//        ChecksumUtils checksumUtils = new ChecksumUtils();
//        String newChecksum = checksumUtils.generateChecksum(uuid);
//        System.out.println("Final checksum " + newChecksum);
//        System.out.println("Validate newly generated check sum ....");
//        System.out.println("Is valid? " +
//                checksumUtils.validateChecksum(newChecksum, uuid));
//    }

}
