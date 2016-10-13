package com.springdemo.dba;

import com.springdemo.service.InjectService;

/**
 * Created by user on 2016/9/28.
 */
public class InjectDba implements InjectDbaImpl{
    @Override
    public void save(String arg) {
        System.out.println("保存 " + arg);
    }

    @Override
    public void say() {
        System.out.println("数据库存储完毕...");
    }
}
