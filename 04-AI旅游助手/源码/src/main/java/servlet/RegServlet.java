package servlet;

import util.MySQLUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet("/reg") // 后端映射地址
public class RegServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        req.setCharacterEncoding("utf-8"); // 解决中文参数乱码
        resp.setContentType("text/html;charset=utf-8"); // 解决响应的中文乱码

        String username = req.getParameter("username"); // 参数名等于input标签name 的属性值
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        System.out.println("用户名：" + username);
        System.out.println("密码：" + password);
        System.out.println("邮箱：" + email);

        // 校验账号是否存在，存在的话不允许注册
        boolean flag = checkUserName(username);
        if (flag) {
            // 响应
            resp.getWriter().write("账号存在，请重新换个账号注册！！");
            return; // 程序执行到这里就返回了，不往下执行
        }

        // 将用户信息存储到数据库
        // 1、获取数据库连接对象Connection
        try {
            Connection connection = MySQLUtil.getConnection();
            // 2、根据连接对象获取sql 执行对象
            Statement statement = connection.createStatement();
            // 3、定义sql 语句
            String sql = "insert into user(username,password,email) values('"+username+"','"+password+"','"+email+"')";
            // 4、执行sql语句
            statement.executeUpdate(sql);
            // 5、关闭资源。先开后关
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 响应
        //resp.getWriter().write("成功");
        // 重定向到登录页面
        resp.sendRedirect("login.html");
    }

    /**
     * 校验用户名是否存在，存在返回true,不存在返回false
     * @param username
     * @return
     */
    private boolean checkUserName(String username) {
        try {
            Connection connection = MySQLUtil.getConnection();
            // 2、根据连接对象获取sql 执行对象
            Statement statement = connection.createStatement();
            // 3、定义sql 语句
            String sql = "select * from user where username='"+username+"'";
            // 4、执行sql语句
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) { // 账号存在
                return true;
            }
            // 5、关闭资源。先开后关
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
