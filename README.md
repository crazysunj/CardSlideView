# CardSlideView
一行代码实现ViewPager卡片效果

## 效果

![](https://github.com/crazysunj/crazysunj.github.io/blob/master/img/vp_card5.gif)

## 用法

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

详细介绍戳[这里](http://crazysunj.com/2017/06/25/%E4%B8%80%E8%A1%8C%E4%BB%A3%E7%A0%81%E5%AE%9E%E7%8E%B0ViewPager%E5%8D%A1%E7%89%87%E6%95%88%E6%9E%9C/)

## gradle依赖

```
compile 'com.crazysunj:cardslideview:1.0.0'
```

## 感谢

[android-page-transition](https://github.com/xmuSistone/android-page-transition)

## 传送门

博客:[http://crazysunj.com/](http://crazysunj.com/)

谷歌邮箱:twsunj@gmail.com

QQ邮箱:387953660@qq.com

## License

> ```
> Copyright 2017 Sun Jian
>
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
>    http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
> ```
