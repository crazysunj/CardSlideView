## 2.x

### 实例
```
CardSlideView<MyBean> slideView = (CardSlideView) findViewById(R.id.slide_view);
slideView.bind(list, new NormalCardHolder());
```


```
static class NormalCardHolder implements CardHolder<MyBean> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.item, container, false);
    }

    @Override
    public void onBindView(@NonNull CardViewHolder holder, MyBean data, int position) {
        ImageView imageView = holder.getView(R.id.image);
        final String img = data.getImg();
        Glide.with(imageView.getContext()).load(img).apply(new RequestOptions().dontAnimate().placeholder(new ColorDrawable(Color.WHITE))).into(imageView);
    }
}
```

### 方法简介
```
// 获取当前选中下标，滑动过程中并不会改动
int currentItem = slideView.getCurrentItem();
// 获取当前距离中心最近的view，这个是实时的
int centerView = slideView.getCenterView();
// 获取当前的方向
int orientation = slideView.getOrientation();
// 设置变换效果，默认是缩放+透明度，最低0.8，实现PageTransformer接口即可
slideView.setItemTransformer(new DefaultTransformer());

public class DefaultTransformer implements PageTransformer {

    public DefaultTransformer() {
    }

    @Override
    public void transformPage(@NonNull View view, float offsetPercent, int orientation) {
        if (orientation == LinearLayout.HORIZONTAL) {
            if (offsetPercent > 0) {
                view.setPivotX(view.getWidth());
                view.setPivotY(view.getHeight() / 2.f);
            } else {
                view.setPivotX(0);
                view.setPivotY(view.getHeight() / 2.f);
            }
        } else {
            if (offsetPercent > 0) {
                view.setPivotX(view.getWidth() / 2.f);
                view.setPivotY(view.getHeight());
            } else {
                view.setPivotX(view.getWidth() / 2.f);
                view.setPivotY(0);
            }
        }
        final float finalPercent = 1 - Math.min(Math.abs(offsetPercent), 2.f) / 2.f;
        float scale = 0.8f + 0.2f * finalPercent;
        view.setScaleX(scale);
        view.setScaleY(scale);
        final float alpha = (float) Math.pow(finalPercent, 0.8);
        view.setAlpha(alpha);
    }
}
// 设置是否无限循环
slideView.setLooper(true);
// 设置page滑动监听，监听方式跟viewPager类似，在滑动时不会回调
slideView.setOnPageChangeListener(new MyPageChangeListener());

public interface OnPageChangeListener {
    void onPageSelected(int position);
}

// 支持RecyclerView的ItemDecoration
public RecyclerView.ItemDecoration getItemDecorationAt(int index);

// 获取绑定的数据
public List<T> getData()；

// 获取总item数
public int getItemCount();

// 获取对应下标ItemDecoration
public RecyclerView.ItemDecoration getItemDecorationAt(int index);

// 获取ItemDecoration已设置总数
public int getItemDecorationCount();

// 获取对应view在列表中的具体下标
public int getPosition(@NonNull View view);

// 移除对应ItemDecoration
public void removeItemDecoration(@NonNull RecyclerView.ItemDecoration decor);
public void removeItemDecorationAt(int index);

// 设置横竖方向是否可滑动
public void setCanScrollHorizontally(boolean canScrollHorizontally);
public void setCanScrollVertically(boolean canScrollVertically);

// 切到对应下标，smoothScroll为true带动画
public void setCurrentItem(int item, boolean smoothScroll);

// 设置每个item的宽高比
public void setItemRate(float itemRate);

// 设置中间item距离两边空隙的距离，为item宽度的百分比
public void setSideOffsetPercent(float sideOffsetPercent);

// 设置item点击事件，如果点击非中间item，那么点击item将会移动到中间
slideView.setOnPageItemClickListener(new MyPageItemClickListener());

public interface OnPageItemClickListener<T> {
    void onItemClick(View view, T data, int position);
}

#BitmapHelper
// 支持bitmap倒影转换，建议放在子线程
public static Bitmap convertReflection(@NonNull Bitmap originalImage, int viewWidth, int viewHeight) ;
```

### 属性简介
```
// item间距百分比，以item宽度为基准，范围-1~1
<attr name="card_item_margin_percent" format="float" />
// 是否支持无线循环
<attr name="card_loop" format="boolean" />
// 是否支持回弹效果，只在非无限循环下生效
<attr name="card_rebound" format="boolean" />
// 中间item两边留边距离百分比，以item宽度为基准，范围0-1
<attr name="card_side_offset_percent" format="float" />
// item的宽高比
<attr name="card_item_rate" format="float" />
// 滑动模式，page-类似ViewPager，linear-类似平时RecyclerView(有作滑动优化)
<attr name="card_page_mode" format="enum">
    <enum name="linear" value="0" />
    <enum name="page" value="1" />
</attr>
```

## gradle依赖

```
implementation 'com.crazysunj:cardslideview:2.2.0'
同时还需要依赖自己的v4包和recyclerview包，androidx哦
```

**喜大普奔，无需再实现序列化，底层采用RecyclerView，无限循环，变换和布局等都是通过自定义layoutManager实现的，原理我抽空会写一篇讲解文章，并不是很难。**


