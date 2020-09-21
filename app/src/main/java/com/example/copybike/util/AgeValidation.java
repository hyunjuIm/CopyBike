package com.example.copybike.util;

import java.util.Calendar;

public class AgeValidation {

    private static Calendar cal = Calendar.getInstance();

    // 오늘 날짜 기준 년
    private static int getNowYear() {
        return cal.get(cal.YEAR);
    }

    // 오늘 날짜 기준 월
    private static int getNowMonth() {
        return cal.get(cal.MONTH) + 1;
    }

    // 오늘 날짜 기준 일
    private static int getNowDay() {
        return cal.get(cal.DATE);
    }

    // 대한민국식 나이 계산
    private static int getKoreaAge(int birth_year, int now_year) {
        int koreaAge = (now_year - birth_year) + 1;

        return koreaAge;
    }

    // 만 나이 계산
    private static int getBasicAge(int birth_year, int birth_month, int birth_day, int now_year, int now_month, int now_day) {
        int basicAge;
        int koreaAge = getKoreaAge(birth_year, now_year);
        if (now_month == birth_month) {
            // 생월과 현재월이 같은 경우
            if (now_day > birth_day) {
                // 생일이 지난 경우 ---> -1
                basicAge = koreaAge - 1;
            } else {
                // 생일이 안지난 경우 ---> -2
                basicAge = koreaAge - 2;
            }
        } else if (now_month > birth_month) {
            // 생월이 지난 경우 ---> -1
            basicAge = koreaAge - 1;
        } else {
            // 생월이 안지난 경우 ---> -2
            basicAge = koreaAge - 2;
        }

        return basicAge;
    }

    // 만 19세 이상인지 아닌지를 판별해 줌
    public static boolean isAge19(int birth_year, int birth_month, int birth_day) {
        return getBasicAge(birth_year, birth_month, birth_day, getNowYear(), getNowMonth(), getNowDay()) >= 19;
    }

    // 만 19세 이상인지 아닌지를 판별해 줌
    public static boolean isAge19(String birthDate, String gender) {
        try {//211212
            if (birthDate.length() == 8) {
                return getBasicAge(Integer.parseInt(birthDate.substring(0, 4)), Integer.parseInt(birthDate.substring(4, 6)), Integer.parseInt(birthDate.substring(6, 8)), getNowYear(), getNowMonth(), getNowDay()) >= 19;
            } else if (birthDate.length() == 6) {
                if (!gender.equals("3") && !gender.equals("4")) {
                    return getBasicAge(Integer.parseInt(19 + birthDate.substring(0, 2)), Integer.parseInt(birthDate.substring(2, 4)), Integer.parseInt(birthDate.substring(4, 6)), getNowYear(), getNowMonth(), getNowDay()) >= 19;
                } else {
                    return getBasicAge(Integer.parseInt(20 + birthDate.substring(0, 2)), Integer.parseInt(birthDate.substring(2, 4)), Integer.parseInt(birthDate.substring(4, 6)), getNowYear(), getNowMonth(), getNowDay()) >= 19;
                }
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isAge19(String birthDate) {
        try {//211212
            if (birthDate.length() == 8) {
                return getBasicAge(Integer.parseInt(birthDate.substring(0, 4)), Integer.parseInt(birthDate.substring(4, 6)), Integer.parseInt(birthDate.substring(6, 8)), getNowYear(), getNowMonth(), getNowDay()) >= 19;
            } else if (birthDate.length() == 6) {
                if (birthDate.substring(0, 1).equals("0") || birthDate.substring(0, 1).equals("1") || birthDate.substring(0, 1).equals("2")) {
                    return getBasicAge(Integer.parseInt(20 + birthDate.substring(0, 2)), Integer.parseInt(birthDate.substring(2, 4)), Integer.parseInt(birthDate.substring(4, 6)), getNowYear(), getNowMonth(), getNowDay()) >= 19;
                } else {
                    return getBasicAge(Integer.parseInt(19 + birthDate.substring(0, 2)), Integer.parseInt(birthDate.substring(2, 4)), Integer.parseInt(birthDate.substring(4, 6)), getNowYear(), getNowMonth(), getNowDay()) >= 19;
                }
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
