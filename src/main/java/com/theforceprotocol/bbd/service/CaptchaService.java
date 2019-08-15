package com.theforceprotocol.bbd.service;

import java.awt.image.RenderedImage;

public interface CaptchaService {
    RenderedImage newImage(String sessionId, String text);

    Boolean isValid(String sessionId, String text);
}
