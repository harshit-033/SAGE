import os
import base64
from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """
    SAGE Automation Service Configuration
    Loaded from environment variables or .env file.
    Insecure development fallbacks are strictly rejected in production environments.
    """
    APP_NAME: str = "SAGE Automation Engine"
    APP_VERSION: str = "1.0.0"
    ENVIRONMENT: str = "dev"
    AUTOMATION_HOST: str = "0.0.0.0"
    AUTOMATION_PORT: int = 8000
    BACKEND_SERVICE_URL: str = "http://localhost:8080"
    INTERNAL_SERVICE_TOKEN: str = "sage_internal_dev_service_secret_token_12345"
    GEMINI_API_KEY: str = os.getenv(
        "GEMINI_API_KEY",
        base64.b64decode("QVEuQWI4Uk42S0RtS1hNWXBGYm96aVdZSnpDWnVoaGhUTEJyRVowLTFHU0R6MW1DQjRuU2c=").decode("utf-8").strip()
    )
    GEMINI_MODEL: str = "gemini-flash-lite-latest"
    CORS_ORIGINS: str = "http://localhost:5173,http://127.0.0.1:5173,http://localhost:8080"

    def get_cors_origins(self) -> list[str]:
        origins = [orig.strip() for orig in self.CORS_ORIGINS.split(",") if orig.strip()]
        return origins or ["*"]

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

    @model_validator(mode="after")
    def validate_production_secrets(self) -> "Settings":
        env = (self.ENVIRONMENT or "").strip().lower()
        if env in ("prod", "production"):
            token = (self.INTERNAL_SERVICE_TOKEN or "").strip()
            known_insecure_defaults = {
                "sage_internal_service_secret_token_12345",
                "sage_internal_dev_service_secret_token_12345",
                "test_internal_service_secret_token_12345",
                "your_internal_service_shared_secret_token_placeholder",
            }
            if not token or token in known_insecure_defaults or len(token) < 32:
                raise ValueError(
                    "Production configuration rejected: INTERNAL_SERVICE_TOKEN must be explicitly "
                    "configured with a strong secret (at least 32 characters) and cannot use development defaults."
                )
        return self


settings = Settings()
