/**
 * Copyright (C) 2024 Chris Just
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
package com.github.cpjust.playwrite.test;

import com.github.cpjust.constants.EchoFirCompressionShortKeys;
import com.github.cpjust.util.PropertyReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

@Slf4j
public class SimplePlaywrightTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;
    private Properties properties;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws IOException {
        PropertyReader propertyReader = new PropertyReader();
        properties = propertyReader.getPropertiesFromResources("locators/magento.softwaretestingboard.com/EchoFirCompressionShort.properties");
        playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(false);
        launchOptions.setArgs(Collections.singletonList("--start-maximized"));
        browser = playwright.chromium().launch(launchOptions);
    }

    @BeforeMethod
    public void beforeMethod() {
        // NOTE: To maximize the browser, we need to set the viewport size to null.
        // See: https://stackoverflow.com/questions/77278023/how-to-maximize-the-window-size-in-playwright
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        contextOptions.setViewportSize(null);
        browserContext = browser.newContext(contextOptions);
        page = browserContext.newPage();
        page.navigate(properties.getProperty(EchoFirCompressionShortKeys.URL));

        // Throttle the network speed.
        setNetworkConditions(10000, -1, 5000, false);
    }

    @AfterMethod
    public void afterMethod() {
        if (page != null) {
            page.close();
            page = null;
        }

        if (browserContext != null) {
            browserContext.close();
            browserContext = null;
        }
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        if (browser != null) {
            browser.close();
            browser = null;
        }

        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    @Test
    public void checkProductInfo_verifyTitleAndPrice() {
        String title = page.textContent(properties.getProperty(EchoFirCompressionShortKeys.PAGE_TITLE))
                .trim();
        String availability = page.textContent(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_AVAILABILITY))
                .trim();
        String price = page.textContent(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_PRICE));

        Assertions.assertEquals("Echo Fit Compression Short", title, "Wrong title!");
        Assertions.assertEquals("In stock", availability, "Wrong availability!");
        Assertions.assertEquals("$24.00", price, "Wrong title!");
    }

    @Test
    public void addToCart_verifyReqiredFieldValidationErrors() {
        page.click(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_ADD_TO_CART_BUTTON));
        String sizeError = page.textContent(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_SIZE_VALIDATION_ERROR))
                .trim();
        String colorError = page.textContent(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_COLOR_VALIDATION_ERROR))
                .trim();

        final String expectedRequiredFieldError = "This is a required field.";
        Assertions.assertEquals(expectedRequiredFieldError, sizeError, "Wrong size error!");
        Assertions.assertEquals(expectedRequiredFieldError, colorError, "Wrong color error!");
    }

    @Test
    public void addToCart_selectValidOptions_verifyAddedToCart() {
        Locator sizeLocator = page.locator(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_SIZES))
                .first();
        Locator colorLocator = page.locator(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_COLORS))
                .first();
        sizeLocator.click();
        colorLocator.click();
        page.click(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_ADD_TO_CART_BUTTON));

        Locator messageLocator = page.locator(properties.getProperty(EchoFirCompressionShortKeys.PAGE_SUCCESS_MESSAGE))
                .first();

        String successMessage = messageLocator.textContent()
                .trim();

        String cartItems = page.textContent(properties.getProperty(EchoFirCompressionShortKeys.PAGE_MINI_CART_COUNTER))
                .trim();

        final String expectedMessage = "You added Echo Fit Compression Short to your shopping cart.";
        Assertions.assertEquals(expectedMessage, successMessage, "Wrong success message!");
        Assertions.assertEquals("1", cartItems, "Wrong cart size!");
    }

    /**
     * Sets the network conditions to throttle the network calls.
     * See: https://tomatoqa.com/blog/stimulate-network-throttling-in-playwright-typescript-javascript/
     *
     * @param downloadThroughput The throughput of the network connection in kb/second for downloading.
     * @param uploadThroughput   The throughput of the network connection in kb/second for uploading.
     * @param latency            The simulated latency of the connection.
     * @param isOffline          Whether the network is set to offline.  Defaults to false.
     */
    private void setNetworkConditions(Integer downloadThroughput, Integer uploadThroughput, Integer latency, Boolean isOffline) {
        NetworkConditions networkConditions = new NetworkConditions();

        if (downloadThroughput != null) {
            networkConditions.setDownloadThroughput(downloadThroughput);
        }

        if (uploadThroughput != null) {
            networkConditions.setUploadThroughput(uploadThroughput);
        }

        if (latency != null) {
            networkConditions.setLatency(latency);
        }

        if (isOffline != null) {
            networkConditions.setOffline(isOffline);
        }

        Gson gson = new Gson();
        String json = gson.toJson(networkConditions);
        log.info("*** JSON = {}", json);

        JsonObject jsonObject = JsonParser.parseString(json)
                .getAsJsonObject();

        browserContext.newCDPSession(page).send("Network.emulateNetworkConditions", jsonObject);
    }

    /**
     * This object gets passed to the 'Network.emulateNetworkConditions' Chrome CDP session.
     */
    @Data
    public static class NetworkConditions {
        private Integer downloadThroughput;
        private Integer uploadThroughput;
        private Integer latency;
        private Boolean offline;
    }
}
