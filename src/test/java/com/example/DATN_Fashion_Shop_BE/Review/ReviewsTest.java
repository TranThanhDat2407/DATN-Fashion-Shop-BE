package com.example.DATN_Fashion_Shop_BE.Review;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

public class ReviewsTest {

    WebDriver driver;
    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }


    @Test
    void insertReview() throws InterruptedException {
        driver.get("http://localhost:4200/client/VND/en/login");

        Thread.sleep(1000);

        WebElement emailInput = driver.findElement(By.name("email"));
        emailInput.sendKeys("customer1@example.com");

        WebElement passInput = driver.findElement(By.name("password"));
        passInput.sendKeys("Abc123");


        WebElement btnLogin = driver.findElement(By.className("btnLogin"));
        btnLogin.click();
        Thread.sleep(2000);

        WebElement btnCategoty = driver.findElement(By.className("btn2"));
        btnCategoty.click();
        Thread.sleep(1000);


        // Tìm tất cả các div có class 'category-item'
        List<WebElement> categoryItems = driver.findElements(By.cssSelector("div.category-item"));

        // Click vào phần tử đầu tiên
        if (!categoryItems.isEmpty()) {
            WebElement firstItem = categoryItems.get(0);

            // Cuộn đến phần tử
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstItem);
            Thread.sleep(300);

            // Click bằng JavaScript để tránh bị che
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstItem);
        }


        Thread.sleep(2000);


    }


    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
