## 2.x

```
CardSlideView<MyBean> slideView = (CardSlideView) findViewById(R.id.slide_view);
slideView.bind(list, new MyCardHolder());
```


```
static class MyCardHolder implements CardHolder<MyBean> {

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
            return inflater.inflate(R.layout.item, container, false);
        }

        @Override
        public void onBindView(@NonNull CardViewHolder holder, MyBean data, int position) {
            Log.e("MainActivity", "onBindView---data:" + data + "position:" + position);
            ImageView imageView = holder.getView(R.id.image);
            final String img = data.getImg();
            Glide.with(imageView.getContext()).load(img).apply(new RequestOptions().dontAnimate()).into(imageView);
            holder.itemView.setOnClickListener(v -> {
                Log.e("MainActivity", "setOnClickListener---data:" + data + "position:" + position);
                TestActivity.start(v.getContext(), img);
            });
        }
    }
```

```
// 获取当前选中下标
int currentItem = slideView.getCurrentItem();
// 获取当前的方向
int orientation = slideView.getOrientation();
// 设置变换效果，默认是缩放的，最低0.8，实现PageTransformer接口即可
slideView.setItemTransformer(new MyScale());

static class MyScale implements PageTransformer {

    @Override
    public void transformPage(@NonNull View view, float offsetPercent, int orientation) {
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
    }
}
// 设置卡片的滑动效果，默认是page，类似于viewPager，库里除了提供这个效果，还提供了快速线性滑动
slideView.setSnapHelper(new CardLinearSnapHelper());
// 设置是否无限循环
slideView.setLooper(true);
// 设置page滑动监听，监听方式跟viewPager类似，在惯性滑动时不会回调
slideView.setOnPageChangeListener(new MyPageChangeListener());
```

## gradle依赖

```
implementation 'com.crazysunj:cardslideview:2.0.0'
同时还需要依赖自己的v4包和recyclerview包，androidx哦
```

**喜大普奔，无需再实现序列化，底层采用RecyclerView，无限循环，变换和布局等都是通过自定义layoutManager实现的，原理我抽空会写一篇讲解文章，并不是很难。**


