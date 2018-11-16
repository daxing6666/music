package com.music.frame.ui.view.recycleview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.music.frame.bean.InfoResult;
import com.music.frame.ui.view.recycleview.ViewHolder;
import com.music.frame.ui.view.recycleview.listener.DataStateListener;
import com.music.utils.Constants;
import java.util.List;

/**
 * [description about this class]
 * Recyclerview基础数据适配器(同一种数据类型)
 * 使用过程中,真实的数据类型Bean应该是不同的,那么这里要引入泛型代表我们的Bean,
 * 内部通过一个List<T>代表我们的数据集合
 * @author jack
 */

public abstract class RecyclerviewBasicAdapter <T> extends RecyclerView.Adapter<ViewHolder>{

    protected Context context;
    protected int layoutId;
    protected List<T> datas;
    protected LayoutInflater inflater;
    public DataStateListener dataStateListener;

    public RecyclerviewBasicAdapter(Context context, List<T> datas, int layoutId) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.layoutId = layoutId;
        this.datas = datas;
    }

    public RecyclerviewBasicAdapter(Context context, List<T> datas, int layoutId,DataStateListener dataStateListener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.layoutId = layoutId;
        this.datas = datas;
        this.dataStateListener = dataStateListener;
    }

    //这个方法用来创建View,被LayoutManager所用,说简单点就是获取RecyclerView的每个Item,但最后返回的不是view而是自定义ViewHolder的对象。
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        ViewHolder viewHolder = ViewHolder.createViewHolder(context,parent, layoutId);
        return viewHolder;
    }

    //这个方法用来绑定数据
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(datas!=null && datas.size()>0){
            if(position<datas.size()){
                convert(holder, datas.get(position),position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return datas != null ? datas.size() : 0;
    }

    public void setDataSource(List<T> datas) {

        if(datas!=null && datas.size()>0){
            this.datas = datas;
        }else{
            if(this.datas!=null && this.datas.size()>0){
                this.datas.clear();
            }
            this.datas = datas;
        }
        notifyDataSetChanged();
    }

    /**
     * 增加数据
     * 1:没有网络 2:没有数据 3:获取数据失败 4:展示内容
     * @param infos
     */
    public final void setDataSource(List<T> infos, InfoResult infoResult) {
        int state = infoResult.getState();
        if(state == Constants.NO_NET_NUMBER){
            if(dataStateListener!=null){
                dataStateListener.onDataState(1);
            }
        }else{
            boolean flag = infoResult.isSuccess();
            if(flag){
                if (infos != null && infos.size() >0) {
                    this.datas = infos;
                    if(dataStateListener!=null){
                        dataStateListener.onDataState(4);
                    }
                    notifyDataSetChanged();
                }else{
                    if(dataStateListener!=null){
                        dataStateListener.onDataState(2);
                    }
                }
            }else{
                if(dataStateListener!=null){
                    dataStateListener.onDataState(3);
                }
            }
        }
    }

    public List<T> getDataSource()
    {
        return this.datas;
    }

    public abstract void convert(ViewHolder holder, T t,int position);

    /**
     * 列表添加一条数据时可以调用，伴有(默认)动画效果
     * @param t
     */
    public void addItem(T t,int index) {
        if(t!=null){
            if(datas == null){
                return;
            }
            if(datas.size()==0){
                datas.add(0, t);
                notifyItemInserted(0);
            }else if (datas.size()==1){
                datas.add(index,t);
                notifyDataSetChanged();
            }else{
                int position = datas.size();
                datas.add(index, t);
                //列表position添加一条数据时可以调用，伴有(默认)动画效果
                notifyItemInserted(index);
            }
        }
    }

    /**
     *  列表position位置移除一条数据时调用，伴有动画效果
     * @param t
     */
    public void removeItem(T t) {
        if(t!=null){
            if(datas == null || datas.size()==0){
                return;
            } else{
                int position = datas.indexOf(t);
                datas.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * 更新列表position位置上的数据可以调用
     * @param position
     */
    public void notifyItem(int position){
        notifyItemChanged(position);
    }

    /**
     * 列表fromPosition位置的数据移到toPosition位置时调用，伴有动画效果
     * 界面效果是交换了，但真实的数据集合没有交换,这里实现数据位置的交换
     * @param fromPosition
     * @param toPosition
     */
    public void moveItemMove(int fromPosition, int toPosition){
        if(datas == null){
            return;
        }
        if(datas.size()==0){
            return;
        }else{
            T tFrom = datas.get(fromPosition);
            T tTo = datas.get(toPosition);
            datas.set(fromPosition,tTo);
            datas.set(toPosition,tFrom);
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    /**
     * 列表从positionStart位置到itemCount数量的列表项进行数据刷新
     * @param positionStart
     * @param itemCount
     */
    public void notifyItemLotSizeChange(int positionStart, int itemCount){
        notifyItemRangeChanged(positionStart, itemCount);
    }

    /**
     * 列表项批量添加数据时调用，伴有动画效果
     */
    public void addLotSizeItem(List<T> beans){
        if(beans != null && beans.size() > 0){
            if(datas == null){
                return;
            }
            if(datas.size()==0){
                datas.addAll(beans);
                notifyItemRangeInserted(0, datas.size());
            }else{
                int positionStart = datas.size();
                datas.addAll(beans);
                //列表从positionStart位置到itemCount数量的列表项批量添加数据时调用，伴有动画效果
                notifyItemRangeInserted(positionStart, datas.size());
            }
        }
    }

    /**
     * 列表从positionStart位置到itemCount数量的列表项批量删除数据时调用，伴有动画效果
     * @param positionStart
     * @param itemCount
     */
    public void notifyItemRangeRemove(int positionStart, int itemCount){
        if(datas == null){
            return;
        }
        if(datas.size()==0){
            return;
        }else{
            /**这里对positionStart itemCount只做了简单的判断,这里只做操作,
               具体的传值是否符合要求,根据自己的数据源发起调该方法着去处理
             */
            notifyItemRangeRemoved(positionStart, itemCount);
            if(itemCount<=0 || positionStart<=0){
                return;
            }else{
                for(int i = 0;i<itemCount;i++){
                    datas.remove(positionStart);
                }
            }
        }
    }
}
