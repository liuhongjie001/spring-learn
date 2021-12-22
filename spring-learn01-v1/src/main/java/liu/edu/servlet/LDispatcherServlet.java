package liu.edu.servlet;

import liu.edu.annocation.LAutoWried;
import liu.edu.annocation.LController;
import liu.edu.annocation.LRequestMapping;
import liu.edu.annocation.LService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class LDispatcherServlet extends HttpServlet {

    //解析配置文件使用
    Properties properties = new Properties();

    //存放需要扫描包的全类名
    List<String> packagePathList = new ArrayList<>();

    //存放初始化的实例
    Map<String,Object> ioc = new HashMap<>();


    //存放url 与method 的映射关系
    Map<String, Method> handlerMapping = new HashMap<>();



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDisPatch(req,resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doDisPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        //如果在tomcat中配置了访问路径中的项目名不为空的时候需要把项目名转化为空。默认是带有项目名称的
        if(!"".equals(contextPath)) {
            requestURI = contextPath.replaceAll(contextPath, "").replaceAll("/+", "/");
        }
        if(!handlerMapping.containsKey(requestURI)){
            resp.getWriter().write("500");
            return;
        }

        Method method = handlerMapping.get(requestURI);
        String beanName = toLowerFitstCase(method.getDeclaringClass().getName());
        handlerMapping.get(requestURI).invoke(ioc.get(beanName),new Object[]{req,resp});

    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 加载配置文件
            loadConfig(config.getInitParameter("contextConfigLocation"));

        //--------------------------------- IOC ------------------------------------------
        // 扫描所有类
            doScanner(properties.getProperty("packageScan"));

        // 初始化IOC 并将所有扫描类初始化进IOC
            doInstance();

        //--------------------------------- DI ------------------------------------------
        //完成DI 自动注入
            doAotoWried();
        //--------------------------------- SpringMVC ------------------------------------------
        //初始化 URL 与方法的映射关系
            doInitHandlerMapping();





    }

    private void doInitHandlerMapping() {
        if(ioc.isEmpty()){
            return;
        }
        //循环所有bean
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            String baseUrl = "";
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(LController.class)){
                continue;
            }
            //先取出类名上带有@RequestMapping 方法
            if(clazz.isAnnotationPresent(LRequestMapping.class)){
                baseUrl = clazz.getAnnotation(LRequestMapping.class).value();
            }

            //再取出当前bean 里边所有方法并找出带有@LRequestMapping的方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(LRequestMapping.class)){
                    continue;
                }
                //拼接上类名的@LRequestMapping的路径
                String url = ("/"+baseUrl+"/" + method.getAnnotation(LRequestMapping.class).value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("mapped"+url+","+method);
            }

        }

    }

    private void doAotoWried() {
        if(ioc.isEmpty()){return;}
        //循环IOC里边的Bean 寻找带有@AutoWried 字段
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //取出当前bean 所有字段
            Field[] declaredField = entry.getValue().getClass().getDeclaredFields();
            //循环字段找出带有@AutoWried注解的字段 完成注入操作
            for (Field fields : declaredField) {
                if(!fields.isAnnotationPresent(LAutoWried.class)){
                    continue;
                }
                //拿到带有@AutoWried 注解的字段
                LAutoWried field = fields.getAnnotation(LAutoWried.class);
                //取注解的配置值
                String beanName = field.value();
                //如果注解没有自己声明需要注入的beanName 需要自己拼装，
                if("".equals(beanName)){
                    beanName = toLowerFitstCase(fields.getType().getName());
                }
                //不管该字段什么修饰符 都会去暴力访问
                fields.setAccessible(true);
                try {
                    //使用反射机制
                    // 根据 BeanName 去IOC寻找相匹配的bean 使用反射给字段赋值
                    // 给entry.getValue()对象的field字段 注入ioc.get(BeanName)这个值
                    fields.set(entry.getValue(),ioc.get(beanName));

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化Bean
     * 什么样的类需要初始化？
     * 加了注解的类，才初始化 怎么判断?
     * 为了简化代码这里只举例@Service @Controller
     *
     */
    private void doInstance() {
        if(packagePathList.isEmpty()){return;}
        for (String className : packagePathList) {
            try {
                Class<?> aClass = Class.forName(className);
                //@LController注解的实例化
                if(aClass.isAnnotationPresent(LController.class)){
                    ioc.put(toLowerFitstCase(aClass.getName()),aClass.newInstance());
                }else if(aClass.isAnnotationPresent(LService.class)){ //@LService
                    ioc.put(toLowerFitstCase(aClass.getName()),aClass.newInstance());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 将所得到的类名开头字母转化成小写
     * 这里没有考虑类名不规范的情况下
     * @param name
     * @return
     */
    private String toLowerFitstCase(String name) {
        char [] chars = name.toCharArray();
        //利用ASCII码 大小写之间相差32的这个规律来转换成大写
        chars[0] += 32;
        return String.valueOf(chars);

    }

    /**
     * 将 scanPackage 路径下的的所有类扫描出来，把全类限定名存储在list
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        //获取到扫描的这个文件夹下的所有文件
        URL uri = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File files = new File(uri.getFile());
        for (File file : files.listFiles()) {
            //如果当前文件为文件夹在解析
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                //这里需要判断只需要.class文件
                if(!file.getName().endsWith(".class")){continue;}
                packagePathList.add(scanPackage+"."+file.getName().replaceAll(".class",""));
            }

        }
    }

    private void loadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
