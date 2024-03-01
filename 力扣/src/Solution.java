import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.List;

class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        return dfs(root,p,q).node;
    }
    class result{
        public TreeNode node=null;
        public int fp=0;
        public int fq=0;
    }
    result dfs(TreeNode root,TreeNode p, TreeNode q){
        if(root==null) return new result();
        result res=new result();
        if(root==p){
            res.fp=1;
        }
        if(root==q){
            res.fq=1;
        }
        result rl,rr;
        rl=dfs(root.left,p,q);
        rr=dfs(root.right,p,q);
        res.fq|=rr.fq|rl.fq;
        res.fp|=rr.fp|rl.fp;
        if(res.fq+res.fp==2){
            res.node=root;
        }
        if(rl.node!=null){
            res.node=rl.node;
        }
        if(rr.node!=null){
            res.node=rr.node;
        }
        return res;
    }
    class TreeNode{
        TreeNode left;
        TreeNode right;
    }
    static int[] a=new int[15];
    public static void dfs(int wei){
        if(wei==5){
            for(int i=1;i<=wei;i++){
                System.out.print(a[i]+" ");
            }
            System.out.println();
            return;
        }
        boolean flag;
        for(int i=1;i<=5;i++){
            flag=true;
            for(int j=1;j<=wei;j++){
                if(a[j]==i){
                    flag=false;
                    break;
                }
            }
            if(wei==3&&i==4) flag=false;
            if(flag){
                a[wei+1]=i;
                dfs(wei+1);
            }
        }
    }

    public static void main(String[] args) {

    }
    static List<List<String>> ans=new ArrayList<>(100005);
    char[][] qp=new char[20][20];
    public List<List<String>> solveNQueens(int n) {
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                qp[i][j]='.';
            }
        }
        dfsnq(0,n);
        return ans;
    }
    void dfsnq(int wei,int n){
        if(wei==n){
            List<String> list=new ArrayList<>(n);
            for(int i=0;i<n;i++){
                list.add(new String(qp[i],0,n));
            }
            ans.add(list);
        }
        for(int i=0;i<n;i++){
            boolean flag=true;
            for(int j=0;j<wei;j++){
                if(qp[j][i]=='Q'){
                    flag=false;
                    break;
                }
            }
            for(int j=wei-1,now=i-1;j>=0&&now>=0;j--,now--){
                if(qp[j][now]=='Q'){
                    flag=false;
                    break;
                }
            }
            for(int j=wei-1,now=i+1;now<n&&j>=0;now++,j--){
                if(qp[j][now]=='Q'){
                    flag=false;
                    break;
                }
            }
            if(flag){
                qp[wei][i]='Q';
                dfsnq(wei+1,n);
                qp[wei][i]='.';
            }
        }
    }


}






