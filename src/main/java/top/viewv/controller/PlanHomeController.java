package top.viewv.controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSnackbar;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import top.viewv.api.Gravatar;
import top.viewv.api.Serialize;
import top.viewv.database.Connect;
import top.viewv.model.Plan;
import top.viewv.model.PlanInfo;
import top.viewv.model.Product;
import top.viewv.model.Tables.ProductTable;
import top.viewv.model.Tables.RecipeTable;
import top.viewv.view.StageManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author oXCToo
 */
// !Thank you very much! oXCToo!

public class PlanHomeController implements Initializable {

    public ImageView userIcon;
    public JFXButton btnExit;
    public AnchorPane BasePane;
    public Label labUserName;
    public Label userId;
    public JFXButton btnCheckAll;
    public JFXDatePicker jdpStartTime;
    public JFXDatePicker jdpDueTime;
    public JFXButton btnClean;
    Connection conn = new Connect().getConnection();

    private ArrayList<PlanInfo> planInfos = new ArrayList<PlanInfo>();
    @FXML
    private VBox pnl_scroll;

    public PlanHomeController() throws IOException, ClassNotFoundException {
    }

    public void setCorlVis(boolean cond){
        btnClean.setVisible(cond);
        btnCheckAll.setVisible(cond);
        jdpStartTime.setVisible(cond);
        jdpDueTime.setVisible(cond);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            setCorlVis(false);
            Serialize.ser(planInfos, "plan.ser");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setlabUserId(String userid) {
        userId.setText(userid);
    }

    public void setLabUserName(String userName) {
        labUserName.setText(userName);
    }

    public void refreshNodes() throws Exception {
        System.out.println("Start Refresh Node");
        pnl_scroll.getChildren().clear();
        setCorlVis(false);

        ProductTable pt = new ProductTable();
        pt.GetLength(conn);
        pt.GetContent(conn);

        RecipeTable rt = new RecipeTable();

        int length = pt.Plength;

        Node[] nodes = new Node[length];
        Node node;

        for (int i = 0; i < length; i++) {
            try {
                FXMLLoader loader = new
                        FXMLLoader(Objects.requireNonNull(getClass()).getClassLoader()
                        .getResource("data/ui/PlanProItem.fxml"));
                node = loader.load();
                //调用下面的函数可以得到控制器
                //商品部分
                PlanProItemController planProItemController = loader.getController();
                planProItemController.setLabPrice(pt.Ptable[i].product_price);
                planProItemController.setLabProductName(pt.Ptable[i].product_name);
                planProItemController.setLabProductId(pt.Ptable[i].product_id);
                planProItemController.setLabProductRtime(pt.Ptable[i].product_period);
                //配方介绍部分
                rt.GetAll(pt.Ptable[i].product_id, conn);
                planProItemController.setLabDescrption(pt.Ptable[i].description);
                nodes[i] = node;
                pnl_scroll.getChildren().add(nodes[i]);
                //删除所有节点，有点残忍，还是隐藏比较好
                //pnl_scroll.getChildren().removeAll();
            } catch (IOException ex) {
                Logger.getLogger(PlanHomeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setUserIcon(String email) {
        userIcon.setImage(Gravatar.imageFromMail(email));
    }

    public void onClickedbtnExit() throws IOException {
        MainController secondControl = (MainController) StageManager.CONTROLLER.get("index");
        secondControl.setTranDataToIndex("第三个窗口的数据");
        //如果本窗口还使用该控制器先不remove这个控制器;
        //StageManager.CONTROLLER.remove("secondControl");

        Stage stage = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("data/ui/Main.fxml")));
        stage.setTitle("Index");
        stage.setScene(new Scene(root));
        stage.show();
        //将第二个窗口保存到map中
        StageManager.STAGE.put("second", stage);
        //将本窗口保存到map中
        StageManager.CONTROLLER.put("index", this);
        //关闭本窗口
        Stage index = (Stage) btnExit.getScene().getWindow();
        index.close();
    }

    public void onClickedbtnShopList(MouseEvent mouseEvent) throws Exception {
        setCorlVis(true);
        refeshShopList();
    }

    public void refeshShopList() throws Exception {
        pnl_scroll.getChildren().clear();
        planInfos = Serialize.planInfodser("plan.ser");
        int length = planInfos.size();

        PlanInfo planInfo;

        Node[] nodes = new Node[length];
        Node node;
        int i ;
        for (i = 0;i < length;i++){
            planInfo = planInfos.get(i);
            try {
                FXMLLoader loader = new
                        FXMLLoader(Objects.requireNonNull(
                        Thread.currentThread().
                                getContextClassLoader().
                                getResource("data/ui/PlanListItem.fxml")));
                node = loader.load();
                PlanListItemController itemController = loader.getController();
                itemController.setLabAmount(planInfo.getAmount());
                itemController.setLabProductId(planInfo.getProduct_id());
                itemController.setLabProductName(planInfo.getProduct_name());
                itemController.setLabLine(planInfo.getLine_id());
                nodes[i] = node;
                pnl_scroll.getChildren().add(nodes[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void onClickedbtnShowAllProduct(MouseEvent mouseEvent) throws Exception {
        refreshNodes();
    }

    public void onClickedbtnCheckAll(MouseEvent mouseEvent)  {
        try {
            Plan plan = new Plan();
            planInfos = Serialize.planInfodser("plan.ser");
            PlanInfo planInfo;
            int length = planInfos.size();
            int[][] mess = new int[length][3];
            for(int i = 0;i<length;i++){
                planInfo = planInfos.get(i);
                mess[i][0] = planInfo.getProduct_id();
                mess[i][1] = planInfo.getAmount();
                mess[i][2] = planInfo.getLine_id();
            }
            LocalDate start = jdpStartTime.getValue();
            LocalDate due = jdpDueTime.getValue();
            Date startdate = Date.valueOf(start);
            Date duedate = Date.valueOf(due);
            plan.SetPlan(mess,startdate,duedate,userId.getText(),conn);

            JFXSnackbar snackbar = new JFXSnackbar(BasePane);
            snackbar.show("Send Order Successfully", 1000);
            planInfos.clear();
            Serialize.ser(planInfos,"plan.ser");
            refeshShopList();
        } catch (Exception e) {
            e.printStackTrace();
            JFXSnackbar snackbar = new JFXSnackbar(BasePane);
            snackbar.show("Send Order Failed", 1000);
        }
    }

    public void onClickedbtnClean(MouseEvent mouseEvent) throws Exception {
        planInfos = Serialize.planInfodser("plan.ser");
        planInfos.clear();
        Serialize.ser(planInfos,"plan.ser");
        refeshShopList();
    }
}
