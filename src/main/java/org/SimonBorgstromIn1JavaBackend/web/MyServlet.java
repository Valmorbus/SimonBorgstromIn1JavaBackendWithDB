package org.SimonBorgstromIn1JavaBackend.web;

import javax.servlet.http.HttpServlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.sql.DataSource;

@WebServlet(name = "MyServlet", urlPatterns = {"/myservlet", "/index"})
public class MyServlet extends HttpServlet {

    @Resource(lookup = "jdbc/TodoDB2")
    DataSource ds;
    //test 

    public static final String DESCRIPTION = "description";
    public static final String DUEDATE = "duedate";
    public static final String DONE = "done";
    public static final String KEY = "key";
    public HashMap<Integer, TodoItem> saveMap = new HashMap<Integer, TodoItem>();
    private int count = 0;

    private Connection connect = null;
    private PreparedStatement pstm = null;
    private ResultSet rset = null;
    private Statement stm = null;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String description = req.getParameter(DESCRIPTION);
        String duedate = req.getParameter(DUEDATE);
        String doneString = req.getParameter(DONE);
        String keyString = req.getParameter(KEY);
        System.out.println("connected");
        if (keyString == null) {
            TodoItem ti = new TodoItem(description, duedate, doneString);
            System.out.println("donestring " + doneString);
            try {
                connect = ds.getConnection();
                PreparedStatement pstm = connect.prepareStatement("insert into Todo(description, duedate, done) "
                        + "values(?, ?, 'false')");
                pstm.setString(1, description);
                pstm.setString(2, duedate);
                System.out.println("commited to db " + pstm.execute());
                pstm.close();
                connect.close();
            } catch (SQLException ex) {
                Logger.getLogger(MyServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            // System.out.println(count);
        } else if (keyString != null) {
            if (duedate != null) {
                try {
                    System.out.println(keyString);
                    connect = ds.getConnection();
                    pstm = connect.prepareStatement("update Todo set duedate = ? where id=?");
                    pstm.setString(1, duedate);
                    pstm.setInt(2, (Integer.valueOf(keyString)+1));
                    pstm.executeUpdate();
                    pstm.close();
                    connect.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MyServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (doneString != null) {
               try {
                    connect = ds.getConnection();
                    pstm = connect.prepareStatement("update Todo set done = ? where id=?");
                    pstm.setString(1, "checked");
                    pstm.setInt(2, (Integer.valueOf(keyString)+1));
                    pstm.executeUpdate();
                     pstm.close();
                     connect.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MyServlet.class.getName()).log(Level.SEVERE, null, ex);
                }

           }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //isDone(doneString, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        System.out.print("get");
        try {
            //resp.sendRedirect("");
            connect = ds.getConnection();
            stm =connect.createStatement();
            ResultSet rs = stm.executeQuery("select * from todo");
            while(rs.next()){
                TodoItem td = new TodoItem(rs.getString("description"), rs.getString("duedate"), rs.getString("done"));
                saveMap.put(rs.getInt("id"),td);
                count++;
            }
                    
        } catch (SQLException ex) {
            Logger.getLogger(MyServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        String objectToReturn = "[";
        for (Map.Entry<Integer, TodoItem> entry : saveMap.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue().toJson();
            objectToReturn += value;
            if (count != key) {
                objectToReturn += ",";
            }
        }
        objectToReturn += "]";
        System.out.println(objectToReturn);
        PrintWriter out = resp.getWriter();
        out.print(objectToReturn);
        out.close();

    }
}
