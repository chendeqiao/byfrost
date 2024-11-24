package com.intelligence.browser.ui.adapter;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.OnLongClickListener;
import static android.view.View.VISIBLE;
import static com.intelligence.browser.data.RecommendUrlEntity.WEIGHT_BOOKMARK_WEBSITE;
import static com.intelligence.browser.data.RecommendUrlEntity.WEIGHT_HOT_WEBSITE;
import static com.intelligence.browser.data.RecommendUrlEntity.WEIGHT_RECOMMEND_WEBSITE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.controller.BackgroundHandler;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.database.DatabaseManager;
import com.intelligence.browser.R;
import com.intelligence.browser.data.RecommendUrlEntity;
import com.intelligence.browser.ui.home.clone.CloneableRelativeLayout;
import com.intelligence.browser.utils.ColorUtils;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.RedSystemControll;
import com.intelligence.browser.view.RoundImageView;
import com.intelligence.browser.ui.widget.AnimationListener;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.componentlib.badge.BadgeView;
import com.intelligence.componentlib.particle.Factory.ExplodeParticleFactory;
import com.intelligence.componentlib.particle.Main.ExplosionSite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.CommonUrlItemViewHolder>
        implements DatabaseManager.DataChangeListener<RecommendUrlEntity> {
    private static final int ANIMATION_TIME = 300;
    private static final int TITLE_ANIMATION_DELAY = 100;
    private static final int RECOVERY_DELAY = 300;
    private static final float START_VALUE = 0f;
    private static final float MIDDLE_VALUE = 0.5f;
    private static final float END_VALUE = 1f;

    private static final int TYPE_NAV = 1;
    private static final int TYPE_ADD = 2;
    private static final int TYPE_SPACE_GRAY = 3;
    private static final int TYPE_SPACE_HIDE = 4;

    private Handler mHandler = new Handler();
    private Context mContext;
    private ItemTouchHelper mItemTouchHelper;
    private List<RecommendUrlEntity> mCommonUrlBeans;
    private SparseArray<CommonUrlItemViewHolder> mViewHolderSet = new SparseArray<>();
    private AdapterItemListener mAdapterItemListener;
    private boolean mIsEdit = false;
    private boolean mIsDeleteMode = false;
    private int mMaxCount;
    private boolean mIsMove;
    private boolean mIncognito;

    public RecommendAdapter(Context context, ItemTouchHelper itemTouchHelper,boolean isEdit) {
        mCommonUrlBeans = new ArrayList<>();
        mIsEdit = isEdit;
        mContext = context;
        mMaxCount = context.getResources().getInteger(R.integer.recommend_item_max_count) * (mIsEdit ? 2 : 1);
        mItemTouchHelper = itemTouchHelper;
    }


    public void setIsEdit(boolean mIsEdit) {
        this.mIsEdit = mIsEdit;
        this.notifyDataSetChanged();
    }

    public boolean isEdit() {
        return mIsEdit;
    }

    public boolean isIncognito() {
        return mIncognito;
    }

    public void setIncognito(boolean incognito) {
        this.mIncognito = incognito;
        this.notifyDataSetChanged();
    }

    public void registerListener(AdapterItemListener adapterItemListener) {
        this.mAdapterItemListener = adapterItemListener;
    }

    public void setDeleteMode(boolean deleteMode) {
        if (deleteMode == mIsDeleteMode) return;
        this.mIsDeleteMode = deleteMode;
        notifyDataSetChanged();
        if (mAdapterItemListener == null) return;
        if (mIsDeleteMode) {
            mAdapterItemListener.onDeleteMode();
        } else {
            mAdapterItemListener.offDeleteMode();
        }
    }

    public boolean isDeleteMode() {
        return mIsDeleteMode;
    }

    public boolean isFull() {
        return mCommonUrlBeans.size() == mMaxCount;
    }

    public List<RecommendUrlEntity> getData() {
        return mCommonUrlBeans;
    }

    public RecommendUrlEntity getData(int position) {
        if(position >= 0 && position<mCommonUrlBeans.size()) {
            return mCommonUrlBeans.get(position);
        }
        return null;
    }

    public void replaceData(List<RecommendUrlEntity> list) {
        mCommonUrlBeans.clear();
        if (list != null) {
            if (list.size() <= mMaxCount) {
                mCommonUrlBeans.addAll(list);
            } else {
                for (int i = 0; i < mMaxCount; i++) {
                    mCommonUrlBeans.add(list.get(i));
                }
            }
        }
        notifyDataSetChanged();
    }


    public void insert(RecommendUrlEntity entity) {
//        if (!insetData(entity)) {
//            return;
//        }
        int insertPosition = mCommonUrlBeans.size();
        if (insertPosition < mMaxCount) {
            mCommonUrlBeans.add(insertPosition, entity);
            startInsertAnimation(insertPosition);
        }
    }

    private boolean insetData(RecommendUrlEntity entity) {
        if (CollectionUtils.isEmpty(mCommonUrlBeans)) {
            return true;
        }
        return false;
    }

    public void modify(int position) {
        if (position < 0 || position >= mCommonUrlBeans.size()) {
            return;
        }
        notifyItemChanged(position);
        if (mAdapterItemListener != null) {
            mAdapterItemListener.onDataSetChange();
        }
    }

    private void delete(int position) {
        int navCount = mCommonUrlBeans.size();
        if (position < 0 || position >= navCount) {
            return;
        }
        mCommonUrlBeans.remove(position);
        if (mIsEdit) {
            startDeleteAnimation(position);
        } else {
            explosionAnimation(position);
        }
    }

    private void explosionAnimation(int position){
        final CommonUrlItemViewHolder viewHolder = mViewHolderSet.get(position);
        View itemView = viewHolder.itemView;
        ExplosionSite explosionSite = new ExplosionSite(itemView.getContext(), new ExplodeParticleFactory());
        explosionSite.explode(itemView, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                DatabaseManager.getInstance().deleteById(RecommendUrlEntity.class, ((RecommendUrlEntity)viewHolder.mClose.getTag()).getId());
                int lastIndex = mCommonUrlBeans.size();
                if (position != lastIndex) {
                    notifyItemMoved(position, lastIndex);
                }
                BackgroundHandler.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        viewHolder.mItemIcon.clearAnimation();
//                        notifyDataSetChanged();
                        if (mAdapterItemListener != null) {
                            mAdapterItemListener.onDataSetChange();
                        }
                    }
                }, RECOVERY_DELAY);
                onBindGraySpaceViewHolder(viewHolder);
            }
        });
    }


    private int getPositionByEntity(RecommendUrlEntity entity) {
        return mCommonUrlBeans.indexOf(entity);
    }

    public CommonUrlItemViewHolder getViewHolderByPosition(int position) {
        return mViewHolderSet.get(position);
    }

    @Override
    public void onInsertToDB(RecommendUrlEntity entity) {
        if (!isDeleteMode()) insert(entity);
    }

    @Override
    public void onUpdateToDB(RecommendUrlEntity entity) {
        if (!isDeleteMode()) modify(getPositionByEntity(entity));
    }

    @Override
    public void onDeleteToDB(RecommendUrlEntity entity) {
        Log.e("===fafd","onAnimaf========7");
        if (!isDeleteMode()) delete(getPositionByEntity(entity));
    }

    private void startInsertAnimation(final int position) {
        final CommonUrlItemViewHolder viewHolder = mViewHolderSet.get(position);
        if (viewHolder == null) return;
        onBindNavigationViewHolder(viewHolder, position);
        ScaleAnimation scaleAnimation = new ScaleAnimation(START_VALUE, END_VALUE, START_VALUE, END_VALUE,
                Animation.RELATIVE_TO_SELF, MIDDLE_VALUE, Animation.RELATIVE_TO_SELF, MIDDLE_VALUE);
        scaleAnimation.setDuration(ANIMATION_TIME);
        scaleAnimation.setStartOffset(TITLE_ANIMATION_DELAY);
        scaleAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                viewHolder.mItemIcon.clearAnimation();
                if (mAdapterItemListener != null) {
                    mAdapterItemListener.onDataSetChange();
                }
                notifyDataSetChanged();
            }
        });
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                float output;
                //分段函数 x <= 0.8时 y = 1.5x  x>0.8 时 y = 2 - x;
                if (input <= 0.8f)
                    output = 1.5f * input;
                else
                    output = 2f - input;
                viewHolder.mItemIcon.setAlpha(output);
                return output;
            }
        });
        viewHolder.mItemIcon.startAnimation(scaleAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(ANIMATION_TIME);
        alphaAnimation.setFillAfter(true);
        viewHolder.mItemTitle.startAnimation(alphaAnimation);
    }

    private void startDeleteAnimation(final int position) {
        final CommonUrlItemViewHolder viewHolder = mViewHolderSet.get(position);
        if (viewHolder == null) return;
        //变化范围从0到1,参考点为中心点
        final ScaleAnimation scaleAnimation = new ScaleAnimation(START_VALUE, END_VALUE, START_VALUE, END_VALUE,
                Animation.RELATIVE_TO_SELF, MIDDLE_VALUE, Animation.RELATIVE_TO_SELF, MIDDLE_VALUE);
        scaleAnimation.setDuration(ANIMATION_TIME);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                float output;
                //分段函数 x >= 0.2时 y = 1.5 - 1.5x  x < 0.2时 y = x + 1;
                if (input >= 0.2f)
                    output = 1.5f - (input * 1.5f);
                else
                    output = input + 1f;
                viewHolder.mItemIcon.setAlpha(output);
                return output;
            }
        });
        viewHolder.mItemIcon.startAnimation(scaleAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(ANIMATION_TIME);
        alphaAnimation.setStartOffset(TITLE_ANIMATION_DELAY);
        alphaAnimation.setFillAfter(true);
        viewHolder.mItemTitle.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                DatabaseManager.getInstance().deleteById(RecommendUrlEntity.class, ((RecommendUrlEntity)viewHolder.mClose.getTag()).getId());
                int lastIndex = mCommonUrlBeans.size();
                if (position != lastIndex) {
                    notifyItemMoved(position, lastIndex);
                }
                BackgroundHandler.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.mItemIcon.setAlpha(1f);
                        viewHolder.mItemIcon.clearAnimation();
//                        notifyDataSetChanged();
                        if (mAdapterItemListener != null) {
                            mAdapterItemListener.onDataSetChange();
                        }
                    }
                }, RECOVERY_DELAY);
                if (isEdit()) {
                    onBindHideSpaceViewHolder(viewHolder);
                } else {
                    onBindGraySpaceViewHolder(viewHolder);
                }
            }
        });
    }

    @Override
    public CommonUrlItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (mIsEdit) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browser_edit_recommend_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browser_homepage_recommend_item, parent, false);
        }
        return new CommonUrlItemViewHolder(view);
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        int size = mCommonUrlBeans.size();
        if (fromPosition >= size || toPosition >= size) return false;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mCommonUrlBeans, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mCommonUrlBeans, i, i - 1);
            }
        }
        mIsMove = true;
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void fireSortChangeIfNeed() {
        if (!mIsMove) return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int count = mCommonUrlBeans.size();
                int size = count;
                for (int i = 0; i < count; i++) {
                    RecommendUrlEntity entity = mCommonUrlBeans.get(i);
                    size--;
                    entity.setOrd(size);
                    entity.updateToDb();
                }
                mIsMove = false;
            }
        });
    }

    @Override
    public void onBindViewHolder(final CommonUrlItemViewHolder holder, int position) {
        mViewHolderSet.put(position, holder);
        switch (getItemType(position)) {
            case TYPE_NAV:
                onBindNavigationViewHolder(holder, position);
                break;
            case TYPE_SPACE_GRAY:
                onBindGraySpaceViewHolder(holder);
                break;
            case TYPE_ADD:
                onBindAddViewHolder(holder);
                break;
            case TYPE_SPACE_HIDE:
                onBindHideSpaceViewHolder(holder);
                break;
            default:
                //do nothing
        }
    }

    public int getItemType(int position) {
        int navSize = mCommonUrlBeans.size();
        if (position < navSize) {
            return TYPE_NAV;
        } else if (!isEdit()) {
            if (!isDeleteMode() && position == navSize)
                return TYPE_ADD;
            else
                return TYPE_SPACE_HIDE;
        } else {
            return TYPE_SPACE_GRAY;
        }
    }

    private void onBindNavigationViewHolder(final CommonUrlItemViewHolder holder, final int position) {
        holder.mItemIcon.setAlpha(1f);
        holder.itemView.setAlpha(1f);
        holder.itemView.clearAnimation();
        holder.mItemIcon.clearAnimation();
        final RecommendUrlEntity bean = mCommonUrlBeans.get(position);
        holder.mItemTitle.setText(bean.getDisplayName());
        holder.position = position;
        holder.mItemIcon.setVisibility(VISIBLE);
        holder.data = bean;
        holder.mItemIcon.setImageResource(0);
        holder.mItemIcon.setBackgroundResource(0);
        holder.mItemIcon.setBackground(null);
        holder.mItemIcon.setRoundBg(Color.TRANSPARENT);
        holder.mItemIcon.setBackgroundBg(Color.TRANSPARENT);
        if (bean.getWeight() <= WEIGHT_RECOMMEND_WEBSITE) {
            int resID = holder.mItemTitle.getContext().getResources().getIdentifier(bean.getImageUrl(),
                    "drawable", holder.mItemTitle.getContext().getPackageName());
            holder.mItemIcon.setImageResource(resID);
        } else {
            if (bean.getImageIcon() != null && bean.getImageIcon().length > 0) {
                Bitmap iconBmp = ImageUtils.getBitmap(bean.getImageIcon(), null);
                if (iconBmp != null) {
                    if (bean.getWeight() == WEIGHT_HOT_WEBSITE || bean.getWeight() == WEIGHT_BOOKMARK_WEBSITE) {
                        holder.mItemIcon.setImageBitmap(iconBmp);
                    } else {
                        holder.mItemIcon.setIcon(bean.getUrl(), iconBmp);
                    }
                } else {
                    holder.mItemIcon.setDefaultIconByUrl(bean.getUrl());
                }
            } else {
//                holder.mItemIcon.setImageBitmap(null);
//                holder.mItemIcon.setBackground(null);
                holder.mItemIcon.setDefaultIconByUrl(bean.getUrl());
            }
        }
        if (mIsEdit || mIncognito) {
            holder.mClose.setImageResource(R.drawable.browser_delete_homepage_light);
            holder.mItemTitle.setTextColor(ColorUtils.getColor(R.color.grid_common_text_color));
        } else {
            holder.mClose.setImageResource(R.drawable.browser_delete_navigation_dark);
            holder.mItemTitle.setTextColor(ColorUtils.getColor(R.color.grid_common_text_color));
        }
        boolean isNavigation = "yunxin://browser/news?goods=1".equals(bean.getUrl());
        if (!mIsEdit) {
            if (position == 0 && isNavigation) {
                long time = (long) SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_NEWS_TODAY, 0l);
                if (!mIsDeleteMode && !DateUtils.isToday(time) && BrowserSettings.getInstance().getRedPointNotifiction()) {
                    holder.mBadgeView.setVisibility(VISIBLE);
                    RedSystemControll.sIsShowNavigationRed = true;
                    holder.mBadgeView.showTextBadge("new", true);
                } else {
                    RedSystemControll.sIsShowNavigationRed = false;
                    holder.mBadgeView.setVisibility(GONE);
                }
            } else if (position == 1) {
                RelativeLayout.LayoutParams layoutParams = ((RelativeLayout.LayoutParams) holder.mBadgeView.getLayoutParams());
                layoutParams.setMargins(0, ScreenUtils.dpToPxInt(mContext, 8), ScreenUtils.dpToPxInt(mContext, 6), 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.mBadgeView.setLayoutParams(layoutParams);
            } else {
                holder.mBadgeView.setVisibility(GONE);
            }
        } else {
            holder.mBadgeView.setVisibility(GONE);
        }

        holder.mClose.setTag(bean);
        holder.mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendUrlEntity entity = (RecommendUrlEntity) v.getTag();
                int position = findPositionByBean(entity);
                if (position != -1) {
                    delete(position);
                    v.setVisibility(GONE);
                }
                if (mCommonUrlBeans.size() == 0) {
                    setDeleteMode(false);
                }
            }
        });
        holder.mItemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapterItemListener != null && !mIsDeleteMode && holder != null) {
                    if (!mIsEdit) {
                        mAdapterItemListener.openUrl(holder);
                        if (isNavigation && holder.mBadgeView != null) {
                            holder.mBadgeView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (holder != null && holder.mBadgeView != null) {
                                        holder.mBadgeView.setVisibility(GONE);
                                    }
                                }
                            }, 100);
                        }
                    } else {
                        if (holder.position < mCommonUrlBeans.size()) {
                            mAdapterItemListener.editNavigation(holder);
                        }
                    }
                }
            }
        });
        //长按编辑
        holder.mItemView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mIsEdit) {
                    if (holder.position < mCommonUrlBeans.size()) {
                        mAdapterItemListener.editNavigation(holder);
                    }
                    return false;
                }
                if (!isEdit() && position < mCommonUrlBeans.size()) setDeleteMode(true);
                return false;
            }
        });
        holder.mItemIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isDeleteMode() && MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                    mItemTouchHelper.startDrag(holder);
                }
                return false;
            }
        });
        if (mIsDeleteMode && !isNavigation) {
            holder.mClose.setVisibility(VISIBLE);
        } else {
            holder.mClose.setVisibility(GONE);
        }
    }

    private void onBindGraySpaceViewHolder(CommonUrlItemViewHolder holder) {
        holder.mItemIcon.setVisibility(VISIBLE);
        holder.mClose.setVisibility(GONE);
        holder.mItemTitle.setText("");
        holder.mItemIcon.setImageResource(R.drawable.browser_navigation_place_icon);
        holder.mItemView.setOnClickListener(null);
    }

    private void onBindHideSpaceViewHolder(CommonUrlItemViewHolder holder) {
        holder.mItemIcon.setVisibility(INVISIBLE);
        holder.mClose.setVisibility(GONE);
        holder.mItemTitle.setText("");
        holder.mItemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setDeleteMode(false);
                if (mAdapterItemListener != null) {
                    mAdapterItemListener.offDeleteMode();
                }
            }
        });
    }

    private void onBindAddViewHolder(final CommonUrlItemViewHolder holder) {
        holder.mItemIcon.setBackgroundResource(0);
        holder.mItemIcon.setBackground(null);
        holder.mItemIcon.setRoundBg(Color.TRANSPARENT);
        holder.mItemIcon.setBackgroundBg(Color.TRANSPARENT);
        holder.mItemIcon.setVisibility(VISIBLE);
        int versioncode = (int) SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_EDIT, 0);
        if (!RedSystemControll.isCanShowAddRecommendRed() || versioncode == DeviceInfoUtils.getAppVersionCode(mContext)) {
            holder.mBadgeView.setVisibility(GONE);
            RedSystemControll.sIsShowAddRecommendRed = false;
        } else {
            holder.mBadgeView.showTextBadge("new", true);
            holder.mBadgeView.setVisibility(VISIBLE);
            RedSystemControll.sIsShowAddRecommendRed = true;
        }
        holder.mItemIcon.setImageResource(R.drawable.browser_recommend_add);
        holder.mClose.setVisibility(GONE);
        holder.mItemTitle.setText(holder.mItemTitle.getContext().getResources().getString(R.string.add));
        holder.mItemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapterItemListener != null && !mIsDeleteMode)
                    mAdapterItemListener.onClickAdd(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMaxCount;
    }

    public int getVisibleCount() {
        if (isEdit()) {
            return mMaxCount;
        }
        int dataSize = mCommonUrlBeans.size();
        if (mIsDeleteMode) {
            return dataSize;
        } else {
            return dataSize + 1;
        }
    }

    public class CommonUrlItemViewHolder extends RecyclerView.ViewHolder {

        private TextView mItemTitle;
        private RoundImageView mItemIcon;
        private ImageView mClose;
        private BadgeView mBadgeView;
        public CloneableRelativeLayout mItemView;
        public int position;
        public RecommendUrlEntity data;

        CommonUrlItemViewHolder(View itemView) {
            super(itemView);
            mItemView = (CloneableRelativeLayout) itemView;
            mItemIcon = itemView.findViewById(R.id.recommend_item_icon);
            mItemTitle = itemView.findViewById(R.id.recommend_item_title);
            mClose = itemView.findViewById(R.id.recommend_item_close);
            mBadgeView = itemView.findViewById(R.id.recommend_item_badgeview);
            mItemIcon.setRoundBg(ColorUtils.getColor(R.color.pop_item_press_color));
        }
    }

    private int findPositionByBean(RecommendUrlEntity entity) {
        int size = mCommonUrlBeans.size();
        for (int i = 0; i < size; i++) {
            if (mCommonUrlBeans.get(i) == entity) {
                return i;
            }
        }
        return -1;
    }

    public interface AdapterItemListener {

        void openUrl(CommonUrlItemViewHolder viewHolder);

        void editNavigation(CommonUrlItemViewHolder viewHolder);

        void onClickAdd(CommonUrlItemViewHolder viewHolder);

        void onDataSetChange();

        void onDeleteMode();

        void offDeleteMode();

    }

}
