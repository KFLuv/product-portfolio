package com.iflytek.chatgpt.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import util.MySQLUtil;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 行程本：把 AI 规划好的行程存下来、随时翻看、不要了就删
 * <p>
 * 所有接口都以 session 里的登录用户为准，别人的行程查不到也删不掉。
 */
@RestController
public class TripController {

    private static final Logger log = LoggerFactory.getLogger(TripController.class);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 保存一条行程
     */
    @PostMapping(value = "/trips/save", produces = "application/json;charset=UTF-8")
    public Map<String, Object> save(@RequestParam(required = false) String title,
                                    @RequestParam(required = false) String content,
                                    HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        String username = ChatController.currentUser(session);
        if (username == null) {
            return fail(result, "请先登录后再使用行程本");
        }
        if (content == null || content.trim().isEmpty()) {
            return fail(result, "行程内容为空，无法保存");
        }

        String safeTitle = title == null || title.trim().isEmpty() ? "未命名行程" : title.trim();
        if (safeTitle.length() > 120) {
            safeTitle = safeTitle.substring(0, 120);
        }

        MySQLUtil.executeUpdate(
                "insert into trip(username, title, content, create_time) values(?, ?, ?, now())",
                username, safeTitle, content);

        result.put("status", "success");
        result.put("message", "已保存到行程本");
        return result;
    }

    /**
     * 当前用户的行程列表（最新的排前面）
     */
    @PostMapping(value = "/trips/list", produces = "application/json;charset=UTF-8")
    public Map<String, Object> list(HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        String username = ChatController.currentUser(session);
        if (username == null) {
            return fail(result, "请先登录后再使用行程本");
        }

        List<Map<String, Object>> trips = new ArrayList<>();
        String sql = "select id, title, create_time from trip where username = ? order by id desc";

        try (Connection conn = MySQLUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> trip = new LinkedHashMap<>();
                    trip.put("id", rs.getInt("id"));
                    trip.put("title", rs.getString("title"));
                    trip.put("createTime", rs.getTimestamp("create_time").toLocalDateTime().format(TIME_FORMAT));
                    trips.add(trip);
                }
            }
        } catch (SQLException e) {
            log.error("查询行程列表失败", e);
            return fail(result, "行程本读取失败，请稍后重试");
        }

        result.put("status", "success");
        result.put("trips", trips);
        return result;
    }

    /**
     * 查看某条行程的完整内容
     */
    @PostMapping(value = "/trips/get", produces = "application/json;charset=UTF-8")
    public Map<String, Object> get(@RequestParam(defaultValue = "0") int id, HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        String username = ChatController.currentUser(session);
        if (username == null) {
            return fail(result, "请先登录后再使用行程本");
        }

        String sql = "select id, title, content, create_time from trip where id = ? and username = ?";

        try (Connection conn = MySQLUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            // ★ 同时限定 username：就算前端伪造 id，也查不到别人的行程
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> trip = new LinkedHashMap<>();
                    trip.put("id", rs.getInt("id"));
                    trip.put("title", rs.getString("title"));
                    trip.put("content", rs.getString("content"));
                    trip.put("createTime", rs.getTimestamp("create_time").toLocalDateTime().format(TIME_FORMAT));
                    result.put("status", "success");
                    result.put("trip", trip);
                    return result;
                }
            }
        } catch (SQLException e) {
            log.error("读取行程失败", e);
            return fail(result, "行程读取失败，请稍后重试");
        }
        return fail(result, "行程不存在");
    }

    /**
     * 删除某条行程
     */
    @PostMapping(value = "/trips/delete", produces = "application/json;charset=UTF-8")
    public Map<String, Object> delete(@RequestParam(defaultValue = "0") int id, HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        String username = ChatController.currentUser(session);
        if (username == null) {
            return fail(result, "请先登录后再使用行程本");
        }

        int rows = MySQLUtil.executeUpdate("delete from trip where id = ? and username = ?", id, username);
        if (rows <= 0) {
            return fail(result, "行程不存在或已被删除");
        }

        result.put("status", "success");
        result.put("message", "已删除");
        return result;
    }

    private Map<String, Object> fail(Map<String, Object> result, String message) {
        result.put("status", "error");
        result.put("message", message);
        return result;
    }
}
