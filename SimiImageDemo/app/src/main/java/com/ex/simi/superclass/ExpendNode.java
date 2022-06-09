package com.ex.simi.superclass;

/**
 * Created by smy on 20-11-10.
 * <p>
 * 升级后 subItemList 需要自己维护，固有此基类。
 * 关于BaseNodeAdapter的一些注意事项：
 * 1.item点击事件可以在界面也可以在provider里，但子view的点击事件只能在界面里注册，不然有bug，并且只能在唯一一个地方去重写;
 * 2.convert方法只能在adapter或provider其中一个里重写，adapter里重写了provider里就失效了;
 * 3.接收的数据只能是 @BaseNode.class 或其子类，这意味着adapter里传的 entity 类不能继承别的类了;
 * 4.多级列表更新时需要调用框架封装的方法，不能使用notify系列，目前在该类中封装了2个更新ChildNodeItem的方法。(注意！调用该类里的方法改变数据源，也要注意更新方面的同步需要调用框架封装的方法！)
 * 5.源码中removeNodesAt（）这个方法有bug，先移除自己再获取，会出现数组越界，目前github中很多人反映了这个bug，还没改。
 * （删除了FootView相关移除代码，所以不要添加FootView！）
 * 6.findParentNode()这个方法在2.X版本时为找不到父节点返回该节点本身（说明自己就是最上级节点了），3.X改成了找不到父节点返回-1
 * 7.BaseNodeAdapter 如果要添加HeaderView，就得用该Adapter中的更新方式;如果自己写了一套更新方式，那就得自己维护一个HeaderView
 * 8.待补充。。。
 */

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public abstract class ExpendNode extends BaseExpandNode {
    protected List<BaseNode> mSubItems;


    public boolean hasSubItem() {
        return mSubItems != null && mSubItems.size() > 0;
    }

    public void setSubItems(List<BaseNode> list) {
        mSubItems = list;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return mSubItems;
    }

    public BaseNode getSubItem(int position) {
        if (hasSubItem() && position < mSubItems.size()) {
            return mSubItems.get(position);
        } else {
            return null;
        }
    }

    public int getSubItemPosition(BaseNode subItem) {
        return mSubItems != null ? mSubItems.indexOf(subItem) : -1;
    }

    public void addSubItem(BaseNode subItem) {
        if (mSubItems == null) {
            mSubItems = new ArrayList<>();
        }
        mSubItems.add(subItem);
    }

    public void addSubItem(int position, BaseNode subItem) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.add(position, subItem);
        } else {
            addSubItem(subItem);
        }
    }

    public boolean contains(BaseNode subItem) {
        return mSubItems != null && mSubItems.contains(subItem);
    }

    public boolean removeSubItem(BaseNode subItem) {
        return mSubItems != null && mSubItems.remove(subItem);
    }

    public boolean removeSubItem(int position) {
        if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
            mSubItems.remove(position);
            return true;
        }
        return false;
    }

    /**
     * ChildNodeItem更新方法补充封装:
     * 这玩意儿必须获取父节点，再通过父节点去更新子节点，故有此逻辑
     * 并且从源码看这玩意儿更新子节点时，如果父节点是展开的，那么父节点下的子节点都会刷新（remove 再 add）一次
     */
    public static void notifyChildNodeChanged(BaseNodeAdapter adapter, BaseNode entity) {
        BaseNode parentNode = adapter.getItem(adapter.findParentNode(entity));
        if (parentNode != null) {
            int childNodeIndex = parentNode.getChildNode().indexOf(entity);
            adapter.nodeSetData(parentNode, childNodeIndex, entity);
        }
    }

    public static void notifyChildNodeChanged(BaseNodeAdapter adapter, int pos) {
        BaseNode parentNode = adapter.getItem(adapter.findParentNode(pos));
        if (parentNode != null) {
            int childNodeIndex = parentNode.getChildNode().indexOf(adapter.getItem(pos));
            adapter.nodeSetData(parentNode, childNodeIndex, adapter.getItem(pos));
        }
    }

    /**
     * ParentNodeItem更新方法补充封装
     */

    public static void notifyParentNodeChanged(BaseNodeAdapter adapter, BaseNode entity, int pos) {
        adapter.setData(pos, entity);
    }
}
