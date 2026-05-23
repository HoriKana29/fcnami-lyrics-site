package com.fcnami.backend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

// เปิดระบบให้ Retryable ใช้งานได้
///  *** สามารถเพิ่ม method recover ได้
@Configuration
@EnableRetry
public class RetryConfig { }
