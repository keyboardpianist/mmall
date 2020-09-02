package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0)
        {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //todo 密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null)
        {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return  ServerResponse.createBySuccess("登录成功", user);
    }

    public ServerResponse<String> register(User user)
    {
        ServerResponse vaildResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!vaildResponse.isSuccess())
        {
            return vaildResponse;
        }
        vaildResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!vaildResponse.isSuccess())
        {
            return vaildResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0)
        {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str, String type)
    {
        if (StringUtils.isNotBlank(type))
        {
            if (Const.USERNAME.equals(type))
            {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0)
                {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type))
            {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0)
                {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
        }
        else
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }//实时反馈是否合适

    public ServerResponse<String> selectQuestion(String username)
    {
        ServerResponse vaildResponse = this.checkValid(username, Const.USERNAME);
        if (vaildResponse.isSuccess())
        {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question))
        {
            return ServerResponse.createBySuccess(question);
        }

        return ServerResponse.createByErrorMessage("找回密码的问题为空");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer)
    {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0)
        {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey("token_" + username, forgetToken);
            return  ServerResponse.createBySuccess(forgetToken);
        }//需要一个专用的token保存到服务器缓存中，来说明确实是当前用户在修改自己的密码
        //为什么不直接用session?因为这是点击了忘记密码！！！我佛了。。
        //这里其实可以在这个基础上改成发送一封邮件到用户指定的邮箱。。当然token还是要保存一下的，毕竟要记住当前是哪个用户，且必须是知晓答案的人
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }
}
