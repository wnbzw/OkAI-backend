package com.zw.okai.controller;

import com.zw.okai.common.BaseResponse;
import com.zw.okai.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

@RestController
@RequestMapping("/dns")
public class DnsController {

    @GetMapping("/search")
    public BaseResponse<String> search(String domain) throws IOException {
        return ResultUtils.success(resolve(domain));
    }

    private static String resolve(String domain) throws IOException {
        short transactionId = (short) new Random().nextInt(0x10000);
        byte[] request = buildDnsRequest(domain, transactionId);
        byte[] response = sendAndReceive(request);
        return parseDnsResponse(response, transactionId);
    }

    /**
     * 构建DNS请求
     * @param domain 要查询的域名（需符合RFC规范），如"example.com"
     * @param transactionId DNS事务ID（2字节），用于匹配请求与响应
     * @return 符合DNS协议标准的查询报文字节数组
     */
    private static byte[] buildDnsRequest(String domain, short transactionId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 创建一个ByteArrayOutputStream，用于存储DNS请求报文
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            // 头部信息构造
            dos.writeShort(transactionId); //事务ID
            dos.writeShort(0x0100); // 标准字段
            dos.writeShort(1); // 问题数: 1
            dos.writeShort(0); // 应答数
            dos.writeShort(0); // 授权记录数
            dos.writeShort(0); // 附加记录数

            // ========== 问题区域构造 ==========
            // 域名编码规则：将域名拆分为标签序列，每个标签前加长度字节
            String[] labels = domain.replaceAll("\\.$", "").split("\\.");
            for (String label : labels) {
                // 标签格式：1字节长度 + ASCII编码的标签内容
                dos.writeByte(label.length());
                dos.write(label.getBytes(StandardCharsets.US_ASCII));
            }
            dos.writeByte(0); // 域名结束标志
            dos.writeShort(1); // 查询类型: A记录
            dos.writeShort(1); // 查询类型: IN
        } catch (IOException e) {
            throw new AssertionError("Unexpected IO error", e);
        }

        return baos.toByteArray();
    }

    /**
     * 发送UDP请求并接收响应数据
     *
     * @param request 要发送的请求字节数组，长度不能超过数据包限制
     * @return 接收到的响应字节数组，已截断到实际数据长度
     * @throws IOException 当发生I/O错误（包括超时未收到响应）时抛出
     */
    private static byte[] sendAndReceive(byte[] request) throws IOException {
        // 使用try-with-resources确保socket自动关闭
        try (DatagramSocket socket = new DatagramSocket()) {
            // 设置5秒超时防止无限等待
            socket.setSoTimeout(5000);
            // 指定Google公共DNS服务器地址
            InetAddress dnsServer = InetAddress.getByName("8.8.8.8");
            // 构建请求数据包（UDP 53端口）
            DatagramPacket requestPacket = new DatagramPacket(request, request.length, dnsServer, 53);
            // 发送请求
            socket.send(requestPacket);

            // 初始化接收缓冲区（最大支持1024字节响应）
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            // 阻塞等待直到收到响应或超时
            socket.receive(responsePacket);

            // 截断缓冲区仅保留实际数据部分
            return Arrays.copyOf(responsePacket.getData(), responsePacket.getLength());
        }
    }

    /**
     * 解析DNS响应报文，提取A记录对应的IP地址
     *
     * @param response    DNS响应字节数组，要求符合DNS协议格式
     * @param expectedId  预期的事务ID，用于验证响应合法性
     * @return 解析到的第一个有效A记录的IPv4地址（点分十进制格式）
     * @throws RuntimeException 当响应非法或未找到A记录时抛出
     */
    private static String parseDnsResponse(byte[] response, short expectedId) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(response))) {
            // 校验事务ID
            short transactionId = dis.readShort();
            if (transactionId != expectedId) {
                throw new RuntimeException("Invalid DNS response: Transaction ID mismatch");
            }

            // 跳过其他头字段
            dis.readShort(); // 标准字段
            dis.readShort(); // 问题数
            int answerCount = dis.readShort() & 0xFFFF; // 应答数
            dis.readShort(); // 授权记录数
            dis.readShort(); // 附加记录数

            // 处理QUESTION SECTION：循环读取直到遇到空标签（长度0）
            while (true) {
                int len = dis.readByte() & 0xFF;
                if (len == 0) break;
                dis.skipBytes(len);
            }
            dis.readShort(); // Type
            dis.readShort(); // Class

            // 遍历ANSWER SECTION查找A记录
            for (int i = 0; i < answerCount; i++) {
                // 处理可能压缩的域名
                readDomainName(dis);
                int type = dis.readShort() & 0xFFFF;
                dis.readShort(); // Class
                dis.readInt(); // TTL
                int dataLength = dis.readShort() & 0xFFFF;

                if (type == 1) { // A 记录类型
                    if (dataLength != 4) { // 跳过无效的记录
                        dis.skipBytes(dataLength);
                        continue;
                    }
                    // 读取IPv4地址并转换为点分十进制格式
                    byte[] ip = new byte[4];
                    dis.readFully(ip);
                    return (ip[0] & 0xFF) + "." + (ip[1] & 0xFF) + "." + (ip[2] & 0xFF) + "." + (ip[3] & 0xFF);
                } else {
                    dis.skipBytes(dataLength);// 跳过未知记录
                }
            }

            throw new RuntimeException("No A record found in DNS response");
        } catch (IOException e) {
            throw new RuntimeException("Error parsing DNS response", e);
        }
    }

    private static void readDomainName(DataInputStream dis) throws IOException {
        while (true) {
            int len = dis.readByte() & 0xFF;// 标签长度 转换为 0-255 无符号整数
            if (len == 0) break; // 遇到空标签结束循环
            if ((len & 0xC0) == 0xC0) {
                dis.readByte(); // 读取指针的第二个字节，组成完整的偏移量
                break;
            } else { // Label
                dis.skipBytes(len);
            }
        }
    }
}
