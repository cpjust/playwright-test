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
package com.github.cpjust.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.util.Collections;

public class PlaywrightTestBase {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext browserContext;
    protected Page page;

    @BeforeClass(alwaysRun = true)
    public void beforePlaywrightTestBaseClass() {
        playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setHeadless(false);
        launchOptions.setArgs(Collections.singletonList("--start-maximized"));
        browser = playwright.chromium().launch(launchOptions);
    }

    @BeforeMethod
    public void beforePlaywrightTestBaseMethod() {
        // NOTE: To maximize the browser, we need to set the viewport size to null.
        // See: https://stackoverflow.com/questions/77278023/how-to-maximize-the-window-size-in-playwright
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        contextOptions.setViewportSize(null);
        browserContext = browser.newContext(contextOptions);
        page = browserContext.newPage();
    }

    @AfterMethod
    public void afterPlaywrightTestBaseMethod() {
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
    public void afterPlaywrightTestBaseClass() {
        if (browser != null) {
            browser.close();
            browser = null;
        }

        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}
