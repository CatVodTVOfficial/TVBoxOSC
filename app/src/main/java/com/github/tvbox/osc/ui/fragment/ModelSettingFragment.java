package com.github.tvbox.osc.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.AboutDialog;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.dialog.XWalkInitDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.util.XWalkUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class ModelSettingFragment extends BaseLazyFragment {
    private TextView tvDebugOpen;
    private TextView tvMediaCodec;
    private TextView tvParseWebView;
    private TextView tvPlay;
    private TextView tvRender;
    private TextView tvXWalkDown;
    private TextView tvApi;
    private TextView tvHomeApi;

    public static ModelSettingFragment newInstance() {
        return new ModelSettingFragment().setArguments();
    }

    public ModelSettingFragment setArguments() {
        return this;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_model;
    }

    @Override
    protected void init() {
        tvDebugOpen = findViewById(R.id.tvDebugOpen);
        tvParseWebView = findViewById(R.id.tvParseWebView);
        tvMediaCodec = findViewById(R.id.tvMediaCodec);
        tvPlay = findViewById(R.id.tvPlay);
        tvRender = findViewById(R.id.tvRenderType);
        tvXWalkDown = findViewById(R.id.tvXWalkDown);
        tvApi = findViewById(R.id.tvApi);
        tvHomeApi = findViewById(R.id.tvHomeApi);
        tvMediaCodec.setText(Hawk.get(HawkConfig.IJK_CODEC, ""));
        tvDebugOpen.setText(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? "已打开" : "已关闭");
        tvParseWebView.setText(Hawk.get(HawkConfig.PARSE_WEBVIEW, true) ? "系统自带" : "XWalkView");
        tvXWalkDown.setText(XWalkUtils.xWalkLibExist(mContext) ? "已下载" : "未下载");
        tvApi.setText(Hawk.get(HawkConfig.API_URL, ""));
        tvHomeApi.setText(ApiConfig.get().getHomeSourceBean().getName());
        tvPlay.setText(getPlayerName(Hawk.get(HawkConfig.PLAY_TYPE, 0)));
        tvRender.setText(getRenderName(Hawk.get(HawkConfig.PLAY_RENDER, 0)));
        findViewById(R.id.llXWalkCore).setVisibility(Hawk.get(HawkConfig.PARSE_WEBVIEW, true) ? View.GONE : View.VISIBLE);
        findViewById(R.id.llDebug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.DEBUG_OPEN, !Hawk.get(HawkConfig.DEBUG_OPEN, false));
                tvDebugOpen.setText(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? "已打开" : "已关闭");
            }
        });
        findViewById(R.id.llStorage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (XXPermissions.isGranted(requireContext(), Permission.Group.STORAGE)) {
                    Toast.makeText(requireContext(), "已获得存储权限", Toast.LENGTH_SHORT).show();
                } else {
                    XXPermissions.with(mActivity)
                            .permission(Permission.Group.STORAGE)
                            .request(new OnPermissionCallback() {
                                @Override
                                public void onGranted(List<String> permissions, boolean all) {
                                    if (all) {
                                        Toast.makeText(requireContext(), "已获得存储权限", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onDenied(List<String> permissions, boolean never) {
                                    if (never) {
                                        Toast.makeText(requireContext(), "获取存储权限失败,请在系统设置中开启", Toast.LENGTH_SHORT).show();
                                        XXPermissions.startPermissionActivity(mActivity, permissions);
                                    } else {
                                        Toast.makeText(requireContext(), "获取存储权限失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        findViewById(R.id.llParseWebVew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                boolean useSystem = !Hawk.get(HawkConfig.PARSE_WEBVIEW, true);
                Hawk.put(HawkConfig.PARSE_WEBVIEW, useSystem);
                tvParseWebView.setText(Hawk.get(HawkConfig.PARSE_WEBVIEW, true) ? "系统自带" : "XWalkView");
                if (!useSystem) {
                    Toast.makeText(mContext, "注意: XWalkView只适用于部分低Android版本，Android4.4以上推荐使用系统自带", Toast.LENGTH_LONG).show();
                    if (!XWalkUtils.xWalkLibExist(mContext)) {
                        XWalkInitDialog dialog = new XWalkInitDialog().setOnListener(new XWalkInitDialog.OnListener() {
                            @Override
                            public void onchange() {
                                tvXWalkDown.setText(XWalkUtils.xWalkLibExist(mContext) ? "已下载" : "未下载");
                            }
                        }).build(mContext);
                        dialog.show();
                    }
                }
                findViewById(R.id.llXWalkCore).setVisibility(useSystem ? View.GONE : View.VISIBLE);
            }
        });
        findViewById(R.id.llXWalkCore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                XWalkInitDialog dialog = new XWalkInitDialog().setOnListener(new XWalkInitDialog.OnListener() {
                    @Override
                    public void onchange() {
                        tvXWalkDown.setText(XWalkUtils.xWalkLibExist(mContext) ? "已下载" : "未下载");
                    }
                }).build(mContext);
                dialog.show();
            }
        });
        findViewById(R.id.llAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                AboutDialog dialog = new AboutDialog().build(mActivity);
                dialog.show();
            }
        });
        findViewById(R.id.llHomeApi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                List<SourceBean> sites = ApiConfig.get().getSourceBeanList();
                if (sites.size() > 0) {
                    SelectDialog<SourceBean> dialog = new SelectDialog<>(mActivity);
                    dialog.setTip("请选择首页数据源");
                    SelectDialogAdapter<SourceBean> selectAdapter = new SelectDialogAdapter(new SelectDialogAdapter.SelectDialogInterface<SourceBean>() {
                        @Override
                        public void click(SourceBean value, int pos) {
                            ApiConfig.get().setSourceBean(value);
                            tvHomeApi.setText(ApiConfig.get().getHomeSourceBean().getName());
                        }

                        @Override
                        public String getDisplay(SourceBean val) {
                            return val.getName();
                        }
                    }, new DiffUtil.ItemCallback<SourceBean>() {
                        @Override
                        public boolean areItemsTheSame(@NonNull @NotNull SourceBean oldItem, @NonNull @NotNull SourceBean newItem) {
                            return oldItem == newItem;
                        }

                        @Override
                        public boolean areContentsTheSame(@NonNull @NotNull SourceBean oldItem, @NonNull @NotNull SourceBean newItem) {
                            return oldItem.getKey().equals(newItem.getKey());
                        }
                    });
                    selectAdapter.setData(sites, sites.indexOf(ApiConfig.get().getHomeSourceBean()));
                    dialog.setAdapter(selectAdapter);
                    dialog.show();
                }
            }
        });
        findViewById(R.id.llApi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("请输入配置地址");
                final EditText edit = new EditText(mActivity);
                edit.setText(tvApi.getText());
                builder.setView(edit);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = edit.getText().toString().trim();
                        Hawk.put(HawkConfig.API_URL, url);
                        tvApi.setText(url);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
            }
        });
        findViewById(R.id.llMediaCodec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<IJKCode> ijkCodes = ApiConfig.get().getIjkCodes();
                if (ijkCodes == null || ijkCodes.size() == 0)
                    return;
                FastClickCheckUtil.check(v);

                int defaultPos = 0;
                String ijkSel = Hawk.get(HawkConfig.IJK_CODEC, "");
                for (int j = 0; j < ijkCodes.size(); j++) {
                    if (ijkSel.equals(ijkCodes.get(j).getName())) {
                        defaultPos = j;
                        break;
                    }
                }

                SelectDialog<SourceBean> dialog = new SelectDialog<>(mActivity);
                dialog.setTip("请选择Ijk解码");
                SelectDialogAdapter<IJKCode> selectAdapter = new SelectDialogAdapter(new SelectDialogAdapter.SelectDialogInterface<IJKCode>() {
                    @Override
                    public void click(IJKCode value, int pos) {
                        value.selected(true);
                        tvMediaCodec.setText(value.getName());
                    }

                    @Override
                    public String getDisplay(IJKCode val) {
                        return val.getName();
                    }
                }, new DiffUtil.ItemCallback<IJKCode>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull IJKCode oldItem, @NonNull @NotNull IJKCode newItem) {
                        return oldItem == newItem;
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull IJKCode oldItem, @NonNull @NotNull IJKCode newItem) {
                        return oldItem.getName().equals(newItem.getName());
                    }
                });
                selectAdapter.setData(ijkCodes, defaultPos);
                dialog.setAdapter(selectAdapter);
                dialog.show();
            }
        });
        findViewById(R.id.llPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int defaultPos = Hawk.get(HawkConfig.PLAY_TYPE, 0);
                ArrayList<Integer> players = new ArrayList<>();
                players.add(0);
                players.add(1);
                players.add(2);
                SelectDialog<SourceBean> dialog = new SelectDialog<>(mActivity);
                dialog.setTip("请选择默认播放器");
                SelectDialogAdapter<Integer> selectAdapter = new SelectDialogAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Hawk.put(HawkConfig.PLAY_TYPE, value);
                        tvPlay.setText(getPlayerName(value));
                        PlayerHelper.init();
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        return getPlayerName(val);
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }
                });
                selectAdapter.setData(players, defaultPos);
                dialog.setAdapter(selectAdapter);
                dialog.show();
            }
        });
        findViewById(R.id.llRender).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int defaultPos = Hawk.get(HawkConfig.PLAY_RENDER, 0);
                ArrayList<Integer> renders = new ArrayList<>();
                renders.add(0);
                renders.add(1);
                SelectDialog<SourceBean> dialog = new SelectDialog<>(mActivity);
                dialog.setTip("请选择默认渲染方式");
                SelectDialogAdapter<Integer> selectAdapter = new SelectDialogAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Hawk.put(HawkConfig.PLAY_RENDER, value);
                        tvRender.setText(getRenderName(value));
                        PlayerHelper.init();
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        return getRenderName(val);
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }
                });
                selectAdapter.setData(renders, defaultPos);
                dialog.setAdapter(selectAdapter);
                dialog.show();
            }
        });
        SettingActivity.callback = new SettingActivity.DevModeCallback() {
            @Override
            public void onChange() {
                findViewById(R.id.llDebug).setVisibility(View.VISIBLE);
            }
        };
    }

    private String getPlayerName(int playType) {
        if (playType == 1) {
            return "IJK播放器";
        } else if (playType == 2) {
            return "Exo播放器";
        } else {
            return "系统播放器";
        }
    }

    private String getRenderName(int renderType) {
        if (renderType == 1) {
            return "SurfaceView";
        } else {
            return "TextureView";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SettingActivity.callback = null;
    }
}