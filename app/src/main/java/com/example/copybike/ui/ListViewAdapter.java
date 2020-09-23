package com.example.copybike.ui;

import android.app.LauncherActivity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.copybike.MainActivity;
import com.example.copybike.R;

import java.util.ArrayList;

public class ListViewAdapter extends BaseExpandableListAdapter {
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
//
//    @Override
//    public int getCount() {
//        return listViewItemList.size() ;
//    }
//
//    // 지정한 위치(position)에 있는 데이터 리턴.
//    @Override
//    public Object getItem(int position) {
//        return null;
//    }
//
//    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴.
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴.
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        final int pos = position;
//        final Context context = parent.getContext();
//
//        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
//        if (convertView == null) {
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(R.layout.list_item_1, parent, false);
//        }
//
//        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
//        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.list_row_image) ;
//        TextView titleTextView = (TextView) convertView.findViewById(R.id.list_row_txt) ;
//
//        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
//        ListViewItem listViewItem = listViewItemList.get(position);
//
//        // 아이템 내 각 위젯에 데이터 반영
//        iconImageView.setImageDrawable(listViewItem.getIcon());
//        titleTextView.setText(listViewItem.getTitle());
//
//        return convertView;
//    }


//    public void addItem(Drawable icon, String title) {
//        ListViewItem item = new ListViewItem();
//
//        item.setIcon(icon);
//        item.setTitle(title);
//
//        listViewItemList.add(item);
//    }

    private Context mContext;
    private ArrayList<ListViewItem> position = new ArrayList<ListViewItem>() ;
    private LayoutInflater inflater;

    public ListViewAdapter (Context mContext, ArrayList<ListViewItem> position) {
        this.mContext = mContext;
        this.position = position;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return position.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return position.get(groupPosition).menu.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return position.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return position.get(groupPosition).menu.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.list_item_1, null);
        }

        ListViewItem position = (ListViewItem) getGroup(groupPosition);

        TextView textView = (TextView) convertView.findViewById(R.id.list_row_txt);
        textView.setText(position.getTitle());

        ImageView icon = (ImageView) convertView.findViewById(R.id.list_row_image);
        icon.setBackground(position.getIcon());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_row_expand_image);
        if(isExpanded){
            imageView.setImageResource(R.drawable.slide_down_icon);
        } else {
            imageView.setImageResource(R.drawable.slide_up_icon);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        //inflate the layout
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_2, null);
        }

        String child = (String) getChild(groupPosition, childPosition);

        TextView name = (TextView) convertView.findViewById(R.id.list_row_child_txt);

        name.setText(child);

        //get position name
        String positionName = (String) getGroup(groupPosition).toString();
        if (positionName == "이용안내") {

        } else if (positionName == "고객센터") {
            if (child == "공지사항") {
                Log.e("사이드메뉴", "공지사항 눌렀음");
            }
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
