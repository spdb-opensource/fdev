package com.csii.pe.channel.http.servlet.ext;

import com.csii.pe.channel.http.Constants;
import com.csii.pe.channel.http.servlet.Controller;
import com.csii.pe.channel.http.servlet.CsiiView;
import com.csii.pe.channel.http.servlet.CsiiViewResolver;
import com.csii.pe.core.LoggingInterceptor;
import com.csii.pe.core.PeRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * 2018/07/03 17:09:22
 * <p>
 * 优化自基础版本PE10 MainServlet0,优化如下内容：
 * 1.解决国际化Local解析不准确
 * @author huangyao
 * @version v1.0
 * @since v1.0
 */
public class MainServlet0 extends HttpServlet implements ApplicationContextAware {
    protected static String[] DEFAULT_ALLOWED_LOCALES = new String[]{"en_US", "zh_CN", "zh_TW"};
    protected Log logger = LogFactory.getLog(this.getClass());
    private boolean publishContext = true;
    public static final String SERVLET_CONTEXT_PREFIX = MainServlet0.class.getName() + ".CONTEXT.";
    private String namespace;
    private MultipartResolver multipartResolver;
    private LocaleResolver localeResolver;
    private Set allowedLocales = new HashSet();
    private Controller controller;
    private CsiiViewResolver viewResolver;
    private String localeParamName = "_locale";
    private boolean synchronizeOnSession = false;
    private ApplicationContext applicationContext;

    public MainServlet0() {
    }

    public final void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        if (config.getInitParameter("localeParamName") != null) {
            this.localeParamName = config.getInitParameter("localeParamName");
        }

        String[] locales = null;
        if (config.getInitParameter("allowedLocales") != null) {
            locales = StringUtils.tokenizeToStringArray(config.getInitParameter("allowedLocales"), ",");
        } else {
            locales = DEFAULT_ALLOWED_LOCALES;
        }

        this.allowedLocales.addAll(Arrays.asList(locales));
        if (config.getInitParameter("synchronizeOnSession") != null) {
            this.synchronizeOnSession = Boolean.valueOf(config.getInitParameter("synchronizeOnSession"));
        }

    }

    private void initMultipartResolver() throws BeansException {
        try {
            this.multipartResolver = (MultipartResolver) this.applicationContext.getBean("multipartResolver");
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Using MultipartResolver [" + this.multipartResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException var2) {
            this.multipartResolver = null;
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Unable to locate MultipartResolver with name [multipartResolver]: no multipart handling provided");
            }
        }

    }

    private void initLocaleResolver() throws BeansException {
        try {
            this.localeResolver = (LocaleResolver) this.applicationContext.getBean("localeResolver");
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Using LocaleResolver [" + this.localeResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException var2) {
            this.localeResolver = new AcceptHeaderLocaleResolver();
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Unable to locate LocaleResolver with name 'localeResolver': using default [" + this.localeResolver + "]");
            }
        }

    }

    private void initController() throws BeansException {
        try {
            this.controller = (Controller) this.applicationContext.getBean("mainController");
        } catch (NoSuchBeanDefinitionException var2) {
            this.logger.fatal("Unable to locate Controller with name 'mainController");
        }

    }

    private void initViewResolver() throws BeansException {
        try {
            this.viewResolver = (CsiiViewResolver) this.applicationContext.getBean("mainViewResolver");
        } catch (NoSuchBeanDefinitionException var2) {
            this.logger.fatal("Unable to locate view resolver with name 'mainViewResolver");
        }

    }

    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (this.synchronizeOnSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                synchronized (session) {
                    this.process(request, response);
                    return;
                }
            }
        }

        this.process(request, response);
    }

    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpServletRequest processedRequest = request;
        if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
            if (request instanceof MultipartHttpServletRequest) {
                this.logger.info("Request is already a MultipartHttpServletRequest - if not in a forward, this typically results from an additional MultipartFilter in web.xml");
            } else {
                try {
                    processedRequest = this.multipartResolver.resolveMultipart(request);
                } catch (Throwable var9) {
                    Map map = null;

                    try {
                        map = (Map) this.applicationContext.getBean("MultipartErrorMap");
                    } catch (Throwable var7) {
                        ;
                    }

                    if (map != null) {
                        this.onError(request, response, var9, (String) map.get("ErrorPage"));
                        return;
                    }

                    if (var9 instanceof RuntimeException) {
                        throw (RuntimeException) var9;
                    }

                    throw new ServletException(var9.getMessage(), var9);
                }
            }
        }

        if (this.synchronizeOnSession) {
            HttpSession session = ((HttpServletRequest) processedRequest).getSession(false);
            if (session != null) {
                synchronized (session) {
                    this.process((HttpServletRequest) processedRequest, response);
                    return;
                }
            }
        }

        this.process((HttpServletRequest) processedRequest, response);
    }

    private void onError(HttpServletRequest request, HttpServletResponse response, Throwable t, String url) {
        try {
            request.setAttribute(Constants.MAINSERVLET_APPLICATION_CONTEXT_ATTRIBUTE, this.applicationContext);
            request.setAttribute("_viewReferer", url);
            String newLocale = request.getParameter(this.localeParamName);
            if (newLocale != null) {
                if (!this.allowedLocales.contains(newLocale)) {
                    this.logger.error("Invalid locale " + newLocale + ", allowed locales: " + this.allowedLocales);
                    throw new PeRuntimeException("invalid_locale", new Object[]{newLocale, this.allowedLocales.toString()});
                }

                LocaleEditor localeEditor = new LocaleEditor();
                localeEditor.setAsText(newLocale);
                this.localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
            }

            Locale locale = this.localeResolver.resolveLocale(request);
            if (locale.toString().indexOf("en") >= 0) {
                locale = new Locale("en", "US");
            }

            Map model = new HashMap();
            model.put("_exception", t);
            model.put("__ExceptionMessage", t.getMessage());
            model.put("__ExceptionName", t.getMessage());
            response.setLocale(locale);
            request.setAttribute(this.localeParamName, locale);
            this.localeResolver.setLocale(request, response, locale);
            this.render(model, request, response, locale);
        } catch (Exception var8) {
            this.logger.error("process error:", var8);
        }

    }

    private void process(HttpServletRequest request, HttpServletResponse response) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("process begin");
        }

        try {
            request.setAttribute(Constants.MAINSERVLET_APPLICATION_CONTEXT_ATTRIBUTE, this.applicationContext);
            String newLocale = request.getParameter(this.localeParamName);
            if (newLocale != null) {
                if (!this.allowedLocales.contains(newLocale)) {
                    this.logger.error("Invalid locale " + newLocale + ", allowed locales: " + this.allowedLocales);
                    throw new PeRuntimeException("invalid_locale", new Object[]{newLocale, this.allowedLocales.toString()});
                }

                LocaleEditor localeEditor = new LocaleEditor();
                localeEditor.setAsText(newLocale);
                this.localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
            } else {
                // 根据_locale定位国际化，如果不传递_locale则默认定位zh_CN
                LocaleEditor localObject1 = new LocaleEditor();
                localObject1.setAsText("zh_CN");
                this.localeResolver.setLocale(request, response, (Locale) localObject1.getValue());
            }

            Locale locale = this.localeResolver.resolveLocale(request);
            if (locale.toString().indexOf("en") >= 0) {
                locale = new Locale("en", "US");
            }

            if (locale.toString().toLowerCase().equals("zh_hans_cn")) {
                locale = new Locale("zh", "CN");
            }

            Object model = null;
            model = this.controller.process(request, response, locale);
//            response.setLocale(locale);
//            request.setAttribute(this.localeParamName, locale);
//            this.localeResolver.setLocale(request, response, locale);
//            this.render(model, request, response, locale);
        } catch (Exception var9) {
            this.logger.error("process error:", var9);
        } finally {
            LoggingInterceptor.cleanUp();
        }

    }

    public void destroy() {
    }

    public String getServletContextAttributeName() {
        return SERVLET_CONTEXT_PREFIX + this.getServletName();
    }

    private void render(Object model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
        String viewName = (String) request.getAttribute("_viewReferer");
        String[] splittedViewName = this.resolverViewResolverName(viewName);
        CsiiView view = this.viewResolver.resolveView(splittedViewName[0]);
        if (view != null) {
            view.render(splittedViewName[1], model, locale, request, response);
        } else {
            throw new PeRuntimeException("pe.cannot_resolve_view", new Object[]{splittedViewName[0], viewName});
        }
    }

    private String[] resolverViewResolverName(String viewName) {
        String[] resolverNames = new String[2];
        String[] splits = StringUtils.delimitedListToStringArray(viewName, ",");
        if (splits.length == 1) {
            resolverNames[0] = "default";
            resolverNames[1] = splits[0];
        } else if (splits.length == 2) {
            resolverNames[0] = splits[0];
            resolverNames[1] = splits[1];
        }

        return resolverNames;
    }

    public void setLocaleParamName(String localeParamName) {
        this.localeParamName = localeParamName;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.initMultipartResolver();
        this.initLocaleResolver();
        this.initViewResolver();
        this.initController();
    }
}
