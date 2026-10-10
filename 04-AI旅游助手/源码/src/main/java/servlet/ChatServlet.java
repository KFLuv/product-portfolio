package servlet;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "ChatServlet", urlPatterns = {"/chat/Aichat", "/chat/myinfo", "/chat/updateUserInfo"})
public class ChatServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        switch(path) {
            case "/chat/Aichat":
                handleAiChat(request, response);
                break;
            case "/chat/myinfo":
                handleMyInfo(request, response);
                break;
            case "/chat/updateUserInfo":
                handleUpdateUserInfo(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAiChat(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 获取用户消息
        String userMessage = request.getReader().readLine();

        // 这里应该是调用AI服务的逻辑
        // 模拟AI回复
        String aiResponse = "感谢您的咨询！关于" + userMessage + "，我建议您可以考虑以下行程：\n\n" +
                "1. 第一天：参观当地著名景点\n" +
                "2. 第二天：体验当地美食和文化\n" +
                "3. 第三天：自由活动或购物\n\n" +
                "如需更详细的行程规划，请提供更多具体信息。";

        // 设置响应
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(aiResponse);
    }

    private void handleMyInfo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 这里应该从数据库或会话中获取用户信息
        // 模拟返回用户数据
        String jsonResponse = "{\"name\":\"旅行助手\",\"email\":\"demo@example.com\"}";

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    private void handleUpdateUserInfo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 获取表单数据
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String newEmail = request.getParameter("newEmail");
        String username = request.getParameter("username");

        // 验证密码是否匹配
        if (!newPassword.equals(confirmPassword)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"新密码和确认密码不匹配\"}");
            return;
        }

        // 这里应该有更复杂的验证逻辑和数据库更新
        // 模拟成功响应
        String jsonResponse = "{\"status\":\"success\",\"message\":\"用户信息更新成功\"}";

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}