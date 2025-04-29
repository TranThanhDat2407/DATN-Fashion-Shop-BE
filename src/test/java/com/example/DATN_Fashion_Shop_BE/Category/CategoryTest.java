package com.example.DATN_Fashion_Shop_BE.Category;

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

public class CategoryTest {
    WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    void insertCategory () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_category']"));
        categoryMenu.click();
        Thread.sleep(1000);


        WebElement buttonIcon = driver.findElement(By.className("button__icon"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", buttonIcon);
        Thread.sleep(2000);

        WebDriverWait waitS2 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement selectedItem = waitS2.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".custom-select .selected-item")
        ));
        selectedItem.click();
        List<WebElement> categoryOptions = waitS2.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector(".custom-select .dropdown ul li")
        ));
        if (!categoryOptions.isEmpty()) {
            categoryOptions.get(0).click();
            Thread.sleep(1000);
        } else {
            System.out.println("Không tìm thấy category nào!");
        }


        WebDriverWait waitS3  = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement childSelectButton = waitS3.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[text()='Category Child']/following-sibling::div//div[@class='selected-item']")
        ));
        childSelectButton.click();
        List<WebElement> childCategoryOptions = waitS3.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//label[text()='Category Child']/following-sibling::div//ul/li")
        ));
        if (!childCategoryOptions.isEmpty()) {
            childCategoryOptions.get(0).click();
            Thread.sleep(1000);

        } else {
            System.out.println("Không tìm thấy category con nào!");
        }

        WebDriverWait waitS4 = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement subChildSelectBtn = waitS4.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[text()='Category Sub Child']/following-sibling::div//div[@class='selected-item']")
        ));
        subChildSelectBtn.click();
        List<WebElement> subChildCategoryOptions = waitS4.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//label[text()='Category Sub Child']/following-sibling::div//ul/li")
        ));
        if (!subChildCategoryOptions.isEmpty()) {
            subChildCategoryOptions.get(0).click();
            Thread.sleep(1000);
        } else {
            System.out.println("Không có category sub child nào!");
        }

        WebElement inputEn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='English']")
        ));
        inputEn.sendKeys("Shirt");
        Thread.sleep(1000);

// Kiểm tra nếu ô English trống
        if (inputEn.getAttribute("value").isEmpty()) {
            throw new AssertionError("English input field is empty! Test failed.");
        }

        WebElement inputVi = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Vietnamese']")
        ));
        inputVi.sendKeys("Áo sơ mi");
        Thread.sleep(1000);

// Kiểm tra nếu ô Vietnamese trống
        if (inputVi.getAttribute("value").isEmpty()) {
            throw new AssertionError("Vietnamese input field is empty! Test failed.");
        }

        WebElement inputJp = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Japanese']")
        ));
        inputJp.sendKeys("シャツ");  // nghĩa là "Shirt" trong tiếng Nhật
        Thread.sleep(1000);

// Kiểm tra nếu ô Japanese trống
        if (inputJp.getAttribute("value").isEmpty()) {
            throw new AssertionError("Japanese input field is empty! Test failed.");
        }

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'pushable')]//span[contains(text(), 'Add')]")
        ));



        WebElement fileInput = driver.findElement(By.cssSelector("input[type='file']"));
        fileInput.sendKeys("C:\\Users\\Admin\\Pictures\\pr\\product-demo2.png");
        Thread.sleep(1000);

// Kiểm tra nếu tệp đã được chọn thành công
        String filePath = fileInput.getAttribute("value");
        if (filePath.isEmpty() || !filePath.contains("product-demo2.png")) {
            throw new AssertionError("File upload failed! Test failed.");
        } else {
            System.out.println("File uploaded successfully: " + filePath);
        }




        addButton.click();
        Thread.sleep(2000);


        WebElement toastMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".toast-message")));  // Cập nhật đúng CSS selector của toast
        Assert.assertTrue(toastMessage.isDisplayed(), "Toast không hiển thị.");
        String toastText = toastMessage.getText();
        System.out.println("Toast message: " + toastText);  // In ra nội dung Toast
        Assert.assertTrue(toastText.contains("Success"), "Error");



    }

    @Test
    void updateCategory () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_category']"));
        categoryMenu.click();
        Thread.sleep(1000);

        List<WebElement> buttonIcon = driver.findElements(By.className("btn-edit"));
        WebElement firstBtn = buttonIcon.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstBtn);
        Thread.sleep(2000);


        WebElement inputEn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='English']")
        ));
        inputEn.sendKeys("Shirt Test Update");
        Thread.sleep(1000);


        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'pushable')]//span[contains(text(), 'Save')]")
        ));

        saveButton.click();
        Thread.sleep(2000);


        // Đợi toast hiển thị
        WebElement toastContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#toast-container .toast-success")
        ));

// Lấy nội dung của toast title
        WebElement toastTitle = toastContainer.findElement(By.cssSelector(".toast-title"));
        String titleText = toastTitle.getText();
        System.out.println("Toast title: " + titleText);
        Assert.assertTrue(titleText.contains("Category updated successfully"), "Toast title không đúng");

// Lấy nội dung của toast message
        WebElement toastMessage = toastContainer.findElement(By.cssSelector(".toast-message"));
        String messageText = toastMessage.getText();
        System.out.println("Toast message: " + messageText);
        Assert.assertTrue(messageText.contains("Success"), "Toast message không đúng");

    }


    @Test
    void deleteCategory () throws InterruptedException {
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

        WebElement categoryMenu = driver.findElement(By.cssSelector("div[routerlink='/admin/list_category']"));
        categoryMenu.click();
        Thread.sleep(1000);

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
        Assert.assertTrue(titleText.contains("Category Deleted successfully!"), "Toast title không đúng");

// Lấy nội dung của toast message
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
