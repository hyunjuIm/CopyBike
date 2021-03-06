package com.example.copybike.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.copybike.R;

import java.util.ArrayList;

public class SideListViewAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private ArrayList<SideListViewItem> menu = new ArrayList<SideListViewItem>() ;
    private LayoutInflater inflater; //레이아웃 XML 파일을 해당 View 객체 로 인스턴스화

    public SideListViewAdapter(Context mContext, ArrayList<SideListViewItem> position) {
        this.mContext = mContext;
        this.menu = position;
        //컨텍스트에서 레이아웃 리소스를 확장
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return menu.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return menu.get(groupPosition).menu.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return menu.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return menu.get(groupPosition).menu.get(childPosition);
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
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.side_list_item_1, parent, false);
        }

        SideListViewItem item = (SideListViewItem) getGroup(groupPosition);

        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.list_row_image) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.list_row_txt) ;

        iconImageView.setImageDrawable(item.getIcon());
        titleTextView.setText(item.getTitle());

        ImageView slideImageView = (ImageView) convertView.findViewById(R.id.list_row_expand_image);

        if(getChildrenCount(groupPosition) > 0){
            slideImageView.setVisibility(View.VISIBLE);
            if(isExpanded){
                slideImageView.setImageResource(R.drawable.slide_down_icon);
            } else {
                slideImageView.setImageResource(R.drawable.slide_up_icon);
            }
        } else {
            slideImageView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.side_list_item_2, parent, false);
        }

        String child = (String) getChild(groupPosition, childPosition);
        TextView name = (TextView) convertView.findViewById(R.id.list_row_child_txt);
        name.setText(child);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
