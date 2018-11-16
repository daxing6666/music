package com.music.frame.ui.view.recycleview;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.music.utils.Constants;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * [description about this class]
 * 1：RecyclerView要求必须使用ViewHolder模式，一般我们在使用过程中，
 *   都需要去建立一个新的ViewHolder然后作为泛型传入Adapter。
 *   那么想要建立通用的Adapter，必须有个通用的ViewHolder。
 * 2：ViewHolder的主要的作用，实际上是通过成员变量存储对应的convertView中需要操作的字View，
 *   避免每次findViewById，从而提升运行的效率
 * 3：取而代之的只能是个集合来存储这些view
 * @author jack
 */

public class ViewHolder extends RecyclerView.ViewHolder{

    private SparseArray<View> sparseArray;
    private View view;

    public ViewHolder(View itemView) {
        super(itemView);
        this.view = itemView;
        sparseArray = new SparseArray<View>();
    }


    public static ViewHolder createViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    public static ViewHolder createViewHolder(Context context, ViewGroup parent, int layoutId) {
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    public View getConvertView() {
        return this.view;
    }

    /**
     * 通过viewId获取控件
     *
     * @param viewId
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = sparseArray.get(viewId);
        if (view == null) {
            view = this.view.findViewById(viewId);
            sparseArray.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为TextView设置文字内容
     * @param viewId
     * @param txt
     */
    public void setText(int viewId,String txt){
        TextView tv = getView(viewId);
        if(isEmpty(txt)){
            tv.setText("");
        }else{
            tv.setText(txt);
        }
    }


    /**
     * 为TextView设置文字内容加横线
     * @param viewId
     * @param txt
     * @param flag 1 2
     */
    public void setTextFlag(int viewId,String txt,int flag){
        TextView tv = getView(viewId);
        if(isEmpty(txt)){
            tv.setText("");
        }else{
            tv.setText(txt);
        }
        if(flag == 1){
            //中间加横线
            tv.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG );
        }else if(flag == 2){
            //底部加横线：
            tv .getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG );
        }
    }

    /**
     *
     * @param path
     */
    public void setImageHttp(int viewId,String path){
        SimpleDraweeView simpleDraweeView = getView(viewId);
        if(isEmpty(path)){
            Uri uri = Uri.parse("");
            simpleDraweeView.setImageURI(uri);
        }else {
            Uri uri = Uri.parse(Constants.IP_PORT_DEFAULT_PICTURE + path);
            simpleDraweeView.setImageURI(uri);
        }
    }

    /**
     * ImageView设置ImageResource
     * @param viewId
     * @param resId
     */
    public void setImageResource(int viewId, int resId){
        ImageView view = getView(viewId);
        view.setImageResource(resId);
    }

    private boolean isEmpty(String txt){
        if(txt!=null && !txt.equals("null") && !txt.equals("") && !txt.equals(" ")){
            return true;
        }
        return false;
    }

}
