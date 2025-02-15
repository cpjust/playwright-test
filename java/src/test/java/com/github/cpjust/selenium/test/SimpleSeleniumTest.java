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
package com.github.cpjust.selenium.test;

import com.github.cpjust.constants.EchoFirCompressionShortKeys;
import com.github.cpjust.selenium.SeleniumTestBase;
import com.github.cpjust.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumNetworkConditions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class SimpleSeleniumTest extends SeleniumTestBase {
    private Properties properties;

    @BeforeClass
    public void beforeClass() throws IOException {
        PropertyReader propertyReader = new PropertyReader();
        properties = propertyReader.getPropertiesFromResources("locators/magento.softwaretestingboard.com/EchoFirCompressionShort.properties");
    }

    @BeforeMethod
    public void beforeMethod() {
        driver.get(properties.getProperty(EchoFirCompressionShortKeys.URL));

        // Throttle the network speed.
        setNetworkConditions(10000, null, Duration.ofSeconds(5), null);
    }

    @Test
    public void checkProductInfo_verifyTitleAndPrice() {
        WebElement element = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PAGE_TITLE)));
        String title = element.getText()
                .trim();
        element = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_AVAILABILITY)));
        String availability = element.getAttribute("textContent")//.getText()
                .trim();
        element = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_PRICE)));
        String price = element.getText();

        Assertions.assertEquals("Echo Fit Compression Short", title, "Wrong title!");
        Assertions.assertEquals("In stock", availability, "Wrong availability!");
        Assertions.assertEquals("$24.00", price, "Wrong title!");
    }

    @Test
    public void addToCart_verifyReqiredFieldValidationErrors() {
        WebElement element = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_ADD_TO_CART_BUTTON)));
        element.click();
        element = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_SIZE_VALIDATION_ERROR)));
        String sizeError = element.getText()
                .trim();
        element = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_COLOR_VALIDATION_ERROR)));
        String colorError = element.getText()
                .trim();

        final String expectedRequiredFieldError = "This is a required field.";
        Assertions.assertEquals(expectedRequiredFieldError, sizeError, "Wrong size error!");
        Assertions.assertEquals(expectedRequiredFieldError, colorError, "Wrong color error!");
    }

    @Test
    public void addToCart_selectValidOptions_verifyAddedToCart() {
        WebElement sizeElement = driver.findElements(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_SIZES)))
                .get(0);
        WebElement colorElement = driver.findElements(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_COLORS)))
                .get(0);
        sizeElement.click();
        colorElement.click();
        WebElement addToCartButton = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PRODUCT_ADD_TO_CART_BUTTON)));
        addToCartButton.click();

        By messageLocator = By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PAGE_SUCCESS_MESSAGE));
        WebElement messageElement = driver.findElements(messageLocator)
                .get(0);

        String successMessage = messageElement.getText()
                .trim();

        WebElement miniCartCounter = driver.findElement(By.cssSelector(properties.getProperty(EchoFirCompressionShortKeys.PAGE_MINI_CART_COUNTER)));
        String cartItems = miniCartCounter.getText()
                .trim();

        final String expectedMessage = "You added Echo Fit Compression Short to your shopping cart.";
        Assertions.assertEquals(expectedMessage, successMessage, "Wrong success message!");
        Assertions.assertEquals("1", cartItems, "Wrong cart size!");
    }

    /**
     * Sets the network conditions such as throttling.
     *
     * @param downloadThroughput The throughput of the network connection in kb/second for downloading.
     * @param uploadThroughput   The throughput of the network connection in kb/second for uploading.
     * @param latency            The simulated latency of the connection.
     * @param isOffline          Whether the network is set to offline.  Defaults to false.
     */
    private void setNetworkConditions(Integer downloadThroughput, Integer uploadThroughput, Duration latency, Boolean isOffline) {
        ChromeDriver chromeDriver = (ChromeDriver) driver;
        ChromiumNetworkConditions networkConditions = new ChromiumNetworkConditions();

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

        chromeDriver.setNetworkConditions(networkConditions);
    }

    /**
     * Attempts to set the network conditions like we do in Playwright, but for some reason it doesn't seem to work.
     * See: https://tomatoqa.com/blog/stimulate-network-throttling-in-playwright-typescript-javascript/
     *
     * @param downloadThroughput The throughput of the network connection in kb/second for downloading.
     * @param uploadThroughput   The throughput of the network connection in kb/second for uploading.
     * @param latency            The simulated latency of the connection.
     * @param isOffline          Whether the network is set to offline.  Defaults to false.
     */
    private void setNetworkConditionsByCdpCommand(Integer downloadThroughput, Integer uploadThroughput, Integer latency, Boolean isOffline) {
        ChromeDriver chromeDriver = (ChromeDriver) driver;

        Map<String, Object> params = new HashMap<>();
        params.put("downloadThroughput", downloadThroughput);
        params.put("uploadThroughput", uploadThroughput);
        params.put("latency", latency);
        params.put("offline", isOffline);

        Map<String, Object> ret = chromeDriver.executeCdpCommand("Network.emulateNetworkConditions", params);
        log.info("executeCdpCommand() returned: {}", ret);
    }
}
