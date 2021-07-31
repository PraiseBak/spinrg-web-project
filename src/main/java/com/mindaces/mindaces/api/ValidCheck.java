package com.mindaces.mindaces.api;


import java.util.regex.Pattern;

public class ValidCheck
{

    public boolean isSignupValid(String inputID, String userPassword, String userEmail)
    {
        //@한개만 나왔는지 @.한개만 나왔는지
        if (!isValidEmail(userEmail))
        {
            return false;
        }
        if (!isValidID(inputID))
        {
            return false;
        }
        if(!isValidPassword(userPassword))
        {
            return false;
        }
        return true;
    }

    public boolean isValidEmail(String email)
    {
        if(email.length() < 4 || email.length() > 40)
        {
            return false;
        }
        //이메일 체크
        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        return Pattern.matches(regex,email);
    }

    public boolean isValidID(String id)
    {
        //6자에서 20자 시작 영문, _ 제외한 특수문자 안됨
        String regex = "^[a-zA-Z]{1}[a-zA-Z0-9_]{5,19}$";
        return Pattern.matches(regex,id);
    }

    public boolean isValidPassword(String password)
    {
        //'숫자', '문자' 무조건 1개 이상, '최소 8자에서 최대 20자' 허용 \
        // (특수문자는 정의된 특수문자만 사용 가능)
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$";
        return Pattern.matches(regex,password);
    }

}
