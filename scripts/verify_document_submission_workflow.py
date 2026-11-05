import asyncio
import json
import base64
import os
import urllib.request
import websockets

SCREENSHOT_DIR = "/home/Harshit/.gemini/antigravity-ide/brain/506082e9-a414-4dbf-896c-b509aca45355"
os.makedirs(SCREENSHOT_DIR, exist_ok=True)

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
        res = await asyncio.wait_for(fut, timeout=25.0)
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

async def run_workflow():
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

        # Step 1: Login as Institution Officer
        print("\n--- STEP 1: Log in as Institution Officer ---")
        await cdp.send("Page.navigate", {"url": "http://localhost:5173/login"})
        await asyncio.sleep(2.5)

        # Click preset button for Institution Officer (Delhi)
        preset_clicked = await cdp.eval_js("""
        (() => {
            const presetBtn = document.getElementById('preset-login-institution-delhi');
            if (presetBtn) {
                presetBtn.click();
                return true;
            }
            return false;
        })()
        """)
        print("Preset button clicked:", preset_clicked)
        await asyncio.sleep(0.8)

        # Click submit button
        submit_login = await cdp.eval_js("""
        (() => {
            const btn = document.querySelector('button[type="submit"]');
            if (btn) {
                btn.click();
                return true;
            }
            return false;
        })()
        """)
        print("Login submit clicked:", submit_login)
        await asyncio.sleep(3.5)

        dash_url = await cdp.eval_js("window.location.href")
        print("Current URL after login:", dash_url)
        await cdp.capture_screenshot("workflow_01_institution_dashboard.png")

        # Step 2: Navigate to Applications and open Create Modal
        print("\n--- STEP 2: Navigate to Applications & Open Initiate Modal ---")
        await cdp.send("Page.navigate", {"url": "http://localhost:5173/applications"})
        await asyncio.sleep(2.5)

        open_modal_script = """
        (() => {
            const btn = document.getElementById('btn-new-application') || Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('New Approval Application'));
            if (btn) {
                btn.click();
                return true;
            }
            return false;
        })()
        """
        modal_opened = await cdp.eval_js(open_modal_script)
        print("Create Modal opened:", modal_opened)
        await asyncio.sleep(2.0)

        # Check required documents checklist preview in modal for NEW_COURSE_INTRODUCTION
        preview_check_js = """
        (() => {
            const preview = document.getElementById('modal-statutory-checklist-preview');
            if (!preview) return { found: false };
            const items = Array.from(preview.querySelectorAll('div > div > span')).map(s => s.textContent.trim());
            return {
                found: true,
                text: preview.innerText
            };
        })()
        """
        preview_data = await cdp.eval_js(preview_check_js)
        print("Checklist Preview in modal found:", preview_data.get("found"))
        await cdp.capture_screenshot("workflow_02_new_course_required_docs_modal.png")

        # Change category to NEW_INSTITUTION in modal to verify dynamic requirements update
        print("\n--- STEP 2B: Switch Category to NEW_INSTITUTION ---")
        switch_category_js = """
        (() => {
            const selects = document.querySelectorAll('select');
            let catSelect = null;
            for (const s of selects) {
                if (Array.from(s.options).some(o => o.value === 'NEW_INSTITUTION')) {
                    catSelect = s;
                    break;
                }
            }
            if (!catSelect) return false;
            const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, "value").set;
            nativeSetter.call(catSelect, 'NEW_INSTITUTION');
            catSelect.dispatchEvent(new Event('change', { bubbles: true }));
            return true;
        })()
        """
        switched = await cdp.eval_js(switch_category_js)
        print("Category switched to NEW_INSTITUTION:", switched)
        await asyncio.sleep(2.0)
        await cdp.capture_screenshot("workflow_03_new_institution_required_docs_modal.png")

        # Switch back to NEW_COURSE_INTRODUCTION and create application
        print("\n--- STEP 2C: Create New Course Application ---")
        create_app_js = """
        (() => {
            const selects = document.querySelectorAll('select');
            let catSelect = null;
            for (const s of selects) {
                if (Array.from(s.options).some(o => o.value === 'NEW_COURSE_INTRODUCTION')) {
                    catSelect = s;
                    break;
                }
            }
            if (catSelect) {
                const nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, "value").set;
                nativeSetter.call(catSelect, 'NEW_COURSE_INTRODUCTION');
                catSelect.dispatchEvent(new Event('change', { bubbles: true }));
            }
            
            const courseInput = document.querySelector('input[placeholder*="Artificial Intelligence"]') || document.querySelector('input[required]');
            if (courseInput) {
                const nativeInputSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value").set;
                nativeInputSetter.call(courseInput, 'B.Tech in Artificial Intelligence & Machine Learning');
                courseInput.dispatchEvent(new Event('input', { bubbles: true }));
            }
            
            const submitBtn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Create Draft Application') || b.textContent.includes('Create Application Draft') || b.textContent.includes('Initiate Application') || (b.type === 'submit' && b.closest('.modal-content, [role="dialog"]')));
            if (submitBtn) {
                submitBtn.click();
                return true;
            }
            return false;
        })()
        """
        created = await cdp.eval_js(create_app_js)
        print("Application create button clicked:", created)
        current_url = ""
        for _ in range(12):
            await asyncio.sleep(1.0)
            current_url = await cdp.eval_js("window.location.href")
            if "/applications/" in current_url:
                break
        print("Current URL after creation:", current_url)

        # Step 3: Verify Incomplete Application on Application Details
        print("\n--- STEP 3: Verify Incomplete Draft & Locked Submission Button ---")
        locked_button_status = await cdp.eval_js("""
        (() => {
            const btnLocked = document.getElementById('btn-submit-application-locked');
            const btnSubmit = document.getElementById('btn-submit-application');
            return {
                isLockedPresent: !!btnLocked,
                lockedText: btnLocked ? btnLocked.innerText : null,
                isSubmitPresent: !!btnSubmit
            };
        })()
        """)
        print("Header Submit Button Status:", locked_button_status)

        # Click locked button to verify warning popup
        await cdp.eval_js("""
        (() => {
            const btnLocked = document.getElementById('btn-submit-application-locked');
            if (btnLocked) btnLocked.click();
        })()
        """)
        await asyncio.sleep(1.0)
        await cdp.capture_screenshot("workflow_04_submission_locked_alert.png")

        # Close warning modal
        await cdp.eval_js("""
        (() => {
            const closeBtn = document.querySelector('[role="dialog"] button') || Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Close') || b.textContent.includes('OK') || b.textContent.includes('Acknowledge'));
            if (closeBtn) closeBtn.click();
        })()
        """)
        await asyncio.sleep(1.0)

        # Switch to Regulatory Documents tab
        print("\n--- STEP 3B: Switch to Regulatory Documents Tab ---")
        await cdp.eval_js("""
        (() => {
            const docTab = document.getElementById('tab-documents') || Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Regulatory Documents') || b.textContent.includes('Documents'));
            if (docTab) docTab.click();
        })()
        """)
        await asyncio.sleep(2.0)

        # Inspect Statutory Compliance Gate
        gate_info = await cdp.eval_js("""
        (() => {
            const gate = document.getElementById('statutory-document-compliance-gate');
            const lockedBadge = document.getElementById('badge-submission-locked');
            const readyBadge = document.getElementById('badge-submission-ready');
            const incompleteAlert = document.getElementById('alert-submission-incomplete');
            return {
                gatePresent: !!gate,
                lockedBadgeText: lockedBadge ? lockedBadge.innerText : null,
                readyBadgeText: readyBadge ? readyBadge.innerText : null,
                alertText: incompleteAlert ? incompleteAlert.innerText : null
            };
        })()
        """)
        print("Documents Tab Gate Info (Incomplete):", gate_info)
        await cdp.capture_screenshot("workflow_05_compliance_gate_incomplete.png")

        # Extract Application ID from URL
        app_id = current_url.split("/applications/")[1].split("?")[0].split("#")[0]
        print(f"Active Application ID for Document Testing: {app_id}")

        # Step 4: Upload 5 clean documents and 1 tampered document
        print("\n--- STEP 4: Uploading 5 Clean Documents + 1 Tampered Document ---")
        token_js = await cdp.eval_js("localStorage.getItem('sage_token') || sessionStorage.getItem('sage_token')")
        print("Retrieved Token from storage:", bool(token_js))

        # We will upload directly via API using python httpx with the auth token
        import httpx
        client = httpx.Client(timeout=60.0)
        login_res = client.post("http://localhost:8080/api/v1/auth/login", json={"username": "officer@delhi-tech.edu", "password": "Password123!"}).json()
        token = login_res["data"]["accessToken"]
        auth_headers = {"Authorization": f"Bearer {token}"}

        # Clean document uploads for:
        # OCCUPANCY_CERTIFICATE, BOARD_RESOLUTION, FACULTY_LIST_QUALIFICATION, LABORATORY_INFRASTRUCTURE_REPORT, SYLLABUS_CURRICULUM_APPROVAL, AFFIDAVIT_FORMAT_1
        clean_docs = [
            ("OCCUPANCY_CERTIFICATE", "occupancy_completion_certificate.pdf", b"%PDF-1.4\nCompetent Municipal Authority Occupancy Certificate\n%%EOF"),
            ("BOARD_RESOLUTION", "governing_board_resolution.pdf", b"%PDF-1.4\nGoverning Body Resolution approving AI & ML B.Tech Programme\n%%EOF"),
            ("FACULTY_LIST_QUALIFICATION", "faculty_cadre_qualification_matrix.pdf", b"%PDF-1.4\nAICTE Conforming Faculty Cadre & PhD Qualifications Roster\n%%EOF"),
            ("LABORATORY_INFRASTRUCTURE_REPORT", "laboratory_infrastructure_report.pdf", b"%PDF-1.4\nDedicated AI/ML GPU Computing Labs & Advanced Software Licenses\n%%EOF"),
            ("SYLLABUS_CURRICULUM_APPROVAL", "university_syllabus_affiliation_approval.pdf", b"%PDF-1.4\nAffiliating University Academic Council Syllabus & Curriculum Sanction\n%%EOF"),
            ("AFFIDAVIT_FORMAT_1", "annual_compliance_affidavit.pdf", b"%PDF-1.4\nAICTE Format 1 Annual Compliance Notarized Affidavit\n%%EOF")
        ]

        for dtype, fname, fcontent in clean_docs:
            r = client.post(
                f"http://localhost:8080/api/v1/applications/{app_id}/documents?documentType={dtype}",
                headers=auth_headers,
                files={"file": (fname, fcontent, "application/pdf")}
            )
            print(f"Uploaded {dtype}: status {r.status_code}")

        # Now upload TAMPERED document for FIRE_SAFETY_CERTIFICATE
        tampered_file = ("fake_fire_safety_tampered.pdf", b"%PDF-1.4\nSuspicious altered digital document\n%%EOF", "application/pdf")
        r_tamper = client.post(
            f"http://localhost:8080/api/v1/applications/{app_id}/documents?documentType=FIRE_SAFETY_CERTIFICATE",
            headers=auth_headers,
            files={"file": tampered_file}
        )
        print(f"Uploaded tampered FIRE_SAFETY_CERTIFICATE: status {r_tamper.status_code}")

        # Refresh page in browser to observe AI Forgery Alert
        print("\n--- STEP 4B: Visually Verify High Forgery Risk Gate ---")
        await cdp.send("Page.reload")
        await asyncio.sleep(3.0)

        # Switch to documents tab
        await cdp.eval_js("""
        (() => {
            const docTab = document.getElementById('tab-documents') || Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Regulatory Documents') || b.textContent.includes('Documents'));
            if (docTab) docTab.click();
        })()
        """)
        await asyncio.sleep(2.0)

        tamper_gate_info = await cdp.eval_js("""
        (() => {
            const alertBox = document.getElementById('alert-submission-incomplete');
            const lockedBadge = document.getElementById('badge-submission-locked');
            const fireItem = document.getElementById('checklist-item-FIRE_SAFETY_CERTIFICATE');
            const btnSubmitLocked = document.getElementById('btn-submit-application-locked');
            return {
                alertText: alertBox ? alertBox.innerText : null,
                lockedBadgeText: lockedBadge ? lockedBadge.innerText : null,
                fireItemText: fireItem ? fireItem.innerText : null,
                btnLockedPresent: !!btnSubmitLocked
            };
        })()
        """)
        print("Tampering Alert Gate Info:", tamper_gate_info)
        await cdp.capture_screenshot("workflow_06_compliance_gate_high_forgery_risk.png")

        # Step 5: Replace tampered Fire Safety with Authentic Certified Copy
        print("\n--- STEP 5: Replace Tampered Document with Authentic Certified Copy ---")
        clean_fire_file = ("fire_safety_certificate_genuine.pdf", b"%PDF-1.4\nGovernment Municipal Fire Directorate Certified Safety NOC\n%%EOF", "application/pdf")
        r_clean_fire = client.post(
            f"http://localhost:8080/api/v1/applications/{app_id}/documents?documentType=FIRE_SAFETY_CERTIFICATE",
            headers=auth_headers,
            files={"file": clean_fire_file}
        )
        print(f"Uploaded genuine replacement for FIRE_SAFETY_CERTIFICATE: status {r_clean_fire.status_code}")

        # Reload page
        await cdp.send("Page.reload")
        await asyncio.sleep(3.0)

        # Switch to documents tab
        await cdp.eval_js("""
        (() => {
            const docTab = document.getElementById('tab-documents') || Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Regulatory Documents') || b.textContent.includes('Documents'));
            if (docTab) docTab.click();
        })()
        """)
        await asyncio.sleep(2.0)

        ready_gate_info = await cdp.eval_js("""
        (() => {
            const readyBadge = document.getElementById('badge-submission-ready');
            const readyAlert = document.getElementById('alert-submission-ready');
            const btnSubmit = document.getElementById('btn-submit-application');
            const btnLocked = document.getElementById('btn-submit-application-locked');
            return {
                readyBadgeText: readyBadge ? readyBadge.innerText : null,
                readyAlertText: readyAlert ? readyAlert.innerText : null,
                btnSubmitPresent: !!btnSubmit,
                btnLockedPresent: !!btnLocked
            };
        })()
        """)
        print("Ready Gate Info (100% Complete & Clean):", ready_gate_info)
        await cdp.capture_screenshot("workflow_07_compliance_gate_ready_for_submission.png")

        # Step 6: Submit Application & Verify State Transition
        print("\n--- STEP 6: Execute Statutory Submission & Verify Transition ---")
        submit_clicked = await cdp.eval_js("""
        (() => {
            const btn = document.getElementById('btn-submit-application');
            if (btn) {
                btn.click();
                return true;
            }
            return false;
        })()
        """)
        print("Submit button clicked:", submit_clicked)
        await asyncio.sleep(3.5)

        post_submission_info = await cdp.eval_js("""
        (() => {
            const stateEl = document.querySelector('.badge, [class*="badge"]') || document.body;
            const text = document.body.innerText;
            return {
                bodyHasSubmitted: text.includes('Submitted') || text.includes('SUBMITTED'),
                textSnippet: text.substring(0, 500)
            };
        })()
        """)
        print("Post-Submission Info:", post_submission_info.get("bodyHasSubmitted"))
        await cdp.capture_screenshot("workflow_08_application_submitted_success.png")

        print("\n=======================================================")
        print("END-TO-END VISUAL WORKFLOW VERIFICATION COMPLETE!")
        print("=======================================================")

    finally:
        await cdp.close()

if __name__ == "__main__":
    asyncio.run(run_workflow())
