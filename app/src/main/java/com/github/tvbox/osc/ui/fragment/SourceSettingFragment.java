package com.github.tvbox.osc.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.receiver.CustomWebReceiver;
import com.github.tvbox.osc.ui.adapter.SourceSettingAdapter;
import com.github.tvbox.osc.ui.dialog.SpeedTestDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SourceSettingFragment extends BaseLazyFragment {
    private TvRecyclerView mGridView;
    private SourceSettingAdapter settingAdapter;

    public static SourceSettingFragment newInstance() {
        return new SourceSettingFragment().setArguments();
    }

    public SourceSettingFragment setArguments() {
        return this;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_source_grid;
    }

    @Override
    protected void init() {
        mGridView = findViewById(R.id.mGridView);
        settingAdapter = new SourceSettingAdapter();
        mGridView.setAdapter(settingAdapter);
        mGridView.setLayoutManager(new V7GridLayoutManager(getContext(), 5));
        List<SourceBean> sourceBeans = new ArrayList<>();
        // sourceBeans.add(SourceBean.speedTestBean);
        sourceBeans.addAll(ApiConfig.get().getSourceBeanList());
        settingAdapter.setNewData(sourceBeans);
        settingAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                SourceBean sourceBean = settingAdapter.getData().get(position);
                if (sourceBean == SourceBean.speedTestBean) {
                    SpeedTestDialog dialog = new SpeedTestDialog().build(mContext);
                    dialog.show();
                } else {
                    int preHome = settingAdapter.getData().indexOf(ApiConfig.get().getHomeSourceBean());
                    ApiConfig.get().setSourceBean(sourceBean);
                    settingAdapter.notifyItemChanged(preHome);
                    settingAdapter.notifyItemChanged(position);
                    /*SourceSetDialog dialog = new SourceSetDialog()
                            .bean(sourceBean)
                            .OnChangeSrcListener(new SourceSetDialog.OnChangeSrcListener() {
                                @Override
                                public void onHome() {
                                    // 之前的首页源可能也要刷新下
                                    int homePosition = settingAdapter.getData().indexOf(ApiConfig.get().getHomeSourceBean());
                                    settingAdapter.notifyItemChanged(homePosition);
                                    settingAdapter.notifyItemChanged(position);
                                }

                                @Override
                                public void onRefresh() {
                                    settingAdapter.notifyItemChanged(position);
                                }

                                @Override
                                public void onDelete() {
                                    settingAdapter.remove(position);
                                }
                            }).build(SourceSettingFragment.this);
                    dialog.show();*/
                }
            }
        });
    }

    CustomWebReceiver.Callback refreshView = new CustomWebReceiver.Callback() {
        @Override
        public void onChange(String action, Object obj) {
            if (action.equals(CustomWebReceiver.REFRESH_SOURCE)) {
                int len = settingAdapter.getData().size();
                settingAdapter.addData(len - 1, (SourceBean) obj);
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CustomWebReceiver.callback.add(refreshView);
    }

    @Override
    public void onDestroyView() {
        CustomWebReceiver.callback.remove(refreshView);
        super.onDestroyView();
    }
}