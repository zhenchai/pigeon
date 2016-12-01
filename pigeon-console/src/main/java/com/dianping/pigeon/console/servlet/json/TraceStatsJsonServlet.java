package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.remoting.common.monitor.trace.ApplicationTraceRepository;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.monitor.trace.MonitorDataFactory;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author qi.yin
 *         2016/11/16  下午3:46.
 */
public class TraceStatsJsonServlet extends HttpServlet {

    private static final ObjectMapper mapper = new ObjectMapper();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationTraceRepository traceData = MonitorDataFactory.getTraceData();

        ApplicationTraceRepository old = traceData.copy();
        traceData.reset();

        String traceDataJson = mapper.writeValueAsString(old);
        response.getWriter().print(traceDataJson);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
