package com.dianping.pigeon.console.servlet.json;

import com.dianping.pigeon.monitor.trace.ApplicationTraceData;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.common.monitor.trace.TraceStatsCollector;

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

    private final JacksonSerializer jacksonSerializer = new JacksonSerializer();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationTraceData traceStatsData = TraceStatsCollector.getStatsData();
        String traceStatsJson = jacksonSerializer.serializeObject(traceStatsData);
        response.getWriter().print(traceStatsJson);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
