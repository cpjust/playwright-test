/*
 * Copyright (C) 2024 ${owner}
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.cpjust.selenium;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.NullOutputStream;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.service.DriverService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.logging.Level;

/**
 * Base class for Selenium tests.
 */
@Slf4j
public class SeleniumTestBase {
    protected WebDriver driver;
    protected ChromeOptions chromeOptions;
    protected ChromeDriverService chromeDriverService;

    @BeforeClass
    public void beforeSeleniumTestBaseClass() {
        disableLogging();

        chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("--no-sandbox");
//        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("disable-gpu");
        chromeOptions.addArguments("--remote-allow-origins=*"); // This fixes a WebSocket error: https://stackoverflow.com/questions/75680149/unable-to-establish-websocket-connection
//        chromeOptions.addArguments("--browserVersion=115");

        // Disables the useless "ChromeDriver was started successfully" messages.
        DriverService.Builder<ChromeDriverService, ChromeDriverService.Builder> serviceBuilder = new ChromeDriverService.Builder();
        chromeDriverService = serviceBuilder.build();
        chromeDriverService.sendOutputTo(NullOutputStream.NULL_OUTPUT_STREAM);
    }

    @BeforeMethod
    public void beforeSeleniumTestBaseMethod() {
        driver = new ChromeDriver(chromeDriverService, chromeOptions);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
    }

    @AfterMethod
    public void afterSeleniumTestBaseMethod() {
        if (driver != null) {
            driver.close();
        }
    }

    @AfterClass
    public void afterSeleniumTestBaseClass() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    /**
     * Gets rid of useless Selenium console messages.
     * See: https://stackoverflow.com/questions/52975287/selenium-chromedriver-disable-logging-or-redirect-it-java
     */
    private void disableLogging() {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
    }
}
