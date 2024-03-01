import java.util.Scanner;

import static java.lang.Math.max;


class Solution {
    public int lengthOfLongestSubstring(String s) {
        int len=s.length();
        int ans=1;
        for(int i=1;i<len;i++){
            int j=i;
            int tmp=1;
            int[] a=new int[28];
            while(j>=0){
                if(a[s.charAt(j)-'a']==1){
                    break;
                }
                tmp++;
                a[s.charAt(j)-'a']=1;
                j--;
            }
            ans=max(ans,tmp);
        }
        return ans;
    }
}