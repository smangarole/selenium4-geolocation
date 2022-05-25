package com.browserstack;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v95.emulation.Emulation;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Main {

    public static String REMOTE_URL = "https://siddharthamdemo1:3iskPrkpEsseiwerAxKB@hub-cloud.browserstack.com/wd/hub";

    public static void main(String[] args) throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("browserVersion", "95.0");
        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("googlegeolocationaccess.enabled", true);
        prefs.put("profile.default_content_setting_values.geolocation", 1); // 1:allow 2:block
        prefs.put("profile.default_content_setting_values.notifications", 1);
        prefs.put("profile.managed_default_content_settings", 1);
        options.setExperimentalOption("prefs", prefs);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        HashMap<String, Object> browserstackOptions = new HashMap<String, Object>();
        browserstackOptions.put("os", "Windows");
        browserstackOptions.put("osVersion", "8");
        browserstackOptions.put("sessionName", "Emulate Geo Location");
        browserstackOptions.put("seleniumVersion", "4.1.2");
        browserstackOptions.put("seleniumCdp", true);
        capabilities.setCapability("bstack:options", browserstackOptions);

        WebDriver driver = new RemoteWebDriver(new URL(REMOTE_URL), capabilities);

        try {
            Augmenter augmenter = new Augmenter();
            driver = augmenter.augment(driver);

            DevTools devTools = ((HasDevTools) driver).getDevTools();
            devTools.createSession();

            // setGeolocationOverride() takes lattitude, longitude, and accuracy as parameters.
            devTools.send(Emulation.setGeolocationOverride(Optional.of(43.651070),
                    Optional.of(-79.347015),
                    Optional.of(1)));
            driver.get("https://my-location.org");


            driver.findElement(By.id("loginUsername_login-modal")).sendKeys("");
            driver.findElement(By.id("loginPassword_login-modal")).sendKeys("");

            String address = driver.findElement(By.id("address")).getText();
            System.out.println(address);
            if (address.contains("Toronto")) {
                markTestStatus("passed", "Location is in Toronto", driver);
            } else {
                markTestStatus("failed", "Location is not in Toronto", driver);
            }
            driver.quit();

        } catch (Exception e) {
            markTestStatus("failed", "Exception!", driver);
            e.printStackTrace();
            driver.quit();
        }
    }

    public static void markTestStatus(String status, String reason, WebDriver driver) {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \""+status+"\", \"reason\": \""+reason+"\"}}");
    }
}
