package com.example.copybike.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.copybike.R;

import java.util.ArrayList;

public class NoticeViewAdapter extends BaseAdapter {
    private ArrayList<NoticeListViewItem> noticeViewItemlist = new ArrayList<NoticeListViewItem>();

    @Override
    public int getCount() {
        return noticeViewItemlist.size();
    }

    // 지정한 위치(position)에 있는 데이터 리턴
    @Override
    public Object getItem(int position) {
        return noticeViewItemlist.get(position);
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴.
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //root : xml이 어디에 붙을지 지정
            //attachToRoot : true - 생성되는 뷰가 root의 자식이 됨, false - root는 생성되는 View의 LayoutParam을 생성하는데만 사용
            convertView = inflater.inflate(R.layout.notice_item, parent, false);
        }

        TextView titleTextView = (TextView) convertView.findViewById(R.id.tv_notice_title) ;
        TextView dateTextView = (TextView) convertView.findViewById(R.id.tv_notice_date) ;

        // noticeViewItemlist에서 position에 위치한 데이터 참조 획득
        NoticeListViewItem noticeListViewItem = noticeViewItemlist.get(position);

        String date = "";
        for(int i=0; i<8; i++){
            if(i==4 || i==6){
                date += "-";
            }
            date += noticeListViewItem.getInputDate().charAt(i);
        }

        titleTextView.setText(noticeListViewItem.getTitle());
        dateTextView.setText(date);

        return convertView;
    }

    public void addItem(NoticeListViewItem item) {
        noticeViewItemlist.add(item);
    }
}
