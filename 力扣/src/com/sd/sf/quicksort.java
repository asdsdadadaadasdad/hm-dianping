package com.sd.sf;


import java.util.*;

import static java.util.Collections.swap;

public class quicksort implements Comparator<quicksort>{

    static void maopao(int a[],int len){
        for(int i=0;i<len-1;i++){
            for(int j=0;j<len-i-1;j++){
                if(a[j]>a[j+1]){
                    int tmp=a[j+1];
                    a[j+1]=a[j];
                    a[j]=tmp;
                }
            }
        }
    }
    static void charu(int a[],int len){
        for(int i=1;i<=len-1;i++){
            int j=i;
            int k=a[i];
            while (j>0&&k<a[j-1]){
                a[j]=a[j-1];
                j--;
            }
            a[j]=k;
        }
    }
    static void print(int a[],int len){
        for(int i=0;i<len;i++){
            System.out.print(a[i]+" ");
        }
    }
    static int[] quicksort(int a[],int left,int right){
        if(left>=right) return a;
        int l=left,r=right;
        int sw=new Random().nextInt(right-left+1)+left;
        int tmp=a[sw];
        a[sw]=a[l];
        a[l]=tmp;
        int base=a[l];
        while(l<r){
            while (a[r]>=base&&l<r) r--;
            while (a[l]<=base&&l<r) l++;
            if(l<r){
                tmp=a[l];
                a[l]=a[r];
                a[r]=tmp;
            }
        }
        a[left]=a[l];
        a[l]=base;
        quicksort(a,left,l-1);
        quicksort(a,l+1,right);
        return a;
    }
    public static void main(String[] args) {
        System.out.println(Math.pow(2,5));
        quicksort[] q=new quicksort[100];
        for(int i=0;i<q.length;i++) q[i]=new quicksort();
        Arrays.sort(q);

    }
    int h=5;

    public int compareTo(quicksort o) {
        if(this.h==o.h) return 0;
        else if(this.h<o.h) return -1;
        return 1;
    }

    @Override
    public int compare(quicksort o1, quicksort o2) {
        if(o1.h==o2.h) return 0;
        else if(o1.h<o2.h) return -1;
        return 1;
    }
}

