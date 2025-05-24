package TestCase;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Lazada {
    WebDriver driver;

    @BeforeClass
    public void beforeClass() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(20,TimeUnit.SECONDS);
        driver.manage().window().maximize();
    }


    @Test
    public void TC_01_Test_Automation() throws InterruptedException {
        System.out.println("Step 01: Open lazada page");
        driver.get("https://www.lazada.co.id/");
        Thread.sleep(3000);


        System.out.println("Step 02: Refer to lazada Viet Name");
        driver.findElement(By.xpath("//a[@class='lzd-footer-country country-vn ']")).click();
        Thread.sleep(3000);

        System.out.println("Step 03: Search product 'Logitech Keyboard'");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='search']")));
        search.sendKeys("Logitech Keyboard");
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.ENTER).perform();


        System.out.println("Step 04: Set the price filter from 150.000 to 4.000.000");
        WebElement minInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Min']")));
        WebElement maxInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Max']")));
        minInput.sendKeys("150000");
        maxInput.sendKeys("4000000");
        driver.findElement(By.xpath("//button[contains(@class,'ant-btn-icon-only')]")).click();


        System.out.println("Step 05: Sort item to 'Harga Rendah ke Tinggi' or 'Low price to high'");
        driver.findElement(By.xpath("//div[@class='ant-select-selector']")).click();
        driver.findElement(By.xpath("//div[text()='Price low to high']/parent::div/parent::div")).click();


        System.out.println("Step 06: Obtain all the item names from page 1 to page 3");

        List<String> getItemName = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            // Click vào nút phân trang
            String paginationXpath = String.format("//ul[@class='ant-pagination css-1bkhbmc app']//a[text()='%s']", i);
            WebElement pageButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(paginationXpath)));
            pageButton.click();

            // Đợi sản phẩm đầu tiên khác với sản phẩm trước (nếu có)
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='RfADt']//a")));
            Thread.sleep(2000); // hoặc dùng ExpectedCondition để đợi nội dung thay đổi nếu bạn có tham chiếu so sánh

            List<WebElement> itemNameList = driver.findElements(By.xpath("//div[@class='RfADt']//a"));
            System.out.println("Page " + i + " found " + itemNameList.size() + " items");

            for (WebElement itemName : itemNameList) {
                String name = itemName.getText().trim();
                if (!name.isEmpty()) {
                    getItemName.add(name);
                }
            }
        }
        System.out.println("All collected item names: ");
        getItemName.forEach(System.out::println);
        exportToExcel(getItemName);
    }

    public void exportToExcel(List<String> itemNames) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Product List");

        // Tạo hàng tiêu đề
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Item Name");

        // Ghi từng dòng sản phẩm
        for (int i = 0; i < itemNames.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Cell cell = row.createCell(0);
            cell.setCellValue(itemNames.get(i));
        }

        // Tự động điều chỉnh cột
        sheet.autoSizeColumn(0);

        try (FileOutputStream fileOut = new FileOutputStream("ItemNames.xlsx")) {
            workbook.write(fileOut);
            workbook.close();
            System.out.println("✅ Excel file created: ItemNames.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @AfterClass
    public void afterClass() {
        driver.quit();
    }
}
