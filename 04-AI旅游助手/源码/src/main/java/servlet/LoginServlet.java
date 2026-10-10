package servlet;

import util.MySQLUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 处理登录逻辑
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * 登录
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 解决中文乱码
        req.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=utf-8");

        // 接收登录的账号和密码
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // 校验账号和密码是否为空
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            resp.getWriter().write("用户名和密码不能为空");
            return;
        }

        // 判断是否勾选记住密码，自动登录选项

        // 验证用户名和密码
        boolean flag = doLogin(username, password);

        if (flag) {
            String remember = req.getParameter("remember");
            String auto = req.getParameter("auto");
            if ("on".equals(remember) || "on".equals(auto)) {
                String encodeUsername = URLEncoder.encode(username, "UTF-8");
                String encodePassword = URLEncoder.encode(password, "UTF-8");
                Cookie usernameCookie = new Cookie("username", encodeUsername);
                Cookie passwordCookie = new Cookie("password", encodePassword);
                Cookie autoLoginCookie = new Cookie("autoLogin", "true");
                usernameCookie.setMaxAge(60);//60秒
                passwordCookie.setMaxAge(60);//60秒
                autoLoginCookie.setMaxAge(60);//60秒
                resp.addCookie(usernameCookie);
                resp.addCookie(passwordCookie);
                resp.addCookie(autoLoginCookie);
            } else {//否则清除cookie
                Cookie[] cookies = req.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("username") || cookie.equals("password") || cookie.getName().equals("autoLogin")) {
                            cookie.setMaxAge(0);//0即为清除
                            resp.addCookie(cookie);
                        }
                    }
                }
            }
        }


        if (flag) {
            // 登录成功，重定向到首页
            resp.sendRedirect("chat.html");
        } else {
            // 登录失败
            resp.getWriter().write("用户名或密码错误");
        }

    }

    /**
     * 校验账号和密码是否正确，正确返回true，否则返回false
     * @param username
     * @param password
     * @return
     */
    private boolean doLogin(String username,String password) {
        try {
            Connection connection = MySQLUtil.getConnection();
            // 2、根据连接对象获取sql 执行对象
            Statement statement = connection.createStatement();
            // 3、定义sql 语句,查询用户名和密码是否正确
            String sql = "select * from user where username='"+username+"' and password='"+password+"'";
            // 4、执行sql语句
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) { // 账号和密码存在
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