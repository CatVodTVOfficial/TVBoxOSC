package com.github.tvbox.osc.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.dialog.AboutDialog;
import com.github.tvbox.osc.ui.dialog.ChangeIJKCodeDialog;
import com.github.tvbox.osc.ui.dialog.ChangePlayDialog;
import com.github.tvbox.osc.ui.dialog.ChangeRenderDialog;
import com.github.tvbox.osc.ui.dialog.XWalkInitDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.util.XWalkUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.orhanobut.hawk.Hawk;

import java.util.List;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class ModelSettingFragment extends BaseLazyFragment {
    private TextView tvDebugOpen;
    private TextView tvTestChannel;
    private TextView tvMediaCodec;
    private TextView tvParseWebView;
    private TextView tvPlay;
    private TextView tvRender;
    private TextView tvXWalkDown;
    private TextView tvApi;

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
        tvTestChannel = findViewById(R.id.tvTestChannel);
        tvParseWebView = findViewById(R.id.tvParseWebView);
        tvMediaCodec = findViewById(R.id.tvMediaCodec);
        tvPlay = findViewById(R.id.tvPlay);
        tvRender = findViewById(R.id.tvRenderType);
        tvXWalkDown = findViewById(R.id.tvXWalkDown);
        tvApi = findViewById(R.id.tvApi);
        tvMediaCodec.setText(Hawk.get(HawkConfig.IJK_CODEC, ""));
        tvDebugOpen.setText(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? "已打开" : "已关闭");
        tvTestChannel.setText(Hawk.get(HawkConfig.TEST_CHANNEL, false) ? "已打开" : "已关闭");
        tvParseWebView.setText(Hawk.get(HawkConfig.PARSE_WEBVIEW, true) ? "系统自带" : "XWalkView");
        tvXWalkDown.setText(XWalkUtils.xWalkLibExist(mContext) ? "已下载" : "未下载");
        tvApi.setText(Hawk.get(HawkConfig.API_URL, ""));
        findViewById(R.id.llXWalkCore).setVisibility(Hawk.get(HawkConfig.PARSE_WEBVIEW, true) ? View.GONE : View.VISIBLE);
        changePlay();
        changeRender();
        findViewById(R.id.llDebug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.DEBUG_OPEN, !Hawk.get(HawkConfig.DEBUG_OPEN, false));
                tvDebugOpen.setText(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? "已打开" : "已关闭");
            }
        });
        findViewById(R.id.llTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.TEST_CHANNEL, !Hawk.get(HawkConfig.TEST_CHANNEL, false));
                tvTestChannel.setText(Hawk.get(HawkConfig.TEST_CHANNEL, false) ? "已打开" : "已关闭");
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
        findViewById(R.id.llApi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                ChangeIJKCodeDialog dialog = new ChangeIJKCodeDialog().build(mActivity, new ChangeIJKCodeDialog.Callback() {
                    @Override
                    public void change() {
                        tvMediaCodec.setText(Hawk.get(HawkConfig.IJK_CODEC, ""));
                    }
                });
                dialog.show();
            }
        });
        findViewById(R.id.llPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                new ChangePlayDialog().setOnChangePlayListener(new ChangePlayDialog.OnChangePlayListener() {
                    @Override
                    public void onChange() {
                        changePlay();
                        PlayerHelper.init();
                    }
                }).build(mContext).show();
            }
        });
        findViewById(R.id.llRender).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                new ChangeRenderDialog().setOnChangePlayListener(new ChangeRenderDialog.OnChangeRenderListener() {
                    @Override
                    public void onChange() {
                        changeRender();
                        PlayerHelper.init();
                    }
                }).build(mContext).show();
            }
        });
        SettingActivity.callback = new SettingActivity.DevModeCallback() {
            @Override
            public void onChange() {
                findViewById(R.id.llDebug).setVisibility(View.VISIBLE);
                findViewById(R.id.llTest).setVisibility(View.VISIBLE);
            }
        };
    }

    private void changePlay() {
        int playType = Hawk.get(HawkConfig.PLAY_TYPE, 0);
        if (playType == 1) {
            tvPlay.setText("IJK播放器");
        } else if (playType == 2) {
            tvPlay.setText("Exo播放器");
        } else {
            tvPlay.setText("系统播放器");
        }
    }

    private void changeRender() {
        int renderType = Hawk.get(HawkConfig.PLAY_RENDER, 0);
        if (renderType == 0) {
            tvRender.setText("TextureView");
        } else if (renderType == 1) {
            tvRender.setText("SurfaceView");
        } else {
            tvRender.setText("TextureView");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SettingActivity.callback = null;
    }
}