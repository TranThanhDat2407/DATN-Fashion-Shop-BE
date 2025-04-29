package com.example.DATN_Fashion_Shop_BE.Attribute;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

public class AttributeTest {

    WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }
    //Color
    @Test
    void insertColor () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_attribute']"));
        categoryMenu.click();
        Thread.sleep(1000);


        WebElement buttonIcon = driver.findElement(By.className("button__icon"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonIcon);
        Thread.sleep(2000);

        WebElement inputColor = driver.findElement(By.name("nameColor"));
        inputColor.sendKeys("12_PINK_TM4 Test");
        Thread.sleep(1000);
        WebElement fileInput = driver.findElement(By.cssSelector("input[type='file']"));
        fileInput.sendKeys("C:\\Users\\Admin\\Pictures\\product\\color\\12_PINK_TM4.png");
        Thread.sleep(1000);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(@class, 'pushable')]//span[contains(text(), 'Add')])[1]")
        ));
        addButton.click();
        Thread.sleep(2000);

        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));
// Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Color created successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }
    @Test
    void updateColor () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_attribute']"));
        categoryMenu.click();
        Thread.sleep(1000);

        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("12_PINK_TM4 Test");
        Thread.sleep(3000);

        List<WebElement> buttonIcon = driver.findElements(By.className("btn-edit"));
        WebElement firstBtn = buttonIcon.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstBtn);
        Thread.sleep(2000);

        WebElement inputColor = driver.findElement(By.name("nameColor"));
        inputColor.clear();
        inputColor.sendKeys("12_PINK_TM4 Update Test");
        Thread.sleep(1000);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(@class, 'pushable')]//span[contains(text(), 'Save')])[1]")
        ));
        addButton.click();
        Thread.sleep(2000);

        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));
// Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Color updated successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }

    @Test
    void deleteColor  () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_attribute']"));
        categoryMenu.click();
        Thread.sleep(1000);

        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("12_PINK_TM4 Test");
        Thread.sleep(3000);

        List<WebElement> buttonIcon = driver.findElements(By.className("btn-delete"));
        WebElement firstBtn = buttonIcon.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstBtn);
        Thread.sleep(2000);

        WebElement confirmDeleteBtn = driver.findElement(By.className("btn-login"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmDeleteBtn);
        Thread.sleep(2000);



        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

// Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Color Deleted successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }


    //Size
    @Test
    void insertSize () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_attribute']"));
        categoryMenu.click();
        Thread.sleep(1000);


        WebElement buttonIcon = driver.findElement(By.className("button__icon"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonIcon);
        Thread.sleep(2000);

        WebElement inputSize = driver.findElement(By.name("nameSize"));
        inputSize.sendKeys("XXL Test");
        Thread.sleep(1000);


        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(@class, 'pushable')]//span[contains(text(), 'Add')])[2]")
        ));
        addButton.click();
        Thread.sleep(2000);

        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));
// Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Size created successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }
    @Test
    void updateSize () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_attribute']"));
        categoryMenu.click();
        Thread.sleep(1000);

        WebElement sizeListLabel = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(text(), 'Size List')]")
        ));
        sizeListLabel.click();

        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("XXL Test");
        Thread.sleep(3000);

        List<WebElement> buttonIcon = driver.findElements(By.className("btn-edit"));
        WebElement lastBtn = buttonIcon.get(buttonIcon.size() - 1);  // Chọn nút cuối cùng
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", lastBtn);
        Thread.sleep(2000);


        WebElement inputSize = driver.findElement(By.name("nameSize"));
        inputSize.clear();
        inputSize.sendKeys("XXL Update Test");
        Thread.sleep(1000);

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//button[contains(@class, 'pushable')]//span[contains(text(), 'Save')])[2]")
        ));
        saveButton.click();
        Thread.sleep(2000);

        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));
// Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Size updated successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }

    @Test
    void deleteSize () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_attribute']"));
        categoryMenu.click();
        Thread.sleep(1000);

        WebElement tab2Label = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[@for='tab-2']")
        ));
        tab2Label.click();

        WebElement inputNameSearch = driver.findElement(By.name("nameSearch"));
        inputNameSearch.sendKeys("XXL Update Test");
        Thread.sleep(3000);


        List<WebElement> buttonIcon = driver.findElements(By.className("btn-delete"));
        WebElement lastBtn = buttonIcon.get(buttonIcon.size() - 1);  // Chọn nút cuối cùng
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", lastBtn);
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
        Assert.assertTrue(titleText.contains("Size Deleted successfully!"), "Toast title không đúng");

        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }


}
