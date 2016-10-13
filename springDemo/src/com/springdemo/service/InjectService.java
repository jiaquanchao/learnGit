package com.springdemo.service;

import com.springdemo.dba.InjectDba;

import java.io.File;

/**
 * Created by user on 2016/9/28.
 */
public class InjectService implements InjectServiceImpl {
    private InjectDba injectDba;

    public void setInjectDba(InjectDba injectDba){
        this.injectDba = injectDba;
    }
    @Override
    public void read(String arg) {
        System.out.println("read str :" + arg);
        arg = arg + ":" + this.hashCode();
        injectDba.save(arg);
    }

    @Override
    public void feedBack() {
        System.out.println("文件读取成功...");
    }
}
