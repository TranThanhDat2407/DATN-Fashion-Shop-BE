package com.example.DATN_Fashion_Shop_BE.Inventory;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

public class InventoryTest {

    WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    void transferInventory() throws InterruptedException {
        driver.get("http://localhost:4200/admin/login_admin");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("admin@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btn-login"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/inventory']"));
        categoryMenu.click();

        WebDriverWait waitss = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement cityDropdown = waitss.until(ExpectedConditions.elementToBeClickable(By.name("storeSelect")));
        Select select = new Select(cityDropdown);
        waitss.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("select[name='storeSelect'] option"), 0));
        select.selectByValue("2");

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement textArea = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("textarea.form-control")));
        textArea.sendKeys("Văn bản trong textarea.");

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> labels = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("product-variant-test")));

        if (labels.size() > 1) {
            labels.get(1).click();
            System.out.println("Đã nhấp vào thẻ thứ 2");
        }

        if (labels.size() > 3) {
            labels.get(3).click();
            System.out.println("Đã nhấp vào thẻ thứ 4");
        }

        Thread.sleep(2000);  // Đợi trước khi nhấn nút
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'pushable')]//span[contains(text(), 'transfer')]")
        ));

        addButton.click();
        Thread.sleep(2000);


        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Success"), "Toast title không đúng");

        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Transfer Store Successfully!"), "Toast message không đúng");
    }


    @Test
    void addInventoryToWareHouse() throws InterruptedException {
        driver.get("http://localhost:4200/admin/login_admin");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("admin@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");

        WebElement btnLogin = driver.findElement(By.className("btn-login"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/inventory']"));
        categoryMenu.click();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement label = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("label[for='tab-3']")));
        label.click();


        WebElement inputQtyInStock = driver.findElement(By.name("qtyInStock"));
        inputQtyInStock.clear();
        inputQtyInStock.sendKeys("1");


        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> labels = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("product-variant-addInventoryToWareHouse")));

        if (labels.size() > 1) {
            labels.get(1).click();
            System.out.println("Đã nhấp vào thẻ thứ 2");
        }

        if (labels.size() > 3) {
            labels.get(3).click();
            System.out.println("Đã nhấp vào thẻ thứ 4");
        }

        Thread.sleep(2000);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'pushable')]//span[contains(text(), 'APPLY')]")
        ));
        addButton.click();
        Thread.sleep(2000);

        WebElement confirmDeleteBtn = driver.findElement(By.className("btn-login"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmDeleteBtn);
        Thread.sleep(2000);


        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Success"), "Toast title không đúng");

        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Add Product Variant Successfully!"), "Toast message không đúng");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
