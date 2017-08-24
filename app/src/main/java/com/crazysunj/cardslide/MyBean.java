package com.crazysunj.cardslide;

import java.io.Serializable;

/**
 * author: sunjian
 * created on: 2017/8/24 下午3:14
 * description:
 */

public class MyBean implements Serializable {
    private long id;
    private String img;

    public MyBean(String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    @Override
    public String toString() {
        return "MyBean{" +
                "id=" + id +
                ", img='" + img + '\'' +
                '}';
    }
}
