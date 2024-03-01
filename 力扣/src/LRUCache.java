import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import static java.lang.Math.min;

public class LRUCache {

    public LRUCache() {

    }

    class node{
        node pre;
        node next;
        public int val;
        Integer key;
        node(){
            pre=null;
            next=null;
            val=-1;
        }
        node(int key,int val){
            pre=null;
            next=null;
            this.val=val;
            this.key=key;
        }
    }
    HashMap<Integer,node> map=new HashMap<Integer,node>();
    int capacity=0;
    int size=0;
    public LRUCache(int capacity) {
        this.capacity=capacity;
        head.next=tail;
        tail.pre=head;

    }
    node head=new node(),tail=new node();
    public int get(int key) {
        node node = map.get(key);
        if(node==null) return -1;
        node.pre.next=node.next;
        node.next.pre=node.pre;
        tail.pre.next=node;
        node.next=tail;
        node.pre=tail.pre;
        tail.pre=node;
        return node.val;
    }
    
    public void put(int key, int value) {
        node o = map.get(key);
        if(o==null){
            if(size==capacity){
                node nowhead=head.next;
                map.remove(nowhead.key);
                head.next=nowhead.next;
                nowhead.next.pre=head;
                node n=new node(key,value);
                map.put(key,n);
                tail.pre.next=n;
                n.pre=tail.pre;
                n.next=tail;
                tail.pre=n;
            }else {
                node n=new node(key,value);
                map.put(key,n);
                tail.pre.next=n;
                n.pre=tail.pre;
                n.next=tail;
                tail.pre=n;
                size++;
            }
        }else {
            o.pre.next=o.next;
            o.next.pre=o.pre;
            tail.pre.next=o;
            o.pre=tail.pre;
            o.next=tail;
            tail.pre=o;
            o.val=value;

        }
    }
    static boolean[][] dp=new boolean[20][20];
    static boolean dfs(String a,String b,int la,int lb){
        if(!dp[la][lb]) return false;
        if(la==a.length()&&lb==b.length()) return true;
        if(lb==b.length()) return dp[la][lb]=false;
        if(la==a.length()){
            if(b.charAt(lb)=='*') {
               return dp[la][lb]=dfs(a,b,la,lb+1);
            }
            if(lb+1<b.length()&&b.charAt(lb+1)=='*')
                return dp[la][lb]=dfs(a,b,la,lb+2);

            return dp[la][lb]=false;
        }
        if(b.charAt(lb)=='*'){
            if(b.charAt(lb-1)=='.'){
                if(dfs(a,b,la+1,lb)) return true;
                if(dfs(a,b,la+1,lb+1)) return true;
            }else {
                if(a.charAt(la)==b.charAt(lb-1)){
                    if(dfs(a,b,la+1,lb)) return true;
                    if(dfs(a,b,la+1,lb+1)) return true;
                }
            }
            if(dfs(a,b,la,lb+1)) return true;
        }
        if(b.charAt(lb)=='.'){
            if(dfs(a,b,la+1,lb+1)) return true;
        }
        if(a.charAt(la)==b.charAt(lb)){
            if(dfs(a,b,la+1,lb+1)) return true;
        }
        if(lb+1<b.length()&&b.charAt(lb+1)=='*'){
            if(dfs(a,b,la,lb+2)) return true;
        }
        return dp[la][lb]=false;
    }

    public static void main(String[] args) {
        int[] ints = new LRUCache().maxNumber(new int[]{3,4,6,5}, new int[]{9,1,2,5,8,3}, 5);
        System.out.println(Arrays.toString(ints));
    }
    char[] stack=new char[30];
    int top=-1;
    int[] cnt=new int[28];
    boolean[] isat=new boolean[28];
    public String removeDuplicateLetters(String s) {
        StringBuilder sb=new StringBuilder();
        char[] chars = s.toCharArray();
        for(int i=0;i<chars.length;i++){
            cnt[chars[i]-'a']++;
        }
        for(int i=0;i<isat.length;i++) isat[i]=false;
        for(int i=0;i<chars.length;i++){
            int wei=chars[i]-'a';
            cnt[wei]--;
            if(!isat[wei]){
                while (top>=0&&cnt[stack[top]-'a']>0&&stack[top]>chars[i]){
                    isat[stack[top]-'a']=false;
                    top--;
                }
                stack[++top]=chars[i];
                isat[stack[top]-'a']=true;
            }
        }
        sb.append(stack,0,top+1);
        return sb.toString();
    }
    public int[] maxNumber(int[] nums1, int[] nums2, int k) {
        int len1=nums1.length,len2=nums2.length;
        int[] s1=new int[len1],s2=new int[len2];int top1=0,top2=0;
        int[] maxint=new int[k];
        int[] tmpans=new int[k];
        for(int i=len1;i>=0;i--){
            int j=k-i;
            top1=-1;top2=-1;
            if(j>len2) break;
            if(j<0) continue;
            for(int k1=0;k1<nums1.length;k1++){
                if(top1+1+nums1.length-k1<=i){
                    s1[++top1]=nums1[k1];
                }else {
                    while (top1>=0&&s1[top1]<nums1[k1]&&(top1+1+nums1.length-k1>i)) top1--;
                    if(top1+1>=i) continue;
                    else{
                        s1[++top1]=nums1[k1];
                    }
                }
            }
            for(int k1=0;k1<nums2.length;k1++){
                if(top2+1+nums2.length-k1<=j){
                    s2[++top2]=nums2[k1];
                }else {
                    while (top2>=0&&s2[top2]<nums2[k1]&&(top2+1+nums2.length-k1>j)) top2--;
                    if(top2+1>=j) continue;
                    else{
                        s2[++top2]=nums2[k1];
                    }
                }
            }
            //int[] tmpans=new int[k];
            int pos1=0,pos2=0,ansnow=-1;
            while (pos1<=top1&&pos2<=top2){
                if(!compare(s1,s2,pos1,pos2)){
                    tmpans[++ansnow]=s2[pos2];
                    pos2++;
                }else {
                    tmpans[++ansnow]=s1[pos1];
                    pos1++;
                }
            }
                while (pos1<=top1) tmpans[++ansnow]=s1[pos1++];
                while (pos2<=top2) tmpans[++ansnow]=s2[pos2++];
            //System.out.println(Arrays.toString(tmpans));
            if(compare(tmpans,maxint,0,0)){
                System.arraycopy(tmpans,0,maxint,0,k);
            }
        }
        return maxint;
    }

    private boolean compare(int[] tmpans, int[] maxint,int k1,int k2) {

        if(maxint==null) return true;
        for(;k1<tmpans.length&&k2<maxint.length;k1++,k2++){
            if(tmpans[k1]>maxint[k2]) return true;
            if (tmpans[k1]<maxint[k2]) return false;
        }

        return (tmpans.length-k1)>(maxint.length-k2);
    }
}

