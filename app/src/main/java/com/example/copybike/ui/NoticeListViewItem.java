package com.example.copybike.ui;

public class NoticeListViewItem {
    private String number;     // 번호
    private String writer;     // 작성자
    private String inputDate; // 작성일
    private String title;      // 제목

    // 번호
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    // 작성자
    public String getWriter() {
        return writer;
    }
    public void setWriter(String writer) {
        this.writer = writer;
    }

    // 작성일
    public String getInputDate() {
        return inputDate;
    }
    public void setInputDate(String inputDate) {
        this.inputDate = inputDate;
    }

    // 제목
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
