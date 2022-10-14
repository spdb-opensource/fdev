package com.spdb.common.exception;

import com.spdb.common.util.ErrorMessageUtil;
import com.spdb.common.util.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

/**
 * 全局异常处理
 *
 * @author xxx
 */

@RestControllerAdvice
@RefreshScope
public class GlobalExceptionHandler implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.application.name:unkonwn}")
    private String applicationName;

    @Autowired
    private ErrorMessageUtil errorMessageUtils;

    /**
     * 自定义要捕获的异常 可以多个@ExceptionHandler({})
     *
     * @param request
     * @param e
     * @param response
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public JsonResult customExceptionHandler(HttpServletRequest request, final Exception e,
                                             HttpServletResponse response) {
        CustomException exception = (CustomException) e;
        String errorMessage = exception.getMessage();
        if (errorMessage == null || "".equals(errorMessage)) {
            errorMessage = errorMessageUtils.get(exception);
        }
        String requestUri = request.getRequestURI();
        logger.error(new StringBuilder("**** trans end:").append(requestUri).append(" process error: ")
                .append(exception.getCode()).append(":").append(errorMessage).append(" ****").toString(), exception);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return JsonResult.buildError(exception.getCode(), errorMessage);
    }

    /**
     * 捕获 RuntimeException 异常
     *
     * @param request
     * @param e
     * @param response
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public JsonResult runtimeExceptionHandler(HttpServletRequest request, final Exception e,
                                              HttpServletResponse response) {
        logger.error("**** trans end: runtime exception! ", e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        RuntimeException exception = (RuntimeException) e;
//        if (exception instanceof UnauthorizedException){
//            return JsonResult.buildError(HttpStatus.BAD_REQUEST.toString(), "交易无权限");
//        }
        return JsonResult.buildError(HttpStatus.BAD_REQUEST.toString(), exception.getMessage());
    }

    /**
     * 方法参数无效异常
     *
     * @param request
     * @param e
     * @param response
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult methodArgumentNotValidExceptionHandler(HttpServletRequest request, final Exception e,
                                                             HttpServletResponse response) {
        logger.error("**** trans end: method argument not valid! ", e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
        BindingResult bindingResult = exception.getBindingResult();
        String msg = "";
        // 现在表示执行的验证出现错误
        if (bindingResult.hasErrors()) {
            // 获取全部错误信息
            Iterator<ObjectError> iterator = bindingResult.getAllErrors().iterator();
            while (iterator.hasNext()) {
                // 取出每一个错误
                ObjectError error = iterator.next();
                msg = msg + error.getDefaultMessage() + ";";
            }
        }
        return JsonResult.buildError(HttpStatus.BAD_REQUEST.toString(), msg);
    }

    /**
     * 方法参数类型不匹配异常
     *
     * @param request
     * @param e
     * @param response
     * @return
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public JsonResult methodArgumentTypeMismatchExceptionHandler(HttpServletRequest request, final Exception e,
                                                                 HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        MethodArgumentTypeMismatchException exception = (MethodArgumentTypeMismatchException) e;
        logger.error("**** trans end: 参数类型转换失败，方法：" + exception.getParameter().getMethod().getName() + ",参数：" + exception.getName()
                + ",信息：" + exception.getLocalizedMessage());
        return JsonResult.buildError(HttpStatus.BAD_REQUEST.toString(), "参数类型转换失败");
    }

    /**
     * 缺少Servlet请求参数异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public JsonResult handleMissingServletRequestParameterException(HttpServletRequest request, final Exception e,
                                                                    HttpServletResponse response) {
        logger.error("**** trans end: missing request parameter! ", e);
        JsonResult jsonResult = JsonResult.buildError(HttpStatus.BAD_REQUEST.toString(), "缺少请求参数");
        return jsonResult;
    }

    /**
     * Http消息不可读异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public JsonResult handleHttpMessageNotReadableException(HttpServletRequest request, final Exception e,
                                                            HttpServletResponse response) {
        logger.error("**** trans end: http message not readable! ", e);
        JsonResult jsonResult = JsonResult.buildError(HttpStatus.BAD_REQUEST.toString(), "参数解析失败");
        return jsonResult;
    }

    /**
     * 异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public JsonResult exceptionHandler(Exception e) {
        String errorMessage = e.getMessage();
        String requestUri = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getRequestURI();
        logger.error(new StringBuilder("**** trans end:").append(requestUri).append(" process error: ")
                .append(errorMessage == null ? "9999999" : errorMessage).append(" ****").toString(), e);
        if (errorMessage == null || "".equals(errorMessage)) {
            errorMessage = "系统内部错误! ";
        }
        JsonResult jsonResult = JsonResult.buildError("9999999", errorMessage);
        return jsonResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JsonResult.setApplicationName(this.applicationName);
    }
}
