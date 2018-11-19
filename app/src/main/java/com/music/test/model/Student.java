package com.music.test.model;

import javax.inject.Inject;

public class Student {

    @Inject
    public Student(){

    }

    public String say(){
        return "jack";
    }
}

/**
 * app\build\generated\source\apt\debug\...\Student_Factory.java
 *
 * 原来我们通过@Inject注解了一个类的构造方法后，可以让编译器帮助我们产生一个对应的Factory类，
 * 通过这个工厂类我们可以通过简单的get()方法获取到Student对象！
 */
