/**
 * EVC Scheduling Engine — Developer Dashboard Frontend Logic
 * Standalone vanilla JavaScript client for testing the CP-SAT solver.
 */
document.addEventListener("DOMContentLoaded", () => {
  // State
  let scenarios = [];
  let inspections = [];
  let expertAvailabilities = [];

  // DOM Elements
  const scenarioContainer = document.getElementById("scenario-container");
  const inspectionsList = document.getElementById("inspections-list");
  const expertsList = document.getElementById("experts-list");
  const btnAddInspection = document.getElementById("btn-add-inspection");
  const btnAddExpert = document.getElementById("btn-add-expert");
  const btnSolve = document.getElementById("btn-solve");
  const btnSolveText = document.getElementById("btn-solve-text");
  const btnReset = document.getElementById("btn-reset");
  const timeLimitInput = document.getElementById("time-limit-input");

  // Metrics Elements
  const metricTotalInspections = document.getElementById("metric-total-inspections");
  const metricTotalAssignments = document.getElementById("metric-total-assignments");
  const metricScheduledCount = document.getElementById("metric-scheduled-count");
  const metricSolverStatus = document.getElementById("metric-solver-status");
  const metricSolveTime = document.getElementById("metric-solve-time");

  // Results Elements
  const resultsSummaryBadge = document.getElementById("results-summary-badge");
  const diagnosticsAlert = document.getElementById("diagnostics-alert");
  const diagnosticsTitle = document.getElementById("diagnostics-title");
  const diagnosticsMessage = document.getElementById("diagnostics-message");
  const tableWrapper = document.getElementById("table-wrapper");
  const scheduleTbody = document.getElementById("schedule-tbody");
  const timelineContainer = document.getElementById("timeline-container");
  const timelineChart = document.getElementById("timeline-chart");
  const rawJson = document.getElementById("raw-json");

  // Initial Scenario Load
  fetchScenarios();

  // Event Listeners
  btnAddInspection.addEventListener("click", () => {
    addInspection({
      inspectionId: "INSP-" + (inspections.length + 1),
      durationInDays: 3,
      requiredMemberCount: 2,
      earliestStartDate: "2026-11-10",
      latestStartDate: "2026-11-15",
      eligibleExpertIds: ["EXP-01", "EXP-02", "EXP-03"]
    });
    renderInspections();
    updateMetrics();
  });

  btnAddExpert.addEventListener("click", () => {
    addExpert({
      expertId: "EXP-" + String(expertAvailabilities.length + 1).padStart(2, '0'),
      availableDates: generateDateRange("2026-11-10", "2026-11-20")
    });
    renderExperts();
  });

  btnReset.addEventListener("click", () => {
    inspections = [];
    expertAvailabilities = [];
    renderInspections();
    renderExperts();
    resetResults();
    updateMetrics();
  });

  btnSolve.addEventListener("click", runSolver);

  // Helper Functions
  function generateDateRange(startStr, endStr) {
    const dates = [];
    const cur = new Date(startStr);
    const end = new Date(endStr);
    while (cur <= end) {
      dates.push(cur.toISOString().split("T")[0]);
      cur.setDate(cur.getDate() + 1);
    }
    return dates;
  }

  function fetchScenarios() {
    fetch("/dev/dashboard/api/scenarios")
      .then(res => {
        if (!res.ok) throw new Error("Status " + res.status);
        return res.json();
      })
      .then(data => {
        scenarios = data;
        renderScenarioButtons();
        if (scenarios.length > 0) {
          loadScenario(scenarios[0].id || scenarios[0].scenarioId);
        }
      })
      .catch(err => {
        console.warn("Could not fetch scenarios from backend, using fallback:", err);
        fallbackScenarios();
      });
  }

  function fallbackScenarios() {
    scenarios = [
      {
        id: "scenario-1",
        name: "1. Dynamic Durations & Sizes",
        description: "Feasible: 3 vs 4 days, 2 vs 3 members",
        request: {
          inspections: [
            {
              inspectionId: "INSP-ENGG-01",
              durationInDays: 3,
              requiredMemberCount: 2,
              earliestStartDate: "2026-11-10",
              latestStartDate: "2026-11-15",
              eligibleExpertIds: ["EXP-01", "EXP-02", "EXP-03"]
            },
            {
              inspectionId: "INSP-POLY-02",
              durationInDays: 4,
              requiredMemberCount: 3,
              earliestStartDate: "2026-11-10",
              latestStartDate: "2026-11-15",
              eligibleExpertIds: ["EXP-04", "EXP-05", "EXP-06", "EXP-07"]
            }
          ],
          expertAvailabilities: [
            { expertId: "EXP-01", availableDates: generateDateRange("2026-11-10", "2026-11-25") },
            { expertId: "EXP-02", availableDates: generateDateRange("2026-11-10", "2026-11-25") },
            { expertId: "EXP-03", availableDates: generateDateRange("2026-11-10", "2026-11-25") },
            { expertId: "EXP-04", availableDates: generateDateRange("2026-11-10", "2026-11-25") },
            { expertId: "EXP-05", availableDates: generateDateRange("2026-11-10", "2026-11-25") },
            { expertId: "EXP-06", availableDates: generateDateRange("2026-11-10", "2026-11-25") },
            { expertId: "EXP-07", availableDates: generateDateRange("2026-11-10", "2026-11-25") }
          ],
          timeLimitSeconds: 5
        }
      }
    ];
    renderScenarioButtons();
    loadScenario("scenario-1");
  }

  function renderScenarioButtons() {
    scenarioContainer.innerHTML = "";
    scenarios.forEach((sc, idx) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "btn-scenario";
      const scId = sc.id || sc.scenarioId;
      btn.dataset.scenarioId = scId;
      btn.innerHTML = `
        <span class="scenario-title">${escapeHtml(sc.name || sc.title)}</span>
        <span class="scenario-desc">${escapeHtml(sc.description)}</span>
      `;
      btn.addEventListener("click", () => {
        document.querySelectorAll(".btn-scenario").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        loadScenario(scId);
      });
      scenarioContainer.appendChild(btn);
    });

    if (scenarioContainer.firstElementChild) {
      scenarioContainer.firstElementChild.classList.add("active");
    }
  }

  function loadScenario(scenarioId) {
    const sc = scenarios.find(s => (s.id || s.scenarioId) === scenarioId);
    if (!sc || !sc.request) return;

    inspections = JSON.parse(JSON.stringify(sc.request.inspections || []));
    expertAvailabilities = JSON.parse(JSON.stringify(sc.request.expertAvailabilities || []));
    timeLimitInput.value = sc.request.timeLimitSeconds || 5;

    renderInspections();
    renderExperts();
    updateMetrics();
    resetResults();
  }

  function addInspection(insp) {
    inspections.push(insp);
  }

  function addExpert(exp) {
    expertAvailabilities.push(exp);
  }

  function renderInspections() {
    inspectionsList.innerHTML = "";
    inspections.forEach((insp, idx) => {
      const card = document.createElement("div");
      card.className = "item-card";
      card.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
          <strong style="font-size: 0.85rem; color: #fff;">#${idx + 1} Inspection</strong>
          <button type="button" class="btn btn-danger btn-sm" data-idx="${idx}">Remove</button>
        </div>
        <div class="form-grid-3">
          <div class="form-group">
            <label>Inspection ID</label>
            <input type="text" value="${escapeHtml(insp.inspectionId)}" data-field="inspectionId" data-idx="${idx}">
          </div>
          <div class="form-group">
            <label>Duration (days)</label>
            <input type="number" min="1" max="14" value="${insp.durationInDays}" data-field="durationInDays" data-idx="${idx}">
          </div>
          <div class="form-group">
            <label>Committee Size</label>
            <input type="number" min="1" max="10" value="${insp.requiredMemberCount}" data-field="requiredMemberCount" data-idx="${idx}">
          </div>
        </div>
        <div class="form-grid-2">
          <div class="form-group">
            <label>Earliest Start Date</label>
            <input type="date" value="${insp.earliestStartDate}" data-field="earliestStartDate" data-idx="${idx}">
          </div>
          <div class="form-group">
            <label>Latest Start Date</label>
            <input type="date" value="${insp.latestStartDate}" data-field="latestStartDate" data-idx="${idx}">
          </div>
        </div>
        <div class="form-group">
          <label>Eligible Experts (comma separated)</label>
          <input type="text" value="${(insp.eligibleExpertIds || []).join(', ')}" data-field="eligibleExpertIds" data-idx="${idx}" placeholder="e.g. EXP-01, EXP-02">
        </div>
      `;

      card.querySelector(".btn-danger").addEventListener("click", () => {
        inspections.splice(idx, 1);
        renderInspections();
        updateMetrics();
      });

      card.querySelectorAll("input").forEach(input => {
        input.addEventListener("input", (e) => {
          const field = e.target.dataset.field;
          const index = parseInt(e.target.dataset.idx, 10);
          if (field === "durationInDays" || field === "requiredMemberCount") {
            inspections[index][field] = parseInt(e.target.value, 10) || 1;
            updateMetrics();
          } else if (field === "eligibleExpertIds") {
            inspections[index][field] = e.target.value.split(",").map(s => s.trim()).filter(Boolean);
          } else {
            inspections[index][field] = e.target.value;
          }
        });
      });

      inspectionsList.appendChild(card);
    });
  }

  function renderExperts() {
    expertsList.innerHTML = "";
    expertAvailabilities.forEach((exp, idx) => {
      const card = document.createElement("div");
      card.className = "item-card";
      const datesStr = (exp.availableDates || []).join(", ");
      card.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
          <strong style="font-size: 0.85rem; color: #fff;">#${idx + 1} Expert</strong>
          <button type="button" class="btn btn-danger btn-sm" data-idx="${idx}">Remove</button>
        </div>
        <div class="form-grid-2">
          <div class="form-group">
            <label>Expert ID</label>
            <input type="text" value="${escapeHtml(exp.expertId)}" data-field="expertId" data-idx="${idx}">
          </div>
          <div class="form-group">
            <label>Total Available Days</label>
            <input type="text" readonly value="${(exp.availableDates || []).length} days" style="background: rgba(0,0,0,0.2); color: var(--text-muted);">
          </div>
        </div>
        <div class="form-group">
          <label>Available Dates (comma separated YYYY-MM-DD)</label>
          <textarea rows="2" data-field="availableDates" data-idx="${idx}" style="resize: vertical;">${datesStr}</textarea>
        </div>
      `;

      card.querySelector(".btn-danger").addEventListener("click", () => {
        expertAvailabilities.splice(idx, 1);
        renderExperts();
      });

      card.querySelector("input[data-field='expertId']").addEventListener("input", (e) => {
        const index = parseInt(e.target.dataset.idx, 10);
        expertAvailabilities[index].expertId = e.target.value.trim();
      });

      card.querySelector("textarea[data-field='availableDates']").addEventListener("change", (e) => {
        const index = parseInt(e.target.dataset.idx, 10);
        const parsed = e.target.value
          .split(",")
          .map(s => s.trim())
          .filter(s => /^\d{4}-\d{2}-\d{2}$/.test(s));
        expertAvailabilities[index].availableDates = parsed;
        renderExperts();
      });

      expertsList.appendChild(card);
    });
  }

  function updateMetrics() {
    metricTotalInspections.textContent = inspections.length;
    const totalReq = inspections.reduce((acc, curr) => acc + (parseInt(curr.requiredMemberCount, 10) || 0), 0);
    metricTotalAssignments.textContent = totalReq;
  }

  function resetResults() {
    metricScheduledCount.textContent = "0";
    metricSolverStatus.textContent = "IDLE";
    metricSolverStatus.className = "status-badge status-idle";
    metricSolveTime.textContent = "—";
    diagnosticsAlert.style.display = "none";
    tableWrapper.style.display = "none";
    timelineContainer.style.display = "none";
    resultsSummaryBadge.style.display = "none";
    rawJson.textContent = "{ \"status\": \"NO_RUN_YET\" }";
  }

  function runSolver() {
    // Validate inputs
    if (inspections.length === 0) {
      showError("Please add at least one inspection before solving.");
      return;
    }

    for (const insp of inspections) {
      if (!insp.inspectionId || !insp.inspectionId.trim()) {
        showError("All inspections must have a valid Inspection ID.");
        return;
      }
      if (!insp.durationInDays || insp.durationInDays <= 0) {
        showError(`Inspection ${insp.inspectionId} must have a positive duration.`);
        return;
      }
      if (!insp.requiredMemberCount || insp.requiredMemberCount <= 0) {
        showError(`Inspection ${insp.inspectionId} must have a positive member count.`);
        return;
      }
      if (!insp.earliestStartDate || !insp.latestStartDate) {
        showError(`Inspection ${insp.inspectionId} must specify both earliest and latest start dates.`);
        return;
      }
      if (insp.earliestStartDate > insp.latestStartDate) {
        showError(`Inspection ${insp.inspectionId}: Earliest start date (${insp.earliestStartDate}) cannot be after latest start date (${insp.latestStartDate}).`);
        return;
      }
    }

    const payload = {
      inspections: inspections.map(i => ({
        inspectionId: i.inspectionId.trim(),
        durationInDays: parseInt(i.durationInDays, 10),
        requiredMemberCount: parseInt(i.requiredMemberCount, 10),
        earliestStartDate: i.earliestStartDate,
        latestStartDate: i.latestStartDate,
        eligibleExpertIds: i.eligibleExpertIds || []
      })),
      expertAvailabilities: expertAvailabilities.map(e => ({
        expertId: e.expertId.trim(),
        availableDates: e.availableDates || []
      })),
      timeLimitSeconds: parseInt(timeLimitInput.value, 10) || 5
    };

    // UI Loading State
    btnSolve.disabled = true;
    btnSolveText.textContent = "Solving via OR-Tools CP-SAT...";
    metricSolverStatus.textContent = "RUNNING";
    metricSolverStatus.className = "status-badge status-idle";

    fetch("/dev/dashboard/api/schedule", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    })
      .then(res => res.json().then(data => ({ status: res.status, ok: res.ok, data })))
      .then(({ status, ok, data }) => {
        btnSolve.disabled = false;
        btnSolveText.textContent = "Solve Schedule (CP-SAT)";

        rawJson.textContent = JSON.stringify(data, null, 2);

        if (!ok) {
          showError(data.error || "Server returned HTTP " + status);
          return;
        }

        renderResults(data);
      })
      .catch(err => {
        btnSolve.disabled = false;
        btnSolveText.textContent = "Solve Schedule (CP-SAT)";
        showError("Network or solver error: " + err.message);
      });
  }

  function showError(msg) {
    diagnosticsAlert.style.display = "flex";
    diagnosticsAlert.className = "alert alert-danger";
    diagnosticsTitle.textContent = "Validation / Execution Error";
    diagnosticsMessage.textContent = msg;

    metricSolverStatus.textContent = "ERROR";
    metricSolverStatus.className = "status-badge status-invalid";
  }

  function renderResults(res) {
    metricScheduledCount.textContent = res.totalInspectionsScheduled != null ? res.totalInspectionsScheduled : (res.scheduledInspections || []).length;
    metricSolveTime.textContent = (res.solveDurationMs != null ? res.solveDurationMs : 0) + " ms";

    const status = res.status || res.solverStatus || "UNKNOWN";
    metricSolverStatus.textContent = status;

    if (status === "OPTIMAL" || status === "FEASIBLE") {
      metricSolverStatus.className = "status-badge status-optimal";
      diagnosticsAlert.style.display = "flex";
      diagnosticsAlert.className = "alert alert-success";
      diagnosticsTitle.textContent = `Schedule Generated (${status})`;
      diagnosticsMessage.textContent = res.explanation || "All inspection requests and committee constraints satisfied.";
    } else if (status === "INFEASIBLE") {
      metricSolverStatus.className = "status-badge status-infeasible";
      diagnosticsAlert.style.display = "flex";
      diagnosticsAlert.className = "alert alert-danger";
      diagnosticsTitle.textContent = "Schedule Infeasible";
      diagnosticsMessage.textContent = res.explanation || "The CP-SAT solver proved that no schedule satisfies all constraints.";
    } else {
      metricSolverStatus.className = "status-badge status-invalid";
      diagnosticsAlert.style.display = "flex";
      diagnosticsAlert.className = "alert alert-warning";
      diagnosticsTitle.textContent = `Solver Status: ${status}`;
      diagnosticsMessage.textContent = res.explanation || "No solution found within the allotted time limit.";
    }

    // Render table
    scheduleTbody.innerHTML = "";
    if (res.scheduledInspections && res.scheduledInspections.length > 0) {
      tableWrapper.style.display = "block";
      res.scheduledInspections.forEach(item => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td><strong>${escapeHtml(item.inspectionId)}</strong></td>
          <td>${item.durationInDays} days</td>
          <td>${item.requiredMemberCount} experts</td>
          <td>${item.scheduledStartDate}</td>
          <td>${item.scheduledEndDate}</td>
          <td><span style="color: var(--accent);">${(item.assignedExpertIds || []).join(", ")}</span></td>
          <td><span class="status-badge status-optimal">SCHEDULED</span></td>
        `;
        scheduleTbody.appendChild(tr);
      });

      renderTimeline(res.scheduledInspections);
    } else {
      tableWrapper.style.display = "none";
      timelineContainer.style.display = "none";
    }
  }

  function renderTimeline(scheduledItems) {
    if (!scheduledItems || scheduledItems.length === 0) {
      timelineContainer.style.display = "none";
      return;
    }

    timelineContainer.style.display = "block";
    timelineChart.innerHTML = "";

    // Find date bounds
    let allDates = [];
    scheduledItems.forEach(item => {
      if (item.occupiedDates && item.occupiedDates.length > 0) {
        allDates.push(...item.occupiedDates);
      } else {
        allDates.push(item.scheduledStartDate, item.scheduledEndDate);
      }
    });

    allDates = Array.from(new Set(allDates)).sort();
    if (allDates.length === 0) return;

    const minDate = new Date(allDates[0]);
    const maxDate = new Date(allDates[allDates.length - 1]);

    // Build timeline full continuous days
    const horizonDays = [];
    const cur = new Date(minDate);
    while (cur <= maxDate) {
      horizonDays.push(cur.toISOString().split("T")[0]);
      cur.setDate(cur.getDate() + 1);
    }

    const colWidth = 52; // px per day

    // Header row
    const headerRow = document.createElement("div");
    headerRow.className = "timeline-header";
    headerRow.style.width = (180 + horizonDays.length * colWidth) + "px";

    const labelCol = document.createElement("div");
    labelCol.className = "timeline-label";
    labelCol.textContent = "Inspection";
    headerRow.appendChild(labelCol);

    horizonDays.forEach(d => {
      const col = document.createElement("div");
      col.className = "timeline-day-col";
      col.style.width = colWidth + "px";
      col.style.minWidth = colWidth + "px";
      const parts = d.split("-");
      col.innerHTML = `<div>${parts[1]}/${parts[2]}</div><div style="font-size:0.65rem; color:var(--text-muted);">${getDayName(d)}</div>`;
      headerRow.appendChild(col);
    });
    timelineChart.appendChild(headerRow);

    // Rows for each inspection
    scheduledItems.forEach((item, idx) => {
      const row = document.createElement("div");
      row.className = "timeline-row";
      row.style.width = (180 + horizonDays.length * colWidth) + "px";

      const rowLabel = document.createElement("div");
      rowLabel.className = "timeline-label";
      rowLabel.title = item.inspectionId;
      rowLabel.textContent = item.inspectionId;
      row.appendChild(rowLabel);

      const track = document.createElement("div");
      track.className = "timeline-track";
      track.style.width = (horizonDays.length * colWidth) + "px";

      // Calculate bar start and width based on inclusive scheduled calendar span
      const startIndex = horizonDays.indexOf(item.scheduledStartDate);
      const endIndex = horizonDays.indexOf(item.scheduledEndDate);
      const spanDays = (startIndex >= 0 && endIndex >= startIndex)
        ? (endIndex - startIndex + 1)
        : (item.durationInDays || 1);

      if (startIndex >= 0) {
        const bar = document.createElement("div");
        bar.className = "timeline-bar" + (idx % 2 === 1 ? " insp-alt" : "");
        bar.style.left = (startIndex * colWidth) + "px";
        bar.style.width = (spanDays * colWidth - 4) + "px";
        bar.title = `${item.inspectionId} (${item.scheduledStartDate} to ${item.scheduledEndDate}, ${item.durationInDays} non-Sunday days): ${(item.assignedExpertIds || []).join(", ")}`;
        bar.textContent = `${item.durationInDays}d: ${(item.assignedExpertIds || []).join(", ")}`;
        track.appendChild(bar);
      }

      row.appendChild(track);
      timelineChart.appendChild(row);
    });
  }

  function getDayName(dateStr) {
    const d = new Date(dateStr + "T00:00:00");
    const names = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    return names[d.getDay()] || "";
  }

  function escapeHtml(str) {
    if (!str) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
});
