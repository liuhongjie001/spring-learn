package liu.edu.service;

import liu.edu.annocation.LService;

@LService
public class IndexService {

    public String getIndex(String name){
        return "My name is "+name;
    }
}
