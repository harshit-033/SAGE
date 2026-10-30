import React from 'react'
import { Outlet } from 'react-router-dom'
import { TopGovBar } from '../common/TopGovBar'
import { Navbar } from './Navbar'
import { Sidebar } from './Sidebar'

export const AppShell: React.FC = () => {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--bg-page)' }}>
      {/* Top Accessibility & Tricolor Bar */}
      <TopGovBar />

      {/* Main Branding Header & Navigation */}
      <Navbar />

      {/* Main App Layout */}
      <div className="app-shell-container">
        <Sidebar />
        <main
          className="app-shell-main"
          id="main-content"
          role="main"
        >
          <div className="app-shell-content">
            <Outlet />
          </div>
        </main>
      </div>

      {/* Official Government Enterprise Portal Footer */}
      <footer className="gov-footer" role="contentinfo">
        <div className="gov-footer-inner">
          <div className="gov-footer-prototype-alert">
            <strong>STATUTORY NOTICE:</strong> System for Approval Governance and Evaluation (SAGE) operates under AICTE Approval Process Handbook (APH 2024–27) statutory guidelines. All institutional disclosures, digital representations, and verification findings are subject to Section 10 of the AICTE Act, 1987.
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
            <div>
              &copy; 2026 All India Council for Technical Education (AICTE), New Delhi. All Rights Reserved.
            </div>
            <div style={{ display: 'flex', gap: '1.25rem', fontSize: '0.78rem' }}>
              <span>Ministry of Education &bull; Government of India</span>
              <span>Accessibility: WCAG 2.2 AA Aligned</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
