package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UniHannoverCustomElementsTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "https://www.uni-hannover.de/";

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get(BASE_URL + "de/");
        waitUntilPageReady();
        dismissCookieBanner();
    }

    private void waitUntilPageReady() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private void dismissCookieBanner() {
        try {
            WebElement acceptBtn = new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Akzeptieren')]")));
            acceptBtn.click();
        } catch (Exception ignored) {}
    }

    @AfterAll
    void teardown() {
        if (driver != null) driver.quit();
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    private void waitUrlMatches(String... urls) {
        wait.until(d -> {
            String current = d.getCurrentUrl();
            for (String url : urls) {
                if (current.equals(url)) return true;
            }
            return false;
        });
    }

    // Método para capturar screenshot em falhas
    private void takeScreenshot(String testName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File destFile = new File("screenshots/" + testName + "_" + timestamp + ".png");
            destFile.getParentFile().mkdirs();
            FileHandler.copy(srcFile, destFile);
            System.out.println("Screenshot salva em: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao salvar screenshot: " + e.getMessage());
        }
    }

    @Test
    void testKontaktLinkNavigation() {
        try {
            By kontaktSelector = By.xpath("//*[contains(@href, '/kontakt') and (contains(text(), 'Kontakt') or contains(., 'Kontakt'))]");
            WebElement kontaktLink = wait.until(ExpectedConditions.visibilityOfElementLocated(kontaktSelector));
            assertTrue(kontaktLink.isDisplayed(), "Link Kontakt deve estar visível");
            assertTrue(kontaktLink.isEnabled(), "Link Kontakt deve estar habilitado");
            scrollIntoView(kontaktLink);
            kontaktLink.click();

            waitUrlMatches("https://www.uni-hannover.de/de/kontakt", "https://www.uni-hannover.de/en/contact");

            // Verificar um elemento característico da página contato
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Kontakt') or contains(text(),'Contact')]")));
            assertTrue(heading.isDisplayed(), "Página de contato não carregou corretamente");
        } catch (Exception e) {
            takeScreenshot("testKontaktLinkNavigation");
            throw e;
        }
    }

    @Test
    void englishButton() {
        try {
            // XPath aprimorado para evitar dependência da posição
            By englishBtnSelector = By.xpath("//a[@href='/en/' and (text()='English' or contains(@aria-label,'English'))]");
            WebElement englishButton = wait.until(ExpectedConditions.elementToBeClickable(englishBtnSelector));
            scrollIntoView(englishButton);
            englishButton.click();

            waitUrlMatches("https://www.uni-hannover.de/en/");

            // Verificar elemento típico em inglês, por ex. título ou menu
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//nav//a[contains(text(),'English') or contains(text(),'Contact')]")));
            assertTrue(heading.isDisplayed(), "Página em inglês não carregou corretamente");
        } catch (Exception e) {
            takeScreenshot("englishButton");
            throw e;
        }
    }

    @Test
    void testVisibleLogo() {
        try {
            By logoSelector = By.xpath("//img[@alt='Logo Leibniz Universität Hannover']");
            WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(logoSelector));
            scrollIntoView(logo);
            assertTrue(logo.isDisplayed(), "Logo deve estar visível");
            wait.until(ExpectedConditions.elementToBeClickable(logo)).click();

            waitUrlMatches("https://www.uni-hannover.de/de/", "https://www.uni-hannover.de/en/");

            // Validar que carregou homepage, ex: esperar header principal
            WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("header")));
            assertTrue(header.isDisplayed(), "Página inicial não carregou após clique no logo");
        } catch (Exception e) {
            takeScreenshot("testVisibleLogo");
            throw e;
        }
    }

    @Test
    void testSearchInputVisibilityAndTyping() {
        try {
            By inputSelector = By.xpath("//input[@placeholder='Suche']");
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(inputSelector));
            scrollIntoView(searchInput);
            assertTrue(searchInput.isEnabled(), "Campo de busca deve estar habilitado");

            // Testar digitação
            String testText = "Testando";
            searchInput.clear();
            searchInput.sendKeys(testText);
            assertEquals(testText, searchInput.getAttribute("value"), "Texto digitado não corresponde");

            // Opcional: limpar e digitar de novo
            searchInput.clear();
            String testText2 = "Teste 2";
            searchInput.sendKeys(testText2);
            assertEquals(testText2, searchInput.getAttribute("value"), "Texto digitado após limpar não corresponde");
        } catch (Exception e) {
            takeScreenshot("testSearchInputVisibilityAndTyping");
            throw e;
        }
    }

    @Test
    void testSearchButton() {
        try {
            By searchButtonSelector = By.xpath("//button[@aria-label='Suche starten']");
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(searchButtonSelector));
            scrollIntoView(searchButton);
            searchButton.click();

            // Validar título e conteúdo típico de resultados
            wait.until(ExpectedConditions.or(
                ExpectedConditions.titleContains("Suche"),
                ExpectedConditions.titleContains("Search")
            ));

            // Exemplo: espera por lista de resultados ou mensagem
            WebElement resultsContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'search-results') or contains(@class,'result-list')]")));
            assertTrue(resultsContainer.isDisplayed(), "Resultados da busca não estão visíveis");
        } catch (Exception e) {
            takeScreenshot("testSearchButton");
            throw e;
        }
    }

    @Test
    void testFakultatenDropdown() {
        try {
            By fakultatenBtnSelector = By.xpath("//button[contains(@class, 'c-topbar_link') and contains(@class, 'c-topbar_dropdown-icon')]");
            WebElement fakultatenButton = wait.until(ExpectedConditions.elementToBeClickable(fakultatenBtnSelector));
            scrollIntoView(fakultatenButton);
            fakultatenButton.click();

            By dropdownMenuSelector = By.xpath("//div[contains(@class, 'c-dropdown') and contains(@class, 'c-dropdown--is-active')]");
            WebElement dropdownMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownMenuSelector));
            assertTrue(dropdownMenu.isDisplayed(), "Dropdown Fakultäten não está visível");

            // Verificar itens do dropdown (exemplo mínimo: pelo menos 1 item)
            List<WebElement> items = dropdownMenu.findElements(By.xpath(".//a"));
            assertFalse(items.isEmpty(), "Dropdown Fakultäten está vazio");

            // Testar toggle: fechar clicando fora
            ((JavascriptExecutor) driver).executeScript("document.body.click();");
            wait.until(ExpectedConditions.invisibilityOf(dropdownMenu));
        } catch (Exception e) {
            takeScreenshot("testFakultatenDropdown");
            throw e;
        }
    }

    @Test
    void testStudIPShortcutMenuItem() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("span.c-shortcut-menu__item--inner")));
            List<WebElement> spans = driver.findElements(By.cssSelector("span.c-shortcut-menu__item--inner"));
            WebElement targetSpan = spans.stream()
                .filter(s -> s.getText().trim().equalsIgnoreCase("Stud.IP") || s.getText().toLowerCase().startsWith("studip"))
                .findFirst().orElse(null);
            assertNotNull(targetSpan, "Item 'Stud.IP' não encontrado");
            scrollIntoView(targetSpan);
            assertTrue(targetSpan.isDisplayed(), "Item 'Stud.IP' deve estar visível");

            WebElement parentLink = targetSpan.findElement(By.xpath("./ancestor::a"));
            assertNotNull(parentLink, "Link pai de 'Stud.IP' não encontrado");
            assertTrue(parentLink.isEnabled(), "Link 'Stud.IP' deve estar habilitado");
            String href = parentLink.getAttribute("href");
            assertNotNull(href, "Href do link 'Stud.IP' é nulo");
            assertTrue(href.toLowerCase().contains("studip"), "Href do link 'Stud.IP' não contém 'studip'");

            // Opcional: clicar e validar navegação
            String originalWindow = driver.getWindowHandle();
            parentLink.click();
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    assertTrue(driver.getCurrentUrl().toLowerCase().contains("studip"),
                        "URL da nova aba não contém 'studip'");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        } catch (Exception e) {
            takeScreenshot("testStudIPShortcutMenuItem");
            throw e;
        }
    }

    @Test
    void testInternationaleTagungenButton() {
        try {
            List<WebElement> buttons = driver.findElements(By.cssSelector("a.c-button[href*='internationale-tagungen']"));
            WebElement targetButton = buttons.stream()
                .filter(b -> b.getText().trim().equalsIgnoreCase("Alle internationalen Tagungen anzeigen"))
                .findFirst().orElse(null);
            assertNotNull(targetButton, "Botão 'Alle internationalen Tagungen anzeigen' não encontrado");
            scrollIntoView(targetButton);
            wait.until(ExpectedConditions.elementToBeClickable(targetButton));
            assertTrue(targetButton.isDisplayed(), "Botão deve estar visível");
            assertTrue(targetButton.isEnabled(), "Botão deve estar habilitado");

            // Testar clique e navegação
            targetButton.click();
            waitUrlMatches("https://www.uni-hannover.de/de/universitaet/aktuelles/veranstaltungen/internationale-tagungen");

            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'internationale') or contains(text(),'International')]")));
            assertTrue(heading.isDisplayed(), "Página de eventos internacionais não carregou corretamente");
        } catch (Exception e) {
            takeScreenshot("testInternationaleTagungenButton");
            throw e;
        }
    }

    @Test
    void testFooterLinkLeibnizUni() {
        try {
            List<WebElement> links = driver.findElements(By.cssSelector("a.o-footer__link[href='/de/copyright']"));
            WebElement targetLink = links.stream()
                .filter(l -> {
                    String txt = l.getText().trim();
                    return txt.contains("Leibniz Universität Hannover") || txt.contains("© Leibniz Universität Hannover");
                }).findFirst().orElse(null);
            assertNotNull(targetLink, "Link do rodapé 'Leibniz Universität Hannover' não encontrado");
            scrollIntoView(targetLink);
            wait.until(ExpectedConditions.elementToBeClickable(targetLink));
            assertTrue(targetLink.isDisplayed(), "Link do rodapé deve estar visível");
            assertTrue(targetLink.isEnabled(), "Link do rodapé deve estar habilitado");
            assertEquals("https://www.uni-hannover.de/de/copyright", targetLink.getAttribute("href"),
                "URL do link do rodapé está incorreta");

            // Opcional: clicar e validar navegação
            targetLink.click();
            waitUrlMatches("https://www.uni-hannover.de/de/copyright");
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Copyright') or contains(text(),'Urheberrecht')]")));
            assertTrue(heading.isDisplayed(), "Página de copyright não carregou corretamente");
        } catch (Exception e) {
            takeScreenshot("testFooterLinkLeibnizUni");
            throw e;
        }
    }

    @Test
    void testSectionElementsByClass() {
        try {
            List<WebElement> els = driver.findElements(By.className("c-section__element"));
            assertFalse(els.isEmpty(), "Nenhum elemento com classe 'c-section__element' encontrado");
            assertTrue(els.stream().anyMatch(WebElement::isDisplayed), "Nenhum elemento 'c-section__element' está visível");
            assertTrue(els.stream().anyMatch(el -> !el.findElements(By.tagName("h2")).isEmpty()),
                "Nenhum elemento 'c-section__element' contém título <h2>");
        } catch (Exception e) {
            takeScreenshot("testSectionElementsByClass");
            throw e;
        }
    }

    @Test
    void testSectionHeadlinesByClass() {
        try {
            List<WebElement> els = driver.findElements(By.className("c-headline--h2"));
            assertFalse(els.isEmpty(), "Nenhum elemento com classe 'c-headline--h2' encontrado");
            assertTrue(els.stream().anyMatch(WebElement::isDisplayed), "Nenhum título 'c-headline--h2' está visível");
            for (WebElement el : els) {
                assertFalse(el.getText().trim().isEmpty(), "Algum título 'c-headline--h2' está vazio");
            }
        } catch (Exception e) {
            takeScreenshot("testSectionHeadlinesByClass");
            throw e;
        }
    }

    @Test
    void testGridColumnsByClass() {
        try {
            List<WebElement> els = driver.findElements(By.className("c-section-grid__column"));
            assertFalse(els.isEmpty(), "Nenhuma coluna com classe 'c-section-grid__column' encontrada");
            assertTrue(els.stream().anyMatch(WebElement::isDisplayed), "Nenhuma coluna está visível");
            assertTrue(els.stream().anyMatch(el -> !el.findElements(By.tagName("p")).isEmpty() || !el.findElements(By.tagName("h2")).isEmpty()),
                "Nenhuma coluna contém parágrafo <p> ou título <h2>");
        } catch (Exception e) {
            takeScreenshot("testGridColumnsByClass");
            throw e;
        }
    }

    @Test
    void testExternalFacebookLink() {
        try {
            WebElement fbLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='facebook.com']")));
            scrollIntoView(fbLink);
            assertTrue(fbLink.isEnabled(), "Link do Facebook deve estar habilitado");
            assertEquals("_blank", fbLink.getAttribute("target"), "Link do Facebook deve abrir em nova aba");
            assertTrue(fbLink.getAttribute("href").contains("facebook.com"), "Link do Facebook está incorreto");

            // Opcional: abrir nova aba e validar domínio
            String originalWindow = driver.getWindowHandle();
            fbLink.click();
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Nova aba não contém facebook.com");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        } catch (Exception e) {
            takeScreenshot("testExternalFacebookLink");
            throw e;
        }
    }

    @Test
    void testScrollToTopButtonFunctionality() {
        try {
            By selector = By.xpath("//div[contains(@class, 'js-scroll-to-top')]");
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

            // Esperar botão visível
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(selector));
            scrollIntoView(button);
            button.click();

            // Esperar scroll até topo
            wait.until(d -> (Long)((JavascriptExecutor) d).executeScript("return window.scrollY;") == 0L);
        } catch (Exception e) {
            takeScreenshot("testScrollToTopButtonFunctionality");
            throw e;
        }
    }

    @Test
    void testHomeButton() {
        try {
            WebElement userIcon = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class, 'u-icon--users4')]")));
            scrollIntoView(userIcon);
            userIcon.click();

            WebElement homeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(@class, 'u-icon--home2')]")));
            scrollIntoView(homeBtn);
            homeBtn.click();

            waitUrlMatches("https://www.uni-hannover.de/de/", "https://www.uni-hannover.de/en/");

            // Validar elemento típico da homepage
            WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("header")));
            assertTrue(header.isDisplayed(), "Página inicial não carregou após clique no botão home");
        } catch (Exception e) {
            takeScreenshot("testHomeButton");
            throw e;
        }
    }

}
