import React, { useState } from 'react'

export const TopGovBar: React.FC = () => {
  const [fontSize, setFontSize] = useState<'small' | 'normal' | 'large'>('normal')
  const [highContrast, setHighContrast] = useState(false)

  const handleFontChange = (size: 'small' | 'normal' | 'large') => {
    setFontSize(size)
    document.documentElement.dataset.fontSize = size
  }

  const handleContrastToggle = () => {
    const next = !highContrast
    setHighContrast(next)
    if (next) {
      document.documentElement.dataset.contrast = 'high'
    } else {
      delete document.documentElement.dataset.contrast
    }
  }

  return (
    <>
      <div className="gov-tricolor-strip" />
      <div className="gov-top-bar" role="region" aria-label="Government Portal Accessibility Options">
        <div className="gov-top-bar-inner">
          <div className="gov-top-bar-left">
            <span>GOVERNMENT OF INDIA</span>
            <span style={{ opacity: 0.4 }}>|</span>
            <span>ALL INDIA COUNCIL FOR TECHNICAL EDUCATION</span>
            <span className="gov-portal-badge" title="Independent Prototype for SIH 2026">
              SIH 2026 PROTOTYPE
            </span>
          </div>
          <div className="gov-top-bar-right">
            <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Accessibility:</span>
            <div style={{ display: 'flex', gap: '0.2rem' }}>
              <button
                type="button"
                className="accessibility-btn"
                onClick={() => handleFontChange('small')}
                aria-label="Decrease text size"
                title="Decrease font size"
                style={{ fontWeight: fontSize === 'small' ? 800 : 500 }}
              >
                A-
              </button>
              <button
                type="button"
                className="accessibility-btn"
                onClick={() => handleFontChange('normal')}
                aria-label="Standard text size"
                title="Standard font size"
                style={{ fontWeight: fontSize === 'normal' ? 800 : 500 }}
              >
                A
              </button>
              <button
                type="button"
                className="accessibility-btn"
                onClick={() => handleFontChange('large')}
                aria-label="Increase text size"
                title="Increase font size"
                style={{ fontWeight: fontSize === 'large' ? 800 : 500 }}
              >
                A+
              </button>
            </div>
            <button
              type="button"
              className="accessibility-btn"
              onClick={handleContrastToggle}
              aria-label="Toggle high contrast"
              title="Toggle high contrast display"
            >
              {highContrast ? 'Standard Mode' : 'High Contrast'}
            </button>
          </div>
        </div>
      </div>
    </>
  )
}
