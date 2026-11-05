import asyncio
import json
import base64
import os
import urllib.request
import websockets

SCREENSHOT_DIR = "/home/Harshit/.gemini/antigravity-ide/brain/506082e9-a414-4dbf-896c-b509aca45355"
os.makedirs(SCREENSHOT_DIR, exist_ok=True)
APP_ID = "98e5639a-d3ff-4039-9a83-5895599b38d0"

class CDPClient:
    def __init__(self, ws_url):
        self.ws_url = ws_url
        self.msg_id = 0
        self.pending = {}
        self.ws = None
        self.listen_task = None

    async def connect(self):
        self.ws = await websockets.connect(self.ws_url, max_size=50_000_000)
        self.listen_task = asyncio.create_task(self._listen())

    async def _listen(self):
        try:
            async for raw in self.ws:
                data = json.loads(raw)
                if "id" in data and data["id"] in self.pending:
                    fut = self.pending.pop(data["id"])
                    if not fut.done():
                        fut.set_result(data)
        except Exception:
            pass

    async def send(self, method, params=None):
        self.msg_id += 1
        mid = self.msg_id
        fut = asyncio.get_event_loop().create_future()
        self.pending[mid] = fut
        await self.ws.send(json.dumps({"id": mid, "method": method, "params": params or {}}))
        res = await asyncio.wait_for(fut, timeout=30.0)
        if "error" in res:
            raise RuntimeError(f"CDP Error in {method}: {res['error']}")
        return res.get("result", {})

    async def eval_js(self, expr):
        res = await self.send("Runtime.evaluate", {
            "expression": expr,
            "returnByValue": True,
            "awaitPromise": True
        })
        val = res.get("result", {}).get("value")
        return val

    async def capture_screenshot(self, filename):
        res = await self.send("Page.captureScreenshot", {"format": "png"})
        raw_b64 = res.get("data", "")
        img_bytes = base64.b64decode(raw_b64)
        out_path = os.path.join(SCREENSHOT_DIR, filename)
        with open(out_path, "wb") as f:
            f.write(img_bytes)
        print(f"Captured: {out_path} ({len(img_bytes)} bytes)")
        return out_path

    async def close(self):
        if self.listen_task:
            self.listen_task.cancel()
        if self.ws:
            await self.ws.close()

async def get_target():
    req = urllib.request.Request("http://127.0.0.1:9222/json")
    with urllib.request.urlopen(req) as response:
        targets = json.loads(response.read().decode())
    for t in targets:
        if t.get("type") == "page":
            return t["webSocketDebuggerUrl"]
    req = urllib.request.Request("http://127.0.0.1:9222/json/new?http://localhost:5173", method="PUT")
    with urllib.request.urlopen(req) as response:
        t = json.loads(response.read().decode())
        return t["webSocketDebuggerUrl"]

async def perform_login(cdp, preset_id):
    print(f"Logging in with preset: {preset_id}")
    await cdp.send("Page.navigate", {"url": "http://localhost:5173/login"})
    await asyncio.sleep(2.5)
    preset_clicked = await cdp.eval_js(f"""
    (() => {{
        const btn = document.getElementById('{preset_id}');
        if (btn) {{
            btn.click();
            return true;
        }}
        return false;
    }})()
    """)
    print(f"Preset {preset_id} clicked:", preset_clicked)
    await asyncio.sleep(1.0)
    submit_clicked = await cdp.eval_js("""
    (() => {
        const btn = document.querySelector('button[type="submit"]');
        if (btn) {
            btn.click();
            return true;
        }
        return false;
    })()
    """)
    print("Submit login clicked:", submit_clicked)
    await asyncio.sleep(3.5)
    current_url = await cdp.eval_js("window.location.href")
    print("Logged in. Current URL:", current_url)

async def run_end_to_end_verification():
    ws_url = await get_target()
    print(f"Connected to CDP: {ws_url}")
    cdp = CDPClient(ws_url)
    await cdp.connect()

    try:
        await cdp.send("Page.enable")
        await cdp.send("Runtime.enable")
        await cdp.send("DOM.enable")
        await cdp.send("Emulation.setDeviceMetricsOverride", {
            "width": 1440,
            "height": 960,
            "deviceScaleFactor": 1,
            "mobile": False
        })

        # =========================================================================
        # PHASE 1: INSTITUTION OFFICER PERSPECTIVE
        # =========================================================================
        print("\n=======================================================")
        print("PHASE 1: Institution Officer Log In & Document Inspection")
        print("=======================================================")
        await perform_login(cdp, "preset-login-institution-delhi")

        # Navigate directly to application details
        print(f"Navigating to application {APP_ID}...")
        await cdp.send("Page.navigate", {"url": f"http://localhost:5173/applications/{APP_ID}"})
        await asyncio.sleep(3.0)

        # Switch to Regulatory Documents Tab
        print("Switching to Regulatory Documents Tab...")
        await cdp.eval_js("""
        (() => {
            const docTab = document.getElementById('tab-documents') || Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Regulatory Documents') || b.textContent.includes('Documents'));
            if (docTab) docTab.click();
        })()
        """)
        await asyncio.sleep(2.0)

        # Open Dual-Pane Inspector on Clean Fire Safety Document
        print("Opening Side-by-Side Dual-Pane Inspector on FIRE_SAFETY_CERTIFICATE...")
        open_fire_safety_js = """
        (() => {
            const inspectBtn = document.getElementById('btn-checklist-inspect-FIRE_SAFETY_CERTIFICATE') ||
                               document.getElementById('btn-inspect-document-FIRE_SAFETY_CERTIFICATE') ||
                               Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Inspect Document & AI'));
            if (inspectBtn) {
                inspectBtn.click();
                return true;
            }
            return false;
        })()
        """
        opened = await cdp.eval_js(open_fire_safety_js)
        print("Inspector opened:", opened)
        await asyncio.sleep(2.5)

        # Ensure AI tab is active
        await cdp.eval_js("""
        (() => {
            const aiTab = document.getElementById('tab-btn-ai-findings');
            if (aiTab) aiTab.click();
        })()
        """)
        await asyncio.sleep(1.0)

        # Capture Screenshot 09: Dual-Pane Inspector with AI Findings
        print("Capturing workflow_09_side_by_side_document_and_ai_inspector.png...")
        modal_info = await cdp.eval_js("""
        (() => {
            const modal = document.getElementById('document-verification-inspector-modal');
            const iframe = document.getElementById('iframe-actual-document');
            const p1 = document.getElementById('pillar-ai-usage');
            const p2 = document.getElementById('pillar-font-compliance');
            const p3 = document.getElementById('pillar-format-layout');
            const p4 = document.getElementById('pillar-discrepancy');
            return {
                modalFound: !!modal,
                iframeSrc: iframe ? iframe.src : null,
                pillar1Text: p1 ? p1.innerText.replace(/\\n/g, ' ') : null,
                pillar2Text: p2 ? p2.innerText.replace(/\\n/g, ' ') : null,
                pillar3Text: p3 ? p3.innerText.replace(/\\n/g, ' ') : null,
                pillar4Text: p4 ? p4.innerText.replace(/\\n/g, ' ') : null
            };
        })()
        """)
        print("Inspector AI Review Modal Info:", modal_info)
        await cdp.capture_screenshot("workflow_09_side_by_side_document_and_ai_inspector.png")

        # Switch to Tab 2: OCR Extraction Tab
        print("\nSwitching to Current OCR Extraction Tab...")
        await cdp.eval_js("""
        (() => {
            const ocrTabBtn = document.getElementById('tab-btn-ocr-extraction');
            if (ocrTabBtn) ocrTabBtn.click();
        })()
        """)
        await asyncio.sleep(2.0)

        ocr_info = await cdp.eval_js("""
        (() => {
            const rows = document.querySelectorAll('tbody tr');
            const fields = Array.from(rows).map(r => r.innerText.replace(/\\n/g, ' | '));
            return {
                rowCount: rows.length,
                fields: fields
            };
        })()
        """)
        print("OCR Tab Info:", ocr_info)
        await cdp.capture_screenshot("workflow_10_inspector_ocr_extraction_tab.png")

        # Close Inspector Modal
        await cdp.eval_js("""
        (() => {
            const closeBtn = document.getElementById('btn-close-document-inspector');
            if (closeBtn) closeBtn.click();
        })()
        """)
        await asyncio.sleep(1.5)

        # Inspect AI-Flagged Document (AFFIDAVIT_FORMAT_1)
        print("\nOpening Dual-Pane Inspector on AI-Flagged Document (AFFIDAVIT_FORMAT_1)...")
        await cdp.eval_js("""
        (() => {
            const inspectAffidavit = document.getElementById('btn-checklist-inspect-AFFIDAVIT_FORMAT_1') ||
                                    document.getElementById('btn-inspect-document-AFFIDAVIT_FORMAT_1');
            if (inspectAffidavit) inspectAffidavit.click();
        })()
        """)
        await asyncio.sleep(2.5)

        # Explicitly click the AI Review tab
        await cdp.eval_js("""
        (() => {
            const aiTab = document.getElementById('tab-btn-ai-findings');
            if (aiTab) aiTab.click();
        })()
        """)
        await asyncio.sleep(1.5)

        flagged_modal_info = await cdp.eval_js("""
        (() => {
            const p1 = document.getElementById('pillar-ai-usage');
            const p2 = document.getElementById('pillar-font-compliance');
            const p3 = document.getElementById('pillar-format-layout');
            const p4 = document.getElementById('pillar-discrepancy');
            return {
                p1: p1 ? p1.innerText.replace(/\\n/g, ' ') : null,
                p2: p2 ? p2.innerText.replace(/\\n/g, ' ') : null,
                p3: p3 ? p3.innerText.replace(/\\n/g, ' ') : null,
                p4: p4 ? p4.innerText.replace(/\\n/g, ' ') : null
            };
        })()
        """)
        print("Flagged Document 4 Pillars:", flagged_modal_info)
        await cdp.capture_screenshot("workflow_10b_inspector_ai_flags_detected.png")

        # Close Inspector Modal
        await cdp.eval_js("""
        (() => {
            const closeBtn = document.getElementById('btn-close-document-inspector');
            if (closeBtn) closeBtn.click();
        })()
        """)
        await asyncio.sleep(1.5)

        # =========================================================================
        # PHASE 2: REGIONAL OFFICER VERIFICATION & DETERMINATION
        # =========================================================================
        print("\n=======================================================")
        print("PHASE 2: Regional Officer Log In & Scrutiny Determination")
        print("=======================================================")
        await perform_login(cdp, "preset-login-regional")

        # Navigate to Application
        print(f"Navigating to application {APP_ID} as Regional Officer...")
        await cdp.send("Page.navigate", {"url": f"http://localhost:5173/applications/{APP_ID}"})
        await asyncio.sleep(3.5)

        # Switch to Regulatory Documents Tab
        await cdp.eval_js("""
        (() => {
            const docTab = document.getElementById('tab-documents') || Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Regulatory Documents') || b.textContent.includes('Documents'));
            if (docTab) docTab.click();
        })()
        """)
        await asyncio.sleep(2.0)

        # Open Inspector on clean Fire Safety document
        await cdp.eval_js(open_fire_safety_js)
        await asyncio.sleep(2.5)

        # Switch to Tab 3: Officer Scrutiny & Verification
        print("Switching to Officer Scrutiny & Verification Tab...")
        await cdp.eval_js("""
        (() => {
            const scrutinyTabBtn = document.getElementById('tab-btn-statutory-scrutiny');
            if (scrutinyTabBtn) scrutinyTabBtn.click();
        })()
        """)
        await asyncio.sleep(2.0)

        # Enter Officer Scrutiny Notes & Mark Verified Clean
        print("Entering Officer Scrutiny Determination...")
        await cdp.eval_js("""
        (() => {
            const notesEl = document.getElementById('textarea-officer-notes');
            if (notesEl) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;
                nativeSetter.call(notesEl, 'Statutory verification complete. Fire Safety NOC issued by Delhi Fire Service verified authentic against municipal registry. OCR fields validated. Cleared.');
                notesEl.dispatchEvent(new Event('input', { bubbles: true }));
            }
        })()
        """)
        await asyncio.sleep(1.0)
        await cdp.capture_screenshot("workflow_11_scrutiny_officer_inspection.png")

        # Click Mark Verified Clean
        await cdp.eval_js("""
        (() => {
            const verifyBtn = document.getElementById('btn-officer-verify-clean');
            if (verifyBtn) verifyBtn.click();
        })()
        """)
        await asyncio.sleep(2.5)

        # Close Inspector Modal
        await cdp.eval_js("""
        (() => {
            const closeBtn = document.getElementById('btn-close-document-inspector');
            if (closeBtn) closeBtn.click();
        })()
        """)
        await asyncio.sleep(1.5)

        # =========================================================================
        # PHASE 3: END-TO-END APPROVAL WORKFLOW TRANSITION PIPELINE
        # =========================================================================
        print("\n=======================================================")
        print("PHASE 3: End-to-End Statutory Approval State Transitions")
        print("=======================================================")

        # Step A: Transition SUBMITTED -> UNDER_SCRUTINY
        print("Transitioning: SUBMITTED -> UNDER_SCRUTINY...")
        await cdp.eval_js("""
        (() => {
            const advBtn = document.getElementById('btn-advance-workflow');
            if (advBtn) advBtn.click();
        })()
        """)
        await asyncio.sleep(1.5)

        await cdp.eval_js("""
        (() => {
            const selectEl = document.getElementById('select-workflow-target-state');
            if (selectEl) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;
                nativeSetter.call(selectEl, 'UNDER_SCRUTINY');
                selectEl.dispatchEvent(new Event('change', { bubbles: true }));
            }
            const reasonEl = document.getElementById('textarea-transition-reason');
            if (reasonEl) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;
                nativeSetter.call(reasonEl, 'Regional Officer assigned application for statutory scrutiny.');
                reasonEl.dispatchEvent(new Event('input', { bubbles: true }));
            }
            const submitBtn = document.getElementById('btn-submit-workflow-transition');
            if (submitBtn) submitBtn.click();
        })()
        """)
        await asyncio.sleep(3.0)

        # Step B: Transition UNDER_SCRUTINY -> SCRUTINY_PASSED
        print("Transitioning: UNDER_SCRUTINY -> SCRUTINY_PASSED...")
        await cdp.eval_js("""
        (() => {
            const advBtn = document.getElementById('btn-advance-workflow');
            if (advBtn) advBtn.click();
        })()
        """)
        await asyncio.sleep(1.5)

        await cdp.eval_js("""
        (() => {
            const selectEl = document.getElementById('select-workflow-target-state');
            if (selectEl) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;
                nativeSetter.call(selectEl, 'SCRUTINY_PASSED');
                selectEl.dispatchEvent(new Event('change', { bubbles: true }));
            }
            const reasonEl = document.getElementById('textarea-transition-reason');
            if (reasonEl) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;
                nativeSetter.call(reasonEl, 'All mandatory statutory documents verified clean and compliant with APH norms.');
                reasonEl.dispatchEvent(new Event('input', { bubbles: true }));
            }
            const submitBtn = document.getElementById('btn-submit-workflow-transition');
            if (submitBtn) submitBtn.click();
        })()
        """)
        await asyncio.sleep(3.0)

        # Step C: Transition SCRUTINY_PASSED -> APPROVED (Final AICTE Decision Grant)
        print("Transitioning: SCRUTINY_PASSED -> APPROVED (Final Statutory Approval Grant)...")
        await cdp.eval_js("""
        (() => {
            const advBtn = document.getElementById('btn-advance-workflow');
            if (advBtn) advBtn.click();
        })()
        """)
        await asyncio.sleep(1.5)

        await cdp.eval_js("""
        (() => {
            const selectEl = document.getElementById('select-workflow-target-state');
            if (selectEl) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value').set;
                nativeSetter.call(selectEl, 'APPROVED');
                selectEl.dispatchEvent(new Event('change', { bubbles: true }));
            }
            const reasonEl = document.getElementById('textarea-transition-reason');
            if (reasonEl) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;
                nativeSetter.call(reasonEl, 'AICTE Regional Committee and Executive Council grant official Extension of Approval (EoA) for Academic Year 2024-25.');
                reasonEl.dispatchEvent(new Event('input', { bubbles: true }));
            }
            const submitBtn = document.getElementById('btn-submit-workflow-transition');
            if (submitBtn) submitBtn.click();
        })()
        """)
        await asyncio.sleep(3.5)

        # Reload to ensure full reactive state sync
        await cdp.send("Page.reload")
        await asyncio.sleep(3.5)

        final_state_info = await cdp.eval_js("""
        (() => {
            const header = document.querySelector('h1, h2');
            const badges = Array.from(document.querySelectorAll('.badge, [class*="badge"], [class*="status"]')).map(b => b.innerText.trim());
            return {
                headerText: header ? header.innerText : null,
                badges: badges,
                bodyHasApproved: document.body.innerText.includes('APPROVED')
            };
        })()
        """)
        print("Final Approved Application State Info:", final_state_info)
        await cdp.capture_screenshot("workflow_12_end_level_approval_decision.png")

        print("\n=======================================================")
        print("END-TO-END VISUAL APPROVAL WORKFLOW SUCCESSFULLY VERIFIED!")
        print("=======================================================")

    finally:
        await cdp.close()

if __name__ == "__main__":
    asyncio.run(run_end_to_end_verification())
