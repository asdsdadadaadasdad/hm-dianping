package com.hmdp.utils;

public interface ilock {
    boolean trylock();
    void unlock();
}
