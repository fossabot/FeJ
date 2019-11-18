package top.viewv.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Plan {
    public int plan_id;
    public java.sql.Date start_time;
    public java.sql.Date end_time;
    public String planner_id;
    public HashMap<Integer,Integer>[] Pro;


    public void SetPlan(HashMap[] mess, java.sql.Date start_time, java.sql.Date end_time, int Aid, Connection conn){
        try{
            this.Pro = mess;
            this.start_time = start_time;
            this.end_time = end_time;

            String sql;
            PreparedStatement st;
            ResultSet rs;
            sql = "select client_id from client where account_id = " +  Aid;
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            rs.next();
            this.planner_id = rs.getString(1);
            sql = "select count(order_id) from `order`";
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            rs.next();
            this.plan_id = rs.getInt(1) + 1;


            sql = "insert into plan values(" +
                    plan_id + "," +
                    start_time + " 00:00:00','" +
                    end_time + " 00:00:00','" +
                    planner_id + ")";
            st.execute(sql);


            for (int i = 0; i < mess.length; ++i){
                Iterator iter = mess[i].entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry entry = (Map.Entry)iter.next();
                    int x = Integer.parseInt(entry.getKey().toString());
                    int y = Integer.parseInt(entry.getValue().toString());
                    sql = "insert into plan_content(plan_id,workshop_id,product_id,amount)" +
                            " values(" + plan_id + "," + i + "," + x + "," + y + ")";
                    st.execute(sql);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void InitPlanById(int plan_id, Connection conn){
        try{
            String sql = "select * from `plan` where plan_id = " + plan_id;
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            rs.next();
            this.plan_id = rs.getInt(1);
            this.start_time = rs.getDate(2);
            this.end_time = rs.getDate(3);
            this.planner_id = rs.getString(4);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public int[][] InitProductById(int plan_id,Connection conn){
        try{
            String sql = "select count(*) from `plan_content` where plan_id = " + plan_id;
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            rs.next();
            int row = rs.getInt(1);
            int[][] Content = new int[row][3];

            sql = "select product_id,amount,workshop_id " +
                    "from `plan_content` where plan_id = " + plan_id;
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            int cnt = 0;
            while(rs.next()){
                for(int i = 0; i < 2; ++i){
                    Content[cnt][i] = rs.getInt(i + 1);
                    //0->产品编号 1->产品数量 2->车间号
                }
            }
            return Content;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

