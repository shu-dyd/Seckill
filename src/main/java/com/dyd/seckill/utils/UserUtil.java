package com.dyd.seckill.utils;

import com.dyd.seckill.pojo.User;
import com.dyd.seckill.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Properties;

// 测试的时候进行使用，在windows上执行
public class UserUtil {

    private static Connection getConn() throws Exception {
        // 1.读取配置文件中的四个基本信息
        InputStream is = UserUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
        Properties pros = new Properties();
        pros.load(is);

        String user = pros.getProperty("user");
        String password = pros.getProperty("password");
        String url = pros.getProperty("url");
        String driverClass = pros.getProperty("driverClass");

        // 2.加载驱动
        Class.forName(driverClass);

        // 3.获取连接
        return DriverManager.getConnection(url, user, password);
    }


    private static void createUser(int count) throws Exception {

        // 批量生成用户后，存入数据库中
        ArrayList<User> users = new ArrayList<>(count);
        for(int i = 0; i < count; i++){
            User user = new User();
            user.setId(13800000000L+i);
            user.setNickname("user"+i);
            user.setSalt("1a2b3c4d");
            user.setPassword(MD5Utils.inputPassToDBPass("123123", user.getSalt()));
            user.setLoginCount(0);
            users.add(user);
        }

        Connection conn = getConn();
        String sql = "insert into t_user(login_count,nickname,register_date,salt,password,id) values (?,?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            ps.setInt(1, user.getLoginCount());
            ps.setString(2, user.getNickname());
//            ps.setTimestamp(3, Timestamp.valueOf(user.getRegisterDate()));
            ps.setTimestamp(3, null);
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getPassword());
            ps.setLong(6, user.getId());
            ps.addBatch();
        }
        ps.executeBatch();// 就一个batch，就不执行clearBatch()了
        ps.clearParameters();
        conn.close();

        // 所有生成的用户去登录，产生cookie，会在redis中进行保存
        // 也保存在本地的txt中，用于压力测试
        String urlString = "http://localhost:8080/login/doLogin";
        File file = new File("D:\\exer\\xiangmu\\config.txt");
        if(file.exists()){
            file.delete();
        }

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Utils.inputPassToFromPass("123123");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buff)) >= 0){
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(response, RespBean.class);
            String userTicket = (String) respBean.getObj();
            String row = user.getId() + "," +userTicket;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
        }
        raf.close();
    }

    public static void main(String[] args) throws Exception {
        createUser(2000);
    }

}
