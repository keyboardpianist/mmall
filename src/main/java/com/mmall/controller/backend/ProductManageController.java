package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    private static Logger logger = LoggerFactory.getLogger(ProductManageController.class);

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        if (iUserService.checkAdminRole(user).isSuccess())
        {
            return iProductService.saveOrUpdateProduct(product);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
    /**
     * 上架/下架
     * */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        if (iUserService.checkAdminRole(user).isSuccess())
        {
            return iProductService.setSaleStatus(productId, status);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        if (iUserService.checkAdminRole(user).isSuccess())
        {
            return iProductService.manageProductDetail(productId);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session,
                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10")int pageSize)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        if (iUserService.checkAdminRole(user).isSuccess())
        {
            return iProductService.getProductList(pageNum, pageSize);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId,
                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10")int pageSize)
    {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        if (iUserService.checkAdminRole(user).isSuccess())
        {
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        }
        else
        {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * ServletContext–范围最大。应用程序级别的，整个应用程序都能訪问
     * HttpSession   – 次之，会话级别的，在当前的浏览器中都能訪问[不论是在同一浏览器开多少窗体，都能够訪问]。可是换个浏览器就不行了。就必须又一次创建session
     * HttpServletRequest  –范围最小，请求级别，请求结束，变量的作用域也结束[也就是仅仅是一次訪问，訪问结束，这个也结束]
     * */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "upload_file", required = false) MultipartFile file,
                                 HttpSession session)
    {
        //logger.info("判断session是否一样" + String.valueOf(request.getSession() == session));
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        if (iUserService.checkAdminRole(user).isSuccess())
        {
            //String path = session.getServletContext().getRealPath("upload");
            String path = session.getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

            Map<String, String> fileMap = new HashMap<>();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);

            return ServerResponse.createBySuccess(fileMap);
        }
        return ServerResponse.createByErrorMessage("无权限操作");
    }

    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map<String, Object> richtextImgUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file,
                                                 HttpSession session, HttpServletResponse response)
    {
        Map<String, Object> resultMap = new HashMap<>();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null)
        {
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员账户");
            return resultMap;
        }

        if (iUserService.checkAdminRole(user).isSuccess())
        {
            String path = session.getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            if (StringUtils.isBlank(targetFileName))
            {
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);

            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;
        }

        resultMap.put("success", false);
        resultMap.put("msg", "无权限操作");
        return resultMap;
    }
}
