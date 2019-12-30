package com.guobank.web;

import com.alibaba.fastjson.JSON;
import com.guobank.entity.AdminInfo;
import com.guobank.service.AdminInfoService;
import com.guobank.service.impl.AdminInfoServiceImpl;
import com.guobank.util.EmailUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author : pengliang
 * @version V1.0
 * @Project: guoBank
 * @Package ${PACKAGE_NAME}
 * @Descriptio /**
 * @Project: guoBank
 * @Package ${PACKAGE_NAME}
 * @Description: TODO
 * @date Date : 2019年12月26日 9:22
 */
public class AdminInfoServlet extends HttpServlet {
    private AdminInfoService adminInfoService = new AdminInfoServiceImpl();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            if ("checkEmail".equals(request.getParameter("action"))) {
                response.getWriter().print(adminInfoService.checkEmail(request.getParameter("email")));
            } else if ("register".equals(request.getParameter("action"))) {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                //解析request，将form表单的各个字段封装为FileItem对象
                List<FileItem> fileItems = upload.parseRequest(request);
                AdminInfo adminInfo = new AdminInfo();
                for (FileItem fileItem : fileItems) {
                    //不是文件类型
                    if (fileItem.isFormField()) {
                        Field field = AdminInfo.class.getDeclaredField(fileItem.getFieldName());
                        field.setAccessible(true);
                        field.set(adminInfo, fileItem.getString("utf-8"));
                    } else {
                        if (fileItem.getName().equals("default")) {
                            InputStream is = new FileInputStream(request.getServletContext().getRealPath("/img/default.jpg"));
                            adminInfo.setAdminPortrait(is);
                        } else {
                            //是文件类型
                            adminInfo.setAdminPortrait(fileItem.getInputStream());
                        }
                    }
                }
                response.getWriter().print(adminInfoService.adminRegister(adminInfo));
            } else if ("loginCheck".equals(request.getParameter("action"))) {
                response.getWriter().print(request.getSession().getAttribute("adminEmail") != null);
            } else if ("getAdminPortrait".equals(request.getParameter("action"))) {
                InputStream is = adminInfoService.getAdminPortrait(request.getSession().getAttribute("adminEmail").toString());
                int len = 0;
                byte[] bytes = new byte[1024];
                OutputStream out = response.getOutputStream();
                while ((len = is.read(bytes)) != -1) {
                    out.write(bytes, 0, len);
                }
                is.close();
                out.flush();
                out.close();
            } else if ("activationAdmin".equals(request.getParameter("action"))) {
                String email = request.getParameter("adminEmail");
                Boolean flag = adminInfoService.activationAdmin(email);
                if (flag) {
                    new Thread(new EmailUtil(email, "激活成功,欢迎加入郭氏银行","郭氏银行: 管理员账户激活信息")).start();
                }
                response.getWriter().write(flag.toString());
            }else if("getAdminInfo".equals(request.getParameter("action"))){
                String email = request.getSession().getAttribute("adminEmail").toString();
                AdminInfo adminInfo = adminInfoService.getAdminInfo(email);
                adminInfo.setAdminPwd("");
                response.getWriter().write(JSON.toJSONString(adminInfo));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
