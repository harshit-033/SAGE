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
      <div style={{ display: 'flex', flex: 1, minHeight: 'calc(100vh - 180px)' }}>
        <Sidebar />
        <main
          style={{
            flex: 1,
            padding: '1.5rem 2rem',
            overflowY: 'auto',
            backgroundColor: 'var(--bg-page)',
          }}
          id="main-content"
          role="main"
        >
          <div style={{ maxWidth: 'var(--container-max-width)', margin: '0 auto' }}>
            <Outlet />
          </div>
        </main>
      </div>

      {/* Restrained Prototype Disclaimer Footer */}
      <footer className="gov-footer" role="contentinfo">
        <div className="gov-footer-inner">
          <div className="gov-footer-prototype-alert">
            <strong>NOTICE &amp; DISCLAIMER:</strong> SAGE is an independent prototype developed for the Smart India Hackathon
            (Problem Statement 26262: <em>“Integrated AICTE Approval Workflow Automation, Compliance Monitoring and Alert Management System”</em>).
            It is not the official AICTE production portal and does not connect to live government databases.
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
            <div>
              &copy; 2026 SAGE Platform &bull; Built with Spring Boot 3, PostgreSQL, FastAPI &amp; React.
            </div>
            <div style={{ display: 'flex', gap: '1.25rem' }}>
              <span>Phase 2: Secure Identity &amp; Tenancy Management</span>
              <span>Accessibility: WCAG 2.2 AA Aligned</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}
