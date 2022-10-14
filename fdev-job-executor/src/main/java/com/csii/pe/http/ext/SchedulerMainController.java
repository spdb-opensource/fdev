//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.csii.pe.http.ext;

import com.csii.pe.channel.http.IdentityResolver;
import com.csii.pe.channel.http.TransactionIdResolver;
import com.csii.pe.channel.http.servlet.ContextResolver;
import com.csii.pe.channel.http.servlet.DefaultExtendedContextResolver;
import com.csii.pe.channel.http.servlet.ExceptionHandler;
import com.csii.pe.channel.http.servlet.ExtendedContextResolver;
import com.csii.pe.channel.http.servlet.ViewExceptionHandler;
import com.csii.pe.channel.http.servlet.ext.MainController;
import com.csii.pe.communication.ws.gsn.GlobalSerialNoObj;
import com.csii.pe.communication.ws.gsn.ThreadLocalTool;
import com.csii.pe.core.Context;
import com.csii.pe.core.CoreController;
import com.csii.pe.core.PeException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

public class SchedulerMainController extends MainController {
    private IdentityResolver idResolver = new TransactionIdResolver();
    private ContextResolver contextResolver = new DefaultExtendedContextResolver();
    private CoreController coreController;
    private ExceptionHandler exceptionHandler = new ViewExceptionHandler();
    private Set<String> streamMIMETypes = new HashSet();
    private String jsonViewName = "";

    public SchedulerMainController() {
        this.streamMIMETypes.add("application/stream");
    }

    public Object process(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse, Locale locale) throws PeException {
        Context context = null;
        if (this.contextResolver instanceof ExtendedContextResolver) {
            context = ((ExtendedContextResolver)this.contextResolver).resolveContext(httpservletrequest, httpservletresponse, locale, this.idResolver, this.streamMIMETypes);
        } else {
            context = this.contextResolver.resolveContext(httpservletrequest, locale, this.idResolver);
        }

        String viewRefer = httpservletrequest.getParameter("_viewReferer");
        if (viewRefer == null) {
            viewRefer = context.getString("_viewReferer");
        }

        httpservletrequest.setAttribute("_viewReferer", viewRefer);
        String globalSeqNo = httpservletrequest.getHeader("SchedulerSeqNo");
        MDC.put("SchedulerSeqNo", globalSeqNo);
        Object returnData = null;
        MDC.put("UniqueId", context.getUser() == null ? "UNKNOW" : context.getUser().getUniqueId());
        MDC.put("IPAddress", "IP:" + (context.getClientInfo() == null ? "UNKNOWN" : context.getClientInfo().getRemoteAddr()));
        GlobalSerialNoObj gso = (GlobalSerialNoObj)ThreadLocalTool.getLocal();
        String jnl_globalseqno = gso == null ? "UNKNOWN" : gso.getGlobalSerialNo();
        MDC.put("Jnl_GlobalSeqNo", jnl_globalseqno);

        try {
            this.preExecute(httpservletrequest, httpservletresponse, context, locale);
            this.coreController.execute(context);
            this.afterExecute(httpservletrequest, httpservletresponse, context, locale);
            String viewName = this.resolveViewName(context);
            httpservletrequest.setAttribute("_viewReferer", viewName);
            returnData = context.getDataMap();
        } catch (Exception var10) {
            returnData = this.exceptionHandler.process(this.getApplicationContext(), httpservletrequest, httpservletresponse, locale, context, var10);
        }

        return returnData;
    }

    protected String resolveViewName(Context context) {
        return "rest,";
    }

    public void setIdResolver(IdentityResolver idResolver) {
        this.idResolver = idResolver;
    }

    public void setContextResolver(ContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    public void setCoreController(CoreController coreController) {
        this.coreController = coreController;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void setStreamMIMETypes(Set set) {
        if (set != null) {
            this.streamMIMETypes.addAll(set);
        }

    }

    public void setJsonViewName(String jsonViewName) {
        this.jsonViewName = jsonViewName;
    }
}
