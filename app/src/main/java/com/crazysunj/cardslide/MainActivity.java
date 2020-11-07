package com.crazysunj.cardslide;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.crazysunj.cardslideview.BitmapHelper;
import com.crazysunj.cardslideview.CardHolder;
import com.crazysunj.cardslideview.CardViewHolder;
import com.crazysunj.cardslideview.DefaultTransformer;
import com.crazysunj.cardslideview.OnPageChangeListener;
import com.crazysunj.cardslideview.OnPageItemClickListener;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String[] imageArray = {
//            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3213173117,1110903080&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1781594915,1366698269&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=1891652328,4280900176&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3117198308,3734342397&fm=26&gp=0.jpg",
//            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=1817262769,1722663763&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2226962572,1331736450&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=2185637402,3767956099&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3193260445,3308495828&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=4233973082,2791353980&fm=26&gp=0.jpg",
//            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3044237662,1661917652&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2459592104,964883207&fm=26&gp=0.jpg",
//            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=4126378158,4237107889&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1955835928,3080371141&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2243141774,3186970935&fm=26&gp=0.jpg",
//            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3103859432,2822357524&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3430323032,501802113&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=4257076965,4140046292&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=120988605,2488551742&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1370761971,3887646162&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2713856587,1798489161&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=982226408,3400592817&fm=26&gp=0.jpg",
//            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2908505488,43474043&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3268359587,3340996830&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1938366643,1751177362&fm=26&gp=0.jpg",
//            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2483415675,2226885028&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2714053634,2951928462&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2980990027,259361045&fm=26&gp=0.jpg",
//            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=800274749,3560269987&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2158332137,3914260445&fm=26&gp=0.jpg",
//            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=934278520,495630521&fm=26&gp=0.jpg",
//            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=1804686124,2817435486&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=10380796,1038230589&fm=26&gp=0.jpg",
//            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=326844657,168862954&fm=26&gp=0.jpg",
            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=559280242,3629937094&fm=26&gp=0.jpg",
            "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=42234963,3359794470&fm=26&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2366521412,4187387997&fm=26&gp=0.jpg",
            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=1673698395,2662990695&fm=26&gp=0.jpg",
            "https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1077412268,1486449152&fm=26&gp=0.jpg",
            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2774420294,2604280244&fm=26&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3982795986,3289528383&fm=26&gp=0.jpg",
            "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3210647450,3365972530&fm=26&gp=0.jpg",
            "https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1152354189,4075004834&fm=26&gp=0.jpg",
            "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=1241938828,3177192306&fm=26&gp=0.jpg"};
    private ImageView mainBG;
    private BannerView<MyBean> bannerView;
    private List<MyBean> list;
    private TextView count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bannerView = findViewById(R.id.banner_view);
        count = findViewById(R.id.count);
        mainBG = (ImageView) findViewById(R.id.main_bg);
        list = new ArrayList<MyBean>();
        for (String s : imageArray) {
            list.add(new MyBean(s));
        }
//        bannerView.setInterval(5000);
        bannerView.setOnPageChangeListener(new MyPageChangeListener());
        bannerView.setOnPageItemClickListener(new MyPageItemClickListener());
        onClick2(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bannerView.stop();
    }

    static class MyPageItemClickListener implements OnPageItemClickListener<MyBean>{

        @Override
        public void onItemClick(View view, MyBean data, int position) {
            TestActivity.start(view.getContext(), data.getImg());
        }
    }

    class MyPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
//            MyBean data = list.get(position);
//            String img = data.getImg();
//            Glide.with(MainActivity.this)
//                    .load(img)
//                    .apply(new RequestOptions()
//                            .transform(new BlurTransformation(5)))
//                    .into(new SimpleTarget<Drawable>() {
//                        @Override
//                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                            mainBG.setImageDrawable(resource);
//                        }
//                    });
            count.setText("第" + position + "个");
        }
    }

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

    static class ReflectionCardHolder implements CardHolder<MyBean> {

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
            return inflater.inflate(R.layout.item, container, false);
        }

        @Override
        public void onBindView(@NonNull CardViewHolder holder, MyBean data, int position) {
            ImageView imageView = holder.getView(R.id.image);
            final String img = data.getImg();
            Glide.with(imageView.getContext()).asBitmap().load(img).apply(new RequestOptions().dontAnimate().transform(new Reflection()).placeholder(R.drawable.placeholder_reflection))
                    .into(imageView);
        }
    }

    public void onClick1(View view) {
        bannerView.setItemRate(0.7f);
        bannerView.setItemTransformer(new DefaultTransformer());
        bannerView.bind(list, new NormalCardHolder(), true);
    }

    public void onClick2(View view) {
        bannerView.setItemRate(1.4f);
        bannerView.setItemTransformer(new ScaleTransformer());
        bannerView.bind(list, new ReflectionCardHolder(), true);
    }

    public void onClick3(View view) {
        int index = new Random().nextInt(list.size());
        bannerView.setCurrentItem(index, true);
    }

    public static class Reflection extends BitmapTransformation {
        private static final String ID = "com.crazysunj.cardslide.Reflection";
        private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

        @Override
        protected Bitmap transform(
                @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            if (toTransform.getWidth() == outWidth && toTransform.getHeight() == outHeight) {
                return toTransform;
            }
            return BitmapHelper.convertReflection(toTransform, outWidth, outHeight);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Reflection;
        }

        @Override
        public int hashCode() {
            return ID.hashCode();
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            messageDigest.update(ID_BYTES);
        }
    }
}
