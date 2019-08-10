## 1.x

```
CardViewPager viewPager = (CardViewPager) findViewById(R.id.viewpager);
viewPager.bind(getSupportFragmentManager(), new MyCardHandler(), Arrays.asList(imageArray));
```


```
public class MyCardHandler implements CardHandler<String> {

    @Override
    public View onBind(final Context context, final String data, final int position) {
        View view = View.inflate(context, R.layout.item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        Glide.with(context).load(data).into(imageView);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "data:" + data + "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
```

```
//正常与卡片效果切换切换，请设置合理的值
private void switchNormal() {
    viewPager.setCardTransformer(0, 0);
    viewPager.setCardPadding(0);
    viewPager.setCardMargin(0);
    viewPager.notifyUI(CardViewPager.MODE_NORMAL);
}

private void switchCard() {
    viewPager.setCardTransformer(180, 0.38f);
    viewPager.setCardPadding(60);
    viewPager.setCardMargin(40);
    viewPager.notifyUI(CardViewPager.MODE_CARD);
}
```

```
// 获取当前下标
public int getCurrentIndex();

// 原setCurrentItem请用setCurrentIndex替代

// addOnPageChangeListener和setOnPageChangeListener回调position对应data，但data不能进行增删操作，如果要进行增删，请重新bind
```

详细介绍戳[这里](http://crazysunj.com/2017/06/25/%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81%E5%AE%9E%E7%8E%B0ViewPager%E5%8D%A1%E7%89%87%E6%95%88%E6%9E%9C/)

具体参考demo

**注意事项，实现Serializable的类其内嵌类也必须实现Serializable**

```
public class A {
    B b;
    public static class B{}
}
```
如上，A和B都必须实现Serializable，如果不想序列化，可以在变量前面添加transient关键字，但是有小概率事件发生Activity被系统杀死重启异常问题，大家尽量不要提到成员变量处

## gradle依赖

```
implementation 'com.crazysunj:cardslideview:1.4.2'
同时还需要依赖自己的v4包和cardview包
```