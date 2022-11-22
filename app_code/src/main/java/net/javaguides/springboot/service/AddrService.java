package net.javaguides.springboot.service;

import javax.servlet.http.HttpServletRequest;

public interface AddrService {
    String getClientIp(HttpServletRequest request);
}
