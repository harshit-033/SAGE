from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """
    SAGE Automation Service Configuration
    Loaded from environment variables or .env file with safe defaults.
    """
    APP_NAME: str = "SAGE Automation Engine"
    APP_VERSION: str = "1.0.0"
    ENVIRONMENT: str = "dev"
    AUTOMATION_HOST: str = "0.0.0.0"
    AUTOMATION_PORT: int = 8000
    BACKEND_SERVICE_URL: str = "http://localhost:8080"
    INTERNAL_SERVICE_TOKEN: str = "sage_internal_service_secret_token_12345"

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )


settings = Settings()
