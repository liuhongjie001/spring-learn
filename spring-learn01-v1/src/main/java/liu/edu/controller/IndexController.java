package liu.edu.controller;

import liu.edu.annocation.LAutoWried;
import liu.edu.annocation.LController;
import liu.edu.annocation.LRequestMapping;
import liu.edu.service.IndexService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@LController
@LRequestMapping("/index")
public class IndexController {

    @LAutoWried
    private IndexService indexService;

    @LRequestMapping("getIndex")
    public String index(){
        return indexService.getIndex();
    }


    @LRequestMapping("aa")
    public void indexcc(HttpServletRequest req, HttpServletResponse res){
        try {
            res.getWriter().write(indexService.getIndex());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return "hello";
    }


}
