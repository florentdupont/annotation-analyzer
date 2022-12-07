package com.example.demo;

public class MyService {

    private static final String MSG_CODE = "msg.code2";

    
    @ThrowsBE({"message.code", MSG_CODE, "654"})
    void service() throws BusinessException {
        
        
        long id = 20;
       
        if(id > 20) {
            throw new BusinessException("message.code");
        } else if (id > 10){
            throw new BusinessException(MSG_CODE);
        } else{
            throw new BusinessException("654");

        }
    }
    
    @ThrowsBE(MSG_CODE)
    void service2(int toto, String tata) throws BusinessException {
        throw new BusinessException(MSG_CODE);
    }
}
