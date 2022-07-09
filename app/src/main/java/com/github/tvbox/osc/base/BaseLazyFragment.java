package com.github.tvbox.osc.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.tvbox.osc.callback.EmptyCallback;
import com.github.tvbox.osc.callback.LoadingCallback;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;

import java.util.List;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * Fragment的基类(懒加载)
 */
public abstract class BaseLazyFragment extends Fragment implements CustomAdapt {
    /**
     * Fragment生命周期 onAttach -> onCreate -> onCreatedView -> onActivityCreated
     * -> onStart -> onResume -> onPause -> onStop -> onDestroyView -> onDestroy
     * -> onDetach 对于 ViewPager + Fragment 的实现我们需要关注的几个生命周期有： onCreatedView +
     * onActivityCreated + onResume + onPause + onDestroyView
     */
    protected View rootView = null;

    /**
     * 布局是否创建完成
     */
    protected boolean isViewCreated = false;

    /**
     * 当前可见状态
     */
    protected boolean currentVisibleState = false;

    /**
     * 是否第一次可见
     */
    protected boolean mIsFirstVisible = true;
    protected Context mContext;
    protected Activity mActivity;
    private LoadService mLoadService;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSize.autoConvertDensity(getActivity(), getSizeInDp(), isBaseOnWidth());
        if (null == rootView) {
            rootView = inflater.inflate(getLayoutResID(), container, false);
        }
        isViewCreated = true;
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isHidden() && getUserVisibleHint()) {
            // 可见状态,进行事件分发
            dispatchUserVisibleHint(true);
        }
    }

    /**
     * 修改Fragment的可见性 setUserVisibleHint 被调用有两种情况：
     * 1）在切换tab的时候，会先于所有fragment的其他生命周期，先调用这个函数，可以看log 2)
     * 对于之前已经调用过setUserVisibleHint方法的fragment后，让fragment从可见到不可见之间状态的变化
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // 对于情况1）不予处理，用 isViewCreated 进行判断，如果isViewCreated false，说明它没有被创建
        if (isViewCreated) {
            // 对于情况2,需要分情况考虑,如果是不可见 -> 可见 2.1
            // 如果是可见 -> 不可见 2.2
            // 对于2.1）我们需要如何判断呢？首先必须是可见的（isVisibleToUser
            // 为true）而且只有当可见状态进行改变的时候才需要切换，否则会出现反复调用的情况
            // 从而导致事件分发带来的多次更新
            if (isVisibleToUser && !currentVisibleState) {
                // 从不可见 -> 可见
                dispatchUserVisibleHint(true);
            } else if (!isVisibleToUser && currentVisibleState) {
                dispatchUserVisibleHint(false);
            }
        }
    }

    /**
     * 用FragmentTransaction来控制fragment的hide和show时，
     * 那么这个方法就会被调用。每当你对某个Fragment使用hide 或者是show的时候，那么这个Fragment就会自动调用这个方法。
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 这里的可见返回为false
        if (hidden) {
            dispatchUserVisibleHint(false);
        } else {
            dispatchUserVisibleHint(true);
        }
    }

    /**
     * 统一处理用户可见事件分发
     */
    private void dispatchUserVisibleHint(boolean isVisible) {
        // 首先考虑一下fragment嵌套fragment的情况(只考虑2层嵌套)
        if (isVisible && isParentInvisible()) {
            // 父Fragmnet此时不可见,直接return不做处理
            return;
        }
        // 为了代码严谨,如果当前状态与需要设置的状态本来就一致了,就不处理了
        if (currentVisibleState == isVisible) {
            return;
        }
        currentVisibleState = isVisible;
        if (isVisible) {
            if (mIsFirstVisible) {
                mIsFirstVisible = false;
                // 第一次可见,进行全局初始化
                init();
            }
            onFragmentResume();
            // 分发事件给内嵌的Fragment
            dispatchChildVisibleState(true);
        } else {
            onFragmentPause();
            dispatchChildVisibleState(false);
        }
    }

    /**
     * 在双重ViewPager嵌套的情况下，第一次滑到Frgment 嵌套ViewPager(fragment)的场景的时候
     * 此时只会加载外层Fragment的数据，而不会加载内嵌viewPager中的fragment的数据，因此，我们
     * 需要在此增加一个当外层Fragment可见的时候，分发可见事件给自己内嵌的所有Fragment显示
     */
    private void dispatchChildVisibleState(boolean visible) {
        FragmentManager fragmentManager = getChildFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (null != fragments) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseLazyFragment && !fragment.isHidden() && fragment.getUserVisibleHint()) {
                    ((BaseLazyFragment) fragment).dispatchUserVisibleHint(visible);
                }
            }
        }

    }

    /**
     * Fragment真正的Pause,暂停一切网络耗时操作
     */
    protected void onFragmentPause() {
    }

    /**
     * Fragment真正的Resume,开始处理网络加载等耗时操作
     */
    protected void onFragmentResume() {
    }

    private boolean isParentInvisible() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof BaseLazyFragment) {
            BaseLazyFragment fragment = (BaseLazyFragment) parentFragment;
            return !fragment.isSupportVisible();
        }
        return false;
    }

    private boolean isSupportVisible() {
        return currentVisibleState;
    }

    /**
     * 在滑动或者跳转的过程中，第一次创建fragment的时候均会调用onResume方法
     */
    @Override
    public void onResume() {
        AutoSize.autoConvertDensity(getActivity(), getSizeInDp(), isBaseOnWidth());
        super.onResume();
        // 如果不是第一次可见
        if (!mIsFirstVisible) {
            // 如果此时进行Activity跳转,会将所有的缓存的fragment进行onResume生命周期的重复
            // 只需要对可见的fragment进行加载,
            if (!isHidden() && !currentVisibleState && getUserVisibleHint()) {
                dispatchUserVisibleHint(true);
            }
        }
    }

    /**
     * 只有当当前页面由可见状态转变到不可见状态时才需要调用 dispatchUserVisibleHint currentVisibleState &&
     * getUserVisibleHint() 能够限定是当前可见的 Fragment 当前 Fragment 包含子 Fragment 的时候
     * dispatchUserVisibleHint 内部本身就会通知子 Fragment 不可见 子 fragment 走到这里的时候自身又会调用一遍
     */
    @Override
    public void onPause() {
        super.onPause();
        if (currentVisibleState && getUserVisibleHint()) {
            dispatchUserVisibleHint(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(@IdRes int viewId) {
        View view = null;
        if (rootView != null) {
            view = rootView.findViewById(viewId);
        }
        return (T) view;
    }

    protected abstract int getLayoutResID();

    /**
     * 第一次可见,根据业务进行初始化操作
     */
    protected abstract void init();

    protected void setLoadSir(View view) {
        if (mLoadService == null) {
            mLoadService = LoadSir.getDefault().register(view, new Callback.OnReloadListener() {
                @Override
                public void onReload(View v) {
                }
            });
        }
    }

    protected void showLoading() {
        if (mLoadService != null) {
            mLoadService.showCallback(LoadingCallback.class);
        }
    }

    protected void showEmpty() {
        if (null != mLoadService) {
            mLoadService.showCallback(EmptyCallback.class);
        }
    }

    protected void showSuccess() {
        if (null != mLoadService) {
            mLoadService.showSuccess();
        }
    }

    public void jumpActivity(Class<? extends BaseActivity> clazz) {
        Intent intent = new Intent(mContext, clazz);
        startActivity(intent);
    }

    public void jumpActivity(Class<? extends BaseActivity> clazz, Bundle bundle) {
        Intent intent = new Intent(mContext, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public float getSizeInDp() {
        if (getActivity() != null && getActivity() instanceof CustomAdapt)
            return ((CustomAdapt) getActivity()).getSizeInDp();
        return 0;
    }

    @Override
    public boolean isBaseOnWidth() {
        if (getActivity() != null && getActivity() instanceof CustomAdapt)
            return ((CustomAdapt) getActivity()).isBaseOnWidth();
        return true;
    }


}
