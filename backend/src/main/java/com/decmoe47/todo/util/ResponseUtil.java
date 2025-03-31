package com.decmoe47.todo.util;

import cn.hutool.json.JSONUtil;
import com.decmoe47.todo.constant.enums.ErrorCodeEnum;
import com.decmoe47.todo.model.vo.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseUtil {

    public static void writeErrMsg(HttpServletResponse response, ErrorCodeEnum errCode) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (PrintWriter writer = response.getWriter()) {
            String jsonResponse = JSONUtil.toJsonStr(R.error(errCode));
            writer.print(jsonResponse);
            writer.flush();
        } catch (IOException e) {
            log.error("Failed to write error message to response", e);
        }
    }
}
