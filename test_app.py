"""Test the form example app with Playwright."""
from playwright.sync_api import sync_playwright
import urllib.request

def reset_state():
    req = urllib.request.Request("http://localhost:8080/reset", data=b"", method="POST")
    urllib.request.urlopen(req)

def test_plain_page():
    reset_state()
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        # Test plain page loads
        page.goto("http://localhost:8080/plain")
        assert page.title() == "Plain HTML Forms"
        assert page.locator("h1").text_content() == "Plain HTML Forms"

        # Check nav links
        assert page.locator("nav a").count() == 3

        # Check 50 items in table
        rows = page.locator("tbody tr")
        assert rows.count() == 50

        # Check quantity fields default to 0
        first_qty = page.locator("input[name='qty-0']")
        assert first_qty.input_value() == "0"

        # Test increment button
        page.locator("button:text('+')").first.click()
        page.wait_for_load_state("networkidle")
        first_qty = page.locator("input[name='qty-0']")
        assert first_qty.input_value() == "1", f"Expected 1, got {first_qty.input_value()}"

        # Test decrement button
        page.locator("button:text('-')").first.click()
        page.wait_for_load_state("networkidle")
        first_qty = page.locator("input[name='qty-0']")
        assert first_qty.input_value() == "0", f"Expected 0, got {first_qty.input_value()}"

        # Test save with modified quantity
        page.locator("input[name='qty-2']").fill("42")
        page.locator("button:text('Save')").first.click()
        page.wait_for_load_state("networkidle")
        assert page.locator("input[name='qty-2']").input_value() == "42"

        # Test settings form
        page.locator("#text-field").fill("hello world")
        page.locator("#select-field").select_option("option-b")
        page.locator("#date-field").fill("2026-01-15")
        page.locator("button:text('Save Settings')").click()
        page.wait_for_load_state("networkidle")
        assert page.locator("#text-field").input_value() == "hello world"
        assert page.locator("#select-field").input_value() == "option-b"
        assert page.locator("#date-field").input_value() == "2026-01-15"

        print("PASS: Plain HTML page")
        browser.close()

def test_htmx_page():
    reset_state()
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        page.goto("http://localhost:8080/htmx")
        assert page.title() == "HTMX + Idiomorph"
        assert page.locator("h1").text_content() == "HTMX + Idiomorph"

        # Check htmx is loaded
        assert page.locator("script[src*='htmx.org']").count() == 1
        assert page.locator("script[src*='idiomorph']").count() == 1

        # Check boost mode
        assert page.locator("body[hx-boost='true']").count() == 1

        # Check 50 items
        assert page.locator("tbody tr").count() == 50

        # Test increment (htmx boost intercepts the form post)
        page.locator("button:text('+')").first.click()
        page.wait_for_timeout(1000)
        first_qty = page.locator("input[name='qty-0']")
        assert first_qty.input_value() == "1", f"Expected 1, got {first_qty.input_value()}"

        print("PASS: HTMX page")
        browser.close()

def test_datastar_page():
    reset_state()
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        page.goto("http://localhost:8080/datastar")
        assert page.title() == "Datastar"
        assert page.locator("h1").text_content() == "Datastar"

        # Check 50 items
        assert page.locator("tbody tr").count() == 50

        # Test increment via datastar
        page.locator("button:text('+')").first.click()
        page.wait_for_timeout(2000)
        first_qty = page.locator("input[name='qty-0']")
        val = first_qty.input_value()
        assert val == "1", f"Expected 1, got {val}"

        # Test decrement
        page.locator("button:text('-')").first.click()
        page.wait_for_timeout(2000)
        first_qty = page.locator("input[name='qty-0']")
        val = first_qty.input_value()
        assert val == "0", f"Expected 0, got {val}"

        print("PASS: Datastar page")
        browser.close()

def test_navigation():
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()

        # Test root redirect
        page.goto("http://localhost:8080/")
        page.wait_for_url("**/plain")
        assert "/plain" in page.url

        # Navigate to htmx
        page.click("a:text('HTMX')")
        page.wait_for_url("**/htmx")
        assert "/htmx" in page.url

        # Navigate to datastar
        page.click("a:text('Datastar')")
        page.wait_for_url("**/datastar")
        assert "/datastar" in page.url

        print("PASS: Navigation")
        browser.close()

if __name__ == "__main__":
    test_navigation()
    test_plain_page()
    test_htmx_page()
    test_datastar_page()
    print("\nAll tests passed!")
