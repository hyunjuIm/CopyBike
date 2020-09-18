package com.example.copybike.util;

import android.util.Log;

import java.util.regex.Pattern;

public class UserInfoValidation {

    private final static String TAG = "UserInfoValidation";

    // 아이디 검증 : 6~20, 시작글자가 영문
    public static boolean checkId(String userId) {
        try {
            if (userId == null) {
                return false;
            }

            if (!(userId.length() >= 1 && userId.length() <= 20)) {
                return false;
            }
            //2018.08.08 대문자 아이디 사용자로 인한 대문자 허용
            /*String first_char = userId.substring(0, 1);
            if (!Pattern.matches("^[a-z]*$", first_char)) {
                return false;
            }*/
            // 공백
            for(int i = 0 ; i < userId.length() ; i++)
            {
                if (userId.charAt(i) == ' ') {
                    return false;
                }
                //2018.08.08 대문자 아이디 사용자로 인한 대문자 허용
                /*if (userId.charAt(i) >= 0x41 && userId.charAt(i) <= 0x5A) {
                    return false;
                }*/
            }
            // 한글 포함 여부
            if (userId.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                return false;
            }
            // 특수문자
            if(!userId.matches("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝]*")) {
                return false;
            }
            return true;
        } catch (NullPointerException e) {
            Log.e(TAG, "UserInfoValidation -> " + "checkId()");
            return false;
        }
    }

    // 아이디 검증 : 6~20, 시작글자가 영문
    public static boolean checkJoinId(String userId) {
        try {
            if (userId == null) {
                return false;
            }

            if (!(userId.length() >= 6 && userId.length() <= 20)) {
                return false;
            }
            String first_char = userId.substring(0, 1);
            if (!Pattern.matches("^[a-z]*$", first_char)) {
                return false;
            }
            // 공백
            for(int i = 0 ; i < userId.length() ; i++)
            {
                if (userId.charAt(i) == ' ') {
                    return false;
                }
                if (userId.charAt(i) >= 0x41 && userId.charAt(i) <= 0x5A) {
                    return false;
                }
            }
            // 한글 포함 여부
            if (userId.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                return false;
            }
            // 특수문자
            if(!userId.matches("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝]*")) {
                return false;
            }
            return true;
        } catch (NullPointerException e) {
            Log.e(TAG, "UserInfoValidation -> " + "checkId()");
            return false;
        }
    }

    // 비밀번호 검증 : 6~12, 영문자혼합, 특수문자는 입력불가
    public static boolean checkPw(String Pw, String PwCheck) {
        try {
            if (Pw == null) {
                return false;
            }

            if (PwCheck == null) {
                return false;
            }

            if (!(Pw.trim().length() >= 6 && Pw.trim().length() <= 12)) {
                return false;
            }

            boolean IsContainDigit = false;
            boolean IsContainEnglish = false;
            for (int i=0; i<Pw.length(); i++) {
                char index = Pw.charAt(i);
                if (index >= '0' && index <= '9') {
                    IsContainDigit = true;
                } else if ((index >= 'a' && index <= 'z') || (index >= 'A' && index <= 'Z')) {
                    IsContainEnglish = true;
                }
            }
            if (!(IsContainDigit && IsContainEnglish)) {
                return false;
            }

            if(!Pw.matches("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝]*")) {
                return false;
            }

            if (!Pw.equals(PwCheck)) {
                return false;
            }

            return true;
        } catch (NullPointerException e) {
            Log.e(TAG, "UserInfoValidation -> " + "checkPw(p1, p2)");
            return false;
        }
    }

    // 비밀번호 검증 : 6~12, 영문자혼합
    public static boolean checkPw(String Pw) {
        try {
            if (Pw == null) {
                return false;
            }

            if (!(Pw.trim().length() >= 6 && Pw.trim().length() <= 12)) {
                return false;
            }

            boolean IsContainDigit = false;
            boolean IsContainEnglish = false;
            for (int i=0; i<Pw.length(); i++) {
                char index = Pw.charAt(i);
                if (index >= '0' && index <= '9') {
                    IsContainDigit = true;
                } else if ((index >= 'a' && index <= 'z') || (index >= 'A' && index <= 'Z')) {
                    IsContainEnglish = true;
                }
            }
            if (!(IsContainDigit && IsContainEnglish)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "UserInfoValidation -> " + "checkPw(p1)");
            return false;
        }
    }

    // 주민번호 검증
    public static boolean checkResidentRegistrationNumber(String ResidentRegistrationNumber) {
        try {
            if (ResidentRegistrationNumber == null) {
                return false;
            }

            int[] check_num = { 2, 3, 4, 5, 6, 7, 0, 8, 9, 2, 3, 4, 5 };
            int sum = 0;
            ResidentRegistrationNumber = ResidentRegistrationNumber.substring(0,6) + "-" + ResidentRegistrationNumber.substring(6,13);
            if (ResidentRegistrationNumber.trim().length() != 14) {
                return false;
            }

            for (int i=0; i<ResidentRegistrationNumber.length()-1; i++) {
                sum += check_num[i] * (ResidentRegistrationNumber.charAt(i) - 48);
            }

            int temp = 11 * (sum/11) + 11 - sum;
            int bn = temp - 10 * (temp / 10);
            if (bn != ResidentRegistrationNumber.charAt(13) - 48) {
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "UserInfoValidation -> " + "checkResidentRegistrationNumber() : Exception");
            return false;
        }
    }

    // 외국인번호 검증
    public static boolean checkForeignerRegistrationNumber(String ForeignerRegistrationNumber) {
        try {
            if (ForeignerRegistrationNumber == null) {
                return false;
            }

            int[] check_num = {2, 3, 4, 5, 6, 7, 0, 8, 9, 2, 3, 4, 5};
            int sum = 0;
            ForeignerRegistrationNumber = ForeignerRegistrationNumber.substring(0, 6) + "-" + ForeignerRegistrationNumber.substring(6, 13);
            if (ForeignerRegistrationNumber.trim().length() != 14) {
                return false;
            }

            for (int i = 0; i < ForeignerRegistrationNumber.length() - 1; i++) {
                sum += check_num[i] * (ForeignerRegistrationNumber.charAt(i) - 48);
            }

            sum = 11 - (sum % 11);
            if (sum >= 10) {
                sum -= 10;
            }
            sum += 2;
            if (sum >= 10) {
                sum -= 10;
            }
            int last = Integer.parseInt(ForeignerRegistrationNumber.substring(ForeignerRegistrationNumber.length() - 1));
            if (sum != last) {
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "UserInfoValidation -> " + "checkResidentRegistrationNumber() : Exception");
            return false;
        }
    }
}
