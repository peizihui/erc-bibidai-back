package com.theforceprotocol.bbd.service.impl;

import com.google.common.hash.Hashing;
import com.theforceprotocol.bbd.consts.SmsTemplates;
import com.theforceprotocol.bbd.domain.dto.SmsSendResp;
import com.theforceprotocol.bbd.props.SmsProperties;
import com.theforceprotocol.bbd.service.SmsSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.security.MD5Encoder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class SmsSendServiceImpl implements SmsSendService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final SmsProperties props;
    private final RestTemplate restTemplate;

    public SmsSendServiceImpl(SmsProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    private static SmsSendResp parse(String xml) {
        SAXReader saxReader = new SAXReader();
        try (StringReader reader = new StringReader(xml)) {
            try {
                Document document = saxReader.read(reader);
                Element rootElement = document.getRootElement();
                String status = rootElement.selectSingleNode("returnstatus").getText();
                String message = rootElement.selectSingleNode("message").getText();
                int remaining = Integer.parseInt(rootElement.selectSingleNode("remainpoint").getText());
                String taskId = rootElement.selectSingleNode("taskID").getText();
                int successCount = Integer.parseInt(rootElement.selectSingleNode("successCounts").getText());
                SmsSendResp result = new SmsSendResp();
                result.setStatus(status);
                result.setMessage(message);
                result.setRemaining(remaining);
                result.setTaskId(taskId);
                result.setSuccessCount(successCount);
                return result;
            } catch (DocumentException e) {
                log.error("failed to read xml:{}", xml);
            }
        }
        return null;
    }

    @Override
    public SmsSendResp send(Integer countryCode, String phone, String message) {
        String ts = LocalDateTime.now().format(FORMATTER);
        String input = String.format("%s%s%s", props.getUsername(), props.getPassword(), ts);
        String signed = MD5Encoder.encode(input.getBytes());
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("userid", props.getUserId());
        map.add("timestamp", ts);
        map.add("sign", signed);
        map.add("mobile", phone);
        map.add("content", String.format("%s%s", SmsTemplates.SMS_PREFIX, message));
        map.add("action", props.getAction());
        SmsSendResp ssp = new SmsSendResp();
        ssp.setMessage("ok");
        return ssp;
    }
}
