package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.MD5;
import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.jessyan.autosize.utils.AutoSizeUtils;

public class SearchAdapter extends BaseQuickAdapter<Movie.Video, BaseViewHolder> {
    public SearchAdapter() {
        super(Hawk.get(HawkConfig.SEARCH_VIEW, 0) == 0 ? R.layout.item_search_lite : R.layout.item_search, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, Movie.Video item) {
        // lite
        if (Hawk.get(HawkConfig.SEARCH_VIEW, 0) == 0) {
            helper.setText(R.id.tvName, String.format("%s  %s %s %s", ApiConfig.get().getSource(item.sourceKey).getName(), item.name, item.type == null ? "" : item.type, item.note == null ? "" : item.note));
        } else {// with preview
            helper.setText(R.id.tvName, item.name);
            helper.setText(R.id.tvSite, ApiConfig.get().getSource(item.sourceKey).getName());
            helper.setVisible(R.id.tvNote, item.note != null && !item.note.isEmpty());
            if (item.note != null && !item.note.isEmpty()) {
                helper.setText(R.id.tvNote, item.note);
            }
            ImageView ivThumb = helper.getView(R.id.ivThumb);
            if (!TextUtils.isEmpty(item.pic)) {
                Picasso.get()
                        .load(item.pic)
                        .transform(new RoundTransformation(MD5.string2MD5(item.pic + "position=" + helper.getLayoutPosition()))
                                .centerCorp(true)
                                .override(AutoSizeUtils.mm2px(mContext, 300), AutoSizeUtils.mm2px(mContext, 400))
                                .roundRadius(AutoSizeUtils.mm2px(mContext, 10), RoundTransformation.RoundType.ALL))
                        .placeholder(R.drawable.img_loading_placeholder)
                        .error(R.drawable.img_loading_placeholder)
                        .into(ivThumb);
            } else {
                ivThumb.setImageResource(R.drawable.img_loading_placeholder);
            }
        }
    }
}