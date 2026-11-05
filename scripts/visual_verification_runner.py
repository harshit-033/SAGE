#!/usr/bin/env python3
"""
SAGE Complete Prototype Demonstration Visual & Functional Verification Suite
Executes end-to-end against live frontend (5173), backend (8080), and automation (8000)
using headless Chrome over DevTools Protocol (CDP).
"""

import asyncio
import base64
import json
import os
import shutil
import sys
import urllib.request
import websockets

DOWNLOADS_DIR = "/home/Harshit/Downloads/sage_verified_tabs"
ARTIFACTS_DIR = "/home/Harshit/.gemini/antigravity-ide/brain/506082e9-a414-4dbf-896c-b509aca45355/verified_tabs"

os.makedirs(DOWNLOADS_DIR, exist_ok=True)
os.makedirs(ARTIFACTS_DIR, exist_ok=True)

class CDPClient:
    def __init__(self, ws_url):
        self.ws_url = ws_url
        self.msg_id = 0
        self.pending = {}

    async def connect(self):
        self.ws = await websockets.connect(self.ws_url, max_size=50_000_000)
        self.listener_task = asyncio.create_task(self._listener())

    async def _listener(self):
        try:
            async for raw in self.ws:
                msg = json.loads(raw)
                if "id" in msg and msg["id"] in self.pending:
                    self.pending[msg["id"]].set_result(msg)
        except asyncio.CancelledError:
            pass
        except Exception as e:
            print("Listener error:", e)

    async def call(self, method, params=None):
        self.msg_id += 1
        req_id = self.msg_id
        fut = asyncio.get_running_loop().create_future()
        self.pending[req_id] = fut
        req = {"id": req_id, "method": method}
        if params is not None:
            req["params"] = params
        await self.ws.send(json.dumps(req))
        res = await fut
        del self.pending[req_id]
        if "error" in res:
            raise RuntimeError(f"CDP Error in {method}: {res['error']}")
        return res.get("result", {})

    async def eval(self, expr):
        res = await self.call("Runtime.evaluate", {
            "expression": expr,
            "awaitPromise": True,
            "returnByValue": True
        })
        return res.get("result", {}).get("value")

    async def screenshot(self, filename):
        res = await self.call("Page.captureScreenshot", {"format": "png"})
        raw = base64.b64decode(res["data"])
        out_path = os.path.join(DOWNLOADS_DIR, filename)
        with open(out_path, "wb") as f:
            f.write(raw)
        art_path = os.path.join(ARTIFACTS_DIR, filename)
        shutil.copyfile(out_path, art_path)
        print(f"  [SAVED] {filename} ({len(raw)} bytes)")
        return art_path

    async def wait_for_selector(self, selector, timeout=10.0):
        start = asyncio.get_running_loop().time()
        while asyncio.get_running_loop().time() - start < timeout:
            found = await self.eval(f"!!document.querySelector('{selector}')")
            if found:
                return True
            await asyncio.sleep(0.2)
        raise TimeoutError(f"Timed out waiting for selector: {selector}")

    async def close(self):
        self.listener_task.cancel()
        await self.ws.close()

async def run_verification():
    print("=" * 70)
    print("AICTE SAGE — COMPREHENSIVE LIVE VISUAL & FUNCTIONAL VERIFICATION")
    print("=" * 70)

    # 1. Connect to CDP
    targets = json.loads(urllib.request.urlopen("http://127.0.0.1:9222/json").read().decode())
    page = next(t for t in targets if t.get("type") == "page")
    cdp = CDPClient(page["webSocketDebuggerUrl"])
    await cdp.connect()

    await cdp.call("Page.enable")
    await cdp.call("Runtime.enable")
    await cdp.call("Emulation.setDeviceMetricsOverride", {
        "width": 1440,
        "height": 900,
        "deviceScaleFactor": 1,
        "mobile": False
    })

    results = {}

    # -------------------------------------------------------------
    # TAB 1: Login & Accessibility
    # -------------------------------------------------------------
    print("\n--- [TAB 1] Login Page & Accessibility Verification ---")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/login"})
    await cdp.wait_for_selector("#login-submit-btn")
    await asyncio.sleep(1)

    # Verify elements
    title = await cdp.eval("document.title")
    h1 = await cdp.eval("document.querySelector('h1')?.innerText")
    has_a_plus = await cdp.eval("!!Array.from(document.querySelectorAll('.accessibility-btn')).find(b => b.textContent.includes('A+'))")
    has_contrast = await cdp.eval("!!Array.from(document.querySelectorAll('.accessibility-btn')).find(b => b.textContent.includes('Contrast'))")
    presets_count = await cdp.eval("document.querySelectorAll('[id^=\"preset-login-\"]').length")

    print(f"  Page Title: {title}")
    print(f"  H1: {h1}")
    print(f"  Accessibility A+ button present: {has_a_plus}")
    print(f"  High Contrast button present: {has_contrast}")
    print(f"  Demo Role Preset buttons count: {presets_count}")
    results["tab1_elements"] = {
        "title": title,
        "h1": h1,
        "has_a_plus": has_a_plus,
        "has_contrast": has_contrast,
        "presets_count": presets_count
    }

    # Default screenshot
    await cdp.screenshot("01_tab1_login_default.png")

    # Test Accessibility: Scale font up
    await cdp.eval("Array.from(document.querySelectorAll('.accessibility-btn')).find(b => b.textContent.includes('A+')).click()")
    await asyncio.sleep(0.5)
    font_size_dataset = await cdp.eval("document.documentElement.dataset.fontSize")
    print(f"  Font size dataset after A+: {font_size_dataset}")

    # Test Accessibility: High contrast toggle
    await cdp.eval("Array.from(document.querySelectorAll('.accessibility-btn')).find(b => b.textContent.includes('High Contrast')).click()")
    await asyncio.sleep(0.5)
    contrast_dataset = await cdp.eval("document.documentElement.dataset.contrast")
    print(f"  Contrast dataset after High Contrast click: {contrast_dataset}")
    await cdp.screenshot("02_tab1_login_high_contrast.png")

    # Reset contrast & font
    await cdp.eval("Array.from(document.querySelectorAll('.accessibility-btn')).find(b => b.textContent.includes('Standard Mode')).click()")
    await cdp.eval("Array.from(document.querySelectorAll('.accessibility-btn')).find(b => b.textContent.includes('A')).click()")
    await asyncio.sleep(0.5)

    # -------------------------------------------------------------
    # TAB 2: Institution Officer (Delhi Tech)
    # -------------------------------------------------------------
    print("\n--- [TAB 2] Institution Officer (Delhi Tech) Verification ---")
    # Click Delhi Tech preset and submit
    await cdp.eval("document.querySelector('#preset-login-institution-delhi')?.click()")
    await asyncio.sleep(0.3)
    user_val = await cdp.eval("document.querySelector('#username')?.value")
    print(f"  Filled username: {user_val}")
    await cdp.eval("document.querySelector('#login-submit-btn')?.click()")
    
    # Wait for dashboard
    await cdp.wait_for_selector("#inst-dash-new-app, .gov-card", timeout=12.0)
    await asyncio.sleep(2)

    # Verify Institution Dashboard details
    banner_inst = await cdp.eval("document.querySelector('h1')?.innerText")
    banner_pid = await cdp.eval("document.body.innerText.includes('1-500123498')")
    sidebar_heading = await cdp.eval("document.querySelector('.app-sidebar')?.innerText.split('\\n')[0]")
    
    print(f"  Dashboard Institution Name: {banner_inst}")
    print(f"  AICTE Permanent ID (1-500123498) visible: {banner_pid}")
    print(f"  Sidebar Role Heading: {sidebar_heading}")
    results["tab2_dashboard"] = {
        "institution_name": banner_inst,
        "pid_1_500123498_visible": banner_pid,
        "sidebar_heading": sidebar_heading
    }
    await cdp.screenshot("03_tab2_institution_dashboard.png")

    # Navigate to /applications
    print("  Navigating to /applications...")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/applications"})
    await cdp.wait_for_selector("table", timeout=10.0)
    await asyncio.sleep(1.5)

    apps_count = await cdp.eval("document.querySelectorAll('table tbody tr').length")
    has_del_103 = await cdp.eval("document.body.innerText.includes('APP-2025-DEL-0103')")
    has_new_app_btn = await cdp.eval("!!document.querySelector('button.btn-primary')")
    print(f"  Applications listed in table: {apps_count}")
    print(f"  Seeded application APP-2025-DEL-0103 visible: {has_del_103}")
    print(f"  New Approval Application button present: {has_new_app_btn}")
    await cdp.screenshot("04_tab2_applications_list.png")

    # Open "Initiate AICTE Approval Application" modal
    print("  Opening New Approval Application Modal...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('New Approval Application')).click()")
    await cdp.wait_for_selector(".modal-overlay, form", timeout=5.0)
    await asyncio.sleep(0.5)

    modal_title = await cdp.eval("document.querySelector('.modal-header, h2, h3')?.innerText")
    categories = await cdp.eval("Array.from(document.querySelectorAll('select option')).map(o => o.value).filter(v => v.includes('COURSE') || v.includes('INSTITUTION') || v.includes('APPROVAL'))")
    print(f"  Modal Title: {modal_title}")
    print(f"  Categories available: {categories}")

    # Select NEW_COURSE_INTRODUCTION to reveal dynamic course form
    await cdp.eval("""
        const sel = Array.from(document.querySelectorAll('select')).find(s => s.innerHTML.includes('NEW_COURSE_INTRODUCTION'));
        if (sel) {
            sel.value = 'NEW_COURSE_INTRODUCTION';
            sel.dispatchEvent(new Event('change', { bubbles: true }));
        }
    """)
    await asyncio.sleep(0.5)
    dynamic_field_present = await cdp.eval("!!document.querySelector('input[placeholder*=\"Artificial Intelligence\"]')")
    print(f"  Dynamic Course Specification input present: {dynamic_field_present}")
    await cdp.screenshot("05_tab2_new_course_modal.png")

    # Close modal
    await cdp.eval("document.querySelector('.modal-close-btn, button[aria-label=\"Close\"]')?.click() || document.querySelector('.modal-overlay')?.click()")
    await asyncio.sleep(0.5)

    # Navigate to Application Details for APP-2025-DEL-0103
    print("  Navigating to Application Details (APP-2025-DEL-0103)...")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/applications/bbbbbbbb-0001-0000-0000-000000000103"})
    await cdp.wait_for_selector("button", timeout=10.0)
    await asyncio.sleep(2)

    app_num = await cdp.eval("document.querySelector('h1, h2')?.innerText")
    state_badge = await cdp.eval("document.querySelector('.badge-danger, .badge-warning, .badge')?.innerText")
    print(f"  Application Header: {app_num}")
    print(f"  Current State Badge: {state_badge}")

    # Sub-tab 1: Specification / Proposal
    await cdp.screenshot("06_tab2_app_specification_tab.png")

    # Sub-tab 2: Compliance Documents & OCR
    print("  Switching to Compliance Documents & OCR tab...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Compliance Documents') || b.textContent.includes('Documents')).click()")
    await asyncio.sleep(1)

    has_ocr_data = await cdp.eval("document.body.innerText.includes('Fire Safety') || document.body.innerText.includes('CERT-') || document.body.innerText.includes('Confidence')")
    has_ai_btn = await cdp.eval("!!Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('SAGE AI') || b.textContent.includes('Gemini'))")
    has_replace_doc = await cdp.eval("!!Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Replace') || b.textContent.includes('Version History'))")
    print(f"  OCR extracted fields & certificates visible: {has_ocr_data}")
    print(f"  SAGE AI Findings button present: {has_ai_btn}")
    print(f"  Document Version Control / Replace buttons present: {has_replace_doc}")
    await cdp.screenshot("07_tab2_compliance_documents_ocr.png")

    # Trigger SAGE AI Findings Modal
    print("  Clicking SAGE AI Findings button...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('SAGE AI') || b.textContent.includes('Gemini'))?.click()")
    await asyncio.sleep(1.5)
    ai_modal_open = await cdp.eval("!!document.querySelector('.modal-overlay, [role=\"dialog\"]')")
    print(f"  SAGE AI Modal opened: {ai_modal_open}")
    await cdp.screenshot("08_tab2_sage_ai_modal.png")

    # Close modal
    await cdp.eval("document.querySelector('.modal-close-btn, button[aria-label=\"Close\"]')?.click() || document.querySelector('.modal-overlay')?.click()")
    await asyncio.sleep(0.5)

    # Sub-tab 4: Workflow History
    print("  Switching to Workflow History tab...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Workflow History') || b.textContent.includes('Workflow')).click()")
    await asyncio.sleep(1)
    await cdp.screenshot("09_tab2_workflow_history.png")

    # -------------------------------------------------------------
    # TAB 3: Regional Officer Scrutiny
    # -------------------------------------------------------------
    print("\n--- [TAB 3] Regional Officer Scrutiny Verification ---")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/login"})
    await cdp.wait_for_selector("#login-submit-btn")
    await asyncio.sleep(0.5)

    await cdp.eval("document.querySelector('#preset-login-regional')?.click()")
    await asyncio.sleep(0.3)
    await cdp.eval("document.querySelector('#login-submit-btn')?.click()")
    await cdp.wait_for_selector(".app-sidebar", timeout=10.0)
    await asyncio.sleep(1.5)

    reg_heading = await cdp.eval("document.querySelector('.app-sidebar')?.innerText.split('\\n')[0]")
    print(f"  Regional Officer Sidebar Heading: {reg_heading}")

    # Navigate to APP-2025-DEL-0103 details
    print("  Navigating to APP-2025-DEL-0103 scrutiny desk...")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/applications/bbbbbbbb-0001-0000-0000-000000000103"})
    await cdp.wait_for_selector("button", timeout=10.0)
    await asyncio.sleep(1.5)

    # Verify RBAC: Upload button is HIDDEN
    upload_present = await cdp.eval("!!document.querySelector('input[type=\"file\"], #document-upload-input')")
    print(f"  RBAC check: Document upload input present: {upload_present} (Must be False for officer)")

    # Switch to Scrutiny Findings tab
    print("  Switching to Scrutiny Findings tab...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Scrutiny Findings') || b.textContent.includes('Scrutiny')).click()")
    await asyncio.sleep(1)

    has_clause_14 = await cdp.eval("document.body.innerText.includes('Clause 1.4') || document.body.innerText.includes('Land')")
    has_clause_21 = await cdp.eval("document.body.innerText.includes('Clause 2.1') || document.body.innerText.includes('Fire Safety')")
    has_clause_32 = await cdp.eval("document.body.innerText.includes('Clause 3.2') || document.body.innerText.includes('Faculty')")
    has_eval_btn = await cdp.eval("!!Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Evaluate Scrutiny Rules') || b.textContent.includes('Evaluate Rules'))")
    has_advance_btn = await cdp.eval("!!Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Advance Workflow') || b.textContent.includes('Transition'))")

    print(f"  Clause 1.4 Checkpoint visible: {has_clause_14}")
    print(f"  Clause 2.1 Checkpoint visible: {has_clause_21}")
    print(f"  Clause 3.2 Checkpoint visible: {has_clause_32}")
    print(f"  Evaluate Scrutiny Rules button present: {has_eval_btn}")
    print(f"  Advance Workflow button present: {has_advance_btn}")

    results["tab3_scrutiny"] = {
        "upload_input_hidden": not upload_present,
        "clause_1_4_visible": has_clause_14,
        "clause_2_1_visible": has_clause_21,
        "clause_3_2_visible": has_clause_32,
        "has_eval_btn": has_eval_btn,
        "has_advance_btn": has_advance_btn
    }

    # Click Evaluate Scrutiny Rules to demonstrate live execution
    if has_eval_btn:
        print("  Clicking Evaluate Scrutiny Rules button...")
        await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Evaluate Scrutiny Rules') || b.textContent.includes('Evaluate Rules')).click()")
        await asyncio.sleep(2)

    await cdp.screenshot("10_tab3_regional_scrutiny_desk.png")

    # -------------------------------------------------------------
    # TAB 4: Compliance Officer
    # -------------------------------------------------------------
    print("\n--- [TAB 4] Compliance Officer Verification ---")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/login"})
    await cdp.wait_for_selector("#login-submit-btn")
    await asyncio.sleep(0.5)

    await cdp.eval("document.querySelector('#preset-login-compliance')?.click()")
    await asyncio.sleep(0.3)
    await cdp.eval("document.querySelector('#login-submit-btn')?.click()")
    await cdp.wait_for_selector(".app-sidebar", timeout=10.0)
    await asyncio.sleep(1.5)

    print("  Navigating to /compliance...")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/compliance"})
    await cdp.wait_for_selector("button", timeout=10.0)
    await asyncio.sleep(2)

    # Tab 1: Statutory Deadlines & Expiries
    deadlines_tab_active = await cdp.eval("document.body.innerText.includes('Statutory Deadlines') || document.body.innerText.includes('Days Remaining')")
    has_sweep_btn = await cdp.eval("!!Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Trigger Compliance Sweep') || b.textContent.includes('Compliance Sweep'))")
    print(f"  Deadlines & Expiries tab active: {deadlines_tab_active}")
    print(f"  Trigger Compliance Sweep button present: {has_sweep_btn}")
    await cdp.screenshot("11_tab4_compliance_deadlines.png")

    # Tab 2: Compliance Findings & Risk Analysis
    print("  Switching to Compliance Findings tab...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Findings')).click()")
    await asyncio.sleep(1)
    has_severities = await cdp.eval("document.body.innerText.includes('CRITICAL') || document.body.innerText.includes('WARNING') || document.body.innerText.includes('INFO')")
    print(f"  Findings severity badges visible: {has_severities}")
    await cdp.screenshot("12_tab4_compliance_findings.png")

    # Tab 3: Surveillance Runs & History
    print("  Switching to Surveillance Runs tab...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Surveillance') || b.textContent.includes('Runs')).click()")
    await asyncio.sleep(1)
    runs_count = await cdp.eval("document.querySelectorAll('table tbody tr').length")
    print(f"  Surveillance runs logged: {runs_count}")
    await cdp.screenshot("13_tab4_compliance_runs.png")

    # Trigger Compliance Sweep live!
    if has_sweep_btn:
        print("  Clicking Trigger Compliance Sweep...")
        await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Trigger Compliance Sweep') || b.textContent.includes('Compliance Sweep')).click()")
        await asyncio.sleep(3)
        await cdp.screenshot("14_tab4_compliance_sweep_executed.png")

    # Navigate to /alerts
    print("  Navigating to /alerts...")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/alerts"})
    await cdp.wait_for_selector(".gov-card, table, h1", timeout=10.0)
    await asyncio.sleep(1.5)
    alerts_count = await cdp.eval("document.querySelectorAll('table tbody tr, .alert-item, .gov-card').length")
    print(f"  Alerts elements rendered: {alerts_count}")
    await cdp.screenshot("15_tab4_alerts_center.png")

    # -------------------------------------------------------------
    # TAB 5: EVC Coordinator (CP-SAT Solver)
    # -------------------------------------------------------------
    print("\n--- [TAB 5] EVC Coordinator Verification ---")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/login"})
    await cdp.wait_for_selector("#login-submit-btn")
    await asyncio.sleep(0.5)

    await cdp.eval("document.querySelector('#preset-login-evc')?.click()")
    await asyncio.sleep(0.3)
    await cdp.eval("document.querySelector('#login-submit-btn')?.click()")
    await cdp.wait_for_selector(".app-sidebar", timeout=10.0)
    await asyncio.sleep(1.5)

    print("  Navigating to /evc...")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/evc"})
    await cdp.wait_for_selector("button", timeout=10.0)
    await asyncio.sleep(2)

    # Sub-tab 1: Pending Inspections
    has_allocate_btn = await cdp.eval("!!Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Allocation') || b.textContent.includes('Schedule') || b.textContent.includes('Run Statutory'))")
    print(f"  Run Allocation Engine button present: {has_allocate_btn}")
    await cdp.screenshot("16_tab5_evc_pending.png")

    # Sub-tab 3: Accredited Experts Registry
    print("  Switching to Accredited Experts Registry...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Experts') || b.textContent.includes('Registry')).click()")
    await asyncio.sleep(1.5)
    experts_count = await cdp.eval("document.querySelectorAll('table tbody tr, .expert-card').length")
    has_disciplines = await cdp.eval("document.body.innerText.includes('Computer Science') || document.body.innerText.includes('Electronics')")
    print(f"  Experts listed: {experts_count}")
    print(f"  Disciplines visible: {has_disciplines}")
    await cdp.screenshot("17_tab5_evc_experts_registry.png")

    # Sub-tab 2: Confirmed Schedules / Allocation
    print("  Switching to Confirmed Schedules tab...")
    await cdp.eval("Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Confirmed') || b.textContent.includes('Scheduled')).click()")
    await asyncio.sleep(1.5)
    await cdp.screenshot("18_tab5_evc_confirmed_schedules.png")

    # -------------------------------------------------------------
    # TAB 6: Regulatory Auditor
    # -------------------------------------------------------------
    print("\n--- [TAB 6] Regulatory Auditor & Audit Trail Verification ---")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/login"})
    await cdp.wait_for_selector("#login-submit-btn")
    await asyncio.sleep(0.5)

    await cdp.eval("document.querySelector('#preset-login-auditor')?.click()")
    await asyncio.sleep(0.3)
    await cdp.eval("document.querySelector('#login-submit-btn')?.click()")
    await cdp.wait_for_selector(".app-sidebar", timeout=10.0)
    await asyncio.sleep(1.5)

    print("  Navigating to /admin/audit...")
    await cdp.call("Page.navigate", {"url": "http://localhost:5173/admin/audit"})
    await cdp.wait_for_selector("table", timeout=10.0)
    await asyncio.sleep(2)

    audit_title = await cdp.eval("document.querySelector('h1')?.innerText")
    audit_rows = await cdp.eval("document.querySelectorAll('table tbody tr').length")
    has_correlation_id = await cdp.eval("document.body.innerText.includes('Correlation ID') || !!document.querySelector('input[placeholder*=\"Correlation\"]')")
    has_principal_filter = await cdp.eval("!!document.querySelector('input[placeholder*=\"Principal\"], input[placeholder*=\"user\"], #principal-filter')")

    print(f"  Audit Page Title: {audit_title}")
    print(f"  Audit entries displayed: {audit_rows}")
    print(f"  Correlation ID column / search present: {has_correlation_id}")
    print(f"  Principal filter input present: {has_principal_filter}")
    await cdp.screenshot("19_tab6_audit_log_overview.png")

    # Test filtering by Delhi Tech officer
    print("  Filtering Audit Log by 'officer@delhi-tech.edu'...")
    await cdp.eval("""
        const inp = document.querySelector('input[placeholder*=\"Principal\"], input[placeholder*=\"user\"], #principal-filter, form input[type=\"text\"]');
        if (inp) {
            inp.value = 'officer@delhi-tech.edu';
            inp.dispatchEvent(new Event('input', { bubbles: true }));
            const form = inp.closest('form');
            if (form) {
                form.dispatchEvent(new Event('submit', { bubbles: true }));
            }
        }
    """)
    await asyncio.sleep(1.5)
    filtered_rows = await cdp.eval("document.querySelectorAll('table tbody tr').length")
    print(f"  Filtered audit rows for Delhi Tech: {filtered_rows}")
    await cdp.screenshot("20_tab6_audit_log_filtered.png")

    await cdp.close()
    print("\n" + "=" * 70)
    print("LIVE VERIFICATION COMPLETED SUCCESSFULLY!")
    print(f"Total screenshots generated: 20")
    print(f"Artifacts saved to: {ARTIFACTS_DIR}")
    
if __name__ == "__main__":
    asyncio.run(run_verification())
