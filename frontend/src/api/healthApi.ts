export interface ServiceHealth {
  status: string
  service: string
  version?: string
  database?: { status: string; details?: string; error?: string }
  backend?: { url: string; status: string }
  timestamp: string
}

export const healthApi = {
  getBackendHealth: async (): Promise<ServiceHealth> => {
    const res = await fetch('/api/v1/health')
    return res.json()
  },

  getBackendReady: async (): Promise<ServiceHealth> => {
    const res = await fetch('/api/v1/health/ready')
    return res.json()
  },

  getAutomationHealth: async (): Promise<ServiceHealth> => {
    const res = await fetch('/automation-api/v1/health')
    return res.json()
  },

  getAutomationReady: async (): Promise<ServiceHealth> => {
    const res = await fetch('/automation-api/v1/health/ready')
    return res.json()
  },
}
